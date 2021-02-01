import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {BusinessContextService} from '../domain/business-context.service';
import {BusinessContext, BusinessContextValue} from '../domain/business-context';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {BusinessContextValueDialogComponent} from '../business-context-value-dialog/business-context-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {v4 as uuid} from 'uuid';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'score-business-context-create',
  templateUrl: './business-context-create.component.html',
  styleUrls: ['./business-context-create.component.css']
})
export class BusinessContextCreateComponent implements OnInit {

  title = 'Create Business Context';
  disabled: boolean;

  businessContext: BusinessContext;
  displayedColumns: string[] = [
    'select', 'contextCategoryName', 'contextSchemeName', 'contextSchemeValue'
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
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit() {
    this.disabled = false;

    this.businessContext = new BusinessContext();

    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  isDisabled(businessContext: BusinessContext) {
    return (this.disabled) ||
      (businessContext.name === undefined || businessContext.name === '');
  }

  _updateDataSource(data: BusinessContextValue[]) {
    this.dataSource.data = data;
    this.businessContext.businessContextValueList = data;
  }

  openDialog(businessContextValue?: BusinessContextValue) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = new BusinessContextValue();

    if (businessContextValue) { // deep copy
      dialogConfig.data = JSON.parse(JSON.stringify(businessContextValue));
    }

    const isAddAction: boolean = (businessContextValue === undefined);

    this.disabled = true;
    const dialogRef = this.dialog.open(BusinessContextValueDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined && result.contextSchemeValueId !== undefined) {
        for (const value of this.dataSource.data) {
          if (value.contextSchemeValueId === result.contextSchemeValueId) {
            this.snackBar.open(result.contextSchemeValue + ' already exist', '', {
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
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove Business Context?';
    dialogConfig.data.content = ['Are you sure you want to remove the business context value?'];
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

  create() {
    this.service.create(this.businessContext).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/context_management/business_context');
    });
  }

}
