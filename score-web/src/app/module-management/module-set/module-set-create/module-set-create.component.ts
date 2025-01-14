import {Location} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {hashCode, initFilter, loadLibrary, saveLibrary} from '../../../common/utility';
import {ReleaseService} from '../../../release-management/domain/release.service';
import {ModuleSet, ModuleSetRelease, ModuleSetReleaseListRequest} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';
import {FormControl} from '@angular/forms';
import {forkJoin, ReplaySubject} from 'rxjs';
import {ReleaseListRequest, SimpleRelease} from '../../../release-management/domain/release';
import {Library} from '../../../library-management/domain/library';
import {LibraryService} from '../../../library-management/domain/library.service';

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
  releaseList: SimpleRelease[] = [];
  libraries: Library[] = [];
  mappedLibraries: { library: Library, selected: boolean }[] = [];

  request: ModuleSetReleaseListRequest = new ModuleSetReleaseListRequest();
  moduleSetReleaseListFilterCtrl: FormControl = new FormControl();
  releaseListFilterCtrl: FormControl = new FormControl();
  filteredModuleSetReleaseList: ReplaySubject<ModuleSetRelease[]> = new ReplaySubject<ModuleSetRelease[]>(1);
  filteredReleaseList: ReplaySubject<SimpleRelease[]> = new ReplaySubject<SimpleRelease[]>(1);

  private $hashCode: string;

  constructor(private service: ModuleService,
              private location: Location,
              private releaseService: ReleaseService,
              private libraryService: LibraryService,
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

    this.request.page.pageIndex = -1;
    this.request.page.pageSize = -1;

    this.libraryService.getLibraries().subscribe(libraries => {
      this.initLibraries(libraries);

      this.loadModuleSetReleaseListAndReleaseList();
    });
  }

  loadModuleSetReleaseListAndReleaseList(): void {
    forkJoin([
      this.service.getModuleSetReleaseList(this.request),
      this.releaseService.getSimpleReleases(this.request.library.libraryId),
    ]).subscribe(([moduleSetReleaseList, releaseList]) => {
      // Sorting by ID desc
      this.moduleSetReleaseList = moduleSetReleaseList.results.sort((a, b) => b.moduleSetReleaseId - a.moduleSetReleaseId);
      initFilter(this.moduleSetReleaseListFilterCtrl, this.filteredModuleSetReleaseList, this.moduleSetReleaseList,
        (e) => e.moduleSetReleaseName + ' ' + e.releaseNum);

      this.initReleases(releaseList);
    });
  }

  init(moduleSet: ModuleSet) {
    this.moduleSet = moduleSet;
    this.$hashCode = hashCode(this.moduleSet);
  }

  initLibraries(libraries: Library[]) {
    this.libraries = libraries;
    if (this.libraries.length > 0) {
      const savedLibraryId = loadLibrary(this.auth.getUserToken());
      if (savedLibraryId) {
        this.request.library = this.libraries.filter(e => e.libraryId === savedLibraryId)[0];
      }
      if (!this.request.library || !this.request.library.libraryId) {
        this.request.library = this.libraries[0];
      }
      if (this.request.library) {
        saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
      }
      this.mappedLibraries = this.libraries.map(e => {
        return {library: e, selected: (this.request.library.libraryId === e.libraryId)};
      });
    }
  }

  initReleases(releases: SimpleRelease[]) {
    this.releaseList = releases;
    this.moduleSet.targetModuleSetReleaseId = undefined;
    initFilter(this.releaseListFilterCtrl, this.filteredReleaseList, this.releaseList, (e) => e.releaseNum);
  }

  onLibraryChange(library: Library) {
    this.request.library = library;
    saveLibrary(this.auth.getUserToken(), this.request.library.libraryId);
    this.loadModuleSetReleaseListAndReleaseList();
  }

  createModuleSet() {
    if (!this.canCreate) {
      return;
    }

    this.moduleSet.libraryId = this.request.library.libraryId;
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
