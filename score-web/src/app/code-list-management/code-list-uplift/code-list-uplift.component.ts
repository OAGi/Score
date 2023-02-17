import {animate, state, style, transition, trigger} from '@angular/animations';
import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {faLocationArrow} from '@fortawesome/free-solid-svg-icons';
import {CodeListForList, CodeListForListRequest} from '../domain/code-list';
import {CodeListService} from '../domain/code-list.service';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {PageRequest} from '../../basis/basis';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter, loadBranch, saveBranch} from '../../common/utility';
import {SimpleRelease, WorkingRelease} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {Release} from '../../bie-management/bie-create/domain/bie-create-list';
import {AuthService} from '../../authentication/auth.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'score-code-list-uplift',
  templateUrl: './code-list-uplift.component.html',
  styleUrls: ['./code-list-uplift.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class CodeListUpliftComponent implements OnInit {

  faLocationArrow = faLocationArrow;
  title = 'Code List';
  workingStateList = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
  releaseStateList = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];

  displayedColumns: string[] = [
    'select', 'state', 'codeListName', 'basedCodeListName', 'agencyId',
    'versionId', 'extensible', 'revision', 'owner', 'module', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<CodeListForList>();
  selection = new SelectionModel<CodeListForList>(false, []);
  expandedElement: CodeListForList | null;
  loading = false;

  releases: Release[] = [];
  targetRelease: SimpleRelease;

  get targetReleaseList(): SimpleRelease[] {
    const sourceRelease: SimpleRelease = this.request.release;
    if (!!sourceRelease) {
      return this.releases.filter(val => val.releaseId > sourceRelease.releaseId);
    }
    return [];
  }

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: CodeListForListRequest;

  contextMenuItem: CodeListForList;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: CodeListService,
              private releaseService: ReleaseService,
              private accountService: AccountListService,
              private auth: AuthService,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    if (this.auth.getUserToken().roles.includes('developer')) {
      this.snackBar.open('Unauthorized access.', '', {
        duration: 3000,
      });
      return this.router.navigateByUrl('/');
    }

    this.request = new CodeListForListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.access = 'CanView';
    this.request.ownedByDeveloper = false;

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
      this.releaseService.getSimpleReleases(['Published']),
      this.accountService.getAccountNames()
    ]).subscribe(([releases, loginIds]) => {
      this.releases.push(...releases.filter(e => e.releaseNum !== 'Working'));
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

      this.onChange();
    });
  }

  getRelease(releaseNum: string): Release | undefined {
    for (const release of this.releases) {
      if (release.releaseNum === releaseNum) {
        return release;
      }
    }
    return undefined;
  }

  onPageChange(event: PageEvent) {
    this.loadCodeList(true);
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), this.request.cookieType, source.releaseId);
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
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  select(row: CodeListForList) {
    this.selection.select(row);
  }

  toggle(row: CodeListForList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CodeListForList) {
    return this.selection.isSelected(row);
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  createCodeList() {
    this.service.create(this.request.release.releaseId)
      .subscribe(resp => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/code_list/' + resp.manifestId);
      });
  }

  get showCreateCodeListBtn(): boolean {
    const userToken = this.auth.getUserToken();
    if (userToken.roles.includes('developer')) {
      if (this.request.release.releaseId !== WorkingRelease.releaseId) {
        return false;
      }
    } else {
      if (this.request.release.releaseId === WorkingRelease.releaseId) {
        return false;
      }
    }
    return true;
  }

  get showDiscardBtn(): boolean {
    return this.selection.selected.length > 0 ?
      this.selection.selected.filter(e => e.state === 'Deleted').length === 0 :
      false;
  }

  get showRestoreBtn(): boolean {
    return this.selection.selected.length > 0 ?
      this.selection.selected.filter(e => e.state === 'WIP').length === 0 :
      false;
  }

  openDialogCodeListListDelete() {
    const codeListIds = this.selection.selected.map(e => e.codeListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Delete Code ' + (codeListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to delete selected code ' + (codeListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Delete anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...codeListIds).subscribe(_ => {
            this.snackBar.open('Deleted', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadCodeList();
          });
        }
      });
  }

  openDialogCodeListListRestore() {
    const codeListIds = this.selection.selected.map(e => e.codeListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore Code ' + (codeListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to Restore selected code ' + (codeListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.restore(...codeListIds).subscribe(_ => {
            this.snackBar.open('Restored', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadCodeList();
          });
        }
      });
  }

  hasRevision(codeList: CodeListForList): boolean {
    return codeList.revision !== '1';
  }

  isEditable(item: CodeListForList) {
    return item.access === 'CanEdit';
  }

  delete(item: CodeListForList, $event) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Delete code list?';
    dialogConfig.data.content = ['Are you sure you want to delete this code list?'];
    dialogConfig.data.action = 'Delete anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }

        this.loading = true;
        this.service.delete(item.codeListManifestId)
          .pipe(
            finalize(() => {
              this.loading = false;
            })
          )
          .subscribe(_ => {
            this.loadCodeList();
            this.snackBar.open('Deleted', '', {duration: 3000});
          }, error => {
          });
      });
  }

  openDetail(item: CodeListForList, $event?) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }
    this.router.navigateByUrl('/code_list/' + item.codeListManifestId);
    return;
  }

  openDialogCcListRestore(item: CodeListForList) {

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore code list';
    dialogConfig.data.content = [
      'Are you sure you want to Restore code list?'
    ];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.restore(item.codeListManifestId).subscribe(_ => {
            this.snackBar.open('Restored', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadCodeList();
          });
        }
      });
  }

  next() {
    const selectedBieList = this.selection.selected[0];
    this.loading = true;
    this.service.uplift(selectedBieList, this.targetRelease.releaseId)
      .pipe(finalize(() => {
        this.loading = false;
      }))
      .subscribe(result => {
        if (result.duplicatedValues?.length > 0) {
          const dialogConfig = this.confirmDialogService.newConfig();
          dialogConfig.data.header = 'Overwritten Values';
          dialogConfig.data.content = [
            'These values are overwritten with the values of the target release.'
          ];

          result.duplicatedValues.forEach(e => {
            dialogConfig.data.content.push(' - ' + e);
          });

          this.confirmDialogService.open(dialogConfig).afterClosed()
            .subscribe(_ => {
              this.router.navigateByUrl('/code_list/' + result.codeListManifestId);
            });

        } else {
          this.router.navigateByUrl('/code_list/' + result.codeListManifestId);
        }
      });
  }

}

