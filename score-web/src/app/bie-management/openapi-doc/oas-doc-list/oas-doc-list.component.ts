import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {Location} from '@angular/common';
import {SelectionModel} from '@angular/cdk/collections';
import {OasDoc, OasDocListRequest} from '../domain/openapi-doc';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {OpenAPIService} from '../domain/openapi.service';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {finalize} from 'rxjs/operators';
import {MatMultiSort, MatMultiSortTableDataSource, TableData} from 'ngx-mat-multi-sort';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {PageRequest} from '../../../basis/basis';
import {initFilter} from '../../../common/utility';
import {
  PreferencesInfo,
  TableColumnsInfo,
  TableColumnsProperty
} from '../../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../../settings-management/settings-preferences/domain/settings-preferences.service';
import {AuthService} from '../../../authentication/auth.service';
import {ScoreTableColumnResizeDirective} from '../../../common/score-table-column-resize/score-table-column-resize.directive';

@Component({
  selector: 'score-oas-doc-list',
  templateUrl: './oas-doc-list.component.html',
  styleUrls: ['./oas-doc-list.component.css']
})
export class OasDocListComponent implements OnInit {

  title = 'OpenAPI Document';

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfOpenApiDocumentPage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfOpenApiDocumentPage = columns;
    this.updateTableColumnsForOpenApiDocumentPage();
  }

  updateTableColumnsForOpenApiDocumentPage() {
    this.preferencesService.updateTableColumnsForOpenApiDocumentPage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfOpenApiDocumentPage;
    this.onColumnsChange(this.columns);
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    const updatedColumnsWithWidth = updatedColumns.map(column => ({
      name: column.name,
      selected: column.selected,
      width: this.width(column.name)
    }));

    this.columns = updatedColumnsWithWidth;

    let columns = [];
    for (const tableColumn of this.table.columns) {
      for (const updatedColumn of updatedColumns) {
        if (tableColumn.name === updatedColumn.name) {
          tableColumn.isActive = updatedColumn.selected;
        }
      }
      columns.push(tableColumn);
    }

    this.table.columns = columns;
    this.table.displayedColumns = this.displayedColumns;
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
      this.updateTableColumnsForOpenApiDocumentPage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  defaultDisplayedColumns = [
    {id: 'select', name: '', isActive: true},
    {id: 'title', name: 'Title', isActive: true},
    {id: 'openAPIVersion', name: 'OpenAPI Version', isActive: true},
    {id: 'version', name: 'Version', isActive: true},
    {id: 'licenseName', name: 'License Name', isActive: true},
    {id: 'description', name: 'Description', isActive: true},
    {id: 'lastUpdateTimestamp', name: 'Last Update Timestamp', isActive: true},
  ];

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (this.preferencesInfo) {
      for (const column of this.columns) {
        switch (column.name) {
          case 'Title':
            if (column.selected) {
              displayedColumns.push('title');
            }
            break;
          case 'OpenAPI Version':
            if (column.selected) {
              displayedColumns.push('openAPIVersion');
            }
            break;
          case 'Version':
            if (column.selected) {
              displayedColumns.push('version');
            }
            break;
          case 'License Name':
            if (column.selected) {
              displayedColumns.push('licenseName');
            }
            break;
          case 'Description':
            if (column.selected) {
              displayedColumns.push('description');
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

  table: TableData<OasDoc>;
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: OasDocListRequest;
  preferencesInfo: PreferencesInfo;

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatMultiSort, {static: true}) sort: MatMultiSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;

  constructor(private openAPIService: OpenAPIService,
              private accountService: AccountListService,
              private auth: AuthService,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    this.table = new TableData<OasDoc>(this.defaultDisplayedColumns, {});
    this.table.dataSource = new MatMultiSortTableDataSource<OasDoc>(this.sort, false);

    this.request = new OasDocListRequest(this.route.snapshot.queryParamMap,
      new PageRequest(['lastUpdateTimestamp'], ['desc'], 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.table.sortParams = this.request.page.sortActives;
    this.table.sortDirs = this.request.page.sortDirections;
    // Prevent the sorting event from being triggered if any columns are currently resizing.
    const originalSort = this.sort.sort;
    this.sort.sort = (sortChange) => {
      if (this.tableColumnResizeDirectives &&
        this.tableColumnResizeDirectives.filter(e => e.resizing).length > 0) {
        return;
      }
      originalSort.apply(this.sort, [sortChange]);
    };
    this.table.sortObservable.subscribe(() => {
      this.onSearch();
    });

    forkJoin([
      this.accountService.getAccountNames(),
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([loginIds, preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;
      this.onColumnsChange(this.preferencesInfo.tableColumnsInfo.columnsOfOpenApiDocumentPage);

      this.loginIdList.push(...loginIds);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.loadOasDocList(true);
    });
  }

  onPageChange(event: PageEvent) {
    this.loadOasDocList();
  }

  onChange(property?: string, source?) {
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

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadOasDocList();
  }

  loadOasDocList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.table.sortParams, this.table.sortDirs,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.openAPIService.getOasDocList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.table.dataSource.data = resp.list.map((elm: OasDoc) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.table.dataSource.data = [];
    });
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.table.dataSource.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.table.dataSource.data.forEach(row => this.select(row));
  }

  select(row: OasDoc) {
    this.selection.select(row.oasDocId);
  }

  toggle(row: OasDoc) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.oasDocId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: OasDoc) {
    return this.selection.isSelected(row.oasDocId);
  }

  discard() {
    const oasDocIds = this.selection.selected;
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard ' + (oasDocIds.length > 1 ? 'oas docs' : 'oas doc') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected ' + (oasDocIds.length > 1 ? 'oas docs' : 'oas doc') + '?',
      'The ' + (oasDocIds.length > 1 ? 'oas docs' : 'oas doc') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.openAPIService.delete(...oasDocIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadOasDocList();
          }, err => {
            this.snackBar.open('Discard\'s forbidden! The oas doc is used.', '', {
              duration: 5000,
            });
          });
        }
      });
  }
}
