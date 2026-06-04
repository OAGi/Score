import {Component, Inject, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {OpenAPIService} from '../domain/openapi.service';
import {AddOperationForOasDoc, OasDoc} from '../domain/openapi-doc';

/**
 * Issue #1730: Dialog to add an API operation (endpoint) that does NOT reference a BIE.
 * Only DELETE/PATCH are offered; the Message Body is always 'Request' and the response
 * status code is derived from the verb at generation time (DELETE -> 202, PATCH -> 204).
 */
@Component({
  standalone: false,
  selector: 'score-oas-doc-add-operation-dialog',
  templateUrl: './oas-doc-add-operation-dialog.component.html',
  styleUrls: ['./oas-doc-add-operation-dialog.component.css']
})
export class OasDocAddOperationDialogComponent {
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

  onVerbChange() {
    this.autoFillOperationId();
  }

  onResourceNameChange() {
    this.autoFillOperationId();
  }

  autoFillOperationId() {
    if (this.operationIdTouched) {
      return;
    }
    this.operation.operationId = this.deriveOperationId(this.operation.verb, this.operation.resourceName);
  }

  // Issue #1730: PATCH -> update..., DELETE -> delete..., using the last non-variable path segment,
  // e.g. PATCH /production-order/{id} -> updateProductionOrder
  deriveOperationId(verb: string, path: string): string {
    if (!verb || !path) {
      return '';
    }
    const segments = path.split('/')
      .map(s => s.trim())
      .filter(s => s.length > 0 && !s.startsWith('{'));
    const last = segments.length > 0 ? segments[segments.length - 1] : '';
    const resource = last.split(/[-_\s]+/)
      .filter(p => p.length > 0)
      .map(p => p.charAt(0).toUpperCase() + p.slice(1))
      .join('');
    const action = {
      DELETE: 'delete',
      PATCH: 'update'
    }[verb] || verb.toLowerCase();
    return resource ? action + resource : '';
  }

  get isValid(): boolean {
    return !!this.operation.verb
      && !!this.operation.resourceName
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
    }, _ => {
      this.isWorking = false;
      this.snackBar.open('Failed to add the operation.', '', {duration: 5000});
    });
  }
}
