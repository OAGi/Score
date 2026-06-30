import { ChangeDetectorRef, Component, ElementRef, HostListener, OnInit, QueryList, Renderer2, ViewChild, ViewChildren, inject } from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {faRecycle, faSitemap} from '@fortawesome/free-solid-svg-icons';
import {BieEditService} from '../bie-edit/domain/bie-edit.service';
import {catchError, finalize, map, startWith, switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {Location} from '@angular/common';
import {forkJoin, Observable, of, ReplaySubject} from 'rxjs';
import {BieViewOrderService} from '../../cc-management/model-browser/domain/bie-view-order.service';
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
  AbieFlatNode, AsbieDetail, AsbiepDetail, AsbiepDetails,
  AsbiepFlatNode, AsbiepSupportDoc,
  BbieDetail,
  BbiepFlatNode,
  BbieScDetail,
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
  BusinessContextListRequest,
  BusinessContextSummary
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
import {MultiActionsSnackBarComponent} from '../../common/multi-actions-snack-bar/multi-actions-snack-bar.component';
import {BieListDialogComponent} from '../bie-list-dialog/bie-list-dialog.component';
import {WebPageInfoService} from '../../basis/basis.service';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {PreferencesInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {CcNodeService} from '../../cc-management/domain/core-component-node.service';
import {BieStateTransitionFlowService} from '../domain/bie-state-transition-flow.service';
import {StateDependencySelection} from '../domain/state-dependency-target';
import {Title} from '@angular/platform-browser';
import {setAppTitleIfPresent} from '../../common/app-title.strategy';
import {
  AssignBieForOasDoc,
  BieForOasDoc,
  BieForOasDocDeleteRequest,
  BieForOasDocUpdateRequest,
  OasDocListRequest,
  OasSecurityRequirement,
  OasSecurityScheme,
  recomputeOperationId
} from '../openapi-doc/domain/openapi-doc';
import {OpenAPIService} from '../openapi-doc/domain/openapi.service';
import {BieOasDocAddDialogComponent} from './bie-oas-doc-add-dialog/bie-oas-doc-add-dialog.component';
import {OasDocSecurityRequirementDialogComponent} from '../openapi-doc/oas-doc-security-requirement-dialog/oas-doc-security-requirement-dialog.component';
import {
  OasDocConfirmMessageDialogComponent,
  OasDocConfirmMessageDialogResult
} from '../openapi-doc/oas-doc-confirm-message-dialog/oas-doc-confirm-message-dialog.component';


@Component({
  standalone: false,
  selector: 'score-bie-edit',
  templateUrl: './bie-edit.component.html',
  styleUrls: ['./bie-edit.component.css']
})
export class BieEditComponent implements OnInit, ChangeListener<BieFlatNode> {
  private service = inject(BieEditService);
  private ccNodeService = inject(CcNodeService);
  private bieViewOrderService = inject(BieViewOrderService);
  private bizCtxService = inject(BusinessContextService);
  private snackBar = inject(MatSnackBar);
  private location = inject(Location);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);
  private businessTermService = inject(BusinessTermService);
  private preferencesService = inject(SettingsPreferencesService);
  private auth = inject(AuthService);
  private stompService = inject(RxStompService);
  private clipboard = inject(Clipboard);
  private renderer = inject(Renderer2);
  private el = inject(ElementRef);
  private cdr = inject(ChangeDetectorRef);
  private titleService = inject(Title);
  webPageInfo = inject(WebPageInfoService);
  private stateTransitionFlowService = inject(BieStateTransitionFlowService);
  private openAPIService = inject(OpenAPIService);


  BROWSER_SCHEMES = ['http:', 'https:', 'ftp:', 'file:', 'data:'];
  EXTERNAL_SCHEMES = ['mailto:', 'tel:', 'sms:', 'geo:', 'magnet:'];

  faRecycle = faRecycle;
  faSitemap = faSitemap;
  loading = false;
  paddingPixel = 12;

  topLevelAsbiepId: number;
  rootNode: BieEditAbieNode;

  innerY: number = window.innerHeight;
  dataSource: BieFlatNodeDataSource<BieFlatNode>;
  searcher: BieFlatNodeDataSourceSearcher<BieFlatNode>;
  // Issue #1638: view-parent ACC manifest ids whose sibling order has already been fetched, so each
  // parent is requested at most once per tree build.
  private _viewOrderLoaded = new Set<number>();
  cursorNode: BieFlatNode;
  selectedNode: BieFlatNode;

  // For renaming 'displayName' of a node.
  renamingNode: BieFlatNode;
  originalDisplayName: string;

  startChangingDisplayName(node: BieFlatNode, $event?: MouseEvent): void {
    if (!this.canChangeDisplayName(node)) {
      return;
    }

    if ($event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.renamingNode = node;
    this.cdr.detectChanges();

    if (node.displayName) {
      this.originalDisplayName = node.displayName;
    } else {
      node.displayName = node.name;
      this.originalDisplayName = undefined;
    }

    setTimeout(() => {
      const editInput = this.el.nativeElement.querySelector('.edit-input');
      if (editInput) {
        this.renderer.selectRootElement(editInput).focus();
        this.renderer.selectRootElement(editInput).select();
      }
    });
  }

  stopChangingDisplayName(revert: boolean = false): void {
    if (revert) {
      this.renamingNode.displayName = this.originalDisplayName;  // Revert the value
    }
    this.renamingNode = undefined;
    this.cdr.detectChanges();
  }

  @HostListener('document:click', ['$event.target'])
  onClickOutside(targetElement: HTMLElement): void {
    const clickedInside = targetElement.closest('.mat-tree-node');
    if (!clickedInside || !this.renamingNode) {
      this.stopChangingDisplayName();
    }
  }

  // Listen for 'esc' key press within the input
  @HostListener('document:keydown.escape', ['$event'])
  cancelEditing($event: KeyboardEvent): void {
    if (this.renamingNode) {
      this.stopChangingDisplayName(true);
      $event.preventDefault();
    }
  }
  // End of 'displayName'

  isUpdating = false;
  _versionChanged: boolean;
  _changedVersionValue: string;
  /* Begin business context management */
  businessContextCtrl: FormControl;
  businessContexts: BusinessContextSummary[] = [];
  allBusinessContexts: BusinessContextSummary[] = [];
  filteredBusinessContexts: Observable<BusinessContextSummary[]>;
  businessContextUpdating = true;
  addOnBlur = true;
  separatorKeysCodes: number[] = [ENTER, COMMA];

  /* reused BIE */
  reusedBusinessContexts: BusinessContextSummary[] = [];
  reusedNode: BieEditNode;
  reusedNodeDetail: AsbiepDetails;

  /* business contexts in base BIE */
  basedBusinessContexts?: BusinessContextSummary[] = [];
  baseNode?: BieEditAbieNode; // If 'rootNode' has basedTopLevelAsbiepId, this must not be null.

  /* reused base BIE */
  reusedBaseBusinessContexts: BusinessContextSummary[] = [];
  reusedBaseNode: BieEditNode;
  reusedBaseNodeDetail: AsbiepDetails;

  /* cardinality management */
  bieCardinalityMin: FormControl;
  bieCardinalityMax: FormControl;

  /* string facets management */
  facetMinimumLength: FormControl;
  facetMaximumLength: FormControl;
  facetPattern: FormControl;
  facetPatternTest: FormControl;
  facetPatternTestErrorStateMatcher: ErrorStateMatcher;

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

  preferencesInfo: PreferencesInfo;
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

  ngOnInit(): void {
    this.loading = true;
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        this.topLevelAsbiepId = parseInt(params.get('id'), 10);

        const businessContextRequest = new BusinessContextListRequest();
        if (this.isTenantEnabled) {
          businessContextRequest.filters.isBieEditing = true;
        }
        businessContextRequest.page = new PageRequest('name', 'asc', -1, -1);

        return forkJoin([
          this.service.getGraphNode(this.topLevelAsbiepId),
          this.service.getUsedBieList(this.topLevelAsbiepId),
          this.service.getRefBieList(this.topLevelAsbiepId),
          this.service.getRootNode(this.topLevelAsbiepId),
          this.bizCtxService.getBusinessContextsByTopLevelAsbiepId(this.topLevelAsbiepId),
          this.bizCtxService.getBusinessContextSummaries(),
          this.preferencesService.load(this.auth.getUserToken())
        ]);
      })).subscribe(([ccGraph, usedBieList, refBieList, rootNode,
                       bizCtxResp, allBizCtxResp, preferencesInfo]) => {
      if (!rootNode) {
        this.redirectToBieList();
        return;
      }

      this.initRootNode(rootNode);

      if (this.state === 'WIP' && (this.access !== 'CanEdit' && !this.auth.isAdmin())) {
        this.snackBar.open('Only the owner can access BIE in this state.', '', {
          duration: 3000
        });
        this.router.navigateByUrl('/profile_bie');
        return;
      }

      this.allBusinessContexts = allBizCtxResp;
      this.businessContextCtrl = new FormControl({
        disabled: false
      });
      this.businessContexts = bizCtxResp;
      this.businessContextUpdating = false;
      this.filteredBusinessContexts = this.businessContextCtrl.valueChanges.pipe(
        startWith(null),
        map((value: string | BusinessContext | null) => value ? this._filter(value) : this._filter()));
      this.preferencesInfo = preferencesInfo;

      // Issue #1638: the sibling view order is fetched LAZILY, one view-parent ACC at a time, as ABIE
      // nodes are expanded (see onNodeExpanded) — never upfront — and the view parent is resolved on
      // the client where group/choice flattening happens.
      this._viewOrderLoaded.clear();
      const database = new BieFlatNodeDatabase<BieFlatNode>(ccGraph,
        this.rootNode, this.topLevelAsbiepId, usedBieList, refBieList);

      const doAfter = () => {
        this.dataSource = new BieFlatNodeDataSource<BieFlatNode>(database, this.service, this.ccNodeService, [this,]);
        this.dataSource.nodeExpanded$.subscribe(node => this.onNodeExpanded(node));
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
      };

      if (!!rootNode.basedTopLevelAsbiepId) {
        forkJoin([
          this.service.getRootNode(rootNode.basedTopLevelAsbiepId),
          this.bizCtxService.getBusinessContextsByTopLevelAsbiepId(rootNode.basedTopLevelAsbiepId),
          this.service.getUsedBieList(rootNode.basedTopLevelAsbiepId)
        ]).subscribe(([baseNode, basedBizCtxResp, baseUsedBieList]) => {
          this.baseNode = new BieEditAbieNode(baseNode);
          this.basedBusinessContexts = basedBizCtxResp;
          database.setBaseUsedBieList(baseUsedBieList);
          doAfter();
        });
      } else {
        doAfter();
      }
    }, err => {
      if (err.status === 404) {
        this.redirectToBieList();
        return;
      }

      const errorMessage = (err.status === 403) ?
        'You do not have access permission.' : 'Something\'s wrong.';
      this.snackBar.open(errorMessage, '', {
        duration: 3000
      });
      this.router.navigateByUrl('/profile_bie');
    });
  }

  private redirectToBieList() {
    this.loading = false;
    this.snackBar.open('The requested BIE is unavailable.', '', {
      duration: 3000
    });
    this.router.navigateByUrl('/profile_bie');
  }

  extractDelimiter(path: string): string {
    // Regular expression to find non-alphanumeric characters as delimiter
    const delimiterMatch = path.match(/[^a-zA-Z0-9]/);
    return delimiterMatch ? delimiterMatch[0] : '/';
  }

  goToPath(path: string) {
    let curNode = this.dataSource.data[0];
    let idx = 0;
    let delimiter = this.extractDelimiter(path);
    path.split(delimiter)
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
    setAppTitleIfPresent(this.titleService, this.rootNode.name, 'BIE');
    this.rootNode.reset();
    // Issue #1519: seed the per-document uniqueness sets + the committed Error Response types, and pre-fetch
    // each bound document's security schemes for the Security cell.
    this.recomputeOasValidation();
    this.seedOasCommittedErrorTypes();
    this.loadOasDocSecuritySchemes();
    this.loadOasDocExistence();
    const that = this;

    const onVersionChange = (version: string) => {
      that._versionChanged = true;
      that._changedVersionValue = version;

      that.assignVersionToVersionIdIfPossible();
    };
    this.rootNode.listeners.push(new class implements ChangeListener<BieEditNode> {
      onChange(entity: BieEditNode, propertyName: string, val: any) {
        if (propertyName === 'version') {
          onVersionChange(val);
        }
      }
    });
    if (!!this.rootNode.version) {
      onVersionChange(this.rootNode.version);
    }
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

    this.dataSource.loadDetails(node, (detailNode: BieFlatNode) => {
      this.selectedNode = node;
      this.cursorNode = node;

      this.resetCardinalities(detailNode);
      this.resetFacets(detailNode);
      this.initFixedOrDefault(detailNode);
      this.valueDomainFilterValues(detailNode);
      this.assignVersionToVersionIdIfPossible(node);

      if (node.bieType.toUpperCase() === 'ASBIEP' && node.reused) {
        forkJoin([
          this.service.getRootNode(node.topLevelAsbiepId),
          this.service.getAsbiepDetailsByPath(node.topLevelAsbiepId,
              (node as AsbiepFlatNode).asccpNode.manifestId, (node as AsbiepFlatNode).asbiepPath),
          this.bizCtxService.getBusinessContextsByTopLevelAsbiepId(node.topLevelAsbiepId)
        ]).subscribe(([resp, detail, bizCtxResp]) => {
          this.reusedNode = resp as BieEditAbieNode;
          this.reusedNodeDetail = detail;
          this.reusedBusinessContexts = bizCtxResp;

          if (node.inherited) {
            if (this.asAsbiepDetail(node).asbie.toAsbiepId !== this.asAsbiepDetail(node).base.asbie.toAsbiepId) {
              const inheritedReuseTopLevelAsbiepId = this.asAsbiepDetail(node).base.asbiep.ownerTopLevelAsbiepId;
              forkJoin([
                this.service.getRootNode(inheritedReuseTopLevelAsbiepId),
                this.service.getAsbiepDetailsByPath(inheritedReuseTopLevelAsbiepId,
                    (node as AsbiepFlatNode).asccpNode.manifestId, (node as AsbiepFlatNode).asbiepPath),
                this.bizCtxService.getBusinessContextsByTopLevelAsbiepId(inheritedReuseTopLevelAsbiepId)
              ]).subscribe(([baseResp, baseDetail, baseBizCtxResp]) => {
                this.reusedBaseNode = baseResp as BieEditAbieNode;
                this.reusedBaseNodeDetail = baseDetail;
                this.reusedBaseBusinessContexts = baseBizCtxResp;
              });
            } else {
              this.reusedBaseNode = this.reusedNode;
              this.reusedBaseNodeDetail = this.reusedNodeDetail;
              this.reusedBaseBusinessContexts = this.reusedBusinessContexts;
            }
          }
        });
      } else {
        this.reusedNode = undefined;
        this.reusedNodeDetail = undefined;
        this.reusedBusinessContexts = [];

        this.reusedBaseNode = undefined;
        this.reusedBaseNodeDetail = undefined;
        this.reusedBaseBusinessContexts = [];
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
    const target = $event.target as HTMLElement;
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
    // Skip if the target is an input, textarea, or contenteditable element
    const target = $event.target as HTMLElement;
    const isInputField = target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable;
    if (isInputField) {
      return;
    }

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
          .filter(e => e.menuData.menuId === 'contextMenu' && e.menuData.hashPath === node.hashPath)
          .forEach(trigger => {
            this.contextMenuItem = node;
            if (!trigger.menuOpen) {
              trigger.openMenu();
            }
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

  openNewEditBieTab(node: BieFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    window.open('/profile_bie/' + node.topLevelAsbiepId, '_blank');
  }

  copyToDefinition(sourceStr: string, targetObj: object) {
    targetObj['definition'] = sourceStr;
    this.snackBar.open('Copied to definition', '', {
      duration: 3000
    });
  }

  get state(): string {
    return this.rootNode && this.rootNode.topLevelAsbiepState || '';
  }

  get access(): string {
    return this.rootNode && this.rootNode.access || 'Unprepared';
  }

  isClickableUrl(url: string): boolean {
    if (!url) {
      return false;
    }
    try {
      const scheme = new URL(url).protocol;
      return [...this.BROWSER_SCHEMES, ...this.EXTERNAL_SCHEMES].includes(scheme);
    } catch {
      return false;
    }
  }

  openLink(url: string) {
    if (!this.isClickableUrl(url)) {
      return;
    }

    const scheme = new URL(url).protocol;
    if (this.BROWSER_SCHEMES.includes(scheme)) {
      window.open(url, '_blank', 'noopener,noreferrer');
    } else if (this.EXTERNAL_SCHEMES.includes(scheme)) {
      window.location.href = url;
    }
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
      if (!!this.facetMinimumLength && !this.facetMinimumLength.disabled && !this.facetMinimumLength.valid) {
        return false;
      }
      if (!!this.facetMaximumLength && !this.facetMaximumLength.disabled && !this.facetMaximumLength.valid) {
        return false;
      }
      if (!!this.facetPattern && !this.facetPattern.disabled && !this.facetPattern.valid) {
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
    return this.canEdit && !node.inherited && !node.locked && !node.isCycle;
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
    return !node.locked && !node.isCycle && !node.reused;
  }

  isCardinalityEditable(node: BieFlatNode): boolean {
    if (!node) {
      return false;
    }
    return !node.locked && !node.isCycle && !node.reused;
  }

  get isChanged(): boolean {
    if (!this.rootNode || !this.dataSource) {
      return false;
    }
    return this.rootNode.isChanged || this.dataSource.getChanged().length > 0;
  }

  get sizeOfChanges(): number {
    // Issue #1519: include the OpenAPI Document Information edits so the badge reflects everything the main
    // Update will commit.
    return this.dataSource.getChanged().length + this.oasBindings.filter(b => b.isChanged).length;
  }

  toggleTreeUsed(node: BieFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      // Using $event.preventDefault() prevents the [checked] behavior from functioning correctly.
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

  get isAdmin(): boolean {
    return this.auth.isAdmin();
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
    return !!node && node.bieType.toUpperCase() === 'ASBIEP' && !node.locked && !node.reused;
  }

  canReuseBIE(node: BieFlatNode): boolean {
    return !!node && node.bieType.toUpperCase() === 'ASBIEP' && !node.locked && !node.reused;
  }

  canRemoveReusedBIE(node: BieFlatNode): boolean {
    return !!node && node.bieType.toUpperCase() === 'ASBIEP' && !node.locked && node.reused;
  }

  canOverrideBaseReusedBIE(node: BieFlatNode): boolean {
    return this.canRemoveReusedBIE(node) && node.inherited;
  }

  canChangeDisplayName(node: BieFlatNode): boolean {
    return !!node && this.canEdit && !node.reused && !node.locked && node.level === 0;
  }

  canUseBaseBIE(node: BieFlatNode): boolean {
    return !!node && node.level === 0 && !node.inherited;
  }

  canRemoveBaseBIE(node: BieFlatNode): boolean {
    return !!node && node.level === 0 && node.inherited;
  }

  copyPath(node: BieFlatNode) {
    if (!node) {
      return;
    }

    const delimiter = this.preferencesInfo.viewSettingsInfo.treeSettings.delimiter;
    let queryPath = node.queryPath;
    queryPath = queryPath.replaceAll('/', delimiter);

    this.clipboard.copy(queryPath);
    this.snackBar.open('Copied to clipboard', '', {
      duration: 3000
    });
  }

  copyLink(node: BieFlatNode, $event?: MouseEvent) {
    if (!!$event) {
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
    this.snackBar.open('Copied to clipboard', '', {
      duration: 3000
    });
  }

  addAsbiepSupportDoc(asbiepDetail: AsbiepDetail) {
    asbiepDetail.supportDocList.push(new AsbiepSupportDoc());
  }

  removeAsbiepSupportDoc(asbiepDetail: AsbiepDetail, supportDoc: AsbiepSupportDoc) {
    const index = asbiepDetail.supportDocList.indexOf(supportDoc);
    if (index !== -1) {
      asbiepDetail.supportDocList.splice(index, 1);
    }
    if (asbiepDetail.supportDocList?.length === 0) {
      this.addAsbiepSupportDoc(asbiepDetail);
    }
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
      this.service.getRootNode(this.topLevelAsbiepId)
    ]).subscribe((
      [ccGraph, usedBieList, refBieList, rootNode]) => {
      this.initRootNode(rootNode);

      // Issue #1638: the sibling view order is fetched lazily per view parent as nodes re-expand.
      this._viewOrderLoaded.clear();

      const doAfter = () => {
        this.dataSource = new BieFlatNodeDataSource<BieFlatNode>(database, this.service, this.ccNodeService, [this,]);
        this.dataSource.nodeExpanded$.subscribe(node => this.onNodeExpanded(node));
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

        // Recover the state of 'Hide Cardinality' and 'Hide Unused' properties.
        this.dataSource.hideCardinality = loadBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, false);
        this.dataSource.hideUnused = loadBooleanProperty(this.auth.getUserToken(), this.HIDE_UNUSED_PROPERTY_KEY, false);

        this.loading = false;
      };

      const database = new BieFlatNodeDatabase<BieFlatNode>(ccGraph,
        this.rootNode, this.topLevelAsbiepId, usedBieList, refBieList);
      if (!!this.rootNode.basedTopLevelAsbiepId) {
        forkJoin([
          this.service.getRootNode(rootNode.basedTopLevelAsbiepId),
          this.bizCtxService.getBusinessContextsByTopLevelAsbiepId(rootNode.basedTopLevelAsbiepId),
          this.service.getUsedBieList(rootNode.basedTopLevelAsbiepId)
        ]).subscribe(([baseNode, basedBizCtxResp, baseUsedBieList]) => {
          this.baseNode = new BieEditAbieNode(baseNode);
          this.basedBusinessContexts = basedBizCtxResp;
          database.setBaseUsedBieList(baseUsedBieList);
          doAfter();
        });
      } else {
        doAfter();
      }
    });
  }

  /**
   * Issue #1638 (view-only): the first time an ABIE node is expanded, lazily fetch its based ACC's
   * sibling order, merge it, and re-sort that node's children. Most parents have no rows (empty list
   * => no visible change); only an explicitly reordered parent re-sorts.
   */
  private onNodeExpanded(node: BieFlatNode) {
    const viewParentAccManifestId = this.dataSource.database.viewParentAccManifestId(node);
    if (viewParentAccManifestId === undefined || this._viewOrderLoaded.has(viewParentAccManifestId)) {
      return; // already fetched -> children() already sorts this position against the cached map
    }
    this._viewOrderLoaded.add(viewParentAccManifestId);
    this.bieViewOrderService.getViewOrder(viewParentAccManifestId)
      .pipe(catchError(() => of(null)))
      .subscribe((entries) => {
        if (entries === null) {
          // Transient failure: un-mark so the next expand retries (don't poison to seq_key order).
          this._viewOrderLoaded.delete(viewParentAccManifestId);
          return;
        }
        this.dataSource.database.setViewOrderForParent(viewParentAccManifestId, entries);
        if (entries.length > 0) {
          this.resortAllExpandedFor(viewParentAccManifestId);
        }
      });
  }

  /**
   * Re-render EVERY currently-expanded position of a view parent. The same based-ACC can sit at
   * several tree positions (reused ABIEs); any expanded while this GET was in flight rendered in
   * seq_key order, so they all need re-sorting once the weights arrive.
   */
  private resortAllExpandedFor(viewParentAccManifestId: number) {
    const targets = this.dataSource.data.filter(n =>
      n.expanded && this.dataSource.database.viewParentAccManifestId(n) === viewParentAccManifestId);
    targets.forEach(n => this.resortUnder(n));
  }

  /**
   * Re-render a parent's children in the (updated) view order, preserving the user's deeper expansion.
   * Snapshot/restore by queryPath (not hashPath): hashPath collides for descendants of two ASBIEPs
   * that reuse the same TopLevelAsbiep, exactly as toggleNode documents.
   */
  private resortUnder(displayParent: BieFlatNode) {
    if (!displayParent || !this.dataSource.isExpanded(displayParent)) {
      return;
    }
    const expandedQueryPaths = this.dataSource.data.filter(e => e.expanded).map(e => e.queryPath);
    this.dataSource.collapse(displayParent);
    this.dataSource.expand(displayParent);
    for (const queryPath of expandedQueryPaths) {
      const datum = this.dataSource.data.find(d => d.queryPath === queryPath && !d.expanded);
      if (datum) {
        this.dataSource.expand(datum);
      }
    }
  }

  reuseBIE(node: BieFlatNode) {
    if (!this.canReuseBIE(node)) {
      return;
    }

    const asbiepNode = (node as AsbiepFlatNode);
    const dialogRef = this.dialog.open(ReuseBieDialogComponent, {
      data: {
        title: 'Select Profile BIE to reuse',
        asccpManifestId: asbiepNode.asccpNode.manifestId,
        den: asbiepNode.asccpNode.den,
        libraryId: this.rootNode.libraryId,
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

  removeOverrideReusedBIE(node: BieFlatNode) {
    const asbiepNode = (node as AsbiepFlatNode);
    // Base.ASBIE -> ASBIEP.OwnerTopLevelAsbiepId = Base Reuse BIE
    this.service.getAsbieDetailsByPath((node.parent as AsbiepFlatNode).basedTopLevelAsbiepId,
        (node as AsbiepFlatNode).asccNode.manifestId, (node as AsbiepFlatNode).asbiePath).subscribe(baseAsbie => {
      this.service.getAsbiepDetails(baseAsbie.toAsbiepId).subscribe(baseAsbiep => {
        const baseReusedTopLevelAsbiepId = baseAsbiep.ownerTopLevelAsbiep.topLevelAsbiepId;

        if (!asbiepNode.used) {
          this.toggleTreeUsed(asbiepNode);
        }
        this.updateDetails(asbiepNode.parents, () => {
          this.isUpdating = true;
          /*
           * The reuse ASBIEP is referencing the topLevelAsbiepId of the Reuse BIE,
           * so this will be overwritten with the current topLevelAsbiepId.
           */
          asbiepNode.topLevelAsbiepId = this.rootNode.topLevelAsbiepId;
          this.service.reuseBIE(asbiepNode, baseReusedTopLevelAsbiepId)
            .pipe(finalize(() => this.isUpdating = false)).subscribe(__ => {
            this.reloadTree(node);
          });
        });
      });
    });
  }

  overrideBaseReusedBIE(node: BieFlatNode) {
    if (!this.canRemoveReusedBIE(node)) {
      return;
    }

    const asbiepNode = (node as AsbiepFlatNode);
    // Base.ASBIE -> ASBIEP.OwnerTopLevelAsbiepId = Base Reuse BIE
    this.service.getAsbieDetailsByPath(asbiepNode.parent.basedTopLevelAsbiepId,
        (node as AsbiepFlatNode).asccNode.manifestId, (node as AsbiepFlatNode).asbiePath).subscribe(asbie => {
      this.service.getAsbiepDetails(asbie.toAsbiepId).subscribe(asbiep => {
        const baseReusedTopLevelAsbiepId = asbiep.ownerTopLevelAsbiep.topLevelAsbiepId;

        const dialogRef = this.dialog.open(ReuseBieDialogComponent, {
          data: {
            title: 'Select Profile BIE to reuse',
            asccpManifestId: asbiepNode.asccpNode.manifestId,
            den: asbiepNode.asccpNode.den,
            libraryId: this.rootNode.libraryId,
            releaseId: this.rootNode.releaseId,
            topLevelAsbiepId: this.topLevelAsbiepId,
            basedTopLevelAsbiepId: baseReusedTopLevelAsbiepId
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
            /*
             * The reuse ASBIEP is referencing the topLevelAsbiepId of the Reuse BIE,
             * so this will be overwritten with the current topLevelAsbiepId.
             */
            asbiepNode.topLevelAsbiepId = this.rootNode.topLevelAsbiepId;
            this.service.reuseBIE(asbiepNode, selectedTopLevelAsbiepId)
              .pipe(finalize(() => this.isUpdating = false)).subscribe(__ => {
              this.reloadTree(node);
            });
          });
        });
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
      this.openStateUpdateErrorDialog(err);
    });
  }

  private openStateUpdateErrorDialog(error: HttpErrorResponse): void {
    const errorMessageId = error?.headers?.get('x-error-message-id');
    const errorMessage = error?.headers?.get('x-error-message') || error?.message || 'Failed to update BIE state';

    this.snackBar.openFromComponent(MultiActionsSnackBarComponent, {
      data: {
        titleIcon: 'error',
        title: 'Error',
        message: errorMessage,
        action: errorMessageId ? 'View detail in Notifications' : 'Copy to clipboard',
        onAction: (data, snackBarRef) => {
          if (errorMessageId) {
            this.router.navigate(['/message/' + errorMessageId]);
            snackBarRef.dismissWithAction();
            return;
          }

          this.clipboard.copy(data.message);
        }
      }
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

        this.dataSource.loadDetails(child, (detailNode: BieFlatNode) => {
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

        this.dataSource.loadDetails(child, (detailNode: BieFlatNode) => {
          this.asBbiepDetail(detailNode).bbie.nillable = false;
        });
      }
    });
  }

  useBaseBIE(node: BieFlatNode) {
    if (!this.canUseBaseBIE(node)) {
      return;
    }

    const abieNode = (node as AbieFlatNode);
    const dialogRef = this.dialog.open(ReuseBieDialogComponent, {
      data: {
        title: 'Select Base Profile BIE',
        asccpManifestId: abieNode.asccpNode.manifestId,
        den: abieNode.asccpNode.den,
        libraryId: this.rootNode.libraryId,
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

      this.isUpdating = true;

      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Use Base BIE';
      dialogConfig.data.content = [
        'Proceeding may lead to data loss as the current data could be overwritten by the selected Base BIE.',
        'Are you sure you want to proceed?'
      ];
      dialogConfig.data.action = 'Proceed';

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

          this.isUpdating = true;

          if (!abieNode.used) {
            this.toggleTreeUsed(abieNode);
          }
          this.updateDetails(abieNode.parents, () => {
            this.isUpdating = true;
            this.service.useBaseBIE(abieNode, selectedTopLevelAsbiepId)
              .pipe(finalize(() => this.isUpdating = false)).subscribe(__ => {
              this.reloadTree(node);
            });
          });
        });
    });
  }

  removeBaseBIE(node: BieFlatNode) {
    if (!this.canRemoveBaseBIE(node)) {
      return;
    }

    this.isUpdating = true;

    const abieNode = (node as AbieFlatNode);
    if (!abieNode.used) {
      this.toggleTreeUsed(abieNode);
    }
    this.updateDetails(abieNode.parents, () => {
      this.isUpdating = true;
      this.service.removeBaseBIE(abieNode)
        .pipe(finalize(() => this.isUpdating = false)).subscribe(__ => {
        this.reloadTree(node);
      });
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

  // Issue #1723: a developer may see end-user (Production) code lists in the Value Domain list
  // so an already-assigned one is visible, but must not (re)assign them. Such options are shown
  // read-only. End-users are unaffected (they assign their own Production code lists).
  isValueDomainAssignable(state: string): boolean {
    return !(this.isDeveloper && state === 'Production');
  }

  goToBusinessTermsForBie(bieId: number, bieType: string) {
    if (bieType === 'ASBIE') {
      const link = this.router.serializeUrl(
        this.router.createUrlTree(['/business_term_management/assign_business_term/'],
          {queryParams: {bieId, bieType}}));
      window.open(link, '_blank');
    } else if (bieType === 'BBIE') {
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
    if (!this.dataSource || !this.dataSource.data) {
      return undefined;
    }
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
      const setFixedOrDefaultValueForBbie = (bbie: BbieDetail) => {
        if (bbie.defaultValue) {
          bbie.fixedOrDefault = 'default';
        } else if (bbie.fixedValue) {
          bbie.fixedOrDefault = 'fixed';
        } else {
          bbie.fixedOrDefault = 'none';
        }
      };

      setFixedOrDefaultValueForBbie(this.asBbiepDetail(detail).bbie);
      if (this.asBbiepDetail(detail).base) {
        setFixedOrDefaultValueForBbie(this.asBbiepDetail(detail).base.bbie);
      }
    } else if (this.isBbieScDetail(detail)) {
      const setFixedOrDefaultValueForBbieSc = (bbieSc: BbieScDetail) => {
        if (bbieSc.defaultValue) {
          bbieSc.fixedOrDefault = 'default';
        } else if (bbieSc.fixedValue) {
          bbieSc.fixedOrDefault = 'fixed';
        } else {
          bbieSc.fixedOrDefault = 'none';
        }
      };

      setFixedOrDefaultValueForBbieSc(this.asBbieScDetail(detail).bbieSc);
      if (this.asBbieScDetail(detail).base) {
        setFixedOrDefaultValueForBbieSc(this.asBbieScDetail(detail).base.bbieSc);
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
      if (detailNode.inherited) {
        ccCardinalityMin = this.asAsbiepDetail(detailNode).base.asbie.cardinalityMin;
      } else {
        ccCardinalityMin = this.asAsbiepDetail(detailNode).ascc.cardinality.min;
      }
    } else if (this.isBbiepDetail(detailNode)) {
      bieCardinalityMin = this.asBbiepDetail(detailNode).bbie.cardinalityMin;
      bieCardinalityMax = this.asBbiepDetail(detailNode).bbie.cardinalityMax;
      if (detailNode.inherited) {
        ccCardinalityMin = this.asBbiepDetail(detailNode).base.bbie.cardinalityMin;
      } else {
        ccCardinalityMin = this.asBbiepDetail(detailNode).bcc.cardinality.min;
      }
    } else if (this.isBbieScDetail(detailNode)) {
      bieCardinalityMin = this.asBbieScDetail(detailNode).bbieSc.cardinalityMin;
      bieCardinalityMax = this.asBbieScDetail(detailNode).bbieSc.cardinalityMax;
      if (detailNode.inherited) {
        ccCardinalityMin = this.asBbieScDetail(detailNode).base.bbieSc.cardinalityMin;
      } else {
        ccCardinalityMin = this.asBbieScDetail(detailNode).bdtSc.cardinality.min;
      }
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
      if (detailNode.inherited) {
        ccCardinalityMax = this.asAsbiepDetail(detailNode).base.asbie.cardinalityMax;
      } else {
        ccCardinalityMax = this.asAsbiepDetail(detailNode).ascc.cardinality.max;
      }
    } else if (this.isBbiepDetail(detailNode)) {
      bieCardinalityMin = this.asBbiepDetail(detailNode).bbie.cardinalityMin;
      bieCardinalityMax = this.asBbiepDetail(detailNode).bbie.cardinalityMax;
      if (detailNode.inherited) {
        ccCardinalityMax = this.asBbiepDetail(detailNode).base.bbie.cardinalityMax;
      } else {
        ccCardinalityMax = this.asBbiepDetail(detailNode).bcc.cardinality.max;
      }
    } else if (this.isBbieScDetail(detailNode)) {
      bieCardinalityMin = this.asBbieScDetail(detailNode).bbieSc.cardinalityMin;
      bieCardinalityMax = this.asBbieScDetail(detailNode).bbieSc.cardinalityMax;
      if (detailNode.inherited) {
        ccCardinalityMax = this.asBbieScDetail(detailNode).base.bbieSc.cardinalityMax;
      } else {
        ccCardinalityMax = this.asBbieScDetail(detailNode).bdtSc.cardinality.max;
      }
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

    let disabled = !this.isEditable(detailNode) ||
      !detailNode.used || !!detailNode.locked;

    let bieMinLength;
    let bieMaxLength;
    if (this.isBbiepDetail(detailNode)) {
      const bbiepDetail = this.asBbiepDetail(detailNode);
      bieMinLength = bbiepDetail.bbie.facetMinLength;
      bieMaxLength = bbiepDetail.bbie.facetMaxLength;
      if (bbiepDetail.base && (bbiepDetail.base.bbie.facetMinLength || bbiepDetail.base.bbie.facetMaxLength || bbiepDetail.base.bbie.facetPattern)) {
        disabled = true;
      }
    } else if (this.isBbieScDetail(detailNode)) {
      const bbieScDetail = this.asBbieScDetail(detailNode);
      bieMinLength = bbieScDetail.bbieSc.facetMinLength;
      bieMaxLength = bbieScDetail.bbieSc.facetMaxLength;
      if (bbieScDetail.base && (bbieScDetail.base.bbieSc.facetMinLength || bbieScDetail.base.bbieSc.facetMaxLength || bbieScDetail.base.bbieSc.facetPattern)) {
        disabled = true;
      }
    } else {
      this.facetMinimumLength = undefined;
      return;
    }

    this.facetMinimumLength = new FormControl({
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
    this.facetMinimumLength.valueChanges.subscribe(value => {
      if (this.facetMinimumLength.valid) {
        value = typeof value === 'number' ? value : Number.parseInt(value, 10);
        if (this.isBbiepDetail(detailNode)) {
          this.asBbiepDetail(detailNode).bbie.facetMinLength = Number.isNaN(value) ? undefined : value;
        } else if (this.isBbieScDetail(detailNode)) {
          this.asBbieScDetail(detailNode).bbieSc.facetMinLength = Number.isNaN(value) ? undefined : value;
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

    let disabled = !this.isEditable(detailNode) ||
      !detailNode.used || !!detailNode.locked;

    let bieMinLength;
    let bieMaxLength;
    if (this.isBbiepDetail(detailNode)) {
      const bbiepDetail = this.asBbiepDetail(detailNode);
      bieMinLength = bbiepDetail.bbie.facetMinLength;
      bieMaxLength = bbiepDetail.bbie.facetMaxLength;
      if (bbiepDetail.base && (bbiepDetail.base.bbie.facetMinLength || bbiepDetail.base.bbie.facetMaxLength || bbiepDetail.base.bbie.facetPattern)) {
        disabled = true;
      }
    } else if (this.isBbieScDetail(detailNode)) {
      const bbieScDetail = this.asBbieScDetail(detailNode);
      bieMinLength = bbieScDetail.bbieSc.facetMinLength;
      bieMaxLength = bbieScDetail.bbieSc.facetMaxLength;
      if (bbieScDetail.base && (bbieScDetail.base.bbieSc.facetMinLength || bbieScDetail.base.bbieSc.facetMaxLength || bbieScDetail.base.bbieSc.facetPattern)) {
        disabled = true;
      }
    } else {
      this.facetMaximumLength = undefined;
      return;
    }

    this.facetMaximumLength = new FormControl({
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
    this.facetMaximumLength.valueChanges.subscribe(value => {
      if (this.facetMaximumLength.valid) {
        value = typeof value === 'number' ? value : Number.parseInt(value, 10);
        if (this.isBbiepDetail(detailNode)) {
          this.asBbiepDetail(detailNode).bbie.facetMaxLength = Number.isNaN(value) ? undefined : value;
        } else if (this.isBbieScDetail(detailNode)) {
          this.asBbieScDetail(detailNode).bbieSc.facetMaxLength = Number.isNaN(value) ? undefined : value;
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

    let disabled = !this.isEditable(detailNode) ||
      !detailNode.used || !!detailNode.locked;

    let biePattern;
    if (this.isBbiepDetail(detailNode)) {
      const bbiepDetail = this.asBbiepDetail(detailNode);
      biePattern = bbiepDetail.bbie.facetPattern;
      if (bbiepDetail.base && (bbiepDetail.base.bbie.facetMinLength || bbiepDetail.base.bbie.facetMaxLength || bbiepDetail.base.bbie.facetPattern)) {
        disabled = true;
      }
    } else if (this.isBbieScDetail(detailNode)) {
      const bbieScDetail = this.asBbieScDetail(detailNode);
      biePattern = bbieScDetail.bbieSc.facetPattern;
      if (bbieScDetail.base && (bbieScDetail.base.bbieSc.facetMinLength || bbieScDetail.base.bbieSc.facetMaxLength || bbieScDetail.base.bbieSc.facetPattern)) {
        disabled = true;
      }
    } else {
      this.facetPattern = undefined;
      return;
    }

    this.facetPattern = new FormControl({
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

    this.facetPattern.valueChanges.subscribe(value => {
      if (this.facetPattern.valid) {
        if (this.isBbiepDetail(detailNode)) {
          this.asBbiepDetail(detailNode).bbie.facetPattern = value;
        } else if (this.isBbieScDetail(detailNode)) {
          this.asBbieScDetail(detailNode).bbieSc.facetPattern = value;
        } else {
          return;
        }
      }

      this._setPatternTestFormControl();
    });

    this._setPatternTestFormControl();
  }

  _setPatternTestFormControl() {
    this.facetPatternTest = new FormControl({
      value: (!!this.facetPatternTest) ? this.facetPatternTest.value : '',
      disabled: !this.facetPattern || !this.facetPattern.value || !this.facetPattern.valid
    }, [
      (this.facetPattern.valid) ? Validators.pattern(this.facetPattern.value) : Validators.nullValidator
    ]);
    this.facetPatternTestErrorStateMatcher = new BiePatternTestShowOnDirtyErrorStateMatcher(this.facetPattern);
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
      return !!(this.asBbiepDetail(detail).bccp.valueConstraint?.defaultValue || this.asBbiepDetail(detail).bccp.valueConstraint?.fixedValue);
    } else {
      return !!(this.asBbieScDetail(detail).bdtSc.valueConstraint?.defaultValue || this.asBbieScDetail(detail).bdtSc.valueConstraint?.fixedValue);
    }
  }

  changeValueDomainType(node: BieFlatNode) {
    switch (this.selectedNode.bieType) {
      case 'BBIEP':
        if (this.asBbiepDetail(node).bbie.valueDomainType === ValueDomainType.Primitive.toString()) {
          (node.detail as BieEditBbiepNodeDetail).bbie.codeListManifestId = undefined;
          (node.detail as BieEditBbiepNodeDetail).bbie.agencyIdListManifestId = undefined;
        } else if (this.asBbiepDetail(node).bbie.valueDomainType === ValueDomainType.Code.toString()) {
          (node.detail as BieEditBbiepNodeDetail).bbie.xbtManifestId = undefined;
          (node.detail as BieEditBbiepNodeDetail).bbie.agencyIdListManifestId = undefined;
        } else {
          (node.detail as BieEditBbiepNodeDetail).bbie.xbtManifestId = undefined;
          (node.detail as BieEditBbiepNodeDetail).bbie.codeListManifestId = undefined;
        }
        break;
      case 'BBIE_SC':
        if (this.asBbieScDetail(node).bbieSc.valueDomainType === ValueDomainType.Primitive.toString()) {
          (node.detail as BieEditBbieScNodeDetail).bbieSc.codeListManifestId = undefined;
          (node.detail as BieEditBbieScNodeDetail).bbieSc.agencyIdListManifestId = undefined;
        } else if (this.asBbieScDetail(node).bbieSc.valueDomainType === ValueDomainType.Code.toString()) {
          (node.detail as BieEditBbieScNodeDetail).bbieSc.xbtManifestId = undefined;
          (node.detail as BieEditBbieScNodeDetail).bbieSc.agencyIdListManifestId = undefined;
        } else {
          (node.detail as BieEditBbieScNodeDetail).bbieSc.xbtManifestId = undefined;
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
      const dtManifestId = bbiepNodeDetail.bdt.dtManifestId;
      if (bbiepNodeDetail.bbie.valueDomainType === undefined) {
        bbiepNodeDetail.bbie.valueDomainType = 'Primitive';
      }
      if (this.asBbiepDetail(detail).bbie.valueDomainType === 'Code') {
        this.service.getDtCodeList(dtManifestId).subscribe(list => {
          valueDomains = list.map(e => new ValueDomain(e.codeListManifestId, e.name, e.state, e.versionId, e.deprecated));
          this._setFilteredValueDomains(valueDomains);
        });
      } else if (this.asBbiepDetail(detail).bbie.valueDomainType === 'Agency') {
        this.service.getDtAgencyIdList(dtManifestId).subscribe(list => {
          valueDomains = list.map(e => new ValueDomain(e.agencyIdListManifestId, e.name, e.state, e.versionId, e.deprecated));
          this._setFilteredValueDomains(valueDomains);
        });
      } else { // valueDomainType === 'Primitive'
        this.service.getDtAwdPriList(dtManifestId).subscribe(list => {
          if (bbiepNodeDetail.bbie.xbtManifestId === null) {
            bbiepNodeDetail.bbie.xbtManifestId = list.find(e => e.isDefault).xbtManifestId;
          }
          valueDomains = list.filter(e => !!e.xbtName).map(e => new ValueDomain(e.xbtManifestId, e.xbtName));
          this._setFilteredValueDomains(valueDomains);
        });
      }
    } else if (this.isBbieScDetail(detail)) {
      const dtScManifestId = this.asBbieScDetail(detail).bdtSc.dtScManifestId;
      if (this.asBbieScDetail(detail).bbieSc.valueDomainType === undefined) {
        this.asBbieScDetail(detail).bbieSc.valueDomainType = 'Primitive';
      }
      if (this.asBbieScDetail(detail).bbieSc.valueDomainType === 'Code') {
        this.service.getDtScCodeList(dtScManifestId).subscribe(list => {
          valueDomains = list.map(e => new ValueDomain(e.codeListManifestId, e.name, e.state, e.versionId, e.deprecated));
          this._setFilteredValueDomains(valueDomains);
        });
      } else if (this.asBbieScDetail(detail).bbieSc.valueDomainType === 'Agency') {
        this.service.getDtScAgencyIdList(dtScManifestId).subscribe(list => {
          valueDomains = list.map(e => new ValueDomain(e.agencyIdListManifestId, e.name, e.state, e.versionId, e.deprecated));
          this._setFilteredValueDomains(valueDomains);
        });
      } else {
        this.service.getDtScAwdPriList(dtScManifestId).subscribe(list => {
          if (this.asBbieScDetail(detail).bbieSc.xbtManifestId === null) {
            this.asBbieScDetail(detail).bbieSc.xbtManifestId = list.find(e => e.isDefault).xbtManifestId;
          }
          valueDomains = list.filter(e => !!e.xbtName).map(e => new ValueDomain(e.xbtManifestId, e.xbtName));
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
    if (this.isUpdating) {
      return true;
    }
    // Issue #1519: the main Update is enabled when the BIE tree has valid changes OR the OpenAPI Document
    // Information panel has savable changes, so pressing Update also commits the OAS edits.
    const bieSavable = this.isChanged && this.isValid;
    const oasSavable = this.hasOasChanges() && !this.hasOasErrors();
    return !(bieSavable || oasSavable);
  }

  updateDetails(include?: BieFlatNode[], callbackFn?) {
    // Issue #1519: the main Update button also commits OpenAPI Document Information edits (the standalone
    // "Update OpenAPI Information" button, by contrast, never touches the BIE tree). Only the user-initiated
    // Update (no `include`) triggers the OAS save.
    if (!include && this.hasOasChanges() && !this.hasOasErrors()) {
      this.updateOasDocInformation();
    }
    // The BIE tree itself is saved only when it has valid changes of its own.
    if (this.isUpdating || !this.isChanged || !this.isValid) {
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
            if (!node.reused) {
              request.abieDetails.push((node.detail as BieEditAsbiepNodeDetail).abie);
              request.asbiepDetails.push((node.detail as BieEditAsbiepNodeDetail).asbiep);
            }
          }
          break;
        case 'BBIEP':
          if (!node.locked) {
            switch ((node.detail as BieEditBbiepNodeDetail).bbie.valueDomainType) {
              case 'Primitive':
                if (!((node.detail as BieEditBbiepNodeDetail).bbie.xbtManifestId)) {
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
                if (!((node.detail as BieEditBbieScNodeDetail).bbieSc.xbtManifestId)) {
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
            (node.detail as BieEditBbiepNodeDetail).bbie.facetMinLength = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.facetMaxLength = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.facetPattern = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.fixedOrDefault = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.fixedValue = null;
            (node.detail as BieEditBbiepNodeDetail).bbie.defaultValue = null;
            (node.detail as BieEditBbiepNodeDetail).bbie.valueDomainType = undefined;
            (node.detail as BieEditBbiepNodeDetail).bbie.xbtManifestId = null;
            (node.detail as BieEditBbiepNodeDetail).bbie.codeListManifestId = null;
            (node.detail as BieEditBbiepNodeDetail).bbie.agencyIdListManifestId = null;
          } else if (node.bieType === 'BBIE_SC') {
            (node.detail as BieEditBbieScNodeDetail).bbieSc.cardinalityMax = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.facetMinLength = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.facetMaxLength = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.facetPattern = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.fixedOrDefault = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.fixedValue = null;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.defaultValue = null;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.valueDomainType = undefined;
            (node.detail as BieEditBbieScNodeDetail).bbieSc.xbtManifestId = null;
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
    this.isUpdating = true;
    this.stateTransitionFlowService.requestDependencySelection({
      state,
      rootTopLevelAsbiepIds: [this.rootNode.topLevelAsbiepId],
      validateSelection: (selection: StateDependencySelection) =>
        this.service.validateStateDependencies(this.rootNode.topLevelAsbiepId, state, selection),
      normalizeTargets: (dependencyTargets) => {
        const rootTopLevelAsbiepId = this.rootNode.topLevelAsbiepId;
        return dependencyTargets.map(target => ({
          ...target,
          selectable: target.selectable !== false,
          // Match the list page behavior: keep the requested root BIE selected,
          // but do not preselect dependency rows when the dialog first opens.
          checked: target.nodeType === 'BIE' && target.topLevelAsbiepId === rootTopLevelAsbiepId
            ? true
            : target.checked
        }));
      }
    }).pipe(
      finalize(() => {
        this.isUpdating = false;
      })
    ).subscribe((selection?: StateDependencySelection) => {
      if (selection === undefined) {
        return;
      }
      this.isUpdating = true;
      this.service.setState(
        this.rootNode.topLevelAsbiepId,
        state,
        selection.topLevelAsbiepIds,
        selection.codeListManifestIds
      ).subscribe(_ => {
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
        this.openStateUpdateErrorDialog(err);
      });
    }, err => {
      this.isUpdating = false;
      this.openStateUpdateErrorDialog(err);
    });
  }

  discard() {
    this.isUpdating = true;
    this.stateTransitionFlowService.requestDependencySelection({
      state: 'Discard',
      rootTopLevelAsbiepIds: [this.rootNode.topLevelAsbiepId],
      validateSelection: (selection: StateDependencySelection) =>
        this.service.validateStateDependencies(this.rootNode.topLevelAsbiepId, 'Discard', selection),
      normalizeTargets: (dependencyTargets) => {
        const rootTopLevelAsbiepId = this.rootNode.topLevelAsbiepId;
        return dependencyTargets.map(target => ({
          ...target,
          selectable: target.selectable !== false,
          checked: target.nodeType === 'BIE' && target.topLevelAsbiepId === rootTopLevelAsbiepId
            ? true
            : target.checked
        }));
      }
    }).pipe(
      finalize(() => {
        this.isUpdating = false;
      })
    ).subscribe((selection?: StateDependencySelection) => {
      if (selection === undefined) {
        return;
      }
      this.isUpdating = true;
      this.service.discard(
        this.rootNode.topLevelAsbiepId,
        selection.topLevelAsbiepIds
      ).subscribe(_ => {
        this.isUpdating = false;
        this.snackBar.open('Discarded', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/profile_bie');
      }, err => {
        this.isUpdating = false;
        this.openStateUpdateErrorDialog(err);
      });
    }, err => {
      this.isUpdating = false;
      this.openStateUpdateErrorDialog(err);
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

  /**
   * Issue #1519: the BIE-root "OpenAPI Document Information" panel is shown only when the BIE root (level 0)
   * is selected and the BIE participates in at least one OpenAPI Document. OAS bindings attach to the
   * top-level ASBIEP only, so nested ABIE nodes never show the panel.
   */
  hasOasDocInformation(): boolean {
    return this.isAbieDetail() && !!this.selectedNode && this.selectedNode.level === 0
      && !!this.rootNode && (this.rootNode.bieForOasDocList?.length || 0) > 0;
  }

  /**
   * Issue #1519: whether the OpenAPI panel is rendered at all. It is shown for the BIE root (level 0) only
   * when at least one OpenAPI Document exists (see oasDocExists) — there is nothing to surface or bind to
   * otherwise — and then either when there is at least one binding (read-only or editable) or when the BIE
   * is editable (so the first binding can be added from here). Nested ABIE nodes never participate.
   */
  showOasDocPanel(): boolean {
    return this.isAbieDetail() && !!this.selectedNode && this.selectedNode.level === 0
      && this.oasDocExists
      && (this.hasOasDocInformation() || this.isOasDocEditable());
  }

  // Issue #1519: whether any OpenAPI Document exists at all. The BIE-root panel (and its "+" add action) is
  // pointless when there are no documents to bind to, so it stays hidden until at least one exists. Loaded
  // once when the BIE root loads; a BIE already bound to a document trivially implies one exists.
  oasDocExists = false;

  private loadOasDocExistence(): void {
    if ((this.rootNode?.bieForOasDocList?.length || 0) > 0) {
      this.oasDocExists = true;
      return;
    }
    const request = new OasDocListRequest();
    request.page.sortActives = ['title'];
    request.page.sortDirections = ['asc'];
    request.page.pageIndex = 0;
    request.page.pageSize = 1;
    this.openAPIService.getOasDocList(request).subscribe(resp => {
      this.oasDocExists = !!resp && ((resp.length || 0) > 0 || (resp.list?.length || 0) > 0);
    });
  }

  // Issue #1519: a binding's document label = title + version (version omitted when blank). Two documents
  // can share a title, so the version disambiguates them in the collapsed summary and tooltip.
  oasDocLabel(b: BieForOasDoc): string {
    const title = b.oasDocTitle || 'OpenAPI Document';
    const version = (b.oasDocVersion || '').trim();
    return version ? `${title} ${version}` : title;
  }

  // Issue #1519: flatten this BIE's bindings to distinct OpenAPI operations — one per (document, verb,
  // path); a Request/Response pair on the same operation counts once. Each keeps its document label.
  private oasOperations(): { title: string; op: string }[] {
    const seen = new Set<string>();
    const operations: { title: string; op: string }[] = [];
    this.oasBindings.forEach(b => {
      const op = [b.verb, b.resourceName].map(s => (s || '').trim()).filter(s => !!s).join(' ')
        || (b.operationId || '').trim();
      const key = b.oasDocId + '|' + op;
      if (seen.has(key)) {
        return;
      }
      seen.add(key);
      operations.push({title: this.oasDocLabel(b), op});
    });
    return operations;
  }

  private oasOperationLabel(e: { title: string; op: string }): string {
    return e.op ? `${e.title}: ${e.op}` : e.title;
  }

  /**
   * Issue #1519: compact one-line summary for the collapsed panel header — the first operation in full, then
   * "+ N operations" for the rest, so the header stays short. The full list is in oasDocBindingTooltip().
   */
  oasDocBindingSummary(): string {
    const operations = this.oasOperations();
    if (operations.length === 0) {
      return '';
    }
    const firstLabel = this.oasOperationLabel(operations[0]);
    const rest = operations.length - 1;
    return rest > 0 ? `${firstLabel} + ${rest} operation${rest > 1 ? 's' : ''}` : firstLabel;
  }

  /** Issue #1519: full per-operation list for the collapsed header tooltip (the summary shows only the first). */
  oasDocBindingTooltip(): string {
    return this.oasOperations().map(e => this.oasOperationLabel(e)).join(' · ');
  }

  /** Issue #1519: OAS bindings are editable from the BIE root only while the BIE root itself is editable. */
  isOasDocEditable(): boolean {
    return this.isEditable(this.selectedNode);
  }

  /**
   * Issue #1610 (parity with the OpenAPI Document editor): a DELETE Request body is honored only from
   * OpenAPI 3.1; a binding whose owning document targets OpenAPI 3.0.x has that body dropped on generation.
   * Because the BIE-root panel spans documents with possibly different OpenAPI versions, the warning is
   * per binding card. This is read-only here — the OpenAPI Version itself is changed on the OpenAPI
   * Document screen, not on the BIE screen.
   */
  isOasBindingDeleteBodyIgnored(b: BieForOasDoc): boolean {
    return b.verb === 'DELETE' && b.messageBody === 'Request'
      && !(b.openAPIVersion || '').trim().startsWith('3.1');
  }

  get oasBindings(): BieForOasDoc[] {
    return (this.rootNode && this.rootNode.bieForOasDocList) || [];
  }

  // Issue #1519: per-document uniqueness sets, recomputed on every edit. Operation IDs must be unique
  // within a document (#1732) and each (resource path, verb) owns at most one Request and one Response
  // body (#1492). Keys are prefixed with oasDocId because the panel spans multiple documents.
  oasDuplicateOperationIds: Set<string> = new Set<string>();
  oasDuplicateBodySlots: Set<string> = new Set<string>();

  private oasOperationIdKey(b: BieForOasDoc): string {
    return b.oasDocId + '|' + (b.operationId || '').trim();
  }

  private oasBodySlotKey(b: BieForOasDoc): string {
    return b.oasDocId + '|' + (b.resourceName || '').trim() + '|' + (b.verb || '') + '|' + (b.messageBody || '');
  }

  recomputeOasValidation(): void {
    const bindings = this.oasBindings;
    // Operation ID must be unique within a document: count DISTINCT operations sharing an operation id.
    const opsByOperationId = new Map<string, Set<number>>();
    bindings.forEach(b => {
      const key = this.oasOperationIdKey(b);
      if (!opsByOperationId.has(key)) {
        opsByOperationId.set(key, new Set<number>());
      }
      opsByOperationId.get(key).add(b.oasOperationId);
    });
    this.oasDuplicateOperationIds = new Set<string>();
    opsByOperationId.forEach((ops, key) => {
      if (ops.size > 1) {
        this.oasDuplicateOperationIds.add(key);
      }
    });
    // Each (resource path, verb, message body) may appear at most once within a document.
    const slotCount = new Map<string, number>();
    bindings.forEach(b => {
      const key = this.oasBodySlotKey(b);
      slotCount.set(key, (slotCount.get(key) || 0) + 1);
    });
    this.oasDuplicateBodySlots = new Set<string>();
    slotCount.forEach((count, key) => {
      if (count > 1) {
        this.oasDuplicateBodySlots.add(key);
      }
    });
  }

  isOasOperationIdDuplicate(b: BieForOasDoc): boolean {
    return this.oasDuplicateOperationIds.has(this.oasOperationIdKey(b));
  }

  isOasBodySlotDuplicate(b: BieForOasDoc): boolean {
    return this.oasDuplicateBodySlots.has(this.oasBodySlotKey(b));
  }

  oasOperationIdErrorStateMatcher(b: BieForOasDoc): ErrorStateMatcher {
    return {
      isErrorState: (): boolean =>
        !b.operationId || !b.operationId.trim() || this.isOasOperationIdDuplicate(b)
    };
  }

  oasBodySlotErrorStateMatcher(b: BieForOasDoc): ErrorStateMatcher {
    return {
      isErrorState: (): boolean => this.isOasBodySlotDuplicate(b)
    };
  }

  // A request body is never valid for GET, so revert it to Response; then resync the operation id's leading
  // verb word (#1732) while preserving the manually edited BIE-name segment.
  onOasVerbChange(b: BieForOasDoc): void {
    if (b.messageBody === 'Request' && b.verb === 'GET') {
      b.messageBody = 'Response';
    }
    b.operationId = recomputeOperationId(b.verb, b.operationId, b.arrayIndicator);
    this.recomputeOasValidation();
  }

  // The array indicator rewrites both the operation id ('List') and the resource path ('-list') suffixes.
  onOasArrayChange(b: BieForOasDoc): void {
    b.operationId = this.applyOasSuffix(b.operationId, 'List', b.arrayIndicator);
    b.resourceName = this.applyOasSuffix(b.resourceName, '-list', b.arrayIndicator);
    this.recomputeOasValidation();
  }

  private applyOasSuffix(value: string, suffix: string, present: boolean): string {
    const current = value || '';
    if (present) {
      return current.endsWith(suffix) ? current : current + suffix;
    }
    return current.endsWith(suffix) ? current.substring(0, current.length - suffix.length) : current;
  }

  hasOasChanges(): boolean {
    return this.oasBindings.some(b => b.isChanged);
  }

  hasOasErrors(): boolean {
    return this.oasBindings.some(b =>
      !b.operationId || !b.operationId.trim() || this.isOasOperationIdDuplicate(b) || this.isOasBodySlotDuplicate(b));
  }

  /**
   * Issue #1519: reload this BIE's OpenAPI bindings from the server (used after a save and on discard). The
   * fresh BieForOasDoc objects are reset() in the node constructor, so change tracking starts clean.
   */
  private reloadOasBindings(done?: () => void): void {
    this.service.getRootNode(this.topLevelAsbiepId).subscribe(rootNode => {
      this.rootNode.bieForOasDocList = new BieEditAbieNode(rootNode).bieForOasDocList;
      this.recomputeOasValidation();
      this.seedOasCommittedErrorTypes();
      this.loadOasDocSecuritySchemes();
      if (done) {
        done();
      }
    });
  }

  /**
   * Issue #1519: unbind a single binding — remove this BIE from the operation in that OpenAPI Document
   * (the per-card '−'). Reuses the same delete endpoint as the OpenAPI Document editor's "Remove" action,
   * with a confirmation, then reloads the panel.
   */
  unbindOasBinding(b: BieForOasDoc): void {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove this operation from the OpenAPI Document?';
    dialogConfig.data.content = [
      'This unbinds the BIE from "' + b.oasDocTitle + '" (' + b.verb + ' ' + b.resourceName + ').',
      'Are you sure you want to remove it?'
    ];
    dialogConfig.data.action = 'Remove';
    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      const request = new BieForOasDocDeleteRequest();
      request.oasDocId = b.oasDocId;
      request.bieForOasDocList = [b];
      this.loading = true;
      this.openAPIService.removeBieForOasDoc(request).pipe(finalize(() => this.loading = false)).subscribe({
        next: () => {
          this.snackBar.open('Removed from OpenAPI Document.', '', {duration: 3000});
          this.reloadOasBindings();
        },
        error: (err: HttpErrorResponse) => {
          const message = (err && err.error && err.error.message)
            ? err.error.message : 'Failed to remove from OpenAPI Document.';
          this.snackBar.open(message, '', {duration: 5000});
        }
      });
    });
  }

  /**
   * Issue #1519: persist the edited OpenAPI bindings. OAS documents are the source of truth, so writes go
   * through the existing per-document endpoint (PUT /api/oas_doc/{id}/bie_list/detail), which preserves the
   * #1492 (one Request/one Response per path+verb), #1732 (operation id) and #1728 (schema naming) rules
   * inside a single @Transactional unit. Because one BIE can bind to several documents, the bindings are
   * grouped by document and one request is sent per document that has a change.
   */
  updateOasDocInformation(): void {
    if (!this.hasOasChanges() || this.hasOasErrors()) {
      return;
    }
    const bindings = this.oasBindings;
    const changedDocIds = Array.from(new Set(bindings.filter(b => b.isChanged).map(b => b.oasDocId)));
    if (changedDocIds.length === 0) {
      return;
    }
    // Each document is its own @Transactional unit, so save them independently and report per document.
    // A document that fails keeps its in-memory edits (so the user can correct and retry just that one)
    // while documents that did persist are marked clean; the panel only fully reloads when all succeeded.
    const requests = changedDocIds.map(oasDocId => {
      const request = new BieForOasDocUpdateRequest();
      request.oasDocId = oasDocId;
      // Send only the changed rows of this document, exactly as the OpenAPI Document editor does (so an
      // unchanged operation's per-op security/error response is never needlessly re-persisted). Cross-row
      // duplicate detection happens client-side over the full union (recomputeOasValidation / hasOasErrors).
      request.bieForOasDocList = bindings.filter(b => b.oasDocId === oasDocId && b.isChanged);
      return this.openAPIService.updateDetails(request).pipe(
        map(() => ({oasDocId, ok: true, error: null as HttpErrorResponse})),
        catchError((error: HttpErrorResponse) => of({oasDocId, ok: false, error}))
      );
    });
    this.loading = true;
    forkJoin(requests).pipe(finalize(() => this.loading = false)).subscribe(resultsByDoc => {
      const failures = resultsByDoc.filter(r => !r.ok);
      if (failures.length === 0) {
        this.snackBar.open('Updated OpenAPI Document information.', '', {duration: 3000});
        this.reloadOasBindings();
        return;
      }
      // Mark the documents that did persist as clean (their in-memory state equals what was saved) and leave
      // the failed documents' rows dirty so only those remain editable/resubmittable.
      resultsByDoc.filter(r => r.ok).forEach(r =>
        bindings.filter(b => b.oasDocId === r.oasDocId).forEach(b => b.reset()));
      this.recomputeOasValidation();
      const firstError = failures[0].error;
      const message = (firstError && firstError.error && firstError.error.message)
        ? firstError.error.message : 'Failed to update OpenAPI Document information.';
      this.snackBar.open(message, '', {duration: 5000});
    });
  }

  /**
   * Issue #1519: open the "Add to OpenAPI Document" dialog (Phase 3). It returns an AssignBieForOasDoc
   * payload; the actual bind reuses the existing assign endpoint so the resource/operation/body chain and
   * the #1492 (one body per path+verb) / #1732 (operation id) rules are honored by the backend.
   */
  openAddOasDocDialog(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      topLevelAsbiepId: this.topLevelAsbiepId,
      propertyTerm: this.rootNode.name,
      // The backend derives the resource path from the BIE's first business context (matches the
      // OpenAPI Document editor's Add flow); pass it so the dialog can preview the same path.
      businessContextName: (this.businessContexts && this.businessContexts.length > 0)
        ? this.businessContexts[0].name : '',
      // Issue #1492: hand the dialog this BIE's current bindings so it can pre-check (client-side, like the
      // OpenAPI Document editor's Add dialog) that the chosen (Verb, Message Body) does not duplicate a body
      // the BIE already has on the selected document.
      existingBindings: this.oasBindings
    };
    this.dialog.open(BieOasDocAddDialogComponent, dialogConfig).afterClosed()
      .subscribe((payload: AssignBieForOasDoc) => {
        if (!payload) {
          return;
        }
        this.loading = true;
        this.openAPIService.assignBieForOasDoc(payload).pipe(finalize(() => this.loading = false)).subscribe({
          next: () => {
            this.snackBar.open('Added to OpenAPI Document.', '', {duration: 3000});
            this.reloadOasBindings();
          },
          error: (err: HttpErrorResponse) => {
            const message = (err && err.error && err.error.message)
              ? err.error.message : 'Failed to add to OpenAPI Document.';
            this.snackBar.open(message, '', {duration: 5000});
          }
        });
      });
  }

  // ----- Issue #1519: per-operation Error Response (#1347), Security (#1729) and Tag on the BIE-root cards.
  // These reuse the OpenAPI Document editor's dialogs and persist through the same save path.

  oasErrorResponseBodyTypeOptions: { value: string; label: string }[] = [
    {value: 'NONE', label: 'No Response Body'},
    {value: 'PROBLEM_DETAILS', label: 'IETF Problem Details'},
    {value: 'CONFIRM_MESSAGE', label: 'OAGi Confirm Message'}
  ];
  // Remembers each binding's last committed Error Response type so a cancelled ConfirmMessage pick reverts.
  private oasCommittedErrorType = new WeakMap<BieForOasDoc, string>();

  private seedOasCommittedErrorTypes(): void {
    this.oasBindings.forEach(b => this.oasCommittedErrorType.set(b, b.errorResponseBodyType || 'NONE'));
  }


  // NONE / PROBLEM_DETAILS commit immediately; CONFIRM_MESSAGE opens the BIE picker. Cancelling with no BIE
  // (and none previously chosen) reverts the selector. The body type is per operation, so it is applied to
  // every binding sharing the oasOperationId.
  onOasErrorResponseBodyTypeChange(b: BieForOasDoc): void {
    const newType = b.errorResponseBodyType;
    const previousType = this.oasCommittedErrorType.get(b) || 'NONE';
    if (newType !== 'CONFIRM_MESSAGE') {
      this.applyOasErrorResponseBodyType(b, newType, undefined, undefined);
      return;
    }
    this.openOasConfirmMessageDialog(b, result => {
      if (result) {
        this.applyOasErrorResponseBodyType(b, 'CONFIRM_MESSAGE', result.topLevelAsbiepId, result.den);
      } else if (b.confirmMessageTopLevelAsbiepId) {
        this.applyOasErrorResponseBodyType(b, 'CONFIRM_MESSAGE', b.confirmMessageTopLevelAsbiepId, b.confirmMessageDen);
      } else {
        this.applyOasErrorResponseBodyType(b, previousType, undefined, undefined);
      }
    });
  }

  openOasConfirmMessageBiePicker(b: BieForOasDoc, event?: Event): void {
    if (event) {
      event.stopPropagation();
      event.preventDefault();
    }
    this.openOasConfirmMessageDialog(b, result => {
      if (result) {
        this.applyOasErrorResponseBodyType(b, 'CONFIRM_MESSAGE', result.topLevelAsbiepId, result.den);
      }
    });
  }

  private openOasConfirmMessageDialog(b: BieForOasDoc,
                                      handler: (result: OasDocConfirmMessageDialogResult | undefined) => void): void {
    // The OpenAPI Document has no release of its own; each connected BIE carries its release, so lock the
    // ConfirmMessage picker to this binding's release.
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      title: 'Select ConfirmMessage BIE',
      releaseId: b.releaseId,
      releaseNum: b.releaseNum
    };
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.autoFocus = false;
    this.dialog.open(OasDocConfirmMessageDialogComponent, dialogConfig)
      .afterClosed().subscribe((result: OasDocConfirmMessageDialogResult | undefined) => handler(result));
  }

  // The Error Response body type is per operation, so apply it to every binding sharing the oasOperationId
  // (a not-yet-persisted row has oasOperationId 0 and must never be treated as a sibling).
  private applyOasErrorResponseBodyType(b: BieForOasDoc, bodyType: string,
                                        confirmTopLevelAsbiepId: number, confirmDen: string): void {
    const siblings = this.oasBindings.filter(r => !!r.oasOperationId && r.oasOperationId === b.oasOperationId);
    const targets = siblings.length > 0 ? siblings : [b];
    const clearConfirm = bodyType !== 'CONFIRM_MESSAGE';
    targets.forEach(r => {
      r.errorResponseBodyType = bodyType;
      r.confirmMessageTopLevelAsbiepId = clearConfirm ? undefined : confirmTopLevelAsbiepId;
      r.confirmMessageDen = clearConfirm ? '' : (confirmDen || '');
      this.oasCommittedErrorType.set(r, bodyType);
    });
  }

  // Issue #1729: the Security cell mirrors the OpenAPI Document editor's per-operation Security column. The
  // scheme catalog lives on the document, so each bound document's schemes are pre-fetched (and cached) so
  // the cell can decide between the em-dash placeholder (no schemes, not overridden) and the clickable
  // summary, and so the Edit dialog lists exactly the schemes defined on that document.
  private oasDocSecuritySchemes = new Map<number, OasSecurityScheme[]>();

  private loadOasDocSecuritySchemes(): void {
    const docIds = Array.from(new Set(this.oasBindings.map(b => b.oasDocId).filter(id => !!id)));
    docIds.forEach(docId => {
      if (this.oasDocSecuritySchemes.has(docId)) {
        return;
      }
      this.openAPIService.getOasDoc(docId).subscribe(oasDoc => {
        this.oasDocSecuritySchemes.set(docId, (oasDoc && oasDoc.securitySchemes) ? oasDoc.securitySchemes : []);
      });
    });
  }

  oasSecuritySchemesFor(b: BieForOasDoc): OasSecurityScheme[] {
    return this.oasDocSecuritySchemes.get(b.oasDocId) || [];
  }

  // Mirrors the doc editor: with no document schemes and no operation override there is nothing to choose,
  // so the cell shows an em dash rather than a clickable summary.
  showOasSecurityDash(b: BieForOasDoc): boolean {
    return this.oasSecuritySchemesFor(b).length === 0 && !b.securityOverridden;
  }

  // Same text the doc editor's Security column shows: 'Inherited' / 'Public' / the requirement summary.
  operationSecuritySummary(b: BieForOasDoc): string {
    if (!b.securityOverridden) {
      return 'Inherited';
    }
    const summary = this.securityRequirementSummary(b.securityRequirements);
    return summary === 'No Security' ? 'Public' : summary;
  }

  private securityRequirementSummary(requirements: OasSecurityRequirement[]): string {
    if (!requirements || requirements.length === 0) {
      return 'No Security';
    }
    return requirements.map(req => {
      if (req.anonymous) {
        return 'anonymous';
      }
      return (req.schemes || [])
        .filter(scheme => !!scheme.schemeName)
        .map(scheme => {
          const scopes = scheme.scopes && scheme.scopes.length > 0 ? ` (${scheme.scopes.join(', ')})` : '';
          return `${scheme.schemeName}${scopes}`;
        })
        .join(' + ');
    }).filter(text => !!text).join(' OR ');
  }

  // Edit per-operation Security (#1729), reusing the same dialog as the doc editor's Security column. Uses
  // the pre-fetched scheme catalog (falls back to a fetch on a cache miss). The result is applied to every
  // binding sharing the operation.
  openOasOperationSecurityDialog(b: BieForOasDoc): void {
    if (!this.isOasDocEditable()) {
      return;
    }
    const cached = this.oasDocSecuritySchemes.get(b.oasDocId);
    if (cached !== undefined) {
      this.openOasSecurityDialogWithSchemes(b, cached);
      return;
    }
    this.openAPIService.getOasDoc(b.oasDocId).subscribe(oasDoc => {
      const schemes = (oasDoc && oasDoc.securitySchemes) ? oasDoc.securitySchemes : [];
      this.oasDocSecuritySchemes.set(b.oasDocId, schemes);
      this.openOasSecurityDialogWithSchemes(b, schemes);
    });
  }

  private openOasSecurityDialogWithSchemes(b: BieForOasDoc, schemes: OasSecurityScheme[]): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      title: 'Operation Security',
      securitySchemes: schemes,
      securityOverridden: b.securityOverridden,
      securityRequirements: JSON.parse(JSON.stringify(b.securityRequirements || [])),
      allowInherit: true
    };
    dialogConfig.width = '760px';
    dialogConfig.maxHeight = '85vh';
    dialogConfig.autoFocus = false;
    this.dialog.open(OasDocSecurityRequirementDialogComponent, dialogConfig)
      .afterClosed().subscribe(result => {
        if (!result) {
          return;
        }
        const siblings = this.oasBindings.filter(r => !!r.oasOperationId && r.oasOperationId === b.oasOperationId);
        const targets = siblings.length > 0 ? siblings : [b];
        targets.forEach(r => {
          r.securityOverridden = result.securityOverridden;
          r.securityRequirements = JSON.parse(JSON.stringify(result.securityRequirements || []));
        });
      });
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
    this.bizCtxService.unassign(this.topLevelAsbiepId, businessContext)
      .subscribe(_ => {
        this.businessContexts = this.businessContexts.filter(e => e.businessContextId !== businessContext.businessContextId);
        this.businessContextUpdating = false;
        this.businessContextCtrl.setValue(null);
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      }, err => {
        this.businessContextUpdating = false;
        throw err;
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
