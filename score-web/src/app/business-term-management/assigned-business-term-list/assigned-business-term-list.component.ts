import {Component, OnInit, ViewChild} from '@angular/core';
import {AssignedBtListRequest, AssignedBusinessTerm, BieToAssign} from '../domain/business-term';
import {BusinessTermService} from '../domain/business-term.service';
import {MatDialog} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {PageRequest, PageResponse} from '../../basis/basis';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter} from '../../common/utility';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {AuthService} from '../../authentication/auth.service';
import {AsbieBbieList} from '../../bie-management/bie-list/domain/bie-list';
import {BieListService} from '../../bie-management/bie-list/domain/bie-list.service';
import {PreferencesInfo, TableColumnsInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';

@Component({
  selector: 'score-assigned-business-term',
  templateUrl: './assigned-business-term-list.component.html',
  styleUrls: ['./assigned-business-term-list.component.css']
})
export class AssignedBusinessTermListComponent implements OnInit {

  bieId: number;

  title = 'Business Term Assignment';

  get columns() {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfAssignedBusinessTermPage;
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    this.preferencesInfo.tableColumnsInfo.columnsOfAssignedBusinessTermPage = updatedColumns;
    this.preferencesService.update(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {});
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.onColumnsChange(defaultTableColumnInfo.columnsOfAssignedBusinessTermPage);
  }

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (this.preferencesInfo) {
      const columns = this.preferencesInfo.tableColumnsInfo.columnsOfAssignedBusinessTermPage;
      for (const column of columns) {
        switch (column.name) {
          case 'BIE DEN':
            if (column.selected) {
              displayedColumns.push('bieDen');
            }
            break;
          case 'Business Term':
            if (column.selected) {
              displayedColumns.push('businessTerm');
            }
            break;
          case 'Preferred Business Term':
            if (column.selected) {
              displayedColumns.push('primary');
            }
            break;
          case 'External Reference URI':
            if (column.selected) {
              displayedColumns.push('externalReferenceUri');
            }
            break;
          case 'External Reference ID':
            if (column.selected) {
              displayedColumns.push('externalReferenceId');
            }
            break;
          case 'Type Code':
            if (column.selected) {
              displayedColumns.push('typeCode');
            }
            break;
          case 'BIE Type':
            if (column.selected) {
              displayedColumns.push('bieType');
            }
            break;
          case 'Updated On':
            if (column.selected) {
              displayedColumns.push('lastUpdateTimestamp');
            }
            break;
        }
      }
    }
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<AssignedBusinessTerm>();
  selection = new SelectionModel<AssignedBusinessTerm>(true, []);
  loading = false;

  loginIdList: string[] = [];
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: AssignedBtListRequest;
  preferencesInfo: PreferencesInfo;
  typeList: string[] = ['BBIE', 'ASBIE'];

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: BusinessTermService,
              private accountService: AccountListService,
              private auth: AuthService,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private bieListService: BieListService) {
  }

  ngOnInit() {
    this.request = new AssignedBtListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));
    this.request.filters.bieTypes = ['BBIE', 'ASBIE'];

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.onSearch();
    });

    forkJoin([
      this.accountService.getAccountNames(),
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([loginIds, preferencesInfo] ) => {
      this.preferencesInfo = preferencesInfo;

      this.loginIdList.push(...loginIds);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      // Load BIE ID
      const bieId = this.route.snapshot.queryParamMap.get('bieId');
      const bieType = this.route.snapshot.queryParamMap.get('bieType');
      if (bieId !== null && bieId !== '' && bieType !== null && bieType !== '') {
        const bie = new BieToAssign();
        bie.bieId = Number(bieId);
        bie.bieType = bieType;
        this.request.filters.bieId = Number(bieId);
        this.request.filters.bieTypes = [bieType];
        this.bieListService.confirmAsbieBbieListByIdAndType([bie])
          .subscribe((resp: PageResponse<AsbieBbieList>) => {
            if (resp === null || resp.list.length !== 1) {
              this.router.navigateByUrl('/business_term_management/assign_business_term/create');
            } else {
              const bieResp = resp.list[0];
              this.request.filters.bieId = bieResp.bieId;
              this.request.filters.bieTypes = [bieResp.type];
              this.request.filters.bieDen = bieResp.den;
            }
          });
      }

      this.loadAssignedBusinessTermList(true);
    });
  }

  onPageChange(event: PageEvent) {
    this.loadAssignedBusinessTermList();
  }

  onChange(property?: string, source?) {
  }

  saveSelectedBIEForSearch() {
    const assignedBizTermIds = this.selection.selected;
    if (assignedBizTermIds.length === 1){
      const selectedRow = this.dataSource.data.filter(row => assignedBizTermIds.includes(row))[0];
      this.request.filters.bieId = selectedRow.bieId;
      this.request.filters.bieDen = selectedRow.den;
      this.loadAssignedBusinessTermList();
      this.selection.clear();
    } else {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Please select one row';
      dialogConfig.data.content = [
        'To perform search by BIE, please select exactly one row.',
        assignedBizTermIds.length + (assignedBizTermIds.length > 1 ? ' rows are' : ' row is') + ' currently selected.'
      ];
      this.confirmDialogService.open(dialogConfig);
    }
  }

  deselect(){
    this.request.filters.bieId = null;
    this.request.filters.bieDen = '';
    this.request.filters.bieTypes = ['ASBIE', 'BBIE'];
    this.loadAssignedBusinessTermList();
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
        this.dateStart.select(undefined);
        this.request.updatedDate.start = null;
        break;
      case 'endDate':
        this.dateEnd.select(undefined);
        this.request.updatedDate.end = null;
        break;
    }
  }

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadAssignedBusinessTermList();
  }

  loadAssignedBusinessTermList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getAssignedBusinessTermList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: AssignedBusinessTerm) => {
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

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: AssignedBusinessTerm) {
    this.selection.select(row);
  }

  toggle(row: AssignedBusinessTerm) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: AssignedBusinessTerm) {
    return this.selection.isSelected(row);
  }

  discard() {
    const assignedBts = this.selection.selected;
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard ' + (assignedBts.length > 1 ? 'business terms assignments' : 'business term assignment') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected ' +
      (assignedBts.length > 1 ? 'business terms assignments' : 'business term assignment') + '?',
      'The ' + (assignedBts.length > 1 ? 'business terms assignments' : 'business term assignment') + ' will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.deleteAssignments(assignedBts).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadAssignedBusinessTermList();
          });
        }
      });
  }

  openMakeAsPrimaryDialog(assignedBusinessTerm: AssignedBusinessTerm, $event) {
    if (assignedBusinessTerm.primary) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Set ' + assignedBusinessTerm.businessTerm + ' to a preferred term?';
    dialogConfig.data.content = [
      'Are you sure you want to set ' + assignedBusinessTerm.businessTerm + ' as a preferred term for BIE ',
      assignedBusinessTerm.den + '?' ];
    dialogConfig.data.action = 'Set to preferred';
    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.makeAsPrimary(assignedBusinessTerm.bieId).subscribe(_ => {
            this.snackBar.open('Set to Preferred', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadAssignedBusinessTermList();
          });
        }
      });
  }

  openAssignedBt(type, id) {
    this.router.navigate(['/business_term_management/assign_business_term/details/' + id],
      {queryParams: { type, id }});
  }

}
