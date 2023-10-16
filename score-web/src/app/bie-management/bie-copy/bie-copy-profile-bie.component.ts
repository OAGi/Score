import {Component, OnInit, ViewChild} from '@angular/core';
import {BieCopyService} from './domain/bie-copy.service';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {finalize, switchMap} from 'rxjs/operators';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContext} from '../../context-management/business-context/domain/business-context';
import {BieList, BieListRequest} from '../bie-list/domain/bie-list';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../basis/basis';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {base64Decode, initFilter, loadBranch, saveBooleanProperty, saveBranch} from '../../common/utility';
import {Location} from '@angular/common';
import {HttpParams} from '@angular/common/http';
import {SimpleRelease} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';

@Component({
  selector: 'score-bie-create-asccp',
  templateUrl: './bie-copy-profile-bie.component.html',
  styleUrls: ['./bie-copy-profile-bie.component.css']
})
export class BieCopyProfileBieComponent implements OnInit {
  title = 'Copy BIE';
  subtitle = 'Select BIE';

  bizCtxIds: number[] = [];
  bizCtxList: BusinessContext[] = [];

  displayedColumns: string[] = [
    'select', 'state', 'branch', 'den', 'owner', 'businessContexts',
    'version', 'status', 'bizTerm', 'remark', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BieList>();
  selection = new SelectionModel<BieList>(false, []);
  loading = false;

  loginIdList: string[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BieListRequest;

  releases: SimpleRelease[] = [];

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  HIDE_UNUSED_PROPERTY_KEY = 'BIE-Settings-Hide-Unused';

  constructor(private bizCtxService: BusinessContextService,
              private service: BieCopyService,
              private bieListService: BieListService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
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

    // Load Business Contexts
    this.route.queryParamMap.pipe(
      switchMap((params: ParamMap) => {
        let bizCtxIds = params.get('bizCtxIds');
        if (!bizCtxIds) {
          const q = (this.route.snapshot.queryParamMap) ? this.route.snapshot.queryParamMap.get('q') : undefined;
          const httpParams = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
          bizCtxIds = httpParams.get('bizCtxIds');
        }

        return forkJoin([
          this.bizCtxService.getBusinessContextsByBizCtxIds(bizCtxIds.split(',').map(e => Number(e))),
          this.accountService.getAccountNames(),
          this.releaseService.getSimpleReleases()
        ]);
      })).subscribe(([resp, loginIds, releases]) => {

      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.releases = releases.filter(e => e.releaseNum !== 'Working' && e.state === 'Published');
      initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);

      this.bizCtxIds = resp.list.map(e => e.businessContextId);
      this.bizCtxList = resp.list;

      this.loadBieList(true);
    }, err => {
      console.error(err);
    });
  }

  onPageChange(event: PageEvent) {
    this.loadBieList();
  }

  onChange(property?: string, source?) {
    if (property === 'filters.den') {
      this.sort.active = '';
      this.sort.direction = '';
    }
  }

  onReleaseChange(source) {
    saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);

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
        this.dateStart.select(undefined);
        this.request.updatedDate.start = null;
        break;
      case 'endDate':
        this.dateEnd.select(undefined);
        this.request.updatedDate.end = null;
        break;
    }
  }

  toggleAllForReleaseFilter(selectAllValue: boolean) {
    if (selectAllValue) {
      this.request.releases = this.releases;
    } else {
      this.request.releases = [];
    }
  }

  loadBieList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

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
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery({
          bizCtxIds: this.bizCtxIds.map(e => '' + e).join(',')
        }));
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  back() {
    this.router.navigateByUrl('/profile_bie/copy');
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

  copy() {
    const topLevelAsbiepId: number = this.selection.selected[0].topLevelAsbiepId;
    this.service.copy(topLevelAsbiepId, this.bizCtxIds).subscribe(_ => {
      this.snackBar.open('Copying request queued', '', {
        duration: 3000,
      });

      // Issue #1366
      // 'Hide Unused' option must be turned off after BIE creation.
      saveBooleanProperty(this.auth.getUserToken(), this.HIDE_UNUSED_PROPERTY_KEY, false);

      this.router.navigateByUrl('/profile_bie');
    });
  }

}
