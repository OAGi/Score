import {Component, OnInit, ViewChild} from '@angular/core';
import {BieListService} from './domain/bie-list.service';
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
import {ActivatedRoute, Router} from '@angular/router';
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
import {ConfirmDialogConfig} from '../../common/confirm-dialog/confirm-dialog.domain';
import {ConfirmDialogComponent} from '../../common/confirm-dialog/confirm-dialog.component';
import {Location} from '@angular/common';
import {finalize} from 'rxjs/operators';
import {ContextMenuComponent, ContextMenuService} from 'ngx-contextmenu';

@Component({
  selector: 'score-bie-list',
  templateUrl: './bie-list.component.html',
  styleUrls: ['./bie-list.component.css']
})
export class BieListComponent implements OnInit {
  title = 'BIEs';

  displayedColumns: string[] = ['select', 'propertyTerm', 'release', 'owner', 'transferOwnership', 'businessContexts', 'version',
    'status', 'lastUpdateTimestamp', 'more'
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
  @ViewChild(ContextMenuComponent, {static: true}) public contextMenu: ContextMenuComponent;

  constructor(private service: BieListService,
              private accountService: AccountListService,
              private auth: AuthService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private contextMenuService: ContextMenuService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.request = new BieListRequest(this.route.snapshot.queryParamMap,
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
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });

    this.loadBieList(true);
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

  loadBieList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getBieListWithRequest(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: BieList) => {
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

  isEditable(element: BieList) {
    return element.owner === this.currentUser && element.state === 'Editing';
  }

  onContextMenu($event: MouseEvent, item: BieList): void {
    if (!this.isEditable(item)) {
      return;
    }

    this.contextMenuService.show.next({
      contextMenu: this.contextMenu,
      event: $event,
      item: item,
    });

    $event.preventDefault();
    $event.stopPropagation();
  }

  discard() {
    this.openDialogBieDiscard();
  }

  openDialogBieDiscard() {
    const topLevelAbieIds = this.selection.selected;

    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Discard ' + (topLevelAbieIds.length > 1 ? 'BIEs' : 'BIE') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected ' + (topLevelAbieIds.length > 1 ? 'BIEs' : 'BIE') + '?',
      'The ' + (topLevelAbieIds.length > 1 ? 'BIEs' : 'BIE') + ' will be permanently removed.'
    ];

    dialogConfig.data.action = 'Discard';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
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

  openTransferDialog(topLevelAbieId: number, $event) {
    console.log('openTransferDialog(' + topLevelAbieId + ', ' + $event + ')');

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
