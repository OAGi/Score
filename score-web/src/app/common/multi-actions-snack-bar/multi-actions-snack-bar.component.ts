import { Component, inject } from '@angular/core';
import {Clipboard} from '@angular/cdk/clipboard';
import {MAT_SNACK_BAR_DATA, MatSnackBarRef} from '@angular/material/snack-bar';

@Component({
  standalone: false,
  selector: 'score-multi-actions-snack-bar',
  templateUrl: './multi-actions-snack-bar.component.html',
  styleUrls: ['./multi-actions-snack-bar.component.css']
})
export class MultiActionsSnackBarComponent {
  private clipboard = inject(Clipboard);
  data = inject(MAT_SNACK_BAR_DATA);


  snackBarRef = inject(MatSnackBarRef);

}
