import {Component, ElementRef, HostListener, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {faRecycle} from '@fortawesome/free-solid-svg-icons';
import {BieEditService} from '../bie-edit/domain/bie-edit.service';
import {finalize, map, startWith, switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {Location} from '@angular/common';
import {forkJoin, Observable, ReplaySubject} from 'rxjs';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {
  BieDetailUpdateRequest,
  BieDetailUpdateResponse,
  BieEditAbieNode,
  BieEditCreateExtensionResponse,
  BieEditNode,
  ValueDomain,
  ValueDomainType
} from '../bie-edit/domain/bie-edit-node';
import {
  AbieFlatNode,
  AsbiepFlatNode,
  BbiepFlatNode,
  BbieScFlatNode,
  BieEditAbieNodeDetail,
  BieEditAsbiepNodeDetail,
  BieEditBbiepNodeDetail,
  BieEditBbieScNodeDetail,
  BieEditNodeDetail,
  BieFlatNode,
  BieFlatNodeDatabase,
  BieFlatNodeDataSource,
  BieFlatNodeDataSourceSearcher,
  ChangeListener
} from '../domain/bie-flat-tree';
import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AbstractControl, FormControl, FormGroupDirective, NgForm, ValidationErrors, Validators} from '@angular/forms';
import {
  BusinessContext,
  BusinessContextListRequest
} from '../../context-management/business-context/domain/business-context';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {MatSort} from '@angular/material/sort';
import {MatPaginator} from '@angular/material/paginator';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {BusinessTermService} from '../../business-term-management/domain/business-term.service';
import {AuthService} from '../../authentication/auth.service';
import {loadBooleanProperty, saveBooleanProperty, UnboundedPipe} from '../../common/utility';
import {PageRequest} from '../../basis/basis';
import {MatAutocomplete, MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {ReuseBieDialogComponent} from '../bie-edit/reuse-bie-dialog/reuse-bie-dialog.component';
import {Clipboard} from '@angular/cdk/clipboard';
import {RxStompService} from '../../common/score-rx-stomp';
import {MatMenuTrigger} from '@angular/material/menu';
import {ErrorStateMatcher} from '@angular/material/core';
import {MultiActionsSnackBarComponent} from "../../common/multi-actions-snack-bar/multi-actions-snack-bar.component";
import {BieListDialogComponent} from '../bie-list-dialog/bie-list-dialog.component';


@Component({
  selector: 'score-bie-edit',
  templateUrl: './bie-edit.component.html',
  styleUrls: ['./bie-edit.component.css']
})
export class BieEditComponent implements OnInit, ChangeListener<BieFlatNode> {

  faRecycle = faRecycle;
  loading = false;
  paddingPixel = 12;

  topLevelAsbiepId: number;
  queryPath: string;
  rootNode: BieEditAbieNode;

  innerY: number = window.innerHeight;
  dataSource: BieFlatNodeDataSource<BieFlatNode>;
  searcher: BieFlatNodeDataSourceSearcher<BieFlatNode>;

  cursorNode: BieFlatNode;
  selectedNode: BieFlatNode;
  isUpdating = false;
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

  /* string facets management */
  bieMinimumLength: FormControl;
  bieMaximumLength: FormControl;
  biePattern: FormControl;
  biePatternTest: FormControl;
  biePatternTestErrorStateMatcher: ErrorStateMatcher;

  /* valueDomain */
  valueDomainFilterCtrl: FormControl = new FormControl();
  filteredValueDomains: ReplaySubject<ValueDomain[]> = new ReplaySubject<ValueDomain[]>(1);
  valueDomainTypes = [ValueDomainType.Primitive, ValueDomainType.Code, ValueDomainType.Agency];

  /* business term management */
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  @ViewChild('virtualScroll', {static: true}) public virtualScroll: CdkVirtualScrollViewport;
  virtualScrollItemSize = 33;

  get minBufferPx(): number {
    return 10000 * this.virtualScrollItemSize;
  }

  get maxBufferPx(): number {
    return 1000000 * this.virtualScrollItemSize;
  }

  @ViewChildren(MatMenuTrigger) menuTriggerList: QueryList<MatMenuTrigger>;
  contextMenuItem: BieFlatNode;
  @ViewChild('businessContextInput') businessContextInput: ElementRef<HTMLInputElement>;
  @ViewChild('matAutocomplete') matAutocomplete: MatAutocomplete;

  HIDE_CARDINALITY_PROPERTY_KEY = 'BIE-Settings-Hide-Cardinality';
  HIDE_UNUSED_PROPERTY_KEY = 'BIE-Settings-Hide-Unused';

  get hideCardinality(): boolean {
    return this.dataSource.hideCardinality;
  }

  set hideCardinality(hideCardinality: boolean) {
    this.dataSource.hideCardinality = hideCardinality;
    saveBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, hideCardinality);
  }

  get hideUnused(): boolean {
    return this.dataSource.hideUnused;
  }

  set hideUnused(hideUnused: boolean) {
    this.dataSource.hideUnused = hideUnused;
    saveBooleanProperty(this.auth.getUserToken(), this.HIDE_UNUSED_PROPERTY_KEY, hideUnused);
  }

  constructor(private service: BieEditService,
              private bizCtxService: BusinessContextService,
              private snackBar: MatSnackBar,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private businessTermService: BusinessTermService,
              private auth: AuthService,
              private stompService: RxStompService,
              private clipboard: Clipboard) {
  }

  ngOnInit(): void {
    this.loading = true;
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

      if (this.state === 'WIP' && this.access !== 'CanEdit') {
        this.snackBar.open('Only the owner can access BIE in this state.', '', {
          duration: 3000
        });
        this.router.navigateByUrl('/profile_bie');
        return;
      }

      this.businessContextCtrl = new FormControl({
        disabled: !this.canEdit
      });
      this.businessContexts = bizCtxResp.list;
      this.businessContextUpdating = false;
      this.filteredBusinessContexts = this.businessContextCtrl.valueChanges.pipe(
        startWith(null),
        map((value: string | BusinessContext | null) => value ? this._filter(value) : this._filter()));
      this._loadAllBusinessContexts();

      const database = new BieFlatNodeDatabase<BieFlatNode>(ccGraph,
        this.rootNode, this.topLevelAsbiepId, usedBieList, refBieList);
      this.dataSource = new BieFlatNodeDataSource<BieFlatNode>(database, this.service, [this, ]);
      this.searcher = new BieFlatNodeDataSourceSearcher<BieFlatNode>(this.dataSource, database);
      this.dataSource.init();
      this.dataSource.hideCardinality = loadBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, false);
      this.dataSource.hideUnused = loadBooleanProperty(this.auth.getUserToken(), this.HIDE_UNUSED_PROPERTY_KEY, false);

      // Issue #1254
      // Initial expanding by the query path
      const url = this.router.url;
      const topLevelAsbiepId = this.topLevelAsbiepId.toString();
      const queryPath = url.substring(url.indexOf(topLevelAsbiepId) + topLevelAsbiepId.length + 1);

      if (!!queryPath) {
        this.goToPath(queryPath);
      } else {
        this.onClick(this.dataSource.data[0]);
      }

      this.loading = false;
      return;
    }, err => {
      this.snackBar.open('Something\'s wrong.', '', {
        duration: 3000
      });
    });
  }

  goToPath(path: string) {
    let curNode = this.dataSource.data[0];
    let idx = 0;
    path.split('/')
      .map(e => decodeURI(e))
      .map(e => e.replace(new RegExp('\\s', 'g'), '')).forEach(nodeName => {
      for (const node of this.dataSource.data.slice(idx)) {
        if (node.name.replace(new RegExp('\\s', 'g'), '') === nodeName) {
          this.dataSource.expand(node);
          curNode = node;
          break;
        }
        idx++;
      }
    });

    this.onClick(curNode);
    this.scrollToNode(curNode, 500);
  }

  onResize(event) {
    this.innerY = window.innerHeight;
  }

  get innerHeight(): number {
    return this.innerY - 200;
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

  toggle(node: BieFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.dataSource.toggle(node);
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
      this.resetFacets(detailNode);
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

  scrollToNode(node: BieFlatNode, delay?: number) {
    const index = this.searcher.getNodeIndex(node);
    this.scrollTree(index, delay);
    this.cursorNode = node;
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

    // Handle 'Ctrl/Command+S'
    const metaOrCtrlKeyPressed = $event.metaKey || $event.ctrlKey;
    if (metaOrCtrlKeyPressed && charCode === 's') {
      $event.preventDefault();
      $event.stopPropagation();

      this.updateDetails();
    }
  }

  keyNavigation(node: BieFlatNode, $event: KeyboardEvent) {
    if ($event.key === 'ArrowDown') {
      this.cursorNode = this.searcher.next(this.cursorNode);
    } else if ($event.key === 'ArrowUp') {
      this.cursorNode = this.searcher.prev(this.cursorNode);
    } else if ($event.key === 'ArrowLeft' || $event.key === 'ArrowRight') {
      this.dataSource.toggle(this.cursorNode);
    } else if ($event.code === 'Space') {
      this.toggleTreeUsed(this.cursorNode);
    } else if ($event.key === 'o' || $event.key === 'O') {
      this.menuTriggerList.toArray().filter(e => !!e.menuData)
        .filter(e => e.menuData.menuId === 'contextMenu').forEach(trigger => {
        this.contextMenuItem = node;
        trigger.openMenu();
      });
    } else if ($event.key === 'Enter') {
      this.onClick(this.cursorNode);
    } else {
      return;
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

  scrollTree(index: number, delay?: number) {
    if (index < 0) {
      return;
    }

    if (delay) {
      setTimeout(() => {
        this.virtualScroll.scrollToOffset(index * this.virtualScrollItemSize, 'smooth');
      }, delay);
    } else {
      this.virtualScroll.scrollToOffset(index * this.virtualScrollItemSize, 'smooth');
    }
  }

  search(inputKeyword, backward?: boolean, force?: boolean) {
    this.searcher.search(inputKeyword, this.selectedNode, backward, force).subscribe(index => {
      this.scrollTree(index, 500);
    });
  }

  move(val: number) {
    this.searcher.go(val).subscribe(index => {
      this.onClick(this.dataSource.data[index]);
      this.scrollTree(index);
    });
  }

  openNewEditBieTab(node: BieFlatNode) {
    window.open('/profile_bie/' + node.topLevelAsbiepId, '_blank');
  }

  get state(): string {
    return this.rootNode && this.rootNode.topLevelAsbiepState || '';
  }

  get access(): string {
    return this.rootNode && this.rootNode.access || 'Unprepared';
  }

  get canEdit(): boolean {
    return this.state === 'WIP' && this.access === 'CanEdit';
  }

  get isValid(): boolean {
    if (this.selectedNode && this.selectedNode.bieType !== 'ABIE' && this.selectedNode.used) {
      if (!!this.bieCardinalityMin && !this.bieCardinalityMin.disabled && !this.bieCardinalityMin.valid) {
        return false;
      }
      if (!!this.bieCardinalityMax && !this.bieCardinalityMax.disabled && !this.bieCardinalityMax.valid) {
        return false;
      }
      if (!!this.bieMinimumLength && !this.bieMinimumLength.disabled && !this.bieMinimumLength.valid) {
        return false;
      }
      if (!!this.bieMaximumLength && !this.bieMaximumLength.disabled && !this.bieMaximumLength.valid) {
        return false;
      }
      if (!!this.biePattern && !this.biePattern.disabled && !this.biePattern.valid) {
        return false;
      }
    }
    return true;
  }

  isEditable(node: BieFlatNode): boolean {
    if (!node) {
      return false;
    }
    return this.canEdit && node.used === true && !node.locked && !node.isCycle;
  }

  isTreeEditable(node: BieFlatNode): boolean {
    if (!node) {
      return false;
    }
    return this.canEdit && !node.locked && !node.isCycle;
  }

  isUsable(node: BieFlatNode): boolean {
    if (!node) {
      return false;
    }
    if (node.required && this.used(node)) {
      return false;
    }
    return this.canEdit && !node.locked && !node.isCycle;
  }

  used(node: BieFlatNode): boolean {
    const used = (!node) ? undefined : node.used;
    return (used !== undefined) ? used : node.inverseMode;
  }

  /*
  completed(node: BieFlatNode): boolean {
    if (node instanceof BbieScFlatNode) {
      return this.used(node);
    }
    if (node.expandable && !node.children) {
      return false;
    }
    for (const child of node.children) {
      if (!this.completed(child as BieFlatNode)) {
        return false;
      }
    }
    return this.used(node);
  }

  indeterminate(node: BieFlatNode): boolean {
    if (this.completed(node)) {
      return false;
    }
    if (node.children !== undefined) {
      for (const child of node.children) {
        if (this.used(child as BieFlatNode)) {
          return true;
        }
      }
    }
    return this.used(node);
  }
  */

  isUsableChildren(node: BieFlatNode): boolean {
    if (!node) {
      return false;
    }
    return !node.locked && !node.isCycle && !node.derived;
  }

  isCardinalityEditable(node: BieFlatNode): boolean {
    if (!node) {
      return false;
    }
    return !node.locked && !node.isCycle && !node.derived;
  }

  get isChanged(): boolean {
    if (!this.rootNode || !this.dataSource) {
      return false;
    }
    return this.rootNode.isChanged || this.dataSource.getChanged().length > 0;
  }

  get sizeOfChanges(): number {
    return this.dataSource.getChanged().length;
  }

  toggleTreeUsed(node: BieFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    if (!this.isUsable(node)) {
      return;
    }

    node.used = !this.used(node);
    if (node.used) {
      this.assignVersionToVersionIdIfPossible();
    }
  }

  toggleDetailUsed(detailNode?: BieFlatNode, $event?: MouseEvent) {
    if (detailNode !== undefined) {
      this.toggleTreeUsed(this.selectedNode, $event);
    }
  }

  canExtend(node: BieFlatNode): boolean {
    return this.isExtension(node) && !this.isDeveloper;
  }

  isExtension(node: BieFlatNode) {
    return !!node && (node.bieType.toUpperCase() === 'ASBIEP' && node.name === 'Extension');
  }

  get isDeveloper() {
    const userToken = this.auth.getUserToken();
    return userToken.roles.includes('developer');
  }

  get isTenantEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.tenant.enabled;
  }

  get isBusinessTermEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.businessTerm.enabled;
  }

  get isBIEInverseModeEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.bie.inverseMode;
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

  copyLink(node: BieFlatNode, $event?) {
    if ($event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    if (!node) {
      return;
    }

    const url = window.location.href;
    const topLevelAsbiepId = this.topLevelAsbiepId.toString();
    const idIdx = url.indexOf(topLevelAsbiepId);
    const queryPath = url.substring(0, idIdx + topLevelAsbiepId.length) + '/' + node.queryPath;

    this.clipboard.copy(queryPath);
    this.snackBar.open('Link copied', '', {
      duration: 3000
    });
  }

  reloadTree(node: BieFlatNode) {
    let selectedNodeHashPath;
    if (this.selectedNode) {
      selectedNodeHashPath = this.selectedNode.hashPath;
      this.selectedNode = undefined;
    }
    const expandedNodes = this.dataSource.data.filter(e => e.expanded);
    this.loading = true;
    forkJoin([
      this.service.getGraphNode(this.topLevelAsbiepId),
      this.service.getUsedBieList(this.topLevelAsbiepId),
      this.service.getRefBieList(this.topLevelAsbiepId),
    ]).subscribe(([ccGraph, usedBieList, refBieList]) => {
      const database = new BieFlatNodeDatabase<BieFlatNode>(ccGraph,
        this.rootNode, this.topLevelAsbiepId, usedBieList, refBieList);
      this.dataSource = new BieFlatNodeDataSource<BieFlatNode>(database, this.service, [this, ]);
      this.searcher = new BieFlatNodeDataSourceSearcher<BieFlatNode>(this.dataSource, database);
      this.dataSource.init();

      // recover the tree expansion status
      for (const expandedNode of expandedNodes) {
        for (const datum of this.dataSource.data) {
          if (expandedNode.hashPath === datum.hashPath && !datum.expanded) {
            this.dataSource.expand(datum);
            break;
          }
        }
      }
      // recover the selected node.
      if (!!selectedNodeHashPath) {
        for (const datum of this.dataSource.data) {
          if (datum.hashPath === selectedNodeHashPath) {
            this.onClick(datum);
            break;
          }
        }
      }

      this.loading = false;
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

      if (!asbiepNode.used) {
        this.toggleTreeUsed(asbiepNode);
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

  findReuses(node: BieFlatNode) {
    if (!this.canRemoveReusedBIE(node)) {
      return;
    }

    const reusedNode = node as AsbiepFlatNode;

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {
      topLevelAsbiepId: reusedNode.topLevelAsbiepId,
      releaseNum: this.rootNode.releaseNum,
      den: reusedNode.asccpNode.propertyTerm + '. ' + reusedNode.accNode.objectClassTerm
    };
    const dialogRef = this.dialog.open(BieListDialogComponent, dialogConfig);
  }

  removeReusedBIE(node: BieFlatNode) {
    if (!this.canRemoveReusedBIE(node)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove reused BIE?';
    dialogConfig.data.content = ['Are you sure you want to remove the reused BIE?'];
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

  retainReusedBIE(node: BieFlatNode) {
    if (!this.canRemoveReusedBIE(node)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Retain reused BIE?';
    dialogConfig.data.content = ['Are you sure you want to retain the reused BIE?'];
    dialogConfig.data.action = 'Retain';

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
      this.service.retainReusedBIE(this.topLevelAsbiepId, asbiepNode.asbieHashPath)
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
          this.isUpdating = true;
          this.updateDetails(node.parents, () => {
            this.service.makeReusableBIE(asbiepNode.asbieHashPath, asbiepNode.topLevelAsbiepId, asbiepNode.asccpNode.manifestId)
              .pipe(finalize(() => {
                this.isUpdating = false;
              })).subscribe(_ => {
              this.snackBar.openFromComponent(MultiActionsSnackBarComponent, {
                data: {
                  titleIcon: 'info',
                  title: 'Info',
                  message: 'The request for making the BIE is processing. The processed BIE will appear on the BIE list.',
                  action: 'Go to \'View/Edit BIE\' page',
                  onAction: (data, snackBarRef) => {
                    this.router.navigateByUrl('/profile_bie');
                    snackBarRef.dismissWithAction();
                  }
                }
              });
            });
          });
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

    this.isUpdating = true;
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

    this.isUpdating = true;
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
    this.dataSource.expand(node);
    node.children.map(e => e as BieFlatNode).forEach(e => {
      if ((e as BieFlatNode).isGroup) {
        this.enableChildren(e);
      } else {
        if (e.used) {
          return;
        }
        this.toggleTreeUsed(e);
      }
    });
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
    });
  }

  checkNillableOnChildBCCs(node: BieFlatNode) {
    node.children.forEach(e => {
      const child = e as BieFlatNode;
      if (child.isGroup) {
        this.checkNillableOnChildBCCs(child);
      } else {
        if (child.bieType !== 'BBIEP') {
          return;
        }
        if (!child.used) {
          return;
        }

        this.dataSource.loadDetail(child, (detailNode: BieFlatNode) => {
          this.asBbiepDetail(detailNode).bbie.nillable = true;
        });
      }
    });
  }

  uncheckNillableOnChildBCCs(node: BieFlatNode) {
    node.children.forEach(e => {
      const child = e as BieFlatNode;
      if (child.isGroup) {
        this.uncheckNillableOnChildBCCs(child);
      } else {
        if (child.bieType !== 'BBIEP') {
          return;
        }
        if (!child.used) {
          return;
        }

        this.dataSource.loadDetail(child, (detailNode: BieFlatNode) => {
          this.asBbiepDetail(detailNode).bbie.nillable = false;
        });
      }
    });
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

  isValidState(state: string): boolean {
    const validState = ['Published', 'Production'];
    return validState.indexOf(state) > -1;
  }

  isStringTypePrimitive(cdtPrimitives: string[]): boolean {
    for (const typeName of ['String', 'NormalizedString', 'Token', 'Binary']) {
      if (cdtPrimitives.includes(typeName)) {
        return true;
      }
    }
    return false;
  }

  goToBusinessTermsForBie(detailNode: BieEditNodeDetail, bieType: string) {
    let bieId: number;
    if (bieType === 'ASBIE') {
      bieId = (detailNode as BieEditAsbiepNodeDetail).asbie.asbieId;
      const link = this.router.serializeUrl(
        this.router.createUrlTree(['/business_term_management/assign_business_term/'],
          {queryParams: {bieId, bieType}}));
      window.open(link, '_blank');
    } else if (bieType === 'BBIE') {
      bieId = (detailNode as BieEditBbiepNodeDetail).bbie.bbieId;
      const link = this.router.serializeUrl(
        this.router.createUrlTree(['/business_term_management/assign_business_term/'],
          {queryParams: {bieId, bieType}}));
      window.open(link, '_blank');
    } else {
      this.snackBar.open('Error occurred. Unrecognized BIE type: ' + bieType);
    }
  }

  goToAssignBusinessTermsForBie(detailNode: BieEditNodeDetail, bieType: string) {
    let bieId: number;
    if (bieType === 'ASBIE') {
      bieId = (detailNode as BieEditAsbiepNodeDetail).asbie.asbieId;
    } else if (bieType === 'BBIE') {
      bieId = (detailNode as BieEditBbiepNodeDetail).bbie.bbieId;
    } else {
      this.snackBar.open('Error occurred. Unrecognized BIE type: ' + bieType);
    }

    if (bieId) {
      const link = this.router.serializeUrl(
        this.router.createUrlTree(['/business_term_management/assign_business_term/create/bt'],
          {queryParams: {bieIds: [bieId], bieTypes: [bieType]}}));
      window.open(link, '_blank');
    } else {
      this.snackBar.open('Please save the current state to assign the business terms to this BIE.', '', {
        duration: 3000,
      });
    }
  }

  onChange(entity: BieFlatNode, propertyName: string, val: any) {
    if (this.selectedNode === entity && propertyName === 'used') {
      this.resetCardinalities(entity);
      this.resetFacets(entity);
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
    for (const detailValue of this.dataSource.data[0].children.map(e => e as BieFlatNode)) {
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

  resetFacets(node?: BieFlatNode) {
    if (!node) {
      node = this.selectedNode;
    }

    this._setMinLengthFormControl(node);
    this._setMaxLengthFormControl(node);
    this._setPatternFormControl(node);
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

  nonWhitespaceValidator(control: AbstractControl): ValidationErrors | null {
    if (!control || !control.value) {
      return null;
    }
    const isWhitespace = control.value.toString().trim().length === 0;
    const isValid = !isWhitespace;
    return isValid ? null : {whitespace: true};
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
        this.nonWhitespaceValidator,
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
        this.nonWhitespaceValidator,
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

  _setMinLengthFormControl(detailNode?: BieFlatNode) {
    if (!detailNode) {
      detailNode = this.selectedNode;
    } else if (detailNode !== this.selectedNode) {
      return;
    }

    const disabled = !this.isEditable(detailNode) ||
      !detailNode.used || !!detailNode.locked;

    let bieMinLength;
    let bieMaxLength;
    if (this.isBbiepDetail(detailNode)) {
      bieMinLength = this.asBbiepDetail(detailNode).bbie.minLength;
      bieMaxLength = this.asBbiepDetail(detailNode).bbie.maxLength;
    } else if (this.isBbieScDetail(detailNode)) {
      bieMinLength = this.asBbieScDetail(detailNode).bbieSc.minLength;
      bieMaxLength = this.asBbieScDetail(detailNode).bbieSc.maxLength;
    } else {
      this.bieMinimumLength = undefined;
      return;
    }

    this.bieMinimumLength = new FormControl({
        value: bieMinLength,
        disabled
      }, [
        Validators.pattern('\\s*|\\d+'),
        // validatorFn for minimum value
        (control: AbstractControl): ValidationErrors | null => {
          const value = (!!control.value) ? control.value.toString().trim() : undefined;
          if (!value) {
            return null;
          }
          const num = Number(value);
          if (Number.isNaN(num)) {
            return null;
          }
          if (num < 0) {
            return {min: 'Minimum Length must be greater than or equals to 0'};
          }
          if (!!bieMaxLength && num > bieMaxLength) {
            return {max: 'Minimum Length ' + num + ' must be less than or equals to ' + bieMaxLength};
          }
          return null;
        }
      ]
    );
    this.bieMinimumLength.valueChanges.subscribe(value => {
      if (this.bieMinimumLength.valid) {
        value = typeof value === 'number' ? value : Number.parseInt(value, 10);
        if (this.isBbiepDetail(detailNode)) {
          this.asBbiepDetail(detailNode).bbie.minLength = Number.isNaN(value) ? undefined : value;
        } else if (this.isBbieScDetail(detailNode)) {
          this.asBbieScDetail(detailNode).bbieSc.minLength = Number.isNaN(value) ? undefined : value;
        } else {
          return;
        }
        this._setMaxLengthFormControl(detailNode);
      }
    });
  }

  _setMaxLengthFormControl(detailNode?: BieFlatNode) {
    if (!detailNode) {
      detailNode = this.selectedNode;
    } else if (detailNode !== this.selectedNode) {
      return;
    }

    const disabled = !this.isEditable(detailNode) ||
      !detailNode.used || !!detailNode.locked;

    let bieMinLength;
    let bieMaxLength;
    if (this.isBbiepDetail(detailNode)) {
      bieMinLength = this.asBbiepDetail(detailNode).bbie.minLength;
      bieMaxLength = this.asBbiepDetail(detailNode).bbie.maxLength;
    } else if (this.isBbieScDetail(detailNode)) {
      bieMinLength = this.asBbieScDetail(detailNode).bbieSc.minLength;
      bieMaxLength = this.asBbieScDetail(detailNode).bbieSc.maxLength;
    } else {
      this.bieMaximumLength = undefined;
      return;
    }

    this.bieMaximumLength = new FormControl({
        value: new UnboundedPipe().transform(bieMaxLength),
        disabled
      }, [
        Validators.pattern('\\s*|\\d+'),
        // validatorFn for minimum value
        (control: AbstractControl): ValidationErrors | null => {
          const value = (!!control.value) ? control.value.toString().trim() : undefined;
          if (!value) {
            return null;
          }
          const num = Number(value);
          if (Number.isNaN(num)) {
            return null;
          }
          if (num < 0) {
            return {min: 'Maximum Length must be greater than or equals to 0'};
          }
          if (!!bieMinLength && num < bieMinLength) {
            return {min: 'Maximum Length ' + num + ' must be greater than ' + bieMinLength};
          }
          return null;
        }
      ]
    );
    this.bieMaximumLength.valueChanges.subscribe(value => {
      if (this.bieMaximumLength.valid) {
        value = typeof value === 'number' ? value : Number.parseInt(value, 10);
        if (this.isBbiepDetail(detailNode)) {
          this.asBbiepDetail(detailNode).bbie.maxLength = Number.isNaN(value) ? undefined : value;
        } else if (this.isBbieScDetail(detailNode)) {
          this.asBbieScDetail(detailNode).bbieSc.maxLength = Number.isNaN(value) ? undefined : value;
        } else {
          return;
        }
        this._setMinLengthFormControl(detailNode);
      }
    });
  }

  _setPatternFormControl(detailNode?: BieFlatNode) {
    if (!detailNode) {
      detailNode = this.selectedNode;
    } else if (detailNode !== this.selectedNode) {
      return;
    }

    const disabled = !this.isEditable(detailNode) ||
      !detailNode.used || !!detailNode.locked;

    let biePattern;
    if (this.isBbiepDetail(detailNode)) {
      biePattern = this.asBbiepDetail(detailNode).bbie.pattern;
    } else if (this.isBbieScDetail(detailNode)) {
      biePattern = this.asBbieScDetail(detailNode).bbieSc.pattern;
    } else {
      this.biePattern = undefined;
      return;
    }

    this.biePattern = new FormControl({
      value: biePattern,
      disabled
    }, [
      (control: AbstractControl): ValidationErrors | null => {
        const value = (!!control.value) ? control.value.toString().trim() : undefined;
        if (!value) {
          return null;
        }

        try {
          const regexp = new RegExp(value);
        } catch (e) {
          return {pattern: 'The pattern \'' + value + '\' is invalid.'};
        }

        return null;
      }
    ]);

    this.biePattern.valueChanges.subscribe(value => {
      if (this.biePattern.valid) {
        if (this.isBbiepDetail(detailNode)) {
          this.asBbiepDetail(detailNode).bbie.pattern = value;
        } else if (this.isBbieScDetail(detailNode)) {
          this.asBbieScDetail(detailNode).bbieSc.pattern = value;
        } else {
          return;
        }
      }

      this._setPatternTestFormControl();
    });

    this._setPatternTestFormControl();
  }

  _setPatternTestFormControl() {
    this.biePatternTest = new FormControl({
      value: (!!this.biePatternTest) ? this.biePatternTest.value : '',
      disabled: !this.biePattern || !this.biePattern.value || !this.biePattern.valid
    }, [
      (this.biePattern.valid) ? Validators.pattern(this.biePattern.value) : Validators.nullValidator
    ]);
    this.biePatternTestErrorStateMatcher = new BiePatternTestShowOnDirtyErrorStateMatcher(this.biePattern);
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

  isReflectValue(detail?: BieFlatNode): boolean {
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
          (node.detail as BieEditBbiepNodeDetail).bbie.codeListManifestId = undefined;
          (node.detail as BieEditBbiepNodeDetail).bbie.agencyIdListManifestId = undefined;
        } else if (this.asBbiepDetail(node).bbie.valueDomainType === ValueDomainType.Code.toString()) {
          (node.detail as BieEditBbiepNodeDetail).bbie.bdtPriRestriId = undefined;
          (node.detail as BieEditBbiepNodeDetail).bbie.agencyIdListManifestId = undefined;
        } else {
          (node.detail as BieEditBbiepNodeDetail).bbie.bdtPriRestriId = undefined;
          (node.detail as BieEditBbiepNodeDetail).bbie.codeListManifestId = undefined;
        }
        break;
      case 'BBIE_SC':
        if (this.asBbieScDetail(node).bbieSc.valueDomainType === ValueDomainType.Primitive.toString()) {
          (node.detail as BieEditBbieScNodeDetail).bbieSc.codeListManifestId = undefined;
          (node.detail as BieEditBbieScNodeDetail).bbieSc.agencyIdListManifestId = undefined;
        } else if (this.asBbieScDetail(node).bbieSc.valueDomainType === ValueDomainType.Code.toString()) {
          (node.detail as BieEditBbieScNodeDetail).bbieSc.bdtScPriRestriId = undefined;
          (node.detail as BieEditBbieScNodeDetail).bbieSc.agencyIdListManifestId = undefined;
        } else {
          (node.detail as BieEditBbieScNodeDetail).bbieSc.bdtScPriRestriId = undefined;
          (node.detail as BieEditBbieScNodeDetail).bbieSc.codeListManifestId = undefined;
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
          valueDomains = list.map(e => new ValueDomain(e.codeListManifestId, e.codeListName, e.state, e.versionId, e.deprecated));
          this._setFilteredValueDomains(valueDomains);
        });
      } else if (this.asBbiepDetail(detail).bbie.valueDomainType === 'Agency') {
        this.service.getBbiepAgencyIdList(this.topLevelAsbiepId, bccpManifestId).subscribe(list => {
          valueDomains = list.map(e => new ValueDomain(e.agencyIdListManifestId, e.agencyIdListName, e.state, e.versionId, e.deprecated));
          this._setFilteredValueDomains(valueDomains);
        });
      } else { // valueDomainType === 'Primitive'
        this.service.getBbiepBdtPriRestriList(this.topLevelAsbiepId, bccpManifestId).subscribe(list => {
          if (bbiepNodeDetail.bbie.bdtPriRestriId === null) {
            bbiepNodeDetail.bbie.bdtPriRestriId = list.find(e => e.default).bdtPriRestriId;
          }
          valueDomains = list.filter(e => !!e.xbtName).map(e => new ValueDomain(e.bdtPriRestriId, e.xbtName));
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
          valueDomains = list.map(e => new ValueDomain(e.codeListManifestId, e.codeListName, e.state, e.versionId, e.deprecated));
          this._setFilteredValueDomains(valueDomains);
        });
      } else if (this.asBbieScDetail(detail).bbieSc.valueDomainType === 'Agency') {
        this.service.getBbieScAgencyIdList(this.topLevelAsbiepId, bdtScManifestId).subscribe(list => {
          valueDomains = list.map(e => new ValueDomain(e.agencyIdListManifestId, e.agencyIdListName, e.state, e.versionId, e.deprecated));
          this._setFilteredValueDomains(valueDomains);
        });
      } else {
        this.service.getBbieScBdtScPriRestriList(this.topLevelAsbiepId, bdtScManifestId).subscribe(list => {
          if (this.asBbieScDetail(detail).bbieSc.bdtScPriRestriId === null) {
            this.asBbieScDetail(detail).bbieSc.bdtScPriRestriId = list.find(e => e.default).bdtScPriRestriId;
          }
          valueDomains = list.filter(e => !!e.xbtName).map(e => new ValueDomain(e.bdtScPriRestriId, e.xbtName));
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

  updateInverseMode() {
    const request = new BieDetailUpdateRequest();
    this.isUpdating = true;
    request.topLevelAsbiepDetail = this.rootNode;

    this.service.updateDetails(this.topLevelAsbiepId, request).pipe(finalize(() => {
      this.isUpdating = false;
      this.loading = false;
    })).subscribe((resp: BieDetailUpdateResponse) => {
      this.rootNode.reset();
      this.snackBar.open(((this.rootNode.inverseMode) ? 'Inverse mode is turned on' : 'Inverse mode is turned off'), '', {
        duration: 3000,
      });
    });
  }

  get updateDisabled(): boolean {
    return this.isUpdating || !this.isChanged || !this.isValid;
  }

  updateDetails(include?: BieFlatNode[], callbackFn?) {
    if (this.updateDisabled) {
      if (callbackFn === undefined) {
        return;
      } else {
        return callbackFn && callbackFn();
      }
    }

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
                if (!((node.detail as BieEditBbiepNodeDetail).bbie.codeListManifestId)) {
                  const message = 'Value Domain is required in ' + (node.detail as BieEditBbiepNodeDetail).bccp.propertyTerm;
                  this.snackBar.open(message, '', {
                    duration: 3000,
                  });
                  throw new Error(message);
                }
                break;
              case 'Agency':
                if (!((node.detail as BieEditBbiepNodeDetail).bbie.agencyIdListManifestId)) {
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
                if (!((node.detail as BieEditBbieScNodeDetail).bbieSc.codeListManifestId)) {
                  // tslint:disable-next-line:max-line-length
                  const message = 'Value Domain is required in ' + (node.detail as BieEditBbieScNodeDetail).bdtSc.propertyTerm + ' ' + (node.detail as BieEditBbieScNodeDetail).bdtSc.representationTerm;
                  this.snackBar.open(message, '', {
                    duration: 3000,
                  });
                  throw new Error(message);
                }
                break;
              case 'Agency':
                if (!((node.detail as BieEditBbieScNodeDetail).bbieSc.agencyIdListManifestId)) {
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

    this.loading = true;
    this.isUpdating = true;

    if (this.rootNode.isChanged) {
      request.topLevelAsbiepDetail = this.rootNode;
    }

    this.service.updateDetails(this.topLevelAsbiepId, request).pipe(finalize(() => {
      this.isUpdating = false;
      this.loading = false;
    })).subscribe((resp: BieDetailUpdateResponse) => {
      this.rootNode.reset();
      this.dataSource.update(resp);
      nodes.forEach(e => e.reset());
      this.service.getUsedBieList(this.topLevelAsbiepId).subscribe(usedBieList => {
        this.dataSource.database.setUsedBieList(usedBieList);
      });
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
            this.rootNode.status = '';
            this.rootNode.version = '';
          } else if (node.bieType === 'ASBIEP') {
            (node.detail as BieEditAsbiepNodeDetail).asbie.cardinalityMax = undefined;
          } else if (node.bieType === 'BBIEP') {
            (node.detail as BieEditBbiepNodeDetail).bbie.cardinalityMax = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.minLength = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.maxLength = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.pattern = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.fixedOrDefault = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.fixedValue = null;
            (node.detail as BieEditBbiepNodeDetail).bbie.defaultValue = null;
            (node.detail as BieEditBbiepNodeDetail).bbie.valueDomainType = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.bdtPriRestriId = null;
            (node.detail as BieEditBbiepNodeDetail).bbie.codeListManifestId = null;
            (node.detail as BieEditBbiepNodeDetail).bbie.agencyIdListManifestId = null;
          } else if (node.bieType === 'BBIE_SC') {
            (node.detail as BieEditBbieScNodeDetail).bbieSc.cardinalityMax = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.minLength = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.maxLength = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.pattern = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.fixedOrDefault = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.fixedValue = null;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.defaultValue = null;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.valueDomainType = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.bdtScPriRestriId = null;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.codeListManifestId = null;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.agencyIdListManifestId = null;
          }
          this.snackBar.open('Reset', '', {
            duration: 3000,
          });
          this.onClick(node);
        });
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
            this.resetFacets();

            this.snackBar.open('State updated', '', {
              duration: 3000,
            });
          });
        }, err => {
          this.isUpdating = false;
        });
      });
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
    if (this.isTenantEnabled) {
      request.filters.isBieEditing = true;
    }
    request.page = new PageRequest('name', 'asc', -1, -1);
    this.bizCtxService.getBusinessContextList(request)
      .subscribe(resp => {
        this.allBusinessContexts = resp.list;
      });
  }

  get isBusinessContextRemovable(): boolean {
    return (!this.businessContextUpdating && this.businessContexts.length > 1);
  }

  _filter(value?: string | BusinessContext) {
    const prevBizCtxNames = this.businessContexts.map(e => e.name);
    let l = this.allBusinessContexts.filter(e => !prevBizCtxNames.includes(e.name));
    if (!!value) {
      let name;
      if (typeof value === 'object') {
        name = (value as BusinessContext).name;
      } else {
        name = value;
      }
      l = l.filter(e => e.name.toLowerCase().indexOf(name.toLowerCase()) === 0);
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
}

class BiePatternTestShowOnDirtyErrorStateMatcher implements ErrorStateMatcher {
  constructor(private biePattern: FormControl) {
  }
  isErrorState(control: AbstractControl | null, form: FormGroupDirective | NgForm | null): boolean {
    return (this.biePattern.dirty || control.dirty) && control.invalid;
  }
}
