import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatDatepicker} from '@angular/material/datepicker';
import {finalize} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ReleaseSummary} from '../../../release-management/domain/release';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {AuthService} from '../../../authentication/auth.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {PageRequest} from '../../../basis/basis';
import {initFilter, loadLibrary, saveBranch, saveLibrary} from '../../../common/utility';
import {MatMultiSort, MatMultiSortTableDataSource, TableData} from 'ngx-mat-multi-sort';
import {BieListEntry} from '../../bie-list/domain/bie-list';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {BieListInBiePackageRequest, BiePackageDetails} from '../domain/bie-package';
import {BiePackageService} from '../domain/bie-package.service';
import {LibrarySummary} from '../../../library-management/domain/library';
import {LibraryService} from '../../../library-management/domain/library.service';

@Component({
  selector: 'score-bie-package-uplift-dialog',
  templateUrl: './bie-package-uplift-dialog.component.html',
  styleUrls: ['./bie-package-uplift-dialog.component.css']
})
export class BiePackageUpliftDialogComponent implements OnInit {

  title = 'Uplift BIE Package';
  biePackage: BiePackageDetails = new BiePackageDetails();

  displayedColumns = [
    {id: 'state', name: 'State'},
    {id: 'branch', name: 'Branch'},
    {id: 'den', name: 'DEN'},
    {id: 'owner', name: 'Owner'},
    {id: 'businessContexts', name: 'Business Contexts'},
    {id: 'version', name: 'Version'},
    {id: 'status', name: 'Status'},
    {id: 'bizTerm', name: 'Business Term'},
    {id: 'remark', name: 'Remark'},
    {id: 'lastUpdateTimestamp', name: 'Updated on'},
  ];

  table: TableData<BieListEntry>;
  selection = new SelectionModel<BieListEntry>(true, []);
  request: BieListInBiePackageRequest;
  loading = false;

  loginIdList: string[] = [];
  releases: ReleaseSummary[] = [];
  library: LibrarySummary = new LibrarySummary();
  libraries: LibrarySummary[] = [];
  mappedLibraries: { library: LibrarySummary, selected: boolean }[] = [];
  sourceRelease: ReleaseSummary;
  targetRelease: ReleaseSummary;
  sourceReleaseListFilterCtrl: FormControl = new FormControl();
  sourceReleaseFilteredList: ReplaySubject<ReleaseSummary[]> = new ReplaySubject<ReleaseSummary[]>(1);
  targetReleaseListFilterCtrl: FormControl = new FormControl();
  targetReleaseFilteredList: ReplaySubject<ReleaseSummary[]> = new ReplaySubject<ReleaseSummary[]>(1);

  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['Production'];

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatMultiSort, {static: true}) sort: MatMultiSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private biePackageService: BiePackageService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private libraryService: LibraryService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService,
              private dialog: MatDialog,
              public webPageInfo: WebPageInfoService,
              public dialogRef: MatDialogRef<BiePackageUpliftDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  ngOnInit(): void {
    this.table = new TableData<BieListEntry>(this.displayedColumns, {});
    this.table.dataSource = new MatMultiSortTableDataSource<BieListEntry>(this.sort, false);

    // Init BIE list table for BIE package
    this.request = new BieListInBiePackageRequest(undefined,
      new PageRequest(['lastUpdateTimestamp'], ['desc'], 0, 10));

    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.initLibraries(libraries);

      this.paginator.pageIndex = this.request.page.pageIndex;
      this.paginator.pageSize = this.request.page.pageSize;
      this.paginator.length = 0;

      this.table.sortParams = this.request.page.sortActives;
      this.table.sortDirs = this.request.page.sortDirections;
      this.table.sortObservable.subscribe(() => {
        this.paginator.pageIndex = 0;
        this.loadBieListInBiePackage();
      });

      this.request.page = new PageRequest(
        this.table.sortParams, this.table.sortDirs,
        this.paginator.pageIndex, this.paginator.pageSize);
      this.request.biePackageId = this.data.biePackageId;

      forkJoin([
        this.biePackageService.get(this.request.biePackageId),
        this.accountService.getAccountNames(),
        this.releaseService.getReleaseSummaryList(this.library.libraryId, ['Published'])
      ]).subscribe(([biePackage, loginIds, releases]) => {
        this.init(biePackage);

        this.loginIdList.push(...loginIds);
        initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
        initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

        this.initReleases(releases);

        this.loadBieListInBiePackage();
      }, error => {
        this.loading = false;
        let errorMessage;
        if (error.status === 403) {
          errorMessage = 'You do not have access permission.';
        } else {
          errorMessage = 'Something\'s wrong.';
        }
        this.snackBar.open(errorMessage, '', {
          duration: 3000
        });
        this.onNoClick();
      });
    });
  }

  get targetReleaseList(): ReleaseSummary[] {
    if (!!this.sourceRelease) {
      return this.releases.filter(val => val.releaseId > this.sourceRelease.releaseId);
    }
    return [];
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  init(biePackage: BiePackageDetails) {
    this.biePackage = biePackage;
    this.loading = false;
  }

  getPath(commands?: any[]): string {
    const urlTree = this.router.createUrlTree(commands);
    const path = this.location.prepareExternalUrl(urlTree.toString());
    return window.location.origin + path;
  }

  initLibraries(libraries: LibrarySummary[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
      }
      if (!this.library || !this.library.libraryId) {
        this.library = this.libraries[0];
      }
      if (this.library) {
        saveLibrary(this.auth.getUserToken(), this.library.libraryId);
      }
      this.mappedLibraries = this.libraries.map(e => {
        return {library: e, selected: (this.library.libraryId === e.libraryId)};
      });
    }
  }

  initReleases(releases: ReleaseSummary[]) {
    this.releases = releases.filter(e => !e.workingRelease);
    if (!!this.data.releaseId) {
      this.releases = this.releases.filter(e => e.releaseId === this.data.releaseId);
    }
  }

  onLibraryChange(library: LibrarySummary) {
    this.library = library;
    this.releaseService.getReleaseSummaryList(this.library.libraryId, ['Published']).subscribe(releases => {
      saveLibrary(this.auth.getUserToken(), this.library.libraryId);
      this.initReleases(releases);
      this.onSearch();
    });
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadBieListInBiePackage();
  }

  loadBieListInBiePackage() {
    this.loading = true;

    this.request.page = new PageRequest(
      this.table.sortParams, this.table.sortDirs,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.biePackageService.getBieListInBiePackage(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.table.dataSource.data = resp.list;

      const releaseNums = new Set(resp.list.map(e => e.release.releaseNum));
      const sourceReleases = this.releases.filter(e => releaseNums.has(e.releaseNum));
      if (sourceReleases.length === 1) {
        this.setSourceRelease(sourceReleases[0]);
      }
    }, error => {
      this.table.dataSource.data = [];
    });
  }

  setSourceRelease(sourceRelease: ReleaseSummary) {
    this.sourceRelease = sourceRelease;

    initFilter(this.sourceReleaseListFilterCtrl, this.sourceReleaseFilteredList, ((this.sourceRelease) ? [this.sourceRelease] : []), (e) => e.releaseNum);
    initFilter(this.targetReleaseListFilterCtrl, this.targetReleaseFilteredList, this.targetReleaseList, (e) => e.releaseNum);
  }

  onPageChange(event: PageEvent) {
    this.loadBieListInBiePackage();
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

  onNoClick(): void {
    this.dialogRef.close();
  }

  onUplift(): void {
    if (!this.targetRelease) {
      this.onNoClick();
    }

    this.dialogRef.close(this.targetRelease.releaseId);
  }
}
