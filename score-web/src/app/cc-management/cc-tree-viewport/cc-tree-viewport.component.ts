import {CdkDragDrop} from '@angular/cdk/drag-drop';
import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {MatMenuPanel} from '@angular/material/menu';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ScoreCommonModule} from '../../common/score-common.module';
import {TreeViewportComponent, TreeViewportNodeKey} from '../../common/tree-viewport/tree-viewport.component';
import {CcFlatNode, CcFlatNodeDataSource, CcFlatNodeDataSourceSearcher} from '../domain/cc-flat-tree';

/** The node tree of the core component screens (ACC, ASCCP, BCCP, BDT, extension): name, cardinality and tags per row. */
@Component({
  selector: 'score-cc-tree-viewport',
  templateUrl: './cc-tree-viewport.component.html',
  imports: [ScoreCommonModule, MatTooltipModule, TreeViewportComponent]
})
export class CcTreeViewportComponent {

  @Input({required: true}) dataSource: CcFlatNodeDataSource<CcFlatNode>;
  @Input({required: true}) searcher: CcFlatNodeDataSourceSearcher<CcFlatNode>;
  @Input() cursorNode: CcFlatNode | undefined;
  @Input() height: number;
  @Input() contextMenu: MatMenuPanel;
  @Input() isDraggable: (node: CcFlatNode) => boolean = () => false;
  @Input() showDragHandle: (node: CcFlatNode) => boolean = () => false;
  @Input() canDrop: () => boolean = () => true;

  @Output() cursorNodeChange = new EventEmitter<CcFlatNode>();
  @Output() nodeClick = new EventEmitter<CcFlatNode>();
  @Output() nodeKey = new EventEmitter<TreeViewportNodeKey<CcFlatNode>>();
  @Output() copyLink = new EventEmitter<CcFlatNode>();
  @Output() contextMenuOpen = new EventEmitter<CcFlatNode>();
  @Output() dropped = new EventEmitter<CdkDragDrop<any>>();

  @ViewChild(TreeViewportComponent, {static: true}) tree: TreeViewportComponent<CcFlatNode>;

  scrollToIndex(index: number, options?: { delay?: number, smooth?: boolean }) {
    this.tree.scrollToIndex(index, options);
  }
}
