import {Location} from '@angular/common';
import {Component, HostListener, OnInit} from '@angular/core';
import {FormControl} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {saveAs} from 'file-saver';
import {ReplaySubject} from 'rxjs';
import {finalize, switchMap} from 'rxjs/operators';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {hashCode} from '../../../common/utility';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {ModuleSetReleaseDetails, ModuleSetReleaseListRequest, ModuleSetSummary} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';
import {UserToken} from '../../../authentication/domain/auth';
import {PageRequest} from '../../../basis/basis';
import {
  ModuleSetReleaseValidationDialogComponent
} from './module-set-release-validation-dialog/module-set-release-validation-dialog.component';
import {ReleaseSummary} from '../../../release-management/domain/release';

@Component({
  selector: 'score-module-set-detail',
  templateUrl: './module-set-release-detail.component.html',
  styleUrls: ['./module-set-release-detail.component.css']
})
export class ModuleSetReleaseDetailComponent implements OnInit {

  title: string;
  isUpdating: boolean;
  moduleSetRelease: ModuleSetReleaseDetails = new ModuleSetReleaseDetails();

  moduleSetListFilterCtrl: FormControl = new FormControl();
  releaseListFilterCtrl: FormControl = new FormControl();
  filteredModuleSetList: ReplaySubject<ModuleSetSummary[]> = new ReplaySubject<ModuleSetSummary[]>(1);
  filteredReleaseList: ReplaySubject<ReleaseSummary[]> = new ReplaySubject<ReleaseSummary[]>(1);
  moduleSetList: ModuleSetSummary[] = [];
  releaseList: ReleaseSummary[] = [];

  private $hashCode: string;

  constructor(private moduleService: ModuleService,
              private releaseService: ReleaseService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService) {
    this.title = (this.roles.includes('developer')) ? 'Edit Module Set Release' : 'View Module Set Release';
  }

  get isChanged(): boolean {
    return this.$hashCode !== hashCode(this.moduleSetRelease);
  }

