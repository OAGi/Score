import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {BieListEntry} from '../../bie-list/domain/bie-list';
import {BieListService} from '../../bie-list/domain/bie-list.service';
import {bieLinkQuery, BIE_FILTERS as F, BIE_TABLE} from '../../bie-list/domain/bie.table';
import {AuthService} from '../../../authentication/auth.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';

const REUSE_BIE_TABLE = defineTable({
  ...BIE_TABLE,
  filters: [F.businessContext, F.version, F.remark, F.owner, F.updater, F.updatedDate],
});

/** What the BIEs that can be reused are asked with: the ones of the ASCCP that the BIE is of, in the branch of the BIE. */
export function reuseBieParams(data: any, isDeveloper: boolean): Record<string, string> {
  const params: Record<string, string> = {
    libraryId: '' + data.libraryId,
    releaseIds: '' + data.releaseId,
    excludeTopLevelAsbiepIds: '' + data.topLevelAsbiepId
  };
  if (data.asccpManifestId) {
    params['asccpManifestId'] = '' + data.asccpManifestId;
  }
  const basedTopLevelAsbiepId = data.basedTopLevelAsbiepId;
  if (Number.isInteger(basedTopLevelAsbiepId) && basedTopLevelAsbiepId > 0) {
    params['basedTopLevelAsbiepIds'] = '' + basedTopLevelAsbiepId;
  }
  // Per Test Assertion #24.1.2: "The end user can reuse developer top-level BIE,"
  // this check is required only when the current user is a developer.
  if (isDeveloper) {
    params['ownedByDeveloper'] = 'true';
  }
  return params;
}

@Component({
  standalone: false,
  selector: 'score-reuse-bie-dialog',
  templateUrl: './reuse-bie-dialog.component.html',
  styleUrls: ['./reuse-bie-dialog.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class ReuseBieDialogComponent {
  dialogRef = inject<MatDialogRef<ReuseBieDialogComponent>>(MatDialogRef);
  private bieListService = inject(BieListService);
  private auth = inject(AuthService);
  webPageInfo = inject(WebPageInfoService);
  data = inject(MAT_DIALOG_DATA);

  readonly columns = createTableColumns(REUSE_BIE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BieListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(false, []);

  readonly ready = signal(false);
  readonly extraParams: Record<string, string> = reuseBieParams(this.data, this.isDeveloper);

  readonly load = (params: HttpParams) => this.bieListService.getBieListPage(params);
  readonly rowKey = (row: BieListEntry) => row.topLevelAsbiepId;

  constructor() {
    afterNextRender(() => {
      // The search is of the BIE that the reuse is for, and cannot be changed.
      this.view().setFilter('den', this.data.den || '');
      this.ready.set(true);
    });
  }

  /** The search part of a link to the list that shows the BIE. */
  linkQuery(extras: { [key: string]: any }): string {
    return bieLinkQuery(REUSE_BIE_TABLE, this.view().query, extras);
  }

  get isDeveloper() {
    const userToken = this.auth.getUserToken();
    return userToken.roles.includes('developer');
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  isDisabled() {
    return this.selection.selected.length === 0;
  }
}
