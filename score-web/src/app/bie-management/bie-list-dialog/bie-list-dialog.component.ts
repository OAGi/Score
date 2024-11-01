import {Component, Inject, OnInit} from '@angular/core';
import {faRecycle} from '@fortawesome/free-solid-svg-icons';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatTableDataSource} from '@angular/material/table';
import {finalize} from 'rxjs/operators';
import {AuthService} from '../../authentication/auth.service';
import {ReuseReport} from '../bie-report/domain/bie-report';
import {BieReportService} from '../bie-report/domain/bie-report.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {forkJoin} from 'rxjs';

@Component({
  selector: 'score-bie-list-dialog',
  templateUrl: './bie-list-dialog.component.html',
  styleUrls: ['./bie-list-dialog.component.css']
})
export class BieListDialogComponent implements OnInit {

  faRecycle = faRecycle;

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfBieReuseReportPage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfBieReuseReportPage = columns;
    this.updateTableColumnsForBieReuseReportPage();
  }

  updateTableColumnsForBieReuseReportPage() {
    this.preferencesService.updateTableColumnsForBieReuseReportPage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfBieReuseReportPage;
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
      this.updateTableColumnsForBieReuseReportPage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  get displayedColumns(): string[] {
    let displayedColumns = [];
    if (this.preferencesInfo) {
      for (const column of this.columns) {
        switch (column.name) {
          case 'Release':
            if (column.selected) {
              displayedColumns.push('releaseNum');
            }
            break;
          case 'Reusing State':
            if (column.selected) {
              displayedColumns.push('reusingState');
            }
            break;
          case 'Reusing DEN':
            if (column.selected) {
              displayedColumns.push('reusingDen');
            }
            break;
          case 'Reusing Owner':
            if (column.selected) {
              displayedColumns.push('reusingOwner');
            }
            break;
          case 'Reusing Version':
            if (column.selected) {
              displayedColumns.push('reusingVersion');
            }
            break;
          case 'Reusing Status':
            if (column.selected) {
              displayedColumns.push('reusingStatus');
            }
            break;
          case 'Reusing Remark':
            if (column.selected) {
              displayedColumns.push('reusingRemark');
            }
            break;
          case 'Reused State':
            if (this.data.showReusedBie && column.selected) {
              displayedColumns.push('reusedState');
            }
            break;
          case 'Reused DEN':
            if (this.data.showReusedBie && column.selected) {
              displayedColumns.push('reusedDen');
            }
            break;
          case 'Reused Owner':
            if (this.data.showReusedBie && column.selected) {
              displayedColumns.push('reusedOwner');
            }
            break;
          case 'Reused Version':
            if (this.data.showReusedBie && column.selected) {
              displayedColumns.push('reusedVersion');
            }
            break;
          case 'Reused Status':
            if (this.data.showReusedBie && column.selected) {
              displayedColumns.push('reusedStatus');
            }
            break;
          case 'Reused Remark':
            if (this.data.showReusedBie && column.selected) {
              displayedColumns.push('reusedRemark');
            }
            break;
        }
      }
    }
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<ReuseReport>();
  preferencesInfo: PreferencesInfo;
  loading = false;

  constructor(public dialogRef: MatDialogRef<BieListDialogComponent>,
              private service: BieReportService,
              private auth: AuthService,
              public webPageInfo: WebPageInfoService,
              private preferencesService: SettingsPreferencesService,
              @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
    forkJoin([
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;

      this.loadBieReportList();
    });
  }

  loadBieReportList() {
    this.loading = true;

    this.service.getBieReuseReport(this.data.topLevelAsbiepId).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      const excludeTopLevelAsbiepIdList = this.data.excludeTopLevelAsbiepIdList || [];
      this.dataSource.data = resp.filter(e => !excludeTopLevelAsbiepIdList.includes(e.reusingTopLevelAsbiepId));
    });
  }

  isAccessibleLeft(report: ReuseReport): boolean {
    return this.auth.getUserToken().username === report.reusingOwner || report.reusingState !== 'WIP';
  }

  isAccessibleRight(report: ReuseReport): boolean {
    return this.auth.getUserToken().username === report.reusedOwner || report.reusedState !== 'WIP';
  }

  getRouteLink(report: ReuseReport): string {
    return '/profile_bie/' + report.reusingTopLevelAsbiepId + report.displayPath;
  }
}
