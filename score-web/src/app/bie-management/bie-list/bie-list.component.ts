import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {BieListService} from './domain/bie-list.service';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogConfig,
  MatPaginator,
  MatSnackBar,
  MatSort,
  MatTableDataSource,
  PageEvent
} from '@angular/material';
import {SelectionModel} from '@angular/cdk/collections';
import {Router} from '@angular/router';
import {BieList, BieListRequest} from './domain/bie-list';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepickerInputEvent} from '@angular/material/typings/datepicker';
import {PageRequest} from '../../basis/basis';
import {AuthService} from '../../authentication/auth.service';
import {TransferOwnershipDialogComponent} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AccountList} from '../../account-management/domain/accounts';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../common/utility';

@Component({
  selector: 'srt-bie-list',
  templateUrl: './bie-list.component.html',
  styleUrls: ['./bie-list.component.css']
})
export class BieListComponent implements OnInit {
  title = 'BIEs';

  displayedColumns: string[] = [
    'select', 'propertyTerm', 'version', 'status', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BieList>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['Initiating', 'Editing', 'Candidate', 'Published'];
  request: BieListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: BieListService,
              private accountService: AccountListService,
              private auth: AuthService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private router: Router) {
  }

  ngOnInit() {
    this.request = new BieListRequest();

    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.paginator.length = 0;

    this.sort.active = 'lastUpdateTimestamp';
    this.sort.direction = 'desc';
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });
    this.onChange();
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  onPageChange(event: PageEvent) {
    this.loadBieList();
  }

  onChange() {
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
        this.request.updatedDate.start = null;
        break;
      case 'endDate':
        this.request.updatedDate.end = null;
        break;
    }
  }

  loadBieList() {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getBieListWithRequest(this.request)
      .subscribe(resp => {
        this.paginator.length = resp.length;
        this.dataSource.data = resp.list.map((elm: BieList) => {
          elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
          return elm;
        });
        this.loading = false;
      });
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.filter(row => row.access === 'CanEdit').length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: BieList) {
    if (row.access === 'CanEdit') {
      this.selection.select(row.topLevelAbieId);
    }
  }

  toggle(row: BieList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.topLevelAbieId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieList) {
    return this.selection.isSelected(row.topLevelAbieId);
  }

  discard() {
    this.openDialogBieDiscard();
  }

  openDialogBieDiscard() {
    const topLevelAbieIds = this.selection.selected;
    const dialogConfig = new MatDialogConfig();
    const dialogRef = this.dialog.open(DialogDiscardBieDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.delete(topLevelAbieIds).subscribe(_ => {
          this.snackBar.open('Discarded', '', {
            duration: 1000,
          });
          this.selection.clear();
          this.loadBieList();
        });
      }
    });
  }

  openTransferDialog(topLevelAbieId, $event) {
    $event.preventDefault();
    $event.stopPropagation();

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnership(topLevelAbieId, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 1000,
          });
          this.selection.clear();
          this.loadBieList();
        });
      }
    });
  }
}

@Component({
  selector: 'srt-dialog-bie-dialog-detail',
  templateUrl: 'dialog-discard-bie-dialog.html',
})
export class DialogDiscardBieDialogComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}
