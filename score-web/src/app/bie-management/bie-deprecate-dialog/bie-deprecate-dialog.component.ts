import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {AuthService} from '../../authentication/auth.service';

@Component({
  selector: 'score-bie-deprecate-dialog',
  templateUrl: './bie-deprecate-dialog.component.html',
  styleUrls: ['./bie-deprecate-dialog.component.css']
})
export class BieDeprecateDialogComponent implements OnInit {

  constructor(public dialogRef: MatDialogRef<BieDeprecateDialogComponent>,
              private auth: AuthService,
              @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  ngOnInit() {
  }

}
