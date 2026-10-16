import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {BieListEntry} from '../../bie-list/domain/bie-list';
import {BieListService} from '../../bie-list/domain/bie-list.service';
import {bieLinkQuery, BIE_FILTERS as F, BIE_TABLE} from '../../bie-list/domain/bie.table';
import {ReleaseSummary} from '../../../release-management/domain/release';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';
import {LibrarySummary} from '../../../library-management/domain/library';
import {LibraryService} from '../../../library-management/domain/library.service';

export interface OasDocConfirmMessageDialogResult {
  topLevelAsbiepId: number;
  den: string;
  // Issue #1347: the release the picked ConfirmMessage BIE belongs to. Per-op callers ignore it; the
  // document-level "apply to all" caller uses it to target only operations in that release.
  releaseId?: number;
  releaseNum?: string;
}

// Issue #1347: the DEN of the standard 'Confirm Message' component. Like the 'Include Meta Header'
// ('Meta Header. Meta Header') and 'Pagination Response' ('Pagination Response. Pagination Response')
// pickers, this dialog is locked to a single well-known BIE, so the DEN filter is fixed and the search
// box is disabled.
const CONFIRM_MESSAGE_DEN = 'Confirm Message. Confirm Message';

/**
 * Issue #1347: a BIE-selection dialog for the ConfirmMessage BIE that backs an operation's
 * CONFIRM_MESSAGE error response. It is a clone of the BIE-Express 'Include Meta Header'
 * (MetaHeaderDialogComponent) and 'Pagination Response' (PaginationResponseDialogComponent) pickers —
 * the full BIE-list table with the standard columns and a paginator, locked to a single well-known BIE.
 *
 * Like those pickers, the DEN filter is fixed (to {@code 'Confirm Message. Confirm Message'}) and the
 * search box is disabled, and the picker is locked to one release: the release of the OpenAPI Document's
 * connected BIE (passed in by the caller), shown in the disabled Branch field. The library is fixed to
 * the default ('connectSpec') with no Library selector; its id is still resolved internally because the
 * BIE-list query requires a libraryId. The body type itself is chosen by the inline Error Response
 * selector; this dialog only returns the picked BIE ({@code topLevelAsbiepId} + DEN for display).
 */
const CONFIRM_MESSAGE_TABLE = defineTable({
  ...BIE_TABLE,
  filters: [F.states, F.businessContext, F.version, F.remark, F.owner, F.updater, F.updatedDate,
    // The release is part of the query: the document's own, or the one that is chosen among those of its endpoints.
    F.releaseIds],
});

@Component({
  standalone: false,
  selector: 'score-oas-doc-confirm-message-dialog',
  templateUrl: './oas-doc-confirm-message-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./oas-doc-confirm-message-dialog.component.css']
})
export class OasDocConfirmMessageDialogComponent {
  dialogRef = inject<MatDialogRef<OasDocConfirmMessageDialogComponent>>(MatDialogRef);
  private bieListService = inject(BieListService);
  private libraryService = inject(LibraryService);
  private releaseService = inject(ReleaseService);
  webPageInfo = inject(WebPageInfoService);
  data = inject(MAT_DIALOG_DATA);

  title: string;

