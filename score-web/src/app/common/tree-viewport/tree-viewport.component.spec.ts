import {TreeViewportComponent} from './tree-viewport.component';

describe('TreeViewportComponent', () => {

  const key = (name: string, target: Partial<HTMLElement> = {tagName: 'DIV'}) => ({
    key: name, target, preventDefault: vi.fn(), stopPropagation: vi.fn()
  } as any);

  let ctx: any;

  beforeEach(() => {
    ctx = {
      keyboardNavigation: true,
      cursorNode: {id: 'cursor'},
      cursorNodeChange: {emit: vi.fn()},
      nodeClick: {emit: vi.fn()},
      nodeKey: {emit: vi.fn()},
      dataSource: {toggle: vi.fn()},
      searcher: {next: vi.fn(() => ({id: 'next'})), prev: vi.fn(() => ({id: 'prev'}))},
      moveCursor(node: any) {
        this.cursorNode = node;
        this.cursorNodeChange.emit(node);
      }
    };
  });

  it('moves the cursor with the arrow keys', () => {
    const down = key('ArrowDown');
    TreeViewportComponent.prototype.onKeydown.call(ctx, {} as any, down);
    expect(ctx.cursorNodeChange.emit).toHaveBeenCalledWith({id: 'next'});
    expect(down.preventDefault).toHaveBeenCalled();

    TreeViewportComponent.prototype.onKeydown.call(ctx, {} as any, key('ArrowUp'));
    expect(ctx.cursorNodeChange.emit).toHaveBeenCalledWith({id: 'prev'});
  });

  it('toggles the cursor node with left/right and selects it with Enter', () => {
    TreeViewportComponent.prototype.onKeydown.call(ctx, {} as any, key('ArrowRight'));
    expect(ctx.dataSource.toggle).toHaveBeenCalledWith({id: 'cursor'});

    TreeViewportComponent.prototype.onKeydown.call(ctx, {} as any, key('Enter'));
    expect(ctx.nodeClick.emit).toHaveBeenCalledWith({id: 'cursor'});
  });

  it('hands other keys to the host and leaves them unhandled', () => {
    const node: any = {hashPath: 'a'};
    const space = key(' ');
    TreeViewportComponent.prototype.onKeydown.call(ctx, node, space);
    expect(ctx.nodeKey.emit).toHaveBeenCalledWith({node, event: space});
    expect(space.preventDefault).not.toHaveBeenCalled();
  });

  it('does nothing on key presses when keyboard navigation is off', () => {
    ctx.keyboardNavigation = false;
    TreeViewportComponent.prototype.onKeydown.call(ctx, {} as any, key('ArrowDown'));
    expect(ctx.searcher.next).not.toHaveBeenCalled();
    expect(ctx.nodeKey.emit).not.toHaveBeenCalled();
  });

  it('uses the per-node context menu when one is given', () => {
    const own: any = {};
    const shared: any = {};
    expect(TreeViewportComponent.prototype.menuOf.call({contextMenu: shared}, {} as any)).toBe(shared);
    expect(TreeViewportComponent.prototype.menuOf.call({contextMenu: shared, contextMenuFor: () => own}, {} as any)).toBe(own);
  });

  it('ignores keys typed into an input', () => {
    TreeViewportComponent.prototype.onKeydown.call(ctx, {} as any, key('ArrowDown', {tagName: 'INPUT'}));
    expect(ctx.searcher.next).not.toHaveBeenCalled();
    expect(ctx.nodeKey.emit).not.toHaveBeenCalled();
  });

  it('scrolls to the row offset, after the delay when given', () => {
    vi.useFakeTimers();
    try {
      const viewport = {scrollToOffset: vi.fn(), checkViewportSize: vi.fn()};
      const self = {viewport, itemSize: 33} as any;

      TreeViewportComponent.prototype.scrollToIndex.call(self, 2, {delay: 100, smooth: true});
      expect(viewport.scrollToOffset).not.toHaveBeenCalled();
      vi.advanceTimersByTime(100);
      expect(viewport.scrollToOffset).toHaveBeenCalledWith(66, 'smooth');

      // without a delay it still waits for the viewport to render rows that were just revealed
      TreeViewportComponent.prototype.scrollToIndex.call(self, 4);
      expect(viewport.scrollToOffset).toHaveBeenCalledTimes(1);
      vi.advanceTimersByTime(0);
      expect(viewport.scrollToOffset).toHaveBeenLastCalledWith(132, 'auto');

      TreeViewportComponent.prototype.scrollToIndex.call(self, -1);
      vi.advanceTimersByTime(1000);
      expect(viewport.scrollToOffset).toHaveBeenCalledTimes(2);
    } finally {
      vi.useRealTimers();
    }
  });
});
