import {DagContextHost, DagNodeContext} from './dag-node-context';
import {DagPathSearch} from './dag-path-search';
import {PathPattern} from './path-pattern';
import {SearchDag, SearchDagBuilder} from './search-dag';

/**
 * The tree, with Party shown under both Sender and Receiver (one record, two places):
 *
 *   Root
 *   ├─ Header ── Id, Date
 *   ├─ Body ──── Sender ── Party ── Id, Code
 *   │            Receiver ─ Party ── Id, Code
 *   └─ Footer
 */
const CARDINALITY: Record<string, [number, number]> = {
  Header: [1, 1], Body: [0, 1], Footer: [0, 0], Sender: [1, Infinity], Receiver: [0, Infinity], Party: [1, 1], Id: [1, 1], Date: [0, 1], Code: [0, 5]
};

function tree(): SearchDag {
  const b = new SearchDagBuilder();
  const node = (name: string, type: string, used = false) => b.record(name, [name], name, {
    $text: name, type, used, property_term: type === 'BBIEP' ? name + ' Term' : undefined,
    cardinality_min: CARDINALITY[name]?.[0], cardinality_max: CARDINALITY[name]?.[1]
  }).index;
  const root = node('Root', 'ABIE');
  const [header, body, footer, sender, receiver, party] = ['Header', 'Body', 'Footer', 'Sender', 'Receiver', 'Party']
    .map(name => node(name, 'ASBIEP'));
  const [id, date, code] = [node('Id', 'BBIEP', true), node('Date', 'BBIEP'), node('Code', 'BBIEP')];
  b.setChildren(root, [header, body, footer]);
  b.setChildren(header, [id, date]);
  b.setChildren(body, [sender, receiver]);
  b.setChildren(sender, [party]);
  b.setChildren(receiver, [party]);
  b.setChildren(party, [id, code]);
  b.setChildren(footer, []);
  b.setChildren(id, []);
  b.setChildren(date, []);
  b.setChildren(code, []);
  return b.build();
}

/** How many nodes of the tree (14 of them, Party and what is below it shown twice) the expression selects. */
function count(expr: string, caseSensitive = false): number {
  const dag = tree();
  const pattern = new PathPattern(expr, caseSensitive);
  expect(pattern.error).withContext(expr).toBeUndefined();
  expect(pattern.supported).withContext(expr).toBe(true);
  const search = new DagPathSearch(dag, pattern);
  const root = new DagNodeContext(search as DagContextHost, 0, undefined, 0, false, pattern.initialMask);
  const mask = pattern.stepContext(pattern.initialMask, root);
  const result = search.search({ancestors: [], record: 0, mask});
  expect(result.exact).toBe(true);
  return result.total;
}

