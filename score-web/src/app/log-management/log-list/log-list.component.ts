import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute} from '@angular/router';
import {Log} from '../domain/log';
import {LogService} from '../domain/log.service';
import {LogCompareDialogComponent} from '../log-compare-dialog/log-compare-dialog.component';
import {createTableColumns, defineTable} from '../../common/score-table';

const LOG_TABLE = defineTable({
  storageKey: 'TableColumns-LogPage',
  columns: [
    {id: 'commit', label: 'Commit', width: 200, sortable: true},
    {id: 'revisionNum', label: 'Revision', width: 100, sortable: true},
    {id: 'revisionAction', label: 'Action', width: 0, sortable: true},
    {id: 'loginId', label: 'Actor', width: 140, sortable: true},
    {id: 'timestamp', label: 'Created At', header: 'Created at', width: 160, sortable: true},
  ],
  fixed: [{id: 'check', position: 'start', header: 'Compare'}],
  sortParam: 'sortActive',
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
});

@Component({
  standalone: false,
  selector: 'score-log-list',
  templateUrl: './log-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./log-list.component.css'],
})
export class LogListComponent {
  private service = inject(LogService);
  private dialog = inject(MatDialog);
  private route = inject(ActivatedRoute);

  readonly columns = createTableColumns(LOG_TABLE);
  /** The two revisions to compare. */
  selection = new SelectionModel<number>(true, []);

  readonly load = (params: HttpParams) => this.service.getRevisions(this.route.snapshot.paramMap.get('reference'), params);

  isSelected(elem: Log): boolean {
    return this.selection.isSelected(elem.logId);
  }

  toggle(elem: Log) {
    if (!this.isSelected(elem)) {
      if (this.selection.selected.length > 1) {
        this.selection.deselect(this.selection.selected[0]);
      }
    }
    this.selection.toggle(elem.logId);
  }

  openCompareDialog() {
    if (this.selection.selected.length !== 2) {
      return false;
    }
    let before;
    let after;
    if (this.selection.selected[0] > this.selection.selected[1]) {
      before = this.selection.selected[1];
      after = this.selection.selected[0];
    } else {
      before = this.selection.selected[0];
      after = this.selection.selected[1];
    }
    this.dialog.open(LogCompareDialogComponent, {
      data: {
        before,
        after
      },
      width: '100%',
      maxWidth: '100%'
    });
  }

  logAction(log: Log) {
    if (log.logAction === 'Revised' && !log.developer) {
      return 'Amended';
    }
    return log.logAction;
  }
}
