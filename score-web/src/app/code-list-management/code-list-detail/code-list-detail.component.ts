import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {CodeListService} from '../domain/code-list.service';
import {CodeList, CodeListValue, SimpleAgencyIdListValue} from '../domain/code-list';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogConfig,
  MatPaginator,
  MatSnackBar,
  MatSort,
  MatTableDataSource
} from '@angular/material';
import {CodeListValueDialogComponent} from '../code-list-value-dialog/code-list-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {switchMap} from 'rxjs/operators';
import {Md5} from 'ts-md5';
import {v4 as uuid} from 'uuid';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';

@Component({
  selector: 'srt-code-list-detail',
  templateUrl: './code-list-detail.component.html',
  styleUrls: ['./code-list-detail.component.css']
})
export class CodeListDetailComponent implements OnInit {

  title = 'Code List Detail';
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

      this.hashCode = this._hashCode(resp);
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

  _hashCode(obj) {
    return Md5.hashStr(JSON.stringify(obj));
  }

  isChanged() {
    return this.hashCode !== this._hashCode(this.codeList);
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

        this.disabled = false;
      } else {
        this.disabled = false;
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

  openDialogCodeList() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {codeList: this.codeList};
    const dialogRef = this.dialog.open(DialogContentCodelistDialogDetailComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.update(this.codeList).subscribe(_ => {
          this.hashCode = this._hashCode(this.codeList);
          this.snackBar.open('Updated', '', {
            duration: 1000,
          });
        });
        this.router.navigateByUrl('/code_list');
      }
    });
  }

  update() {
    if (this.checkUniqueness(this.codeList)) {
      this.snackBar.open('Another code list with the triplet (ListID, AgencyID, Version) already exist', 'Close');
    } else if (this.checkNameUniqueness(this.codeList)) {
      this.openDialogCodeList();
    } else {
      this.service.update(this.codeList).subscribe(_ => {
        this.hashCode = this._hashCode(this.codeList);
        this.snackBar.open('Updated', '', {
          duration: 1000,
        });
      });
      this.router.navigateByUrl('/code_list');
    }
  }

  publish() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {codeList: this.codeList.codeListName};

    const dialogRef = this.dialog.open(DialogPublishCodelistDialogDetailComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.publish(this.codeList).subscribe(_ => {
          this.snackBar.open('Published', '', {
            duration: 1000,
          });
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
    const dialogRef = this.dialog.open(DialogDiscardCodeListDetailDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.delete(this.codeList.codeListId).subscribe(_ => {
          this.snackBar.open('Discarded', '', {
            duration: 1000,
          });
          this.router.navigateByUrl('/code_list');
        });
      }
    });
  }

}

@Component({
  selector: 'srt-dialog-content-code-list-dialog-detail',
  templateUrl: 'dialog-content-code-list-detail-dialog.html',
})
export class DialogContentCodelistDialogDetailComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}

@Component({
  selector: 'srt-dialog-discard-code-list-detail-dialog',
  templateUrl: 'dialog-discard-code-list-detail-dialog.html',
})
export class DialogDiscardCodeListDetailDialogComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}

@Component({
  selector: 'srt-dialog-publish-code-list-dialog-detail',
  templateUrl: 'dialog-publish-code-list-detail-dialog.html',
})
export class DialogPublishCodelistDialogDetailComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}
