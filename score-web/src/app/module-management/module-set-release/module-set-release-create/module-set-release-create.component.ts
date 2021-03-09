import {Component, OnInit} from '@angular/core';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {PageRequest} from '../../../basis/basis';
import {Release} from '../../../bie-management/bie-create/domain/bie-create-list';
import {ReleaseListRequest, SimpleRelease} from '../../../release-management/domain/release';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {ModuleSet, ModuleSetRelease} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDialog} from '@angular/material/dialog';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {finalize, switchMap} from 'rxjs/operators';
import {hashCode, initFilter} from '../../../common/utility';

@Component({
  selector: 'score-module-set-create',
  templateUrl: './module-set-release-create.component.html',
  styleUrls: ['./module-set-release-create.component.css']
})
export class ModuleSetReleaseCreateComponent implements OnInit {

  title = 'Create Module Set Release';
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
    })
  }

  init(moduleSetRelease: ModuleSetRelease) {
    this.moduleSetRelease = moduleSetRelease;
    this.$hashCode = hashCode(this.moduleSetRelease);
  }


  get canCreate(): boolean {
    return this.moduleSetRelease.releaseId !== undefined && this.moduleSetRelease.moduleSetId !== undefined;
  }

  createModuleSet() {
    if (!this.canCreate) {
      return;
    }

    this.isUpdating = true;

    this.moduleService.createModuleSetRelease(this.moduleSetRelease)
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
