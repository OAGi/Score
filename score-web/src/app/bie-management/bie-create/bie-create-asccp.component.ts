import {Component, OnInit, ViewChild} from '@angular/core';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {Release} from './domain/bie-create-list';
import {BieCreateService} from './domain/bie-create.service';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {finalize, switchMap} from 'rxjs/operators';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContext} from '../../context-management/business-context/domain/business-context';
import {ReleaseService} from '../../release-management/domain/release.service';
import {CcList, CcListRequest} from '../../cc-management/cc-list/domain/cc-list';
import {CcListService} from '../../cc-management/cc-list/domain/cc-list.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {PageRequest, PageResponse} from '../../basis/basis';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {WorkingRelease} from '../../release-management/domain/release';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {base64Decode, initFilter, loadBranch, saveBooleanProperty, saveBranch} from '../../common/utility';
import {Location} from '@angular/common';
import {HttpParams} from '@angular/common/http';
import {AuthService} from '../../authentication/auth.service';
import {Tag} from '../../tag-management/domain/tag';
import {TagService} from '../../tag-management/domain/tag.service';

@Component({
  selector: 'score-bie-create-asccp',
  templateUrl: './bie-create-asccp.component.html',
  styleUrls: ['./bie-create-asccp.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class BieCreateAsccpComponent implements OnInit {
  title = 'Create BIE';
  subtitle = 'Select Top-Level Concept';

  businessContextIdList: number[] = [];
  businessContextList: BusinessContext[] = [];
  releaseId: number;
  releases: Release[] = [];

  displayedColumns: string[] = [
    'select', 'type', 'state', 'den', 'revision', 'owner', 'module', 'lastUpdateTimestamp'
  ];
  stateList = ['Published', 'Production'];
  dataSource = new MatTableDataSource<CcList>();
  selection = new SelectionModel<CcList>(false, []);
  expandedElement: CcList | null;
  loading = false;

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: CcListRequest;
  tags: Tag[] = [];

  workingRelease = WorkingRelease;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  HIDE_UNUSED_PROPERTY_KEY = 'BIE-Settings-Hide-Unused';

  constructor(private bizCtxService: BusinessContextService,
              private releaseService: ReleaseService,
              private ccListService: CcListService,
              private accountService: AccountListService,
              private service: BieCreateService,
              private tagService: TagService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    // Init ASCCP table
    this.request = new CcListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.types = ['ASCCP'];
    this.request.states = this.stateList;
    this.request.isBIEUsable = true;

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
      this.tagService.getTags()
    ]).subscribe(([loginIds, releases, tags]) => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.tags = tags;

      this.releases = releases.filter(e => e.releaseNum !== 'Working' && e.state === 'Published');
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

    this.ccListService.getCcList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.paginator.pageIndex = resp.page;

      const list = resp.list.map((elm: CcList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        if (this.request.filters.module.length > 0) {
          elm.module = elm.module.replace(
            new RegExp(this.request.filters.module, 'ig'),
            '<b>$&</b>');
        }
        if (this.request.filters.definition.length > 0) {
          elm.definition = elm.definition.replace(
            new RegExp(this.request.filters.definition, 'ig'),
            '<b>$&</b>');
        }
        return elm;
      });

      this.dataSource.data = list;
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery({
          businessContextIdList: this.businessContextIdList.map(e => '' + e).join(',')
        }));
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  back() {
    this.router.navigateByUrl('/profile_bie/create');
  }

  select(row: CcList) {
    this.selection.select(row);
  }

  toggle(row: CcList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CcList) {
    return this.selection.isSelected(row);
  }

  create() {
    const asccpManifestId: number = this.selection.selected[0].manifestId;
    this.service.create(asccpManifestId, this.businessContextIdList)
      .subscribe(resp => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });

        // Issue #1366
        // 'Hide Unused' option must be turned off after BIE creation.
        saveBooleanProperty(this.auth.getUserToken(), this.HIDE_UNUSED_PROPERTY_KEY, false);

        this.router.navigateByUrl('/profile_bie/' + resp.topLevelAsbiepId);
      });
  }

  get isDeveloper(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.roles.includes('developer');
  }

}
