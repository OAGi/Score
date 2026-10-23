import {AttrValue, parseSearchExpression} from './search-expression';
import {PathPattern} from './path-pattern';

/** The mask after a path of nodes, each given as its name and attributes. */
function matches(expr: string, ...path: {name: string, [attribute: string]: AttrValue}[]): boolean {
  const pattern = new PathPattern(expr);
  expect(pattern.error).toBeUndefined();
  const mask = pattern.stepAll(path.map(n => [n.name]), path.map(n => (attribute: string) => n[attribute]));
  return pattern.isMatch(mask);
}

describe('parseSearchExpression', () => {
  it('reads an expression without a predicate as plain names, as it always was', () => {
    const parsed = parseSearchExpression('/Sender/Release ID');
    expect(parsed.rooted).toBe(true);
    expect(parsed.steps.map(s => s.tests[0].token)).toEqual(['Sender', 'Release ID']);
    expect(parseSearchExpression('ReleaseID').steps[0].tests[0].token).toBe('Release ID');
  });

  it('does not cut a step at a / inside a predicate', () => {
    const parsed = parseSearchExpression('Sender/Code[contains(displayName, "a/b")]');
    expect(parsed.error).toBeUndefined();
    expect(parsed.steps.length).toBe(2);
  });

  it('reports where a malformed predicate goes wrong, and gives no steps', () => {
    for (const [expr, position] of [['Code[used =]', 11], ['Code[used', 9], ['Code[contains(a)]', 5], ['Code[foo(a, b)]', 5], ['Code[]', 5]]) {
      const parsed = parseSearchExpression(expr as string);
      expect(parsed.error).withContext(expr as string).toBeDefined();
      expect(parsed.error.position).withContext(expr as string).toBe(position);
      expect(parsed.steps).toEqual([]);
    }
  });
});

describe('parseSearchExpression with an unfinished path in a predicate', () => {
  it('reports an error instead of throwing when the path ends with a slash', () => {
    for (const expr of ['*[a/]', '*[./]', '*[a//]', '*[x = a/]', 'A[b/]/C']) {
      const parsed = parseSearchExpression(expr);
      expect(parsed.error).withContext(expr).toBeDefined();
      expect(parsed.steps).toEqual([]);
    }
  });
});

describe('bare words that start with a digit', () => {
  it('are read as text, as a GUID is', () => {
    const node = {name: 'Code', guid: '6f3ab1c2'};
    expect(matches('*[guid = 6f3ab1c2]', node)).toBe(true);
    expect(matches('*[guid = 6f3ab1c3]', node)).toBe(false);
    expect(matches('*[guid != 6f3ab1c3]', node)).toBe(true);
  });

  it('are text after guid even when they are all digits', () => {
    const node = {name: 'Code', guid: '00000000000000000000000000000001'};
    expect(matches('*[guid = 1]', node)).toBe(false);
    expect(matches('*[guid = 00000000000000000000000000000001]', node)).toBe(true);
    expect(matches('*[guid = "00000000000000000000000000000001"]', node)).toBe(true);
    expect(matches('*[guid != 1]', node)).toBe(true);
  });

  it('leave numbers and ranges alone', () => {
    expect(matches('*[cardinality = 0..1]', {name: 'A', cardinality_min: 0, cardinality_max: 1})).toBe(true);
    expect(matches('*[n >= 10]', {name: 'A', n: 12})).toBe(true);
  });
});

describe('search predicates', () => {
  it('compare an attribute with a value', () => {
    expect(matches('Code[used = true]', {name: 'Code', used: true})).toBe(true);
    expect(matches('Code[used = true]', {name: 'Code', used: false})).toBe(false);
    expect(matches('Code[used]', {name: 'Code', used: true})).toBe(true);
    expect(matches('Code[used]', {name: 'Code'})).toBe(false);
    expect(matches('*[cardinalityMax > 1]', {name: 'Any', cardinalityMax: 5})).toBe(true);
    expect(matches('*[cardinalityMax > 1]', {name: 'Any', cardinalityMax: 1})).toBe(false);
    expect(matches('*[type = \'asbiep\']', {name: 'Any', type: 'ASBIEP'})).toBe(true);
  });

  it('treat a missing attribute as no match, whatever the comparison', () => {
    expect(matches('*[note = "x"]', {name: 'Any'})).toBe(false);
    expect(matches('*[note != "x"]', {name: 'Any'})).toBe(false);
    expect(matches('*[not(note = "x")]', {name: 'Any'})).toBe(true);
  });

  it('call functions on text, ignoring case', () => {
    const node = {name: 'Code', displayName: 'Sender Code'};
    expect(matches('*[contains(displayName, "ender c")]', node)).toBe(true);
    expect(matches('*[starts-with(displayName, "sender")]', node)).toBe(true);
    expect(matches('*[ends-with(displayName, "Sender")]', node)).toBe(false);
  });

  it('combine with and, or, not and parentheses', () => {
    const node = {name: 'Code', used: true, type: 'BBIEP'};
    expect(matches('*[used and type = "BBIEP"]', node)).toBe(true);
    expect(matches('*[used and not type = "BBIEP"]', node)).toBe(false);
    expect(matches('*[(used or type = "X") and type != "Y"]', node)).toBe(true);
    expect(matches('*[used][type = "X"]', node)).toBe(false);
  });

  it('apply to the step they are written on, along the path', () => {
    const sender = {name: 'Sender', used: false};
    expect(matches('Sender/Code[used]', sender, {name: 'Code', used: true})).toBe(true);
    expect(matches('Sender[used]/Code', sender, {name: 'Code', used: true})).toBe(false);
    expect(matches('Sender[used = false]/Code', sender, {name: 'Code', used: true})).toBe(true);
  });

  it('read a bare word after a comparison as text, and attributes with underscores', () => {
    const node = {name: 'Extension', type: 'ACC', component_type: 'Extension', entity_type: 'Attribute'};
    expect(matches('*[type=ACC and component_type=Extension]', node)).toBe(true);
    expect(matches('*[type=ACC and component_type=Base]', node)).toBe(false);
    expect(matches('*[entity_type = \'Attribute\']', node)).toBe(true);
    expect(matches('*[entity_type = attribute]', node)).toBe(true);
    expect(matches('*[entity_type != Element]', node)).toBe(true);
    expect(matches('*[used = false]', {name: 'A', used: false})).toBe(true);
  });
});
