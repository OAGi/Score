import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TagService} from '../domain/tag.service';
import {Tag} from '../domain/tag';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'score-edit-tags-dialog',
  templateUrl: './edit-tags-dialog.component.html',
  styleUrls: ['./edit-tags-dialog.component.css']
})
export class EditTagsDialogComponent implements OnInit {

  loading = false;

  tagList: Tag[] = [];
  newTag: Tag = new Tag();

  constructor(public dialogRef: MatDialogRef<EditTagsDialogComponent>,
              private tagService: TagService,
              private confirmDialogService: ConfirmDialogService,
              private snackBar: MatSnackBar,
              @Inject(MAT_DIALOG_DATA) public data: any) {

  }

  ngOnInit(): void {
    this.loading = true;
    this.tagService.getTags().subscribe(tagList => {
      this.tagList = tagList;

      this.loading = false;
    });
  }

  add(tag: Tag) {
    this.loading = true;
    this.tagService.add(tag).subscribe(_ => {
      this.newTag = new Tag();
      this.snackBar.open('Added', '', {
        duration: 3000,
      });
      this.tagService.getTags().subscribe(tagList => {
        this.tagList = tagList;
        this.loading = false;
      });
    });
  }

  update(tag: Tag) {
    this.loading = true;
    this.tagService.update(tag).subscribe(_ => {
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
      this.tagService.getTags().subscribe(tagList => {
        this.tagList = tagList;
        this.loading = false;
      });
    });
  }

  discard(tag: Tag) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard?';
    dialogConfig.data.content = ['Deleting a tag will remove it from all components across releases.',
      'Are you sure you want to discard this tag?'];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).beforeClosed()
      .subscribe(result => {
        if (result) {
          this.tagService.discard(tag).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.tagService.getTags().subscribe(tagList => {
              this.tagList = tagList;
              this.loading = false;
            });
          });
        }
      });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
