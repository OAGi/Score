import {Component, Injectable, OnInit, ViewChild} from '@angular/core';
import {CollectionViewer, SelectionChange, SelectionModel} from '@angular/cdk/collections';
import {FlatTreeControl} from '@angular/cdk/tree';
import {BehaviorSubject, merge, Observable} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {ReleaseService} from '../../release-management/domain/release.service';
import {MatDialog, MatSnackBar} from '@angular/material';
import {HotkeysService} from 'angular2-hotkeys';
import {CcNodeService} from '../domain/core-component-node.service';
import {hashCode, UnboundedPipe} from '../../common/utility';
import {AbstractControl, FormControl, ValidationErrors, Validators} from '@angular/forms';
import {isNumber} from 'util';

import {
  CcAccNode,
  CcAccNodeDetail,
  CcAsccpNode,
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
import {ContextMenuComponent, ContextMenuService} from 'ngx-contextmenu';
import {AppendAsccpDialogComponent} from './append-asccp-dialog/append-asccp-dialog.component';
import {AppendBccpDialogComponent} from './append-bccp-dialog/append-bccp-dialog.component';
import {ExtensionDetailService} from './domain/extension-detail.service';
import {ConfirmDialogComponent} from './confirm-dialog/confirm-dialog.component';
import {GrowlService} from 'ngx-growl';

@Injectable()
export class DynamicCcDataSource {

  dataChange = new BehaviorSubject<DynamicCcFlatNode[]>([]);

  constructor(private component: ExtensionDetailComponent,
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

  loadDetail(node: DynamicCcFlatNode, callbackFn?) {
    this.service.getDetail(node.item, this.component.releaseId).subscribe(detail => {
      detail = this.resetHashCode(detail);
      return callbackFn && callbackFn(detail);
    });
  }

  resetHashCode(detail: CcNodeDetail) {
    if (detail.type === 'acc') {
      (detail as CcAccNodeDetail).$hashCode = hashCode(detail);
    } else if (detail.type === 'asccp') {
      const asccpDetail = (detail as CcAsccpNodeDetail);
      asccpDetail.ascc.$hashCode = hashCode(asccpDetail.ascc);
      asccpDetail.asccp.$hashCode = hashCode(asccpDetail.asccp);
    } else if (detail.type === 'bccp') {
      const bccpDetail = (detail as CcBccpNodeDetail);
      bccpDetail.bcc.$hashCode = hashCode(bccpDetail.bcc);
      bccpDetail.bccp.$hashCode = hashCode(bccpDetail.bccp);
      bccpDetail.bdt.$hashCode = hashCode(bccpDetail.bdt);
    } else if (detail.type === 'bdt_sc') {
      (detail as CcBdtScNodeDetail).$hashCode = hashCode(detail);
    }
    return detail;
  }

  onSelect(...nodes: DynamicCcFlatNode[]) {
    this.component.resetCardinalities();
  }

  onDeselect(...nodes: DynamicCcFlatNode[]) {
    this.component.resetCardinalities();
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
  selector: 'srt-extension-detail',
  templateUrl: './extension-detail.component.html',
  styleUrls: ['./extension-detail.component.css']
})
export class ExtensionDetailComponent implements OnInit {

  title: string;
  _innerHeight: number = window.innerHeight;
  paddingPixel = 15;
  releaseId: number;
  extensionId: number;
  private $isChanged: Map<string, CcNodeDetail> = new Map();
  isUpdating: boolean;
  componentTypes: OagisComponentType[] = OagisComponentTypes;
  entityTypes: EntityType[] = EntityTypes;
  rootNode: DynamicCcFlatNode;
  treeControl: CustomTreeControl<DynamicCcFlatNode>;
  dataSource: DynamicCcDataSource;

  detail: CcNodeDetail;
  selectedNode: DynamicCcFlatNode;

  /* Begin cardinality management */
  ccCardinalityMin: FormControl;
  ccCardinalityMax: FormControl;
  /* End cardinality management */


  checklistSelection = new SelectionModel<DynamicCcFlatNode>(true /* multiple */);
  @ViewChild(ContextMenuComponent, {static: true}) public appendingMenu: ContextMenuComponent;

  constructor(private service: CcNodeService,
              private extensionDetailService: ExtensionDetailService,
              private releaseService: ReleaseService,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private growlService: GrowlService,
              private hotkeysService: HotkeysService,
              private contextMenuService: ContextMenuService,
              private dialog: MatDialog) {

  }

  ngOnInit() {
    this.treeControl = new CustomTreeControl<DynamicCcFlatNode>(this.getLevel, this.isExpandable);
    this.dataSource = new DynamicCcDataSource(this, this.treeControl, this.service);

    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        this.releaseId = parseInt(params.get('releaseId'), 10);
        this.extensionId = parseInt(params.get('extensionId'), 10);
        return this.service.getExtensionNode(this.extensionId, this.releaseId);
      })).subscribe(resp => {
      this.title = 'Structure of ' + resp.objectClassTerm;
      this.rootNode = new DynamicCcFlatNode(resp);
      this.dataSource.data = [this.rootNode];
      this.onClick(this.rootNode);
    });

    this.isUpdating = false;
  }

  getLevel = (node: DynamicCcFlatNode) => node.level;
  isExpandable = (node: DynamicCcFlatNode) => node.expandable;
  hasChild = (_: number, _nodeData: DynamicCcFlatNode) => _nodeData.expandable;

  onResize(event) {
    this._innerHeight = window.innerHeight;
  }

  get innerHeight(): number {
    return this._innerHeight - 160;
  }

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

  isChildOfUserExtensionGroup(node?: DynamicCcFlatNode) {
    const targetNode = (node) ? node : this.selectedNode;
    const typeStr = targetNode.item.type;
    return (targetNode.level === 1) && (typeStr.startsWith('asccp') || typeStr.startsWith('bccp'));
  }

  get state() {
    return (this.rootNode) ? this.rootNode.item.state : '';
  }

  get access() {
    return (this.rootNode) ? this.rootNode.item.access : '';
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

  onClick(node: DynamicCcFlatNode) {
    this.dataSource.loadDetail(node, (detail: CcNodeDetail) => {
      this.selectedNode = node;
      this.detail = detail;
      this.resetCardinalities(this.detail);
    });
  }

  /* For type casting of detail property */
  isAccDetail(detail?: CcNodeDetail): boolean {
    if (!detail) {
      detail = this.detail;
    }
    return (detail !== undefined) && (detail.type === 'acc');
  }

  asAccDetail(detail?: CcNodeDetail): CcAccNodeDetail {
    if (!detail) {
      detail = this.detail;
    }
    return detail as CcAccNodeDetail;
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

  onContextMenu($event: MouseEvent, item: any): void {
    this.contextMenuService.show.next({
      contextMenu: this.appendingMenu,
      event: $event,
      item: item,
    });
    $event.preventDefault();
    $event.stopPropagation();
  }

  onChange(node?: CcNodeDetail) {
    if (!node) {
      node = this.detail;
    }

    if (this.isAsccpDetail(node)) {
      const asccpDetail = this.asAsccpDetail(node);
      const changed = asccpDetail.ascc.$hashCode !== hashCode(asccpDetail.ascc);
      const key = 'ascc-' + asccpDetail.ascc.asccId;
      if (changed) {
        this.$isChanged.set(key, asccpDetail);
      } else if (this.$isChanged.has(key)) {
        this.$isChanged.delete(key);
      }

    } else if (this.isBccpDetail(node)) {
      const bccpDetail = this.asBccpDetail(node);
      const changed = bccpDetail.bcc.$hashCode !== hashCode(bccpDetail.bcc);
      const key = 'bcc-' + bccpDetail.bcc.bccId;
      if (changed) {
        this.$isChanged.set(key, bccpDetail);
      } else if (this.$isChanged.has(key)) {
        this.$isChanged.delete(key);
      }
    }
  }

  get isChanged() {
    return this.$isChanged.size > 0;
  }

  updateDetails() {
    if (!this.isChanged || this.isUpdating) {
      return;
    }

    const details = Array.from(this.$isChanged.values());
    this.isUpdating = true;
    this.extensionDetailService.updateDetails(details, this.releaseId, this.extensionId).subscribe(_ => {
      for (const detail of details) {
        this.dataSource.resetHashCode(detail);
      }
      this.$isChanged.clear();

      this.snackBar.open('Updated', '', {
        duration: 1000,
      });
      this.isUpdating = false;
    }, err => {

      this.isUpdating = false;
    });
  }

  appendAscc(node: DynamicCcFlatNode) {
    const dialogRef = this.dialog.open(AppendAsccpDialogComponent, {
      data: {
        releaseId: this.releaseId,
        extensionId: this.extensionId
      },
      width: window.innerWidth + 'px'
    });
    dialogRef.afterClosed().subscribe(asccp => {
      if (!asccp) {
        return;
      }
      if (this.treeControl.isExpanded(this.rootNode)) {
        this.treeControl.toggle(this.rootNode);
      }
      this.treeControl.toggle(this.rootNode);
    });
  }

  appendBcc(node: DynamicCcFlatNode) {
    const dialogRef = this.dialog.open(AppendBccpDialogComponent, {
      data: {
        releaseId: this.releaseId,
        extensionId: this.extensionId
      },
      width: window.innerWidth + 'px'
    });
    dialogRef.afterClosed().subscribe(bccp => {
      if (!bccp) {
        return;
      }
      if (this.treeControl.isExpanded(this.rootNode)) {
        this.treeControl.toggle(this.rootNode);
      }
      this.treeControl.toggle(this.rootNode);
    });
  }

  onClickDiscard($event: MouseEvent, node: DynamicCcFlatNode): void {
    const nodeItem: CcNode = node.item;
    const typeStr = nodeItem.type;
    switch (typeStr) {
      case 'asccp':
        this.isUpdating = true;

        const asccpNode: CcAsccpNode = nodeItem as CcAsccpNode;
        this.extensionDetailService.discardAscc(asccpNode.asccId, this.releaseId, this.extensionId).subscribe(_ => {
          this.isUpdating = false;

          this.selectedNode = undefined;
          this.detail = undefined;
          this.$isChanged.delete('ascc-' + asccpNode.asccId);

          if (this.treeControl.isExpanded(this.rootNode)) {
            this.treeControl.toggle(this.rootNode);
          }
          this.treeControl.toggle(this.rootNode);
        });

        break;

      case 'bccp':
        this.isUpdating = true;

        const bccpNode: CcBccpNode = nodeItem as CcBccpNode;
        this.extensionDetailService.discardBcc(bccpNode.bccId, this.releaseId, this.extensionId).subscribe(_ => {
          this.isUpdating = false;

          this.selectedNode = undefined;
          this.detail = undefined;
          this.$isChanged.delete('bcc-' + bccpNode.bccId);

          if (this.treeControl.isExpanded(this.rootNode)) {
            this.treeControl.toggle(this.rootNode);
          }
          this.treeControl.toggle(this.rootNode);
        });

        break;
    }
  }

  delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  numberOnly(event): boolean {
    const charCode = (event.which) ? event.which : event.keyCode;
    return !(charCode > 31 && (charCode < 45 || charCode > 57));
  }

  updateState(state: string) {
    this.isUpdating = true;
    this.extensionDetailService.setState(this.releaseId, this.extensionId, state).subscribe(_ => {
      this.rootNode.item.state = state;
      this.isUpdating = false;
      this.resetCardinalities();

      this.snackBar.open('State updated', '', {
        duration: 1000,
      });
    }, err => {
      this.isUpdating = false;
    });
  }

  openConfirmDialog() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.updateState('Published');
      }
    });
  }

  resetCardinalities(nodeDetail?: CcNodeDetail) {
    this._setCardinalityMinFormControl(nodeDetail);
    this._setCardinalityMaxFormControl(nodeDetail);
  }

  _setCardinalityMinFormControl(nodeDetail?: CcNodeDetail) {
    if (!nodeDetail) {
      nodeDetail = this.detail;
    }
    if (!nodeDetail) {
      return;
    }

    let obj;
    if (this.isAsccpDetail(nodeDetail)) {
      obj = (<unknown>nodeDetail as CcAsccpNodeDetail).ascc;
    } else if (this.isBccpDetail(nodeDetail)) {
      obj = (<unknown>nodeDetail as CcBccpNodeDetail).bcc;
    } else if (this.isBdtScDetail(nodeDetail)) {
      obj = (<unknown>nodeDetail as CcBdtScNodeDetail);
    } else {
      return false;
    }

    this.ccCardinalityMin = new FormControl({
        value: obj.cardinalityMin,
      disabled: (this.state !== 'Editing' || this.access !== 'CanEdit' || !this.isChildOfUserExtensionGroup())
      }, [
        Validators.required,
        Validators.pattern('[0-9]+'),
        // validatorFn for maximum value
        (control: AbstractControl): ValidationErrors | null => {
          if (obj.cardinalityMax === -1) {
            return null;
          }
          if (Number(control.value) > obj.cardinalityMax) {
            return {'max': 'Cardinality Min must be less than or equals to ' + obj.cardinalityMax};
          }
          return null;
        }
      ]
    );
    this.ccCardinalityMin.valueChanges.subscribe(value => {
      if (this.ccCardinalityMin.valid) {
        value = isNumber(value) ? value : Number.parseInt(value, 10);
        obj.cardinalityMin = Number(value);
        this.onChange(nodeDetail);
        this._setCardinalityMaxFormControl(nodeDetail);
      }
    });
  }

  _setCardinalityMaxFormControl(nodeDetail?: CcNodeDetail) {
    if (!nodeDetail) {
      nodeDetail = this.detail;
    }
    if (!nodeDetail) {
      return;
    }

    let obj;
    if (this.isAsccpDetail(nodeDetail)) {
      obj = (<unknown>nodeDetail as CcAsccpNodeDetail).ascc;
    } else if (this.isBccpDetail(nodeDetail)) {
      obj = (<unknown>nodeDetail as CcBccpNodeDetail).bcc;
    } else if (this.isBdtScDetail(nodeDetail)) {
      obj = (<unknown>nodeDetail as CcBdtScNodeDetail);
    } else {
      return false;
    }

    this.ccCardinalityMax = new FormControl({
        value: new UnboundedPipe().transform(obj.cardinalityMax),
        disabled: (this.state !== 'Editing' || this.access !== 'CanEdit' || !this.isChildOfUserExtensionGroup())
      }, [
        Validators.required,
        Validators.pattern('[0-9]+|-1|unbounded'),
        // validatorFn for minimum value
        (control: AbstractControl): ValidationErrors | null => {
          let controlValue = control.value;
          controlValue = (controlValue === 'unbounded') ? -1 : (isNumber(controlValue) ? controlValue : Number.parseInt(controlValue, 10));

          if (!controlValue || controlValue === -1) {
            return null;
          }
          if (controlValue < obj.cardinalityMin) {
            return {'min': 'Cardinality Max must be greater than ' + obj.cardinalityMin};
          }
          return null;
        },
      ]
    );
    this.ccCardinalityMax.valueChanges.subscribe(value => {
      if (this.ccCardinalityMax.valid) {
        value = (value === 'unbounded') ? -1 : (isNumber(value) ? value : Number.parseInt(value, 10));
        obj.cardinalityMax = value;
        this.onChange(nodeDetail);
        this._setCardinalityMinFormControl(nodeDetail);
      }
    });
  }

  isValid() {
    if (this.ccCardinalityMin === undefined || this.ccCardinalityMax === undefined) {
      return true;
    }
    return !this.ccCardinalityMin.invalid && !this.ccCardinalityMax.invalid;
  }
}
