import {Component, EventEmitter, HostListener, Input, Output, ViewEncapsulation} from '@angular/core';
import {MatCardModule} from '@angular/material/card';
import {AngularSplitModule} from 'angular-split';
import {TreeSearcher, TreeSearchBarComponent, TreeSearchRequest} from '../tree-search-bar/tree-search-bar.component';
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
  @Input() searcher: TreeSearcher | undefined;
  @Input() selectedNode: N | undefined | null;
  /** Space (px) taken by everything but the tree viewport. */
  @Input() heightOffset = 180;
  /** Let the split scroll vertically (BIE edit). */
  @Input() splitScroll = false;

  @Output() searchRequest = new EventEmitter<TreeSearchRequest>();
  @Output() moveRequest = new EventEmitter<number>();
  @Output() breadcrumbClick = new EventEmitter<N>();

  private viewportHeight = window.innerHeight;

  @HostListener('window:resize')
  onResize() {
    this.viewportHeight = window.innerHeight;
  }

  onBreadcrumbClick(node: TreeBreadcrumbNode) {
    this.breadcrumbClick.emit(node as N);
  }

  get treeHeight(): number {
    return this.viewportHeight - this.heightOffset;
  }
}
