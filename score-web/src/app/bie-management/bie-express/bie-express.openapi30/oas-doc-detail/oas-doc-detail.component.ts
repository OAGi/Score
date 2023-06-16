import {Component, OnInit, ViewChild} from '@angular/core';
import {OasDoc, simpleOasDoc} from '../domain/openapi-doc';
import {MatSort} from '@angular/material/sort';
import {MatPaginator} from '@angular/material/paginator';
import {BusinessContextService} from '../../../../context-management/business-context/domain/business-context.service';
import {OpenAPIService} from '../domain/openapi.service';
import {AccountListService} from '../../../../account-management/domain/account-list.service';
import {AuthService} from '../../../../authentication/auth.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmDialogService} from '../../../../common/confirm-dialog/confirm-dialog.service';
import {forkJoin} from 'rxjs';
import { hashCode } from 'src/app/common/utility';

@Component({
  selector: 'score-oas-doc-detail',
  templateUrl: './oas-doc-detail.component.html',
  styleUrls: ['./oas-doc-detail.component.css']
})
export class OasDocDetailComponent implements OnInit {
  title = 'Edit Open API Doc';
  oasDocs: simpleOasDoc[];
  oasDoc: OasDoc;
  hashCode;
  disabled: boolean;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private bizCtxService: BusinessContextService,
              private openAPIService: OpenAPIService,
              private accountService: AccountListService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService) { }

  ngOnInit(): void {
    this.oasDoc = new OasDoc();
    this.oasDoc.used = true;
    const oasDocId = this.route.snapshot.params.id;

    forkJoin(
      this.openAPIService.getOasDoc(oasDocId)
    )
      .subscribe(([simpleOasDoc]) => {
        this.oasDoc = simpleOasDoc;
        this.hashCode = hashCode(this.oasDoc);
      });
  }

  isChanged() {
    return this.hashCode !== hashCode(this.oasDoc);
  }

  isDisabled(oasDoc1: OasDoc) {
    return (this.disabled) ||
      (oasDoc1.oasDocId === undefined || !oasDoc1.oasDocId) ||
      (oasDoc1.title === undefined || oasDoc1.title === '') ||
      (oasDoc1.openAPIVersion === undefined || oasDoc1.openAPIVersion === '') ||
      (oasDoc1.version === undefined || oasDoc1.version === '') ||
      (oasDoc1.licenseName === undefined || oasDoc1.licenseName === '');
  }

  back() {
    this.location.back();
  }

  update() {
    this.checkUniqueness(this.oasDoc, (_) => {
      this.doUpdate();
    });
  }

  checkUniqueness(oasDoc: OasDoc, callbackFn?) {
    this.openAPIService.checkUniqueness(oasDoc).subscribe(resp => {
      if (!resp) {
        this.openDialogOasDocUpdate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  checkOasDocTitle(oasDoc: OasDoc, callbackFn?) {
    this.openAPIService.checkTitleUniqueness(oasDoc).subscribe(resp => {
      if (!resp) {
        this.openDialogOasDocUpdateIgnore();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  doUpdate() {
    this.openAPIService.updateOasDoc(this.oasDoc).subscribe(_ => {
      this.hashCode = hashCode(this.oasDoc);
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/profile_bie/express/oas_doc');
    });
  }

  openDialogOasDocUpdate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another Open API Doc with the same title, OpenAPI Version, Doc Version and License Name already exists!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

  openDialogOasDocUpdateIgnore() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'The OpenAPI Doc already has a variable with the same properties';
    dialogConfig.data.content = [
      'Are you sure you want to update the OpenAPI Doc?'
    ];
    dialogConfig.data.action = 'Update anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.doUpdate();
        }
      });
  }

  discard() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard OpenAPI Doc?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this OpenAPI Doc?',
      'The OpenAPI Doc will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.openAPIService.delete(this.oasDoc.oasDocId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.router.navigateByUrl('/profile_bie/express/oas_doc');
          }, err => {
            console.log(err);
            this.snackBar.open('Discard\'s forbidden! The OpenAPI Doc is used.', '', {
              duration: 5000,
            });
          });
        }
      });
  }

  isDirty(): boolean {
    return !!this.oasDoc.oasDocId
      || this.oasDoc.title && this.oasDoc.title.length > 0
      || this.oasDoc.description && this.oasDoc.description.length > 0;
  }
}
