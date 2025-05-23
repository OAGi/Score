import {Component, HostListener, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {BusinessContextService} from '../domain/business-context.service';
import {BieListService} from '../../../bie-management/bie-list/domain/bie-list.service';
import {BusinessContextDetails, BusinessContextValue} from '../domain/business-context';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {BusinessContextValueDialogComponent} from '../business-context-value-dialog/business-context-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {v4 as uuid} from 'uuid';
import {switchMap} from 'rxjs/operators';
import {hashCode} from '../../../common/utility';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {
  PreferencesInfo,
  TableColumnsInfo,
  TableColumnsProperty
} from '../../../settings-management/settings-preferences/domain/preferences';
import {ScoreTableColumnResizeDirective} from '../../../common/score-table-column-resize/score-table-column-resize.directive';
import {SettingsPreferencesService} from '../../../settings-management/settings-preferences/domain/settings-preferences.service';
import {AuthService} from '../../../authentication/auth.service';
import {forkJoin} from 'rxjs';

@Component({
  selector: 'score-business-context-detail',
  templateUrl: './business-context-detail.component.html',
  styleUrls: ['./business-context-detail.component.css']
})
export class BusinessContextDetailComponent implements OnInit {

  title = 'Edit Business Context';
  disabled: boolean;
  hashCode;
  businessContext: BusinessContextDetails;
  preferencesInfo: PreferencesInfo;

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfBusinessContextValuePage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfBusinessContextValuePage = columns;
    this.updateTableColumnsForBusinessContextValuePage();
  }

  updateTableColumnsForBusinessContextValuePage() {
    this.preferencesService.updateTableColumnsForBusinessContextValuePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfBusinessContextValuePage;
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    const updatedColumnsWithWidth = updatedColumns.map(column => ({
      name: column.name,
      selected: column.selected,
      width: this.width(column.name)
    }));

    this.columns = updatedColumnsWithWidth;
  }

  onResizeWidth($event) {
    switch ($event.name) {
      default:
        this.setWidth($event.name, $event.width);
        break;
    }
  }

  setWidth(name: string, width: number | string) {
    const matched = this.columns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.updateTableColumnsForBusinessContextValuePage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    for (const column of this.columns) {
      switch (column.name) {
        case 'Context Category':
          if (column.selected) {
            displayedColumns.push('contextCategoryName');
          }
          break;
        case 'Context Scheme':
          if (column.selected) {
            displayedColumns.push('contextSchemeName');
          }
          break;
        case 'Context Scheme Value':
          if (column.selected) {
            displayedColumns.push('contextSchemeValue');
          }
          break;
      }
    }
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<BusinessContextValue>();
  selection = new SelectionModel<BusinessContextValue>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;

  constructor(private service: BusinessContextService,
              private bieListService: BieListService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService) {
  }

  ngOnInit() {
    this.disabled = false;
    this.businessContext = new BusinessContextDetails();
    this.businessContext.used = true;

    // load business context
    this.route.paramMap.pipe(
        switchMap((params: ParamMap) => forkJoin([
              this.service.getBusinessContextDetails(params.get('id')),
              this.service.getBusinessContextValues(params.get('id')),
              this.preferencesService.load(this.auth.getUserToken())
            ])
        )).subscribe(([businessContextDetails, businessContextValues, preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;

      if (!businessContextDetails) {
        this.snackBar.open('Access denied.', '', {
          duration: 3000
        });
        this.router.navigateByUrl('/context_management/business_context');
        return;
      }

      businessContextDetails.businessContextValues = businessContextValues;
      businessContextDetails.businessContextValues.forEach((businessContextValue: BusinessContextValue) => {
        businessContextValue.guid = uuid();
      });
      this.hashCode = hashCode(businessContextDetails);
      this.businessContext = businessContextDetails;

      this._updateDataSource(this.businessContext.businessContextValues);
    });

    // Prevent the sorting event from being triggered if any columns are currently resizing.
    const originalSort = this.sort.sort;
    this.sort.sort = (sortChange) => {
      if (this.tableColumnResizeDirectives &&
        this.tableColumnResizeDirectives.filter(e => e.resizing).length > 0) {
        return;
      }
      originalSort.apply(this.sort, [sortChange]);
    };
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  isChanged() {
    return this.hashCode !== hashCode(this.businessContext);
  }

  _updateDataSource(data: BusinessContextValue[]) {
    this.dataSource.data = data;
    this.businessContext.businessContextValues = data;
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
