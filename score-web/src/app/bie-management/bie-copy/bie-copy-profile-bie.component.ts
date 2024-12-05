import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {BieCopyService} from './domain/bie-copy.service';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {finalize, switchMap} from 'rxjs/operators';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContext} from '../../context-management/business-context/domain/business-context';
import {BieList, BieListRequest} from '../bie-list/domain/bie-list';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../basis/basis';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {base64Decode, initFilter, loadLibrary, saveBooleanProperty, saveBranch, saveLibrary} from '../../common/utility';
import {Location} from '@angular/common';
import {HttpParams} from '@angular/common/http';
import {SimpleRelease} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {SearchBarComponent} from '../../common/search-bar/search-bar.component';
import {MultiActionsSnackBarComponent} from '../../common/multi-actions-snack-bar/multi-actions-snack-bar.component';
import {Library} from '../../library-management/domain/library';
import {LibraryService} from '../../library-management/domain/library.service';

@Component({
  selector: 'score-bie-create-asccp',
  templateUrl: './bie-copy-profile-bie.component.html',
  styleUrls: ['./bie-copy-profile-bie.component.css']
})
export class BieCopyProfileBieComponent implements OnInit {

  title = 'Copy BIE';
  subtitle = 'Select BIE';

  bizCtxIds: number[] = [];
  bizCtxList: BusinessContext[] = [];

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfBiePage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfBiePage = columns;
    this.updateTableColumnsForBiePage();
  }

  updateTableColumnsForBiePage() {
    this.preferencesService.updateTableColumnsForBiePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfBiePage;
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    const updatedColumnsWithWidth = updatedColumns.map(column => ({
      name: column.name,
      selected: column.selected,
      width: this.width(column.name)
    }));

    this.columns = updatedColumnsWithWidth;
  }

  onResizeWidth($event) {
    switch ($event.name) {
      case 'Updated on':
        this.setWidth('Updated On', $event.width);
        break;

      default:
        this.setWidth($event.name, $event.width);
        break;
    }
  }

  setWidth(name: string, width: number | string) {
    const matched = this.columns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.updateTableColumnsForBiePage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (this.preferencesInfo) {
      for (const column of this.columns) {
        switch (column.name) {
          case 'State':
            if (column.selected) {
              displayedColumns.push('state');
            }
            break;
          case 'Branch':
            if (column.selected) {
              displayedColumns.push('branch');
            }
            break;
          case 'DEN':
            if (column.selected) {
              displayedColumns.push('den');
            }
            break;
          case 'Owner':
            if (column.selected) {
              displayedColumns.push('owner');
            }
            break;
          case 'Business Contexts':
            if (column.selected) {
              displayedColumns.push('businessContexts');
            }
            break;
          case 'Version':
            if (column.selected) {
              displayedColumns.push('version');
            }
            break;
          case 'Status':
            if (column.selected) {
              displayedColumns.push('status');
            }
            break;
          case 'Business Term':
            if (column.selected) {
              displayedColumns.push('bizTerm');
            }
            break;
          case 'Remark':
            if (column.selected) {
              displayedColumns.push('remark');
            }
            break;
          case 'Updated On':
            if (column.selected) {
              displayedColumns.push('lastUpdateTimestamp');
            }
            break;
        }
      }
    }
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<BieList>();
  selection = new SelectionModel<BieList>(false, []);
  loading = false;

  loginIdList: string[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BieListRequest;
  preferencesInfo: PreferencesInfo;

  releases: SimpleRelease[] = [];
  libraries: Library[] = [];
  mappedLibraries: {library: Library, selected: boolean}[] = [];

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;

  HIDE_UNUSED_PROPERTY_KEY = 'BIE-Settings-Hide-Unused';

  constructor(private bizCtxService: BusinessContextService,
              private service: BieCopyService,
              private bieListService: BieListService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private libraryService: LibraryService,
              private auth: AuthService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit() {
    this.request = new BieListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.access = 'CanView';

    this.libraryService.getLibraries().subscribe(libraries => {
      this.initLibraries(libraries);

      // The value should be 'true' unless 'adv_ser' is false.
      this.searchBar.showAdvancedSearch =
        (!this.route.snapshot.queryParamMap || (!this.route.snapshot.queryParamMap.get('adv_ser')) ||
          this.route.snapshot.queryParamMap.get('adv_ser') === 'true');

      this.paginator.pageIndex = this.request.page.pageIndex;
      this.paginator.pageSize = this.request.page.pageSize;
      this.paginator.length = 0;

      this.sort.active = this.request.page.sortActive;
      this.sort.direction = this.request.page.sortDirection as SortDirection;
      // Prevent the sorting event from being triggered if any columns are currently resizing.
      const originalSort = this.sort.sort;
      this.sort.sort = (sortChange) => {
        if (this.tableColumnResizeDirectives &&
          this.tableColumnResizeDirectives.filter(e => e.resizing).length > 0) {
          return;
        }
        originalSort.apply(this.sort, [sortChange]);
      };
      this.sort.sortChange.subscribe(() => {
        this.onSearch();
      });

      // Load Business Contexts
      this.route.queryParamMap.pipe(
        switchMap((params: ParamMap) => {
          let bizCtxIds = params.get('bizCtxIds');
          if (!bizCtxIds) {
            const q = (this.route.snapshot.queryParamMap) ? this.route.snapshot.queryParamMap.get('q') : undefined;
            const httpParams = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
            bizCtxIds = httpParams.get('bizCtxIds');
          }

          return forkJoin([
            this.bizCtxService.getBusinessContextsByBizCtxIds(bizCtxIds.split(',').map(e => Number(e))),
            this.accountService.getAccountNames(),
            this.releaseService.getSimpleReleases(this.request.library.libraryId, ['Published']),
            this.preferencesService.load(this.auth.getUserToken())
          ]);
        })).subscribe(([resp, loginIds, releases, preferencesInfo]) => {
        this.preferencesInfo = preferencesInfo;

        this.loginIdList.push(...loginIds);
        initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
        initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

        this.initReleases(releases);

        this.bizCtxIds = resp.list.map(e => e.businessContextId);
        this.bizCtxList = resp.list;
        // Issue #1625
        this.request.filters.businessContext = this.bizCtxList.map(e => e.name).join(', ');

        this.loadBieList(true);
      }, err => {
        console.error(err);
      });
    });
  }

  get businessContextNames(): string {
    if (!this.bizCtxList) {
      return '';
    } else {
      return this.bizCtxList.map(e => e.name).join(', ');
    }
  }

  onPageChange(event: PageEvent) {
    this.loadBieList();
  }

  onChange(property?: string, source?) {
    if (property === 'filters.den' && !!source) {
      this.request.page.sortActive = '';
      this.request.page.sortDirection = '';
      this.sort.active = '';
      this.sort.direction = '';
    }
  }

  onReleaseChange(source) {
    saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);

    this.paginator.pageIndex = 0;
    this.loadBieList();
  }

  onDateEvent(type: string, event: MatDatepickerInputEvent<Date>) {
    switch (type) {
      case 'startDate':
        this.request.updatedDate.start = new Date(event.value);
        break;
      case 'endDate':
        this.request.updatedDate.end = new Date(event.value);
        break;
    }
  }

  reset(type: string) {
    switch (type) {
      case 'startDate':
        this.dateStart.select(undefined);
        this.request.updatedDate.start = null;
        break;
      case 'endDate':
        this.dateEnd.select(undefined);
        this.request.updatedDate.end = null;
        break;
    }
  }

  toggleAllForReleaseFilter(selectAllValue: boolean) {
    if (selectAllValue) {
      this.request.releases = this.releases;
    } else {
      this.request.releases = [];
    }
  }

  initLibraries(libraries: Library[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.request.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
        saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      }
      if (!this.request.library || this.request.library.libraryId === 0) {
        this.request.library = this.libraries[0];
      }
      this.mappedLibraries = this.libraries.map(e => {
        return {library: e, selected: (this.request.library.libraryId === e.libraryId)};
      });
    }
  }

  initReleases(releases: SimpleRelease[]) {
    this.releases = releases.filter(e => !e.workingRelease && e.state === 'Published');
    initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
  }

  onLibraryChange(library: Library) {
    this.request.library = library;
    this.request.releases = [];
    this.releaseService.getSimpleReleases(this.request.library.libraryId, ['Published']).subscribe(releases => {
      saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      this.initReleases(releases);
      this.onSearch();
    });
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadBieList();
  }

  loadBieList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.bieListService.getBieListWithRequest(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: BieList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery({
          bizCtxIds: this.bizCtxIds.map(e => '' + e).join(',')
        }) + '&adv_ser=' + (this.searchBar.showAdvancedSearch));
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  back() {
    this.router.navigateByUrl('/profile_bie/copy');
  }

  select(row: BieList) {
    this.selection.select(row);
  }

  toggle(row: BieList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieList) {
    return this.selection.isSelected(row);
  }

  copy() {
    const topLevelAsbiepId: number = this.selection.selected[0].topLevelAsbiepId;
    this.service.copy(topLevelAsbiepId, this.bizCtxIds).subscribe(_ => {
      this.snackBar.openFromComponent(MultiActionsSnackBarComponent, {
        data: {
          titleIcon: 'info',
          title: 'Request Received',
          message: 'This may take a moment, so please check back shortly.'
        }
      });

      // Issue #1366
      // 'Hide Unused' option must be turned off after BIE creation.
      saveBooleanProperty(this.auth.getUserToken(), this.HIDE_UNUSED_PROPERTY_KEY, false);

      this.router.navigateByUrl('/profile_bie');
    });
  }

}
