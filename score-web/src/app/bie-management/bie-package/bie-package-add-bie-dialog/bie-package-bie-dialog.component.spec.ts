import {BieListEntry} from '../../bie-list/domain/bie-list';
import {
  addDialogData,
  BiePackageBieDialogComponent,
  replaceDialogData
} from './bie-package-bie-dialog.component';

describe('BiePackageBieDialogComponent', () => {
  const biePackage = {biePackageId: 1, libraryId: 2};
  const webPageInfo = {};
  const selected = {den: 'Order. Details', topLevelAsbiepId: 7} as unknown as BieListEntry;

  it('should be defined', () => {
    expect(BiePackageBieDialogComponent).toBeTruthy();
  });

  it('Add data has no replace state', () => {
    const data = addDialogData(biePackage, webPageInfo);
    expect(data.action).toBe('Add');
    expect(data.den).toBeUndefined();
    expect(data.excludeTopLevelAsbiepIds).toBeUndefined();
  });

  it('Replace data carries the BIE to replace', () => {
    const data = replaceDialogData(biePackage, webPageInfo, selected);
    expect(data.action).toBe('Replace');
    expect(data.den).toBe('Order. Details');
    expect(data.excludeTopLevelAsbiepIds).toEqual([7]);
  });

  it('Add data after Replace data is not contaminated', () => {
    const replace = replaceDialogData(biePackage, webPageInfo, selected);
    const add = addDialogData(biePackage, webPageInfo);
    expect(add).not.toBe(replace);
    expect('den' in add).toBe(false);
    expect(add.den).toBeUndefined();
    expect(add.excludeTopLevelAsbiepIds).toBeUndefined();
  });
});
