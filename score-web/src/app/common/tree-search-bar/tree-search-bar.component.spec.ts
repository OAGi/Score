import {vi} from 'vitest';
import {of} from 'rxjs';
import {TreeSearchBarComponent} from './tree-search-bar.component';

describe('TreeSearchBarComponent', () => {
  function bar(overrides: any = {}): any {
    return {
      searcher: {search: vi.fn().mockReturnValue(of(12)), go: vi.fn().mockReturnValue(of(30)), nodeAt: vi.fn().mockReturnValue('node')},
      tree: {scrollToIndex: vi.fn()},
      scope: 'selected',
      selectMatch: (TreeSearchBarComponent.prototype as any).selectMatch,
      nodeSelect: {emit: vi.fn()},
      hideSuggestions: vi.fn(),
      ...overrides
    };
  }

  it('searches below the node the user picked and scrolls the first match into view after the tree has rendered', () => {
    const ctx = bar();

    TreeSearchBarComponent.prototype.onSearch.call(ctx, 'Release', true, true);

    expect(ctx.searcher.search).toHaveBeenCalledWith('Release', 'selected', true, true);
    expect(ctx.tree.scrollToIndex).toHaveBeenCalledWith(12, {delay: 500, smooth: true});
  });

  it('selects the first match so the breadcrumbs follow it, and selects nothing when there is no match', () => {
    const ctx = bar();
    TreeSearchBarComponent.prototype.onSearch.call(ctx, 'Release');
    expect(ctx.searcher.nodeAt).toHaveBeenCalledWith(12);
    expect(ctx.nodeSelect.emit).toHaveBeenCalledWith('node');

    const none = bar({searcher: {search: vi.fn().mockReturnValue(of(-1)), nodeAt: vi.fn()}});
    TreeSearchBarComponent.prototype.onSearch.call(none, 'Nothing');
    expect(none.nodeSelect.emit).not.toHaveBeenCalled();
  });

  it('puts the suggestions away when it searches', () => {
    const ctx = bar();

    TreeSearchBarComponent.prototype.onSearch.call(ctx, 'Release');

    expect(ctx.hideSuggestions).toHaveBeenCalled();
  });

  it('searches the whole tree when nothing is selected', () => {
    const ctx = bar({scope: undefined});

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

  describe('the search scope', () => {
    const setSelectedNode = Object.getOwnPropertyDescriptor(TreeSearchBarComponent.prototype, 'selectedNode')!.set!;

    it('follows the node the user picks but not the match the search selected', () => {
      const ctx = bar({scope: undefined, selectedByMatch: undefined});
      setSelectedNode.call(ctx, 'root');
      expect(ctx.scope).toBe('root');

      TreeSearchBarComponent.prototype.onSearch.call(ctx, 'Item/Party');
      setSelectedNode.call(ctx, 'node'); // the screen shows the match the search selected
      expect(ctx.scope).toBe('root');

      TreeSearchBarComponent.prototype.onMove.call(ctx, 1);
      setSelectedNode.call(ctx, 'node');
      expect(ctx.scope).toBe('root');

      setSelectedNode.call(ctx, 'clicked'); // the user picks another node
      expect(ctx.scope).toBe('clicked');
    });

    it('names the node it is scoped to', () => {
      const label = Object.getOwnPropertyDescriptor(TreeSearchBarComponent.prototype, 'scopeLabel')!.get!;
      expect(label.call({scope: {displayName: 'Party', name: 'party'}})).toBe('Party');
      expect(label.call({scope: {name: 'party'}})).toBe('party');
      expect(label.call({scope: undefined})).toBeUndefined();
    });
  });
});
