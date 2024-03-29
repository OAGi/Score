import {Injectable} from '@angular/core';
import {MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material/dialog';
import {ConfirmDialogConfig} from './confirm-dialog.domain';
import {ConfirmDialogComponent} from './confirm-dialog.component';

@Injectable()
export class ConfirmDialogService {

  constructor(private dialog: MatDialog) {
  }

  newConfig(): MatDialogConfig {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    return dialogConfig;
  }

  open(dialogConfig: MatDialogConfig): MatDialogRef<ConfirmDialogComponent, any> {
    return this.dialog.open(ConfirmDialogComponent, dialogConfig);
  }
}
