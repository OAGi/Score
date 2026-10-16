import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {BieCopyService} from './domain/bie-copy.service';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContextSummary} from '../../context-management/business-context/domain/business-context';
import {BieListEntry} from '../bie-list/domain/bie-list';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {BIE_FILTERS as F, BIE_TABLE, BIE_URL_FILTERS} from '../bie-list/domain/bie.table';
import {base64Decode, base64Encode, saveBooleanProperty} from '../../common/utility';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {createTableColumns, defineTable, queryToUrl, ScoreTableViewComponent} from '../../common/score-table';
import {MultiActionsSnackBarComponent} from '../../common/multi-actions-snack-bar/multi-actions-snack-bar.component';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';

const BIE_COPY_TABLE = defineTable({
  ...BIE_TABLE,
  filters: [F.states, F.businessContext, F.version, F.remark, F.deprecated, F.owner, F.updater, F.updatedDate, F.releaseIds,
    ...BIE_URL_FILTERS,
    // The business contexts that the copy is for: kept in the URL, as the previous step gave them.
    {type: 'text', key: 'bizCtxIds', label: 'Business contexts', hidden: true}],
});

@Component({
  standalone: false,
  selector: 'score-bie-create-asccp',
  templateUrl: './bie-copy-profile-bie.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./bie-copy-profile-bie.component.css']
})
export class BieCopyProfileBieComponent {
  private bizCtxService = inject(BusinessContextService);
  private service = inject(BieCopyService);
  private bieListService = inject(BieListService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  webPageInfo = inject(WebPageInfoService);

  title = 'Copy BIE';
  subtitle = 'Select BIE';

  bizCtxIds: number[] = [];
  bizCtxList: BusinessContextSummary[] = [];

  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly columns = createTableColumns(BIE_COPY_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BieListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<BieListEntry>(false, []);

  /** The list is of a library, for the business contexts: it waits for them. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});

  // The business contexts are only kept in the URL: the endpoint is not asked for them.
  readonly load = (params: HttpParams) => this.bieListService.getBieListPage(params.delete('bizCtxIds'));

  HIDE_UNUSED_PROPERTY_KEY = 'BIE-Settings-Hide-Unused';

  constructor() {
    afterNextRender(() => this.start());
  }

  /** Once the table view has the query of the URL: the business contexts and the library that it works for. */
  private start() {
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      this.applyLibrary();

      // The business contexts are in the URL: as a parameter, or in the query.
      const bizCtxIds = this.route.snapshot.queryParamMap.get('bizCtxIds') || (this.view().filterValue('bizCtxIds') as string);
      this.view().setFilter('bizCtxIds', bizCtxIds);
      this.bizCtxService.getBusinessContextsByBizCtxIds(bizCtxIds.split(',').map(e => Number(e))).subscribe(resp => {
        this.bizCtxIds = resp.map(e => e.businessContextId);
        this.bizCtxList = resp;
        // Issue #1625
        this.view().setFilter('businessContext', this.bizCtxList.map(e => e.name).join(', '));

        this.releaseService.getReleaseSummaryList(this.libraryChoice.library?.libraryId, ['Published']).subscribe(releases => {
          this.initReleases(releases);
          this.ready.set(true);
        });
      }, err => {
        console.error(err);
      });
    });
  }

  /** Only what the user can view. */
  private applyLibrary() {
    this.extraParams.set({libraryId: '' + this.libraryChoice.library?.libraryId, access: 'CanView'});
  }

  /** The branches of the library that the BIEs can be of, and those chosen. */
  releases: ReleaseSummary[] = [];
  selectedReleases: ReleaseSummary[] = [];

  /** The branches that the URL asks for are among those of the library. */
  private initReleases(releases: ReleaseSummary[]) {
    this.releases = releases.filter(e => !e.workingRelease && e.state === 'Published');
    const requested = (this.view().filterValue('releaseIds') as string[] | undefined) ?? [];
    this.selectedReleases = requested.map(id => this.releases.find(r => '' + r.releaseId === id)).filter(e => !!e);
    this.applyReleases();
  }

  /** The branches that are chosen go to the query: searching is the user's (or the library's change's) call. */
  applyReleases() {
    this.view().setFilter('releaseIds', this.selectedReleases.map(e => '' + e.releaseId));
  }

  get businessContextNames(): string {
    if (!this.bizCtxList) {
      return '';
    } else {
      return this.bizCtxList.map(e => e.name).join(', ');
    }
  }

  onLibraryChange(library: LibrarySummary) {
    this.libraryChoice.choose(library);
    this.selectedReleases = [];
    this.applyReleases();
    this.releaseService.getReleaseSummaryList(library.libraryId, ['Published']).subscribe(releases => {
      this.initReleases(releases);
      // A change of the library's parameter searches by itself.
      this.applyLibrary();
    });
  }

  /** The search part of a link to the list that shows the BIE (a `q=` of the current query, with what the link says). */
  linkQuery(extras: { [key: string]: any }): string {
    const q = queryToUrl(BIE_COPY_TABLE, this.view().query);
    let params = new HttpParams({fromString: base64Decode(q.substring('q='.length))});
    Object.keys(extras).forEach(key => {
      params = params.set(key, extras[key]);
    });
    return 'q=' + base64Encode(params.toString());
  }

  back() {
    this.router.navigateByUrl('/profile_bie/copy');
  }

  copy() {
    const topLevelAsbiepId: number = this.selection.selected[0].topLevelAsbiepId;
    this.service.copy(topLevelAsbiepId, this.bizCtxIds).subscribe(_ => {
      this.snackBar.openFromComponent(MultiActionsSnackBarComponent, {
        data: {
          titleIcon: 'info',
          title: 'Request Received',
          message: 'This may take a moment, so please check back shortly.'
        }
      });

      // Issue #1366
      // 'Hide Unused' option must be turned off after BIE creation.
      saveBooleanProperty(this.auth.getUserToken(), this.HIDE_UNUSED_PROPERTY_KEY, false);

      this.router.navigateByUrl('/profile_bie');
    });
  }

}
