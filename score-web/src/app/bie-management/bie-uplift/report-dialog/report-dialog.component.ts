import {ChangeDetectionStrategy, Component, inject, OnInit, viewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {finalize} from 'rxjs/operators';
import {MatchInfo} from '../domain/bie-uplift';
import {BieUpliftService} from '../domain/bie-uplift.service';
import {createTableColumns, defineTable, inMemoryLoad, ScoreTableViewComponent} from '../../../common/score-table';

@Component({
  standalone: false,
  selector: 'score-report-dialog',
  templateUrl: './report-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./report-dialog.component.css']
})
export class ReportDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<ReportDialogComponent>>(MatDialogRef);
  service = inject(BieUpliftService);
  data = inject(MAT_DIALOG_DATA);



  readonly columns = createTableColumns(defineTable({
    storageKey: 'TableColumns-BieUpliftReportPage',
    columns: [
      {id: 'ccType', label: 'Type', width: 80, sortable: true},
      {
        id: 'displayPath', label: 'Path', width: 0, sortable: true,
        header: `Source ${this.data.sourceReleaseNum} Path \u2192 Target ${this.data.targetReleaseNum} Path`
      },
      {id: 'context', label: 'Context Definition', width: '40%', sortable: true},
      {id: 'match', label: 'Matched', width: '5%', sortable: true},
      {id: 'reuse', label: 'Reused', width: '5%', sortable: true},
      {id: 'validCode', label: 'Issue', width: '10%', sortable: true},
    ],
  }));
  readonly view = viewChild.required<ScoreTableViewComponent<MatchInfo>>(ScoreTableViewComponent);

  /** The rows of the report that are shown (all, or the ones with issues). */
  rows: MatchInfo[] = [];
  readonly load = inMemoryLoad<MatchInfo>(() => this.rows, {
    displayPath: row => row.sourceDisplayPath,
    validCode: row => [row.status, row.message].filter(value => !!value).join(' ')
  });
  hideSystemMatched = true;
  matches: MatchInfo[];
  matchMap: Map<string, MatchInfo>;
  loading = false;

  ngOnInit() {
    this.loading = true;
    const {topLevelAsbiepId, releaseId, targetAsccpManifestId} = this.data;
    this.matchMap = new Map<string, MatchInfo>();
    this.matches = this.data.matches;
    this.matches.forEach(m => this.matchMap.set(this.validationKey(m), m));

    this.service.checkValidationMatches(topLevelAsbiepId, releaseId, targetAsccpManifestId, this.matches).pipe(finalize(() => {
      this.loading = false;
    })).subscribe(resp => {

      resp.validations.forEach(v => {
        const match = this.matchMap.get(this.validationKey(v));
        if (!match) {
          return;
        }
        match.valid = v.valid;
        match.message = v.message ? v.message : '';
        match.status = v.status ? v.status : '';
      });
      this.setRows(this.matches.filter(r => this.show(r)));
    }, () => {
      this.matches.forEach(match => {
        match.valid = false;
        match.message = 'Unable to validate this mapping.';
      });
      this.setRows(this.matches);
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
    this.setRows(this.matches.filter(r => this.show(r)));
  }

  private setRows(rows: MatchInfo[]) {
    this.rows = rows;
    this.view()?.reload();
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
    ]), ...this.rows.map(e => {
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
