import {ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {AccountList} from '../../account-management/domain/accounts';
import {AuthService} from '../../authentication/auth.service';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';
import {TransferOwnershipDialogComponent} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';
import {NamespaceListEntry} from '../domain/namespace';
import {NamespaceService} from '../domain/namespace.service';

const NAMESPACE_TABLE = defineTable({
  storageKey: 'TableColumns-NamespacePage',
  columns: [
    {id: 'uri', label: 'URI', width: 0, sortable: true},
    {id: 'prefix', label: 'Prefix', width: 100, sortable: true},
    {id: 'owner', label: 'Owner', width: 140, sortable: true},
    {id: 'std', label: 'Standard', width: 100, sortable: true},
    {id: 'description', label: 'Description', width: 400, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  fixed: [{id: 'more', position: 'end', width: 40}],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'uri', placeholder: 'Search by URI'},
  filters: [
    {type: 'updater', key: 'ownerLoginIdList', label: 'Owner'},
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
    {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpParam: 'lastUpdatedOn'},
    {type: 'text', key: 'description', label: 'Description'},
    {type: 'text', key: 'prefix', label: 'Prefix'},
    {
      type: 'select', key: 'standard', label: 'Standard',
      options: [{value: 'true', label: 'True'}, {value: 'false', label: 'False'}]
    },
  ],
});

@Component({
  standalone: false,
  selector: 'score-namespace-list',
  templateUrl: './namespace-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./namespace-list.component.css']
})
export class NamespaceListComponent implements OnInit {
  private service = inject(NamespaceService);
  private libraryService = inject(LibraryService);
  private auth = inject(AuthService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private confirmDialogService = inject(ConfirmDialogService);
  private router = inject(Router);

  title = 'Namespace';
  readonly libraryChoice = new LibraryChoice(this.auth);

  readonly columns = createTableColumns(NAMESPACE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<NamespaceListEntry>>(ScoreTableViewComponent);
  contextMenuItem: NamespaceListEntry;

  /** The list is of a library: it waits for the libraries, and loads again when another one is chosen. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  /** While an item is being discarded. */
  readonly busy = signal(false);

  readonly load = (params: HttpParams) => this.service.getNamespaceListPage(params);

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

  create() {
    if (!this.libraryChoice.library?.libraryId) {
      return;
    }
    this.router.navigateByUrl('/namespace/create');
  }

  isEditable(item: NamespaceListEntry): boolean {
    if (!item) {
      return false;
    }
    return (item.owner.loginId === this.auth.getUserToken().username);
  }

  discard(item: NamespaceListEntry) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Namespace?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this namespace?',
      'The namespace will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.busy.set(true);
          this.service.discard(item.namespaceId).pipe(finalize(() => {
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

  openTransferDialog(item: NamespaceListEntry, event?: MouseEvent) {
    if (!this.isEditable(item)) {
      return;
    }
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnership(item.namespaceId, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.view().reload();
        });
      }
    });
  }
}
