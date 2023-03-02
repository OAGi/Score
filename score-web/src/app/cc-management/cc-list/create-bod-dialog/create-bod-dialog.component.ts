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
  nounSelection = new SelectionModel<CcList>(true, []);
  verbSelection = new SelectionModel<CcList>(true, []);
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
    this.verbRequest.tags = ['Verb'];
    this.nounRequest.types = ['ASCCP'];
    this.nounRequest.tags = ['Noun'];

    this.verbPaginator.pageIndex = 0;
    this.verbPaginator.pageSize = 10;
    this.verbPaginator.length = 0;

    this.nounPaginator.pageIndex = 0;
    this.nounPaginator.pageSize = 10;
    this.nounPaginator.length = 0;

    this.verbSort.active = '';
    this.verbSort.direction = '';
    this.verbSort.sortChange.subscribe(() => {
      this.verbPaginator.pageIndex = 0;
      this.loadCcList('verb');
    });

    this.nounSort.active = 'lastUpdateTimestamp';
    this.nounSort.direction = 'desc';
    this.nounSort.sortChange.subscribe(() => {
      this.nounPaginator.pageIndex = 0;
      this.loadCcList('noun');
    });

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });

    this.loadCcList('verb');
    this.loadCcList('noun');
  }

  loadCcList(type: string) {
    this.loading = true;

    if (!type || 'verb' === type) {
      this.verbRequest.page = new PageRequest(
        this.verbSort.active, this.verbSort.direction,
        this.verbPaginator.pageIndex, this.verbPaginator.pageSize);

      this.ccListService.getCcList(this.verbRequest).subscribe(resp => {
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
        this.verbPaginator.length = resp.length;
        this.verbPaginator.pageIndex = resp.page;

        // clean up the selection
        if (this.verbSelection.selected.length > 0) {
          const selectedMap = new Map(this.verbSelection.selected.map(e => [e.id, e]));
          this.verbDataSource.data.forEach(e => {
            if (selectedMap.has(e.id)) {
              this.verbSelection.deselect(selectedMap.get(e.id));
              this.verbSelection.select(e);
            }
          });
        }
        this.loading = false;
      });
    }

    if (!type || 'noun' === type) {
      this.nounRequest.page = new PageRequest(
        this.nounSort.active, this.nounSort.direction,
        this.nounPaginator.pageIndex, this.nounPaginator.pageSize);

      this.ccListService.getCcList(this.nounRequest).subscribe(resp => {
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
        this.nounPaginator.length = resp.length;
        this.nounPaginator.pageIndex = resp.page;

        // clean up the selection
        if (this.nounSelection.selected.length > 0) {
          const selectedMap = new Map(this.nounSelection.selected.map(e => [e.id, e]));
          this.nounDataSource.data.forEach(e => {
            if (selectedMap.has(e.id)) {
              this.nounSelection.deselect(selectedMap.get(e.id));
              this.nounSelection.select(e);
            }
          });
        }
        this.loading = false;
      });
    }
  }

  onPageChange(event: PageEvent, type: string) {
    this.loadCcList(type);
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

  onChange(type: string, property?: string, source?) {
    if (type === 'verb') {
      if (property === 'filters.den') {
        this.verbSort.active = '';
        this.verbSort.direction = '';
      }

      if (property === 'fuzzySearch') {
        if (this.verbRequest.fuzzySearch) {
          this.verbSort.active = '';
          this.verbSort.direction = '';
        } else {
          this.verbSort.active = 'lastUpdateTimestamp';
          this.verbSort.direction = 'desc';
        }
      }
    } else if (type === 'noun') {
      if (property === 'filters.den') {
        this.nounSort.active = '';
        this.nounSort.direction = '';
      }

      if (property === 'fuzzySearch') {
        if (this.nounRequest.fuzzySearch) {
          this.nounSort.active = '';
          this.nounSort.direction = '';
        } else {
          this.nounSort.active = 'lastUpdateTimestamp';
          this.nounSort.direction = 'desc';
        }
      }
    }
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onClick(): void {
    if (this.nounSelection.selected.length === 0 || this.verbSelection.selected.length === 0) {
      return;
    }

    const numOfDeprecatedComponents =
      this.nounSelection.selected.filter(e => e.deprecated).length +
      this.verbSelection.selected.filter(e => e.deprecated).length;

    const numOfDeletedComponents =
      this.nounSelection.selected.filter(e => e.state === 'Deleted').length +
      this.verbSelection.selected.filter(e => e.state === 'Deleted').length;

    if (numOfDeprecatedComponents > 0 || numOfDeletedComponents > 0) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Confirmation required';
      dialogConfig.data.content = [
        (numOfDeprecatedComponents > 0) ?
          'There ' + ((numOfDeprecatedComponents === 1) ? 'is a deprecate component' : 'are deprecate components') + ' in the selected list.' :
          'There ' + ((numOfDeletedComponents === 1) ? 'is a deleted component' : 'are deleted components') + ' in the selected list.',
        'Are you sure you want to proceed with it?'];
      dialogConfig.data.action = 'Proceed anyway';

      this.confirmDialogService.open(dialogConfig).beforeClosed()
        .subscribe(result => {
          if (result) {
            this.dialogRef.close({
              verbManifestIdList: this.verbSelection.selected.map(e => e.manifestId),
              nounManifestIdList: this.nounSelection.selected.map(e => e.manifestId)
            });
          }
        });
    } else {
      this.dialogRef.close({
        verbManifestIdList: this.verbSelection.selected.map(e => e.manifestId),
        nounManifestIdList: this.nounSelection.selected.map(e => e.manifestId)
      });
    }
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected(type: string) {
    const selection = (type === 'verb') ? this.verbSelection : this.nounSelection;
    const dataSource = (type === 'verb') ? this.verbDataSource : this.nounDataSource;

    const selectedIdList = selection.selected.map(e => e.manifestId).slice().sort();
    const rowIdList = dataSource.data.map(e => e.manifestId).slice().sort();
    return selectedIdList.length === rowIdList.length &&
      selectedIdList.every(val => rowIdList.includes(val));
  }

  numOfSelected(type: string): number {
    const selection = (type === 'verb') ? this.verbSelection : this.nounSelection;
    const dataSource = (type === 'verb') ? this.verbDataSource : this.nounDataSource;

    const selectedIdList = selection.selected.map(e => e.manifestId).slice().sort();
    const rowIdList = dataSource.data.map(e => e.manifestId).slice().sort();
    const numOfSelected = selectedIdList.filter(val => rowIdList.includes(val)).length;
    return numOfSelected;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle(type: string, event) {
    const selection = (type === 'verb') ? this.verbSelection : this.nounSelection;
    const dataSource = (type === 'verb') ? this.verbDataSource : this.nounDataSource;
    const paginator = (type === 'verb') ? this.verbPaginator : this.nounPaginator;

    (selection.selected.length === paginator.length) ?
      selection.clear() :
      ((this.numOfSelected(type) > 0) ?
        dataSource.data.forEach(row => this.deselect(row, type)) :
        dataSource.data.forEach(row => this.select(row, type)));

    event.stopPropagation();
    event.preventDefault();
  }

  select(row: CcList, type: string) {
    if (type === 'verb') {
      this.verbSelection.select(row);
    } else {
      this.nounSelection.select(row);
    }
  }

  deselect(row: CcList, type: string) {
    if (type === 'verb') {
      this.verbSelection.deselect(row);
    } else {
      this.nounSelection.deselect(row);
    }
  }

  toggle(row: CcList, type: string) {
    if (this.isSelected(row, type)) {
      this.deselect(row, type);
    } else {
      this.select(row, type);
    }
  }

  isSelected(row: CcList, type: string) {
    if (type === 'verb') {
      return this.verbSelection.isSelected(row);
    } else {
      return this.nounSelection.isSelected(row);
    }
  }

  get totalNumberOfBODs(): number {
    const numOfNounSelection = this.nounSelection.selected.length;
    const numOfVerbSelection = this.verbSelection.selected.length;
    return numOfNounSelection * numOfVerbSelection;
  }

  get BODName(): string {
    const numOfNounSelection = this.nounSelection.selected.length;
    const numOfVerbSelection = this.verbSelection.selected.length;
    if (numOfNounSelection === 0 || numOfVerbSelection === 0) {
      return '';
    }
    return this.verbSelection.selected[0].name + ' ' + this.nounSelection.selected[0].name;
  }

}
