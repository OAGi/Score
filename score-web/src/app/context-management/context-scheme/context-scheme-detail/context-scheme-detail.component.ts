import {Component, HostListener, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {CodelistListDialogComponent} from '../codelist-list-dialog/codelist-list-dialog.component';
import {ContextSchemeService} from '../domain/context-scheme.service';
import {ContextSchemeSummary, ContextSchemeUpdateRequest, ContextSchemeValue} from '../domain/context-scheme';
import {BusinessContextService} from '../../business-context/domain/business-context.service';
import {MatCheckboxChange} from '@angular/material/checkbox';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ContextSchemeValueDialogComponent} from '../context-scheme-value-dialog/context-scheme-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {hashCode} from '../../../common/utility';
import {CodeList, CodeListDetails, CodeListListEntry, CodeListValueDetails} from '../../../code-list-management/domain/code-list';
import {forkJoin, ReplaySubject} from 'rxjs';
import {v4 as uuid} from 'uuid';
import {FormControl} from '@angular/forms';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {
  PreferencesInfo,
  TableColumnsInfo,
  TableColumnsProperty
} from '../../../settings-management/settings-preferences/domain/preferences';
import {ScoreTableColumnResizeDirective} from '../../../common/score-table-column-resize/score-table-column-resize.directive';
import {SettingsPreferencesService} from '../../../settings-management/settings-preferences/domain/settings-preferences.service';
import {AuthService} from '../../../authentication/auth.service';
import {ContextCategoryService} from '../../context-category/domain/context-category.service';
import {ContextCategorySummary} from '../../context-category/domain/context-category';
import {CodeListService} from '../../../code-list-management/domain/code-list.service';
import {finalize} from 'rxjs/operators';

@Component({
  selector: 'score-context-scheme-detail',
  templateUrl: './context-scheme-detail.component.html',
  styleUrls: ['./context-scheme-detail.component.css']
})
export class ContextSchemeDetailComponent implements OnInit {

  title = 'Edit Context Scheme';
  isUpdating: boolean;

  ctxCategories: ContextCategorySummary[];
  ctxCategoriesFilterCtrl: FormControl = new FormControl();
  filteredCtxCategories: ReplaySubject<ContextCategorySummary[]> = new ReplaySubject<ContextCategorySummary[]>(1);

  codeLists: CodeList[];
  currCodeList: CodeList;
  codeListFilterCtrl: FormControl = new FormControl();
  filteredCodeLists: ReplaySubject<CodeList[]> = new ReplaySubject<CodeList[]>(1);

