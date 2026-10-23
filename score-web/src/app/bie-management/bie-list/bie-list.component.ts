import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {HttpErrorResponse, HttpParams} from '@angular/common/http';
import {ReleaseSummary} from '../../release-management/domain/release';
import {ReleaseService} from '../../release-management/domain/release.service';
import {BieListDialogComponent} from '../bie-list-dialog/bie-list-dialog.component';
import {BieListService} from './domain/bie-list.service';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {SelectionModel} from '@angular/cdk/collections';
import {Clipboard} from '@angular/cdk/clipboard';
import {ActivatedRoute, Router} from '@angular/router';
import {BieListEntry} from './domain/bie-list';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {AuthService} from '../../authentication/auth.service';
import {TransferOwnershipDialogComponent} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AccountList} from '../../account-management/domain/accounts';
import {Observable, of} from 'rxjs';
import {PageResponse} from '../../basis/basis';
import {base64Decode, base64Encode} from '../../common/utility';
import {finalize} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {UserToken} from '../../authentication/domain/auth';
import {WebPageInfoService} from '../../basis/basis.service';
import {BieDeprecateDialogComponent} from '../bie-deprecate-dialog/bie-deprecate-dialog.component';
import {BieEditService} from '../bie-edit/domain/bie-edit.service';
import {MailService} from '../../common/score-mail.service';
import {MultiActionsSnackBarComponent} from '../../common/multi-actions-snack-bar/multi-actions-snack-bar.component';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';
import {BieDiagramDialogComponent} from '../bie-diagram-dialog/bie-diagram-dialog.component';
import {BieStateTransitionFlowService} from '../domain/bie-state-transition-flow.service';
import {StateDependencySelection} from '../domain/state-dependency-target';
import {BieCreateService} from '../bie-create/domain/bie-create.service';
import {createTableColumns, defineTable, queryToUrl, ScoreTableViewComponent} from '../../common/score-table';
import {BIE_FILTERS as F, BIE_TABLE, BIE_URL_FILTERS} from './domain/bie.table';

const BIE_LIST_TABLE = defineTable({
  ...BIE_TABLE,
  fixed: [{id: 'more', position: 'end'}],
  filters: [F.states, F.businessContext, F.version, F.remark, F.deprecated, F.owner, F.updater, F.updatedDate, F.releaseIds,
    ...BIE_URL_FILTERS],
});

@Component({
  standalone: false,
  selector: 'score-bie-list',
  templateUrl: './bie-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./bie-list.component.css']
})
export class BieListComponent {
  private service = inject(BieListService);
  private bieCreateService = inject(BieCreateService);
  private bieEditService = inject(BieEditService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private accountService = inject(AccountListService);
  private mailService = inject(MailService);
  private auth = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private clipboard = inject(Clipboard);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  webPageInfo = inject(WebPageInfoService);
  private stateTransitionFlowService = inject(BieStateTransitionFlowService);

  title = 'BIE';

  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly columns = createTableColumns(BIE_LIST_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<BieListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<BieListEntry>(true, [],
    true, (a, b) => a.topLevelAsbiepId === b.topLevelAsbiepId);

  /** The list is of a library: it waits for it, and loads again when another one is chosen. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  /** While an action is in progress. */
  readonly busy = signal(false);

  /** The branches of the library that the BIEs can be of, and those chosen. */
  releases: ReleaseSummary[] = [];
  selectedReleases: ReleaseSummary[] = [];

  contextMenuItem: BieListEntry;

  /** A library that none of the BIEs belong to has nothing to ask for. */
  readonly load = (params: HttpParams): Observable<PageResponse<BieListEntry>> => params.has('libraryId') ?
    this.service.getBieListPage(params) : of({length: 0, page: 0, size: 0, list: []});
  readonly rowSelectable = (row: BieListEntry) => this.isAdmin || row.owner.loginId === this.username;

  constructor() {
    afterNextRender(() => this.start());
  }

  /** Once the table view has the query of the URL: the library that it works for, and the branches that it asks for. */
  private start() {
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      this.applyLibrary();
      const libraryId = this.libraryChoice.library?.libraryId;
      if (!libraryId) {
        this.ready.set(true);
        return;
      }
      this.releaseService.getReleaseSummaryList(libraryId, ['Published']).subscribe(releases => {
        this.initReleases(releases);
        this.ready.set(true);
      });
    });
  }

