import {Component, Inject, OnInit} from '@angular/core';
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

  displayedColumns: string[] = [
    'releaseNum',
    'reusingState',
    'reusingPropertyTerm',
    'reusingOwner',
    'reusingVersion',
    'reusingStatus',
    'arrow',
    'reusedState',
    'reusedPropertyTerm',
    'reusedOwner',
    'reusedVersion',
    'reusedStatus'
  ];
  dataSource = new MatTableDataSource<ReuseReport>();
  loading = false;

  constructor(public dialogRef: MatDialogRef<BieListDialogComponent>,
              private service: BieReportService,
              private auth: AuthService,
              @Inject(MAT_DIALOG_DATA) public data: any) {
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
      this.dataSource.data = resp;
    })
  }

  isAccessibleLeft(report: ReuseReport): boolean {
    return this.auth.getUserToken().username === report.reusingOwner || report.reusingState !== 'WIP';
  }

  isAccessibleRight(report: ReuseReport): boolean {
    return this.auth.getUserToken().username === report.reusedOwner || report.reusedState !== 'WIP';
  }

  getRouteLink(report: ReuseReport): string {
    return "/profile_bie/" + report.reusingTopLevelAsbiepId + "?q=" + base64Encode(report.displayPath);
  }
}
