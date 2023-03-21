import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {MatSidenav} from '@angular/material/sidenav';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {Comment} from '../../cc-management/domain/core-component-node';
import {AgencyIdListService} from '../domain/agency-id-list.service';
import {AgencyIdList, AgencyIdListValue} from '../domain/agency-id-list';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {AgencyIdListValueDialogComponent} from '../agency-id-list-value-dialog/agency-id-list-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {finalize, switchMap} from 'rxjs/operators';
import {v4 as uuid} from 'uuid';
import {FormControl} from '@angular/forms';
import {forkJoin, Observable, ReplaySubject} from 'rxjs';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {WorkingRelease} from '../../release-management/domain/release';
import {SimpleNamespace} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {AgencyIdListCommentControl} from './agency-id-list-comment-component';
import {RxStompService} from '../../common/score-rx-stomp';
import {Message} from '@stomp/stompjs';
import {initFilter} from "../../common/utility";

@Component({
  selector: 'score-agency-id-list-detail',
  templateUrl: './agency-id-list-detail.component.html',
  styleUrls: ['./agency-id-list-detail.component.css']
})
export class AgencyIdListDetailComponent implements OnInit {

  title = 'Edit Agency ID List';
  namespaces: SimpleNamespace[] = [];
  isUpdating: boolean;
  manifestId: number;

  agencyIdList: AgencyIdList;
  hashCode;
  valueSearch: string;
  workingRelease = WorkingRelease;

  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<SimpleNamespace[]> = new ReplaySubject<SimpleNamespace[]>(1);
  valueFilterCtrl: FormControl = new FormControl();
  valueFilteredList: ReplaySubject<AgencyIdListValue[]> = new ReplaySubject<AgencyIdListValue[]>(1);

  displayedColumns: string[] = [
    'select', 'value', 'name', 'deprecated', 'definition', 'definitionSource'
  ];

  dataSource = new MatTableDataSource<AgencyIdListValue>();
  selection = new SelectionModel<AgencyIdListValue>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('sidenav', {static: true}) sidenav: MatSidenav;

  commentControl: AgencyIdListCommentControl;

  constructor(private service: AgencyIdListService,
              private namespaceService: NamespaceService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService,
              private stompService: RxStompService) {
  }

