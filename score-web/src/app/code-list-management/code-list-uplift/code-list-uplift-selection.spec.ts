import {SelectionModel} from '@angular/cdk/collections';
import {of} from 'rxjs';
import {CodeListUpliftComponent} from './code-list-uplift.component';

vi.mock('../../common/utility', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../common/utility')>()),
  initFilter: () => {},
  saveBranch: () => {},
  loadBranch: () => undefined,
}));

/** The code list selected on one branch is not uplifted after the source branch or the library changes. */
describe('CodeListUpliftComponent selection', () => {
  const published = (id: number) => ({releaseId: id, releaseNum: '10.' + id, workingRelease: false});
  const newSelf = (releases: unknown[] = []) => {
    const view = {setFilter: vi.fn(), onSearch: vi.fn()};
    const self = {
      selection: new SelectionModel<unknown>(false, [{codeListManifestId: 1}]),
      auth: {getUserToken: () => ({})},
      cookieType: 'x',
      sourceRelease: published(1) as unknown,
      targetRelease: published(2) as unknown,
      releases: [] as unknown[],
      targetReleaseList: [] as unknown[],
      targetReleaseListFilterCtrl: {},
      targetReleaseFilteredList: {},
      libraryChoice: {choose: vi.fn()},
      releaseService: {getReleaseSummaryList: () => of(releases)},
      view: () => view,
      applySourceRelease: vi.fn(),
      applyLibrary: vi.fn(),
      initReleases: (r: never) => CodeListUpliftComponent.prototype.initReleases.call(self, r),
    };
    return self;
  };

  it('clears the selection when the source branch changes', () => {
    const self = newSelf();
    CodeListUpliftComponent.prototype.onSourceReleaseChange.call(self as never, published(3) as never);
    expect(self.selection.selected).toEqual([]);
  });

  it('clears the selection when the library changes', () => {
    const self = newSelf([published(1)]);
    CodeListUpliftComponent.prototype.onLibraryChange.call(self as never, {libraryId: 2} as never);
    expect(self.selection.selected).toEqual([]);
  });

  it('drops the stale source and target releases when the new library has no published release', () => {
    const self = newSelf([{releaseId: 9, releaseNum: 'Working', workingRelease: true}]);
    CodeListUpliftComponent.prototype.onLibraryChange.call(self as never, {libraryId: 2} as never);
    expect(self.sourceRelease).toBeUndefined();
    expect(self.targetRelease).toBeUndefined();
  });
});
