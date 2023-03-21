import {SelectionModel} from '@angular/cdk/collections';
import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {FormControl} from '@angular/forms';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ActivatedRoute, Router} from '@angular/router';
import {faLocationArrow} from '@fortawesome/free-solid-svg-icons';
import {forkJoin, ReplaySubject} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {AuthService} from '../../../authentication/auth.service';
import {PageRequest} from '../../../basis/basis';
import {Release} from '../../../bie-management/bie-create/domain/bie-create-list';
import {CodeListForList, CodeListForListRequest} from '../../../code-list-management/domain/code-list';
import {CodeListService} from '../../../code-list-management/domain/code-list.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {initFilter, loadBranch, saveBranch} from '../../../common/utility';
import {WorkingRelease} from '../../../release-management/domain/release';
import {ReleaseService} from '../../../release-management/domain/release.service';

@Component({
  selector: 'score-context-scheme-value-dialog',
  templateUrl: './codelist-list-dialog.component.html',
  styleUrls: ['./codelist-list-dialog.component.css']
})
export class CodelistListDialogComponent implements OnInit {

  faLocationArrow = faLocationArrow;
  title = 'Code List';
  workingRelease = WorkingRelease;
  releaseStateList = ['Published', 'Production'];

  innerWidth: number;
  get displayedColumns(): string[] {
    const columns = ['select', 'state', 'codeListName'];
    const innerWidth = this.innerWidth;
    if (innerWidth > 900) {
      columns.push('agencyId');
    }
    if (innerWidth > 1000) {
      columns.push('versionId');
    }
    if (innerWidth > 1100) {
      columns.push('extensible');
    }
    if (innerWidth > 1200) {
      columns.push('revision');
    }
    if (innerWidth > 1300) {
      columns.splice(3, 0, 'basedCodeListName');
    }
    if (innerWidth > 800) {
      columns.push('owner');
    }
    columns.push('lastUpdateTimestamp');
    return columns;
  }
  dataSource = new MatTableDataSource<CodeListForList>();
  selection = new SelectionModel<CodeListForList>(true, []);
  loading = false;

  releases: Release[];
  loginIdList: string[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<Release[]> = new ReplaySubject<Release[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: CodeListForListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(public dialogRef: MatDialogRef<CodelistListDialogComponent>,
              private service: CodeListService,
              private releaseService: ReleaseService,
              private accountService: AccountListService,
              private auth: AuthService,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.innerWidth = window.innerWidth;
    this.request = new CodeListForListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;
    this.request.states = this.releaseStateList;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadCodeList();
    });

    this.releases = [];

    forkJoin([
      this.releaseService.getSimpleReleases(['Published']),
      this.accountService.getAccountNames()
    ]).subscribe(([releases, loginIds]) => {
      this.releases.push(...releases.filter(e => e.releaseNum !== this.workingRelease.releaseNum));
      initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
      if (this.releases.length > 0) {
        const savedReleaseId = loadBranch(this.auth.getUserToken(), this.request.cookieType);
        if (savedReleaseId) {
          this.request.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
          if (!this.request.release) {
            this.request.release = this.releases[0];
            saveBranch(this.auth.getUserToken(), this.request.cookieType, this.request.release.releaseId);
          }
        } else {
          this.request.release = this.releases[0];
        }
      }

      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.loadCodeList(true);
    });
  }

  onResize(event) {
    this.innerWidth = window.innerWidth;
  }

  onPageChange(event: PageEvent) {
    this.loadCodeList();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), this.request.cookieType, source.releaseId);
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
      if (!isInit) {
        // this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.selection.selected.length > 0) {
      this.service.getCodeList(this.selection.selected[0].codeListManifestId).subscribe(codeList => {
        return this.dialogRef.close(codeList);
      });
    }
  }

  select(row: CodeListForList) {
    this.selection.select(row);
  }

  toggle(row: CodeListForList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.selection.clear();
      this.select(row);
    }
  }

  isSelected(row: CodeListForList) {
    return this.selection.isSelected(row);
  }
}
