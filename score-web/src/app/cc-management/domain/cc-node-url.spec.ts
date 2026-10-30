import {ccNodeUrl} from './cc-node-url';

describe('ccNodeUrl', () => {
  it('opens a Data Type on the Data Type page', () => {
    expect(ccNodeUrl({type: 'DT', manifestId: 5})).toBe('/data_type/5');
  });

  it('has no page for a supplementary component', () => {
    expect(ccNodeUrl({type: 'DT_SC', manifestId: 5})).toBeUndefined();
  });

  it('keeps the core component route of the other node types', () => {
    expect(ccNodeUrl({type: 'ACC', manifestId: 5})).toBe('/core_component/acc/5');
    expect(ccNodeUrl({type: 'BCCP', manifestId: 6})).toBe('/core_component/bccp/6');
  });
});
