import {Location} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormControl} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {forkJoin, ReplaySubject} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {hashCode, initFilter, loadLibrary, saveLibrary} from '../../../common/utility';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {ModuleSet, ModuleSetListRequest, ModuleSetRelease, ModuleSetReleaseListRequest} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';
import {PageRequest} from '../../../basis/basis';
import {SimpleRelease} from '../../../release-management/domain/release';
import {Library} from '../../../library-management/domain/library';
import {LibraryService} from '../../../library-management/domain/library.service';

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
  filteredReleaseList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);
  filteredModuleSetReleaseList: ReplaySubject<ModuleSetRelease[]> = new ReplaySubject<ModuleSetRelease[]>(1);
  moduleSetList: ModuleSet[] = [];
  releaseList: SimpleRelease[] = [];
  libraries: Library[] = [];
  mappedLibraries: { library: Library, selected: boolean }[] = [];
  moduleSetReleaseList: ModuleSetRelease[] = [];
  copyTargetModuleSetRelease: ModuleSetRelease;

  request = new ModuleSetReleaseListRequest();

  private $hashCode: string;

  constructor(private moduleService: ModuleService,
              private releaseService: ReleaseService,
              private libraryService: LibraryService,
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

    this.request.page.pageIndex = -1;
    this.request.page.pageSize = -1;

    this.libraryService.getLibraries().subscribe(libraries => {
      this.initLibraries(libraries);

      this.loadModuleSetReleaseListAndReleaseList();
    });
  }

  loadModuleSetReleaseListAndReleaseList() {
    const moduleSetListRequest = new ModuleSetListRequest();
    moduleSetListRequest.library = this.request.library;
    moduleSetListRequest.page.pageIndex = -1;
    moduleSetListRequest.page.pageSize = -1;

    forkJoin([
      this.moduleService.getModuleSetList(moduleSetListRequest),
      this.releaseService.getSimpleReleases(this.request.library.libraryId),
      this.moduleService.getModuleSetReleaseList(this.request)
    ]).subscribe(([moduleSetList, releaseList, moduleSetReleaseList]) => {
      // Sorting by ID desc
      this.moduleSetList = moduleSetList.results.sort((a, b) => b.moduleSetId - a.moduleSetId);
      initFilter(this.moduleSetListFilterCtrl, this.filteredModuleSetList, this.moduleSetList, (e) => e.name);

      this.initReleases(releaseList);

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

  initLibraries(libraries: Library[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.request.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
        saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      }
      if (!this.request.library || this.request.library.libraryId === 0) {
        this.request.library = this.libraries[0];
      }
      this.mappedLibraries = this.libraries.map(e => {
        return {library: e, selected: (this.request.library.libraryId === e.libraryId)};
      });
    }
  }

  initReleases(releases: SimpleRelease[]) {
    this.releaseList = releases;
    this.moduleSetRelease.moduleSetId = undefined;
    this.moduleSetRelease.releaseId = undefined;
    this.copyTargetModuleSetRelease = undefined;
    initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releaseList, (e) => e.releaseNum);
  }

  onLibraryChange(library: Library) {
    this.request.library = library;
    saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
    this.loadModuleSetReleaseListAndReleaseList();
  }

  createModuleSetRelease() {
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
      request.library = this.request.library;
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
    request.library.libraryId = this.request.library.libraryId;
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
