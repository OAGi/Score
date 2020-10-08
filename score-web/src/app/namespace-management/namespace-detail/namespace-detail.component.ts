import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {hashCode} from '../../common/utility';
import {Namespace} from '../domain/namespace';
import {NamespaceService} from '../domain/namespace.service';
import {switchMap} from 'rxjs/operators';

@Component({
  selector: 'score-namespace-detail',
  templateUrl: './namespace-detail.component.html',
  styleUrls: ['./namespace-detail.component.css']
})
export class NamespaceDetailComponent implements OnInit {

  title = 'Namespace Detail';
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

    // load context scheme
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) =>
        this.service.getNamespace(params.get('id')))
    ).subscribe(resp => {
      this.namespace = resp;

      this.hashCode = hashCode(resp);
    });
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

  update() {
    this.service.update(this.namespace).subscribe(_ => {
      this.hashCode = hashCode(this.namespace);
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    });
  }

}
