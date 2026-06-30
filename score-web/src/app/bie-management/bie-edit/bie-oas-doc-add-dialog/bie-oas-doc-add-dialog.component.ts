import {Component, inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {OpenAPIService} from '../../openapi-doc/domain/openapi.service';
import {AssignBieForOasDoc, BieForOasDoc, buildOperationId, OasDoc, OasDocListRequest} from '../../openapi-doc/domain/openapi-doc';
import {initFilter} from '../../../common/utility';

export interface BieOasDocAddDialogData {
  topLevelAsbiepId: number;
  propertyTerm: string;
  // The BIE's first business context name — the backend builds the resource path from it (see resourcePath).
  businessContextName: string;
  // Issue #1492: the BIE's existing OpenAPI bindings (across all documents), so this dialog can pre-check —
  // client-side, exactly as the OpenAPI Document editor's Add dialog does — that the chosen (Verb, Message
  // Body) would not duplicate a body the BIE already has on the SELECTED document (the backend enforces it
  // too, returning a 400).
  existingBindings: BieForOasDoc[];
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
  styleUrls: ['./bie-oas-doc-add-dialog.component.css']
})
export class BieOasDocAddDialogComponent implements OnInit {
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

  // A request body is never valid for GET, so revert it to Response.
  onVerbChange(): void {
    if (this.messageBody === 'Request' && this.verb === 'GET') {
      this.messageBody = 'Response';
    }
  }

  get operationId(): string {
    return buildOperationId(this.verb, this.data.propertyTerm, this.arrayIndicator);
  }

  // The default Resource Name, derived exactly the way the backend does when adding a BIE to a document:
  // /<business-context>/<document-version>/<property-term>[-list]  (all lowercased, spaces -> dashes).
  // The document version segment is omitted only when the selected document has no version.
  get resourcePath(): string {
    const bc = (this.data.businessContextName || '').toLowerCase().replace(/ /g, '-');
    const term = (this.data.propertyTerm || '').replace(/\s/g, '-').toLowerCase();
    const name = this.arrayIndicator ? term + '-list' : term;
    const version = this.selectedOasDoc && this.selectedOasDoc.version ? this.selectedOasDoc.version : '';
    return version ? `/${bc}/${version}/${name}` : `/${bc}/${name}`;
  }

  // Issue #1492: the (path, verb, bodyType) slot the chosen selection would occupy. The backend derives the
  // path from the property term (+ array indicator), so the property term is the stable identity used for
  // the client-side duplicate pre-check. Mirrors oas-doc-assign-dialog.bodySlotKeyForAdd.
  private bodySlotKey(propertyTerm: string, verb: string, messageBody: string): string {
    return `${(propertyTerm || '').trim().toLowerCase()}|${verb || ''}|${messageBody || ''}`;
  }

  // Issue #1492: true when the chosen (Verb, Message Body) would duplicate a body this BIE already has on
  // the SELECTED document — the same client-side guard the OpenAPI Document editor's Add dialog applies.
  // Surfaced inline and used to disable Add (the backend enforces it as well, returning a 400).
  isDuplicateBodySlot(): boolean {
    if (!this.selectedOasDoc || !this.verb || !this.messageBody) {
      return false;
    }
    const key = this.bodySlotKey(this.data.propertyTerm, this.verb, this.messageBody);
    return (this.data.existingBindings || []).some(b =>
      b.oasDocId === this.selectedOasDoc.oasDocId && b.verb && b.messageBody &&
      (this.bodySlotKey(b.propertyTerm, b.verb, b.messageBody) === key ||
        (!!b.resourceName && this.bodySlotKey(b.resourceName, b.verb, b.messageBody) === key)));
  }

  get isValid(): boolean {
    return !!this.selectedOasDoc && !!this.verb && !!this.messageBody && !this.isDuplicateBodySlot();
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
    payload.operationId = this.operationId;
    this.dialogRef.close(payload);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
