import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {Component, OnInit, ViewChild} from '@angular/core';
import {MatSidenav} from '@angular/material/sidenav';
import {finalize, switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {DataSourceSearcher, VSFlatTreeControl} from '../../common/flat-tree';
import {SimpleNamespace} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {ReleaseService} from '../../release-management/domain/release.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AsccpFlatNode, CcFlatNode, CcFlatNodeFlattener, VSCcTreeDataSource} from '../domain/cc-flat-tree';
import {CcNodeService} from '../domain/core-component-node.service';
import {
  CcAccNodeDetail,
  CcAsccpNodeDetail,
  CcBccpNodeDetail,
  CcBdtScNodeDetail,
  CcNodeDetail,
  CcRevisionResponse,
  Comment,
  EntityType,
  EntityTypes,
  OagisComponentType,
  OagisComponentTypes
} from '../domain/core-component-node';
import {ContextMenuComponent, ContextMenuService} from 'ngx-contextmenu';
import {CreateAsccpDialogComponent} from '../cc-list/create-asccp-dialog/create-asccp-dialog.component';
import {AuthService} from '../../authentication/auth.service';
import {WorkingRelease} from '../../release-management/domain/release';
import {CommentControl} from '../domain/comment-component';
import {forkJoin} from 'rxjs';
import {Message} from '@stomp/stompjs';
import {RxStompService} from '@stomp/ng2-stompjs';
import {Location} from '@angular/common';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {SplitAreaDirective} from 'angular-split';
import {SearchOptionsService} from '../search-options-dialog/domain/search-options-service';
import {SearchOptionsDialogComponent} from '../search-options-dialog/search-options-dialog.component';

@Component({
  selector: 'score-asccp-detail',
  templateUrl: './asccp-detail.component.html',
  styleUrls: ['./asccp-detail.component.css']
})
export class AsccpDetailComponent implements OnInit {
  title: string;
  _innerHeight: number = window.innerHeight;
  paddingPixel = 12;
  manifestId: number;
  type = 'ASCCP';
  isUpdating: boolean;
  componentTypes: OagisComponentType[] = OagisComponentTypes;
  entityTypes: EntityType[] = EntityTypes;

  rootNode: AsccpFlatNode;
  dataSource: VSCcTreeDataSource<CcFlatNode>;
  treeControl: VSFlatTreeControl<CcFlatNode> = new VSFlatTreeControl<CcFlatNode>();
  searcher: DataSourceSearcher<CcFlatNode>;

  lastRevision: CcRevisionResponse;
  selectedNode: CcFlatNode;
  cursorNode: CcFlatNode;

  workingRelease = WorkingRelease;
  namespaces: SimpleNamespace[];
  commentControl: CommentControl;

  @ViewChild('sidenav', {static: true}) sidenav: MatSidenav;
  @ViewChild('defaultContextMenu', {static: true}) public defaultContextMenu: ContextMenuComponent;
  @ViewChild('asccpContextMenu', {static: true}) public asccpContextMenu: ContextMenuComponent;
  @ViewChild('leftPanel', {static: true}) public leftPanel: SplitAreaDirective;
  @ViewChild('rightPanel', {static: true}) public rightPanel: SplitAreaDirective;
  @ViewChild('virtualScroll', {static: true}) public virtualScroll: CdkVirtualScrollViewport;
  virtualScrollItemSize: number = 33;

  get minBufferPx(): number {
    return Math.max((this.rootNode) ? this.rootNode.children.length : 0, 20) * this.virtualScrollItemSize;
  }

  get maxBufferPx(): number {
    return Math.max((this.rootNode) ? this.rootNode.children.length : 0, 20) * 20 * this.virtualScrollItemSize;
  }

  constructor(private service: CcNodeService,
              private searchOptionsService: SearchOptionsService,
              private releaseService: ReleaseService,
              private snackBar: MatSnackBar,
              private contextMenuService: ContextMenuService,
              private namespaceService: NamespaceService,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private auth: AuthService,
              private stompService: RxStompService) {
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
          this.service.getAsccpNode(this.manifestId),
          this.namespaceService.getSimpleNamespaces()
        ]);
      })).subscribe(([ccGraph, revisionResponse, rootNode, namespaces]) => {
      this.lastRevision = revisionResponse;
      this.namespaces = namespaces;

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

      const flattener = new CcFlatNodeFlattener(ccGraph, 'ASCCP', this.manifestId);
      setTimeout(() => {
        const nodes = flattener.flatten();
        this.dataSource = new VSCcTreeDataSource(this.treeControl, nodes, this.service, []);
        this.isUpdating = false;
        this.rootNode = nodes[0] as AsccpFlatNode;
        this.rootNode.access = rootNode.access;
        this.rootNode.state = rootNode.state;
        this.rootNode.reset();

        this.searcher = new DataSourceSearcher(this.dataSource);
        this.onClick(this.rootNode);
      }, 0);
    });
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
      let idx = this.commentControl.comments.findIndex(e => e.commentId === comment.prevCommentId);
      let childrenCnt = this.commentControl.comments.filter(e => e.prevCommentId === comment.prevCommentId).length;
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
      return (this.userRole === 'developer') ? e.standard : !e.standard;
    });
  }

  getGraph(callbackFn) {
    this.service.getGraphNode(this.rootNode.type, this.manifestId).subscribe(graph => {
      const flattener = new CcFlatNodeFlattener(
        graph, 'ASCCP', this.manifestId);
      setTimeout(() => {
        const nodes = flattener.flatten();
        return callbackFn(nodes);
      });
    });
  }

  reload(snackMsg?: string) {
    this.isUpdating = true;
    forkJoin([
      this.service.getAsccpNode(this.manifestId),
      this.service.getGraphNode(this.rootNode.type, this.manifestId)
    ]).subscribe(([rootNode, graph]) => {
      const flattener = new CcFlatNodeFlattener(
        graph, 'ASCCP', this.manifestId);
      setTimeout(() => {
        const nodes = flattener.flatten();
        this.dataSource = new VSCcTreeDataSource(this.treeControl, nodes, this.service, []);
        this.isUpdating = false;
        this.rootNode = nodes[0] as AsccpFlatNode;
        this.rootNode.access = rootNode.access;
        this.rootNode.state = rootNode.state;
        this.rootNode.reset();
        this.searcher = new DataSourceSearcher(this.dataSource);
        this.treeControl.expand(this.dataSource.getRootNode());
        this.onClick(this.dataSource.getRootNode());
      }, 0);
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
    this.dataSource.loadDetail(node, (detail: CcNodeDetail) => {
      this.selectedNode = node;
      this.cursorNode = node;
    });
  }

  toggle(node: CcFlatNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.treeControl.toggle(node);
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
    return (node !== undefined) && (node.type.toUpperCase() === 'BDT_SC');
  }

  asBdtScDetail(node?: CcFlatNode): CcBdtScNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as CcBdtScNodeDetail;
  }

  get isChanged() {
    return this.dataSource.changedNodes.length > 0;
  }

  _updateDetails(details: CcFlatNode[]) {
    this.isUpdating = true;
    this.service.updateDetails(this.manifestId, details)
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

  updateDetails() {
    if (!this.isChanged || this.isUpdating) {
      return;
    }

    const details = this.dataSource.changedNodes;
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

  onContextMenu($event: MouseEvent, node: CcFlatNode): void {
    this.contextMenuService.show.next({
      contextMenu: (node.level === 0) ? this.asccpContextMenu : this.defaultContextMenu,
      event: $event,
      item: node,
    });
    $event.preventDefault();
    $event.stopPropagation();
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

  changeAcc(node: CcFlatNode) {
    const dialogRef = this.dialog.open(CreateAsccpDialogComponent, {
      data: {
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
    dialogRef.afterClosed().subscribe(accManifestId => {
      if (!accManifestId) {
        return;
      }
      this.service.updateAsccpManifest(this.rootNode.manifestId, accManifestId).subscribe(asccp => {
        this.getGraph((nodes: CcFlatNode[]) => {
          this.dataSource.removeNodes(0);
          const targetNodes = this.dataSource.getNodesByLevelAndIndex(nodes, 0);
          this.dataSource.insertNodes(targetNodes, 0);
          this.snackBar.open('Updated', '', { duration: 3000});
          this.isUpdating = false;
        });
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
    const rootNode = this.dataSource.getRootNode();

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
    const root = this.dataSource.getRootNode();
    (root.detail as CcAsccpNodeDetail).asccp.state = state;
  }

  makeNewRevision() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = (this.userRole === 'developer') ? 'Revise this ASCCP?' : 'Amend this ASCCP?';
    dialogConfig.data.content = [(this.userRole === 'developer') ? 'Are you sure you want to revise this ASCCP?' : 'Are you sure you want to amend this ASCCP?'];
    dialogConfig.data.action = (this.userRole === 'developer') ? 'Revise' : 'Amend';

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
              this.snackBar.open((this.userRole === 'developer') ? 'Revised' : 'Amended', '', {
                duration: 3000,
              });
            });
            this.reload();
          });
      });
  }

  get userRole(): string {
    const userToken = this.auth.getUserToken();
    return userToken.role;
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  isWorkingRelease(): boolean {
    if (this.rootNode) {
      return this.rootNode.releaseId === this.workingRelease.releaseId;
    }
    return false;
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
    window.open('/log/core-component/' + node.guid, '_blank');
  }

  openComments(type: string, node?: CcFlatNode) {
    if (!node) {
      node = this.selectedNode;
    }
    this.commentControl.toggleCommentSlide(type, node.detail);
  }

  scrollToNode(node: CcFlatNode) {
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

  cancelRevision(): void {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = (this.userRole === 'developer') ? 'Cancel this revision?' : 'Cancel this amendment?';
    dialogConfig.data.content = [(this.userRole === 'developer') ? 'Are you sure you want to cancel this revision?' : 'Are you sure you want to cancel this amendment?'];
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
      this.virtualScroll.scrollToIndex(index);
    });
  }

  move(val: number) {
    this.searcher.go(val).subscribe(index => {
      this.virtualScroll.scrollToIndex(index);
    });
  }
}
