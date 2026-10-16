import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {UserToken} from '../../authentication/domain/auth';
import {BIE_FILTERS as F, BIE_TABLE} from '../../bie-management/bie-list/domain/bie.table';
import {BusinessTermService} from '../domain/business-term.service';
import {AsbieBbieListEntry} from '../domain/business-term';
import {WebPageInfoService} from '../../basis/basis.service';
import {ReleaseService} from '../../release-management/domain/release.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';

const TYPE_LIST = ['BBIE', 'ASBIE'];

const ASSIGN_BUSINESS_TERM_BIE_TABLE = defineTable({
  ...BIE_TABLE,
  filters: [
    {type: 'select', key: 'types', label: 'Type', options: TYPE_LIST.map(e => ({value: e, label: e}))},
    F.states, F.businessContext, F.version, F.remark, F.updater, F.updatedDate],
});

@Component({
  standalone: false,
  selector: 'score-bie-create-bie',
  templateUrl: './assign-business-term-bie.component.html',
  styleUrls: ['./assign-business-term-bie.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class AssignBusinessTermBieComponent {
  private businessTermService = inject(BusinessTermService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private auth = inject(AuthService);
  private router = inject(Router);
  webPageInfo = inject(WebPageInfoService);

  title = 'Assign Business Term';
  subtitle = 'Select BIE';

  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly columns = createTableColumns(ASSIGN_BUSINESS_TERM_BIE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<AsbieBbieListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<AsbieBbieListEntry>(true, []);

  /** The list is of a library, and of the BIEs of all its branches: it waits for them. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});

  readonly load = (params: HttpParams) => this.businessTermService.getAsbieBbieListPage(params);

  constructor() {
    afterNextRender(() => this.start());
  }

  /** Once the table view has the query of the URL. */
  private start() {
    // Both kinds of BIE, to begin with.
    if (!this.view().filterValue('types')?.toString()) {
      this.view().setFilter('types', TYPE_LIST);
    }
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      this.applyLibrary();
    });
  }

  /** The BIEs of the user, of every branch of the library. */
  private applyLibrary() {
    this.releaseService.getReleaseSummaryList(this.libraryChoice.library?.libraryId, ['Published']).subscribe(releases => {
      const params: Record<string, string> = {
        libraryId: '' + this.libraryChoice.library?.libraryId,
        ownerLoginIdList: this.username
      };
      const releaseIds = releases.filter(e => !e.workingRelease).map(e => '' + e.releaseId);
      if (releaseIds.length > 0) {
        params['releaseIds'] = releaseIds.join(',');
      }
      this.extraParams.set(params);
      this.ready.set(true);
    });
  }

  get username(): string {
    const userToken = this.userToken;
    return (userToken) ? userToken.username : undefined;
  }

  get roles(): string[] {
    const userToken = this.userToken;
    return (userToken) ? userToken.roles : undefined;
  }

  get userToken(): UserToken {
    return this.auth.getUserToken();
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  onLibraryChange(library: LibrarySummary) {
    this.libraryChoice.choose(library);
    this.applyLibrary();
  }

  get isDeveloper(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.roles.includes('developer');
  }

  next() {
    const bieIds = this.selection.selected.map(v => v.bieId.toString()).join(',');
    const bieTypes = this.selection.selected.map(v => v.type.toString()).join(',');
    this.router.navigate(['/business_term_management/assign_business_term/create/bt'],
      {queryParams: { bieIds, bieTypes }});
  }
}
