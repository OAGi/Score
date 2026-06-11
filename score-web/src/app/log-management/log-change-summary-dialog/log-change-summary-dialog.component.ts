import { Component, OnInit, inject } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {finalize} from 'rxjs/operators';
import {ComponentChangeSummary, ComponentFieldChange} from '../domain/log';
import {LogService} from '../domain/log.service';

/**
 * Shows the change summary of the two compared log entries (issue #1533, sub-task 3),
 * opened from the compare view; data = the two log IDs ({@code before}, {@code after}).
 */
@Component({
  standalone: false,
  selector: 'score-log-change-summary-dialog',
  templateUrl: './log-change-summary-dialog.component.html',
  styleUrls: ['./log-change-summary-dialog.component.css'],
})
export class LogChangeSummaryDialogComponent implements OnInit {
  private service = inject(LogService);
  dialogRef = inject<MatDialogRef<LogChangeSummaryDialogComponent>>(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);

  loading = true;
  summary: ComponentChangeSummary;
  errorMessage: string;

  ngOnInit() {
    this.service.getChangeSummaryByCompare(this.data.before, this.data.after).pipe(
      finalize(() => this.loading = false)
    ).subscribe(summary => {
      this.summary = summary;
    }, error => {
      this.errorMessage = error.error?.message || 'Failed to load the change summary.';
    });
  }

  get isNew(): boolean {
    return this.summary?.summaryType === 'NEW';
  }

  get prevRevisionNum(): number {
    return this.summary?.prevRevisionNum ?? (this.summary?.revisionNum - 1);
  }

  get isEmptyRevision(): boolean {
    return !!this.summary && !this.isNew
      && this.summary.fieldChanges.length === 0
      && this.summary.childrenAdded.length === 0
      && this.summary.childrenRemoved.length === 0
      && this.summary.childrenChanged.length === 0;
  }

  valueOrNone(value: string | undefined): string {
    return (value === undefined || value === null) ? '(none)' : '"' + value + '"';
  }

  changeText(change: ComponentFieldChange): string {
    return change.label + ': ' + this.valueOrNone(change.before) + ' → ' + this.valueOrNone(change.after);
  }
}