  private applyLibrary() {
    const libraryId = this.libraryChoice.library?.libraryId;
    this.extraParams.set(libraryId ? {libraryId: '' + libraryId} : {});
  }

  /** The branches that the URL asks for are among those of the library. */
  private initReleases(releases: ReleaseSummary[]) {
    this.releases = releases.filter(e => !e.workingRelease);
    const requested = (this.view().filterValue('releaseIds') as string[] | undefined) ?? [];
    this.selectedReleases = requested.map(id => this.releases.find(r => '' + r.releaseId === id)).filter(e => !!e);
    this.applyReleases();
  }

  /** The branches that are chosen go to the query: searching is the user's (or the library's change's) call. */
  applyReleases() {
    this.view().setFilter('releaseIds', this.selectedReleases.map(e => '' + e.releaseId));
  }

  onLibraryChange(library?: LibrarySummary) {
    this.libraryChoice.choose(library);
    this.selectedReleases = [];
    this.applyReleases();
    if (!library?.libraryId) {
      this.releases = [];
      this.applyLibrary();
      this.view().onSearch();
      return;
    }
    this.releaseService.getReleaseSummaryList(library.libraryId, ['Published']).subscribe(releases => {
      this.initReleases(releases);
      // A change of the library's parameter searches by itself.
      this.applyLibrary();
    });
  }

  /** The search part of a link to the list that shows the BIE (a `q=` of the current query, with what the link says). */
  linkQuery(extras: { [key: string]: any }): string {
    const q = queryToUrl(BIE_LIST_TABLE, this.view().query);
    let params = new HttpParams({fromString: base64Decode(q.substring('q='.length))});
    Object.keys(extras).forEach(key => {
      params = params.set(key, extras[key]);
    });
    return 'q=' + base64Encode(params.toString());
  }

  create() {
    if (!this.libraryChoice.library?.libraryId) {
      return;
    }
    this.router.navigateByUrl('/profile_bie/create');
  }

