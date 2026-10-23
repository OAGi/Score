import {ChangeDetectionStrategy, Component, inject, OnInit, viewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {BusinessContextService} from '../domain/business-context.service';
import {BusinessContext, BusinessContextValue} from '../domain/business-context';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BusinessContextValueDialogComponent} from '../business-context-value-dialog/business-context-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {BUSINESS_CONTEXT_VALUE_TABLE} from '../domain/business-context-value.table';
import {createTableColumns, inMemoryLoad, ScoreTableViewComponent} from '../../../common/score-table';
import {v4 as uuid} from 'uuid';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';

@Component({
  standalone: false,
  selector: 'score-business-context-create',
  templateUrl: './business-context-create.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class BusinessContextCreateComponent implements OnInit {
  private service = inject(BusinessContextService);
  private location = inject(Location);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);


  title = 'Create Business Context';
  disabled: boolean;

  businessContext: BusinessContext;

  readonly columns = createTableColumns(BUSINESS_CONTEXT_VALUE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BusinessContextValue>>(ScoreTableViewComponent);
  /** The guids of the selected values. */
  selection = new SelectionModel<string>(true, []);

  /** The values that the table lists. */
  values: BusinessContextValue[] = [];
  readonly load = inMemoryLoad<BusinessContextValue>(() => this.values);
  readonly allRows = () => this.values;
  readonly rowKey = (row: BusinessContextValue) => row.guid;

  ngOnInit() {
    this.disabled = false;

    this.businessContext = new BusinessContext();

  }

  isDisabled(businessContext: BusinessContext) {
    return (this.disabled) ||
      (businessContext.name === undefined || businessContext.name === '');
  }

  _updateDataSource(data: BusinessContextValue[]) {
    this.values = data;
    this.businessContext.businessContextValueList = data;
    this.view()?.reload();
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
        for (const value of this.values) {
          if (value.contextSchemeValueId === result.contextSchemeValueId) {
            this.snackBar.open(result.contextSchemeValue + ' already exist', '', {
              duration: 3000,
            });
            this.disabled = false;
            return;
          }
        }
        if (isAddAction) {
          const data = this.values;
          result.guid = uuid();
          data.push(result);

          this._updateDataSource(data);
        } else {
          const newData = [];

          this.values.forEach(row => {
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

  removeBizCtxValues() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove Business Context?';
    dialogConfig.data.content = ['Are you sure you want to remove the business context value?'];
    dialogConfig.data.action = 'Remove';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          const newData = [];
          this.values.forEach(row => {
            if (!this.selection.isSelected(row.guid)) {
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
    this.service.create(this.businessContext.name, this.businessContext.businessContextValueList).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/context_management/business_context');
    });
  }

}
