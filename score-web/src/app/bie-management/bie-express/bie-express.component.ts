import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {BieListEntry, BieListRequest} from '../bie-list/domain/bie-list';
import {BieExpressService} from './domain/bie-express.service';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {BieExpressOption} from './domain/generate-expression';
import {saveAs} from 'file-saver';
import {MetaHeaderDialogComponent} from './meta-header-dialog/meta-header-dialog.component';
import {PaginationResponseDialogComponent} from './pagination-response-dialog/pagination-response-dialog.component';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepicker} from '@angular/material/datepicker';
import {PageRequest} from '../../basis/basis';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter, loadBranch, loadLibrary, saveBranch, saveLibrary} from '../../common/utility';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';
import {SelectionModel} from '@angular/cdk/collections';
import {WebPageInfoService} from '../../basis/basis.service';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {SearchBarComponent} from '../../common/search-bar/search-bar.component';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryService} from '../../library-management/domain/library.service';

@Component({
  selector: 'score-bie-express',
  templateUrl: './bie-express.component.html',
  styleUrls: ['./bie-express.component.css']
})
export class BieExpressComponent implements OnInit {

  title = 'Express BIE';
  subtitle = 'Selected Top-Level ASBIEPs';

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

  dataSource = new MatTableDataSource<BieListEntry>();
  selection = new SelectionModel<number>(true, []);
  businessContextSelection = {};
  loading = false;

