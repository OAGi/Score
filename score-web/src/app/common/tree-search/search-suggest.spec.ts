import {predicateTail} from './search-grammar';
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
    expect(labels('')).toEqual(['*', '*[used = true]', '*[contains(text(), "id")]', '*[../Code]', '*[2]']);
  });

  it('offers to open a predicate after a name', () => {
    expect(labels('Party')).toEqual(['[', '/', '//']);
  });

  it('offers attributes, functions and not at the start of a predicate', () => {
    expect(labels('*[').slice(0, 7)).toEqual(['displayName', 'type', 'used', 'contains()', 'starts-with()', 'ends-with()', 'text()']);
    expect(labels('*[')).toEqual(expect.arrayContaining(['ancestor::', '..', 'last()', 'count()', 'not']));
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
    expect(labels('Body/')).toEqual(expect.arrayContaining(['following-sibling::', '..']));
    expect(labels('Body/ancestor::')).toEqual(['*']);
    expect(labels('*[child::')).toEqual(['*']);
    expect(labels('*[child::*/')).toEqual(expect.arrayContaining(['*', 'descendant-or-self::']));
    expect(labels('*[child::*[')).toContain('type');
  });

  it('offers the cardinalities without quotes', () => {
    const items = suggestSearch('*[cardinality = ', 16, COMPONENT_SEARCH_ATTRIBUTES).items;
    expect(items.map(i => i.insert)).toEqual(['Optional ', 'Required ', 'Prohibited ', '0..1 ', '1..1 ', '0..* ', '1..* ']);
    expect(suggestSearch('*[cardinality_max ', 18, COMPONENT_SEARCH_ATTRIBUTES).items.map(i => i.label)).toEqual(expect.arrayContaining(['<', '>=']));
    // cardinality itself is compared with = and != only: the parser rejects the rest
    expect(suggestSearch('*[cardinality ', 14, COMPONENT_SEARCH_ATTRIBUTES).items.map(i => i.label).filter(l => ['=', '!=', '<', '<=', '>', '>='].includes(l)))
      .toEqual(['=', '!=']);
  });

  describe('follow the grammar', () => {
    const labelsAt = (expression: string) => suggestSearch(expression, expression.length, COMPONENT_SEARCH_ATTRIBUTES).items.map(i => i.label);

    it('offers only what the parser accepts next', () => {
      const prefixes = ['*[', '*[used', '*[type = ', '*[type = ACC ', '*[cardinality ', '*[cardinality = ', '*[cardinality = 0', '*[cardinality = 0..',
        '*[contains(', '*[contains(den ', '*[contains(den, ', '*[count(', '*[count(child::*) ', '*[child::', '*[child::*', '*[child::X/', '*[../',
        '*[not ', '*[(', '*[(type = ACC ', '*[ancestor::A[', '*[type = ACC and ', '*[position() ', '*[last() = '];
      for (const prefix of prefixes) {
        for (const item of suggestSearch(prefix, prefix.length, COMPONENT_SEARCH_ATTRIBUTES).items) {
          if (item.insert === ']') {
            continue;
          }
          const text = prefix.substring(0, suggestSearch(prefix, prefix.length, COMPONENT_SEARCH_ATTRIBUTES).from) + item.insert;
          expect(predicateTail(text.substring(2)), prefix + ' + ' + item.label).toBeDefined();
        }
      }
    });

    it('leaves out what a comparison cannot take', () => {
      expect(labelsAt('*[type = ')).not.toContain('ancestor::');
      expect(labelsAt('*[last() = ')).not.toEqual(expect.arrayContaining(['*', '..', 'ancestor::']));
      expect(labelsAt('*[cardinality_max > ')).not.toContain('..');
    });

    it('goes on with a path only after a step, and starts one without a slash', () => {
      expect(labelsAt('*[')).not.toEqual(expect.arrayContaining(['/', '//']));
      expect(labelsAt('*[used ')).not.toEqual(expect.arrayContaining(['/', '//', '[']));
      expect(labelsAt('*[type = ACC ')).not.toContain('/');
      expect(labelsAt('*[child::*')).toEqual(expect.arrayContaining(['/', '//', '[', 'and', 'or', ']']));
    });

    it('offers the range and the ranges of cardinality', () => {
      expect(labelsAt('*[cardinality = 0 ')).toEqual(expect.arrayContaining(['..']));
      expect(labelsAt('*[cardinality = 0..')).toEqual(['*']);
      expect(labelsAt('*[cardinality = 0..*')).toEqual(expect.arrayContaining(['and', 'or', ']']));
    });

    it('closes what is open', () => {
      expect(labelsAt('*[contains(den ')).toEqual(expect.arrayContaining([',', ')']));
      expect(labelsAt('*[(type = ACC ')).toContain(')');
      expect(labelsAt('*[ancestor::A[type = ACC ')).toContain(']');
    });

    it('offers the short forms instead of child::, self::, parent:: and descendant::', () => {
      for (const where of ['*[', '*[child::*/', 'Body/']) {
        const labels = where.startsWith('*[') ? labelsAt(where) : suggestSearch(where, where.length, COMPONENT_SEARCH_ATTRIBUTES).items.map(i => i.label);
        expect(labels).not.toEqual(expect.arrayContaining(['child::']));
        for (const long of ['child::', 'self::', 'parent::', 'descendant::']) {
          expect(labels).not.toContain(long);
        }
        expect(labels).toEqual(expect.arrayContaining(['ancestor::', 'following-sibling::']));
      }
      expect(labelsAt('*[')).toEqual(expect.arrayContaining(['..', '.']));
    });

    describe('functions', () => {
      const at = (expression: string, caret = expression.length) => suggestSearch(expression, caret, COMPONENT_SEARCH_ATTRIBUTES);
      const named = (expression: string, label: string) => at(expression).items.find(i => i.label === label);

      it('are completed with their parentheses, the caret between them when they take arguments', () => {
        expect(named('*[', 'count()')).toMatchObject({insert: 'count()', caretBack: 1});
        expect(named('*[', 'contains()')).toMatchObject({insert: 'contains()', caretBack: 1});
        expect(named('*[', 'text()')).toMatchObject({insert: 'text()'});
        expect(named('*[', 'text()').caretBack).toBeUndefined();
        expect(named('*[', 'last()').caretBack).toBeUndefined();
      });

      it('ask for a path where count() takes one', () => {
        const labels = at('*[count(').items.map(i => i.label);
        expect(labels).toEqual(expect.arrayContaining(['*', '..', 'ancestor::']));
        expect(labels).not.toEqual(expect.arrayContaining(['type']));
        expect(labels).not.toContain('not');
        expect(at('*[count(').items.find(i => i.label === '*').detail).toContain('path');
      });

      it('ask for an attribute, then a text, where contains() takes them', () => {
        const first = at('*[contains(').items.map(i => i.label);
        expect(first).toEqual(expect.arrayContaining(['den', 'text()']));
        expect(first).not.toContain('count()');
        const second = at('*[contains(den, ').items;
        expect(second).toEqual([expect.objectContaining({label: '"text"', insert: '""', caretBack: 1})]);
        expect(at('*[contains(type, ').items.map(i => i.insert)).toEqual(expect.arrayContaining(['""', '"ACC"']));
      });

      it('do not offer the closing parenthesis that is already there', () => {
        const items = at('*[count()', 9).items.map(i => i.label);
        expect(items).not.toContain(')');
        expect(at('*[count(*').items.map(i => i.label)).toContain(')');
      });
    });

    it('offers nothing after a lexical error', () => {
      expect(labelsAt('*[type = ?')).toEqual([]);
    });
  });
});
