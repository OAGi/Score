import {afterNextRender, ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {EMPTY, from, of} from 'rxjs';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {catchError, concatMap, finalize, map} from 'rxjs/operators';
import {AssignBieForOasDoc, BieForOasDoc, OasDoc} from '../domain/openapi-doc';
import {buildOperationId} from '../domain/oas-operation-validation';
import {OpenAPIService} from '../domain/openapi.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ReleaseSummary} from '../../../release-management/domain/release';
import {ReleaseChoice} from '../../../release-management/domain/release-choice';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {AuthService} from '../../../authentication/auth.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {BIE_FILTERS as F, BIE_TABLE} from '../../bie-list/domain/bie.table';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';
import {LibrarySummary} from '../../../library-management/domain/library';
import {LibraryChoice} from '../../../library-management/domain/library-choice';
import {LibraryService} from '../../../library-management/domain/library.service';

const OAS_DOC_ASSIGN_TABLE = defineTable({
  ...BIE_TABLE,
  sortParam: 'sortActive',
  // The verb, the message body and the array indicator are chosen in the table, and not part of the column settings.
  fixed: [
    {id: 'verb', position: 'end', header: 'Verb'},
    {id: 'messageBody', position: 'end', header: 'Message Body'},
    {id: 'arrayIndicator', position: 'end', header: 'Array Indicator'},
  ],
  filters: [F.states, F.businessContext, F.version, F.remark, F.owner, F.updater, F.updatedDate,
    // The branch is chosen beside the search box; it is part of the query.
    {type: 'text', key: 'releaseId', label: 'Branch', hidden: true}],
});

@Component({
  standalone: false,
  selector: 'score-oas-doc-assign-dialog',
  templateUrl: './oas-doc-assign-dialog.component.html',
  styleUrls: ['./oas-doc-assign-dialog.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class OasDocAssignDialogComponent implements OnInit {
  private openAPIService = inject(OpenAPIService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private auth = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  webPageInfo = inject(WebPageInfoService);
  dialogRef = inject<MatDialogRef<OasDocAssignDialogComponent>>(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);

  title = 'Add BIE For OpenAPI Document';

  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly releaseChoice = new ReleaseChoice(this.auth, 'BIE');
  readonly columns = createTableColumns(OAS_DOC_ASSIGN_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BieForOasDoc>>(ScoreTableViewComponent);
  selection = new SelectionModel<BieForOasDoc>(true, []);

  /** The list is of a library and a branch: it waits for them, and loads again when another one is chosen. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});

  businessContextSelection = {};
  verbSelection = {};
  messageBodySelection = {};
  loading = false;
  oasDoc: OasDoc;
  assignBieForOasDoc: AssignBieForOasDoc;
  assignBieForOasDocList: AssignBieForOasDoc[];
  // Issue #1492 (Option 2): the document's already-added rows, for the duplicate-body pre-check.
  existingRows: BieForOasDoc[] = [];

  readonly load = (params: HttpParams) => this.openAPIService.selectBieForOasDocListPage(params, this.oasDoc).pipe(
    map(resp => {
      const list = resp.list.map((elm: BieForOasDoc) => {
        elm = new BieForOasDoc(elm);
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });
      list.forEach((elm: BieForOasDoc) => {
        this.businessContextSelection[elm.topLevelAsbiepId] = elm.businessContext;
        this.verbSelection[elm.topLevelAsbiepId] = elm.verb;
        this.messageBodySelection[elm.topLevelAsbiepId] = elm.messageBody;
      });
      return {...resp, list};
    }),
    catchError(error => {
      this.businessContextSelection = {};
      this.verbSelection = {};
      this.messageBodySelection = {};
      throw error;
    })
  );
  readonly rowKey = (row: BieForOasDoc) => row;

  constructor() {
    afterNextRender(() => this.start());
  }

  ngOnInit(): void {
    this.oasDoc = this.data.oasDoc;
    // Issue #1492 (Option 2): the live existing rows of the document are passed as the dialog data
    // (an array, with oasDoc/webPageInfo/isEditable attached as extra properties). Capture them so a
    // fast client pre-check can block adding a body that already exists on a (resourceName, verb).
    this.existingRows = Array.isArray(this.data) ? (this.data as BieForOasDoc[]).slice() : [];
    this.assignBieForOasDoc = new AssignBieForOasDoc();
    this.assignBieForOasDocList = [];
  }

  private start() {
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      this.applyLibrary();
      this.releaseService.getReleaseSummaryList(this.libraryChoice.library?.libraryId, ['Published']).subscribe(releases => {
        this.releaseChoice.init(releases.filter(e => !e.workingRelease), 0);
        this.applyRelease();
        this.ready.set(true);
      });
    });
  }

  /** Only what the user can view. */
  private applyLibrary() {
    this.extraParams.set({libraryId: '' + this.libraryChoice.library?.libraryId, access: 'CanView'});
  }

  private applyRelease() {
    this.view().setFilter('releaseId', '' + (this.releaseChoice.release?.releaseId || 0));
  }

  onBranchChange(release: ReleaseSummary) {
    this.releaseChoice.choose(release);
    this.applyRelease();
    this.selection.clear();
    this.view().onSearch();
  }

  onLibraryChange(library: LibrarySummary) {
    this.libraryChoice.choose(library);
    this.releaseService.getReleaseSummaryList(library.libraryId, ['Published']).subscribe(releases => {
      this.releaseChoice.init(releases.filter(e => !e.workingRelease));
      this.applyRelease();
      this.selection.clear();
      this.view().onSearch();
      this.applyLibrary();
    });
  }

  onChange(property?: string, source?) {
    if (property === 'verbSelection') {
      // A request body is never valid for GET, so revert it to Response. A DELETE request body is kept
      // in any version (Issue #1610): honored in OpenAPI 3.1, dropped (with a banner) in 3.0.
      if (this.messageBodySelection[source] === 'Request' && this.verbSelection[source] === 'GET') {
        this.messageBodySelection[source] = 'Response';
      }
    }
  }

  isSelected(row: BieForOasDoc) {
    return this.selection.isSelected(row);
  }

  isOptionMissing(): boolean {
    const selectedBieForOasDocs = this.selection.selected;
    for (const bieForOasDoc of selectedBieForOasDocs) {
      if (!this.verbSelection[bieForOasDoc.topLevelAsbiepId] || !this.messageBodySelection[bieForOasDoc.topLevelAsbiepId]) {
        return true;
      }
    }
    return false;
  }

  // Issue #1492 (Option 2): the (path, verb, bodyType) slot a selected BIE would occupy. The backend
  // derives the resource path from the property term (+ array indicator), so the property term is the
  // stable identity used for the client-side duplicate pre-check. Normalized to a comparable key.
  private bodySlotKeyForAdd(propertyTerm: string, verb: string, messageBody: string): string {
    return `${(propertyTerm || '').trim().toLowerCase()}|${verb || ''}|${messageBody || ''}`;
  }

  // Issue #1492 (Option 2): true when this row's chosen (verb, messageBody) would duplicate a body
  // already on the document. Surfaced inline (mat-error) and used to disable Add for that selection.
  isDuplicateBodySlotForAdd(bieForOasDoc: BieForOasDoc): boolean {
    const verb = this.verbSelection[bieForOasDoc.topLevelAsbiepId];
    const messageBody = this.messageBodySelection[bieForOasDoc.topLevelAsbiepId];
    if (!verb || !messageBody) {
      return false;
    }
    const key = this.bodySlotKeyForAdd(bieForOasDoc.propertyTerm, verb, messageBody);
    return (this.existingRows || []).some(row =>
      row.verb && row.messageBody &&
      (this.bodySlotKeyForAdd(row.propertyTerm, row.verb, row.messageBody) === key ||
        (!!row.resourceName && this.bodySlotKeyForAdd(row.resourceName, row.verb, row.messageBody) === key)));
  }

  // Issue #1492 (Option 2): block the Add button when any selected row duplicates an existing body slot.
  hasDuplicateBodySlotSelected(): boolean {
    return this.selection.selected.some(row => this.isDuplicateBodySlotForAdd(row));
  }

  addBieForOasDoc() {
    const selectedBieForOasDocs = this.selection.selected;
    for (const bieForOasDoc of selectedBieForOasDocs) {
      bieForOasDoc.verb = this.verbSelection[bieForOasDoc.topLevelAsbiepId];
      bieForOasDoc.messageBody = this.messageBodySelection[bieForOasDoc.topLevelAsbiepId];
    }

    // Issue #1492 (Option 2): fast client pre-check — block before any POST if a selection would add a
    // body that already exists on the document (a loaded row), or two selections collide within this
    // batch on the same (propertyTerm, verb, messageBody). The backend 400 remains the source of truth.
    const existingKeys = new Set<string>();
    for (const row of (this.existingRows || [])) {
      if (row.verb && row.messageBody) {
        existingKeys.add(this.bodySlotKeyForAdd(row.propertyTerm, row.verb, row.messageBody));
        // A row may have been added under a renamed Resource Name; also key by it so a rename is caught.
        if (row.resourceName && row.resourceName !== row.propertyTerm) {
          existingKeys.add(this.bodySlotKeyForAdd(row.resourceName, row.verb, row.messageBody));
        }
      }
    }
    // De-duplicate the batch WITHOUT aborting it. The in-batch key is identity-aware (topLevelAsbiepId +
    // array indicator) so two DIFFERENT selected BIEs that merely share a property term + (verb,
    // messageBody) are not collapsed into one slot — that false collision previously made a multi-select
    // Add silently no-op. A selection that truly duplicates a body already on the document, or an exact
    // in-batch duplicate, is SKIPPED (not aborted) so the remaining selections are still added. The
    // backend 400 remains the source of truth for real (path, verb, body) collisions.
    const batchKeys = new Set<string>();
    const toAdd: BieForOasDoc[] = [];
    let skipped = 0;
    for (const bieForOasDoc of selectedBieForOasDocs) {
      const verb = this.verbSelection[bieForOasDoc.topLevelAsbiepId];
      const messageBody = this.messageBodySelection[bieForOasDoc.topLevelAsbiepId];
      const existingKey = this.bodySlotKeyForAdd(bieForOasDoc.propertyTerm, verb, messageBody);
      const batchKey = `${bieForOasDoc.topLevelAsbiepId}|${!!bieForOasDoc.arrayIndicator}|${existingKey}`;
      if (existingKeys.has(existingKey) || batchKeys.has(batchKey)) {
        skipped++;
        continue;
      }
      batchKeys.add(batchKey);
      toAdd.push(bieForOasDoc);
    }
    if (toAdd.length === 0) {
      this.snackBar.open('The selected BIE(s) already have that message body on this document.', '', {
        duration: 5000,
      });
      return;
    }
    if (skipped > 0) {
      this.snackBar.open(skipped + ' selection(s) skipped — that message body already exists on this document.', '', {
        duration: 5000,
      });
    }

    // Issue #1492 (Option 2): SERIALIZE the adds. Each Add find-or-creates the (path, verb) operation
    // on the backend; firing the POSTs concurrently can race two find-or-creates into duplicate
    // operations, and the old loop also closed the dialog on the first response. Run them one at a time
    // (checkBIEReusedAcrossMultipleOperations then assignBieForOasDoc per BIE) and close only after all
    // complete.
    this.loading = true;
    let addedCount = 0;
    from(toAdd).pipe(
      concatMap(bieForOasDoc => this.openAPIService.checkBIEReusedAcrossMultipleOperations(bieForOasDoc, this.oasDoc).pipe(
        concatMap(resp => {
          if (resp.errorMessages && resp.errorMessages.length > 0) {
            this.snackBar.open(resp.errorMessages[0], '', {duration: 5000});
            return EMPTY; // skip this BIE, continue the batch
          }
          return this.openAPIService.assignBieForOasDoc(this.buildAssignPayload(bieForOasDoc)).pipe(
            concatMap(_ => {
              addedCount++;
              return of(true);
            }),
            // The global interceptor surfaces the backend 400 (dup-body) message; swallow it here so the
            // remaining adds still run and the dialog closes to reload whatever did succeed.
            catchError(_ => EMPTY)
          );
        }),
        catchError(_ => EMPTY)
      )),
      finalize(() => {
        this.loading = false;
        if (addedCount > 0) {
          this.snackBar.open('Added', '', {duration: 3000});
        }
        this.dialogRef.close({
          result: {
            status: 'OK'
          }
        });
      })
    ).subscribe();
  }

  // Builds the assign payload for one selected BIE (extracted so the serialized add pipeline can map
  // each BIE to its POST). Issue #1732: the frontend owns the initial operationId ('<verb><BIEName>[List]').
  private buildAssignPayload(bieForOasDoc: BieForOasDoc): AssignBieForOasDoc {
    const payload = new AssignBieForOasDoc();
    payload.messageBody = this.messageBodySelection[bieForOasDoc.topLevelAsbiepId];
    payload.propertyTerm = bieForOasDoc.propertyTerm;
    payload.tagName = bieForOasDoc.propertyTerm;
    payload.topLevelAsbiepId = bieForOasDoc.topLevelAsbiepId;
    payload.verb = this.verbSelection[bieForOasDoc.topLevelAsbiepId];
    if (payload.messageBody === 'Request') {
      payload.oasRequest = true;
    } else if (payload.messageBody === 'Response') {
      payload.oasRequest = false;
    }
    payload.oasDocId = this.oasDoc.oasDocId;
    payload.arrayIndicator = bieForOasDoc.arrayIndicator;
    payload.suppressRootIndicator = bieForOasDoc.suppressRootIndicator;
    payload.operationId = buildOperationId(payload.verb, bieForOasDoc.propertyTerm, bieForOasDoc.arrayIndicator);
    return payload;
  }

  // Retained for the unit spec (#1610): maps a single selection to the shared assignBieForOasDoc field
  // and POSTs it. The Add button uses the serialized addBieForOasDoc() pipeline above.
  doAddBieForOasDoc(bieForOasDoc: BieForOasDoc) {
      this.assignBieForOasDoc = this.buildAssignPayload(bieForOasDoc);
      this.openAPIService.assignBieForOasDoc(this.assignBieForOasDoc).subscribe(resp => {
        this.snackBar.open('Added', '', {
          duration: 3000,
        });
        this.dialogRef.close({
          result: {
            status: 'OK'
          }
        });
      });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
