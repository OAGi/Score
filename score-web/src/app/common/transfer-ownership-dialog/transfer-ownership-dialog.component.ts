import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {AccountList, AccountListRequest} from '../../account-management/domain/accounts';
import {PageRequest} from '../../basis/basis';
import {SelectionModel} from '@angular/cdk/collections';
import {AuthService} from '../../authentication/auth.service';

@Component({
  selector: 'score-transfer-ownership-dialog',
  templateUrl: './transfer-ownership-dialog.component.html',
  styleUrls: ['./transfer-ownership-dialog.component.css']
})
export class TransferOwnershipDialogComponent implements OnInit {

  loading = false;
  displayedColumns: string[] = [
    'select', 'loginId', 'name', 'organization', 'developer'
  ];
  dataSource = new MatTableDataSource<AccountList>();
  selection = new SelectionModel<AccountList>(false, []);

  loginIdList: string[] = [];
  request: AccountListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(
    public dialogRef: MatDialogRef<TransferOwnershipDialogComponent>,
    private accountService: AccountListService,
    private authService: AuthService,
    @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
    this.request = new AccountListRequest();

    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.paginator.length = 0;

    this.sort.active = 'name';
    this.sort.direction = 'asc';
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadAccounts();
    });

    this.loadAccounts(true);
  }

  onPageChange(event: PageEvent) {
    this.loadAccounts();
  }

  onChange(property?: string, source?) {
  }

  loadAccounts(isInit?: boolean) {
    this.loading = true;
    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    if (this.authService.getUserToken().tenant.enabled) {
      this.request.filters.businessCtxIds = this.data.businesCtxIds;
    }

    if (this.data && this.data.roles) {
      this.request.filters.roles = this.data.roles;
    }
    this.accountService.getAccountsList(this.request, true).subscribe(resp => {
      this.paginator.length = resp.length;
      this.paginator.pageIndex = resp.page;
      this.dataSource.data = resp.list;
      this.loading = false;
    }, error => {
      this.loading = false;
    });
  }

  select(row: AccountList) {
    this.selection.select(row);
  }

  toggle(row: AccountList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: AccountList) {
    return this.selection.isSelected(row);
  }

}
