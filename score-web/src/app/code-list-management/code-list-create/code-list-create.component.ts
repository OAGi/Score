import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {CodeListService} from '../domain/code-list.service';
import {CodeList, CodeListValue, SimpleAgencyIdListValue} from '../domain/code-list';
import {MatTableDataSource} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {CodeListValueDialogComponent} from '../code-list-value-dialog/code-list-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {switchMap} from 'rxjs/operators';
import {hashCode} from '../../common/utility';
import {v4 as uuid} from 'uuid';
import {EMPTY, ReplaySubject} from 'rxjs';
import {FormControl} from '@angular/forms';
import {ConfirmDialogConfig} from '../../common/confirm-dialog/confirm-dialog.domain';
import {ConfirmDialogComponent} from '../../common/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'score-code-list-create',
  templateUrl: './code-list-create.component.html',
  styleUrls: ['./code-list-create.component.css']
})
export class CodeListCreateComponent implements OnInit {

  title: string;
  agencyIdListValues: SimpleAgencyIdListValue[];
  disabled: boolean;
  codeLists: CodeList[];

  codeList: CodeList;
  basedCodeList: CodeList;
  agencyListFilterCtrl: FormControl = new FormControl();
  filteredAgencyLists: ReplaySubject<SimpleAgencyIdListValue[]> = new ReplaySubject<SimpleAgencyIdListValue[]>(1);
  hashCode;

  displayedColumns: string[] = [
    'select', 'value', 'name', 'definition', 'definitionSource'
  ];

