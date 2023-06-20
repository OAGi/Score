import {Component, OnInit, ViewChild} from '@angular/core';
import {BieForOasDoc, BieForOasDocListRequest, OasDoc} from '../domain/openapi-doc';
import {BusinessContext} from '../../../../context-management/business-context/domain/business-context';
import {Release} from '../../../bie-create/domain/bie-create-list';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {WorkingRelease} from '../../../../release-management/domain/release';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {BusinessContextService} from '../../../../context-management/business-context/domain/business-context.service';
import {OpenAPIService} from '../domain/openapi.service';
import {ReleaseService} from '../../../../release-management/domain/release.service';
import {AccountListService} from '../../../../account-management/domain/account-list.service';
import {AuthService} from '../../../../authentication/auth.service';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {PageRequest, PageResponse} from '../../../../basis/basis';
import {base64Decode, initFilter, loadBranch, saveBranch} from '../../../../common/utility';
import {finalize, switchMap} from 'rxjs/operators';
import {HttpParams} from '@angular/common/http';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';

@Component({
  selector: 'score-oas-doc-bie-list',
  templateUrl: './oas-doc-bie-list.component.html',
  styleUrls: ['./oas-doc-bie-list.component.css']
})
export class OasDocBieListComponent implements OnInit {
  subtitle = 'Select BIEs';
  oasDoc: OasDoc;
  businessContextIdList: number[] = [];
  businessContextList: BusinessContext[] = [];
  releaseId: number;
  releases: Release[] = [];
  displayedColumns: string[] = [
    'select', 'state', 'den', 'owner', 'version', 'verb', 'arrayIndicator', 'suppressRoot', 'messageBody',
    'resourceName', 'operationId', 'tagName', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BieForOasDoc>();
  selection = new SelectionModel<BieForOasDoc>(false, []);

  loading = false;

  loginIdList: string[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<Release[]> = new ReplaySubject<Release[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: BieForOasDocListRequest;

  workingRelease = WorkingRelease;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private bizCtxService: BusinessContextService,
              private openAPIService: OpenAPIService,
              private releaseService: ReleaseService,
              private accountService: AccountListService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    this.oasDoc = new OasDoc();
    this.oasDoc.used = true;
    const oasDocId = this.route.snapshot.params.id;

    forkJoin(
      this.openAPIService.getOasDoc(oasDocId)
    )
      .subscribe(([simpleOasDoc]) => {
        this.oasDoc = simpleOasDoc;
      });
    // Init BIE List table
    this.request = new BieForOasDocListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadData();
    });

    // Init releases
    this.releaseId = 0;
    this.releases = [];

    forkJoin([
      this.accountService.getAccountNames(),
      this.releaseService.getSimpleReleases(),
    ]).subscribe(([loginIds, releases]) => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
      this.releases = releases.filter(e => e.releaseNum !== 'Working' && e.state === 'Published');
      initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
      if (this.releases.length > 0) {
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
        this.releaseId = this.request.release.releaseId;

        // Load Business Contexts
        this.route.queryParamMap.pipe(
          switchMap((params: ParamMap) => {
            let businessContextIdList = params.get('businessContextIdList');
            if (!businessContextIdList) {
              const q = (this.route.snapshot.queryParamMap) ? this.route.snapshot.queryParamMap.get('q') : undefined;
              const httpParams = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
              businessContextIdList = httpParams.get('businessContextIdList');
            }
            return this.bizCtxService.getBusinessContextsByBizCtxIds(businessContextIdList.split(',').map(e => Number(e)));
          })).subscribe((resp: PageResponse<BusinessContext>) => {
          if (resp.length === 0) {
            this.router.navigateByUrl('/profile_bie/create');
          } else {
            this.businessContextIdList = resp.list.map(e => e.businessContextId);
            this.businessContextList = resp.list;
            this.loadData(true);
          }
        }, err => {
          console.error(err);
        });
      }
    });
  }

  onPageChange(event: PageEvent) {
    this.loadData();
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

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);
    }
    if (property === 'filters.den') {
      this.sort.active = '';
      this.sort.direction = '';
    }
  }

  loadData(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.openAPIService.getBieListForOasDoc(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.paginator.pageIndex = resp.page;

      const list = resp.list.map((elm: BieForOasDoc) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });

      this.dataSource.data = list;
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  back() {
    this.router.navigateByUrl('/profile_bie/express/oas_doc');
  }

  select(row: BieForOasDoc) {
    this.selection.select(row);
  }

  toggle(row: BieForOasDoc) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieForOasDoc) {
    return this.selection.isSelected(row);
  }

  create() {
  }

  get isDeveloper(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.roles.includes('developer');
  }

}
