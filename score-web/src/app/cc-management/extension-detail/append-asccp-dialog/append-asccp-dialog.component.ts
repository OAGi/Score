import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ExtensionDetailComponent} from '../extension-detail.component';
import {ExtensionDetailService} from '../domain/extension-detail.service';
import {SelectionModel} from '@angular/cdk/collections';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {CcList, CcListRequest} from '../../cc-list/domain/cc-list';
import {CcListService} from '../../cc-list/domain/cc-list.service';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../../basis/basis';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../../common/utility';
import {finalize} from 'rxjs/operators';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'score-append-asccp-dialog',
  templateUrl: './append-asccp-dialog.component.html',
  styleUrls: ['./append-asccp-dialog.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class AppendAsccpDialogComponent implements OnInit {

  displayedColumns: string[] = [
    'select', 'state', 'den', 'revision', 'owner', 'module', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<CcList>();
  selection = new SelectionModel<number>(false, []);
  expandedElement: CcList | null;
  loading = false;

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: CcListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(public dialogRef: MatDialogRef<ExtensionDetailComponent>,
              private ccListService: CcListService,
              private accountService: AccountListService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private service: ExtensionDetailService) {
  }

  ngOnInit() {
    this.request = new CcListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.releaseId = this.data.releaseId;
    this.request.types = ['ASCCP'];
    this.request.states = ['Published'];

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadData();
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });

    this.loadData();
  }

  onPageChange(event: PageEvent) {
    this.loadData();
  }

  onChange() {
    this.paginator.pageIndex = 0;
    this.loadData();
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

  loadData() {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.ccListService.getCcList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: CcList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        if (this.request.filters.module.length > 0) {
          elm.module = elm.module.replace(
            new RegExp(this.request.filters.module, 'ig'),
            '<b>$&</b>');
        }
        if (this.request.filters.definition.length > 0) {
          elm.definition = elm.definition.replace(
            new RegExp(this.request.filters.definition, 'ig'),
            '<b>$&</b>');
        }
        return elm;
      });
    });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  select(row: CcList) {
    this.selection.select(row.id);
  }

  toggle(row: CcList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.id);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CcList) {
    return this.selection.isSelected(row.id);
  }

  onClick(): void {
    const asccpId: number = this.selection.selected[0];
    this.service.appendAsccp(asccpId, this.data.releaseId, this.data.extensionId).subscribe(_ => {
      this.dialogRef.close(this.selection.selected[0]);
    });
  }

}
