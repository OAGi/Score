import {Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {PageRequest, PageResponse} from '../../basis/basis';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {base64Decode, initFilter} from '../../common/utility';
import {Location} from '@angular/common';
import {finalize, map, switchMap} from 'rxjs/operators';
import {BieToAssign, BusinessTerm, BusinessTermListRequest, PostAssignBusinessTerm} from '../domain/business-term';
import {BusinessTermService} from '../domain/business-term.service';
import {HttpParams} from '@angular/common/http';
import {BieEditService} from '../../bie-management/bie-edit/domain/bie-edit.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BieListService} from '../../bie-management/bie-list/domain/bie-list.service';
import {AsbieBbieList} from '../../bie-management/bie-list/domain/bie-list';
import {PreferencesInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {AuthService} from '../../authentication/auth.service';


@Component({
  selector: 'score-assign-business-term',
  templateUrl: './assign-business-term-bt.component.html',
  styleUrls: ['./assign-business-term-bt.component.css']
})
export class AssignBusinessTermBtComponent implements OnInit {
  title = 'Assign Business Term';
  subtitle = 'Select Business Term';


  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (this.preferencesInfo) {
      const columns = this.preferencesInfo.tableColumnsInfo.columnsOfBusinessTermPage;
      for (const column of columns) {
        switch (column.name) {
          case 'Business Term':
            if (column.selected) {
              displayedColumns.push('businessTerm');
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
          case 'Definition':
            if (column.selected) {
              displayedColumns.push('definition');
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

  dataSource = new MatTableDataSource<BusinessTerm>();
  selection = new SelectionModel<number>(true, []);
  loading = false;

  postAssignBtObj = new PostAssignBusinessTerm();

  loginIdList: string[] = [];
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: BusinessTermListRequest;
  preferencesInfo: PreferencesInfo;

  @ViewChild('dateStart', {static: true}) dateStart: MatDatepicker<any>;
  @ViewChild('dateEnd', {static: true}) dateEnd: MatDatepicker<any>;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private businessTermService: BusinessTermService,
              private accountService: AccountListService,
              private bieService: BieEditService,
              private bieListService: BieListService,
              private auth: AuthService,
              private location: Location,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.request = new BusinessTermListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('name', 'asc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.onSearch();
    });

    this.route.queryParamMap.pipe(
      switchMap((params: ParamMap) => {
        const bieIds = this.extractArrayParam(params, 'bieIds');
        const bieTypes = this.extractArrayParam(params, 'bieTypes');
        if (bieIds.length !== bieTypes.length) {
          this.router.navigateByUrl('/business_term_management/assign_business_term/create');
        }
        const bies: BieToAssign[] = bieIds.map((value, index) => {
          const bie = new BieToAssign();
          bie.bieId = Number(value);
          bie.bieType = bieTypes[index];
          return bie;
        });
        return forkJoin([
          this.bieListService.confirmAsbieBbieListByIdAndType(bies),
          this.accountService.getAccountNames(),
          this.preferencesService.load(this.auth.getUserToken())
        ]);
      })).subscribe(([resp, loginIds, preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;

      this.loginIdList.push(...loginIds);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      if (resp === null) {
        this.router.navigateByUrl('/business_term_management/assign_business_term/create');
      } else {
        this.postAssignBtObj.biesToAssign = resp.list.map(e => {
          const bie = new BieToAssign();
          bie.bieId = e.bieId;
          bie.bieType = e.type;
          return bie;
        });

        this.loadBusinessTermList(true);
      }
    }, err => {
      console.error(err);
    });
  }

  private extractArrayParam(params: ParamMap, paramName: string) {
    let str = params.get(paramName);
    if (!str) {
      const q = (this.route.snapshot.queryParamMap) ? this.route.snapshot.queryParamMap.get('q') : undefined;
      const httpParams = (q) ? new HttpParams({fromString: base64Decode(q)}) : new HttpParams();
      str = httpParams.get(paramName);
    }
    const arr = str.split(',');
    return arr;
  }

  onPageChange(event: PageEvent) {
    this.loadBusinessTermList();
  }

  onChange(property?: string, source?) {
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
    this.loadBusinessTermList();
  }

  loadBusinessTermList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.businessTermService.getBusinessTermList(this.request, this.postAssignBtObj.biesToAssign).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list.map((elm: BusinessTerm) => {
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
    const numRows = this.dataSource.data.filter(row => !row.used).length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: BusinessTerm) {
    if (!row.used) {
      this.selection.select(row.businessTermId);
    }
  }

  toggle(row: BusinessTerm) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.businessTermId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: BusinessTerm) {
    return this.selection.isSelected(row.businessTermId);
  }

  create() {
    this.checkUniqueness(this.postAssignBtObj, (_) => {
      if (this.postAssignBtObj.primary) {
        forkJoin(this.postAssignBtObj.biesToAssign
          .map(bie => (
            this.businessTermService.findIfPrimaryExist(bie.bieId, bie.bieType,
              this.postAssignBtObj.primary, this.postAssignBtObj.typeCode)
              .pipe(map(resp => (resp && resp.length > 0))
              ))))
          .subscribe((ifPrimaries: boolean[]) => {
            if (ifPrimaries.filter(ifPrimary => ifPrimary === true).length > 0) {
              const dialogConfig = this.confirmDialogService.newConfig();
              dialogConfig.data.header = 'Overwrite previous preferred business terms?';
              dialogConfig.data.content = [
                ' The preferred business term already exists for ' +
                ((this.postAssignBtObj.biesToAssign.length === 1) ? 'selected BIE and type code.' : 'some of the selected BIEs and type code.') +
                ' Are you sure you want to proceed and overwrite the previous preferred business term assignment?'
              ];
              dialogConfig.data.action = 'Create';
              this.confirmDialogService.open(dialogConfig).afterClosed()
                .subscribe(result => {
                  if (result) {
                    this.doCreate();
                  }
                });
            } else {
              this.doCreate();
            }
          });
      } else {
        this.doCreate();
      }
    });
  }

  doCreate() {
    const businessTerm = this.selection.selected[0];

    this.businessTermService.assignBusinessTermToBie(businessTerm, this.postAssignBtObj.biesToAssign,
      this.postAssignBtObj.primary, this.postAssignBtObj.typeCode)
      .subscribe(resp => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/business_term_management/assign_business_term');
      });
  }

  back() {
    this.router.navigateByUrl('/business_term_management/assign_business_term/create');
  }

  isCreateDisabled() {
    return (this.selection.selected.length === 0);
  }

  checkUniqueness(_postAssignBusinessTerm: PostAssignBusinessTerm, callbackFn?) {
    forkJoin(_postAssignBusinessTerm.biesToAssign
      .map( bie => (
        this.businessTermService.checkAssignmentUniqueness(bie.bieId, bie.bieType,
        this.selection.selected[0], _postAssignBusinessTerm.typeCode, _postAssignBusinessTerm.primary)
      )))
      .subscribe((ifUniques: boolean[]) => {
        if (ifUniques.filter(isUnique => !isUnique).length > 0) {
          this.openDialogDisabledAssignment();
          return;
        }
        return callbackFn && callbackFn(_postAssignBusinessTerm);
      });
  }

  openDialogDisabledAssignment() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another business term assignment for the same BIE and type code already exists!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

}
