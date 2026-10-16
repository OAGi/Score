import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatMenuPanel} from '@angular/material/menu';
import {MatTooltipModule} from '@angular/material/tooltip';
import {FaIconComponent} from '@fortawesome/angular-fontawesome';
import {faCircleExclamation, faRecycle} from '@fortawesome/free-solid-svg-icons';
import {ScoreCommonModule} from '../../../common/score-common.module';
import {TreeViewportComponent} from '../../../common/tree-viewport/tree-viewport.component';
import {BieFlatNode, BieFlatNodeDataSource, BieFlatNodeDataSourceSearcher} from '../../domain/bie-flat-tree';
import {BieUpliftSourceFlatNode, BieUpliftTargetFlatNode} from '../domain/bie-uplift';

/** What the uplift rows (mapping checkbox, reused BIE link) need from the screen that owns the trees. */
export interface BieUpliftTreeRowHost {
  isSourceNodeCheckable(node: BieUpliftSourceFlatNode): boolean;

  isTargetNodeCheckable(node: BieUpliftTargetFlatNode): boolean;

  canMatch(node: BieUpliftTargetFlatNode): boolean;

  getMatchDisabledReason(node: BieUpliftTargetFlatNode): string;

  checkMatch(event: any, node: BieUpliftTargetFlatNode): void;

  matchReused(node: BieUpliftTargetFlatNode): void;
}

/**
 * One of the two node trees of the BIE uplift verification: the source BIE (mapped nodes are shown checked) or the
 * target BIE (where the user maps nodes). Rows are click-to-select; there is no keyboard navigation or copy link.
 */
@Component({
  selector: 'score-bie-uplift-tree-viewport',
  templateUrl: './bie-uplift-tree-viewport.component.html',
  styleUrl: './bie-uplift-tree-viewport.component.css',
  imports: [ScoreCommonModule, MatCheckboxModule, MatTooltipModule, FaIconComponent, TreeViewportComponent]
})
export class BieUpliftTreeViewportComponent<N extends BieFlatNode = BieFlatNode> {

  @Input({required: true}) side: 'source' | 'target';
  @Input({required: true}) dataSource: BieFlatNodeDataSource<N>;
  @Input({required: true}) searcher: BieFlatNodeDataSourceSearcher<N>;
  @Input({required: true}) host: BieUpliftTreeRowHost;
  @Input() selectedNode: N | undefined;
  @Input() height: number;
  @Input() contextMenu: MatMenuPanel;

  @Output() nodeClick = new EventEmitter<N>();
  @Output() contextMenuOpen = new EventEmitter<N>();

  @ViewChild(TreeViewportComponent, {static: true}) tree: TreeViewportComponent<N>;

  faRecycle = faRecycle;
  faCircleExclamation = faCircleExclamation;

  /** Only a node that has children has a context menu. */
  readonly menuOf = (node: N) => this.dataSource.isExpandable(node) ? this.contextMenu : null;

  scrollToIndex(index: number, options?: { delay?: number, smooth?: boolean }) {
    this.tree.scrollToIndex(index, options);
  }

  scrollToNode(node: N, delay?: number) {
    this.tree.scrollToNode(node, delay);
  }
}
