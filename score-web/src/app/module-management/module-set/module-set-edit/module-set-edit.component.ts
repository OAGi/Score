import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDialog} from '@angular/material/dialog';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {ModuleService} from '../../domain/module.service';
import {ModuleSet, ModuleSetModule, ModuleSetModuleListRequest} from '../../domain/module';
import {finalize, switchMap} from 'rxjs/operators';
import {hashCode} from '../../../common/utility';
import {MatTableDataSource} from '@angular/material/table';
import {SelectionModel} from '@angular/cdk/collections';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {PageRequest} from '../../../basis/basis';

@Component({
  selector: 'score-module-set-edit',
  templateUrl: './module-set-edit.component.html',
  styleUrls: ['./module-set-edit.component.css']
})
export class ModuleSetEditComponent implements OnInit {

  title = 'Edit Module Set';
  isUpdating: boolean;
  moduleSet: ModuleSet = new ModuleSet();
  private $hashCode: string;

  request: ModuleSetModuleListRequest;
  displayedColumns: string[] = [
    'select', 'path', 'namespaceUri', 'assigned', 'lastUpdateTimestamp'
  ];

  dataSource = new MatTableDataSource<ModuleSetModule>();
  selection = new SelectionModel<number>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: ModuleService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit(): void {
    this.request = new ModuleSetModuleListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('lastUpdateTimestamp', 'desc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0;

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.loadModuleSetModuleList();
    });

    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        const moduleSetId = Number(params.get('moduleSetId'));
        this.request.moduleSetId = moduleSetId;
        return this.service.getModuleSet(this.request.moduleSetId);
      }))
      .subscribe(moduleSet => {
        this.init(moduleSet);
        this.loadModuleSetModuleList(true);
      });
  }

  init(moduleSet: ModuleSet) {
    this.moduleSet = moduleSet;
    this.$hashCode = hashCode(this.moduleSet);
  }

  get isChanged(): boolean {
    return hashCode(this.moduleSet) !== this.$hashCode;
  }

  updateModuleSet() {
    if (!this.isChanged) {
      return;
    }

    this.isUpdating = true;

    this.service.updateModuleSet(this.moduleSet)
      .pipe(finalize(() => {
        this.isUpdating = false;
      }))
      .subscribe(_ => {
        this.init(this.moduleSet);
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      });
  }

  onPageChange(event: PageEvent) {
    this.loadModuleSetModuleList();
  }

  loadModuleSetModuleList(isInit?: boolean) {
    this.isUpdating = true;

    this.request.page = new PageRequest(
      this.sort.active, this.sort.direction,
      this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getModuleSetModuleList(this.request).pipe(
      finalize(() => {
        this.isUpdating = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.paginator.pageIndex = resp.page;

      this.dataSource.data = resp.results.map((elm: ModuleSetModule) => {
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

  select(row: ModuleSetModule) {
    this.selection.select(row.moduleId);
  }

  toggle(row: ModuleSetModule) {
    if (this.isSelected(row)) {
      this.selection.deselect(row.moduleId);
    } else {
      this.select(row);
    }
  }

  isSelected(row: ModuleSetModule) {
    return this.selection.isSelected(row.moduleId);
  }

}
