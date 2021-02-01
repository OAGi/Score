import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {SearchOptions} from './domain/search-options';
import {SearchOptionsService} from './domain/search-options-service';

@Component({
  selector: 'score-search-options-dialog',
  templateUrl: './search-options-dialog.component.html',
  styleUrls: ['./search-options-dialog.component.css']
})
export class SearchOptionsDialogComponent implements OnInit {

  options: SearchOptions;

  constructor(public dialogRef: MatDialogRef<SearchOptionsDialogComponent>,
              private service: SearchOptionsService,
              @Inject(MAT_DIALOG_DATA) public data: any) {
  }

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
