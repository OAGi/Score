import {BehaviorSubject, merge, Observable, Subscriber} from 'rxjs';
import {
  CcAccNode,
  CcAsccpNode,
  CcBccpNode,
  CcFlatNode2,
  CcGraph,
  CcGraphEdge,
  CcGraphNode,
  CcNode,
  UserExtensionGroup
} from '../domain/core-component-node';
import {FlatTreeControl} from '@angular/cdk/tree';
import {CollectionViewer, SelectionChange} from '@angular/cdk/collections';
import {finalize, map} from 'rxjs/operators';
import {Injectable} from '@angular/core';
import {CcNodeService} from '../domain/core-component-node.service';
import {sha256} from '../../common/utility';
import {SearchOptionsService} from '../search-options-dialog/domain/search-options-service';
import {SearchOptions} from '../search-options-dialog/domain/search-options';

@Injectable()
export class DynamicCcDataSource {

  searchKeyword = '';
  inputKeyword = '';
  searchResult = [];
  searchIndex = 0;
  isSearching = false;
  searchPrefix = '';
  searchOptions: SearchOptions;

  dataChange: BehaviorSubject<CcFlatNode2[]> = new BehaviorSubject<CcFlatNode2[]>([]);
  public ccGraph: CcGraph;

  get data(): CcFlatNode2[] {
    return this.dataChange.value;
  }

  set data(value: CcFlatNode2[]) {
    this.treeControl.dataNodes = value;
    this.dataChange.next(value);
  }

  constructor(public treeControl: FlatTreeControl<CcFlatNode2>,
              public service: CcNodeService,
              public searchOptionsService: SearchOptionsService) {
    this.resetSearch();
  }

  getName(node: CcGraphNode): string {
    if (node === undefined) {
      return '';
    }
    switch (node.type.toUpperCase()) {
      case 'ACC':
        return node.den;
      case 'BDT_SC':
        return node.propertyTerm + '. ' + node.representationTerm;
      default:
        return node.propertyTerm;
    }
  }

  _isAssociation(nodeId: string): boolean {
    if (nodeId.startsWith('ASCC') || nodeId.startsWith('BCC') || nodeId.startsWith('BDT')) {
      return true;
    }
    return false;
  }

  _isUserExtensionGroup(nodeId: string): boolean {
    if (!nodeId.startsWith('ASCC')) {
      return false;
    }
    const edge = this.ccGraph.graph.edges[nodeId];
    if (edge && edge.targets.length === 1) {
      const node = this.ccGraph.graph.nodes[edge.targets[0]];
      if (node.type === 'ASCCP' && node.propertyTerm.endsWith('User Extension Group')) {
        return true;
      }
    }
    return false;
  }

  _getTargets(targets: string[], node: CcGraphNode) {
    if (node.type === 'BDT') {
      return targets.filter(target => {
        return this.ccGraph.graph.nodes[target].cardinalityMax > 0;
      });
    }
    const nodeId = node.type + '-' + node.manifestId;
    if (this._isUserExtensionGroup(nodeId)) {
      return this._getUserExtensionGroupTargets(nodeId);
    }
    return targets;
  }

  _getManifestIdByEdge(nodeId: string) {
    return parseInt(nodeId.replace(/[^0-9]/g, ''), 10);
  }

  _getAllExtensionEdge(allExtensionId: string): string[] {
    // acc(Extension) -> ascc -> asccp -> acc(User Extension Group) -> ascc/bcc
    const edge: CcGraphEdge = this.ccGraph.graph.edges[allExtensionId];
    if (edge === undefined) {
      return [];
    } else {
      let targets = [];
      edge.targets.map(e => {
        if (this._isAssociation(e)) {
          const uegAsccp = this.ccGraph.graph.edges[e];
          if (!(uegAsccp && uegAsccp.targets && uegAsccp.targets.length > 0)) {
            return [];
          }
          const uegAcc = this.ccGraph.graph.edges[uegAsccp.targets[0]];
          if (!(uegAcc && uegAcc.targets && uegAcc.targets.length > 0)) {
            return [];
          }
          if (this.ccGraph.graph.edges[uegAcc.targets[0]] === undefined) {
            return [];
          }
          this.ccGraph.graph.edges[uegAcc.targets[0]].targets.map(association => {
            for (const t of this.ccGraph.graph.edges[association].targets) {
              this.ccGraph.graph.nodes[t].associationManifestId = this._getManifestIdByEdge(association);
            }
            targets = targets.concat(this.ccGraph.graph.edges[association].targets);
          });
        } else {
          targets.push(e);
        }
      });
      return targets;
    }
  }

