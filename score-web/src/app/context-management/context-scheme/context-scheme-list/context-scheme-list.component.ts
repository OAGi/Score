import {Component, OnInit, ViewChild} from '@angular/core';
import {ContextScheme, ContextSchemeListRequest} from '../domain/context-scheme';
import {ContextSchemeService} from '../domain/context-scheme.service';
import {
  MatDialog,
  MatDialogConfig,
  MatPaginator,
  MatSnackBar,
  MatSort,
  MatTableDataSource,
  PageEvent,
  SortDirection
} from '@angular/material';
import {SelectionModel} from '@angular/cdk/collections';
import {PageRequest} from '../../../basis/basis';
import {MatDatepickerInputEvent} from '@angular/material/typings/datepicker';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../../common/utility';
import {ConfirmDialogConfig} from '../../../common/confirm-dialog/confirm-dialog.domain';
import {ConfirmDialogComponent} from '../../../common/confirm-dialog/confirm-dialog.component';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';

@Component({
  selector: 'score-context-scheme',
  templateUrl: './context-scheme-list.component.html',
  styleUrls: ['./context-scheme-list.component.css']
})
export class ContextSchemeListComponent implements OnInit {

  title = 'Context Schemes';
  displayedColumns: string[] = [
    'select', 'schemeName', 'ctxCategoryName', 'schemeId', 'schemeAgencyId',
    'schemeVersionId', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<ContextScheme>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: ContextSchemeListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: ContextSchemeService,
              private accountService: AccountListService,
              private dialog: MatDialog,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.request = new ContextSchemeListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });

    this.loadContextSchemeList(true);
  }

  onPageChange(event: PageEvent) {
    this.loadContextSchemeList();
  }

  onChange() {
    this.paginator.pageIndex = 0;
    this.loadContextSchemeList();
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

  loadContextSchemeList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getContextSchemeList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: ContextScheme) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        elm.ctxSchemeValues = [];
        return elm;
      });
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.filter(row => !row.used).length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: ContextScheme) {
    if (!row.used) {
      this.selection.select(row.ctxSchemeId);
    }
  }

  toggle(row: ContextScheme) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.ctxSchemeId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: ContextScheme) {
    return this.selection.isSelected(row.ctxSchemeId);
  }

  discard() {
    const ctxSchemeIds = this.selection.selected;

    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Discard Context ' + (ctxSchemeIds.length > 1 ? 'Schemes' : 'Scheme') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected context ' + (ctxSchemeIds.length > 1 ? 'schemes' : 'scheme') + '?',
      'The context ' + (ctxSchemeIds.length > 1 ? 'schemes' : 'scheme') + ' will be permanently removed.'
    ];

    dialogConfig.data.action = 'Discard';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...ctxSchemeIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 1000,
            });
            this.selection.clear();
            this.loadContextSchemeList();
          });
        }
      });
  }

}
