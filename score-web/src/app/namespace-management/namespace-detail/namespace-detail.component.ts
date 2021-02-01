import {Component, OnInit} from '@angular/core';
import {FormControl, Validators} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {hashCode} from '../../common/utility';
import {Namespace, NamespaceList} from '../domain/namespace';
import {NamespaceService} from '../domain/namespace.service';
import {finalize, switchMap} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'score-namespace-detail',
  templateUrl: './namespace-detail.component.html',
  styleUrls: ['./namespace-detail.component.css']
})
export class NamespaceDetailComponent implements OnInit {

  title = 'Namespace Detail';
  disabled: boolean;
  namespace: Namespace;
  uriForm = new FormControl('');
  hashCode;

  constructor(private service: NamespaceService,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private auth: AuthService) {
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
      this.uriForm = new FormControl({value: this.namespace.uri, disabled: !this.namespace.canEdit},
        Validators.pattern('\\w+:(\\/?\\/?)[^\\s]+'));
      this.hashCode = hashCode(resp);
    });
  }

  isChanged() {
    if (this.uriForm) {
      this.namespace.uri = this.uriForm.value;
    }
    return this.hashCode !== hashCode(this.namespace);
  }

  isDisabled() {
    return (this.disabled) ||
      (this.namespace.uri === undefined || this.namespace.uri === '' || !this.uriForm.valid);
  }

  back() {
    this.location.back();
  }

  update() {
    this.namespace.uri = this.uriForm.value;
    this.service.update(this.namespace).subscribe(_ => {
      this.hashCode = hashCode(this.namespace);
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    });
  }

  discard() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Namespace?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this namespace?',
      'The namespace will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.disabled = true;
          this.service.discard(this.namespace.namespaceId).pipe(finalize(() => {
            this.disabled = false;
          })).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.router.navigateByUrl('/namespace');
          });
        }
      });
  }

}
