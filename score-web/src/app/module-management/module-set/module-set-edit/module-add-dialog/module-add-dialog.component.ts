import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {finalize} from 'rxjs/operators';
import {SimpleNamespace} from '../../../../namespace-management/domain/namespace';
import {NamespaceService} from '../../../../namespace-management/domain/namespace.service';
import {Module, ModuleElement, ModuleSet, Tile} from '../../../domain/module';
import {ModuleService} from '../../../domain/module.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {initFilter} from '../../../../common/utility';

@Component({
  selector: 'score-module-add-dialog',
  templateUrl: './module-add-dialog.component.html',
  styleUrls: ['./module-add-dialog.component.css']
})
export class ModuleAddDialogComponent implements OnInit {

  parentDirName: string;
  moduleSetId: number;
  parentModuleId: number;

  /* create new module */
  namespaceList: SimpleNamespace[];
  module: ModuleElement;

  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<SimpleNamespace[]> = new ReplaySubject<SimpleNamespace[]>(1);

  /* create new moduleDir */
  moduleDir: ModuleElement;

  /* copy from */
  moduleSetList: ModuleSet[] = [];
  tiles: Tile[] = [];
  rootElement: ModuleElement;
  selected: ModuleElement;
  copySubModules = true;
  selectedModuleSetId: number;

  disableAnimation = true;

  isUpdating = false;

  constructor(public dialogRef: MatDialogRef<ModuleAddDialogComponent>,
              private snackBar: MatSnackBar,
              private namespaceService: NamespaceService,
              private moduleService: ModuleService,
              @Inject(MAT_DIALOG_DATA) public data: any) {

    this.moduleSetId = data.moduleSetId;
    this.parentModuleId = data.parentModuleId;
    this.parentDirName = data.parentDirName;
  }

  ngOnInit() {
    this.module = new ModuleElement();
    this.moduleDir = new ModuleElement();
    this.namespaceService.getSimpleNamespaces().subscribe(resp => {
      this.namespaceList = resp.filter(e => e.standard);
      initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList,
        this.namespaceList, (e) => e.uri);
    });

    this.moduleService.getModuleSetList().subscribe(resp => {
      this.moduleSetList = resp.results;
    });
  }

  onModuleSetChange(moduleSetId: number) {
    this.moduleService.getModules(moduleSetId).subscribe(resp => {
      this.rootElement = resp as ModuleElement;
      this.tiles = [];
      this.tiles.push({elements: this.rootElement.child, current: undefined});
      if (this.rootElement.child && this.rootElement.child.length > 0) {
        this.onClickElement(this.tiles[0], this.tiles[0].elements[0]);
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
      const element = new ModuleElement();
      element.moduleId = (resp.module as Module).moduleId;
      element.name = (resp.module as Module).name;
      element.namespaceId = (resp.module as Module).namespaceId;
      element.versionNum = (resp.module as Module).versionNum;
      element.directory = false;
      this.dialogRef.close(element);
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
      const element = new ModuleElement();
      element.moduleId = (resp.module as Module).moduleId;
      element.name = (resp.module as Module).name;
      element.path = (resp.module as Module).path;
      element.directory = true;
      element.child = [];
      this.dialogRef.close(element);
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
      this.tiles.push({elements: element.child.sort(this._sort), current: undefined});
    }
  }

  _sort(e1: ModuleElement, e2: ModuleElement): number {
    // @ts-ignore
    return e2.directory - e1.directory ? e2.directory - e1.directory : e1.name > e2.name ? 1 : -1;
  }


  onCopy() {
    if (!this.selected) {
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
