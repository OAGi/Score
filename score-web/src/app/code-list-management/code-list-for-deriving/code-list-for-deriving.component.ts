import {Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {ActivatedRoute, Router} from '@angular/router';
import {CodeListForList, CodeListForListRequest} from '../domain/code-list';
import {CodeListService} from '../domain/code-list.service';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {PageRequest} from '../../basis/basis';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter, loadBranch, saveBranch} from '../../common/utility';
import {Release} from '../../bie-management/bie-create/domain/bie-create-list';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';
import {WorkingRelease} from '../../release-management/domain/release';
import {finalize} from 'rxjs/operators';

@Component({
  selector: 'score-code-list-for-deriving',
  templateUrl: './code-list-for-deriving.component.html',
  styleUrls: ['./code-list-for-deriving.component.css']
})
export class CodeListForDerivingComponent implements OnInit {

  title = 'Derive Code List';
  workingRelease = WorkingRelease;
  workingStateList = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
  releaseStateList = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];

  displayedColumns: string[] = [
    'select', 'codeListName', 'basedCodeListName', 'agencyId',
    'versionId', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<CodeListForList>();
  selection = new SelectionModel<number>(false, []);
  loading = false;

  releases: Release[];
  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: CodeListForListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: CodeListService,
              private releaseService: ReleaseService,
              private accountService: AccountListService,
              private auth: AuthService,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.request = new CodeListForListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.states.push('Published');
    this.request.extensible = true;

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    this.releases = [];

    forkJoin([
      this.releaseService.getSimpleReleases(['Published',]),
      this.accountService.getAccountNames()
    ]).subscribe(([releases, loginIds]) => {
      this.releases.push(...releases);
      if (this.releases.length > 0) {
        const savedReleaseId = loadBranch(this.auth.getUserToken(), 'CC');
        if (savedReleaseId) {
          this.request.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
          if (!this.request.release) {
            this.request.release = this.releases[0];
            saveBranch(this.auth.getUserToken(), 'CC', this.request.release.releaseId);
          }
        } else {
          this.request.release = this.releases[0];
        }
      }

      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.onChange();
    });
  }

  onPageChange(event: PageEvent) {
    this.loadCodeList(true);
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'CC', source.releaseId);
    }

    this.paginator.pageIndex = 0;
    this.loadCodeList();
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

  loadCodeList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getCodeListList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: CodeListForList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });
    }, error => {
      this.dataSource.data = [];
    });
  }

  applyFilter(filterValue: string) {
    this.dataSource.filter = filterValue.trim();
  }

  select(row: CodeListForList) {
    this.selection.select(row.codeListManifestId);
  }

  toggle(row: CodeListForList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.codeListManifestId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CodeListForList) {
    return this.selection.isSelected(row.codeListManifestId);
  }

  next() {
    this.router.navigate(['/code_list/create/' + this.selection.selected[0]], {
      queryParams: {
        releaseId: this.request.release.releaseId
      },
    });
  }

}
