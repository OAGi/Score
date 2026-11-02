import {defineTable} from './table-def';
import {emptyQuery, queryFromUrl, queryToHttpParams, queryToUrl} from './table-query';
import {base64Decode, base64Encode} from '../utility';

/** The same shape as the context category list: the expectations below are what it sent before `score-table-view`. */
const TABLE = defineTable({
  storageKey: 'TableColumns-SpecPage',
  columns: [
    {id: 'name', label: 'Name', width: 0, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', width: 160, sortable: true},
  ],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'name', placeholder: 'Search by Name'},
  filters: [
    {type: 'text', key: 'description', label: 'Description'},
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
    {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpParam: 'lastUpdatedOn'},
  ],
});

const http = (q: ReturnType<typeof emptyQuery>, def = TABLE) => queryToHttpParams(def, q).toString();

describe('table query', () => {
  it('starts from the default sort, page 0 and size 10', () => {
    const q = emptyQuery(TABLE);
    expect(http(q)).toBe('pageIndex=0&pageSize=10&orderBy=-lastUpdateTimestamp');
  });

  it('sends the filters in the endpoint convention', () => {
    const q = emptyQuery(TABLE);
    q.values['name'] = 'x';
    q.values['description'] = 'y';
    q.values['updaterLoginIdList'] = ['a', 'b'];
    q.values['updatedDate'] = {start: new Date(1000), end: new Date(2000)};
    q.sorts = [{active: 'name', direction: 'asc'}];
    q.pageIndex = 2;
    const params = queryToHttpParams(TABLE, q);
    expect(params.get('pageIndex')).toBe('2');
    expect(params.get('orderBy')).toBe('+name');
    expect(params.get('updaterLoginIdList')).toBe('a,b');
    expect(params.get('lastUpdatedOn')).toBe('[1000~2000]');
    expect(params.get('name')).toBe('x');
    expect(params.get('description')).toBe('y');
  });

  it('sends an open ended date range', () => {
    const q = emptyQuery(TABLE);
    q.values['updatedDate'] = {start: null, end: new Date(2000)};
    expect(queryToHttpParams(TABLE, q).get('lastUpdatedOn')).toBe('[~2000]');
  });

  it('sends several sorts in the order of priority', () => {
    const q = emptyQuery(TABLE);
    q.sorts = [{active: 'name', direction: 'asc'}, {active: 'lastUpdateTimestamp', direction: 'desc'}];
    expect(queryToHttpParams(TABLE, q).get('orderBy')).toBe('+name,-lastUpdateTimestamp');
  });

  it('keeps the sorts of a multiSort table as sortActives/sortDirections, and reads a bookmark made before score-table-view', () => {
    const q = emptyQuery(TABLE);
    q.sorts = [{active: 'name', direction: 'asc'}, {active: 'lastUpdateTimestamp', direction: 'desc'}];
    const url = queryToUrl(TABLE, q, true);
    const decoded = new URLSearchParams(base64Decode(url.substring(2)));
    expect(decoded.get('sortActives')).toBe('name,lastUpdateTimestamp');
    expect(decoded.get('sortDirections')).toBe('asc,desc');
    expect(decoded.has('sortActive')).toBe(false);
    expect(queryFromUrl(TABLE, url.substring(2), true).sorts).toEqual(q.sorts);

    const old = base64Encode('sortActives=name,lastUpdateTimestamp&sortDirections=desc,asc&pageIndex=0&pageSize=10');
    expect(queryFromUrl(TABLE, old, true).sorts).toEqual([
      {active: 'name', direction: 'desc'}, {active: 'lastUpdateTimestamp', direction: 'asc'}]);
  });

  it('can have several default sorts', () => {
    const multi = defineTable({...TABLE, defaultSort: [{active: 'name', direction: 'asc'}, {active: 'size', direction: 'desc'}]});
    expect(queryToHttpParams(multi, emptyQuery(multi)).get('orderBy')).toBe('+name,-size');
  });

  it('can send only the direction of the sort', () => {
    const directionOnly = defineTable({...TABLE, sortParam: 'sortDirection'});
    const params = queryToHttpParams(directionOnly, emptyQuery(directionOnly));
    expect(params.toString()).toBe('pageIndex=0&pageSize=10&sortDirection=desc');
  });

  it('can use the legacy sort parameters', () => {
    const legacy = defineTable({...TABLE, sortParam: 'sortActive'});
    const params = queryToHttpParams(legacy, emptyQuery(legacy));
    expect(params.get('sortActive')).toBe('lastUpdateTimestamp');
    expect(params.get('sortDirection')).toBe('desc');
    expect(params.has('orderBy')).toBe(false);
  });

  it('round-trips the URL, and reads a bookmark made before score-table-view', () => {
    const q = emptyQuery(TABLE);
    q.values['name'] = 'x';
    q.values['updaterLoginIdList'] = ['a', 'b'];
    q.values['updatedDate'] = {start: new Date('2025-01-02T00:00:00Z'), end: null};
    const url = queryToUrl(TABLE, q);
    const back = queryFromUrl(TABLE, url.substring(2));
    expect(back).toEqual(q);

    const old = base64Encode('sortActive=name&sortDirection=desc&pageIndex=1&pageSize=25&updaterLoginIdList=a,b'
      + '&updatedDateStart=' + encodeURIComponent('Thu, 02 Jan 2025 00:00:00 GMT') + '&name=x&description=y');
    const restored = queryFromUrl(TABLE, old);
    expect(restored.sorts).toEqual([{active: 'name', direction: 'desc'}]);
    expect(restored.pageIndex).toBe(1);
    expect(restored.pageSize).toBe(25);
    expect(restored.values['updaterLoginIdList']).toEqual(['a', 'b']);
    expect(restored.values['description']).toBe('y');
    expect((restored.values['updatedDate'] as any).start.toISOString()).toBe('2025-01-02T00:00:00.000Z');
  });

  it('can send the two ends of a date range apart', () => {
    const apart = defineTable({
      ...TABLE,
      filters: [{type: 'dateRange', key: 'createdDate', label: 'Created', httpStart: 'createStart', httpEnd: 'createEnd'}]
    });
    const q = emptyQuery(apart);
    expect(queryToHttpParams(apart, q).has('createStart')).toBe(false);
    q.values['createdDate'] = {start: new Date(1000), end: null};
    expect(queryToHttpParams(apart, q).toString()).toBe('pageIndex=0&pageSize=10&orderBy=-lastUpdateTimestamp&createStart=1000');
    q.values['createdDate'] = {start: new Date(1000), end: new Date(2000)};
    expect(queryToHttpParams(apart, q).get('createEnd')).toBe('2000');
  });

  it('has select and boolean filters, under their own HTTP names', () => {
    const kinds = defineTable({
      ...TABLE,
      filters: [
        {type: 'select', key: 'roles', label: 'Role', options: [{value: 'a', label: 'A'}, {value: 'b', label: 'B'}]},
        {type: 'boolean', key: 'mine', label: 'Mine', httpParam: 'ownedByMe'},
      ]
    });
    const q = emptyQuery(kinds);
    expect(q.values).toEqual({name: '', roles: [], mine: false});
    q.values['roles'] = ['a', 'b'];
    q.values['mine'] = true;
    const http = queryToHttpParams(kinds, q);
    expect(http.get('roles')).toBe('a,b');
    expect(http.get('ownedByMe')).toBe('true');
    const url = queryToUrl(kinds, q);
    expect(queryFromUrl(kinds, url.substring(2)).values).toEqual(q.values);
    expect(queryToHttpParams(kinds, emptyQuery(kinds)).has('ownedByMe')).toBe(false);
  });

  it('can keep a date range in the URL as ms, under keys of its own', () => {
    const epoch = defineTable({
      ...TABLE,
      filters: [{type: 'dateRange', key: 'updatedDate', label: 'Updated', httpStart: 'updateStart', httpEnd: 'updateEnd', urlStart: 'updateStart', urlEnd: 'updateEnd'}]
    });
    const q = emptyQuery(epoch);
    q.values['updatedDate'] = {start: new Date(1000), end: new Date(2000)};
    const decoded = new URLSearchParams(base64Decode(queryToUrl(epoch, q).substring(2)));
    expect(decoded.get('updateStart')).toBe('1000');
    expect(decoded.get('updateEnd')).toBe('2000');
    expect(queryFromUrl(epoch, queryToUrl(epoch, q).substring(2)).values['updatedDate']).toEqual(q.values['updatedDate']);
    expect(queryToHttpParams(epoch, q).get('updateEnd')).toBe('2000');
  });

  it('can send the sorts as lists', () => {
    const lists = defineTable({...TABLE, sortParam: 'sortActives', defaultSort: [{active: 'name', direction: 'asc'}, {active: 'size', direction: 'desc'}]});
    const params = queryToHttpParams(lists, emptyQuery(lists));
    expect(params.get('sortActives')).toBe('name,size');
    expect(params.get('sortDirections')).toBe('asc,desc');
    expect(params.has('orderBy')).toBe(false);
  });

  it('tells the endpoint of a yes/no choice only when exactly one is chosen', () => {
    const yesNo = defineTable({
      ...TABLE,
      filters: [{type: 'select', key: 'deprecated', label: 'Deprecated', onlyWhenOne: true, options: [{value: 'true', label: 'True'}, {value: 'false', label: 'False'}]}]
    });
    const q = emptyQuery(yesNo);
    q.values['deprecated'] = ['true', 'false'];
    expect(queryToHttpParams(yesNo, q).has('deprecated')).toBe(false);
    expect(queryToUrl(yesNo, q)).toBeTruthy();
    q.values['deprecated'] = ['false'];
    expect(queryToHttpParams(yesNo, q).get('deprecated')).toBe('false');
    q.values['deprecated'] = [];
    expect(queryToHttpParams(yesNo, q).has('deprecated')).toBe(false);
  });

  it('asks for the sort parameters of a legacy endpoint even when the table is not sorted', () => {
    const legacy = defineTable({...TABLE, sortParam: 'sortActives'});
    const q = emptyQuery(legacy);
    q.sorts = [];
    expect(queryToHttpParams(legacy, q).toString()).toBe('pageIndex=0&pageSize=10&sortActives=&sortDirections=');
    const single = defineTable({...TABLE, sortParam: 'sortActive'});
    expect(queryToHttpParams(single, {...emptyQuery(single), sorts: []}).toString()).toBe('pageIndex=0&pageSize=10&sortActive=&sortDirection=');
    expect(queryToHttpParams(TABLE, {...emptyQuery(TABLE), sorts: []}).has('orderBy')).toBe(false);
  });
});
