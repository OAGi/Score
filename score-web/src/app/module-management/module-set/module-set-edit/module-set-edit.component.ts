import {Location} from '@angular/common';
import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatSort} from '@angular/material/sort';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {finalize, switchMap} from 'rxjs/operators';
import {AuthService} from '../../../authentication/auth.service';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {hashCode} from '../../../common/utility';
import {ModuleElement, ModuleSet, Tile} from '../../domain/module';
import {ModuleService} from '../../domain/module.service';
import {ModuleAddDialogComponent} from './module-add-dialog/module-add-dialog.component';
import {ModuleEditDialogComponent} from './module-edit-dialog/module-edit-dialog.component';
import {UserToken} from '../../../authentication/domain/auth';

@Component({
  selector: 'score-module-set-edit',
  templateUrl: './module-set-edit.component.html',
  styleUrls: ['./module-set-edit.component.css']
})
export class ModuleSetEditComponent implements OnInit {

  title;
  isUpdating: boolean;
  moduleSet: ModuleSet = new ModuleSet();
  tiles: Tile[] = [];
  rootElement: ModuleElement;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  private $hashCode: string;

  constructor(private service: ModuleService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private auth: AuthService,
              private confirmDialogService: ConfirmDialogService) {
    this.title = (this.role === 'developer') ? 'Edit Module Set' : 'View Module Set';
  }

  get canUpdate(): boolean {
    if (this.moduleSet && !this.moduleSet.name) {
      return false;
    }
    return hashCode(this.moduleSet) !== this.$hashCode;
  }

  ngOnInit(): void {
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => {
        const moduleSetId = Number(params.get('moduleSetId'));
        return this.service.getModuleSet(moduleSetId);
      }))
      .subscribe(moduleSet => {
        this.init(moduleSet);
        this.service.getModules(this.moduleSet.moduleSetId).subscribe(resp => {
          this.rootElement = resp as ModuleElement;
          this.tiles.push({elements: this.rootElement.child, current: undefined});
          if (this.rootElement.child && this.rootElement.child.length > 0) {
            this.onClickElement(this.tiles[0], this.tiles[0].elements[0]);
          }
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

  init(moduleSet: ModuleSet) {
    this.moduleSet = moduleSet;
    this.$hashCode = hashCode(this.moduleSet);
  }

  updateModuleSet() {
    if (!this.canUpdate) {
      return;
    }

    this.isUpdating = true;

    this.service.updateModuleSet(this.moduleSet)
      .pipe(finalize(() => {
        this.isUpdating = false;
      }))
      .subscribe(_ => {
        this.init(this.moduleSet);
        this.snackBar.open('Updated', '', {
          duration: 3000,
        });
      });
  }

  onClickElement(tile: Tile, element: ModuleElement) {
    tile.current = element;
    let tileIndex = this.tiles.indexOf(tile) + 1;
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

  openAddDialog(tile: Tile) {
    const tileIndex = this.tiles.indexOf(tile);
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth + 'px';

    dialogConfig.data = {
      moduleSetId: this.moduleSet.moduleSetId,
      parentModuleId: tileIndex > 0 ? this.tiles[tileIndex - 1].current.moduleId : this.rootElement.moduleId,
      parentDirName: tileIndex > 0 ? this.tiles[tileIndex - 1].current.name : this.rootElement.name
    };
    const dialogRef = this.dialog.open(ModuleAddDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(resp => {
      if (resp) {
        if (resp === true) {
          this.service.getModules(this.moduleSet.moduleSetId).subscribe(modules => {
            this.rootElement = modules as ModuleElement;
            this.tiles = [];
            this.tiles.push({elements: this.rootElement.child, current: undefined});
          });
        } else {
          tile.elements.push(resp);
          this.onClickElement(tile, resp);
        }
      }
    });

  }

  openEditDialog(element: ModuleElement, tile: Tile) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = window.innerWidth / 2 + 'px';
    element.moduleSetId = this.moduleSet.moduleSetId;
    dialogConfig.data = element;
    const buf = {
      'name': element.name,
      'versionNum': element.versionNum,
      'namespaceUri': element.namespaceUri,
      'namespaceId': element.namespaceId,
    };
    const dialogRef = this.dialog.open(ModuleEditDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(action => {
      switch (action) {
        case 'Discarded':
        case 'Unassigned':
          const tileIndex = this.tiles.indexOf(tile);
          if (tileIndex > 0) {
            const child = this.tiles[tileIndex - 1].current.child;
            const elementIndex = child.indexOf(element);
            child.splice(elementIndex, 1);
            this.onClickElement(this.tiles[tileIndex - 1], this.tiles[tileIndex - 1].current);
          } else {
            const child = this.rootElement.child;
            const elementIndex = child.indexOf(element);
            child.splice(elementIndex, 1);
            this.tiles[0].current = undefined;
            if (element.directory) {
              this.tiles = [{elements: this.rootElement.child, current: undefined}];
            }
          }

          this.snackBar.open(action, '', {
            duration: 3000,
          });
          break;
        case 'Updated':
          this.snackBar.open(action, '', {
            duration: 3000,
          });
          break;
        default:
          element.name = buf['name'];
          element.versionNum = buf['versionNum'];
          element.namespaceUri = buf['namespaceUri'];
          element.namespaceId = buf['namespaceId'];
          break;
      }
    });
  }
}
