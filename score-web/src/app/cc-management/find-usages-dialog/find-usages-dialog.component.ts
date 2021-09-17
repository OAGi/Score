import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FindUsagesDialogService} from './domain/find-usages-dialog.service';
import {
  AccFlatNode,
  AsccpFlatNode,
  BccpFlatNode,
  BdtFlatNode,
  CcFlatNode,
  CcFlatNodeFlattener,
  VSCcTreeDataSource
} from '../domain/cc-flat-tree';
import {DataSourceSearcher, getKey, next, VSFlatTreeControl} from '../../common/flat-tree';
import {CcNodeService} from '../domain/core-component-node.service';
import {forkJoin} from 'rxjs';
import {CcGraph, CcGraphNode, CcNodeDetail} from '../domain/core-component-node';
import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';

class FindUsagesCcFlatNodeFlattener extends CcFlatNodeFlattener {

  toAsccpNode(asccpNode: CcGraphNode, parent: CcFlatNode) {
    const node = new AsccpFlatNode(asccpNode);
    node.state = asccpNode.state;
    node.deprecated = asccpNode.deprecated;
    node.level = parent.level + 1;
    node.parent = parent;
    node.isCycle = this.detectCycle(node);
    return node;
  }

  toBccpNode(bccpNode: CcGraphNode, parent: CcFlatNode) {
    const bdtNode = next(this._ccGraph, bccpNode);
    const node = new BccpFlatNode(bccpNode, bdtNode);
    node.deprecated = bccpNode.deprecated || bdtNode.deprecated;
    node.state = bccpNode.state;
    node.level = parent.level + 1;
    node.parent = parent;
    return node;
  }

  flatten(): CcFlatNode[] {
    return super.flatten().slice(1).map(e => {
      e.level -= 1;
      return e;
    });
  }

  getChildren(node: CcFlatNode): CcFlatNode[] {
    const nodes = this._ccGraph.graph.nodes;
    const edges = this._ccGraph.graph.edges;

    if (node instanceof BccpFlatNode) {
      if ((node as BccpFlatNode).bccNode && (node as BccpFlatNode).bccNode.entityType === 'Attribute') {
        return [];
      }
    }

    const edge = edges[getKey(node)];
    const targets = (!!edge) ? edge.targets : [];
    if (!targets || targets.length === 0) {
      return [];
    }

    let children = [];

    if (node instanceof BdtFlatNode) {
      targets.forEach(target => {
        children.push(this.toBdtScNode(nodes[target], node));
      });
      return children;
    }

    targets.forEach(target => {
      if (target.startsWith('ACC-')) {
        children.push(this.toAccNode(nodes[target], node));
      } else if (target.startsWith('ASCCP-')) {
        const asccpNode = this.toAsccpNode(nodes[target], node);
        if (asccpNode.isUserExtensionGroup) {
          const uegAccNode = this.getChildren(asccpNode)[0];
          children.push(...this.getChildren(uegAccNode).map(e => {
            e.level = node.level + 1;
            e.parent = node;
            return e;
          }));
        } else {
          children.push(asccpNode);
        }
      } else if (target.startsWith('BCCP-')) {
        children.push(this.toBccpNode(nodes[target], node));
      } else if (target.startsWith('BDT-')) {
        const bdtScEdges = edges[target];
        if (bdtScEdges) {
          bdtScEdges.targets.map(e => nodes[e]).filter(e => e.cardinalityMax > 0).forEach(e => {
            children.push(this.toBdtScNode(e, node));
          });
        }
      }
    });
    return children;
  }
}

@Component({
  selector: 'score-find-usages-dialog',
  templateUrl: './find-usages-dialog.component.html',
  styleUrls: ['./find-usages-dialog.component.css']
})
export class FindUsagesDialogComponent implements OnInit {

  title: string;
  _innerHeight: number = window.innerHeight;
  paddingPixel = 12;
  isUpdating: boolean;

  dataSource: VSCcTreeDataSource<CcFlatNode>;
  treeControl: VSFlatTreeControl<CcFlatNode> = new VSFlatTreeControl<CcFlatNode>();

  selectedNode: CcFlatNode;
  cursorNode: CcFlatNode;

  hasBasedAcc: boolean;

  @ViewChild('virtualScroll', {static: true}) public virtualScroll: CdkVirtualScrollViewport;
  virtualScrollItemSize: number = 33;

  get minBufferPx(): number {
    return 20 * this.virtualScrollItemSize;
  }

  get maxBufferPx(): number {
    return 20 * 20 * this.virtualScrollItemSize;
  }

  constructor(private dialogRef: MatDialogRef<FindUsagesDialogComponent>,
              private service: FindUsagesDialogService,
              private ccNodeService: CcNodeService,
              @Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit(): void {
    forkJoin([
      this.service.findUsages(this.data.type, this.data.manifestId)
    ]).subscribe(([findUsagesResp]) => {
      const edge = findUsagesResp.graph.edges[findUsagesResp.rootNodeKey];
      let sizeOfNodes: number = (!!edge) ? edge.targets.length : 0;
      if (!sizeOfNodes) {
        this.title = 'Nothing found';
      } else {
        this.title = 'Where Used: ' + sizeOfNodes + ' result' + ((sizeOfNodes === 1) ? '' : 's');
      }

      const ccGraph = new CcGraph();
      ccGraph.accManifestId = this.data.manifestId;
      ccGraph.graph = findUsagesResp.graph;

      const flattener = new FindUsagesCcFlatNodeFlattener(ccGraph, this.data.type, this.data.manifestId);
      setTimeout(() => {
        const nodes = flattener.flatten();
        this.dataSource = new VSCcTreeDataSource(this.treeControl, nodes, this.ccNodeService, []);
        this.isUpdating = false;
      }, 0);
    });
  }

  getLevel = (node: CcFlatNode) => node.level;
  isExpandable = (node: CcFlatNode) => node.expandable;
  hasChild = (_: number, _nodeData: CcFlatNode) => _nodeData.expandable;

  onResize(event) {
    this._innerHeight = window.innerHeight;
  }

  get innerHeight(): number {
    return this._innerHeight - 180;
  }

  onClick(node: CcFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }
  }

  toggle(node: CcFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.treeControl.toggle(node);
  }

  keyNavigation($event: KeyboardEvent) {
    if ($event.key === 'ArrowLeft' || $event.key === 'ArrowRight') {
      this.treeControl.toggle(this.cursorNode);
    } else if ($event.key === 'Enter') {
      this.onClick(this.cursorNode);
    }
    $event.preventDefault();
    $event.stopPropagation();
  }

  openCoreComponent(node: CcFlatNode) {
    window.open('/core_component/' + node.type.toLowerCase() + '/' + node.manifestId, '_blank');
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
