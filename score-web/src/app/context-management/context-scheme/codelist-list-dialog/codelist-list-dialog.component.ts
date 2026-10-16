import {SelectionModel} from '@angular/cdk/collections';
import {ChangeDetectionStrategy, Component, inject, OnInit, signal} from '@angular/core';
import {FormControl} from '@angular/forms';
import {HttpParams} from '@angular/common/http';
import {MatDialogRef} from '@angular/material/dialog';
import {faLocationArrow} from '@fortawesome/free-solid-svg-icons';
import {ReplaySubject} from 'rxjs';
import {AuthService} from '../../../authentication/auth.service';
import {CodeListListEntry} from '../../../code-list-management/domain/code-list';
import {CODE_LIST_TABLE} from '../../../code-list-management/domain/code-list.table';
import {CodeListService} from '../../../code-list-management/domain/code-list.service';
import {initFilter, loadBranch, loadLibrary, saveBranch, saveLibrary} from '../../../common/utility';
import {createTableColumns} from '../../../common/score-table';
import {ReleaseSummary, WorkingRelease} from '../../../release-management/domain/release';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {WebPageInfoService} from '../../../basis/basis.service';
import {LibrarySummary} from '../../../library-management/domain/library';
import {LibraryService} from '../../../library-management/domain/library.service';

@Component({
  standalone: false,
  selector: 'score-context-scheme-value-dialog',
  templateUrl: './codelist-list-dialog.component.html',
  styleUrls: ['./codelist-list-dialog.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager
})
export class CodelistListDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<CodelistListDialogComponent>>(MatDialogRef);
  private service = inject(CodeListService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private auth = inject(AuthService);
  webPageInfo = inject(WebPageInfoService);

  faLocationArrow = faLocationArrow;
  title = 'Code List';
  workingRelease = WorkingRelease;
  releaseStateList = ['Published', 'Production'];
  /** Where the chosen branch is remembered. */
  private readonly cookieType = 'CC';

  readonly columns = createTableColumns(CODE_LIST_TABLE);
  selection = new SelectionModel<CodeListListEntry>(true, []);

  libraries: LibrarySummary[] = [];
  mappedLibraries: { library: LibrarySummary, selected: boolean }[] = [];
  library: LibrarySummary = new LibrarySummary();
  release: ReleaseSummary = new ReleaseSummary();
  releases: ReleaseSummary[] = [];
  releaseListFilterCtrl: FormControl = new FormControl();
  filteredReleaseList: ReplaySubject<ReleaseSummary[]> = new ReplaySubject<ReleaseSummary[]>(1);

  /** The table lists what the library and the branch hold, so it waits for them. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});

  readonly load = (params: HttpParams) => this.service.getCodeListListPage(params);

  ngOnInit() {
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.initLibraries(libraries);

      this.releaseService.getReleaseSummaryList(this.library.libraryId, ['Published']).subscribe(releases => {
        this.initReleases(releases);
        this.updateExtraParams();
        this.ready.set(true);
      });
    });
  }

  private updateExtraParams() {
    this.extraParams.set({
      libraryId: '' + this.library.libraryId,
      releaseId: '' + (this.release?.releaseId || 0),
      states: this.releaseStateList.join(',')
    });
  }

  onBranchChange() {
    saveBranch(this.auth.getUserToken(), this.cookieType, this.release.releaseId);
    this.updateExtraParams();
  }

  initLibraries(libraries: LibrarySummary[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
      }
      if (!this.library || !this.library.libraryId) {
        this.library = this.libraries.find(e => e.isDefault) || this.libraries[0];
      }
      if (this.library) {
        saveLibrary(this.auth.getUserToken(), this.library.libraryId);
      }
      this.mappedLibraries = this.libraries.map(e => {
        return {library: e, selected: (this.library.libraryId === e.libraryId)};
      });
    }
  }

  initReleases(releases: ReleaseSummary[]) {
    this.releases = [...releases];
    if (this.releases.length > 0) {
      if (this.release.releaseId === 0) {
        const savedReleaseId = loadBranch(this.auth.getUserToken(), this.cookieType);
        if (savedReleaseId) {
          this.release = this.releases.filter(e => e.releaseId === savedReleaseId)[0];
          if (!this.release) {
            this.release = this.releases[0];
            saveBranch(this.auth.getUserToken(), this.cookieType, this.release.releaseId);
          }
        }
      } else {
        this.release = this.releases.filter(e => e.releaseId === this.release.releaseId)[0];
      }
    }
    if (!this.release || this.release.releaseId === 0) {
      this.release = this.releases[0];
    }
    initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releases, (e) => e.releaseNum);
  }

  onLibraryChange(library: LibrarySummary) {
    this.library = library;
    this.releaseService.getReleaseSummaryList(this.library.libraryId, ['Published']).subscribe(releases => {
      saveLibrary(this.auth.getUserToken(), this.library.libraryId);
      this.initReleases(releases);
      this.updateExtraParams();
    });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.selection.selected.length > 0) {
      this.service.getCodeListDetails(this.selection.selected[0].codeListManifestId).subscribe(codeList => {
        return this.dialogRef.close(codeList);
      });
    }
  }
}
