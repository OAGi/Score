import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {interval, mergeMap, Subject, takeUntil} from "rxjs";
import {finalize, switchMap} from "rxjs/operators";
import {ModuleService} from "../../../domain/module.service";
import {ModuleSetReleaseValidateResponse} from "../../../domain/module";

@Component({
  selector: 'score-module-set-release-validation-dialog',
  templateUrl: './module-set-release-validation-dialog.component.html',
  styleUrls: ['./module-set-release-validation-dialog.component.css']
})
export class ModuleSetReleaseValidationDialogComponent implements OnInit {

  determinate: boolean;
  resp: ModuleSetReleaseValidateResponse;

  constructor(
    public dialogRef: MatDialogRef<ModuleSetReleaseValidationDialogComponent>,
    public moduleService: ModuleService,
    @Inject(MAT_DIALOG_DATA) public data: any) {

    this.determinate = false;
    this.resp = new ModuleSetReleaseValidateResponse();
    this.resp.progress = 0;
    this.resp.length = 0;
  }

  ngOnInit(): void {
    this.moduleService.validate(this.data.moduleSetReleaseId).subscribe(resp => {
      const closeTimer$ = new Subject<any>();
      interval(2000).pipe(
        mergeMap(() => this.moduleService.progressValidation(this.data.moduleSetReleaseId, resp.requestId)),
        takeUntil(closeTimer$),
      ).subscribe({
        next: (res) => {
          this.resp = res;
          this.determinate = true;
          if (res.done) {
            closeTimer$.next('Done');
          }
        },
        error: (res) => {
        }
      });
    });
  }

  get progress(): number {
    if (!this.resp || this.resp.progress === 0 || this.resp.length === 0) {
      return 0;
    }
    return Math.round((this.resp.progress / this.resp.length) * 100);
  }

  get keys(): string[] {
    if (!this.resp.results) {
      return [];
    }

    return Object.keys(this.resp.results);
  }

  get invalidKeys(): string[] {
    return this.keys.filter(e => this.value(e) !== 'Valid');
  }

  get validKeys(): string[] {
    return this.keys.filter(e => this.value(e) === 'Valid');
  }

  value(key: string): string {
    return this.resp.results[key];
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
