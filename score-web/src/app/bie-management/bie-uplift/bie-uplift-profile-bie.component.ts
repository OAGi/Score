import {Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BieList, BieListRequest} from '../bie-list/domain/bie-list';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../basis/basis';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter, loadBranch, saveBranch} from '../../common/utility';
import {Location} from '@angular/common';
import {SimpleRelease} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../../basis/basis.service';

@Component({
  selector: 'score-bie-uplift-profile-bie',
  templateUrl: './bie-uplift-profile-bie.component.html',
  styleUrls: ['./bie-uplift-profile-bie.component.css']
})
export class BieUpliftProfileBieComponent implements OnInit {

  title = 'Uplift BIE';
  subtitle = 'Select BIE';

  displayedColumns: string[] = [
    'select', 'state', 'branch', 'den', 'owner', 'businessContexts',
    'version', 'status', 'bizTerm', 'remark', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BieList>();
  selection = new SelectionModel<BieList>(false, []);
  loading = false;

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BieListRequest;

  releases: SimpleRelease[] = [];
  sourceRelease: SimpleRelease;
  targetRelease: SimpleRelease;
  sourceReleaseListFilterCtrl: FormControl = new FormControl();
  sourceReleaseFilteredList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  targetReleaseListFilterCtrl: FormControl = new FormControl();
  targetReleaseFilteredList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);

  get targetReleaseList(): SimpleRelease[] {
    const sourceRelease: SimpleRelease = this.request.releases[0];
    if (!!sourceRelease) {
      return this.releases.filter(val => val.releaseId > sourceRelease.releaseId);
    }
    return [];
  }

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private bizCtxService: BusinessContextService,
              private bieListService: BieListService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit() {
    this.request = new BieListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.access = 'CanView';

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadBieList();
    });

    forkJoin([
      this.accountService.getAccountNames(),
      this.releaseService.getSimpleReleases(['Published'])
    ]).subscribe(([loginIds, releases]) => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.releases = releases.filter(e => e.releaseNum !== 'Working');
      if (this.releases.length > 0) {
        const savedReleaseId = loadBranch(this.auth.getUserToken(), 'BIE');
        if (savedReleaseId) {
          this.sourceRelease = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
          if (!this.sourceRelease) {
            this.sourceRelease = this.releases[0];
            saveBranch(this.auth.getUserToken(), 'BIE', this.sourceRelease.releaseId);
          }
        } else {
          this.sourceRelease = this.releases[0];
        }
      }

      this.loadBieList(true);

      initFilter(this.sourceReleaseListFilterCtrl, this.sourceReleaseFilteredList, this.releases, (e) => e.releaseNum);
      initFilter(this.targetReleaseListFilterCtrl, this.targetReleaseFilteredList, this.targetReleaseList, (e) => e.releaseNum);
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });
  }

  onPageChange(event: PageEvent) {
    this.loadBieList();
  }

  onChange(property?: string, source?) {
  }

  onSourceReleaseChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);
    }

    this.targetRelease = undefined;
    this.paginator.pageIndex = 0;
    this.loadBieList();

    // Reset targetReleaseFilteredList using targetReleaseList
    initFilter(this.targetReleaseListFilterCtrl, this.targetReleaseFilteredList, this.targetReleaseList, (e) => e.releaseNum);
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

  loadBieList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);
    this.request.releases = (!!this.sourceRelease) ? [this.sourceRelease, ] : [];

    this.bieListService.getBieListWithRequest(this.request).pipe(
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
        this.location.replaceState(this.router.url.split('?')[0]);
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  select(row: BieList) {
    this.selection.select(row);
  }

  toggle(row: BieList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieList) {
    return this.selection.isSelected(row);
  }

  next() {
    const selectedBieList = this.selection.selected[0];
    this.router.navigate(['/profile_bie/uplift/' + selectedBieList.topLevelAsbiepId], {queryParams: {targetReleaseId: this.targetRelease.releaseId}});
  }

}
