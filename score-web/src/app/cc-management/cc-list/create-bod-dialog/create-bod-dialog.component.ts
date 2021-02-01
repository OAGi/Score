import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {CcList, CcListRequest} from '../../cc-list/domain/cc-list';
import {CcListService} from '../../cc-list/domain/cc-list.service';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest} from '../../../basis/basis';
import {CcListComponent} from '../cc-list.component';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../../common/utility';
import {WorkingRelease} from '../../../release-management/domain/release';

@Component({
  selector: 'score-create-asccp-dialog',
  templateUrl: './create-bod-dialog.component.html',
  styleUrls: ['./create-bod-dialog.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0', display: 'none'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class CreateBodDialogComponent implements OnInit {

  workingStateList = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
  releaseStateList = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];

  displayedColumns: string[] = [
    'select', 'state', 'den', 'lastUpdateTimestamp'
  ];
  nounDataSource = new MatTableDataSource<CcList>();
  verbDataSource = new MatTableDataSource<CcList>();
  expandedElement: CcList | null;
  nounSelection = new SelectionModel<CcList>(false, []);
  verbSelection = new SelectionModel<CcList>(false, []);
  loading = false;

  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  verbRequest: CcListRequest;
  nounRequest: CcListRequest;
  action: string;

  workingRelease = WorkingRelease;

  @ViewChild('verbSort', {static: true}) verbSort: MatSort;
  @ViewChild('nounSort', {static: true}) nounSort: MatSort;
  @ViewChild('verbPaginator', {static: true}) verbPaginator: MatPaginator;
  @ViewChild('nounPaginator', {static: true}) nounPaginator: MatPaginator;

  constructor(public dialogRef: MatDialogRef<CcListComponent>,
              private ccListService: CcListService,
              private accountService: AccountListService,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit() {
    this.verbRequest = new CcListRequest();
    this.nounRequest = new CcListRequest();
    this.verbRequest.release.releaseId = this.data.releaseId;
    this.nounRequest.release.releaseId = this.data.releaseId;
    this.action = this.data.action;
    this.verbRequest.types = ['ASCCP'];
    this.verbRequest.asccpTypes = ['Verb'];
    this.nounRequest.types = ['ASCCP'];
    this.nounRequest.asccpTypes = ['Default'];

    this.verbPaginator.pageIndex = 0;
    this.verbPaginator.pageSize = 10;
    this.verbPaginator.length = 0;

    this.nounPaginator.pageIndex = 0;
    this.nounPaginator.pageSize = 10;
    this.nounPaginator.length = 0;

    this.verbSort.active = 'lastUpdateTimestamp';
    this.verbSort.direction = 'desc';
    this.verbSort.sortChange.subscribe(() => {
      this.verbPaginator.pageIndex = 0;
      this.onChange('verb');
    });

    this.nounSort.active = 'lastUpdateTimestamp';
    this.nounSort.direction = 'desc';
    this.nounSort.sortChange.subscribe(() => {
      this.nounPaginator.pageIndex = 0;
      this.onChange('noun');
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
        this.nounRequest.updatedDate.start = new Date(event.value);
        break;
      case 'endDate':
        this.nounRequest.updatedDate.end = new Date(event.value);
        break;
    }
  }

  reset(type: string) {
    switch (type) {
      case 'startDate':
        this.nounRequest.updatedDate.start = null;
        break;
      case 'endDate':
        this.nounRequest.updatedDate.end = null;
        break;
    }
  }

  onChange(type?: string) {
    this.loading = true;

    if (!type || 'verb' === type) {
      this.verbRequest.page = new PageRequest(
        this.verbSort.active, this.verbSort.direction,
        this.verbPaginator.pageIndex, this.verbPaginator.pageSize);

      this.ccListService.getCcList(this.verbRequest).subscribe(resp => {
        this.verbPaginator.length = resp.length;
        this.verbPaginator.pageIndex = resp.page;

        const list = resp.list.map((elm: CcList) => {
          elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
          if (this.verbRequest.filters.module.length > 0) {
            elm.module = elm.module.replace(
              new RegExp(this.verbRequest.filters.module, 'ig'),
              '<b>$&</b>');
          }
          if (this.verbRequest.filters.definition.length > 0) {
            elm.definition = elm.definition.replace(
              new RegExp(this.verbRequest.filters.definition, 'ig'),
              '<b>$&</b>');
          }
          return elm;
        });

        this.verbDataSource.data = list;
        this.loading = false;
      });
    }

    if (!type || 'noun' === type) {
      this.nounRequest.page = new PageRequest(
        this.nounSort.active, this.nounSort.direction,
        this.nounPaginator.pageIndex, this.nounPaginator.pageSize);

      this.ccListService.getCcList(this.nounRequest).subscribe(resp => {
        this.nounPaginator.length = resp.length;
        this.nounPaginator.pageIndex = resp.page;

        const list = resp.list.map((elm: CcList) => {
          elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
          if (this.nounRequest.filters.module.length > 0) {
            elm.module = elm.module.replace(
              new RegExp(this.nounRequest.filters.module, 'ig'),
              '<b>$&</b>');
          }
          if (this.nounRequest.filters.definition.length > 0) {
            elm.definition = elm.definition.replace(
              new RegExp(this.nounRequest.filters.definition, 'ig'),
              '<b>$&</b>');
          }
          return elm;
        });

        this.nounDataSource.data = list;
        this.loading = false;
      });
    }
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onClick(): void {
    if (this.nounSelection.selected.length === 0 || this.verbSelection.selected.length === 0) {
      return;
    }

    if (this.nounSelection.selected[0].deprecated || this.verbSelection.selected[0].deprecated) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Confirmation required';
      dialogConfig.data.content = ['The selected component is deprecated', 'Are you sure you want to proceed with it?'];
      dialogConfig.data.action = 'Proceed anyway';

      this.confirmDialogService.open(dialogConfig).beforeClosed()
        .subscribe(result => {
          if (result) {
            this.dialogRef.close({
              verbManifestId: this.verbSelection.selected[0].manifestId,
              nounManifestId: this.nounSelection.selected[0].manifestId
            });
          }
        });
    } else {
      this.dialogRef.close({
        verbManifestId: this.verbSelection.selected[0].manifestId,
        nounManifestId: this.nounSelection.selected[0].manifestId
      });
    }
  }

  select(row: CcList, type: string) {
    if (type === 'verb') {
      this.verbSelection.select(row);
    } else {
      this.nounSelection.select(row);
    }
  }

  toggle(row: CcList, type: string) {
    if (type === 'verb') {
      if (this.isSelected(row, type)) {
        this.verbSelection.deselect(row);
      } else {
        this.select(row, type);
      }
    } else {
      if (this.isSelected(row, type)) {
        this.nounSelection.deselect(row);
      } else {
        this.select(row, type);
      }
    }
  }

  isSelected(row: CcList, type: string) {
    if (type === 'verb') {
      return this.verbSelection.isSelected(row);
    } else {
      return this.nounSelection.isSelected(row);
    }
  }

  get BODName(): string {
    if (this.nounSelection.selected.length === 0 || this.verbSelection.selected.length === 0) {
      return '';
    }
    return this.verbSelection.selected[0].name + ' ' + this.nounSelection.selected[0].name;
  }

}
