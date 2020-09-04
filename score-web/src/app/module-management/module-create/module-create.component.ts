import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {ModuleService} from '../domain/module.service';
import {Module, ModuleDependency, SimpleModule} from '../domain/module';
import {MatTableDataSource} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {SelectionModel} from '@angular/cdk/collections';
import {hashCode} from '../../common/utility';
import {v4 as uuid} from 'uuid';
import {SimpleNamespace} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {ModuleDependencyDialogComponent} from '../module-dependency-dialog/module-dependency-dialog.component';

@Component({
  selector: 'score-module-create',
  templateUrl: './module-create.component.html',
  styleUrls: ['./module-create.component.css']
})
export class ModuleCreateComponent implements OnInit {

  title = 'Create Module';
  namespaces: SimpleNamespace[] = [];
  modules: SimpleModule[] = [];
  moduleMap: Map<number, String> = new Map();
  disabled: boolean;

  module: Module = new Module();
  hashCode;

  displayedColumns: string[] = [
    'select', 'dependencyType', 'module'
  ];

  dataSource = new MatTableDataSource<ModuleDependency>();
  selection = new SelectionModel<ModuleDependency>(true, []);

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: ModuleService,
              private namespaceService: NamespaceService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.disabled = false;

    this.namespaceService.getSimpleNamespaces().subscribe(resp => this.namespaces = resp);
    this.loadModules();

    this.hashCode = hashCode(this.module);
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }
  loadModules() {
    this.service.getSimpleModules().subscribe(resp => {
      this.modules = resp;
      for (const module of resp) {
        this.moduleMap.set(module.moduleId, module.module);
      }
    });
  }

  isChanged() {
    return this.hashCode !== hashCode(this.module);
  }

  isDisabled(module: Module) {
    return (this.disabled) ||
      (module.module === undefined || module.module === '');
  }

  getModule(moduleId: number) {
    return this.moduleMap.get(moduleId);
  }

  openDialog(moduleDependency?: ModuleDependency) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = {
      moduleDependency: new ModuleDependency(),
      modules: this.modules
    };

    if (moduleDependency) { // deep copy
      dialogConfig.data.moduleDependency = JSON.parse(JSON.stringify(moduleDependency));
    }

    const isAddAction: boolean = (moduleDependency === undefined);

    this.disabled = true;
    const dialogRef = this.dialog.open(ModuleDependencyDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined && result.dependencyType !== undefined && result.dependencyType !== '') {

        if (isAddAction) {
          const data = this.dataSource.data;
          result.guid = uuid();
          data.push(result);

          this._updateDataSource(data);
        } else {
          const newData = [];

          this.dataSource.data.forEach(row => {
            if (row.guid === result.guid) {
              newData.push(result);
            } else {
              newData.push(row);
            }
          });

          this._updateDataSource(newData);
        }

        this.disabled = false;
      } else {
        this.disabled = false;
      }
    });
  }

  _updateDataSource(data: ModuleDependency[]) {
    this.dataSource.data = data;
    this.module.moduleDependencies = data;
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

  select(row: ModuleDependency) {
    this.selection.select(row);
  }

  toggle(row: ModuleDependency) {
    if (this.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  isSelected(row: ModuleDependency) {
    return this.selection.isSelected(row);
  }

  removeModuleDependencies() {
    const newData = [];
    this.dataSource.data.forEach(row => {
      if (!this.selection.isSelected(row)) {
        newData.push(row);
      }
    });
    this.selection.clear();

    this._updateDataSource(newData);
  }

  back() {
    this.location.back();
  }

  create() {
    this.service.create(this.module).subscribe(_ => {
      this.hashCode = hashCode(this.module);
      this.snackBar.open('Created', '', {
        duration: 1000,
      });
    });
  }

  onPageChange(event: PageEvent) {
    this.onChange();
  }

  onChange() {
    this.loadModules();
  }

}
