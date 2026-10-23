import {afterNextRender, ChangeDetectionStrategy, Component, inject, signal, viewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpParams} from '@angular/common/http';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {forkJoin, of} from 'rxjs';
import {catchError, finalize, take, tap} from 'rxjs/operators';
import {CcListService} from '../cc-list/domain/cc-list.service';
import {CcListEntry} from '../cc-list/domain/cc-list';
import {ReleaseService} from '../../release-management/domain/release.service';
import {AuthService} from '../../authentication/auth.service';
import {TransferOwnershipDialogComponent} from '../../common/transfer-ownership-dialog/transfer-ownership-dialog.component';
import {AccountList} from '../../account-management/domain/accounts';
import {CcNodeService} from '../domain/core-component-node.service';
import {GithubIntegrationService, GithubStatus} from '../domain/github-integration.service';
import {
  StateChangeDialogComponent,
  StateChangeDialogResult,
  usesStateChangeDialog
} from '../state-change-dialog/state-change-dialog.component';
import {saveAsBlobResponse} from '../../common/utility';
import {ReleaseSummary, WorkingRelease} from '../../release-management/domain/release';
import {ReleaseChoice} from '../../release-management/domain/release-choice';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {AboutService} from '../../basis/about/domain/about.service';
import {TagService} from '../../tag-management/domain/tag.service';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {SettingsPreferencesService} from '../../settings-management/settings-preferences/domain/settings-preferences.service';
import {PreferencesInfo} from '../../settings-management/settings-preferences/domain/preferences';
import {loadDtFilterTypes} from '../domain/cc-filter-types';
import {LibrarySummary} from '../../library-management/domain/library';
import {LibraryChoice} from '../../library-management/domain/library-choice';
import {LibraryService} from '../../library-management/domain/library.service';
import {createTableColumns, defineTable, ScoreTableViewComponent} from '../../common/score-table';

import {CreateBdtDialogComponent} from './create-bdt-dialog/create-bdt-dialog.component';
const WORKING_STATE_LIST = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
const RELEASE_STATE_LIST = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];
const TRUE_FALSE = [{value: 'true', label: 'True'}, {value: 'false', label: 'False'}];
const separate = (state: string) => state.replace(/[A-Z][a-z]/g, (str) => ' ' + str).trim();

