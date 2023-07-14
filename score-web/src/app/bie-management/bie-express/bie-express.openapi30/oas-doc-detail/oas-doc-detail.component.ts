import {Component, OnInit, ViewChild} from '@angular/core';
import {
  BieForOasDoc,
  BieForOasDocListRequest,
  BieForOasDocUpdateRequest,
  OasDoc,
  simpleOasDoc
} from '../domain/openapi-doc';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {BusinessContextService} from '../../../../context-management/business-context/domain/business-context.service';
import {OpenAPIService} from '../domain/openapi.service';
import {AccountListService} from '../../../../account-management/domain/account-list.service';
import {AuthService} from '../../../../authentication/auth.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmDialogService} from '../../../../common/confirm-dialog/confirm-dialog.service';
import {forkJoin} from 'rxjs';
import {hashCode, saveBranch} from 'src/app/common/utility';
import {v4 as uuid} from 'uuid';
import {saveAs} from 'file-saver';
import {BusinessContext} from '../../../../context-management/business-context/domain/business-context';
import {WorkingRelease} from '../../../../release-management/domain/release';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {PageRequest} from '../../../../basis/basis';
import {finalize} from 'rxjs/operators';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {OasDocAssignDialogComponent} from '../oas-doc-assign-dialog/oas-doc-assign-dialog.component';
import {BieExpressOption} from '../../domain/generate-expression';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';

