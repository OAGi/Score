import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FindUsagesDialogService} from './domain/find-usages-dialog.service';
import {
  AsccpFlatNode,
  BccpFlatNode,
  CcFlatNode,
  CcFlatNodeDatabase,
  CcFlatNodeDataSource,
  DtFlatNode
} from '../domain/cc-flat-tree';
import {getKey, next} from '../../common/flat-tree';
import {CcNodeService} from '../domain/core-component-node.service';
import {forkJoin} from 'rxjs';
import {CcGraph, CcGraphNode} from '../domain/core-component-node';
import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';


class FindUsagesCcFlatNodeDatabase<T extends CcFlatNode> extends CcFlatNodeDatabase<T> {
  toAsccpNode(asccpNode: CcGraphNode, parent: CcFlatNode) {
    const node = new AsccpFlatNode(asccpNode);
    node.state = asccpNode.state;
    node.deprecated = asccpNode.deprecated;
    node.level = parent.level + 1;
    node.parent = parent;
    node.isCycle = this.detectCycle(node);
    return node;
  }

  toBccpNode(bccpNode: CcGraphNode, parent: CcFlatNode, bdtNode?: CcGraphNode) {
    if (!bdtNode) {
      bdtNode = next(this._ccGraph, bccpNode);
    }
    const node = new BccpFlatNode(bccpNode, bdtNode);
    node.expandable = false;
    node.deprecated = bccpNode.deprecated || bdtNode.deprecated;
    node.state = bccpNode.state;
    node.level = parent.level + 1;
    node.parent = parent;
    return node;
  }

  getChildren(node: T): T[] {
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

    const children = [];
    targets.forEach(target => {
      if (target.startsWith('ACC-')) {
        children.push(this.toAccNode(nodes[target], node));
      } else if (target.startsWith('ASCCP-')) {
        const asccpNode = this.toAsccpNode(nodes[target], node);
        if (asccpNode.isUserExtensionGroup) {
          const uegAccNode = this.getChildren(asccpNode as unknown as T)[0];
          children.push(...this.getChildren(uegAccNode).map(e => {
            e.level = node.level + 1;
            e.parent = node;
            return e;
          }));
        } else {
          children.push(asccpNode);
        }
      } else if (target.startsWith('BCCP-')) {
        if (node instanceof DtFlatNode) {
          children.push(this.toBccpNode(nodes[target], node, nodes[getKey(node)]));
        } else {
          children.push(this.toBccpNode(nodes[target], node));
        }
      } else if (target.startsWith('DT-')) {
        const bccpEdges = edges[target];
        if (bccpEdges) {
          bccpEdges.targets.map(e => nodes[e]).forEach(e => {
            children.push(this.toBccpNode(e, node));
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

  dataSource: CcFlatNodeDataSource<CcFlatNode>;

  selectedNode: CcFlatNode;
  cursorNode: CcFlatNode;

  hasBasedAcc: boolean;

  @ViewChild('virtualScroll', {static: true}) public virtualScroll: CdkVirtualScrollViewport;
  virtualScrollItemSize = 33;

  get minBufferPx(): number {
    return 10000 * this.virtualScrollItemSize;
  }

  get maxBufferPx(): number {
    return 1000000 * this.virtualScrollItemSize;
  }

  constructor(private dialogRef: MatDialogRef<FindUsagesDialogComponent>,
              private service: FindUsagesDialogService,
              private ccNodeService: CcNodeService,
              @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  ngOnInit(): void {
    forkJoin([
      this.service.findUsages(this.data.type, this.data.manifestId)
    ]).subscribe(([findUsagesResp]) => {
      const edge = findUsagesResp.graph.edges[findUsagesResp.rootNodeKey];
      const sizeOfNodes: number = (!!edge) ? edge.targets.length : 0;
      if (!sizeOfNodes) {
        this.title = 'Nothing found';
      } else {
        this.title = 'Where Used: ' + sizeOfNodes + ' result' + ((sizeOfNodes === 1) ? '' : 's');
      }

      const ccGraph = new CcGraph();
      ccGraph.graph = findUsagesResp.graph;

      const delimiter = findUsagesResp.rootNodeKey.indexOf('-');
      const keys = [findUsagesResp.rootNodeKey.substring(0, delimiter),
        findUsagesResp.rootNodeKey.substring(delimiter + 1)];

      const database = new FindUsagesCcFlatNodeDatabase<CcFlatNode>(ccGraph, keys[0], Number(keys[1]));
      this.dataSource = new CcFlatNodeDataSource<CcFlatNode>(database, this.ccNodeService);
      this.dataSource.data = database.rootNode.children as CcFlatNode[];

      this.isUpdating = false;
    });
  }

  onResize(event) {
    this._innerHeight = window.innerHeight;
  }

  get innerHeight(): number {
    return this._innerHeight - 280;
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

    this.dataSource.toggle(node);
  }

  keyNavigation($event: KeyboardEvent) {
    if ($event.key === 'ArrowLeft' || $event.key === 'ArrowRight') {
      this.dataSource.toggle(this.cursorNode);
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
