import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {hashCode} from 'src/app/common/utility';
import {SelectionModel} from '@angular/cdk/collections';
import {finalize} from 'rxjs/operators';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatMultiSort, MatMultiSortTableDataSource, TableData} from 'ngx-mat-multi-sort';
import {AccountListService} from '../../../account-management/domain/account-list.service';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {PageRequest} from '../../../basis/basis';
import {BieListInBiePackageRequest, BiePackage} from '../domain/bie-package';
import {BieList} from '../../bie-list/domain/bie-list';
import {BiePackageService} from '../domain/bie-package.service';
import {BieListService} from '../../bie-list/domain/bie-list.service';
import {BiePackageAddBieDialogComponent} from '../bie-package-add-bie-dialog/bie-package-add-bie-dialog.component';
import {BieEditAbieNode} from '../../bie-edit/domain/bie-edit-node';

@Component({
  selector: 'score-bie-package-detail',
  templateUrl: './bie-package-detail.component.html',
  styleUrls: ['./bie-package-detail.component.css']
})
export class BiePackageDetailComponent implements OnInit {

  title = 'Edit BIE Package';
  biePackage: BiePackage = new BiePackage();
  hashCode;
  disabled: boolean;
  displayedColumns = [
    {id: 'select', name: ''},
    {id: 'state', name: 'State'},
    {id: 'branch', name: 'Branch'},
    {id: 'den', name: 'DEN'},
    {id: 'owner', name: 'Owner'},
    {id: 'businessContexts', name: 'Business Contexts'},
    {id: 'version', name: 'Version'},
    {id: 'status', name: 'Status'},
    {id: 'bizTerm', name: 'Business Term'},
    {id: 'remark', name: 'Remark'},
    {id: 'lastUpdateTimestamp', name: 'Updated on'},
  ];

  table: TableData<BieList>;
  selection = new SelectionModel<BieList>(true, []);
  request: BieListInBiePackageRequest;
  loading = false;
  @ViewChild(MatMultiSort, {static: true}) sort: MatMultiSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private biePackageService: BiePackageService,
              private bieListService: BieListService,
              private accountService: AccountListService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService,
              private dialog: MatDialog,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit(): void {
    const localStorageKey = 'X-Score-Table[BiePackageDetail]';
    const value = JSON.parse(localStorage.getItem(localStorageKey)!);
    this.table = new TableData<BieList>((value) ? value._columns : this.displayedColumns, {localStorageKey: localStorageKey});
    this.table.dataSource = new MatMultiSortTableDataSource<BieList>(this.sort, false);

    // Init BIE list table for OasDoc
    this.request = new BieListInBiePackageRequest(this.route.snapshot.queryParamMap,
      new PageRequest(['lastUpdateTimestamp'], ['desc'], 0, 10));
    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.table.sortParams = this.request.page.sortActives;
    this.table.sortDirs = this.request.page.sortDirections;
    this.table.sortObservable.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadBieListInBiePackage();
    });

    this.request.page = new PageRequest(
      this.table.sortParams, this.table.sortDirs,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.request.biePackageId = this.route.snapshot.params.id;
    this.biePackageService.get(this.request.biePackageId).subscribe(biePackage => {
      this.init(biePackage);
      this.loadBieListInBiePackage(true);
    }, err => {
      this.loading = false;
      let errorMessage;
      if (err.status === 403) {
        errorMessage = 'You do not have access permission.';
      } else {
        errorMessage = 'Something\'s wrong.';
      }
      this.snackBar.open(errorMessage, '', {
        duration: 3000
      });
      this.router.navigateByUrl('/bie_package');
    });
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  init(biePackage: BiePackage) {
    this.hashCode = hashCode(biePackage);
    this.biePackage = biePackage;
    this.loading = false;
  }

  getPath(commands?: any[]): string {
    const urlTree = this.router.createUrlTree(commands);
    const path = this.location.prepareExternalUrl(urlTree.toString());
    return window.location.origin + path;
  }

  loadBieListInBiePackage(isInit?: boolean) {
    this.loading = true;

    this.request.page = new PageRequest(
      this.table.sortParams, this.table.sortDirs,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.biePackageService.getBieListInBiePackage(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.table.dataSource.data = resp.list.map((elm: BieList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });

      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.table.dataSource.data = [];
    });
  }

  onPageChange(event: PageEvent) {
    this.loadBieListInBiePackage();
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

  openDialog($event: any) {
    $event.preventDefault();
    $event.stopPropagation();

    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = this.table.dataSource.data;
    dialogConfig.data.releaseId = this.biePackage.releaseId;
    dialogConfig.data.webPageInfo = this.webPageInfo;
    dialogConfig.width = '100%';
    dialogConfig.maxWidth = '100%';
    dialogConfig.height = '100%';
    dialogConfig.maxHeight = '100%';
    dialogConfig.autoFocus = false;

    this.loading = true;
    const dialogRef = this.dialog.open(BiePackageAddBieDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(topLevelAsbiepIdList => {
      if (!topLevelAsbiepIdList) {
        this.loading = false;
        return;
      }

      this.biePackageService.addBieToBiePackage(this.biePackage.biePackageId, ...topLevelAsbiepIdList).subscribe(_ => {
        this.snackBar.open('Added', '', {
          duration: 3000,
        });
        this.loadBieListInBiePackage();

        this.loading = false;
      }, err => {
        this.loading = false;
        throw err;
      });
    }, err => {
      this.loading = false;
      throw err;
    });
  }

  select(row: BieList) {
    this.selection.select(row);
  }

  isSelected(row: BieList) {
    return this.selection.isSelected(row);
  }

  toggle(row: BieList) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  removeBieInBiePackage() {
    const bieLists = this.selection.selected;
    this.biePackageService.deleteBieInBiePackage(
      this.biePackage.biePackageId, ...bieLists.map(e => e.topLevelAsbiepId)).subscribe(_ => {
      this.snackBar.open('Removed', '', {
        duration: 3000,
      });

      this.loadBieListInBiePackage();
    });
  }

  isChanged(): boolean {
    if (!this.biePackage) {
      return false;
    }
    return this.hashCode !== hashCode(this.biePackage);
  }

  isDisabled(biePackage: BiePackage) {
    return (this.disabled) ||
      (biePackage.versionId === undefined || biePackage.versionId === '') ||
      (biePackage.versionName === undefined || biePackage.versionName === '') ||
      (biePackage.description === undefined || biePackage.description === '');
  }

  update() {
    if (this.hashCode !== hashCode(this.biePackage)) {
      this.biePackageService.update(this.biePackage).subscribe(_ => {
        this.init(this.biePackage);
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      });
    }
  }

  updateState(state: string) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Update state to \'' + state + '\'?';
    dialogConfig.data.content = ['Are you sure you want to update the state to \'' + state + '\'?'];
    dialogConfig.data.action = 'Update';

    this.loading = true;
    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (!result) {
          this.loading = false;
          return;
        }
        this.biePackageService.updateState(this.biePackage.biePackageId, state).subscribe(_ => {
          this.snackBar.open('State updated', '', {
            duration: 3000,
          });

          this.biePackageService.get(this.request.biePackageId).subscribe(biePackage => {
            this.init(biePackage);
          });

          this.loading = false;
        }, err => {
          this.loading = false;
          throw err;
        });
      });
  }

  discard() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard BIE Package?';
    dialogConfig.data.content = [
      'Are you sure you want to discard selected BIE package?',
      'The BIE package will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.biePackageService.delete(this.biePackage.biePackageId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.selection.clear();
            this.router.navigateByUrl('/bie_package');
          });
        }
      });
  }

}
