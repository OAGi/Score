import {Component, HostListener, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {Location} from '@angular/common';
import {MatSidenav} from '@angular/material/sidenav';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {Comment} from '../../cc-management/domain/core-component-node';
import {CodeListService} from '../domain/code-list.service';
import {CodeListDetails, CodeListValue, CodeListValueDetails} from '../domain/code-list';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {CodeListValueDialogComponent} from '../code-list-value-dialog/code-list-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {finalize, switchMap} from 'rxjs/operators';
import {v4 as uuid} from 'uuid';
import {FormControl} from '@angular/forms';
import {forkJoin, Observable, ReplaySubject} from 'rxjs';
import {hashCode, initFilter, saveBranch} from '../../common/utility';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {CodeListCommentControl} from './code-list-comment-component';
import {RxStompService} from '../../common/score-rx-stomp';
import {Message} from '@stomp/stompjs';
import {PreferencesInfo, TableColumnsInfo, TableColumnsProperty} from '../../settings-management/settings-preferences/domain/preferences';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {ScoreTableColumnResizeDirective} from '../../common/score-table-column-resize/score-table-column-resize.directive';
import {AgencyIdListService} from '../../agency-id-list-management/domain/agency-id-list.service';
import {AgencyIdListSummary, AgencyIdListValueSummary} from '../../agency-id-list-management/domain/agency-id-list';

@Component({
  selector: 'score-code-list-detail',
  templateUrl: './code-list-detail.component.html',
  styleUrls: ['./code-list-detail.component.css']
})
export class CodeListDetailComponent implements OnInit {

  title = 'Edit Code List';

  allAgencyIdListValues: AgencyIdListValueSummary[];

  agencyIdLists: AgencyIdListSummary[];
  agencyIdListValues: AgencyIdListValueSummary[];

  namespaces: NamespaceSummary[] = [];
  isUpdating: boolean;

  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<NamespaceSummary[]> = new ReplaySubject<NamespaceSummary[]>(1);

  agencyListFilterCtrl: FormControl = new FormControl();
  filteredAgencyLists: ReplaySubject<AgencyIdListSummary[]> = new ReplaySubject<AgencyIdListSummary[]>(1);

  agencyListValueFilterCtrl: FormControl = new FormControl();
  filteredAgencyListValues: ReplaySubject<AgencyIdListValueSummary[]> = new ReplaySubject<AgencyIdListValueSummary[]>(1);

  manifestId: number;

  codeList: CodeListDetails;
  agencyIdList: AgencyIdListSummary;
  prevCodeList: CodeListDetails;
  preferencesInfo: PreferencesInfo;
  hashCode;
  valueSearch: string;
  highlightText: string;

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfCodeListValuePage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfCodeListValuePage = columns;
    this.updateTableColumnsForCodeListValuePage();
  }

  updateTableColumnsForCodeListValuePage() {
    this.preferencesService.updateTableColumnsForCodeListValuePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfCodeListValuePage;
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    const updatedColumnsWithWidth = updatedColumns.map(column => ({
      name: column.name,
      selected: column.selected,
      width: this.width(column.name)
    }));

    this.columns = updatedColumnsWithWidth;
  }

  onResizeWidth($event) {
    switch ($event.name) {
      default:
        this.setWidth($event.name, $event.width);
        break;
    }
  }

  setWidth(name: string, width: number | string) {
    const matched = this.columns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.updateTableColumnsForCodeListValuePage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (!this.preferencesInfo) {
      return displayedColumns;
    }
    for (const column of this.columns) {
      switch (column.name) {
        case 'Value':
          if (column.selected) {
            displayedColumns.push('value');
          }
          break;
        case 'Meaning':
          if (column.selected) {
            displayedColumns.push('meaning');
          }
          break;
        case 'Deprecated':
          if (column.selected) {
            displayedColumns.push('deprecated');
          }
          break;
        case 'Definition':
          if (column.selected) {
            displayedColumns.push('definition');
          }
          break;
        case 'Definition Source':
          if (column.selected) {
            displayedColumns.push('definitionSource');
          }
          break;
      }
    }
    return displayedColumns;
  }

  dataSource = new MatTableDataSource<CodeListValueDetails>();
  selection = new SelectionModel<CodeListValueDetails>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('sidenav', {static: true}) sidenav: MatSidenav;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;

  commentControl: CodeListCommentControl;

  constructor(private service: CodeListService,
              private agencyIdListService: AgencyIdListService,
              private namespaceService: NamespaceService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private stompService: RxStompService) {
  }

  ngOnInit() {
    this.commentControl = new CodeListCommentControl(this.sidenav, this.service);
    this.isUpdating = true;

    this.agencyListFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterAgencyList();
      });
    this.agencyListValueFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterAgencyListValue();
      });

    this.codeList = new CodeListDetails();

    // load a code list by given manifest id
    this.route.paramMap.pipe(
        switchMap((params: ParamMap) => {
          this.manifestId = Number(params.get('manifestId'));
          return forkJoin([
            this.service.getCodeListDetails(this.manifestId),
            this.preferencesService.load(this.auth.getUserToken())
          ]);
        })).subscribe(([codeList, preferencesInfo]) => {
      /** Save again as Release info may have changed via Uplifting or other routes. */
      saveBranch(this.auth.getUserToken(), 'CC', codeList.release.releaseId);

      this.namespaceService.getNamespaceSummaries(codeList.library.libraryId).subscribe(namespaces => {
        this.namespaces = namespaces;
        initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList,
            this.getSelectableNamespaces(), (e) => e.uri);
      });

      this.agencyIdListService.getAgencyIdListSummaries(
          codeList.release.releaseId).subscribe(resp => {
        this.agencyIdLists = resp;
        this.allAgencyIdListValues = resp.flatMap(item => item.valueList);
        this.preferencesInfo = preferencesInfo;

        this.filteredAgencyLists.next(this.agencyIdLists.slice());

        this.init(codeList);
      }, _ => {
        this.isUpdating = false;
      });
    }, err => {
      this.isUpdating = false;
      let errorMessage;
      if (err.status === 403) {
        errorMessage = 'You do not have access permission.';
      } else {
        errorMessage = 'Something\'s wrong.';
      }
      this.snackBar.open(errorMessage, '', {
        duration: 3000
      });
      this.router.navigateByUrl('/code_list');
      return;
    });

    // Prevent the sorting event from being triggered if any columns are currently resizing.
    const originalSort = this.sort.sort;
    this.sort.sort = (sortChange) => {
      if (this.tableColumnResizeDirectives &&
        this.tableColumnResizeDirectives.filter(e => e.resizing).length > 0) {
        return;
      }
      originalSort.apply(this.sort, [sortChange]);
    };
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.dataSource.filterPredicate = (data: CodeListValueDetails, filter: string) => {
      return (data.value && data.value.toLowerCase().indexOf(filter) > -1)
        || (data.meaning && data.meaning.toLowerCase().indexOf(filter) > -1)
        || (data.definition && data.definition.content && data.definition.content.toLowerCase().indexOf(filter) > -1)
        || (data.definition && data.definition.source && data.definition.source.toLowerCase().indexOf(filter) > -1);
    };

    this.subscribeEvent();
  }

  receiveCommentEvent(evt) {
    const comment = new Comment();
    comment.commentId = evt.properties.commentId;
    comment.prevCommentId = evt.properties.prevCommentId;
    comment.text = evt.properties.text;
    comment.created.who.loginId = evt.properties.actor;
    comment.timestamp = evt.properties.timestamp;
    comment.isNew = true;

    if (comment.prevCommentId) {
      const idx = this.commentControl.comments.findIndex(e => e.commentId === comment.prevCommentId);
      const childrenCnt = this.commentControl.comments.filter(e => e.prevCommentId === comment.prevCommentId).length;
      this.commentControl.comments.splice(idx + childrenCnt + 1, 0, comment);
    } else {
      this.commentControl.comments.push(comment);
    }
  }

  getSelectableNamespaces(namespaceId?: number): NamespaceSummary[] {
    return this.namespaces.filter(e => {
      if (!!namespaceId && e.namespaceId === namespaceId) {
        return true;
      }
      return (this.userRoles.includes('developer')) ? e.standard : !e.standard;
    });
  }

  get userRoles(): string[] {
    const userToken = this.auth.getUserToken();
    return userToken.roles;
  }

  applyFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // MatTableDataSource defaults to lowercase matches
    this.dataSource.filter = filterValue;
    this.highlightText = filterValue;
  }

  clearFilter() {
    this.valueSearch = '';
    this.applyFilter(this.valueSearch);
  }

  init(codeList: CodeListDetails) {
    if (!!codeList && codeList.log.revisionNum > 1) {
      this.service.getPrevCodeListDetails(codeList.codeListManifestId).subscribe(prev => {
        this.prevCodeList = prev;
        this._doInit(codeList);
      }, err => {
        if (err.status === 404) {
          // ignore
        } else {
          throw err;
        }
        this._doInit(codeList);
      });
    } else {
      this._doInit(codeList);
    }
  }

  _doInit(codeList: CodeListDetails) {
    this.hashCode = hashCode(codeList);
    if (!!codeList.agencyIdListValue) {
      let matchedAgencyIdLists = this.allAgencyIdListValues.filter(e => e.agencyIdListValueManifestId === codeList.agencyIdListValue.agencyIdListValueManifestId);
      if (matchedAgencyIdLists.length === 0) {
        matchedAgencyIdLists = this.allAgencyIdListValues.filter(e => e.name === codeList.agencyIdListValue.name);
      }

      if (matchedAgencyIdLists.length > 0) {
        const matchedAgencyIdListManifestId = matchedAgencyIdLists[0].agencyIdListManifestId;
        this.agencyIdList = this.agencyIdLists.filter(e => e.agencyIdListManifestId === matchedAgencyIdListManifestId)[0];
      }
    }
    this.onAgencyIdListChange();
    this.codeList = codeList;

    this._updateDataSource(this.codeList.valueList);

    this.isUpdating = false;
  }

  get currentAgencyIdListValues(): AgencyIdListValueSummary[] {
    let agencyIdListValues;
    if (!!this.agencyIdList) {
      agencyIdListValues = this.allAgencyIdListValues.filter(
        e => e.agencyIdListManifestId === this.agencyIdList.agencyIdListManifestId);
    } else {
      agencyIdListValues = [];
    }
    return agencyIdListValues;
  }

  onAgencyIdListChange() {
    this.filteredAgencyListValues.next(this.currentAgencyIdListValues.slice());
  }

  filterAgencyList() {
    let search = this.agencyListFilterCtrl.value;
    if (!search) {
      this.filteredAgencyLists.next(this.agencyIdLists.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredAgencyLists.next(
      this.agencyIdLists.filter(agencyList => agencyList.name.toLowerCase().indexOf(search) > -1)
    );
  }

  filterAgencyListValue() {
    let search = this.agencyListValueFilterCtrl.value;
    if (!search) {
      this.filteredAgencyListValues.next(this.currentAgencyIdListValues.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredAgencyListValues.next(
      this.currentAgencyIdListValues.filter(agencyIdListValue =>
        (agencyIdListValue.name.toLowerCase() + ' (' + agencyIdListValue.value.toLowerCase() + ')').indexOf(search) > -1)
    );
  }

  color(codeListValue: CodeListValue): string {
    return 'blue';
  }

  get isChanged(): boolean {
    return this.hashCode !== hashCode(this.codeList);
  }

  isDisabled(codeList: CodeListDetails) {
    return (this.isUpdating) ||
      (codeList.name === undefined || codeList.name === '') ||
      (codeList.listId === undefined || codeList.listId === '') ||
      (!codeList.agencyIdListValue || codeList.agencyIdListValue.agencyIdListValueManifestId === undefined || codeList.agencyIdListValue.agencyIdListValueManifestId === 0) ||
      (codeList.versionId === undefined || codeList.versionId === '');
  }

  openDialog(codeListValue?: CodeListValueDetails) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = {};
    dialogConfig.data.codeListValue = new CodeListValueDetails();
    // Default indicator values
    dialogConfig.data.codeListValue.used = true;
    dialogConfig.data.codeListValue.extension = true;
    dialogConfig.data.isEditable = this.isEditable();

    if (codeListValue) { // deep copy
      const copiedCLV = JSON.parse(JSON.stringify(codeListValue));
      if (this.hasRevision) {
        const lastRevisionValue = this.prevCodeList.valueList.find(
          e => e.guid === codeListValue.guid);
        dialogConfig.data.lastRevisionValue = lastRevisionValue;
      }
      dialogConfig.data.codeListValue = copiedCLV;
    }

    const isAddAction: boolean = (codeListValue === undefined);

    this.isUpdating = true;
    const dialogRef = this.dialog.open(CodeListValueDialogComponent, dialogConfig);
    dialogRef.afterClosed().pipe(finalize(() => {
      this.isUpdating = false;
    })).subscribe(result => {
      if (!result) {
        return;
      }

      const data = this.dataSource.data;
      if (isAddAction) {
        for (const value of data) {
          if (value.value === result.value) {
            this.snackBar.open(result.value + ' already exist', '', {
              duration: 3000,
            });

            return;
          }
        }

        result.guid = uuid();
        data.push(result);

        this._updateDataSource(data);
      } else {
        for (const value of data) {
          if (value.guid !== result.guid && value.value === result.value) {
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

  _updateDataSource(data: CodeListValueDetails[]) {
    this.dataSource.data = data;
    this.codeList.valueList = data;
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.filter(row => this.isAvailable(row)).length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: CodeListValueDetails) {
    if (this.isAvailable(row)) {
      this.selection.select(row);
    }
  }

  toggle(row: CodeListValueDetails) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CodeListValueDetails) {
    return this.selection.isSelected(row);
  }

  isAvailable(codeListValue: CodeListValueDetails) {
    return this.codeList.state === 'WIP';
  }

  removeCodeListValues() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove Code List Value?';
    dialogConfig.data.content = ['Are you sure you want to remove the code list value?'];
    dialogConfig.data.action = 'Remove';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          const newData = [];
          this.dataSource.data.forEach(row => {
            if (!this.selection.isSelected(row)) {
              newData.push(row);
            }
          });
          this.selection.clear();

          this._updateDataSource(newData);
        }
      });
  }

  back() {
    this.location.back();
  }

  derive() {
    this.isUpdating = true;
    this.service.create(this.codeList.release.releaseId, this.codeList.codeListManifestId)
      .pipe(finalize(() => {this.isUpdating = false; }))
      .subscribe(resp => {
        this.router.navigate(['/code_list/' + resp.codeListManifestId]);
      });
  }

  checkUniqueness(codeList: CodeListDetails): Observable<boolean> {
    return this.service.checkUniqueness(codeList);
  }

  checkNameUniqueness(codeList: CodeListDetails): Observable<boolean> {
    return this.service.checkNameUniqueness(codeList);
  }

  _update() {
    this.isUpdating = true;
    forkJoin([
      this.checkUniqueness(this.codeList),
      this.checkNameUniqueness(this.codeList)
    ]).subscribe(([isViolateUniqueness, isViolateNameUniqueness]) => {
      if (isViolateUniqueness) {
        this.isUpdating = false;
        this.alertInvalidParameters();
      } else if (isViolateNameUniqueness) {
        this.isUpdating = false;
        this.alertDuplicatedProperties();
      } else {
        this.doUpdate();
      }
    });
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

    // Handle 'Ctrl/Command+S'
    const metaOrCtrlKeyPressed = $event.metaKey || $event.ctrlKey;
    if (metaOrCtrlKeyPressed && charCode === 's') {
      $event.preventDefault();
      $event.stopPropagation();

      this.update();
    }
  }

  get updateDisabled(): boolean {
    return (this.state !== 'WIP' || this.access !== 'CanEdit') || this.isUpdating || !this.isChanged;
  }

  update() {
    if (this.updateDisabled) {
      return;
    }

    if (!this.codeList.name) {
      this.snackBar.open('Name is required', '', {
        duration: 3000,
      });
      return;
    }
    if (!this.codeList.listId) {
      this.snackBar.open('List ID is required', '', {
        duration: 3000,
      });
      return;
    }
    if (!this.codeList.agencyIdListValue || !this.codeList.agencyIdListValue.agencyIdListValueManifestId) {
      this.snackBar.open('Agency ID List Value is required', '', {
        duration: 3000,
      });
      return;
    }
    if (!this.codeList.versionId) {
      this.snackBar.open('Version is required', '', {
        duration: 3000,
      });
      return;
    }
    if (!this.codeList.namespace || !this.codeList.namespace.namespaceId) {
      this.snackBar.open('Namespace is required', '', {
        duration: 3000,
      });
      return;
    }

    if (!this.codeList.definition || !this.codeList.definition.content) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Empty Definition';
      dialogConfig.data.content = [
        'Are you sure you want to update this without definitions?'
      ];
      dialogConfig.data.action = 'Update Anyway';

      this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(result => {
        if (result) {
          this._update();
        }
      });
    } else {
      this._update();
    }
  }

  doUpdate() {
    this.isUpdating = true;

    this.service.update(this.codeList).pipe(finalize(() => {
      this.isUpdating = false;
    })).subscribe(_ => {
      this.service.getCodeListDetails(this.codeList.codeListManifestId).subscribe(codeList => {
        this.init(codeList);
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      });
    });
  }

  alertInvalidParameters() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another code list with the triplet (ListID, AgencyID, Version) already exist!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

  alertDuplicatedProperties() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Duplicated Properties';
    dialogConfig.data.content = [
      'Another code list with the same name already exists.',
      'Are you sure you want to update the code list?'
    ];
    dialogConfig.data.action = 'Update anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.doUpdate();
        }
      });
  }

  updateState(state: string) {
    if (!state) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Update state to \'' + state + '\'?';
    dialogConfig.data.content = ['Are you sure you want to update the state to \'' + state + '\'?'];
    if (state === 'Published' || state === 'Production') {
      dialogConfig.data.content.push(...['Once in the ' + state + ' state it can no longer be changed or discarded.', ]);
    }
    dialogConfig.data.action = (state === 'Published' || state === 'Production') ? 'Update anyway' : 'Update';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.isUpdating = true;

          this.service.updateState(this.codeList, state).pipe(finalize(() => {
            this.isUpdating = false;
          })).subscribe(_ => {
            forkJoin([
              this.service.getCodeListDetails(this.manifestId)
            ]).subscribe(([codeList]) => {
              this.init(codeList);
              this.snackBar.open('Updated', '', {
                duration: 3000,
              });
            });
          });
        }
      });
  }

  makeNewRevision() {
    const isDeveloper = this.userRoles.includes('developer');
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = (isDeveloper) ? 'Revise this code list?' : 'Amend this code list?';
    dialogConfig.data.content = [(isDeveloper) ? 'Are you sure you want to revise this code list?' : 'Are you sure you want to amend this code list?'];
    dialogConfig.data.action = (isDeveloper) ? 'Revise' : 'Amend';

    this.confirmDialogService.open(dialogConfig).afterClosed()

      .subscribe(result => {
        if (result) {
          this.isUpdating = true;
          this.service.makeNewRevision(this.codeList).pipe(finalize(() => {
            this.isUpdating = false;
          })).subscribe(_ => {
            forkJoin([
              this.service.getCodeListDetails(this.manifestId)
            ]).subscribe(([codeList]) => {
              this.init(codeList);
              this.snackBar.open((isDeveloper) ? 'Revised' : 'Amended', '', {
                duration: 3000,
              });
            });
          });
        }
      });
  }

  delete() {
    this.openDialogCodeListDelete();
  }

  restore() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore this code list?';
    dialogConfig.data.content = ['Are you sure you want to restore this code list?'];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }
        this.isUpdating = true;
        const state = 'WIP';
        this.service.restore(this.codeList.codeListManifestId).pipe(finalize(() => {
          this.isUpdating = false;
        })).subscribe(_ => {
          this.service.getCodeListDetails(this.codeList.codeListManifestId).subscribe(resp => {
            this.init(resp);
            this.snackBar.open('Restored', '', {
              duration: 3000,
            });
          });
        });
      });
  }

  openDialogCodeListDelete() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Delete code list?';
    dialogConfig.data.content = ['Are you sure you want to delete this code list?'];
    dialogConfig.data.action = 'Delete anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.isUpdating = true;

          this.service.delete(this.codeList.codeListManifestId).subscribe(_ => {
            this.snackBar.open('Deleted', '', {
              duration: 3000,
            });
            this.isUpdating = false;

            this.router.navigateByUrl('/code_list');
          });
        }
      });
  }

  isWorkingRelease(): boolean {
    return !!this.codeList && this.codeList.release.workingRelease;
  }

  get state(): string {
    if (this.codeList) {
      return this.codeList.state;
    }
    return '';
  }

  get access(): string {
    if (this.codeList) {
      return this.codeList.access;
    }
    return '';
  }

  get hasRevision(): boolean {
    return !!this.codeList && this.codeList.log.revisionNum > 1;
  }

  get canDeprecate(): boolean {
    if (this.prevCodeList && this.prevCodeList.guid) {
      return !this.prevCodeList.deprecated;
    } else {
      return false;
    }
  }

  isRevisionValue(value: CodeListValue): boolean {
    if (this.prevCodeList && this.prevCodeList.valueList.length > 0) {
      return this.prevCodeList.valueList.find(e => e.guid === value.guid) !== undefined;
    }
    return false;
  }

  isEditable(): boolean {
    return this.state === 'WIP' && this.access === 'CanEdit';
  }

  cancelRevision(): void {
    const isDeveloper = this.userRoles.includes('developer');
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = (isDeveloper) ? 'Cancel this revision?' : 'Cancel this amendment?';
    dialogConfig.data.content = [(isDeveloper) ? 'Are you sure you want to cancel this revision?' : 'Are you sure you want to cancel this amendment?'];
    dialogConfig.data.action = 'Okay';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }

        this.isUpdating = true;
        this.service.cancelRevision(this.manifestId)
          .pipe(
            finalize(() => {
              this.isUpdating = false;
            })
          )
          .subscribe(resp => {
            forkJoin([
              this.service.getCodeListDetails(this.manifestId)
            ]).subscribe(([codeList]) => {
              this.init(codeList);
              this.snackBar.open('Canceled', '', {
                duration: 3000,
              });
            });
          }, err => {
          });
      });
  }

  openComments() {
    this.commentControl.toggleCommentSlide(this.codeList);
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  subscribeEvent() {
    this.stompService.watch('/topic/code_list/' + this.manifestId).subscribe((message: Message) => {
      const data = JSON.parse(message.body);
      if (data.properties.actor !== this.currentUser) {
        let noti;
        if (data.action === 'UpdateDetail') {
          noti = 'Code List updated by ' + data.properties.actor;
        } else if (data.action === 'ChangeState') {
          noti = 'State changed to \'' + data.properties.State + '\' by ' + data.properties.actor;
        } else if (data.action === 'AddComment' && this.sidenav.opened) {
          this.receiveCommentEvent(data);
        } else {
          return;
        }

        if (noti) {
          const snackBarRef = this.snackBar.open(noti, 'Reload');
          snackBarRef.onAction().subscribe(() => {
            this.ngOnInit();
          });
        }
      }
    });
  }
}
