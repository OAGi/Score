import {Component, HostListener, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {
  BieForOasDoc,
  BieForOasDocDeleteRequest,
  BieForOasDocListRequest,
  BieForOasDocUpdateRequest,
  OasDoc,
  SimpleOasDoc
} from '../domain/openapi-doc';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {OpenAPIService} from '../domain/openapi.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {forkJoin} from 'rxjs';
import {hashCode, saveBranch} from 'src/app/common/utility';
import {saveAs} from 'file-saver';
import {SelectionModel} from '@angular/cdk/collections';
import {finalize} from 'rxjs/operators';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {OasDocAssignDialogComponent} from '../oas-doc-assign-dialog/oas-doc-assign-dialog.component';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {MatMultiSort, MatMultiSortTableDataSource, TableData} from 'ngx-mat-multi-sort';
import {WorkingRelease} from '../../../release-management/domain/release';
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
import {SearchBarComponent} from '../../../common/search-bar/search-bar.component';

@Component({
  selector: 'score-oas-doc-detail',
  templateUrl: './oas-doc-detail.component.html',
  styleUrls: ['./oas-doc-detail.component.css']
})
export class OasDocDetailComponent implements OnInit {

  title = 'Edit OpenAPI Document';
  oasDocs: SimpleOasDoc[];
  oasDoc: OasDoc;
  workingRelease = WorkingRelease;
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

  @ViewChild(MatMultiSort, {static: true}) sort: MatMultiSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;

  constructor(private bizCtxService: BusinessContextService,
              private openAPIService: OpenAPIService,
              private accountService: AccountListService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private dialog: MatDialog,
              public webPageInfo: WebPageInfoService) {
  }

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
      this.preferencesInfo = preferencesInfo;
      this.onColumnsChange(this.preferencesInfo.tableColumnsInfo.columnsOfBieForOasDocPage);

      this.oasDoc = simpleOasDoc;
      this.init(this.oasDoc);
      this.loadBieListForOasDoc(true);
    }, _ => {
      this.isUpdating = false;
    });
  }

  init(oasDoc: OasDoc) {
    this.hashCodeForOasDoc = hashCode(oasDoc);
    this.oasDoc = oasDoc;
    this.isUpdating = false;
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
      (oasDoc1.version === undefined || oasDoc1.version === '');
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
    }
  }

  onDateEvent(type: string, event: MatDatepickerInputEvent<Date>) {
    switch (type) {
      case 'startDate':
        this.request.updatedDate.start = new Date(event.value);
        break;
      case 'endDate':
        this.request.updatedDate.end = new Date(event.value);
        break;
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

    if (this.hashCodeForOasDoc !== hashCode(this.oasDoc)) {
      this.openAPIService.updateOasDoc(this.oasDoc).subscribe(_ => {
        this.init(this.oasDoc);
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      });
    }

    if (this.getChanged().length > 0) {
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

  _updateDataSource(data: BieForOasDoc[]) {
    this.table.dataSource.data = data;
    this.oasDoc.bieList = data;
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
      return this.oasDoc.releaseId === this.workingRelease.releaseId;
    }
    return false;
  }

  generate() {
    this.loading = true;
    this.openAPIService.generateOpenAPI(this.oasDoc.oasDocId, this.request.page).subscribe(resp => {
      const blob = new Blob([resp.body], {type: resp.headers.get('Content-Type')});
      saveAs(blob, this._getFilenameFromContentDisposition(resp));

      this.loading = false;
    }, err => {
      this.loading = false;
      throw err;
    });
  }

  _getFilenameFromContentDisposition(resp) {
    const contentDisposition = resp.headers.get('Content-Disposition') || '';
    const matches = /filename=([^;]+)/ig.exec(contentDisposition);
    return (matches[1] || 'untitled').replace(/\"/gi, '').trim();
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
