import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {FormControl} from '@angular/forms';
import {SelectionModel} from '@angular/cdk/collections';
import {Router} from '@angular/router';
import {ReplaySubject} from 'rxjs';
import {BieListEntry} from '../bie-list/domain/bie-list';
import {BieListService} from '../bie-list/domain/bie-list.service';
import {BIE_FILTERS as F, BIE_TABLE, BIE_URL_FILTERS} from '../bie-list/domain/bie.table';
import {base64Decode, base64Encode, initFilter, loadBranch, saveBranch} from '../../common/utility';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {createTableColumns, defineTable, queryToUrl, ScoreTableViewComponent} from '../../common/score-table';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';

const BIE_UPLIFT_TABLE = defineTable({
  ...BIE_TABLE,
  filters: [F.states, F.businessContext, F.version, F.remark, F.deprecated, F.owner, F.updater, F.updatedDate, F.releaseIds,
    ...BIE_URL_FILTERS],
});

@Component({
  standalone: false,
  selector: 'score-bie-uplift-profile-bie',
  templateUrl: './bie-uplift-profile-bie.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./bie-uplift-profile-bie.component.css']
})
export class BieUpliftProfileBieComponent {
  private bieListService = inject(BieListService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private auth = inject(AuthService);
  private router = inject(Router);
  webPageInfo = inject(WebPageInfoService);

  title = 'Uplift BIE';
  subtitle = 'Select BIE';

  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly columns = createTableColumns(BIE_UPLIFT_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BieListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<BieListEntry>(false, []);

  /** The list is of a library and a source branch: it waits for them. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});

  readonly load = (params: HttpParams) => this.bieListService.getBieListPage(params);

  releases: ReleaseSummary[] = [];
  sourceRelease: ReleaseSummary;
  targetRelease: ReleaseSummary;
  sourceReleaseListFilterCtrl: FormControl = new FormControl();
  sourceReleaseFilteredList: ReplaySubject<ReleaseSummary[]> = new ReplaySubject<ReleaseSummary[]>(1);
  targetReleaseListFilterCtrl: FormControl = new FormControl();
  targetReleaseFilteredList: ReplaySubject<ReleaseSummary[]> = new ReplaySubject<ReleaseSummary[]>(1);

  get targetReleaseList(): ReleaseSummary[] {
    const sourceRelease: ReleaseSummary = this.sourceRelease;
    if (!!sourceRelease) {
      return this.releases.filter(val => val.releaseId > sourceRelease.releaseId);
    }
    return [];
  }

  constructor() {
    afterNextRender(() => this.start());
  }

  /** Once the table view has the query of the URL: the library that it works for, and the branches. */
  private start() {
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      this.applyLibrary();

      this.releaseService.getReleaseSummaryList(this.libraryChoice.library?.libraryId, ['Published']).subscribe(releases => {
        this.initReleases(releases);
        this.ready.set(true);
      });
    });
  }

  /** Only what the user can view. */
  private applyLibrary() {
    this.extraParams.set({libraryId: '' + this.libraryChoice.library?.libraryId, access: 'CanView'});
  }

  /** The BIEs of the source branch are listed. */
  private applySourceRelease() {
    this.view().setFilter('releaseIds', (!!this.sourceRelease) ? ['' + this.sourceRelease.releaseId] : []);
  }

  onSourceReleaseChange(source: ReleaseSummary) {
    saveBranch(this.auth.getUserToken(), 'BIE', source.releaseId);

    this.targetRelease = undefined;
    this.applySourceRelease();
    this.view().onSearch();

    // Reset targetReleaseFilteredList using targetReleaseList
    initFilter(this.targetReleaseListFilterCtrl, this.targetReleaseFilteredList, this.targetReleaseList, (e) => e.releaseNum);

    const targetReleaseList = this.targetReleaseList;
    if (targetReleaseList.length > 0) {
      this.targetRelease = targetReleaseList[0];
    }
  }

  initReleases(releases: ReleaseSummary[]) {
    this.releases = releases.filter(e => !e.workingRelease);
    if (this.releases.length > 0) {
      const savedReleaseId = loadBranch(this.auth.getUserToken(), 'BIE');
      if (savedReleaseId) {
        this.sourceRelease = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
        if (!this.sourceRelease) {
          this.sourceRelease = this.releases[0];
          saveBranch(this.auth.getUserToken(), 'BIE', this.sourceRelease.releaseId);
        }
      } else {
        this.sourceRelease = this.releases[0];
      }

      initFilter(this.sourceReleaseListFilterCtrl, this.sourceReleaseFilteredList, this.releases, (e) => e.releaseNum);
      initFilter(this.targetReleaseListFilterCtrl, this.targetReleaseFilteredList, this.targetReleaseList, (e) => e.releaseNum);

      const targetReleaseList = this.targetReleaseList;
      if (targetReleaseList.length > 0) {
        this.targetRelease = targetReleaseList[0];
      }
    }
    this.applySourceRelease();
  }

  onLibraryChange(library: LibrarySummary) {
    this.libraryChoice.choose(library);
    this.releaseService.getReleaseSummaryList(library.libraryId, ['Published']).subscribe(releases => {
      this.initReleases(releases);
      // A change of the library's parameter searches by itself.
      this.applyLibrary();
    });
  }

  /** The search part of a link to the list that shows the BIE (a `q=` of the current query, with what the link says). */
  linkQuery(extras: { [key: string]: any }): string {
    const q = queryToUrl(BIE_UPLIFT_TABLE, this.view().query);
    let params = new HttpParams({fromString: base64Decode(q.substring('q='.length))});
    Object.keys(extras).forEach(key => {
      params = params.set(key, extras[key]);
    });
    return 'q=' + base64Encode(params.toString());
  }

  next() {
    const selectedBieList = this.selection.selected[0];
    this.router.navigate(['/profile_bie/uplift/' + selectedBieList.topLevelAsbiepId], {queryParams: {targetReleaseId: this.targetRelease.releaseId}});
  }

}
