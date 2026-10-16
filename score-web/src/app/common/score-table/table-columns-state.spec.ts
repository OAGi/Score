import {TableColumnsState} from './table-columns-state';
import {defineTable} from './table-def';
import {UserToken} from '../../authentication/domain/auth';
import {loadProperty, saveProperty} from '../utility';

const TABLE = defineTable({
  storageKey: 'TableColumns-SpecPage',
  columns: [
    {id: 'name', label: 'Name', width: 0},
    {id: 'lastUpdateTimestamp', label: 'Updated On', width: 160},
  ],
});

const token = {username: 'spec-user'} as UserToken;
const stored = () => JSON.parse(loadProperty(token, TABLE.storageKey, 'null'));

describe('TableColumnsState', () => {
  beforeEach(() => localStorage.clear());

  it('starts from the definition and shows every column', () => {
    const state = new TableColumnsState(TABLE, token);
    expect(state.list()).toEqual([
      {name: 'Name', selected: true, width: 0},
      {name: 'Updated On', selected: true, width: 160},
    ]);
    expect(state.displayed()).toEqual(['name', 'lastUpdateTimestamp']);
    expect(state.width('lastUpdateTimestamp')).toBe(160);
  });

  it('reads what the former plumbing stored, in the same key and format', () => {
    saveProperty(token, TABLE.storageKey, JSON.stringify([
      {name: 'Updated On', selected: true, width: 200},
      {name: 'Name', selected: false, width: 0},
    ]));
    const state = new TableColumnsState(TABLE, token);
    expect(state.displayed()).toEqual(['lastUpdateTimestamp']);
    expect(state.width('lastUpdateTimestamp')).toBe(200);
  });

  it('drops stored settings whose column count differs from the definition', () => {
    saveProperty(token, TABLE.storageKey, JSON.stringify([{name: 'Name', selected: false, width: 0}]));
    expect(new TableColumnsState(TABLE, token).displayed()).toEqual(['name', 'lastUpdateTimestamp']);
  });

  it('keeps widths when the selector changes order and visibility, and persists', () => {
    const state = new TableColumnsState(TABLE, token);
    state.resize('lastUpdateTimestamp', 220);
    state.update([{name: 'Updated On', selected: true}, {name: 'Name', selected: false}]);
    expect(state.displayed()).toEqual(['lastUpdateTimestamp']);
    expect(stored()).toEqual([
      {name: 'Updated On', selected: true, width: 220},
      {name: 'Name', selected: false, width: 0},
    ]);
  });

  it('resizes by column id and ignores unknown ids', () => {
    const state = new TableColumnsState(TABLE, token);
    state.resize('unknown', 50);
    state.resize(undefined, 50);
    expect(stored()).toBeNull();
    state.resize('name', 90);
    expect(state.width('name')).toBe(90);
  });

  it('resets to the definition', () => {
    const state = new TableColumnsState(TABLE, token);
    state.update([{name: 'Updated On', selected: false}, {name: 'Name', selected: true}]);
    state.reset();
    expect(state.list()).toEqual([
      {name: 'Name', selected: true, width: 0},
      {name: 'Updated On', selected: true, width: 160},
    ]);
  });
});
