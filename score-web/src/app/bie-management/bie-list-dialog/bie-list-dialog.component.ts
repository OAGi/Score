import {Component, Inject, OnInit} from '@angular/core';
import {faRecycle} from '@fortawesome/free-solid-svg-icons';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatTableDataSource} from '@angular/material/table';
import {finalize} from 'rxjs/operators';
import {AuthService} from '../../authentication/auth.service';
import {base64Encode} from '../../common/utility';
import {ReuseReport} from '../bie-report/domain/bie-report';
import {BieReportService} from '../bie-report/domain/bie-report.service';

@Component({
  selector: 'score-bie-list-dialog',
  templateUrl: './bie-list-dialog.component.html',
  styleUrls: ['./bie-list-dialog.component.css']
})
export class BieListDialogComponent implements OnInit {

  faRecycle = faRecycle;
  displayedColumns: string[];
  dataSource = new MatTableDataSource<ReuseReport>();
  loading = false;

  constructor(public dialogRef: MatDialogRef<BieListDialogComponent>,
              private service: BieReportService,
              private auth: AuthService,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.displayedColumns = [
      'releaseNum',
      'reusingState',
      'reusingDen',
      'reusingOwner',
      'reusingVersion',
      'reusingStatus'
    ];

    if (data.showReusedBie) {
      this.displayedColumns = this.displayedColumns.concat([
        'arrow',
        'reusedState',
        'reusedDen',
        'reusedOwner',
        'reusedVersion',
        'reusedStatus'
      ]);
    } else {
      this.displayedColumns = this.displayedColumns.concat([
        'reusingRemark',
      ]);
    }
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
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
