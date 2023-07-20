import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {BieList, BieListRequest} from '../../bie-list/domain/bie-list';
import {SelectionModel} from '@angular/cdk/collections';
import {SimpleRelease} from '../../../release-management/domain/release';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {BieExpressService} from '../domain/bie-express.service';
import {BieListService} from '../../bie-list/domain/bie-list.service';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {AuthService} from '../../../authentication/auth.service';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {PageRequest} from '../../../basis/basis';
import {finalize} from 'rxjs/operators';
import {initFilter, loadBranch, saveBranch} from '../../../common/utility';
import {Location} from '@angular/common';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {BieExpressOption} from '../domain/generate-expression';

@Component({
  selector: 'score-bie-express.openapi30',
  templateUrl: './bie-express.openapi30.component.html',
  styleUrls: ['./bie-express.openapi30.component.css']
})
export class BieExpressOpenapi30Component implements OnInit {
  title = 'Express BIE in Open API 3.0 Expression';

  displayedColumns: string[] = [
    'select', 'state', 'den', 'owner', 'version', 'status', 'verb', 'arrayIndicator', 'suppressRootIndicator', 'messageBody', 'resourceName', 'operationId',
    'tag'
  ];
  dataSource = new MatTableDataSource<BieList>();
  selection = new SelectionModel<number>(true, []);
  businessContextSelection = {};
  loading = false;

  loginIdList: string[] = [];
  releases: SimpleRelease[] = [];
  selectedRelease: SimpleRelease;
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BieListRequest;
  option: BieExpressOption;

  // Memorizer
  previousPackageOption: string;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: BieExpressService,
              private bieListService: BieListService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private auth: AuthService,
              private dialog: MatDialog,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute
  ) {
  }

  ngOnInit(): void {
    this.option = new BieExpressOption();
    this.option.bieDefinition = true;
    this.option.packageOption = 'ALL';
    // Default Open API expression format is 'YAML'.
    this.option.openAPIExpressionFormat = 'YAML';
    // Init BIE table
    this.request = new BieListRequest(this.route.snapshot.queryParamMap, new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
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
    forkJoin([this.accountService.getAccountNames(),
      this.releaseService.getSimpleReleases()]).subscribe(([loginIds, releases]) => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
      this.releases = releases.filter(e => e.releaseNum !== 'Working' && e.state === 'Published');
      initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
      const savedReleaseId = loadBranch(this.auth.getUserToken(), 'BIE');
      if (savedReleaseId) {
        this.selectedRelease = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
        if (!this.request.releases) {
          this.selectedRelease = this.releases[0];
          saveBranch(this.auth.getUserToken(), 'BIE', this.selectedRelease.releaseId);
        }
      } else {
        this.selectedRelease = this.releases[0];
      }
      this.loadBieList(true);
    });
  }

  onPageChange(event: PageEvent) {
    this.loadBieList();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.released);
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

  bieAnnotationChange() {
    if (!this.option.bieCctsMetaData) {
      this.option.includeCctsDefinitionTag = false;
    }

    if (!this.option.bieOagiScoreMetaData) {
      this.option.includeWhoColumns = false;
    }
  }


}
