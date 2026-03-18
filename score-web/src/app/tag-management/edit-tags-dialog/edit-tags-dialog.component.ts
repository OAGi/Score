import { Component, OnInit, inject } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TagService} from '../domain/tag.service';
import {Tag} from '../domain/tag';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  standalone: false,
  selector: 'score-edit-tags-dialog',
  templateUrl: './edit-tags-dialog.component.html',
  styleUrls: ['./edit-tags-dialog.component.css']
})
export class EditTagsDialogComponent implements OnInit {
  dialogRef = inject<MatDialogRef<EditTagsDialogComponent>>(MatDialogRef);
  private tagService = inject(TagService);
  private confirmDialogService = inject(ConfirmDialogService);
  private snackBar = inject(MatSnackBar);
  data = inject(MAT_DIALOG_DATA);


  loading = false;

  tagList: Tag[] = [];
  newTag: Tag = new Tag();

  ngOnInit(): void {
    this.loading = true;
    this.tagService.getTags().subscribe(tagList => {
      this.tagList = tagList;

      this.loading = false;
    });
  }

  add(tag: Tag) {
    this.loading = true;
    this.tagService.create(tag).subscribe(_ => {
      this.newTag = new Tag();
      this.snackBar.open('Added', '', {
        duration: 3000,
      });
      this.tagService.getTags().subscribe(tagList => {
        this.tagList = tagList;
        this.loading = false;
      });
    }, err => {
      this.loading = false;
      throw err;
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
    }, err => {
      this.loading = false;
      throw err;
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
          }, err => {
            this.loading = false;
            throw err;
          });
        }
      });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
