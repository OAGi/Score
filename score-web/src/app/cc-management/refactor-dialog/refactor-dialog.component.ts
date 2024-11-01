import {Component, Inject, OnInit, QueryList, ViewChildren} from '@angular/core';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {CcList, CcListRequest} from '../cc-list/domain/cc-list';
import {ExtensionDetailComponent} from '../extension-detail/extension-detail.component';
import {RefactorDialogService} from './domain/refactor-dialog.service';
import {ActivatedRoute, Router} from '@angular/router';
import {WebPageInfoService} from '../../basis/basis.service';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {AuthService} from '../../authentication/auth.service';
import {forkJoin} from 'rxjs';

@Component({
  selector: 'score-based-acc-dialog',
  templateUrl: './refactor-dialog.component.html',
  styleUrls: ['./refactor-dialog.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0', display: 'none'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class RefactorDialogComponent implements OnInit {

  // ACC Table
  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfCoreComponentPage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfCoreComponentPage = columns;
    this.updateTableColumnsForCoreComponentPage();
  }

  updateTableColumnsForCoreComponentPage() {
    this.preferencesService.updateTableColumnsForCoreComponentPage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfCoreComponentPage;
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
      case 'Updated on':
        this.setWidth('Updated On', $event.width);
        break;

      default:
        this.setWidth($event.name, $event.width);
        break;
    }
  }

  setWidth(name: string, width: number | string) {
    const matched = this.columns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.updateTableColumnsForCoreComponentPage();
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
        case 'Type':
          if (column.selected) {
            displayedColumns.push('type');
          }
          break;
        case 'State':
          if (column.selected) {
            displayedColumns.push('state');
          }
          break;
        case 'DEN':
          if (column.selected) {
            displayedColumns.push('den');
          }
          break;
        case 'Revision':
          if (column.selected) {
            displayedColumns.push('revision');
          }
          break;
        case 'Owner':
          if (column.selected) {
            displayedColumns.push('owner');
          }
          break;
        case 'Module':
          if (column.selected) {
            displayedColumns.push('module');
          }
          break;
        case 'Updated On':
          if (column.selected) {
            displayedColumns.push('lastUpdateTimestamp');
          }
          break;
      }
    }
    return displayedColumns;
  }

  // Issue Table
  get issueColumns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfCoreComponentAccRefactorPage;
  }

  set issueColumns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfCoreComponentAccRefactorPage = columns;
    this.updateTableColumnsForCoreComponentAccRefactorPage();
  }

  updateTableColumnsForCoreComponentAccRefactorPage() {
    this.preferencesService.updateTableColumnsForCoreComponentAccRefactorPage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onIssueColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.issueColumns = defaultTableColumnInfo.columnsOfCoreComponentAccRefactorPage;
  }

  onIssueColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    const updatedColumnsWithWidth = updatedColumns.map(column => ({
      name: column.name,
      selected: column.selected,
      width: this.issueWidth(column.name)
    }));

    this.issueColumns = updatedColumnsWithWidth;
  }

  onResizeIssueWidth($event) {
    switch ($event.name) {
      case 'Updated on':
        this.setIssueWidth('Updated On', $event.width);
        break;

      default:
        this.setIssueWidth($event.name, $event.width);
        break;
    }
  }

  setIssueWidth(name: string, width: number | string) {
    const matched = this.issueColumns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.updateTableColumnsForCoreComponentAccRefactorPage();
    }
  }

  issueWidth(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.issueColumns.find(c => c.name === name)?.width;
  }

  get displayedIssueColumns(): string[] {
    let displayedColumns = [];
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    for (const column of this.issueColumns) {
      switch (column.name) {
        case 'Type':
          if (column.selected) {
            displayedColumns.push('type');
          }
          break;
        case 'State':
          if (column.selected) {
            displayedColumns.push('state');
          }
          break;
        case 'DEN':
          if (column.selected) {
            displayedColumns.push('den');
          }
          break;
        case 'Issue':
          if (column.selected) {
            displayedColumns.push('reason');
          }
          break;
        case 'Revision':
          if (column.selected) {
            displayedColumns.push('revision');
          }
          break;
        case 'Owner':
          if (column.selected) {
            displayedColumns.push('owner');
          }
          break;
        case 'Updated On':
          if (column.selected) {
            displayedColumns.push('lastUpdateTimestamp');
          }
          break;
      }
    }
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<CcList>();
  expandedElement: CcList | null;
  selection = new SelectionModel<CcList>(false, []);

  loading = false;
  isValid = false;

  issueDataSource = new MatTableDataSource<CcList>();

  request: CcListRequest;
  preferencesInfo: PreferencesInfo;
  highlightTextForDefinition: string;
  action: string;
  title: string;

  constructor(public dialogRef: MatDialogRef<ExtensionDetailComponent>,
              private service: RefactorDialogService,
              private accountService: AccountListService,
              private auth: AuthService,
              private router: Router,
              private route: ActivatedRoute,
              public webPageInfo: WebPageInfoService,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService) {
  }

  ngOnInit() {
    this.loading = true;

    this.title = this.data.title;

    forkJoin([
      this.service.baseAccList(this.data.accManifestId),
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([ccList, preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;

      this.dataSource.data = ccList;
      this.loading = false;
    });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  checkValidation(): void {
    if (this.selection.selected.length === 0) {
      return;
    }
    const destinationManifestId = this.selection.selected[0].manifestId;

    this.service.validateRefactoring(this.data.type, this.data.targetManifestId, destinationManifestId)
      .subscribe(resp => {
        let valid = true;
        if (resp.issueList && resp.issueList.length > 0) {
          this.issueDataSource.data = resp.issueList;
          valid = resp.issueList.filter(e => e.reasons.length > 0).length === 0;
        } else {
          this.issueDataSource.data = [];
        }
        this.isValid = valid;
      });
  }

  doRefactor(): void {
    if (!this.isValid) {
      return;
    }
    if (this.selection.selected.length === 0) {
      return;
    }
    const destinationManifestId = this.selection.selected[0].manifestId;
    if (this.selection.selected[0].deprecated) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Confirmation required';
      dialogConfig.data.content = ['The selected component is deprecated', 'Are you sure you want to proceed with it?'];
      dialogConfig.data.action = 'Proceed anyway';

      this.confirmDialogService.open(dialogConfig).beforeClosed()
        .subscribe(result => {
          if (result) {
            this.service.refactor(this.data.type, this.data.targetManifestId, destinationManifestId)
              .subscribe(resp => {
                this.dialogRef.close(resp);
              });
          }
        });
    } else {
      this.service.refactor(this.data.type, this.data.targetManifestId, destinationManifestId)
        .subscribe(resp => {
          this.dialogRef.close(resp);
        });
    }
  }

  select(row: CcList) {
    this.selection.select(row);
    this.isValid = false;
  }

  toggle(row: CcList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CcList) {
    return this.selection.isSelected(row);
  }

  getRouterLink(ccList: CcList) {
    if (ccList.oagisComponentType === 'UserExtensionGroup') {
      return 'core_component/extension/' + ccList.manifestId;
    } else {
      return 'core_component/acc/' + ccList.manifestId;
    }
  }
}
