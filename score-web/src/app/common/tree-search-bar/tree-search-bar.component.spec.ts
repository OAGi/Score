import {vi} from 'vitest';
import {of} from 'rxjs';
import {TreeSearchBarComponent} from './tree-search-bar.component';

describe('TreeSearchBarComponent', () => {
  function bar(overrides: any = {}): any {
    return {
      searcher: {search: vi.fn().mockReturnValue(of(12)), go: vi.fn().mockReturnValue(of(30)), nodeAt: vi.fn().mockReturnValue('node')},
      tree: {scrollToIndex: vi.fn()},
      selectedNode: 'selected',
      nodeSelect: {emit: vi.fn()},
      ...overrides
    };
  }

  it('searches below the selected node and scrolls the first match into view after the tree has rendered', () => {
    const ctx = bar();

    TreeSearchBarComponent.prototype.onSearch.call(ctx, 'Release', true, true);

    expect(ctx.searcher.search).toHaveBeenCalledWith('Release', 'selected', true, true);
    expect(ctx.tree.scrollToIndex).toHaveBeenCalledWith(12, {delay: 500, smooth: true});
  });

  it('searches the whole tree when nothing is selected', () => {
    const ctx = bar({selectedNode: null});

    TreeSearchBarComponent.prototype.onSearch.call(ctx, 'Release');

    expect(ctx.searcher.search).toHaveBeenCalledWith('Release', undefined, false, false);
  });

  it('selects the match it steps to and scrolls to it smoothly', () => {
    const ctx = bar();

    TreeSearchBarComponent.prototype.onMove.call(ctx, -1);

    expect(ctx.searcher.go).toHaveBeenCalledWith(-1);
    expect(ctx.nodeSelect.emit).toHaveBeenCalledWith('node');
    expect(ctx.searcher.nodeAt).toHaveBeenCalledWith(30);
    expect(ctx.tree.scrollToIndex).toHaveBeenCalledWith(30, {smooth: true});
  });

  it('still selects when the screen gave no tree to scroll', () => {
    const ctx = bar({tree: undefined});

    TreeSearchBarComponent.prototype.onMove.call(ctx, 1);

    expect(ctx.nodeSelect.emit).toHaveBeenCalledWith('node');
  });
});
