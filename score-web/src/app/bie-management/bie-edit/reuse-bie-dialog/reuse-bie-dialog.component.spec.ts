import {ReuseBieDialogComponent, reuseBieParams} from './reuse-bie-dialog.component';

describe('ReuseBieDialogComponent', () => {
  it('should be defined', () => {
    expect(ReuseBieDialogComponent).toBeTruthy();
  });

  it('builds an unscoped inheritance filter while retaining the target ASCCP and release filters', () => {
    const params = reuseBieParams({asccpManifestId: 77, libraryId: 3, releaseId: 4, topLevelAsbiepId: 9}, false);

    expect(params['asccpManifestId']).toBe('77');
    expect(params['libraryId']).toBe('3');
    expect(params['releaseIds']).toBe('4');
    expect(params['excludeTopLevelAsbiepIds']).toBe('9');
    expect(params['basedTopLevelAsbiepIds']).toBeUndefined();
    expect(params['ownedByDeveloper']).toBeUndefined();
  });

  it('asks for the BIEs that are based on a BIE only when there is one, and for the developers\' only of a developer', () => {
    const params = reuseBieParams({libraryId: 3, releaseId: 4, topLevelAsbiepId: 9, basedTopLevelAsbiepId: 10}, true);

    expect(params['basedTopLevelAsbiepIds']).toBe('10');
    expect(params['ownedByDeveloper']).toBe('true');
    expect(reuseBieParams({libraryId: 3, releaseId: 4, topLevelAsbiepId: 9, basedTopLevelAsbiepId: 0}, false)['basedTopLevelAsbiepIds'])
      .toBeUndefined();
  });
});
