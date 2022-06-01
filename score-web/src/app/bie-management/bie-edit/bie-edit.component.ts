import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {AbstractControl, FormControl, ValidationErrors, Validators} from '@angular/forms';
import {MatAutocomplete, MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {MatDialog} from '@angular/material/dialog';
import {PageRequest} from '../../basis/basis';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {base64Decode, UnboundedPipe} from '../../common/utility';
import {BusinessContext, BusinessContextListRequest} from '../../context-management/business-context/domain/business-context';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {ReuseBieDialogComponent} from './reuse-bie-dialog/reuse-bie-dialog.component';
import {BieEditNode, UsedBie, ValueDomain, ValueDomainType} from './domain/bie-edit-node';
import {BieEditService} from './domain/bie-edit.service';
import {finalize, map, startWith, switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {Location} from '@angular/common';
import {AuthService} from '../../authentication/auth.service';
import {forkJoin, Observable, ReplaySubject} from 'rxjs';
import {CcGraph} from '../../cc-management/domain/core-component-node';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ContextMenuComponent, ContextMenuService} from 'ngx-contextmenu';
import {BieDetailUpdateRequest, BieEditAbieNode, BieEditCreateExtensionResponse, RefBie} from '../bie-edit/domain/bie-edit-node';
import {
  AbieFlatNode, AsbiepDetail,
  AsbiepFlatNode,
  BbiepFlatNode,
  BbieScFlatNode,
  BdtDetail,
  BieDataSourceSearcher,
  BieEditAbieNodeDetail,
  BieEditAsbiepNodeDetail,
  BieEditBbiepNodeDetail,
  BieEditBbieScNodeDetail,
  BieFlatNode,
  BieFlatNodeFlattener,
  ChangeListener,
  VSBieFlatTreeControl,
  VSBieFlatTreeDataSource
} from '../domain/bie-flat-tree';

export class VSBieEditTreeDataSource<T extends BieFlatNode> extends VSBieFlatTreeDataSource<T> implements ChangeListener<T> {

  changedNodes: T[] = [];

  constructor(treeControl: VSBieFlatTreeControl<T>, data: T[],
              public service: BieEditService,
              listeners?: ChangeListener<T>[]) {
    super(treeControl, data);
    data.forEach(e => {
      e.addChangeListener(this);
      if (listeners) {
        listeners.forEach(listener => e.addChangeListener(listener));
      }
    });
  }

  onChange(entity: T, propertyName: string, val: any) {
    const idx = this.changedNodes.indexOf(entity);
    if (entity.isChanged) {
      if (idx === -1) {
        this.changedNodes.push(entity);
      }
    } else {
      if (idx > -1) {
        this.changedNodes.splice(idx, 1);
      }
    }
  }

  getNodeFullIndex(node: T) {
    return this.cachedData.indexOf(node);
  }

  getNodeByFullIndex(index: number) {
    return this.cachedData.length > index ? this.cachedData[index] : undefined;
  }

  loadDetail(node: BieFlatNode, callbackFn?) {
    if (node.detail.isLoaded) {
      return callbackFn && callbackFn(node);
    }

    switch (node.bieType.toUpperCase()) {
      case 'ABIE':
        forkJoin([
          this.service.getDetail(node.topLevelAsbiepId, 'ABIE',
            (node as AbieFlatNode).accNode.manifestId, (node as AbieFlatNode).abiePath),
          this.service.getDetail(node.topLevelAsbiepId, 'ASBIEP',
            (node as AbieFlatNode).asccpNode.manifestId, (node as AbieFlatNode).asbiepPath),
        ]).subscribe(([abieDetail, asbiepDetail]) => {
          (node.detail as BieEditAbieNodeDetail).acc = (abieDetail as unknown as BieEditAbieNodeDetail).acc;
          (node.detail as BieEditAbieNodeDetail).abie.update((abieDetail as unknown as BieEditAbieNodeDetail).abie);
          (node.detail as BieEditAbieNodeDetail).asccp = (asbiepDetail as unknown as BieEditAbieNodeDetail).asccp;
          (node.detail as BieEditAbieNodeDetail).asbiep.update((asbiepDetail as unknown as BieEditAbieNodeDetail).asbiep);
          (node.detail as BieEditAbieNodeDetail).reset();
          node.detail.isLoaded = true;
          return callbackFn && callbackFn(node);
        });
        break;

      case 'ASBIEP':
        let topLevelAsbiepId = node.topLevelAsbiepId;
        if (node.derived) {
          topLevelAsbiepId = (node.parent as AsbiepFlatNode).topLevelAsbiepId;
        }
        forkJoin([
          this.service.getDetail(topLevelAsbiepId, 'ASBIE',
            (node as AsbiepFlatNode).asccNode.manifestId, (node as AsbiepFlatNode).asbiePath),
          this.service.getDetail(node.topLevelAsbiepId, 'ASBIEP',
            (node as AsbiepFlatNode).asccpNode.manifestId, (node as AsbiepFlatNode).asbiepPath),
          this.service.getDetail(node.topLevelAsbiepId, 'ABIE',
            (node as AsbiepFlatNode).accNode.manifestId, (node as AsbiepFlatNode).abiePath),
        ]).subscribe(([asbieDetail, asbiepDetail, abieDetail]) => {
          const stored = (node.detail as BieEditAsbiepNodeDetail).asbie.cardinalityMax;
          (node.detail as BieEditAsbiepNodeDetail).ascc = (asbieDetail as unknown as BieEditAsbiepNodeDetail).ascc;
          (node.detail as BieEditAsbiepNodeDetail).asbie.update((asbieDetail as unknown as BieEditAsbiepNodeDetail).asbie);
          (node.detail as BieEditAsbiepNodeDetail).asccp = (asbiepDetail as unknown as BieEditAsbiepNodeDetail).asccp;
          (node.detail as BieEditAsbiepNodeDetail).asbiep.update((asbiepDetail as unknown as BieEditAsbiepNodeDetail).asbiep);
          (node.detail as BieEditAsbiepNodeDetail).acc = (abieDetail as unknown as BieEditAsbiepNodeDetail).acc;
          (node.detail as BieEditAsbiepNodeDetail).abie.update((abieDetail as unknown as BieEditAsbiepNodeDetail).abie);
          (node.detail as BieEditAsbiepNodeDetail).reset();
          node.detail.isLoaded = true;
          if (stored != undefined) {
            (node.detail as BieEditAsbiepNodeDetail).asbie.cardinalityMax = stored;
          }
          return callbackFn && callbackFn(node);
        });
        break;
      case 'BBIEP':
        forkJoin([
          this.service.getDetail(node.topLevelAsbiepId, 'BBIE',
            (node as BbiepFlatNode).bccNode.manifestId, (node as BbiepFlatNode).bbiePath),
          this.service.getDetail(node.topLevelAsbiepId, 'BBIEP',
            (node as BbiepFlatNode).bccpNode.manifestId, (node as BbiepFlatNode).bbiepPath),
          this.service.getDetail(node.topLevelAsbiepId, 'DT',
            (node as BbiepFlatNode).bdtNode.manifestId, ''),
        ]).subscribe(([bbieDetail, bbiepDetail, bdtDetail]) => {
          const stored = (node.detail as BieEditBbiepNodeDetail).bbie.cardinalityMax;
          (node.detail as BieEditBbiepNodeDetail).bcc = (bbieDetail as unknown as BieEditBbiepNodeDetail).bcc;
          (node.detail as BieEditBbiepNodeDetail).bbie.update((bbieDetail as unknown as BieEditBbiepNodeDetail).bbie);
          (node.detail as BieEditBbiepNodeDetail).bccp = (bbiepDetail as unknown as BieEditBbiepNodeDetail).bccp;
          (node.detail as BieEditBbiepNodeDetail).bbiep.update((bbiepDetail as unknown as BieEditBbiepNodeDetail).bbiep);
          (node.detail as BieEditBbiepNodeDetail).bdt = bdtDetail as unknown as BdtDetail;
          (node.detail as BieEditBbiepNodeDetail).reset();
          node.detail.isLoaded = true;
          if (stored != undefined) {
            (node.detail as BieEditBbiepNodeDetail).bbie.cardinalityMax = stored;
          }
          return callbackFn && callbackFn(node);
        });
        break;
      case 'BBIE_SC':
        forkJoin([
          this.service.getDetail(node.topLevelAsbiepId, 'BBIE_SC',
            (node as BbieScFlatNode).bdtScNode.manifestId, (node as BbieScFlatNode).bbieScPath),
          this.service.getDetail(node.topLevelAsbiepId, 'DT',
            (node as BbieScFlatNode).bccNode.manifestId, ''),
        ]).subscribe(([bbieScDetail, bdtDetail]) => {
          const stored = (node.detail as BieEditBbieScNodeDetail).bbieSc.cardinalityMax;
          (node.detail as BieEditBbieScNodeDetail).bdtSc = (bbieScDetail as unknown as BieEditBbieScNodeDetail).bdtSc;
          (node.detail as BieEditBbieScNodeDetail).bbieSc.update((bbieScDetail as unknown as BieEditBbieScNodeDetail).bbieSc);
          (node.detail as BieEditBbieScNodeDetail).bdt = bdtDetail as unknown as BdtDetail;
          (node.detail as BieEditBbieScNodeDetail).reset();
          node.detail.isLoaded = true;
          if (stored != undefined) {
            (node.detail as BieEditBbieScNodeDetail).bbieSc.cardinalityMax = stored;
          }
          return callbackFn && callbackFn(node);
        });
        break;
    }
  }

  getRootNode(): BieFlatNode {
    return this.data[0];
  }

  getChanged(): BieFlatNode[] {
    return this.cachedData.filter(e => e.isChanged);
  }
}

export class BieEditFlatNodeFlattener extends BieFlatNodeFlattener {

  private _usedAsbieMap = {};
  private _usedBbieMap = {};
  private _usedBbieScMap = {};
  private _refBieList: RefBie[] = [];

  constructor(ccGraph: CcGraph, asccpManifestId: number, topLevelAsbiepId: number,
              usedBieList: UsedBie[], refBieList: RefBie[]) {
    super(ccGraph, asccpManifestId, topLevelAsbiepId);

    this._usedAsbieMap = usedBieList.filter(e => e.type === 'ASBIE').reduce((r, a) => {
      r[a.manifestId] = [...r[a.manifestId] || [], a];
      return r;
    }, {});
    this._usedBbieMap = usedBieList.filter(e => e.type === 'BBIE').reduce((r, a) => {
      r[a.manifestId] = [...r[a.manifestId] || [], a];
      return r;
    }, {});
    this._usedBbieScMap = usedBieList.filter(e => e.type === 'BBIE_SC').reduce((r, a) => {
      r[a.manifestId] = [...r[a.manifestId] || [], a];
      return r;
    }, {});
    this._refBieList = refBieList;
  }

  afterAsbiepFlatNode(node: AsbiepFlatNode) {
    if (node.used === undefined) {
      node.used = new Observable(subscriber => {
        let used = this._usedAsbieMap[node.asccNode.manifestId];
        if (!used || used.length === 0) {
          subscriber.next(false);
        } else {
          if (!node.locked && !(node.parent as BieFlatNode).used) {
            subscriber.next(false);
          } else {
            used = used.filter(u => {
              if (node.derived) {
                return u.ownerTopLevelAsbiepId === (node.parent as AbieFlatNode).topLevelAsbiepId &&
                  u.hashPath === node.asbieHashPath;
              } else {
                return u.ownerTopLevelAsbiepId === node.topLevelAsbiepId &&
                  u.hashPath === node.asbieHashPath;
              }
            });
            subscriber.next(!!used && used.length > 0 ? true : false);
          }
        }
        subscriber.complete();
      });
    }

    let derived = this._refBieList.filter(u => u.basedAsccManifestId === node.asccNode.manifestId);
    if (!!derived && derived.length > 0) {
      derived = derived.filter(u => u.hashPath === node.asbieHashPath);
    }
    node.derived = !!derived && derived.length > 0;
    if (node.derived) {
      node.topLevelAsbiepId = derived[0].refTopLevelAsbiepId;
    }
  }

  afterBbiepFlatNode(node: BbiepFlatNode) {
    if (node.used === undefined) {
      node.used = new Observable(subscriber => {
        let used = this._usedBbieMap[node.bccNode.manifestId];
        if (!used || used.length === 0) {
          subscriber.next(false);
        } else {
          if (!node.locked && !(node.parent as BieFlatNode).used) {
            subscriber.next(false);
          } else {
            used = used.filter(u => u.ownerTopLevelAsbiepId === node.topLevelAsbiepId && u.hashPath === node.bbieHashPath);
            subscriber.next(!!used && used.length > 0 ? true : false);
          }
        }
        subscriber.complete();
      });
    }
  }

  afterBbieScFlatNode(node: BbieScFlatNode) {
    if (node.used === undefined) {
      node.used = new Observable(subscriber => {
        let used = this._usedBbieScMap[node.bdtScNode.manifestId];
        if (!used || used.length === 0) {
          subscriber.next(false);
        } else {
          if (!node.locked && !(node.parent as BieFlatNode).used) {
            subscriber.next(false);
          } else {
            used = used.filter(u => u.ownerTopLevelAsbiepId === node.topLevelAsbiepId && u.hashPath === node.hashPath);
            subscriber.next(!!used && used.length > 0 ? true : false);
          }
        }
        subscriber.complete();
      });
    }
  }
}

@Component({
  selector: 'score-bie-edit',
  templateUrl: './bie-edit.component.html',
  styleUrls: ['./bie-edit.component.css']
})
export class BieEditComponent implements OnInit, ChangeListener<BieFlatNode> {

  loading: boolean = false;
  paddingPixel = 12;

  topLevelAsbiepId: number;
  rootNode: BieEditAbieNode;

  innerY: number = window.innerHeight;
  dataSource: VSBieEditTreeDataSource<BieFlatNode>;
  treeControl: VSBieFlatTreeControl<BieFlatNode>;
  searcher: BieDataSourceSearcher;
  excludeSCs: boolean = true;
  initialExpandDepth: number = 10;

  cursorNode: BieFlatNode;
  selectedNode: BieFlatNode;
  isUpdating: boolean = false;
  _versionChanged: boolean;
  _changedVersionValue: string;

  /* Begin business context management */
  businessContextCtrl: FormControl;
  businessContexts: BusinessContext[] = [];
  allBusinessContexts: BusinessContext[] = [];
  filteredBusinessContexts: Observable<BusinessContext[]>;
  businessContextUpdating = true;
  addOnBlur = true;
  separatorKeysCodes: number[] = [ENTER, COMMA];

  /* reused BIE */
  reusedBusinessContexts: BusinessContext[] = [];
  reusedNode: BieEditNode;
  reusedNodeDetail: BieEditAsbiepNodeDetail;

  /* cardinality management */
  bieCardinalityMin: FormControl;
  bieCardinalityMax: FormControl;

  /* valueDomain */
  valueDomainFilterCtrl: FormControl = new FormControl();
  filteredValueDomains: ReplaySubject<ValueDomain[]> = new ReplaySubject<ValueDomain[]>(1);
  valueDomainTypes = [ValueDomainType.Primitive, ValueDomainType.Code, ValueDomainType.Agency];

  usedBieList: UsedBie[];

  @ViewChild('virtualScroll', {static: true}) public virtualScroll: CdkVirtualScrollViewport;
  virtualScrollItemSize: number = 33;

  get minBufferPx(): number {
    return Math.max(this.dataSource.data[0].children.length, 20) * this.virtualScrollItemSize;
  }

  get maxBufferPx(): number {
    return Math.max(this.dataSource.data[0].children.length, 20) * 20 * this.virtualScrollItemSize;
  }

  @ViewChild('defaultContextMenu', {static: true}) public defaultContextMenu: ContextMenuComponent;
  @ViewChild('developerContextMenu', {static: true}) public developerContextMenu: ContextMenuComponent;
  @ViewChild('endUserContextMenu', {static: true}) public endUserContextMenu: ContextMenuComponent;
  @ViewChild('businessContextInput') businessContextInput: ElementRef<HTMLInputElement>;
  @ViewChild('matAutocomplete') matAutocomplete: MatAutocomplete;

  constructor(private service: BieEditService,
              private bizCtxService: BusinessContextService,
              private snackBar: MatSnackBar,
              private contextMenuService: ContextMenuService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private auth: AuthService) {

  }

  ngOnInit(): void {
    this.loading = true;
    const q = (this.route.snapshot.queryParamMap) ? this.route.snapshot.queryParamMap.get('q') : undefined;
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        this.topLevelAsbiepId = parseInt(params.get('id'), 10);
        return forkJoin([
          this.service.getGraphNode(this.topLevelAsbiepId),
          this.service.getUsedBieList(this.topLevelAsbiepId),
          this.service.getRefBieList(this.topLevelAsbiepId),
          this.service.getRootNode(this.topLevelAsbiepId),
          this.bizCtxService.getBusinessContextsByTopLevelAsbiepId(this.topLevelAsbiepId)
        ]);
      })).subscribe(([ccGraph, usedBieList, refBieList, rootNode, bizCtxResp]) => {
      this.initRootNode(rootNode);

      this.usedBieList = usedBieList;

      this.businessContextCtrl = new FormControl({
        disabled: !this.canEdit
      });
      this.businessContexts = bizCtxResp.list;
      this.businessContextUpdating = false;
      this.filteredBusinessContexts = this.businessContextCtrl.valueChanges.pipe(
        startWith(null),
        map((value: string | null) => value ? this._filter(value) : this._filter()));
      this._loadAllBusinessContexts();

      const flattener = new BieEditFlatNodeFlattener(
        ccGraph, rootNode.asccpManifestId, this.topLevelAsbiepId, usedBieList, refBieList);
      setTimeout(() => {
        const nodes = flattener.flatten(this.excludeSCs, this.initialExpandDepth);
        this.treeControl = new VSBieFlatTreeControl<BieFlatNode>(undefined, undefined, undefined, this.excludeSCs ? flattener : undefined);
        this.dataSource = new VSBieEditTreeDataSource(this.treeControl, nodes, this.service, [this,]);
        this.loading = false;
        nodes[0].used = true;
        nodes[0].reset();

        this.searcher = new BieDataSourceSearcher(this.dataSource, this.excludeSCs);
        this.onClick(nodes[0]);
        if (!!q) {
          this.selectedNode = nodes[0];
          this.searcher.inputKeyword = base64Decode(q);
          this.search(this.searcher.inputKeyword);
        }
      }, 0);
    });
  }

  initRootNode(rootNode) {
    this.rootNode = new BieEditAbieNode(rootNode);
    this.rootNode.reset();
    const that = this;
    this.rootNode.listeners.push(new class implements ChangeListener<BieEditNode> {
      onChange(entity: BieEditNode, propertyName: string, val: any) {
        if (propertyName === 'version') {
          that._versionChanged = true;
          that._changedVersionValue = val;

          that.assignVersionToVersionIdIfPossible();
        }
      }
    });
  }

  onResize(event) {
    this.innerY = window.innerHeight;
  }

  get innerHeight(): number {
    return this.innerY - 200;
  }

  get canEdit(): boolean {
    return this.state === 'WIP' && this.access === 'CanEdit';
  }

  get isValid(): boolean {
    if (this.selectedNode && this.selectedNode.used) {
      if (!!this.bieCardinalityMin) {
        if (!this.bieCardinalityMin.valid) {
          return false;
        }
      }
      if (!!this.bieCardinalityMax) {
        if (!this.bieCardinalityMax.valid) {
          return false;
        }
      }
    }
    return true;
  }

  isEditable(node: BieFlatNode): boolean {
    return this.canEdit && node.used === true && !node.locked && !node.isCycle;
  }

  isTreeEditable(node: BieFlatNode): boolean {
    return this.canEdit && !node.locked && !node.isCycle;
  }

  isUsable(node: BieFlatNode): boolean {
    return this.canEdit && !node.locked && !node.required && !node.isCycle;
  }

  isUsableChildren(node: BieFlatNode): boolean {
    return !node.locked && !node.required && !node.isCycle && !node.derived;
  }

  isCardinalityEditable(node: BieFlatNode): boolean {
    return !node.locked && !node.isCycle && !node.derived;
  }

  get isChanged(): boolean {
    return this.rootNode.isChanged || this.dataSource.changedNodes.length > 0;
  }

  toggleTreeUsed(node: BieFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    if (!this.isUsable(node)) {
      return;
    }

    node.used = !node.used;
    if (node.used) {
      this.assignVersionToVersionIdIfPossible();
    }
  }

  toggleDetailUsed(detailNode?: BieFlatNode, $event?: MouseEvent) {
    if (detailNode !== undefined) {
      this.toggleTreeUsed(this.selectedNode, $event);
    }
  }

  onContextMenu($event: MouseEvent, item: BieFlatNode): void {
    let contextMenu;
    if (this.isTreeEditable(item)) {
      if (this.canExtend(item)) {
        contextMenu = this.endUserContextMenu;
      } else {
        contextMenu = this.developerContextMenu;
      }
    } else {
      contextMenu = this.defaultContextMenu;
    }
    this.contextMenuService.show.next({
      contextMenu,
      event: $event,
      item,
    });

    $event.preventDefault();
    $event.stopPropagation();
  }

  canExtend(node: BieFlatNode): boolean {
    return this.isExtension(node) && !this.isDeveloper;
  }

  isExtension(node: BieFlatNode) {
    return (node.bieType.toUpperCase() === 'ASBIEP' && node.name === 'Extension');
  }

  get isDeveloper() {
    const userToken = this.auth.getUserToken();
    return userToken.roles.includes('developer');
  }

  canCreateBIEFromThis(node: BieFlatNode): boolean {
    return !!node && node.bieType.toUpperCase() === 'ASBIEP' && !node.locked && !node.derived;
  }

  canReuseBIE(node: BieFlatNode): boolean {
    return !!node && node.bieType.toUpperCase() === 'ASBIEP' && !node.locked && !node.derived;
  }

  canRemoveReusedBIE(node: BieFlatNode): boolean {
    return !!node && node.bieType.toUpperCase() === 'ASBIEP' && !node.locked && node.derived;
  }

  canSeeReuseContextMenus(node: BieFlatNode): boolean {
    return !!node && node.bieType.toUpperCase() === 'ASBIEP' && !node.locked;
  }

  reloadTree(node: BieFlatNode) {
    const index = this.dataSource.getNodeFullIndex(node);
    this.loading = true;
    forkJoin([
      this.service.getGraphNode(this.topLevelAsbiepId),
      this.service.getUsedBieList(this.topLevelAsbiepId),
      this.service.getRefBieList(this.topLevelAsbiepId),
    ]).subscribe(([ccGraph, usedBieList, refBieList]) => {
      this.usedBieList = usedBieList;
      const flattener = new BieEditFlatNodeFlattener(
        ccGraph, this.rootNode.asccpManifestId, this.topLevelAsbiepId, usedBieList, refBieList);
      setTimeout(() => {
        const nodes = flattener.flatten(this.excludeSCs);
        this.treeControl = new VSBieFlatTreeControl<BieFlatNode>(undefined, undefined, undefined, this.excludeSCs ? flattener : undefined);
        this.dataSource = new VSBieEditTreeDataSource(this.treeControl, nodes, this.service, [this, ]);
        this.loading = false;
        nodes[0].used = true;
        nodes[0].reset();

        this.searcher = new BieDataSourceSearcher(this.dataSource, this.excludeSCs);

        const current = this.dataSource.getNodeByFullIndex(index);
        this.treeControl.collapseAll();
        current.parents.forEach(e => {
          this.treeControl.expand(e);
        });
        this.onClick(current);
      }, 0);
    });
  }

  reuseBIE(node: BieFlatNode) {
    if (!this.canReuseBIE(node)) {
      return;
    }

    const asbiepNode = (node as AsbiepFlatNode);
    const dialogRef = this.dialog.open(ReuseBieDialogComponent, {
      data: {
        asccpManifestId: asbiepNode.asccpNode.manifestId,
        releaseId: this.rootNode.releaseId,
        topLevelAsbiepId: this.topLevelAsbiepId
      },
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
      this.updateDetails(asbiepNode.parents, () => {
        this.isUpdating = true;
        this.service.reuseBIE(asbiepNode, selectedTopLevelAsbiepId)
          .pipe(finalize(() => this.isUpdating = false)).subscribe(__ => {
          this.reloadTree(node);
        });
      });
    });
  }

  removeReusedBIE(node: BieFlatNode) {
    if (!this.canRemoveReusedBIE(node)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove reused BIE?';
    dialogConfig.data.content = ['Are you sure you want to remove reused BIE?'];
    dialogConfig.data.action = 'Remove';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .pipe(
        finalize(() => {
          this.isUpdating = false;
        })
      ).subscribe(result => {
      if (!result) {
        return;
      }

      const asbiepNode = (node as AsbiepFlatNode);

      this.isUpdating = true;
      this.service.removeReusedBIE(this.topLevelAsbiepId, asbiepNode.asbieHashPath)
        .pipe(finalize(() => {
          this.isUpdating = false;
        })).subscribe(_ => {
        this.reloadTree(node);
      });
    });
  }

  createBIEfromThis(node: BieFlatNode) {
    if (!this.canCreateBIEFromThis(node)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Make BIE reusable?';
    dialogConfig.data.content = ['Are you sure you want to make a BIE reusable?'];
    dialogConfig.data.action = 'Make';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        } else {
          const asbiepNode = node as AsbiepFlatNode;
          if (this.canEdit) {
            this.updateDetails(node.parents, () => {
              this.service.makeReusableBIE(asbiepNode.asbieHashPath, asbiepNode.topLevelAsbiepId, asbiepNode.asccpNode.manifestId)
                .pipe(finalize(() => {
                  this.isUpdating = false;
                })).subscribe(_ => {

                this.snackBar.open('Making BIE reusable request queued', '', {
                  duration: 3000,
                });

                this.router.navigateByUrl('/profile_bie');
              });
            });
          } else {
            this.service.makeReusableBIE(asbiepNode.asbieHashPath, asbiepNode.topLevelAsbiepId, asbiepNode.asccpNode.manifestId)
              .pipe(finalize(() => {
                this.isUpdating = false;
              })).subscribe(_ => {

              this.snackBar.open('Making BIE reusable request queued', '', {
                duration: 3000,
              });

              this.router.navigateByUrl('/profile_bie');
            });
          }

          this.isUpdating = true;
        }
      });
  }

  createLocalAbieExtension(node: BieFlatNode) {
    if (!this.isExtension(node)) {
      return;
    }
    if (this.isDeveloper) {
      this.snackBar.open('Developer cannot create User Extension.', '', {
        duration: 3000,
      });
      return;
    }

    const nodeItem = node as AsbiepFlatNode;
    this.service.createLocalAbieExtension(nodeItem).subscribe((resp: BieEditCreateExtensionResponse) => {
      if (resp.canEdit) {
        const commands = ['/core_component/extension/' + resp.extensionId];
        this.router.navigate(commands);
      } else {
        if (resp.canView) {
          this.openConfirmDialog('/core_component/extension/' + resp.extensionId);
        } else {
          this.snackBar.open('Editing extension already exist.', '', {
            duration: 3000,
          });
        }
      }
      this.isUpdating = false;
    }, err => {
      this.isUpdating = false;
    });
  }

  createGlobalAbieExtension(node: BieFlatNode) {
    if (!this.isExtension(node)) {
      return;
    }
    if (this.isDeveloper) {
      this.snackBar.open('Developer cannot create User Extension.', '', {
        duration: 3000,
      });
      return;
    }

    const nodeItem = node as AsbiepFlatNode;
    this.service.createGlobalAbieExtension(nodeItem).subscribe((resp: BieEditCreateExtensionResponse) => {
      if (resp.canEdit) {
        const commands = ['/core_component/extension/' + resp.extensionId];
        this.router.navigate(commands);
      } else {
        if (resp.canView) {
          this.openConfirmDialog('/core_component/extension/' + resp.extensionId);
        } else {
          this.snackBar.open('Editing extension already exist.', '', {
            duration: 3000,
          });
        }
      }
      this.isUpdating = false;
    }, err => {
      this.isUpdating = false;
    });
  }

  enableChildren(node: BieFlatNode) {
    this.treeControl.expand(node);
    node.children.forEach(e => {
      if ((e as BieFlatNode).isGroup) {
        this.enableChildren(e as BieFlatNode);
      } else {
        if ((e as BieFlatNode).used) {
          return;
        }
        this.toggleTreeUsed(e as BieFlatNode);
      }
    })
  }

  setMaxCardinality(node: BieFlatNode) {

    this.resetCardinalities(this.selectedNode);

    node.children.forEach(e => {
      const child = e as BieFlatNode;
      if (child.isGroup) {
        this.setMaxCardinality(child);
      } else {
        if (child.used) {
          switch (child.bieType) {
            case 'ASBIEP':
              (child.detail as BieEditAsbiepNodeDetail).asbie.cardinalityMax = 1;
              break;
            case 'BBIEP':
              (child.detail as BieEditBbiepNodeDetail).bbie.cardinalityMax = 1;
              break;
            case 'BBIE_SC':
              (child.detail as BieEditBbieScNodeDetail).bbieSc.cardinalityMax = 1;
              break;
          }
          this.setMaxCardinality(child);
        }
      }
    })
  }


  openConfirmDialog(url: string) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Attention!';
    dialogConfig.data.content = [
      'Another user is working on the extension.',
      'It is in the QA state. You can only review the extension.',
      'Would you like to open the extension to review?'
    ];
    dialogConfig.data.action = 'Yes';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .pipe(
        finalize(() => {
          this.isUpdating = false;
        })
      ).subscribe(result => {
      if (!result) {
        return;
      }
      window.open(url, '_blank');
    });
  }

  get state(): string {
    return this.rootNode && this.rootNode.topLevelAsbiepState || '';
  }

  get access(): string {
    return this.rootNode && this.rootNode.access || 'Unprepared';
  }

  toggle(node: BieFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.treeControl.toggle(node);
  }

  onClick(node: BieFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.dataSource.loadDetail(node, (detailNode: BieFlatNode) => {
      this.selectedNode = node;
      this.cursorNode = node;

      this.resetCardinalities(detailNode);
      this.initFixedOrDefault(detailNode);
      this.valueDomainFilterValues(detailNode);
      this.assignVersionToVersionIdIfPossible(node);

      if (node.bieType.toUpperCase() === 'ASBIEP' && node.derived) {
        this.service.getRootNode(node.topLevelAsbiepId).subscribe(resp => {
          this.reusedNode = resp as BieEditAbieNode;
          this.service.getDetail(node.topLevelAsbiepId, 'ASBIEP',
            resp.asccpManifestId, (node as AsbiepFlatNode).asbiepPath).subscribe(detail => {
            this.reusedNodeDetail = detail as BieEditAsbiepNodeDetail;
          });
        });
        this.bizCtxService.getBusinessContextsByTopLevelAsbiepId(node.topLevelAsbiepId)
          .subscribe(bizCtxResp => {
            this.reusedBusinessContexts = bizCtxResp.list;
          });
      } else {
        this.reusedNode = undefined;
        this.reusedNodeDetail = undefined;
        this.reusedBusinessContexts = [];
      }
    });
  }

  scrollToNode(node: BieFlatNode) {
    const index = this.searcher.getNodeIndex(node);
    this.scrollTree(index);
    this.cursorNode = node;
  }

  keyNavigation($event: KeyboardEvent) {
    if ($event.key === 'ArrowDown') {
      this.cursorNode = this.searcher.next(this.cursorNode);
    } else if ($event.key === 'ArrowUp') {
      this.cursorNode = this.searcher.prev(this.cursorNode);
    } else if ($event.key === 'ArrowLeft' || $event.key === 'ArrowRight') {
      this.treeControl.toggle(this.cursorNode);
    } else if ($event.key === 'Enter') {
      this.onClick(this.cursorNode);
    }
    $event.preventDefault();
    $event.stopPropagation();
  }

  scrollBreadcrumb(elementId: string) {
    const breadcrumbs = document.getElementById(elementId);
    if (breadcrumbs.scrollWidth > breadcrumbs.clientWidth) {
      breadcrumbs.scrollLeft = breadcrumbs.scrollWidth - breadcrumbs.clientWidth;
      breadcrumbs.classList.add('inner-box');
    } else {
      breadcrumbs.scrollLeft = 0;
      breadcrumbs.classList.remove('inner-box');
    }
    return '';
  }

  scrollTree(index: number) {
    const range = this.virtualScroll.getRenderedRange();
    if (range.start > index || range.end < index) {
      this.virtualScroll.scrollToOffset(index * 33);
    }
  }

  search(inputKeyword, backward?: boolean, force?: boolean) {
    this.searcher.search(inputKeyword, this.selectedNode, backward, force).subscribe(index => {
      this.virtualScroll.scrollToIndex(index);
    });
  }

  move(val: number) {
    this.searcher.go(val).subscribe(index => {
      this.virtualScroll.scrollToIndex(index);
    });
  }

  openNewEditBieTab(node: BieFlatNode) {
    window.open('/profile_bie/edit/' + node.topLevelAsbiepId, '_blank');
  }

  /* For type casting of detail property */
  isAbieDetail(node?: BieFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.bieType.toUpperCase() === 'ABIE');
  }

  asAbieDetail(node?: BieFlatNode): BieEditAbieNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as BieEditAbieNodeDetail;
  }

  isAsbiepDetail(node?: BieFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.bieType.toUpperCase() === 'ASBIEP');
  }

  asAsbiepDetail(node?: BieFlatNode): BieEditAsbiepNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as BieEditAsbiepNodeDetail;
  }

  isBbiepDetail(node?: BieFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.bieType.toUpperCase() === 'BBIEP');
  }

  asBbiepDetail(node?: BieFlatNode): BieEditBbiepNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as BieEditBbiepNodeDetail;
  }

  isBbieScDetail(node?: BieFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.bieType.toUpperCase() === 'BBIE_SC');
  }

  asBbieScDetail(node?: BieFlatNode): BieEditBbieScNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as BieEditBbieScNodeDetail;
  }

  _loadAllBusinessContexts() {
    const request = new BusinessContextListRequest();
    request.page = new PageRequest('name', 'asc', -1, -1);
    this.bizCtxService.getBusinessContextList(request)
      .subscribe(resp => {
        this.allBusinessContexts = resp.list;
      });
  }

  get isBusinessContextRemovable(): boolean {
    return (!this.businessContextUpdating && this.businessContexts.length > 1);
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
    this.bizCtxService.dismiss(this.topLevelAsbiepId, businessContext)
      .subscribe(_ => {
        this.businessContexts = this.businessContexts.filter(e => e.businessContextId !== businessContext.businessContextId);
        this.businessContextUpdating = false;
        this.businessContextCtrl.setValue(null);
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      }, err => {
        this.businessContextUpdating = false;
      });
  }

  addBusinessContext(event: MatAutocompleteSelectedEvent): void {
    const selectedBusinessContext: BusinessContext = event.option.value;
    this.bizCtxService.assign(this.topLevelAsbiepId, selectedBusinessContext)
      .subscribe(_ => {
        this.businessContexts = this.businessContexts.concat(selectedBusinessContext);
        this.businessContextUpdating = false;
        this.businessContextInput.nativeElement.value = '';
        this.businessContextCtrl.setValue(null);
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      }, err => {
        this.businessContextUpdating = false;
      });
  }

  onChange(entity: BieFlatNode, propertyName: string, val: any) {
    if (this.selectedNode === entity && propertyName === 'used') {
      this.resetCardinalities(entity);
    }
  }

  assignVersionToVersionIdIfPossible(node?: BieFlatNode) {
    if (!this._versionChanged) {
      return;
    }

    if (node === undefined) {
      node = this.versionIdDetail;
    }

    if (!node) {
      return;
    }
    if (!this.isVersionIdNode(node)) {
      return;
    }

    if (this.asBbiepDetail(node).bbie.fixedValue !== this._changedVersionValue) {
      if (!this._changedVersionValue) {
        this.asBbiepDetail(node).bbie.fixedOrDefault = 'none';
        this.asBbiepDetail(node).bbie.fixedValue = '';
        this.asBbiepDetail(node).bbie.defaultValue = '';
      } else {
        this.asBbiepDetail(node).bbie.fixedOrDefault = 'fixed';
        this.asBbiepDetail(node).bbie.fixedValue = this._changedVersionValue;
        this.asBbiepDetail(node).bbie.defaultValue = '';
      }

      this.snackBar.open('Synchronized \'Version\' value with the the fixed value.', '', {
        duration: 3000,
      });
    }

    this._versionChanged = false;
    this._changedVersionValue = undefined;
  }

  isVersionIdNode(node: BieFlatNode): boolean {
    if (node.level !== 1 || !node.used) {
      return false;
    }
    if (node.bieType.toUpperCase() !== 'BBIEP') {
      return false;
    }
    const bbiepNode = (node as BbiepFlatNode);
    if (node.name === 'Version Identifier'
      && bbiepNode.entityType === 'Attribute') {
      return true;
    }
    return false;
  }

  get versionIdDetail(): BieFlatNode | undefined {
    for (const detailValue of this.dataSource.getRootNode().children.map(e => e as BieFlatNode)) {
      if (this.isVersionIdNode(detailValue)) {
        return detailValue;
      }
    }
    return undefined;
  }

  resetCardinalities(node?: BieFlatNode) {
    if (!node) {
      node = this.selectedNode;
    }

    this._setCardinalityMinFormControl(node);
    this._setCardinalityMaxFormControl(node);
  }

  initFixedOrDefault(detail?: BieFlatNode) {
    if (this.isBbiepDetail(detail)) {
      if (this.asBbiepDetail(detail).bbie.defaultValue) {
        this.asBbiepDetail(detail).bbie.fixedOrDefault = 'default';
      } else if (this.asBbiepDetail(detail).bbie.fixedValue) {
        this.asBbiepDetail(detail).bbie.fixedOrDefault = 'fixed';
      } else {
        this.asBbiepDetail(detail).bbie.fixedOrDefault = 'none';
      }
    } else if (this.isBbieScDetail(detail)) {
      if (this.asBbieScDetail(detail).bbieSc.defaultValue) {
        this.asBbieScDetail(detail).bbieSc.fixedOrDefault = 'default';
      } else if (this.asBbieScDetail(detail).bbieSc.fixedValue) {
        this.asBbieScDetail(detail).bbieSc.fixedOrDefault = 'fixed';
      } else {
        this.asBbieScDetail(detail).bbieSc.fixedOrDefault = 'none';
      }
    }
  }

  _setCardinalityMinFormControl(detailNode?: BieFlatNode) {
    if (!detailNode) {
      detailNode = this.selectedNode;
    } else if (detailNode !== this.selectedNode) {
      return;
    }

    const disabled = !this.isEditable(this.selectedNode) ||
      !this.selectedNode.used || this.selectedNode.locked;
    // assign cardinality;
    let bieCardinalityMin;
    let bieCardinalityMax;
    let ccCardinalityMin;
    if (this.isAsbiepDetail(detailNode)) {
      bieCardinalityMin = this.asAsbiepDetail(detailNode).asbie.cardinalityMin;
      bieCardinalityMax = this.asAsbiepDetail(detailNode).asbie.cardinalityMax;
      ccCardinalityMin = this.asAsbiepDetail(detailNode).ascc.cardinalityMin;
    } else if (this.isBbiepDetail(detailNode)) {
      bieCardinalityMin = this.asBbiepDetail(detailNode).bbie.cardinalityMin;
      bieCardinalityMax = this.asBbiepDetail(detailNode).bbie.cardinalityMax;
      ccCardinalityMin = this.asBbiepDetail(detailNode).bcc.cardinalityMin;
    } else if (this.isBbieScDetail(detailNode)) {
      bieCardinalityMin = this.asBbieScDetail(detailNode).bbieSc.cardinalityMin;
      bieCardinalityMax = this.asBbieScDetail(detailNode).bbieSc.cardinalityMax;
      ccCardinalityMin = this.asBbieScDetail(detailNode).bdtSc.cardinalityMin;
    } else {
      return;
    }

    this.bieCardinalityMin = new FormControl({
        value: bieCardinalityMin,
        disabled
      }, [
        Validators.required,
        Validators.pattern('[0-9]+'),
        Validators.min(ccCardinalityMin),
        // validatorFn for maximum value
        (control: AbstractControl): ValidationErrors | null => {
          if (bieCardinalityMax === -1) {
            return null;
          }
          if (Number(control.value) > bieCardinalityMax) {
            return {max: 'Cardinality Min must be less than or equals to ' + bieCardinalityMax};
          }
          return null;
        }
      ]
    );
    this.bieCardinalityMin.valueChanges.subscribe(value => {
      if (this.bieCardinalityMin.valid) {
        value = typeof value === 'number' ? value : Number.parseInt(value, 10);
        if (this.isAsbiepDetail(detailNode)) {
          this.asAsbiepDetail(detailNode).asbie.cardinalityMin = value;
        } else if (this.isBbiepDetail(detailNode)) {
          this.asBbiepDetail(detailNode).bbie.cardinalityMin = value;
        } else if (this.isBbieScDetail(detailNode)) {
          this.asBbieScDetail(detailNode).bbieSc.cardinalityMin = value;
        } else {
          return;
        }
        this._setCardinalityMaxFormControl(detailNode);
      }
    });
  }

  _setCardinalityMaxFormControl(detailNode?: BieFlatNode) {
    if (!detailNode) {
      detailNode = this.selectedNode;
    } else if (detailNode !== this.selectedNode) {
      return;
    }

    const disabled = !this.isEditable(this.selectedNode) ||
      !this.selectedNode.used ||
      this.selectedNode.locked;
    // assign cardinality;
    let bieCardinalityMin;
    let bieCardinalityMax;
    let ccCardinalityMax;
    if (this.isAsbiepDetail(detailNode)) {
      bieCardinalityMin = this.asAsbiepDetail(detailNode).asbie.cardinalityMin;
      bieCardinalityMax = this.asAsbiepDetail(detailNode).asbie.cardinalityMax;
      ccCardinalityMax = this.asAsbiepDetail(detailNode).ascc.cardinalityMax;
    } else if (this.isBbiepDetail(detailNode)) {
      bieCardinalityMin = this.asBbiepDetail(detailNode).bbie.cardinalityMin;
      bieCardinalityMax = this.asBbiepDetail(detailNode).bbie.cardinalityMax;
      ccCardinalityMax = this.asBbiepDetail(detailNode).bcc.cardinalityMax;
    } else if (this.isBbieScDetail(detailNode)) {
      bieCardinalityMin = this.asBbieScDetail(detailNode).bbieSc.cardinalityMin;
      bieCardinalityMax = this.asBbieScDetail(detailNode).bbieSc.cardinalityMax;
      ccCardinalityMax = this.asBbieScDetail(detailNode).bdtSc.cardinalityMax;
    } else {
      return;
    }

    this.bieCardinalityMax = new FormControl({
        value: new UnboundedPipe().transform(bieCardinalityMax),
        disabled
      }, [
        Validators.required,
        Validators.pattern('[0-9]+|-1|unbounded'),
        // validatorFn for minimum value
        (control: AbstractControl): ValidationErrors | null => {
          let controlValue = control.value;
          // tslint:disable-next-line:max-line-length
          controlValue = (controlValue === 'unbounded') ? -1 : (typeof controlValue === 'number' ? controlValue : Number.parseInt(controlValue, 10));

          if (!controlValue || controlValue === -1) {
            return null;
          }
          if (controlValue < bieCardinalityMin) {
            return {min: 'Cardinality Max must be greater than ' + bieCardinalityMin};
          }
          return null;
        },
        // validatorFn for maximum value
        (control: AbstractControl): ValidationErrors | null => {
          let controlValue = control.value;
          // tslint:disable-next-line:max-line-length
          controlValue = (controlValue === 'unbounded') ? -1 : (typeof controlValue === 'number' ? controlValue : Number.parseInt(controlValue, 10));

          if (!controlValue || ccCardinalityMax === -1) {
            return null;
          }

          if ((controlValue === -1 && ccCardinalityMax > -1) ||
            (controlValue > ccCardinalityMax)) {
            return {min: 'Cardinality Max must be less than or equals to ' + ccCardinalityMax};
          }
          return null;
        },
      ]
    );
    this.bieCardinalityMax.valueChanges.subscribe(value => {
      if (this.bieCardinalityMax.valid) {
        value = (value === 'unbounded') ? -1 : (typeof value === 'number' ? value : Number.parseInt(value, 10));
        if (this.isAsbiepDetail(detailNode)) {
          this.asAsbiepDetail(detailNode).asbie.cardinalityMax = value;
        } else if (this.isBbiepDetail(detailNode)) {
          this.asBbiepDetail(detailNode).bbie.cardinalityMax = value;
        } else if (this.isBbieScDetail(detailNode)) {
          this.asBbieScDetail(detailNode).bbieSc.cardinalityMax = value;
        } else {
          return;
        }
        this._setCardinalityMinFormControl(detailNode);
      }
    });
  }

  onChangeFixedOrDefault(value: string) {
    if (this.isBbiepDetail()) {
      if (value === 'fixed') {
        this.asBbiepDetail().bbie.defaultValue = '';
      } else if (value === 'default') {
        this.asBbiepDetail().bbie.fixedValue = '';
      } else {
        this.asBbiepDetail().bbie.defaultValue = '';
        this.asBbiepDetail().bbie.fixedValue = '';
      }
    } else if (this.isBbieScDetail()) {
      if (value === 'fixed') {
        this.asBbieScDetail().bbieSc.defaultValue = '';
      } else if (value === 'default') {
        this.asBbieScDetail().bbieSc.fixedValue = '';
      } else {
        this.asBbieScDetail().bbieSc.defaultValue = '';
        this.asBbieScDetail().bbieSc.fixedValue = '';
      }
    }
  }

  isReflectValue(detail?: BieFlatNode) {
    if (!detail) {
      detail = this.selectedNode;
    }

    if (this.isBbiepDetail(detail)) {
      return !!(this.asBbiepDetail(detail).bccp.defaultValue || this.asBbiepDetail(detail).bccp.fixedValue);
    } else {
      return !!(this.asBbieScDetail(detail).bdtSc.defaultValue || this.asBbieScDetail(detail).bdtSc.fixedValue);
    }
  }

  changeValueDomainType(node: BieFlatNode) {
    switch (this.selectedNode.bieType) {
      case 'BBIEP':
        if (this.asBbiepDetail(node).bbie.valueDomainType === ValueDomainType.Primitive.toString()) {
          (node.detail as BieEditBbiepNodeDetail).bbie.codeListId = undefined;
          (node.detail as BieEditBbiepNodeDetail).bbie.agencyIdListId = undefined;
        } else if (this.asBbiepDetail(node).bbie.valueDomainType === ValueDomainType.Code.toString()) {
          (node.detail as BieEditBbiepNodeDetail).bbie.bdtPriRestriId = undefined;
          (node.detail as BieEditBbiepNodeDetail).bbie.agencyIdListId = undefined;
        } else {
          (node.detail as BieEditBbiepNodeDetail).bbie.bdtPriRestriId = undefined;
          (node.detail as BieEditBbiepNodeDetail).bbie.codeListId = undefined;
        }
        break;
      case 'BBIE_SC':
        if (this.asBbieScDetail(node).bbieSc.valueDomainType === ValueDomainType.Primitive.toString()) {
          (node.detail as BieEditBbieScNodeDetail).bbieSc.codeListId = undefined;
          (node.detail as BieEditBbieScNodeDetail).bbieSc.agencyIdListId = undefined;
        } else if (this.asBbieScDetail(node).bbieSc.valueDomainType === ValueDomainType.Code.toString()) {
          (node.detail as BieEditBbieScNodeDetail).bbieSc.bdtScPriRestriId = undefined;
          (node.detail as BieEditBbieScNodeDetail).bbieSc.agencyIdListId = undefined;
        } else {
          (node.detail as BieEditBbieScNodeDetail).bbieSc.bdtScPriRestriId = undefined;
          (node.detail as BieEditBbieScNodeDetail).bbieSc.codeListId = undefined;
        }
        break;
    }
    this.valueDomainFilterValues(node);
    this.onChange(undefined, undefined, node);
  }

  valueDomainFilterValues(detail?: BieFlatNode) {
    let valueDomains: ValueDomain[] = [];
    if (this.isBbiepDetail(detail)) {
      const bbiepNodeDetail = this.asBbiepDetail(detail);
      const bccpManifestId = bbiepNodeDetail.bccp.bccpManifestId;
      if (bbiepNodeDetail.bbie.valueDomainType === undefined) {
        bbiepNodeDetail.bbie.valueDomainType = 'Primitive';
      }
      if (this.asBbiepDetail(detail).bbie.valueDomainType === 'Code') {
        this.service.getBbiepCodeList(this.topLevelAsbiepId, bccpManifestId).subscribe(list => {
          valueDomains = list.map(e => new ValueDomain(e.codeListId, e.codeListName, e.state, e.versionId, e.deprecated));
          this._setFilteredValueDomains(valueDomains);
        });
      } else if (this.asBbiepDetail(detail).bbie.valueDomainType === 'Agency') {
        this.service.getBbiepAgencyIdList(this.topLevelAsbiepId, bccpManifestId).subscribe(list => {
          valueDomains = list.map(e => new ValueDomain(e.agencyIdListId, e.agencyIdListName, e.state, e.versionId, e.deprecated));
          this._setFilteredValueDomains(valueDomains);
        });
      } else {
        this.service.getBbiepBdtPriRestriList(this.topLevelAsbiepId, bccpManifestId).subscribe(list => {
          if (bbiepNodeDetail.bbie.bdtPriRestriId === null) {
            bbiepNodeDetail.bbie.bdtPriRestriId = list.find(e => e.default).bdtPriRestriId;
          }
          valueDomains = list.map(e => new ValueDomain(e.bdtPriRestriId, e.xbtName));
          this._setFilteredValueDomains(valueDomains);
        });
      }
    } else if (this.isBbieScDetail(detail)) {
      const bdtScManifestId = this.asBbieScDetail(detail).bdtSc.dtScManifestId;
      if (this.asBbieScDetail(detail).bbieSc.valueDomainType === undefined) {
        this.asBbieScDetail(detail).bbieSc.valueDomainType = 'Primitive';
      }
      if (this.asBbieScDetail(detail).bbieSc.valueDomainType === 'Code') {
        this.service.getBbieScCodeList(this.topLevelAsbiepId, bdtScManifestId).subscribe(list => {
          valueDomains = list.map(e => new ValueDomain(e.codeListId, e.codeListName, e.state, e.versionId, e.deprecated));
          this._setFilteredValueDomains(valueDomains);
        });
      } else if (this.asBbieScDetail(detail).bbieSc.valueDomainType === 'Agency') {
        this.service.getBbieScAgencyIdList(this.topLevelAsbiepId, bdtScManifestId).subscribe(list => {
          valueDomains = list.map(e => new ValueDomain(e.agencyIdListId, e.agencyIdListName, e.state, e.versionId, e.deprecated));
          this._setFilteredValueDomains(valueDomains);
        });
      } else {
        this.service.getBbieScBdtScPriRestriList(this.topLevelAsbiepId, bdtScManifestId).subscribe(list => {
          if (this.asBbieScDetail(detail).bbieSc.bdtScPriRestriId === null) {
            this.asBbieScDetail(detail).bbieSc.bdtScPriRestriId = list.find(e => e.default).bdtScPriRestriId;
          }
          valueDomains = list.map(e => new ValueDomain(e.bdtScPriRestriId, e.xbtName));
          this._setFilteredValueDomains(valueDomains);
        });
      }
    } else {
      return;
    }
  }

  _setFilteredValueDomains(list: ValueDomain[]) {
    this.valueDomainFilterCtrl.valueChanges.subscribe(() => {
      let search = this.valueDomainFilterCtrl.value;
      if (!search) {
        this.filteredValueDomains.next(list.slice());
        return;
      } else {
        search = search.toLowerCase();
      }

      this.filteredValueDomains.next(
        list.filter(e => e.name.toLowerCase().indexOf(search) > -1)
      );
    });
    this.filteredValueDomains.next(list.slice());
  }

  _hasPath(base: string, path): boolean {
    if (path) {
      return path.indexOf(base) === 0;
    }
    return false;
  }

  updateDetails(include?: BieFlatNode[], callbackFn?) {
    const request = new BieDetailUpdateRequest();

    let nodes = this.dataSource.getChanged();
    if (include) {
      nodes = nodes.concat(include.filter(e => !e.isChanged));
    }

    nodes.forEach(node => {
      switch (node.bieType.toUpperCase()) {
        case 'ABIE':
          request.asbiepDetails.push((node.detail as BieEditAbieNodeDetail).asbiep);
          request.abieDetails.push((node.detail as BieEditAbieNodeDetail).abie);
          break;
        case 'ASBIEP':
          if (!node.locked) {
            request.asbieDetails.push((node.detail as BieEditAsbiepNodeDetail).asbie);
            if (!node.derived) {
              request.abieDetails.push((node.detail as BieEditAsbiepNodeDetail).abie);
              request.asbiepDetails.push((node.detail as BieEditAsbiepNodeDetail).asbiep);
            }
          }
          break;
        case 'BBIEP':
          if (!node.locked) {
            switch ((node.detail as BieEditBbiepNodeDetail).bbie.valueDomainType) {
              case 'Primitive':
                if (!((node.detail as BieEditBbiepNodeDetail).bbie.bdtPriRestriId)) {
                  const message = 'Value Domain is required in ' + (node.detail as BieEditBbiepNodeDetail).bccp.propertyTerm;
                  this.snackBar.open(message, '', {
                    duration: 3000,
                  });
                  throw new Error(message);
                }
                break;
              case 'Code':
                if (!((node.detail as BieEditBbiepNodeDetail).bbie.codeListId)) {
                  const message = 'Value Domain is required in ' + (node.detail as BieEditBbiepNodeDetail).bccp.propertyTerm;
                  this.snackBar.open(message, '', {
                    duration: 3000,
                  });
                  throw new Error(message);
                }
                break;
              case 'Agency':
                if (!((node.detail as BieEditBbiepNodeDetail).bbie.agencyIdListId)) {
                  const message = 'Value Domain is required in ' + (node.detail as BieEditBbiepNodeDetail).bccp.propertyTerm;
                  this.snackBar.open(message, '', {
                    duration: 3000,
                  });
                  throw new Error(message);
                }
                break;
            }
            request.bbieDetails.push((node.detail as BieEditBbiepNodeDetail).bbie);
            request.bbiepDetails.push((node.detail as BieEditBbiepNodeDetail).bbiep);
          }
          break;
        case 'BBIE_SC':
          if (!node.locked) {
            switch ((node.detail as BieEditBbieScNodeDetail).bbieSc.valueDomainType) {
              case 'Primitive':
                if (!((node.detail as BieEditBbieScNodeDetail).bbieSc.bdtScPriRestriId)) {
                  // tslint:disable-next-line:max-line-length
                  const message = 'Value Domain is required in ' + (node.detail as BieEditBbieScNodeDetail).bdtSc.propertyTerm + ' ' + (node.detail as BieEditBbieScNodeDetail).bdtSc.representationTerm;
                  this.snackBar.open(message, '', {
                    duration: 3000,
                  });
                  throw new Error(message);
                }
                break;
              case 'Code':
                if (!((node.detail as BieEditBbieScNodeDetail).bbieSc.codeListId)) {
                  // tslint:disable-next-line:max-line-length
                  const message = 'Value Domain is required in ' + (node.detail as BieEditBbieScNodeDetail).bdtSc.propertyTerm + ' ' + (node.detail as BieEditBbieScNodeDetail).bdtSc.representationTerm;
                  this.snackBar.open(message, '', {
                    duration: 3000,
                  });
                  throw new Error(message);
                }
                break;
              case 'Agency':
                if (!((node.detail as BieEditBbieScNodeDetail).bbieSc.agencyIdListId)) {
                  // tslint:disable-next-line:max-line-length
                  const message = 'Value Domain is required in ' + (node.detail as BieEditBbieScNodeDetail).bdtSc.propertyTerm + ' ' + (node.detail as BieEditBbieScNodeDetail).bdtSc.representationTerm;
                  this.snackBar.open(message, '', {
                    duration: 3000,
                  });
                  throw new Error(message);
                }
                break;
            }
            request.bbieScDetails.push((node.detail as BieEditBbieScNodeDetail).bbieSc);
          }
          break;
        default:
          break;
      }
    });
    this.isUpdating = true;

    if (this.rootNode.isChanged) {
      request.topLevelAsbiepDetail = this.rootNode
    };

    this.service.updateDetails(this.topLevelAsbiepId, request).pipe(finalize(() => {
      this.isUpdating = false;
    })).subscribe(resp => {
      this.rootNode.reset();
      this.dataSource.getChanged().forEach(e => e.reset());
      this.service.getUsedBieList(this.topLevelAsbiepId).subscribe(list => this.usedBieList = list);
      if (callbackFn === undefined) {
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      } else {
        return callbackFn && callbackFn();
      }
    });
  }

  resetDetail(node: BieFlatNode) {

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Reset current values to initial values.';
    dialogConfig.data.content = ['Are you sure you want to reset values to initial values?'];
    dialogConfig.data.action = 'Reset';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .pipe(
        finalize(() => {
          this.isUpdating = false;
        })
      )
      .subscribe(result => {
        if (!result) {
          return;
        }

        node.detail.isLoaded = false;

        let path = node.path;

        if (node.bieType === 'ASBIEP') {
          path = (node as AsbiepFlatNode).asbiePath;
        } else if (node.bieType === 'BBIEP') {
          path = (node as BbiepFlatNode).bbiePath;
        } else if (node.bieType === 'BBIE_SC') {
          path = (node as BbieScFlatNode).bbieScPath;
        }

        this.service.resetDetails(this.topLevelAsbiepId, node.bieType, path).subscribe(_ => {
          if (node.bieType === 'ABIE') {
            this.rootNode.status = "";
            this.rootNode.version = "";
          } else if (node.bieType === 'ASBIEP') {
            (node.detail as BieEditAsbiepNodeDetail).asbie.cardinalityMax = undefined;
          } else if (node.bieType === 'BBIEP') {
            (node.detail as BieEditBbiepNodeDetail).bbie.cardinalityMax = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.fixedOrDefault = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.fixedValue = null;
            (node.detail as BieEditBbiepNodeDetail).bbie.defaultValue = null;
            (node.detail as BieEditBbiepNodeDetail).bbie.valueDomainType = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.bdtPriRestriId = null;
            (node.detail as BieEditBbiepNodeDetail).bbie.codeListId = null;
            (node.detail as BieEditBbiepNodeDetail).bbie.agencyIdListId = null;
          } else if (node.bieType === 'BBIE_SC') {
            (node.detail as BieEditBbieScNodeDetail).bbieSc.cardinalityMax = undefined;

            (node.detail as BieEditBbieScNodeDetail).bbieSc.fixedOrDefault = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.fixedValue = null;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.defaultValue = null;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.valueDomainType = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.bdtScPriRestriId = null;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.codeListId = null;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.agencyIdListId = null;
          }
          this.snackBar.open('Reset.', '', {
            duration: 3000,
          });
          this.onClick(node);
        })
      });
  }

  updateState(state: string) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Update state to \'' + state + '\'?';
    dialogConfig.data.content = ['Are you sure you want to update the state to \'' + state + '\'?'];
    dialogConfig.data.action = 'Update';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .pipe(
        finalize(() => {
          this.isUpdating = false;
        })
      )
      .subscribe(result => {
        if (!result) {
          return;
        }
        this.service.setState(this.rootNode.topLevelAsbiepId, state).subscribe(_ => {
          this.service.getRootNode(this.topLevelAsbiepId).subscribe(root => {
            (this.rootNode as BieEditAbieNode).topLevelAsbiepState = root.topLevelAsbiepState;
            (this.rootNode as BieEditAbieNode).access = root.access;
            this.isUpdating = false;
            this.resetCardinalities();

            this.snackBar.open('State updated', '', {
              duration: 3000,
            });
          });
        }, err => {
          this.isUpdating = false;
        });
      });
  }

  isPublished(node: BieFlatNode): boolean {
    const validState = ['Published', 'Production'];
    if (this.isAbieDetail(node)) {
      return validState.indexOf((node as AbieFlatNode).accNode.state) > -1;
    } else if (this.isAsbiepDetail(node)) {
      return validState.indexOf((node as AsbiepFlatNode).asccNode.state) > -1
        && validState.indexOf((node as AsbiepFlatNode).asccpNode.state) > -1
        && validState.indexOf((node as AsbiepFlatNode).accNode.state) > -1;
    } else if (this.isBbiepDetail(node)) {
      return validState.indexOf((node as BbiepFlatNode).bccNode.state) > -1
        && validState.indexOf((node as BbiepFlatNode).bccpNode.state) > -1
        && validState.indexOf((node as BbiepFlatNode).bdtNode.state) > -1;
    } else if (this.isBbieScDetail(node)) {
      return validState.indexOf((node as BbieScFlatNode).bdtScNode.state) > -1
        && validState.indexOf((node as BbieScFlatNode).bccNode.state) > -1;
    }
    return true;
  }

  getNodeTooltip(node: BieFlatNode): string {
    if (node.isCycle) {
      return 'This component is disabled by a circular reference.';
    }
    if (node.required) {
      return 'This component is required.';
    }
    if (!this.isPublished(node)) {
      return 'This component is not in Published or Production state.';
    }
    return '';
  }

  isValidState(state: string): boolean {
    const validState = ['Published', 'Production'];
    return validState.indexOf(state) > -1;
  }

  changeExcludeSCs(): void{
    if (this.isChanged) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Warning';
      dialogConfig.data.content = ['Unsaved changes will be lost.'];
      dialogConfig.data.action = 'Okay';

      this.confirmDialogService.open(dialogConfig).afterClosed()
        .subscribe(result => {
          if (!result) {
            this.excludeSCs = !this.excludeSCs;
            return;
          }
          this.reloadTree(this.selectedNode);
        });
    } else {
      this.reloadTree(this.selectedNode);
    }
  }
}
