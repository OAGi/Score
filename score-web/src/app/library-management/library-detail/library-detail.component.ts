import { Component, ElementRef, HostListener, ViewChild, inject } from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {FormControl, Validators} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {AuthService} from '../../authentication/auth.service';
import {switchMap} from 'rxjs/operators';
import {LibraryDetails, LibraryReleaseDependency, LibraryReleaseDependenciesResponse} from '../domain/library';
import {LibraryService} from '../domain/library.service';
import {hashCode} from 'src/app/common/utility';
import {Title} from '@angular/platform-browser';
import {setAppTitleIfPresent} from '../../common/app-title.strategy';
import {forkJoin, Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';

@Component({
  standalone: false,
  selector: 'score-library-detail',
  templateUrl: './library-detail.component.html',
  styleUrl: './library-detail.component.css'
})
export class LibraryDetailComponent {
  @ViewChild('dependencyInput') dependencyInput: ElementRef<HTMLInputElement>;

  private service = inject(LibraryService);
  private snackBar = inject(MatSnackBar);
  private confirmDialogService = inject(ConfirmDialogService);
  private location = inject(Location);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private auth = inject(AuthService);
  private titleService = inject(Title);


  title = 'Library Detail';
  loading = false;
  library: LibraryDetails;
  availableReleaseDependencies: LibraryReleaseDependency[] = [];
  persistedReleaseDependencies: LibraryReleaseDependency[] = [];
  removedDependencies: LibraryReleaseDependency[] = [];
  selectedDependencyReleaseIds: number[] = [];
  dependencyHash = '';
  dependencyCtrl = new FormControl({value: '', disabled: !this.isAdmin});
  filteredDependencyOptions: Observable<LibraryReleaseDependency[]> = this.dependencyCtrl.valueChanges.pipe(
      startWith(''),
      map(value => this.filterDependencyOptions(value)));

  uriForm = new FormControl('');
  hashCode;

  ngOnInit() {
    this.loading = true;
    this.library = new LibraryDetails();

    // load library
    this.route.paramMap.pipe(
        switchMap((params: ParamMap) => {
          const libraryId = Number(params.get('id'));
          return forkJoin({
            library: this.service.getLibraryById(libraryId),
            dependencies: this.service.getReleaseDependencies(libraryId)
          });
        })
    ).subscribe(resp => {
      if (!resp?.library) {
        this.redirectToLibraryList();
        return;
      }

      this.library = resp.library;
      this.resetDependencyState(resp.dependencies);
      setAppTitleIfPresent(this.titleService, this.library.name, 'Library');
      this.uriForm = new FormControl({value: this.library.link, disabled: !this.isAdmin},
          Validators.pattern('\\w+:(\\/?\\/?)[^\\s]+'));
      this.hashCode = hashCode(resp.library);
      this.loading = false;
    }, err => {
      this.loading = false;

      if (err.status === 404) {
        this.redirectToLibraryList();
        return;
      }

      this.snackBar.open('Something\'s wrong.', '', {
        duration: 3000,
      });
    });
  }

  private redirectToLibraryList() {
    this.loading = false;
    this.snackBar.open('The requested library is unavailable.', '', {
      duration: 3000,
    });
    this.router.navigateByUrl('/library');
  }

  isChanged() {
    if (this.uriForm) {
      this.library.link = this.uriForm.value;
    }
    return this.hashCode !== hashCode(this.library);
  }

  isDisabled() {
    return (!this.library.name) ||
        (!this.library.link || !this.uriForm.valid) ||
        (!this.library.organization) ||
        (!this.library.domain);
  }

  back() {
    this.location.back();
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

    // Handle 'Ctrl/Command+S'
    const metaOrCtrlKeyPressed = $event.metaKey || $event.ctrlKey;
    if (metaOrCtrlKeyPressed && charCode === 's') {
      $event.preventDefault();
      $event.stopPropagation();

      this.update();
    }
  }

  get updateDisabled(): boolean {
    if (!this.isAdmin) {
      return true;
    }
    return this.isDisabled() || (!this.isChanged() && !this.isDependencyChanged());
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  get selectedDependencies(): LibraryReleaseDependency[] {
    const dependencyMap = new Map<number, LibraryReleaseDependency>();
    this.availableReleaseDependencies.forEach(dependency => dependencyMap.set(dependency.releaseId, dependency));
    this.persistedReleaseDependencies.forEach(dependency => dependencyMap.set(dependency.releaseId, dependency));
    return this.selectedDependencyReleaseIds
        .map(releaseId => dependencyMap.get(releaseId))
        .filter((dependency): dependency is LibraryReleaseDependency => !!dependency);
  }

  update() {
    if (this.updateDisabled) {
      return;
    }

    if (this.removedDependencies.length > 0) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Update Library Including Dependencies?';
      dialogConfig.data.content = [
        'Removing release dependencies can break existing component relationships that rely on those releases.',
        'Please review the removed release dependencies before you continue.'
      ];
      dialogConfig.data.list = this.removedDependencies.map(dependency => this.releaseDependencyLabel(dependency));
      dialogConfig.data.action = 'Update';

      this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(result => {
        if (result) {
          this.performUpdate();
        }
      });
      return;
    }

    this.performUpdate();
  }

  private performUpdate() {
    if (this.updateDisabled) {
      return;
    }

    this.loading = true;
    this.library.link = this.uriForm.value;
    const requests: Observable<any>[] = [];
    const libraryChanged = this.isChanged();
    const dependencyChanged = this.isDependencyChanged();
    const submittedDependencyReleaseIds = [...this.selectedDependencyReleaseIds];

    if (libraryChanged) {
      requests.push(this.service.update(this.library));
    }
    if (dependencyChanged) {
      requests.push(this.service.updateReleaseDependencies(this.library.libraryId, this.selectedDependencyReleaseIds));
    }

    forkJoin(requests).subscribe(_ => {
      if (libraryChanged) {
        this.hashCode = hashCode(this.library);
        setAppTitleIfPresent(this.titleService, this.library.name, 'Library');
      }
      if (dependencyChanged) {
        this.service.getReleaseDependencies(this.library.libraryId).subscribe(dependencies => {
          this.resetDependencyState(dependencies, submittedDependencyReleaseIds);
          this.clearDependencyInput();
          this.snackBar.open('Updated', '', {
            duration: 3000,
          });
          this.loading = false;
        }, error => {
          this.openLibraryUpdateError(error);
          this.loading = false;
        });
        return;
      }
      this.dependencyHash = this.getDependencyHash();
      this.clearDependencyInput();
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
      this.loading = false;
    }, error => {
      this.openLibraryUpdateError(error);
      this.loading = false;
    });
  }

  releaseDependencyLabel(dependency: LibraryReleaseDependency): string {
    return `${dependency.libraryName} - ${dependency.releaseNum}`;
  }

  addDependency(event: MatAutocompleteSelectedEvent) {
    const dependency: LibraryReleaseDependency = event.option.value;
    if (!dependency || this.selectedDependencyReleaseIds.includes(dependency.releaseId)) {
      return;
    }

    const selectedLibraryIds = new Set(this.selectedDependencies.map(selectedDependency => selectedDependency.libraryId));
    if (selectedLibraryIds.has(dependency.libraryId)) {
      return;
    }

    this.selectedDependencyReleaseIds = [...this.selectedDependencyReleaseIds, dependency.releaseId];
    this.clearDependencyInput();
  }

  removeDependency(releaseId: number) {
    const dependency = this.selectedDependencies.find(selectedDependency => selectedDependency.releaseId === releaseId);
    this.selectedDependencyReleaseIds = this.selectedDependencyReleaseIds
        .filter(selectedReleaseId => selectedReleaseId !== releaseId);
    if (dependency?.releaseDepId != null &&
        !this.removedDependencies.some(removedDependency => removedDependency.releaseId === releaseId)) {
      this.removedDependencies = [...this.removedDependencies, dependency];
    }
    this.clearDependencyInput();
    this.refreshDependencyOptions();
  }

  recoverDependency(dependency: LibraryReleaseDependency) {
    if (!dependency || this.selectedDependencyReleaseIds.includes(dependency.releaseId)) {
      return;
    }

    const selectedLibraryIds = new Set(this.selectedDependencies.map(selectedDependency => selectedDependency.libraryId));
    if (selectedLibraryIds.has(dependency.libraryId)) {
      this.snackBar.open('Only one release dependency can be selected from each library.', '', {
        duration: 3000,
      });
      return;
    }

    this.selectedDependencyReleaseIds = [...this.selectedDependencyReleaseIds, dependency.releaseId];
    this.removedDependencies = this.removedDependencies
        .filter(removedDependency => removedDependency.releaseId !== dependency.releaseId);
    this.clearDependencyInput();
    this.refreshDependencyOptions();
  }

  private filterDependencyOptions(value: string | LibraryReleaseDependency): LibraryReleaseDependency[] {
    const filterValue = typeof value === 'string'
        ? this.normalizeDependencySearchValue(value)
        : '';
    const selectedLibraryIds = new Set(this.selectedDependencies.map(selectedDependency => selectedDependency.libraryId));
    const removedReleaseIds = new Set(this.removedDependencies.map(dependency => dependency.releaseId));
    return this.availableReleaseDependencies
        .filter(dependency => !this.selectedDependencyReleaseIds.includes(dependency.releaseId))
        .filter(dependency => !removedReleaseIds.has(dependency.releaseId))
        .filter(dependency => !selectedLibraryIds.has(dependency.libraryId))
        .filter(dependency => !filterValue || this.matchesDependencySearch(dependency, filterValue))
        .sort((a, b) => this.releaseDependencyLabel(a).localeCompare(this.releaseDependencyLabel(b)));
  }

  private matchesDependencySearch(dependency: LibraryReleaseDependency, filterValue: string): boolean {
    const normalizedLabel = this.normalizeDependencySearchValue(this.releaseDependencyLabel(dependency));
    return filterValue
        .split(' ')
        .every(token => normalizedLabel.includes(token));
  }

  private normalizeDependencySearchValue(value: string): string {
    return value
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, ' ')
        .trim()
        .replace(/\s+/g, ' ');
  }

  private clearDependencyInput() {
    if (this.dependencyInput) {
      this.dependencyInput.nativeElement.value = '';
    }
    this.dependencyCtrl.setValue('');
  }

  private openLibraryUpdateError(error: HttpErrorResponse): void {
    const errorMessage = typeof error?.error === 'string' ? error.error :
        error?.error?.message ||
        error?.headers?.get('x-error-message') ||
        'Something\'s wrong.';
    if (this.openDependencyUpdateErrorDialog(errorMessage)) {
      return;
    }
    this.snackBar.open(errorMessage, '', {
      duration: 5000,
    });
  }

  private openDependencyUpdateErrorDialog(errorMessage: string): boolean {
    const lines = (errorMessage || '')
        .split('\n')
        .map(line => line.trim())
        .filter(line => line.length > 0);
    if (lines.length <= 1) {
      return false;
    }

    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = lines[0];
    dialogConfig.data.content = lines.slice(1).filter(line => !line.startsWith('- '));
    dialogConfig.data.list = lines
        .slice(1)
        .filter(line => line.startsWith('- '))
        .map(line => line.substring(2));
    dialogConfig.data.action = undefined;
    this.confirmDialogService.open(dialogConfig);
    return true;
  }

  private refreshDependencyOptions() {
    this.dependencyCtrl.setValue(this.dependencyCtrl.value ?? '');
  }

  private resetDependencyState(
      response: LibraryReleaseDependenciesResponse,
      preferredOrder?: number[]) {
    this.persistedReleaseDependencies = this.sortCurrentDependencies(response.currentDependencies, preferredOrder);
    this.availableReleaseDependencies = response.availableDependencies;
    this.removedDependencies = [];
    this.selectedDependencyReleaseIds = this.persistedReleaseDependencies.map(dependency => dependency.releaseId);
    this.dependencyHash = this.getDependencyHash();
    this.refreshDependencyOptions();
  }

  private sortCurrentDependencies(
      currentDependencies: LibraryReleaseDependency[],
      preferredOrder?: number[]): LibraryReleaseDependency[] {
    const sortedDependencies = [...currentDependencies];
    if (preferredOrder && preferredOrder.length > 0) {
      const preferredOrderIndex = new Map(preferredOrder.map((releaseId, index) => [releaseId, index]));
      sortedDependencies.sort((a, b) => {
        const leftIndex = preferredOrderIndex.get(a.releaseId);
        const rightIndex = preferredOrderIndex.get(b.releaseId);
        if (leftIndex != null && rightIndex != null) {
          return leftIndex - rightIndex;
        }
        if (leftIndex != null) {
          return -1;
        }
        if (rightIndex != null) {
          return 1;
        }
        return (a.releaseDepId ?? Number.MAX_SAFE_INTEGER) - (b.releaseDepId ?? Number.MAX_SAFE_INTEGER);
      });
      return sortedDependencies;
    }

    return sortedDependencies
        .sort((a, b) => (a.releaseDepId ?? Number.MAX_SAFE_INTEGER) - (b.releaseDepId ?? Number.MAX_SAFE_INTEGER));
  }

  private getDependencyHash(): string {
    const removedReleaseIds = new Set(this.removedDependencies.map(dependency => dependency.releaseId));
    return this.selectedDependencyReleaseIds
        .filter(releaseId => !removedReleaseIds.has(releaseId))
        .sort((a, b) => a - b)
        .join(',');
  }

  private isDependencyChanged(): boolean {
    return this.dependencyHash !== this.getDependencyHash();
  }

  discard() {
    this.loading = true;
    this.service.checkDiscard(this.library.libraryId).subscribe(response => {
      if (!response.discardable) {
        this.snackBar.open(response.message, '', {
          duration: 5000,
        });
        this.loading = false;
        return;
      }

      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Discard Library?';
      dialogConfig.data.content = [
        'Are you sure you want to discard this library?',
        'The library and all related core components, data types, code lists, agency ID lists, and related records will be permanently removed.'
      ];
      dialogConfig.data.action = 'Discard';

      this.confirmDialogService.open(dialogConfig).afterClosed()
          .subscribe(result => {
            if (result) {
              this.service.discard(this.library.libraryId).subscribe(_ => {
                this.snackBar.open('Discarded', '', {
                  duration: 3000,
                });
                this.router.navigateByUrl('/library');
              }, error => {
                const errorMessage = typeof error?.error === 'string' ? error.error :
                    error?.error?.message || 'Something\'s wrong.';
                this.snackBar.open(errorMessage, '', {
                  duration: 5000,
                });
                this.loading = false;
              });
              return;
            }
            this.loading = false;
          }, error => {
            this.loading = false;
            throw error;
          });
    }, error => {
      const errorMessage = typeof error?.error === 'string' ? error.error :
          error?.error?.message || 'Something\'s wrong.';
      this.snackBar.open(errorMessage, '', {
        duration: 5000,
      });
      this.loading = false;
      throw error;
    });
  }

}
