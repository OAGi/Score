import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {ContextSchemeService} from '../domain/context-scheme.service';
import {ContextScheme, ContextSchemeValue, SimpleContextCategory, SimpleContextScheme} from '../domain/context-scheme';
import {
  MatCheckboxChange,
  MatDialog,
  MatDialogConfig,
  MatPaginator,
  MatSelectChange,
  MatSnackBar,
  MatSort,
  MatTableDataSource
} from '@angular/material';
import {ContextSchemeValueDialogComponent} from '../context-scheme-value-dialog/context-scheme-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {CodeList, CodeListValue} from '../../../code-list-management/domain/code-list';
import {v4 as uuid} from 'uuid';
import {forkJoin, ReplaySubject} from 'rxjs';
import {FormControl} from '@angular/forms';
import {take, takeUntil} from 'rxjs/operators';

@Component({
  selector: 'srt-context-scheme-create',
  templateUrl: './context-scheme-create.component.html',
  styleUrls: ['./context-scheme-create.component.css']
})
export class ContextSchemeCreateComponent implements OnInit {

  title = 'Create Context Scheme';
  ctxCategories: SimpleContextCategory[];
  contextSchemes: SimpleContextScheme[];
  codeLists: CodeList[];
  currCodeList: CodeList;
  importFromCodeList: boolean;
  codeListFilterCtrl: FormControl = new FormControl();
  filteredCodeLists: ReplaySubject<CodeList[]> = new ReplaySubject<CodeList[]>(1);
  disabled: boolean;

  contextScheme: ContextScheme;
  displayedColumns: string[] = [
    'select', 'value', 'meaning'
  ];
  dataSource = new MatTableDataSource<ContextSchemeValue>();
  selection = new SelectionModel<ContextSchemeValue>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: ContextSchemeService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.disabled = false;
    this.importFromCodeList = false;
    this.contextScheme = new ContextScheme();
    forkJoin(
      this.service.getSimpleContextCategories(),
      this.service.getCodeLists(),
      this.service.getSimpleContextSchemes())
      .subscribe(([simpleContextCategories, codeLists, simpleContextSchemes]) => {
        this.ctxCategories = simpleContextCategories;
        this.codeLists = codeLists.list;
        this.contextSchemes = simpleContextSchemes;
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

    dialogConfig.data = contextSchemeValue || new class implements ContextSchemeValue {
      ctxSchemeValueId: number;
      guid: string;
      value: string;
      meaning: string;
      used: boolean;
    };

    const isAddAction: boolean = (contextSchemeValue === undefined);

    this.disabled = true;
    const dialogRef = this.dialog.open(ContextSchemeValueDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined && result.value !== undefined && result.value !== '') {
        for ( const value of this.dataSource.data) {
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
      this.dataSource.data.forEach(row => this.selection.select(row));
  }

  removeSchemeValues() {
    const newData = [];
    this.dataSource.data.forEach(row => {
      if (!this.selection.isSelected(row)) {
        newData.push(row);
      }
    });
    this.selection.clear();

    this._updateDataSource(newData);
  }

  back() {
    this.location.back();
  }

  create() {
    if (this.checkUniqueness(this.contextScheme)) {
      this.snackBar.open('Another context scheme with the triplet (schemeID, AgencyID, Version) already exist', '', {
        duration: 4000,
      });
    } else if (this.checkSchemeAgencyName(this.contextScheme)) {
      this.snackBar.open('  Warning, another context scheme with the same properties already exist !', 'Create anyway', {
        duration: 4000,
      }).onAction().subscribe(() => {
        this.service.create(this.contextScheme).subscribe(_ => {
          this.snackBar.open('Created', '', {
            duration: 1000,
          });
          this.router.navigateByUrl('/context_management/context_scheme');
        });
      });
    } else {
      this.service.create(this.contextScheme).subscribe(_ => {
        this.snackBar.open('Created', '', {
          duration: 1000,
        });
        this.router.navigateByUrl('/context_management/context_scheme');
      });
    }
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
        listUniqueness.push(1);
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
        listUniqueness.push(1);
      } else {
        listUniqueness.push(0);
      }
    }
    return this.sum(listUniqueness) > 0;
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
    let _contextschemevalues: ContextSchemeValue[];
    _contextschemevalues = [];
    for (const codelistvalue of codeListValues) {
      let _contextschemevalue: ContextSchemeValue;
      _contextschemevalue = new ContextSchemeValue();
      _contextschemevalue.meaning = codelistvalue.name;
      _contextschemevalue.value = codelistvalue.value;
      _contextschemevalues.push(_contextschemevalue);
      this.selection.select(_contextschemevalue);
    }
    return _contextschemevalues;
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
