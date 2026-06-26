import {Component, OnInit, QueryList, ViewChild, ViewChildren, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ReleaseSummary} from '../../../release-management/domain/release';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {BieListEntry, BieListRequest} from '../../bie-list/domain/bie-list';
import {BieListService} from '../../bie-list/domain/bie-list.service';
import {SelectionModel} from '@angular/cdk/collections';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {MatDatepicker} from '@angular/material/datepicker';
import {PageRequest} from '../../../basis/basis';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter} from '../../../common/utility';
import {finalize} from 'rxjs/operators';
import {WebPageInfoService} from '../../../basis/basis.service';
import {PreferencesInfo, TableColumnsProperty} from '../../../settings-management/settings-preferences/domain/preferences';
import {AuthService} from '../../../authentication/auth.service';
import {SettingsPreferencesService} from '../../../settings-management/settings-preferences/domain/settings-preferences.service';
import {ScoreTableColumnResizeDirective} from '../../../common/score-table-column-resize/score-table-column-resize.directive';
import {LibrarySummary} from '../../../library-management/domain/library';
import {LibraryService} from '../../../library-management/domain/library.service';

export interface OasDocConfirmMessageDialogResult {
  topLevelAsbiepId: number;
  den: string;
}

// Issue #1347: the DEN of the standard 'Confirm Message' component. Like the 'Include Meta Header'
// ('Meta Header. Meta Header') and 'Pagination Response' ('Pagination Response. Pagination Response')
// pickers, this dialog is locked to a single well-known BIE, so the DEN filter is fixed and the search
// box is disabled.
const CONFIRM_MESSAGE_DEN = 'Confirm Message. Confirm Message';

/**
 * Issue #1347: a BIE-selection dialog for the ConfirmMessage BIE that backs an operation's
 * CONFIRM_MESSAGE error response. It is a clone of the BIE-Express 'Include Meta Header'
 * (MetaHeaderDialogComponent) and 'Pagination Response' (PaginationResponseDialogComponent) pickers —
 * the full BIE-list table with the standard columns and a paginator, locked to a single well-known BIE.
 *
 * Like those pickers, the DEN filter is fixed (to {@code 'Confirm Message. Confirm Message'}) and the
 * search box is disabled, and the picker is locked to one release: the release of the OpenAPI Document's
 * connected BIE (passed in by the caller), shown in the disabled Branch field. The library is fixed to
 * the default ('connectSpec') with no Library selector; its id is still resolved internally because the
 * BIE-list query requires a libraryId. The body type itself is chosen by the inline Error Response
 * selector; this dialog only returns the picked BIE ({@code topLevelAsbiepId} + DEN for display).
 */
@Component({
  standalone: false,
  selector: 'score-oas-doc-confirm-message-dialog',
  templateUrl: './oas-doc-confirm-message-dialog.component.html',
  styleUrls: ['./oas-doc-confirm-message-dialog.component.css']
})
export class OasDocConfirmMessageDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<OasDocConfirmMessageDialogComponent>>(MatDialogRef);
  private bieListService = inject(BieListService);
  private accountService = inject(AccountListService);
  private libraryService = inject(LibraryService);
  private releaseService = inject(ReleaseService);
  private auth = inject(AuthService);
  private preferencesService = inject(SettingsPreferencesService);
  webPageInfo = inject(WebPageInfoService);
  data = inject(MAT_DIALOG_DATA);

  title: string;

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfBiePage;
  }

  updateTableColumnsForBiePage() {
    this.preferencesService.updateTableColumnsForBiePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
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
  selection = new SelectionModel<number>(false, []);
  selectedEntry: BieListEntry;
  loading = false;

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BieListRequest;
  preferencesInfo: PreferencesInfo;
  // Library is fixed to the default ('connectSpec') and the release is locked to the document's
  // connected BIE — there are no selectors for either (see the class doc).
  library: LibrarySummary;
  release: ReleaseSummary;

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;

  constructor() {
    this.title = (this.data && this.data.title) || 'Select ConfirmMessage BIE';
  }

  ngOnInit() {
    this.request = new BieListRequest(undefined,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    // Locked to the standard 'Confirm Message' BIE: a fixed DEN filter and a disabled search box (see
    // the template). Authors narrow the candidates via the advanced filters (e.g. Business Context).
    this.request.filters.den = CONFIRM_MESSAGE_DEN;
    this.request.access = 'CanView';

    // The OpenAPI Document has no release of its own; the picker is locked to the release of the
    // document's connected BIE, passed in by the caller. releaseNum drives the disabled Branch field.
    if (this.data && this.data.releaseId) {
      this.release = new ReleaseSummary();
      this.release.releaseId = this.data.releaseId;
      this.release.releaseNum = this.data.releaseNum;
    }

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

    forkJoin([
      this.accountService.getAccountNames(),
      this.preferencesService.load(this.auth.getUserToken()),
      this.libraryService.getLibrarySummaryList()
    ]).subscribe(([loginIds, preferencesInfo, libraries]) => {
      this.preferencesInfo = preferencesInfo;

      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      // Fix the library to the default ('connectSpec') — there is no Library selector. The id is still
      // needed because the BIE-list query requires a libraryId (it is AND'd with the locked release).
      this.library = (libraries || []).find(e => e.isDefault) || (libraries || [])[0];

      if (this.release && this.release.releaseId) {
        // Release locked to the document's connected BIE.
        this.loadBieList(true);
      } else if (this.library) {
        // Fallback: a document with only bodyless operations carries no connected BIE (hence no
        // release), so default to the library's latest Published, non-working release.
        this.releaseService.getReleaseSummaryList(this.library.libraryId, ['Published']).subscribe(releases => {
          this.release = (releases || []).filter(e => !e.workingRelease)[0];
          this.loadBieList(true);
        });
      } else {
        // No library available (e.g. an empty instance) — loadBieList shows an empty list.
        this.loadBieList(true);
      }
    });
  }

  onPageChange(event: PageEvent) {
    this.loadBieList();
  }

  onChange(property?: string, source?) {
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

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadBieList();
  }

  loadBieList(isInit?: boolean) {
    // The picker is locked to a single library + release; without both, show nothing rather than
    // querying across all releases (or throwing on a missing libraryId).
    if (!this.library || !this.release) {
      this.dataSource.data = [];
      this.paginator.length = 0;
      this.loading = false;
      return;
    }
    this.loading = true;

    this.request.library = this.library;
    this.request.releases = [this.release];
    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.bieListService.getBieListWithRequest(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list;
    }, error => {
      this.dataSource.data = [];
    });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  isDisabled() {
    return this.selection.selected.length === 0 || !this.selectedEntry;
  }

  select(row: BieListEntry) {
    this.selection.select(row.topLevelAsbiepId);
    this.selectedEntry = row;
  }

  toggle(row: BieListEntry) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.topLevelAsbiepId);
      this.selectedEntry = undefined;
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieListEntry) {
    return this.selection.isSelected(row.topLevelAsbiepId);
  }

  onSelect(): void {
    if (this.isDisabled()) {
      return;
    }
    this.dialogRef.close({
      topLevelAsbiepId: this.selectedEntry.topLevelAsbiepId,
      den: this.selectedEntry.den
    } as OasDocConfirmMessageDialogResult);
  }
}
