import {CdkDragDrop} from '@angular/cdk/drag-drop';
import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {Component, HostListener, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {MatSidenav} from '@angular/material/sidenav';
import {finalize, switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {SimpleNamespace} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {ReleaseService} from '../../release-management/domain/release.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {
  AccFlatNode,
  AsccpFlatNode,
  BccpFlatNode,
  CcFlatNode,
  CcFlatNodeDatabase,
  CcFlatNodeDataSource,
  CcFlatNodeDataSourceSearcher
} from '../domain/cc-flat-tree';
import {CcNodeService} from '../domain/core-component-node.service';
import {
  Attribute,
  Base,
  CcAccNodeDetail,
  CcAsccpNodeDetail,
  CcBccpNodeDetail, CcBdtPriRestri,
  CcBdtScNodeDetail,
  CcId,
  CcNodeDetail,
  CcRevisionResponse,
  CcSeqUpdateRequest,
  Comment,
  Embedded,
  EntityType,
  EntityTypes,
  Extension,
  OAGIS10BODs,
  OAGIS10Nouns,
  OagisComponentType,
  OagisComponentTypes,
  Semantics,
  UserExtensionGroup
} from '../domain/core-component-node';
import {initFilter, loadBooleanProperty, saveBooleanProperty, UnboundedPipe} from '../../common/utility';
import {
  AppendAssociationDialogComponent
} from '../acc-detail/append-association-dialog/append-association-dialog.component';
import {AbstractControl, FormControl, ValidationErrors, Validators} from '@angular/forms';
import {AuthService} from '../../authentication/auth.service';
import {WorkingRelease} from '../../release-management/domain/release';
import {CommentControl} from '../domain/comment-component';
import {forkJoin, ReplaySubject} from 'rxjs';
import {Location} from '@angular/common';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {CcList} from '../cc-list/domain/cc-list';
import {SearchOptionsService} from '../search-options-dialog/domain/search-options-service';
import {SearchOptionsDialogComponent} from '../search-options-dialog/search-options-dialog.component';
import {FindUsagesDialogComponent} from '../find-usages-dialog/find-usages-dialog.component';
import {Clipboard} from '@angular/cdk/clipboard';
import {RxStompService} from '../../common/score-rx-stomp';
import {Message} from '@stomp/stompjs';
import {MatMenuTrigger} from '@angular/material/menu';
import {ShortTag, Tag} from '../../tag-management/domain/tag';
import {TagService} from '../../tag-management/domain/tag.service';
import {EditTagsDialogComponent} from '../../tag-management/edit-tags-dialog/edit-tags-dialog.component';
import {PreferencesInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';

@Component({
  selector: 'score-extension-detail',
  templateUrl: './extension-detail.component.html',
  styleUrls: ['./extension-detail.component.css']
})
export class ExtensionDetailComponent implements OnInit {
  title: string;
  _innerHeight: number = window.innerHeight;
  paddingPixel = 12;
  manifestId: number;
  type = 'ACC';
  isUpdating: boolean;
  componentTypes: OagisComponentType[] = OagisComponentTypes;
  entityTypes: EntityType[] = EntityTypes;
  private entityTypeChanged: Map<number, boolean> = new Map();

  rootNode: AccFlatNode;
  dataSource: CcFlatNodeDataSource<CcFlatNode>;
  searcher: CcFlatNodeDataSourceSearcher<CcFlatNode>;

  lastRevision: CcRevisionResponse;
  selectedNode: CcFlatNode;
  cursorNode: CcFlatNode;

  /* Begin cardinality management */
  ccCardinalityMin: FormControl;
  ccCardinalityMax: FormControl;
  /* End cardinality management */

  workingRelease = WorkingRelease;
  namespaces: SimpleNamespace[];
  tags: Tag[] = [];
  commentControl: CommentControl;

  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<SimpleNamespace[]> = new ReplaySubject<SimpleNamespace[]>(1);

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
          this.service.getLastPublishedRevision(this.type, this.manifestId),
          this.service.getAccNode(this.manifestId),
          this.namespaceService.getSimpleNamespaces(),
          this.tagService.getTags(),
          this.preferencesService.load(this.auth.getUserToken())
        ]);
      })).subscribe(([ccGraph, revisionResponse, rootNode, namespaces, tags, preferencesInfo]) => {
      this.lastRevision = revisionResponse;
      this.namespaces = namespaces;
      initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList,
        this.getSelectableNamespaces(), (e) => e.uri);
      this.tags = tags;
      this.preferencesInfo = preferencesInfo;

      // subscribe an event
      this.stompService.watch('/topic/acc/' + this.manifestId).subscribe((message: Message) => {
        const data = JSON.parse(message.body);
        if (data.properties.actor !== this.currentUser) {
          let noti;
          if (data.action === 'UpdateDetail') {
            noti = 'Acc updated by ' + data.properties.actor;
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

      const database = new CcFlatNodeDatabase<CcFlatNode>(ccGraph, 'ACC', this.manifestId);
      this.dataSource = new CcFlatNodeDataSource<CcFlatNode>(database, this.service);
      this.searcher = new CcFlatNodeDataSourceSearcher<CcFlatNode>(this.dataSource, database);
      this.dataSource.init();
      this.dataSource.hideCardinality = loadBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, false);

      this.rootNode = this.dataSource.data[0] as AccFlatNode;
      this.rootNode.access = rootNode.access;
      this.rootNode.state = rootNode.state;
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
    comment.loginId = evt.properties.actor;
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

  getSelectableNamespaces(namespaceId?: number): SimpleNamespace[] {
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

    const delimiter = this.preferencesInfo.treeSettingsInfo.delimiter;
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
      this.service.getAccNode(this.manifestId),
      this.service.getGraphNode(this.rootNode.type, this.manifestId)
    ]).subscribe(([rootNode, ccGraph]) => {
      const database = new CcFlatNodeDatabase<CcFlatNode>(ccGraph, 'ACC', this.manifestId);
      this.dataSource = new CcFlatNodeDataSource<CcFlatNode>(database, this.service);
      this.searcher = new CcFlatNodeDataSourceSearcher<CcFlatNode>(this.dataSource, database);
      this.dataSource.init();

      this.rootNode = this.dataSource.data[0] as AccFlatNode;
      this.rootNode.access = rootNode.access;
      this.rootNode.state = rootNode.state;
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
    return (this.rootNode) ? this.rootNode.state : '';
  }

  get access(): string {
    return (this.rootNode) ? this.rootNode.access : '';
  }

  hasRevision() {
    return this.lastRevision && this.lastRevision.ccId !== null;
  }

  isEditable() {
    return this.state === 'WIP' && this.access === 'CanEdit';
  }

  isExtension() {
    return this.rootNode && (this.rootNode.detail as CcAccNodeDetail).type === 'Extension';
  }

  onClick(node: CcFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.commentControl.closeCommentSlide();
    this.dataSource.loadDetail(node, (detail: CcNodeDetail) => {
      this.selectedNode = node;
      this.cursorNode = node;
      this.resetCardinalities(node);
    });
  }

  toggle(node: CcFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.dataSource.toggle(node);
  }

  hasRevisionAssociation() {
    const key = this.getKey(this.selectedNode);
    if (key.length > 0 && this.lastRevision.associations && this.lastRevision.associations[key]) {
      return this.lastRevision.associations[key] !== undefined;
    }
    return false;
  }

  isDeprecateAble() {
    const key = this.getKey(this.selectedNode);
    if (key.length > 0) {
      if (this.lastRevision.associations && this.lastRevision.associations[key]) {
        return !this.lastRevision.associations[key].deprecated;
      }
    }
    return false;
  }

  getKey(node: CcFlatNode) {
    return node.type + '-' + node.manifestId;
  }

  isDraggable(node: CcFlatNode) {
    if (!node) {
      return false;
    }
    if (node.level !== 1) {
      return false;
    }
    const type = node.type.toUpperCase();
    if (type === 'BCCP') {
      return (node as BccpFlatNode).bccNode.entityType !== 'Attribute';
    } else if (type === 'ASCCP') {
      return true;
    }
    return false;
  }

  /* For type casting of detail property */
  isAccDetail(node?: CcFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.type.toUpperCase() === 'ACC');
  }

  asAccDetail(node?: CcFlatNode): CcAccNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as CcAccNodeDetail;
  }

  isAsccpDetail(node?: CcFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.type.toUpperCase() === 'ASCCP');
  }

  asAsccpDetail(node?: CcFlatNode): CcAsccpNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as CcAsccpNodeDetail;
  }

  isBccpDetail(node?: CcFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.type.toUpperCase() === 'BCCP');
  }

  asBccpDetail(node?: CcFlatNode): CcBccpNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as CcBccpNodeDetail;
  }

  isBdtScDetail(node?: CcFlatNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.type.toUpperCase() === 'DT_SC');
  }

  asBdtScDetail(node?: CcFlatNode): CcBdtScNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as CcBdtScNodeDetail;
  }

  get isChanged() {
    return this.dataSource.getChanged().length > 0;
  }

  get isValid(): boolean {
    if (!!this.ccCardinalityMin && !this.ccCardinalityMin.disabled && !this.ccCardinalityMin.valid) {
      return false;
    }
    if (!!this.ccCardinalityMax && !this.ccCardinalityMax.disabled && !this.ccCardinalityMax.valid) {
      return false;
    }
    return true;
  }

  openInNewTab(url: string) {
    window.open(url, '_blank');
  }

  copyToDefinition(text: string) {
    this.asAccDetail(this.rootNode).definition = text;
    this.snackBar.open('Copied to definition', '', {
      duration: 3000
    });
  }

  _updateDetails(details: CcFlatNode[]) {
    this.isUpdating = true;
    this.service.updateDetails(this.manifestId, details)
      .pipe(finalize(() => {
        this.isUpdating = false;
      }))
      .subscribe(_ => {
        if (this.entityTypeChanged.size > 0) {
          this.reload('Updated');
          this.entityTypeChanged.clear();
        } else {
          this.dataSource.resetChanged();
          if (this.selectedNode) {
            this.onClick(this.selectedNode);
          }
          this.snackBar.open('Updated', '', {
            duration: 3000,
          });
        }
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
    return this.state !== 'WIP' || this.access !== 'CanEdit' || !this.isChanged || this.isUpdating || !this.isValid;
  }

  updateDetails() {
    if (this.updateDisabled) {
      return;
    }

    const details = this.dataSource.getChanged();
    let emptyDefinition = false;
    let emptyObjectClassTerm = false;
    let emptyNamespace = false;
    for (const detail of details) {
      if (detail.type.toUpperCase() === 'ACC') {
        if (!this.asAccDetail(detail).objectClassTerm || this.asAccDetail(detail).objectClassTerm.length === 0) {
          emptyObjectClassTerm = true;
        }
        if (!this.asAccDetail(detail).definition || this.asAccDetail(detail).definition.length === 0) {
          emptyDefinition = true;
        }
        if (!this.asAccDetail(detail).namespaceId) {
          emptyNamespace = true;
        }
      } else if (detail.type.toUpperCase() === 'ASCCP') {
        if (!this.asAsccpDetail(detail).ascc.definition || this.asAsccpDetail(detail).ascc.definition.length === 0) {
          emptyDefinition = true;
        }
      } else if (detail.type.toUpperCase() === 'BCCP') {
        if (!this.asBccpDetail(detail).bcc.definition || this.asBccpDetail(detail).bcc.definition.length === 0) {
          emptyDefinition = true;
        }
      }
    }

    if (emptyObjectClassTerm) {
      this.snackBar.open('Object Class Term is required', '', {
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

  pos(node: CcFlatNode): number {
    if (!node.parent) {
      return 0;
    }
    return node.parent.children.indexOf(node);
  }

  appendAssociation(node: CcFlatNode, pos: number) {
    if (pos === 0 && this.hasRevision()) {
      pos = Object.getOwnPropertyNames(this.lastRevision.associations).length;
    }
    const dialogRef = this.dialog.open(AppendAssociationDialogComponent, {
      data: {
        releaseId: this.rootNode.releaseId,
        manifestId: this.rootNode.manifestId,
        componentType: this.asAccDetail(this.rootNode).oagisComponentType,
        state: this.rootNode.state,
        action: (pos === -1) ? 'Append' : 'Insert',
        isGlobal: this.rootNode.den === 'All User Extension Group. Details'
      },
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe((association: CcList | undefined) => {
      if (!association) {
        return;
      }

      this.isUpdating = true;
      this.service.appendAssociation(
        this.rootNode.releaseId,
        this.rootNode.manifestId,
        association.manifestId,
        association.type,
        false,
        pos).subscribe(_ => {
        this.reload((pos === -1) ? 'Appended' : 'Inserted');
      }, err => {
        this.isUpdating = false;
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
        this.service.updateState(this.rootNode.type, this.rootNode.manifestId, state)
          .subscribe(resp => {
            this.afterStateChanged(resp.state, resp.access);
            this.snackBar.open('Updated', '', {
              duration: 3000,
            });
          }, err => {
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
        if (!this.asAccDetail(detail).namespaceId) {
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
    (root.detail as CcAccNodeDetail).state = state;
    root.children.forEach(child => {
      if ((child as CcFlatNode).type === 'ASCCP' && (child as AsccpFlatNode).detail) {
        ((child as AsccpFlatNode).detail as CcAsccpNodeDetail).ascc.state = state;
      } else if ((child as CcFlatNode).type === 'BCCP' && (child as BccpFlatNode).detail) {
        ((child as BccpFlatNode).detail as CcBccpNodeDetail).bcc.state = state;
      }
    });
  }

  makeNewRevision() {
    const isDeveloper = this.userRoles.includes('developer');
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = (isDeveloper) ? 'Revise this ACC?' : 'Amend this ACC?';
    dialogConfig.data.content = [(isDeveloper) ? 'Are you sure you want to revise this ACC?' : 'Are you sure you want to amend this ACC?'];
    dialogConfig.data.action = (isDeveloper) ? 'Revise' : 'Amend';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }

        this.isUpdating = true;
        this.service.makeNewRevision(this.rootNode.type, this.rootNode.manifestId).pipe(
          finalize(() => {
            this.isUpdating = false;
          })
        ).subscribe(resp => {
            this.manifestId = resp.manifestId;
            this.afterStateChanged(resp.state, resp.access);
            this.service.getLastPublishedRevision(this.type, this.manifestId).subscribe(revision => {
              this.lastRevision = revision;
              this.snackBar.open((isDeveloper) ? 'Revised' : 'Amended', '', {
                duration: 3000,
              });
            });
            this.reload();
          });
      });
  }

  discardAble(node: CcFlatNode): boolean {
    if (!node) {
      return false;
    }
    if (this.state !== 'WIP') {
      return false;
    }
    if (this.access !== 'CanEdit') {
      return false;
    }
    if (node.level !== 1) {
      return false;
    }
    if (node.type.toUpperCase() === 'ACC') {
      if (this.hasRevision() && this.lastRevision.hasBaseCc) {
        return false;
      }
    }
    return !(this.hasRevision() && this.lastRevision.associations[this.getKey(node)]);
  }

  get userRoles(): string[] {
    const userToken = this.auth.getUserToken();
    return userToken.roles;
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  onClickDiscard(node: CcFlatNode): void {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove association?';
    dialogConfig.data.content = ['Are you sure you want to remove this association?'];
    dialogConfig.data.action = 'Remove anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }
        const type = node.type.toUpperCase();
        switch (type) {
          case 'ASCCP':
            this.isUpdating = true;
            this.service.deleteNode('ascc', (node as AsccpFlatNode).asccManifestId).pipe(
              finalize(() => {
                this.isUpdating = false;
              })
            ).subscribe(_ => {
              this.reload('Removed');
            }, error => {
            });
            break;

          case 'BCCP':
            this.isUpdating = true;
            this.service.deleteNode('BCC', (node as BccpFlatNode).bccManifestId).pipe(
              finalize(() => {
                this.isUpdating = false;
              })
            ).subscribe(_ => {
              const index = this.dataSource.data[0].children.indexOf(node);
              this.dataSource.removeNodes(index);
              this.isUpdating = false;
            }, error => {
            });
            break;
        }
      });
  }

  componentTypeAble(componentType: number) {
    if (this.userRoles.includes('developer')) {
      if ([UserExtensionGroup.value, OAGIS10BODs.value, OAGIS10Nouns.value, Embedded.value, Extension.value].indexOf(componentType) > -1) {
        return false;
      }
    } else {
      if ([UserExtensionGroup.value, OAGIS10BODs.value, OAGIS10Nouns.value, Embedded.value, Extension.value].indexOf(componentType) > -1) {
        return false;
      }
    }
    return true;
  }

  abstractAble(detail: CcAccNodeDetail) {
    if (this.lastRevision && this.lastRevision.ccId) {
      if (detail.oagisComponentType === Base.value) {
        return false;
      } else {
        if (this.lastRevision.isAbstract) {
          return true;
        }
        return false;
      }
    } else {
      if (detail.oagisComponentType === Semantics.value) {
        return true;
      }
      return false;
    }
  }

  isChildOfUserExtensionGroup(node?: CcFlatNode) {
    const targetNode = (node) ? node : this.selectedNode;
    const typeStr = targetNode.type.toUpperCase();
    return (targetNode.level === 1) && (typeStr.startsWith('ASCCP') || typeStr.startsWith('BCCP'));
  }

  resetCardinalities(node?: CcFlatNode) {
    this._setCardinalityMinFormControl(node);
    this._setCardinalityMaxFormControl(node);
  }

  nonWhitespaceValidator(control: AbstractControl): ValidationErrors | null {
    if (!control || !control.value) {
      return null;
    }
    const isWhitespace = control.value.toString().trim().length === 0;
    const isValid = !isWhitespace;
    return isValid ? null : {whitespace: true};
  }

  _setCardinalityMinFormControl(node?: CcFlatNode) {
    if (!node) {
      node = this.selectedNode;
    }
    if (!node) {
      return;
    }

    let obj;
    let prevRevision = null;
    if (this.isAsccpDetail(node)) {
      const detail = node.detail as CcAsccpNodeDetail;
      obj = detail.ascc;
      if (this.lastRevision && this.lastRevision.associations) {
        prevRevision = this.lastRevision.associations[node.type + '-' + detail.asccp.manifestId];
      }
    } else if (this.isBccpDetail(node)) {
      const detail = node.detail as CcBccpNodeDetail;
      obj = detail.bcc;
      if (this.lastRevision && this.lastRevision.associations) {
        prevRevision = this.lastRevision.associations[node.type + '-' + detail.bccp.manifestId];
      }
    } else if (this.isBdtScDetail(node)) {
      obj = node.detail;
    } else {
      return false;
    }

    const validators = [];
    validators.push(Validators.required);
    validators.push(Validators.pattern('[0-9]+'));
    validators.push(this.nonWhitespaceValidator);
    if (this.isBccpDetail(node)) {
      if (this.asBccpDetail(node).bcc.entityType === 0) { // is_attribute
        validators.push((control: AbstractControl): ValidationErrors | null => {
          if (obj.cardinalityMin > 1) {
            return {min: 'Cardinality Min must be less than or equals to ' + 1};
          }
          return null;
        });
      }
    }

    let disabled = (this.state !== 'WIP' || this.access !== 'CanEdit' || !this.isChildOfUserExtensionGroup());
    if (prevRevision) {
      if (prevRevision.cardinalityMin === 0) {
        disabled = true;
      }
      validators.push((control: AbstractControl): ValidationErrors | null => {
        if (Number(control.value) < 0) {
          return {min: 'Cardinality Min must be bigger than or equals to ' + 0};
        }
        if (Number(control.value) > prevRevision.cardinalityMin) {
          return {max: 'Cardinality Min must be less than or equals to ' + prevRevision.cardinalityMin};
        }
        if (obj.cardinalityMax >= 0 && Number(control.value) > obj.cardinalityMax) {
          return {max: 'Cardinality Min must be less than or equals to ' + obj.cardinalityMax};
        }
        return null;
      });
    } else {
      validators.push((control: AbstractControl): ValidationErrors | null => {
        if (obj.cardinalityMax >= 0 && Number(control.value) > obj.cardinalityMax) {
          return {max: 'Cardinality Min must be less than or equals to ' + obj.cardinalityMax};
        }
        return null;
      });
    }

    this.ccCardinalityMin = new FormControl({
        value: obj.cardinalityMin,
        disabled
      }, validators
    );
    this.ccCardinalityMin.valueChanges.subscribe(value => {
      if (this.ccCardinalityMin.valid) {
        value = typeof value === 'number' ? value : Number.parseInt(value, 10);
        obj.cardinalityMin = Number(value);
        this.ccCardinalityMax.updateValueAndValidity({onlySelf: true, emitEvent: false});
      }
    });
  }

  _setCardinalityMaxFormControl(node?: CcFlatNode) {
    if (!node) {
      node = this.selectedNode;
    }
    if (!node) {
      return;
    }

    let obj;
    let prevRevision = null;
    if (this.isAsccpDetail(node)) {
      const detail = node.detail as CcAsccpNodeDetail;
      obj = detail.ascc;
      if (this.lastRevision && this.lastRevision.associations) {
        prevRevision = this.lastRevision.associations[node.type + '-' + detail.asccp.manifestId];
      }
    } else if (this.isBccpDetail(node)) {
      const detail = node.detail as CcBccpNodeDetail;
      obj = detail.bcc;
      if (this.lastRevision && this.lastRevision.associations) {
        prevRevision = this.lastRevision.associations[node.type + '-' + detail.bccp.manifestId];
      }
    } else if (this.isBdtScDetail(node)) {
      obj = node.detail;
    } else {
      return false;
    }

    const validators = [];
    validators.push(Validators.required);
    validators.push(Validators.pattern('[0-9]+|-1|unbounded'));
    validators.push(this.nonWhitespaceValidator);
    if (this.isBccpDetail(node)) {
      if (this.asBccpDetail(node).bcc.entityType === 0) { // is_attribute
        validators.push((control: AbstractControl): ValidationErrors | null => {
          if (Number(control.value) < 0) {
            return {max: 'Cardinality Max must be greater than or equals to ' + 0};
          }
          if (Number(control.value) > 1) {
            return {max: 'Cardinality Max must be less than or equals to ' + 1};
          }
          return null;
        });
      }
    }

    let disabled = (this.state !== 'WIP' || this.access !== 'CanEdit' || !this.isChildOfUserExtensionGroup());
    if (prevRevision) {
      if (prevRevision.cardinalityMax === -1) {
        disabled = true;
      }
      validators.push((control: AbstractControl): ValidationErrors | null => {
        let controlValue = control.value;
        controlValue = (controlValue === 'unbounded') ? -1 : (typeof controlValue === 'number' ? controlValue : Number.parseInt(controlValue, 10));

        if (controlValue === -1) {
          return null;
        }
        if (prevRevision.cardinalityMax === -1) {
          return {max: 'Cardinality Max cannot be changed'};
        }
        if (controlValue < prevRevision.cardinalityMax) {
          return {min: 'Cardinality Max must be greater than ' + prevRevision.cardinalityMax};
        }
        if (controlValue < obj.cardinalityMin) {
          return {min: 'Cardinality Max must be greater than ' + obj.cardinalityMin};
        }
        return null;
      });
    } else {
      validators.push((control: AbstractControl): ValidationErrors | null => {
        let controlValue = control.value;
        controlValue = (controlValue === 'unbounded') ? -1 : (typeof controlValue === 'number' ? controlValue : Number.parseInt(controlValue, 10));

        if (!controlValue || controlValue === -1) {
          return null;
        }
        if (controlValue < obj.cardinalityMin) {
          return {min: 'Cardinality Max must be greater than ' + obj.cardinalityMin};
        }
        return null;
      });
    }
    this.ccCardinalityMax = new FormControl({
        value: new UnboundedPipe().transform(obj.cardinalityMax),
        disabled
      }, validators
    );
    this.ccCardinalityMax.valueChanges.subscribe(value => {
      if (this.ccCardinalityMax.valid) {
        value = (value === 'unbounded') ? -1 : (typeof value === 'number' ? value : Number.parseInt(value, 10));
        obj.cardinalityMax = value;
        this.ccCardinalityMin.updateValueAndValidity({onlySelf: true, emitEvent: false});
      }
    });
  }

  onChangeEntityType(nodeDetail: CcBccpNodeDetail) {
    if (this.entityTypeChanged.get(nodeDetail.bcc.manifestId) === undefined) {
      this.entityTypeChanged.set(nodeDetail.bcc.manifestId, true);
    } else {
      this.entityTypeChanged.delete(nodeDetail.bcc.manifestId);
    }
    if (EntityTypes[nodeDetail.bcc.entityType].name === 'Element') {
      // Issue #1406
      if (!!nodeDetail.bcc.defaultValue || !!nodeDetail.bcc.fixedValue) {
        const dialogConfig = this.confirmDialogService.newConfig();
        dialogConfig.data.header = 'Change the entity type to \'Element\'?';
        dialogConfig.data.content = ['The default and the fixed value constraints will be cleared if changing the entity type to \'Element\'.',
          'Are you sure you want to change the entity type to \'Element\'?'];
        dialogConfig.data.action = 'Change';

        this.confirmDialogService.open(dialogConfig).afterClosed()
          .subscribe(result => {
            if (!!result) {
              nodeDetail.bcc.defaultValue = '';
              nodeDetail.bcc.fixedValue = '';
              nodeDetail.bcc.fixedOrDefault = 'none';
            } else {
              nodeDetail.bcc.entityType = Attribute.value;
            }
          });
      } else {
        nodeDetail.bcc.defaultValue = '';
        nodeDetail.bcc.fixedValue = '';
        nodeDetail.bcc.fixedOrDefault = 'none';
      }
    } else {
      // Issue #919
      if (nodeDetail.bcc.cardinalityMin < 0 || nodeDetail.bcc.cardinalityMin > 1) {
        nodeDetail.bcc.cardinalityMin = 0;
      }
      if (nodeDetail.bcc.cardinalityMax < 0 || nodeDetail.bcc.cardinalityMax > 1) {
        nodeDetail.bcc.cardinalityMax = 1;
      }
    }
  }

  isWorkingRelease(): boolean {
    if (this.rootNode) {
      return this.rootNode.releaseId === this.workingRelease.releaseId;
    }
    return false;
  }

  get username(): string {
    return this.auth.getUserToken().username;
  }

  deleteNode(): void {
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
        this.service.deleteNode(this.type, this.manifestId)
          .pipe(
            finalize(() => {
              this.isUpdating = false;
            })
          )
          .subscribe(_ => {
            this.router.navigateByUrl('/core_component');
          }, error => {
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
        const state = 'Purge';
        this.service.updateState('extension', this.rootNode.manifestId, state)
          .pipe(
            finalize(() => {
              this.isUpdating = false;
            })
          )
          .subscribe(resp => {
            this.snackBar.open('Purged', '', {duration: 3000});
            this.location.back();
            this.router.navigateByUrl('/core_component');
          }, err => {
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
        const state = 'WIP';
        this.service.updateState(this.rootNode.type, this.rootNode.manifestId, state)
          .pipe(
            finalize(() => {
              this.isUpdating = false;
            })
          )
          .subscribe(resp => {
            this.afterStateChanged(resp.state, resp.access);
            this.snackBar.open('Restored', '', {duration: 3000});
          }, err => {
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

    this.tagService.toggleTag(node.type, node.manifestId, tag.name).subscribe(_ => {
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
      width: '90%',
      maxWidth: '90%',
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

  canDrop() {
    return true;
  }

  getAfterNodeForSeq(nodes: CcFlatNode[], currentIndex: number, prevIndex: number) {
    const isDown = (currentIndex - prevIndex > 0);
    currentIndex = isDown ? currentIndex + 1 : currentIndex;
    let after: CcFlatNode;
    if (currentIndex > 0) {
      const justBefore = nodes[currentIndex - 1];
      if (justBefore.level === 0) {
        return null;
      } else if (justBefore.level === 1) {
        after = justBefore;
      } else {
        currentIndex -= 1;
        while (currentIndex > 0) {
          if (nodes[currentIndex].level === 1) {
            after = nodes[currentIndex];
            break;
          }
          currentIndex = currentIndex - 1;
        }
      }
    }
    if (after && after.type === 'ACC') {
      return null;
    }
    return after;
  }

  isValidIndex(nodes: CcFlatNode[], currentIndex: number, prevIndex: number) {
    const isUp = (currentIndex - prevIndex < 0);
    if (currentIndex === prevIndex || currentIndex === 0) {
      return false;
    }
    currentIndex = isUp ? currentIndex - 1 : currentIndex;
    const nextNode = nodes.length > currentIndex ? nodes[currentIndex + 1] : null;
    if (nextNode) {
      //  middle of other node || before Base ACC
      if (nextNode.level !== 1 || nextNode.type === 'ACC') {
        return false;
      }
      if (nextNode.type.toUpperCase() === 'BCCP' && (nextNode as BccpFlatNode).entityType === 'Attribute') {
        return false;
      }
    }
    return true;
  }

  drop(event: CdkDragDrop<CcFlatNode>) {
    if (!event.isPointerOverContainer) {
      return;
    }

    const nodes = this.dataSource.data;

    const eventItem = event.item.data as unknown as CcFlatNode;
    const hashPathMap = nodes.map(e => e.hashPath);

    const previousIndex = hashPathMap.indexOf(eventItem.hashPath);
    const currentIndex = previousIndex + (event.currentIndex - event.previousIndex);

    if (!this.isValidIndex(this.dataSource.data, currentIndex, previousIndex)) {
      return;
    }
    const currentItem = nodes[previousIndex];
    const request = new CcSeqUpdateRequest();
    request.item = new CcId(currentItem.type, currentItem.manifestId);
    const after = this.getAfterNodeForSeq(nodes, currentIndex, previousIndex);
    if (after) {
      request.after = new CcId(after.type, after.manifestId);
      if (request.item.id === request.after.id) {
        return;
      }
    }
    this.isUpdating = true;
    this.service.updateCcSeq(request, this.manifestId).subscribe(_ => {
      this.reload('Updated');
    }, error => {
      this.reload('Failed');
    });
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
        .filter(e => e.menuData.menuId === 'contextMenu').forEach(trigger => {
        this.contextMenuItem = node;
        trigger.openMenu();
      });
    } else if ($event.key === 'c' || $event.key === 'C') {
      this.menuTriggerList.toArray().filter(e => !!e.menuData)
        .filter(e => e.menuData.menuId === 'contextMenu').forEach(trigger => {
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
