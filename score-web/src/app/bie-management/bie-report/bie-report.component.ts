import {Component, OnInit} from '@angular/core';
import {faRecycle} from '@fortawesome/free-solid-svg-icons';
import {AuthService} from '../../authentication/auth.service';
import {base64Encode} from '../../common/utility';
import {BieReportService} from './domain/bie-report.service';
import {MatTableDataSource} from '@angular/material/table';
import {ReuseReport} from './domain/bie-report';
import {saveAs} from 'file-saver';
import {forkJoin} from 'rxjs';
import {WebPageInfoService} from '../../basis/basis.service';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';

@Component({
  selector: 'score-bie-report',
  templateUrl: './bie-report.component.html',
  styleUrls: ['./bie-report.component.css']
})
export class BieReportComponent implements OnInit {

  faRecycle = faRecycle;
  title = 'Reuse Report';

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
            if (column.selected) {
              displayedColumns.push('reusedState');
            }
            break;
          case 'Reused DEN':
            if (column.selected) {
              displayedColumns.push('reusedDen');
            }
            break;
          case 'Reused Owner':
            if (column.selected) {
              displayedColumns.push('reusedOwner');
            }
            break;
          case 'Reused Version':
            if (column.selected) {
              displayedColumns.push('reusedVersion');
            }
            break;
          case 'Reused Status':
            if (column.selected) {
              displayedColumns.push('reusedStatus');
            }
            break;
          case 'Reused Remark':
            if (column.selected) {
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

  constructor(private service: BieReportService,
              private auth: AuthService,
              private preferencesService: SettingsPreferencesService,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit() {
    this.loading = true;

    forkJoin([
      this.service.getBieReuseReport(),
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([resp, preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;
      this.dataSource.data = resp;

      this.loading = false;
    }, error => {
      this.loading = false;
    });
  }

  isAccessibleLeft(report: ReuseReport): boolean {
    return this.auth.getUserToken().username === report.reusingOwner || report.reusingState !== 'WIP';
  }

  isAccessibleRight(report: ReuseReport): boolean {
    return this.auth.getUserToken().username === report.reusedOwner || report.reusedState !== 'WIP';
  }

  getRouteLink(report: ReuseReport): string {
    return '/profile_bie/' + report.reusingTopLevelAsbiepId + '?q=' + base64Encode(report.displayPath);
  }

  convertToCsv(): string {
    return [
      [ 'Release', 'State', 'Guid', 'PropertyTerm', 'Path', 'Owner', 'Version', 'Status',
        'ReusedState', 'ReusedGuid', 'ReusedPropertyTerm', 'ReusedOwner', 'ReusedVersion', 'ReusedStatus', ],
      ...this.dataSource.data.map(e => [
        e.releaseNum, e.reusingState, e.reusingGuid, e.reusingPropertyTerm,
        e.displayPath, e.reusingOwner, e.reusingVersion, e.reusingStatus,

        e.reusedState, e.reusedGuid, e.reusedPropertyTerm,
        e.reusedOwner, e.reusedVersion, e.reusedStatus, ])
    ].map(e => e.join(',')).join('\n');
  }

  download() {
    const blob = new Blob([this.convertToCsv()], {type: 'application/csv'});
    saveAs(blob, 'reuse-report.csv');
  }
}
