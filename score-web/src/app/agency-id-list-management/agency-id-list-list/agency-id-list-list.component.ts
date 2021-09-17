import {animate, state, style, transition, trigger} from '@angular/animations';
import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {ContextMenuComponent, ContextMenuService} from 'ngx-contextmenu';
import {AccountList} from '../../account-management/domain/accounts';
import {TransferOwnershipDialogComponent} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AgencyIdList, AgencyIdListForListRequest} from '../domain/agency-id-list';
import {AgencyIdListService} from '../domain/agency-id-list.service';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {PageRequest} from '../../basis/basis';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {initFilter, loadBranch, saveBranch} from '../../common/utility';
import {WorkingRelease} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {Release} from '../../bie-management/bie-create/domain/bie-create-list';
import {AuthService} from '../../authentication/auth.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'score-agency-id-list-list',
  templateUrl: './agency-id-list-list.component.html',
  styleUrls: ['./agency-id-list-list.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class AgencyIdListListComponent implements OnInit {

  title = 'Agency Id List';
  workingRelease = WorkingRelease;
  workingStateList = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
  releaseStateList = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];

  displayedColumns: string[] = [
    'select', 'state', 'name', 'versionId', 'revision', 'owner', 'transferOwnership', 'module', 'lastUpdateTimestamp', 'more'
  ];
  dataSource = new MatTableDataSource<AgencyIdList>();
  selection = new SelectionModel<AgencyIdList>(true, []);
  expandedElement: AgencyIdList | null;
  canSelect = ['WIP', 'Deleted'];
  loading = false;

  releases: Release[] = [];
  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: AgencyIdListForListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild('contextMenuDefault', {static: true}) public contextMenuDefault: ContextMenuComponent;
  @ViewChild('contextMenuDeleted', {static: true}) public contextMenuDeleted: ContextMenuComponent;
  @ViewChild('contextMenuEditable', {static: true}) public contextMenuEditable: ContextMenuComponent;
  @ViewChild('contextMenuEditableRevised', {static: true}) public contextMenuEditableRevised: ContextMenuComponent;
  @ViewChild('createContextMenu', {static: true}) public createContextMenu: ContextMenuComponent;

  constructor(private service: AgencyIdListService,
              private releaseService: ReleaseService,
              private accountService: AccountListService,
              private auth: AuthService,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private contextMenuService: ContextMenuService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.request = new AgencyIdListForListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });

    this.releases = [];
    forkJoin([
      this.releaseService.getSimpleReleases(['Published', 'Draft']),
      this.accountService.getAccountNames()
    ]).subscribe(([releases, loginIds]) => {
      this.releases.push(...releases);
      if (this.releases.length > 0) {
        const savedReleaseId = loadBranch(this.auth.getUserToken(), 'AgencyIdList');
        if (savedReleaseId) {
          this.request.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
          if (!this.request.release) {
            this.request.release = this.releases[0];
            saveBranch(this.auth.getUserToken(), 'AgencyIdList', this.request.release.releaseId);
          }
        } else {
          this.request.release = this.releases[0];
        }
      }

      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);

      this.onChange();
    });
  }

  getRelease(releaseNum: string): Release | undefined {
    for (const release of this.releases) {
      if (release.releaseNum === releaseNum) {
        return release;
      }
    }
    return undefined;
  }

  onPageChange(event: PageEvent) {
    this.loadAgencyIdList(true);
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), 'AgencyIdList', source.releaseId);
    }

    this.paginator.pageIndex = 0;
    this.loadAgencyIdList();
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

  loadAgencyIdList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getAgencyIdListList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.results.map((elm: AgencyIdList) => {
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
    const numRows = this.dataSource.data.filter(row => this.canSelect.indexOf(row.state) > -1 && !this.hasRevision(row) && row.owner.username === this.currentUser).length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.select(row));
  }

  select(row: AgencyIdList) {
    if (this.canSelect.indexOf(row.state) > -1 && !this.hasRevision(row) && row.owner.username === this.currentUser) {
      this.selection.select(row);
    }
  }

  toggle(row: AgencyIdList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: AgencyIdList) {
    return this.selection.isSelected(row);
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  createAgencyIdList() {
    this.service.create(this.request.release.releaseId)
      .subscribe(resp => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/agency_id_list/' + resp.manifestId);
      });
  }

  get showCreateAgencyIdListBtn(): boolean {
    const userToken = this.auth.getUserToken();
    if (userToken.role === 'developer') {
      return false;
    } else {
      if (this.request.release.releaseId === WorkingRelease.releaseId) {
        return false;
      } else {
        return this.request.release.state === 'Published';
      }
    }
    return true;
  }

  get showDiscardBtn(): boolean {
    return this.selection.selected.length > 0 ?
      this.selection.selected.filter(e => e.state === 'Deleted').length === 0 :
      false;
  }

  get showRestoreBtn(): boolean {
    return this.selection.selected.length > 0 ?
      this.selection.selected.filter(e => e.state === 'WIP').length === 0 :
      false;
  }

  openDialogAgencyIdListListDelete() {
    const agencyIdListIds = this.selection.selected.map(e => e.agencyIdListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Delete Agency Id ' + (agencyIdListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to delete selected agency id ' + (agencyIdListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Delete anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(...agencyIdListIds).subscribe(_ => {
            this.snackBar.open('Deleted', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadAgencyIdList();
          });
        }
      });
  }

  openDialogAgencyIdListListRestore() {
    const agencyIdListIds = this.selection.selected.map(e => e.agencyIdListManifestId);
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore Agency Id ' + (agencyIdListIds.length > 1 ? 'Lists' : 'List') + '?';
    dialogConfig.data.content = [
      'Are you sure you want to Restore selected agency Id ' + (agencyIdListIds.length > 1 ? 'lists' : 'list') + '?'
    ];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.restore(...agencyIdListIds).subscribe(_ => {
            this.snackBar.open('Restored', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadAgencyIdList();
          });
        }
      });
  }

  hasRevision(agencyIdList: AgencyIdList): boolean{
    return agencyIdList.prevAgencyIdListManifestId != undefined;
  }

  isEditable(item: AgencyIdList) {
    return item.owner.username === this.currentUser && item.state === 'WIP';
  }

  openTransferDialog(item: AgencyIdList, $event) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {role: this.auth.getUserToken().role};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnership(item.agencyIdListManifestId, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.loadAgencyIdList();
        });
      }
    });
  }

  delete(item: AgencyIdList, $event) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Delete agency id list?';
    dialogConfig.data.content = ['Are you sure you want to delete this agency id list?'];
    dialogConfig.data.action = 'Delete anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }

        this.loading = true;
        this.service.delete(item.agencyIdListManifestId)
          .pipe(
            finalize(() => {
              this.loading = false;
            })
          )
          .subscribe(_ => {
            this.loadAgencyIdList();
            this.snackBar.open('Deleted', '', {duration: 3000});
          }, error => {
          });
      });
  }

  onContextMenu($event: MouseEvent, item: AgencyIdList): void {
    let contextMenu;
    contextMenu = this.contextMenuDefault;
    if (item.owner.username === this.currentUser && item.state === 'Deleted') {
      contextMenu = this.contextMenuDeleted;
    } else if (this.isEditable(item)) {
      if (this.hasRevision(item)) {
        contextMenu = this.contextMenuEditable;
      } else {
        contextMenu = this.contextMenuEditableRevised;
      }
    }

    this.contextMenuService.show.next({
      contextMenu,
      event: $event,
      item,
    });

    $event.preventDefault();
    $event.stopPropagation();
  }

  openDetail(item: AgencyIdList, $event?) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }
    this.router.navigateByUrl('/agency_id_list/' + item.agencyIdListManifestId);
    return;
  }

  openDialogCcListRestore(item: AgencyIdList) {

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Restore agency id list';
    dialogConfig.data.content = [
      'Are you sure you want to Restore agency id list?'
    ];
    dialogConfig.data.action = 'Restore';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.restore(item.agencyIdListManifestId).subscribe(_ => {
            this.snackBar.open('Restored', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.loadAgencyIdList();
          });
        }
      });
  }

}

