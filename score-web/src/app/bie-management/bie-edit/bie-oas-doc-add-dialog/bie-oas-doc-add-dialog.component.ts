import {Component, inject, OnDestroy, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FormControl} from '@angular/forms';
import {EMPTY, ReplaySubject, Subject} from 'rxjs';
import {switchMap, tap} from 'rxjs/operators';
import {OpenAPIService} from '../../openapi-doc/domain/openapi.service';
import {AssignBieForOasDoc, BieForOasDoc, OasDoc, OasDocListRequest} from '../../openapi-doc/domain/openapi-doc';
import {OasOperationCandidate, OasOperationProposal} from '../../openapi-doc/domain/oas-doc-change';
import {initFilter} from '../../../common/utility';

export interface BieOasDocAddDialogData {
  topLevelAsbiepId: number;
  propertyTerm: string;
}

/**
 * Issue #1519: from a BIE root, add the BIE to an OpenAPI Document (the inverse of the doc-centric
 * "Add BIE" dialog). It reuses the existing assign endpoint (POST /api/oas_doc/{id}/bie_list), so the
 * created resource + operation + request/response + message-body chain and the #1732 operation id are
 * produced exactly as on the OpenAPI Document screen. The dialog only builds and returns the payload; the
 * caller performs the request so it can refresh the panel and surface backend validation (e.g. #1492 400).
 */
@Component({
  standalone: false,
  selector: 'score-bie-oas-doc-add-dialog',
  templateUrl: './bie-oas-doc-add-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./bie-oas-doc-add-dialog.component.css']
})
export class BieOasDocAddDialogComponent implements OnInit, OnDestroy {
  private openAPIService = inject(OpenAPIService);
  dialogRef = inject(MatDialogRef<BieOasDocAddDialogComponent>);
  data: BieOasDocAddDialogData = inject(MAT_DIALOG_DATA);

  loading = false;
  oasDocs: OasDoc[] = [];
  oasDocFilterCtrl: FormControl = new FormControl();
  filteredOasDocs: ReplaySubject<OasDoc[]> = new ReplaySubject<OasDoc[]>(1);

  selectedOasDoc: OasDoc;
  verb = 'GET';
  messageBody = 'Response';
  arrayIndicator = false;
  suppressRootIndicator = false;

  ngOnInit(): void {
    this.loading = true;
    const request = new OasDocListRequest();
    request.page.sortActives = ['title'];
    request.page.sortDirections = ['asc'];
    request.page.pageIndex = 0;
    request.page.pageSize = 10000;
    this.openAPIService.getOasDocList(request).subscribe(resp => {
      this.oasDocs = resp.list || [];
      initFilter(this.oasDocFilterCtrl, this.filteredOasDocs, this.oasDocs, (e) => e.title);
      this.loading = false;
    });
  }

  /** What the server makes of the operation that adding makes, and whether it can be added: shown, not made here. */
  proposal?: OasOperationProposal;
  private readonly proposalRequests = new Subject<void>();
  private proposalSubscription = this.proposalRequests.pipe(
    tap(() => this.proposal = undefined), // not known until the server answers
    switchMap(() => this.selectedOasDoc ? this.openAPIService.proposeOperation(this.selectedOasDoc.oasDocId, this.candidate()) : EMPTY)
  ).subscribe(proposal => this.proposal = proposal);

  /** What the user has said of the operation: the server makes the rest. */
  candidate(): OasOperationCandidate {
    return {
      topLevelAsbiepId: this.data.topLevelAsbiepId, propertyTerm: this.data.propertyTerm,
      verb: this.verb, messageBody: this.messageBody, arrayIndicator: this.arrayIndicator
    };
  }

  /** The document, the verb, the message body or the array indicator was chosen: ask the server again. */
  propose(): void {
    this.proposalRequests.next();
  }

  ngOnDestroy(): void {
    this.proposalSubscription.unsubscribe();
  }

  get operationId(): string {
    return this.proposal?.operationId ?? '';
  }

  get resourcePath(): string {
    return this.proposal?.resourceName ?? '';
  }

  /** What the server says is wrong with adding it: the endpoint has the body already, the Operation ID is used... */
  get problems(): string[] {
    return (this.proposal?.violations ?? []).map(violation => violation.message);
  }

  get isValid(): boolean {
    return !!this.selectedOasDoc && !!this.verb && !!this.messageBody && !!this.proposal && this.proposal.violations.length === 0;
  }

  add(): void {
    if (!this.isValid) {
      return;
    }
    const payload = new AssignBieForOasDoc();
    payload.oasDocId = this.selectedOasDoc.oasDocId;
    payload.topLevelAsbiepId = this.data.topLevelAsbiepId;
    payload.propertyTerm = this.data.propertyTerm;
    payload.tagName = this.data.propertyTerm;
    payload.verb = this.verb;
    payload.messageBody = this.messageBody;
    payload.oasRequest = this.messageBody === 'Request';
    payload.arrayIndicator = this.arrayIndicator;
    payload.suppressRootIndicator = this.suppressRootIndicator;
    this.dialogRef.close(payload);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
