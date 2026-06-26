import { Component, OnInit, QueryList, ViewChild, ViewChildren, inject } from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {FormControl} from '@angular/forms';
import {EMPTY, forkJoin, from, of, ReplaySubject} from 'rxjs';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatDatepicker} from '@angular/material/datepicker';
import {catchError, concatMap, finalize} from 'rxjs/operators';
import {AssignBieForOasDoc, BieForOasDoc, BieForOasDocListRequest, buildOperationId, OasDoc} from '../domain/openapi-doc';
import {OpenAPIService} from '../domain/openapi.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ReleaseSummary} from '../../../release-management/domain/release';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {AuthService} from '../../../authentication/auth.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {PageRequest} from '../../../basis/basis';
import {initFilter, loadBranch, loadLibrary, saveBranch, saveLibrary} from '../../../common/utility';
import {PreferencesInfo, TableColumnsProperty} from '../../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../../settings-management/settings-preferences/domain/settings-preferences.service';
import {ScoreTableColumnResizeDirective} from '../../../common/score-table-column-resize/score-table-column-resize.directive';
import {SearchBarComponent} from '../../../common/search-bar/search-bar.component';
import {LibrarySummary} from '../../../library-management/domain/library';
import {LibraryService} from '../../../library-management/domain/library.service';

@Component({
  standalone: false,
  selector: 'score-oas-doc-assign-dialog',
  templateUrl: './oas-doc-assign-dialog.component.html',
  styleUrls: ['./oas-doc-assign-dialog.component.css']
})
export class OasDocAssignDialogComponent implements OnInit {
  private openAPIService = inject(OpenAPIService);
  private accountService = inject(AccountListService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private auth = inject(AuthService);
  private dialog = inject(MatDialog);
  private location = inject(Location);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  webPageInfo = inject(WebPageInfoService);
  private preferencesService = inject(SettingsPreferencesService);
  dialogRef = inject<MatDialogRef<OasDocAssignDialogComponent>>(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);


  title = 'Add BIE For OpenAPI Document';

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfBiePage;
  }

  updateTableColumnsForBiePage() {
    this.preferencesService.updateTableColumnsForBiePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onResizeWidth($event) {
    switch ($event.name) {
      case 'Updated on':
        this.setWidth('Updated On', $event.width);
        break;

      default:
        this.setWidth($event.name, $event.width);
        break;
    }
  }

  setWidth(name: string, width: number | string) {
    const matched = this.columns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.updateTableColumnsForBiePage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    switch (name) {
      case 'Verb':
        return 160;
      case 'Message Body':
        return 160;
      case 'Array Indicator':
        return 70;
      default:
        return this.columns.find(c => c.name === name)?.width;
    }
  }

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (this.preferencesInfo) {
      for (const column of this.columns) {
        switch (column.name) {
          case 'State':
            if (column.selected) {
              displayedColumns.push('state');
            }
            break;
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
          case 'Owner':
            if (column.selected) {
              displayedColumns.push('owner');
            }
            break;
          case 'Business Contexts':
            if (column.selected) {
              displayedColumns.push('businessContexts');
            }
            break;
          case 'Version':
            if (column.selected) {
              displayedColumns.push('version');
            }
            break;
          case 'Status':
            if (column.selected) {
              displayedColumns.push('status');
            }
            break;
          case 'Remark':
            if (column.selected) {
              displayedColumns.push('remark');
            }
            break;
          case 'Business Term':
            if (column.selected) {
              displayedColumns.push('bizTerm');
            }
            break;
        }
      }
    }
    displayedColumns.push('verb');
    displayedColumns.push('messageBody');
    displayedColumns.push('arrayIndicator');
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<BieForOasDoc>();
  selection = new SelectionModel<BieForOasDoc>(true, []);
  businessContextSelection = {};
  verbSelection = {};
  messageBodySelection = {};
  loading = false;
  oasDoc: OasDoc;
  loginIdList: string[] = [];
  releases: ReleaseSummary[] = [];
  libraries: LibrarySummary[] = [];
  mappedLibraries: {library: LibrarySummary, selected: boolean}[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<ReleaseSummary[]> = new ReplaySubject<ReleaseSummary[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BieForOasDocListRequest;
  assignBieForOasDoc: AssignBieForOasDoc;
  assignBieForOasDocList: AssignBieForOasDoc[];
  preferencesInfo: PreferencesInfo;
  // Issue #1492 (Option 2): the document's already-added rows, for the duplicate-body pre-check.
  existingRows: BieForOasDoc[] = [];

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;
  @ViewChild(SearchBarComponent, {static: true}) searchBar: SearchBarComponent;

  ngOnInit(): void {
    this.oasDoc = this.data.oasDoc;
    // Issue #1492 (Option 2): the live existing rows of the document are passed as the dialog data
    // (an array, with oasDoc/webPageInfo/isEditable attached as extra properties). Capture them so a
    // fast client pre-check can block adding a body that already exists on a (resourceName, verb).
    this.existingRows = Array.isArray(this.data) ? (this.data as BieForOasDoc[]).slice() : [];
    this.assignBieForOasDoc = new AssignBieForOasDoc();
    this.assignBieForOasDocList = [];
    // Init BIE list table for OasDoc
    this.request = new BieForOasDocListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.access = 'CanView';

    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.initLibraries(libraries);

      this.paginator.pageIndex = this.request.page.pageIndex;
      this.paginator.pageSize = this.request.page.pageSize;
      this.paginator.length = 0;

      this.sort.active = this.request.page.sortActive;
      this.sort.direction = this.request.page.sortDirection as SortDirection;
      // Prevent the sorting event from being triggered if any columns are currently resizing.
      const originalSort = this.sort.sort;
      this.sort.sort = (sortChange) => {
        if (this.tableColumnResizeDirectives &&
          this.tableColumnResizeDirectives.filter(e => e.resizing).length > 0) {
          return;
        }
        originalSort.apply(this.sort, [sortChange]);
      };
      this.sort.sortChange.subscribe(() => {
        this.onSearch();
      });

      forkJoin([
        this.accountService.getAccountNames(),
        this.releaseService.getReleaseSummaryList(this.request.library.libraryId, ['Published']),
        this.preferencesService.load(this.auth.getUserToken())
      ]).subscribe(([loginIds, releases, preferencesInfo]) => {
        this.preferencesInfo = preferencesInfo;

        this.loginIdList.push(...loginIds);
        initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
        initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

        this.initReleases(releases);

        this.selectBieForOasDocList(true);
      });
    });
  }

  initLibraries(libraries: LibrarySummary[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.request.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
      }
      if (!this.request.library || !this.request.library.libraryId) {
        this.request.library = this.libraries.find(e => e.isDefault) || this.libraries[0];
      }
      if (this.request.library) {
        saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      }
      this.mappedLibraries = this.libraries.map(e => {
        return {library: e, selected: (this.request.library.libraryId === e.libraryId)};
      });
    }
  }

