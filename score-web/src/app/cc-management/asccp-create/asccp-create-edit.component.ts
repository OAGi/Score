import {Component, Injectable, OnInit} from '@angular/core';
import {CcNodeService} from '../domain/core-component-node.service';
import {MatSnackBar} from '@angular/material';
import {
  CcAccNode,
  CcAsccpNodeDetail,
  CcBccpNode,
  CcBccpNodeDetail,
  CcBdtScNodeDetail,
  CcNode,
  CcNodeDetail,
  DynamicCcFlatNode,
  EntityType,
  EntityTypes,
  OagisComponentType,
  OagisComponentTypes
} from '../domain/core-component-node';
import {map, switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {BehaviorSubject, merge, Observable} from 'rxjs';
import {FlatTreeControl} from '@angular/cdk/tree';
import {CollectionViewer, SelectionChange, SelectionModel} from '@angular/cdk/collections';

@Injectable()
export class DynamicCcDataSource {

  dataChange = new BehaviorSubject<DynamicCcFlatNode[]>([]);
  dataDetailMap: Map<string, DynamicCcFlatNode> = new Map();

  constructor(private component: AsccpCreateEditComponent,
              private treeControl: FlatTreeControl<DynamicCcFlatNode>,
              private service: CcNodeService) {
  }

  get data(): DynamicCcFlatNode[] {
    return this.dataChange.value;
  }

  set data(value: DynamicCcFlatNode[]) {
    this.treeControl.dataNodes = value;
    this.dataChange.next(value);
  }

  connect(collectionViewer: CollectionViewer): Observable<DynamicCcFlatNode[]> {
    this.treeControl.expansionModel.onChange.subscribe(change => {
      if ((change as SelectionChange<DynamicCcFlatNode>).added ||
        (change as SelectionChange<DynamicCcFlatNode>).removed) {
        this.handleTreeControl(change as SelectionChange<DynamicCcFlatNode>);
      }
    });

    return merge(collectionViewer.viewChange, this.dataChange).pipe(map(() => this.data));
  }

  /** Handle expand/collapse behaviors */
  handleTreeControl(change: SelectionChange<DynamicCcFlatNode>) {
    if (change.added) {
      change.added.forEach(node => this.toggleNode(node, true));
    }
    if (change.removed) {
      change.removed.slice().reverse().forEach(node => this.toggleNode(node, false));
    }
  }

  /**
   * Toggle the node, remove from display list
   */
  toggleNode(node: DynamicCcFlatNode, expand: boolean) {
    const index = this.data.indexOf(node);
    // If it cannot find the node, no op
    if (index < 0) {
      return;
    }

    if (expand) {
      node.isLoading = true;
      this.service.getChildren(node.item, this.component.releaseId).subscribe(children => {
        if (!children) { // If no children, no op
          return;
        }

        const nodes = children.map(item =>
          new DynamicCcFlatNode(item, node.level + 1));
        this.data.splice(index + 1, 0, ...nodes);

        // notify the change
        this.dataChange.next(this.data);
        node.isLoading = false;
      }, err => {
        node.isLoading = false;
      });

    } else {
      if (typeof this.treeControl.isExpandable(node) !== 'undefined') {
        this.treeControl.getDescendants(node).forEach((e: DynamicCcFlatNode) => {
          if (this.treeControl.isExpanded(e)) {
            this.treeControl.collapse(e);
          }
        });

        let count = 0;
        for (let i = index + 1; i < this.data.length && this.data[i].level > node.level; i++, count++) {
        }
        this.data.splice(index + 1, count);

        // notify the change
        this.dataChange.next(this.data);
      }
    }
  }

  loadDetail(node: DynamicCcFlatNode, callbackFn?) {
    this.service.getDetail(node.item, this.component.releaseId).subscribe(detail => {
      return callbackFn && callbackFn(detail);
    });
  }

  onSelect(...nodes: DynamicCcFlatNode[]) {

  }

  onDeselect(...nodes: DynamicCcFlatNode[]) {

  }
}

export class CustomTreeControl<T> extends FlatTreeControl<T> {
  getParent(node: T) {
    const currentLevel = this.getLevel(node);

    if (currentLevel <= 0) {
      return undefined;
    }

    const startIndex = this.dataNodes.indexOf(node) - 1;

    for (let i = startIndex; i >= 0; i--) {
      const currentNode = this.dataNodes[i];

      if (this.getLevel(currentNode) < currentLevel) {
        return currentNode;
      }
    }
  }
}

@Component({
  selector: 'srt-asccp-create-edit',
  templateUrl: './asccp-create-edit.component.html',
  styleUrls: ['./asccp-create-edit.component.css']
})
export class AsccpCreateEditComponent implements OnInit {

  title: string;
  paddingPixel = 15;
  _innerHeight: number = window.innerHeight;

  releaseId: number;
  isUpdating: boolean;
  componentTypes: OagisComponentType[] = OagisComponentTypes;
  entityTypes: EntityType[] = EntityTypes;

  rootNode: DynamicCcFlatNode;
  treeControl: CustomTreeControl<DynamicCcFlatNode>;
  dataSource: DynamicCcDataSource;

  detail: CcNodeDetail;
  selectedNode: DynamicCcFlatNode;
  checklistSelection = new SelectionModel<DynamicCcFlatNode>(true /* multiple */);

  asccpNode: CcAsccpNodeDetail;

  constructor(private service: CcNodeService,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) { }

  ngOnInit() {
    this.treeControl = new CustomTreeControl<DynamicCcFlatNode>(this.getLevel, this.isExpandable);
    this.dataSource = new DynamicCcDataSource(this, this.treeControl, this.service);

    this.releaseId = 0;
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        return this.service.getAsccpNode(params.get('id'), this.releaseId);
      })).subscribe(resp => {
      this.title = 'Structure of ' + resp.name + ' ASCCP';
      this.rootNode = new DynamicCcFlatNode(resp);
      this.dataSource.data = [this.rootNode];
      this.onClick(this.rootNode);
    });
  }

  onResize(event) {
    this._innerHeight = window.innerHeight;
  }

  get innerHeight(): number {
    return this._innerHeight - 160;
  }

  getLevel = (node: DynamicCcFlatNode) => node.level;
  isExpandable = (node: DynamicCcFlatNode) => node.expandable;
  hasChild = (_: number, _nodeData: DynamicCcFlatNode) => _nodeData.expandable;

  type(node: DynamicCcFlatNode) {
    const nodeItem: CcNode = node.item;
    let typeStr = nodeItem.type;
    if (typeStr === 'acc') {
      const accNode: CcAccNode = nodeItem as CcAccNode;
      if (accNode.group) {
        typeStr = typeStr + '-group';
      }
    } else if (typeStr === 'bccp') {
      const bccpNode: CcBccpNode = nodeItem as CcBccpNode;
      if (bccpNode.attribute) {
        typeStr = typeStr + '-attribute';
      }
    }
    return typeStr;
  }

  get state() {
    return this.rootNode.item.state;
  }

  get access(): string {
    return this.rootNode && (this.rootNode.item as CcAccNode).access || 'Unprepared';
  }

  itemSelectionToggle(node: DynamicCcFlatNode) {
    this.checklistSelection.toggle(node);

    const selected: boolean = this.checklistSelection.isSelected(node);
    if (selected) {
      this.dataSource.onSelect(node);
    } else {
      this.dataSource.onDeselect(node);
    }

    let parent = this.treeControl.getParent(node);
    while (parent !== undefined) {

      if (selected) {

        this.checklistSelection.select(parent);
        this.dataSource.onSelect(parent);

      } else {

        if (this.treeControl.getDescendants(parent).length === 0) {
          this.checklistSelection.deselect(parent);
          this.dataSource.onDeselect(parent);
        }
      }

      parent = this.treeControl.getParent(parent);
    }

    if (!selected) {
      const descendants = this.treeControl.getDescendants(node);
      this.checklistSelection.deselect(...descendants);
      this.dataSource.onDeselect(...descendants);
    }
  }

  get details(): DynamicCcFlatNode[] {
    return Array.from(this.dataSource.dataDetailMap.values());
  }

  get isChanged() {
    for (const detail of this.details) {
      if (detail.isChanged()) {
        return true;
      }
    }
    return false;
  }

  onClick(node: DynamicCcFlatNode) {
    if (typeof node !== 'undefined') {
      this.dataSource.loadDetail(node, (detail: CcNodeDetail) => {
        this.selectedNode = node;
        this.detail = detail;
      });
    }
  }

  isEditable() {
    return (this.state === 'Editing' && this.access === 'CanEdit');
  }

  /* For type casting of detail property */
  isAccDetail(detail?: CcNodeDetail): boolean {
    if (!detail) {
      detail = this.detail;
    }
    return (detail !== undefined) && (detail.type === 'acc');
  }

  asAccDetail(detail?: CcNodeDetail): CcAccNode {
    if (!detail) {
      detail = this.detail;
    }
    return detail as CcAccNode;
  }

  isAsccpDetail(detail?: CcNodeDetail): boolean {
    if (!detail) {
      detail = this.detail;
    }
    return (detail !== undefined) && (detail.type === 'asccp');
  }

  asAsccpDetail(detail?: CcNodeDetail): CcAsccpNodeDetail {
    if (!detail) {
      detail = this.detail;
    }
    return detail as CcAsccpNodeDetail;
  }

  isBccpDetail(detail?: CcNodeDetail): boolean {
    if (!detail) {
      detail = this.detail;
    }
    return (detail !== undefined) && (detail.type === 'bccp');
  }

  asBccpDetail(detail?: CcNodeDetail): CcBccpNodeDetail {
    if (!detail) {
      detail = this.detail;
    }
    return detail as CcBccpNodeDetail;
  }

  isBdtScDetail(detail?: CcNodeDetail): boolean {
    if (!detail) {
      detail = this.detail;
    }
    return (detail !== undefined) && (detail.type === 'bdt_sc');
  }

  asBdtScDetail(detail?: CcNodeDetail): CcBdtScNodeDetail {
    if (!detail) {
      detail = this.detail;
    }
    return detail as CcBdtScNodeDetail;
  }

  update(asccpNode: CcAsccpNodeDetail) {
    this.service.updateAsccp(asccpNode).subscribe(_ => {
      this.snackBar.open('Updated', '', {
        duration: 1000,
      });
    });
  }

  candidate() {

  }

  dataChanged($event: any) {
    this.title = 'Structure of ' + this.asAsccpDetail().asccp.propertyTerm + ' ASCCP';
    this.rootNode.item.name = this.asAsccpDetail().asccp.propertyTerm;
    const denFromAcc = this.asAsccpDetail().asccp.den.split('.')[1].replace(/^\s+/g, '');
    this.asAsccpDetail().asccp.den = this.asAsccpDetail().asccp.propertyTerm + '. ' + denFromAcc;
  }
}
