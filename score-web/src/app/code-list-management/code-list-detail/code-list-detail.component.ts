import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {MatSidenav} from '@angular/material/sidenav';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {Comment} from '../../cc-management/domain/core-component-node';
import {CodeListService} from '../domain/code-list.service';
import {CodeList, CodeListValue, SimpleAgencyIdList, SimpleAgencyIdListValue} from '../domain/code-list';
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
import {hashCode, initFilter} from '../../common/utility';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {WorkingRelease} from '../../release-management/domain/release';
import {SimpleNamespace} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {CodeListCommentControl} from './code-list-comment-component';
import {RxStompService} from '../../common/score-rx-stomp';
import {Message} from '@stomp/stompjs';

@Component({
  selector: 'score-code-list-detail',
  templateUrl: './code-list-detail.component.html',
  styleUrls: ['./code-list-detail.component.css']
})
export class CodeListDetailComponent implements OnInit {

  title = 'Edit Code List';

  allAgencyIdListValues: SimpleAgencyIdListValue[];

  agencyIdLists: SimpleAgencyIdList[];
  agencyIdListValues: SimpleAgencyIdListValue[];

  namespaces: SimpleNamespace[] = [];
  isUpdating: boolean;

  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<SimpleNamespace[]> = new ReplaySubject<SimpleNamespace[]>(1);

  agencyListFilterCtrl: FormControl = new FormControl();
  filteredAgencyLists: ReplaySubject<SimpleAgencyIdList[]> = new ReplaySubject<SimpleAgencyIdList[]>(1);

  agencyListValueFilterCtrl: FormControl = new FormControl();
  filteredAgencyListValues: ReplaySubject<SimpleAgencyIdListValue[]> = new ReplaySubject<SimpleAgencyIdListValue[]>(1);

  manifestId: number;

  codeList: CodeList;
  agencyIdList: SimpleAgencyIdList;
  revision: CodeList;
  hashCode;
  valueSearch: string;
  workingRelease = WorkingRelease;

  displayedColumns: string[] = [
    'select', 'value', 'meaning', 'deprecated', 'definition', 'definitionSource'
  ];

  dataSource = new MatTableDataSource<CodeListValue>();
  selection = new SelectionModel<CodeListValue>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('sidenav', {static: true}) sidenav: MatSidenav;

  commentControl: CodeListCommentControl;

