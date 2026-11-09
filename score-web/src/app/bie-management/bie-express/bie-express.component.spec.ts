import {HttpParams} from '@angular/common/http';
import {TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {of, throwError} from 'rxjs';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {LibraryService} from '../../library-management/domain/library.service';
import {ReleaseService} from '../../release-management/domain/release.service';
import {BieListEntry} from '../bie-list/domain/bie-list';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {BieExpressComponent} from './bie-express.component';
import {BieExpressService} from './domain/bie-express.service';

describe('BieExpressComponent', () => {
  it('should be defined', () => {
    expect(BieExpressComponent).toBeTruthy();
  });
});

describe('BieExpressComponent Business Context selection on reload', () => {
  const contexts = (...ids: number[]) => ids.map(id => ({businessContextId: id, guid: 'g' + id, name: 'BC ' + id}));
  const bie = (topLevelAsbiepId: number, ...contextIds: number[]) =>
    ({topLevelAsbiepId, businessContextList: contexts(...contextIds)}) as unknown as BieListEntry;

  let page: () => any;
  let component: BieExpressComponent;

  beforeEach(() => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        {provide: BieExpressService, useValue: {}},
        {provide: BieListService, useValue: {getBieListPage: () => page()}},
        {provide: ReleaseService, useValue: {}},
        {provide: LibraryService, useValue: {}},
        {provide: AuthService, useValue: {getUserToken: () => undefined}},
        {provide: MatDialog, useValue: {}},
        {provide: WebPageInfoService, useValue: {}},
      ]
    });
    component = TestBed.runInInjectionContext(() => new BieExpressComponent());
  });

  /** Loads a page of the list, as the table does on opening, paging, sorting and searching. */
  const reload = (...list: BieListEntry[]) => {
    page = () => of({list});
    component.load(new HttpParams()).subscribe();
  };

  const selected = (topLevelAsbiepId: number) => component.businessContextSelection[topLevelAsbiepId]?.businessContextId;

  it('defaults to the first Business Context of each BIE the first time it is listed', () => {
    reload(bie(1, 10, 11), bie(2, 20));

    expect(selected(1)).toBe(10);
    expect(selected(2)).toBe(20);
  });

  it('keeps the Business Context the user chose when the list is loaded again', () => {
    reload(bie(1, 10, 11));
    component.businessContextSelection[1] = contexts(10, 11)[1];

    reload(bie(1, 10, 11));

    expect(selected(1)).toBe(11);
  });

  it('keeps the choice of a BIE that is not on the page being loaded, and of one that is', () => {
    reload(bie(1, 10, 11), bie(2, 20, 21));
    component.businessContextSelection[1] = contexts(10, 11)[1];
    component.businessContextSelection[2] = contexts(20, 21)[1];

    reload(bie(2, 20, 21), bie(3, 30));
    expect(selected(1)).toBe(11);
    expect(selected(2)).toBe(21);
    expect(selected(3)).toBe(30);

    reload(bie(1, 10, 11));
    expect(selected(1)).toBe(11);
  });

  it('falls back to the first Business Context when the chosen one is no longer one of the BIE\'s', () => {
    reload(bie(1, 10, 11));
    component.businessContextSelection[1] = contexts(10, 11)[1];

    reload(bie(1, 10, 12));

    expect(selected(1)).toBe(10);
  });

  it('forgets every choice when the list fails to load', () => {
    reload(bie(1, 10, 11));
    page = () => throwError(() => new Error('failed'));

    component.load(new HttpParams()).subscribe({error: () => {}});

    expect(component.businessContextSelection).toEqual({});
  });
});
