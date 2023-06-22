import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {BieList, BieListRequest} from '../../../bie-list/domain/bie-list';
import {SelectionModel} from '@angular/cdk/collections';
import {SimpleRelease} from '../../../../release-management/domain/release';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {BieListService} from '../../../bie-list/domain/bie-list.service';
import {AccountListService} from '../../../../account-management/domain/account-list.service';
import {ReleaseService} from '../../../../release-management/domain/release.service';
import {AuthService} from '../../../../authentication/auth.service';
import {MatDialog} from '@angular/material/dialog';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSort, SortDirection} from '@angular/material/sort';
import {initFilter, loadBranch, saveBranch} from '../../../../common/utility';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../../../basis/basis';
import {finalize} from 'rxjs/operators';

@Component({
  selector: 'score-oas-doc-assign-dialog',
  templateUrl: './oas-doc-assign-dialog.component.html',
  styleUrls: ['./oas-doc-assign-dialog.component.css']
})
export class OasDocAssignDialogComponent implements OnInit {

  title = 'Add BIE';
  subtitle = 'Selected Top-Level ABIEs';

  displayedColumns: string[] = [
    'select', 'state', 'den', 'owner', 'version', 'verb', 'arrayIndicator', 'suppressRoot', 'messageBody',
    'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BieList>();
  selection = new SelectionModel<number>(true, []);
  businessContextSelection = {};
  loading = false;

  loginIdList: string[] = [];
  releases: SimpleRelease[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  verbs: string[] = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'];
  selectedVerb: string;
  arrayIndicators: string[] = ['TRUE', 'FALSE'];
  selectedArrayIndicator: string;
  suppressRootIndicators: string[] = ['TRUE', 'FALSE'];
  selectedSuppressRootIndicator: string;
  messageBodyList: string[] = ['requestBody', 'responseBody'];
  selectedMessageBody: string;
  request: BieListRequest;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private bieListService: BieListService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private auth: AuthService,
              private dialog: MatDialog,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {

    // Init BIE table
    this.request = new BieListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.access = 'CanView';
    this.request.excludePropertyTerms = ['Meta Header', 'Pagination Response'];

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
      this.releaseService.getSimpleReleases()
    ]).subscribe(([loginIds, releases]) => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.releases = releases.filter(e => e.releaseNum !== 'Working' && e.state === 'Published');
      initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
      const savedReleaseId = loadBranch(this.auth.getUserToken(), 'BIE');
      if (savedReleaseId) {
        this.request.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
        if (!this.request.release) {
          this.request.release = this.releases[0];
          saveBranch(this.auth.getUserToken(), 'BIE', this.request.release.releaseId);
        }
      } else {
        this.request.release = this.releases[0];
      }

      this.loadBieList(true);
    });
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
      this.dataSource.data.forEach((elm: BieList) => {
        this.businessContextSelection[elm.topLevelAsbiepId] = elm.businessContexts[0];
      });

      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
      this.businessContextSelection = {};
    });
  }

  onPageChange(event: PageEvent) {
    this.loadBieList();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);
    }
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

  select(row: BieList) {
    this.selection.select(row.topLevelAsbiepId);
  }

  toggle(row: BieList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.topLevelAsbiepId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieList) {
    return this.selection.isSelected(row.topLevelAsbiepId);
  }

  createBieForOasDoc() {
  }
}
