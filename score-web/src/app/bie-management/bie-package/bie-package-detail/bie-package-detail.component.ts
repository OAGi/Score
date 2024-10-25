import {Component, HostListener, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {hashCode} from 'src/app/common/utility';
import {SelectionModel} from '@angular/cdk/collections';
import {finalize} from 'rxjs/operators';
import {saveAs} from 'file-saver';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatMultiSort, MatMultiSortTableDataSource, TableData} from 'ngx-mat-multi-sort';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {PageRequest} from '../../../basis/basis';
import {BieListInBiePackageRequest, BiePackage} from '../domain/bie-package';
import {BieList} from '../../bie-list/domain/bie-list';
import {BiePackageService} from '../domain/bie-package.service';
import {BiePackageAddBieDialogComponent} from '../bie-package-add-bie-dialog/bie-package-add-bie-dialog.component';
import {
  PreferencesInfo,
  TableColumnsInfo,
  TableColumnsProperty
} from '../../../settings-management/settings-preferences/domain/preferences';
import {ScoreTableColumnResizeDirective} from '../../../common/score-table-column-resize/score-table-column-resize.directive';
import {SettingsPreferencesService} from '../../../settings-management/settings-preferences/domain/settings-preferences.service';
import {forkJoin} from 'rxjs';

@Component({
  selector: 'score-bie-package-detail',
  templateUrl: './bie-package-detail.component.html',
  styleUrls: ['./bie-package-detail.component.css']
})
export class BiePackageDetailComponent implements OnInit {

  title = 'Edit BIE Package';
  biePackage: BiePackage = new BiePackage();
  schemaExpression = 'XML';
  hashCode;
  disabled: boolean;

  get columns(): TableColumnsProperty[] {
    if (!this.preferencesInfo) {
      return [];
    }
    return this.preferencesInfo.tableColumnsInfo.columnsOfBiePage;
  }

  set columns(columns: TableColumnsProperty[]) {
    if (!this.preferencesInfo) {
      return;
    }

    this.preferencesInfo.tableColumnsInfo.columnsOfBiePage = columns;
    this.updateTableColumnsForBiePage();
  }

