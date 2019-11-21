import {Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, MatSnackBar, MatSort, MatTableDataSource} from '@angular/material';
import {SelectionModel} from '@angular/cdk/collections';
import {Router} from '@angular/router';
import {NamespaceList} from '../domain/namespace';
import {NamespaceService} from '../domain/namespace.service';

@Component({
  selector: 'srt-namespace-list',
  templateUrl: './namespace-list.component.html',
  styleUrls: ['./namespace-list.component.css']
})
export class NamespaceListComponent implements OnInit {

  title = 'Namespace';

  displayedColumns: string[] = [
    'uri', 'prefix', 'owner', 'lastUpdateTimestamp', 'description'
  ];
  dataSource = new MatTableDataSource<NamespaceList>();
  selection = new SelectionModel<NamespaceList>(true, []);
  loading = false;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: NamespaceService,
              private snackBar: MatSnackBar,
              private router: Router) {
  }

  ngOnInit() {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.dataSource.filterPredicate = (data: NamespaceList, filter: string) => {
      return (data.uri.indexOf(filter) !== -1) ||
        (data.uri && data.uri.indexOf(filter) !== -1);
    };

    this.loadCodeList();
  }

  loadCodeList() {
    this.loading = true;
    this.service.getNamespaceList().subscribe(resp => {
      resp = resp.map((elm: NamespaceList) => {
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
    this.router.navigateByUrl('/namespace/create');
  }

}
