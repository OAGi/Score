import {Component, OnInit, ViewChild} from '@angular/core';
import {ModuleService} from '../domain/module.service';
import {MatPaginator, MatSnackBar, MatSort, MatTableDataSource} from '@angular/material';
import {SelectionModel} from '@angular/cdk/collections';
import {Router} from '@angular/router';
import {ModuleList} from '../domain/module';

@Component({
  selector: 'srt-module-list',
  templateUrl: './module-list.component.html',
  styleUrls: ['./module-list.component.css']
})
export class ModuleListComponent implements OnInit {

  title = 'Module';

  displayedColumns: string[] = [
    'module', 'namespace', 'owner', 'lastUpdatedBy', 'lastUpdateTimestamp', 'sinceRelease'
  ];
  dataSource = new MatTableDataSource<ModuleList>();
  selection = new SelectionModel<ModuleList>(true, []);
  loading = false;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: ModuleService,
              private snackBar: MatSnackBar,
              private router: Router) {
  }

  ngOnInit() {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.dataSource.filterPredicate = (data: ModuleList, filter: string) => {
      return (data.module.indexOf(filter) !== -1) ||
        (data.module && data.module.indexOf(filter) !== -1);
    };

    this.loadCodeList();
  }

  loadCodeList() {
    this.loading = true;
    this.service.getModuleList().subscribe(resp => {
      resp = resp.map((elm: ModuleList) => {
        elm.lastUpdateTimestamp = new Date(elm.lastUpdateTimestamp);
        return elm;
      });

      this.dataSource.data = resp;
      this.loading = false;
    });
  }

  applyFilter(filterValue: string) {
    this.dataSource.filter = filterValue.trim();
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

  select(row) {
    this.selection.select(row);
  }

  toggle(row) {
    if (this.selection.isSelected(row)) {
      this.selection.deselect(row);
    } else {
      this.select(row);
    }
  }

  create() {
    this.router.navigateByUrl('/module/create');
  }

}
