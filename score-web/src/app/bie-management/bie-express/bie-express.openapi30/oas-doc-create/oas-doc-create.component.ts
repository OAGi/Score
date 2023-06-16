import {Component, OnInit} from '@angular/core';
import {OasDoc} from '../domain/openapi-doc';
import {BusinessContextService} from '../../../../context-management/business-context/domain/business-context.service';
import {AccountListService} from '../../../../account-management/domain/account-list.service';
import {AuthService} from '../../../../authentication/auth.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {OpenAPIService} from '../domain/openapi.service';
import {ConfirmDialogService} from '../../../../common/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'score-oas-doc-create',
  templateUrl: './oas-doc-create.component.html',
  styleUrls: ['./oas-doc-create.component.css']
})
export class OasDocCreateComponent implements OnInit {
  title = 'Create Open API Doc';
  subtitle = 'Open API Doc Metadata';
  oasDoc: OasDoc;
  disabled: boolean;

  constructor(private bizCtxService: BusinessContextService,
              private openAPIService: OpenAPIService,
              private accountService: AccountListService,
              private auth: AuthService,
              private location: Location,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit(): void {
    this.disabled = false;
    this.oasDoc = new OasDoc();
  }

  isDisabled(oasDoc: OasDoc) {
    return (this.disabled) ||
      (this.oasDoc.title === undefined || this.oasDoc.title === '') ||
      (this.oasDoc.openAPIVersion === undefined || this.oasDoc.openAPIVersion === '') ||
      (this.oasDoc.version === undefined || this.oasDoc.version === '') ||
      (this.oasDoc.licenseName === undefined || this.oasDoc.licenseName === '');
  }

  back() {
    this.location.back();
  }

  openDialogOasDocCreate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = ['Another Open API Doc with the same title, OpenAPI Version, Doc Version and License Name already exists!'];
  }

  doCreate() {
    this.openAPIService.createOasDoc(this.oasDoc).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/profile_bie/express/oas_doc');
    });
  }

  create() {
    this.checkUniqueness(this.oasDoc, (_) => {
      this.checkOasDocTitle(this.oasDoc, (dummy) => {
        this.doCreate();
      });
    });
  }

  createWithoutCheck() {
    this.doCreate();
  }

  checkOasDocTitle(oasDoc: OasDoc, callbackFn?) {
    this.openAPIService.checkUniqueness(oasDoc).subscribe(resp => {
      if (!resp) {
        this.openDialogOasDocCreate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  checkUniqueness(oasDoc: OasDoc, callbackFn?) {
    this.openAPIService.checkUniqueness(oasDoc).subscribe(resp => {
      if (!resp) {
        this.openDialogOasDocCreate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }


}
