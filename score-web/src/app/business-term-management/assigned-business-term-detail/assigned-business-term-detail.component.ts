import { Component, OnInit, ViewChild, inject } from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {BusinessTermService} from '../domain/business-term.service';
import {MatDialog} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort} from '@angular/material/sort';
import {hashCode} from '../../common/utility';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {AssignedBusinessTermDetails} from '../domain/business-term';
import {Title} from '@angular/platform-browser';
import {setAppTitleIfPresent} from '../../common/app-title.strategy';

@Component({
  standalone: false,
  selector: 'score-business-term-detail',
  templateUrl: './assigned-business-term-detail.component.html',
  styleUrls: ['./assigned-business-term-detail.component.css']
})
export class AssignedBusinessTermDetailComponent implements OnInit {
  private service = inject(BusinessTermService);
  private location = inject(Location);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);
  private titleService = inject(Title);


  title = 'Edit Business Term Assignment';
  assignedBusinessTerm: AssignedBusinessTermDetails;
  hashCode;

  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  ngOnInit() {
    this.assignedBusinessTerm = new AssignedBusinessTermDetails();
    const assignedBusinessTermId = this.route.snapshot.queryParams.id;
    const bieType = this.route.snapshot.queryParams.type;
    this.service.getAssignedBusinessTerm(bieType, assignedBusinessTermId).subscribe(assignedBusinessTerm => {
      if (!assignedBusinessTerm) {
        this.redirectToAssignedBusinessTermList();
        return;
      }

      this.assignedBusinessTerm = assignedBusinessTerm;
      setAppTitleIfPresent(this.titleService, this.assignedBusinessTerm.businessTerm, 'Business Term Assignment');
      this.hashCode = hashCode(this.assignedBusinessTerm);
    }, err => {
      if (err.status === 404) {
        this.redirectToAssignedBusinessTermList();
        return;
      }

      const errorMessage = (err.status === 403) ?
        'You do not have access permission.' : 'Something\'s wrong.';
      this.snackBar.open(errorMessage, '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/business_term_management/assign_business_term');
    });
  }

  private redirectToAssignedBusinessTermList() {
    this.snackBar.open('The requested business term assignment is unavailable.', '', {
      duration: 3000,
    });
    this.router.navigateByUrl('/business_term_management/assign_business_term');
  }

  isChanged() {
    return this.hashCode !== hashCode(this.assignedBusinessTerm);
  }

  isDisabled() {
    return (this.assignedBusinessTerm.assignedBizTermId === undefined || !this.assignedBusinessTerm.assignedBizTermId) ||
      (this.assignedBusinessTerm.bieType !== 'ASBIE' && this.assignedBusinessTerm.bieType !== 'BBIE') ||
      (this.assignedBusinessTerm.primaryIndicator === undefined) ||
      (this.assignedBusinessTerm.typeCode === undefined || this.assignedBusinessTerm.typeCode === '');
  }

  back() {
    this.location.back();
  }

  update() {
    this.checkUniqueness(this.assignedBusinessTerm, (_) => {
        this.checkIfPrimaryWillBeOverwrittenAndDoUpdate(this.assignedBusinessTerm);
    });
  }

  checkUniqueness(_assignedBusinessTerm: AssignedBusinessTermDetails, callbackFn?) {
    this.service.checkAssignmentUniqueness(
      _assignedBusinessTerm.bieId, _assignedBusinessTerm.bieType,
      _assignedBusinessTerm.businessTermId, _assignedBusinessTerm.typeCode,
      _assignedBusinessTerm.primaryIndicator)
      .subscribe(resp => {
      if (!resp) {
        this.openDialogAssignedBusinessTermUpdate();
        return;
      }
      return callbackFn && callbackFn(_assignedBusinessTerm);
    });
  }

  checkIfPrimaryWillBeOverwrittenAndDoUpdate(_assignedBusinessTerm: AssignedBusinessTermDetails) {
    if (_assignedBusinessTerm.primaryIndicator) {
      return this.service.findIfPrimaryExist(_assignedBusinessTerm.bieId,
        _assignedBusinessTerm.bieType, _assignedBusinessTerm.primaryIndicator, _assignedBusinessTerm.typeCode)
        .subscribe(resp => {
        if (resp && resp.length > 0) {
          const dialogConfig = this.confirmDialogService.newConfig();
          dialogConfig.data.header = 'Overwrite previous preferred business term?';
          dialogConfig.data.content = [
            'The preferred business term already exists for selected BIE and type code.',
            'Are you sure you want to do the update and overwrite the previous preferred business term assignment?'
          ];
          dialogConfig.data.action = 'Update';
          this.confirmDialogService.open(dialogConfig).afterClosed()
            .subscribe(result => {
              if (result) {
                this.doUpdate(_assignedBusinessTerm);
              }
            });
        } else {
          this.doUpdate(_assignedBusinessTerm);
        }
      });
    }
    else {
      this.doUpdate(_assignedBusinessTerm);
    }
  }

  doUpdate(assignedBusinessTerm: AssignedBusinessTermDetails) {
    this.service.updateAssignment(assignedBusinessTerm).subscribe(_ => {
      this.hashCode = hashCode(assignedBusinessTerm);
      setAppTitleIfPresent(this.titleService, assignedBusinessTerm.businessTerm, 'Business Term Assignment');
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/business_term_management/assign_business_term');
    });
  }

  openDialogAssignedBusinessTermUpdate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another business term assignment for the same BIE and type code already exists!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

  discard() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard business term assignment?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this business term assignment?',
      'The business term assignment will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(this.assignedBusinessTerm.assignedBizTermId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.router.navigateByUrl('/business_term_management/assign_business_term');
          });
        }
      });
  }

  isDirty(): boolean {
    return !!this.assignedBusinessTerm.assignedBizTermId
      || this.assignedBusinessTerm.typeCode && this.assignedBusinessTerm.typeCode.length > 0
      || this.assignedBusinessTerm.primaryIndicator != null;
  }

  followTheExternalUri() {
    window.open(this.assignedBusinessTerm.externalReferenceUri, '_blank');
  }

}
