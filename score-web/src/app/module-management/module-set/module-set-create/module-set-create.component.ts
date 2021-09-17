import {Location} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {AuthService} from '../../../authentication/auth.service';
import {Release} from '../../../bie-management/bie-create/domain/bie-create-list';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {hashCode} from '../../../common/utility';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {ModuleSet, ModuleSetRelease} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';

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

    this.service.getModuleSetReleaseList().subscribe(resp => {
      this.moduleSetReleaseList = resp.results;
    });

    this.releaseService.getSimpleReleases().subscribe(list => {
      this.releaseList.push(...list);
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
