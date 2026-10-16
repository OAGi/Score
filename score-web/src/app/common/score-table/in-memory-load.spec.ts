import {HttpParams} from '@angular/common/http';
import {describe, expect, it} from 'vitest';
import {inMemoryLoad} from './in-memory-load';

interface Row {
  name: string;
  n?: number;
  nested?: { text: string };
}

const ROWS: Row[] = [
  {name: 'b', n: 2, nested: {text: 'z'}},
  {name: 'a', n: 10, nested: {text: 'y'}},
  {name: 'c', nested: {text: 'x'}},
  {name: 'd', n: 1, nested: {text: 'w'}},
];

function run(load: ReturnType<typeof inMemoryLoad<Row>>, params: Record<string, string>) {
  let result;
  load(new HttpParams({fromObject: params})).subscribe(r => result = r);
  return result;
}

describe('inMemoryLoad', () => {
  it('cuts the page and reports the total', () => {
    const r = run(inMemoryLoad(() => ROWS), {pageIndex: '1', pageSize: '3'});
    expect(r.list.map(e => e.name)).toEqual(['d']);
    expect(r.length).toBe(4);
    expect(r.page).toBe(1);
  });

  it('sorts by orderBy, numbers as numbers and empty values first', () => {
    const load = inMemoryLoad(() => ROWS);
    expect(run(load, {pageIndex: '0', pageSize: '10', orderBy: '+n'}).list.map(e => e.name)).toEqual(['c', 'd', 'b', 'a']);
    expect(run(load, {pageIndex: '0', pageSize: '10', orderBy: '-n'}).list.map(e => e.name)).toEqual(['a', 'b', 'd', 'c']);
  });

  it('sorts by sortActive/sortDirection with the key of the column', () => {
    const load = inMemoryLoad<Row>(() => ROWS, {text: row => row.nested?.text});
    const r = run(load, {pageIndex: '0', pageSize: '10', sortActive: 'text', sortDirection: 'asc'});
    expect(r.list.map(e => e.name)).toEqual(['d', 'c', 'a', 'b']);
  });

  it('keeps the rows that match', () => {
    const load = inMemoryLoad<Row>(() => ROWS, {}, (row, params) => row.name === (params.get('search') ?? row.name));
    const r = run(load, {pageIndex: '0', pageSize: '10', search: 'a'});
    expect(r.list.map(e => e.name)).toEqual(['a']);
    expect(r.length).toBe(1);
  });

  it('goes back to the last page when rows were removed from it', () => {
    const r = run(inMemoryLoad(() => ROWS), {pageIndex: '5', pageSize: '3'});
    expect(r.page).toBe(1);
    expect(r.list.map(e => e.name)).toEqual(['d']);
  });
});
