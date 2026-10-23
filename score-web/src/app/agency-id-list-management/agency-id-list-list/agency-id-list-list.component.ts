import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {forkJoin} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {AccountList} from '../../account-management/domain/accounts';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';
import {TransferOwnershipDialogComponent} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ReleaseChoice} from '../../release-management/domain/release-choice';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AgencyIdListListEntry} from '../domain/agency-id-list';
import {AgencyIdListService} from '../domain/agency-id-list.service';

const WORKING_STATE_LIST = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
const RELEASE_STATE_LIST = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];
const TRUE_FALSE = [{value: 'true', label: 'True'}, {value: 'false', label: 'False'}];

const AGENCY_ID_LIST_TABLE = defineTable({
  storageKey: 'TableColumns-AgencyIdListPage',
  columns: [
    {id: 'state', label: 'State', width: 104, sortable: true},
    {id: 'name', label: 'Name', width: 0, sortable: true},
    {id: 'versionId', label: 'Version', width: 100, sortable: true},
    {id: 'revision', label: 'Revision', width: 100, sortable: true},
    {id: 'owner', label: 'Owner', width: 140, sortable: true},
    {id: 'module', label: 'Module', width: 400, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'name', placeholder: 'Search by Name'},
  filters: [
    {type: 'select', key: 'states', label: 'State'},
    {type: 'text', key: 'definition', label: 'Definition'},
    {type: 'text', key: 'module', label: 'Module'},
    {type: 'select', key: 'deprecated', label: 'Deprecated', options: TRUE_FALSE, onlyWhenOne: true},
    {type: 'select', key: 'newComponent', label: 'New', options: TRUE_FALSE, onlyWhenOne: true},
    {type: 'updater', key: 'ownerLoginIdList', label: 'Owner'},
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
    {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpParam: 'lastUpdatedOn'},
    // The namespaces of the library: fetched, and many.
    {type: 'select', key: 'namespaces', label: 'Namespace', searchable: true},
    // The branch is chosen beside the search box; it is part of the query.
    {type: 'text', key: 'releaseId', label: 'Branch', hidden: true},
  ],
});

@Component({
  standalone: false,
  selector: 'score-agency-id-list-list',
  templateUrl: './agency-id-list-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./agency-id-list-list.component.css']
})
export class AgencyIdListListComponent {
  private service = inject(AgencyIdListService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private namespaceService = inject(NamespaceService);
  private auth = inject(AuthService);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  webPageInfo = inject(WebPageInfoService);

  title = 'Agency ID List';
  canSelect = ['WIP', 'Deleted'];
  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly releaseChoice = new ReleaseChoice(this.auth);

  readonly columns = createTableColumns(AGENCY_ID_LIST_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<AgencyIdListListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<AgencyIdListListEntry>(true, [],
    true, (a, b) => a.agencyIdListManifestId === b.agencyIdListManifestId);

  /** The list is of a library and a branch: it waits for them, and loads again when another one is chosen. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  readonly filterOptions = signal<Record<string, { value: string, label: string }[]>>({states: [], namespaces: []});
  /** While an item is being deleted. */
  readonly busy = signal(false);

  readonly load = (params: HttpParams) => this.service.getAgencyIdListListPage(params);
  readonly rowSelectable = (row: AgencyIdListListEntry) =>
    this.canSelect.indexOf(row.state) > -1 && !this.hasRevision(row) && row.owner.loginId === this.currentUser;

  constructor() {
    afterNextRender(() => this.start());
  }

  /** Once the table view has the query of the URL: the library and the branch that it asks for, and the choices of the filters. */
  private start() {
    const requestedReleaseId = Number(this.view().filterValue('releaseId') || 0);
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      const libraryId = this.libraryChoice.library?.libraryId;
      forkJoin([
        this.releaseService.getReleaseSummaryList(libraryId, ['Draft', 'Published']),
        this.namespaceService.getNamespaceSummaries(libraryId)
      ]).subscribe(([releases, namespaces]) => {
        this.releaseChoice.init(releases, requestedReleaseId);
        this.filterOptions.update(options => ({...options, namespaces: namespaces.map(e => ({value: '' + e.namespaceId, label: e.uri}))}));
        this.applyChoices();
        this.ready.set(true);
      });
    });
  }

