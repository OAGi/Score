import {Component, OnInit, ViewChild} from '@angular/core';
import {BusinessContext} from '../../../../context-management/business-context/domain/business-context';
import {Release} from '../../../bie-create/domain/bie-create-list';
import {MatTableDataSource} from '@angular/material/table';
import {CcList} from '../../../../cc-management/cc-list/domain/cc-list';
import {SelectionModel} from '@angular/cdk/collections';
import {BieListForOasDoc, BieListForOasDocRequest} from '../domain/openapi-doc';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {WorkingRelease} from '../../../../release-management/domain/release';
import {MatSort} from '@angular/material/sort';
import {MatPaginator} from '@angular/material/paginator';
import {BusinessContextService} from '../../../../context-management/business-context/domain/business-context.service';
import {ReleaseService} from '../../../../release-management/domain/release.service';
import {AccountListService} from '../../../../account-management/domain/account-list.service';
import {AuthService} from '../../../../authentication/auth.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'score-oas-doc-create',
  templateUrl: './oas-doc-create.component.html',
  styleUrls: ['./oas-doc-create.component.css']
})
export class OasDocCreateComponent implements OnInit {
  title = 'Create Open API Doc';
  subtitle = 'Open API Doc Metadata';
  oasDoc;
  OasDoc;

  businessContextIdList: number[] = [];
  businessContextList: BusinessContext[] = [];
  releaseId: number;
  releases: Release[] = [];
  displayedColumns: string[] = [
    'select', 'type', 'state', 'den', 'revision', 'owner', 'module', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BieListForOasDoc>();
  selection = new SelectionModel<CcList>(false, []);

  loading = false;

  loginIdList: string[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<Release[]> = new ReplaySubject<Release[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: BieListForOasDocRequest;

  workingRelease = WorkingRelease;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private bizCtxService: BusinessContextService,
              private releaseService: ReleaseService,
              private accountService: AccountListService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
  }

}
