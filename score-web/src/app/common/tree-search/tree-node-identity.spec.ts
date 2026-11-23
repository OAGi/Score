import {findRowIndex, identityOfNode, isSameNode} from './tree-node-identity';

interface Row {
  id: string;
  self?: Row;
  queryPath?: string;
  hashPath?: string;
}

const row = (id: string, extra: Partial<Row> = {}): Row => ({id, ...extra});

describe('identityOfNode', () => {
  it('is the node itself when it stands for nothing else', () => {
    const node = row('a');
    expect(identityOfNode(node)).toBe(node);
    expect(identityOfNode(row('b', {self: undefined}))).not.toBeUndefined();
  });

  it('is the node a wrapper stands for, however many wrappers there are', () => {
    const inner = row('inner');
    const middle = row('middle', {self: inner});
    const outer = row('outer', {self: middle});
    expect(identityOfNode(outer)).toBe(inner);
    expect(identityOfNode(middle)).toBe(inner);
  });

  it('stops at a node that says it is itself', () => {
    const node = row('a');
    node.self = node;
    expect(identityOfNode(node)).toBe(node);
  });
});

describe('isSameNode', () => {
  it('holds for a node and its wrapper, and for two nodes of one path', () => {
    const inner = row('inner');
    expect(isSameNode(inner, row('wrapper', {self: inner}))).toBe(true);
    expect(isSameNode(row('x', {queryPath: 'a/b'}), row('y', {queryPath: 'a/b'}))).toBe(true);
  });

  it('does not hold for other nodes, nor for nodes that have no path to go by', () => {
    expect(isSameNode(row('x'), row('y'))).toBe(false);
    expect(isSameNode(row('x', {queryPath: 'a/b'}), row('y', {queryPath: 'a/c'}))).toBe(false);
    expect(isSameNode(row('x'), undefined)).toBe(false);
  });
});

describe('findRowIndex', () => {
  it('finds the node itself first, then the node it stands for', () => {
    const rows = [row('a'), row('b')];
    expect(findRowIndex(rows, rows[1])).toBe(1);
    expect(findRowIndex(rows, row('wrapper', {self: rows[0]}))).toBe(0);
  });

  it('finds a place by its path only when the path names one row', () => {
    const rows = [row('a', {queryPath: 'p/a'}), row('b', {queryPath: 'p/b'}), row('c', {queryPath: 'p/b'})];
    expect(findRowIndex(rows, row('x', {queryPath: 'p/a'}))).toBe(0);
    expect(findRowIndex(rows, row('x', {queryPath: 'p/b'}))).toBe(-1);
  });

  it('falls back to the hash path of the place, and gives -1 for a node that is not a row', () => {
    const rows = [row('a', {hashPath: 'h1'}), row('b', {hashPath: 'h2'})];
    expect(findRowIndex(rows, row('x', {hashPath: 'h2'}))).toBe(1);
    expect(findRowIndex(rows, row('x'))).toBe(-1);
    expect(findRowIndex(rows, undefined)).toBe(-1);
  });
});
