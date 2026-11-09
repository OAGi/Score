import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {finalize} from 'rxjs/operators';
import {NamespaceSummary} from '../../../../namespace-management/domain/namespace';
import {NamespaceService} from '../../../../namespace-management/domain/namespace.service';
import {ModuleElement, ModuleSetSummary, Tile} from '../../../domain/module';
import {ModuleService} from '../../../domain/module.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject, Subscription} from 'rxjs';
import {initFilter} from '../../../../common/utility';

@Component({
  standalone: false,
  selector: 'score-module-add-dialog',
  templateUrl: './module-add-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./module-add-dialog.component.css']
})
export class ModuleAddDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<ModuleAddDialogComponent>>(MatDialogRef);
  private snackBar = inject(MatSnackBar);
  private namespaceService = inject(NamespaceService);
  private moduleService = inject(ModuleService);
  data = inject(MAT_DIALOG_DATA);


  parentDirName: string;
  moduleSetId: number;
  libraryId: number;
  parentModuleId: number;

  /* create new module */
  namespaceList: NamespaceSummary[];
  module: ModuleElement;

  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<NamespaceSummary[]> = new ReplaySubject<NamespaceSummary[]>(1);

  /* create new moduleDir */
  moduleDir: ModuleElement;

  /* copy from */
  moduleSetList: ModuleSetSummary[] = [];
  tiles: Tile[] = [];
  rootElement: ModuleElement;
  selected: ModuleElement;
  copySubModules = true;
  selectedModuleSetId: number;
  private loadSubscription: Subscription;

  disableAnimation = true;

  isUpdating = false;

  constructor() {
    const data = this.data;


    this.moduleSetId = data.moduleSetId;
    this.libraryId = data.libraryId;
    this.parentModuleId = data.parentModuleId;
    this.parentDirName = data.parentDirName;
  }

  ngOnInit() {
    this.module = new ModuleElement();
    this.moduleDir = new ModuleElement();
    this.namespaceService.getNamespaceSummaries(this.libraryId).subscribe(resp => {
      this.namespaceList = resp.filter(e => e.standard);
      initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList,
        this.namespaceList, (e) => e.uri);
    });

    this.moduleService.getModuleSetSummaries(this.libraryId).subscribe(resp => {
      this.moduleSetList = resp;
    });
  }

  onModuleSetChange(moduleSetId: number) {
    // Nothing of the previously chosen module set may stay selected while the new one loads or when it fails to load.
    this.loadSubscription?.unsubscribe();
    this.rootElement = undefined;
    this.tiles = [];
    this.selected = undefined;
    this.loadSubscription = this.moduleService.getModules(moduleSetId).subscribe({
      next: resp => {
        this.rootElement = resp as ModuleElement;
        this.tiles = [];
        this.tiles.push({elements: this.rootElement.children, current: undefined});
        if (this.rootElement.children && this.rootElement.children.length > 0) {
          this.onClickElement(this.tiles[0], this.tiles[0].elements[0]);
        }
      }
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.disableAnimation = false);
  }

  onCreateModule() {
    if (!this.module.name) {
      return;
    }
    this.module.parentModuleId = this.parentModuleId;
    this.module.moduleSetId = this.moduleSetId;
    this.module.directory = false;
    this.moduleService.createModule(this.module).subscribe(resp => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });

      this.moduleService.getModule(this.moduleSetId, resp.moduleId).subscribe(module => {
        const element = new ModuleElement();
        element.moduleId = module.moduleId;
        element.name = module.name;
        element.namespaceId = module.namespaceId;
        element.versionNum = module.versionNum;
        element.directory = false;
        this.dialogRef.close(element);
      });
    });
  }

  onCreateModuleDir() {
    if (!this.moduleDir.name) {
      return;
    }
    this.moduleDir.parentModuleId = this.parentModuleId;
    this.moduleDir.moduleSetId = this.moduleSetId;
    this.moduleDir.directory = true;
    this.moduleService.createModule(this.moduleDir).subscribe(resp => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });

      this.moduleService.getModule(this.moduleSetId, resp.moduleId).subscribe(module => {
        const element = new ModuleElement();
        element.moduleId = module.moduleId;
        element.name = module.name;
        element.namespaceId = module.namespaceId;
        element.versionNum = module.versionNum;
        element.directory = true;
        element.children = [];
        this.dialogRef.close(element);
      });
    });
  }

  onClickElement(tile: Tile, element: ModuleElement) {
    tile.current = element;
    this.selected = element;
    const tileIndex = this.tiles.indexOf(tile) + 1;
    if (this.tiles.length > tileIndex) {
      this.tiles.splice(tileIndex, this.tiles.length - tileIndex);
    }
    if (element.directory) {
      this.tiles.push({elements: element.children.sort(this._sort), current: undefined});
    }
  }

  _sort(e1: ModuleElement, e2: ModuleElement): number {
    // @ts-ignore
    return e2.directory - e1.directory ? e2.directory - e1.directory : e1.name > e2.name ? 1 : -1;
  }


  /**
   * A directory cannot be copied into itself or into one of its sub directories: the copy would be copied again
   * and again. It only applies when the copy source is the module set that is being edited, because the destination
   * is a module of that module set.
   */
  isCopyBlocked(element: ModuleElement): boolean {
    if (!element || this.selectedModuleSetId !== this.moduleSetId) {
      return false;
    }
    return element.moduleId === this.parentModuleId || this.containsModule(element, this.parentModuleId);
  }

  private containsModule(element: ModuleElement, moduleId: number): boolean {
    return (element.children || []).some(child => child.moduleId === moduleId || this.containsModule(child, moduleId));
  }

  onCopy() {
    if (!this.selected || this.isCopyBlocked(this.selected)) {
      return;
    }
    this.isUpdating = true;
    this.moduleService.copyModule(this.selected, this.moduleSetId, this.copySubModules, this.parentModuleId)
      .pipe(finalize(() => {
        this.isUpdating = false;
      }))
      .subscribe(resp => {
        this.snackBar.open('Copied', '', {
          duration: 3000,
        });
        this.dialogRef.close(true);
      });
  }

  cancel() {
    this.dialogRef.close();
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  canCreateModule(): boolean {
    return !!this.module.name;
  }

  canCreateModuleDir(): boolean {
    return !!this.moduleDir.name;
  }
}
