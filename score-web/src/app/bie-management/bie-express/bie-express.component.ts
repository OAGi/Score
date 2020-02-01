import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog, MatDialogConfig, MatPaginator, MatSort, MatTableDataSource, PageEvent} from '@angular/material';
import {BieList, BieListRequest} from '../bie-list/domain/bie-list';
import {SelectionModel} from '../../../../node_modules/@angular/cdk/collections';
import {BieExpressService} from './domain/bie-express.service';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {BieExpressOption} from './domain/generate-expression';
import {saveAs} from 'file-saver/FileSaver';
import {MetaHeaderDialogComponent} from './meta-header-dialog/meta-header-dialog.component';
import {PaginationResponseDialogComponent} from './pagination-response-dialog/pagination-response-dialog.component';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepickerInputEvent} from '@angular/material/typings/datepicker';
import {PageRequest} from '../../basis/basis';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../common/utility';

@Component({
  selector: 'srt-bie-express',
  templateUrl: './bie-express.component.html',
  styleUrls: ['./bie-express.component.css']
})
export class BieExpressComponent implements OnInit {

  title = 'Express BIE';
  subtitle = 'Selected Top-Level ABIEs';

  displayedColumns: string[] = [
    'select', 'propertyTerm', 'version', 'status', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<BieList>();
  selection = new SelectionModel<BieList>(true, []);
  loading = false;

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  states: string[] = ['Editing', 'Candidate', 'Published'];
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
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.option = new BieExpressOption();
    this.option.bieDefinition = true;
    this.option.expressionOption = 'XML';
    this.option.packageOption = 'ALL';
    // Default Open API expression format is 'YAML'.
    this.option.openAPIExpressionFormat = 'YAML';

    // Init BIE table
    this.request = new BieListRequest();
    this.request.access = 'CanView';
    this.request.excludes = ['Meta Header', 'Pagination Response'];

    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.paginator.length = 0;

    this.sort.active = 'lastUpdateTimestamp';
    this.sort.direction = 'desc';
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });
    this.onChange();
  }

  onPageChange(event: PageEvent) {
    this.loadBieList();
  }

  onChange() {
    this.paginator.pageIndex = 0;
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

  loadBieList() {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.bieListService.getBieListWithRequest(this.request)
      .subscribe(resp => {
        this.paginator.length = resp.length;
        this.dataSource.data = resp.list.map((elm: BieList) => {
          elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
          return elm;
        });
        this.loading = false;
      }, error => {
        this.loading = false;
      });
  }

  select(row) {
    this.selection.select(row);
  }

  toggle(row) {
    if (this.selection.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  generate() {
    const selectedTopLevelAbieIds = this.selection.selected
      .map((bieList: BieList) => bieList.topLevelAbieId);

    this.loading = true;
    this.service.generate(selectedTopLevelAbieIds, this.option).subscribe(resp => {

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
                         metaHeaderTopLevelAbieIdPropertyKey: string) {
    event.preventDefault();
    if (disabled) {
      return;
    }

    if (this.option[metaHeaderTopLevelAbieIdPropertyKey]) {
      this.option[includeMetaHeaderForJsonPropertyKey] = false;
      this.option[metaHeaderTopLevelAbieIdPropertyKey] = undefined;

      this.option.packageOption = this.previousPackageOption;
    } else {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.minWidth = 1000;

      this.bieListService.getMetaHeaderBieList().subscribe(resp => {
        dialogConfig.data = resp;
        const dialogRef = this.dialog.open(MetaHeaderDialogComponent, dialogConfig);
        dialogRef.afterClosed().subscribe(selected => {
          if (selected) {
            this.option[includeMetaHeaderForJsonPropertyKey] = true;
            this.option[metaHeaderTopLevelAbieIdPropertyKey] = selected.topLevelAbieId;

            this.previousPackageOption = this.option.packageOption;
            this.option.packageOption = 'EACH';
          } else {
            this.option[includeMetaHeaderForJsonPropertyKey] = false;
            this.option[metaHeaderTopLevelAbieIdPropertyKey] = undefined;
          }
        });
      });
    }
  }

  togglePaginationResponseOption(event, disabled: boolean,
                                 includePaginationResponseForJsonPropertyKey: string,
                                 paginationResponseTopLevelAbieIdPropertyKey: string) {
    event.preventDefault();
    if (disabled) {
      return;
    }

    if (this.option[paginationResponseTopLevelAbieIdPropertyKey]) {
      this.option[includePaginationResponseForJsonPropertyKey] = false;
      this.option[paginationResponseTopLevelAbieIdPropertyKey] = undefined;

      this.option.packageOption = this.previousPackageOption;
    } else {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.minWidth = 1000;

      this.bieListService.getPaginationResponseBieList().subscribe(resp => {
        dialogConfig.data = resp;
        const dialogRef = this.dialog.open(PaginationResponseDialogComponent, dialogConfig);
        dialogRef.afterClosed().subscribe(selected => {
          if (selected) {
            this.option[includePaginationResponseForJsonPropertyKey] = true;
            this.option[paginationResponseTopLevelAbieIdPropertyKey] = selected.topLevelAbieId;

            this.previousPackageOption = this.option.packageOption;
            this.option.packageOption = 'EACH';
          } else {
            this.option[includePaginationResponseForJsonPropertyKey] = false;
            this.option[paginationResponseTopLevelAbieIdPropertyKey] = undefined;
          }
        });
      });
    }
  }

}