  selectionClear() {
    this.selection = new SelectionModel<BieListEntry>(true, [],
      true, (a, b) => a.topLevelAsbiepId === b.topLevelAsbiepId);
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

  isEditable(element: BieListEntry): boolean {
    if (!element) {
      return false;
    }
    return element.owner.loginId === this.username && element.state === 'WIP';
  }

  isDeprecatable(element: BieListEntry): boolean {
    if (!element) {
      return false;
    }
    return element.owner.loginId === this.username && element.state === 'Production';
  }

  isInheritable(element: BieListEntry): boolean {
    if (!element) {
      return false;
    }
    return (this.auth.isDeveloper() && element.ownedByDeveloper ||
            this.auth.isEndUser() && !element.ownedByDeveloper);
  }

  discardAllSelected() {
    this.openDialogBieDiscard(this.selection.selected);
  }

  discard(bieList: BieListEntry) {
    this.openDialogBieDiscard([bieList,]);
  }

  openDialogBieDiscard(bieLists: BieListEntry[]) {
    this.busy.set(true);
    this.stateTransitionFlowService.requestDependencySelection({
      state: 'Discard',
      rootTopLevelAsbiepIds: bieLists.map(e => e.topLevelAsbiepId),
      validateSelection: (selection: StateDependencySelection) =>
        this.service.validateStateDependencies(
          bieLists.map(e => e.topLevelAsbiepId),
          'Discard',
          selection),
      normalizeTargets: (dependencyTargets) => {
        const selectedTopLevelAsbiepIdSet = new Set(bieLists.map(e => e.topLevelAsbiepId));
        const visibleTopLevelAsbiepIdSet = new Set(this.view().dataSource.data.map(e => e.topLevelAsbiepId));
        return dependencyTargets.map(target => ({
          ...target,
          selectable: target.selectable !== false,
          checked: target.selectable === false ? false : (target.nodeType === 'BIE' &&
          visibleTopLevelAsbiepIdSet.has(target.topLevelAsbiepId) ?
            selectedTopLevelAsbiepIdSet.has(target.topLevelAsbiepId) :
            target.checked)
        }));
      }
    }).pipe(
      finalize(() => {
        this.busy.set(false);
      })
    ).subscribe((selection?: StateDependencySelection) => {
      if (selection === undefined) {
        return;
      }
      this.busy.set(true);
      this.service.delete(
        bieLists.map(e => e.topLevelAsbiepId),
        selection.topLevelAsbiepIds
      ).pipe(finalize(() => {
        this.busy.set(false);
      })).subscribe(_ => {
        this.snackBar.open('Discarded', '', {
          duration: 3000,
        });
        this.selectionClear();
        this.view().reload();
      }, error => {
        this.openStateUpdateErrorDialog(error);
      });
    }, error => {
      this.openStateUpdateErrorDialog(error);
      this.busy.set(false);
    });
  }

  openTransferDialog(bieList: BieListEntry, $event?: Event) {
    if ($event) {
      $event.stopPropagation();
      $event.preventDefault();
    }

    if (!this.isEditable(bieList) && !this.isAdmin) {
      return;
    }

    this.accountService.getAccount(bieList.owner.userId).subscribe(resp => {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.width = window.innerWidth + 'px';
      dialogConfig.data = {roles: [resp.developer ? 'developer' : 'end-user']};
      if (this.userToken.tenant.enabled) {
        dialogConfig.data = {businessCtxIds: bieList.businessContextList.map(b => b.businessContextId)};
      }
      const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

      dialogRef.afterClosed().subscribe((result: AccountList) => {
        if (result) {
          this.busy.set(true);
          this.service.transferOwnership(bieList.topLevelAsbiepId, result.loginId).subscribe(_ => {
            this.snackBar.open('Transferred', '', {
              duration: 3000,
            });
            this.selectionClear();
            this.view().reload();
            this.busy.set(false);
          }, error => {
            this.busy.set(false);
          });
        }
      });
    });
  }

  requestOwnershipTransfer(bie: BieListEntry) {
    this.busy.set(true);
    this.mailService.sendMail('bie-ownership-transfer-request', bie.owner.userId, {
      parameters: {
        bie_link: window.location.href + '/' + bie.topLevelAsbiepId,
        bie_name: bie.den,
        topLevelAsbiepId: bie.topLevelAsbiepId,
        targetLoginId: this.auth.getUserToken().username
      }
    }).subscribe(resp => {
      this.busy.set(false);
      this.snackBar.open('Request Sent', '', {
        duration: 3000,
      });
    }, error => {
      this.busy.set(false);
    });
  }

  openFindReuseBieListDialog(bie: BieListEntry) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {
      topLevelAsbiepId: bie.topLevelAsbiepId,
      releaseNum: bie.release.releaseNum,
      den: bie.den
    };
    const dialogRef = this.dialog.open(BieListDialogComponent, dialogConfig);
  }

  createInheritedBie(bie: BieListEntry) {
    if (!bie) {
      return;
    }

    this.busy.set(true);
    this.bieCreateService.createInheritedBie(bie.topLevelAsbiepId)
      .pipe(finalize(() => {
        this.busy.set(false);
      }))
      .subscribe(_ => {
        this.snackBar.openFromComponent(MultiActionsSnackBarComponent, {
          data: {
            titleIcon: 'info',
            title: 'Request Received',
            message: 'This may take a moment, so please check back shortly.',
            action: 'Search',
            onAction: (data, snackBarRef) => {
              this.view().onSearch();
              snackBarRef.dismissWithAction();
            }
          }
        });
      }, error => {
        this.busy.set(false);
      });
  }

