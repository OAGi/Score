import {ChangeDetectionStrategy, Component, HostListener, inject, OnInit, viewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {BusinessContextService} from '../domain/business-context.service';
import {BieListService} from '../../../bie-management/bie-list/domain/bie-list.service';
import {BusinessContextDetails, BusinessContextValue} from '../domain/business-context';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BusinessContextValueDialogComponent} from '../business-context-value-dialog/business-context-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {BUSINESS_CONTEXT_VALUE_TABLE} from '../domain/business-context-value.table';
import {createTableColumns, inMemoryLoad, ScoreTableViewComponent} from '../../../common/score-table';
import {v4 as uuid} from 'uuid';
import {switchMap} from 'rxjs/operators';
import {hashCode} from '../../../common/utility';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {forkJoin} from 'rxjs';
import {Title} from '@angular/platform-browser';
import {setAppTitleIfPresent} from '../../../common/app-title.strategy';

@Component({
  standalone: false,
  selector: 'score-business-context-detail',
  templateUrl: './business-context-detail.component.html',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class BusinessContextDetailComponent implements OnInit {
  private service = inject(BusinessContextService);
  private bieListService = inject(BieListService);
  private location = inject(Location);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);
  private titleService = inject(Title);


  title = 'Edit Business Context';
  disabled: boolean;
  hashCode;
  businessContext: BusinessContextDetails;

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
    this.businessContext = new BusinessContextDetails();
    this.businessContext.used = true;

    // load business context
    this.route.paramMap.pipe(
        switchMap((params: ParamMap) => forkJoin([
              this.service.getBusinessContextDetails(params.get('id')),
              this.service.getBusinessContextValues(params.get('id'))
            ])
        )).subscribe(([businessContextDetails, businessContextValues]) => {

      if (!businessContextDetails) {
        this.redirectToBusinessContextList();
        return;
      }

      businessContextDetails.businessContextValues = businessContextValues;
      businessContextDetails.businessContextValues.forEach((businessContextValue: BusinessContextValue) => {
        businessContextValue.guid = uuid();
      });
      this.hashCode = hashCode(businessContextDetails);
      this.businessContext = businessContextDetails;
      setAppTitleIfPresent(this.titleService, this.businessContext.name, 'Business Context');

      this._updateDataSource(this.businessContext.businessContextValues);
    }, err => {
      let errorMessage;
      if (err.status === 404) {
        this.redirectToBusinessContextList();
        return;
      } else if (err.status === 403) {
        errorMessage = 'You do not have access permission.';
      } else {
        errorMessage = 'Something\'s wrong.';
      }
      this.snackBar.open(errorMessage, '', {
        duration: 3000
      });
      this.router.navigateByUrl('/context_management/business_context');
    });

  }

  private redirectToBusinessContextList() {
    this.snackBar.open('The requested business context is unavailable.', '', {
      duration: 3000
    });
    this.router.navigateByUrl('/context_management/business_context');
  }

  isChanged() {
    return this.hashCode !== hashCode(this.businessContext);
  }

  _updateDataSource(data: BusinessContextValue[]) {
    this.values = data;
    this.businessContext.businessContextValues = data;
    this.view()?.reload();
  }

  isDisabled(businessContext: BusinessContextDetails) {
    return (this.disabled) ||
      (businessContext.name === undefined || businessContext.name === '');
  }

  openDialog(businessContextValue?: BusinessContextValue) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.panelClass = ['center-dialog'];
    dialogConfig.height = 'fit-content';
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

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

    // Handle 'Ctrl/Command+S'
    const metaOrCtrlKeyPressed = $event.metaKey || $event.ctrlKey;
    if (metaOrCtrlKeyPressed && charCode === 's') {
      $event.preventDefault();
      $event.stopPropagation();

      this.update();
    }
  }

  get updateDisabled(): boolean {
    return !this.isChanged() || this.isDisabled(this.businessContext);
  }

  update() {
    if (this.updateDisabled) {
      return;
    }

    this.service.update(this.businessContext.businessContextId, this.businessContext.name,
      this.businessContext.businessContextValues).subscribe(_ => {
      this.hashCode = hashCode(this.businessContext);
      setAppTitleIfPresent(this.titleService, this.businessContext.name, 'Business Context');
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    });
  }

  discard() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Business Context?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this business context?',
      'The business context will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(this.businessContext.businessContextId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.router.navigateByUrl('/context_management/business_context');
          });
        }
      });
  }

}
