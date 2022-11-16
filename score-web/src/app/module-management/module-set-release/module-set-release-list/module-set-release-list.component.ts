import {SelectionModel} from '@angular/cdk/collections';
import {Location} from '@angular/common';
import {Component, OnInit, ViewChild} from '@angular/core';
import {FormControl} from '@angular/forms';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ActivatedRoute, Router} from '@angular/router';
import {ReplaySubject} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {AuthService} from '../../../authentication/auth.service';
import {PageRequest} from '../../../basis/basis';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {initFilter} from '../../../common/utility';
import {ModuleSetRelease, ModuleSetReleaseListRequest} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';
import {UserToken} from '../../../authentication/domain/auth';

@Component({
  selector: 'score-module-set-release-list',
  templateUrl: './module-set-release-list.component.html',
  styleUrls: ['./module-set-release-list.component.css']
})
export class ModuleSetReleaseListComponent implements OnInit {
  title = 'Module Set Release';

  displayedColumns: string[];
  dataSource = new MatTableDataSource<ModuleSetRelease>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: ModuleSetReleaseListRequest;

  contextMenuItem: ModuleSetRelease;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: ModuleService,
              private accountService: AccountListService,
              private auth: AuthService,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
    this.displayedColumns = (this.roles.includes('developer')) ? [
      'name', 'release', 'default', 'lastUpdateTimestamp', 'more'
    ] : [
      'name', 'release', 'default', 'lastUpdateTimestamp'
    ];
  }

  ngOnInit(): void {
    this.request = new ModuleSetReleaseListRequest(this.route.snapshot.queryParamMap,
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

    this.loadModuleSetReleaseList(true);
  }

  get userToken(): UserToken {
    return this.auth.getUserToken();
  }

  get roles(): string[] {
    const userToken = this.userToken;
    return (userToken) ? userToken.roles : [];
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
    this.loadModuleSetReleaseList();
  }

  onPageChange(event: PageEvent) {
    this.loadModuleSetReleaseList();
  }

  loadModuleSetReleaseList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getModuleSetReleaseList(this.request).pipe(
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


  isEditable(item: ModuleSetRelease) {
    if (!item) {
      return false;
    }
    return true;
  }

  discard(item: ModuleSetRelease) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Module Set Release?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this module set release?',
      'The module set will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.loading = true;
          this.service.discardModuleSetRelease(item.moduleSetReleaseId).pipe(finalize(() => {
            this.loading = false;
          })).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadModuleSetReleaseList();
          });
        }
      });
  }

  create() {
    this.router.navigateByUrl('/module_management/module_set_release/create');
  }

}
