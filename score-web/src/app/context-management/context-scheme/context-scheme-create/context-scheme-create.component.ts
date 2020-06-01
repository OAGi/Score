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
import {ConfirmDialogConfig} from '../../../common/confirm-dialog/confirm-dialog.domain';
import {ConfirmDialogComponent} from '../../../common/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'score-context-scheme-create',
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
      this.service.getSimpleContextSchemes()
    ).subscribe(([simpleContextCategories, codeLists, simpleContextSchemes]) => {
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
      ownerCtxSchemeId: number;
    };

    const isAddAction: boolean = (contextSchemeValue === undefined);

    this.disabled = true;
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

  back() {
    this.location.back();
  }

  openDialogContextSchemeCreate() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another context scheme with the triplet (schemeID, AgencyID, Version) already exist!'
    ];

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed().subscribe(_ => {});
  }

  openDialogContextSchemeUpdateCreate() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'The context scheme already has a variable with the same properties';
    dialogConfig.data.content = [
      'Are you sure you want to create the context scheme?'
    ];

    dialogConfig.data.action = 'Create anyway';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.doCreate();
        }
      });
  }

  doCreate() {
    this.service.create(this.contextScheme).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 1000,
      });
      this.router.navigateByUrl('/context_management/context_scheme');
    });
  }

  create() {
    if (this.checkUniqueness(this.contextScheme)) {
      this.openDialogContextSchemeCreate();
    } else if (this.checkSchemeAgencyName(this.contextScheme)) {
      this.openDialogContextSchemeUpdateCreate();
    } else {
      this.doCreate();
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
