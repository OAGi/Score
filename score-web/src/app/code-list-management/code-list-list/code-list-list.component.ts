import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {faLocationArrow} from '@fortawesome/free-solid-svg-icons';
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
import {CodeListListEntry} from '../domain/code-list';
import {CODE_LIST_TABLE} from '../domain/code-list.table';
import {CodeListService} from '../domain/code-list.service';

const WORKING_STATE_LIST = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
const RELEASE_STATE_LIST = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];
const TRUE_FALSE = [{value: 'true', label: 'True'}, {value: 'false', label: 'False'}];

const CODE_LIST_LIST_TABLE = defineTable({
  ...CODE_LIST_TABLE,
  filters: [
    {type: 'select', key: 'states', label: 'State'},
    {type: 'text', key: 'definition', label: 'Definition'},
    {type: 'text', key: 'module', label: 'Module'},
    {type: 'select', key: 'deprecated', label: 'Deprecated', options: TRUE_FALSE, onlyWhenOne: true},
    {type: 'select', key: 'newComponent', label: 'New', options: TRUE_FALSE, onlyWhenOne: true},
    ...CODE_LIST_TABLE.filters,
    // The namespaces of the library: fetched, and many.
    {type: 'select', key: 'namespaces', label: 'Namespace', searchable: true},
    // The branch is chosen beside the search box; it is part of the query.
    {type: 'text', key: 'releaseId', label: 'Branch', hidden: true},
  ],
});

@Component({
  standalone: false,
  selector: 'score-code-list-list',
  templateUrl: './code-list-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./code-list-list.component.css']
})
export class CodeListListComponent {
  private service = inject(CodeListService);
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

  faLocationArrow = faLocationArrow;
  title = 'Code List';
  canSelect = ['WIP', 'Deleted'];
  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly releaseChoice = new ReleaseChoice(this.auth);

  readonly columns = createTableColumns(CODE_LIST_LIST_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<CodeListListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<CodeListListEntry>(true, [],
    true, (a, b) => a.codeListManifestId === b.codeListManifestId);

  /** The list is of a library and a branch: it waits for them, and loads again when another one is chosen. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  readonly filterOptions = signal<Record<string, { value: string, label: string }[]>>({states: [], namespaces: []});
  /** While a code list is being created. */
  readonly busy = signal(false);

  readonly load = (params: HttpParams) => this.service.getCodeListListPage(params);
  readonly rowSelectable = (row: CodeListListEntry) =>
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
    this.selection = new SelectionModel<CodeListListEntry>(true, [],
      true, (a, b) => a.codeListManifestId === b.codeListManifestId);
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  createCodeList() {
    this.busy.set(true);
    this.service.create(this.releaseChoice.release?.releaseId)
      .subscribe(resp => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/code_list/' + resp.codeListManifestId);
      }, _ => {
        this.busy.set(false);
      });
  }

  get showCreateCodeListBtn(): boolean {
    const userToken = this.auth.getUserToken();
    if (userToken.roles.includes('developer')) {
      if (!this.releaseChoice.release?.workingRelease) {
        return false;
      }
    } else {
      if (this.releaseChoice.release?.workingRelease) {
        return false;
      } else {
        return this.releaseChoice.release?.state === 'Published';
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
            this.selectionClear();
            this.view().reload();
          });
        }
      });
  }

  openDialogCodeListListPurge() {
    const codeListIds = this.selection.selected.map(e => e.codeListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Purge Code ' + (codeListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to purge selected code ' + (codeListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Purge anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.purge(...codeListIds).subscribe(_ => {
            this.snackBar.open('Purged', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.view().reload();
          });
        }
      });
  }

  openDialogCodeListListRestore() {
    const codeListIds = this.selection.selected.map(e => e.codeListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore Code ' + (codeListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to restore selected code ' + (codeListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.restore(...codeListIds).subscribe(_ => {
            this.snackBar.open('Restored', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.view().reload();
          });
        }
      });
  }

  hasRevision(item: CodeListListEntry): boolean {
    if (!item) {
      return false;
    }
    return item.log.revisionNum > 1;
  }

  isEditable(item: CodeListListEntry) {
    if (!item) {
      return false;
    }
    return item.access === 'CanEdit';
  }

  openTransferDialog(item: CodeListListEntry, event?: MouseEvent) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnership(item.codeListManifestId, result.loginId).subscribe(_ => {
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
