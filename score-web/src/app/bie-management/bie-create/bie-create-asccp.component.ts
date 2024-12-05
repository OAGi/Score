import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {BieCreateService} from './domain/bie-create.service';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {finalize, switchMap} from 'rxjs/operators';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContext} from '../../context-management/business-context/domain/business-context';
import {ReleaseService} from '../../release-management/domain/release.service';
import {CcList, CcListRequest} from '../../cc-management/cc-list/domain/cc-list';
import {CcListService} from '../../cc-management/cc-list/domain/cc-list.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {PageRequest, PageResponse} from '../../basis/basis';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {SimpleRelease, WorkingRelease} from '../../release-management/domain/release';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {base64Decode, initFilter, loadBranch, loadLibrary, saveBooleanProperty, saveBranch, saveLibrary} from '../../common/utility';
import {Location} from '@angular/common';
import {HttpParams} from '@angular/common/http';
import {AuthService} from '../../authentication/auth.service';
import {Tag} from '../../tag-management/domain/tag';
import {TagService} from '../../tag-management/domain/tag.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {SearchBarComponent} from '../../common/search-bar/search-bar.component';
import {Library} from '../../library-management/domain/library';
import {LibraryService} from '../../library-management/domain/library.service';

@Component({
  selector: 'score-bie-create-asccp',
  templateUrl: './bie-create-asccp.component.html',
  styleUrls: ['./bie-create-asccp.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class BieCreateAsccpComponent implements OnInit {

  title = 'Create BIE';
  subtitle = 'Select Top-Level Concept';

  businessContextIdList: number[] = [];
  businessContextList: BusinessContext[] = [];
  releaseId: number;
  releases: SimpleRelease[] = [];
  libraries: Library[] = [];
  mappedLibraries: {library: Library, selected: boolean}[] = [];

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfCoreComponentWithoutDtColumnsPage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfCoreComponentWithoutDtColumnsPage = columns;
    this.updateTableColumnsForCoreComponentWithoutDtColumnsPage();
  }

  updateTableColumnsForCoreComponentWithoutDtColumnsPage() {
    this.preferencesService.updateTableColumnsForCoreComponentWithoutDtColumnsPage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfCoreComponentWithoutDtColumnsPage;
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
      this.updateTableColumnsForCoreComponentWithoutDtColumnsPage();
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
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    for (const column of this.columns) {
      switch (column.name) {
        case 'Type':
          if (column.selected) {
            displayedColumns.push('type');
          }
          break;
        case 'State':
          if (column.selected) {
            displayedColumns.push('state');
          }
          break;
        case 'DEN':
          if (column.selected) {
            displayedColumns.push('den');
          }
          break;
        case 'Revision':
          if (column.selected) {
            displayedColumns.push('revision');
          }
          break;
        case 'Owner':
          if (column.selected) {
            displayedColumns.push('owner');
          }
          break;
        case 'Module':
          if (column.selected) {
            displayedColumns.push('module');
          }
          break;
        case 'Updated On':
          if (column.selected) {
            displayedColumns.push('lastUpdateTimestamp');
          }
          break;
      }
    }
    return displayedColumns;
  }

  stateList = ['Published', 'Production'];
  dataSource = new MatTableDataSource<CcList>();
  selection = new SelectionModel<CcList>(false, []);
  expandedElement: CcList | null;
  loading = false;

  loginIdList: string[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: CcListRequest;
  highlightTextForModule: string;
  highlightTextForDefinition: string;
  tags: Tag[] = [];
  preferencesInfo: PreferencesInfo;

  workingRelease = WorkingRelease;

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;

  HIDE_UNUSED_PROPERTY_KEY = 'BIE-Settings-Hide-Unused';

  constructor(private bizCtxService: BusinessContextService,
              private releaseService: ReleaseService,
              private libraryService: LibraryService,
              private ccListService: CcListService,
              private accountService: AccountListService,
              private service: BieCreateService,
              private tagService: TagService,
              private auth: AuthService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit() {
    // Init ASCCP table
    this.request = new CcListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.types = ['ASCCP'];
    this.request.states = this.stateList;
    this.request.isBIEUsable = true;

    this.libraryService.getLibraries().subscribe(libraries => {
      this.initLibraries(libraries);

      this.searchBar.showAdvancedSearch =
        (this.route.snapshot.queryParamMap && this.route.snapshot.queryParamMap.get('adv_ser') === 'true');

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

      // Init releases
      this.releaseId = 0;
      this.releases = [];

      forkJoin([
        this.accountService.getAccountNames(),
        this.releaseService.getSimpleReleases(this.request.library.libraryId, ['Published']),
        this.tagService.getTags(),
        this.preferencesService.load(this.auth.getUserToken())
      ]).subscribe(([loginIds, releases, tags, preferencesInfo]) => {
        this.loginIdList.push(...loginIds);
        initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
        initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

        this.tags = tags;
        this.preferencesInfo = preferencesInfo;

        this.initReleases(releases);

        // Load Business Contexts
        this.route.queryParamMap.pipe(
          switchMap((params: ParamMap) => {
            let businessContextIdList = params.get('businessContextIdList');
            if (!businessContextIdList) {
              const q = (this.route.snapshot.queryParamMap) ? this.route.snapshot.queryParamMap.get('q') : undefined;
              const httpParams = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
              businessContextIdList = httpParams.get('businessContextIdList');
            }
            return this.bizCtxService.getBusinessContextsByBizCtxIds(businessContextIdList.split(',').map(e => Number(e)));
          })).subscribe((resp: PageResponse<BusinessContext>) => {
          if (resp.length === 0) {
            this.router.navigateByUrl('/profile_bie/create');
          } else {
            this.businessContextIdList = resp.list.map(e => e.businessContextId);
            this.businessContextList = resp.list;
            this.loadData(true);
          }
        }, err => {
          console.error(err);
        });
      });
    });
  }

  get businessContextNames(): string {
    if (!this.businessContextList) {
      return '';
    } else {
      return this.businessContextList.map(e => e.name).join(', ');
    }
  }

  onPageChange(event: PageEvent) {
    this.loadData();
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

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);
    }
    if (property === 'filters.den' && !!source) {
      this.request.page.sortActive = '';
      this.request.page.sortDirection = '';
      this.sort.active = '';
      this.sort.direction = '';
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
    this.releases = [...releases.filter(e => !e.workingRelease)];
    if (this.releases.length > 0) {
      if (this.request.release.releaseId === 0) {
        const savedReleaseId = loadBranch(this.auth.getUserToken(), 'BIE');
        if (savedReleaseId) {
          this.request.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
          if (!this.request.release) {
            this.request.release = this.releases[0];
            saveBranch(this.auth.getUserToken(), 'BIE', this.request.release.releaseId);
          }
        }
      } else {
        this.request.release = this.releases.filter(e => e.releaseId === this.request.release.releaseId)[0];
      }
    }
    if (!this.request.release || this.request.release.releaseId === 0) {
      this.request.release = this.releases[0];
    }
    initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
  }

  onLibraryChange(library: Library) {
    this.request.library = library;
    this.releaseService.getSimpleReleases(this.request.library.libraryId, ['Published']).subscribe(releases => {
      saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      this.initReleases(releases);
      this.onSearch();
    });
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadData();
  }

  loadData(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.ccListService.getCcList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.paginator.pageIndex = resp.page;

      this.dataSource.data = resp.list.map((elm: CcList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });
      this.highlightTextForModule = this.request.filters.module;
      this.highlightTextForDefinition = this.request.filters.definition;

      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery({
          businessContextIdList: this.businessContextIdList.map(e => '' + e).join(',')
        }) + '&adv_ser=' + (this.searchBar.showAdvancedSearch));
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  back() {
    this.router.navigateByUrl('/profile_bie/create');
  }

  select(row: CcList) {
    this.selection.select(row);
  }

  toggle(row: CcList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CcList) {
    return this.selection.isSelected(row);
  }

  create() {
    const asccpManifestId: number = this.selection.selected[0].manifestId;
    this.service.create(asccpManifestId, this.businessContextIdList)
      .subscribe(resp => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });

        // Issue #1366
        // 'Hide Unused' option must be turned off after BIE creation.
        saveBooleanProperty(this.auth.getUserToken(), this.HIDE_UNUSED_PROPERTY_KEY, false);

        this.router.navigateByUrl('/profile_bie/' + resp.topLevelAsbiepId);
      });
  }

  get isDeveloper(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.roles.includes('developer');
  }

}
