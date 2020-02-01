import {Component, OnInit, ViewChild} from '@angular/core';
import {Release} from './domain/bie-create-list';
import {BieCreateService} from './domain/bie-create.service';
import {MatPaginator, MatSnackBar, MatSort, MatTableDataSource, PageEvent} from '@angular/material';
import {SelectionModel} from '@angular/cdk/collections';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {switchMap} from 'rxjs/operators';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContext} from '../../context-management/business-context/domain/business-context';
import {ReleaseService} from '../../release-management/domain/release.service';
import {CcList, CcListRequest} from '../../cc-management/cc-list/domain/cc-list';
import {CcListService} from '../../cc-management/cc-list/domain/cc-list.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {PageRequest, PageResponse} from '../../basis/basis';
import {MatDatepickerInputEvent} from '@angular/material/typings/datepicker';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../common/utility';

@Component({
  selector: 'srt-bie-create-asccp',
  templateUrl: './bie-create-asccp.component.html',
  styleUrls: ['./bie-create-asccp.component.css']
})
export class BieCreateAsccpComponent implements OnInit {
  title = 'Create BIE';
  subtitle = 'Select Top-Level Concept';

  bizCtxIds: number[] = [];
  bizCtxList: BusinessContext[] = [];
  releaseId: number;
  releases: Release[] = [];

  displayedColumns: string[] = [
    'select', 'den', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<CcList>();
  selection = new SelectionModel<CcList>(false, []);
  loading = false;

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: CcListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private bizCtxService: BusinessContextService,
              private releaseService: ReleaseService,
              private ccListService: CcListService,
              private accountService: AccountListService,
              private service: BieCreateService,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    // Load Business Contexts
    this.route.queryParamMap.pipe(
      switchMap((params: ParamMap) => {
        const bizCtxIds: number[] = params.get('bizCtxIds').split(',').map(e => Number(e));
        return this.bizCtxService.getBusinessContextsByBizCtxIds(bizCtxIds);
      })).subscribe((resp: PageResponse<BusinessContext>) => {
        this.bizCtxIds = resp.list.map(e => e.bizCtxId);
        this.bizCtxList = resp.list;
      }, err => {
        console.error(err);
      });

    // Init ASCCP table
    this.request = new CcListRequest();
    this.request.types = ['ASCCP'];
    this.request.states = ['Published'];

    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.paginator.length = 0;

    this.sort.active = 'den';
    this.sort.direction = 'asc';
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadData();
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });

    // Init releases
    this.releaseId = 0;
    this.releases = [];
    this.releaseService.getSimpleReleases().subscribe(releases => {
      this.releases.push(...releases);
      if (this.releases.length > 0) {
        this.releaseId = this.releases[0].releaseId;
        this.loadData();
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
  onChange() {
    this.paginator.pageIndex = 0;
    this.loadData();
  }

  loadData() {
    this.loading = true;

    this.request.releaseId = this.releaseId;
    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.ccListService.getCcList(this.request).subscribe(resp => {
      this.paginator.length = resp.length;

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
      this.loading = false;
    });
  }

  back() {
    this.router.navigateByUrl('/profile_bie/create');
  }

  create() {
    const asccpId: number = this.selection.selected[0].id;
    this.service.create(asccpId, this.releaseId, this.bizCtxIds)
      .subscribe(resp => {
      this.snackBar.open('Created', '', {
        duration: 1000,
      });

      this.router.navigateByUrl('/profile_bie/edit/' + resp['topLevelAbieId']);
    });
  }

}
