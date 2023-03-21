import {SelectionModel} from '@angular/cdk/collections';
import {Location} from '@angular/common';
import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {PageRequest} from '../../basis/basis';
import {finalize} from 'rxjs/operators';
import {AccountListService} from '../domain/account-list.service';
import {AccountList, AccountListRequest} from '../domain/accounts';
import {PendingAccount} from '../domain/pending-list';

@Component({
  selector: 'score-account-list-dialog',
  templateUrl: './account-list-dialog.component.html',
  styleUrls: ['./account-list-dialog.component.css']
})
export class AccountListDialogComponent implements OnInit {

  title = 'Link to existing account';
  displayedColumns: string[] = [
    'select', 'loginId', 'name', 'organization', 'developer', 'appOauth2UserId'
  ];
  dataSource = new MatTableDataSource<AccountList>();
  loading = false;

  request: AccountListRequest;
  selection = new SelectionModel<AccountList>(false, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private auth: AuthService,
              private service: AccountListService,
              public dialogRef: MatDialogRef<AccountListDialogComponent>,
              private location: Location,
              private router: Router,
              @Inject(MAT_DIALOG_DATA) public pending: PendingAccount,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.request = new AccountListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('name', 'asc', 0, 10));
    this.request.filters.excludeSSO = true;
    this.request.filters.roles = ['end-user',];

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
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
    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);
    this.service.getAccountsList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.selection.clear();
      this.paginator.length = resp.length;
      this.paginator.pageIndex = resp.page;
      this.dataSource.data = resp.list;
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  link() {
    if (this.selection.selected.length > 0) {
      this.service.link(this.pending, this.selection.selected[0]).subscribe(resp => {
        this.dialogRef.close('Linked');
      }, error => {
      });
    }
  }
  close() {
    this.dialogRef.close('');
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
