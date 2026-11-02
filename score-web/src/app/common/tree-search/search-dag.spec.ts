import {sameSearchDag, SearchDag, SearchDagBuilder} from './search-dag';

describe('sameSearchDag', () => {
  function dag(options: {name?: string, definition?: string, children?: number[], extraAttribute?: boolean} = {}): SearchDag {
    const builder = new SearchDagBuilder();
    builder.record('root', ['Root'], 'root', {definition: 'Of the root'});
    builder.record('item', [options.name ?? 'Item'], 'item', {definition: options.definition ?? '', ...(options.extraAttribute ? {used: true} : {})});
    builder.record('other', ['Other'], 'other', {definition: ''});
    builder.setChildren(0, options.children ?? [1, 2]);
    return builder.build();
  }

  it('takes DAGs that were built from the same tree for the same', () => {
    expect(sameSearchDag(dag(), dag())).toBe(true);
  });

  it('tells a changed value, name, order or attribute apart', () => {
    expect(sameSearchDag(dag(), dag({definition: 'New text'}))).toBe(false);
    expect(sameSearchDag(dag(), dag({name: 'Renamed'}))).toBe(false);
    expect(sameSearchDag(dag(), dag({children: [2, 1]}))).toBe(false);
    expect(sameSearchDag(dag(), dag({children: [1]}))).toBe(false);
    expect(sameSearchDag(dag(), dag({extraAttribute: true}))).toBe(false);
  });

  it('tells a value that is there from one that is not', () => {
    const builder = new SearchDagBuilder();
    builder.record('root', ['Root'], 'root', {});
    const without = builder.build();
    const withValue = new SearchDagBuilder();
    withValue.record('root', ['Root'], 'root', {definition: ''});

    expect(sameSearchDag(without, withValue.build())).toBe(false);
  });
});
