import {Component, OnInit, ViewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {BusinessTermService} from '../domain/business-term.service';
import {BusinessTermDetails} from '../domain/business-term';
import {MatDialog} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort} from '@angular/material/sort';
import {hashCode} from '../../common/utility';
import {forkJoin} from 'rxjs';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'score-business-term-detail',
  templateUrl: './business-term-detail.component.html',
  styleUrls: ['./business-term-detail.component.css']
})
export class BusinessTermDetailComponent implements OnInit {

  title = 'Edit Business Term';
  businessTerm: BusinessTermDetails;
  hashCode;
  disabled: boolean;


  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private service: BusinessTermService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit() {
    this.businessTerm = new BusinessTermDetails();
    const businessTermId = this.route.snapshot.params.id;

    forkJoin(
      this.service.getBusinessTermDetails(businessTermId)
    )
      .subscribe(([businessTerm]) => {
        this.businessTerm = businessTerm;
        this.hashCode = hashCode(this.businessTerm);
      });

  }

  isChanged() {
    return this.hashCode !== hashCode(this.businessTerm);
  }

  isDisabled(businessTerm1: BusinessTermDetails) {
    return (this.disabled) ||
      (businessTerm1.businessTermId === undefined || !businessTerm1.businessTermId) ||
      (businessTerm1.businessTerm === undefined || businessTerm1.businessTerm === '') ||
      (businessTerm1.externalReferenceUri === undefined || businessTerm1.externalReferenceUri === '');
  }

  back() {
    this.location.back();
  }

  update() {
    this.checkUniqueness(this.businessTerm, (_) => {
        this.doUpdate();
    });
  }

  checkUniqueness(businessTerm: BusinessTermDetails, callbackFn?) {
    this.service.checkUniqueness(
        businessTerm.businessTermId,
        businessTerm.businessTerm,
        businessTerm.externalReferenceUri).subscribe(resp => {
      if (!resp) {
        this.openDialogBusinessTermUpdate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  checkBusinessTermName(businessTerm: BusinessTermDetails, callbackFn?) {
    this.service.checkNameUniqueness(
        businessTerm.businessTermId,
        businessTerm.businessTerm).subscribe(resp => {
      if (!resp) {
        this.openDialogBusinessTermUpdateIgnore();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  doUpdate() {
    this.service.update(this.businessTerm).subscribe(_ => {
      this.hashCode = hashCode(this.businessTerm);
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/business_term_management/business_term');
    });
  }

  openDialogBusinessTermUpdate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another business term with the same business term and external reference URI already exist!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

  openDialogBusinessTermUpdateIgnore() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'The business term already has a variable with the same properties';
    dialogConfig.data.content = [
      'Are you sure you want to update the business term?'
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
    dialogConfig.data.header = 'Discard business term?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this business term?',
      'The business term will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(this.businessTerm.businessTermId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.router.navigateByUrl('/business_term_management/business_term');
          }, err => {
            this.snackBar.open('Discard\'s forbidden! The business term is used.', '', {
              duration: 5000,
            });
          });
        }
      });
  }

  isDirty(): boolean {
    return !!this.businessTerm.businessTermId
      || this.businessTerm.businessTerm && this.businessTerm.businessTerm.length > 0
      || this.businessTerm.comment && this.businessTerm.comment.length > 0;
  }

  followTheExternalUri() {
    window.open(this.businessTerm.externalReferenceUri, '_blank');
  }

}
