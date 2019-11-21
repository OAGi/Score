import {Component, OnInit, ViewChild} from '@angular/core';
import {Router} from '@angular/router';
import {MatPaginator, MatSort, MatTableDataSource, PageEvent} from '@angular/material';
import {AccountList, AccountListRequest} from '../domain/accounts';
import {AccountListService} from '../domain/account-list.service';
import {AuthService} from '../../authentication/auth.service';
import {PageRequest} from '../../basis/basis';

@Component({
  selector: 'srt-account-list',
  templateUrl: './account-list.component.html',
  styleUrls: ['./account-list.component.css']
})
export class AccountListComponent implements OnInit {

  title = 'Accounts';
  displayedColumns: string[] = [
    'loginId', 'name', 'organization', 'developer'
  ];
  dataSource = new MatTableDataSource<AccountList>();

  request: AccountListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private router: Router,
              private auth: AuthService,
              private service: AccountListService) {
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
      this.onChange();
    });

    this.onChange();
  }

  onPageChange(event: PageEvent) {
    this.onChange();
  }

  onChange() {
    this.loadAccounts();
  }

  loadAccounts() {
    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getAccountsList(this.request).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list;
    });
  }

}
