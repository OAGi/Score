import {Component, ElementRef, Inject, Injectable, OnInit, ViewChild} from '@angular/core';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {BieEditService} from './domain/bie-edit.service';
import {CollectionViewer, SelectionChange, SelectionModel} from '@angular/cdk/collections';
import {FlatTreeControl} from '@angular/cdk/tree';
import {BehaviorSubject, merge, Observable, ReplaySubject} from 'rxjs';
import {finalize, map, startWith, switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {
  BieEditAbieNode,
  BieEditAbieNodeDetail,
  BieEditAsbiepNode,
  BieEditAsbiepNodeDetail,
  BieEditBbiepNode,
  BieEditBbiepNodeDetail,
  BieEditBbieScNodeDetail,
  BieEditCreateExtensionResponse,
  BieEditNode,
  BieEditNodeDetail,
  BieEditUpdateResponse,
  CardinalityAware,
  DynamicBieFlatNode,
  Primitive,
  PrimitiveType
} from './domain/bie-edit-node';
import {ReleaseService} from '../../release-management/domain/release.service';
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ContextMenuComponent, ContextMenuService} from 'ngx-contextmenu';
import {BusinessContext, BusinessContextListRequest} from '../../context-management/business-context/domain/business-context';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {MatAutocomplete, MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {PageRequest} from '../../basis/basis';

import {AbstractControl, FormControl, ValidationErrors, Validators} from '@angular/forms';
import {isNumber} from 'util';
import {UnboundedPipe} from '../../common/utility';
import {ConfirmDialogComponent as ExtensionConfirmDialogComponent} from './confirm-dialog/confirm-dialog.component';
import {ReuseBieDialogComponent} from './reuse-bie-dialog/reuse-bie-dialog.component';
import {AuthService} from '../../authentication/auth.service';
import {ConfirmDialogConfig} from '../../common/confirm-dialog/confirm-dialog.domain';
import {ConfirmDialogComponent} from '../../common/confirm-dialog/confirm-dialog.component';


@Injectable()
export class DynamicDataSource {

  dataChange = new BehaviorSubject<DynamicBieFlatNode[]>([]);

  dataDetailMap: Map<string, DynamicBieFlatNode> = new Map();
  dataChildrenMap: Map<string, DynamicBieFlatNode[]> = new Map();

  constructor(private component: BieEditComponent,
              private treeControl: FlatTreeControl<DynamicBieFlatNode>,
              private service: BieEditService) {
  }

  clear() {
    this.dataChange.next([]);
    this.dataChildrenMap.clear();
    this.dataDetailMap.clear();
  }

  get data(): DynamicBieFlatNode[] {
    return this.dataChange.value;
  }

  set data(value: DynamicBieFlatNode[]) {
    this.treeControl.dataNodes = value;
    this.dataChange.next(value);
  }

  connect(collectionViewer: CollectionViewer): Observable<DynamicBieFlatNode[]> {
    this.treeControl.expansionModel.changed.subscribe(change => {
      if ((change as SelectionChange<DynamicBieFlatNode>).added ||
        (change as SelectionChange<DynamicBieFlatNode>).removed) {
        this.handleTreeControl(change as SelectionChange<DynamicBieFlatNode>);
      }
    });

    return merge(collectionViewer.viewChange, this.dataChange).pipe(map(() => this.data));
  }

  /** Handle expand/collapse behaviors */
  handleTreeControl(change: SelectionChange<DynamicBieFlatNode>) {
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
  toggleNode(node: DynamicBieFlatNode, expand: boolean) {
    const index = this.data.indexOf(node);
    // If it cannot find the node, no op
    if (index < 0) {
      return;
    }

    if (expand) {
      if (this.dataChildrenMap.has(node.key)) {

        const nodes = this.dataChildrenMap.get(node.key);
        this.data.splice(index + 1, 0, ...nodes);

        // notify the change
        this.dataChange.next(this.data);
      } else {
        node.isLoading = true;
        this.service.getChildren(node, this.component.hideUnused).subscribe(children => {
          if (!children) { // If no children, no op
            return;
          }

          const nodes = children.map(item =>
            new DynamicBieFlatNode(item, node.level + 1));
          this.data.splice(index + 1, 0, ...nodes);
          nodes.forEach((e: DynamicBieFlatNode) => {
            if (e.item.used) {
              this.component.itemSelectionToggle(e);
            }
          });

          this.dataChildrenMap.set(node.key, nodes);

          // notify the change
          this.dataChange.next(this.data);
          node.isLoading = false;
        }, err => {
          node.isLoading = false;
        });
      }

    } else {
      this.treeControl.getDescendants(node).forEach((e: DynamicBieFlatNode) => {
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

  _processPrimitiveType(detail) {
    if (this.component.isBbiepDetail(detail)) {
      const bbiepDetail: BieEditBbiepNodeDetail = this.component.asBbiepDetail(detail);

      bbiepDetail.primitiveTypes = [];
      if (bbiepDetail.xbtList.length > 0) {
        bbiepDetail.primitiveTypes.push(PrimitiveType.Primitive);
      }
      if (bbiepDetail.codeLists.length > 0) {
        bbiepDetail.primitiveTypes.push(PrimitiveType.Code);
      }
      if (bbiepDetail.agencyIdLists.length > 0) {
        bbiepDetail.primitiveTypes.push(PrimitiveType.Agency);
      }

      if (bbiepDetail.bdtPriRestriId > 0) {
        bbiepDetail.primitiveType = PrimitiveType.Primitive;
      }
      if (bbiepDetail.codeListId > 0) {
        bbiepDetail.primitiveType = PrimitiveType.Code;
      }
      if (bbiepDetail.agencyIdListId > 0) {
        bbiepDetail.primitiveType = PrimitiveType.Agency;
      }
    }

    if (this.component.isBbieScDetail(detail)) {
      const bbieScDetail: BieEditBbieScNodeDetail = this.component.asBbieScDetail(detail);

      bbieScDetail.primitiveTypes = [];
      if (bbieScDetail.xbtList.length > 0) {
        bbieScDetail.primitiveTypes.push(PrimitiveType.Primitive);
      }
      if (bbieScDetail.codeLists.length > 0) {
        bbieScDetail.primitiveTypes.push(PrimitiveType.Code);
      }
      if (bbieScDetail.agencyIdLists.length > 0) {
        bbieScDetail.primitiveTypes.push(PrimitiveType.Agency);
      }

      if (bbieScDetail.dtScPriRestriId > 0) {
        bbieScDetail.primitiveType = PrimitiveType.Primitive;
      }
      if (bbieScDetail.codeListId > 0) {
        bbieScDetail.primitiveType = PrimitiveType.Code;
      }
      if (bbieScDetail.agencyIdListId > 0) {
        bbieScDetail.primitiveType = PrimitiveType.Agency;
      }
    }
  }

  loadDetail(node: DynamicBieFlatNode, callbackFn?) {
    if (this.dataDetailMap.has(node.key)) {
      const detail = this.dataDetailMap.get(node.key);
      if (!detail.isNullObject) {
        return callbackFn && callbackFn(detail);
      }
    }

    this.service.getDetail(node).subscribe(detail => {
      this._processPrimitiveType(detail);

      const detailNode = this.putDetail(node, detail);
      if (detail.used !== node.item.used) {
        detailNode.item.used = node.item.used;
      }
      detailNode.item.required = node.item.required;
      return callbackFn && callbackFn(detailNode);
    });
  }

  putDetail(node: DynamicBieFlatNode, detail: BieEditNodeDetail) {
    const detailNode = new DynamicBieFlatNode(detail, node.level, false, false, true);
    this.dataDetailMap.set(node.key, detailNode);
    return detailNode;
  }

  resetDetail(node: DynamicBieFlatNode) {
    const children = this.dataChildrenMap.get(node.key);
    this.dataDetailMap.delete(node.key);

    node.reset();

    this.dataChildrenMap.set(node.key, children);
    this.dataDetailMap.set(node.key, node);
  }

  clearDescendants(node: DynamicBieFlatNode) {
    this.dataChildrenMap.delete(node.key);
    this.dataDetailMap.delete(node.key);

    const index = this.data.indexOf(node);
    for (let i = index + 1; i < this.data.length && this.data[i].level > node.level; i++) {
      const child = this.data[i];
      this.dataChildrenMap.delete(child.key);
      this.dataDetailMap.delete(child.key);
    }
  }

  _createNullDetail(node: DynamicBieFlatNode) {
    switch (node.item.type) {
      case 'asbiep':
        this.putDetail(node, new BieEditAsbiepNodeDetail(node.item)).isNullObject = true;
        break;
      case 'bbiep':
        this.putDetail(node, new BieEditBbiepNodeDetail(node.item)).isNullObject = true;
        break;
      case 'bbie_sc':
        this.putDetail(node, new BieEditBbieScNodeDetail(node.item)).isNullObject = true;
        break;
    }
  }

  onSelect(...nodes: DynamicBieFlatNode[]) {
    for (const node of nodes) {
      if (!this.dataDetailMap.has(node.key)) {
        this._createNullDetail(node);
      }

      node.item.used = true;
      const detailNode = this.dataDetailMap.get(node.key);
      detailNode.item.used = true;
      this.component.onChange(node);
    }

    this.component.resetCardinalities();
  }

  onDeselect(...nodes: DynamicBieFlatNode[]) {
    for (const node of nodes) {
      if (!this.dataDetailMap.has(node.key)) {
        this._createNullDetail(node);
      }

      node.item.used = false;
      const detailNode = this.dataDetailMap.get(node.key);
      detailNode.item.used = false;
      this.component.onChange(node);
    }

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
  selector: 'score-bie-edit',
  templateUrl: './bie-edit.component.html',
  styleUrls: ['./bie-edit.component.css']
})
export class BieEditComponent implements OnInit {

  exampleContentTypes = ['json', 'yaml'];

  _innerHeight: number = window.innerHeight;
  paddingPixel = 15;
  hideUnused: boolean;
  releaseNum: string;
  isUpdating: boolean;

  topLevelAsbiepId: number;
  rootNode: DynamicBieFlatNode;
  reusedRootNode: DynamicBieFlatNode;
  treeControl: CustomTreeControl<DynamicBieFlatNode>;
  dataSource: DynamicDataSource;
  detailNode: DynamicBieFlatNode;
  reusedDetailNode: BieEditAbieNodeDetail;
  selectedNode: DynamicBieFlatNode;
  checklistSelection = new SelectionModel<DynamicBieFlatNode>(true /* multiple */);

  /* Begin cardinality management */
  bieCardinalityMin: FormControl;
  bieCardinalityMax: FormControl;
  /* End cardinality management */

  primitiveFilterCtrl: FormControl = new FormControl();
  filteredPrimitives: ReplaySubject<Primitive[]> = new ReplaySubject<Primitive[]>(1);

  /* Begin business context management */
  businessContextCtrl = new FormControl();
  businessContexts: BusinessContext[] = [];
  reusedBusinessContexts: BusinessContext[] = [];
  allBusinessContexts: BusinessContext[] = [];
  filteredBusinessContexts: Observable<BusinessContext[]>;

  visible = true;
  businessContextUpdating = true;
  addOnBlur = true;
  separatorKeysCodes: number[] = [ENTER, COMMA];
  @ViewChild('businessContextInput', {static: false}) businessContextInput: ElementRef<HTMLInputElement>;
  @ViewChild('matAutocomplete', {static: false}) matAutocomplete: MatAutocomplete;
  /* End business context management */

  @ViewChild('createBieContextMenu', {static: true}) public createBieContextMenu: ContextMenuComponent;
  @ViewChild('defaultContextMenu', {static: true}) public defaultContextMenu: ContextMenuComponent;
  @ViewChild('extensionContextMenu', {static: true}) public extensionContextMenu: ContextMenuComponent;

  constructor(private service: BieEditService,
              private authService: AuthService,
              private businessContextService: BusinessContextService,
              private releaseService: ReleaseService,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private contextMenuService: ContextMenuService,
              private dialog: MatDialog) {

  }

  ngOnInit() {
    this.hideUnused = false;
    this.treeControl = new CustomTreeControl<DynamicBieFlatNode>(this.getLevel, this.isExpandable);
    this.dataSource = new DynamicDataSource(this, this.treeControl, this.service);

    this.primitiveFilterCtrl.valueChanges
      .subscribe(() => {
        this.primitiveFilterValues();
      });

    // load context scheme
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        this.topLevelAsbiepId = Number(params.get('id'));
        return this.service.getRootNode(this.topLevelAsbiepId);
      })
    ).subscribe((resp: BieEditAbieNode) => {
      this.rootNode = new DynamicBieFlatNode(resp);
      this.dataSource.data = [this.rootNode];

      this.businessContextService.getBusinessContextsByTopLevelAsbiepId(this.rootNode.item.topLevelAsbiepId)
        .subscribe(bizCtxResp => {
          this.businessContexts = bizCtxResp.list;
          this.businessContextUpdating = false;
        });

      this.filteredBusinessContexts = this.businessContextCtrl.valueChanges.pipe(
        startWith(null),
        map((value: string | null) => value ? this._filter(value) : this._filter()));

      this._loadAllBusinessContexts();

      // To load a root node detail
      this.onClick(this.rootNode);

      this.releaseService.getSimpleRelease(resp.releaseId)
        .subscribe(release => this.releaseNum = release.releaseNum);
    }, err => {
      if (err.status === 403) {
        this.snackBar.open('Forbidden', '', {
          duration: 1000,
        });
        this.router.navigate(['/profile_bie']);
      } else {
        throw err;
      }
    });

    this.isUpdating = false;
  }

  getLevel = (node: DynamicBieFlatNode) => node.level;
  isExpandable = (node: DynamicBieFlatNode) => node.expandable;
  hasChild = (_: number, _nodeData: DynamicBieFlatNode) => _nodeData.expandable;

  onResize(event) {
    this._innerHeight = window.innerHeight;
  }

  get innerHeight(): number {
    return this._innerHeight - 200;
  }

  primitiveFilterValues(detail?: BieEditNodeDetail) {
    let primitives: Primitive[] = [];
    if (this.isBbiepDetail(detail)) {
      switch (this.asBbiepDetail(detail).primitiveType) {
        case 'Primitive':
          primitives = this.asBbiepDetail(detail).xbtList.map(e => new Primitive(e.priRestriId, e.xbtName));
          break;
        case 'Code':
          primitives = this.asBbiepDetail(detail).codeLists.map(e => new Primitive(e.codeListId, e.codeListName));
          break;
        case 'Agency':
          primitives = this.asBbiepDetail(detail).agencyIdLists.map(e => new Primitive(e.agencyIdListId, e.agencyIdListName));
          break;
      }
    } else if (this.isBbieScDetail(detail)) {
      switch (this.asBbieScDetail(detail).primitiveType) {
        case 'Primitive':
          primitives = this.asBbieScDetail(detail).xbtList.map(e => new Primitive(e.priRestriId, e.xbtName));
          break;
        case 'Code':
          primitives = this.asBbieScDetail(detail).codeLists.map(e => new Primitive(e.codeListId, e.codeListName));
          break;
        case 'Agency':
          primitives = this.asBbieScDetail(detail).agencyIdLists.map(e => new Primitive(e.agencyIdListId, e.agencyIdListName));
          break;
      }
    } else {
      return;
    }

    let search = this.primitiveFilterCtrl.value;
    if (!search) {
      this.filteredPrimitives.next(primitives.slice());
      return;
    } else {
      search = search.toLowerCase();
    }

    this.filteredPrimitives.next(
      primitives.filter(e => e.name.toLowerCase().indexOf(search) > -1)
    );
  }

  type(node: DynamicBieFlatNode) {
    const nodeItem: BieEditNode = node.item;
    let typeStr = nodeItem.type;
    if (typeStr === 'asbiep') {
      const asbiepNode: BieEditAsbiepNode = nodeItem as BieEditAsbiepNode;
      if (asbiepNode.name === 'Extension') {
        typeStr = typeStr + '-extension';
      }
    } else if (typeStr === 'bbiep') {
      const bbiepNode: BieEditBbiepNode = nodeItem as BieEditBbiepNode;
      if (bbiepNode.attribute) {
        typeStr = typeStr + '-attribute';
      }
    }
    return typeStr;
  }

  get state(): string {
    return this.rootNode && (this.rootNode.item as BieEditAbieNode).topLevelAsbiepState || 'Published';
  }

  get access(): string {
    return this.rootNode && (this.rootNode.item as BieEditAbieNode).access || 'Unprepared';
  }

  itemSelectionToggle(node: DynamicBieFlatNode) {
    this.checklistSelection.toggle(node);

    const selected: boolean = this.checklistSelection.isSelected(node);
    if (selected) {
      this.dataSource.onSelect(node);
    } else {
      this.dataSource.onDeselect(node);
    }

    if (node.item.derived || node.item.locked) {
      return;
    }

    if (!node.item.required) {
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
    }

    if (!selected) {
      const descendants = this.treeControl.getDescendants(node).filter(e => !e.item.required);
      this.checklistSelection.deselect(...descendants);
      this.dataSource.onDeselect(...descendants);
    }
  }

  onClick(node: DynamicBieFlatNode) {

    if (node.item.type === 'asbiep' && node.item.derived) {
      this.service.getRootNode(node.item.topLevelAsbiepId).subscribe(resp => {
        this.reusedRootNode = new DynamicBieFlatNode(resp);
        this.service.getDetail(this.reusedRootNode).subscribe(detail => {
          this.reusedDetailNode = this.asAbieDetail(detail);
        });
      });
      this.businessContextService.getBusinessContextsByTopLevelAsbiepId(node.item.topLevelAsbiepId)
        .subscribe(bizCtxResp => {
          this.reusedBusinessContexts = bizCtxResp.list;
        });
    } else {
      this.reusedRootNode = null;
      this.reusedDetailNode = null;
      this.reusedBusinessContexts = [];
    }
    this.dataSource.loadDetail(node, (detailNode: DynamicBieFlatNode) => {
      this.selectedNode = node;
      this.detailNode = detailNode;
      this.resetCardinalities(detailNode);
      this.initFixedOrDefault(detailNode.item);
      this.primitiveFilterValues(detailNode.item);
    });
  }

  resetCardinalities(detailNode?: DynamicBieFlatNode) {
    this._setCardinalityMinFormControl(detailNode);
    this._setCardinalityMaxFormControl(detailNode);
    this.onChange(detailNode);
  }

  initFixedOrDefault(detail?: BieEditNodeDetail) {
    if (this.isBbiepDetail(detail)) {
      if (this.asBbiepDetail(detail).bieDefaultValue) {
        this.asBbiepDetail(detail).fixedOrDefault = 'default';
      } else if (this.asBbiepDetail(detail).bieFixedValue) {
        this.asBbiepDetail(detail).fixedOrDefault = 'fixed';
      } else {
        this.asBbiepDetail(detail).fixedOrDefault = 'none';
      }
    } else if (this.isBbieScDetail(detail)) {
      if (this.asBbieScDetail(detail).bieDefaultValue) {
        this.asBbieScDetail(detail).fixedOrDefault = 'default';
      } else if (this.asBbieScDetail(detail).bieFixedValue) {
        this.asBbieScDetail(detail).fixedOrDefault = 'fixed';
      } else {
        this.asBbieScDetail(detail).fixedOrDefault = 'none';
      }
    }
  }

  _setCardinalityMinFormControl(detailNode?: DynamicBieFlatNode) {
    if (!detailNode) {
      detailNode = this.detailNode;
    }
    if (!detailNode) {
      return;
    }

    if (detailNode && detailNode.item.type !== 'abie') {
      const disabled = !this.isEditable() || !detailNode.item.used || detailNode.item.locked;
      const obj = (<unknown>detailNode.item as CardinalityAware);

      this.bieCardinalityMin = new FormControl({
          value: obj.bieCardinalityMin,
          disabled: disabled
        }, [
          Validators.required,
          Validators.pattern('[0-9]+'),
          Validators.min(obj.ccCardinalityMin),
          // validatorFn for maximum value
          (control: AbstractControl): ValidationErrors | null => {
            if (obj.bieCardinalityMax === -1) {
              return null;
            }
            if (Number(control.value) > obj.bieCardinalityMax) {
              return {'max': 'Cardinality Min must be less than or equals to ' + obj.bieCardinalityMax};
            }
            return null;
          }
        ]
      );
      this.bieCardinalityMin.valueChanges.subscribe(value => {
        if (this.bieCardinalityMin.valid) {
          value = isNumber(value) ? value : Number.parseInt(value, 10);
          obj.bieCardinalityMin = value;
          this.bieCardinalityMax.updateValueAndValidity({onlySelf: true, emitEvent: false});
        }
      });
    }
  }

  _setCardinalityMaxFormControl(detailNode?: DynamicBieFlatNode) {
    if (!detailNode) {
      detailNode = this.detailNode;
    }
    if (!detailNode) {
      return;
    }

    if (detailNode && detailNode.item.type !== 'abie') {
      const disabled = !this.isEditable() || !detailNode.item.used || detailNode.item.locked;
      const obj = (<unknown>detailNode.item as CardinalityAware);

      this.bieCardinalityMax = new FormControl({
          value: new UnboundedPipe().transform(obj.bieCardinalityMax),
          disabled: disabled
        }, [
          Validators.required,
          Validators.pattern('[0-9]+|-1|unbounded'),
          // validatorFn for minimum value
          (control: AbstractControl): ValidationErrors | null => {
            let controlValue = control.value;
            // tslint:disable-next-line:max-line-length
            controlValue = (controlValue === 'unbounded') ? -1 : (isNumber(controlValue) ? controlValue : Number.parseInt(controlValue, 10));

            if (!controlValue || controlValue === -1) {
              return null;
            }
            if (controlValue < obj.bieCardinalityMin) {
              return {'min': 'Cardinality Max must be greater than ' + obj.bieCardinalityMin};
            }
            return null;
          },
          // validatorFn for maximum value
          (control: AbstractControl): ValidationErrors | null => {
            let controlValue = control.value;
            // tslint:disable-next-line:max-line-length
            controlValue = (controlValue === 'unbounded') ? -1 : (isNumber(controlValue) ? controlValue : Number.parseInt(controlValue, 10));

            if (!controlValue || obj.ccCardinalityMax === -1) {
              return null;
            }

            if ((controlValue === -1 && obj.ccCardinalityMax > -1) ||
              (controlValue > obj.ccCardinalityMax)) {
              return {'min': 'Cardinality Max must be less than or equals to ' + obj.ccCardinalityMax};
            }
            return null;
          },
        ]
      );
      this.bieCardinalityMax.valueChanges.subscribe(value => {
        if (this.bieCardinalityMax.valid) {
          value = (value === 'unbounded') ? -1 : (isNumber(value) ? value : Number.parseInt(value, 10));
          obj.bieCardinalityMax = value;
          this.bieCardinalityMin.updateValueAndValidity({onlySelf: true, emitEvent: false});
        }
      });
    }
  }

  onChange(detail?: DynamicBieFlatNode) {
    if (!detail) {
      detail = this.detailNode;
    }

    this.primitiveFilterValues(detail.item);
  }

  onChangeFixedOrDefault(value: string) {
    if (this.isBbiepDetail()) {
      if (value === 'fixed') {
        this.asBbiepDetail().bieDefaultValue = '';
      } else if ( value === 'default') {
        this.asBbiepDetail().bieFixedValue = '';
      } else {
        this.asBbiepDetail().bieDefaultValue = '';
        this.asBbiepDetail().bieFixedValue = '';
      }
    } else if (this.isBbieScDetail()) {
      if (value === 'fixed') {
        this.asBbieScDetail().bieDefaultValue = '';
      } else if ( value === 'default') {
        this.asBbieScDetail().bieFixedValue = '';
      } else {
        this.asBbieScDetail().bieDefaultValue = '';
        this.asBbieScDetail().bieFixedValue = '';
      }
    }
  }

  onHideUnusedChange() {
    // Issue #768
    this.updateDetails();
    this.dataSource.clear();

    this.service.getRootNode(this.rootNode.item.topLevelAsbiepId).subscribe((resp: BieEditAbieNode) => {
      this.rootNode = new DynamicBieFlatNode(resp);
      this.dataSource.data = [this.rootNode];

      this.onClick(this.rootNode);

      const expanded = this.treeControl.isExpanded(this.rootNode);
      this.dataSource.toggleNode(this.rootNode, false);
      if (expanded) {
        this.dataSource.toggleNode(this.rootNode, true);
      }
    });
  }

  get details(): DynamicBieFlatNode[] {
    return Array.from(this.dataSource.dataDetailMap.values());
  }

  get isChanged(): boolean {
    for (const detail of this.details) {
      if (detail.isChanged()) {
        return true;
      }
    }
    return false;
  }

  get isBusinessContextRemovable(): boolean {
    return (!this.businessContextUpdating && this.businessContexts.length > 1);
  }

  _loadAllBusinessContexts() {
    const request = new BusinessContextListRequest();
    request.page = new PageRequest('name', 'asc', -1, -1);
    this.businessContextService.getBusinessContextList(request)
      .subscribe(resp => {
        this.allBusinessContexts = resp.list;
      });
  }

  _filter(name?: string) {
    const prevBizCtxNames = this.businessContexts.map(e => e.name);
    let l = this.allBusinessContexts.filter(e => !prevBizCtxNames.includes(e.name));
    if (name) {
      l = l.filter(e => e.name.toLowerCase().indexOf(name) === 0);
    }
    return l;
  }

  removeBusinessContext(businessContext: BusinessContext) {
    this.businessContextUpdating = true;
    this.businessContextService.dismiss(this.rootNode.item.topLevelAsbiepId, businessContext)
      .subscribe(_ => {
        this.businessContexts = this.businessContexts.filter(e => e.bizCtxId !== businessContext.bizCtxId);
        this.businessContextUpdating = false;
        this.businessContextCtrl.setValue(null);
        this.snackBar.open('Updated', '', {
          duration: 1000,
        });
      }, err => {
        this.businessContextUpdating = false;
      });
  }

  addBusinessContext(event: MatAutocompleteSelectedEvent): void {
    const selectedBusinessContext: BusinessContext = event.option.value;
    this.businessContextService.assign(this.rootNode.item.topLevelAsbiepId, selectedBusinessContext)
      .subscribe(_ => {
        this.businessContexts = this.businessContexts.concat(selectedBusinessContext);
        this.businessContextUpdating = false;
        this.businessContextCtrl.setValue(null);
        this.snackBar.open('Updated', '', {
          duration: 1000,
        });
      }, err => {
        this.businessContextUpdating = false;
      });
  }

  _arrange(detail: BieEditNodeDetail) {

    switch (detail.type) {
      case 'asbiep':
        const asbiep = new BieEditAsbiepNodeDetail(detail as BieEditAsbiepNodeDetail);

        asbiep.associationDefinition = null;
        asbiep.componentDefinition = null;
        asbiep.typeDefinition = null;

        return asbiep;

      case 'bbiep':
        const bbiep = new BieEditBbiepNodeDetail(detail as BieEditBbiepNodeDetail);

        bbiep.bdtDen = null;
        bbiep.xbtList = null;
        bbiep.codeLists = null;
        bbiep.agencyIdLists = null;
        bbiep.associationDefinition = null;
        bbiep.componentDefinition = null;

        switch (bbiep.primitiveType) {
          case PrimitiveType.Primitive:
            bbiep.codeListId = null;
            bbiep.agencyIdListId = null;
            break;
          case PrimitiveType.Code:
            bbiep.bdtPriRestriId = null;
            bbiep.agencyIdListId = null;
            break;
          case PrimitiveType.Agency:
            bbiep.bdtPriRestriId = null;
            bbiep.codeListId = null;
            break;
        }

        bbiep.primitiveType = null;
        bbiep.primitiveTypes = null;

        return bbiep;

      case 'bbie_sc':
        const bbieSc = new BieEditBbieScNodeDetail(detail as BieEditBbieScNodeDetail);

        bbieSc.xbtList = null;
        bbieSc.codeLists = null;
        bbieSc.agencyIdLists = null;
        bbieSc.componentDefinition = null;

        switch (bbieSc.primitiveType) {
          case PrimitiveType.Primitive:
            bbieSc.codeListId = null;
            bbieSc.agencyIdListId = null;
            break;
          case PrimitiveType.Code:
            bbieSc.dtScPriRestriId = null;
            bbieSc.agencyIdListId = null;
            break;
          case PrimitiveType.Agency:
            bbieSc.dtScPriRestriId = null;
            bbieSc.codeListId = null;
            break;
        }

        bbieSc.primitiveType = null;
        bbieSc.primitiveTypes = null;

        return bbieSc;
    }

    return detail;
  }

  updateDetails() {
    if (!this.isChanged || this.isUpdating) {
      return;
    }

    const details: BieEditNodeDetail[] =
      this.details.filter((e: DynamicBieFlatNode) => e.isChanged())
        .map(e => e.item);

    /*
     * Issue #762
     *
     * It must be set one primtive type among 'DT', 'Code', or 'Agency'.
     */
    details.forEach((e: BieEditNodeDetail) => {
      switch (e.type) {
        case 'bbiep':
          switch (this.asBbiepDetail(e).primitiveType) {
            case 'Primitive':
              this.asBbiepDetail(e).codeListId = null;
              this.asBbiepDetail(e).agencyIdListId = null;
              break;
            case 'Code':
              this.asBbiepDetail(e).bdtPriRestriId = null;
              this.asBbiepDetail(e).agencyIdListId = null;
              break;
            case 'Agency':
              this.asBbiepDetail(e).bdtPriRestriId = null;
              this.asBbiepDetail(e).codeListId = null;
              break;
          }
          break;
        case 'bbie_sc':
          switch (this.asBbieScDetail(e).primitiveType) {
            case 'Primitive':
              this.asBbieScDetail(e).codeListId = null;
              this.asBbieScDetail(e).agencyIdListId = null;
              break;
            case 'Code':
              this.asBbieScDetail(e).dtScPriRestriId = null;
              this.asBbieScDetail(e).agencyIdListId = null;
              break;
            case 'Agency':
              this.asBbieScDetail(e).dtScPriRestriId = null;
              this.asBbieScDetail(e).codeListId = null;
              break;
          }
          break;
      }
    });
    this.isUpdating = true;

    this.service.updateDetails(this.rootNode.item.topLevelAsbiepId, details)
      .subscribe((resp: BieEditUpdateResponse) => {
        for (const detail of this.details) {
          switch (detail.item.type) {
            case 'abie':
              if (resp.abieNodeResult) {
                this.dataSource.resetDetail(detail);
              }
              break;
            case 'asbiep':
              if (resp.asbiepNodeResults[detail.item.guid]) {
                this.dataSource.resetDetail(detail);
              }
              break;
            case 'bbiep':
              if (resp.bbiepNodeResults[detail.item.guid]) {
                this.dataSource.resetDetail(detail);
              }
              break;
            case 'bbie_sc':
              if (resp.bbieScNodeResults[detail.item.guid]) {
                this.dataSource.resetDetail(detail);
              }
              break;
          }
        }

        this.snackBar.open('Updated', '', {
          duration: 1000,
        });
        this.isUpdating = false;
      }, err => {

        this.isUpdating = false;
      });
  }

  updateState(state: string) {
    const dialogConfig = new MatDialogConfig();

    if (state === 'Published') {
      const dialogRef = this.dialog.open(BieEditPublishDialogDetailComponent, dialogConfig);
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this._doUpdateState(state);
        }
      });
    } else {
      this._doUpdateState(state);
    }
  }

  _doUpdateState(state: string) {
    this.isUpdating = true;
    this.service.setState(this.rootNode.item.topLevelAsbiepId, state).subscribe(_ => {
      (this.rootNode.item as BieEditAbieNode).topLevelAsbiepState = state;
      this.isUpdating = false;
      this.resetCardinalities();

      this.snackBar.open('State updated', '', {
        duration: 1000,
      });
    }, err => {
      this.isUpdating = false;
    });
  }

  /* For type casting of detail property */
  isAbieDetail(detail?: BieEditNodeDetail): boolean {
    if (!detail) {
      detail = this.detailNode && this.detailNode.item;
    }
    return (detail !== undefined) && (detail.type === 'abie');
  }

  asAbieDetail(detail?: BieEditNodeDetail): BieEditAbieNodeDetail {
    if (!detail) {
      detail = this.detailNode && this.detailNode.item;
    }
    return detail as BieEditAbieNodeDetail;
  }

  isAsbiepDetail(detail?: BieEditNodeDetail): boolean {
    if (!detail) {
      detail = this.detailNode && this.detailNode.item;
    }
    return (detail !== undefined) && (detail.type === 'asbiep');
  }

  asAsbiepDetail(detail?: BieEditNodeDetail): BieEditAsbiepNodeDetail {
    if (!detail) {
      detail = this.detailNode && this.detailNode.item;
    }
    return detail as BieEditAsbiepNodeDetail;
  }

  isBbiepDetail(detail?: BieEditNodeDetail): boolean {
    if (!detail) {
      detail = this.detailNode && this.detailNode.item;
    }
    return (detail !== undefined) && (detail.type === 'bbiep');
  }

  asBbiepDetail(detail?: BieEditNodeDetail): BieEditBbiepNodeDetail {
    if (!detail) {
      detail = this.detailNode && this.detailNode.item;
    }
    return detail as BieEditBbiepNodeDetail;
  }

  isBbieScDetail(detail?: BieEditNodeDetail): boolean {
    if (!detail) {
      detail = this.detailNode && this.detailNode.item;
    }
    return (detail !== undefined) && (detail.type === 'bbie_sc');
  }

  asBbieScDetail(detail?: BieEditNodeDetail): BieEditBbieScNodeDetail {
    if (!detail) {
      detail = this.detailNode && this.detailNode.item;
    }
    return detail as BieEditBbieScNodeDetail;
  }

  isEditable() {
    return (this.state === 'Editing' && this.access === 'CanEdit');
  }

  isReflectValue(detail?: BieEditNodeDetail) {
    if (!detail) {
      detail = this.detailNode && this.detailNode.item;
    }
    if (this.isBbiepDetail(detail)) {
      return this.asBbiepDetail(detail).ccDefaultValue || this.asBbiepDetail(detail).ccFixedValue;
    } else {
      return this.asBbieScDetail(detail).ccDefaultValue || this.asBbieScDetail(detail).ccFixedValue;
    }
  }

  isValid() {
    if (this.bieCardinalityMin === undefined || this.bieCardinalityMax === undefined) {
      return true;
    }
    return !this.bieCardinalityMin.invalid && !this.bieCardinalityMax.invalid;
  }

  isAsbiep(node: DynamicBieFlatNode) {
    const nodeItem: BieEditNode = node.item;
    let typeStr = nodeItem.type;
    return typeStr.startsWith('asbiep');
  }

  get userToken() {
    return this.authService.getUserToken();
  }

  get isDeveloper(): boolean {
    const userToken = this.userToken;
    return userToken.role === 'developer';
  }

  canCreateBIEFromThis(node: DynamicBieFlatNode): boolean {
    return !!node && node.isEditable && node.isAsbiep && !node.item.locked && !node.item.derived;
  }

  canReuseBIE(node: DynamicBieFlatNode): boolean {
    return !!node && node.isEditable && node.isAsbiep && !node.item.locked && !node.item.derived;
  }

  canRemoveReusedBIE(node: DynamicBieFlatNode): boolean {
    return !!node && node.isEditable && node.isAsbiep && !node.item.locked && node.item.derived;
  }

  canSeeReuseContextMenus(node: DynamicBieFlatNode): boolean {
    return !!node && node.isEditable && node.isAsbiep && !node.item.locked;
  }

  openNewEditBieTab(node: DynamicBieFlatNode) {
    const asbiepNode = (node.item as BieEditAsbiepNode);
    window.open('/profile_bie/edit/' + asbiepNode.topLevelAsbiepId, '_blank');
  }

  onContextMenu($event: MouseEvent, node: DynamicBieFlatNode): void {
    if (!this.isAsbiep(node) || node.item.locked) {
      return;
    }
    let ctxMenu;
    if (this.isEditable()) {
      if (!this.isDeveloper && this.type(node) === 'asbiep-extension') {
        ctxMenu = this.extensionContextMenu;
      } else {
        ctxMenu = this.defaultContextMenu;
      }
    } else {
      ctxMenu = this.createBieContextMenu;
    }

    this.contextMenuService.show.next({
      contextMenu: ctxMenu,
      event: $event,
      item: node,
    });

    $event.preventDefault();
    $event.stopPropagation();
  }

  reuseBIE(node: DynamicBieFlatNode) {
    if (!this.canReuseBIE(node)) {
      return;
    }

    const asbiepNode = (node.item as BieEditAsbiepNode);
    const dialogRef = this.dialog.open(ReuseBieDialogComponent, {
      data: asbiepNode,
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    });
    dialogRef.afterClosed().subscribe(selectedTopLevelAsbiepId => {
      if (!selectedTopLevelAsbiepId) {
        return;
      }

      this.updateDetails();
      this.isUpdating = true;
      this.service.reuseBIE(asbiepNode, selectedTopLevelAsbiepId)
        .pipe(finalize(() => {
          this.isUpdating = false;
        })).subscribe(_ => {

        const parent = this.treeControl.getParent(node);
        this.dataSource.clearDescendants(parent);
        // To reload descendants info in the tree and details.
        this.treeControl.collapse(parent);
        this.treeControl.expand(parent);
        this.onClick(this.rootNode); // To clear the current detail state.

        this.snackBar.open('Reused', '', {
          duration: 1500,
        });
      });
    });
  }

  createBIEfromThis(node: DynamicBieFlatNode) {
    if (!this.canReuseBIE(node)) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Make BIE reusable?';
    dialogConfig.data.content = ['Are you sure you want to make a BIE reusable?'];
    dialogConfig.data.action = 'Make';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        } else {
          const asbiepNode = (node.item as BieEditAsbiepNode);
          this.updateDetails();
          this.isUpdating = true;
          this.service.makeReusableBIE(asbiepNode)
            .pipe(finalize(() => {
              this.isUpdating = false;
            })).subscribe(_ => {

            this.snackBar.open('Making BIE reusable request queued', '', {
              duration: 1000,
            });

            this.router.navigateByUrl('/profile_bie');
          });

        }
      });
  }

  removeReusedBIE(node: DynamicBieFlatNode) {
    if (!this.canRemoveReusedBIE(node)) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Remove reused BIE?';
    dialogConfig.data.content = ['Are you sure you want to remove reused BIE?'];
    dialogConfig.data.action = 'Remove';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }

        this.updateDetails();
        this.isUpdating = true;
        const asbiepNode = (node.item as BieEditAsbiepNode);
        this.service.removeReusedBIE(asbiepNode).pipe(finalize(() => {
          this.isUpdating = false;
        })).subscribe(_ => {
          const parent = this.treeControl.getParent(node);
          this.dataSource.clearDescendants(parent);
          // To reload descendants info in the tree and details.
          this.treeControl.collapse(parent);
          this.treeControl.expand(parent);
          this.onClick(this.rootNode); // To clear the current detail state.

          this.snackBar.open('Removed', '', {
            duration: 1500,
          });
        });
      });
  }

  createLocalAbieExtension(node: DynamicBieFlatNode) {
    if (this.type(node) !== 'asbiep-extension') {
      return;
    }

    const nodeItem: BieEditNode = node.item;
    this.service.createLocalAbieExtension(nodeItem).subscribe((resp: BieEditCreateExtensionResponse) => {
      if (resp.canEdit) {
        const commands = ['/core_component/extension/' + nodeItem.releaseId + '/' + resp.extensionId];
        this.router.navigate(commands);
      } else {
        if (resp.canView) {
          this.openConfirmDialog('/core_component/extension/' + nodeItem.releaseId + '/' + resp.extensionId);
        } else {
          this.snackBar.open('Editing extension already exist.', '', {
            duration: 1000,
          });
        }
      }
      this.isUpdating = false;
    }, err => {
      this.isUpdating = false;
    });
  }

  createGlobalAbieExtension(node: DynamicBieFlatNode) {
    if (this.type(node) !== 'asbiep-extension') {
      return;
    }

    const nodeItem: BieEditNode = node.item;
    this.service.createGlobalAbieExtension(nodeItem).subscribe((resp: BieEditCreateExtensionResponse) => {
      if (resp.canEdit) {
        const commands = ['/core_component/extension/' + nodeItem.releaseId + '/' + resp.extensionId];
        this.router.navigate(commands);
      } else {
        if (resp.canView) {
          this.openConfirmDialog('/core_component/extension/' + nodeItem.releaseId + '/' + resp.extensionId);
        } else {
          this.snackBar.open('Editing extension already exist.', '', {
            duration: 1000,
          });
        }
      }
      this.isUpdating = false;
    }, err => {
      this.isUpdating = false;
    });
  }

  openConfirmDialog(url: string) {
    const dialogRef = this.dialog.open(ExtensionConfirmDialogComponent, {
      data: {}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        window.open(url, '_blank');
      }
    });
  }

  delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

@Component({
  selector: 'score-bie-edit-publish-dialog.component',
  templateUrl: 'bie-edit-publish-dialog.component.html',
})
export class BieEditPublishDialogDetailComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}
