import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {ContextMenuComponent, ContextMenuService} from 'ngx-contextmenu';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {PageRequest} from '../../../basis/basis';
import {initFilter} from '../../../common/utility';
import {ModuleSet, ModuleSetListRequest} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';

@Component({
  selector: 'score-module-set-list',
  templateUrl: './module-set-list.component.html',
  styleUrls: ['./module-set-list.component.css']
})
export class ModuleSetListComponent implements OnInit {

  title = 'Module Set';

  displayedColumns: string[] = [
    'name', 'description', 'lastUpdateTimestamp', 'more'
  ];
  dataSource = new MatTableDataSource<ModuleSet>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: ModuleSetListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild(ContextMenuComponent, {static: true}) public contextMenu: ContextMenuComponent;

  constructor(private service: ModuleService,
              private accountService: AccountListService,
              private auth: AuthService,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService,
              private contextMenuService: ContextMenuService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.request = new ModuleSetListRequest(this.route.snapshot.queryParamMap,
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

    this.loadModuleSetList(true);
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

  onChange() {
    this.paginator.pageIndex = 0;
    this.loadModuleSetList();
  }

  onPageChange(event: PageEvent) {
    this.loadModuleSetList();
  }

  loadModuleSetList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getModuleSetList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.dataSource.data = resp.results;
      this.paginator.length = resp.length;
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  applyFilter(filterValue: string) {
    this.dataSource.filter = filterValue.trim();
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: ModuleSet) {
    this.selection.select(row.moduleSetId);
  }

  toggle(row: ModuleSet) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.moduleSetId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: ModuleSet) {
    return this.selection.isSelected(row.moduleSetId);
  }

  create() {
    this.router.navigateByUrl('/module_management/module_set/create');
  }

  isEditable(item: ModuleSet) {
    return true;
  }

  discard(item: ModuleSet, $event) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Module Set?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this module set?',
      'The module set will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.loading = true;
          this.service.discardModuleSet(item.moduleSetId).pipe(finalize(() => {
            this.loading = false;
          })).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadModuleSetList();
          });
        }
      });
  }

  onContextMenu($event: MouseEvent, item: ModuleSet): void {
    this.contextMenuService.show.next({
      contextMenu: this.contextMenu,
      event: $event,
      item: item,
    });

    $event.preventDefault();
    $event.stopPropagation();
  }

}