@Component({
  selector: 'score-oas-doc-detail',
  templateUrl: './oas-doc-detail.component.html',
  styleUrls: ['./oas-doc-detail.component.css']
})
export class OasDocDetailComponent implements OnInit {
  title = 'Edit Open API Document';
  oasDocs: simpleOasDoc[];
  oasDoc: OasDoc;
  workingRelease = WorkingRelease;
  businessContextIdList: number[] = [];
  businessContextList: BusinessContext[] = [];
  hashCodeForOasDoc;
  bizCtxSearch: string;
  disabled: boolean;
  displayedColumns: string[] = [
    'select', 'den', 'owner', 'verb', 'arrayIndicator', 'suppressRoot', 'messageBody',
    'resourceName', 'operationId', 'tagName', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BieForOasDoc>();
  selection = new SelectionModel<number>(true, []);
  businessContextSelection = {};
  request: BieForOasDocListRequest;
  loading = false;
  isUpdating: boolean;
  option: BieExpressOption;
  openApiFormats: string[] = ['YAML', 'JSON'];
  topLevelAsbiepIds: number[];
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private bizCtxService: BusinessContextService,
              private openAPIService: OpenAPIService,
              private accountService: AccountListService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService,
              private dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.topLevelAsbiepIds = [];
    this.option = new BieExpressOption();
    this.option.bieDefinition = true;
    this.option.packageOption = 'ALL';
    // Default Open API expression format is 'YAML'.
    this.option.openAPIExpressionFormat = 'YAML';
    this.oasDoc = new OasDoc();

    const oasDocId = this.route.snapshot.params.id;

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
      this.loadBieListForOasDoc();
    });

    forkJoin([
      this.openAPIService.getOasDoc(oasDocId),
      this.openAPIService.getBieListForOasDoc(oasDocId)]
    ).subscribe(([simpleOasDoc, bieForOasDoc]) => {
      this.oasDoc = simpleOasDoc;
      this.init(this.oasDoc);
      this.loadBieListForOasDoc(true);
    }, _ => {
      this.isUpdating = false;
    });
  }

  init(oasDoc: OasDoc) {
    this.hashCodeForOasDoc = hashCode(oasDoc);
    this.oasDoc = oasDoc;
    this.isUpdating = false;
  }

  loadBieListForOasDoc(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.openAPIService.getBieForOasDocListWithRequest(this.request, this.oasDoc).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: BieForOasDoc) => {
        elm = new BieForOasDoc(elm);
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        elm.reset(); // reset the hashCode calculation when the bieForOasDoc is list and reloaded
        return elm;
      });
      this.dataSource.data.forEach((elm: BieForOasDoc) => {
        this.businessContextSelection[elm.topLevelAsbiepId] = elm.businessContexts[0];
      });

      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
      this.businessContextSelection = {};
    });
  }

  getChanged(): BieForOasDoc[] {
    if (!this.dataSource) {
      return [];
    }
    return this.dataSource.data.filter(e => e.isChanged);
  }

  isChanged(): boolean {
    return this.hashCodeForOasDoc !== hashCode(this.oasDoc) || this.getChanged().length > 0;
  }

  isDisabled(oasDoc1: OasDoc) {
    return (this.disabled) ||
      (oasDoc1.oasDocId === undefined || !oasDoc1.oasDocId) ||
      (oasDoc1.title === undefined || oasDoc1.title === '') ||
      (oasDoc1.openAPIVersion === undefined || oasDoc1.openAPIVersion === '') ||
      (oasDoc1.version === undefined || oasDoc1.version === '') ||
      (oasDoc1.licenseName === undefined || oasDoc1.licenseName === '');
  }

  back() {
    this.location.back();
  }

  onPageChange(event: PageEvent) {
    this.loadBieListForOasDoc();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);
    }
    if (property === 'filters.den') {
      this.sort.active = '';
      this.sort.direction = '';
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

  update() {
    this.checkUniqueness(this.oasDoc, (_) => {
      this.doUpdate();
    });
  }

  applyFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // MatTableDataSource defaults to lowercase matches
    this.dataSource.filter = filterValue;
  }

  clearFilter() {
    this.bizCtxSearch = '';
    this.applyFilter(this.bizCtxSearch);
  }

  get access(): string {
    if (this.oasDoc) {
      return this.oasDoc.access;
    }
    return '';
  }

  checkUniqueness(oasDoc: OasDoc, callbackFn?) {
    this.openAPIService.checkUniqueness(oasDoc).subscribe(resp => {
      if (!resp) {
        this.openDialogOasDocUpdate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  checkOasDocTitle(oasDoc: OasDoc, callbackFn?) {
    this.openAPIService.checkTitleUniqueness(oasDoc).subscribe(resp => {
      if (!resp) {
        this.openDialogOasDocUpdateIgnore();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  doUpdate() {
    this.openAPIService.updateOasDoc(this.oasDoc).subscribe(_ => {
      this.init(this.oasDoc);
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    });
  }

  openDialogOasDocUpdate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another Open API Doc with the same title, OpenAPI Version, Doc Version and License Name already exists!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {
    });
  }

  openDialogOasDocUpdateIgnore() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'The OpenAPI Doc already has a variable with the same properties';
    dialogConfig.data.content = [
      'Are you sure you want to update the OpenAPI Doc?'
    ];
    dialogConfig.data.action = 'Update anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.doUpdate();
        }
      });
  }

  discard() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard OpenAPI Doc?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this OpenAPI Doc?',
      'The OpenAPI Doc will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.openAPIService.delete(this.oasDoc.oasDocId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.router.navigateByUrl('/profile_bie/express/oas_doc');
          }, err => {
            console.log(err);
            this.snackBar.open('Discard\'s forbidden! The OpenAPI Doc is used.', '', {
              duration: 5000,
            });
          });
        }
      });
  }

  openDialog($event: any, bieForOasDoc?: BieForOasDoc) {
    $event.preventDefault();
    $event.stopPropagation();

    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = {};
    dialogConfig.data.oasDoc = this.oasDoc;
    // Default indicator values
    dialogConfig.data.isEditable = this.isEditable();

    const isAddAction: boolean = (bieForOasDoc === undefined);

    this.isUpdating = true;
    const dialogRef = this.dialog.open(OasDocAssignDialogComponent, dialogConfig);
    dialogRef.afterClosed().pipe(finalize(() => {
      this.isUpdating = false;
    })).subscribe(result => {
      if (!result) {
        return;
      }

      const data = this.dataSource.data;
      if (isAddAction) {
        for (const bieAdded of data) {
          if (bieAdded.propertyTerm === result.propertyTerm) {
            this.snackBar.open(result.propertyTerm + ' already exist', '', {
              duration: 3000,
            });

            return;
          }
        }

        result.guid = uuid();
        data.push(result);

        this._updateDataSource(data);
      } else {
        for (const bieAdded of data) {
          if (bieAdded.guid !== result.guid && bieAdded.propertyTerm === result.propertyTerm) {
            this.snackBar.open(result.value + ' already exist', '', {
              duration: 3000,
            });
            return;
          }
        }

        this._updateDataSource(data.map(row => {
          if (row.guid === result.guid) {
            return result;
          } else {
            return row;
          }
        }));
      }
    });
  }

  _updateDataSource(data: BieForOasDoc[]) {
    this.dataSource.data = data;
    this.oasDoc.bieList = data;
  }

  select(row: BieForOasDoc) {
    this.selection.select(row.topLevelAsbiepId);
  }

  isSelected(row: BieForOasDoc) {
    return this.selection.isSelected(row.topLevelAsbiepId);
  }

  toggle(row: BieForOasDoc) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.topLevelAsbiepId);
    } else {
      this.select(row);
    }
  }

  isEditable(): boolean {
    return this.access === 'CanEdit';
  }

  isDirty(): boolean {
    return !!this.oasDoc.oasDocId
      || this.oasDoc.title && this.oasDoc.title.length > 0
      || this.oasDoc.description && this.oasDoc.description.length > 0;
  }

  isWorkingRelease(): boolean {
    if (this.oasDoc) {
      return this.oasDoc.releaseId === this.workingRelease.releaseId;
    }
    return false;
  }

  generate() {
    this.loading = true;
    this.openAPIService.generateOpenAPI(this.oasDoc.oasDocId).subscribe(resp => {
      const blob = new Blob([resp.body], {type: resp.headers.get('Content-Type')});
      saveAs(blob, this._getFilenameFromContentDisposition(resp));

      this.loading = false;
    }, err => {
      this.loading = false;
    });
  }

  _getFilenameFromContentDisposition(resp) {
    const contentDisposition = resp.headers.get('Content-Disposition') || '';
    const matches = /filename=([^;]+)/ig.exec(contentDisposition);
    return (matches[1] || 'untitled').replace(/\"/gi, '').trim();
  }

  removeBieForOasDoc() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove selected BIE from the OpenAPI Doc?';
    dialogConfig.data.content = ['Are you sure you want to remove the selected BIE?'];
    dialogConfig.data.action = 'Remove';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          const newData = [];
          this.dataSource.data.forEach(row => {
            if (!this.selection.isSelected(row.topLevelAsbiepId)) {
              newData.push(row);
            }
          });
          this.selection.clear();

          this._updateDataSource(newData);
        }
      });
  }

  get sizeOfChanges(): number {
    return this.getChanged().length;
  }

  get updateDisabled(): boolean {
    return !this.isChanged();
  }

  updateDetails(callbackFn?) {
    if (this.updateDisabled) {
      if (callbackFn === undefined) {
        return;
      } else {
        return callbackFn && callbackFn();
      }
    }
    const request = new BieForOasDocUpdateRequest();
    const nodes = this.getChanged();
    request.oasDocId = this.oasDoc.oasDocId;
    request.bieForOasDocList = nodes;
    this.loading = true;
    this.isUpdating = true;
    this.openAPIService.updateDetails(request).pipe(finalize(() => {
      this.isUpdating = false;
      this.loading = false;
    })).subscribe(_ => {
      this.loadBieListForOasDoc(true);
    });
    if (callbackFn === undefined) {
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    } else {
      return callbackFn && callbackFn();
    }
  }
}
