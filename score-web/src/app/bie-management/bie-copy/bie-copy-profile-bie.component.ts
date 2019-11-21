import {Component, OnInit, ViewChild} from '@angular/core';
import {BieCopyService} from './domain/bie-copy.service';
import {MatPaginator, MatSnackBar, MatSort, MatTableDataSource, PageEvent} from '@angular/material';
import {SelectionModel} from '@angular/cdk/collections';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {switchMap} from 'rxjs/operators';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContext} from '../../context-management/business-context/domain/business-context';
import {BieList, BieListRequest} from '../bie-list/domain/bie-list';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepickerInputEvent} from '@angular/material/typings/datepicker';
import {PageRequest, PageResponse} from '../../basis/basis';

@Component({
  selector: 'srt-bie-create-asccp',
  templateUrl: './bie-copy-profile-bie.component.html',
  styleUrls: ['./bie-copy-profile-bie.component.css']
})
export class BieCopyProfileBieComponent implements OnInit {
  title = 'Copy BIE';
  subtitle = 'Select BIE';

  bizCtxIds: number[] = [];
  bizCtxList: BusinessContext[] = [];

  displayedColumns: string[] = [
    'select', 'propertyTerm', 'releaseNum', 'bizCtxName', 'owner', 'version', 'status', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BieList>();
  selection = new SelectionModel<BieList>(false, []);
  loading = false;

  loginIdList: string[] = [];
  states: string[] = ['Editing', 'Candidate', 'Published'];
  request: BieListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private bizCtxService: BusinessContextService,
              private service: BieCopyService,
              private bieListService: BieListService,
              private accountService: AccountListService,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.request = new BieListRequest();
    this.request.access = 'CanView';

    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.paginator.length = 0;

    this.sort.active = 'propertyTerm';
    this.sort.direction = 'asc';
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    this.accountService.getAccountNames().subscribe(loginIds => this.loginIdList.push(...loginIds));

    // Load Business Contexts
    this.route.queryParamMap.pipe(
      switchMap((params: ParamMap) => {
        const bizCtxIds: number[] = params.get('bizCtxIds').split(',').map(e => Number(e));
        return this.bizCtxService.getBusinessContextsByBizCtxIds(bizCtxIds);
      })).subscribe((resp: PageResponse<BusinessContext>) => {
      this.bizCtxIds = resp.list.map(e => e.bizCtxId);
      this.bizCtxList = resp.list;
      this.onChange();
    }, err => {
      console.error(err);
    });
  }

  onPageChange(event: PageEvent) {
    this.onChange();
  }

  onChange() {
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

  loadBieList() {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.bieListService.getBieListWithRequest(this.request)
      .subscribe(resp => {
        this.paginator.length = resp.length;
        this.dataSource.data = resp.list.map((elm: BieList) => {
          elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
          return elm;
        });
        this.loading = false;
      });
  }

  back() {
    this.router.navigateByUrl('/profile_bie/copy');
  }

  copy() {
    const topLevelAbieId: number = this.selection.selected[0].topLevelAbieId;
    this.service.copy(topLevelAbieId, this.bizCtxIds).subscribe(_ => {
      this.snackBar.open('Copying request queued', '', {
        duration: 1000,
      });

      this.router.navigateByUrl('/profile_bie');
    });
  }

}
