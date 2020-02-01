import {Component, OnInit, ViewChild} from '@angular/core';
import {ReleaseService} from '../domain/release.service';
import {MatPaginator, MatSnackBar, MatSort, MatTableDataSource, PageEvent} from '@angular/material';
import {SelectionModel} from '@angular/cdk/collections';
import {Router} from '@angular/router';
import {ReleaseList} from '../domain/release';

@Component({
  selector: 'srt-release-list',
  templateUrl: './release-list.component.html',
  styleUrls: ['./release-list.component.css']
})
export class ReleaseListComponent implements OnInit {

  title = 'Release';

  displayedColumns: string[] = [
    'releaseNum', 'state', 'namespace', 'lastUpdatedBy', 'lastUpdateTimestamp'
  ];
  dataSource = new MatTableDataSource<ReleaseList>();
  selection = new SelectionModel<ReleaseList>(true, []);
  loading = false;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: ReleaseService,
              private snackBar: MatSnackBar,
              private router: Router) {
  }

  ngOnInit() {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.dataSource.filterPredicate = (data: ReleaseList, filter: string) => {
      return (data.releaseNum.indexOf(filter) !== -1) ||
        (data.releaseNum && data.releaseNum.indexOf(filter) !== -1);
    };

    this.loadCodeList();
  }

  onPageChange(event: PageEvent) {
    this.loadCodeList();
  }

  onChange() {
    this.paginator.pageIndex = 0;
    this.loadCodeList();
  }

  loadCodeList() {
    this.loading = true;
    this.service.getReleaseList().subscribe(resp => {
      resp = resp.map((elm: ReleaseList) => {
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
    this.router.navigateByUrl('/release/create');
  }

}
