import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {BieListEntry} from '../../bie-list/domain/bie-list';
import {BieListService} from '../../bie-list/domain/bie-list.service';
import {bieLinkQuery, BIE_FILTERS as F, BIE_TABLE} from '../../bie-list/domain/bie.table';
import {AuthService} from '../../../authentication/auth.service';
import {UserToken} from '../../../authentication/domain/auth';
import {WebPageInfoService} from '../../../basis/basis.service';
import {ReleaseSummary} from '../../../release-management/domain/release';
import {ReleaseChoice} from '../../../release-management/domain/release-choice';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';

const BIE_PACKAGE_BIE_TABLE = defineTable({
  ...BIE_TABLE,
  filters: [F.states, F.businessContext, F.version, F.remark, F.owner, F.updater, F.updatedDate,
    // The branch is chosen beside the search box; it is part of the query.
    F.releaseIds],
});

@Component({
  standalone: false,
  selector: 'score-bie-package-bie-dialog',
  templateUrl: './bie-package-bie-dialog.component.html',
  styleUrls: ['./bie-package-bie-dialog.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class BiePackageBieDialogComponent {
  dialogRef = inject<MatDialogRef<BiePackageBieDialogComponent>>(MatDialogRef);
  private service = inject(BieListService);
  private releaseService = inject(ReleaseService);
  private auth = inject(AuthService);
  webPageInfo = inject(WebPageInfoService);
  data = inject(MAT_DIALOG_DATA);

  action: string;

  readonly releaseChoice = new ReleaseChoice(this.auth, 'BIE');
  readonly columns = createTableColumns(BIE_PACKAGE_BIE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BieListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<BieListEntry>(true, [], true, (a, b) => a.topLevelAsbiepId === b.topLevelAsbiepId);

  /** The list is of the library of the package, in a branch: it waits for them. */
  readonly ready = signal(false);
  readonly extraParams: Record<string, string> = this.extraParamsOf();
  readonly filterOptions = signal<Record<string, { value: string, label: string }[]>>({});

  readonly load = (params: HttpParams) => this.service.getBieListPage(params);
  readonly rowKey = (row: BieListEntry) => row;

  constructor() {
    this.action = this.data?.action;
    if (!!this.data?.den && !!this.data?.excludeTopLevelAsbiepIds) {
      // A BIE is chosen to replace another one.
      this.selection = new SelectionModel<BieListEntry>(false, []);
    }
    afterNextRender(() => this.start());
  }

  private extraParamsOf(): Record<string, string> {
    const params: Record<string, string> = {libraryId: '' + this.data.biePackage.libraryId};
    if (!!this.data?.den && !!this.data?.excludeTopLevelAsbiepIds) {
      params['excludeTopLevelAsbiepIds'] = Array.isArray(this.data.excludeTopLevelAsbiepIds) ?
        this.data.excludeTopLevelAsbiepIds.join(',') : '' + this.data.excludeTopLevelAsbiepIds;
    }
    return params;
  }

  private start() {
    // The BIEs in production, to begin with.
    this.view().setFilter('states', ['Production']);
    if (!!this.data?.den && !!this.data?.excludeTopLevelAsbiepIds) {
      this.view().setFilter('den', this.data.den);
    }
    this.releaseService.getReleaseSummaryList(this.data.biePackage.libraryId).subscribe(releases => {
      this.releaseChoice.init(releases.filter(e => !e.workingRelease), 0);
      this.applyRelease();
      this.ready.set(true);
    });
  }

  private applyRelease() {
    this.view().setFilter('releaseIds', (!!this.releaseChoice.release) ? ['' + this.releaseChoice.release.releaseId] : []);
  }

  onBranchChange(release: ReleaseSummary) {
    this.releaseChoice.choose(release);
    this.applyRelease();
    this.selection.clear();
    this.view().onSearch();
  }

  /** The search part of a link to the list that shows the BIE. */
  linkQuery(extras: { [key: string]: any }): string {
    return bieLinkQuery(BIE_PACKAGE_BIE_TABLE, this.view().query, extras);
  }

  get username(): string {
    const userToken = this.userToken;
    return (userToken) ? userToken.username : undefined;
  }

  get roles(): string[] {
    const userToken = this.userToken;
    return (userToken) ? userToken.roles : [];
  }

  get userToken(): UserToken {
    return this.auth.getUserToken();
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  add() {
    const selectedBieList = this.selection.selected;
    this.dialogRef.close(selectedBieList.map(e => e.topLevelAsbiepId));
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
