import {Location} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {AuthService} from '../../../authentication/auth.service';
import {Release} from '../../../bie-management/bie-create/domain/bie-create-list';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {hashCode, initFilter} from '../../../common/utility';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {ModuleSet, ModuleSetRelease} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';
import {FormControl} from "@angular/forms";
import {forkJoin, ReplaySubject} from "rxjs";

@Component({
  selector: 'score-module-set-create',
  templateUrl: './module-set-create.component.html',
  styleUrls: ['./module-set-create.component.css']
})
export class ModuleSetCreateComponent implements OnInit {

  title = 'Create Module Set';
  isUpdating: boolean;
  moduleSet: ModuleSet = new ModuleSet();
  moduleSetReleaseList: ModuleSetRelease[] = [];
  releaseList: Release[] = [];

  moduleSetReleaseListFilterCtrl: FormControl = new FormControl();
  releaseListFilterCtrl: FormControl = new FormControl();
  filteredModuleSetReleaseList: ReplaySubject<ModuleSetRelease[]> = new ReplaySubject<ModuleSetRelease[]>(1);
  filteredReleaseList: ReplaySubject<Release[]> = new ReplaySubject<Release[]>(1);

  private $hashCode: string;

  constructor(private service: ModuleService,
              private location: Location,
              private releaseService: ReleaseService,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService) {
  }

  get canCreate(): boolean {
    if (!this.moduleSet) {
      return false;
    }
    if (this.moduleSet.createModuleSetRelease) {
      if (!this.moduleSet.targetReleaseId) {
        return false;
      }
      if (!this.moduleSet.targetModuleSetReleaseId) {
        return false;
      }
    }

    return this.moduleSet.name && this.moduleSet.name.length > 0;
  }

  ngOnInit(): void {
    this.moduleSetReleaseList = [];
    this.releaseList = [];
    this.init(this.moduleSet);

    forkJoin([
      this.service.getModuleSetReleaseList(),
      this.releaseService.getSimpleReleases(),
    ]).subscribe(([moduleSetReleaseList, releaseList]) => {
      // Sorting by ID desc
      this.moduleSetReleaseList = moduleSetReleaseList.results.sort((a, b) => b.moduleSetReleaseId - a.moduleSetReleaseId);
      initFilter(this.moduleSetReleaseListFilterCtrl, this.filteredModuleSetReleaseList, this.moduleSetReleaseList,
        (e) => e.moduleSetReleaseName + ' ' + e.releaseNum);

      this.releaseList.push(...releaseList);
      initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releaseList, (e) => e.releaseNum);
    });
  }

  init(moduleSet: ModuleSet) {
    this.moduleSet = moduleSet;
    this.$hashCode = hashCode(this.moduleSet);
  }

  createModuleSet() {
    if (!this.canCreate) {
      return;
    }

    this.isUpdating = true;

    this.service.createModuleSet(this.moduleSet)
      .pipe(finalize(() => {
        this.isUpdating = false;
      }))
      .subscribe(moduleSet => {
        this.snackBar.open('Created', '', {
          duration: 3000,
        });
        this.router.navigateByUrl('/module_management/module_set');
      });
  }

}
