import {ChangeDetectionStrategy, Component, DoCheck, HostListener, inject, OnDestroy, OnInit, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {
  BieForOasDoc,
  BieForOasDocDeleteRequest,
  OasDoc,
  OasDocHttpHeader,
  OasSecurityRequirement,
  OasSecurityScheme,
  SimpleOasDoc,
  toMinorOpenApiVersion
} from '../domain/openapi-doc';
import {
  applyResourceArrayMarker,
  buildOperationId,
  recomputeOperationId
} from '../domain/oas-operation-validation';
import {OasHttpHeaderDirection} from '../domain/openapi-doc';
import {OpenAPIService} from '../domain/openapi.service';
import {directionLabel, locationOf, newHttpHeader} from '../domain/oas-doc-http-header-form';
import {
  adoptServerHeaders,
  hasAnyPreset,
  headersOf,
  lacksPreset,
  linkHeadersToRows,
  offerPreset,
  sortHeaders,
  TRACEPARENT_PRESET,
  withdrawPreset
} from '../domain/oas-doc-http-header-preset';
import {OasDocChangeChecker} from '../domain/oas-doc-change-checker';
import {OasDocChangeResult, OasDocChangeSet, OasDocHeaderEdits, OasDocViolation} from '../domain/oas-doc-change';
import {FormControl} from '@angular/forms';
import {httpUrlValidator, optionalEmailValidator} from '../oas-doc-url.validator';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ErrorStateMatcher} from '@angular/material/core';
import {forkJoin} from 'rxjs';
import {hashCode, saveAsBlobResponse, saveBranch} from '../../../common/utility';
import {SelectionModel} from '@angular/cdk/collections';
import {finalize, map, tap} from 'rxjs/operators';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {OasDocAssignDialogComponent} from '../oas-doc-assign-dialog/oas-doc-assign-dialog.component';
import {OasDocAddOperationDialogComponent} from '../oas-doc-add-operation-dialog/oas-doc-add-operation-dialog.component';
import {OasDocHttpHeaderDialogComponent} from '../oas-doc-http-header-dialog/oas-doc-http-header-dialog.component';
import {OasDocSecuritySchemeDialogComponent} from '../oas-doc-security-scheme-dialog/oas-doc-security-scheme-dialog.component';
import {OasDocSecurityRequirementDialogComponent} from '../oas-doc-security-requirement-dialog/oas-doc-security-requirement-dialog.component';
import {
  OasDocConfirmMessageDialogComponent,
  OasDocConfirmMessageDialogResult
} from '../oas-doc-confirm-message-dialog/oas-doc-confirm-message-dialog.component';
import {OAS_DOC_BIE_TABLE} from '../domain/oas-doc-bie.table';
import {createTableColumns, EditedRows, ScoreTableViewComponent} from '../../../common/score-table';
import {BusinessContext} from '../../../context-management/business-context/domain/business-context';
import {BieExpressOption} from '../../bie-express/domain/generate-expression';
import {BusinessContextService} from '../../../context-management/business-context/domain/business-context.service';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {PageRequest} from '../../../basis/basis';
import {Title} from '@angular/platform-browser';
import {setAppTitleIfPresent} from '../../../common/app-title.strategy';
import {ReleaseSummary} from '../../../release-management/domain/release';
import {CompactRecord, memoizeRow} from '../../../common/record-compact-row';

@Component({
  standalone: false,
  selector: 'score-oas-doc-detail',
  templateUrl: './oas-doc-detail.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./oas-doc-detail.component.css']
})
export class OasDocDetailComponent implements OnInit, DoCheck, OnDestroy {
  private bizCtxService = inject(BusinessContextService);
  private openAPIService = inject(OpenAPIService);
  private accountService = inject(AccountListService);
  private auth = inject(AuthService);
  private location = inject(Location);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  private confirmDialogService = inject(ConfirmDialogService);
  private dialog = inject(MatDialog);
  private titleService = inject(Title);
  webPageInfo = inject(WebPageInfoService);


  title = 'Edit OpenAPI Document';
  oasDocs: SimpleOasDoc[];
  oasDoc: OasDoc;
  businessContextIdList: number[] = [];
  businessContextList: BusinessContext[] = [];
  hashCodeForOasDoc;
  bizCtxSearch: string;
  disabled: boolean;
  contactUrl = new FormControl('', [httpUrlValidator]);
  contactEmail = new FormControl('', [optionalEmailValidator]);
  licenseUrl = new FormControl('', [httpUrlValidator]);
  termServiceUrl = new FormControl('', [httpUrlValidator]);

  /** What the compact row shows of a BIE of the document: made once for a row. The controls of the endpoint go under it. */
  readonly compactRecord = memoizeRow((bie: BieForOasDoc): CompactRecord => ({
    badges: bie.releaseNum ? [{text: bie.releaseNum}] : [],
    title: bie.den,
    link: bie.topLevelAsbiepId ? '/profile_bie/' + bie.topLevelAsbiepId : undefined,
    titleTag: bie.displayName || undefined,
    subtitle: bie.guid,
    meta: bie.remark ? [{label: 'Remark', value: bie.remark}] : []
  }));

