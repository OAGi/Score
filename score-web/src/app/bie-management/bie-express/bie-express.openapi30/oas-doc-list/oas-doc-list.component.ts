import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {BusinessTerm, BusinessTermListRequest} from '../../../../business-term-management/domain/business-term';
import {SelectionModel} from '@angular/cdk/collections';
import {OasDoc, OasDocListRequest} from '../domain/openapi-doc';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {MatSort} from '@angular/material/sort';
import {MatPaginator} from '@angular/material/paginator';
import {AccountListService} from '../../../../account-management/domain/account-list.service';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmDialogService} from '../../../../common/confirm-dialog/confirm-dialog.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {OpenAPIService} from '../domain/openapi.service';

@Component({
  selector: 'score-oas-doc-list',
  templateUrl: './oas-doc-list.component.html',
  styleUrls: ['./oas-doc-list.component.css']
})
export class OasDocListComponent implements OnInit {

  title = 'Open API Doc';
  displayedColumns: string[] = [
    'select', 'title', 'openAPIVersion', 'description', 'version', 'licenseName',
    'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<OasDoc>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: OasDocListRequest;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private openAPIService: OpenAPIService,
              private accountService: AccountListService,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) { }

  ngOnInit(): void {
  }

}