  updateTableColumnsForBiePage() {
    this.preferencesService.updateTableColumnsForBiePage(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
    });
  }

  onColumnsReset() {
    const defaultTableColumnInfo = new TableColumnsInfo();
    this.columns = defaultTableColumnInfo.columnsOfBiePage;
    this.onColumnsChange(this.columns);
  }

  onColumnsChange(updatedColumns: { name: string; selected: boolean }[]) {
    const updatedColumnsWithWidth = updatedColumns.map(column => ({
      name: column.name,
      selected: column.selected,
      width: this.width(column.name)
    }));

    this.columns = updatedColumnsWithWidth;

    let columns = [];
    for (const tableColumn of this.table.columns) {
      for (const updatedColumn of updatedColumns) {
        if (tableColumn.name === updatedColumn.name) {
          tableColumn.isActive = updatedColumn.selected;
        }
      }
      columns.push(tableColumn);
    }

    this.table.columns = columns;
    this.table.displayedColumns = this.displayedColumns;
  }

  onResizeWidth($event) {
    switch ($event.name) {
      case 'Updated on':
        this.setWidth('Updated On', $event.width);
        break;

      default:
        this.setWidth($event.name, $event.width);
        break;
    }
  }

  setWidth(name: string, width: number | string) {
    const matched = this.columns.find(c => c.name === name);
    if (matched) {
      matched.width = width;
      this.updateTableColumnsForBiePage();
    }
  }

  width(name: string): number | string {
    if (!this.preferencesInfo) {
      return 0;
    }
    return this.columns.find(c => c.name === name)?.width;
  }

  defaultDisplayedColumns = [
    {id: 'select', name: '', isActive: true},
    {id: 'state', name: 'State', isActive: true},
    {id: 'branch', name: 'Branch', isActive: true},
    {id: 'den', name: 'DEN', isActive: true},
    {id: 'owner', name: 'Owner', isActive: true},
    {id: 'businessContexts', name: 'Business Contexts', isActive: true},
    {id: 'version', name: 'Version', isActive: true},
    {id: 'status', name: 'Status', isActive: true},
    {id: 'bizTerm', name: 'Business Term', isActive: true},
    {id: 'remark', name: 'Remark', isActive: true},
    {id: 'lastUpdateTimestamp', name: 'Updated on', isActive: true}
  ];

  get displayedColumns(): string[] {
    let displayedColumns = ['select'];
    if (this.preferencesInfo) {
      for (const column of this.columns) {
        switch (column.name) {
          case 'State':
            if (column.selected) {
              displayedColumns.push('state');
            }
            break;
          case 'Branch':
            if (column.selected) {
              displayedColumns.push('branch');
            }
            break;
          case 'DEN':
            if (column.selected) {
              displayedColumns.push('den');
            }
            break;
          case 'Owner':
            if (column.selected) {
              displayedColumns.push('owner');
            }
            break;
          case 'Business Contexts':
            if (column.selected) {
              displayedColumns.push('businessContexts');
            }
            break;
          case 'Version':
            if (column.selected) {
              displayedColumns.push('version');
            }
            break;
          case 'Status':
            if (column.selected) {
              displayedColumns.push('status');
            }
            break;
          case 'Business Term':
            if (column.selected) {
              displayedColumns.push('bizTerm');
            }
            break;
          case 'Remark':
            if (column.selected) {
              displayedColumns.push('remark');
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

  table: TableData<BieList>;
  selection = new SelectionModel<BieList>(true, []);
  request: BieListInBiePackageRequest;
  preferencesInfo: PreferencesInfo;
  loading = false;

  @ViewChild(MatMultiSort, {static: true}) sort: MatMultiSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChildren(ScoreTableColumnResizeDirective) tableColumnResizeDirectives: QueryList<ScoreTableColumnResizeDirective>;

  constructor(private biePackageService: BiePackageService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private dialog: MatDialog,
              public webPageInfo: WebPageInfoService) {
  }

  ngOnInit(): void {
    this.table = new TableData<BieList>(this.defaultDisplayedColumns, {});
    this.table.dataSource = new MatMultiSortTableDataSource<BieList>(this.sort, false);

    // Init BIE list table for BIE package
    this.request = new BieListInBiePackageRequest(this.route.snapshot.queryParamMap,
      new PageRequest(['lastUpdateTimestamp'], ['desc'], 0, 10));
    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.table.sortParams = this.request.page.sortActives;
    this.table.sortDirs = this.request.page.sortDirections;
    // Prevent the sorting event from being triggered if any columns are currently resizing.
    const originalSort = this.sort.sort;
    this.sort.sort = (sortChange) => {
      if (this.tableColumnResizeDirectives &&
        this.tableColumnResizeDirectives.filter(e => e.resizing).length > 0) {
        return;
      }
      originalSort.apply(this.sort, [sortChange]);
    };
    this.table.sortObservable.subscribe(() => {
      this.onSearch();
    });

    this.request.page = new PageRequest(
      this.table.sortParams, this.table.sortDirs,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.request.biePackageId = this.route.snapshot.params.id;

    forkJoin([
      this.biePackageService.get(this.request.biePackageId),
      this.preferencesService.load(this.auth.getUserToken())
    ]).subscribe(([biePackage, preferencesInfo]) => {
      this.preferencesInfo = preferencesInfo;
      this.onColumnsChange(this.preferencesInfo.tableColumnsInfo.columnsOfBiePage);

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

  onSearch() {
    this.paginator.pageIndex = 0;
    this.loadBieListInBiePackage();
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

  generate() {
    const selectedBieLists = this.selection.selected;
    let topLevelAsbiepIdList;
    if (selectedBieLists === undefined || selectedBieLists.length === 0) {
      topLevelAsbiepIdList = [];
    } else {
      topLevelAsbiepIdList = selectedBieLists.map(e => e.topLevelAsbiepId);
    }

    this.loading = true;
    this.biePackageService.generateBiePackage(
      this.biePackage.biePackageId, {
        schemaExpression: this.schemaExpression
      }, ...topLevelAsbiepIdList).subscribe(resp => {
      const blob = new Blob([resp.body], {type: resp.headers.get('Content-Type')});
      saveAs(blob, this._getFilenameFromContentDisposition(resp));

      this.loading = false;
    }, err => {
      this.loading = false;
      throw err;
    });
  }

  _getFilenameFromContentDisposition(resp) {
    const contentDisposition = resp.headers.get('Content-Disposition') || '';
    const matches = /filename=([^;]+)/ig.exec(contentDisposition);
    return (matches[1] || 'untitled').replace(/\"/gi, '').trim();
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
