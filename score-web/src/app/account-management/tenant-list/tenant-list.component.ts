import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSort, SortDirection} from '@angular/material/sort';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {Location} from '@angular/common';
import {MatTableDataSource} from '@angular/material/table';
import {AuthService} from '../../authentication/auth.service';
import {TenantListRequest, TenantList} from '../domain/tenants'
import {TenantListService} from '../domain/tenant-list.service';
import {PageRequest} from '../../basis/basis';
import {finalize} from 'rxjs/operators';

@Component({
  selector: 'score-tenant-list',
  templateUrl: './tenant-list.component.html',
  styleUrls: ['./tenant-list.component.css']
})

export class TenantListComponent implements OnInit {

  title = 'Tenant Roles';
  loading = false;
  displayedColumns: string[] = [ 'Tenant name', 'Users', 'Business Contexts' ];
  dataSource = new  MatTableDataSource<TenantList>();

  request: TenantListRequest;

  contextMenuItem: TenantList;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private auth: AuthService,
              private service: TenantListService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute) {
                
  }

  ngOnInit() {
    this.request = new TenantListRequest(this.route.snapshot.queryParamMap,
      new PageRequest('sortActive', 'asc', 0, 10));

    this.paginator.pageIndex = this.request.page.pageIndex;
    this.paginator.pageSize = this.request.page.pageSize;
    this.paginator.length = 0; 

    this.sort.active = this.request.page.sortActive;
    this.sort.direction = this.request.page.sortDirection as SortDirection;
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.onChange();
    });
    this.loadTenantList(true);
  }

  onChange(property?: string, source?) {
    this.paginator.pageIndex = 0;
    this.loadTenantList();
  }

  onPageChange(event: PageEvent) {
    this.loadTenantList();
  }

  loadTenantList(isInit?: boolean) {
   this.loading = true;
   this.request.page = new PageRequest(
   this.sort.active, this.sort.direction,
   this.paginator.pageIndex, this.paginator.pageSize);

   this.request.page = new PageRequest(
   this.sort.active, this.sort.direction,
   this.paginator.pageIndex, this.paginator.pageSize);

    this.service.getTenants(this.request).pipe(
      finalize(() => {
        this.loading = false;
      })
    ).subscribe(resp => {
      this.paginator.length = resp.length;
      this.dataSource.data = resp.list;
      if (!isInit) {
        this.location.replaceState(this.router.url.split('?')[0], this.request.toQuery());
      }
    }, error => {
      this.dataSource.data = [];
    });
  }

}