  _getUserExtensionGroupTargets(extensionAssociationId: string): string[] {
    const asccpEdge = this.ccGraph.graph.edges[extensionAssociationId];
    if (!(asccpEdge && asccpEdge.targets && asccpEdge.targets.length === 1)) {
      return [];
    }
    const accEdge = this.ccGraph.graph.edges[asccpEdge.targets[0]];
    if (!(accEdge && accEdge.targets && accEdge.targets.length === 1)) {
      return [];
    }
    return this._getEdge(accEdge.targets[0]);
  }

  _getEdge(id: string): string[] {
    const edge: CcGraphEdge = this.ccGraph.graph.edges[id];
    let targets = [];
    if (edge === undefined) {
      return targets;
    }
    const node = this.ccGraph.graph.nodes[id];
    if (node.objectClassTerm === 'All Extension') {
      return this._getAllExtensionEdge(id);
    }

    for (const child of edge.targets) {
      if (this._isAssociation(child)) {
        const childEdges = this.ccGraph.graph.edges[child];
        if (childEdges) {
          const childNode = this.ccGraph.graph.nodes[child];
          childEdges.targets.forEach(target => {
            this.ccGraph.graph.nodes[target].associationManifestId = this._getManifestIdByEdge(child);
            this.ccGraph.graph.nodes[target].entityType = childNode.entityType;
          });
          targets = targets.concat(this._getTargets(childEdges.targets, childNode));
        }
      } else {
        targets.push(child);
      }
    }
    return targets;
  }

  getChildren(node: CcFlatNode2): CcFlatNode2[] {
    if (node.children !== undefined) {
      return node.children;
    }

    const targets = this._getEdge(node.id);
    if (targets.length > 0) {
      let pos = 0;
      node.children = targets.map(target => {
        const ccGraphNode = this.ccGraph.graph.nodes[target];
        const ccNode = this.toCcNode(ccGraphNode);
        const childType = ccNode.type.toUpperCase();
        const dynamicCcFlatNode2 = new CcFlatNode2(this, ccNode,
          node.level + 1, (childType === 'ASCCP' || childType === 'BCCP') ? pos++ : 0,
          false, false,
          node.path + '>' + childType + '-' + ccNode.manifestId);
        dynamicCcFlatNode2.item.hasChild = this.isExpandable(childType + '-' + ccNode.manifestId);
        return dynamicCcFlatNode2;
      });
    } else {
      node.children = [];
    }

    return node.children;
  }

  toCcNode(ccGraphNode: CcGraphNode) {
    let item;
    switch (ccGraphNode.type.toUpperCase()) {
      case 'ACC':
        item = new CcAccNode();
        item.group = ccGraphNode.componentType.endsWith('Group');

        break;

      case 'ASCCP':
        item = new CcAsccpNode();
        item.asccManifestId = ccGraphNode.associationManifestId;

        break;

      case 'BCCP':
        item = new CcBccpNode();
        item.bccManifestId = ccGraphNode.associationManifestId;
        item.attribute = (ccGraphNode.entityType === 'Attribute');

        break;

      default:
        item = new CcNode();
        break;
    }

    item.manifestId = ccGraphNode.manifestId;
    item.name = this.getName(ccGraphNode);
    item.type = ccGraphNode.type.toUpperCase();
    item.state = ccGraphNode.state;
    item.guid = ccGraphNode.guid;

    return item;
  }

  isExpandable(nodeId: string): boolean {
    return this._getEdge(nodeId).length > 0;
  }

  connect(collectionViewer: CollectionViewer): Observable<CcFlatNode2[]> {
    // tslint:disable-next-line:no-non-null-assertion
    this.treeControl.expansionModel.changed!.subscribe(change => {
      if ((change as SelectionChange<CcFlatNode2>).added ||
        (change as SelectionChange<CcFlatNode2>).removed) {
        this.handleTreeControl(change as SelectionChange<CcFlatNode2>);
      }
    });

    return merge(collectionViewer.viewChange, this.dataChange).pipe(map(() => this.data));
  }

  /** Handle expand/collapse behaviors */
  handleTreeControl(change: SelectionChange<CcFlatNode2>) {
    if (change.added) {
      change.added.forEach((node) => this.toggleNode(node, true));
    }
    if (change.removed) {
      change.removed.reverse().forEach((node) => this.toggleNode(node, false));
    }
  }

