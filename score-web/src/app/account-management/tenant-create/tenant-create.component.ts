import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {TenantListService} from '../domain/tenant-list.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'score-tenant-create-component',
  templateUrl: './tenant-create.component.html'
})
export class TenantCreateComponent implements OnInit {

  title = 'Create Tenant';
  name;
  HTTP_CONFLICT = 409;

  constructor(private service: TenantListService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.name = '';
  }

  isDisabled(name: string) {
    return name === undefined || name === '';
  }

  back() {
    this.location.back();
  }

  create() {
    this.service.createTenant(this.name).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/tenant');
    }, error => {
      if (error.status === this.HTTP_CONFLICT) {
        this.snackBar.open(`Tenant with name ${this.name} already exists!`, '', {
          duration: 3000,
        });
      } else {
        this.snackBar.open(`There is a problem with creation of tenant with name ${this.name}`, '', {
          duration: 3000,
        });
      }
    });
  }

}