  constructor(private service: CodeListService,
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

    this.codeList = new CodeList();

    // load a code list by given manifest id
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        this.manifestId = Number(params.get('manifestId'));
        return forkJoin([
          this.service.getCodeList(this.manifestId),
          this.service.getCodeListRevision(this.manifestId),
          this.namespaceService.getSimpleNamespaces()
        ]);
      })).subscribe(([codeList, revision, namespaces]) => {
      this.service.getSimpleAgencyIdListValues(codeList.releaseId).subscribe(resp => {
        this.agencyIdLists = resp.agencyIdLists;
        this.allAgencyIdListValues = resp.agencyIdListValues;
        this.namespaces = namespaces;
        initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList,
          this.getSelectableNamespaces(), (e) => e.uri);
        this.revision = revision;

        this.filteredAgencyLists.next(this.agencyIdLists.slice());

        this.init(codeList);
      }, _ => {
        this.isUpdating = false;
      });
    });

    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.dataSource.filterPredicate = (data: CodeListValue, filter: string) => {
      return (data.value && data.value.toLowerCase().indexOf(filter) > -1)
        || (data.meaning && data.meaning.toLowerCase().indexOf(filter) > -1)
        || (data.definition && data.definition.toLowerCase().indexOf(filter) > -1)
        || (data.definitionSource && data.definitionSource.toLowerCase().indexOf(filter) > -1);
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
  }

  clearFilter() {
    this.valueSearch = '';
    this.applyFilter(this.valueSearch);
  }

  init(codeList: CodeList) {
    this.hashCode = hashCode(codeList);
    if (!!codeList.agencyIdListValueManifestId) {
      let matchedAgencyIdLists = this.allAgencyIdListValues.filter(e => e.agencyIdListValueManifestId === codeList.agencyIdListValueManifestId);
      if (matchedAgencyIdLists.length === 0) {
        matchedAgencyIdLists = this.allAgencyIdListValues.filter(e => e.name === codeList.agencyIdListValueName);
      }

      if (matchedAgencyIdLists.length > 0) {
        const matchedAgencyIdListManifestId = matchedAgencyIdLists[0].agencyIdListManifestId;
        this.agencyIdList = this.agencyIdLists.filter(e => e.agencyIdListManifestId === matchedAgencyIdListManifestId)[0];
      }
    }
    this.onAgencyIdListChange();
    this.codeList = codeList;

    this._updateDataSource(this.codeList.codeListValues);

    this.isUpdating = false;
  }

  get currentAgencyIdListValues(): SimpleAgencyIdListValue[] {
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

  isDisabled(codeList: CodeList) {
    return (this.isUpdating) ||
      (codeList.codeListName === undefined || codeList.codeListName === '') ||
      (codeList.listId === undefined || codeList.listId === '') ||
      (codeList.agencyIdListValueManifestId === undefined || codeList.agencyIdListValueManifestId === 0) ||
      (codeList.versionId === undefined || codeList.versionId === '');
  }

  openDialog(codeListValue?: CodeListValue) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = {};
    dialogConfig.data.codeListValue = new CodeListValue();
    // Default indicator values
    dialogConfig.data.codeListValue.used = true;
    dialogConfig.data.codeListValue.extension = true;
    dialogConfig.data.isEditable = this.isEditable();

    if (codeListValue) { // deep copy
      const copiedCLV = JSON.parse(JSON.stringify(codeListValue));
      if (this.hasRevision) {
        const lastRevisionValue = this.revision.codeListValues.find(
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

  _updateDataSource(data: CodeListValue[]) {
    this.dataSource.data = data;
    this.codeList.codeListValues = data;
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

  select(row: CodeListValue) {
    if (this.isAvailable(row)) {
      this.selection.select(row);
    }
  }

  toggle(row: CodeListValue) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: CodeListValue) {
    return this.selection.isSelected(row);
  }

  isAvailable(codeListValue: CodeListValue) {
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
    this.service.create(this.codeList.releaseId, this.codeList.codeListManifestId)
      .pipe(finalize(() => {this.isUpdating = false; }))
      .subscribe(resp => {
        this.router.navigate(['/code_list/' + resp.manifestId]);
      });
  }

  checkUniqueness(codeList: CodeList): Observable<boolean> {
    return this.service.checkUniqueness(codeList);
  }

  checkNameUniqueness(codeList: CodeList): Observable<boolean> {
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

    if (!this.codeList.codeListName) {
      this.snackBar.open('Name is required', '', {
        duration: 3000,
      });
      return;
    }
    if (!this.codeList.listId) {
      this.snackBar.open('List Id is required', '', {
        duration: 3000,
      });
      return;
    }
    if (!this.codeList.agencyIdListValueManifestId) {
      this.snackBar.open('Agency Id is required', '', {
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
    if (!this.codeList.namespaceId) {
      this.snackBar.open('Namespace is required', '', {
        duration: 3000,
      });
      return;
    }

    if (!this.codeList.definition) {
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
      this.service.getCodeList(this.codeList.codeListManifestId).subscribe(codeList => {
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
      dialogConfig.data.content.push(...['Once in the ' + state + ' state it can no longer be changed or discarded.',]);
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
              this.service.getCodeList(this.manifestId),
              this.service.getCodeListRevision(this.manifestId)
            ]).subscribe(([codeList, revision]) => {
              this.init(codeList);
              this.revision = revision;
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
              this.service.getCodeList(this.manifestId),
              this.service.getCodeListRevision(this.manifestId)
            ]).subscribe(([codeList, revision]) => {
              this.init(codeList);
              this.revision = revision;
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
          this.service.getCodeList(this.codeList.codeListManifestId).subscribe(resp => {
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
    if (this.codeList) {
      return this.codeList.releaseId === this.workingRelease.releaseId;
    }
    return false;
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
    return this.revision != null && this.revision.guid !== null;
  }

  get canDeprecate(): boolean {
    if (this.revision && this.revision.guid) {
      return !this.revision.deprecated;
    } else {
      return false;
    }
  }

  isRevisionValue(value: CodeListValue): boolean {
    if (this.revision && this.revision.codeListValues.length > 0) {
      return this.revision.codeListValues.find(e => e.guid === value.guid) !== undefined;
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
              this.service.getCodeList(this.manifestId),
              this.service.getCodeListRevision(this.manifestId)
            ]).subscribe(([codeList, revision]) => {
              this.init(codeList);
              this.revision = revision;
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