  ngOnInit() {
    this.commentControl = new AgencyIdListCommentControl(this.sidenav, this.service);
    this.isUpdating = true;

    this.agencyIdList = new AgencyIdList();

    // load an agency ID list by given manifest id
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        this.manifestId = Number(params.get('manifestId'));
        return forkJoin([
          this.service.getAgencyIdList(this.manifestId),
          this.namespaceService.getSimpleNamespaces()
        ]);
      })
    ).subscribe(([agencyIdList, namespaces]) => {
      this.namespaces = namespaces;
      initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList,
        this.getSelectableNamespaces(), (e) => e.uri);
      this.init(agencyIdList);
      this.isUpdating = false;
    });

    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.dataSource.filterPredicate = (data: AgencyIdListValue, filter: string) => {
      return (data.value && data.value.indexOf(filter) > -1)
        || (data.name && data.name.indexOf(filter) > -1)
        || (data.definition && data.definition.indexOf(filter) > -1);
    };

    this.subscribeEvent();
  }

  receiveCommentEvent(evt) {
    const comment = new Comment();
    comment.commentId = evt.properties.commentId;
    comment.prevCommentId = evt.properties.prevCommentId;
    comment.text = evt.properties.text;
    comment.loginId = evt.properties.actor;
    comment.timestamp = evt.properties.timestamp;
    comment.isNew = true;

    if (comment.prevCommentId) {
      let idx = this.commentControl.comments.findIndex(e => e.commentId === comment.prevCommentId);
      let childrenCnt = this.commentControl.comments.filter(e => e.prevCommentId === comment.prevCommentId).length;
      this.commentControl.comments.splice(idx + childrenCnt + 1, 0, comment);
    } else {
      this.commentControl.comments.push(comment);
    }
  }

  getSelectableNamespaces(namespaceId?: number): SimpleNamespace[] {
    return this.namespaces.filter(e => {
      if (!!namespaceId && e.namespaceId === namespaceId) {
        return true;
      }
      return ('developer' in this.userRoles) ? e.standard : !e.standard;
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
  }

  clearFilter() {
    this.valueSearch = '';
    this.applyFilter(this.valueSearch);
  }

  init(agencyIdList: AgencyIdList) {
    this.agencyIdList = new AgencyIdList(agencyIdList);
    this.hashCode = this.agencyIdList.hashCode;

    this._updateDataSource(this.agencyIdList.values);
    this.valueFilteredList.next(agencyIdList.values.slice());
    this.valueFilterCtrl.valueChanges.subscribe(() => {
      this.filterValueList();
    });
  }

  filterValueList() {
    let search = this.valueFilterCtrl.value;
    if (!search) {
      this.valueFilteredList.next(this.agencyIdList.values.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.valueFilteredList.next(
      this.agencyIdList.values.filter(value => value.name.toLowerCase().indexOf(search) > -1)
    );
  }

  color(agencyIdListValue: AgencyIdListValue): string {
    return 'blue';
  }

  get isChanged(): boolean {
    return this.hashCode !== this.agencyIdList.hashCode;
  }

  isDisabled(agencyIdList: AgencyIdList) {
    return (this.isUpdating) ||
      (agencyIdList.name === undefined || agencyIdList.name === '') ||
      (agencyIdList.listId === undefined || agencyIdList.listId === '') ||
      (agencyIdList.versionId === undefined || agencyIdList.versionId === '');
  }

  openDialog(agencyIdListValue?: AgencyIdListValue) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = {};
    dialogConfig.data.agencyIdListValue = new AgencyIdListValue();
    dialogConfig.data.isEditable = this.isEditable();
    dialogConfig.data.agencyId = this.agencyIdList.agencyIdListValueManifestId;

    if (agencyIdListValue) { // deep copy
      const copiedValue = JSON.parse(JSON.stringify(agencyIdListValue));
      if (this.hasRevision) {
        const lastRevisionValue = this.agencyIdList.prev.values.find(
          e => e.guid === agencyIdListValue.guid);
        dialogConfig.data.lastRevisionValue = lastRevisionValue;
      }
      dialogConfig.data.agencyIdListValue = copiedValue;
    }

    const isAddAction: boolean = (agencyIdListValue === undefined);

    this.isUpdating = true;
    const dialogRef = this.dialog.open(AgencyIdListValueDialogComponent, dialogConfig);
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

  _updateDataSource(data: AgencyIdListValue[]) {
    this.dataSource.data = data;
    this.agencyIdList.values = data;

    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.filteredData.filter(row => this.isAvailable(row)).length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.filteredData.forEach(row => this.select(row));
  }

  select(row: AgencyIdListValue) {
    if (this.isAvailable(row)) {
      this.selection.select(row);
    }
  }

  toggle(row: AgencyIdListValue) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: AgencyIdListValue) {
    return this.selection.isSelected(row);
  }

  isAvailable(agencyIdListValue: AgencyIdListValue) {
    if (agencyIdListValue.used) {
      return false;
    }
    if (this.agencyIdList.agencyIdListValueManifestId === agencyIdListValue.agencyIdListValueManifestId) {
      return false;
    }
    return this.agencyIdList.state === 'WIP';
  }

  removeAgencyIdListValues() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove Agency ID List Value?';
    dialogConfig.data.content = ['Are you sure you want to remove the agency ID list value?'];
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
    this.service.create(this.agencyIdList.releaseId, this.agencyIdList.agencyIdListManifestId)
      .pipe(finalize(() => {
        this.isUpdating = false;
      }))
      .subscribe(resp => {
        this.router.navigate(['/agency_id_list/' + resp.manifestId]);
      });
  }

  checkUniqueness(agencyIdList: AgencyIdList): Observable<boolean> {
    return this.service.checkUniqueness(agencyIdList);
  }

  checkNameUniqueness(agencyIdList: AgencyIdList): Observable<boolean> {
    return this.service.checkNameUniqueness(agencyIdList);
  }

  _update() {
    forkJoin([
      this.checkUniqueness(this.agencyIdList),
      this.checkNameUniqueness(this.agencyIdList)
    ]).subscribe(([isViolateUniqueness, isViolateNameUniqueness]) => {
      if (isViolateUniqueness) {
        this.alertInvalidParameters();
      } else if (isViolateNameUniqueness) {
        this.alertDuplicatedProperties();
      } else {
        this.doUpdate();
      }
    });
  }

  update() {
    if (!this.agencyIdList.name) {
      this.snackBar.open('Name is required', '', {
        duration: 3000,
      });
      return;
    }
    if (!this.agencyIdList.listId) {
      this.snackBar.open('List Id is required', '', {
        duration: 3000,
      });
      return;
    }
    if (!this.agencyIdList.versionId) {
      this.snackBar.open('Version is required', '', {
        duration: 3000,
      });
      return;
    }
    if (!this.agencyIdList.namespaceId) {
      this.snackBar.open('Namespace is required', '', {
        duration: 3000,
      });
      return;
    }

    if (!this.agencyIdList.definition) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Empty Definition';
      dialogConfig.data.content = [
        'Are you sure you want to update this without definitions?'
      ];
      dialogConfig.data.action = 'Update Anyway';

      this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(result => {
        if (!result) {
          return;
        }
        this._update();
      });
    } else {
      this._update();
    }
  }

  doUpdate() {
    this.isUpdating = true;

    this.service.update(this.agencyIdList).subscribe(_ => {
      this.service.getAgencyIdList(this.agencyIdList.agencyIdListManifestId).subscribe(agencyIdList => {
        this.init(agencyIdList);
        this.isUpdating = false;
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      });
    }, error => {
      this.isUpdating = false;
    });
  }

  alertInvalidParameters() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another agency ID list with the triplet (ListID, AgencyID, Version) already exist!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

  alertDuplicatedProperties() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Duplicated Properties';
    dialogConfig.data.content = [
      'Another agency ID list with the same name already exists.',
      'Are you sure you want to update the agency ID list?'
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

    if (!this.agencyIdList.name) {
      this.snackBar.open('Name is required', '', {
        duration: 3000,
      });
      return;
    }
    if (!this.agencyIdList.listId) {
      this.snackBar.open('List Id is required', '', {
        duration: 3000,
      });
      return;
    }
    if (!this.agencyIdList.versionId) {
      this.snackBar.open('Version is required', '', {
        duration: 3000,
      });
      return;
    }
    if (!this.agencyIdList.namespaceId) {
      this.snackBar.open('Namespace is required', '', {
        duration: 3000,
      });
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Update state to \'' + state + '\'?';
    dialogConfig.data.content = ['Are you sure you want to update the state to \'' + state + '\'?'];
    if (state === 'Published' || state === 'Production') {
      dialogConfig.data.content.push(...['Once in the ' + state + ' state it can no longer be changed or discarded.',]);
    }
    dialogConfig.data.action = (state === 'Published' || state === 'Production') ? 'Update anyway' : 'Update';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.isUpdating = true;

          this.service.updateState(this.agencyIdList, state).subscribe(_ => {
            forkJoin([
              this.service.getAgencyIdList(this.manifestId),
            ]).subscribe(([agencyIdList]) => {
              this.init(agencyIdList);

              this.isUpdating = false;
              this.snackBar.open('Updated', '', {
                duration: 3000,
              });
            });
          }, error => {
            this.isUpdating = false;
          });
        }
      });
  }

  makeNewRevision() {
    const isDeveloper = this.userRoles.includes('developer');
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = (isDeveloper) ? 'Revise this agency ID list?' : 'Amend this agency ID list?';
    dialogConfig.data.content = [(isDeveloper) ? 'Are you sure you want to revise this agency ID list?' : 'Are you sure you want to amend this agency ID list?'];
    dialogConfig.data.action = (isDeveloper) ? 'Revise' : 'Amend';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.isUpdating = true;
          this.service.makeNewRevision(this.agencyIdList).subscribe(_ => {
            this.service.getAgencyIdList(this.manifestId).subscribe(agencyIdList => {
              this.init(agencyIdList);
              this.isUpdating = false;
              this.snackBar.open((isDeveloper) ? 'Revised' : 'Amended', '', {
                duration: 3000,
              });
            });
          }, error => {
            this.isUpdating = false;
          });
        }
      });
  }

  delete() {
    this.openDialogAgencyIdListDelete();
  }

  restore() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore this agency ID list?';
    dialogConfig.data.content = ['Are you sure you want to restore this agency ID list?'];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }
        this.isUpdating = true;
        const state = 'WIP';
        this.service.restore(this.agencyIdList.agencyIdListManifestId).subscribe(_ => {
          this.service.getAgencyIdList(this.agencyIdList.agencyIdListManifestId).subscribe(resp => {
            this.init(resp);
            this.isUpdating = false;
            this.snackBar.open('Restored', '', {
              duration: 3000,
            });
          });
        }, error => {
          this.isUpdating = false;
        });
      });
  }

  openDialogAgencyIdListDelete() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Delete agency ID list?';
    dialogConfig.data.content = ['Are you sure you want to delete this agency ID list?'];
    dialogConfig.data.action = 'Delete anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.isUpdating = true;

          this.service.delete(this.agencyIdList.agencyIdListManifestId).subscribe(_ => {
            this.snackBar.open('Deleted', '', {
              duration: 3000,
            });
            this.isUpdating = false;

            this.router.navigateByUrl('/agency_id_list');
          }, error => {
            this.isUpdating = false;
          });
        }
      });
  }

  isWorkingRelease(): boolean {
    if (this.agencyIdList) {
      return this.agencyIdList.releaseNum === this.workingRelease.releaseNum;
    }
    return false;
  }

  get state(): string {
    if (this.agencyIdList) {
      return this.agencyIdList.state;
    }
    return '';
  }

  get access(): string {
    if (this.agencyIdList) {
      return this.agencyIdList.access;
    }
    return '';
  }

  get hasRevision(): boolean {
    return this.agencyIdList.prev != null;
  }

  get canDeprecate(): boolean {
    if (!this.hasRevision) {
      return false;
    } else {
      return !this.agencyIdList.prev.deprecated;
    }
  }

  get showDeprecateCheckbox(): boolean {
    if (!this.agencyIdList.lastUpdatedBy || this.agencyIdList.lastUpdatedBy.roles.includes('developer') ) {
      return false;
    } else {
      return true;
    }
  }

  get canExtensible(): boolean {
    if (!this.userRoles.includes('developer')) {
      return false;
    }

    return true;
  }

  isRevisionValue(value: AgencyIdListValue): boolean {
    if (this.hasRevision) {
      return !!this.agencyIdList.prev.values.find(e => e.guid === value.guid);
    }
    return false;
  }

  isDerived(value: AgencyIdListValue): boolean {
    return !!value.basedAgencyIdListValueManifestId;
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
          .subscribe(resp => {
            this.service.getAgencyIdList(this.manifestId).subscribe(agencyIdList => {
              this.init(agencyIdList);
              this.isUpdating = false;
              this.snackBar.open('Canceled', '', {
                duration: 3000,
              });
            });
          }, err => {
            this.isUpdating = false;
          });
      });
  }

  openComments() {
    this.commentControl.toggleCommentSlide(this.agencyIdList);
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  subscribeEvent() {
    this.stompService.watch('/topic/agency_id_list/' + this.manifestId).subscribe((message: Message) => {
      const data = JSON.parse(message.body);
      if (data.properties.actor !== this.currentUser) {
        let noti;
        if (data.action === 'UpdateDetail') {
          noti = 'Agency ID List updated by ' + data.properties.actor;
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
