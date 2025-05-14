import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {BusinessTermService} from '../domain/business-term.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {BusinessTermDetails} from '../domain/business-term';

@Component({
  selector: 'score-business-term-create',
  templateUrl: './business-term-create.component.html',
  styleUrls: ['./business-term-create.component.css']
})
export class BusinessTermCreateComponent implements OnInit {

  title = 'Create Business Term';
  businessTerm: BusinessTermDetails;
  disabled: boolean;

  constructor(private service: BusinessTermService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit() {
    this.disabled = false;
    this.businessTerm = new BusinessTermDetails();
  }

  isDisabled(businessTerm: BusinessTermDetails) {
    return (this.disabled) ||
      (businessTerm.businessTerm === undefined || businessTerm.businessTerm === '') ||
      (businessTerm.externalReferenceUri === undefined || businessTerm.externalReferenceUri === '');
  }

  back() {
    this.location.back();
  }

  openDialogbusinessTermCreate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another business term with the same business term and external reference URI already exists!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

  doCreate() {
    this.service.create(this.businessTerm).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/business_term_management/business_term');
    });
  }

  create() {
    this.checkUniqueness(this.businessTerm, (_) => {
      this.checkBusinessTermName(this.businessTerm, (dummy) => {
        this.doCreate();
      });
    });
  }

  checkBusinessTermName(businessTerm: BusinessTermDetails, callbackFn?) {
    this.service.checkNameUniqueness(
        businessTerm.businessTermId,
        businessTerm.businessTerm).subscribe(resp => {
      if (!resp) {
        this.openDialogbusinessTermCreate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  checkUniqueness(businessTerm: BusinessTermDetails, callbackFn?) {
    this.service.checkUniqueness(
        businessTerm.businessTermId,
        businessTerm.businessTerm,
        businessTerm.externalReferenceUri).subscribe(resp => {
      if (!resp) {
        this.openDialogbusinessTermCreate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  isDirty(): boolean {
    return !!this.businessTerm.businessTermId
      || this.businessTerm.businessTerm && this.businessTerm.businessTerm.length > 0
      || this.businessTerm.comment && this.businessTerm.comment.length > 0;
  }
}
