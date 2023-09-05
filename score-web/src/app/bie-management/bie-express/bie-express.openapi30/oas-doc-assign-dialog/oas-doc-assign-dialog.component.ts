import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {SimpleRelease} from '../../../../release-management/domain/release';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {AccountListService} from '../../../../account-management/domain/account-list.service';
import {ReleaseService} from '../../../../release-management/domain/release.service';
import {AuthService} from '../../../../authentication/auth.service';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSort, SortDirection} from '@angular/material/sort';
import {initFilter, loadBranch, saveBranch} from '../../../../common/utility';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../../../basis/basis';
import {finalize} from 'rxjs/operators';
import {AssignBieForOasDoc, BieForOasDoc, BieForOasDocListRequest, OasDoc} from '../domain/openapi-doc';
import {OpenAPIService} from '../domain/openapi.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'score-oas-doc-assign-dialog',
  templateUrl: './oas-doc-assign-dialog.component.html',
  styleUrls: ['./oas-doc-assign-dialog.component.css']
})
export class OasDocAssignDialogComponent implements OnInit {

  title = 'Add BIE';
  subtitle = 'Selected Top-Level ABIEs';

  displayedColumns: string[] = [
    'select', 'state', 'den', 'owner', 'version', 'verb', 'arrayIndicator', 'suppressRootIndicator', 'messageBody',
    'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BieForOasDoc>();
  selection = new SelectionModel<BieForOasDoc>(true, []);
  businessContextSelection = {};
  verbSelection = {};
  messageBodySelection = {};
  loading = false;
  oasDoc: OasDoc;
  loginIdList: string[] = [];
  releases: SimpleRelease[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BieForOasDocListRequest;
  assignBieForOasDoc: AssignBieForOasDoc;
  assignBieForOasDocList: AssignBieForOasDoc[];
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(
    private openAPIService: OpenAPIService,
    private accountService: AccountListService,
    private releaseService: ReleaseService,
    private auth: AuthService,
    private dialog: MatDialog,
    private location: Location,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<OasDocAssignDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
  }

  ngOnInit(): void {
    this.oasDoc = this.data.oasDoc;
    this.assignBieForOasDoc = new AssignBieForOasDoc();
    this.assignBieForOasDocList = [];
    // Init BIE list table for OasDoc
    this.request = new BieForOasDocListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.access = 'CanView';
    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.selectBieForOasDocList();
    });

    forkJoin([
      this.accountService.getAccountNames(),
      this.releaseService.getSimpleReleases()
    ]).subscribe(([loginIds, releases]) => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.releases = releases.filter(e => e.releaseNum !== 'Working' && e.state === 'Published');
      initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
      const savedReleaseId = loadBranch(this.auth.getUserToken(), 'BIE');
      if (savedReleaseId) {
        this.request.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
        if (!this.request.release) {
          this.request.release = this.releases[0];
          saveBranch(this.auth.getUserToken(), 'BIE', this.request.release.releaseId);
        }
      } else {
        this.request.release = this.releases[0];
      }

      this.selectBieForOasDocList(true);
    });
  }

  selectBieForOasDocList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.openAPIService.selectBieForOasDocListWithRequest(this.request, this.oasDoc).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: BieForOasDoc) => {
        elm = new BieForOasDoc(elm);
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });
      this.dataSource.data.forEach((elm: BieForOasDoc) => {
        this.businessContextSelection[elm.topLevelAsbiepId] = elm.businessContext;
        this.verbSelection[elm.topLevelAsbiepId] = elm.verb;
        this.messageBodySelection[elm.topLevelAsbiepId] = elm.messageBody;
      });
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
      this.businessContextSelection = {};
      this.verbSelection = {};
      this.messageBodySelection = {};
    });
  }

  onPageChange(event: PageEvent) {
    this.selectBieForOasDocList();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);
    }
  }

  onDateEvent(type: string, event: MatDatepickerInputEvent<Date>) {
    switch (type) {
      case 'startDate':
        this.request.updatedDate.start = new Date(event.value);
        break;
      case 'endDate':
        this.request.updatedDate.end = new Date(event.value);
        break;
    }
  }

  reset(type: string) {
    switch (type) {
      case 'startDate':
        this.request.updatedDate.start = null;
        break;
      case 'endDate':
        this.request.updatedDate.end = null;
        break;
    }
  }

  select(row: BieForOasDoc) {
    this.selection.select(row);
  }

  toggle(row: BieForOasDoc) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieForOasDoc) {
    return this.selection.isSelected(row);
  }

  addBieForOasDoc() {
    const selectedBieForOasDocs = this.selection.selected;
    for (const bieForOasDoc of selectedBieForOasDocs) {
      this.openAPIService.checkBIEreusedAcrossMultipleOperations(bieForOasDoc).subscribe(
        resp => {
          this.doAddBieForOasDoc(bieForOasDoc);
        }, _ => {
        });
    }
  }

  doAddBieForOasDoc(bieForOasDoc: BieForOasDoc) {
      this.assignBieForOasDoc.messageBody = this.messageBodySelection[bieForOasDoc.topLevelAsbiepId];
      this.assignBieForOasDoc.propertyTerm = bieForOasDoc.propertyTerm;
      this.assignBieForOasDoc.tagName = bieForOasDoc.propertyTerm;
      this.assignBieForOasDoc.topLevelAsbiepId = bieForOasDoc.topLevelAsbiepId;
      this.assignBieForOasDoc.verb = this.verbSelection[bieForOasDoc.topLevelAsbiepId];
      if (this.assignBieForOasDoc.messageBody === 'Request') {
        this.assignBieForOasDoc.oasRequest = true;
      } else if (this.assignBieForOasDoc.messageBody === 'Response') {
        this.assignBieForOasDoc.oasRequest = false;
      }
      this.assignBieForOasDoc.oasDocId = this.oasDoc.oasDocId;
      this.assignBieForOasDoc.arrayIndicator = bieForOasDoc.arrayIndicator;
      this.assignBieForOasDoc.suppressRootIndicator = bieForOasDoc.suppressRootIndicator;
      this.openAPIService.assignBieForOasDoc(this.assignBieForOasDoc).subscribe(resp => {
        this.snackBar.open('Added', '', {
          duration: 3000,
        });
        this.dialogRef.close({
          result: {
            status: 'OK'
          }
        });
      });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
