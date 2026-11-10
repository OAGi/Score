import { Component, DestroyRef, HostListener, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import {finalize, switchMap, take} from 'rxjs/operators';
import {AuthService} from '../../authentication/auth.service';
import {ReleaseService} from '../domain/release.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {ReleaseDetails} from '../domain/release';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {NamespaceSummary} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {RxStompService} from '../../common/score-rx-stomp';
import {Message} from '@stomp/stompjs';
import {hashCode, saveAsBlobResponse} from '../../common/utility';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {ReleaseWhatsChangedDialogComponent} from './release-whats-changed-dialog/release-whats-changed-dialog.component';
import {Title} from '@angular/platform-browser';
import {setAppTitleIfPresent} from '../../common/app-title.strategy';

@Component({
  standalone: false,
  selector: 'score-release-list',
  templateUrl: './release-detail.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./release-detail.component.css']
})
export class ReleaseDetailComponent implements OnInit {
  private service = inject(ReleaseService);
  private accountService = inject(AccountListService);
  private namespaceService = inject(NamespaceService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private auth = inject(AuthService);
  private confirmDialogService = inject(ConfirmDialogService);
  private titleService = inject(Title);
  private stompService = inject(RxStompService);
  private destroyRef = inject(DestroyRef);


  title = 'Releases Detail';
  $hashCode: string;

  releaseDetail = new ReleaseDetails();
  namespaceList: NamespaceSummary[];
  selectedNamespace: NamespaceSummary;
  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<NamespaceSummary[]> = new ReplaySubject<NamespaceSummary[]>(1);
  isLoading = false;
  private filterWired = false;

  assignable: string[];
  assigned: string[];

  // Issue #1638: opt-in to dump bie_view_order (sibling view order) into the migration script.
  includeViewOrder = false;

  ngOnInit() {
    this.watchChangesOfThisRelease();
    this.load();
  }

  /** The release is being drafted, published or taken back to 'Initialized' (or was left so by an earlier version): it is recovered, not opened. */
  get inProgress(): boolean {
    return ['Processing', 'Drafting', 'Publishing', 'Initializing'].includes(this.releaseDetail.state);
  }

  /**
   * The release can be given back: it is in the middle of a transition and nothing is doing the work (it is not busy), for example
   * because the server stopped. While the work is going on it is refused, so it is not offered.
   */
  get recoverable(): boolean {
    return ['Drafting', 'Publishing', 'Initializing'].includes(this.releaseDetail.state) && !this.releaseDetail.busy;
  }

  /** What a release that is in the middle of a transition is doing, and what recovering it does. */
  get inProgressMessage(): string {
    const busy = this.releaseDetail.busy;
    switch (this.releaseDetail.state) {
      case 'Drafting':
        return busy
          ? 'This release is being made a draft. Wait until it is done.'
          : 'This release is being made a draft, but nothing is doing it. If it stays so, e.g. because the server stopped, recover it: it goes back to \'Initialized\', as it was before.';
      case 'Initializing':
        return busy
          ? 'This release is being taken back to \'Initialized\'. Wait until it is done.'
          : 'This release is being taken back to \'Initialized\', but nothing is doing it. If it stays so, e.g. because the server stopped, recover it: it goes back to \'Draft\', as it was before.';
      case 'Publishing':
        return busy
          ? 'This release is being published. Wait until it is done.'
          : 'This release is being published, but nothing is doing it. If it stays so, e.g. because the server stopped, recover it: it goes back to \'Draft\', as it was before.';
      default:
        return 'This release was left in the middle of a transition by an earlier version. Ask an administrator to look into it.';
    }
  }

  /** When the release changes (the work for it is over, or it was given back), what is shown is loaded again. */
  private watchChangesOfThisRelease() {
    this.stompService.watch('/topic/release').pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe((message: Message) => {
      const change = JSON.parse(message.body);
      // what the user has edited is not replaced, unless the release is the one that was being processed
      if (this.releaseDetail.releaseId && change.releaseId === this.releaseDetail.releaseId && !this.isLoading
        && (this.inProgress || !this.isChanged)) {
        this.load();
      }
    });
  }

  private load() {
    this.isLoading = true;
    this.route.paramMap.pipe(take(1),
      switchMap((params: ParamMap) =>
        this.service.getReleaseDetail(params.get('id')))
    ).subscribe(resp => {
      if (!resp) {
        this.isLoading = false;
        this.snackBar.open('The requested release is unavailable.', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/release');
        return;
      }

      this.releaseDetail = resp;
      setAppTitleIfPresent(this.titleService, this.releaseDetail.releaseNum, 'Release');
      this.$hashCode = hashCode(this.releaseDetail);

      this.namespaceService.getNamespaceSummaries(this.releaseDetail.libraryId).subscribe(resp => {
        this.namespaceList = resp.filter(e => e.standard);
        this.filteredNamespaceList.next(this.namespaceList.slice());

        if (this.releaseDetail.namespaceId) {
          this.selectedNamespace = this.namespaceList.filter(e => e.namespaceId === this.releaseDetail.namespaceId)[0];
        }
      });

      if (!this.filterWired) {
        this.filterWired = true;
        this.namespaceListFilterCtrl.valueChanges
          .subscribe(() => {
            this.filterNamespaceList();
          });
      }

      this.isLoading = false;
    }, err => {
      this.isLoading = false;

      if (err.status === 404) {
        this.snackBar.open('The requested release is unavailable.', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/release');
        return;
      }

      this.snackBar.open('Something\'s wrong.', '', {
        duration: 3000,
      });
    });
  }

  get userToken() {
    return this.auth.getUserToken();
  }

  filterNamespaceList() {
    let search = this.namespaceListFilterCtrl.value;
    if (!search) {
      this.filteredNamespaceList.next(this.namespaceList.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredNamespaceList.next(
      this.namespaceList.filter(namespaceList => namespaceList.uri.toLowerCase().indexOf(search) > -1)
    );
  }

  onChangeNamespace() {
    this.releaseDetail.namespaceId = this.selectedNamespace.namespaceId;
  }

  isDisabled() {
    return !this.userToken.roles.includes('developer') || this.releaseDetail.state === 'Published' || this.inProgress;
  }

  get isChanged() {
    return this.$hashCode !== hashCode(this.releaseDetail);
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

    // Handle 'Ctrl/Command+S'
    const metaOrCtrlKeyPressed = $event.metaKey || $event.ctrlKey;
    if (metaOrCtrlKeyPressed && charCode === 's') {
      $event.preventDefault();
      $event.stopPropagation();

      this.update();
    }
  }

  get updateDisabled(): boolean {
    return (this.releaseDetail.state !== 'Initialized' && this.releaseDetail.state !== 'Draft') || !this.isChanged || !this.canUpdate();
  }

  update() {
    if (this.updateDisabled) {
      return;
    }

    this.isLoading = true;
    this.service.updateRelease(this.releaseDetail)
      .pipe(finalize(() => {
        this.isLoading = false;
      }))
      .subscribe(_ => {
        this.$hashCode = hashCode(this.releaseDetail);
        setAppTitleIfPresent(this.titleService, this.releaseDetail.releaseNum, 'Release');
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      }, err => {
      });
  }

  createDraft() {
    this.router.navigateByUrl('release/' + this.releaseDetail.releaseId + '/assign');
  }

  updateState(state: string) {
    if (state === 'Published' && !this.auth.isAdmin()) {
      this.snackBar.open('Only administrators can publish the release.', '', {
        duration: 3000,
      });
      return;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Update state to \'' + state + '\'?';
    dialogConfig.data.content = ['Are you sure you want to update the state to \'' + state + '\'?'];
    dialogConfig.data.action = 'Update';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.isLoading = true;
          this.service.updateState(this.releaseDetail.releaseId, state)
            .pipe(finalize(() => {
              this.isLoading = false;
            }))
            .subscribe(_ => {
              this.snackBar.open('Updated', '', {
                duration: 3000,
              });
              return this.router.navigateByUrl('release');
            });
        }
      });
  }

  recover() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Recover Release?';
    dialogConfig.data.content = [this.inProgressMessage];
    dialogConfig.data.action = 'Recover';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.isLoading = true;
          this.service.recover(this.releaseDetail.releaseId)
            .pipe(finalize(() => {
              this.isLoading = false;
            }))
            .subscribe(_ => {
              this.snackBar.open('Recovered', '', {
                duration: 3000,
              });
              this.load();
            });
        }
      });
  }

  canUpdate() {
    if (this.releaseDetail.releaseNum === undefined || this.releaseDetail.releaseNum === '') {
      return false;
    }
    if (this.selectedNamespace === undefined || this.selectedNamespace === null) {
      return false;
    }
    return true;
  }

  openWhatsChangedDialog() {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.width = '80vw';
    dialogConfig.height = '60%';
    dialogConfig.data = {
      releaseId: this.releaseDetail.releaseId
    };
    const dialogRef = this.dialog.open(ReleaseWhatsChangedDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
    });
  }

  generateMigrationScript() {
    this.isLoading = true;
    this.service.generateMigrationScript(this.releaseDetail.releaseId, this.includeViewOrder).subscribe(resp => {
      saveAsBlobResponse(resp);
      this.isLoading = false;
    }, err => {
      this.isLoading = false;
    });
  }
}
