import { Component, OnInit, inject } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {AuthService} from '../../authentication/auth.service';

@Component({
  standalone: false,
  selector: 'score-bie-deprecate-dialog',
  templateUrl: './bie-deprecate-dialog.component.html',
  styleUrls: ['./bie-deprecate-dialog.component.css']
})
export class BieDeprecateDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<BieDeprecateDialogComponent>>(MatDialogRef);
  private auth = inject(AuthService);
  data = inject(MAT_DIALOG_DATA);


  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
  }

}