describe('searching with paths', () => {

  it('goes down by / and at any depth by //', () => {
    expect(count('Id')).toBe(3);
    expect(count('Body/Sender')).toBe(1);
    expect(count('Body/Id')).toBe(0);
    expect(count('Body//Id')).toBe(2);
    expect(count('//Id')).toBe(3);
    expect(count('Body/*')).toBe(2);
    expect(count('Body/*/*')).toBe(2);
    expect(count('Body//*')).toBe(8);
    expect(count('Root//Code')).toBe(2);
  });

  it('starts at the root with a leading /, anywhere with //', () => {
    expect(count('/Root')).toBe(1);
    expect(count('/Header')).toBe(0);
    expect(count('/Root/Header')).toBe(1);
    expect(count('/Root/Body/Sender')).toBe(1);
    expect(count('/Root/Id')).toBe(0);
    expect(count('/Root//Id')).toBe(3);
    expect(count('//Header')).toBe(1);
  });

  it('names the axes', () => {
    expect(count('Body/child::*')).toBe(2);
    expect(count('Body/descendant::Id')).toBe(2);
    expect(count('Root/descendant::*')).toBe(13);
    expect(count('Body/descendant-or-self::*')).toBe(9);
    expect(count('Body/self::Body')).toBe(1);
    expect(count('Body/self::Header')).toBe(0);
  });

  it('goes back to the parent', () => {
    expect(count('Party/..')).toBe(2);
    expect(count('Id/..')).toBe(3);
    expect(count('Id/../Date')).toBe(1);
    expect(count('Code/parent::Party')).toBe(2);
    expect(count('Code/parent::Sender')).toBe(0);
    expect(count('Body//Id/..')).toBe(2);
    expect(count('/Root/Header/Id/..')).toBe(1);
  });

  it('goes to the ancestors of the first step', () => {
    expect(count('Id/ancestor::*')).toBe(7);
    expect(count('Code/ancestor::Body')).toBe(1);
    expect(count('Code/ancestor-or-self::Code')).toBe(2);
  });

  it('goes to the siblings', () => {
    expect(count('Id/following-sibling::*')).toBe(3);
    expect(count('Id/preceding-sibling::*')).toBe(0);
    expect(count('Code/preceding-sibling::Id')).toBe(2);
    expect(count('Date/preceding-sibling::*')).toBe(1);
    expect(count('Header/following-sibling::*')).toBe(2);
  });

  it('judges a node by its children and descendants', () => {
    expect(count('*[child::Id]')).toBe(3);
    expect(count('*[descendant::Code]')).toBe(6);
    expect(count('*[child::Id and child::Date]')).toBe(1);
    expect(count('*[not(child::*)]')).toBe(7);
    expect(count('*[count(child::*) = 2]')).toBe(4);
    expect(count('*[count(descendant::*) > 3]')).toBe(2);
    expect(count('*[./Code]')).toBe(2);
    expect(count('*[.//Code]')).toBe(6);
  });

  it('judges a node by its parent', () => {
    expect(count('*[parent::Body]')).toBe(2);
    expect(count('*[parent::*[type = ABIE]]')).toBe(3);
    expect(count('*[../Date]')).toBe(2);
  });

  it('judges a node by its ancestors', () => {
    expect(count('*[ancestor::Sender]')).toBe(3);
    expect(count('*[ancestor::Body]')).toBe(8);
    expect(count('Id[ancestor::Receiver]')).toBe(1);
    expect(count('*[ancestor-or-self::Sender]')).toBe(4);
    expect(count('*[ancestor::*[type = ABIE]]')).toBe(13);
    expect(count('Code[ancestor::Sender or ancestor::Header]')).toBe(1);
  });

  it('judges a node by its siblings', () => {
    expect(count('*[following-sibling::Footer]')).toBe(2);
    expect(count('*[preceding-sibling::Header]')).toBe(2);
    expect(count('*[preceding-sibling::*[type = BBIEP]]')).toBe(3);
  });

  it('counts a position among the siblings', () => {
    expect(count('Body/*[1]')).toBe(1);
    expect(count('Body/*[2]')).toBe(1);
    expect(count('Body/*[3]')).toBe(0);
    expect(count('Body/*[last()]')).toBe(1);
    expect(count('Party/*[2]')).toBe(2);
    expect(count('//Id[1]')).toBe(3);
    expect(count('*[1]')).toBe(8);
    expect(count('*[position() > 1]')).toBe(6);
    expect(count('Party/*[position() = last()]')).toBe(2);
  });

  it('counts a position among the siblings the step selects, after the predicates before it', () => {
    expect(count('Header/*[type = BBIEP][2]')).toBe(1);
    expect(count('Header/*[2][type = BBIEP]')).toBe(1);
    expect(count('Party/*[used = true][1]')).toBe(2);
    expect(count('Party/*[used = true][2]')).toBe(0);
    expect(count('Party/Code[1]')).toBe(2);
  });

  it('judges a node by the text it shows, and by the columns of its own kind', () => {
    expect(count('*[contains(text(), "ar")]')).toBe(2);
    expect(count('*[text() = "Party"]')).toBe(2);
    expect(count('*[starts-with(text(), "S") and type = ASBIEP]')).toBe(1);
    expect(count('*[contains(property_term, "Term")]')).toBe(6);
    expect(count('*[type = ASBIEP and contains(property_term, "Term")]')).toBe(0);
    expect(count('Party/*[property_term = "Id Term"]')).toBe(2);
  });

  it('compares the cardinality as a range, or as its minimum and maximum', () => {
    expect(count('*[cardinality = 1..1]')).toBe(6);
    expect(count('*[cardinality = Required]')).toBe(6);
    expect(count('*[cardinality = "1..1"]')).toBe(6);
    expect(count('*[cardinality = Optional]')).toBe(2);
    expect(count('*[cardinality = prohibited]')).toBe(1);
    expect(count('*[cardinality = 0..*]')).toBe(1);
    expect(count('*[cardinality = 1..unbounded]')).toBe(1);
    expect(count('*[cardinality != 1..1]')).toBe(7);
    expect(count('*[cardinality = 0..1]')).toBe(count('*[cardinality_min = 0 and cardinality_max = 1]'));
    expect(count('*[cardinality_min = 1 and cardinality_max < 10]')).toBe(6);
    expect(count('*[cardinality_min = 1]')).toBe(7);
    expect(count('*[cardinality_max > 1]')).toBe(4);
    expect(count('*[cardinality_max = unbounded]')).toBe(2);
    expect(count('*[cardinality_max >= 5]')).toBe(4);
    expect(count('Root[cardinality = 1..1]')).toBe(0);
  });

  it('refuses a cardinality it cannot read', () => {
    for (const expr of ['*[cardinality < 1..1]', '*[cardinality = 2]', '*[cardinality = Maybe]', '*[cardinality = "a..b"]']) {
      expect(new PathPattern(expr).error).withContext(expr).toBeDefined();
    }
  });

  it('reads a bare word after a comparison as text', () => {
    expect(count('*[type=BBIEP and used]')).toBe(3);
    expect(count('*[type=ABIE]')).toBe(1);
    expect(count('*[type=BBIEP and used=true]/..')).toBe(3);
  });

  it('works out a count once for a component shown in several places', () => {
    // Party, Id and Code show twice: the second visit is answered from what the first found
    expect(count('Sender//*[type=BBIEP]')).toBe(2);
    expect(count('Receiver//*[type=BBIEP]')).toBe(2);
    expect(count('Body//*[type=BBIEP]')).toBe(4);
  });

  it('refuses what it cannot search', () => {
    for (const expr of ['/Root/..', '/Root/Body/ancestor::Root', 'Id/ancestor::*[1]', 'Body/descendant::Id[2]', '*[Id/child::Code = 1]',
      '*[ancestor::A[ancestor::B]]', '*[parent::*/parent::*]', 'Foo::Bar', '*[count(x) > 1]']) {
      expect(new PathPattern(expr).error).withContext(expr).toBeDefined();
    }
  });
});
