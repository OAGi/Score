import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {AccountList, AccountListRequest} from '../domain/accounts';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {AuthService} from '../../authentication/auth.service';
import {AccountListService} from '../domain/account-list.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {PageRequest} from '../../basis/basis';
import {finalize} from 'rxjs/operators';
import {SelectionModel} from '@angular/cdk/collections';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'score-transfer-ownership-list',
  templateUrl: './transfer-ownership-list.component.html',
  styleUrls: ['./transfer-ownership-list.component.css']
})
export class TransferOwnershipListComponent implements OnInit {

  title = 'Transfer Ownership';
  displayedColumns: string[] = [
    'select', 'loginId', 'role', 'name', 'organization', 'status'
  ];
  selection = new SelectionModel<AccountList>(false, []);
  dataSource = new MatTableDataSource<AccountList>();
  loading = false;

  request: AccountListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private auth: AuthService,
              private service: AccountListService,
              private confirmDialogService: ConfirmDialogService,
              private snackBar: MatSnackBar,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.request = new AccountListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('name', 'asc', 0, 10));

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
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getAccountsList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
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

  select(row: AccountList) {
    this.selection.select(row);
  }

  transferOwnership(row: AccountList): void {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Transfer Ownership?';
    dialogConfig.data.content = [
      'Are you sure you want to transfer ownership of all components from the user ',
      '\'' + row.name + ' (' + row.loginId + ')\' to you? This action is irreversible.'
    ];
    dialogConfig.data.action = 'Transfer';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.loading = true;
          this.service.transferOwnership(row).subscribe(_ => {
            this.loading = false;
            this.selection.clear();

            this.snackBar.open('Transferred', '', {
              duration: 3000,
            });
          }, error => {
            this.loading = false;
          });
        }
      });
  }

}
