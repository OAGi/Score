import {HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {describe, expect, it} from 'vitest';
import {inMemoryLoad} from './in-memory-load';
import {defineTable} from './table-def';

interface Row {
  name: string;
  n?: number;
  nested?: { text: string };
}

const TABLE = defineTable({
  storageKey: 'test',
  columns: [
    {id: 'name', label: 'Name', width: 0, sortable: true},
    {id: 'n', label: 'N', width: 0, sortable: true},
    {id: 'text', label: 'Text', width: 0, sortable: true},
  ],
});
const PLAIN_TABLE = defineTable({
  storageKey: 'test',
  columns: [{id: 'name', label: 'Name', width: 0, sortable: true}, {id: 'n', label: 'N', width: 0, sortable: true}],
});
const TEXT_KEY = {text: (row: Row) => row.nested?.text};

const ROWS: Row[] = [
  {name: 'b', n: 2, nested: {text: 'z'}},
  {name: 'a', n: 10, nested: {text: 'y'}},
  {name: 'c', nested: {text: 'x'}},
  {name: 'd', n: 1, nested: {text: 'w'}},
];

function run(load: (params: HttpParams) => Observable<unknown>, params: Record<string, string>) {
  let result: any;
  load(new HttpParams({fromObject: params})).subscribe(r => result = r);
  return result;
}

describe('inMemoryLoad', () => {
  it('cuts the page and reports the total', () => {
    const r = run(inMemoryLoad(PLAIN_TABLE, () => ROWS), {pageIndex: '1', pageSize: '3'});
    expect(r.list.map(e => e.name)).toEqual(['d']);
    expect(r.length).toBe(4);
    expect(r.page).toBe(1);
  });

  it('sorts by orderBy, numbers as numbers and empty values first', () => {
    const load = inMemoryLoad(PLAIN_TABLE, () => ROWS);
    expect(run(load, {pageIndex: '0', pageSize: '10', orderBy: '+n'}).list.map(e => e.name)).toEqual(['c', 'd', 'b', 'a']);
    expect(run(load, {pageIndex: '0', pageSize: '10', orderBy: '-n'}).list.map(e => e.name)).toEqual(['a', 'b', 'd', 'c']);
  });

  it('sorts by sortActive/sortDirection with the key of the column', () => {
    const load = inMemoryLoad(TABLE, () => ROWS, TEXT_KEY);
    const r = run(load, {pageIndex: '0', pageSize: '10', sortActive: 'text', sortDirection: 'asc'});
    expect(r.list.map(e => e.name)).toEqual(['d', 'c', 'a', 'b']);
  });

  it('sorts by sortActives/sortDirections of a multi-sort table, the first being the most significant', () => {
    const rows: Row[] = [{name: 'b', n: 1}, {name: 'a', n: 2}, {name: 'c', n: 1}, {name: 'd', n: 2}];
    const load = inMemoryLoad(PLAIN_TABLE, () => rows);
    const params = {pageIndex: '0', pageSize: '10'};
    expect(run(load, {...params, sortActives: 'n,name', sortDirections: 'asc,desc'}).list.map(e => e.name)).toEqual(['c', 'b', 'd', 'a']);
    expect(run(load, {...params, sortActives: 'n,name', sortDirections: 'desc,asc'}).list.map(e => e.name)).toEqual(['a', 'd', 'b', 'c']);
  });

  it('keeps the order of the rows when no sort is given', () => {
    expect(run(inMemoryLoad(PLAIN_TABLE, () => ROWS), {pageIndex: '0', pageSize: '10', sortActives: '', sortDirections: ''}).list.map(e => e.name))
      .toEqual(['b', 'a', 'c', 'd']);
  });

  it('keeps the rows that match', () => {
    const load = inMemoryLoad(PLAIN_TABLE, () => ROWS, {}, (row, params) => row.name === (params.get('search') ?? row.name));
    const r = run(load, {pageIndex: '0', pageSize: '10', search: 'a'});
    expect(r.list.map(e => e.name)).toEqual(['a']);
    expect(r.length).toBe(1);
  });

  it('goes back to the last page when rows were removed from it', () => {
    const r = run(inMemoryLoad(PLAIN_TABLE, () => ROWS), {pageIndex: '5', pageSize: '3'});
    expect(r.page).toBe(1);
    expect(r.list.map(e => e.name)).toEqual(['d']);
  });

  it('does not compile when a sortable column is not a property of the row and says nothing of what it sorts by', () => {
    // @ts-expect-error `text` is not a property of Row
    inMemoryLoad(TABLE, () => ROWS);
    // @ts-expect-error the key of `text` is the only one that is given, `n` is a property
    inMemoryLoad(TABLE, () => ROWS, {n: (row: Row) => row.n});
    expect(inMemoryLoad(TABLE, () => ROWS, TEXT_KEY)).toBeTypeOf('function');
  });
});