  readonly columns = createTableColumns(OAS_DOC_BIE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BieForOasDoc>>(ScoreTableViewComponent);
  selection = new SelectionModel<BieForOasDoc>(true, [], true, (a, b) => a.oasOperationId === b.oasOperationId && a.messageBody === b.messageBody);
  readonly rowKey = (row: BieForOasDoc) => row;
  businessContextSelection = {};
  /** The list waits for the document. */
  readonly ready = signal(false);
  /** The operations of the page that the table shows (edited in place). */
  rows: BieForOasDoc[] = [];
  /** The number of operations of the document. */
  totalRows = 0;
  /** The edited operations of the pages that were left: the server pages the table, and a page that is left would take its edits along. */
  readonly edits = new EditedRows<BieForOasDoc>(
    row => row.oasOperationId + '|' + row.messageBody, row => row.isChanged);
  readonly rowClass = (row: BieForOasDoc) => ({'highlight': row.status === 'Duplication'});
  readonly load = (params: HttpParams) => this.openAPIService.getBieForOasDocPage(this.oasDoc.oasDocId, params.set('access', 'CanView')).pipe(
    tap({
      next: resp => {
        this.totalRows = resp.length;
        resp.list = resp.list.map((elm: BieForOasDoc) => {
          elm = new BieForOasDoc(elm);
          elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
          elm.reset(); // reset the hashCode calculation when the bieForOasDoc is list and reloaded
          return elm;
        });
        // The page that was left keeps its edits (a sort, a search or the paginator shows the rows again).
        resp.list = this.edits.carry(this.rows, resp.list);
        this.rows = resp.list;
        // Issue #1791: the rows show the headers of the document.
        linkHeadersToRows(this.oasDoc, this.rows);
        this.rows.forEach((elm: BieForOasDoc) => {
          this.businessContextSelection[elm.topLevelAsbiepId] = elm.businessContext;
          // Issue #1347: seed the per-row baseline body type so the inline selector can revert a
          // cancelled CONFIRM_MESSAGE choice to its last committed value.
          this.committedErrorResponseBodyType.set(elm, elm.errorResponseBodyType || 'NONE');
        });
      },
      error: () => {
        this.edits.remember(this.rows);
        this.rows = [];
        this.totalRows = 0;
        this.businessContextSelection = {};
      }
    })
  );
  loading = false;
  isUpdating: boolean;
  option: BieExpressOption;
  openApiFormats: string[] = ['YAML', 'JSON'];
  topLevelAsbiepIds: number[];

  // Issue #1347: the Error Response column is an inline selector. The body type lives on the operation,
  // so a change propagates to every sibling row sharing the oasOperationId. CONFIRM_MESSAGE additionally
  // requires a picked ConfirmMessage BIE (a separate BIE-selection dialog), so when the user selects it
  // and cancels the dialog without a BIE we must revert the selector to its previously committed value —
  // this WeakMap remembers that value per row (seeded on load, refreshed on every commit).
  errorResponseBodyTypeOptions: { value: string; label: string }[] = [
    {value: 'NONE', label: 'No Response Body'},
    {value: 'PROBLEM_DETAILS', label: 'IETF Problem Details'},
    {value: 'CONFIRM_MESSAGE', label: 'OAGi Confirm Message'}
  ];
  private committedErrorResponseBodyType = new WeakMap<BieForOasDoc, string>();

  // Issue #1347: the document-level "apply to all" selector shown above the grid. Its value is applied
  // to every operation at once via applyErrorResponseBodyTypeToAll().
  bulkErrorResponseBodyType = 'NONE';

  // The rules about the operations and the entries of the document are the server's (an Operation ID that another operation
  // has, a second Request body on an endpoint, a path parameter that the path does not have...): the page says what the user
  // has changed, the server says whether it can be saved, and what it says is wrong is shown where it is. Only what the
  // field itself needs (an Operation ID that is not blank) is the page's.
  private readonly headerEdits = new OasDocHeaderEdits();
  readonly changeChecker = new OasDocChangeChecker({
    build: () => this.buildChangeSet(),
    check: changeSet => this.openAPIService.checkChanges(this.oasDoc.oasDocId, changeSet),
    onResult: result => this.takeCheckResult(result)
  });
  /** What the page was when the server was last asked, and when the last change was seen. */
  private checkedFingerprint = '';
  private lastFingerprint = '';
  /** The fields of the document (not its entries) as saved. */
  private savedFieldsHash: string;

  /** What the server said is wrong with a cell of a row: the operation, and the Request or Response row when it says which. */
  violationOf(row: BieForOasDoc, field: string): OasDocViolation | undefined {
    return this.changeChecker.violations().find(violation => violation.scope === 'OPERATION'
      && violation.oasOperationId === row.oasOperationId && violation.field === field
      && (!violation.messageBody || violation.messageBody === row.messageBody));
  }

  isDuplicateBodySlot(row: BieForOasDoc): boolean {
    return !!this.violationOf(row, 'messageBody');
  }

  operationIdErrorStateMatcher(row: BieForOasDoc): ErrorStateMatcher {
    return {isErrorState: () => !(row.operationId || '').trim() || !!this.violationOf(row, 'operationId')};
  }

  bodySlotErrorStateMatcher(row: BieForOasDoc): ErrorStateMatcher {
    return {isErrorState: () => !!this.violationOf(row, 'messageBody')};
  }

  /** Asks the server whenever the user has changed anything: of the document, of an operation of any page, or of the entries. */
  ngDoCheck(): void {
    if (!this.oasDoc?.oasDocId) {
      return;
    }
    const fingerprint = this.changeFingerprint();
    if (fingerprint !== this.lastFingerprint) {
      this.lastFingerprint = fingerprint;
      this.changeChecker.request();
    }
  }

  ngOnDestroy(): void {
    this.changeChecker.destroy();
  }

  private changeFingerprint(): string {
    return hashCode([this.oasDoc, this.edits.edited(this.rows).map(row => [row.oasOperationId, row.messageBody, row.hashCode])]);
  }

  private fieldsHash(oasDoc: OasDoc): string {
    return hashCode({...oasDoc, httpHeaders: undefined});
  }

  /** What the user has changed and has not saved, or `null` when nothing. */
  private buildChangeSet(): OasDocChangeSet | null {
    if (!this.isChanged()) {
      return null;
    }
    this.headerEdits.observe(this.oasDoc.httpHeaders);
    this.checkedFingerprint = this.changeFingerprint();
    return {
      document: this.fieldsHash(this.oasDoc) !== this.savedFieldsHash ? this.oasDoc : undefined,
      operations: this.getChanged(),
      headers: this.headerEdits.changes()
    };
  }

  /**
   * Takes the answer in: the entries of the page become what the server says they would be (those it registered for the
   * operations included). An answer to what the page was before the user changed it again is not taken.
   */
  private takeCheckResult(result: OasDocChangeResult): boolean {
    if (this.changeFingerprint() !== this.checkedFingerprint) {
      return false;
    }
    if (result.valid) {
      this.oasDoc.httpHeaders = result.httpHeaders || [];
      sortHeaders(this.oasDoc);
      this.headerEdits.adopt(this.oasDoc.httpHeaders);
      linkHeadersToRows(this.oasDoc, this.rows);
      this.lastFingerprint = this.changeFingerprint(); // the answer is not a change of the user's
    }
    return true;
  }

  ngOnInit(): void {
    this.topLevelAsbiepIds = [];
    this.option = new BieExpressOption();
    this.option.bieDefinition = true;
    this.option.packageOption = 'ALL';
    // Default OpenAPI expression format is 'YAML'.
    this.option.openAPIExpressionFormat = 'YAML';
    this.oasDoc = new OasDoc();

    const oasDocId = this.route.snapshot.params.id;

    forkJoin([
      this.openAPIService.getOasDoc(oasDocId)
    ]).subscribe(([simpleOasDoc]) => {
      if (!simpleOasDoc) {
        this.redirectToOasDocList();
        return;
      }

      this.oasDoc = simpleOasDoc;
      this.init(this.oasDoc);
      this.ready.set(true);
    }, err => {
      this.isUpdating = false;
      if (err.status === 404) {
        this.redirectToOasDocList();
        return;
      }

      const errorMessage = (err.status === 403) ?
        'You do not have access permission.' : 'Something\'s wrong.';
      this.snackBar.open(errorMessage, '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/profile_bie/express/oas_doc');
    });
  }

  private redirectToOasDocList() {
    this.loading = false;
    this.isUpdating = false;
    this.snackBar.open('The requested OpenAPI document is unavailable.', '', {
      duration: 3000,
    });
    this.router.navigateByUrl('/profile_bie/express/oas_doc');
  }

  init(oasDoc: OasDoc) {
    // Issue #1729: default to an empty list (= legacy OAuth2 default) so the baseline hashCode
    // includes it and adding/removing schemes is detected as a change.
    if (!oasDoc.securitySchemes) {
      oasDoc.securitySchemes = [];
    }
    if (!oasDoc.securityRequirements) {
      oasDoc.securityRequirements = [];
    }
    // Issue #1791: likewise for the HTTP headers.
    if (!oasDoc.httpHeaders) {
      oasDoc.httpHeaders = [];
    }
    // Issue #1760: the version selector offers only the minor family (3.0 / 3.1). Fold any stored patch
    // value (including legacy 3.0.3 / 3.1.1) to its minor so the mat-select matches the loaded value and
    // the baseline hashCode reflects what is displayed (no spurious "changed" state).
    oasDoc.openAPIVersion = toMinorOpenApiVersion(oasDoc.openAPIVersion);
    sortHeaders(oasDoc); // one order, so that enabling after disabling is no change
    this.hashCodeForOasDoc = hashCode(oasDoc);
    this.savedHttpHeadersHash = hashCode(oasDoc.httpHeaders);
    this.oasDoc = oasDoc;
    this.savedFieldsHash = this.fieldsHash(oasDoc);
    this.headerEdits.reset(oasDoc.httpHeaders);
    this.changeChecker.reset();
    this.lastFingerprint = this.changeFingerprint();
    setAppTitleIfPresent(this.titleService, this.oasDoc.title, 'OpenAPI Document');
    this.isUpdating = false;
    this.showValidationErrors();
    this.refreshOperationIds();
  }

  // An invalid value that is already in the document (a Terms of Service that is not a URL, say) keeps Update disabled
  // for every change. A field shows its error only once it is touched, so touch them: the reason is on the screen.
  showValidationErrors() {
    [this.contactUrl, this.contactEmail, this.licenseUrl, this.termServiceUrl].forEach(control => control.markAsTouched());
  }

  // Issue #1791: the traceparent header (a preset of the UI, the server knows no header by name) of the operations of the
  // document. Enable gives every operation that has none a header of its own, Disable takes every one away. Either is a
  // change of the document in the page: Update saves it. Enable is there while an operation lacks the header, Disable
  // while any operation has it; a BIE that is added later lacks it until Enable is used again.
  /** The operations of the document, of every page: the ones the table shows are only a page of them. */
  operationIds: number[] = [];

  private fetchOperationIds() {
    const params = new HttpParams().set('access', 'CanView').set('pageIndex', '-1').set('pageSize', '-1')
      .set('sortActives', '').set('sortDirections', '');
    return this.openAPIService.getBieForOasDocPage(this.oasDoc.oasDocId, params).pipe(
      map(resp => [...new Set((resp.list || []).map(row => row.oasOperationId).filter(id => !!id))]));
  }

  refreshOperationIds() {
    this.fetchOperationIds().subscribe(ids => this.operationIds = ids);
  }

  get canEnableTraceparent(): boolean {
    return !!this.oasDoc && lacksPreset(this.oasDoc, TRACEPARENT_PRESET, this.operationIds);
  }

  get canDisableTraceparent(): boolean {
    return !!this.oasDoc && hasAnyPreset(this.oasDoc, TRACEPARENT_PRESET);
  }

  enableTraceparent() {
    this.fetchOperationIds().subscribe(operationIds => {
      this.operationIds = operationIds;
      const {added, requestParameters, responseHeaders} = offerPreset(this.oasDoc, TRACEPARENT_PRESET, operationIds);
      linkHeadersToRows(this.oasDoc, this.rows);
      this.snackBar.open(added.length === 0
        ? 'Every operation has traceparent already.'
        : 'traceparent is added (' + requestParameters + ' request parameter' + (requestParameters === 1 ? '' : 's')
        + ' and ' + responseHeaders + ' response header' + (responseHeaders === 1 ? '' : 's') + ').', '', {duration: 4000});
    });
  }

  disableTraceparent() {
    const {removed, requestParameters, responseHeaders} = withdrawPreset(this.oasDoc, TRACEPARENT_PRESET);
    linkHeadersToRows(this.oasDoc, this.rows);
    this.snackBar.open(removed === 0
      ? 'No operation has traceparent.'
      : 'traceparent is removed (' + requestParameters + ' request parameter' + (requestParameters === 1 ? '' : 's')
      + ' and ' + responseHeaders + ' response header' + (responseHeaders === 1 ? '' : 's') + ').', '', {duration: 4000});
  }

  headersOf = headersOf;

  /** What follows the name of a parameter in the table: where it is, unless it is in a header, as most are. */
  locationLabel(header: OasDocHttpHeader): string {
    const location = locationOf(header);
    return header.direction === 'REQUEST' && location !== 'header' ? ' (' + location + ')' : '';
  }

  // Issue #1791: the request parameters and the response headers of an operation are edited in a dialog, whatever they
  // are: the + of a column of a row adds one of that direction to its operation (so a GET, which has no Request row,
  // gets its request parameters here too), a click on one edits or deletes it. The dialog edits a copy; what it returns changes the document in
  // the page (the objects of the rows are those of the document), and Update saves it.
  addHttpHeader(row: BieForOasDoc, direction: OasHttpHeaderDirection, event?: Event) {
    event?.stopPropagation();
    this.openHttpHeaderDialog(row, undefined, direction);
  }

  editHttpHeader(row: BieForOasDoc, header: OasDocHttpHeader, event?: Event) {
    event?.stopPropagation();
    this.openHttpHeaderDialog(row, header, header.direction);
  }

  private openHttpHeaderDialog(row: BieForOasDoc, header: OasDocHttpHeader | undefined, direction: OasHttpHeaderDirection) {
    const isNew = !header;
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      header: header ? JSON.parse(JSON.stringify(header)) : newHttpHeader(direction, row.oasOperationId),
      isNew,
      scopeLabel: header && !header.oasOperationId
        ? 'This ' + directionLabel(direction).toLowerCase() + ' is one of the whole document: it applies to every operation.'
        : 'Operation: ' + (row.operationId || row.oasOperationId),
      others: (this.oasDoc.httpHeaders || []).filter(other => other !== header),
      // the path as the generated document has it: the {version} of the path is the version of the document
      resourcePath: this.oasDoc.version ? (row.resourceName || '').split('{version}').join(this.oasDoc.version) : row.resourceName,
      openApiVersion: this.oasDoc.openAPIVersion
    };
    // wide for a wide screen, where the form is in two columns, and as narrow as it was for a narrow one
    dialogConfig.width = window.innerWidth >= 1200 ? '1100px' : '720px';
    dialogConfig.maxWidth = '95vw';
    dialogConfig.maxHeight = '85vh';
    dialogConfig.autoFocus = false;
    this.dialog.open(OasDocHttpHeaderDialogComponent, dialogConfig).afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      this.oasDoc.httpHeaders = this.oasDoc.httpHeaders || [];
      if (result.action === 'delete') {
        this.oasDoc.httpHeaders = this.oasDoc.httpHeaders.filter(other => other !== header);
      } else if (header) {
        // the same object stays in the document, so that what shows it still shows it
        Object.keys(header).forEach(key => delete (header as any)[key]);
        Object.assign(header, result.header);
      } else {
        this.oasDoc.httpHeaders.push(result.header);
      }
      sortHeaders(this.oasDoc);
      linkHeadersToRows(this.oasDoc, this.rows);
    });
  }

  // Issue #1729: selectable security scheme types (OpenAPI 3.0).
  securitySchemeTypes = [
    {value: 'apiKey', label: 'API Key'},
    {value: 'http', label: 'HTTP'},
    {value: 'oauth2', label: 'OAuth 2.0'},
    {value: 'openIdConnect', label: 'OpenID Connect'}
  ];

  // The document's Security Schemes (empty = default OAuth2).
  get securitySchemes(): OasSecurityScheme[] {
    if (!this.oasDoc.securitySchemes) {
      this.oasDoc.securitySchemes = [];
    }
    return this.oasDoc.securitySchemes;
  }

  // Issue #1729: open the dialog to add a new security scheme.
  addSecurityScheme() {
    this.openSchemeDialog(null, -1);
  }

  // Open the dialog to edit an existing scheme (clicking its card).
  editSecurityScheme(scheme: OasSecurityScheme, index: number) {
    this.openSchemeDialog(scheme, index);
  }

  removeSecurityScheme(index: number, event?: Event) {
    if (event) {
      event.stopPropagation();
    }
    const removedSchemeName = this.securitySchemes[index]?.schemeName;
    this.securitySchemes.splice(index, 1);
    if (removedSchemeName) {
      this.oasDoc.securityRequirements = this.pruneSecurityRequirements(this.oasDoc.securityRequirements, removedSchemeName);
      this.rows.forEach(row => {
        const hadRequirements = (row.securityRequirements || []).length > 0;
        row.securityRequirements = this.pruneSecurityRequirements(row.securityRequirements, removedSchemeName);
        // Only a custom override that lost ALL its schemes reverts to inherit. A Public override
        // (securityOverridden=true with intentionally-empty requirements) must stay public.
        if (hadRequirements && row.securityRequirements.length === 0) {
          row.securityOverridden = false;
        }
      });
    }
  }

  private openSchemeDialog(scheme: OasSecurityScheme, index: number) {
    const isNew = index < 0;
    // Deep copy so Cancel discards edits; existingNames excludes the scheme being edited.
    const working: OasSecurityScheme = scheme
      ? JSON.parse(JSON.stringify(scheme))
      : ({type: 'apiKey'} as OasSecurityScheme);
    const existingNames = this.securitySchemes
      .filter((_, i) => i !== index)
      .map(s => s.schemeName)
      .filter(n => !!n);
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {scheme: working, isNew, existingNames};
    dialogConfig.width = '720px';
    dialogConfig.maxHeight = '85vh';
    dialogConfig.autoFocus = false;
    this.dialog.open(OasDocSecuritySchemeDialogComponent, dialogConfig)
      .afterClosed().subscribe(result => {
        if (!result) {
          return;
        }
        if (isNew) {
          this.securitySchemes.push(result);
        } else {
          const previousSchemeName = this.securitySchemes[index]?.schemeName;
          this.securitySchemes[index] = result;
          if (previousSchemeName && previousSchemeName !== result.schemeName) {
            const newName = result.schemeName;
            // Propagate a rename into the dependent requirements (the scheme keeps its id). A blank new
            // name (shouldn't happen — Update is gated) falls back to pruning the now-unnamed references.
            const apply = (reqs: OasSecurityRequirement[]) => newName
              ? this.renameSecurityRequirements(reqs, previousSchemeName, newName)
              : this.pruneSecurityRequirements(reqs, previousSchemeName);
            this.oasDoc.securityRequirements = apply(this.oasDoc.securityRequirements);
            this.rows.forEach(row => {
              const hadRequirements = (row.securityRequirements || []).length > 0;
              row.securityRequirements = apply(row.securityRequirements);
              // Preserve a Public override (empty by intent); only revert a custom override that lost all schemes.
              if (hadRequirements && row.securityRequirements.length === 0) {
                row.securityOverridden = false;
              }
            });
          }
        }
      });
  }

  // A short label/summary for a scheme card.
  securitySchemeTypeLabel(type: string): string {
    const t = this.securitySchemeTypes.find(x => x.value === type);
    return t ? t.label : type;
  }

  securitySchemeSummary(scheme: OasSecurityScheme): string {
    if (scheme.type === 'apiKey') {
      return `in: ${scheme.apiKeyIn || ''}, name: ${scheme.apiKeyName || ''}`;
    }
    if (scheme.type === 'http') {
      return `scheme: ${scheme.httpScheme || ''}` + (scheme.bearerFormat ? `, ${scheme.bearerFormat}` : '');
    }
    if (scheme.type === 'openIdConnect') {
      return scheme.openIdConnectUrl || '';
    }
    if (scheme.type === 'oauth2') {
      const flows = (scheme.flows || []).map(f => f.flowType).join(', ');
      return flows ? `flows: ${flows}` : 'default flow';
    }
    return '';
  }

  // Validate every scheme: Scheme Name required + unique; type-specific required fields present.
  areSecuritySchemesValid(oasDoc1: OasDoc): boolean {
    const schemes = oasDoc1.securitySchemes || [];
    const names = new Set<string>();
    for (const s of schemes) {
      if (!s.type) {
        return false;
      }
      const name = (s.schemeName || '').trim();
      if (!name || names.has(name)) {
        return false;
      }
      names.add(name);
      if (s.type === 'apiKey' && (!s.apiKeyIn || !s.apiKeyName)) {
        return false;
      }
      if (s.type === 'http' && !s.httpScheme) {
        return false;
      }
      if (s.type === 'openIdConnect' && !s.openIdConnectUrl) {
        return false;
      }
    }
    return true;
  }

  areSecurityRequirementsValid(oasDoc1: OasDoc): boolean {
    const schemeNames = new Set((oasDoc1.securitySchemes || []).map(s => s.schemeName));
    const valid = (requirements: OasSecurityRequirement[]) => (requirements || []).every(req =>
      (req.schemes || []).every(scheme => schemeNames.has(scheme.schemeName)));
    return valid(oasDoc1.securityRequirements);
  }

  documentSecuritySummary(): string {
    const summary = this.securityRequirementSummary(this.oasDoc.securityRequirements, false);
    return summary === 'No Security' ? 'None' : summary;
  }

  operationSecuritySummary(row: BieForOasDoc): string {
    if (!row.securityOverridden) {
      return 'Inherited';
    }
    const summary = this.securityRequirementSummary(row.securityRequirements, false);
    // securityOverridden with no requirements => security: [] (public).
    return summary === 'No Security' ? 'Public' : summary;
  }

  openDocumentSecurityDialog() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      title: 'Document Security',
      securitySchemes: this.securitySchemes,
      securityRequirements: JSON.parse(JSON.stringify(this.oasDoc.securityRequirements || [])),
      allowInherit: false
    };
    dialogConfig.width = '760px';
    dialogConfig.maxHeight = '85vh';
    dialogConfig.autoFocus = false;
    this.dialog.open(OasDocSecurityRequirementDialogComponent, dialogConfig)
      .afterClosed().subscribe(result => {
        if (!result) {
          return;
        }
        this.oasDoc.securityRequirements = result.securityRequirements || [];
      });
  }

  openOperationSecurityDialog(row: BieForOasDoc, event?: Event) {
    if (event) {
      event.stopPropagation();
    }
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      title: 'Operation Security',
      securitySchemes: this.securitySchemes,
      securityOverridden: row.securityOverridden,
      securityRequirements: JSON.parse(JSON.stringify(row.securityRequirements || [])),
      allowInherit: true
    };
    dialogConfig.width = '760px';
    dialogConfig.maxHeight = '85vh';
    dialogConfig.autoFocus = false;
    this.dialog.open(OasDocSecurityRequirementDialogComponent, dialogConfig)
      .afterClosed().subscribe(result => {
        if (!result) {
          return;
        }
        // An oas_operation can be shown as two rows (Request + Response). Security is per operation, so
        // apply the result to every sibling row sharing the oasOperationId (each gets its own copy).
        // Truthy check: a not-yet-persisted operation has oasOperationId 0 (falsy); never treat those as
        // siblings of one another, otherwise the edit would cross-contaminate unrelated new rows.
        const siblings = this.rows.filter(r =>
          !!r.oasOperationId && r.oasOperationId === row.oasOperationId);
        const targets = siblings.length > 0 ? siblings : [row];
        targets.forEach(r => {
          r.securityOverridden = result.securityOverridden;
          r.securityRequirements = JSON.parse(JSON.stringify(result.securityRequirements || []));
        });
      });
  }

  // Issue #1347: the DEN shown beside the selector when CONFIRM_MESSAGE is chosen. The chip is clickable
  // to re-pick the ConfirmMessage BIE; a blank DEN prompts the author to pick one.
  confirmMessageChipLabel(row: BieForOasDoc): string {
    return row.confirmMessageDen ? row.confirmMessageDen : 'Pick a ConfirmMessage BIE…';
  }

  // Issue #1347: the inline Error Response selector changed. NONE / PROBLEM_DETAILS commit immediately
  // (and clear any ConfirmMessage BIE); CONFIRM_MESSAGE opens the BIE-selection dialog. If the dialog is
  // cancelled without a BIE (and none was previously chosen) the selector reverts to its prior value.
  onErrorResponseBodyTypeChange(row: BieForOasDoc): void {
    const newType = row.errorResponseBodyType;
    const previousType = this.committedErrorResponseBodyType.get(row) || 'NONE';
    if (newType !== 'CONFIRM_MESSAGE') {
      this.applyErrorResponseBodyType(row, newType, undefined, undefined);
      return;
    }
    this.openConfirmMessageDialog(row, result => {
      if (result) {
        this.applyErrorResponseBodyType(row, 'CONFIRM_MESSAGE', result.topLevelAsbiepId, result.den);
      } else if (row.confirmMessageTopLevelAsbiepId) {
        // Cancelled, but the operation already had a ConfirmMessage BIE — keep CONFIRM_MESSAGE as-is.
        this.applyErrorResponseBodyType(row, 'CONFIRM_MESSAGE',
          row.confirmMessageTopLevelAsbiepId, row.confirmMessageDen);
      } else {
        // Cancelled with no BIE picked — revert the selector to the previously committed body type.
        this.applyErrorResponseBodyType(row, previousType, undefined, undefined);
      }
    });
  }

  // Issue #1347: re-open the ConfirmMessage BIE picker from the cell chip (CONFIRM_MESSAGE already set).
  // Cancelling keeps the existing BIE; picking replaces it on every sibling row.
  openConfirmMessageBiePicker(row: BieForOasDoc, event?: Event): void {
    if (event) {
      event.stopPropagation();
      // Keyboard activation (Enter/Space) reaches here too; stop Space from scrolling the page.
      event.preventDefault();
    }
    this.openConfirmMessageDialog(row, result => {
      if (result) {
        this.applyErrorResponseBodyType(row, 'CONFIRM_MESSAGE', result.topLevelAsbiepId, result.den);
      }
    });
  }

  private openConfirmMessageDialog(row: BieForOasDoc,
                                   handler: (result: OasDocConfirmMessageDialogResult | undefined) => void): void {
    // A BIE-backed operation carries its own release, so lock the picker's Branch to it (as before).
    if (row.releaseId) {
      this.openConfirmMessageDialogWith(row, handler, undefined,
        {releaseId: row.releaseId, releaseNum: row.releaseNum});
      return;
    }
    // A bodyless operation has no release of its own. In a document that spans MULTIPLE releases a single
    // locked release cannot reach every release's Confirm Message BIE (the BIE-list query is release-bound
    // — e.g. a 10.11 Confirm Message BIE is invisible while locked to 10.12.7), so offer the enabled Branch
    // selector over the whole document's releases — the same mechanism the document-level "apply to all"
    // mode uses. With one release there is nothing to choose (lock to it); with none, the dialog falls back
    // to the connectSpec library's latest release.
    this.fetchAllOasDocReleasesThen(selectableReleases => {
      if (selectableReleases.length > 1) {
        this.openConfirmMessageDialogWith(row, handler, selectableReleases, selectableReleases[0]);
      } else {
        const only = selectableReleases[0];
        this.openConfirmMessageDialogWith(row, handler, undefined,
          {releaseId: only ? only.releaseId : undefined, releaseNum: only ? only.releaseNum : undefined});
      }
    });
  }

  // Issue #1347: open the ConfirmMessage BIE picker. When `selectableReleases` is set the dialog's Branch
  // becomes an enabled selector over those releases (document-level / bodyless-multi-release mode); when it
  // is undefined the Branch is locked to `defaultRelease`. Like the 'Include Meta Header' / 'Pagination
  // Response' pickers, the dialog fixes the library (connectSpec) and lists only 'Confirm Message' BIEs.
  private openConfirmMessageDialogWith(row: BieForOasDoc,
                                       handler: (result: OasDocConfirmMessageDialogResult | undefined) => void,
                                       selectableReleases: ReleaseSummary[] | undefined,
                                       defaultRelease: { releaseId: number; releaseNum: string } | undefined): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      title: 'Select Confirm Message BIE',
      selectableReleases,
      releaseId: defaultRelease ? defaultRelease.releaseId : undefined,
      releaseNum: defaultRelease ? defaultRelease.releaseNum : undefined
    };
    // Full-width like the BIE-Express 'Include Meta Header' / 'Pagination Response' BIE pickers.
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.autoFocus = false;
    this.dialog.open(OasDocConfirmMessageDialogComponent, dialogConfig)
      .afterClosed().subscribe((result: OasDocConfirmMessageDialogResult | undefined) => handler(result));
  }

  // Issue #1347: apply an error-response body type (and ConfirmMessage BIE, if any) to an operation.
  // An oas_operation can be shown as two rows (Request + Response); the body type is per operation, so
  // the value is applied to every sibling row sharing the oasOperationId. Truthy check: a not-yet-
  // persisted operation has oasOperationId 0 (falsy) and must never be treated as a sibling.
  private applyErrorResponseBodyType(row: BieForOasDoc, bodyType: string,
                                     confirmTopLevelAsbiepId: number, confirmDen: string): void {
    const siblings = this.rows.filter(r =>
      !!r.oasOperationId && r.oasOperationId === row.oasOperationId);
    const targets = siblings.length > 0 ? siblings : [row];
    const clearConfirm = bodyType !== 'CONFIRM_MESSAGE';
    targets.forEach(r => {
      r.errorResponseBodyType = bodyType;
      r.confirmMessageTopLevelAsbiepId = clearConfirm ? undefined : confirmTopLevelAsbiepId;
      r.confirmMessageDen = clearConfirm ? '' : (confirmDen || '');
      this.committedErrorResponseBodyType.set(r, bodyType);
    });
  }

  private bulkBodyTypeLabel(value: string): string {
    return (this.errorResponseBodyTypeOptions.find(o => o.value === value) || {label: value}).label;
  }

  // Issue #1347: document-level "apply to all" of the Error Response Body Type. This is a SERVER-SIDE
  // action, so it covers EVERY operation of the document (the grid is server-paginated — a client-side
  // apply would silently miss off-page operations). Any unsaved inline edits are persisted first (so
  // they are not lost and the server state the bulk apply reads is consistent), then the bulk applies
  // and the grid is reloaded to reflect it. NONE / IETF Problem Details apply to all operations
  // regardless of release; OAGi Confirm Message prompts for a release (from the whole document's unique
  // releases) + a ConfirmMessage BIE, and the server applies it to that release's operations plus
  // bodyless operations.
  applyErrorResponseBodyTypeToAll(): void {
    if (this.rows.length === 0 && this.totalRows === 0) {
      return;
    }
    const bodyType = this.bulkErrorResponseBodyType;
    this.persistChangesThen(() => {
      if (bodyType !== 'CONFIRM_MESSAGE') {
        this.sendBulkErrorResponse({errorResponseBodyType: bodyType});
        return;
      }
      // Confirm Message is release-bound: fetch the whole document's releases (without disturbing the
      // paginated grid), then pick a release + ConfirmMessage BIE.
      this.fetchAllOasDocReleasesThen(selectableReleases => {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
          title: 'Select Confirm Message BIE',
          selectableReleases,
          // Default to the first release. A document with only bodyless operations has none, so the
          // dialog falls back to the connectSpec library's latest Published release.
          releaseId: selectableReleases.length > 0 ? selectableReleases[0].releaseId : undefined,
          releaseNum: selectableReleases.length > 0 ? selectableReleases[0].releaseNum : undefined
        };
        dialogConfig.width = window.innerWidth + 'px';
        dialogConfig.autoFocus = false;
        this.dialog.open(OasDocConfirmMessageDialogComponent, dialogConfig)
          .afterClosed().subscribe((result: OasDocConfirmMessageDialogResult | undefined) => {
            if (!result || !result.topLevelAsbiepId) {
              // Cancelled — refresh the grid so it reflects any edits persisted above.
              this.loadBieListForOasDoc();
              return;
            }
            this.sendBulkErrorResponse({
              errorResponseBodyType: 'CONFIRM_MESSAGE',
              confirmMessageTopLevelAsbiepId: result.topLevelAsbiepId,
              releaseId: result.releaseId
            });
          });
      });
    });
  }

  // Issue #1347: fetch the document's distinct BIE releases for the "apply to all" ConfirmMessage Branch
  // selector. The backend derives them in a single SELECT DISTINCT query (bodyless operations carry no
  // release and are excluded server-side), so the whole paginated BIE list is no longer fetched only to
  // combine releases client-side, and the visible grid's pagination/filters are left untouched.
  private fetchAllOasDocReleasesThen(done: (releases: ReleaseSummary[]) => void): void {
    this.loading = true;
    this.openAPIService.getReleasesForOasDoc(this.oasDoc)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        releases => done(releases || []),
        _ => done([]));
  }

  // Issue #1347: persist any unsaved inline edits before a document-level bulk apply, so those edits are
  // not lost (the bulk apply + reload would otherwise overwrite them) and the server state the bulk
  // reads is consistent. Mirrors doUpdate's validation + doc/details save, but chains `onDone` to run
  // only after the LAST server write completes. No unsaved changes -> onDone runs immediately.
  private persistChangesThen(onDone: () => void): void {
    if (!this.isChanged()) {
      onDone();
      return;
    }
    if (this.updateDisabled) {
      this.snackBar.open(this.cannotSaveMessage(), '', {duration: 5000});
      return;
    }
    this.saveChanges(() => onDone());
  }

  /** Why what the user has changed cannot be saved now. */
  private cannotSaveMessage(): string {
    switch (this.changeChecker.status()) {
      case 'invalid':
        return this.changeChecker.violations()[0]?.message || 'The changes are not valid.';
      case 'failed':
        return 'The changes could not be checked.';
      default:
        return 'The changes are being checked. Try again in a moment.';
    }
  }

  // Issue #1347: POST the document-level bulk error-response apply, then reload the grid so it reflects
  // the server-applied state across all operations.
  private sendBulkErrorResponse(payload: {
    errorResponseBodyType: string;
    confirmMessageTopLevelAsbiepId?: number;
    releaseId?: number;
  }): void {
    this.loading = true;
    this.openAPIService.applyErrorResponseBodyTypeToAll(this.oasDoc.oasDocId, payload)
      .pipe(finalize(() => this.loading = false))
      .subscribe(() => {
        this.snackBar.open('Applied "' + this.bulkBodyTypeLabel(payload.errorResponseBodyType) + '" to all operations.',
          '', {duration: 3000});
        this.loadBieListForOasDoc();
      });
  }

  private securityRequirementSummary(requirements: OasSecurityRequirement[], inherit: boolean): string {
    if (inherit) {
      return 'Use Root';
    }
    if (!requirements || requirements.length === 0) {
      return 'No Security';
    }
    return requirements.map(req => {
      if (req.anonymous) {
        return 'anonymous';
      }
      return (req.schemes || [])
        .filter(scheme => !!scheme.schemeName)
        .map(scheme => {
          const scopes = scheme.scopes && scheme.scopes.length > 0 ? ` (${scheme.scopes.join(', ')})` : '';
          return `${scheme.schemeName}${scopes}`;
        })
        .join(' + ');
    })
      .filter(text => !!text)
      .join(' OR ');
  }

  private pruneSecurityRequirements(requirements: OasSecurityRequirement[], schemeName: string): OasSecurityRequirement[] {
    return (requirements || [])
      .map(req => ({
        anonymous: req.anonymous,
        schemes: (req.schemes || []).filter(s => s.schemeName !== schemeName)
      }))
      .filter(req => req.anonymous || req.schemes.length > 0);
  }

  // A renamed scheme keeps its oas_security_scheme_id, so carry the new name into the dependent
  // requirements (which reference schemes by name) instead of dropping them.
  private renameSecurityRequirements(requirements: OasSecurityRequirement[], oldName: string, newName: string): OasSecurityRequirement[] {
    return (requirements || []).map(req => ({
      anonymous: req.anonymous,
      schemes: (req.schemes || []).map(s => s.schemeName === oldName ? {...s, schemeName: newName} : s)
    }));
  }

  getPath(commands?: any[]): string {
    const urlTree = this.router.createUrlTree(commands);
    const path = this.location.prepareExternalUrl(urlTree.toString());
    return window.location.origin + path;
  }

  loadBieListForOasDoc() {
    this.edits.clear(); // read again from the server: what was saved is there, and a row that is gone is not edited
    this.view().reload();
    this.refreshOperationIds();
    this.refreshHttpHeaders();
  }

  // Issue #1791: the server registers request parameters when the operations change (sinceLastDateTime of a GET that
  // returns an array, the path parameters of a Resource Path), in the same step that changes the operation. The page
  // takes them, or its next Update would save the headers it had before and take them away.
  private savedHttpHeadersHash: any;

  private refreshHttpHeaders() {
    this.openAPIService.getOasDoc(this.oasDoc.oasDocId).subscribe(reloaded => {
      if (!reloaded) {
        return;
      }
      const pageHasChanges = hashCode(this.oasDoc.httpHeaders) !== this.savedHttpHeadersHash;
      const docWasUnchanged = this.hashCodeForOasDoc === hashCode(this.oasDoc);
      adoptServerHeaders(this.oasDoc, reloaded.httpHeaders || [], pageHasChanges);
      linkHeadersToRows(this.oasDoc, this.rows);
      if (!pageHasChanges) {
        this.savedHttpHeadersHash = hashCode(this.oasDoc.httpHeaders);
        if (docWasUnchanged) {
          this.hashCodeForOasDoc = hashCode(this.oasDoc);
        }
      }
    });
  }

  getChanged(): BieForOasDoc[] {
    return this.edits.edited(this.rows); // of every page
  }

  isChanged(): boolean {
    if (!this.oasDoc) {
      return false;
    }
    return this.hashCodeForOasDoc !== hashCode(this.oasDoc) || this.getChanged().length > 0;
  }

  isDisabled(oasDoc1: OasDoc) {
    return (this.disabled) ||
      (oasDoc1.oasDocId === undefined || !oasDoc1.oasDocId) ||
      (oasDoc1.title === undefined || oasDoc1.title === '') ||
      (oasDoc1.openAPIVersion === undefined || oasDoc1.openAPIVersion === '') ||
      (oasDoc1.version === undefined || oasDoc1.version === '') ||
      this.termServiceUrl.invalid || this.contactUrl.invalid ||
      this.contactEmail.invalid || this.licenseUrl.invalid ||
      // Issue #1729: block update/generate while any security scheme is incomplete.
      !this.areSecuritySchemesValid(oasDoc1) ||
      !this.areSecurityRequirementsValid(oasDoc1);
  }
  back() {
    this.location.back();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);
    }
    if (property === 'verb') {
      // A request body is never valid for GET, so revert it to Response. A DELETE request body is kept
      // in any version (Issue #1610): it is honored in OpenAPI 3.1 and dropped (with a banner) in 3.0.
      if (source.messageBody === 'Request' && source.verb === 'GET') {
        source.messageBody = 'Response';
      }
      this.updateOperationIdForVerb(source);
    }
  }

  // Issue #1610: true when the document targets OpenAPI 3.1.x (where a DELETE request body is honored).
  isOpenApi31(): boolean {
    return !!this.oasDoc && !!this.oasDoc.openAPIVersion && this.oasDoc.openAPIVersion.trim().startsWith('3.1');
  }

  // Issue #1610: a DELETE request body is honored only from OpenAPI 3.1. When the document targets an
  // earlier version (3.0), the body is dropped from the generated document, so a banner above the
  // Endpoint Details table prompts the user to switch to 3.1. (The list is server-paginated, so this
  // reflects the operations currently loaded on this page.)
  hasIgnoredDeleteRequestBody(): boolean {
    if (this.isOpenApi31()) {
      return false;
    }
    return this.rows.some(
      element => element.verb === 'DELETE' && element.messageBody === 'Request');
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

    // Handle 'Ctrl/Command+S'
    const metaOrCtrlKeyPressed = $event.metaKey || $event.ctrlKey;
    if (metaOrCtrlKeyPressed && charCode === 's') {
      $event.preventDefault();
      $event.stopPropagation();

      // Same gate as the Update button: nothing to save, or the document is not valid to save.
      if (!this.isChanged() || this.isDisabled(this.oasDoc)) {
        return;
      }
      this.update();
    }
  }

  update() {
    this.checkUniqueness(this.oasDoc, (_) => {
      this.doUpdate();
    });
  }

  get access(): string {
    if (this.oasDoc) {
      return this.oasDoc.access;
    }
    return '';
  }

  checkUniqueness(oasDoc: OasDoc, callbackFn?) {
    this.openAPIService.checkUniqueness(oasDoc).subscribe(resp => {
      if (!resp) {
        this.openDialogOasDocUpdate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  checkOasDocTitle(oasDoc: OasDoc, callbackFn?) {
    this.openAPIService.checkTitleUniqueness(oasDoc).subscribe(resp => {
      if (!resp) {
        this.openDialogOasDocUpdateIgnore();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  doUpdate() {
    if (this.updateDisabled) {
      return;
    }
    this.saveChanges(changeSet => {
      this.snackBar.open('Updated', '', {duration: 3000});
      if (changeSet.operations.length > 0) {
        this.openAPIService.checkBIEReusedAcrossOperationsAfterUpdate(this.oasDoc).subscribe(resp => {
          if (resp.errorMessages && resp.errorMessages.length > 0) {
            this.snackBar.open(resp.errorMessages[0], '', {duration: 5000});
          }
        }, _ => {
        });
      }
    });
  }

  /**
   * Saves what the user has changed, all of it or nothing, once the server has said that it can be saved; the server says it
   * again here, as what it said may be out of date. The page is then as saved: the Security Schemes with their ids, and the
   * entries with those registered for the operations.
   */
  private saveChanges(onSaved: (changeSet: OasDocChangeSet) => void): void {
    const changeSet = this.buildChangeSet();
    if (!changeSet) {
      return;
    }
    this.loading = true;
    this.isUpdating = true;
    this.openAPIService.saveChanges(this.oasDoc.oasDocId, changeSet).pipe(finalize(() => {
      this.isUpdating = false;
      this.loading = false;
    })).subscribe({
      next: () => this.reloadAfterSave(() => onSaved(changeSet)),
      error: err => this.showRefusal(err)
    });
  }

  private reloadAfterSave(then: () => void): void {
    this.openAPIService.getOasDoc(this.oasDoc.oasDocId).subscribe(reloaded => {
      if (reloaded) {
        this.oasDoc.securitySchemes = reloaded.securitySchemes || [];
        this.oasDoc.securityRequirements = reloaded.securityRequirements || [];
        this.oasDoc.httpHeaders = reloaded.httpHeaders || [];
      }
      this.init(this.oasDoc);
      this.loadBieListForOasDoc();
      then();
    });
  }

  openDialogOasDocUpdate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another OpenAPI Doc with the same title, OpenAPI Version and Doc Version already exists!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {
    });
  }

  openDialogOasDocUpdateIgnore() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'The OpenAPI Doc already has a variable with the same properties';
    dialogConfig.data.content = [
      'Are you sure you want to update the OpenAPI Doc?'
    ];
    dialogConfig.data.action = 'Update anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.doUpdate();
        }
      });
  }

  discard() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard OpenAPI Doc?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this OpenAPI Doc?',
      'The OpenAPI Doc will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.openAPIService.delete(this.oasDoc.oasDocId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.router.navigateByUrl('/profile_bie/express/oas_doc');
          }, err => {
            this.snackBar.open('Discard\'s forbidden! The OpenAPI Doc is used.', '', {
              duration: 5000,
            });
          });
        }
      });
  }

  openDialog($event: any, bieForOasDoc?: BieForOasDoc) {
    $event.preventDefault();
    $event.stopPropagation();

    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = this.rows;
    dialogConfig.data.webPageInfo = this.webPageInfo;
    dialogConfig.data.oasDoc = this.oasDoc;
    // Default indicator values
    dialogConfig.data.isEditable = this.isEditable();
    dialogConfig.width = '100%';
    dialogConfig.maxWidth = '100%';
    dialogConfig.height = '100%';
    dialogConfig.maxHeight = '100%';
    dialogConfig.autoFocus = false;

    const isAddAction: boolean = (bieForOasDoc === undefined);

    this.isUpdating = true;
    const dialogRef = this.dialog.open(OasDocAssignDialogComponent, dialogConfig);
    dialogRef.afterClosed().pipe(finalize(() => {
      this.isUpdating = false;
    })).subscribe(result => {
      if (!result) {
        return;
      }

      this.loadBieListForOasDoc();
    });
  }

  // Issue #1730: open the dialog to add a BIE-less operation (endpoint).
  openAddOperationDialog($event: any) {
    $event.preventDefault();
    $event.stopPropagation();

    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {oasDoc: this.oasDoc};
    dialogConfig.autoFocus = false;
    dialogConfig.width = '600px';

    this.isUpdating = true;
    const dialogRef = this.dialog.open(OasDocAddOperationDialogComponent, dialogConfig);
    dialogRef.afterClosed().pipe(finalize(() => {
      this.isUpdating = false;
    })).subscribe(result => {
      if (!result) {
        return;
      }

      this.loadBieListForOasDoc();
    });
  }

  // Issue #1732: keep the Operation ID's verb word in sync when the Verb changes. Rebuilds from the
  // BIE's property term (the immutable name source), so a name that itself ends in "List" is not
  // mistaken for the array marker; for a bodyless operation (no property term, issue #1730) it falls
  // back to swapping only the leading verb word of the manually entered id. The frontend owns the
  // operationId; the backend stores it verbatim on save.
  updateOperationIdForVerb(element: BieForOasDoc): void {
    element.operationId = recomputeOperationId(
      element.verb, element.operationId, element.arrayIndicator, element.propertyTerm);
  }

  // Issue #1732: when the Array Indicator toggles, rebuild the operationId's 'List' marker and the
  // resource path's '-list' marker from the property term + the new indicator (the array checkbox is
  // BIE-only, so a property term is always present here). Driving the marker from the boolean — not by
  // inspecting the string — keeps the marker correct even for a term ending in "List" (e.g.
  // "Price List" -> queryPriceListList / .../price-list-list). The frontend owns these; the backend
  // stores them as-is.
  updateOperationIdForArray(element: BieForOasDoc): void {
    element.operationId = buildOperationId(element.verb, element.propertyTerm, element.arrayIndicator);
    element.resourceName = applyResourceArrayMarker(
      element.resourceName, element.propertyTerm, element.arrayIndicator);
  }

  isEditable(): boolean {
    return this.access === 'CanEdit';
  }

  isDirty(): boolean {
    return !!this.oasDoc.oasDocId
      || this.oasDoc.title && this.oasDoc.title.length > 0
      || this.oasDoc.description && this.oasDoc.description.length > 0;
  }

  isWorkingRelease(): boolean {
    if (this.oasDoc) {
      return this.oasDoc.workingRelease;
    }
    return false;
  }

  /** The sort of the table, which the generated document follows. */
  private sortRequest(): PageRequest {
    const sorts = this.view().query.sorts;
    return new PageRequest(sorts.map(e => e.active), sorts.map(e => e.direction));
  }

  generate() {
    // Issue #1610: the document is generated from the persisted record, so any pending edit (e.g. an
    // OpenAPI Version change from 3.0 to 3.1, or a new DELETE Request body) would NOT be reflected.
    // Block generation while there are unsaved changes and prompt the user to Update first.
    if (this.isChanged()) {
      this.snackBar.open('There are unsaved changes. Please click Update before generating the document.', '', {
        duration: 5000,
      });
      return;
    }
    this.loading = true;
    this.openAPIService.generateOpenAPI(this.oasDoc.oasDocId, this.sortRequest()).subscribe(resp => {
      saveAsBlobResponse(resp);

      this.loading = false;
    }, err => {
      this.loading = false;
      throw err;
    });
  }

  removeBieForOasDoc() {
    const request = new BieForOasDocDeleteRequest();
    const nodes = this.selection.selected;
    request.oasDocId = this.oasDoc.oasDocId;
    request.bieForOasDocList = nodes;

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove selected BIE from the OpenAPI Doc?';
    dialogConfig.data.content = ['Are you sure you want to remove the selected BIE?'];
    dialogConfig.data.action = 'Remove';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.openAPIService.removeBieForOasDoc(request).subscribe(_ => {
            this.snackBar.open('Removed', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.edits.clear();
            this.view().reload();
            this.refreshHttpHeaders();
          });
        }
      });
  }
  get sizeOfChanges(): number {
    return this.getChanged().length;
  }

  /** Update is there when something is changed and the server has said that it can be saved. */
  get updateDisabled(): boolean {
    return !this.isChanged() || this.changeChecker.status() !== 'valid';
  }

  /** The server refused to save: what it says is wrong is shown where it is, and the first of it is said. */
  private showRefusal(err: any) {
    const result = err?.error as OasDocChangeResult | undefined;
    if (result?.violations?.length) {
      this.changeChecker.refuse(result.violations);
    }
    this.snackBar.open(result?.violations?.[0]?.message || err?.headers?.get('x-error-message') || 'Failed to update.', '', {
      duration: 5000,
    });
  }
}
