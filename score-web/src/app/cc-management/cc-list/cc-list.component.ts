import {SelectionModel} from '@angular/cdk/collections';
import {Component, OnInit, ViewChild} from '@angular/core';
import {faFlask} from '@fortawesome/free-solid-svg-icons';
import {forkJoin, ReplaySubject} from 'rxjs';
import {CreateBdtDialogComponent} from './create-bdt-dialog/create-bdt-dialog.component';
import {CreateBodDialogComponent} from './create-bod-dialog/create-bod-dialog.component';
import {CcListService} from './domain/cc-list.service';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort, SortDirection} from '@angular/material/sort';
import {CcList, CcListRequest} from './domain/cc-list';
import {PageRequest} from '../../basis/basis';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {Release} from '../../bie-management/bie-create/domain/bie-create-list';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {AuthService} from '../../authentication/auth.service';
import {
  TransferOwnershipDialogComponent
} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AccountList} from '../../account-management/domain/accounts';
import {CcNodeService} from '../domain/core-component-node.service';
import {ActivatedRoute, Router} from '@angular/router';
import {CreateAsccpDialogComponent} from './create-asccp-dialog/create-asccp-dialog.component';
import {CreateBccpDialogComponent} from './create-bccp-dialog/create-bccp-dialog.component';
import {MatTableDataSource} from '@angular/material/table';
import {FormControl} from '@angular/forms';
import {initFilter, loadBranch, saveBranch} from '../../common/utility';
import {WorkingRelease} from '../../release-management/domain/release';
import {OagisComponentType, OagisComponentTypes} from '../domain/core-component-node';
import {finalize} from 'rxjs/operators';
import {Location} from '@angular/common';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {CreateVerbDialogComponent} from './create-verb-dialog/create-verb-dialog.component';
import {AboutService} from '../../basis/about/domain/about.service';
import {TagService} from '../../tag-management/domain/tag.service';
import {Tag} from '../../tag-management/domain/tag';