  dataSource = new MatTableDataSource<CodeListValue>();
  selection = new SelectionModel<CodeListValue>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: CodeListService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.disabled = false;
    this.service.getSimpleAgencyIdListValues().subscribe(resp => {
      this.agencyIdListValues = resp;
      this.filteredAgencyLists.next(this.agencyIdListValues.slice());
    });
    this.agencyListFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterAgencyList();
      });
    this.codeList = new CodeList();
    this.service.getCodeLists().subscribe(resp2 => this.codeLists = resp2.list);

    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
          const basedCodeListId = params.get('basedCodeListId');
          if (basedCodeListId !== null) {
            return this.service.getCodeList(basedCodeListId);
          } else {

            this.title = 'Create Code List';
            this.codeList.extensible = false;

            return EMPTY;
          }
        }
      )).subscribe(resp => {
      this.basedCodeList = resp;

      this.title = 'Derive Code List from ' + this.basedCodeList.codeListName;

      this.codeList.codeListName = this.basedCodeList.codeListName + '_Extension';
      this.codeList.basedCodeListId = this.basedCodeList.codeListId;
      this.codeList.basedCodeListName = this.basedCodeList.codeListName;
      this.codeList.agencyId = this.basedCodeList.agencyId;
      this.codeList.versionId = this.basedCodeList.versionId;
      this.codeList.definition = this.basedCodeList.definition;
      this.codeList.definitionSource = this.basedCodeList.definitionSource;
      this.codeList.remark = this.basedCodeList.remark;
      this.codeList.extensible = this.basedCodeList.extensible;

      this._updateDataSource(this.basedCodeList.codeListValues.map((codeListValue: CodeListValue) => {
        const clone = new CodeListValue();

        clone.guid = uuid();
        clone.value = codeListValue.value;
        clone.name = codeListValue.name;
        clone.definition = codeListValue.definition;
        clone.definitionSource = codeListValue.definitionSource;
        clone.used = codeListValue.used;
        clone.locked = codeListValue.locked;
        clone.extension = codeListValue.extension;

        if (this.color(clone) === 'green') {
          clone.extension = false;
        }

        return clone;
      }));
    });

    this.codeList.listId = 'oagis-id-' + uuid().replace('-', '');
    this.codeList.state = 'Editing';

    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  filterAgencyList() {
    let search = this.agencyListFilterCtrl.value;
    if (!search) {
      this.filteredAgencyLists.next(this.agencyIdListValues.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredAgencyLists.next(
      this.agencyIdListValues.filter(agencyList => agencyList.name.toLowerCase().indexOf(search) > -1)
    );
  }

  color(codeListValue: CodeListValue): string {
    if (codeListValue.locked) {
      return 'bright-red';
    }

    if (codeListValue.used) {
      if (codeListValue.extension) {
        return 'green';
      } else {
        return 'blue';
      }
    }

    return 'dull-red';
  }

  isChanged() {
    return this.hashCode !== hashCode(this.codeList);
  }

  isDisabled(codeList: CodeList) {
    return (this.disabled) ||
      (codeList.codeListName === undefined || codeList.codeListName === '') ||
      (codeList.listId === undefined || codeList.listId === '') ||
      (codeList.agencyId === undefined) ||
      (codeList.versionId === undefined || codeList.versionId === '');
  }

  openDialog(codeListValue?: CodeListValue) {
    if (this.codeList.state !== 'Editing') {
      return;
    }

    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = new CodeListValue();
    // Default indicator values
    dialogConfig.data.used = true;
    dialogConfig.data.extension = true;

    if (codeListValue) { // deep copy
      dialogConfig.data = JSON.parse(JSON.stringify(codeListValue));
    }

    const isAddAction: boolean = (codeListValue === undefined);

    this.disabled = true;
    const dialogRef = this.dialog.open(CodeListValueDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      this.disabled = false;

      if (!result) {
        return;
      }

      const data = this.dataSource.data;
      if (isAddAction) {
        for (const value of data) {
          if (value.value === result.value) {
            this.snackBar.open(result.value + ' already exist', '', {
              duration: 4000,
            });

            return;
          }
        }

        result.guid = uuid();
        data.push(result);

        this._updateDataSource(data);
      } else {
        for (const value of data) {
          if (value.guid !== result.guid && value.value === result.value) {
            this.snackBar.open(result.value + ' already exist', '', {
              duration: 4000,
            });

            return;
          }
        }

        this._updateDataSource(data.map(row => {
          if (row.guid === result.guid) {
            return result;
          } else {
            return row;
          }
        }));
      }
    });
  }

  _updateDataSource(data: CodeListValue[]) {
    this.dataSource.data = data;
    this.codeList.codeListValues = data;
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.filter(row => this.isAvailable(row)).length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: CodeListValue) {
    if (this.isAvailable(row)) {
      this.selection.select(row);
    }
  }

  toggle(row: CodeListValue) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CodeListValue) {
    return this.selection.isSelected(row);
  }

  isAvailable(codeListValue: CodeListValue) {
    return this.codeList.state === 'Editing' && this.color(codeListValue) === 'green';
  }

  removeCodeListValues() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Remove Code List Value?';
    dialogConfig.data.content = 'Are you sure you want to remove the code list value?';
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

  sum(list: number[]): number {
    let sum = 0;
    for (const i of list) {
      sum = sum + i;
    }
    return sum;
  }

  checkUniqueness(_codeList: CodeList): boolean {
    const listUniqueness: number[] = [0];
    for (const codeList of this.codeLists) {
      if (_codeList.listId === codeList.listId
        && _codeList.agencyId === codeList.agencyId
        && _codeList.versionId === codeList.versionId) {
        listUniqueness.push(1);
      } else {
        listUniqueness.push(0);
      }
    }
    return this.sum(listUniqueness) > 0;
  }

  checkNameUniqueness(_codeList: CodeList): boolean {
    const listUniqueness: number[] = [0];
    for (const codeList of this.codeLists) {
      if (_codeList.codeListName === codeList.codeListName) {
        listUniqueness.push(1);
      } else {
        listUniqueness.push(0);
      }
    }
    return this.sum(listUniqueness) > 0;
  }

  alertInvalidParameters() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another code list with the triplet (ListID, AgencyID, Version) already exist!'
    ];

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed().subscribe(_ => {});
  }

  alertDuplicatedProperties() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Duplicated Properties';
    dialogConfig.data.content = [
      'Another code list with the same name already exists.',
      'Are you sure you want to create the code list?'
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
    this.service.create(this.codeList).subscribe(_ => {
      this.hashCode = hashCode(this.codeList);
      this.snackBar.open('Created', '', {
        duration: 1000,
      });
      this.router.navigateByUrl('/code_list');
    });
  }

  create() {
    if (this.checkUniqueness(this.codeList)) {
      this.alertInvalidParameters();
    } else if (this.checkNameUniqueness(this.codeList)) {
      this.alertDuplicatedProperties();
    } else {
      this.doCreate();
    }
  }

}
