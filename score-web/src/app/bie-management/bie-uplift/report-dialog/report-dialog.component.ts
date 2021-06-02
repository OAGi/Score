import {templateJitUrl} from '@angular/compiler';
import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatTableDataSource} from '@angular/material/table';
import {finalize} from 'rxjs/operators';
import {MatchInfo} from '../domain/bie-uplift';
import {BieUpliftService} from '../domain/bie-uplift.service';

@Component({
  selector: 'score-report-dialog',
  templateUrl: './report-dialog.component.html',
  styleUrls: ['./report-dialog.component.css']
})
export class ReportDialogComponent implements OnInit {

  dataSource = new MatTableDataSource<MatchInfo>();

  displayedColumns: string[] = [
    'sourceDisplayPath', 'targetDisplayPath', 'match', 'reuse', 'validCode'
  ];

  hideSystemMatched = true;
  matches: MatchInfo[];
  matchMap: Map<string, MatchInfo>;
  loading = false;

  constructor(
    public dialogRef: MatDialogRef<ReportDialogComponent>,
    public service: BieUpliftService,
    @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  ngOnInit() {
    this.loading = true;
    const topLevelAsbiepId = this.data.topLevelAsbiepId;
    const releaseId = this.data.releaseId;
    const targetAsccpManifestId = this.data.targetAsccpManifestId;
    this.matchMap = new Map<string, MatchInfo>();
    this.matches = this.data.matches;
    this.matches.forEach(m => this.matchMap.set(m.bieType + '-' + m.bieId, m));
    this.service.checkValidationMatches(topLevelAsbiepId, releaseId, targetAsccpManifestId, this.matches)
    .pipe(finalize(() => {
      this.loading = false;
    }))
    .subscribe(resp => {
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
      return row.match == 'Unmatched' || !!(row.reuse) || row.valid === false;
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
    csvContent += 'Source Path, Target Path, Matched, Reused, Code List Issue\n';
    csvContent += this.dataSource.data.map(e => {
          return e.sourceDisplayPath + ',' + e.targetDisplayPath + ',' + e.match + ',' + e.reuse + ',' + e.message;
      }).join('\n');
    window.open(encodeURI(csvContent));
  }
}
