import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {CcList, CcListRequest} from '../../cc-list/domain/cc-list';
import {SelectionModel} from '@angular/cdk/collections';
import {CcListService} from '../../cc-list/domain/cc-list.service';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../../basis/basis';
import {CcNodeService} from '../../domain/core-component-node.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../../common/utility';
import {WorkingRelease} from '../../../release-management/domain/release';
import {OagisComponentType, OagisComponentTypes} from '../../domain/core-component-node';

@Component({
  selector: 'score-append-association-dialog',
  templateUrl: './append-association-dialog.component.html',
  styleUrls: ['./append-association-dialog.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0', display: 'none'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class AppendAssociationDialogComponent implements OnInit {

  typeList: string[] = ['ASCCP', 'BCCP'];
  workingStateList = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
  releaseStateList = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];
  componentTypeList: OagisComponentType[] = OagisComponentTypes;
  workingRelease = WorkingRelease;
  action: string;

  displayedColumns: string[] = [
    'select', 'type', 'state', 'den', 'revision', 'owner', 'module', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<CcList>();
  expandedElement: CcList | null;
  selection = new SelectionModel<CcList>(false, []);
  loading = false;

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: CcListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(public dialogRef: MatDialogRef<AppendAssociationDialogComponent>,
              private ccListService: CcListService,
              private accountService: AccountListService,
              private service: CcNodeService,
              private confirmDialogService: ConfirmDialogService,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.action = data.action;
    if (this.data.isGlobal) {
      this.typeList = ['BCCP'];
    }
  }

  ngOnInit() {
    this.request = new CcListRequest();
    this.request.release.releaseId = this.data.releaseId;
    this.request.types = this.data.isGlobal ? ['BCCP'] : this.typeList;
    this.request.excludes = this.data.excludes ? this.data.excludes : [];

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
    this.onChange();
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

  onChange() {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    if (this.request.types.length === 0) {
      this.request.types = this.typeList;
    }

    this.ccListService.getCcList(this.request).subscribe(resp => {
      this.paginator.length = resp.length;
      this.paginator.pageIndex = resp.page;

      const list = resp.list.map((elm: CcList) => {
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

      this.dataSource.data = list;
      this.loading = false;
    });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onClick(): void {
    if (this.selection.selected.length === 0) {
      return;
    }
    if (this.selection.selected[0].deprecated) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Confirmation required';
      dialogConfig.data.content = ['The selected component is deprecated', 'Are you sure you want to proceed with it?'];
      dialogConfig.data.action = 'Proceed anyway';

      this.confirmDialogService.open(dialogConfig).beforeClosed()
        .subscribe(result => {
          if (result) {
            this.dialogRef.close(this.selection.selected[0]);
          }
        });
    } else {
      this.dialogRef.close(this.selection.selected[0]);
    }
  }

  select(row: CcList) {
    this.selection.select(row);
  }

  toggle(row: CcList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CcList) {
    return this.selection.isSelected(row);
  }

  getRouterLink(ccList: CcList) {
    switch (ccList.type.toUpperCase()) {
      case 'ASCCP':
        return 'core_component/asccp/' + ccList.manifestId;

      case 'BCCP':
        return 'core_component/bccp/' + ccList.manifestId;

      default:
        return window.location.pathname;
    }
  }

}
