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

  title = 'Create Context Category';
  name;

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
    });
  }

}
