import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  HostBinding,
  HostListener,
  inject,
  Input,
  OnDestroy,
  Output,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {MatCardModule} from '@angular/material/card';
import {AngularSplitModule} from 'angular-split';
import {TreeScrollTarget, TreeSearcher, TreeSearchBarComponent} from '../tree-search-bar/tree-search-bar.component';
import {TreeBreadcrumbNode, TreeBreadcrumbsComponent} from '../tree-breadcrumbs/tree-breadcrumbs.component';

/** Below this width (px) the tree is stacked above the detail pane instead of beside it. */
const NARROW_WIDTH = 768;
/** Height (px) of the header (search row + breadcrumbs) when it fits on one line; `heightOffset` already counts it. */
const BASE_HEADER_HEIGHT = 83;

export interface TreeDetailNode extends TreeBreadcrumbNode {
  parents?: TreeBreadcrumbNode[];
}

/**
 * Common layout of the tree-based detail screens (ACC, ASCCP, BCCP, BDT, Extension, BIE edit):
 * a sticky header (tree search, actions, breadcrumbs) above a resizable split of the node tree and the node detail.
 *
 * Content slots: `[treeDetailActions]` (header buttons), `[treeDetailTree]` (tree viewport and its menus),
 * `[treeDetailPane]` (detail panels of the selected node).
 * The tree viewport should be sized with the component's `treeHeight` (e.g. `[style.height.px]="treeDetail.treeHeight"`).
 *
 * The operations every such screen needs on its tree are done here, not by the screen: searching, stepping through the
 * matches (which selects each one) and a click on a breadcrumb all bring the node into view in `tree`. A screen gives
 * the tree viewport as `[tree]` and reacts to `(nodeSelect)`.
 */
@Component({
  selector: 'score-tree-detail',
  templateUrl: './tree-detail.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './tree-detail.component.css',
  encapsulation: ViewEncapsulation.None,
  imports: [MatCardModule, AngularSplitModule, TreeSearchBarComponent, TreeBreadcrumbsComponent]
})
export class TreeDetailComponent<N extends TreeDetailNode = TreeDetailNode> implements OnDestroy {

  private readonly cdr = inject(ChangeDetectorRef);

  /** Show the header once the tree and its searcher are ready. */
  @Input() ready = true;
  @Input() searcher: TreeSearcher<N> | undefined;
  /** The tree viewport (`score-cc-tree-viewport` etc.): where searches and breadcrumb clicks scroll to. */
  @Input() tree: TreeScrollTarget<N> | undefined;
  @Input() selectedNode: N | undefined | null;
  /** Space (px) taken by everything but the tree viewport. */
  @Input() heightOffset = 180;
  /** Let the split scroll vertically (BIE edit). */
  @Input() splitScroll = false;

  /** A match was stepped to with the search arrows; the screen selects it. */
  @Output() nodeSelect = new EventEmitter<N>();

  private viewportHeight = window.innerHeight;
  private viewportWidth = window.innerWidth;
  private headerHeight = BASE_HEADER_HEIGHT;
  private headerObserver?: ResizeObserver;

  /** The header wraps onto more lines on a small screen; its real height keeps the tree from overflowing the page. */
  @ViewChild('header', {read: ElementRef}) set header(ref: ElementRef<HTMLElement> | undefined) {
    this.headerObserver?.disconnect();
    if (!ref || typeof ResizeObserver === 'undefined') {
      return;
    }
    this.headerObserver = new ResizeObserver(() => {
      const height = ref.nativeElement.offsetHeight;
      if (height !== this.headerHeight) {
        this.headerHeight = height;
        this.cdr.detectChanges();
      }
    });
    this.headerObserver.observe(ref.nativeElement);
  }

  @HostBinding('class.narrow') get narrow(): boolean {
    return this.viewportWidth < NARROW_WIDTH;
  }

  @HostListener('window:resize')
  onResize() {
    this.viewportHeight = window.innerHeight;
    this.viewportWidth = window.innerWidth;
  }

  ngOnDestroy() {
    this.headerObserver?.disconnect();
  }

  onBreadcrumbClick(node: TreeBreadcrumbNode) {
    this.tree?.scrollToNode(node as N);
  }

  get treeHeight(): number {
    if (this.narrow) {
      return Math.min(480, Math.max(240, Math.round(this.viewportHeight * 0.45)));
    }
    return this.viewportHeight - this.heightOffset - Math.max(0, this.headerHeight - BASE_HEADER_HEIGHT);
  }
}
