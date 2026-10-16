import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {initFilter} from '../../../common/utility';
import {BiePackage, BiePackageListEntry} from '../domain/bie-package';
import {BiePackageService} from '../domain/bie-package.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {ReleaseSummary} from '../../../release-management/domain/release';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {AuthService} from '../../../authentication/auth.service';
import {UserToken} from '../../../authentication/domain/auth';
import {TransferOwnershipDialogComponent} from '../../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AccountList} from '../../../account-management/domain/accounts';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';
import {LibraryService} from '../../../library-management/domain/library.service';
import {LibrarySummary} from '../../../library-management/domain/library';
import {LibraryChoice} from '../../../library-management/domain/library-choice';

const BIE_PACKAGE_TABLE = defineTable({
  storageKey: 'TableColumns-BiePackagePage',
  columns: [
    {id: 'state', label: 'State', width: 104, sortable: true},
    {id: 'branch', label: 'Branch', width: 100, sortable: true},
    {id: 'name', label: 'Package Name', width: 0, sortable: true},
    {id: 'versionId', label: 'Version ID', width: 160, sortable: true},
    {id: 'versionName', label: 'Version Name', width: 160, sortable: true},
    {id: 'owner', label: 'Owner', width: 140, sortable: true},
    {id: 'description', label: 'Description', width: 400, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', sortLabel: 'Last Update Timestamp', width: 160, sortable: true},
  ],
  defaultSort: [{active: 'lastUpdateTimestamp', direction: 'desc'}],
  search: {key: 'name', placeholder: 'Search by Package Name'},
  filters: [
    {type: 'select', key: 'states', label: 'State', options: ['WIP', 'QA', 'Production'].map(e => ({value: e, label: e}))},
    {type: 'text', key: 'versionId', label: 'Version ID'},
    {type: 'text', key: 'versionName', label: 'Version Name'},
    {type: 'text', key: 'description', label: 'Package Description'},
    {type: 'updater', key: 'ownerLoginIdList', label: 'Owner'},
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
    {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpParam: 'lastUpdatedOn'},
    {type: 'text', key: 'businessTerm', label: 'BIE Business Term'},
    {type: 'text', key: 'version', label: 'BIE Version'},
    {type: 'text', key: 'remark', label: 'BIE Remark'},
    // The branches are chosen beside the search box; they are part of the query.
    {type: 'select', key: 'releaseIds', label: 'Branch', hidden: true},
    // In the URL, with no field to edit it.
    {type: 'text', key: 'den', label: 'DEN', hidden: true},
  ],
});

@Component({
  standalone: false,
  selector: 'score-bie-package-list',
  templateUrl: './bie-package-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./bie-package-list.component.css']
})
export class BiePackageListComponent {
  private biePackageService = inject(BiePackageService);
  private accountService = inject(AccountListService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private auth = inject(AuthService);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  webPageInfo = inject(WebPageInfoService);

  title = 'BIE Package';

  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly columns = createTableColumns(BIE_PACKAGE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BiePackageListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(true, []);

  /** The list is of a library: it waits for it, and loads again when another one is chosen. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  /** While an action is in progress. */
  readonly busy = signal(false);

  /** The branches of the library that the packages can be of, and those chosen. */
  releases: ReleaseSummary[] = [];
  selectedReleases: ReleaseSummary[] = [];
  readonly releaseListFilterCtrl = new FormControl();
  readonly filteredReleaseList = new ReplaySubject<ReleaseSummary[]>(1);

  readonly load = (params: HttpParams) => this.biePackageService.getBiePackageListPage(params);
  readonly rowKey = (row: BiePackageListEntry) => row.biePackageId;

  constructor() {
    afterNextRender(() => this.start());
  }

  /** Once the table view has the query of the URL: the library that it works for, and the branches that it asks for. */
  private start() {
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      this.applyLibrary();
      this.releaseService.getReleaseSummaryList(this.libraryChoice.library?.libraryId, ['Published']).subscribe(releases => {
        this.initReleases(releases);
        this.ready.set(true);
      });
    });
  }

  private applyLibrary() {
    this.extraParams.set({libraryId: '' + this.libraryChoice.library?.libraryId});
  }

  /** The branches that the URL asks for are among those of the library. */
  private initReleases(releases: ReleaseSummary[]) {
    this.releases = releases.filter(e => !e.workingRelease);
    initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
    const requested = (this.view().filterValue('releaseIds') as string[] | undefined) ?? [];
    this.selectedReleases = requested.map(id => this.releases.find(r => '' + r.releaseId === id)).filter(e => !!e);
    this.applyReleases();
  }

  /** The branches that are chosen go to the query: searching is the user's (or the library's change's) call. */
  applyReleases() {
    this.view().setFilter('releaseIds', this.selectedReleases.map(e => '' + e.releaseId));
  }

  toggleAllForReleaseFilter(selectAllValue: boolean) {
    this.selectedReleases = selectAllValue ? this.releases : [];
    this.applyReleases();
  }

  onLibraryChange(library: LibrarySummary) {
    this.libraryChoice.choose(library);
    this.releaseService.getReleaseSummaryList(library.libraryId, ['Published']).subscribe(releases => {
      this.initReleases(releases);
      // A change of the library's parameter searches by itself.
      this.applyLibrary();
    });
  }

  get userToken(): UserToken {
    return this.auth.getUserToken();
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  releaseNums(elm: BiePackage): string[] {
    return (!!elm.releases && elm.releases.length > 0) ? elm.releases.map(e => e.releaseNum) : [];
  }

  isEditable(element: BiePackageListEntry): boolean {
    if (!element) {
      return false;
    }
    return element.access === 'CanEdit';
  }

  create() {
    this.biePackageService.create(this.libraryChoice.library?.libraryId).subscribe(biePackageId => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });

      this.router.navigateByUrl('/bie_package/' + biePackageId);
    });
  }

  discard(biePackage?: BiePackageListEntry) {
    const biePackageIds = (!!biePackage) ? [biePackage.biePackageId,] : this.selection.selected;
    this.openDialogBiePackageDiscard(biePackageIds);
  }

  openDialogBiePackageDiscard(biePackageIds: number[]) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard BIE Package' + (biePackageIds.length > 1 ? 's' : '') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected BIE package' + (biePackageIds.length > 1 ? 's' : '') + '?',
      'The BIE package' + (biePackageIds.length > 1 ? 's' : '') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.biePackageService.delete(...biePackageIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.view().reload();
          });
        }
      });
  }

  openTransferDialog(biePackage: BiePackageListEntry, $event?) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    if (!this.isEditable(biePackage) && !this.isAdmin) {
      return;
    }

    this.accountService.getAccount(biePackage.owner.userId).subscribe(resp => {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.width = window.innerWidth + 'px';
      dialogConfig.data = {roles: [resp.developer ? 'developer' : 'end-user']};
      const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

      dialogRef.afterClosed().subscribe((result: AccountList) => {
        if (result) {
          this.busy.set(true);
          this.biePackageService.transferOwnership(biePackage.biePackageId, result.loginId).subscribe(_ => {
            this.snackBar.open('Transferred', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.view().reload();
            this.busy.set(false);
          }, error => {
            this.busy.set(false);
            throw error;
          });
        }
      });
    });
  }
}
