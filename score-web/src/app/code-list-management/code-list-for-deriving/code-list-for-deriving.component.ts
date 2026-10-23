import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {SelectionModel} from '@angular/cdk/collections';
import {Router} from '@angular/router';
import {faLocationArrow} from '@fortawesome/free-solid-svg-icons';
import {CodeListListEntry} from '../domain/code-list';
import {CodeListService} from '../domain/code-list.service';
import {CODE_LIST_TABLE} from '../domain/code-list.table';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ReleaseChoice} from '../../release-management/domain/release-choice';
import {ReleaseService} from '../../release-management/domain/release.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';

const WORKING_STATE_LIST = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
const RELEASE_STATE_LIST = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];
const TRUE_FALSE = [{value: 'true', label: 'True'}, {value: 'false', label: 'False'}];

const CODE_LIST_FOR_DERIVING_TABLE = defineTable({
  ...CODE_LIST_TABLE,
  filters: [
    {type: 'select', key: 'states', label: 'State'},
    {type: 'select', key: 'deprecated', label: 'Deprecated', options: TRUE_FALSE, onlyWhenOne: true},
    ...CODE_LIST_TABLE.filters,
  ],
});

@Component({
  standalone: false,
  selector: 'score-code-list-for-deriving',
  templateUrl: './code-list-for-deriving.component.html',
  styleUrls: ['./code-list-for-deriving.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class CodeListForDerivingComponent {
  private service = inject(CodeListService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private auth = inject(AuthService);
  private router = inject(Router);
  webPageInfo = inject(WebPageInfoService);

  faLocationArrow = faLocationArrow;
  title = 'Derive Code List';

  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly releaseChoice = new ReleaseChoice(this.auth);
  readonly columns = createTableColumns(CODE_LIST_FOR_DERIVING_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<CodeListListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<number>(false, []);

  /** The list is of a library and a branch: it waits for them, and loads again when another one is chosen. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  readonly filterOptions = signal<Record<string, { value: string, label: string }[]>>({states: []});

  readonly load = (params: HttpParams) => this.service.getCodeListListPage(params);
  readonly rowKey = (row: CodeListListEntry) => row.codeListManifestId;

  constructor() {
    afterNextRender(() => this.start());
  }

  private start() {
    // The published code lists, to begin with.
    this.view().setFilter('states', ['Published']);
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      this.releaseService.getReleaseSummaryList(this.libraryChoice.library?.libraryId, ['Published']).subscribe(releases => {
        this.releaseChoice.init(releases, 0);
        this.applyChoices();
        this.ready.set(true);
      });
    });
  }

  /** The library and the branch that the list is of. Returns whether the parameters for the endpoint changed. */
  private applyChoices(): boolean {
    const states = this.releaseChoice.release?.workingRelease ? WORKING_STATE_LIST : RELEASE_STATE_LIST;
    this.filterOptions.update(options => ({...options, states: states.map(e => ({value: e, label: e}))}));
    const params = {
      libraryId: '' + this.libraryChoice.library?.libraryId,
      releaseId: '' + (this.releaseChoice.release?.releaseId || 0)
    };
    if (JSON.stringify(params) === JSON.stringify(this.extraParams())) {
      return false;
    }
    this.extraParams.set(params);
    return true;
  }

  onBranchChange(release: ReleaseSummary) {
    this.releaseChoice.choose(release);
    this.applyChoices();
  }

  onLibraryChange(library: LibrarySummary) {
    this.libraryChoice.choose(library);
    this.releaseService.getReleaseSummaryList(library.libraryId, ['Published']).subscribe(releases => {
      this.releaseChoice.init(releases);
      this.applyChoices();
    });
  }

  next() {
    this.router.navigate(['/code_list/create/' + this.selection.selected[0]], {
      queryParams: {
        releaseId: this.releaseChoice.release.releaseId
      },
    });
  }
}
