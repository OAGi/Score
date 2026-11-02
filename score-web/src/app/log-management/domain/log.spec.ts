import {CcSnapshot} from './log';

describe('CcSnapshot', () => {
  it('reads the DEN from the component manifest metadata', () => {
    const snapshot = new CcSnapshot({
      guid: 'g1',
      component: 'acc',
      _metadata: {accManifest: {den: 'Object Class Term. Details'}, acc: {objectClassTerm: 'Object Class Term'}}
    });

    expect(snapshot.den).toBe('Object Class Term. Details');
  });

  it('reads the DEN of a BCCP from bccpManifest', () => {
    const snapshot = new CcSnapshot({guid: 'g2', component: 'bccp', _metadata: {bccpManifest: {den: 'Amount. Amount'}}});

    expect(snapshot.den).toBe('Amount. Amount');
  });

  it('leaves the DEN undefined when the metadata is missing', () => {
    expect(new CcSnapshot({guid: 'g3', component: 'dt'}).den).toBeUndefined();
  });
});
