import {Component, OnInit} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {of} from 'rxjs';
import {TenantBusinessCtxInfo} from '../domain/tenants';
import {TenantListService} from '../domain/tenant-list.service';
import {finalize, switchMap} from 'rxjs/operators';

@Component({
  selector: 'score-tenant-detail',
  templateUrl: './tenant-detail.component.html',
  styleUrls: ['./tenant-detail.component.css']
})
export class TenantDetailComponent implements OnInit {
  title = 'View/Edit Tenant Business Context';
  tenantId : number;
  tenantBussinessCtxInfo: TenantBusinessCtxInfo;
  loading: boolean;
  displayedColumns: string[] = [ 'name' ];

  constructor(private service: TenantListService,
              private snackBar: MatSnackBar,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => of(Number(params.get('id'))))
    ).subscribe(tenantId => {
      this.loading = true;
      this.tenantId = tenantId;
      this.service.getTenantBusinessCtxInfo(tenantId).pipe(finalize(() => {
        this.loading = false;
      })).subscribe(resp => {
        this.tenantBussinessCtxInfo = resp;
      });
    });
  }

   update() {
    this.service.updateTenantBusinessCtxInfo(this.tenantBussinessCtxInfo).subscribe(_ => {
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/tenant');
    });
  } 
}
