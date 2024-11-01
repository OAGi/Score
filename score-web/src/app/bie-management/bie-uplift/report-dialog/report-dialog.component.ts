import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatTableDataSource} from '@angular/material/table';
import {finalize} from 'rxjs/operators';
import {MatchInfo} from '../domain/bie-uplift';
import {BieUpliftService} from '../domain/bie-uplift.service';
import {
  PreferencesInfo,
  TableColumnsInfo,
  TableColumnsProperty
} from '../../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../../settings-management/settings-preferences/domain/settings-preferences.service';
import {AuthService} from '../../../authentication/auth.service';
import {forkJoin} from 'rxjs';

@Component({
  selector: 'score-report-dialog',
  templateUrl: './report-dialog.component.html',
  styleUrls: ['./report-dialog.component.css']
})
export class ReportDialogComponent implements OnInit {

  dataSource = new MatTableDataSource<MatchInfo>();

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfBieUpliftReportPage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfBieUpliftReportPage = columns;
    this.updateTableColumnsForBieUpliftReportPage();
  }

  updateTableColumnsForBieUpliftReportPage() {
    this.preferencesService.updateTableColumnsForBieUpliftReportPage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfBieUpliftReportPage;
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
      this.updateTableColumnsForBieUpliftReportPage();
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
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    for (const column of this.columns) {
      switch (column.name) {
        case 'Type':
          if (column.selected) {
            displayedColumns.push('ccType');
          }
          break;
        case 'Path':
          if (column.selected) {
            displayedColumns.push('displayPath');
          }
          break;
        case 'Context Definition':
          if (column.selected) {
            displayedColumns.push('context');
          }
          break;
        case 'Matched':
          if (column.selected) {
            displayedColumns.push('match');
          }
          break;
        case 'Reused':
          if (column.selected) {
            displayedColumns.push('reuse');
          }
          break;
        case 'Issue':
          if (column.selected) {
            displayedColumns.push('validCode');
          }
          break;
      }
    }
    return displayedColumns;
  }

  hideSystemMatched = true;
  matches: MatchInfo[];
  matchMap: Map<string, MatchInfo>;
  preferencesInfo: PreferencesInfo;
  downloadHeader: string;
  loading = false;

  constructor(
    public dialogRef: MatDialogRef<ReportDialogComponent>,
    public service: BieUpliftService,
    private auth: AuthService,
    private preferencesService: SettingsPreferencesService,
    @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  ngOnInit() {
    this.loading = true;
    const {topLevelAsbiepId, releaseId, targetAsccpManifestId, sourceReleaseNum, targetReleaseNum} = this.data;
    this.downloadHeader = `Source ${sourceReleaseNum} Path, Source Context Definition, Target ${targetReleaseNum} Path, Type, Matched, Reused, Issue\n`;
    this.matchMap = new Map<string, MatchInfo>();
    this.matches = this.data.matches;
    this.matches.forEach(m => this.matchMap.set(m.bieType + '-' + m.bieId, m));

    forkJoin([
      this.service.checkValidationMatches(topLevelAsbiepId, releaseId, targetAsccpManifestId, this.matches),
      this.preferencesService.load(this.auth.getUserToken())
    ]).pipe(finalize(() => {
      this.loading = false;
    })).subscribe(([resp, preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;

      resp.validations.forEach(v => {
        this.matchMap.get(v.bieType + '-' + v.bieId).valid = v.valid;
        this.matchMap.get(v.bieType + '-' + v.bieId).message = v.message ? v.message : '';
      });
      this.dataSource.data = this.matches.filter(r => this.show(r));
    });
  }

  show(row: MatchInfo): boolean {
    if (this.hideSystemMatched) {
      if (row.message !== '') {
        return true;
      }
      return row.match === 'Unmatched' || !!(row.reuse) || row.valid === false;
    }
    return true;
  }

  onToggleHide() {
    this.dataSource.data = this.matches.filter(r => this.show(r));
  }

  onClose(): void {
    this.dialogRef.close(false);
  }

  onUplift(): void {
    this.dialogRef.close(true);
  }

  onDownload(): void {
    let csvContent = 'data:text/csv;charset=utf-8,';
    csvContent += this.downloadHeader;
    csvContent += this.dataSource.data.map(e => {
      return [e.sourceDisplayPath, '"' + e.context + '"', e.targetDisplayPath, e.ccType, e.match, e.reuse, e.message].map(e => {
        return (!!e) ? e : '""';
      }).join(',');
    }).join('\n');
    // window.open(encodeURI(csvContent));

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.style.visibility = 'hidden';
    link.setAttribute('href', encodedUri);

    link.setAttribute('download', `UpliftReport-${this.data.name}-${this.data.guid}.csv`);
    document.body.appendChild(link); // Required for FF
    link.click();
    document.body.removeChild(link);
  }
}