@Component({
  selector: 'score-cc-list',
  templateUrl: './cc-list.component.html',
  styleUrls: ['./cc-list.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class CcListComponent implements OnInit {

  faFlask = faFlask;
  title = 'Core Component';

  typeList: string[] = ['ACC', 'ASCCP', 'BCCP', 'CDT', 'BDT', 'ASCC', 'BCC'];
  workingStateList = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
  releaseStateList = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];
  componentTypeList: OagisComponentType[] = OagisComponentTypes;
  workingRelease = WorkingRelease;

  displayedColumns: string[] = [
    'select', 'type', 'state', 'den', 'valueDomain', 'sixDigitId', 'revision', 'owner', 'transferOwnership', 'module', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<CcList>();
  selection = new SelectionModel<CcList>(true, []);
  expandedElement: CcList | null;
  loading = false;

  isElasticsearchOn = false;

  releases: Release[] = [];
  loginIdList: string[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<Release[]> = new ReplaySubject<Release[]>(1);
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: CcListRequest;
  tags: Tag[] = [];

  contextMenuItem: CcList;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: CcListService,
              private nodeService: CcNodeService,
              private releaseService: ReleaseService,
              private accountService: AccountListService,
              private auth: AuthService,
              private aboutService: AboutService,
              private tagService: TagService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
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

  ngOnInit() {
    // Init CcList table
    this.request = new CcListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadCcList();
    });

    this.loading = true;
    forkJoin([
      this.releaseService.getSimpleReleases(['Draft', 'Published']),
      this.accountService.getAccountNames(),
      this.aboutService.getProductInfo(),
      this.tagService.getTags()
    ]).subscribe(([releases, loginIds, productInfos, tags]) => {
      for (const productInfo of productInfos) {
        if (productInfo.productName === 'Elasticsearch' && productInfo.productVersion !== '0.0.0.0') {
          this.isElasticsearchOn = true;
        }
      }
      this.request.fuzzySearch = this.isElasticsearchOn;

      this.releases.push(...releases);
      if (this.releases.length > 0) {
        if (this.request.release.releaseId === 0) {
          const savedReleaseId = loadBranch(this.auth.getUserToken(), this.request.cookieType);
          if (savedReleaseId) {
            this.request.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
            if (!this.request.release) {
              this.request.release = this.releases[0];
              saveBranch(this.auth.getUserToken(), this.request.cookieType, this.request.release.releaseId);
            }
          } else {
            this.request.release = this.releases[0];
          }
        } else {
          this.request.release = this.releases.filter(e => e.releaseId === this.request.release.releaseId)[0];
        }
      }
      initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
      this.tags = tags;

      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
      this.loadCcList(true);
    }, error => {
      this.loading = false;
    });
  }

  loadCcList(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getCcList(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.paginator.pageIndex = resp.page;

      this.dataSource.data = resp.list.map((elm: CcList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        if (this.request.filters.module.length > 0) {
          elm.module = elm.module.replace(
            new RegExp(this.request.filters.module, 'ig'),
            '<b class="bg-warning">$&</b>');
        }
        if (this.request.filters.definition.length > 0) {
          elm.definition = elm.definition.replace(
            new RegExp(this.request.filters.definition, 'ig'),
            '<b class="bg-warning">$&</b>');
        }
        return elm;
      });
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
      this.selection.clear();
    }, error => {
      this.dataSource.data = [];
    });
  }

  onPageChange(event: PageEvent) {
    this.loadCcList();
  }

  onChange(property?: string, source?) {
    if (property === 'branch') {
      saveBranch(this.auth.getUserToken(), this.request.cookieType, source.releaseId);
    }
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

  getRouterLink(ccList: CcList) {
    switch (ccList.type.toUpperCase()) {
      case 'ACC':
        if (ccList.oagisComponentType === 'UserExtensionGroup') {
          return 'extension/' + ccList.manifestId;
        } else {
          return 'acc/' + ccList.manifestId;
        }
      case 'ASCCP':
        return 'asccp/' + ccList.manifestId;

      case 'BCCP':
        return 'bccp/' + ccList.manifestId;

      case 'DT':
        return 'dt/' + ccList.manifestId;

      default:
        return window.location.pathname;
    }
  }

  isEditable(element: CcList) {
    return element.owner === this.currentUser && element.state === 'WIP';
  }

  isAssociation(element: CcList) {
    return element.type.toUpperCase() === 'ASCC' || element.type.toUpperCase() === 'BCC';
  }

  openTransferDialog(item: CcList, $event) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnership(item.type, item.manifestId, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.loadCcList();
        });
      }
    });
  }

  openTransferDialogMultiple() {
    if (this.selection.selected.length === 0) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnershipOnList(this.selection.selected, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.loadCcList();
        });
      }
    });
  }

  multipleUpdate(action: string) {
    if (this.selection.selected.length === 0) {
      return;
    }
    const dialogConfig = this.confirmDialogService.newConfig();
    let notiMsg = 'Updated';
    let toState = action;
    let actionType = 'Update';

    switch (action) {
      case 'WIP':
      case 'Draft':
      case 'QA':
      case 'Candidate':
      case 'Production':
        dialogConfig.data.header = 'Update state to \'' + action + '\'?';
        dialogConfig.data.content = ['Are you sure you want to update the state to \'' + action + '\'?'];
        dialogConfig.data.action = 'Update';
        break;
      case 'Delete':
        toState = 'Deleted';
        notiMsg = 'Deleted';
        actionType = 'Delete';
        dialogConfig.data.header = action + ' Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?';
        dialogConfig.data.content = [
          'Are you sure you want to ' + action.toLowerCase() + ' selected Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?'
        ];
        dialogConfig.data.action = action;
        break;
      case 'Restore':
        toState = 'WIP';
        notiMsg = 'Restored';
        actionType = 'Restore';
        dialogConfig.data.header = action + ' Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?';
        dialogConfig.data.content = [
          'Are you sure you want to ' + action.toLowerCase() + ' selected Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?'
        ];
        dialogConfig.data.action = action;
        break;
      case 'Purge':
        toState = 'Purge';
        notiMsg = 'Purged';
        actionType = 'Purge';
        dialogConfig.data.header = action + ' Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?';
        dialogConfig.data.content = [
          'Are you sure you want to ' + action.toLowerCase() + ' selected Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?'
        ];
        dialogConfig.data.action = action;
        break;
      default:
        return false;
    }

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          return;
        }
        this.loading = true;
        this.service.updateStateOnList(actionType, toState, this.selection.selected)
          .pipe(
            finalize(() => {
              this.loading = false;
            })
          )
          .subscribe(_ => {
            this.loadCcList();
            this.snackBar.open(notiMsg, '', {
              duration: 3000
            });
            this.selection.clear();
          }, error => {
          });
      });
  }

  hasCreatePermission(): boolean {
    const userToken = this.auth.getUserToken();
    if (this.request.release.state !== 'Published') {
      return false;
    }
    if (userToken.roles.includes('developer')) {
      if (this.request.release.releaseId !== WorkingRelease.releaseId) {
        return false;
      }
    } else {
      if (this.request.release.releaseId === WorkingRelease.releaseId) {
        return false;
      }
    }
    return true;
  }

  createAcc() {
    this.loading = true;
    this.nodeService.createAcc(this.request.release.releaseId).pipe(finalize(() => {
      this.loading = false;
    })).subscribe(resp => {
      return this.router.navigateByUrl('/core_component/acc/' + resp.manifestId);
    });
  }

  createAsccp(asccpType: string) {
    let component;
    if ('Verb' === asccpType) {
      component = CreateVerbDialogComponent;
    } else {
      component = CreateAsccpDialogComponent;
    }

    const dialogRef = this.dialog.open(component, {
      data: {
        releaseId: this.request.release.releaseId,
        action: 'create',
        stateList: (this.request.release.releaseId === this.workingRelease.releaseId)
          ? this.workingStateList : this.releaseStateList
      },
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    });

    dialogRef.afterClosed().pipe(finalize(() => {
      this.loading = false;
    })).subscribe(roleOfAcc => {
      if (!roleOfAcc.manifestId || !roleOfAcc.objectClassTerm) {
        return;
      }

      this.loading = true;
      const initialPropertyTerm = roleOfAcc.objectClassTerm;
      this.nodeService.createAsccp(this.request.release.releaseId, roleOfAcc.manifestId, initialPropertyTerm, asccpType)
        .pipe(finalize(() => {
          this.loading = false;
        })).subscribe(resp => {
        return this.router.navigateByUrl('/core_component/asccp/' + resp.manifestId);
      });
    });
  }

  createBccp() {
    this.loading = true;

    const dialogRef = this.dialog.open(CreateBccpDialogComponent, {
      data: {
        releaseId: this.request.release.releaseId,
        action: 'create',
        stateList: (this.request.release.releaseId === this.workingRelease.releaseId)
          ? this.workingStateList : this.releaseStateList
      },
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    });

    dialogRef.afterClosed().pipe(finalize(() => {
      this.loading = false;
    })).subscribe(bdtManifestId => {
      if (!bdtManifestId) {
        return;
      }

      this.loading = true;
      this.nodeService.createBccp(this.request.release.releaseId, bdtManifestId).pipe(finalize(() => {
        this.loading = false;
      })).subscribe(resp => {
        return this.router.navigateByUrl('/core_component/bccp/' + resp.manifestId);
      });
    });
  }

  createBdt() {
    this.loading = true;

    const dialogRef = this.dialog.open(CreateBdtDialogComponent, {
      data: {
        releaseId: this.request.release.releaseId,
        action: 'create',
        stateList: (this.request.release.releaseId === this.workingRelease.releaseId)
          ? this.workingStateList : this.releaseStateList
      },
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    });

    dialogRef.afterClosed().pipe(finalize(() => {
      this.loading = false;
    })).subscribe(data => {
      if (!data) {
        return;
      }

      this.loading = true;
      this.nodeService.createBdt(this.request.release.releaseId, data.manifestId, data.specId).pipe(finalize(() => {
        this.loading = false;
      })).subscribe(resp => {
        return this.router.navigateByUrl('/core_component/dt/' + resp.manifestId);
      });
    });
  }

  createBOD() {
    if (this.request.release.releaseId !== this.workingRelease.releaseId) {
      return;
    }
    this.loading = true;

    const dialogRef = this.dialog.open(CreateBodDialogComponent, {
      data: {
        releaseId: this.request.release.releaseId,
        action: 'create',
      },
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(pair => {
      if (!pair) {
        this.loading = false;
        return;
      }
      this.loading = true;
      this.nodeService.createBOD(pair.verbManifestIdList, pair.nounManifestIdList).pipe(finalize(() => {
        this.loading = false;
      })).subscribe(resp => {
        if (resp.manifestIdList.length === 1) {
          this.snackBar.open('A new BOD created.', '', {
            duration: 3000,
          });
        } else {
          this.snackBar.open(resp.manifestIdList.length + ' new BODs created.', '', {
            duration: 3000,
          });
        }

        this.loadCcList();
      });
    });
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.filter(e => ['ASCC', 'BCC'].indexOf(e.type) === -1).length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => {
        if (['ASCC', 'BCC'].indexOf(row.type) === -1) {
          this.select(row);
        }
      });
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

  openDetail(ccList: CcList, $event?) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    this.router.navigateByUrl('/core_component/' + this.getRouterLink(ccList));
    return;
  }

  isInitialRevision(ccList: CcList): boolean {
    return !!ccList && ccList.revision === '1';
  }

  canToolbarAction(action: string) {
    if (this.selection.selected.length === 0) {
      return false;
    }
    switch (action) {
      case 'BackWIP':
        return this.selection.selected.filter(e => {
          if (['Draft', 'QA', 'Candidate'].indexOf(e.state) > -1 && e.owner === this.currentUser) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Draft':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner === this.currentUser && e.releaseNum === this.workingRelease.releaseNum) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'QA':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner === this.currentUser && e.releaseNum !== this.workingRelease.releaseNum) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Transfer':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner === this.currentUser) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Candidate':
        return this.selection.selected.filter(e => {
          if (e.state === 'Draft' && e.owner === this.currentUser && e.releaseNum === this.workingRelease.releaseNum) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Production':
        return this.selection.selected.filter(e => {
          if (e.state === 'QA' && e.owner === this.currentUser && e.releaseNum !== this.workingRelease.releaseNum) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Restore':
        return this.selection.selected.filter(e => {
          if (e.state === 'Deleted') {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Delete':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner === this.currentUser && this.isInitialRevision(e)) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Purge':
        return this.selection.selected.filter(e => {
          if (e.state === 'Deleted') {
            return e;
          }
        }).length === this.selection.selected.length;
      default :
        return false;
    }
  }

  displayType(elem: CcList): string {
    return (elem.type.toUpperCase() === 'DT') ? elem.dtType : elem.type;
  }
}
