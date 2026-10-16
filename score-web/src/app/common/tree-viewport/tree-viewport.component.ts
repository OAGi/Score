import {CdkDragDrop, CdkDragMove, DragDropModule} from '@angular/cdk/drag-drop';
import {CdkVirtualScrollViewport, ScrollingModule} from '@angular/cdk/scrolling';
import {DataSource} from '@angular/cdk/collections';
import {NgTemplateOutlet} from '@angular/common';
import {
  Component,
  ContentChild,
  EventEmitter,
  Input,
  Output,
  QueryList,
  TemplateRef,
  ViewChild,
  ViewChildren,
  ViewEncapsulation
} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatMenuModule, MatMenuPanel, MatMenuTrigger} from '@angular/material/menu';
import {MatTooltipModule} from '@angular/material/tooltip';

/** What a tree row needs from a node. */
export interface TreeViewportNode {
  level: number;
  hashPath: string;
  path?: string;
  queryPath?: string;
  isCycle?: boolean;
  showCopyLinkIcon?: boolean;
}

export interface TreeViewportDataSource<N> extends DataSource<N> {
  isExpandable(node: N): boolean;

  isExpanded(node: N): boolean;

  toggle(node: N): void;
}

/** What the row keyboard navigation needs from a tree searcher (it owns the visible order of the nodes). */
export interface TreeViewportSearcher<N> {
  next(node: N): N;

  prev(node: N): N;
}

export interface TreeViewportNodeKey<N> {
  node: N;
  event: KeyboardEvent;
}

/**
 * The node tree of a tree-detail screen: a virtual-scrolled list of rows, each with an expand toggle, a content
 * area, the copy-link / context-menu actions and, when enabled, drag-and-drop reordering.
 *
 * The row content comes from the template named `content` (context: the node); a template named `lead` renders
 * before the expand toggle (e.g. guide lines) and `prefix` between the toggle and the content (e.g. a checkbox). Use a derivative (`score-cc-tree-viewport`,
 * `score-bie-tree-viewport`) rather than this component directly.
 *
 * Keyboard (row focused): arrows move the cursor / expand / collapse, Enter selects, `o` opens the context menu;
 * any other key is emitted as `nodeKey` for the host to handle.
 */
@Component({
  selector: 'score-tree-viewport',
  templateUrl: './tree-viewport.component.html',
  styleUrl: './tree-viewport.component.css',
  encapsulation: ViewEncapsulation.None,
  imports: [ScrollingModule, DragDropModule, NgTemplateOutlet, MatButtonModule, MatIconModule, MatMenuModule,
    MatTooltipModule]
})
export class TreeViewportComponent<N extends TreeViewportNode = TreeViewportNode> {

  @Input({required: true}) dataSource: TreeViewportDataSource<N>;
  @Input() searcher: TreeViewportSearcher<N>;
  @Input() cursorNode: N | undefined;
  @Input() height: number;
  @Input() itemSize = 33;
  @Input() paddingPixel = 12;
  @Input() contextMenu: MatMenuPanel;
  /** The context menu of a node, when it differs per node; a node without one has no context-menu action. */
  @Input() contextMenuFor: (node: N) => MatMenuPanel | null;
  @Input() showCopyLink = true;
  /** Arrow keys, Enter and `o` act on the cursor node; turn off for a tree that only reacts to clicks. */
  @Input() keyboardNavigation = true;
  /** Rows for which drag-and-drop is enabled; without it nothing is draggable. */
  @Input() isDraggable: (node: N) => boolean = () => false;
  @Input() showDragHandle: (node: N) => boolean = () => false;
  @Input() canDrop: () => boolean = () => true;
  /** The drop slot is highlighted as invalid while dragging. */
  @Input() dropInvalid = false;
  @Input() isDragHandleHidden: (node: N) => boolean = () => false;
  /** A click on the drag handle does not select the row. */
  @Input() dragHandleStopsClick = false;
  /** Rows whose copy-link / context-menu actions are hidden (e.g. while the row is being renamed). */
  @Input() isActionsHidden: (node: N) => boolean = () => false;

  @Output() cursorNodeChange = new EventEmitter<N>();
  @Output() nodeClick = new EventEmitter<N>();
  @Output() nodeKey = new EventEmitter<TreeViewportNodeKey<N>>();
  @Output() copyLink = new EventEmitter<N>();
  /** The context menu of a node is about to open. */
  @Output() contextMenuOpen = new EventEmitter<N>();
  @Output() dropped = new EventEmitter<CdkDragDrop<any>>();
  @Output() dragStarted = new EventEmitter<N>();
  @Output() dragMoved = new EventEmitter<CdkDragMove>();
  @Output() dragEnded = new EventEmitter<void>();

  @ContentChild('lead', {read: TemplateRef}) lead: TemplateRef<{ $implicit: N }>;
  @ContentChild('content', {read: TemplateRef}) content: TemplateRef<{ $implicit: N }>;
  @ContentChild('prefix', {read: TemplateRef}) prefix: TemplateRef<{ $implicit: N }>;

  @ViewChild(CdkVirtualScrollViewport, {static: true}) viewport: CdkVirtualScrollViewport;
  @ViewChildren(MatMenuTrigger) private menuTriggers: QueryList<MatMenuTrigger>;

  get minBufferPx(): number {
    return 10000 * this.itemSize;
  }

  get maxBufferPx(): number {
    return 1000000 * this.itemSize;
  }

  onClick(node: N, event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.nodeClick.emit(node);
  }

  toggle(node: N, event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.dataSource.toggle(node);
  }

  onCopyLink(node: N, event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.copyLink.emit(node);
  }

  onContextMenuOpen(node: N, event: MouseEvent) {
    event.stopPropagation();
    this.contextMenuOpen.emit(node);
  }

  menuOf(node: N): MatMenuPanel | null {
    return this.contextMenuFor ? this.contextMenuFor(node) : this.contextMenu;
  }

  onKeydown(node: N, event: KeyboardEvent) {
    if (!this.keyboardNavigation) {
      return;
    }

    const target = event.target as HTMLElement;
    if (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable) {
      return;
    }

    if (event.key === 'ArrowDown') {
      this.moveCursor(this.searcher.next(this.cursorNode));
    } else if (event.key === 'ArrowUp') {
      this.moveCursor(this.searcher.prev(this.cursorNode));
    } else if (event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
      this.dataSource.toggle(this.cursorNode);
    } else if (event.key === 'Enter') {
      this.nodeClick.emit(this.cursorNode);
    } else if (event.key === 'o' || event.key === 'O') {
      this.menuTriggers.filter(e => e.menuData?.menuId === 'contextMenu' && e.menuData.hashPath === node.hashPath)
        .forEach(trigger => {
          this.contextMenuOpen.emit(node);
          if (!trigger.menuOpen) {
            trigger.openMenu();
          }
        });
    } else {
      this.nodeKey.emit({node, event});
      return;
    }
    event.preventDefault();
    event.stopPropagation();
  }

  private moveCursor(node: N) {
    this.cursorNode = node;
    this.cursorNodeChange.emit(node);
  }

  /** Scroll so that the row at `index` is the first visible one. */
  scrollToIndex(index: number, options: { delay?: number, smooth?: boolean } = {}) {
    if (index < 0) {
      return;
    }

    const scroll = () => {
      this.viewport.scrollToOffset(index * this.itemSize, options.smooth ? 'smooth' : 'auto');
      this.viewport.checkViewportSize();
    };
    if (options.delay) {
      setTimeout(scroll, options.delay);
    } else {
      scroll();
    }
  }
}