  initReleases(releases: ReleaseSummary[]) {
    this.releases = releases.filter(e => !e.workingRelease);
    initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
    const savedReleaseId = loadBranch(this.auth.getUserToken(), 'BIE');
    if (savedReleaseId) {
      this.request.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
      if (!this.request.release) {
        this.request.release = this.releases[0];
        saveBranch(this.auth.getUserToken(), 'BIE', this.request.release.releaseId);
      }
    } else {
      this.request.release = this.releases[0];
    }
  }

  onLibraryChange(library: LibrarySummary) {
    this.request.library = library;
    this.request.release.releaseId = 0;
    this.releaseService.getReleaseSummaryList(this.request.library.libraryId, ['Published']).subscribe(releases => {
      saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      this.initReleases(releases);
      this.onSearch();
    });
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.selection.clear();
    this.selectBieForOasDocList();
  }

  selectBieForOasDocList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.openAPIService.selectBieForOasDocListWithRequest(this.request, this.oasDoc).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: BieForOasDoc) => {
        elm = new BieForOasDoc(elm);
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });
      this.dataSource.data.forEach((elm: BieForOasDoc) => {
        this.businessContextSelection[elm.topLevelAsbiepId] = elm.businessContext;
        this.verbSelection[elm.topLevelAsbiepId] = elm.verb;
        this.messageBodySelection[elm.topLevelAsbiepId] = elm.messageBody;
      });
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0],
          this.request.toQuery() + '&adv_ser=' + (this.searchBar.showAdvancedSearch));
      }
    }, error => {
      this.dataSource.data = [];
      this.businessContextSelection = {};
      this.verbSelection = {};
      this.messageBodySelection = {};
    });
  }

  onPageChange(event: PageEvent) {
    this.selectBieForOasDocList();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);
    }
    if (property === 'verbSelection') {
      // A request body is never valid for GET, so revert it to Response. A DELETE request body is kept
      // in any version (Issue #1610): honored in OpenAPI 3.1, dropped (with a banner) in 3.0.3.
      if (this.messageBodySelection[source] === 'Request' && this.verbSelection[source] === 'GET') {
        this.messageBodySelection[source] = 'Response';
      }
    }
  }

  reset(type: string) {
    switch (type) {
      case 'startDate':
        this.dateStart.select(undefined);
        this.request.updatedDate.start = null;
        break;
      case 'endDate':
        this.dateStart.select(undefined);
        this.request.updatedDate.end = null;
        break;
    }
  }

  select(row: BieForOasDoc) {
    this.selection.select(row);
  }

  toggle(row: BieForOasDoc) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
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