  ngOnInit(): void {
    this.moduleSetList = [];
    this.releaseList = [];

    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        const moduleSetReleaseId = Number(params.get('moduleSetReleaseId'));
        return this.moduleService.getModuleSetReleaseDetails(moduleSetReleaseId);
      }))
      .subscribe(moduleSetRelease => {
        this.init(moduleSetRelease);

        this.moduleService.getModuleSetSummaries(moduleSetRelease.library.libraryId).subscribe(resp => {
          this.initModuleSetList(resp);
        });

        this.releaseService.getReleaseSummaryList(moduleSetRelease.library.libraryId).subscribe(list => {
          this.initReleaseList(list);
        });
      });
  }

  get userToken(): UserToken {
    return this.auth.getUserToken();
  }

  get roles(): string[] {
    const userToken = this.userToken;
    return (userToken) ? userToken.roles : [];
  }

  init(moduleSetRelease: ModuleSetReleaseDetails) {
    this.moduleSetRelease = moduleSetRelease;
    this.$hashCode = hashCode(this.moduleSetRelease);
  }

  initModuleSetList(list: ModuleSetSummary[]) {
    this.moduleSetList.push(...list);
    this.moduleSetListFilterCtrl.valueChanges
      .subscribe(() => {
        let search = this.moduleSetListFilterCtrl.value;
        if (!search) {
          this.filteredModuleSetList.next(this.moduleSetList.slice());
          return;
        } else {
          search = search.toLowerCase();
        }
        this.filteredModuleSetList.next(
          this.moduleSetList.filter(e => e.name.toLowerCase().indexOf(search) > -1)
        );
      });
    this.filteredModuleSetList.next(this.moduleSetList.slice());
  }

  initReleaseList(list: ReleaseSummary[]) {
    this.releaseList.push(...list);
    this.releaseListFilterCtrl.valueChanges
      .subscribe(() => {
        let search = this.releaseListFilterCtrl.value;
        if (!search) {
          this.filteredReleaseList.next(this.releaseList.slice());
          return;
        } else {
          search = search.toLowerCase();
        }
        this.filteredReleaseList.next(
          this.releaseList.filter(e => e.releaseNum.toLowerCase().indexOf(search) > -1)
        );
      });
    this.filteredReleaseList.next(this.releaseList.slice());
  }

  validateSchemas() {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.width = '80vw';
    dialogConfig.height = '60%';
    dialogConfig.data = {
      moduleSetReleaseId: this.moduleSetRelease.moduleSetReleaseId
    };
    const dialogRef = this.dialog.open(ModuleSetReleaseValidationDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
    });
  }

  exportSchemas() {
    this.isUpdating = true;
    this.moduleService.export(this.moduleSetRelease.moduleSetReleaseId).subscribe(resp => {
      const blob = new Blob([resp.body], {type: resp.headers.get('Content-Type')});
      saveAs(blob, this._getFilenameFromContentDisposition(resp));
      this.isUpdating = false;
    }, err => {
      this.isUpdating = false;
    });
  }

  _getFilenameFromContentDisposition(resp) {
    const contentDisposition = resp.headers.get('Content-Disposition') || '';
    const matches = /filename=([^;]+)/ig.exec(contentDisposition);
    return (matches[1] || 'untitled').replace(/\"/gi, '').trim();
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

    // Handle 'Ctrl/Command+S'
    const metaOrCtrlKeyPressed = $event.metaKey || $event.ctrlKey;
    if (metaOrCtrlKeyPressed && charCode === 's') {
      $event.preventDefault();
      $event.stopPropagation();

      this.updateModuleSetRelease();
    }
  }

  get updateDisabled(): boolean {
    return !this.roles.includes('developer') || !this.isChanged;
  }

  updateModuleSetRelease() {
    if (this.updateDisabled) {
      return;
    }

    /*
     * #1280
     * If there is another default module set release, it shows a dialog to get a confirmation from the user.
     */
    if (this.moduleSetRelease.isDefault) {
      const request = new ModuleSetReleaseListRequest();
      request.page = new PageRequest('lastUpdateTimestamp', 'desc', 0, 10);
      request.library.libraryId = this.moduleSetRelease.library.libraryId;
      request.releaseId = this.moduleSetRelease.release.releaseId;
      request.isDefault = true;
      this.moduleService.getModuleSetReleaseList(request).subscribe(resp => {
        const results = resp.list.filter(e => this.moduleSetRelease.moduleSetReleaseId !== e.moduleSetReleaseId);
        if (results.length > 0) {
          const dialogConfig = this.confirmDialogService.newConfig();
          dialogConfig.data.header = 'Update default module set release?';
          dialogConfig.data.content = [
            'There is another default module set release, \'' + results[0].name + '\' for \'' +
            results[0].releaseNum + '\' branch.',
            'Are you sure you want to update this module set release as a default?',
          ];
          dialogConfig.data.action = 'Update';

          this.confirmDialogService.open(dialogConfig).afterClosed()
            .subscribe(result => {
              if (result) {
                this.doUpdateModuleSetRelease();
              }
            });
        } else {
          this.doUpdateModuleSetRelease();
        }
      });
    } else {
      this.doUpdateModuleSetRelease();
    }
  }

  doUpdateModuleSetRelease() {
    const request = new ModuleSetReleaseListRequest();
    request.page = new PageRequest('lastUpdateTimestamp', 'desc', 0, 10);
    request.library.libraryId = this.moduleSetRelease.library.libraryId;
    request.releaseId = this.moduleSetRelease.release.releaseId;
    request.filters.name = this.moduleSetRelease.name;
    this.moduleService.getModuleSetReleaseList(request).subscribe(resp => {
      const results = resp.list.filter(e => this.moduleSetRelease.moduleSetReleaseId !== e.moduleSetReleaseId);
      if (results.length > 0 &&
        results[0].name === this.moduleSetRelease.name) {

        const dialogConfig = this.confirmDialogService.newConfig();
        dialogConfig.data.header = 'Update module set release?';
        dialogConfig.data.content = [
          'There is another same module set release, \'' + results[0].name + '\' for \'' +
          results[0].releaseNum + '\' branch.',
          'Are you sure you want to update this module set release?',
        ];
        dialogConfig.data.action = 'Update';

        this.confirmDialogService.open(dialogConfig).afterClosed()
          .subscribe(result => {
            if (result) {
              this._doUpdateModuleSetRelease();
            }
          });
      } else {
        this._doUpdateModuleSetRelease();
      }
    });
  }

  _doUpdateModuleSetRelease() {
    this.isUpdating = true;

    this.moduleService.updateModuleSetRelease(this.moduleSetRelease)
        .pipe(finalize(() => {
          this.isUpdating = false;
        }))
        .subscribe(_ => {
          this.snackBar.open('Updated', '', {
            duration: 3000,
          });

          this.moduleService.getModuleSetReleaseDetails(this.moduleSetRelease.moduleSetReleaseId).subscribe(moduleSetRelease => {
            this.moduleSetRelease = moduleSetRelease;
            this.$hashCode = hashCode(this.moduleSetRelease);
          });
        });
  }

  assignCCs() {
    return this.router.navigateByUrl('/module_management/module_set_release/' + this.moduleSetRelease.moduleSetReleaseId + '/assign');
  }
}
