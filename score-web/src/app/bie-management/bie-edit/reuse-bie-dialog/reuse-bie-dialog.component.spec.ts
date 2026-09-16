import {ReuseBieDialogComponent} from './reuse-bie-dialog.component';
import {FormControl} from '@angular/forms';
import {MatTableDataSource} from '@angular/material/table';
import {of, ReplaySubject, Subject} from 'rxjs';
import {vi} from 'vitest';

describe('ReuseBieDialogComponent', () => {
  it('should be defined', () => {
    expect(ReuseBieDialogComponent).toBeTruthy();
  });

  it('builds an unscoped inheritance filter while retaining the target ASCCP and release filters', () => {
    const component = Object.create(ReuseBieDialogComponent.prototype) as ReuseBieDialogComponent;
    const candidates = [
      {topLevelAsbiepId: 10, basedTopLevelAsbiepId: undefined},
      {topLevelAsbiepId: 11, basedTopLevelAsbiepId: 10}
    ];
    const getBieListWithRequest = vi.fn(() => of({length: candidates.length, list: candidates}));
    const sortChange = new Subject<void>();

    Object.assign(component as any, {
      data: {asccpManifestId: 77, libraryId: 3, releaseId: 4, topLevelAsbiepId: 9},
      route: {snapshot: {queryParamMap: {get: () => undefined}}},
      accountService: {getAccountNames: () => of([])},
      preferencesService: {load: () => of({tableColumnsInfo: {columnsOfBiePage: []}})},
      auth: {getUserToken: () => ({roles: []})},
      bieListService: {getBieListWithRequest},
      dataSource: new MatTableDataSource(),
      paginator: {pageIndex: 0, pageSize: 10, length: 0},
      sort: {active: '', direction: '', sortChange, sort: vi.fn()},
      loginIdList: [],
      loginIdListFilterCtrl: new FormControl(),
      updaterIdListFilterCtrl: new FormControl(),
      filteredLoginIdList: new ReplaySubject<string[]>(1),
      filteredUpdaterIdList: new ReplaySubject<string[]>(1)
    });

    component.ngOnInit();

    const request = getBieListWithRequest.mock.calls[0][0];
    expect(request.filters.asccpManifestId).toBe(77);
    expect(request.releases.map(release => release.releaseId)).toEqual([4]);
    expect(request.basedTopLevelAsbiepIds).toEqual([]);
    expect(component.dataSource.data.map(row => row.topLevelAsbiepId)).toEqual([10, 11]);
    expect(component.dataSource.data.map(row => row.basedTopLevelAsbiepId)).toEqual([undefined, 10]);
  });
});
