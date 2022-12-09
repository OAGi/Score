import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {switchMap} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material/snack-bar';
import {hashCode} from '../../common/utility';
import {TenantListService} from '../domain/tenant-list.service';
import {TenantList} from '../domain/tenants'


@Component({
  selector: 'score-tenant-update-component',
  templateUrl: './tenant-update.component.html'
})
export class UpdateTenantComponent implements OnInit {

  title = 'Edit Tenant';
  tenant: TenantList;
  hashCode; 

  constructor(private service: TenantListService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              ) {
  }

  ngOnInit() {
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) =>
        this.service.getTenantInfo(Number(params.get('id'))))
    ).subscribe(resp => {
      this.hashCode = hashCode(resp);
      this.tenant = resp;
    });
  }

  isChanged() {
    return this.hashCode !== hashCode(this.tenant);
  }

  isDisabled() {
    return this.tenant.name === undefined || this.tenant.name === '';
  } 

  back() {
    this.location.back();
  }

  update() {
    this.service.updateTenant(this.tenant.tenantId, this.tenant.name).subscribe(_ => {
      this.hashCode = hashCode(this.tenant);
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/tenant');
    });
  }


}
