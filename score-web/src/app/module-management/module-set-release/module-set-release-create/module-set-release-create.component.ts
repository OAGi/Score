import {Location} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormControl} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {ReplaySubject} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {AuthService} from '../../../authentication/auth.service';
import {Release} from '../../../bie-management/bie-create/domain/bie-create-list';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {hashCode} from '../../../common/utility';
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
  filteredModuleSetList: ReplaySubject<ModuleSet[]> = new ReplaySubject<ModuleSet[]>(1);
  filteredReleaseList: ReplaySubject<Release[]> = new ReplaySubject<Release[]>(1);
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
    this.moduleService.getModuleSetList().subscribe(resp => {
      this.moduleSetList.push(...resp.results);
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
    });

    this.releaseService.getSimpleReleases().subscribe(list => {
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
    });

    this.moduleService.getModuleSetReleaseList().subscribe(resp => {
      this.moduleSetReleaseList = resp.results;
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
