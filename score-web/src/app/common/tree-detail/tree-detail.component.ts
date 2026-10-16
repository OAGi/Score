import {Component, EventEmitter, HostListener, Input, Output, ViewEncapsulation} from '@angular/core';
import {MatCardModule} from '@angular/material/card';
import {AngularSplitModule} from 'angular-split';
import {TreeScrollTarget, TreeSearcher, TreeSearchBarComponent} from '../tree-search-bar/tree-search-bar.component';
import {TreeBreadcrumbNode, TreeBreadcrumbsComponent} from '../tree-breadcrumbs/tree-breadcrumbs.component';

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
  styleUrl: './tree-detail.component.css',
  encapsulation: ViewEncapsulation.None,
  imports: [MatCardModule, AngularSplitModule, TreeSearchBarComponent, TreeBreadcrumbsComponent]
})
export class TreeDetailComponent<N extends TreeDetailNode = TreeDetailNode> {

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

  @HostListener('window:resize')
  onResize() {
    this.viewportHeight = window.innerHeight;
  }

  onBreadcrumbClick(node: TreeBreadcrumbNode) {
    this.tree?.scrollToNode(node as N);
  }

  get treeHeight(): number {
    return this.viewportHeight - this.heightOffset;
  }
}
