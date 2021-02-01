import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {BieList, BieListRequest} from '../bie-list/domain/bie-list';
import {SelectionModel} from '../../../../node_modules/@angular/cdk/collections';
import {BieExpressService} from './domain/bie-express.service';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {BieExpressOption} from './domain/generate-expression';
import {saveAs} from 'file-saver/FileSaver';
import {MetaHeaderDialogComponent} from './meta-header-dialog/meta-header-dialog.component';
import {PaginationResponseDialogComponent} from './pagination-response-dialog/pagination-response-dialog.component';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../basis/basis';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter, loadBranch, saveBranch} from '../../common/utility';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {SimpleRelease} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';

@Component({
  selector: 'score-bie-express',
  templateUrl: './bie-express.component.html',
  styleUrls: ['./bie-express.component.css']
})
export class BieExpressComponent implements OnInit {

  title = 'Express BIE';
  subtitle = 'Selected Top-Level ABIEs';

  displayedColumns: string[] = [
    'select', 'state', 'propertyTerm', 'owner', 'businessContexts',
    'version', 'status', 'bizTerm', 'remark', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BieList>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  loginIdList: string[] = [];
  releases: SimpleRelease[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['WIP', 'QA', 'Production'];
  request: BieListRequest;

  option: BieExpressOption;
  openApiFormats: string[] = ['YAML', 'JSON'];

  // Memorizer
  previousPackageOption: string;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: BieExpressService,
              private bieListService: BieListService,
              private accountService: AccountListService,
              private releaseService: ReleaseService,
              private auth: AuthService,
              private dialog: MatDialog,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.option = new BieExpressOption();
    this.option.bieDefinition = true;
    this.option.expressionOption = 'XML';
    this.option.packageOption = 'ALL';
    // Default Open API expression format is 'YAML'.
    this.option.openAPIExpressionFormat = 'YAML';

    // Init BIE table
    this.request = new BieListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.access = 'CanView';
    this.request.excludePropertyTerms = ['Meta Header', 'Pagination Response'];

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    forkJoin([
      this.accountService.getAccountNames(),
      this.releaseService.getSimpleReleases()
    ]).subscribe(([loginIds, releases]) => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.releases = releases.filter(e => e.releaseNum !== 'Working' && e.state === 'Published');
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

      this.loadBieList(true);
    });
  }

  onPageChange(event: PageEvent) {
    this.loadBieList();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);
    }

    this.paginator.pageIndex = 0;
    this.selection.clear();
    this.loadBieList();
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

  loadBieList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.bieListService.getBieListWithRequest(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: BieList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

  select(row: BieList) {
    this.selection.select(row.topLevelAsbiepId);
  }

  toggle(row: BieList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.topLevelAsbiepId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BieList) {
    return this.selection.isSelected(row.topLevelAsbiepId);
  }

  generate() {
    const selectedTopLevelAsbiepIds = this.selection.selected;

    this.loading = true;
    this.service.generate(selectedTopLevelAsbiepIds, this.option).subscribe(resp => {

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

  toggleMetaHeaderOption(event, disabled: boolean,
                         includeMetaHeaderForJsonPropertyKey: string,
                         metaHeaderTopLevelAsbiepIdPropertyKey: string) {
    event.preventDefault();
    if (disabled) {
      return;
    }

    if (this.option[metaHeaderTopLevelAsbiepIdPropertyKey]) {
      this.option[includeMetaHeaderForJsonPropertyKey] = false;
      this.option[metaHeaderTopLevelAsbiepIdPropertyKey] = undefined;

      this.option.packageOption = this.previousPackageOption;
    } else {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.minWidth = 1000;
      const dialogRef = this.dialog.open(MetaHeaderDialogComponent, dialogConfig);
      dialogRef.afterClosed().subscribe(selectedTopLevelAsbiepId => {
        if (selectedTopLevelAsbiepId) {
          this.option[includeMetaHeaderForJsonPropertyKey] = true;
          this.option[metaHeaderTopLevelAsbiepIdPropertyKey] = selectedTopLevelAsbiepId;

          this.previousPackageOption = this.option.packageOption;
          this.option.packageOption = 'EACH';
        } else {
          this.option[includeMetaHeaderForJsonPropertyKey] = false;
          this.option[metaHeaderTopLevelAsbiepIdPropertyKey] = undefined;
        }
      });
    }
  }

  togglePaginationResponseOption(event, disabled: boolean,
                                 includePaginationResponseForJsonPropertyKey: string,
                                 paginationResponseTopLevelAsbiepIdPropertyKey: string) {
    event.preventDefault();
    if (disabled) {
      return;
    }

    if (this.option[paginationResponseTopLevelAsbiepIdPropertyKey]) {
      this.option[includePaginationResponseForJsonPropertyKey] = false;
      this.option[paginationResponseTopLevelAsbiepIdPropertyKey] = undefined;

      this.option.packageOption = this.previousPackageOption;
    } else {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.minWidth = 1000;
      const dialogRef = this.dialog.open(PaginationResponseDialogComponent, dialogConfig);
      dialogRef.afterClosed().subscribe(selectedTopLevelAsbiepId => {
        if (selectedTopLevelAsbiepId) {
          this.option[includePaginationResponseForJsonPropertyKey] = true;
          this.option[paginationResponseTopLevelAsbiepIdPropertyKey] = selectedTopLevelAsbiepId;

          this.previousPackageOption = this.option.packageOption;
          this.option.packageOption = 'EACH';
        } else {
          this.option[includePaginationResponseForJsonPropertyKey] = false;
          this.option[paginationResponseTopLevelAsbiepIdPropertyKey] = undefined;
        }
      });
    }
  }

}
