import {CdkDragDrop, CdkDragMove} from '@angular/cdk/drag-drop';
import {Component, EventEmitter, Input, Output, ViewChild, ViewEncapsulation} from '@angular/core';
import {MatMenuPanel} from '@angular/material/menu';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ScoreCommonModule} from '../../../common/score-common.module';
import {TreeViewportComponent, TreeViewportNodeKey} from '../../../common/tree-viewport/tree-viewport.component';
import {
  ModelBrowserNode,
  ModelBrowserNodeDataSource,
  ModelBrowserNodeDataSourceSearcher
} from '../domain/model-browser-node';

export interface ChoiceGuide {
  offset: number;
  continuation: boolean;
  first: boolean;
  last: boolean;
  only: boolean;
  tooltip: string | null;
}

/** What the model browser rows (choice guides, order weight badge, drag mode) need from the screen. */
export interface ModelBrowserTreeRowHost {
  /** A node is being dragged. */
  dragging: boolean;

  choiceGuides(node: ModelBrowserNode): ChoiceGuide[];

  currentWeightOf(node: ModelBrowserNode): number | undefined | null;
}

/** The node tree of the model browser: choice branch guides, name, cardinality and order weight per row. */
@Component({
  selector: 'score-model-browser-tree-viewport',
  templateUrl: './model-browser-tree-viewport.component.html',
  styleUrl: './model-browser-tree-viewport.component.css',
  encapsulation: ViewEncapsulation.None,
  imports: [ScoreCommonModule, MatTooltipModule, TreeViewportComponent]
})
export class ModelBrowserTreeViewportComponent {

  @Input({required: true}) dataSource: ModelBrowserNodeDataSource<ModelBrowserNode>;
  @Input({required: true}) searcher: ModelBrowserNodeDataSourceSearcher<ModelBrowserNode>;
  @Input({required: true}) host: ModelBrowserTreeRowHost;
  @Input() cursorNode: ModelBrowserNode | undefined;
  @Input() height: number;
  /** Menu of a node that has children, and of a leaf node. */
  @Input() contextMenu: MatMenuPanel;
  @Input() leafContextMenu: MatMenuPanel;
  @Input() isDraggable: (node: ModelBrowserNode) => boolean = () => false;
  @Input() canDrop: () => boolean = () => true;
  @Input() dropInvalid = false;

  @Output() cursorNodeChange = new EventEmitter<ModelBrowserNode>();
  @Output() nodeClick = new EventEmitter<ModelBrowserNode>();
  @Output() nodeKey = new EventEmitter<TreeViewportNodeKey<ModelBrowserNode>>();
  @Output() copyLink = new EventEmitter<ModelBrowserNode>();
  @Output() contextMenuOpen = new EventEmitter<ModelBrowserNode>();
  @Output() dropped = new EventEmitter<CdkDragDrop<any>>();
  @Output() dragStarted = new EventEmitter<ModelBrowserNode>();
  @Output() dragMoved = new EventEmitter<CdkDragMove>();
  @Output() dragEnded = new EventEmitter<void>();

  @ViewChild(TreeViewportComponent, {static: true}) tree: TreeViewportComponent<ModelBrowserNode>;

  readonly menuOf = (node: ModelBrowserNode) =>
    this.dataSource.isExpandable(node) ? this.contextMenu : this.leafContextMenu;
  /** The handle only shows while hovering a row, and never during a drag. */
  readonly isDragHandleHidden = (node: ModelBrowserNode) => !(node.showCopyLinkIcon && !this.host.dragging);

  get viewportElement(): HTMLElement | null {
    return this.tree.viewport?.elementRef?.nativeElement ?? null;
  }

  scrollToIndex(index: number, options?: { delay?: number, smooth?: boolean }) {
    this.tree.scrollToIndex(index, options);
  }
}
