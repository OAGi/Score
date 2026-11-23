import {satisfiesServerPredicate, splitServerPredicates} from './server-predicate';
import {ParseError} from './search-grammar';

const SERVER = new Set(['remark', 'definition', 'business_term']);

describe('splitServerPredicates', () => {
  it('leaves an expression without a server attribute alone', () => {
    const split = splitServerPredicates('//*[used = true and name = "Code"]', SERVER);
    expect(split.predicates).toEqual([]);
    expect(split.expression).toBe('//*[used = true and name = "Code"]');
  });

  it('replaces a comparison and a text function by the test of a flag', () => {
    const split = splitServerPredicates('//*[remark = "x"]/*[contains(definition, "price")]', SERVER);
    expect(split.expression).toBe('//*[srv_0]/*[srv_1]');
    expect(split.predicates.map(p => [p.attribute, p.operator, p.value]))
      .toEqual([['remark', 'EQUALS', 'x'], ['definition', 'CONTAINS', 'price']]);
  });

  it('keeps what is around the condition, so graph and server tests mix', () => {
    const split = splitServerPredicates('//*[used = true and not(starts-with(remark, "a b")) or business_term != \'t\']', SERVER);
    expect(split.expression).toBe('//*[used = true and not(srv_0) or srv_1]');
    expect(split.predicates.map(p => p.operator)).toEqual(['STARTS_WITH', 'NOT_EQUALS']);
  });

  it('finds conditions in nested predicates and shares the flag of a condition written twice', () => {
    const split = splitServerPredicates('//*[child::Code[remark = "x"] and remark = "x"]', SERVER);
    expect(split.expression).toBe('//*[child::Code[srv_0] and srv_0]');
    expect(split.predicates.length).toBe(1);
  });

  it('is not misled by a quote or a bracket inside a string', () => {
    const split = splitServerPredicates('//*[contains(remark, "a]b") and name = "[x"]', SERVER);
    expect(split.expression).toBe('//*[srv_0 and name = "[x"]');
    expect(split.predicates[0].value).toBe('a]b');
  });

  it('refuses to order a text or to ask for it without a value', () => {
    expect(() => splitServerPredicates('//*[remark < "x"]', SERVER)).toThrow(ParseError);
    expect(() => splitServerPredicates('//*[remark]', SERVER)).toThrow(ParseError);
  });

  it('returns an expression that does not parse as it is', () => {
    expect(splitServerPredicates('//*[remark = ]', SERVER).predicates).toEqual([]);
  });
});

describe('satisfiesServerPredicate', () => {
  const predicate = (operator: any, value: string) => ({id: '0', flag: 'srv_0', attribute: 'remark', operator, value});

  it('compares the way the backend does, and an absent text satisfies nothing', () => {
    expect(satisfiesServerPredicate('Price List', predicate('CONTAINS', 'price'), false)).toBe(true);
    expect(satisfiesServerPredicate('Price List', predicate('CONTAINS', 'price'), true)).toBe(false);
    expect(satisfiesServerPredicate('a', predicate('NOT_EQUALS', 'b'), false)).toBe(true);
    expect(satisfiesServerPredicate(undefined, predicate('NOT_EQUALS', 'b'), false)).toBe(false);
  });

  it('does not tell a letter with an accent from one without when case does not matter, as the backend does', () => {
    const holds = (text: string, operator: string, value: string) => satisfiesServerPredicate(text, predicate(operator, value), false);
    expect(holds('café', 'CONTAINS', 'cafe')).toBe(true);
    expect(holds('cafe', 'CONTAINS', 'café')).toBe(true);
    expect(holds('CAFÉ', 'CONTAINS', 'café')).toBe(true);
    expect(holds('café', 'EQUALS', 'cafe')).toBe(true);
    expect(holds('café', 'STARTS_WITH', 'cafe')).toBe(true);
    expect(holds('café', 'ENDS_WITH', 'fe')).toBe(true);
    expect(holds('Müller', 'CONTAINS', 'muller')).toBe(true);
    expect(holds('ÀÉÎ', 'EQUALS', 'aei')).toBe(true);
    expect(holds('résumé', 'NOT_EQUALS', 'resume')).toBe(false);
  });

  it('keeps what the backend collation keeps apart, and compares a case sensitive predicate as it is', () => {
    expect(satisfiesServerPredicate('straße', predicate('CONTAINS', 'strasse'), false)).toBe(false);
    expect(satisfiesServerPredicate('한글', predicate('CONTAINS', '하'), false)).toBe(false);
    expect(satisfiesServerPredicate('한글', predicate('CONTAINS', '한'), false)).toBe(true);
    expect(satisfiesServerPredicate('café', predicate('CONTAINS', 'cafe'), true)).toBe(false);
    expect(satisfiesServerPredicate('café', predicate('CONTAINS', 'café'), true)).toBe(true);
  });
});
