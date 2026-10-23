import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {forkJoin} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {BusinessContextService} from '../../context-management/business-context/domain/business-context.service';
import {BusinessContextSummary} from '../../context-management/business-context/domain/business-context';
import {CcListEntry} from '../../cc-management/cc-list/domain/cc-list';
import {CcListService} from '../../cc-management/cc-list/domain/cc-list.service';
import {CC_DIALOG_FILTERS as F, CC_WITH_TYPE_TABLE} from '../../cc-management/domain/cc-dialog.table';
import {BieCreateService} from './domain/bie-create.service';
import {saveBooleanProperty} from '../../common/utility';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ReleaseChoice} from '../../release-management/domain/release-choice';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {TagService} from '../../tag-management/domain/tag.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';

const STATE_LIST = ['Published', 'Production'];

const BIE_CREATE_ASCCP_TABLE = defineTable({
  ...CC_WITH_TYPE_TABLE,
  filters: [
    {...F.states, options: STATE_LIST.map(e => ({value: e, label: e.replace(/[A-Z][a-z]/g, (str) => ' ' + str).trim()}))},
    F.definition, F.module, F.deprecated, F.owner, F.updater, F.updatedDate, F.tags,
    // The branch is chosen beside the search box; it is part of the query.
    {type: 'text', key: 'releaseId', label: 'Branch', hidden: true},
    // The business contexts that the BIE is for: kept in the URL, as the previous step gave them.
    {type: 'text', key: 'businessContextIdList', label: 'Business contexts', hidden: true},
  ],
});

@Component({
  standalone: false,
  selector: 'score-bie-create-asccp',
  templateUrl: './bie-create-asccp.component.html',
  styleUrls: ['./bie-create-asccp.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class BieCreateAsccpComponent {
  private bizCtxService = inject(BusinessContextService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private ccListService = inject(CcListService);
  private service = inject(BieCreateService);
  private tagService = inject(TagService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  webPageInfo = inject(WebPageInfoService);

  title = 'Create BIE';
  subtitle = 'Select Top-Level Concept';

  businessContextIdList: number[] = [];
  businessContextList: BusinessContextSummary[] = [];

  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly releaseChoice = new ReleaseChoice(this.auth, 'BIE');
  readonly columns = createTableColumns(BIE_CREATE_ASCCP_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<CcListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<CcListEntry>(false, []);

  /** The list is of a library and a branch, for the business contexts: it waits for them. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  readonly filterOptions = signal<Record<string, { value: string, label: string }[]>>({tags: []});
  /** While the BIE is being made. */
  readonly busy = signal(false);

  readonly load = (params: HttpParams) => this.ccListService.getCcListPage(params.delete('businessContextIdList'));

  HIDE_UNUSED_PROPERTY_KEY = 'BIE-Settings-Hide-Unused';

  constructor() {
    afterNextRender(() => this.start());
  }

  /** Once the table view has the query of the URL: the library, the branch and the business contexts that it asks for. */
  private start() {
    const requestedReleaseId = Number(this.view().filterValue('releaseId') || 0);
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      this.applyLibrary();

      // The business contexts are in the URL: as a parameter, or in the query.
      const businessContextIdList = this.route.snapshot.queryParamMap.get('businessContextIdList') ||
        (this.view().filterValue('businessContextIdList') as string);
      this.view().setFilter('businessContextIdList', businessContextIdList);

      forkJoin([
        this.releaseService.getReleaseSummaryList(this.libraryChoice.library?.libraryId, ['Published']),
        this.tagService.getTags(),
        this.bizCtxService.getBusinessContextsByBizCtxIds(businessContextIdList.split(',').map(e => Number(e)))
      ]).subscribe(([releases, tags, resp]) => {
        this.releaseChoice.init(releases.filter(e => !e.workingRelease), requestedReleaseId);
        this.filterOptions.set({tags: tags.map(e => ({value: e.name, label: e.name}))});
        if (resp.length === 0) {
          this.router.navigateByUrl('/profile_bie/create');
        } else {
          this.businessContextIdList = resp.map(e => e.businessContextId);
          this.businessContextList = resp;
          // Only the states that a BIE can be made of.
          this.view().setFilter('states', STATE_LIST);
          this.applyRelease();
          this.ready.set(true);
        }
      }, err => {
        console.error(err);
      });
    });
  }

  /** Only the top-level concepts that a BIE can be made of. */
  private applyLibrary() {
    this.extraParams.set({libraryId: '' + this.libraryChoice.library?.libraryId, types: 'asccp', isBIEUsable: 'true'});
  }

  private applyRelease() {
    this.view().setFilter('releaseId', '' + (this.releaseChoice.release?.releaseId || 0));
  }

  get businessContextNames(): string {
    if (!this.businessContextList) {
      return '';
    } else {
      return this.businessContextList.map(e => e.name).join(', ');
    }
  }

  onBranchChange(release: ReleaseSummary) {
    this.releaseChoice.choose(release);
    this.applyRelease();
    this.view().onSearch();
  }

  onLibraryChange(library: LibrarySummary) {
    this.libraryChoice.choose(library);
    this.releaseService.getReleaseSummaryList(library.libraryId, ['Published']).subscribe(releases => {
      this.releaseChoice.init(releases.filter(e => !e.workingRelease));
      this.applyRelease();
      this.view().onSearch();
      this.applyLibrary();
    });
  }

  back() {
    this.router.navigateByUrl('/profile_bie/create');
  }

  create() {
    const asccpManifestId: number = this.selection.selected[0].manifestId;
    this.busy.set(true);
    this.service.create(asccpManifestId, this.businessContextIdList)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe(resp => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });

        // Issue #1366
        // 'Hide Unused' option must be turned off after BIE creation.
        saveBooleanProperty(this.auth.getUserToken(), this.HIDE_UNUSED_PROPERTY_KEY, false);

        this.router.navigateByUrl('/profile_bie/' + resp.topLevelAsbiepId);
      });
  }

  get isDeveloper(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.roles.includes('developer');
  }

}
