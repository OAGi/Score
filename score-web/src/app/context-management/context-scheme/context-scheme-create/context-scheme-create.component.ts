import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {CodelistListDialogComponent} from '../codelist-list-dialog/codelist-list-dialog.component';
import {ContextSchemeService} from '../domain/context-scheme.service';
import {ContextSchemeCreateRequest, ContextSchemeSummary, ContextSchemeValue} from '../domain/context-scheme';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ContextSchemeValueDialogComponent} from '../context-scheme-value-dialog/context-scheme-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {CodeList, CodeListDetails, CodeListListEntry, CodeListValueDetails} from '../../../code-list-management/domain/code-list';
import {v4 as uuid} from 'uuid';
import {forkJoin, ReplaySubject} from 'rxjs';
import {FormControl} from '@angular/forms';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {
  PreferencesInfo,
  TableColumnsInfo,
  TableColumnsProperty
} from '../../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../../settings-management/settings-preferences/domain/settings-preferences.service';
import {AuthService} from '../../../authentication/auth.service';
import {ContextCategoryService} from '../../context-category/domain/context-category.service';
import {ContextCategorySummary} from '../../context-category/domain/context-category';
import {CodeListService} from '../../../code-list-management/domain/code-list.service';

@Component({
  selector: 'score-context-scheme-create',
  templateUrl: './context-scheme-create.component.html',
  styleUrls: ['./context-scheme-create.component.css']
})
export class ContextSchemeCreateComponent implements OnInit {

  title = 'Create Context Scheme';
  ctxCategories: ContextCategorySummary[];
  ctxCategoriesFilterCtrl: FormControl = new FormControl();
  filteredCtxCategories: ReplaySubject<ContextCategorySummary[]> = new ReplaySubject<ContextCategorySummary[]>(1);

  contextSchemes: ContextSchemeSummary[];
  codeLists: CodeList[];
  currCodeList: CodeList;
  codeListFilterCtrl: FormControl = new FormControl();
  filteredCodeLists: ReplaySubject<CodeList[]> = new ReplaySubject<CodeList[]>(1);
  disabled: boolean;

  contextSchemeCreateRequest: ContextSchemeCreateRequest;
  preferencesInfo: PreferencesInfo;
  valueSearch: string;
  highlightText: string;

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

  constructor(private service: ContextSchemeService,
              private contextCategoryService: ContextCategoryService,
              private codeListService: CodeListService,
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
    this.disabled = false;
    this.contextSchemeCreateRequest = new ContextSchemeCreateRequest();

    forkJoin([
      this.contextCategoryService.getContextCategorySummaries(),
      this.service.getContextSchemeSummaries(),
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([contextCategorySummries, contextSchemeSummaries, preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;
      this.ctxCategories = contextCategorySummries;
      this.filteredCtxCategories.next(this.ctxCategories.slice());
      this.codeLists = [];
      this.contextSchemes = contextSchemeSummaries;
      this.filteredCodeLists.next(this.codeLists.slice());
    });

    this.ctxCategoriesFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterCtxCategories();
      });

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

  isDisabled() {
    return (this.disabled) ||
      (this.contextSchemeCreateRequest.contextCategoryId === undefined || this.contextSchemeCreateRequest.contextCategoryId <= 0) ||
      (this.contextSchemeCreateRequest.schemeName === undefined || this.contextSchemeCreateRequest.schemeName === '') ||
      (this.contextSchemeCreateRequest.schemeId === undefined || this.contextSchemeCreateRequest.schemeId === '') ||
      (this.contextSchemeCreateRequest.schemeAgencyId === undefined || this.contextSchemeCreateRequest.schemeAgencyId === '') ||
      (this.contextSchemeCreateRequest.schemeVersionId === undefined || this.contextSchemeCreateRequest.schemeVersionId === '');
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

    dialogConfig.data = contextSchemeValue || new class implements ContextSchemeValue {
      contextSchemeValueId: number;
      guid: string;
      value: string;
      meaning: string;
      used: boolean;
      ownerContextSchemeId: number;
    };

    const isAddAction: boolean = (contextSchemeValue === undefined);
    if (!isAddAction) {
      dialogConfig.data = JSON.parse(JSON.stringify(contextSchemeValue));
    }
    this.disabled = true;
    const dialogRef = this.dialog.open(ContextSchemeValueDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined && result.value !== undefined && result.value !== '') {
        for (const value of this.dataSource.data) {
          if (value.value === result.value) {
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

  _updateDataSource(data: ContextSchemeValue[]) {
    this.dataSource.data = data;
    this.contextSchemeCreateRequest.contextSchemeValueList = data;
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
      this.dataSource.data.forEach(row => this.selection.select(row));
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

  back() {
    this.location.back();
  }

  openDialogContextSchemeCreate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another context scheme with the triplet (schemeID, AgencyID, Version) already exist!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

  openDialogContextSchemeUpdateCreate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'The context scheme already has a variable with the same properties';
    dialogConfig.data.content = [
      'Are you sure you want to create the context scheme?'
    ];
    dialogConfig.data.action = 'Create anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.doCreate();
        }
      });
  }

  doCreate() {
    this.service.create(this.contextSchemeCreateRequest).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/context_management/context_scheme');
    });
  }

