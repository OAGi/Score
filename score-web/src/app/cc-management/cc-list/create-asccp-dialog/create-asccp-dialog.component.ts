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
import {SemanticGroup, Semantics} from '../../domain/core-component-node';
import {CcListComponent} from '../cc-list.component';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter} from '../../../common/utility';
import {WorkingRelease} from '../../../release-management/domain/release';
import {TagService} from "../../../tag-management/domain/tag.service";
import {Tag} from "../../../tag-management/domain/tag";

@Component({
  selector: 'score-create-asccp-dialog',
  templateUrl: './create-asccp-dialog.component.html',
  styleUrls: ['./create-asccp-dialog.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0', display: 'none'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class CreateAsccpDialogComponent implements OnInit {

  workingStateList = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
  releaseStateList = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];

  displayedColumns: string[] = [
    'select', 'state', 'den', 'revision', 'owner', 'module', 'lastUpdateTimestamp'
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
  tags: Tag[] = [];
  request: CcListRequest;
  action: string;

  workingRelease = WorkingRelease;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(public dialogRef: MatDialogRef<CcListComponent>,
              private ccListService: CcListService,
              private accountService: AccountListService,
              private tagService: TagService,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit() {
    this.request = new CcListRequest();
    this.request.release.releaseId = this.data.releaseId;
    this.action = this.data.action;
    this.request.excludes = this.data.excludes ? this.data.excludes : [];
    this.request.types = ['ACC'];
    this.request.states = [];

    this.paginator.pageIndex = 0;
    this.paginator.pageSize = 10;
    this.paginator.length = 0;

    this.sort.active = 'lastUpdateTimestamp';
    this.sort.direction = 'desc';
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadCcList();
    });

    this.loading = true;
    forkJoin([
      this.accountService.getAccountNames(),
      this.tagService.getTags()
    ]).subscribe(([loginIds, tags]) => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.tags = tags;

      this.loadCcList(true);
    });
  }

  loadCcList(isInit?: boolean) {
    this.loading = true;
    this.request.componentTypes = [Semantics, SemanticGroup];

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

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

  onPageChange(event: PageEvent) {
    this.loadCcList();
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

  onChange(property?: string, source?) {
    if (property === 'filters.den') {
      this.sort.active = '';
      this.sort.direction = '';
    }

    if (property === 'fuzzySearch') {
      if (this.request.fuzzySearch) {
        this.sort.active = '';
        this.sort.direction = '';
      } else {
        this.sort.active = 'lastUpdateTimestamp';
        this.sort.direction = 'desc';
      }
    }
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
            this.dialogRef.close({
              manifestId: this.selection.selected[0].manifestId,
              objectClassTerm: this.selection.selected[0].name
            });
          }
        });
    } else {
      this.dialogRef.close({
        manifestId: this.selection.selected[0].manifestId,
        objectClassTerm: this.selection.selected[0].name
      });
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

}
