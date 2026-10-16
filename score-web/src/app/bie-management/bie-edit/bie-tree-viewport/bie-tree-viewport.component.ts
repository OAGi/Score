import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatCheckbox, MatCheckboxModule} from '@angular/material/checkbox';
import {MatInputModule} from '@angular/material/input';
import {MatMenuPanel} from '@angular/material/menu';
import {MatTooltipModule} from '@angular/material/tooltip';
import {FaIconComponent} from '@fortawesome/angular-fontawesome';
import {faRecycle, faSitemap} from '@fortawesome/free-solid-svg-icons';
import {ScoreCommonModule} from '../../../common/score-common.module';
import {TreeViewportComponent, TreeViewportNodeKey} from '../../../common/tree-viewport/tree-viewport.component';
import {BieFlatNode, BieFlatNodeDataSource, BieFlatNodeDataSourceSearcher} from '../../domain/bie-flat-tree';

/** What the BIE rows (Used checkbox, display name editing, reused-BIE link) need from the screen that owns the tree. */
export interface BieTreeRowHost {
  renamingNode: BieFlatNode | undefined;

  used(node: BieFlatNode): boolean;

  isUsable(node: BieFlatNode): boolean;

  getNodeTooltip(node: BieFlatNode): string;

  toggleTreeUsed(node: BieFlatNode, event?: MouseEvent, checkbox?: MatCheckbox): void;

  startChangingDisplayName(node: BieFlatNode, event?: MouseEvent): void;

  stopChangingDisplayName(revert?: boolean): void;

  openNewEditBieTab(node: BieFlatNode, event?: MouseEvent): void;
}

/** The node tree of the BIE edit screen: Used checkbox, display name (editable in place), cardinality and status icons. */
@Component({
  selector: 'score-bie-tree-viewport',
  templateUrl: './bie-tree-viewport.component.html',
  imports: [ScoreCommonModule, FormsModule, MatCheckboxModule, MatInputModule, MatTooltipModule, FaIconComponent,
    TreeViewportComponent]
})
export class BieTreeViewportComponent {

  @Input({required: true}) dataSource: BieFlatNodeDataSource<BieFlatNode>;
  @Input({required: true}) searcher: BieFlatNodeDataSourceSearcher<BieFlatNode>;
  @Input({required: true}) host: BieTreeRowHost;
  @Input() cursorNode: BieFlatNode | undefined;
  @Input() height: number;
  @Input() contextMenu: MatMenuPanel;

  @Output() cursorNodeChange = new EventEmitter<BieFlatNode>();
  @Output() nodeClick = new EventEmitter<BieFlatNode>();
  @Output() nodeKey = new EventEmitter<TreeViewportNodeKey<BieFlatNode>>();
  @Output() copyLink = new EventEmitter<BieFlatNode>();
  @Output() contextMenuOpen = new EventEmitter<BieFlatNode>();

  @ViewChild(TreeViewportComponent, {static: true}) tree: TreeViewportComponent<BieFlatNode>;

  faRecycle = faRecycle;
  faSitemap = faSitemap;

  readonly isActionsHidden = (node: BieFlatNode) => this.host?.renamingNode === node;

  scrollToIndex(index: number, options?: { delay?: number, smooth?: boolean }) {
    this.tree.scrollToIndex(index, options);
  }

  scrollToNode(node: BieFlatNode, delay?: number) {
    this.tree.scrollToNode(node, delay);
  }
}
