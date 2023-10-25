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

@Component({
  selector: 'score-bie-report',
  templateUrl: './bie-report.component.html',
  styleUrls: ['./bie-report.component.css']
})
export class BieReportComponent implements OnInit {

  faRecycle = faRecycle;
  title = 'Reuse Report';

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

  constructor(private service: BieReportService,
              private auth: AuthService,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit() {
    this.loading = true;

    forkJoin([
      this.service.getBieReuseReport()
    ]).subscribe(([resp]) => {
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
