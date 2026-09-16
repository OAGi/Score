import {BieListRequest} from './bie-list';
import {base64Decode} from '../../../common/utility';

describe('BieListRequest', () => {
  it('does not serialize missing inherited-base ids', () => {
    const request = new BieListRequest();
    request.basedTopLevelAsbiepIds = [undefined, null, Number.NaN, 0] as unknown as number[];

    const query = request.toQuery();
    const params = new URLSearchParams(base64Decode(query.substring(2)));

    expect(params.has('basedTopLevelAsbiepIds')).toBe(false);
  });

  it('serializes a valid inherited-base id', () => {
    const request = new BieListRequest();
    request.basedTopLevelAsbiepIds = [undefined, 27] as unknown as number[];

    const query = request.toQuery();
    const params = new URLSearchParams(base64Decode(query.substring(2)));

    expect(params.get('basedTopLevelAsbiepIds')).toBe('27');
  });
});