  canToolbarAction(action: string) {
    if (this.selection.selected.length === 0) {
      return false;
    }
    switch (action) {
      case 'BackWIP':
        return this.selection.selected.filter(e => {
          if (['QA'].indexOf(e.state) > -1 && e.owner.loginId === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'QA':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Production':
        return this.selection.selected.filter(e => {
          if (e.state === 'QA' && e.owner.loginId === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Transfer':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Discard':
        if (this.isAdmin) {
          return true;
        }
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.username) {
            return e;
          }
        }).length === this.selection.selected.length;
      default :
        return false;
    }
  }

  multipleUpdate(action: string) {
    if (this.selection.selected.length === 0) {
      return;
    }

    const notiMsg = 'Updated';
    const toState = action;
    const actionType = 'Update';
    switch (action) {
      case 'WIP':
      case 'QA':
      case 'Production':
        break;
      default:
        return false;
    }

    this.busy.set(true);
    this.stateTransitionFlowService.requestDependencySelection({
      state: action,
      rootTopLevelAsbiepIds: this.selection.selected.map(e => e.topLevelAsbiepId),
      validateSelection: (selection: StateDependencySelection) =>
        this.service.validateStateDependencies(
          this.selection.selected.map(e => e.topLevelAsbiepId),
          action,
          selection),
      normalizeTargets: (dependencyTargets) => {
        const selectedTopLevelAsbiepIdSet = new Set(this.selection.selected.map(e => e.topLevelAsbiepId));
        const visibleTopLevelAsbiepIdSet = new Set(this.view().dataSource.data.map(e => e.topLevelAsbiepId));
        return dependencyTargets.map(target => ({
          ...target,
          selectable: target.selectable !== false,
          // Respect explicit omissions on the current list page. If a BIE is
          // visible in the grid but was left unchecked, keep it unchecked when
          // it appears as an optional dependency row in the dialog.
          checked: target.selectable === false ? false : (target.nodeType === 'BIE' &&
          visibleTopLevelAsbiepIdSet.has(target.topLevelAsbiepId) ?
            selectedTopLevelAsbiepIdSet.has(target.topLevelAsbiepId) :
            target.checked)
        }));
      }
    }).pipe(
      finalize(() => {
        this.busy.set(false);
      })
    ).subscribe((selection?: StateDependencySelection) => {
      if (selection === undefined) {
        return;
      }
      this.busy.set(true);
      this.service.updateStateOnList(
        actionType,
        toState,
        this.selection.selected,
        selection.topLevelAsbiepIds,
        selection.codeListManifestIds
      ).pipe(
        finalize(() => {
          this.busy.set(false);
        })
      ).subscribe(_ => {
        this.view().reload();
        this.snackBar.open(notiMsg, '', {
          duration: 3000
        });
        this.selectionClear();
      }, error => {
        this.openStateUpdateErrorDialog(error);
      });
    }, error => {
      this.openStateUpdateErrorDialog(error);
      this.busy.set(false);
    });
  }

  private openStateUpdateErrorDialog(error: HttpErrorResponse): void {
    const errorMessageId = error?.headers?.get('x-error-message-id');
    const errorMessage = error?.headers?.get('x-error-message') || error?.message || 'Failed to update BIE state';

    this.snackBar.openFromComponent(MultiActionsSnackBarComponent, {
      data: {
        titleIcon: 'error',
        title: 'Error',
        message: errorMessage,
        action: errorMessageId ? 'View detail in Notifications' : 'Copy to clipboard',
        onAction: (data, snackBarRef) => {
          if (errorMessageId) {
            this.router.navigate(['/message/' + errorMessageId]);
            snackBarRef.dismissWithAction();
            return;
          }

          this.clipboard.copy(data.message);
        }
      }
    });
  }

  openTransferDialogMultiple() {
    if (this.selection.selected.length === 0) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.userToken.roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.busy.set(true);
        this.service.transferOwnershipOnList(this.selection.selected, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.view().reload();
          this.selectionClear();
          this.busy.set(false);
        }, error => {
          this.busy.set(false);
        });
      }
    });
  }

  deprecate(bie: BieListEntry) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {
      deprecated: bie.deprecated,
      reason: bie.deprecatedReason,
      remark: bie.deprecatedRemark
    };
    const dialogRef = this.dialog.open(BieDeprecateDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.busy.set(true);
        this.bieEditService.deprecate(bie.topLevelAsbiepId, result['reason'], result['remark']).subscribe(_ => {
          this.snackBar.open('Deprecated', '', {
            duration: 3000,
          });
          this.view().reload();
          this.selectionClear();
          this.busy.set(false);
        }, error => {
          this.busy.set(false);
        });
      }
    });
  }

  showUmlDiagram(bieListEntry: BieListEntry, $event?: MouseEvent) {
    if (!!$event) {
      $event.preventDefault();
      $event.stopPropagation();
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.height = '80vh';
    dialogConfig.data = {topLevelAsbiepId: bieListEntry.topLevelAsbiepId};
    const dialogRef = this.dialog.open(BieDiagramDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(_ => {
    });
  }

}
