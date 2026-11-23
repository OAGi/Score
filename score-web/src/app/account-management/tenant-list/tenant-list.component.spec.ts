import {emptyQuery, queryToHttpParams} from '../../common/score-table/table-query';
import {TENANT_TABLE} from './tenant-list.component';

describe('TENANT_TABLE', () => {
  it('sends the sorted column with its direction as orderBy, which the endpoint needs to sort', () => {
    const query = emptyQuery(TENANT_TABLE);
    query.sorts = [{active: 'name', direction: 'desc'}];

    const params = queryToHttpParams(TENANT_TABLE, query);

    expect(params.get('orderBy')).toBe('-name');
    expect(params.has('sortActive')).toBe(false);
    expect(params.has('sortDirection')).toBe(false);
  });
});