  /**
   * Toggle the node, remove from display list
   */
  toggleNode(node: CcFlatNode2, expand: boolean): CcFlatNode2[] {
    const children = this.getChildren(node);
    const index = this.data.indexOf(node);
    if (!children || index < 0) { // If no children, or cannot find the node, no op
      return;
    }

    if (expand) {
      this.data.splice(index + 1, 0, ...children);
    } else {
      let count = 0;
      for (let i = index + 1; i < this.data.length && this.data[i].level > node.level; i++, count++) {
      }
      this.data.splice(index + 1, count);
      node.children = undefined;
    }

    // notify the change
    this.dataChange.next(this.data);
    return children;
  }

  expandPath(path: string, highlight: boolean, scroll: boolean) {
    const paths = path.split('>');
    let found = null;
    let tracking = '';
    for (const item of paths) {
      tracking += tracking.length > 0 ? '>' + item : item;
      for (const node of this.data) {
        if (node.path === tracking) {
          found = node;
          if (!this.treeControl.isExpanded(node)) {
            this.treeControl.expand(node);
          }
          break;
        }
      }
    }

    const itemToScrollTo = document.getElementById(tracking);
    if (highlight) {
      const focusedElem = document.getElementsByClassName('search-highlight');
      for (let i = 0; i < focusedElem.length; i++) {
        focusedElem.item(i).removeAttribute('class');
      }
      if (itemToScrollTo) {
        const mark = itemToScrollTo.getElementsByTagName('mark');
        if (mark.length > 0) {
          mark[0].setAttribute('class', 'search-highlight');
        } else {
          // waiting for highlight filter applied;
          setTimeout(() => {
            mark[0].setAttribute('class', 'search-highlight');
          }, 500);
        }
      }
    }

    if (scroll && itemToScrollTo) {
      // @ts-ignore
      itemToScrollTo.scrollIntoViewIfNeeded(false);
    }

    return found;
  }

  traverse(ccGraphNode: CcGraphNode, callback: (node: CcGraphNode, paths?: (CcGraphNode[])) => void, paths?: (CcGraphNode[])) {
    if (!paths) {
      paths = [ccGraphNode];
    }

    callback(ccGraphNode, paths);

    const key = ccGraphNode.type.toUpperCase() + '-' + ccGraphNode.manifestId;
    const edges = this.ccGraph.graph.edges[key];

    const children = (edges) ? edges.targets.map(e => this.ccGraph.graph.nodes[e]) : [];
    children.forEach(child => {
      this.traverse(child, callback, [...paths, child]);
    });
  }

  updateGraph(ccGraph: CcGraph, rootNode: CcFlatNode2) {
    this.ccGraph = ccGraph;
    this.data = [rootNode, ];
  }

  type(node: CcFlatNode2) {
    const nodeItem: CcNode = node.item;
    let typeStr = nodeItem.type.toUpperCase();
    if (typeStr === 'ACC') {
      const accNode: CcAccNode = nodeItem as CcAccNode;
      if (accNode.group) {
        typeStr = typeStr + '-GROUP';
      }
    } else if (typeStr === 'BCCP') {
      const bccpNode: CcBccpNode = nodeItem as CcBccpNode;
      if (bccpNode.attribute) {
        typeStr = typeStr + '-ATTRIBUTE';
      }
    }
    return typeStr;
  }

  getParent(node: CcFlatNode2): CcFlatNode2 {
    const currentLevel = node.level;
    const startIndex = this.data.indexOf(node) - 1;

    for (let i = startIndex; i >= 0; i--) {
      const currentNode = this.data[i];

      if (currentNode.level < currentLevel) {
        return currentNode;
      }
    }
  }

  getParents(node: CcFlatNode2): CcFlatNode2[] {
    if (node.level < 1) {
      return [];
    }
    const parents: CcFlatNode2[] = [];
    while (this.getParent(node).level !== 0) {
      node = this.getParent(node);
      parents.push(node);
    }
    parents.push(this.getParent(node));
    return parents;
  }

  resetSearch() {
    this.inputKeyword = undefined;
    this.searchKeyword = undefined;
    this.searchResult = [];
    this.searchIndex = 0;
    this.isSearching = false;
    this.searchPrefix = '';
    this.searchOptions = this.searchOptionsService.loadOptions();
  }

