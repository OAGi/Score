import {Location} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormControl} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {saveAs} from 'file-saver/FileSaver';
import {ReplaySubject} from 'rxjs';
import {finalize, switchMap} from 'rxjs/operators';
import {AuthService} from '../../../authentication/auth.service';
import {Release} from '../../../bie-management/bie-create/domain/bie-create-list';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {hashCode} from '../../../common/utility';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {ModuleSet, ModuleSetRelease, ModuleSetReleaseListRequest} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';
import {UserToken} from '../../../authentication/domain/auth';
import {PageRequest} from "../../../basis/basis";

@Component({
  selector: 'score-module-set-detail',
  templateUrl: './module-set-release-detail.component.html',
  styleUrls: ['./module-set-release-detail.component.css']
})
export class ModuleSetReleaseDetailComponent implements OnInit {

  title: string;
  isUpdating: boolean;
  moduleSetRelease: ModuleSetRelease = new ModuleSetRelease();

  moduleSetListFilterCtrl: FormControl = new FormControl();
  releaseListFilterCtrl: FormControl = new FormControl();
  filteredModuleSetList: ReplaySubject<ModuleSet[]> = new ReplaySubject<ModuleSet[]>(1);
  filteredReleaseList: ReplaySubject<Release[]> = new ReplaySubject<Release[]>(1);
  moduleSetList: ModuleSet[] = [];
  releaseList: Release[] = [];

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
        return this.moduleService.getModuleSetRelease(moduleSetReleaseId);
      }))
      .subscribe(moduleSetRelease => {
        this.init(moduleSetRelease);
        this.moduleService.getModuleSetList().subscribe(resp => {
          this.initModuleSetList(resp.results);
        });

        this.releaseService.getSimpleReleases().subscribe(list => {
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

  init(moduleSetRelease: ModuleSetRelease) {
    this.moduleSetRelease = moduleSetRelease;
    this.$hashCode = hashCode(this.moduleSetRelease);
  }

  initModuleSetList(list: ModuleSet[]) {
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

  initReleaseList(list: Release[]) {
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

  exportScheme() {
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

  updateModuleSetRelease() {
    if (!this.isChanged) {
      return;
    }

    /*
     * #1280
     * If there is another default module set release, it shows a dialog to get a confirmation from the user.
     */
    if (this.moduleSetRelease.default) {
      const request = new ModuleSetReleaseListRequest();
      request.page = new PageRequest('lastUpdateTimestamp', 'desc', 0, 10);
      request.releaseId = this.moduleSetRelease.releaseId;
      request.isDefault = true;
      this.moduleService.getModuleSetReleaseList(request).subscribe(resp => {
        const results = resp.results.filter(e => this.moduleSetRelease.moduleSetReleaseId !== e.moduleSetReleaseId);
        if (results.length > 0) {
          const dialogConfig = this.confirmDialogService.newConfig();
          dialogConfig.data.header = 'Update default module set release?';
          dialogConfig.data.content = [
            'There is another default module set release, \'' + results[0].moduleSetReleaseName + '\' for \'' +
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
    request.releaseId = this.moduleSetRelease.releaseId;
    request.filters.name = this.moduleSetRelease.moduleSetReleaseName;
    this.moduleService.getModuleSetReleaseList(request).subscribe(resp => {
      const results = resp.results.filter(e => this.moduleSetRelease.moduleSetReleaseId !== e.moduleSetReleaseId);
      if (results.length > 0 &&
        results[0].moduleSetReleaseName === this.moduleSetRelease.moduleSetReleaseName) {

        const dialogConfig = this.confirmDialogService.newConfig();
        dialogConfig.data.header = 'Update module set release?';
        dialogConfig.data.content = [
          'There is another same module set release, \'' + results[0].moduleSetReleaseName + '\' for \'' +
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
      .subscribe(moduleSetRelease => {
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
        this.moduleSetRelease = moduleSetRelease;
        this.$hashCode = hashCode(this.moduleSetRelease);
      });
  }

  assignCCs() {
    return this.router.navigateByUrl('/module_management/module_set_release/' + this.moduleSetRelease.moduleSetReleaseId + '/assign');
  }
}
