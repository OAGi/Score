import {Component, Inject, inject, OnDestroy, ChangeDetectionStrategy} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {OpenAPIService} from '../domain/openapi.service';
import {AddOperationForOasDoc, OasDoc} from '../domain/openapi-doc';
import {OasOperationProposal} from '../domain/oas-doc-change';
import {of, Subject} from 'rxjs';
import {debounceTime, switchMap} from 'rxjs/operators';

/**
 * Issue #1730: Dialog to add an API operation (endpoint) that does NOT reference a BIE.
 * Only DELETE/PATCH are offered; the Message Body is always 'Request' and the response
 * status code is derived from the verb at generation time (DELETE -> 202, PATCH -> 204).
 */
@Component({
  standalone: false,
  selector: 'score-oas-doc-add-operation-dialog',
  templateUrl: './oas-doc-add-operation-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./oas-doc-add-operation-dialog.component.css']
})
export class OasDocAddOperationDialogComponent implements OnDestroy {
  private dialogRef = inject(MatDialogRef<OasDocAddOperationDialogComponent>);
  private openAPIService = inject(OpenAPIService);
  private snackBar = inject(MatSnackBar);

  oasDoc: OasDoc;
  operation: AddOperationForOasDoc = new AddOperationForOasDoc();
  verbs = ['DELETE', 'PATCH'];
  isWorking = false;
  // Tracks whether the user manually edited the Operation ID; once touched, stop auto-filling.
  operationIdTouched = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
    this.oasDoc = data && data.oasDoc;
    this.operation.oasDocId = this.oasDoc ? this.oasDoc.oasDocId : undefined;
    this.operation.verb = 'DELETE';
    // Issue #1730: Message Body is always Request; the response status code is derived from the verb.
    this.operation.messageBody = 'Request';
  }

  /** What the server makes of the operation (its Operation ID, when the user gave none), and whether it can be added. */
  proposal?: OasOperationProposal;
  private readonly proposalRequests = new Subject<void>();
  private proposalSubscription = this.proposalRequests.pipe(
    debounceTime(250),
    switchMap(() => this.operation.resourceName ? this.openAPIService.proposeOperation(this.oasDoc.oasDocId, {
      verb: this.operation.verb, messageBody: this.operation.messageBody, arrayIndicator: false,
      resourceName: this.operation.resourceName,
      operationId: this.operationIdTouched ? this.operation.operationId : undefined
    }) : of(undefined))
  ).subscribe(proposal => {
    this.proposal = proposal;
    if (proposal && !this.operationIdTouched) {
      this.operation.operationId = proposal.operationId;
    }
  });

  /** The verb, the path or the Operation ID was changed: ask the server again. */
  propose() {
    this.proposal = undefined; // not known until the server answers
    if (!this.operationIdTouched) {
      this.operation.operationId = ''; // the Operation ID that the user did not give is the server's: not known either
    }
    this.proposalRequests.next();
  }

  onVerbChange() {
    this.propose();
  }

  onResourceNameChange() {
    this.propose();
  }

  onOperationIdChange() {
    this.operationIdTouched = true;
    this.propose();
  }

  ngOnDestroy() {
    this.proposalSubscription.unsubscribe();
  }

  // Material only shows <mat-error> in an error state; this input has no validator, so drive it from what the server says.
  resourceNameErrorStateMatcher = {isErrorState: (): boolean => !!this.resourceNameError};

  /** Why the server says that the Resource Name is not one. */
  get resourceNameError(): string {
    return this.proposal?.violations.find(violation => violation.field === 'resourceName')?.message ?? '';
  }

  /** What else the server says is wrong with adding it (the endpoint has the body, the Operation ID is used...). */
  get problems(): string[] {
    return (this.proposal?.violations ?? []).filter(violation => violation.field !== 'resourceName').map(violation => violation.message);
  }

  get isValid(): boolean {
    return !!this.operation.verb
      && !!this.operation.resourceName
      && !!this.proposal && this.proposal.violations.length === 0
      && !!this.operation.operationId;
  }

  cancel() {
    this.dialogRef.close(false);
  }

  add() {
    if (!this.isValid || this.isWorking) {
      return;
    }
    this.isWorking = true;
    this.openAPIService.addOperationForOasDoc(this.operation).subscribe(_ => {
      this.snackBar.open('Operation added', '', {duration: 3000});
      this.dialogRef.close(true);
    }, err => {
      this.isWorking = false;
      this.snackBar.open(err?.headers?.get('x-error-message') || 'Failed to add the operation.', '', {duration: 5000});
    });
  }
}
