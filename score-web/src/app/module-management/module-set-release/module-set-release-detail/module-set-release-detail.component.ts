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
import {ModuleSet, ModuleSetRelease} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';
import {UserToken} from '../../../authentication/domain/auth';

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
    this.title = (this.role === 'developer') ? 'Edit Module Set Release' : 'View Module Set Release';
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

  get role(): string {
    const userToken = this.userToken;
    return (userToken) ? userToken.role : undefined;
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

    this.isUpdating = true;

    this.moduleService.updateModuleSetRelease(this.moduleSetRelease)
      .pipe(finalize(() => {
        this.isUpdating = false;
      }))
      .subscribe(moduleSet => {
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
        this.$hashCode = hashCode(this.moduleSetRelease);
      });
  }

  assignCCs() {
    return this.router.navigateByUrl('/module_management/module_set_release/' + this.moduleSetRelease.moduleSetReleaseId + '/assign');
  }
}
