import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {AgencyIdListValue} from '../domain/agency-id-list';
import {hashCode} from '../../common/utility';

@Component({
  selector: 'score-agency-id-list-value-dialog',
  templateUrl: './agency-id-list-value-dialog.component.html',
  styleUrls: ['./agency-id-list-value-dialog.component.css']
})
export class AgencyIdListValueDialogComponent implements OnInit {

  _hashCode;
  isAddAction;
  actionName;
  agencyIdListValue: AgencyIdListValue;
  lastRevisionValue: AgencyIdListValue;
  isEditable = false;

  constructor(
    public dialogRef: MatDialogRef<AgencyIdListValueDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) {

    this.agencyIdListValue = data.agencyIdListValue;
    this.lastRevisionValue = data.lastRevisionValue;
    this.isEditable = data.isEditable;

    this._hashCode = hashCode(this.agencyIdListValue);
  }

  get hasRevision(): boolean {
    return this.lastRevisionValue !== undefined;
  }

  get revisionDeprecated(): boolean {
    return (!!this.lastRevisionValue && this.lastRevisionValue.deprecated);
  }

  get isUsedBefore(): boolean {
    return (!!this.lastRevisionValue && this.lastRevisionValue.used);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
    if (!this.isAvailable(this.agencyIdListValue)) {
      this.actionName = 'View';
      return;
    }

    this.isAddAction = (this.agencyIdListValue.guid === undefined);
    if (this.isAddAction) {
      this.actionName = 'Add';
    } else {
      this.actionName = 'Edit';
    }
  }

  isDisabled() {
    return (this.agencyIdListValue.value === undefined || this.agencyIdListValue.value === '') ||
      (this.agencyIdListValue.name === undefined || this.agencyIdListValue.name === '') ||
      !this.isDirty();
  }

  color(agencyIdListValue: AgencyIdListValue): string {
    if (agencyIdListValue.locked) {
      return 'bright-red';
    }

    if (agencyIdListValue.used) {
      if (agencyIdListValue.extension) {
        return 'green';
      } else {
        return 'blue';
      }
    }

    return 'dull-red';
  }

  isDisabledColor(agencyIdListValue: AgencyIdListValue) {
    return this.color(agencyIdListValue) !== 'green';
  }

  isAvailable(agencyIdListValue: AgencyIdListValue): boolean {
    return this.color(agencyIdListValue) !== 'bright-red' || this.color(agencyIdListValue) !== 'dull-red';
  }

  isDirty(): boolean {
    return this._hashCode !== hashCode(this.agencyIdListValue);
  }

}
