import {ChangeDetectionStrategy, Component, inject, OnInit, viewChild} from '@angular/core';
import {faRecycle} from '@fortawesome/free-solid-svg-icons';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {finalize} from 'rxjs/operators';
import {AuthService} from '../../authentication/auth.service';
import {ReuseReport} from '../bie-report/domain/bie-report';
import {BieReportService} from '../bie-report/domain/bie-report.service';
import {BIE_REUSE_REPORT_TABLE} from '../bie-report/bie-report.table';
import {WebPageInfoService} from '../../basis/basis.service';
import {createTableColumns, inMemoryLoad, ScoreTableViewComponent} from '../../common/score-table';

const REUSED_COLUMNS = ['reusedState', 'reusedDen', 'reusedOwner', 'reusedVersion', 'reusedStatus', 'reusedRemark'];

@Component({
  standalone: false,
  selector: 'score-bie-list-dialog',
  templateUrl: './bie-list-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./bie-list-dialog.component.css']
})
export class BieListDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<BieListDialogComponent>>(MatDialogRef);
  private service = inject(BieReportService);
  private auth = inject(AuthService);
  webPageInfo = inject(WebPageInfoService);
  data = inject(MAT_DIALOG_DATA);

  faRecycle = faRecycle;

  readonly columns = createTableColumns(BIE_REUSE_REPORT_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<ReuseReport>>(ScoreTableViewComponent);
  /** The BIEs that reuse the BIE. */
  rows: ReuseReport[] = [];
  readonly load = inMemoryLoad<ReuseReport>(() => this.rows);
  /** The reused BIE is the one of the dialog unless the dialog says to show it. */
  hiddenColumns: string[] = REUSED_COLUMNS;
  loading = false;

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
    this.hiddenColumns = this.data.showReusedBie ? [] : REUSED_COLUMNS;
    this.loadBieReportList();
  }

  loadBieReportList() {
    this.loading = true;

    this.service.getBieReuseReport(this.data.topLevelAsbiepId).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      const excludeTopLevelAsbiepIdList = this.data.excludeTopLevelAsbiepIdList || [];
      this.rows = resp.filter(e => !excludeTopLevelAsbiepIdList.includes(e.reusingTopLevelAsbiepId));
      this.view().reload();
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
