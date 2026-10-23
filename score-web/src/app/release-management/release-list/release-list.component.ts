import {ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {AuthService} from '../../authentication/auth.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {ReleaseListEntry, WorkingRelease} from '../domain/release';
import {ReleaseService} from '../domain/release.service';
import {ReleaseDiagramDialogComponent} from '../release-diagram-dialog/release-diagram-dialog.component';

const RELEASE_STATES = ['Initialized', 'Draft', 'Published'];

const RELEASE_TABLE = defineTable({
  storageKey: 'TableColumns-ReleasePage',
  columns: [
    {id: 'releaseNum', label: 'Release', width: 0, sortable: true},
    {id: 'state', label: 'State', width: 200, sortable: true},
    {id: 'creationTimestamp', label: 'Created On', header: 'Created on', width: 160, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  fixed: [{id: 'more', position: 'end', width: 40}],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'releaseNum', placeholder: 'Search by Release'},
  filters: [
    {type: 'select', key: 'states', httpParam: 'releaseStates', label: 'State', options: RELEASE_STATES.map(e => ({value: e, label: e}))},
    {type: 'updater', key: 'creatorLoginIdList', label: 'Creator'},
    {type: 'dateRange', key: 'createdDate', label: 'Created', httpParam: 'createdOn'},
    // The namespaces of the library: fetched, and many.
    {type: 'select', key: 'namespaces', httpParam: 'namespaceIds', label: 'Namespace', searchable: true},
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
    {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpParam: 'lastUpdatedOn'},
  ],
});

@Component({
  standalone: false,
  selector: 'score-release-list',
  templateUrl: './release-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class ReleaseListComponent implements OnInit {
  private service = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private namespaceService = inject(NamespaceService);
  private snackBar = inject(MatSnackBar);
  private auth = inject(AuthService);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);
  private router = inject(Router);

  title = 'Release';
  readonly libraryChoice = new LibraryChoice(this.auth);

  readonly columns = createTableColumns(RELEASE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<ReleaseListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(true, []);
  contextMenuItem: ReleaseListEntry;

  /** The list is of a library: it waits for the libraries, and loads again when another one is chosen. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  readonly filterOptions = signal<Record<string, { value: string, label: string }[]>>({namespaces: []});
  /** While a release is being updated. */
  readonly busy = signal(false);

  readonly load = (params: HttpParams) => this.service.getReleaseListPage(params);
  readonly rowKey = (row: ReleaseListEntry) => row.releaseId;
  readonly rowSelectable = (row: ReleaseListEntry) => this.isSelectable(row);

  get userToken() {
    return this.auth.getUserToken();
  }

  ngOnInit() {
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      const libraryId = this.libraryChoice.library?.libraryId;
      if (!libraryId) {
        this.updateExtraParams();
        return;
      }
      this.namespaceService.getNamespaceSummaries(libraryId).subscribe(namespaces => {
        this.filterOptions.set({namespaces: namespaces.map(e => ({value: '' + e.namespaceId, label: e.uri}))});
        this.updateExtraParams();
        this.ready.set(true);
      });
    });
  }

  private updateExtraParams() {
    this.extraParams.set({
      libraryId: '' + this.libraryChoice.library?.libraryId,
      excludeReleaseNums: WorkingRelease.releaseNum
    });
  }

  onLibraryChange(library?: LibrarySummary) {
    this.libraryChoice.choose(library);
    this.updateExtraParams();
  }

  isSelectable(row: ReleaseListEntry) {
    return (row.state === 'Initialized');
  }

  create() {
    if (!this.libraryChoice.library?.libraryId) {
      return;
    }
    this.router.navigateByUrl('/release/create');
  }

  createDraft(release: ReleaseListEntry) {
    this.router.navigateByUrl('release/' + release.releaseId + '/assign');
  }

  updateState(release: ReleaseListEntry, state: string) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Update state to \'' + state + '\'?';
    dialogConfig.data.content = ['Are you sure you want to update the state to \'' + state + '\'?'];
    dialogConfig.data.action = 'Update';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.busy.set(true);
          this.service.updateState(release.releaseId, state)
            .pipe(finalize(() => {
              this.view().reload();
              this.busy.set(false);
            }))
            .subscribe(_ => {
              this.snackBar.open('Updated', '', {
                duration: 3000,
              });
            });
        }
      });
  }

  discard(release?: ReleaseListEntry) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Release?';
    if (release) {
      dialogConfig.data.content = ['Are you sure you want to discard the release \'' + release.releaseNum + '\'?'];
    } else if (this.selection.selected.length > 0) {
      dialogConfig.data.content = ['Are you sure you want to discard selected releases?'];
    } else {
      return;
    }
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          const releaseIds = (release) ? [release.releaseId, ] : this.selection.selected;
          this.service.discard(releaseIds).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.view().reload();
          });
        }
      });
  }

  showUmlDiagram(release: ReleaseListEntry, $event: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.height = '80vh';
    dialogConfig.data = {releaseId: release.releaseId};
    const dialogRef = this.dialog.open(ReleaseDiagramDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(_ => {
    });
  }
}
