import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {hashCode} from '../../common/utility';
import {Namespace} from '../domain/namespace';
import {NamespaceService} from '../domain/namespace.service';

@Component({
  selector: 'score-namespace-create',
  templateUrl: './namespace-create.component.html',
  styleUrls: ['./namespace-create.component.css']
})
export class NamespaceCreateComponent implements OnInit {

  title = 'Create Namespace';
  disabled: boolean;
  namespace: Namespace;
  hashCode;

  constructor(private service: NamespaceService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.disabled = false;
    this.namespace = new Namespace();
    this.hashCode = hashCode(this.namespace);
  }

  isChanged() {
    return this.hashCode !== hashCode(this.namespace);
  }

  isDisabled() {
    return (this.disabled) ||
      (this.namespace.uri === undefined || this.namespace.uri === '');
  }

  back() {
    this.location.back();
  }

  create() {
    this.service.create(this.namespace).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/namespace');
    });
  }

}
