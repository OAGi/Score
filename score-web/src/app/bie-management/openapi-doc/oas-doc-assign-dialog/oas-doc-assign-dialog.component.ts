import {afterNextRender, ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {EMPTY, from, of, Subject} from 'rxjs';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {catchError, concatMap, debounceTime, finalize, map, switchMap} from 'rxjs/operators';
import {AssignBieForOasDoc, BieForOasDoc, OasDoc} from '../domain/openapi-doc';
import {OasOperationProposal} from '../domain/oas-doc-change';
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
import {BieCompactRowElement} from '../../bie-compact-row/bie-compact-row.component';

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
export class OasDocAssignDialogComponent implements OnInit, OnDestroy {
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
  /** What the server makes of each selected BIE as an operation of the document, and whether it can be added: shown, not made here. */
  proposals: { [topLevelAsbiepId: number]: OasOperationProposal } = {};
  private readonly proposalRequests = new Subject<void>();
  private proposalSubscription = this.proposalRequests.pipe(
    debounceTime(200),
    switchMap(() => {
      const rows = this.selection.selected;
      this.proposals = {};
      return rows.length === 0 ? EMPTY : this.openAPIService.proposeOperations(this.oasDoc.oasDocId, rows.map(row => ({
        topLevelAsbiepId: row.topLevelAsbiepId, propertyTerm: row.propertyTerm,
        verb: this.verbSelection[row.topLevelAsbiepId], messageBody: this.messageBodySelection[row.topLevelAsbiepId],
        arrayIndicator: !!row.arrayIndicator
      }))).pipe(map(proposals => rows.map((row, i) => [row.topLevelAsbiepId, proposals[i]] as [number, OasOperationProposal])));
    })
  ).subscribe(entries => entries.forEach(([id, proposal]) => this.proposals[id] = proposal));

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

  private readonly compactRows = new WeakMap<BieForOasDoc, BieCompactRowElement>();

  /** What the compact row shows of a BIE of the list (which has its own shape): made once for a row, so that the row is not drawn again. */
  compactRow(element: BieForOasDoc): BieCompactRowElement {
    let row = this.compactRows.get(element);
    if (!row) {
      const owner: any = element.owner;
      row = {
        guid: element.guid,
        den: element.den,
        displayName: element.displayName,
        state: element.state,
        release: {releaseNum: element.releaseNum},
        owner: {loginId: typeof owner === 'string' ? owner : owner?.loginId},
        version: element.version,
        status: element.status,
        bizTerm: (element as any).bizTerm,
        remark: element.remark,
        businessContextList: element.businessContexts,
        // The update is left out, as the column of it is (see `hiddenColumns` of the table).
        lastUpdated: undefined
      };
      this.compactRows.set(element, row);
    }
    return row;
  }

  constructor() {
    afterNextRender(() => this.start());
  }

  ngOnInit(): void {
    this.oasDoc = this.data.oasDoc;
    // what the server makes of a selection is asked again when the selection changes
    this.selection.changed.subscribe(() => this.propose());
    this.assignBieForOasDoc = new AssignBieForOasDoc();
    this.assignBieForOasDocList = [];
  }

  ngOnDestroy(): void {
    this.proposalSubscription.unsubscribe();
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

  /** The verb, the message body or the array indicator of a row was chosen: ask the server again. */
  propose(): void {
    this.proposalRequests.next();
  }

  onChange(property?: string, source?) {
    if (property === 'verbSelection') {
      this.propose();
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

  /** What the server says is wrong with adding the BIE as it is chosen (the endpoint has the body already, the Operation ID is used...). */
  problemsOf(bieForOasDoc: BieForOasDoc): string[] {
    return (this.proposals[bieForOasDoc.topLevelAsbiepId]?.violations ?? []).map(violation => violation.message);
  }

  /** Add waits for the server's answer about every selected BIE, and is there when none of them is refused. */
  isAddRefused(): boolean {
    return this.selection.selected.some(row => {
      const proposal = this.proposals[row.topLevelAsbiepId];
      return !proposal || proposal.violations.length > 0;
    });
  }

  /**
   * The selected BIEs are made into rows of the document that the page holds; they are saved by Update (the page presses it when
   * they can be saved). The Resource Name and the Operation ID are those that the server made (shown); the server makes them
   * again when the page asks, so a change of the verb or the array indicator of the row follows.
   */
  addBieForOasDoc() {
    const rows: BieForOasDoc[] = [];
    this.loading = true;
    from([...this.selection.selected]).pipe(
      concatMap(bieForOasDoc => this.openAPIService.checkBIEReusedAcrossMultipleOperations(bieForOasDoc, this.oasDoc).pipe(
        map(resp => {
          if (resp.errorMessages && resp.errorMessages.length > 0) {
            this.snackBar.open(resp.errorMessages[0], '', {duration: 5000});
            return; // skip this BIE, continue the batch
          }
          rows.push(this.draftOf(bieForOasDoc));
        }),
        catchError(_ => EMPTY)
      )),
      finalize(() => {
        this.loading = false;
        this.dialogRef.close({result: {status: 'OK', rows}});
      })
    ).subscribe();
  }

  private draftOf(bieForOasDoc: BieForOasDoc): BieForOasDoc {
    const id = bieForOasDoc.topLevelAsbiepId;
    const draft = new BieForOasDoc(bieForOasDoc);
    draft.verb = this.verbSelection[id];
    draft.messageBody = this.messageBodySelection[id];
    draft.arrayIndicator = !!bieForOasDoc.arrayIndicator;
    draft.suppressRootIndicator = true; // as when a BIE is added (Issue #1492: Suppress Root is the default)
    draft.tagName = bieForOasDoc.propertyTerm;
    draft.resourceName = this.proposals[id]?.resourceName ?? '';
    draft.operationId = this.proposals[id]?.operationId ?? '';
    return draft;
  }

  // Builds the assign payload for one selected BIE (extracted so the serialized add pipeline can map
  // each BIE to its POST). The server makes the Operation ID of the BIE ('<verb><BIEName>[List]') and its Resource Name.
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