  toPathString(paths: (CcGraphNode[])): string {
    return paths.filter((n: CcGraphNode) => {
      const type = n.type.toUpperCase();
      return !(type === 'ASCC' || type === 'BCC' || type === 'BDT');
    }).map(n => n.type.toUpperCase() + '-' + n.manifestId).join('>');
  }

  toHashPath(pathString: string): string {
    return sha256(pathString);
  }

  search(type: string, manifestId: number, keyword: string): Observable<string[]> {
    return new Observable((subscriber: Subscriber<string[]>) => {
      const searchContextNode = this.ccGraph.graph.nodes[type.toUpperCase() + '-' + manifestId];
      const queryTerm = keyword.toLowerCase().trim();

      setTimeout(() => {
        const res = [];

        this.traverse(searchContextNode, (node, paths?: (CcGraphNode[])) => {
          let name = this.getName(node);
          if (!name) {
            return;
          }

          if (node.type === 'BDT_SC') {
            if (this.searchOptions.excludeDataTypeSupplementaryComponents) {
              return;
            }
            if (node.cardinalityMax === 0) {
              return;
            }
          }

          name = name.toLowerCase().trim();
          if (name.indexOf(queryTerm) >= 0) {
            res.push(this.toPathString(paths));
          }
        });

        subscriber.next(res);
        subscriber.complete();
      }, 0);
    });
  }

  treeSearch(inputKeyword, selectedNode: CcFlatNode2, backward?: boolean, force?: boolean) {
    if (this.isSearching) {
      return;
    }

    if (!inputKeyword || inputKeyword.length === 0) {
      this.resetSearch();
      return;
    }

    if (this.searchKeyword !== inputKeyword || force) {
      this.isSearching = true;
      this.search(selectedNode.item.type, selectedNode.item.manifestId, inputKeyword).pipe(
        finalize(() => {
          this.isSearching = false;
        })
      ).subscribe((paths: string[]) => {
        this.searchPrefix = '';
        if (selectedNode.level !== 0) {
          const parents = this.getParents(selectedNode);
          for (const parent of parents.reverse()) {
            this.searchPrefix += parent.id + '>';
          }
        }

        this.searchResult = paths;
        this.searchKeyword = inputKeyword;
        if (this.searchResult.length > 0) {
          this.searchIndex = 0;
          this.expandPath(this.searchPrefix + this.searchResult[this.searchIndex], true, true);
        }
      });
    } else {
      if (backward) {
        this.searchIndex -= 1;
      } else {
        this.searchIndex += 1;
      }
      if (this.searchResult.length <= this.searchIndex) {
        this.searchIndex = 0;
      }
      if (this.searchIndex < 0) {
        this.searchIndex = this.searchResult.length - 1;
      }
      this.expandPath(this.searchPrefix + this.searchResult[this.searchIndex], true, true);
    }
  }

  expandAll(node: CcFlatNode2, depth?: number) {
    depth = depth || node.level + 3;

    const nodes = [node,];
    while (nodes.length > 0) {
      const node = nodes.shift();
      if (!node) {
        continue;
      }
      if (node.level >= depth) {
        continue;
      }
      if (!node.expandable) {
        continue;
      }
      if (!this.treeControl.isExpanded(node)) {
        this.treeControl.expand(node);
      }
      nodes.splice(nodes.length - 1, 0, ...node.children);
    }
  }

  collapseAll(node: CcFlatNode2) {
    if (node.expandable) {
      if (this.treeControl.isExpanded(node)) {
        this.treeControl.collapse(node);
      }
    }
  }

  pathBreadcrumbs(node?: CcFlatNode2) {
    if (!node) {
      return [];
    }

    const result = [];
    let track = '';
    for (const path of node.path.split('>')) {
      track += path;
      result.push({
        path: track,
        name: this.getName(this.ccGraph.graph.nodes[path])
      });
      track += '>';
    }
    return result;
  }

  scrollBreadcrumb() {
    const breadcrumbs = document.getElementById('tree-breadcrumbs-wrap');
    if (breadcrumbs.scrollWidth > breadcrumbs.clientWidth) {
      breadcrumbs.scrollLeft = breadcrumbs.scrollWidth - breadcrumbs.clientWidth;
      breadcrumbs.classList.add('inner-box');
    } else {
      breadcrumbs.scrollLeft = 0;
      breadcrumbs.classList.remove('inner-box');
    }
    return '';
  }
}
