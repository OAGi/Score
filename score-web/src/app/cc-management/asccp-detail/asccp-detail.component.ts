import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {Component, HostListener, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {MatSidenav} from '@angular/material/sidenav';
import {finalize, switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {ReleaseService} from '../../release-management/domain/release.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AsccpFlatNode, CcFlatNode, CcFlatNodeDatabase, CcFlatNodeDataSource, CcFlatNodeDataSourceSearcher} from '../domain/cc-flat-tree';
import {CcNodeService} from '../domain/core-component-node.service';
import {
  AsccpDetails,
  CcAccNodeInfo,
  CcAsccpNodeInfo,
  CcBccpNodeInfo,
  CcDtScNodeInfo,
  CcNodeInfo,
  Comment,
  EntityType,
  EntityTypes,
  OagisComponentType,
  OagisComponentTypes
} from '../domain/core-component-node';
import {CreateAsccpDialogComponent} from '../cc-list/create-asccp-dialog/create-asccp-dialog.component';
import {AuthService} from '../../authentication/auth.service';
import {CommentControl} from '../domain/comment-component';
import {forkJoin, ReplaySubject} from 'rxjs';
import {Location} from '@angular/common';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {SearchOptionsService} from '../search-options-dialog/domain/search-options-service';
import {SearchOptionsDialogComponent} from '../search-options-dialog/search-options-dialog.component';
import {FindUsagesDialogComponent} from '../find-usages-dialog/find-usages-dialog.component';
import {Clipboard} from '@angular/cdk/clipboard';
import {initFilter, loadBooleanProperty, saveBooleanProperty, trim} from '../../common/utility';
import {RxStompService} from '../../common/score-rx-stomp';
import {Message} from '@stomp/stompjs';
import {MatMenuTrigger} from '@angular/material/menu';
import {ShortTag, Tag} from '../../tag-management/domain/tag';
import {TagService} from '../../tag-management/domain/tag.service';
import {EditTagsDialogComponent} from '../../tag-management/edit-tags-dialog/edit-tags-dialog.component';
import {FormControl} from '@angular/forms';
import {PreferencesInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';

@Component({
  selector: 'score-asccp-detail',
  templateUrl: './asccp-detail.component.html',
  styleUrls: ['./asccp-detail.component.css']
})
export class AsccpDetailComponent implements OnInit {

  protected readonly trim = trim;

  title: string;
  _innerHeight: number = window.innerHeight;
  paddingPixel = 12;
  manifestId: number;
  type = 'ASCCP';
  isUpdating: boolean;
  availableModels: string[] = [];
  selectedModel: string;
  isGenerating: boolean = false;
  originalText: string = undefined;
  componentTypes: OagisComponentType[] = OagisComponentTypes;
  entityTypes: EntityType[] = EntityTypes;

  rootNode: AsccpFlatNode;
  dataSource: CcFlatNodeDataSource<CcFlatNode>;
  searcher: CcFlatNodeDataSourceSearcher<CcFlatNode>;

  prevAsccpDetails: AsccpDetails;
  selectedNode: CcFlatNode;
  cursorNode: CcFlatNode;

  workingRelease = false;
  namespaces: NamespaceSummary[];
  tags: Tag[] = [];
  commentControl: CommentControl;

  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<NamespaceSummary[]> = new ReplaySubject<NamespaceSummary[]>(1);

  initialExpandDepth = 10;

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

  get hideCardinality(): boolean {
    return this.dataSource.hideCardinality;
  }

  set hideCardinality(hideCardinality: boolean) {
    this.dataSource.hideCardinality = hideCardinality;
    saveBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, hideCardinality);
  }

  constructor(private service: CcNodeService,
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
          this.service.getAsccpDetails(this.manifestId),
          this.tagService.getTags(),
          this.preferencesService.load(this.auth.getUserToken()),
          this.service.availableModels()
        ]);
      })).subscribe(([ccGraph, asccpDetails, tags, preferencesInfo, models]) => {

      this.namespaceService.getNamespaceSummaries(asccpDetails.library.libraryId).subscribe(namespaces => {
        this.namespaces = namespaces;
        initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList,
          this.getSelectableNamespaces(), (e) => e.uri);
      });

      if (asccpDetails.log.revisionNum > 1) {
        this.service.getPrevAsccpDetails(this.manifestId)
            .subscribe(prevAsccpDetails => {
              this.prevAsccpDetails = prevAsccpDetails;
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
      this.availableModels = models;
      this.selectedModel = (!!this.availableModels && this.availableModels.length > 0) ? this.availableModels[0] : undefined;
      this.isGenerating = false;

      // subscribe an event
      this.stompService.watch('/topic/asccp/' + this.manifestId).subscribe((message: Message) => {
        const data = JSON.parse(message.body);
        if (data.properties.actor !== this.currentUser) {
          let noti;
          if (data.action === 'UpdateDetail') {
            noti = 'Asccp updated by ' + data.properties.actor;
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

      const database = new CcFlatNodeDatabase<CcFlatNode>(ccGraph, 'ASCCP', this.manifestId);
      this.dataSource = new CcFlatNodeDataSource<CcFlatNode>(database, this.service);
      this.searcher = new CcFlatNodeDataSourceSearcher<CcFlatNode>(this.dataSource, database);
      this.dataSource.init();
      this.dataSource.hideCardinality = loadBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, false);

      this.workingRelease = asccpDetails.release.workingRelease;

      this.rootNode = this.dataSource.data[0] as AsccpFlatNode;
      this.rootNode.access = asccpDetails.access;
      this.rootNode.state = asccpDetails.state;
      this.rootNode.reset();

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

    this.isUpdating = true;
    forkJoin([
      this.service.getAsccpDetails(this.manifestId),
      this.service.getGraphNode(this.rootNode.type, this.manifestId)
    ]).subscribe(([asccpDetails, ccGraph]) => {
      const database = new CcFlatNodeDatabase<CcFlatNode>(ccGraph, 'ASCCP', this.manifestId);
      this.dataSource = new CcFlatNodeDataSource<CcFlatNode>(database, this.service);
      this.searcher = new CcFlatNodeDataSourceSearcher<CcFlatNode>(this.dataSource, database);
      this.dataSource.init();

      this.workingRelease = asccpDetails.release.workingRelease;

      this.rootNode = this.dataSource.data[0] as AsccpFlatNode;
      this.rootNode.access = asccpDetails.access;
      this.rootNode.state = asccpDetails.state;
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

      // Reset
      this.originalText = undefined;

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
    return (this.rootNode) ? this.rootNode.state : '';
  }

  get access(): string {
    return (this.rootNode) ? this.rootNode.access : '';
  }

  hasRevision() {
    return !!this.prevAsccpDetails;
  }

  isEditable() {
    if (!this.isAsccpDetail() || this.selectedNode.manifestId !== this.manifestId) {
      return false;
    }
    return this.state === 'WIP' && this.access === 'CanEdit';
  }

  isExtension() {
    return this.rootNode && this.rootNode.asccpNode.type === 'Extension';
  }

  onClick(node: CcFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.commentControl.closeCommentSlide();
    this.dataSource.loadDetail(node, (detail: CcNodeInfo) => {
      this.selectedNode = node;
      this.cursorNode = node;
    });
  }

  toggle(node: CcFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.dataSource.toggle(node);
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
    this.asAsccpDetail(this.rootNode).asccp.definition = text;
    this.snackBar.open('Copied to definition', '', {
      duration: 3000
    });
  }

  handleMouseDownForGenerate($event: MouseEvent, matMenuTrigger: MatMenuTrigger): void {
    if ($event.button === 0) { // left-click
      if (!this.isGenerating && !!this.selectedModel) {
        this.generateDefinition(this.asAsccpDetail(), this.selectedModel);
      }
    } else if ($event.button === 2) { // right-click
      $event.preventDefault();
      setTimeout(() => {
        matMenuTrigger.openMenu();
      }, 0);
    }
  }

  generateDefinition(asccpNodeDetails: CcAsccpNodeInfo, model: string, $event?: MouseEvent) {
    this.originalText = undefined;
    this.isGenerating = true;
    this.selectedModel = model;
    this.service.generateDefinition('ASCCP', asccpNodeDetails.asccp.manifestId, model,
        asccpNodeDetails.asccp.definition).subscribe(gen => {
      let generatedText = gen.generation || ''; // Ensure it's a valid string
      const originalText = asccpNodeDetails.asccp.definition;
      asccpNodeDetails.asccp.definition = ''; // Start with an empty string
      let i = 0;

      if (generatedText.length === 0) {
        this.isGenerating = false; // No need for animation if nothing to generate
        return;
      }

      const interval = setInterval(() => {
        if (i < generatedText.length) {
          if (!asccpNodeDetails.asccp.definition) {
            asccpNodeDetails.asccp.definition = generatedText[i];
          } else {
            asccpNodeDetails.asccp.definition += generatedText[i];
          }
          i++;
        } else {
          clearInterval(interval);
          this.isGenerating = false;
          this.originalText = (originalText !== undefined) ? originalText : '';
        }
      }, 10); // Very fast typing
    }, err => {
      this.isGenerating = false;
    });
  }

  revert(asccpNodeDetails: CcAsccpNodeInfo) {
    asccpNodeDetails.asccp.definition = this.originalText;
    this.originalText = undefined;
  }

  _updateDetails(details: CcFlatNode[]) {
    this.isUpdating = true;
    this.service.updateNodes(details)
      .pipe(finalize(() => {
          this.isUpdating = false;
        }))
      .subscribe(_ => {
        this.dataSource.resetChanged();
        if (this.selectedNode) {
          this.onClick(this.selectedNode);
        }
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
    let emptyPropertyTerm = false;
    for (const detail of details) {
      if (detail.type.toUpperCase() === 'ASCCP') {
        if (!this.asAsccpDetail(detail).asccp.definition || this.asAsccpDetail(detail).asccp.definition.length === 0) {
          emptyDefinition = true;
        }
        if (!this.asAsccpDetail(detail).asccp.namespaceId) {
          emptyNamespace = true;
        }
        if (!this.asAsccpDetail(detail).asccp.propertyTerm || this.asAsccpDetail(detail).asccp.propertyTerm.length === 0) {
          emptyPropertyTerm = true;
        }
      }
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
      data: {
      },
      width: '600px',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(_ => {});
  }

  isAccChangeable(): boolean {
    if (!this.hasRevision()) {
      return true;
    } else {
      return this.userRoles.includes('developer');
    }
  }

  changeAcc(node: CcFlatNode) {
    const dialogRef = this.dialog.open(CreateAsccpDialogComponent, {
      data: {
        libraryId: this.rootNode.libraryId,
        releaseId: this.rootNode.releaseId,
        action: 'update',
        state: node.state,
        // @ts-ignore
        excludes: [this.rootNode.accId]
      },
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    });
    dialogRef.afterClosed().subscribe(accManifest => {
      if (!accManifest) {
        return;
      }
      this.service.updateRoleOfAcc(this.rootNode.manifestId, accManifest.manifestId).subscribe(asccp => {
        this.reload('Updated');
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
      .subscribe(result => {
        if (!result) {
          return;
        }

        this.service.updateState(this.rootNode.type, this.rootNode.manifestId, state).subscribe({
          next: () => {
            this.snackBar.open('Updated', '', {duration: 3000});

            this.service.getAsccpDetails(this.manifestId).subscribe({
              next: asccpDetails => this.afterStateChanged(asccpDetails.state, asccpDetails.access),
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
        if (!this.asAsccpDetail(detail).asccp.namespaceId) {
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
    (root.detail as CcAsccpNodeInfo).asccp.state = state;
  }

  makeNewRevision() {
    const isDeveloper = this.userRoles.includes('developer');
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = (isDeveloper) ? 'Revise this ASCCP?' : 'Amend this ASCCP?';
    dialogConfig.data.content = [(isDeveloper) ? 'Are you sure you want to revise this ASCCP?' : 'Are you sure you want to amend this ASCCP?'];
    dialogConfig.data.action = (isDeveloper) ? 'Revise' : 'Amend';

    this.confirmDialogService.open(dialogConfig).afterClosed()
        .subscribe(result => {
          if (!result) {
            return;
          }

          this.isUpdating = true;
          this.service.makeNewRevision(this.rootNode.type, this.rootNode.manifestId).subscribe(_ => {
            forkJoin([
              this.service.getAsccpDetails(this.manifestId),
              this.service.getPrevAsccpDetails(this.manifestId),
            ]).subscribe(([asccpDetails, prevAsccpDetails]) => {
              this.manifestId = asccpDetails.asccpManifestId;
              this.afterStateChanged(asccpDetails.state, asccpDetails.access);
              this.prevAsccpDetails = prevAsccpDetails;
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
        this.service.updateState(this.rootNode.type, this.rootNode.manifestId, 'Deleted').subscribe({
          next: () => {
            this.snackBar.open('Deleted', '', {duration: 3000});
            this.router.navigateByUrl('/core_component');
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
            this.router.navigateByUrl('/core_component');
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

            this.service.getAsccpDetails(this.manifestId).subscribe({
              next: asccpDetails => this.afterStateChanged(asccpDetails.state, asccpDetails.access),
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
    return node.type.toUpperCase() === 'ACC' || node.type.toUpperCase() === 'ASCCP' || node.type.toUpperCase() === 'BCCP';
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
}
