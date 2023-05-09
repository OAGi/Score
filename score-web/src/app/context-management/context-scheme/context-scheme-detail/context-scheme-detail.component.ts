import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {CodelistListDialogComponent} from '../codelist-list-dialog/codelist-list-dialog.component';
import {ContextSchemeService} from '../domain/context-scheme.service';
import {ContextScheme, ContextSchemeValue, SimpleContextCategory, SimpleContextScheme} from '../domain/context-scheme';
import {BusinessContextService} from '../../business-context/domain/business-context.service';
import {BusinessContextValue} from '../../business-context/domain/business-context';
import {MatCheckboxChange} from '@angular/material/checkbox';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSelectChange} from '@angular/material/select';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ContextSchemeValueDialogComponent} from '../context-scheme-value-dialog/context-scheme-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {hashCode} from '../../../common/utility';
import {CodeList, CodeListValue} from '../../../code-list-management/domain/code-list';
import {forkJoin, ReplaySubject} from 'rxjs';
import {v4 as uuid} from 'uuid';
import {FormControl} from '@angular/forms';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'score-context-scheme-detail',
  templateUrl: './context-scheme-detail.component.html',
  styleUrls: ['./context-scheme-detail.component.css']
})
export class ContextSchemeDetailComponent implements OnInit {

  title = 'Edit Context Scheme';
  ctxCategories: SimpleContextCategory[];
  ctxCategoriesFilterCtrl: FormControl = new FormControl();
  filteredCtxCategories: ReplaySubject<SimpleContextCategory[]> = new ReplaySubject<SimpleContextCategory[]>(1);

  codeLists: CodeList[];
  currCodeList: CodeList;
  codeListFilterCtrl: FormControl = new FormControl();
  filteredCodeLists: ReplaySubject<CodeList[]> = new ReplaySubject<CodeList[]>(1);

  contextSchemes: SimpleContextScheme[];
  businessContextValueList: BusinessContextValue[];
  contextScheme: ContextScheme;
  hashCode;
  businessContext;
  disabled: boolean;

  displayedColumns: string[] = [
    'select', 'value', 'meaning'
  ];
  dataSource = new MatTableDataSource<ContextSchemeValue>();
  selection = new SelectionModel<ContextSchemeValue>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: ContextSchemeService,
              private businessContextService: BusinessContextService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit() {
    this.contextScheme = new ContextScheme();
    this.contextScheme.used = true;
    const contextSchemeId = this.route.snapshot.params.id;

    forkJoin(
      this.service.getSimpleContextCategories(),
      this.service.getSimpleContextSchemes(),
      this.businessContextService.getBusinessContextValues(),
      this.service.getContextScheme(contextSchemeId))
      .subscribe(([simpleContextCategories, simpleContextSchemes, businessContextValueList, contextScheme]) => {
        this.ctxCategories = simpleContextCategories;
        this.filteredCtxCategories.next(this.ctxCategories.slice());
        this.codeLists = [];
        this.contextSchemes = simpleContextSchemes;
        this.businessContextValueList = businessContextValueList;
        this.contextScheme = contextScheme;
        this.hashCode = hashCode(contextScheme);
        this.dataSource.data = this.contextScheme.contextSchemeValueList;
        this.filteredCodeLists.next(this.codeLists.slice());
      });

    this.ctxCategoriesFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterCtxCategories();
      });

    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
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
    return this.hashCode !== hashCode(this.contextScheme);
  }

  isDisabled(contextScheme: ContextScheme) {
    return (this.disabled) ||
      (contextScheme.contextCategoryId === undefined || contextScheme.contextCategoryId <= 0) ||
      (contextScheme.schemeName === undefined || contextScheme.schemeName === '') ||
      (contextScheme.schemeId === undefined || contextScheme.schemeId === '') ||
      (contextScheme.schemeAgencyId === undefined || contextScheme.schemeAgencyId === '') ||
      (contextScheme.schemeVersionId === undefined || contextScheme.schemeVersionId === '');
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
    this.contextScheme.contextSchemeValueList = data;
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

  update() {
    this.checkUniqueness(this.contextScheme, (_) => {
      this.checkSchemeAgencyName(this.contextScheme, (dummy) => {
        this.doUpdate();
      });
    });
  }

  checkUniqueness(_contextScheme: ContextScheme, callbackFn?) {
    this.service.checkUniqueness(_contextScheme).subscribe(resp => {
      if (resp) {
        this.openDialogContextSchemeUpdate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  checkSchemeAgencyName(_contextScheme: ContextScheme, callbackFn?) {
    this.service.checkNameUniqueness(_contextScheme).subscribe(resp => {
      if (resp) {
        this.openDialogContextSchemeUpdateIgnore();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  doUpdate() {
    this.service.update(this.contextScheme).subscribe(_ => {
      this.hashCode = hashCode(this.contextScheme);
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    });
  }

  completeInputAgencyAndVersion(event: MatSelectChange) {
    this.service.getCodeList(event.value).subscribe(val => {
      this.currCodeList = val;
      this.contextScheme.schemeId = this.currCodeList.listId.toString();
      this.contextScheme.schemeAgencyId = this.currCodeList.agencyIdListValueManifestId.toString();
      this.contextScheme.schemeVersionId = this.currCodeList.versionId.toString();
      this._updateDataSource(this.convertCodeListValuesIntoContextSchemeValues(this.currCodeList.codeListValues));
    });
  }

  convertCodeListValuesIntoContextSchemeValues(codeListValues: CodeListValue[]) {
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
          this.service.delete(this.contextScheme.contextSchemeId).subscribe(_ => {
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
      this.contextScheme.codeListId = undefined;
      this.selection.clear();
      this._updateDataSource([]);
    }

    this.contextScheme.schemeId = undefined;
    this.contextScheme.schemeAgencyId = undefined;
    this.contextScheme.schemeVersionId = undefined;
  }

  canImport(): boolean {
    if (this.contextScheme.used) {
      return false;
    }
    if (this.contextScheme.contextSchemeValueList && this.contextScheme.contextSchemeValueList.filter(e => e.used).length > 0) {
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
    dialogRef.afterClosed().subscribe(codeList => {
      if (codeList) {
        this.contextScheme.schemeId = codeList.listId.toString();
        this.contextScheme.schemeAgencyId = codeList.agencyIdListValueValue.toString();
        this.contextScheme.schemeVersionId = codeList.versionId.toString();
        this._updateDataSource([]);
        this._updateDataSource(this.convertCodeListValuesIntoContextSchemeValues(codeList.codeListValues));
      }
    });
  }

  isDirty(): boolean {
    return this.contextScheme.schemeId && this.contextScheme.schemeId.length > 0
      || this.contextScheme.schemeAgencyId && this.contextScheme.schemeAgencyId.length > 0
      || this.contextScheme.schemeVersionId && this.contextScheme.schemeVersionId.length > 0
      || this.contextScheme.contextSchemeValueList && this.contextScheme.contextSchemeValueList.length > 0;
  }

}
