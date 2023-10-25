import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {finalize} from 'rxjs/operators';
import {ConfirmDialogService} from '../../../../common/confirm-dialog/confirm-dialog.service';
import {initFilter, sha256} from '../../../../common/utility';
import {SimpleNamespace} from '../../../../namespace-management/domain/namespace';
import {NamespaceService} from '../../../../namespace-management/domain/namespace.service';
import {ModuleElement} from '../../../domain/module';
import {ModuleService} from '../../../domain/module.service';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';

@Component({
  selector: 'score-module-edit-dialog',
  templateUrl: './module-edit-dialog.component.html',
  styleUrls: ['./module-edit-dialog.component.css']
})
export class ModuleEditDialogComponent implements OnInit {

  $hashCode: string;

  namespaceList: SimpleNamespace[];

  namespaceListFilterCtrl: FormControl = new FormControl();
  filteredNamespaceList: ReplaySubject<SimpleNamespace[]> = new ReplaySubject<SimpleNamespace[]>(1);

  title: string;

  isUpdating = false;

  constructor(public dialogRef: MatDialogRef<ModuleEditDialogComponent>,
              private service: ModuleService,
              private snackBar: MatSnackBar,
              private namespaceService: NamespaceService,
              private confirmDialogService: ConfirmDialogService,
              @Inject(MAT_DIALOG_DATA) public element: ModuleElement, ) {
  }

  ngOnInit() {
    this.title = this.element.name.toString();
    this.$hashCode = sha256(JSON.stringify({
      name: this.element.name,
      versionNum: this.element.versionNum,
      namespaceId: (!!this.element.namespaceId) ? this.element.namespaceId : undefined
    }));

    this.namespaceService.getSimpleNamespaces().subscribe(resp => {
      this.namespaceList = resp.filter(e => e.standard);
      initFilter(this.namespaceListFilterCtrl, this.filteredNamespaceList,
        this.namespaceList, (e) => e.uri);
    });
  }

  onUpdateElement() {
    this.isUpdating = true;
    this.service.updateModule(this.element).pipe(finalize(() => {
      this.isUpdating = false;
    })).subscribe(resp => {
      this.dialogRef.close('Updated');
    });
  }

  canUpdate(): boolean {
    if (!this.changed()) {
      return false;
    }
    if (this.element.name.length === 0) {
      return false;
    }
    return true;
  }

  cancel() {
    this.dialogRef.close('Canceled');
  }

  changed() {
    return this.$hashCode !== sha256(JSON.stringify({
      name: this.element.name,
      versionNum: this.element.versionNum,
      namespaceId: (!!this.element.namespaceId) ? this.element.namespaceId : undefined,
    }));
  }

  deleteModule() {
    if (this.element.directory) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Discard directory';
      dialogConfig.data.content = ['Are you sure you want to discard this and sub modules?'];
      dialogConfig.data.action = 'Discard anyway';

      this.confirmDialogService.open(dialogConfig).afterClosed()
        .subscribe(result => {
          if (result) {
            this.isUpdating = true;
            this.service.deleteModule(this.element)
              .pipe(finalize(() => {
                this.isUpdating = false;
              }))
              .subscribe(_ => {
                this.dialogRef.close('Discarded');
              });
          }
        });
    } else {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Discard file';
      dialogConfig.data.content = ['The CC assigned to this file will also be deleted.'];
      dialogConfig.data.action = 'Discard anyway';

      this.confirmDialogService.open(dialogConfig).afterClosed()
        .subscribe(result => {
          if (result) {
            this.isUpdating = true;
            this.service.deleteModule(this.element)
              .pipe(finalize(() => {
                this.isUpdating = false;
              }))
              .subscribe(_ => {
                this.dialogRef.close('Discarded');
              });
          }
        });
    }
  }
}