const DT_TABLE = defineTable({
  storageKey: 'TableColumns-DataTypePage',
  columns: [
    {id: 'state', label: 'State', width: 104, sortable: true},
    {id: 'den', label: 'DEN', width: 0, sortable: true},
    {id: 'valueDomain', label: 'Value Domain', width: 126, sortable: true},
    {id: 'sixDigitId', label: 'Six Hexadecimal ID', width: 140, sortable: true},
    {id: 'revision', label: 'Revision', width: 80, sortable: true},
    {id: 'owner', label: 'Owner', width: 140, sortable: true},
    {id: 'module', label: 'Module', width: 400, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'den', placeholder: 'Search by DEN'},
  filters: [
    {type: 'select', key: 'states', label: 'State'},
    {type: 'text', key: 'definition', label: 'Definition'},
    {type: 'text', key: 'module', label: 'CORE_COMPONENT_LIST.MODULE'},
    {type: 'select', key: 'deprecated', label: 'Deprecated', options: TRUE_FALSE, onlyWhenOne: true},
    {type: 'select', key: 'commonlyUsed', label: 'Commonly Used', options: TRUE_FALSE, onlyWhenOne: true},
    {type: 'select', key: 'newComponent', label: 'New', options: TRUE_FALSE, onlyWhenOne: true},
    {type: 'updater', key: 'ownerLoginIdList', label: 'Owner'},
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
    {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpStart: 'updateStart', httpEnd: 'updateEnd'},
    // The namespaces of the library: fetched, and many.
    {type: 'select', key: 'namespaces', label: 'Namespace', searchable: true},
    {type: 'select', key: 'tags', label: 'Tag'},
    // The branch is chosen beside the search box; it is part of the query.
    {type: 'text', key: 'releaseId', label: 'Branch', hidden: true},
  ],
});

@Component({
  standalone: false,
  selector: 'score-dt-list',
  templateUrl: './dt-list.component.html',
  styleUrls: ['./dt-list.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
})
export class DtListComponent {
  private service = inject(CcListService);
  private nodeService = inject(CcNodeService);
  protected githubService = inject(GithubIntegrationService);
  private releaseService = inject(ReleaseService);
  private libraryService = inject(LibraryService);
  private namespaceService = inject(NamespaceService);
  private aboutService = inject(AboutService);
  private auth = inject(AuthService);
  private tagService = inject(TagService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);
  private preferencesService = inject(SettingsPreferencesService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  webPageInfo = inject(WebPageInfoService);

  title = 'Data Type';

  workingStateList = WORKING_STATE_LIST;
  releaseStateList = RELEASE_STATE_LIST;
  workingRelease = WorkingRelease;

  readonly libraryChoice = new LibraryChoice(this.auth);
  readonly releaseChoice = new ReleaseChoice(this.auth);

  readonly columns = createTableColumns(DT_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<CcListEntry>>(ScoreTableViewComponent);
  selection = new SelectionModel<CcListEntry>(true, []);

  /** The list is of a library and a branch: it waits for them, and loads again when another one is chosen. */
  readonly ready = signal(false);
  readonly extraParams = signal<Record<string, string>>({});
  readonly filterOptions = signal<Record<string, { value: string, label: string }[]>>({states: [], namespaces: [], tags: []});
  /** While an action is in progress. */
  readonly busy = signal(false);

  isElasticsearchOn = false;
  fuzzySearch = false;
  preferencesInfo: PreferencesInfo;
  /** The types of data types that the list shows. */
  readonly filterTypes = loadDtFilterTypes(this.auth.getUserToken());

  readonly load = (params: HttpParams) => this.service.getCcListPage(params).pipe(tap(() => this.selection.clear()));
  readonly rowSelectable = (row: CcListEntry) => !this.isAssociation(row);

  get browserMode(): boolean {
    if (!this.preferencesInfo) {
      return false;
    }

    return this.preferencesInfo.viewSettingsInfo.pageSettings.browserViewMode;
  }

  get currentUser(): string {
    const userToken = this.auth.getUserToken();
    return (userToken) ? userToken.username : undefined;
  }

  get showDiscardBtn(): boolean {
    return this.selection.selected.length > 0 ?
      this.selection.selected.filter(e => e.state === 'Deleted').length === 0 :
      false;
  }

  get showRestoreBtn(): boolean {
    return this.selection.selected.length > 0 ?
      this.selection.selected.filter(e => e.state === 'WIP').length === 0 :
      false;
  }

  constructor() {
    afterNextRender(() => this.start());
  }

  /** Once the table view has the query of the URL: the library and the branch that it asks for, and the choices of the filters. */
  private start() {
    const requestedReleaseId = Number(this.view().filterValue('releaseId') || 0);
    this.libraryService.getLibrarySummaryList().subscribe(libraries => {
      this.libraryChoice.init(libraries);
      const libraryId = this.libraryChoice.library?.libraryId;

      this.busy.set(true);
      forkJoin([
        this.releaseService.getReleaseSummaryList(libraryId, ['Draft', 'Published']),
        this.namespaceService.getNamespaceSummaries(libraryId),
        this.aboutService.getProductInfo(),
        this.tagService.getTags(),
        this.preferencesService.load(this.auth.getUserToken())
      ]).pipe(finalize(() => this.busy.set(false))).subscribe(([releases, namespaces, productInfos, tags, preferencesInfo]) => {
        for (const productInfo of productInfos) {
          if (productInfo.productName === 'Elasticsearch' && productInfo.productVersion !== '0.0.0.0') {
            this.isElasticsearchOn = true;
          }
        }
        this.fuzzySearch = this.isElasticsearchOn;

        this.releaseChoice.init(releases, requestedReleaseId);
        this.preferencesInfo = preferencesInfo;
        this.filterOptions.update(options => ({
          ...options,
          namespaces: namespaces.map(e => ({value: '' + e.namespaceId, label: e.uri})),
          tags: tags.map(e => ({value: e.name, label: e.name}))
        }));
        this.applyChoices();
        this.ready.set(true);
      });
    });
  }

  /** The library, the branch and the types go to the query. Returns whether the parameters for the endpoint changed. */
  private applyChoices(): boolean {
    this.view().setFilter('releaseId', '' + (this.releaseChoice.release?.releaseId || 0));
    const states = this.releaseChoice.release?.workingRelease ? WORKING_STATE_LIST : RELEASE_STATE_LIST;
    this.filterOptions.update(options => ({...options, states: states.map(e => ({value: e, label: separate(e)}))}));

    const params: Record<string, string> = {libraryId: '' + this.libraryChoice.library?.libraryId};
    const types = this.filterTypes.filter(e => e.selected).map(e => e.name);
    if (types.length > 0) {
      params['types'] = types.map(e => e.toLowerCase()).join(',');
    }
    if (JSON.stringify(params) === JSON.stringify(this.extraParams())) {
      return false;
    }
    this.extraParams.set(params);
    return true;
  }

  onBranchChange(release: ReleaseSummary) {
    this.releaseChoice.choose(release);
    if (!this.applyChoices()) {
      this.view().onSearch();
    }
  }

  onLibraryChange(library: LibrarySummary) {
    this.busy.set(true);
    this.libraryChoice.choose(library);
    this.releaseService.getReleaseSummaryList(library.libraryId, ['Draft', 'Published']).pipe(
      finalize(() => this.busy.set(false))
    ).subscribe(releases => {
      this.releaseChoice.init(releases);
      if (!this.applyChoices()) {
        this.view().onSearch();
      }
    });
  }

  onFuzzySearchChange(fuzzySearch: boolean) {
    this.fuzzySearch = fuzzySearch;
    if (fuzzySearch) {
      this.view().setSort('', '');
    } else {
      this.view().setSort('lastUpdateTimestamp', 'desc');
    }
  }

  getRouterLink(ccList: CcListEntry) {
    switch (ccList.type.toUpperCase()) {
      case 'ACC':
        if (ccList.oagisComponentType === 'UserExtensionGroup') {
          return '/core_component/extension/' + ccList.manifestId;
        } else {
          return '/core_component/acc/' + ccList.manifestId;
        }

      case 'ASCCP':
        if (this.browserMode) {
          return '/core_component/browser/asccp/' + ccList.manifestId;
        } else {
          return '/core_component/asccp/' + ccList.manifestId;
        }

      case 'BCCP':
        if (this.browserMode) {
          return '/core_component/browser/bccp/' + ccList.manifestId;
        } else {
          return '/core_component/bccp/' + ccList.manifestId;
        }

      case 'DT':
        return '/data_type/' + ccList.manifestId;

      default:
        return window.location.pathname;
    }
  }

  isEditable(element: CcListEntry) {
    return element.owner.loginId === this.currentUser && element.state === 'WIP';
  }

  isAssociation(element: CcListEntry) {
    return element.type.toUpperCase() === 'ASCC' || element.type.toUpperCase() === 'BCC';
  }

  openTransferDialog(item: CcListEntry, $event) {
    if (!this.isEditable(item)) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnership(item.type, item.manifestId, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.view().reload();
        });
      }
    });
  }

  openTransferDialogMultiple() {
    if (this.selection.selected.length === 0) {
      return;
    }

    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';
    dialogConfig.data = {roles: this.auth.getUserToken().roles};
    const dialogRef = this.dialog.open(TransferOwnershipDialogComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result: AccountList) => {
      if (result) {
        this.service.transferOwnershipOnList(this.selection.selected, result.loginId).subscribe(_ => {
          this.snackBar.open('Transferred', '', {
            duration: 3000,
          });
          this.view().reload();
        });
      }
    });
  }

  multipleUpdate(action: string) {
    if (this.selection.selected.length === 0) {
      return;
    }
    const dialogConfig = this.confirmDialogService.newConfig();
    let notiMsg = 'Updated';
    let toState = action;
    let actionType = 'Update';

    switch (action) {
      case 'WIP':
      case 'Draft':
      case 'QA':
      case 'Candidate':
      case 'Production':
        dialogConfig.data.header = 'Update state to \'' + action + '\'?';
        dialogConfig.data.content = ['Are you sure you want to update the state to \'' + action + '\'?'];
        dialogConfig.data.action = 'Update';
        break;
      case 'Delete':
        toState = 'Deleted';
        notiMsg = 'Deleted';
        actionType = 'Delete';
        dialogConfig.data.header = action + ' Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?';
        dialogConfig.data.content = [
          'Are you sure you want to ' + action.toLowerCase() + ' selected Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?'
        ];
        dialogConfig.data.action = action;
        break;
      case 'Restore':
        toState = 'WIP';
        notiMsg = 'Restored';
        actionType = 'Restore';
        dialogConfig.data.header = action + ' Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?';
        dialogConfig.data.content = [
          'Are you sure you want to ' + action.toLowerCase() + ' selected Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?'
        ];
        dialogConfig.data.action = action;
        break;
      case 'Purge':
        toState = 'Purge';
        notiMsg = 'Purged';
        actionType = 'Purge';
        dialogConfig.data.header = action + ' Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?';
        dialogConfig.data.content = [
          'Are you sure you want to ' + action.toLowerCase() + ' selected Core ' + (this.selection.selected.length > 1 ? 'Components' : 'Component') + '?'
        ];
        dialogConfig.data.action = action;
        break;
      default:
        return false;
    }

    if (actionType === 'Update' && toState !== 'WIP' &&
      this.selection.selected.some(item => !item.namespaceId)) {
      this.snackBar.open('Namespace is required for all selected data types before moving to Draft/QA.', '', {
        duration: 3000
      });
      return;
    }

    const executeAction = (comments?: { [key: string]: string }, projectFieldOptionOverrides?: { [key: string]: string }) => {
      this.busy.set(true);
      let call;
      switch (actionType) {
        case 'Update':
          call = this.service.updateState(this.selection.selected, toState, comments, projectFieldOptionOverrides);
          break;
        case 'Delete':
          call = this.service.delete(this.selection.selected);
          break;
        case 'Restore':
          call = this.service.restore(this.selection.selected);
          break;
        case 'Purge':
          call = this.service.purge(this.selection.selected);
          break;
      }
      call.subscribe(_ => {
        this.snackBar.open(notiMsg, '', {
          duration: 3000
        });
        this.selection.clear();
        this.view().reload();
      }, err => {
        this.busy.set(false);
        throw err;
      });
    };

    const openConfirmDialog = () => {
      this.confirmDialogService.open(dialogConfig).afterClosed()
        .subscribe(result => {
          if (!result) {
            return;
          }
          executeAction();
        });
    };

    if (actionType === 'Update') {
      // When the GitHub integration is enabled and at least one selected data type undergoes a
      // Draft -> Candidate or Candidate -> WIP transition, a dedicated dialog shows those data
      // types' linked GitHub issues (and pre-fills per-component GitHub
      // status posts for the user to edit); otherwise the generic confirm dialog runs unchanged (issue #1533).
      // Either way, the confirm still updates ALL selected data types.
      this.githubService.getStatus()
        .pipe(take(1), catchError(() => of({enabled: false, connected: false} as GithubStatus)))
        .subscribe(status => {
          const eligibleRows = this.selection.selected.filter(item => usesStateChangeDialog(item.state, toState));
          if (status.enabled && eligibleRows.length > 0) {
            const targets = eligibleRows.map(item => ({
              ccType: item.type.toLowerCase(),
              manifestId: item.manifestId,
              name: item.den || item.name,
              state: item.state
            }));
            this.dialog.open(StateChangeDialogComponent, {
              data: {
                header: dialogConfig.data.header,
                content: dialogConfig.data.content,
                actionLabel: dialogConfig.data.action,
                toState,
                targets
              },
              autoFocus: false
            }).afterClosed().subscribe((result: StateChangeDialogResult | undefined) => {
              if (!result || !result.confirmed) {
                return;
              }
              executeAction(result.comments, result.fieldOptionOverrides);
            });
          } else {
            openConfirmDialog();
          }
        });
    } else {
      openConfirmDialog();
    }
  }

  exportStandaloneSchemas() {
    if (this.selection.selected.length === 0) {
      return;
    }

    this.busy.set(true);
    this.service.exportStandaloneSchemas(this.selection.selected).subscribe(resp => {
      saveAsBlobResponse(resp);
      this.busy.set(false);
    }, err => {
      this.busy.set(false);
    });
  }

  hasCreatePermission(): boolean {
    const userToken = this.auth.getUserToken();
    if (this.releaseChoice.release?.state !== 'Published') {
      return false;
    }
    if (userToken.roles.includes('developer')) {
      if (!this.releaseChoice.release?.workingRelease) {
        return false;
      }
    } else {
      if (this.releaseChoice.release?.workingRelease) {
        return false;
      }
    }
    return true;
  }

  createDt() {
    this.busy.set(true);

    const dialogRef = this.dialog.open(CreateBdtDialogComponent, {
      data: {
        libraryId: this.libraryChoice.library?.libraryId,
        releaseId: this.releaseChoice.release?.releaseId,
        action: 'create',
        stateList: (this.releaseChoice.release?.workingRelease) ? this.workingStateList : this.releaseStateList
      },
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    });

    dialogRef.afterClosed().pipe(finalize(() => {
      this.busy.set(false);
    })).subscribe(data => {
      if (!data) {
        return;
      }

      this.busy.set(true);
      this.nodeService.createDt(this.releaseChoice.release?.releaseId, data.manifestId).subscribe(resp => {
        return this.router.navigateByUrl('/data_type/' + resp.manifestId);
      });
    });
  }

  isInitialRevision(ccList: CcListEntry): boolean {
    return !!ccList && ccList.log.revisionNum === 1;
  }

  canToolbarAction(action: string) {
    if (this.selection.selected.length === 0) {
      return false;
    }
    switch (action) {
      case 'BackWIP':
        return this.selection.selected.filter(e => {
          if (['Draft', 'QA', 'Candidate'].indexOf(e.state) > -1 && e.owner.loginId === this.currentUser) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Draft':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.currentUser && e.release.releaseNum === this.workingRelease.releaseNum) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'QA':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.currentUser && e.release.releaseNum !== this.workingRelease.releaseNum) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Transfer':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.currentUser) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Candidate':
        return this.selection.selected.filter(e => {
          if (e.state === 'Draft' && e.owner.loginId === this.currentUser && e.release.releaseNum === this.workingRelease.releaseNum) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Production':
        return this.selection.selected.filter(e => {
          if (e.state === 'QA' && e.owner.loginId === this.currentUser && e.release.releaseNum !== this.workingRelease.releaseNum) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Restore':
        return this.selection.selected.filter(e => {
          if (e.state === 'Deleted') {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Delete':
        return this.selection.selected.filter(e => {
          if (e.state === 'WIP' && e.owner.loginId === this.currentUser && this.isInitialRevision(e)) {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Purge':
        return this.selection.selected.filter(e => {
          if (e.state === 'Deleted') {
            return e;
          }
        }).length === this.selection.selected.length;
      case 'Export':
        return this.selection.selected.filter(e => {
          if (e.type === 'ASCCP') {
            return e;
          }
        }).length === this.selection.selected.length;
      default :
        return false;
    }
  }

  displayType(elem: CcListEntry): string {
    return (elem.type.toUpperCase() === 'DT') ? elem.dtType : elem.type;
  }
}
