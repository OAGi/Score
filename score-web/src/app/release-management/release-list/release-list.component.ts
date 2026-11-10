import {ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal, viewChild} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';
import {debounceTime, filter, finalize, map, tap} from 'rxjs/operators';
import {Message} from '@stomp/stompjs';
import {AuthService} from '../../authentication/auth.service';
import {RxStompService} from '../../common/score-rx-stomp';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {ReleaseListEntry, WorkingRelease} from '../domain/release';
import {ReleaseService} from '../domain/release.service';
import {ReleaseDiagramDialogComponent} from '../release-diagram-dialog/release-diagram-dialog.component';
import {CompactRecord, memoizeRow} from '../../common/record-compact-row';

/** What the server announces of a release: `username` is who asked for it, when the work for it `failed`. */
interface ReleaseChange {
  libraryId: number;
  releaseId: number;
  outcome: 'changed' | 'failed';
  username?: string;
}

/** How long the list waits for more changes before it is loaded again. */
const RELOAD_DELAY_MS = 300;

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
  private stompService = inject(RxStompService);
  private destroyRef = inject(DestroyRef);

  title = 'Release';

  /** A release is being drafted or published (or was left being processed by an earlier version): it is not opened, and shows a spinner. */
  isProcessing(state: string): boolean {
    return state === 'Processing' || state === 'Drafting' || state === 'Publishing';
  }

  readonly libraryChoice = new LibraryChoice(this.auth);

  /** What the compact row shows of a release: made once for a row. A release that is being processed is not a link. */
  readonly compactRecord = memoizeRow((release: ReleaseListEntry): CompactRecord => ({
    badges: [{text: release.state, kind: 'state', cssClass: 'release-state', colors: 'release',
      state: this.isProcessing(release.state) ? 'Processing' : release.state}],
    title: release.releaseNum,
    link: this.isProcessing(release.state) ? undefined : '/release/' + release.releaseId,
    subtitle: release.guid,
    created: release.created ? {when: release.created.when, by: release.created.who?.loginId, label: 'Created'} : undefined,
    updated: release.lastUpdated ? {when: release.lastUpdated.when, by: release.lastUpdated.who?.loginId} : undefined
  }));

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
    this.watchReleaseChanges();
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

  /**
   * A release of the library is created, discarded, or moved to another state, e.g. when a release that was
   * 'Processing' is a draft or published (or was given back after it failed): the list is loaded again, keeping the page,
   * the sort and the filters, without the user pressing the search button. The server announces it once the change is
   * committed. Changes in quick succession (a release and its components) are loaded once.
   */
  private watchReleaseChanges() {
    this.stompService.watch('/topic/release').pipe(
      map((message: Message) => JSON.parse(message.body) as ReleaseChange),
      filter(change => this.ready() && this.isOfChosenLibrary(change.libraryId)),
      tap(change => this.tellTheRequesterOfAFailure(change)),
      debounceTime(RELOAD_DELAY_MS),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(() => this.view().reload());
  }

  /**
   * The work for a release failed and the release was given back: the user who asked for it is told here at once, and
   * what went wrong is in the message sent to the user. The others see the list change, and nothing more.
   */
  private tellTheRequesterOfAFailure(change: ReleaseChange) {
    if (change.outcome === 'failed' && change.username === this.auth.getUserToken()?.username) {
      this.snackBar.open('A release could not be processed. See your messages for the details.', 'Messages', {duration: 10000})
        .onAction().subscribe(() => this.router.navigateByUrl('/message'));
    }
  }

  /** A change that does not say which library it is of (0) is taken as of this one. */
  private isOfChosenLibrary(libraryId: number): boolean {
    return !libraryId || libraryId === this.libraryChoice.library?.libraryId;
  }

  private updateExtraParams() {
    this.extraParams.set({
      libraryId: '' + this.libraryChoice.library?.libraryId,
      excludeReleaseNums: WorkingRelease.releaseNum
    });
  }

  onLibraryChange(library?: LibrarySummary) {
    this.libraryChoice.choose(library);
    if (!library?.libraryId) {
      this.updateExtraParams();
      return;
    }
    // The namespaces of the previous library are not offered, and a chosen one is not sent with the new library.
    this.namespaceService.getNamespaceSummaries(library.libraryId).subscribe(namespaces => {
      this.view().setFilter('namespaces', []);
      this.filterOptions.set({namespaces: namespaces.map(e => ({value: '' + e.namespaceId, label: e.uri}))});
      this.updateExtraParams();
    });
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
