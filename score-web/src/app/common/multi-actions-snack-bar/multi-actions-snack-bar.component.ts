import {Component, Inject, inject} from '@angular/core';
import {Clipboard} from '@angular/cdk/clipboard';
import {MAT_SNACK_BAR_DATA, MatSnackBarRef} from '@angular/material/snack-bar';

@Component({
  selector: 'score-multi-actions-snack-bar',
  templateUrl: './multi-actions-snack-bar.component.html',
  styleUrls: ['./multi-actions-snack-bar.component.css']
})
export class MultiActionsSnackBarComponent {

  snackBarRef = inject(MatSnackBarRef);

  constructor(
    private clipboard: Clipboard,
    @Inject(MAT_SNACK_BAR_DATA) public data: any) {

  }

}
