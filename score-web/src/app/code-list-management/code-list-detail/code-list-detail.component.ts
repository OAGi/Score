import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {CodeListService} from '../domain/code-list.service';
import {CodeList, CodeListValue, SimpleAgencyIdListValue} from '../domain/code-list';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {CodeListValueDialogComponent} from '../code-list-value-dialog/code-list-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {finalize, switchMap} from 'rxjs/operators';
import {v4 as uuid} from 'uuid';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {hashCode} from '../../common/utility';
import {ConfirmDialogConfig} from '../../common/confirm-dialog/confirm-dialog.domain';
import {ConfirmDialogComponent} from '../../common/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'score-code-list-detail',
  templateUrl: './code-list-detail.component.html',
  styleUrls: ['./code-list-detail.component.css']
})
export class CodeListDetailComponent implements OnInit {

  title = 'Edit Code List';
  agencyIdListValues: SimpleAgencyIdListValue[];
  disabled: boolean;
  codeLists: CodeList[];
  agencyListFilterCtrl: FormControl = new FormControl();
  filteredAgencyLists: ReplaySubject<SimpleAgencyIdListValue[]> = new ReplaySubject<SimpleAgencyIdListValue[]>(1);

  codeList: CodeList;
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
    this.service.getCodeLists().subscribe(resp2 => this.codeLists = resp2.list);

    this.codeList = new CodeList();

    // load context scheme
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) =>
        this.service.getCodeList(params.get('id')))
    ).subscribe(resp => {
      resp.codeListValues.forEach((codeListValue: CodeListValue) => {
        codeListValue.guid = uuid();
      });

      this.hashCode = hashCode(resp);
      this.codeList = resp;

      this._updateDataSource(this.codeList.codeListValues);
    });

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
      (codeList.agencyId === undefined || codeList.agencyId === 0) ||
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

  create() {
    this.router.navigateByUrl('/code_list/create/' + this.codeList.codeListId);
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
        if (_codeList.codeListId === codeList.codeListId) {
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

  checkNameUniqueness(_codeList: CodeList): boolean {
    const listUniqueness: number[] = [0];
    for (const codeList of this.codeLists) {
      if (_codeList.codeListName === codeList.codeListName && _codeList.codeListId !== codeList.codeListId) {
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
      'Are you sure you want to update the code list?'
    ];

    dialogConfig.data.action = 'Update anyway';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.doUpdate();
        }
      });
  }

  doUpdate() {
    this.disabled = true;

    this.service.update(this.codeList).subscribe(_ => {
      this.hashCode = hashCode(this.codeList);
      this.snackBar.open('Updated', '', {
        duration: 1000,
      });
      this.disabled = false;
    });
  }

  update() {
    if (this.checkUniqueness(this.codeList)) {
      this.alertInvalidParameters();
    } else if (this.checkNameUniqueness(this.codeList)) {
      this.alertDuplicatedProperties();
    } else {
      this.doUpdate();
    }
  }

  publish() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Update state to Published?';
    dialogConfig.data.content = [
      'Do you really want to move ' + this.codeList.codeListName + ' to the Published state?',
      'Once in the Published state it can no longer be changed or discarded.'
    ];

    dialogConfig.data.action = 'Update anyway';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.disabled = true;

          this.service.publish(this.codeList).subscribe(_ => {
            this.snackBar.open('Published', '', {
              duration: 1000,
            });
            this.disabled = false;

            this.router.navigateByUrl('/code_list');
          });
        }
      });
  }

  discard() {
    this.openDialogCodeListDiscard();
  }

  openDialogCodeListDiscard() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Discard Code List?';
    dialogConfig.data.content = [
      'Are you sure you want to discard the code list?',
      'The code list will be permanently removed.'
    ];

    dialogConfig.data.action = 'Discard';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.disabled = true;

          this.service.delete(this.codeList.codeListId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 1000,
            });
            this.disabled = false;

            this.router.navigateByUrl('/code_list');
          });
        }
      });
  }

}