  contextSchemes: ContextSchemeSummary[];
  contextSchemeUpdateRequest: ContextSchemeUpdateRequest;
  preferencesInfo: PreferencesInfo;
  hashCode;
  valueSearch: string;
  highlightText: string;
  businessContext;
  disabled: boolean;

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfContextSchemeValuePage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfContextSchemeValuePage = columns;
    this.updateTableColumnsForContextSchemeValuePage();
  }

  updateTableColumnsForContextSchemeValuePage() {
    this.preferencesService.updateTableColumnsForContextSchemeValuePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfContextSchemeValuePage;
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    const updatedColumnsWithWidth = updatedColumns.map(column => ({
      name: column.name,
      selected: column.selected,
      width: this.width(column.name)
    }));

    this.columns = updatedColumnsWithWidth;
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
      this.updateTableColumnsForContextSchemeValuePage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    for (const column of this.columns) {
      switch (column.name) {
        case 'Value':
          if (column.selected) {
            displayedColumns.push('value');
          }
          break;
        case 'Meaning':
          if (column.selected) {
            displayedColumns.push('meaning');
          }
          break;
      }
    }
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<ContextSchemeValue>();
  selection = new SelectionModel<ContextSchemeValue>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;

  constructor(private service: ContextSchemeService,
              private contextCategoryService: ContextCategoryService,
              private codeListService: CodeListService,
              private businessContextService: BusinessContextService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService) {
  }

  ngOnInit() {
    this.contextSchemeUpdateRequest = new ContextSchemeUpdateRequest();
    this.contextSchemeUpdateRequest.used = true;
    const contextSchemeId = this.route.snapshot.params.id;

    this.isUpdating = true;
    forkJoin([
      this.contextCategoryService.getContextCategorySummaries(),
      this.service.getContextSchemeSummaries(),
      this.service.getContextSchemeDetails(contextSchemeId),
      this.service.getContextSchemeValues(contextSchemeId),
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([contextCategorySummries, contextSchemeSummaries,
                    contextScheme, contextSchemeValues, preferencesInfo]) => {

      if (!contextScheme) {
        this.snackBar.open('Access denied.', '', {
          duration: 3000
        });
        this.router.navigateByUrl('/context_management/context_scheme');
        return;
      }

      this.preferencesInfo = preferencesInfo;
      this.ctxCategories = contextCategorySummries;
      this.filteredCtxCategories.next(this.ctxCategories.slice());
      this.codeLists = [];
      this.contextSchemes = contextSchemeSummaries;

      this.contextSchemeUpdateRequest.contextSchemeId = contextScheme.contextSchemeId;
      this.contextSchemeUpdateRequest.schemeName = contextScheme.schemeName;
      this.contextSchemeUpdateRequest.schemeId = contextScheme.schemeId;
      this.contextSchemeUpdateRequest.schemeAgencyId = contextScheme.schemeAgencyId;
      this.contextSchemeUpdateRequest.schemeVersionId = contextScheme.schemeVersionId;
      this.contextSchemeUpdateRequest.description = contextScheme.description;

      this.contextSchemeUpdateRequest.contextCategoryId = contextScheme.contextCategory.contextCategoryId;
      this.contextSchemeUpdateRequest.codeListId = (!!contextScheme.codeList) ? contextScheme.codeList.codeListId : undefined;

      this.contextSchemeUpdateRequest.contextSchemeValueList = contextSchemeValues;
      this.contextSchemeUpdateRequest.used = contextScheme.used;

      this.hashCode = hashCode(this.contextSchemeUpdateRequest);
      this.dataSource.data = this.contextSchemeUpdateRequest.contextSchemeValueList;
      this.filteredCodeLists.next(this.codeLists.slice());

      this.isUpdating = false;
    }, err => {
      this.isUpdating = false;
      let errorMessage;
      if (err.status === 403) {
        errorMessage = 'You do not have access permission.';
      } else {
        errorMessage = 'Something\'s wrong.';
      }
      this.snackBar.open(errorMessage, '', {
        duration: 3000
      });
      this.router.navigateByUrl('/context_management/context_scheme');
      return;
    });

    this.ctxCategoriesFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterCtxCategories();
      });

    // Prevent the sorting event from being triggered if any columns are currently resizing.
    const originalSort = this.sort.sort;
    this.sort.sort = (sortChange) => {
      if (this.tableColumnResizeDirectives &&
        this.tableColumnResizeDirectives.filter(e => e.resizing).length > 0) {
        return;
      }
      originalSort.apply(this.sort, [sortChange]);
    };
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.dataSource.filterPredicate = (data: ContextSchemeValue, filter: string) => {
      return (data.value && data.value.toLowerCase().indexOf(filter) > -1)
          || (data.meaning && data.meaning.toLowerCase().indexOf(filter) > -1);
    };
  }

  filterCtxCategories() {
    let search = this.ctxCategoriesFilterCtrl.value;
    if (!search) {
      this.filteredCtxCategories.next(this.ctxCategories.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredCtxCategories.next(
      this.ctxCategories.filter(contextCategory => contextCategory.name.toLowerCase().indexOf(search) > -1)
    );
  }

  isChanged() {
    return this.hashCode !== hashCode(this.contextSchemeUpdateRequest);
  }

  isDisabled(contextScheme: ContextSchemeUpdateRequest) {
    return (this.disabled) || (this.isUpdating) ||
      (contextScheme.contextCategoryId === undefined || contextScheme.contextCategoryId <= 0) ||
      (contextScheme.schemeName === undefined || contextScheme.schemeName === '') ||
      (contextScheme.schemeId === undefined || contextScheme.schemeId === '') ||
      (contextScheme.schemeAgencyId === undefined || contextScheme.schemeAgencyId === '') ||
      (contextScheme.schemeVersionId === undefined || contextScheme.schemeVersionId === '');
  }

  applyFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // MatTableDataSource defaults to lowercase matches
    this.dataSource.filter = filterValue;
    this.highlightText = filterValue;
  }

  clearFilter() {
    this.valueSearch = '';
    this.applyFilter(this.valueSearch);
  }

  openDialog(contextSchemeValue?: ContextSchemeValue) {
    const dialogConfig = new MatDialogConfig();
    this.disabled = true;
    dialogConfig.data = new ContextSchemeValue();

    if (contextSchemeValue) { // deep copy
      dialogConfig.data = JSON.parse(JSON.stringify(contextSchemeValue));
    }
    const isAddAction: boolean = (contextSchemeValue === undefined);
    const dialogRef = this.dialog.open(ContextSchemeValueDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined && result.value !== undefined && result.value !== '') {
        for (const value of this.dataSource.data) {
          if (value.guid !== result.guid && value.value === result.value) {
            this.snackBar.open(result.value + ' already exist', '', {
              duration: 3000,
            });
            this.disabled = false;
            return;
          }
        }
        if (isAddAction) {
          const data = this.dataSource.data;
          result.guid = uuid();
          data.push(result);
          this._updateDataSource(data);
        } else {
          const newData = [];
          this.dataSource.data.forEach(row => {
            if (row.guid === result.guid) {
              newData.push(result);
            } else {
              newData.push(row);
            }
          });
          this._updateDataSource(newData);
        }
      }
      this.disabled = false;
    });
  }

  removeSchemeValues() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove Context Scheme Value?';
    dialogConfig.data.content = ['Are you sure you want to remove the context scheme value?'];
    dialogConfig.data.action = 'Remove';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          const newData = [];
          this.dataSource.data.forEach(row => {
            if (!this.selection.isSelected(row)) {
              newData.push(row);
            }
          });
          this.selection.clear();

          this._updateDataSource(newData);
        }
      });
  }


  _updateDataSource(data: ContextSchemeValue[]) {
    this.dataSource.data = data;
    this.contextSchemeUpdateRequest.contextSchemeValueList = data;
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => {
        this.selection.select(row);
      });
  }

  select(row: ContextSchemeValue) {
    this.selection.select(row);
  }

  toggle(row: ContextSchemeValue) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: ContextSchemeValue) {
    return this.selection.isSelected(row);
  }

  back() {
    this.location.back();
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

  get updateDisabled(): boolean {
    return !this.isChanged() || this.isDisabled(this.contextSchemeUpdateRequest);
  }

  update() {
    if (this.updateDisabled) {
      return;
    }

    this.checkUniqueness((_) => {
      this.checkSchemeAgencyName((_) => {
        this.doUpdate();
      });
    });
  }

  checkUniqueness(callbackFn?) {
    this.service.checkUniqueness(this.contextSchemeUpdateRequest.schemeId,
        this.contextSchemeUpdateRequest.schemeAgencyId,
        this.contextSchemeUpdateRequest.schemeVersionId,
        this.contextSchemeUpdateRequest.contextSchemeId).subscribe(resp => {
      if (resp) {
        this.openDialogContextSchemeUpdate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  checkSchemeAgencyName(callbackFn?) {
    this.service.checkNameUniqueness(this.contextSchemeUpdateRequest.schemeName,
        this.contextSchemeUpdateRequest.schemeId,
        this.contextSchemeUpdateRequest.schemeAgencyId,
        this.contextSchemeUpdateRequest.schemeVersionId,
        this.contextSchemeUpdateRequest.contextSchemeId).subscribe(resp => {
      if (resp) {
        this.openDialogContextSchemeUpdateIgnore();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  doUpdate() {
    this.isUpdating = true;

    this.service.update(this.contextSchemeUpdateRequest).pipe(finalize(() => {
      this.isUpdating = false;
    })).subscribe(_ => {
      this.hashCode = hashCode(this.contextSchemeUpdateRequest);
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    });
  }

  convertCodeListValuesIntoContextSchemeValues(codeListValues: CodeListValueDetails[]): ContextSchemeValue[] {
    let contextSchemeValueList: ContextSchemeValue[];
    contextSchemeValueList = [];
    for (const codelistvalue of codeListValues) {
      let contextSchemeValue: ContextSchemeValue;
      contextSchemeValue = new ContextSchemeValue();
      contextSchemeValue.meaning = codelistvalue.meaning;
      contextSchemeValue.value = codelistvalue.value;
      contextSchemeValueList.push(contextSchemeValue);
      this.selection.select(contextSchemeValue);
    }
    return contextSchemeValueList;
  }

  openDialogContextSchemeUpdate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another context scheme with the triplet (schemeID, AgencyID, Version) already exist!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

  openDialogContextSchemeUpdateIgnore() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'The context scheme already has a variable with the same properties';
    dialogConfig.data.content = [
      'Are you sure you want to update the context scheme?'
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
    dialogConfig.data.header = 'Discard Context Scheme?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this context scheme?',
      'The context scheme will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(this.contextSchemeUpdateRequest.contextSchemeId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.router.navigateByUrl('/context_management/context_scheme');
          });
        }
      });
  }

  resetCreateForm(event: MatCheckboxChange) {
    if (!event.checked) {
      this.contextSchemeUpdateRequest.codeListId = undefined;
      this.selection.clear();
      this._updateDataSource([]);
    }

    this.contextSchemeUpdateRequest.schemeId = undefined;
    this.contextSchemeUpdateRequest.schemeAgencyId = undefined;
    this.contextSchemeUpdateRequest.schemeVersionId = undefined;
  }

  canImport(): boolean {
    if (this.contextSchemeUpdateRequest.used) {
      return false;
    }
    if (this.contextSchemeUpdateRequest.contextSchemeValueList &&
        this.contextSchemeUpdateRequest.contextSchemeValueList.filter(e => e.used).length > 0) {
      return false;
    }
    return true;
  }

  openCodeListDialog() {
    if (this.isDirty()) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Confirmation';
      dialogConfig.data.content = ['All existing values will be removed and replaced with values from the code list.'];
      dialogConfig.data.action = 'Continue';

      this.confirmDialogService.open(dialogConfig).afterClosed()
        .subscribe(result => {
          if (result) {
            this._loadFromCodeList();
          }
        });
    } else {
      this._loadFromCodeList();
    }
  }

  _loadFromCodeList() {
    const codeListDialogConfig = {
      data: {},
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    };
    codeListDialogConfig.width = window.innerWidth + 'px';

    const dialogRef = this.dialog.open(CodelistListDialogComponent, codeListDialogConfig);
    dialogRef.afterClosed().subscribe((codeList: CodeListListEntry) => {
      if (codeList) {
        this.codeListService.getCodeListDetails(codeList.codeListManifestId).subscribe((codeListDetails: CodeListDetails) => {
          this.contextSchemeUpdateRequest.schemeId = codeListDetails.listId.toString();
          this.contextSchemeUpdateRequest.schemeAgencyId = codeListDetails.agencyIdListValue.value.toString();
          this.contextSchemeUpdateRequest.schemeVersionId = codeListDetails.versionId.toString();
          this._updateDataSource([]);
          this._updateDataSource(this.convertCodeListValuesIntoContextSchemeValues(codeListDetails.valueList));
        });
      }
    });
  }

  isDirty(): boolean {
    return this.contextSchemeUpdateRequest.schemeId && this.contextSchemeUpdateRequest.schemeId.length > 0
      || this.contextSchemeUpdateRequest.schemeAgencyId && this.contextSchemeUpdateRequest.schemeAgencyId.length > 0
      || this.contextSchemeUpdateRequest.schemeVersionId && this.contextSchemeUpdateRequest.schemeVersionId.length > 0
      || this.contextSchemeUpdateRequest.contextSchemeValueList && this.contextSchemeUpdateRequest.contextSchemeValueList.length > 0;
  }

}
