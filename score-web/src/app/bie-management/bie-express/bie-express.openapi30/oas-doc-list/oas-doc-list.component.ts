import {Component, OnInit, ViewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {OasDoc, OasDocListRequest} from '../domain/openapi-doc';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {AccountListService} from '../../../../account-management/domain/account-list.service';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmDialogService} from '../../../../common/confirm-dialog/confirm-dialog.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {OpenAPIService} from '../domain/openapi.service';
import {PageRequest} from '../../../../basis/basis';
import {initFilter} from '../../../../common/utility';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {finalize} from 'rxjs/operators';
import {MatMultiSort, MatMultiSortTableDataSource, TableData} from 'ngx-mat-multi-sort';

@Component({
  selector: 'score-oas-doc-list',
  templateUrl: './oas-doc-list.component.html',
  styleUrls: ['./oas-doc-list.component.css']
})
export class OasDocListComponent implements OnInit {

  title = 'OpenAPI Document';
  displayedColumns = [
    {id: 'select', name: ''},
    {id: 'title', name: 'Title'},
    {id: 'openAPIVersion', name: 'OpenAPI Version'},
    {id: 'version', name: 'Version'},
    {id: 'licenseName', name: 'License Name'},
    {id: 'description', name: 'Description'},
    {id: 'lastUpdateTimestamp', name: 'Last Update Timestamp'},
  ];
  table: TableData<OasDoc>;
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: OasDocListRequest;
  @ViewChild(MatMultiSort, {static: true}) sort: MatMultiSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private openAPIService: OpenAPIService,
              private accountService: AccountListService,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    const localStorageKey = 'X-Score-Table[OasDocList]';
    const value = JSON.parse(localStorage.getItem(localStorageKey)!);
    this.table = new TableData<OasDoc>((value) ? value._columns : this.displayedColumns, {localStorageKey: localStorageKey});
    this.table.dataSource = new MatMultiSortTableDataSource<OasDoc>(this.sort, false);

    this.request = new OasDocListRequest(this.route.snapshot.queryParamMap,
      new PageRequest(['lastUpdateTimestamp'], ['desc'], 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.table.sortParams = this.request.page.sortActives;
    this.table.sortDirs = this.request.page.sortDirections;
    this.table.sortObservable.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadOasDocList();
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });

    this.loadOasDocList(true);
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
        this.request.updatedDate.start = null;
        break;
      case 'endDate':
        this.request.updatedDate.end = null;
        break;
    }
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
