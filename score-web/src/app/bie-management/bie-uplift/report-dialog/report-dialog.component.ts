import { Component, OnInit, inject } from '@angular/core';
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
  standalone: false,
  selector: 'score-report-dialog',
  templateUrl: './report-dialog.component.html',
  styleUrls: ['./report-dialog.component.css']
})
export class ReportDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<ReportDialogComponent>>(MatDialogRef);
  service = inject(BieUpliftService);
  private auth = inject(AuthService);
  private preferencesService = inject(SettingsPreferencesService);
  data = inject(MAT_DIALOG_DATA);


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
  loading = false;

  ngOnInit() {
    this.loading = true;
    const {topLevelAsbiepId, releaseId, targetAsccpManifestId} = this.data;
    this.matchMap = new Map<string, MatchInfo>();
    this.matches = this.data.matches;
    this.matches.forEach(m => this.matchMap.set(this.validationKey(m), m));

    forkJoin([
      this.service.checkValidationMatches(topLevelAsbiepId, releaseId, targetAsccpManifestId, this.matches),
      this.preferencesService.load(this.auth.getUserToken())
    ]).pipe(finalize(() => {
      this.loading = false;
    })).subscribe(([resp, preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;

      resp.validations.forEach(v => {
        const match = this.matchMap.get(this.validationKey(v));
        if (!match) {
          return;
        }
        match.valid = v.valid;
        match.message = v.message ? v.message : '';
        match.status = v.status ? v.status : '';
      });
      this.dataSource.data = this.matches.filter(r => this.show(r));
    }, () => {
      this.matches.forEach(match => {
        match.valid = false;
        match.message = 'Unable to validate this mapping.';
      });
      this.dataSource.data = this.matches;
    });
  }

  private validationKey(value: {bieType: string; bieId: number; sourcePath?: string}): string {
    return value.bieType + '-' + (value.sourcePath || value.bieId);
  }

  show(row: MatchInfo): boolean {
    if (this.hideSystemMatched) {
      if (row.message !== '' || row.status !== '') {
        return true;
      }
      // "Issues Only" hides clean system matches, but manual mappings are
      // user decisions and must remain visible in the uplift report.
      return row.match !== 'System' || !!(row.reuse) || row.valid === false;
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
    const csvContent = [this.toCsvRow([
      `Source ${this.data.sourceReleaseNum} Path`,
      'Source Context Definition',
      `Target ${this.data.targetReleaseNum} Path`,
      'Type', 'Matched', 'Reused', 'Issue'
    ]), ...this.dataSource.data.map(e => {
      const status = [e.status, e.message].filter(value => !!value).join(' ');
      return this.toCsvRow([
        e.sourceDisplayPath, e.context, e.targetDisplayPath,
        e.ccType, e.match, e.reuse, status
      ]);
    })].join('\n');

    const blob = new Blob([csvContent], {type: 'text/csv;charset=utf-8'});
    const objectUrl = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.style.visibility = 'hidden';
    link.setAttribute('href', objectUrl);

    link.setAttribute('download', `UpliftReport-${this.data.name}-${this.data.guid}.csv`);
    document.body.appendChild(link); // Required for FF
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(objectUrl);
  }

  private toCsvRow(values: unknown[]): string {
    return values.map(value => {
      const text = value === null || value === undefined ? '' : String(value);
      return `"${text.replace(/"/g, '""')}"`;
    }).join(',');
  }
}
