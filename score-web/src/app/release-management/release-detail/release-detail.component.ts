import {Component, OnInit} from '@angular/core';
import {finalize, switchMap} from 'rxjs/operators';
import {AuthService} from '../../authentication/auth.service';
import {ReleaseService} from '../domain/release.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {ReleaseDetail} from '../domain/release';
import {AccountListService} from '../../account-management/domain/account-list.service';
import {NamespaceList} from '../../namespace-management/domain/namespace';
import {NamespaceService} from '../../namespace-management/domain/namespace.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {hashCode} from '../../common/utility';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {saveAs} from 'file-saver';
import {MatDialog, MatDialogConfig} from "@angular/material/dialog";
import {ReleaseWhatsChangedDialogComponent} from "./release-whats-changed-dialog/release-whats-changed-dialog.component";

@Component({
  selector: 'score-release-list',
  templateUrl: './release-detail.component.html',
  styleUrls: ['./release-detail.component.css']
})
export class ReleaseDetailComponent implements OnInit {

  title = 'Releases Detail';
  $hashCode: string;

  releaseDetail = new ReleaseDetail();
  namespaceList: NamespaceList[];
  selectedNamespace: NamespaceList;
  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<NamespaceList[]> = new ReplaySubject<NamespaceList[]>(1);
  isLoading = false;

  assignable: string[];
  assigned: string[];

  constructor(private service: ReleaseService,
              private accountService: AccountListService,
              private namespaceService: NamespaceService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private route: ActivatedRoute,
              private router: Router,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService) {
  }

  ngOnInit() {
    this.isLoading = true;
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) =>
        this.service.getReleaseDetail(params.get('id')))
    ).subscribe(resp => {
      this.releaseDetail = resp;
      this.$hashCode = hashCode(this.releaseDetail);

      this.namespaceService.getNamespaceList().subscribe(resp => {
        this.namespaceList = resp.list.filter(e => e.std);
        this.filteredNamespaceList.next(this.namespaceList.slice());

        if (this.releaseDetail.namespaceId) {
          this.selectedNamespace = this.namespaceList.filter(e => e.namespaceId === this.releaseDetail.namespaceId)[0];
        }
      });

      this.namespaceListFilterCtrl.valueChanges
        .subscribe(() => {
          this.filterNamespaceList();
        });

      this.isLoading = false;
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
    return !this.userToken.roles.includes('developer') || this.releaseDetail.state === 'Published';
  }

  get isChanged() {
    return this.$hashCode !== hashCode(this.releaseDetail);
  }

  update() {
    this.isLoading = true;
    this.service.updateRelease(this.releaseDetail)
      .pipe(finalize(() => {
        this.isLoading = false;
      }))
      .subscribe(_ => {
        this.$hashCode = hashCode(this.releaseDetail);
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
    this.service.generateMigrationScript(this.releaseDetail.releaseId).subscribe(resp => {
      const blob = new Blob([resp.body], {type: resp.headers.get('Content-Type')});
      saveAs(blob, this._getFilenameFromContentDisposition(resp));
      this.isLoading = false;
    }, err => {
      this.isLoading = false;
    });
  }

  _getFilenameFromContentDisposition(resp) {
    const contentDisposition = resp.headers.get('Content-Disposition') || '';
    const matches = /filename=([^;]+)/ig.exec(contentDisposition);
    return (matches[1] || 'untitled').replace(/\"/gi, '').trim();
  }
}
