import { Component, HostListener, OnInit, QueryList, ViewChild, ViewChildren, inject } from '@angular/core';
import {
  BieForOasDoc,
  BieForOasDocDeleteRequest,
  BieForOasDocListRequest,
  BieForOasDocUpdateRequest,
  OasDoc,
  OasSecurityRequirement,
  OasSecurityScheme,
  recomputeOperationId,
  SimpleOasDoc
} from '../domain/openapi-doc';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {OpenAPIService} from '../domain/openapi.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ErrorStateMatcher} from '@angular/material/core';
import {UntypedFormControl} from '@angular/forms';
import {forkJoin} from 'rxjs';
import {hashCode, saveAsBlobResponse, saveBranch} from 'src/app/common/utility';
import {SelectionModel} from '@angular/cdk/collections';
import {finalize} from 'rxjs/operators';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {OasDocAssignDialogComponent} from '../oas-doc-assign-dialog/oas-doc-assign-dialog.component';
import {OasDocAddOperationDialogComponent} from '../oas-doc-add-operation-dialog/oas-doc-add-operation-dialog.component';
import {OasDocSecuritySchemeDialogComponent} from '../oas-doc-security-scheme-dialog/oas-doc-security-scheme-dialog.component';
import {OasDocSecurityRequirementDialogComponent} from '../oas-doc-security-requirement-dialog/oas-doc-security-requirement-dialog.component';
import {MatMultiSort, MatMultiSortTableDataSource, TableData} from 'ngx-mat-multi-sort';
import {BusinessContext} from '../../../context-management/business-context/domain/business-context';
import {BieExpressOption} from '../../bie-express/domain/generate-expression';
import {BusinessContextService} from '../../../context-management/business-context/domain/business-context.service';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {PageRequest} from '../../../basis/basis';
import {
  PreferencesInfo,
  TableColumnsInfo,
  TableColumnsProperty
} from '../../../settings-management/settings-preferences/domain/preferences';
import {ScoreTableColumnResizeDirective} from '../../../common/score-table-column-resize/score-table-column-resize.directive';
import {SettingsPreferencesService} from '../../../settings-management/settings-preferences/domain/settings-preferences.service';
import {Title} from '@angular/platform-browser';
import {setAppTitleIfPresent} from '../../../common/app-title.strategy';