  /** The library and the branch go to the query. */
  private applyChoices() {
    this.view().setFilter('releaseId', '' + (this.releaseChoice.release?.releaseId || 0));
    this.extraParams.set({libraryId: '' + this.libraryChoice.library?.libraryId});
    const states = this.releaseChoice.release?.workingRelease ? WORKING_STATE_LIST : RELEASE_STATE_LIST;
    this.filterOptions.update(options => ({...options, states: states.map(e => ({value: e, label: e}))}));
  }

  onBranchChange(release: ReleaseSummary) {
    this.releaseChoice.choose(release);
    this.applyChoices();
    this.view().onSearch();
  }

  onLibraryChange(library: LibrarySummary) {
    this.libraryChoice.choose(library);
    this.releaseService.getReleaseSummaryList(library.libraryId, ['Draft', 'Published']).subscribe(releases => {
      this.releaseChoice.init(releases);
      this.applyChoices();
      this.view().onSearch();
    });
  }

  selectionClear() {
    this.selection = new SelectionModel<AgencyIdListListEntry>(true, [],
      true, (a, b) => a.agencyIdListManifestId === b.agencyIdListManifestId);
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  createAgencyIdList() {
    this.service.create(this.releaseChoice.release?.releaseId)
      .subscribe(resp => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/agency_id_list/' + resp.agencyIdListManifestId);
      });
  }

  get showCreateAgencyIdListBtn(): boolean {
    const userToken = this.auth.getUserToken();
    if (userToken.roles.includes('developer')) {
      if (!this.releaseChoice.release?.workingRelease) {
        return false;
      }
      return (this.view()?.dataSource.data ?? []).filter(e => e.state !== 'Deleted').length === 0;
    } else {
      if (this.releaseChoice.release?.workingRelease) {
        return false;
      } else {
        return this.releaseChoice.release?.state === 'Published';
      }
    }
  }

  get showDeleteBtn(): boolean {
    return this.selection.selected.length > 0 ?
      this.selection.selected.filter(e => e.state === 'Deleted').length === 0 :
      false;
  }

  get showRestoreBtn(): boolean {
    return this.selection.selected.length > 0 ?
      this.selection.selected.filter(e => e.state === 'WIP').length === 0 :
      false;
  }

  openDialogAgencyIdListListDelete() {
    const agencyIdListIds = this.selection.selected.map(e => e.agencyIdListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Delete Agency Id ' + (agencyIdListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to delete selected agency id ' + (agencyIdListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Delete anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...agencyIdListIds).subscribe(_ => {
            this.snackBar.open('Deleted', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.view().reload();
          });
        }
      });
  }

  openDialogAgencyIdListListPurge() {
    const agencyIdListIds = this.selection.selected.map(e => e.agencyIdListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Purge Agency Id ' + (agencyIdListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to purge selected agency id ' + (agencyIdListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Purge anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.purge(...agencyIdListIds).subscribe(_ => {
            this.snackBar.open('Purged', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.view().reload();
          });
        }
      });
  }

  openDialogAgencyIdListListRestore() {
    const agencyIdListIds = this.selection.selected.map(e => e.agencyIdListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore Agency Id ' + (agencyIdListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to restore selected agency Id ' + (agencyIdListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.restore(...agencyIdListIds).subscribe(_ => {
            this.snackBar.open('Restored', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.view().reload();
          });
        }
      });
  }

  hasRevision(item: AgencyIdListListEntry): boolean {
    if (!item) {
      return false;
    }
    return item.log.revisionNum > 1;
  }

  isEditable(item: AgencyIdListListEntry) {
    if (!item) {
      return false;
    }
    return item.owner.username === this.currentUser && item.state === 'WIP';
  }

  openTransferDialog(item: AgencyIdListListEntry, event?: MouseEvent) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnership(item.agencyIdListManifestId, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.selection.clear();
          this.view().reload();
        });
      }
    });
  }
}
