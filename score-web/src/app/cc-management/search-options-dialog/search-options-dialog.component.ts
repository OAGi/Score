import { Component, OnInit, inject } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {SearchOptions} from './domain/search-options';
import {SearchOptionsService} from './domain/search-options-service';

@Component({
  standalone: false,
  selector: 'score-search-options-dialog',
  templateUrl: './search-options-dialog.component.html',
  styleUrls: ['./search-options-dialog.component.css']
})
export class SearchOptionsDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<SearchOptionsDialogComponent>>(MatDialogRef);
  private service = inject(SearchOptionsService);
  data = inject(MAT_DIALOG_DATA);


  options: SearchOptions;

  ngOnInit(): void {
    this.options = this.service.loadOptions();
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onClick(): void {
    this.service.saveOptions(this.options);
    this.dialogRef.close();
  }
}
