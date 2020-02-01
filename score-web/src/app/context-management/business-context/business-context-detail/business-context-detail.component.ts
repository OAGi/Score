import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {BusinessContextService} from '../domain/business-context.service';
import {BieListService} from '../../../bie-management/bie-list/domain/bie-list.service';
import {BusinessContext, BusinessContextValue} from '../domain/business-context';
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig, MatPaginator, MatSnackBar, MatSort, MatTableDataSource} from '@angular/material';
import {BusinessContextValueDialogComponent} from '../business-context-value-dialog/business-context-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {v4 as uuid} from 'uuid';
import {switchMap} from 'rxjs/operators';
import {hashCode} from '../../../common/utility';

@Component({
  selector: 'srt-business-context-detail',
  templateUrl: './business-context-detail.component.html',
  styleUrls: ['./business-context-detail.component.css']
})
export class BusinessContextDetailComponent implements OnInit {

  title = 'Business Context Detail';
  disabled: boolean;
  hashCode;
  bizCtx: BusinessContext;
  listDisplayed;
  listSize;
  displayedColumns: string[] = [
    'select', 'ctxCategoryName', 'ctxSchemeName', 'ctxSchemeValue'
  ];
  dataSource = new MatTableDataSource<BusinessContextValue>();
  selection = new SelectionModel<BusinessContextValue>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: BusinessContextService,
              private bieListService: BieListService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.disabled = false;
    this.bizCtx = new BusinessContext();

    // load business context
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) =>
        this.service.getBusinessContext(params.get('id')))
    ).subscribe(resp => {
      resp.bizCtxValues.forEach((bizCtxValue: BusinessContextValue) => {
        bizCtxValue.guid = uuid();
      });
      this.hashCode = hashCode(resp);
      this.bizCtx = resp;

      this._updateDataSource(this.bizCtx.bizCtxValues);
    });
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  isChanged() {
    return this.hashCode !== hashCode(this.bizCtx);
  }

  _updateDataSource(data: BusinessContextValue[]) {
    this.dataSource.data = data;
    this.bizCtx.bizCtxValues = data;
  }

  isDisabled(bizCtx: BusinessContext) {
    return (this.disabled) ||
      (bizCtx.name === undefined || bizCtx.name === '');
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

  removeBizCtxValues() {
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

  update() {
    this.service.update(this.bizCtx).subscribe(_ => {
      this.hashCode = hashCode(this.bizCtx);
      this.snackBar.open('Updated', '', {
        duration: 1000,
      });
    });
  }

  openDialogBizContext() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {list: this.listDisplayed, size: this.listSize};
    const dialogRef = this.dialog.open(DialogContentBizContextDialogDetailComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(result => {
    });
  }

  openDialogContextCategoryDiscard() {
    const dialogConfig = new MatDialogConfig();
    const dialogRef = this.dialog.open(DialogContentBizContextDialogDiscardComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.delete(this.bizCtx.bizCtxId).subscribe(_ => {
          this.snackBar.open('Discarded', '', {
            duration: 1000,
          });
          this.router.navigateByUrl('/context_management/business_context');
        });
      }
    });
  }

  discard() {
    this.bieListService.getBieListByBizCtxId(this.bizCtx.bizCtxId).subscribe(value => {
      if (value.length > 0) {
        const displayedList = [];
        value.forEach(el => {
          displayedList.push(el.guid);
        });
        this.listSize = value.length;
        this.listDisplayed = displayedList;
        this.openDialogBizContext();
      } else {
        this.openDialogContextCategoryDiscard();
      }
    });
  }

}

@Component({
  selector: 'srt-dialog-content-biz-context-dialog-detail',
  templateUrl: 'dialog-content-biz-context-detail-dialog.html',
})
export class DialogContentBizContextDialogDetailComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}


@Component({
  selector: 'srt-dialog-content-biz-context-dialog-discard',
  templateUrl: 'dialog-content-biz-context-discard-dialog.html',
})
export class DialogContentBizContextDialogDiscardComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}
