import {Component, OnInit} from '@angular/core';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
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
  selector: 'score-module-set-detail',
  templateUrl: './module-set-release-detail.component.html',
  styleUrls: ['./module-set-release-detail.component.css']
})
export class ModuleSetReleaseDetailComponent implements OnInit {

  title = 'Edit Module Set Release';
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
        })

      });
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


  get isChanged(): boolean {
    return this.$hashCode !== hashCode(this.moduleSetRelease);
  }

  createModuleSet() {
    if (!this.isChanged) {
      return;
    }

    this.isUpdating = true;

    this.moduleService.updateModuleSetRelease(this.moduleSetRelease)
      .pipe(finalize(() => {
        this.isUpdating = false;
      }))
      .subscribe(moduleSet => {
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      });
  }

}
