import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {switchMap} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material/snack-bar';
import {hashCode} from '../../common/utility';
import {TenantListService} from '../domain/tenant-list.service';
import {TenantInfo} from '../domain/tenants'
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';


@Component({
  selector: 'score-tenant-update-component',
  templateUrl: './tenant-update.component.html'
})
export class UpdateTenantComponent implements OnInit {

  title = 'Edit Tenant';
  tenant: TenantInfo;
  hashCode; 
  HTTP_CONFLICT = 409;


  constructor(private service: TenantListService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit() {
    this.tenant = new TenantInfo();
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
    }, error => {
      if (error.status === this.HTTP_CONFLICT) {
        this.snackBar.open(`Tenant with name ${this.tenant.name} already exists!`, '', {
          duration: 3000,
        });
      } else {
        this.snackBar.open(`There is a problem with creation of tenant with name ${this.tenant.name}`, '', {
          duration: 3000,
        });
      }
    });
  }

  discard() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Tenant?';
    dialogConfig.data.content = [
      'Are you sure you want to discard the tenant?',
      'The tenant will be permanently removed.'
    ];
    if (this.tenant.usersCount > 0 || this.tenant.businessCtxCount > 0) {
      dialogConfig.data.content = [
        `This tenant is associated with ${this.tenant.usersCount} Account(s) and ${this.tenant.businessCtxCount} Business context(s)!`,
        'Are you sure you want to discard the tenant?',
        'The tenant will be permanently removed.'
      ];
    }
    dialogConfig.data.action = 'Discard';
    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(result => {
      if (result) {
       this.service.deleteTenant(this.tenant.tenantId).subscribe(_ => {
          this.snackBar.open('Tenant is deleted', '', {
           duration: 3000,
         });
          this.router.navigateByUrl('/tenant');
        });
      }
    });

  }


}
