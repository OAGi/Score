import {ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {AuthService} from '../../../authentication/auth.service';
import {LibraryService} from '../../../library-management/domain/library.service';
import {LibraryChoice} from '../../../library-management/domain/library-choice';
import {LibrarySummary} from '../../../library-management/domain/library';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../../common/score-table';
import {ModuleService} from '../../domain/module.service';
import {ModuleSetReleaseListEntry} from '../../domain/module';

const MODULE_SET_RELEASE_TABLE = defineTable({
  storageKey: 'TableColumns-ModuleSetReleasePage',
  columns: [
    {id: 'name', label: 'Name', width: 0, sortable: true},
    {id: 'release', label: 'Release Num', width: 300, sortable: true},
    {id: 'default', label: 'Default', width: 80, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  fixed: [{id: 'more', position: 'end', width: 40}],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'name', placeholder: 'Search by Name'},
  filters: [
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
    {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpParam: 'lastUpdatedOn'},
  ],
});

@Component({
  standalone: false,
  selector: 'score-module-set-release-list',
  templateUrl: './module-set-release-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./module-set-release-list.component.css']
})
export class ModuleSetReleaseListComponent implements OnInit {
  private service = inject(ModuleService);
  private libraryService = inject(LibraryService);
  private auth = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private confirmDialogService = inject(ConfirmDialogService);
  private router = inject(Router);

  title = 'Module Set Release';
  readonly libraryChoice = new LibraryChoice(this.auth);

  readonly columns = createTableColumns(MODULE_SET_RELEASE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<ModuleSetReleaseListEntry>>(ScoreTableViewComponent);
  contextMenuItem: ModuleSetReleaseListEntry;

  /** The list is of a library: it waits for the libraries, and loads again when another one is chosen. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  /** While an item is being discarded. */
  readonly busy = signal(false);

  readonly load = (params: HttpParams) => this.service.getModuleSetReleaseListPage(params);

  ngOnInit() {
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      this.updateExtraParams();
      this.ready.set(!!this.libraryChoice.library?.libraryId);
    });
  }

  private updateExtraParams() {
    this.extraParams.set({libraryId: '' + this.libraryChoice.library?.libraryId});
  }

  onLibraryChange(library?: LibrarySummary) {
    this.libraryChoice.choose(library);
    this.updateExtraParams();
  }

  get isDeveloper(): boolean {
    return (this.auth.getUserToken()?.roles ?? []).includes('developer');
  }

  isEditable(item: ModuleSetReleaseListEntry) {
    if (!item) {
      return false;
    }
    return true;
  }

  discard(item: ModuleSetReleaseListEntry) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Module Set Release?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this module set release?',
      'The module set will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.busy.set(true);
          this.service.discardModuleSetRelease(item.moduleSetReleaseId).pipe(finalize(() => {
            this.busy.set(false);
          })).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.view().reload();
          });
        }
      });
  }

  create() {
    if (!this.libraryChoice.library?.libraryId) {
      return;
    }
    this.router.navigateByUrl('/module_management/module_set_release/create');
  }
}
