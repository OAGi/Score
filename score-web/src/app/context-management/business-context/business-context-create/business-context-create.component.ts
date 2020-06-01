import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {BusinessContextService} from '../domain/business-context.service';
import {BusinessContext, BusinessContextValue} from '../domain/business-context';
import {MatDialog, MatDialogConfig, MatPaginator, MatSnackBar, MatSort, MatTableDataSource} from '@angular/material';
import {BusinessContextValueDialogComponent} from '../business-context-value-dialog/business-context-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {v4 as uuid} from 'uuid';
import {ConfirmDialogConfig} from '../../../common/confirm-dialog/confirm-dialog.domain';
import {ConfirmDialogComponent} from '../../../common/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'score-business-context-create',
  templateUrl: './business-context-create.component.html',
  styleUrls: ['./business-context-create.component.css']
})
export class BusinessContextCreateComponent implements OnInit {

  title = 'Create Business Context';
  disabled: boolean;

  bizCtx: BusinessContext;
  displayedColumns: string[] = [
    'select', 'ctxCategoryName', 'ctxSchemeName', 'ctxSchemeValue'
  ];
  dataSource = new MatTableDataSource<BusinessContextValue>();
  selection = new SelectionModel<BusinessContextValue>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: BusinessContextService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.disabled = false;

    this.bizCtx = new BusinessContext();

    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  isDisabled(bizCtx: BusinessContext) {
    return (this.disabled) ||
      (bizCtx.name === undefined || bizCtx.name === '');
  }

  _updateDataSource(data: BusinessContextValue[]) {
    this.dataSource.data = data;
    this.bizCtx.bizCtxValues = data;
  }

  openDialog(bizCtxValue?: BusinessContextValue) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = new BusinessContextValue();

    if (bizCtxValue) { // deep copy
      dialogConfig.data = JSON.parse(JSON.stringify(bizCtxValue));
    }

    const isAddAction: boolean = (bizCtxValue === undefined);

    this.disabled = true;
    const dialogRef = this.dialog.open(BusinessContextValueDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined && result.ctxSchemeValueId !== undefined) {
        for ( const value of this.dataSource.data) {
          if (value.ctxSchemeValueId === result.ctxSchemeValueId) {
            this.snackBar.open(result.ctxSchemeValue + ' already exist', '', {
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

  select(row: BusinessContextValue) {
    this.selection.select(row);
  }

  toggle(row: BusinessContextValue) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BusinessContextValue) {
    return this.selection.isSelected(row);
  }

  removeBizCtxValues() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Remove Business Context?';
    dialogConfig.data.content = 'Are you sure you want to remove the business context value?';
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
    this.service.create(this.bizCtx).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 1000,
      });
      this.router.navigateByUrl('/context_management/business_context');
    });
  }

}
