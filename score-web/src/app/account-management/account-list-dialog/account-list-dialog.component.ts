import {SelectionModel} from '@angular/cdk/collections';
import {Location} from '@angular/common';
import {Component, Inject, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
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
import {PreferencesInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {forkJoin} from 'rxjs';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';

@Component({
  selector: 'score-account-list-dialog',
  templateUrl: './account-list-dialog.component.html',
  styleUrls: ['./account-list-dialog.component.css']
})
export class AccountListDialogComponent implements OnInit {

  title = 'Link to existing account';

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
    const columns = this.preferencesInfo.tableColumnsInfo.columnsOfAccountPage;
    const matched = columns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.preferencesService.update(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {});
    }
  }

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    const columns = this.preferencesInfo.tableColumnsInfo.columnsOfAccountPage;
    for (const column of columns) {
      switch (column.name) {
        case 'Login ID':
          if (column.selected) {
            displayedColumns.push('loginId');
          }
          break;
        case 'Role':
          if (column.selected) {
            displayedColumns.push('role');
          }
          break;
        case 'Name':
          if (column.selected) {
            displayedColumns.push('name');
          }
          break;
        case 'Organization':
          if (column.selected) {
            displayedColumns.push('organization');
          }
          break;
      }
    }
    displayedColumns.push('appOauth2UserId');
    return displayedColumns;
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfAccountPage.find(c => c.name === name)?.width;
  }

  dataSource = new MatTableDataSource<AccountList>();
  loading = false;

  request: AccountListRequest;
  preferencesInfo: PreferencesInfo;
  selection = new SelectionModel<AccountList>(false, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;

  constructor(private auth: AuthService,
              private service: AccountListService,
              private preferencesService: SettingsPreferencesService,
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
    this.request.filters.roles = ['developer', 'end-user'];

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
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;

      this.loadAccounts(true);
    });
  }

  onPageChange(event: PageEvent) {
    this.loadAccounts();
  }

  onChange(property?: string, source?) {
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadAccounts();
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