@Component({
  standalone: false,
  selector: 'score-oas-doc-detail',
  templateUrl: './oas-doc-detail.component.html',
  styleUrls: ['./oas-doc-detail.component.css']
})
export class OasDocDetailComponent implements OnInit {
  private bizCtxService = inject(BusinessContextService);
  private openAPIService = inject(OpenAPIService);
  private accountService = inject(AccountListService);
  private auth = inject(AuthService);
  private location = inject(Location);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  private confirmDialogService = inject(ConfirmDialogService);
  private preferencesService = inject(SettingsPreferencesService);
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

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfBieForOasDocPage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfBieForOasDocPage = columns;
    this.updateTableColumnsForBieForOasDocPage();
  }

  updateTableColumnsForBieForOasDocPage() {
    this.preferencesService.updateTableColumnsForBieForOasDocPage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  ensureSecurityColumn() {
    const columns = this.preferencesInfo.tableColumnsInfo.columnsOfBieForOasDocPage;
    // Position the Security column right after Message Body (preserve a saved entry's width/selected).
    let entry = columns.find(c => c.name === 'Security');
    if (entry) {
      columns.splice(columns.indexOf(entry), 1);
    } else {
      entry = {name: 'Security', selected: true, width: 120};
    }
    const idx = columns.findIndex(c => c.name === 'Message Body');
    if (idx >= 0) {
      columns.splice(idx + 1, 0, entry);
    } else {
      columns.push(entry);
    }
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfBieForOasDocPage;
    this.onColumnsChange(this.columns);
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    const updatedColumnsWithWidth = updatedColumns.map(column => ({
      name: column.name,
      selected: column.selected,
      width: this.width(column.name)
    }));

    this.columns = updatedColumnsWithWidth;

    let columns = [];
    for (const tableColumn of this.table.columns) {
      for (const updatedColumn of updatedColumns) {
        if (tableColumn.name === updatedColumn.name) {
          tableColumn.isActive = updatedColumn.selected;
        }
      }
      columns.push(tableColumn);
    }

    this.table.columns = columns;
    this.table.displayedColumns = this.displayedColumns;
  }

  onResizeWidth($event) {
    switch ($event.name) {
      default:
        this.setWidth($event.name, $event.width);
        break;
    }
  }

  setWidth(name: string, width: number | string) {
    const matched = this.columns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.updateTableColumnsForBieForOasDocPage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  defaultDisplayedColumns = [
    {id: 'select', name: '', isActive: true},
    {id: 'branch', name: 'Branch', isActive: true},
    {id: 'den', name: 'DEN', isActive: true},
    {id: 'remark', name: 'Remark', isActive: true},
    {id: 'verb', name: 'Verb', isActive: true},
    {id: 'arrayIndicator', name: 'Array Indicator', isActive: true},
    {id: 'suppressRootIndicator', name: 'Suppress Root Indicator', isActive: true},
    {id: 'messageBody', name: 'Message Body', isActive: true},
    {id: 'security', name: 'Security', isActive: true},
    {id: 'resourceName', name: 'Resource Name', isActive: true},
    {id: 'operationId', name: 'Operation ID', isActive: true},
    {id: 'tagName', name: 'Tag Name', isActive: true}
  ];

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (this.preferencesInfo) {
      for (const column of this.columns) {
        switch (column.name) {
          case 'Branch':
            if (column.selected) {
              displayedColumns.push('branch');
            }
            break;
          case 'DEN':
            if (column.selected) {
              displayedColumns.push('den');
            }
            break;
          case 'Remark':
            if (column.selected) {
              displayedColumns.push('remark');
            }
            break;
          case 'Verb':
            if (column.selected) {
              displayedColumns.push('verb');
            }
            break;
          case 'Array Indicator':
            if (column.selected) {
              displayedColumns.push('arrayIndicator');
            }
            break;
          case 'Suppress Root Indicator':
            if (column.selected) {
              displayedColumns.push('suppressRootIndicator');
            }
            break;
          case 'Message Body':
            if (column.selected) {
              displayedColumns.push('messageBody');
            }
            break;
          case 'Resource Name':
            if (column.selected) {
              displayedColumns.push('resourceName');
            }
            break;
          case 'Operation ID':
            if (column.selected) {
              displayedColumns.push('operationId');
            }
            break;
          case 'Tag Name':
            if (column.selected) {
              displayedColumns.push('tagName');
            }
            break;
          case 'Security':
            if (column.selected) {
              displayedColumns.push('security');
            }
            break;
        }
      }
    }
    return displayedColumns;
  }

  table: TableData<BieForOasDoc>;
  selection = new SelectionModel<BieForOasDoc>(true, []);
  businessContextSelection = {};
  request: BieForOasDocListRequest;
  preferencesInfo: PreferencesInfo;
  loading = false;
  isUpdating: boolean;
  option: BieExpressOption;
  openApiFormats: string[] = ['YAML', 'JSON'];
  topLevelAsbiepIds: number[];

  // Issue #1732: operationId no longer carries a business-context prefix, so the same BIE+verb
  // under different contexts can produce identical operationIds. OpenAPI requires operationId to be
  // unique within a document, so collisions among the loaded operations are flagged as a mat-error
  // (application-level validation). User-entered values are never auto-corrected or blocked.
  duplicateOperationIds = new Set<string>();
  operationIdErrorStateMatcher: ErrorStateMatcher = {
    isErrorState: (control: UntypedFormControl | null): boolean => {
      const value = (control?.value || '').trim();
      return !value || this.duplicateOperationIds.has(value);
    }
  };

  @ViewChild(MatMultiSort, {static: true}) sort: MatMultiSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;

  ngOnInit(): void {
    this.table = new TableData<BieForOasDoc>(this.defaultDisplayedColumns, {});
    this.table.dataSource = new MatMultiSortTableDataSource<BieForOasDoc>(this.sort, false);

    this.topLevelAsbiepIds = [];
    this.option = new BieExpressOption();
    this.option.bieDefinition = true;
    this.option.packageOption = 'ALL';
    // Default OpenAPI expression format is 'YAML'.
    this.option.openAPIExpressionFormat = 'YAML';
    this.oasDoc = new OasDoc();

    const oasDocId = this.route.snapshot.params.id;

    // Init BIE list table for OasDoc
    this.request = new BieForOasDocListRequest(this.route.snapshot.queryParamMap,
      new PageRequest(['tagName', 'operationId'], ['asc', 'asc'], 0, 10));
    this.request.access = 'CanView';

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.table.sortParams = this.request.page.sortActives;
    this.table.sortDirs = this.request.page.sortDirections;
    // Prevent the sorting event from being triggered if any columns are currently resizing.
    const originalSort = this.sort.sort;
    this.sort.sort = (sortChange) => {
      if (this.tableColumnResizeDirectives &&
        this.tableColumnResizeDirectives.filter(e => e.resizing).length > 0) {
        return;
      }
      originalSort.apply(this.sort, [sortChange]);
    };
    this.table.sortObservable.subscribe(() => {
      this.onSearch();
    });

    this.request.page = new PageRequest(
      this.table.sortParams, this.table.sortDirs,
      this.paginator.pageIndex, this.paginator.pageSize);

    forkJoin([
      this.openAPIService.getOasDoc(oasDocId),
      this.openAPIService.getBieListForOasDoc(this.request, oasDocId),
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([simpleOasDoc, bieForOasDoc, preferencesInfo]) => {
      if (!simpleOasDoc) {
        this.redirectToOasDocList();
        return;
      }

      this.preferencesInfo = preferencesInfo;
      this.ensureSecurityColumn();
      this.onColumnsChange(this.preferencesInfo.tableColumnsInfo.columnsOfBieForOasDocPage);

      this.oasDoc = simpleOasDoc;
      this.init(this.oasDoc);
      this.loadBieListForOasDoc(true);
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
    this.hashCodeForOasDoc = hashCode(oasDoc);
    this.oasDoc = oasDoc;
    setAppTitleIfPresent(this.titleService, this.oasDoc.title, 'OpenAPI Document');
    this.isUpdating = false;
  }

  // Issue #1729: selectable security scheme types (OpenAPI 3.0.3).
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
      (this.table?.dataSource?.data || []).forEach(row => {
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
            (this.table?.dataSource?.data || []).forEach(row => {
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
        const siblings = (this.table.dataSource.data || []).filter(r =>
          !!r.oasOperationId && r.oasOperationId === row.oasOperationId);
        const targets = siblings.length > 0 ? siblings : [row];
        targets.forEach(r => {
          r.securityOverridden = result.securityOverridden;
          r.securityRequirements = JSON.parse(JSON.stringify(result.securityRequirements || []));
        });
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

  onSearch() {
    this.paginator.pageIndex = 0;
    this.selection.clear();
    this.loadBieListForOasDoc();
  }

  loadBieListForOasDoc(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.table.sortParams, this.table.sortDirs,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.openAPIService.getBieForOasDocListWithRequest(this.request, this.oasDoc).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.table.dataSource.data = resp.list.map((elm: BieForOasDoc) => {
        elm = new BieForOasDoc(elm);
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        elm.reset(); // reset the hashCode calculation when the bieForOasDoc is list and reloaded
        return elm;
      });
      this.table.dataSource.data.forEach((elm: BieForOasDoc) => {
        this.businessContextSelection[elm.topLevelAsbiepId] = elm.businessContext;
      });
      this.recomputeDuplicateOperationIds();

      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.table.dataSource.data = [];
      this.businessContextSelection = {};
    });
  }

  getChanged(): BieForOasDoc[] {
    if (!this.table.dataSource) {
      return [];
    }
    return this.table.dataSource.data.filter(e => e.isChanged);
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
      // Issue #1729: block update/generate while any security scheme is incomplete.
      !this.areSecuritySchemesValid(oasDoc1) ||
      !this.areSecurityRequirementsValid(oasDoc1);
  }
  back() {
    this.location.back();
  }

  onPageChange(event: PageEvent) {
    this.loadBieListForOasDoc();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);
    }
    if (property === 'filters.den') {
      this.sort.active = '';
      this.sort.direction = '';
    }
    if (property === 'verb') {
      if (source.verb === 'GET' && source.messageBody === 'Request') {
        source.messageBody = 'Response';
      }
      this.updateOperationIdForVerb(source);
    }
  }

  reset(type: string) {
    switch (type) {
      case 'startDate':
        this.request.updatedDate.start = null;
        break;
      case 'endDate':
        this.request.updatedDate.end = null;
        break;
    }
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

    // Handle 'Ctrl/Command+S'
    const metaOrCtrlKeyPressed = $event.metaKey || $event.ctrlKey;
    if (metaOrCtrlKeyPressed && charCode === 's') {
      $event.preventDefault();
      $event.stopPropagation();

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
    // See #isChanged for the conditions in this method.

    // Issue #1732: never persist a blank operationId (it is required and NOT NULL in the DB).
    const blankOperationId = this.getChanged().some(e => !e.operationId || !e.operationId.trim());
    if (blankOperationId) {
      this.snackBar.open('Operation ID is required.', '', {duration: 3000});
      return;
    }

    const docChanged = this.hashCodeForOasDoc !== hashCode(this.oasDoc);
    const detailsChanged = this.getChanged().length > 0;

    if (docChanged) {
      // Persist the document (incl. its security schemes) FIRST, then the per-operation details.
      // updateDetails() resolves each operation Security Requirement's scheme name to its
      // oas_security_scheme_id from the database, so a renamed/added scheme must be committed before the
      // operation rows are saved — otherwise the lookup misses and the requirement is silently dropped.
      this.openAPIService.updateOasDoc(this.oasDoc).subscribe(_ => {
        // Re-fetch the persisted document so its Security Schemes carry their freshly-assigned
        // oas_security_scheme_id. A scheme added/edited in this session has no id yet; without it the
        // NEXT updateOasDoc would send schemes with no id, and the backend (matching kept schemes by id)
        // would treat every scheme as removed, delete-and-reinsert them, and wipe every operation's
        // security override. Reloading the ids here keeps subsequent updates a stable in-place diff.
        this.openAPIService.getOasDoc(this.oasDoc.oasDocId).subscribe(reloaded => {
          if (reloaded) {
            this.oasDoc.securitySchemes = reloaded.securitySchemes || [];
            this.oasDoc.securityRequirements = reloaded.securityRequirements || [];
          }
          this.init(this.oasDoc);
          if (detailsChanged) {
            // Pass a no-op callback so updateDetails() stays silent (it still persists the rows); this
            // method shows the single 'Updated' snackBar below. Otherwise both fire and it shows twice.
            this.updateDetails(() => {});
          }
          this.snackBar.open('Updated', '', {
            duration: 3000,
          });
        });
      });
    } else if (detailsChanged) {
      // No scheme change -> existing schemes are already persisted, so resolution is safe.
      this.updateDetails();
    }
  }

  openDialogOasDocUpdate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another OpenAPI Doc with the same title, OpenAPI Version, Doc Version and License Name already exists!'
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

    dialogConfig.data = this.table.dataSource.data;
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

  _updateDataSource(data: BieForOasDoc[]) {
    this.table.dataSource.data = data;
    this.oasDoc.bieList = data;
    this.recomputeDuplicateOperationIds();
  }

  // Recomputes the set of operationIds that occur more than once among the loaded operations.
  // Note: the list is server-paginated, so this validates within the currently loaded rows.
  recomputeDuplicateOperationIds(): void {
    const counts = new Map<string, number>();
    for (const row of (this.table?.dataSource?.data || [])) {
      const id = (row.operationId || '').trim();
      if (id) {
        counts.set(id, (counts.get(id) || 0) + 1);
      }
    }
    this.duplicateOperationIds = new Set<string>(
      Array.from(counts.entries()).filter(([, count]) => count > 1).map(([id]) => id));
  }

  // Issue #1732: keep the Operation ID's verb word in sync when the Verb changes. Swaps only the
  // leading verb word, preserving the BIE-name segment (so a manually edited name survives), and
  // re-applies the 'List' suffix from the array indicator. The frontend owns the operationId; the
  // backend stores it verbatim on save.
  updateOperationIdForVerb(element: BieForOasDoc): void {
    element.operationId = recomputeOperationId(element.verb, element.operationId, element.arrayIndicator);
    this.recomputeDuplicateOperationIds();
  }

  // Issue #1732: when the Array Indicator toggles, keep the 'List' suffix on operationId and the
  // '-list' suffix on resourceName in sync. The frontend owns these; the backend stores them as-is.
  // Only the suffix is touched (the verb word / name are preserved).
  updateOperationIdForArray(element: BieForOasDoc): void {
    element.operationId = this.applySuffix(element.operationId, 'List', element.arrayIndicator);
    element.resourceName = this.applySuffix(element.resourceName, '-list', element.arrayIndicator);
    this.recomputeDuplicateOperationIds();
  }

  private applySuffix(value: string, suffix: string, present: boolean): string {
    const current = value || '';
    if (present) {
      return current.endsWith(suffix) ? current : current + suffix;
    }
    return current.endsWith(suffix) ? current.substring(0, current.length - suffix.length) : current;
  }

  select(row: BieForOasDoc) {
    this.selection.select(row);
  }

  isSelected(row: BieForOasDoc) {
    return this.selection.isSelected(row);
  }

  toggle(row: BieForOasDoc) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
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

  generate() {
    this.loading = true;
    this.openAPIService.generateOpenAPI(this.oasDoc.oasDocId, this.request.page).subscribe(resp => {
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
          });
          const newData = [];
          this.table.dataSource.data.forEach(row => {
            if (!this.selection.isSelected(row)) {
              newData.push(row);
            }
          });
          this.selection.clear();
          this._updateDataSource(newData);
        }
      });
  }
  get sizeOfChanges(): number {
    return this.getChanged().length;
  }

  get updateDisabled(): boolean {
    return !this.isChanged();
  }

  updateDetails(callbackFn?) {
    if (this.updateDisabled) {
      if (callbackFn === undefined) {
        return;
      } else {
        return callbackFn && callbackFn();
      }
    }
    const request = new BieForOasDocUpdateRequest();
    const nodes = this.getChanged();
    request.oasDocId = this.oasDoc.oasDocId;
    request.bieForOasDocList = nodes;

    this.loading = true;
    this.isUpdating = true;
    this.openAPIService.updateDetails(request).pipe(finalize(() => {
      this.isUpdating = false;
      this.loading = false;
    })).subscribe(_ => {
      this.loadBieListForOasDoc(true);
      this.openAPIService.checkBIEReusedAcrossOperationsAfterUpdate(this.oasDoc).subscribe(
        resp => {
          if (resp.errorMessages && resp.errorMessages.length > 0) {
            this.snackBar.open(resp.errorMessages[0], '', {
              duration: 5000,
            });
          } else {
          }
        }, _ => {
        });
    });
    if (callbackFn === undefined) {
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    } else {
      return callbackFn && callbackFn();
    }
  }
}
