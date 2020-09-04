import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
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
import {ConfirmDialogConfig} from '../../../common/confirm-dialog/confirm-dialog.domain';
import {ConfirmDialogComponent} from '../../../common/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'score-context-scheme-detail',
  templateUrl: './context-scheme-detail.component.html',
  styleUrls: ['./context-scheme-detail.component.css']
})
export class ContextSchemeDetailComponent implements OnInit {

  title = 'Edit Context Scheme';
  ctxCategories: SimpleContextCategory[];
  codeLists: CodeList[];
  currCodeList: CodeList;
  codeListFilterCtrl: FormControl = new FormControl();
  filteredCodeLists: ReplaySubject<CodeList[]> = new ReplaySubject<CodeList[]>(1);

  contextSchemes: SimpleContextScheme[];
  bizCtxValues: BusinessContextValue[];
  contextScheme: ContextScheme;
  hashCode;
  listDisplayed;
  bizCtx;
  importFromCodeList: boolean;
  disabled: boolean;

  displayedColumns: string[] = [
    'select', 'value', 'meaning'
  ];
  dataSource = new MatTableDataSource<ContextSchemeValue>();
  selection = new SelectionModel<ContextSchemeValue>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: ContextSchemeService,
              private bizCtxService: BusinessContextService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.contextScheme = new ContextScheme();
    const contextSchemeId = this.route.snapshot.params.id;

    forkJoin(
      this.service.getSimpleContextCategories(),
      this.service.getCodeLists(),
      this.service.getSimpleContextSchemes(),
      this.bizCtxService.getBusinessContextValues(),
      this.service.getContextScheme(contextSchemeId)
    ).subscribe(([simpleContextCategories, codeLists,
                   simpleContextSchemes, businessContextValues,
                   contextScheme]) => {
      this.ctxCategories = simpleContextCategories;
      this.codeLists = codeLists.list;
      this.contextSchemes = simpleContextSchemes;
      this.bizCtxValues = businessContextValues;
      this.contextScheme = contextScheme;
      this.importFromCodeList = this.contextScheme.codeListId ? true : false;
      this.hashCode = hashCode(contextScheme);
      this.dataSource.data = this.contextScheme.ctxSchemeValues;
      this.filteredCodeLists.next(this.codeLists.slice());
    });

