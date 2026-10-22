import {BIE_SEARCH_ATTRIBUTES, COMPONENT_SEARCH_ATTRIBUTES, MODEL_BROWSER_SEARCH_ATTRIBUTES, SearchAttribute, suggestSearch} from './search-suggest';

const ATTRIBUTES: SearchAttribute[] = [
  {name: 'displayName', kind: 'text', description: 'name'},
  {name: 'type', kind: 'text', description: 'type', values: ['ABIE', 'BBIEP']},
  {name: 'used', kind: 'flag', description: 'used'}
];

function labels(expression: string, caret = expression.length) {
  return suggestSearch(expression, caret, ATTRIBUTES).items.map(i => i.label);
}

describe('suggestSearch', () => {

  it('offers a wildcard at the start of a step', () => {
    expect(labels('')[0]).toBe('*');
    expect(labels('a/').slice(0, 2)).toEqual(['*', '*[']);
  });

  it('offers examples while the field is empty', () => {
    expect(labels('')).toEqual(['*', '*[used = true]', '*[contains(text(), "id")]', '*[parent::*]', '*[2]']);
  });

  it('offers to open a predicate after a name', () => {
    expect(labels('Party')).toEqual(['[', '/', '//']);
  });

  it('offers attributes, functions and not at the start of a predicate', () => {
    expect(labels('*[').slice(0, 7)).toEqual(['displayName', 'type', 'used', 'contains', 'starts-with', 'ends-with', 'text()']);
    expect(labels('*[')).toEqual(expect.arrayContaining(['parent::', 'ancestor::', '..', 'last()', 'count(', 'not']));
  });

  it('filters by the typed prefix and replaces just the word', () => {
    const s = suggestSearch('a/*[us', 6, ATTRIBUTES);
    expect(s.items.map(i => i.label)).toEqual(['used']);
    expect([s.from, s.to]).toEqual([4, 6]);
  });

  it('offers comparisons after a text attribute and only equality after a flag', () => {
    expect(labels('*[type ')).toContain('<=');
    expect(labels('*[used ')).not.toContain('<=');
    expect(labels('*[used ')).toEqual(expect.arrayContaining(['=', '!=', 'and', 'or', ']']));
  });

  it('offers the values of a flag and of an enumerated attribute', () => {
    expect(labels('*[used = ')).toEqual(['true', 'false']);
    const type = suggestSearch('*[type = ', 9, ATTRIBUTES).items;
    expect(type.map(i => i.insert)).toEqual(['"ABIE" ', '"BBIEP" ']);
  });

  it('completes an enumerated value inside its quotes without adding quotes', () => {
    const s = suggestSearch('*[type = "AB', 12, ATTRIBUTES);
    expect(s.items.map(i => i.insert)).toEqual(['ABIE"']);
  });

  it('offers nothing inside a quoted text of a free attribute', () => {
    expect(labels('*[contains(displayName, "i')).toEqual([]);
  });

  it('offers and/or/] after a complete comparison, and attributes after and', () => {
    expect(labels('*[used = true ')).toEqual(['and', 'or', ']']);
    expect(labels('*[used = true and ')).toContain('displayName');
  });

  it('goes on to and/or/] after a flag value is chosen', () => {
    expect(labels('*[used = true ')).toEqual(['and', 'or', ']']);
  });

  it('offers / or [ after a closed predicate', () => {
    expect(labels('*[used]')).toEqual(['/', '//', '[']);
  });

  it('judges the text before the caret only', () => {
    expect(labels('*[used = true]', 2)).toContain('displayName');
  });

  it('offers the component and entity types of the trees that have them', () => {
    const values = (attributes: SearchAttribute[], text: string) =>
      suggestSearch(text, text.length, attributes).items.map(i => i.label);
    expect(values(COMPONENT_SEARCH_ATTRIBUTES, '*[entity_type = ')).toEqual(['Element', 'Attribute']);
    expect(values(COMPONENT_SEARCH_ATTRIBUTES, '*[component_type = ')).toContain('Extension');
    expect(values(COMPONENT_SEARCH_ATTRIBUTES, '*[type = ')).toContain('DT');
  });

  it('types the model browser like a BIE and a component tree like a component', () => {
    const types = (attributes: SearchAttribute[]) => suggestSearch('*[type = ', 9, attributes).items.map(i => i.label);
    expect(types(MODEL_BROWSER_SEARCH_ATTRIBUTES)).toEqual(['ABIE', 'ASBIEP', 'BBIEP', 'BBIE_SC']);
    expect(types(BIE_SEARCH_ATTRIBUTES)).toEqual(['ABIE', 'ASBIEP', 'BBIEP', 'BBIE_SC']);
    expect(types(COMPONENT_SEARCH_ATTRIBUTES)).toEqual(['ACC', 'ASCCP', 'BCCP', 'DT', 'DT_SC']);
  });

  it('offers the axes after a / and the names after an axis', () => {
    expect(labels('Body/')).toEqual(expect.arrayContaining(['parent::', 'following-sibling::', '..']));
    expect(labels('Body/parent::')).toEqual(['*']);
    expect(labels('*[child::')).toEqual(['*']);
    expect(labels('*[child::*/')).toEqual(expect.arrayContaining(['*', 'descendant::']));
    expect(labels('*[child::*[')).toContain('type');
  });

  it('offers the cardinalities without quotes', () => {
    const items = suggestSearch('*[cardinality = ', 16, COMPONENT_SEARCH_ATTRIBUTES).items;
    expect(items.map(i => i.insert)).toEqual(['Optional ', 'Required ', 'Prohibited ', '0..1 ', '1..1 ', '0..* ', '1..* ']);
    expect(suggestSearch('*[cardinality_max ', 18, COMPONENT_SEARCH_ATTRIBUTES).items.map(i => i.label)).toEqual(expect.arrayContaining(['<', '>=']));
  });
});
