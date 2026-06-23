import { ChangeDetectorRef, Component, HostListener, OnInit, QueryList, ViewChild, ViewChildren, inject } from '@angular/core';
import {catchError, switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {Location} from '@angular/common';
import {forkJoin, of} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {AuthService} from '../../authentication/auth.service';
import {Clipboard} from '@angular/cdk/clipboard';
import {RxStompService} from '../../common/score-rx-stomp';
import {WebPageInfoService} from '../../basis/basis.service';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {PreferencesInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {
  ModelBrowserAccNode,
  ModelBrowserAsccpNode,
  ModelBrowserBccpNode,
  ModelBrowserAccNodeDetail,
  ModelBrowserAsccpNodeDetail,
  ModelBrowserBccpNodeDetail,
  ModelBrowserDtScNodeDetail,
  ModelBrowserNode,
  ModelBrowserNodeDatabase,
  ModelBrowserNodeDataSource,
  ModelBrowserNodeDataSourceSearcher
} from './domain/model-browser-node';
import {CdkVirtualScrollViewport} from '@angular/cdk/scrolling';
import {CdkDragDrop, CdkDragMove} from '@angular/cdk/drag-drop';
import {MatMenuTrigger} from '@angular/material/menu';
import {loadBooleanProperty, saveBooleanProperty} from '../../common/utility';
import {ChangeListener} from '../../bie-management/domain/bie-flat-tree';
import {CcNodeService} from '../domain/core-component-node.service';
import {CcFlatNode} from '../domain/cc-flat-tree';
import {FindUsagesDialogComponent} from '../find-usages-dialog/find-usages-dialog.component';
import {BieViewOrderService} from './domain/bie-view-order.service';
import {
  BieViewOrderUpdateEntry,
  computeMinimalReweights,
  ReorderSiblingRef,
  reorderRejectReason
} from './domain/bie-view-order';
import {SetWeightDialogComponent, SetWeightDialogData} from './set-weight-dialog/set-weight-dialog.component';


@Component({
  standalone: false,
  selector: 'score-model-browser',
  templateUrl: './model-browser.component.html',
  styleUrls: ['./model-browser.component.css']
})
export class ModelBrowserComponent implements OnInit, ChangeListener<ModelBrowserNode> {
  private ccNodeService = inject(CcNodeService);
  private snackBar = inject(MatSnackBar);
  private location = inject(Location);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);
  private preferencesService = inject(SettingsPreferencesService);
  private auth = inject(AuthService);
  private stompService = inject(RxStompService);
  private clipboard = inject(Clipboard);
  private bieViewOrderService = inject(BieViewOrderService);
  private cdr = inject(ChangeDetectorRef);
  webPageInfo = inject(WebPageInfoService);


  loading = false;
  paddingPixel = 12;

  type: string;
  manifestId: number;

  innerY: number = window.innerHeight;
  dataSource: ModelBrowserNodeDataSource<ModelBrowserNode>;
  searcher: ModelBrowserNodeDataSourceSearcher<ModelBrowserNode>;
  cursorNode: ModelBrowserNode;
  selectedNode: ModelBrowserNode;

  @ViewChild('virtualScroll', {static: true}) public virtualScroll: CdkVirtualScrollViewport;
  virtualScrollItemSize = 33;

  get minBufferPx(): number {
    return 10000 * this.virtualScrollItemSize;
  }

  get maxBufferPx(): number {
    return 1000000 * this.virtualScrollItemSize;
  }

  @ViewChildren(MatMenuTrigger) menuTriggerList: QueryList<MatMenuTrigger>;
  contextMenuItem: ModelBrowserNode;

  // Issue #1638 — drag state: hover-only handle, and a red drop-slot (placeholder) on an invalid drop.
  // Issue #1638: view-parent ACC manifest ids whose sibling order has already been fetched, so each
  // parent is requested at most once (and again after a write to it).
  private _viewOrderLoaded = new Set<number>();
  dragging = false;
  dropInvalid = false;
  private draggedNode?: ModelBrowserNode;
  private validTargetHashPaths = new Set<string>();

  preferencesInfo: PreferencesInfo;
  HIDE_CARDINALITY_PROPERTY_KEY = 'ModelBrowser-Settings-Hide-Cardinality';

  get hideCardinality(): boolean {
    return this.dataSource.hideCardinality;
  }

  set hideCardinality(hideCardinality: boolean) {
    this.dataSource.hideCardinality = hideCardinality;
    saveBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, hideCardinality);
  }

  ngOnInit(): void {
    this.loading = true;
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        this.type = params.get('type');
        this.manifestId = parseInt(params.get('manifestId'), 10);

        return forkJoin([
          this.ccNodeService.getGraphNode(this.type, this.manifestId),
          this.preferencesService.load(this.auth.getUserToken())
        ]);
      })).subscribe(([ccGraph, preferencesInfo]) => {

      this.preferencesInfo = preferencesInfo;

      // Issue #1638: the sibling view order is fetched LAZILY, one view-parent ACC at a time, as nodes
      // are expanded (see onNodeExpanded) — never upfront — so the client only asks for the parents it
      // actually shows, and the view parent is resolved on the client where group flattening happens.
      this._viewOrderLoaded.clear();
      const database = new ModelBrowserNodeDatabase<ModelBrowserNode>(ccGraph, this.type, this.manifestId);
      this.dataSource = new ModelBrowserNodeDataSource<ModelBrowserNode>(database, this.ccNodeService, [this,]);
      this.dataSource.nodeExpanded$.subscribe(node => this.onNodeExpanded(node));
      this.searcher = new ModelBrowserNodeDataSourceSearcher<ModelBrowserNode>(this.dataSource, database);
      this.dataSource.init();
      this.dataSource.hideCardinality = loadBooleanProperty(this.auth.getUserToken(), this.HIDE_CARDINALITY_PROPERTY_KEY, false);

      this.onClick(this.dataSource.data[0]);

      this.loading = false;
      return;
    }, err => {
      let errorMessage;
      if (err.status === 403) {
        errorMessage = 'You do not have access permission.';
      } else {
        errorMessage = 'Something\'s wrong.';
      }
      this.snackBar.open(errorMessage, '', {
        duration: 3000
      });
      this.router.navigateByUrl('/core_component');
    });
  }

  onResize(event) {
    this.innerY = window.innerHeight;
  }

  get innerHeight(): number {
    return this.innerY - 200;
  }

  toggle(node: ModelBrowserNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.dataSource.toggle(node);
  }

  onClick(node: ModelBrowserNode, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.dataSource.loadDetail(node, (detailNode: ModelBrowserNode) => {
      this.selectedNode = node;
      this.cursorNode = node;
    });

  }

  scrollToNode(node: ModelBrowserNode, delay?: number) {
    const index = this.searcher.getNodeIndex(node);
    this.scrollTree(index, delay);
    this.cursorNode = node;
  }

  private visibleChoiceChildrenOf(choiceNode: ModelBrowserNode): ModelBrowserNode[] {
    return (((choiceNode.children || []) as ModelBrowserNode[]))
      .filter(child => !child.isGroup && !child.isChoice);
  }

  isChoiceChild(node: ModelBrowserNode): boolean {
    return !!node?.parent && (node.parent as ModelBrowserNode).isChoice;
  }

  private visibleChoiceChildren(node: ModelBrowserNode): ModelBrowserNode[] {
    if (!node?.parent || !(node.parent as ModelBrowserNode).isChoice) {
      return [];
    }
    return this.visibleChoiceChildrenOf(node.parent as ModelBrowserNode);
  }

  isFirstChoiceChild(node: ModelBrowserNode): boolean {
    const siblings = this.visibleChoiceChildren(node);
    return siblings.length > 0 && siblings[0] === node;
  }

  isLastChoiceChild(node: ModelBrowserNode): boolean {
    const siblings = this.visibleChoiceChildren(node);
    return siblings.length > 0 && siblings[siblings.length - 1] === node;
  }

  isOnlyChoiceChild(node: ModelBrowserNode): boolean {
    return this.visibleChoiceChildren(node).length === 1;
  }

  private choiceBranchRoot(node: ModelBrowserNode): ModelBrowserNode | null {
    let current = node;
    while (!!current?.parent) {
      const parent = current.parent as ModelBrowserNode;
      if (parent.isChoice) {
        return current;
      }
      current = parent;
    }
    return null;
  }

  choiceTooltip(node: ModelBrowserNode): string | null {
    let current = node;
    while (!!current?.parent) {
      const parent = current.parent as ModelBrowserNode;
      if (parent.isChoice) {
        return (parent as ModelBrowserAsccpNode).accNode?.objectClassTerm || parent.name;
      }
      current = parent;
    }
    return null;
  }

  hasChoiceBranchContinuation(node: ModelBrowserNode): boolean {
    const branchRoot = this.choiceBranchRoot(node);
    return !!branchRoot && branchRoot !== node && !this.isLastChoiceChild(branchRoot);
  }

  choiceGuideExtraOffset(node: ModelBrowserNode): number {
    const branchRoot = this.choiceBranchRoot(node);
    if (!branchRoot) {
      return 0;
    }
    return Math.max(0, node.level - branchRoot.level) * this.paddingPixel;
  }

  choiceGuides(node: ModelBrowserNode): {offset: number, continuation: boolean, first: boolean, last: boolean, only: boolean, tooltip: string | null}[] {
    const guides: {offset: number, continuation: boolean, first: boolean, last: boolean, only: boolean, tooltip: string | null}[] = [];
    let current: ModelBrowserNode = node;

    while (!!current?.parent) {
      const parent = current.parent as ModelBrowserNode;
      if (parent.isChoice) {
        const siblings = this.visibleChoiceChildrenOf(parent);
        const branchRoot = current;
        const branchIndex = siblings.indexOf(branchRoot);
        if (branchIndex !== -1) {
          const continuation = branchRoot !== node;
          const last = branchIndex === siblings.length - 1;
          if (!continuation || !last) {
            guides.push({
              offset: Math.max(0, node.level - branchRoot.level) * this.paddingPixel,
              continuation,
              first: branchIndex === 0,
              last,
              only: siblings.length === 1,
              tooltip: (parent as ModelBrowserAsccpNode).accNode?.objectClassTerm || parent.name
            });
          }
        }
      }
      current = parent;
    }

    return guides.sort((a, b) => b.offset - a.offset);
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

  }

  keyNavigation(node: ModelBrowserNode, $event: KeyboardEvent) {
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

  copyPath(node: ModelBrowserNode) {
    if (!node) {
      return;
    }

    const delimiter = this.preferencesInfo.viewSettingsInfo.treeSettings.delimiter;
    let queryPath = node.queryPath;
    queryPath = queryPath.split('/').join(delimiter);

    this.clipboard.copy(queryPath);
    this.snackBar.open('Copied to clipboard', '', {
      duration: 3000
    });
  }

  openDiagram(node: ModelBrowserNode) {
    if (!node) {
      return;
    }

    if (this.isAccDetail(node)) {
      window.open('/core_component/browser/asccp/' + this.asAccDetail(node).asccp.manifestId + '/plantuml', '_blank');
    } else if (this.isAsccpDetail(node)) {
      window.open('/core_component/browser/asccp/' + this.asAsccpDetail(node).asccp.manifestId + '/plantuml', '_blank');
    } else {
      return;
    }
  }

  findUsages(node: ModelBrowserNode) {
    if (!node) {
      return;
    }

    let data;
    if (this.isAccDetail(node)) { // if the node is a root
      data = {
        type: 'ASCCP',
        manifestId: this.asAccDetail(node).asccp.manifestId
      };
    } else if (this.isAsccpDetail(node)) {
      data = {
        type: node.ccType,
        manifestId: this.asAsccpDetail(node).asccp.manifestId
      };
    } else if (this.isBccpDetail(node)) {
      data = {
        type: node.ccType,
        manifestId: this.asBccpDetail(node).bccp.manifestId
      };
    } else {
      return;
    }

    const dialogRef = this.dialog.open(FindUsagesDialogComponent, {
      data,
      width: '600px',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(_ => {
    });
  }

  copyLink(node: ModelBrowserNode, $event?) {
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

  onChange(entity: ModelBrowserNode, propertyName: string, val: any) {
  }

  /* For type casting of detail property */
  isAccDetail(node?: ModelBrowserNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.ccType.toUpperCase() === 'ACC');
  }

  asAccDetail(node?: ModelBrowserNode): ModelBrowserAccNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as ModelBrowserAccNodeDetail;
  }

  isAsccpDetail(node?: ModelBrowserNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.ccType.toUpperCase() === 'ASCCP');
  }

  asAsccpDetail(node?: ModelBrowserNode): ModelBrowserAsccpNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as ModelBrowserAsccpNodeDetail;
  }

  isBccpDetail(node?: ModelBrowserNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.ccType.toUpperCase() === 'BCCP');
  }

  asBccpDetail(node?: ModelBrowserNode): ModelBrowserBccpNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as ModelBrowserBccpNodeDetail;
  }

  isDtScDetail(node?: ModelBrowserNode): boolean {
    if (!node) {
      node = this.selectedNode;
    }
    return (node !== undefined) && (node.ccType.toUpperCase() === 'BDT_SC');
  }

  asDtScDetail(node?: ModelBrowserNode): ModelBrowserDtScNodeDetail {
    if (!node) {
      node = this.selectedNode;
    }
    return node.detail as ModelBrowserDtScNodeDetail;
  }

  visibleFindUsages(node?: ModelBrowserNode): boolean {
    if (!node) {
      return false;
    }
    return node.ccType.toUpperCase() === 'ACC' || node.ccType.toUpperCase() === 'ASCCP' || node.ccType.toUpperCase() === 'BCCP';
  }

  /* Issue #1638 — sibling sort order (developer-only "Set weight…") */

  /**
   * The nearest displayed ancestor of a flattened sibling (skips group/choice nodes, which are
   * flattened away). This is the view parent whose acc keys the sibling's weight.
   */
  private displayParentNode(node: ModelBrowserNode): ModelBrowserNode | undefined {
    let parent = node.parent as ModelBrowserNode;
    while (parent && (parent.isGroup || parent.isChoice)) {
      parent = parent.parent as ModelBrowserNode;
    }
    return parent;
  }

  /** Developers only; an ASCCP/BCCP sibling (not the root, not a group/choice, not DT_SC) with a view parent. */
  canReorder(node?: ModelBrowserNode): boolean {
    if (!node || !this.auth.isDeveloper()) {
      return false;
    }
    const self = node.self;
    if (self.isGroup || self.isChoice) {
      return false;
    }
    const isAsccp = self instanceof ModelBrowserAsccpNode;
    const isBccp = self instanceof ModelBrowserBccpNode;
    if (!isAsccp && !isBccp) {
      return false;
    }
    const parent = this.displayParentNode(node);
    return !!parent && !!(parent.self as ModelBrowserAccNode).accNode;
  }

  /**
   * Current weight of a sibling (for prefilling the dialog and for the order-weight badge), or
   * undefined if it has none. Called UNCONDITIONALLY for every rendered row by the badge, so it must
   * be null-safe for nodes whose display parent is not an ACC view parent (e.g. a DT_SC node, whose
   * display parent is a BCCP with no accNode): viewParentAccManifestId returns undefined for those.
   */
  currentWeightOf(node: ModelBrowserNode): number | undefined {
    const parent = this.displayParentNode(node);
    if (!parent) {
      return undefined;
    }
    const viewParentAccManifestId = this.dataSource.database.viewParentAccManifestId(parent);
    if (viewParentAccManifestId === undefined) {
      return undefined;
    }
    return this.dataSource.database.getViewOrderWeight(viewParentAccManifestId, node);
  }

  setWeight(node: ModelBrowserNode) {
    if (!this.canReorder(node)) {
      return;
    }
    const self = node.self;
    const parent = this.displayParentNode(node);
    const viewParentAccManifestId = (parent.self as ModelBrowserAccNode).accNode.manifestId;

    const dialogRef = this.dialog.open(SetWeightDialogComponent, {
      data: {name: node.name, currentWeight: this.currentWeightOf(node)} as SetWeightDialogData,
      width: '460px',
      autoFocus: 'first-tabbable'
    });

    dialogRef.afterClosed().subscribe((result: number | null | undefined) => {
      if (result === undefined) {
        return; // cancelled
      }
      const entry: BieViewOrderUpdateEntry = {
        asccManifestId: (self instanceof ModelBrowserAsccpNode) ? (self as ModelBrowserAsccpNode).asccNode.manifestId : undefined,
        bccManifestId: (self instanceof ModelBrowserBccpNode) ? (self as ModelBrowserBccpNode).bccNode.manifestId : undefined,
        weight: result // null => reset to seq_key position
      };
      this.bieViewOrderService.updateViewOrder(viewParentAccManifestId, [entry]).subscribe(() => {
        this.reloadViewOrderAndResort(viewParentAccManifestId);
        this.snackBar.open((result === null) ? 'Order weight reset' : 'Order weight updated', '', {duration: 3000});
      }, () => {
        this.snackBar.open('Failed to update order weight', '', {duration: 3000});
      });
    });
  }

  /**
   * Developer-only "Reset Order Weights" — shown on ANY view-parent node (the root ACC OR a nested
   * ASCCP), so a developer can clear the custom order weights of THAT node's own flattened children
   * (Issue #1638). Enabled when the node is an ACC view parent; a group/choice node is not a view
   * parent (its children key under the display parent, mirroring canReorder) and a BCCP/DT_SC node
   * has no view-parent ACC, so both are excluded.
   */
  canResetOrderWeights(node?: ModelBrowserNode): boolean {
    if (!node || !this.auth.isDeveloper()) {
      return false;
    }
    const self = node.self;
    if (self.isGroup || self.isChoice) {
      return false;
    }
    return this.dataSource.database.viewParentAccManifestId(node) !== undefined;
  }

  /**
   * Issue #1638: developer-only "Reset Order Weights" on a view-parent node (the root ACC or a nested
   * ASCCP). After a warning, delete ALL sibling weights stored directly under THIS node's ACC
   * (from_acc_manifest_id = node.accNode.manifestId), then re-render its children back in the default
   * (seq_key) order. Children reordered under deeper (nested) ACCs are NOT affected — this resets only
   * this node's own children.
   */
  resetOrderWeights(node: ModelBrowserNode) {
    if (!this.canResetOrderWeights(node)) {
      return;
    }
    const viewParentAccId = this.dataSource.database.viewParentAccManifestId(node);
    if (viewParentAccId === undefined) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Reset order weights?';
    dialogConfig.data.content = [
      'This removes every custom order weight set directly under \'' + node.name + '\', returning its children to the default order.',
      'This cannot be undone.'
    ];
    dialogConfig.data.action = 'Reset';

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(result => {
      if (!result) {
        return; // cancelled
      }
      this.bieViewOrderService.resetViewOrder(viewParentAccId).subscribe(() => {
        // A confirmed DELETE leaves zero rows for this view parent, so the post-state is known
        // exactly — clear the cached weights locally and re-render in seq_key order right away
        // (no dependence on a follow-up GET, unlike setWeight/drop which must re-fetch the merged
        // result). This keeps the tree consistent with the success toast even if a re-sync failed.
        this.dataSource.database.setViewOrderForParent(viewParentAccId, []);
        this.resortAllExpandedFor(viewParentAccId);
        this.snackBar.open('Order weights reset', '', {duration: 3000});
      }, () => {
        this.snackBar.open('Failed to reset order weights', '', {duration: 3000});
      });
    });
  }

  /** A flattened sibling is an attribute (BCCP entityType=Attribute) vs an element (ASCCP / non-attribute BCCP). */
  private isAttributeNode(node: ModelBrowserNode): boolean {
    const self = node.self;
    return self instanceof ModelBrowserBccpNode && (self as ModelBrowserBccpNode).bccNode.entityType === 'Attribute';
  }

  /** cdkDropList enter predicate — the whole tree is one drop list; validity is enforced in drop(). */
  canDrop = () => true;

  /**
   * The minimal facts the partition/level reorder rules need about a flattened sibling. The view
   * parent is keyed by its hashPath, a sound surrogate for parent identity HERE because the model
   * browser never sets `reused` on its nodes, so each display position has a distinct positional
   * hashPath (unlike the BIE editor, whose reused subtrees collapse to a bare path).
   */
  private toReorderRef(node: ModelBrowserNode): ReorderSiblingRef {
    const parent = this.displayParentNode(node);
    return {
      viewParentKey: parent ? parent.hashPath : undefined,
      level: node.level,
      isAttribute: this.isAttributeNode(node),
      key: node.hashPath
    };
  }

  /** Why a drop of `dragged` onto `target` is rejected, or null when it is valid (or a no-op). */
  private dropRejectReason(dragged: ModelBrowserNode, target: ModelBrowserNode): string | null {
    return reorderRejectReason(this.toReorderRef(dragged), this.toReorderRef(target));
  }

  /** A valid drop target = a real sibling (same view parent, level, partition), excluding the dragged node. */
  private isValidDropTarget(dragged: ModelBrowserNode, target: ModelBrowserNode): boolean {
    return !!target && target.hashPath !== dragged.hashPath && this.dropRejectReason(dragged, target) === null;
  }

  /** Drag started: enter drag mode (hides all handles) and pre-compute the valid drop targets. */
  onDragStarted(node: ModelBrowserNode) {
    this.dragging = true;
    this.draggedNode = node;
    this.dropInvalid = false;
    this.validTargetHashPaths = new Set(
      this.dataSource.data.filter(n => this.isValidDropTarget(node, n)).map(n => n.hashPath));
    this.cdr.detectChanges();
  }

  /** While dragging: mark the drop slot (placeholder) red when the drop position would be invalid. */
  onDragMoved(event: CdkDragMove) {
    if (!this.draggedNode) {
      return;
    }
    // During a CDK *sorting* drag the rows get pointer-events:none (so elementFromPoint returns the
    // drop-list container) and the placeholder follows the cursor — there is no "row under the cursor".
    // So look at the drop slot's two immediate NEIGHBOURS (nearest non-dragged rows above & below the
    // pointer) using their live rects. The drop is VALID when at least one neighbour is a valid sibling
    // — which includes the node's OWN home position (it sits next to a real sibling), so returning it to
    // where it started is never flagged red. It is invalid only when BOTH neighbours are non-siblings
    // (i.e. the slot has crossed the partition / level boundary into foreign territory).
    let next = false;
    if (this.validTargetHashPaths.size > 0) {
      const pointerY = event.pointerPosition.y;
      const viewport: HTMLElement = this.virtualScroll && this.virtualScroll.elementRef
        ? this.virtualScroll.elementRef.nativeElement : null;
      const rows = viewport ? Array.from(viewport.querySelectorAll('.mat-tree-node[data-hash-path]')) : [];
      let aboveHash: string | null = null, aboveBottom = -Infinity;
      let belowHash: string | null = null, belowTop = Infinity;
      for (const row of rows) {
        const hashPath = (row as HTMLElement).getAttribute('data-hash-path');
        if (hashPath === this.draggedNode.hashPath) {
          continue; // skip the dragged node's placeholder
        }
        const rect = (row as HTMLElement).getBoundingClientRect();
        if (pointerY < rect.top) {
          if (rect.top < belowTop) { belowTop = rect.top; belowHash = hashPath; }
        } else if (pointerY >= rect.bottom) {
          if (rect.bottom > aboveBottom) { aboveBottom = rect.bottom; aboveHash = hashPath; }
        } else {
          // the cursor sits within a real (non-dragged) row -> it is the neighbour on both sides
          aboveHash = belowHash = hashPath;
          aboveBottom = rect.bottom;
          belowTop = rect.top;
        }
      }
      const aboveValid = aboveHash != null && this.validTargetHashPaths.has(aboveHash);
      const belowValid = belowHash != null && this.validTargetHashPaths.has(belowHash);
      next = !(aboveValid || belowValid);
    }
    if (next !== this.dropInvalid) {
      this.dropInvalid = next;
      this.cdr.detectChanges();
    }
  }

  /** Drag ended: leave drag mode and clear the highlight. */
  onDragEnded() {
    this.dragging = false;
    this.dropInvalid = false;
    this.draggedNode = undefined;
    this.validTargetHashPaths = new Set<string>();
    this.cdr.detectChanges();
  }

  /**
   * Drag&drop reorder (#1638): re-space the dragged node's sibling group (same VIEW parent + same
   * attribute/element partition) with descending integer weights so the new visual order persists.
   * A drop onto a different level/parent, or that crosses the attribute/element partition, is rejected
   * with a message (per issue rules — attributes and elements are a hard partition, and reorder is
   * only among same-level siblings). Developer-only via canReorder/cdkDragDisabled.
   */
  drop(event: CdkDragDrop<ModelBrowserNodeDataSource<ModelBrowserNode>>) {
    if (!event.isPointerOverContainer) {
      return;
    }
    const data = this.dataSource.data;
    const dragged = event.item.data as ModelBrowserNode;
    if (!this.canReorder(dragged)) {
      return;
    }
    const previousIndex = data.findIndex(e => e.hashPath === dragged.hashPath);
    if (previousIndex < 0) {
      return;
    }
    // Virtual scroll reports rendered-window indices; the delta maps to data space (as in acc-detail).
    let currentIndex = previousIndex + (event.currentIndex - event.previousIndex);
    currentIndex = Math.max(0, Math.min(data.length - 1, currentIndex));
    if (currentIndex === previousIndex) {
      return; // no movement
    }

    // The sibling the node was dropped onto — validate the destination before reordering.
    const target = data[currentIndex];
    if (!target || target.hashPath === dragged.hashPath) {
      return;
    }
    const reason = this.dropRejectReason(dragged, target);
    if (reason) {
      this.snackBar.open(reason, '', {duration: 4000});
      return;
    }
    const parent = this.displayParentNode(dragged);
    if (!parent) {
      return;
    }
    const draggedIsAttr = this.isAttributeNode(dragged);

    const viewParentAccManifestId = (parent.self as ModelBrowserAccNode).accNode?.manifestId;
    if (viewParentAccManifestId === undefined) {
      return;
    }

    // Simulate the move in a copy; `group` is the dragged node's partition in its new order (top -> bottom).
    const reordered = data.slice();
    reordered.splice(previousIndex, 1);
    reordered.splice(currentIndex, 0, dragged);
    const group = reordered.filter(n =>
      this.canReorder(n) &&
      this.displayParentNode(n) === parent &&
      this.isAttributeNode(n) === draggedIsAttr);
    if (group.length < 2) {
      return;
    }

    // MINIMAL re-weighting: only assign weights to siblings whose order is actually violated; leave the
    // rest unset (default 0 / seq_key) or at their existing weight. Needs each sibling's seq_key (default)
    // rank, taken from the database's flatten() order. See computeMinimalReweights.
    const seqKeyList = draggedIsAttr
      ? this.dataSource.database.seqKeyPartitions(parent).attributes
      : this.dataSource.database.seqKeyPartitions(parent).nodes;
    const rankByHash = new Map<string, number>(seqKeyList.map((n, idx) => [n.hashPath, idx] as [string, number]));
    const currentWeights = group.map(n => this.dataSource.database.getViewOrderWeight(viewParentAccManifestId, n));
    const seqKeyRank = group.map(n => rankByHash.get(n.hashPath) ?? 0);
    const reweights = computeMinimalReweights(currentWeights, seqKeyRank);
    if (reweights.size === 0) {
      return; // the new order needs no weight changes
    }
    const entries: BieViewOrderUpdateEntry[] = [];
    reweights.forEach((weight, i) => {
      const self = group[i].self;
      entries.push({
        asccManifestId: (self instanceof ModelBrowserAsccpNode) ? (self as ModelBrowserAsccpNode).asccNode.manifestId : undefined,
        bccManifestId: (self instanceof ModelBrowserBccpNode) ? (self as ModelBrowserBccpNode).bccNode.manifestId : undefined,
        weight
      });
    });

    this.bieViewOrderService.updateViewOrder(viewParentAccManifestId, entries).subscribe(() => {
      this.reloadViewOrderAndResort(viewParentAccManifestId);
      this.snackBar.open('Order updated', '', {duration: 2000});
    }, () => {
      this.snackBar.open('Failed to update order', '', {duration: 3000});
    });
  }

  /**
   * Issue #1638: the first time a view-parent ACC node is expanded, fetch that parent's sibling order
   * lazily, merge it, and re-sort its children. Most parents have no rows (empty list => no visible
   * change); only an explicitly reordered parent re-sorts.
   */
  private onNodeExpanded(node: ModelBrowserNode) {
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

  /** Re-fetch one view parent's order after a write and re-render its children in the new order. */
  private reloadViewOrderAndResort(viewParentAccManifestId: number) {
    this._viewOrderLoaded.add(viewParentAccManifestId);
    this.bieViewOrderService.getViewOrder(viewParentAccManifestId)
      .pipe(catchError(() => of(null)))
      .subscribe((entries) => {
        if (entries === null) {
          this._viewOrderLoaded.delete(viewParentAccManifestId);
          return;
        }
        this.dataSource.database.setViewOrderForParent(viewParentAccManifestId, entries);
        this.resortAllExpandedFor(viewParentAccManifestId);
      });
  }

  /**
   * Re-render EVERY currently-expanded position of a view parent. The same ACC can sit at several
   * tree positions; any that were expanded while this parent's GET was in flight rendered in seq_key
   * order, so they all need re-sorting once the weights arrive.
   */
  private resortAllExpandedFor(viewParentAccManifestId: number) {
    const targets = this.dataSource.data.filter(n =>
      n.expanded && this.dataSource.database.viewParentAccManifestId(n) === viewParentAccManifestId);
    targets.forEach(n => this.resortUnder(n));
  }

  /** Re-render a parent's children in the (updated) view order, preserving the user's deeper expansion. */
  private resortUnder(displayParent: ModelBrowserNode) {
    if (!displayParent || !this.dataSource.isExpanded(displayParent)) {
      return;
    }
    // Snapshot expansion so re-rendering the new order does not collapse the user's deeper expansion
    // below the reordered parent (parity with the BIE editor's reloadTree).
    const expandedHashPaths = this.dataSource.data.filter(e => e.expanded).map(e => e.hashPath);
    this.dataSource.collapse(displayParent);
    this.dataSource.expand(displayParent);
    for (const hashPath of expandedHashPaths) {
      const datum = this.dataSource.data.find(d => d.hashPath === hashPath && !d.expanded);
      if (datum) {
        this.dataSource.expand(datum);
      }
    }
  }

}