  readonly columns = createTableColumns(CONFIRM_MESSAGE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BieListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<BieListEntry>(false, []);

  /** The list is of a library and a release: it waits for them, and loads again when another one is chosen. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});

  // Library is fixed to the default ('connectSpec') and the release is locked to the document's
  // connected BIE — there are no selectors for either (see the class doc).
  library: LibrarySummary;
  release: ReleaseSummary;
  // Issue #1347: in document-level "apply to all" mode the caller supplies the releases present among
  // the Endpoint Details BIEs; when set, the Branch field becomes an enabled selector limited to them
  // (per-op callers leave this empty, so the Branch field stays locked as before).
  selectableReleases: ReleaseSummary[];

  readonly load = (params: HttpParams) => this.bieListService.getBieListPage(params);
  readonly rowKey = (row: BieListEntry) => row;

  constructor() {
    this.title = (this.data && this.data.title) || 'Select Confirm Message BIE';

    // Issue #1347: document-level "apply to all" mode — the caller passes the distinct releases of the
    // Endpoint Details BIEs; the Branch field then offers exactly those (see the template).
    if (this.data && Array.isArray(this.data.selectableReleases) && this.data.selectableReleases.length > 0) {
      this.selectableReleases = this.data.selectableReleases.map((r: any) => {
        const rs = new ReleaseSummary();
        rs.releaseId = r.releaseId;
        rs.releaseNum = r.releaseNum;
        return rs;
      });
    }

    // The OpenAPI Document has no release of its own; the picker is locked to the release of the
    // document's connected BIE, passed in by the caller. releaseNum drives the (disabled) Branch field.
    // In "apply to all" mode the default lands on the matching selectable release (else the first).
    if (this.data && this.data.releaseId) {
      this.release = (this.selectableReleases || []).find(r => r.releaseId === this.data.releaseId);
      if (!this.release) {
        this.release = new ReleaseSummary();
        this.release.releaseId = this.data.releaseId;
        this.release.releaseNum = this.data.releaseNum;
      }
    } else if (this.selectableReleases && this.selectableReleases.length > 0) {
      this.release = this.selectableReleases[0];
    }

    afterNextRender(() => this.start());
  }

  private start() {
    // Locked to the standard 'Confirm Message' BIE: a fixed DEN filter and a disabled search box (see
    // the template). Authors narrow the candidates via the advanced filters (e.g. Business Context).
    this.view().setFilter('den', CONFIRM_MESSAGE_DEN);

    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      // Fix the library to the default ('connectSpec') — there is no Library selector. The id is still
      // needed because the BIE-list query requires a libraryId (it is AND'd with the locked release).
      this.library = (libraries || []).find(e => e.isDefault) || (libraries || [])[0];

      if (this.release && this.release.releaseId) {
        // Release locked to the document's connected BIE.
        this.apply();
      } else if (this.library) {
        // Fallback: a document with only bodyless operations carries no connected BIE (hence no
        // release), so default to the library's latest Published, non-working release.
        this.releaseService.getReleaseSummaryList(this.library.libraryId, ['Published']).subscribe(releases => {
          this.release = (releases || []).filter(e => !e.workingRelease)[0];
          this.apply();
        });
      } else {
        // No library available (e.g. an empty instance) — the list stays empty.
        this.apply();
      }
    });
  }

  /** The picker is locked to a single library + release; without both, there is nothing to list. */
  private apply() {
    this.ready.set(!!this.library && !!this.release);
    if (this.ready()) {
      this.view().setFilter('releaseIds', ['' + this.release.releaseId]);
      this.extraParams.set({libraryId: '' + this.library.libraryId, access: 'CanView'});
    }
  }

  // Issue #1347: "apply to all" mode only — switch the Branch, then reload the ConfirmMessage BIE list
  // for that release (clearing any prior selection, which belonged to the previous release).
  onBranchChange(release: ReleaseSummary): void {
    this.release = release;
    this.selection.clear();
    this.view().setFilter('releaseIds', ['' + release.releaseId]);
    this.view().onSearch();
  }

  // Compare selectable releases by id so the Branch mat-select reflects the current release regardless
  // of object identity.
  compareRelease = (a: ReleaseSummary, b: ReleaseSummary): boolean => {
    return (!!a && !!b) ? a.releaseId === b.releaseId : a === b;
  };

  /** The search part of a link to the list that shows the BIE. */
  linkQuery(extras: { [key: string]: any }): string {
    return bieLinkQuery(CONFIRM_MESSAGE_TABLE, this.view().query, extras);
  }

  get selectedEntry(): BieListEntry {
    return this.selection.selected[0];
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  isDisabled() {
    return this.selection.selected.length === 0 || !this.selectedEntry;
  }

  onSelect(): void {
    if (this.isDisabled()) {
      return;
    }
    this.dialogRef.close({
      topLevelAsbiepId: this.selectedEntry.topLevelAsbiepId,
      den: this.selectedEntry.den,
      releaseId: this.release ? this.release.releaseId : undefined,
      releaseNum: this.release ? this.release.releaseNum : undefined
    } as OasDocConfirmMessageDialogResult);
  }
}
