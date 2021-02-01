import {Component, OnInit} from '@angular/core';
import {FormControl, Validators} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
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
  uriForm: FormControl;
  hashCode;

  constructor(private service: NamespaceService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private auth: AuthService,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.disabled = false;
    this.namespace = new Namespace();
    this.uriForm = new FormControl(this.namespace.uri, Validators.pattern('\\w+:(\\/?\\/?)[^\\s]+'));
  }

  isDisabled() {
    return !this.uriForm.valid;
  }

  get isDeveloper(): boolean {
    return this.auth.getUserToken().role === 'developer';
  }

  back() {
    this.location.back();
  }

  create() {
    this.namespace.uri = this.uriForm.value;
    this.service.create(this.namespace).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/namespace');
    });
  }

}
