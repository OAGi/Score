import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {OagisComponentType, OagisComponentTypes} from '../domain/core-component-node';
import {CcListService} from './domain/cc-list.service';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {SelectionModel} from '@angular/cdk/collections';
import {CcList, CcListRequest} from './domain/cc-list';
import {PageRequest} from '../../basis/basis';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {Release} from '../../bie-management/bie-create/domain/bie-create-list';
import {ReleaseService} from '../../release-management/domain/release.service';
import {SimpleRelease} from '../../release-management/domain/release';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {MatDatepickerInputEvent} from '@angular/material/datepicker';
import {AuthService} from '../../authentication/auth.service';
import {TransferOwnershipDialogComponent} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AccountList} from '../../account-management/domain/accounts';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../common/utility';
import {finalize} from 'rxjs/operators';
import {ActivatedRoute, Router} from '@angular/router';
import {ContextMenuComponent, ContextMenuService} from 'ngx-contextmenu';
import {MatSnackBar} from '@angular/material/snack-bar';

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

  title = 'Core Component';

  typeList: string[] = ['ACC', 'ASCCP', 'BCCP', 'ASCC', 'BCC'];
  stateList: string[] = ['Editing', 'Candidate', 'Published'];
  componentTypeList: OagisComponentType[] = OagisComponentTypes;

  displayedColumns: string[] = [
    'type', 'state', 'den', 'revision', 'owner', 'transferOwnership', 'module', 'lastUpdateTimestamp', 'more'
  ];
  data: CcList[] = [];
  selection = new SelectionModel<CcList>(true, []);
  expandedElement: CcList | null;
  loading = false;

  releases: Release[];
  loginIdList: string[] = [];
  loginIdListFilterCtrl: FormControl = new FormControl();
  updaterIdListFilterCtrl: FormControl = new FormControl();
  filteredLoginIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  filteredUpdaterIdList: ReplaySubject<string[]> = new ReplaySubject<string[]>(1);
  request: CcListRequest;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild(ContextMenuComponent, {static: true}) public contextMenu: ContextMenuComponent;

  constructor(private service: CcListService,
              private releaseService: ReleaseService,
              private accountService: AccountListService,
              private auth: AuthService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private contextMenuService: ContextMenuService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
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

    this.releases = [];
    const workingRelease = new SimpleRelease();
    workingRelease.releaseId = 0;
    workingRelease.releaseNum = 'Working';
    this.releases.push(workingRelease);

    this.releaseService.getSimpleReleases().subscribe(releases => this.releases.push(...releases));

    this.accountService.getAccountNames().subscribe(loginIds => {
      this.loginIdList.push(...loginIds);
      initFilter(this.loginIdListFilterCtrl, this.filteredLoginIdList, this.loginIdList);
      initFilter(this.updaterIdListFilterCtrl, this.filteredUpdaterIdList, this.loginIdList);
    });

    this.loadCcList(true);
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
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
      this.data = resp.list.map((elm: CcList) => {
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
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.data = [];
    });
  }

  onPageChange(event: PageEvent) {
    this.loadCcList();
  }

  onChange() {
    this.paginator.pageIndex = 0;
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

  getRouterLink(ccList: CcList) {
    switch (ccList.type) {
      case 'ACC':
        if (ccList.oagisComponentType === 'UserExtensionGroup') {
          if (this.request.releaseId === 0) {
            return 'extension/1/' + ccList.id;
          } else {
            return 'extension/' + this.request.releaseId + '/' + ccList.currentId;
          }
        } else {
          return 'acc/' + this.request.releaseId + '/' + ccList.id;
        }

      case 'ASCCP':
        return 'asccp/' + this.request.releaseId + '/' + ccList.id;

      case 'BCCP':
        return 'bccp/' + this.request.releaseId + '/' + ccList.id;

      default:
        return window.location.pathname;
    }
  }

  isEditable(element: CcList) {
    return element.type === 'ACC' &&
      element.oagisComponentType === 'UserExtensionGroup' &&
      element.owner === this.currentUser &&
      element.state === 'Editing';
  }

  onContextMenu($event: MouseEvent, item: CcList): void {
    if (!this.isEditable(item)) {
      return;
    }

    this.contextMenuService.show.next({
      contextMenu: this.contextMenu,
      event: $event,
      item: item,
    });

    $event.preventDefault();
    $event.stopPropagation();
  }

  openTransferDialog(extensionId: number, $event) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnership(extensionId, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 1000,
          });
          this.selection.clear();
          this.loadCcList();
        });
      }
    });
  }

}
