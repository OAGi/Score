import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {BieListEntry} from '../../bie-list/domain/bie-list';
import {BieListService} from '../../bie-list/domain/bie-list.service';
import {bieLinkQuery, BIE_FILTERS as F, BIE_TABLE} from '../../bie-list/domain/bie.table';
import {WebPageInfoService} from '../../../basis/basis.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';

const PICKER_TABLE = defineTable({
  ...BIE_TABLE,
  filters: [F.states, F.businessContext, F.version, F.remark, F.owner, F.updater, F.updatedDate,
    // The release is locked to the one that the caller gives: it is part of the query.
    F.releaseIds],
});

@Component({
  standalone: false,
  selector: 'score-pagination-response-dialog',
  templateUrl: './pagination-response-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./pagination-response-dialog.component.css']
})
export class PaginationResponseDialogComponent {
  dialogRef = inject<MatDialogRef<PaginationResponseDialogComponent>>(MatDialogRef);
  private bieListService = inject(BieListService);
  webPageInfo = inject(WebPageInfoService);
  data = inject(MAT_DIALOG_DATA);

  readonly columns = createTableColumns(PICKER_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BieListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(false, []);

  readonly ready = signal(false);
  /** Locked to a library, and to the BIEs that the user can view. */
  readonly extraParams = signal<Record<string, string>>({libraryId: '' + this.data.library?.libraryId, access: 'CanView'});
  release = this.data.release;

  readonly load = (params: HttpParams) => this.bieListService.getBieListPage(params);
  readonly rowKey = (row: BieListEntry) => row.topLevelAsbiepId;

  constructor() {
    afterNextRender(() => {
      // The search is of the well-known BIE, and cannot be changed.
      this.view().setFilter('den', 'Pagination Response. Pagination Response');
      this.view().setFilter('releaseIds', ['' + this.release.releaseId]);
      this.ready.set(true);
    });
  }

  /** The search part of a link to the list that shows the BIE. */
  linkQuery(extras: { [key: string]: any }): string {
    return bieLinkQuery(PICKER_TABLE, this.view().query, extras);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  isDisabled() {
    return this.selection.selected.length === 0;
  }
}