    this.codeListFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterCodeLists();
      });

    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  filterCodeLists() {
    let search = this.codeListFilterCtrl.value;
    if (!search) {
      this.filteredCodeLists.next(this.codeLists.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredCodeLists.next(
      this.codeLists.filter(codeList => codeList.codeListName.toLowerCase().indexOf(search) > -1)
    );
  }

  isChanged() {
    return this.hashCode !== hashCode(this.contextScheme);
  }

  isDisabled(contextScheme: ContextScheme) {
    return (this.disabled) ||
      (contextScheme.ctxCategoryId === undefined || contextScheme.ctxCategoryId <= 0) ||
      (contextScheme.schemeName === undefined || contextScheme.schemeName === '') ||
      (contextScheme.schemeId === undefined || contextScheme.schemeId === '') ||
      (contextScheme.schemeAgencyId === undefined || contextScheme.schemeAgencyId === '') ||
      (contextScheme.schemeVersionId === undefined || contextScheme.schemeVersionId === '');
  }

  openDialog(contextSchemeValue?: ContextSchemeValue) {
    if (this.importFromCodeList) {
      return false;
    }
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
          if (value.value === result.value) {
            this.snackBar.open(result.value + ' already exist', '', {
              duration: 4000,
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
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Remove Context Scheme Value?';
    dialogConfig.data.content = 'Are you sure you want to remove the context scheme value?';
    dialogConfig.data.action = 'Remove';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
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
    this.contextScheme.ctxSchemeValues = data;
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

  sum(list: number[]): number {
    let sum: number;
    sum = 0;
    for (const i of list) {
      sum = sum + i;
    }
    return sum;
  }

  checkUniqueness(_contextScheme: ContextScheme): boolean {
    const listUniqueness: number[] = [0];
    for (const contextScheme of this.contextSchemes) {
      if (_contextScheme.schemeId === contextScheme.schemeId
        && _contextScheme.schemeAgencyId === contextScheme.schemeAgencyId
        && _contextScheme.schemeVersionId === contextScheme.schemeVersionId) {
        if (_contextScheme.ctxSchemeId === contextScheme.ctxSchemeId) {
          listUniqueness.push(0);
        } else {
          listUniqueness.push(1);
        }
      } else {
        listUniqueness.push(0);
      }
    }
    return this.sum(listUniqueness) > 0;
  }

  checkSchemeAgencyName(_contextScheme: ContextScheme): boolean {
    const listUniqueness: number[] = [0];
    for (const contextScheme of this.contextSchemes) {
      if (_contextScheme.schemeId === contextScheme.schemeId
        && _contextScheme.schemeAgencyId === contextScheme.schemeAgencyId) {
        if (_contextScheme.ctxSchemeId === contextScheme.ctxSchemeId) {
          listUniqueness.push(0);
        } else {
          listUniqueness.push(1);
        }
      } else {
        listUniqueness.push(0);
      }
    }
    return this.sum(listUniqueness) > 0;
  }

  update() {
    if (this.checkUniqueness(this.contextScheme)) {
      this.alertInvalidParameters();
    } else if (this.checkSchemeAgencyName(this.contextScheme)) {
      this.alertDuplicatedProperties();
    } else {
      this.doUpdate();
    }
  }

  doUpdate() {
    this.service.update(this.contextScheme).subscribe(_ => {
      this.hashCode = hashCode(this.contextScheme);
      this.snackBar.open('Updated', '', {
        duration: 1000,
      });
      this.router.navigateByUrl('/context_management/context_scheme');
    });
  }

  completeInputAgencyAndVersion(event: MatSelectChange) {
    this.service.getCodeList(event.value).subscribe(val => {
      this.currCodeList = val;
      this.contextScheme.schemeId = this.currCodeList.listId.toString();
      this.contextScheme.schemeAgencyId = this.currCodeList.agencyId.toString();
      this.contextScheme.schemeVersionId = this.currCodeList.versionId.toString();
      this._updateDataSource(this.convertCodeListValuesIntoContextSchemeValues(this.currCodeList.codeListValues));
    });
  }

  convertCodeListValuesIntoContextSchemeValues(codeListValues: CodeListValue[]) {
    if (!codeListValues || codeListValues.length === 0) {
      return [];
    }

    return codeListValues.map(codeListValue => {
      const contextSchemeValue = new ContextSchemeValue();
      contextSchemeValue.meaning = codeListValue.name;
      contextSchemeValue.value = codeListValue.value;
      this.selection.select(contextSchemeValue);
      return contextSchemeValue;
    });
  }

  openDialogContextScheme(listDisplayed: string[]) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'The context scheme cannot be deleted!';
    dialogConfig.data.content = [
      'The business contexts with the following IDs depend on it. They need to be deleted first.'
    ];
    dialogConfig.data.list = listDisplayed;

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed().subscribe(_ => {
    });
  }

  alertInvalidParameters() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another context scheme with the triplet (schemeID, AgencyID, Version) already exist!'
    ];

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed().subscribe(_ => {
    });
  }

  alertDuplicatedProperties() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Duplicated Properties';
    dialogConfig.data.content = [
      'Another context scheme with the same properties already exists.',
      'Are you sure you want to update the context scheme?'
    ];

    dialogConfig.data.action = 'Update anyway';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.doUpdate();
        }
      });
  }

  openDialogContextSchemeDiscard() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Discard Context Scheme?';
    dialogConfig.data.content = [
      'Are you sure you want to discard the context scheme?',
      'The context scheme will be permanently removed.'
    ];

    dialogConfig.data.action = 'Discard';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(this.contextScheme.ctxSchemeId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 1000,
            });
            this.router.navigateByUrl('/context_management/context_scheme');
          });
        }
      });
  }

  discard() {
    const linkedBizCtxIds: number[] = [];
    const displayedList = [];
    this.service.getSimpleContextSchemeValues(this.contextScheme.ctxSchemeId).subscribe(values => {
      values.forEach(value => {
        for (let i = 0; i < this.bizCtxValues.length; i++) {
          if (value.ctxSchemeValueId === this.bizCtxValues[i].ctxSchemeValueId) {
            linkedBizCtxIds.push(this.bizCtxValues[i].bizCtxId);
          }
        }
      });
      // remove the duplicate id's from the list
      const uniqueBizCtxIds = linkedBizCtxIds.filter(function (elem, index, self) {
        return index === self.indexOf(elem);
      });

      if (uniqueBizCtxIds.length > 0) {
        uniqueBizCtxIds.forEach(bizCtxId => {
          this.bizCtxService.getBusinessContext(bizCtxId).subscribe(data => {
            displayedList.push(data.guid);
          });
        });
        this.openDialogContextScheme(displayedList);
      } else {
        this.openDialogContextSchemeDiscard();
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
    if (this.importFromCodeList) {
      return true;
    } else {
      if (this.contextScheme.ctxSchemeValues && this.contextScheme.ctxSchemeValues.length > 0) {
        return false;
      }
      return true;
    }
  }
}