  loginIdList: string[] = [];
  releases: ReleaseSummary[] = [];
  libraries: LibrarySummary[] = [];
  mappedLibraries: {library: LibrarySummary, selected: boolean}[] = [];
  selectedRelease: ReleaseSummary;
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<ReleaseSummary[]> = new ReplaySubject<ReleaseSummary[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BieListRequest;
  preferencesInfo: PreferencesInfo;

  option: BieExpressOption;
  openApiFormats: string[] = ['YAML', 'JSON'];
  odfFormats: string[] = ['ODS', 'FODS', 'XLSX'];

  // Memorizer
  previousPackageOption: string;

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;

  constructor(private service: BieExpressService,
              private bieListService: BieListService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private libraryService: LibraryService,
              private auth: AuthService,
              private preferencesService: SettingsPreferencesService,
              private dialog: MatDialog,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit() {
    this.option = new BieExpressOption();
    this.option.bieDefinition = true;
    this.option.expressionOption = 'XML';
    this.option.packageOption = 'ALL';
    // Default OpenAPI expression format is 'YAML'.
    this.option.openAPIExpressionFormat = 'YAML';
    // Default ODF expression format is 'ODS'.
    this.option.odfExpressionFormat = 'ODS';

    // Init BIE table
    this.request = new BieListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.access = 'CanView';
    this.request.excludePropertyTerms = ['Meta Header', 'Pagination Response'];

    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
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
        this.paginator.pageIndex = 0;
        this.loadBieList();
      });

      forkJoin([
        this.accountService.getAccountNames(),
        this.releaseService.getReleaseSummaryList(this.request.library.libraryId, ['Published']),
        this.preferencesService.load(this.auth.getUserToken())
      ]).subscribe(([loginIds, releases, preferencesInfo]) => {
        this.preferencesInfo = preferencesInfo;

        this.loginIdList.push(...loginIds);
        initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
        initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

        this.initReleases(releases);

        this.loadBieList(true);
      });
    });
  }

  onPageChange(event: PageEvent) {
    this.loadBieList();
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

  initLibraries(libraries: LibrarySummary[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.request.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
      }
      if (!this.request.library || !this.request.library.libraryId) {
        this.request.library = this.libraries[0];
      }
      if (this.request.library) {
        saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      }
      this.mappedLibraries = this.libraries.map(e => {
        return {library: e, selected: (this.request.library.libraryId === e.libraryId)};
      });
    }
  }

  initReleases(releases: ReleaseSummary[]) {
    this.releases = releases.filter(e => !e.workingRelease);
    initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
    const savedReleaseId = loadBranch(this.auth.getUserToken(), 'BIE');
    if (savedReleaseId) {
      this.selectedRelease = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
      if (!this.selectedRelease) {
        this.selectedRelease = this.releases[0];
        saveBranch(this.auth.getUserToken(), 'BIE', this.selectedRelease.releaseId);
      }
    } else {
      this.selectedRelease = this.releases[0];
    }
  }

  onLibraryChange(library: LibrarySummary) {
    this.request.library = library;
    this.releaseService.getReleaseSummaryList(this.request.library.libraryId, ['Published']).subscribe(releases => {
      saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      this.initReleases(releases);
      this.onSearch();
    });
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.selection.clear();
    this.loadBieList();
  }

  loadBieList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);
    this.request.releases = (!!this.selectedRelease) ? [this.selectedRelease, ] : [];

    this.bieListService.getBieListWithRequest(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list;
      this.dataSource.data.forEach((elm: BieListEntry) => {
        this.businessContextSelection[elm.topLevelAsbiepId] = elm.businessContextList[0];
      });

      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0],
          this.request.toQuery() + '&adv_ser=' + (this.searchBar.showAdvancedSearch));
      }
    }, error => {
      this.dataSource.data = [];
      this.businessContextSelection = {};
    });
  }

  select(row: BieListEntry) {
    this.selection.select(row.topLevelAsbiepId);
  }

  toggle(row: BieListEntry) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.topLevelAsbiepId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieListEntry) {
    return this.selection.isSelected(row.topLevelAsbiepId);
  }

  generate() {
    const selectedTopLevelAsbiepIds = this.selection.selected;

    this.option.filenames = {};
    this.option.bizCtxIds = {};
    for (const selectedTopLevelAsbiepId of selectedTopLevelAsbiepIds) {
      const filename = this.getFilename(selectedTopLevelAsbiepId);
      this.option.filenames[selectedTopLevelAsbiepId] = filename;

      const selectedBusinessContext = this.businessContextSelection[selectedTopLevelAsbiepId];
      this.option.bizCtxIds[selectedTopLevelAsbiepId] = selectedBusinessContext.businessContextId;
    }

    this.loading = true;
    this.service.generate(selectedTopLevelAsbiepIds, this.option).subscribe(resp => {

      const blob = new Blob([resp.body], {type: resp.headers.get('Content-Type')});
      saveAs(blob, this._getFilenameFromContentDisposition(resp));

      this.loading = false;
    }, err => {
      this.loading = false;
    });
  }

  getFilename(topLevelAsbiepId: number): string {
    const topLevelAsbiep = this.dataSource.data.filter(e => e.topLevelAsbiepId === topLevelAsbiepId)[0];
    const separator = '';

    let filename = topLevelAsbiep.propertyTerm.trim().split(' ').join(separator);
    if (this.option.includeBusinessContextInFilename) {
      const selectedBusinessContext = this.businessContextSelection[topLevelAsbiepId];
      if (!!selectedBusinessContext) {
        filename += '-' + selectedBusinessContext.name.trim().split(' ').join(separator);
      }
    }
    if (this.option.includeVersionInFilename) {
      if (!!topLevelAsbiep.version) {
        const versionSeparator = '_';
        filename += '-' + topLevelAsbiep.version.trim().split(' ').join(versionSeparator)
          .split('.').join(versionSeparator);
      }
    }
    return filename;
  }

  _getFilenameFromContentDisposition(resp) {
    const contentDisposition = resp.headers.get('Content-Disposition') || '';
    const matches = /filename=([^;]+)/ig.exec(contentDisposition);
    return (matches[1] || 'untitled').replace(/\"/gi, '').trim();
  }

  toggleMetaHeaderOption(event, disabled: boolean,
                         includeMetaHeaderForJsonPropertyKey: string,
                         metaHeaderTopLevelAsbiepIdPropertyKey: string) {
    if (disabled) {
      return;
    }

    if (this.option[metaHeaderTopLevelAsbiepIdPropertyKey]) {
      this.option[includeMetaHeaderForJsonPropertyKey] = false;
      this.option[metaHeaderTopLevelAsbiepIdPropertyKey] = undefined;

      this.option.packageOption = this.previousPackageOption;
    } else {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.width = window.innerWidth + 'px';
      dialogConfig.data = {
        library: this.request.library,
        release: this.selectedRelease
      };
      const dialogRef = this.dialog.open(MetaHeaderDialogComponent, dialogConfig);
      dialogRef.afterClosed().subscribe(selectedTopLevelAsbiepId => {
        if (selectedTopLevelAsbiepId) {
          this.option[includeMetaHeaderForJsonPropertyKey] = true;
          this.option[metaHeaderTopLevelAsbiepIdPropertyKey] = selectedTopLevelAsbiepId;

          if (includeMetaHeaderForJsonPropertyKey.includes('GetTemplate')) {
            this.option.suppressRootPropertyForOpenAPI30GetTemplate = false;
          } else if (includeMetaHeaderForJsonPropertyKey.includes('PostTemplate')) {
            this.option.suppressRootPropertyForOpenAPI30PostTemplate = false;
          }
          this.previousPackageOption = this.option.packageOption;
          this.option.packageOption = 'EACH';
        } else {
          this.option[includeMetaHeaderForJsonPropertyKey] = false;
          this.option[metaHeaderTopLevelAsbiepIdPropertyKey] = undefined;
        }
      });
    }
  }

  togglePaginationResponseOption(event, disabled: boolean,
                                 includePaginationResponseForJsonPropertyKey: string,
                                 paginationResponseTopLevelAsbiepIdPropertyKey: string) {
    if (disabled) {
      return;
    }

    if (this.option[paginationResponseTopLevelAsbiepIdPropertyKey]) {
      this.option[includePaginationResponseForJsonPropertyKey] = false;
      this.option[paginationResponseTopLevelAsbiepIdPropertyKey] = undefined;

      this.option.packageOption = this.previousPackageOption;
    } else {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.width = window.innerWidth + 'px';
      dialogConfig.data = {
        library: this.request.library,
        release: this.selectedRelease
      };
      const dialogRef = this.dialog.open(PaginationResponseDialogComponent, dialogConfig);
      dialogRef.afterClosed().subscribe(selectedTopLevelAsbiepId => {
        if (selectedTopLevelAsbiepId) {
          this.option[includePaginationResponseForJsonPropertyKey] = true;
          this.option[paginationResponseTopLevelAsbiepIdPropertyKey] = selectedTopLevelAsbiepId;

          if (includePaginationResponseForJsonPropertyKey.includes('GetTemplate')) {
            this.option.suppressRootPropertyForOpenAPI30GetTemplate = false;
          } else if (includePaginationResponseForJsonPropertyKey.includes('PostTemplate')) {
            this.option.suppressRootPropertyForOpenAPI30PostTemplate = false;
          }
          this.previousPackageOption = this.option.packageOption;
          this.option.packageOption = 'EACH';
        } else {
          this.option[includePaginationResponseForJsonPropertyKey] = false;
          this.option[paginationResponseTopLevelAsbiepIdPropertyKey] = undefined;
        }
      });
    }
  }

  expressionOptionChange() {
    if (this.option.expressionOption === 'ODF' || this.option.expressionOption === 'AVRO') {
      this.option.packageOption = 'EACH';
    }

    if (this.option.expressionOption === 'JSON') {
      if (this.option.includeMetaHeaderForJson || this.option.includePaginationResponseForJson) {
        this.option.packageOption = 'EACH';
      }
    }

    if (this.option.expressionOption !== 'XML') {
      this.option.bieCctsMetaData = false;
      this.option.includeCctsDefinitionTag = false;
      this.option.bieGuid = false;
      this.option.businessContext = false;
      this.option.bieOagiScoreMetaData = false;
      this.option.includeWhoColumns = false;
      this.option.basedCcMetaData = false;
    }
  }

  bieAnnotationChange() {
    if (!this.option.bieCctsMetaData) {
      this.option.includeCctsDefinitionTag = false;
    }

    if (!this.option.bieOagiScoreMetaData) {
      this.option.includeWhoColumns = false;
    }
  }
}
