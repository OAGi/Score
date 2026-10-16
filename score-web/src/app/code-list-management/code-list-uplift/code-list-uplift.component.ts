import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {FormControl} from '@angular/forms';
import {SelectionModel} from '@angular/cdk/collections';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';
import {faLocationArrow} from '@fortawesome/free-solid-svg-icons';
import {ReplaySubject} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {CodeListListEntry} from '../domain/code-list';
import {CodeListService} from '../domain/code-list.service';
import {CODE_LIST_TABLE} from '../domain/code-list.table';
import {initFilter, loadBranch, saveBranch} from '../../common/utility';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';

const RELEASE_STATE_LIST = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];
const TRUE_FALSE = [{value: 'true', label: 'True'}, {value: 'false', label: 'False'}];

const CODE_LIST_UPLIFT_TABLE = defineTable({
  ...CODE_LIST_TABLE,
  filters: [
    {type: 'select', key: 'states', label: 'State', options: RELEASE_STATE_LIST.map(e => ({value: e, label: e}))},
    {type: 'text', key: 'definition', label: 'Definition'},
    {type: 'text', key: 'module', label: 'Module'},
    {type: 'select', key: 'deprecated', label: 'Deprecated', options: TRUE_FALSE, onlyWhenOne: true},
    ...CODE_LIST_TABLE.filters,
    // The source branch is chosen beside the search box; it is part of the query.
    {type: 'text', key: 'releaseId', label: 'Source branch', hidden: true},
  ],
});

@Component({
  standalone: false,
  selector: 'score-code-list-uplift',
  templateUrl: './code-list-uplift.component.html',
  styleUrls: ['./code-list-uplift.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class CodeListUpliftComponent {
  private service = inject(CodeListService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private auth = inject(AuthService);
  private confirmDialogService = inject(ConfirmDialogService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  webPageInfo = inject(WebPageInfoService);

  faLocationArrow = faLocationArrow;
  title = 'Uplift Code List';
  /** Where the chosen branch is remembered. */
  private readonly cookieType = 'CC';

  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly columns = createTableColumns(CODE_LIST_UPLIFT_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<CodeListListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<CodeListListEntry>(false, []);

  /** The list is of a library and a source branch: it waits for them. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  /** While a code list is being uplifted. */
  readonly busy = signal(false);

  readonly load = (params: HttpParams) => this.service.getCodeListListPage(params);

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
    if (this.auth.getUserToken().roles.includes('developer')) {
      this.snackBar.open('Unauthorized access.', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/');
      return;
    }
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

  /** The code lists that the user can view, which are not the developers'. */
  private applyLibrary() {
    this.extraParams.set({libraryId: '' + this.libraryChoice.library?.libraryId, access: 'CanView', ownedByDeveloper: 'false'});
  }

  /** The code lists of the source branch are listed. */
  private applySourceRelease() {
    this.view().setFilter('releaseId', '' + (this.sourceRelease?.releaseId || 0));
  }

  onSourceReleaseChange(source: ReleaseSummary) {
    saveBranch(this.auth.getUserToken(), this.cookieType, source.releaseId);

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
      const savedReleaseId = loadBranch(this.auth.getUserToken(), this.cookieType);
      if (savedReleaseId) {
        this.sourceRelease = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
        if (!this.sourceRelease) {
          this.sourceRelease = this.releases[0];
          saveBranch(this.auth.getUserToken(), this.cookieType, this.sourceRelease.releaseId);
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

  next() {
    const selectedBieList = this.selection.selected[0];
    this.busy.set(true);
    this.service.uplift(selectedBieList, this.targetRelease.releaseId)
        .pipe(finalize(() => {
          this.busy.set(false);
        }))
        .subscribe(result => {
          this.snackBar.open('Uplifted', '', {
            duration: 3000,
          });

          if (result.duplicatedValues?.length > 0) {
            const dialogConfig = this.confirmDialogService.newConfig();
            dialogConfig.data.header = 'Overwritten Values';
            dialogConfig.data.content = [
              'These values are overwritten with the values of the target release.'
            ];

            result.duplicatedValues.forEach(e => {
              dialogConfig.data.content.push(' - ' + e);
            });

            this.confirmDialogService.open(dialogConfig).afterClosed()
                .subscribe(_ => {
                  this.router.navigateByUrl('/code_list/' + result.codeListManifestId);
                });

          } else {
            this.router.navigateByUrl('/code_list/' + result.codeListManifestId);
          }
        });
  }

}

