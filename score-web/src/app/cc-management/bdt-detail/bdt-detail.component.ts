import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {Component, HostListener, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {MatChipInputEvent} from '@angular/material/chips';
import {MatSidenav} from '@angular/material/sidenav';
import {MatTableDataSource} from '@angular/material/table';
import {finalize, switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {AgencyIdListSummary} from '../../agency-id-list-management/domain/agency-id-list';
import {AgencyIdListService} from '../../agency-id-list-management/domain/agency-id-list.service';
import {CodeListSummary} from '../../code-list-management/domain/code-list';
import {CodeListService} from '../../code-list-management/domain/code-list.service';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {ReleaseService} from '../../release-management/domain/release.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {CcListService} from '../cc-list/domain/cc-list.service';
import {
  CcFlatNode,
  CcFlatNodeDatabase,
  CcFlatNodeDataSource,
  CcFlatNodeDataSourceSearcher,
  DtFlatNode,
  DtScFlatNode
} from '../domain/cc-flat-tree';
import {CcNodeService} from '../domain/core-component-node.service';
import {
  CcAccNodeInfo,
  CcAsccpNodeInfo,
  CcBccpNodeInfo,
  CcBdtPriRestri,
  CcDtNodeInfo,
  CcDtScNodeInfo,
  CcNodeInfo,
  Comment,
  DtAwdPriDetails,
  DtDetails,
  DtPrimitiveAware,
  DtScAwdPriDetails,
  EntityType,
  EntityTypes,
  OagisComponentType,
  OagisComponentTypes,
  XbtSummary
} from '../domain/core-component-node';
import {AuthService} from '../../authentication/auth.service';
import {CommentControl} from '../domain/comment-component';
import {forkJoin, ReplaySubject} from 'rxjs';
import {Location} from '@angular/common';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {SearchOptionsService} from '../search-options-dialog/domain/search-options-service';
import {SearchOptionsDialogComponent} from '../search-options-dialog/search-options-dialog.component';
import {Clipboard} from '@angular/cdk/clipboard';
import {initFilter, loadBooleanProperty, saveBooleanProperty, trim} from '../../common/utility';
import {RxStompService} from '../../common/score-rx-stomp';
import {Message} from '@stomp/stompjs';
import {MatMenuTrigger} from '@angular/material/menu';
import {ShortTag, Tag} from '../../tag-management/domain/tag';
import {TagService} from '../../tag-management/domain/tag.service';
import {EditTagsDialogComponent} from '../../tag-management/edit-tags-dialog/edit-tags-dialog.component';
import {FormControl} from '@angular/forms';
import {FindUsagesDialogComponent} from '../find-usages-dialog/find-usages-dialog.component';
import {PreferencesInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';


@Component({
  selector: 'score-bccp-detail',
  templateUrl: './bdt-detail.component.html',
  styleUrls: ['./bdt-detail.component.css']
})
export class BdtDetailComponent implements OnInit, DtPrimitiveAware {

  protected readonly trim = trim;

  title: string;
  _innerHeight: number = window.innerHeight;
  paddingPixel = 12;
  manifestId: number;
  type = 'DT';
  isUpdating: boolean;
  componentTypes: OagisComponentType[] = OagisComponentTypes;
  entityTypes: EntityType[] = EntityTypes;

  rootNode: DtFlatNode;
  dataSource: CcFlatNodeDataSource<CcFlatNode>;
  searcher: CcFlatNodeDataSourceSearcher<CcFlatNode>;

  prevDtDetails: DtDetails;
  selectedNode: CcFlatNode;
  cursorNode: CcFlatNode;

  workingRelease = false;
  namespaces: NamespaceSummary[];
  tags: Tag[] = [];
  commentControl: CommentControl;

  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<NamespaceSummary[]> = new ReplaySubject<NamespaceSummary[]>(1);

  initialExpandDepth = 10;

  primitiveTypeList = ['Primitive', 'Code List', 'Agency ID List'];
  primitiveList = ['Binary', 'Boolean', 'Decimal', 'Double', 'Float', 'Integer', 'NormalizedString', 'String', 'TimeDuration', 'TimePoint', 'Token'];

  selectedValueDomain: any;

  codeLists: CodeListSummary[];
  agencyIdLists: AgencyIdListSummary[];
  xbtList: XbtSummary[];

  restrictionListDisplayedColumns: string[] = ['select', 'type', 'name', 'xbt'];
  restrictionDataSource = new MatTableDataSource<any>();

  @ViewChildren(MatMenuTrigger) menuTriggerList: QueryList<MatMenuTrigger>;
  contextMenuItem: CcFlatNode;
  @ViewChild('sidenav', {static: true}) sidenav: MatSidenav;
  @ViewChild('virtualScroll', {static: true}) public virtualScroll: CdkVirtualScrollViewport;
  virtualScrollItemSize = 33;

  get minBufferPx(): number {
    return 10000 * this.virtualScrollItemSize;
  }

  get maxBufferPx(): number {
    return 1000000 * this.virtualScrollItemSize;
  }

  preferencesInfo: PreferencesInfo;
  HIDE_CARDINALITY_PROPERTY_KEY = 'CC-Settings-Hide-Cardinality';
  HIDE_PROHIBITED_PROPERTY_KEY = 'CC-Settings-Hide-Prohibited';

  get hideCardinality(): boolean {
    return this.dataSource.hideCardinality;
  }

  set hideCardinality(hideCardinality: boolean) {
    this.dataSource.hideCardinality = hideCardinality;
    saveBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, hideCardinality);
  }

  get hideProhibited(): boolean {
    return this.dataSource.hideProhibited;
  }

  set hideProhibited(hideProhibited: boolean) {
    this.dataSource.hideProhibited = hideProhibited;
    saveBooleanProperty(this.auth.getUserToken(), this.HIDE_PROHIBITED_PROPERTY_KEY, hideProhibited);
  }

  constructor(private service: CcNodeService,
              private codeListService: CodeListService,
              private agencyIdListservice: AgencyIdListService,
              private ccListService: CcListService,
              private searchOptionsService: SearchOptionsService,
              private releaseService: ReleaseService,
              private snackBar: MatSnackBar,
              private namespaceService: NamespaceService,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private tagService: TagService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private auth: AuthService,
              private stompService: RxStompService,
              private clipboard: Clipboard) {
  }

  ngOnInit() {
    this.commentControl = new CommentControl(this.sidenav, this.service);

    this.isUpdating = true;
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        this.manifestId = parseInt(params.get('manifestId'), 10);
        return forkJoin([
          this.service.getGraphNode(this.type, this.manifestId),
          this.service.getDtDetails(this.manifestId),
          this.tagService.getTags(),
          this.preferencesService.load(this.auth.getUserToken())
        ]);
      })).subscribe(([ccGraph, dtDetails, tags, preferencesInfo]) => {

      this.namespaceService.getNamespaceSummaries(dtDetails.library.libraryId).subscribe(namespaces => {
        this.namespaces = namespaces;
        initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList,
          this.getSelectableNamespaces(), (e) => e.uri);
      });

      if (dtDetails.log.revisionNum > 1) {
        this.service.getPrevDtDetails(this.manifestId)
            .subscribe(prevDtDetails => {
              this.prevDtDetails = prevDtDetails;
            }, err => {
              if (err.status === 404) {
                // ignore
              } else {
                throw err;
              }
            });
      }

      this.tags = tags;
      this.preferencesInfo = preferencesInfo;

      // subscribe an event
      this.stompService.watch('/topic/dt/' + this.manifestId).subscribe((message: Message) => {
        const data = JSON.parse(message.body);
        if (data.properties.actor !== this.currentUser) {
          let noti;
          if (data.action === 'UpdateDetail') {
            noti = 'BDT is updated by ' + data.properties.actor;
          } else if (data.action === 'ChangeState') {
            noti = 'State changed to \'' + data.properties.State + '\' by ' + data.properties.actor;
          } else if (data.action === 'AddComment' && this.sidenav.opened) {
            this.receiveCommentEvent(data);
          } else {
            return;
          }

          if (noti) {
            const snackBarRef = this.snackBar.open(noti, 'Reload');
            snackBarRef.onAction().subscribe(() => {
              this.ngOnInit();
            });
          }
        }
      });

      const database = new CcFlatNodeDatabase<CcFlatNode>(ccGraph, 'DT', this.manifestId);
      this.dataSource = new CcFlatNodeDataSource<CcFlatNode>(database, this.service);
      this.searcher = new CcFlatNodeDataSourceSearcher<CcFlatNode>(this.dataSource, database);
      this.dataSource.init();
      this.dataSource.hideCardinality = loadBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, false);
      this.dataSource.hideProhibited = loadBooleanProperty(this.auth.getUserToken(), this.HIDE_PROHIBITED_PROPERTY_KEY, true);

      this.workingRelease = dtDetails.release.workingRelease;

      this.rootNode = this.dataSource.data[0] as DtFlatNode;
      this.rootNode.access = dtDetails.access;
      this.rootNode.state = dtDetails.state;
      this.rootNode.reset();

      forkJoin([
        this.codeListService.getCodeListSummaries(dtDetails.release.releaseId),
        this.agencyIdListservice.getAgencyIdListSummaries(dtDetails.release.releaseId),
        this.ccListService.getXbtListSummaries(dtDetails.release.releaseId),
      ]).subscribe(([codeLists, agencyIdLists, xbtList]) => {
        this.codeLists = codeLists;
        this.agencyIdLists = agencyIdLists;
        this.xbtList = xbtList;

        // Issue #1254
        // Initial expanding by the query path
        const url = this.router.url;
        const manifestId = this.manifestId.toString();
        const queryPath = url.substring(url.indexOf(manifestId) + manifestId.length + 1);

        if (!!queryPath) {
          this.goToPath(queryPath);
        } else {
          this.onClick(this.dataSource.data[0]);
        }

        this.isUpdating = false;
      });
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

  receiveCommentEvent(evt) {
    const comment = new Comment();
    comment.commentId = evt.properties.commentId;
    comment.prevCommentId = evt.properties.prevCommentId;
    comment.text = evt.properties.text;
    comment.created.who.loginId = evt.properties.actor;
    comment.timestamp = evt.properties.timestamp;
    comment.isNew = true;

    if (comment.prevCommentId) {
      const idx = this.commentControl.comments.findIndex(e => e.commentId === comment.prevCommentId);
      const childrenCnt = this.commentControl.comments.filter(e => e.prevCommentId === comment.prevCommentId).length;
      this.commentControl.comments.splice(idx + childrenCnt + 1, 0, comment);
    } else {
      this.commentControl.comments.push(comment);
    }
  }

  getSelectableNamespaces(namespaceId?: number): NamespaceSummary[] {
    return this.namespaces.filter(e => {
      if (!!namespaceId && e.namespaceId === namespaceId) {
        return true;
      }
      return (this.userRoles.includes('developer')) ? e.standard : !e.standard;
    });
  }

  isInvalidState(node: CcFlatNode): boolean {
    if (!node) {
      return false;
    }

    if (!(node instanceof DtFlatNode) || !node.detail) {
      return false;
    }

    const detail = node.detail as CcDtNodeInfo;
    return detail.basedBdtState === 'Deleted';
  }

  copyPath(node: CcFlatNode) {
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

  copyLink(node: CcFlatNode, $event?) {
    if ($event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    if (!node) {
      return;
    }

    const url = window.location.href;
    const manifestId = this.manifestId.toString();
    const idIdx = url.indexOf(manifestId);
    const queryPath = url.substring(0, idIdx + manifestId.length) + '/' + node.queryPath;

    this.clipboard.copy(queryPath);
    this.snackBar.open('Copied to clipboard', '', {
      duration: 3000
    });
  }

  reload(snackMsg?: string) {
    let selectedNodeManifestId;
    if (this.selectedNode) {
      selectedNodeManifestId = this.selectedNode.manifestId;
      this.selectedNode = undefined;
    }
    const expandedNodes = this.dataSource.data.filter(e => e.expanded);
    const hideProhibited = this.dataSource.hideProhibited;

    this.isUpdating = true;
    forkJoin([
      this.service.getDtDetails(this.manifestId),
      this.service.getGraphNode(this.rootNode.type, this.manifestId)
    ]).subscribe(([dtDetails, ccGraph]) => {
      const database = new CcFlatNodeDatabase<CcFlatNode>(ccGraph, 'DT', this.manifestId);
      this.dataSource = new CcFlatNodeDataSource<CcFlatNode>(database, this.service);
      this.searcher = new CcFlatNodeDataSourceSearcher<CcFlatNode>(this.dataSource, database);
      this.dataSource.init();
      this.dataSource.hideProhibited = hideProhibited;

      this.workingRelease = dtDetails.release.workingRelease;

      this.rootNode = this.dataSource.data[0] as DtFlatNode;
      this.rootNode.access = dtDetails.access;
      this.rootNode.state = dtDetails.state;
      this.rootNode.reset();

      this.onClick(this.dataSource.data[0]);

      // recover the tree expansion status
      for (const expandedNode of expandedNodes) {
        for (const datum of this.dataSource.data) {
          if (expandedNode.manifestId === datum.manifestId && !this.dataSource.isExpanded(datum)) {
            this.dataSource.toggle(datum);
            break;
          }
        }
      }
      // recover the selected node.
      if (!!selectedNodeManifestId) {
        for (const datum of this.dataSource.data) {
          if (datum.manifestId === selectedNodeManifestId) {
            this.onClick(datum);
            break;
          }
        }
      }

      if (snackMsg) {
        this.snackBar.open(snackMsg, '', {duration: 3000});
      }
      this.isUpdating = false;
    }, err => {
      this.isUpdating = false;
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

  get state(): string {
    return (!!this.rootNode) ? this.rootNode.state : '';
  }

  get access(): string {
    return (!!this.rootNode) ? this.rootNode.access : '';
  }

  hasRevision(): boolean {
    return !!this.prevDtDetails;
  }

  isEditable(): boolean {
    return this.state === 'WIP' && this.access === 'CanEdit';
  }

  onClick(node: CcFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.commentControl.closeCommentSlide();
    this.dataSource.loadDetail(node, (detail: CcNodeInfo) => {
      if (detail instanceof DtFlatNode || detail instanceof DtScFlatNode) {
        detail = detail.detail;
      }
      if (detail instanceof CcDtNodeInfo) {
        (node.detail as CcDtNodeInfo).update(this);
        this.restrictionDataSource.data = (node.detail as CcDtNodeInfo).valueDomains;
      } else {
        (node.detail as CcDtScNodeInfo).update(this);
        this.restrictionDataSource.data = (node.detail as CcDtScNodeInfo).valueDomains;
      }

      this.selectedNode = node;
      this.cursorNode = node;
    });
  }

  sort(list: CcBdtPriRestri[], idColumn: string): CcBdtPriRestri[] {
    return list.sort((a, b) => {
      if (a.type === b.type) {
        let aId, bId;
        if (idColumn === 'bdtPriRestriId') {
          aId = a.bdtPriRestriId;
          bId = b.bdtPriRestriId;
        } else {
          aId = a.bdtScPriRestriId;
          bId = b.bdtScPriRestriId;
        }
        if (aId === bId) {
          return 0;
        }
        if (aId === undefined) {
          return 1;
        }
        if (bId === undefined) {
          return -1;
        }
        return (aId - bId) < 0 ? -1 : 1;
      } else {
        const pA = ('Primitive' === a.type) ? 0 : (('CodeList' === a.type) ? 1 : 2);
        const pB = ('Primitive' === b.type) ? 0 : (('CodeList' === b.type) ? 1 : 2);
        return pA - pB;
      }
    });
  }

  select(row: any) {
    this.selectedValueDomain = row;
  }

  toggleValueDomain(row: any) {
    if (this.isSelected(row)) {
      this.selectedValueDomain = undefined;
    } else {
      this.select(row);
    }
  }

  isSelected(row: any) {
    return this.selectedValueDomain === row;
  }

  /* For type casting of detail property */
  isAccDetail(node?: CcFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.type.toUpperCase() === 'ACC');
  }

  asAccDetail(node?: CcFlatNode): CcAccNodeInfo {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as CcAccNodeInfo;
  }

  isAsccpDetail(node?: CcFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.type.toUpperCase() === 'ASCCP');
  }

  asAsccpDetail(node?: CcFlatNode): CcAsccpNodeInfo {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as CcAsccpNodeInfo;
  }

  isBccpDetail(node?: CcFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.type.toUpperCase() === 'BCCP');
  }

  asBccpDetail(node?: CcFlatNode): CcBccpNodeInfo {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as CcBccpNodeInfo;
  }

  isBdtDetail(node?: CcFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.type.toUpperCase() === 'DT');
  }

  asBdtDetail(node?: CcFlatNode): CcDtNodeInfo {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as CcDtNodeInfo;
  }

  isDtScDetail(node?: CcFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.type.toUpperCase() === 'DT_SC');
  }

  asDtScDetail(node?: CcFlatNode): CcDtScNodeInfo {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as CcDtScNodeInfo;
  }

  get isChanged() {
    return this.dataSource.getChanged().length > 0;
  }

  openInNewTab(url: string) {
    window.open(url, '_blank');
  }

  copyToDefinition(text: string) {
    this.asBdtDetail(this.rootNode).definition = text;
    this.snackBar.open('Copied to definition', '', {
      duration: 3000
    });
  }

  _updateDetails(details: CcFlatNode[]) {
    this.isUpdating = true;
    this.service.updateNodes(details)
      .pipe(finalize(() => {
        this.isUpdating = false;
      }))
      .subscribe(_ => {
        this.dataSource.resetChanged();

        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      }, err => {
      });
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

  get updateDisabled(): boolean {
    return this.state !== 'WIP' || this.access !== 'CanEdit' || !this.isChanged || this.isUpdating;
  }

  updateDetails() {
    if (this.updateDisabled) {
      return;
    }

    const details = this.dataSource.getChanged();
    let emptyDefinition = false;
    let emptyNamespace = false;
    let emptyQualifier = false;
    let emptyPropertyTerm = false;
    for (const detail of details) {
      if (detail.type.toUpperCase() === 'DT') {
        if (!this.asBdtDetail(detail).definition || this.asBdtDetail(detail).definition.length === 0) {
          emptyDefinition = true;
        }
        if (!this.asBdtDetail(detail).namespaceId) {
          emptyNamespace = true;
        }
        const hasBasedDTQualifier = this.asBdtDetail(detail).basedBdtDen.indexOf('_ ') !== -1;
        if (hasBasedDTQualifier && (!this.asBdtDetail(detail).qualifier || this.asBdtDetail(detail).qualifier.length === 0)) {
          emptyQualifier = true;
        }
      }
      if (detail.type.toUpperCase() === 'DT_SC') {
        if (!this.asDtScDetail(detail).definition || this.asDtScDetail(detail).definition.length === 0) {
          emptyDefinition = true;
        }
        if (!this.asDtScDetail(detail).propertyTerm || this.asDtScDetail(detail).propertyTerm.length === 0) {
          emptyPropertyTerm = true;
        }
      }
    }

    if (emptyQualifier) {
      this.snackBar.open('Qualifier is required', '', {
        duration: 3000,
      });
      return;
    }

    if (emptyPropertyTerm) {
      this.snackBar.open('Property Term is required', '', {
        duration: 3000,
      });
      return;
    }

    if (emptyNamespace) {
      this.snackBar.open('Namespace is required', '', {
        duration: 3000,
      });
      return;
    }

    if (emptyDefinition) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Update without definitions.';
      dialogConfig.data.content = ['Are you sure you want to update this without definitions?'];
      dialogConfig.data.action = 'Update anyway';

      this.confirmDialogService.open(dialogConfig).afterClosed()
        .subscribe(result => {
          if (result) {
            this._updateDetails(details);
          }
        });
    } else {
      this._updateDetails(details);
    }
  }

  openSearchOptions() {
    const dialogRef = this.dialog.open(SearchOptionsDialogComponent, {
      data: {},
      width: '600px',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(_ => {
    });
  }

  appendSc(node: CcFlatNode) {
    this.service.appendDtSc(this.rootNode.manifestId).subscribe(_ => {
      this.reload('Appended');
    });
  }

  canDiscard(node: CcFlatNode): boolean {
    if (!(node instanceof DtScFlatNode)) {
      return false;
    }
    return (node as DtScFlatNode).removeAble;
  }

  discardSc(node: CcFlatNode) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove supplementary component?';
    dialogConfig.data.content = ['Are you sure you want to remove this supplementary component?'];
    dialogConfig.data.action = 'Remove anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }
        this.service.discardDtSc(node.manifestId).subscribe(_ => {
          this.reload('Removed');
        });
      });
  }

  _updateState(state: string) {
    if (!state) {
      return;
    }

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
          this.service.updateState(this.rootNode.type, this.rootNode.manifestId, state).subscribe({
            next: () => {
              this.snackBar.open('Updated', '', {duration: 3000});

              this.service.getDtDetails(this.manifestId).subscribe({
                next: dtDetails => this.afterStateChanged(dtDetails.state, dtDetails.access),
                error: err => console.error(err),
                complete: () => (this.isUpdating = false),
              });
            },
            error: err => {
              this.isUpdating = false;
              throw err;
            },
          });
        });
  }

  updateState(state: string) {
    if (!state) {
      return;
    }
    const rootNode = this.dataSource.data[0];

    if (state !== 'WIP') {
      this.dataSource.loadDetail(rootNode, (detail) => {
        if (!this.asBdtDetail(detail).namespaceId) {
          this.snackBar.open('Namespace is required', '', {
            duration: 3000,
          });
          this.onClick(rootNode);
          return;
        } else {
          return this._updateState(state);
        }
      });
    } else {
      return this._updateState(state);
    }
  }

  afterStateChanged(state: string, access: string) {
    this.rootNode.state = state;
    this.rootNode.access = access;
    const root = this.dataSource.data[0];
    (root.detail as CcDtNodeInfo).state = state;
  }

  makeNewRevision() {
    const isDeveloper = this.userRoles.includes('developer');
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = (isDeveloper) ? 'Revise this DT?' : 'Amend this DT?';
    dialogConfig.data.content = [(isDeveloper) ? 'Are you sure you want to revise this DT?' : 'Are you sure you want to amend this DT?'];
    dialogConfig.data.action = (isDeveloper) ? 'Revise' : 'Amend';

    this.confirmDialogService.open(dialogConfig).afterClosed()
        .subscribe(result => {
          if (!result) {
            return;
          }

          this.isUpdating = true;
          this.service.makeNewRevision(this.rootNode.type, this.rootNode.manifestId).subscribe(_ => {
            forkJoin([
              this.service.getDtDetails(this.manifestId),
              this.service.getPrevDtDetails(this.manifestId),
            ]).subscribe(([dtDetails, prevDtDetails]) => {
              this.manifestId = dtDetails.dtManifestId;
              this.afterStateChanged(dtDetails.state, dtDetails.access);
              this.prevDtDetails = prevDtDetails;
              this.snackBar.open((isDeveloper) ? 'Revised' : 'Amended', '', {
                duration: 3000,
              });
              this.reload();
            });
          }, err => {
            this.isUpdating = false;
            throw err;
          });
        });
  }

  get userRoles(): string[] {
    const userToken = this.auth.getUserToken();
    return userToken.roles;
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  isWorkingRelease(): boolean {
    return this.workingRelease;
  }

  markAsDeleteNode(): void {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Delete core component?';
    dialogConfig.data.content = ['Are you sure you want to delete this core component?'];
    dialogConfig.data.action = 'Delete anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }
        this.isUpdating = true;
        this.service.updateState(this.type, this.manifestId, 'Deleted').subscribe({
          next: () => {
            this.snackBar.open('Deleted', '', {duration: 3000});
            this.router.navigateByUrl('/data_type');
          },
          error: err => {
            this.isUpdating = false;
            throw err;
          },
        });
      });
  }

  purgeNode(): void {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Purge this core component?';
    dialogConfig.data.content = ['Are you sure you want to purge this core component?'];
    dialogConfig.data.action = 'Purge';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }
        this.isUpdating = true;
        this.service.purge(this.type, this.manifestId).subscribe({
          next: () => {
            this.snackBar.open('Purged', '', {duration: 3000});
            this.router.navigateByUrl('/data_type');
          },
          error: err => {
            this.isUpdating = false;
            throw err;
          },
        });
      });
  }

  restoreNode(): void {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore this core component?';
    dialogConfig.data.content = ['Are you sure you want to restore this core component?'];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }
        this.isUpdating = true;
        this.service.updateState(this.rootNode.type, this.rootNode.manifestId, 'WIP').subscribe({
          next: () => {
            this.snackBar.open('Restored', '', {duration: 3000});

            this.service.getDtDetails(this.manifestId).subscribe({
              next: dtDetails => this.afterStateChanged(dtDetails.state, dtDetails.access),
              error: err => console.error(err),
              complete: () => (this.isUpdating = false),
            });
          },
          error: err => {
            this.isUpdating = false;
            throw err;
          },
        });
      });
  }

  openCoreComponent(node: CcFlatNode) {
    window.open('/core_component/' + node.type.toLowerCase() + '/' + node.manifestId, '_blank');
  }

  openHistory(node: CcFlatNode) {
    window.open('/log/core-component/' + node.guid + '?type=' + node.type + '&manifestId=' + node.manifestId, '_blank');
  }

  contains(node: CcFlatNode, tag: Tag): boolean {
    if (!node || !node.tagList) {
      return false;
    }
    return node.tagList.filter(e => e.tagId === tag.tagId).length > 0;
  }

  toggleTag(node: CcFlatNode, tag: Tag) {
    if (!node || !tag) {
      return;
    }

    let call;
    if (!this.contains(node, tag)) {
      call = this.tagService.appendTag(node.type, node.manifestId, tag.tagId);
    } else {
      call = this.tagService.removeTag(node.type, node.manifestId, tag.tagId);
    }

    call.subscribe(_ => {
      if (this.contains(node, tag)) {
        node.tagList.splice(node.tagList.map(e => e.tagId).indexOf(tag.tagId), 1);
      } else {
        if (!node.tagList) {
          node.tagList = [];
        }
        const shortTag = new ShortTag();
        shortTag.tagId = tag.tagId;
        shortTag.name = tag.name;
        shortTag.textColor = tag.textColor;
        shortTag.backgroundColor = tag.backgroundColor;
        node.tagList.push(shortTag);
      }
    });
  }

  openEditTags() {
    const dialogRef = this.dialog.open(EditTagsDialogComponent, {
      autoFocus: false
    });
    dialogRef.afterClosed().subscribe(_ => {
      this.tagService.getTags().subscribe(tags => {
        this.tags = tags;
      });
    });
  }

  visibleFindUsages(node: CcFlatNode): boolean {
    if (!node) {
      return false;
    }
    return node.type.toUpperCase() === 'DT';
  }

  findUsages(node: CcFlatNode) {
    const dialogRef = this.dialog.open(FindUsagesDialogComponent, {
      data: {
        type: node.type,
        manifestId: node.manifestId
      },
      width: '600px',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(_ => {});
  }

  openComments(type: string, node?: CcFlatNode) {
    if (!node) {
      node = this.selectedNode;
    }
    this.commentControl.toggleCommentSlide(type, node.detail);
  }

  scrollToNode(node: CcFlatNode, delay?: number) {
    const index = this.searcher.getNodeIndex(node);
    this.scrollTree(index, delay);
    this.cursorNode = node;
  }

  keyNavigation(node: CcFlatNode, $event: KeyboardEvent) {
    if ($event.key === 'ArrowDown') {
      this.cursorNode = this.searcher.next(this.cursorNode);
    } else if ($event.key === 'ArrowUp') {
      this.cursorNode = this.searcher.prev(this.cursorNode);
    } else if ($event.key === 'ArrowLeft' || $event.key === 'ArrowRight') {
      this.dataSource.toggle(this.cursorNode);
    } else if ($event.key === 'o' || $event.key === 'O') {
      this.menuTriggerList.toArray().filter(e => !!e.menuData)
          .filter(e => e.menuData.menuId === 'contextMenu' && e.menuData.hashPath === node.hashPath)
          .forEach(trigger => {
            this.contextMenuItem = node;
            if (!trigger.menuOpen) {
              trigger.openMenu();
            }
          });
    } else if ($event.key === 'c' || $event.key === 'C') {
      this.menuTriggerList.toArray().filter(e => !!e.menuData)
          .filter(e => e.menuData.menuId === 'contextMenu' && e.menuData.hashPath === node.hashPath)
          .forEach(trigger => {
            this.contextMenuItem = node;
            this.openComments(node.type, node);
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

  cancelRevision(): void {
    const isDeveloper = this.userRoles.includes('developer');
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = (isDeveloper) ? 'Cancel this revision?' : 'Cancel this amendment?';
    dialogConfig.data.content = [(isDeveloper) ? 'Are you sure you want to cancel this revision?' : 'Are you sure you want to cancel this amendment?'];
    dialogConfig.data.action = 'Okay';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }

        this.isUpdating = true;
        this.service.cancelRevision(this.rootNode.type, this.rootNode.manifestId)
          .subscribe(resp => {
            this.reload('Canceled');
          }, err => {
            this.isUpdating = false;
          });
      });
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

  isEmpty(str: string): boolean {
    return str === undefined || str === null || str.trim() === '';
  }

  randSixDigitId(): string {
    return (Math.random() + 1).toString(36).substring(2, 8).toUpperCase();
  }

  toggle(node: CcFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.dataSource.toggle(node);
  }

  hasTokenPrimitive(detail: CcDtNodeInfo | CcDtScNodeInfo) {
    if (detail instanceof CcDtNodeInfo) {
      return (detail as CcDtNodeInfo).dtAwdPriList.filter(e => !!e.xbt && e.xbt.name === 'token').length > 0;
    } else {
      return (detail as CcDtScNodeInfo).dtScAwdPriList.filter(e => !!e.xbt && e.xbt.name === 'token').length > 0;
    }
  }

  addValueDomain(detail: CcNodeInfo) {
    if (detail instanceof CcDtNodeInfo) {
      const dtAwdPriDetails = new DtAwdPriDetails();
      dtAwdPriDetails.codeList = new CodeListSummary();
      dtAwdPriDetails.isDefault = false;
      (detail as CcDtNodeInfo).dtAwdPriList.push(dtAwdPriDetails);
      this.onChangeValueDomain(detail);
      this.restrictionDataSource.data = (detail as CcDtNodeInfo).valueDomains;
    } else {
      const dtScAwdPriDetails = new DtScAwdPriDetails();
      dtScAwdPriDetails.codeList = new CodeListSummary();
      dtScAwdPriDetails.isDefault = false;
      (detail as CcDtScNodeInfo).dtScAwdPriList.push(dtScAwdPriDetails);
      this.onChangeValueDomain(detail);
      this.restrictionDataSource.data = (detail as CcDtScNodeInfo).valueDomains;
    }
  }

  deleteValueDomain(detail: CcNodeInfo) {
    if (!this.selectedValueDomain) {
      return;
    }

    if (detail instanceof CcDtNodeInfo) {
      const selectedDtAwdPri = this.selectedValueDomain.dtAwdPri;
      const selectedValueDomainName = (!!selectedDtAwdPri.xbt) ? selectedDtAwdPri.xbt.name :
          ((!!selectedDtAwdPri.codeList) ? selectedDtAwdPri.codeList.name :
              selectedDtAwdPri.agencyIdList.name);

      if (selectedValueDomainName === (detail as CcDtNodeInfo).defaultValueDomain.name) {
        this.snackBar.open('\'Default Value Domain\' cannot be discarded.', '', {duration: 3000});
        return;
      }

      const newDtAwdPriList = [];
      for (const dtAwdPri of (detail as CcDtNodeInfo).dtAwdPriList) {
        const selected = this.selectedValueDomain.dtAwdPri;

        if (selected.xbt) {
          if (!dtAwdPri.xbt || dtAwdPri.xbt.xbtManifestId !== selected.xbt.xbtManifestId) {
            newDtAwdPriList.push(dtAwdPri);
          }
        } else if (selected.codeList) {
          if (!dtAwdPri.codeList || dtAwdPri.codeList.codeListManifestId !== selected.codeList.codeListManifestId) {
            newDtAwdPriList.push(dtAwdPri);
          }
        } else if (selected.agencyIdList) {
          if (!dtAwdPri.agencyIdList || dtAwdPri.agencyIdList.agencyIdListManifestId !== selected.agencyIdList.agencyIdListManifestId) {
            newDtAwdPriList.push(dtAwdPri);
          }
        } else {
          // If none of the value domains are selected, include all
          newDtAwdPriList.push(dtAwdPri);
        }
      }

      if ((detail as CcDtNodeInfo).defaultValueDomain.name === selectedValueDomainName) {
        (detail as CcDtNodeInfo).defaultValueDomain = 'token';
      }

      (detail as CcDtNodeInfo).dtAwdPriList = newDtAwdPriList;
      this.onChangeValueDomain(detail);
      this.restrictionDataSource.data = (detail as CcDtNodeInfo).valueDomains;
    } else {
      const selectedDtScAwdPri = this.selectedValueDomain.dtScAwdPri;
      const selectedValueDomainName = (!!selectedDtScAwdPri.xbt) ? selectedDtScAwdPri.xbt.name :
          ((!!selectedDtScAwdPri.codeList) ? selectedDtScAwdPri.codeList.name :
              selectedDtScAwdPri.agencyIdList.name);

      if (selectedValueDomainName === (detail as CcDtScNodeInfo).defaultValueDomain.name) {
        this.snackBar.open('\'Default Value Domain\' cannot be discarded.', '', {duration: 3000});
        return;
      }

      const newDtScAwdPriList = [];
      for (const dtScAwdPri of (detail as CcDtScNodeInfo).dtScAwdPriList) {
        const selected = this.selectedValueDomain.dtScAwdPri;

        if (selected.xbt) {
          if (!dtScAwdPri.xbt || dtScAwdPri.xbt.xbtManifestId !== selected.xbt.xbtManifestId) {
            newDtScAwdPriList.push(dtScAwdPri);
          }
        } else if (selected.codeList) {
          if (!dtScAwdPri.codeList || dtScAwdPri.codeList.codeListManifestId !== selected.codeList.codeListManifestId) {
            newDtScAwdPriList.push(dtScAwdPri);
          }
        } else if (selected.agencyIdList) {
          if (!dtScAwdPri.agencyIdList || dtScAwdPri.agencyIdList.agencyIdListManifestId !== selected.agencyIdList.agencyIdListManifestId) {
            newDtScAwdPriList.push(dtScAwdPri);
          }
        } else {
          // If none of the value domains are selected, include all
          newDtScAwdPriList.push(dtScAwdPri);
        }
      }

      if ((detail as CcDtScNodeInfo).defaultValueDomain.name === selectedValueDomainName) {
        (detail as CcDtScNodeInfo).defaultValueDomain = 'token';
      }

      (detail as CcDtScNodeInfo).dtScAwdPriList = newDtScAwdPriList;
      this.onChangeValueDomain(detail);
      this.restrictionDataSource.data = (detail as CcDtScNodeInfo).valueDomains;
    }

    this.selectedValueDomain = null;
    this.onChangeValueDomain(detail);
  }

  filteredCodeLists(element: any): CodeListSummary[] {
    let selfCodeListManifestId;
    if (!!element.dtAwdPri) {
      selfCodeListManifestId = element.dtAwdPri.codeList.codeListManifestId;
    } else if (!!element.dtScAwdPri) {
      selfCodeListManifestId = element.dtScAwdPri.codeList.codeListManifestId;
    }

    const selectedCodeLists = this.restrictionDataSource.data
        .map(e => {
          if (!!e.dtAwdPri) {
            return e.dtAwdPri.codeList;
          }
          if (!!e.dtScAwdPri) {
            return e.dtScAwdPri.codeList;
          }
          return undefined;
        })
        .filter(e => !!e).map(e => e.codeListManifestId);

    return this.codeLists.filter(e => !selectedCodeLists.includes(e.codeListManifestId) || e.codeListManifestId === selfCodeListManifestId);
  }

  filteredAgencyIdLists(element: any): AgencyIdListSummary[] {
    let selfAgencyIdListManifestId;
    if (!!element.dtAwdPri && !!element.dtAwdPri.agencyIdList) {
      selfAgencyIdListManifestId = element.dtAwdPri.agencyIdList.agencyIdListManifestId;
    } else if (!!element.dtScAwdPri && !!element.dtScAwdPri.agencyIdList) {
      selfAgencyIdListManifestId = element.dtScAwdPri.agencyIdList.agencyIdListManifestId;
    }

    const selectedAgencyIdLists = this.restrictionDataSource.data
        .map(e => {
          if (!!e.dtAwdPri) {
            return e.dtAwdPri.agencyIdList;
          }
          if (!!e.dtScAwdPri) {
            return e.dtScAwdPri.agencyIdList;
          }
          return undefined;
        })
        .filter(e => !!e).map(e => e.agencyIdListManifestId);

    return this.agencyIdLists.filter(e => !selectedAgencyIdLists.includes(e.agencyIdListManifestId) || e.agencyIdListManifestId === selfAgencyIdListManifestId);
  }

  compareCodeList(a: CodeListSummary, b: CodeListSummary): boolean {
    return !!a && !!b && a.codeListManifestId === b.codeListManifestId;
  }

  compareAgencyIdList(a: AgencyIdListSummary, b: AgencyIdListSummary): boolean {
    return !!a && !!b && a.agencyIdListManifestId === b.agencyIdListManifestId;
  }

  onChangeValueDomain(detail: CcNodeInfo) {
    if (detail instanceof CcDtNodeInfo) {
      (detail as CcDtNodeInfo).updateValueDomainGroup();
      detail._node.fireChangeEvent('ValueDomain', (detail as CcDtNodeInfo).dtAwdPriList);
    } else {
      (detail as CcDtScNodeInfo).updateValueDomainGroup();
      detail._node.fireChangeEvent('ValueDomain', (detail as CcDtScNodeInfo).dtScAwdPriList);
    }
  }

  loadDefaultPrimitiveValues(detail: CcDtScNodeInfo) {
    if (!detail.representationTerm) {
      this.restrictionDataSource.data = detail.valueDomains;
      return;
    }

    // Test Assertion #38.6.1.b
    // When the Representation Term is changed the Value Constraint should be reset.
    detail.fixedOrDefault = 'none';
    detail.fixedValue = undefined;
    detail.defaultValue = undefined;

    this.service.getPrimitiveListByRepresentationTerm(detail.representationTerm, detail.manifestId)
      .subscribe(resp => {
        detail.dtScAwdPriList = resp;
        this.onChangeValueDomain(detail);
        this.restrictionDataSource.data = detail.valueDomains;
      });
  }

  addQualifier(event: MatChipInputEvent) {
    const value = (event.value || '').trim();

    if (value) {
      if (this.asBdtDetail().qualifier) {
        this.asBdtDetail().qualifier += '_ ' + value;
      } else {
        this.asBdtDetail().qualifier = value;
      }
    }
    event.input.value = '';
  }

  removeQualifier(qualifier: string) {
    const list: string[] = JSON.parse(JSON.stringify(this.asBdtDetail().qualifierList));
    list.splice(list.indexOf(qualifier), 1);
    if (list.length > 0) {
      this.asBdtDetail().qualifier = list.join('_ ');
    } else {
      this.asBdtDetail().qualifier = '';
    }
  }
}
