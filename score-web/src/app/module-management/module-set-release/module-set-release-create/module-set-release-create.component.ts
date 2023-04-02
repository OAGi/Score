import {Location} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormControl} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {forkJoin, ReplaySubject} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {AuthService} from '../../../authentication/auth.service';
import {Release} from '../../../bie-management/bie-create/domain/bie-create-list';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {hashCode, initFilter} from '../../../common/utility';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {ModuleSet, ModuleSetRelease, ModuleSetReleaseListRequest} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';
import {PageRequest} from '../../../basis/basis';

@Component({
  selector: 'score-module-set-create',
  templateUrl: './module-set-release-create.component.html',
  styleUrls: ['./module-set-release-create.component.css']
})
export class ModuleSetReleaseCreateComponent implements OnInit {

  title = 'Create Module Set Release';
  isUpdating: boolean;
  moduleSetRelease: ModuleSetRelease = new ModuleSetRelease();

  copyFromOther = false;

  moduleSetListFilterCtrl: FormControl = new FormControl();
  releaseListFilterCtrl: FormControl = new FormControl();
  moduleSetReleaseListFilterCtrl: FormControl = new FormControl();
  filteredModuleSetList: ReplaySubject<ModuleSet[]> = new ReplaySubject<ModuleSet[]>(1);
  filteredReleaseList: ReplaySubject<Release[]> = new ReplaySubject<Release[]>(1);
  filteredModuleSetReleaseList: ReplaySubject<ModuleSetRelease[]> = new ReplaySubject<ModuleSetRelease[]>(1);
  moduleSetList: ModuleSet[] = [];
  releaseList: Release[] = [];
  moduleSetReleaseList: ModuleSetRelease[] = [];
  copyTargetModuleSetRelease: ModuleSetRelease;

  moduleSetReleaseRequest: ModuleSetReleaseListRequest;

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
  }

  get canCreate(): boolean {
    if (this.copyFromOther) {
      if (!this.copyTargetModuleSetRelease) {
        return false;
      }
    }
    return this.moduleSetRelease.releaseId !== undefined && this.moduleSetRelease.moduleSetId !== undefined;
  }

  ngOnInit(): void {
    this.moduleSetList = [];
    this.releaseList = [];
    this.init(this.moduleSetRelease);

    forkJoin([
      this.moduleService.getModuleSetList(),
      this.releaseService.getSimpleReleases(),
      this.moduleService.getModuleSetReleaseList()
    ]).subscribe(([moduleSetList, releaseList, moduleSetReleaseList]) => {
      // Sorting by ID desc
      this.moduleSetList.push(...moduleSetList.results.sort((a, b) => b.moduleSetId - a.moduleSetId));
      initFilter(this.moduleSetListFilterCtrl, this.filteredModuleSetList, this.moduleSetList, (e) => e.name);

      this.releaseList.push(...releaseList);
      initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releaseList, (e) => e.releaseNum);

      // Sorting by ID desc
      this.moduleSetReleaseList = moduleSetReleaseList.results.sort((a, b) => b.moduleSetReleaseId - a.moduleSetReleaseId);
      initFilter(this.moduleSetReleaseListFilterCtrl, this.filteredModuleSetReleaseList, this.moduleSetReleaseList,
        (e) => e.moduleSetReleaseName + ' ' + e.releaseNum);
    });
  }

  init(moduleSetRelease: ModuleSetRelease) {
    this.moduleSetRelease = moduleSetRelease;
    this.$hashCode = hashCode(this.moduleSetRelease);
  }

  createModuleSet() {
    if (!this.canCreate) {
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
        if (resp.length > 0) {
          const dialogConfig = this.confirmDialogService.newConfig();
          dialogConfig.data.header = 'Create default module set release?';
          dialogConfig.data.content = [
            'There is another default module set release, \'' + resp.results[0].moduleSetReleaseName + '\' for \'' +
            resp.results[0].releaseNum + '\' branch.',
            'Are you sure you want to create this module set release as a default?',
          ];
          dialogConfig.data.action = 'Create';

          this.confirmDialogService.open(dialogConfig).afterClosed()
            .subscribe(result => {
              if (result) {
                this.doCreateModuleSetRelease();
              }
            });
        } else {
          this.doCreateModuleSetRelease();
        }
      });
    } else {
      this.doCreateModuleSetRelease();
    }
  }

  doCreateModuleSetRelease() {
    const request = new ModuleSetReleaseListRequest();
    request.page = new PageRequest('lastUpdateTimestamp', 'desc', 0, 10);
    request.releaseId = this.moduleSetRelease.releaseId;
    request.filters.name = this.moduleSetRelease.moduleSetReleaseName;
    this.moduleService.getModuleSetReleaseList(request).subscribe(resp => {
      if (resp.length > 0 &&
        resp.results[0].moduleSetReleaseName === this.moduleSetRelease.moduleSetReleaseName) {

        const dialogConfig = this.confirmDialogService.newConfig();
        dialogConfig.data.header = 'Create module set release?';
        dialogConfig.data.content = [
          'There is another same module set release, \'' + resp.results[0].moduleSetReleaseName + '\' for \'' +
          resp.results[0].releaseNum + '\' branch.',
          'Are you sure you want to create this module set release?',
        ];
        dialogConfig.data.action = 'Create';

        this.confirmDialogService.open(dialogConfig).afterClosed()
          .subscribe(result => {
            if (result) {
              this._doCreateModuleSetRelease();
            }
          });
      } else {
        this._doCreateModuleSetRelease();
      }
    });
  }

  _doCreateModuleSetRelease() {
    this.isUpdating = true;

    let basedModuleSetReleaseId;
    if (this.copyFromOther && this.copyTargetModuleSetRelease) {
      basedModuleSetReleaseId = this.copyTargetModuleSetRelease.moduleSetReleaseId;
    }

    this.moduleService.createModuleSetRelease(this.moduleSetRelease, basedModuleSetReleaseId)
      .pipe(finalize(() => {
        this.isUpdating = false;
      }))
      .subscribe(moduleSet => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/module_management/module_set_release');
      });
  }

}
