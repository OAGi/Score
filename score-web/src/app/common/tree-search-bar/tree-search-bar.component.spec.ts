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
      fittingPanelWidth: (TreeSearchBarComponent.prototype as any).fittingPanelWidth,
      refreshPanelWidth: (TreeSearchBarComponent.prototype as any).refreshPanelWidth,
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

  it('puts the caret between the parentheses of a function that takes arguments, and after one that does not', () => {
    const attribute = {name: 'den', kind: 'text', description: 'den'};
    const ctx = bar({
      searcher: {searchAttributes: [attribute], isSearching: false},
      searchInput: {nativeElement: {value: '*[', selectionStart: 2}},
      host: {nativeElement: {getBoundingClientRect: () => ({width: 300})}},
      suggestions: []
    });

    TreeSearchBarComponent.prototype.updateSuggestions.call(ctx);

    const caretAfter = (label: string) => ctx.suggestions.find((s: any) => s.suggestion.label === label);
    expect(caretAfter('count()')).toMatchObject({expression: '*[count()', caret: '*[count('.length});
    expect(caretAfter('text()')).toMatchObject({expression: '*[text()', caret: '*[text()'.length});
  });

  it('makes the suggestion panel as wide as the search bar', () => {
    const widthOf = (width: number) => {
      const ctx = bar({
        searcher: {searchAttributes: [{name: 'used', kind: 'flag', description: 'used'}], isSearching: false},
        searchInput: {nativeElement: {value: '*[', selectionStart: 2}},
        host: {nativeElement: {getBoundingClientRect: () => ({width})}},
        suggestions: []
      });
      TreeSearchBarComponent.prototype.updateSuggestions.call(ctx);
      return ctx.panelWidth;
    };

    expect(widthOf(300)).toBe('300px');
    expect(widthOf(299.6)).toBe('299px');
    expect(widthOf(480)).toBe('480px');
    expect(widthOf(900)).toBe('900px');
  });

  it('gives the open autocomplete the width of the bar again when the window is resized', () => {
    let width = 700;
    const autocomplete = {autocomplete: {panelWidth: '480px'}};
    const ctx = bar({host: {nativeElement: {getBoundingClientRect: () => ({width})}}, autocomplete});

    TreeSearchBarComponent.prototype.refreshPanelWidth.call(ctx);
    expect(autocomplete.autocomplete.panelWidth).toBe('700px');

    width = 366;
    TreeSearchBarComponent.prototype.refreshPanelWidth.call(ctx);
    expect(autocomplete.autocomplete.panelWidth).toBe('366px');
    expect(ctx.panelWidth).toBe('366px');
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
