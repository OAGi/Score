import {CdkDragDrop} from '@angular/cdk/drag-drop';
import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {MatMenuPanel} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ScoreCommonModule} from '../../common/score-common.module';
import {TreeViewportComponent, TreeViewportNodeKey} from '../../common/tree-viewport/tree-viewport.component';
import {CcFlatNode, CcFlatNodeDataSource, CcFlatNodeDataSourceSearcher} from '../domain/cc-flat-tree';

export interface ValidationMarker {
  severity: 'ERROR' | 'WARNING' | 'INFO';
  stale: boolean;
  messages: string[];
}

/** The node tree of the core component screens (ACC, ASCCP, BCCP, BDT, extension): name, cardinality and tags per row. */
@Component({
  selector: 'score-cc-tree-viewport',
  templateUrl: './cc-tree-viewport.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [ScoreCommonModule, MatIconModule, MatTooltipModule, TreeViewportComponent]
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
  /** Whether a node is flagged with the invalid state icon (a BDT whose base BDT is deleted). */
  @Input() isInvalidState: (node: CcFlatNode) => boolean = () => false;
  /** The most serious problem a validation found with a node, flagged with an icon of that severity; dimmed when the page has changed since. */
  @Input() validationMarker: (node: CcFlatNode) => ValidationMarker | undefined = () => undefined;

  /** How many problems a closed validation console holds; the root row shows a button that opens it while there are any. */
  @Input() validationCount = 0;
  @Input() validationHasErrors = false;
  @Output() validationShow = new EventEmitter<void>();
  /** A validation marker of a node was clicked. */
  @Output() validationMarkerClick = new EventEmitter<CcFlatNode>();

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

  scrollToNode(node: CcFlatNode, delay?: number) {
    this.tree.scrollToNode(node, delay);
  }
}