  create() {
    this.checkUniqueness((_) => {
      this.checkSchemeAgencyName((_) => {
        this.doCreate();
      });
    });
  }

  checkUniqueness(callbackFn?) {
    this.service.checkUniqueness(this.contextSchemeCreateRequest.schemeId,
        this.contextSchemeCreateRequest.schemeAgencyId,
        this.contextSchemeCreateRequest.schemeVersionId).subscribe(resp => {
      if (resp) {
        this.openDialogContextSchemeCreate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  checkSchemeAgencyName(callbackFn?) {
    this.service.checkNameUniqueness(this.contextSchemeCreateRequest.schemeName,
        this.contextSchemeCreateRequest.schemeId,
        this.contextSchemeCreateRequest.schemeAgencyId,
        this.contextSchemeCreateRequest.schemeVersionId).subscribe(resp => {
      if (resp) {
        this.openDialogContextSchemeUpdateCreate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  convertCodeListValuesIntoContextSchemeValues(codeListValues: CodeListValueDetails[]): ContextSchemeValue[] {
    let contextSchemeValueList: ContextSchemeValue[];
    contextSchemeValueList = [];
    for (const codeListValue of codeListValues) {
      let contextSchemeValue: ContextSchemeValue;
      contextSchemeValue = new ContextSchemeValue();
      contextSchemeValue.meaning = codeListValue.meaning;
      contextSchemeValue.value = codeListValue.value;
      contextSchemeValue.guid = uuid();
      contextSchemeValueList.push(contextSchemeValue);
    }
    return contextSchemeValueList;
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
    const codeListdialogConfig = {
      data: {},
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    };
    codeListdialogConfig.width = window.innerWidth + 'px';

    const dialogRef = this.dialog.open(CodelistListDialogComponent, codeListdialogConfig);
    dialogRef.afterClosed().subscribe((codeList: CodeListListEntry) => {
      if (codeList) {
        this.codeListService.getCodeListDetails(codeList.codeListManifestId).subscribe((codeListDetails: CodeListDetails) => {
          this.contextSchemeCreateRequest.schemeId = codeListDetails.listId.toString();
          this.contextSchemeCreateRequest.schemeAgencyId = codeListDetails.agencyIdListValue.value.toString();
          this.contextSchemeCreateRequest.schemeVersionId = codeListDetails.versionId.toString();
          this._updateDataSource([]);
          this._updateDataSource(this.convertCodeListValuesIntoContextSchemeValues(codeListDetails.valueList));
        });
      }
    });
  }

  isDirty(): boolean {
    return this.contextSchemeCreateRequest.schemeId && this.contextSchemeCreateRequest.schemeId.length > 0
      || this.contextSchemeCreateRequest.schemeAgencyId && this.contextSchemeCreateRequest.schemeAgencyId.length > 0
      || this.contextSchemeCreateRequest.schemeVersionId && this.contextSchemeCreateRequest.schemeVersionId.length > 0
      || this.contextSchemeCreateRequest.contextSchemeValueList && this.contextSchemeCreateRequest.contextSchemeValueList.length > 0;
  }
}
