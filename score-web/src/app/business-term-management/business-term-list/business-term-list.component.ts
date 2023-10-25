import {Component, OnInit, ViewChild} from '@angular/core';
import {BusinessTerm, BusinessTermListRequest} from '../domain/business-term';
import {BusinessTermService} from '../domain/business-term.service';
import {MatDialog} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {PageRequest} from '../../basis/basis';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../common/utility';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'score-business-term',
  templateUrl: './business-term-list.component.html',
  styleUrls: ['./business-term-list.component.css']
})
export class BusinessTermListComponent implements OnInit {

  title = 'Business Term';
  displayedColumns: string[] = [
    'select', 'businessTerm', 'externalReferenceUri', 'externalReferenceId', 'definition',
    'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BusinessTerm>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: BusinessTermListRequest;

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: BusinessTermService,
              private accountService: AccountListService,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.request = new BusinessTermListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadBusinessTermList();
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });

    this.loadBusinessTermList(true);
  }

  onPageChange(event: PageEvent) {
    this.loadBusinessTermList();
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

  loadBusinessTermList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getBusinessTermList(this.request, null).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: BusinessTerm) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
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

  select(row: BusinessTerm) {
    if (!row.used) {
      this.selection.select(row.businessTermId);
    }
  }

  toggle(row: BusinessTerm) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.businessTermId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BusinessTerm) {
    return this.selection.isSelected(row.businessTermId);
  }

  discard() {
    const businessTermIds = this.selection.selected;
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard ' + (businessTermIds.length > 1 ? 'business terms' : 'business term') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected ' + (businessTermIds.length > 1 ? 'business terms' : 'business term') + '?',
      'The ' + (businessTermIds.length > 1 ? 'business terms' : 'business term') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...businessTermIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadBusinessTermList();
          }, err => {
            this.snackBar.open('Discard\'s forbidden! The business term is used.', '', {
              duration: 5000,
            });
          });
        }
      });
  }

}
