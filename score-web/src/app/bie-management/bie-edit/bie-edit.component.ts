import {Component, ElementRef, Inject, Injectable, OnInit, ViewChild} from '@angular/core';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {BieEditService} from './domain/bie-edit.service';
import {CollectionViewer, SelectionChange, SelectionModel} from '@angular/cdk/collections';
import {FlatTreeControl} from '@angular/cdk/tree';
import {BehaviorSubject, merge, Observable} from 'rxjs';
import {map, startWith, switchMap} from 'rxjs/operators';
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
  PrimitiveType
} from './domain/bie-edit-node';
import {ReleaseService} from '../../release-management/domain/release.service';
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig, MatSnackBar} from '@angular/material';
import {Hotkey, HotkeysService} from 'angular2-hotkeys';
import {ContextMenuComponent, ContextMenuService} from 'ngx-contextmenu';
import {GrowlService} from 'ngx-growl';
import {CodeList} from '../../code-list-management/domain/code-list';
import {BusinessContext, BusinessContextListRequest} from '../../context-management/business-context/domain/business-context';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {MatAutocomplete, MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {PageRequest} from '../../basis/basis';
import {AbstractControl, FormControl, ValidationErrors, Validators} from '@angular/forms';
import {isNumber} from 'util';
import {UnboundedPipe} from '../../common/utility';

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
    this.treeControl.expansionModel.onChange.subscribe(change => {
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
      detailNode.item.used = node.item.used;
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
  selector: 'srt-bie-edit',
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

  rootNode: DynamicBieFlatNode;
  treeControl: CustomTreeControl<DynamicBieFlatNode>;
  dataSource: DynamicDataSource;
  codeLists: CodeList[];
  detailNode: DynamicBieFlatNode;
  selectedNode: DynamicBieFlatNode;
  checklistSelection = new SelectionModel<DynamicBieFlatNode>(true /* multiple */);

  /* Begin cardinality management */
  bieCardinalityMin: FormControl;
  bieCardinalityMax: FormControl;
  /* End cardinality management */

  /* Begin business context management */
  businessContextCtrl = new FormControl();
  businessContexts: BusinessContext[] = [];
  allBusinessContexts: BusinessContext[] = [];
  filteredBusinessContexts: Observable<BusinessContext[]>;

  visible = true;
  businessContextUpdating = true;
  addOnBlur = true;
  separatorKeysCodes: number[] = [ENTER, COMMA];
  @ViewChild('businessContextInput', {static: false}) businessContextInput: ElementRef<HTMLInputElement>;
  @ViewChild('matAutocomplete', {static: false}) matAutocomplete: MatAutocomplete;
  /* End business context management */

  @ViewChild(ContextMenuComponent, {static: true}) public extensionMenu: ContextMenuComponent;

  constructor(private service: BieEditService,
              private businessContextService: BusinessContextService,
              private releaseService: ReleaseService,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private growlService: GrowlService,
              private hotkeysService: HotkeysService,
              private contextMenuService: ContextMenuService,
              private dialog: MatDialog) {

  }

  ngOnInit() {
    this.hideUnused = false;
    this.treeControl = new CustomTreeControl<DynamicBieFlatNode>(this.getLevel, this.isExpandable);
    this.dataSource = new DynamicDataSource(this, this.treeControl, this.service);

    // load context scheme
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) =>
        this.service.getRootNode(params.get('id')))
    ).subscribe((resp: BieEditAbieNode) => {
      this.rootNode = new DynamicBieFlatNode(resp);
      this.dataSource.data = [this.rootNode];

      this.businessContextService.getBusinessContextsByTopLevelAbieId(this.rootNode.item.topLevelAbieId)
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
    this.hotkeysService.add(new Hotkey(['meta+s', 'ctrl+s'],
      (event: KeyboardEvent, combo: string): ExtendedKeyboardEvent => {

        this.updateDetails();

        const e: ExtendedKeyboardEvent = event;
        e.returnValue = false; // Prevent bubbling
        return e;
      }));
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
    return this.rootNode && (this.rootNode.item as BieEditAbieNode).topLevelAbieState || 'Published';
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

  onClick(node: DynamicBieFlatNode) {
    this.dataSource.loadDetail(node, (detailNode: DynamicBieFlatNode) => {
      this.selectedNode = node;
      this.detailNode = detailNode;

      this.resetCardinalities(detailNode);
    });
  }

  resetCardinalities(detailNode?: DynamicBieFlatNode) {
    this._setCardinalityMinFormControl(detailNode);
    this._setCardinalityMaxFormControl(detailNode);
    this.onChange(detailNode);
  }

  _setCardinalityMinFormControl(detailNode?: DynamicBieFlatNode) {
    if (!detailNode) {
      detailNode = this.detailNode;
    }
    if (!detailNode) {
      return;
    }

    if (detailNode && detailNode.item.type !== 'abie') {
      const disabled = !this.isEditable() || !detailNode.item.used;
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
          this._setCardinalityMaxFormControl(detailNode);
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
      const disabled = !this.isEditable() || !detailNode.item.used;
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
          this._setCardinalityMinFormControl(detailNode);
        }
      });
    }
  }

  onChange(node?: DynamicBieFlatNode) {
    if (!node) {
      node = this.detailNode;
    }
  }

  onHideUnusedChange() {
    this.dataSource.clear();
    this.onClick(this.rootNode);

    const expanded = this.treeControl.isExpanded(this.rootNode);
    this.dataSource.toggleNode(this.rootNode, false);
    if (expanded) {
      this.dataSource.toggleNode(this.rootNode, true);
    }
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
    this.businessContextService.dismiss(this.rootNode.item.topLevelAbieId, businessContext)
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
    this.businessContextService.assign(this.rootNode.item.topLevelAbieId, selectedBusinessContext)
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
    this.isUpdating = true;

    this.service.updateDetails(this.rootNode.item.topLevelAbieId, details)
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
    this.service.setState(this.rootNode.item.topLevelAbieId, state).subscribe(_ => {
      (this.rootNode.item as BieEditAbieNode).topLevelAbieState = state;
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

  isValid() {
    if (this.bieCardinalityMin === undefined || this.bieCardinalityMax === undefined) {
      return true;
    }
    return !this.bieCardinalityMin.invalid && !this.bieCardinalityMax.invalid;
  }

  onContextMenu($event: MouseEvent, item: any): void {
    if (!this.isEditable()) {
      return;
    }

    this.contextMenuService.show.next({
      contextMenu: this.extensionMenu,
      event: $event,
      item: item,
    });

    $event.preventDefault();
    $event.stopPropagation();
  }

  createLocalAbieExtension(node: DynamicBieFlatNode) {
    if (this.type(node) !== 'asbiep-extension') {
      return;
    }

    const nodeItem: BieEditNode = node.item;
    this.service.createLocalAbieExtension(nodeItem).subscribe((resp: BieEditCreateExtensionResponse) => {
      this.isUpdating = false;

      const commands = ['/core_component/extension/' + nodeItem.releaseId + '/' + resp.extensionId];
      this.router.navigate(commands);
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
      this.isUpdating = false;

      const commands = ['/core_component/extension/' + nodeItem.releaseId + '/' + resp.extensionId];
      this.router.navigate(commands);
    }, err => {
      this.isUpdating = false;
    });
  }

  delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

@Component({
  selector: 'srt-bie-edit-publish-dialog.component',
  templateUrl: 'bie-edit-publish-dialog.component.html',
})
export class BieEditPublishDialogDetailComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}
