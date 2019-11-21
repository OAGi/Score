import {Component, Inject, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {switchMap} from 'rxjs/operators';
import {ContextCategoryService} from '../domain/context-category.service';
import {ContextCategory} from '../domain/context-category';
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig, MatSnackBar} from '@angular/material';
import {hashCode} from '../../../common/utility';
import {ContextScheme} from '../../context-scheme/domain/context-scheme';

@Component({
  selector: 'srt-context-category-detail',
  templateUrl: './context-category-detail.component.html',
  styleUrls: ['./context-category-detail.component.css']
})
export class ContextCategoryDetailComponent implements OnInit {

  title;
  contextCategory: ContextCategory;
  hashCode;
  contextSchemes: ContextScheme[];
  listDisplayed;

  constructor(private service: ContextCategoryService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.title = 'Context Category Detail';
    this.contextCategory = new ContextCategory();

    this.route.paramMap.pipe(
      switchMap((params: ParamMap) =>
        this.service.getContextCategory(params.get('id')))
    ).subscribe(resp => {
      this.hashCode = hashCode(resp);
      this.contextCategory = resp;
    });
  }

  isChanged() {
    return this.hashCode !== hashCode(this.contextCategory);
  }

  isDisabled(contextCategory: ContextCategory) {
    return contextCategory.name === undefined || contextCategory.name === '';
  }

  back() {
    this.location.back();
  }

  update() {
    this.service.update(this.contextCategory).subscribe(_ => {
      this.hashCode = hashCode(this.contextCategory);
      this.snackBar.open('Updated', '', {
        duration: 1000,
      });
      this.router.navigateByUrl('/context_management/context_category');
    });
  }

  openDialogContextCategory() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {list: this.listDisplayed};
    const dialogRef = this.dialog.open(DialogContentContextCategoryDialogDetailComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(result => {
    });
  }

  openDialogContextCategoryDiscard() {
    const dialogConfig = new MatDialogConfig();
    const dialogRef = this.dialog.open(DialogDiscardContextCategoryDialogDetailComponent, dialogConfig);

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.delete(this.contextCategory.ctxCategoryId).subscribe(_ => {
          this.snackBar.open('Discarded', '', {
            duration: 1000,
          });
          this.router.navigateByUrl('/context_management/context_category');
        });
      }
    });
  }

  discard() {
    this.service.getContextSchemeFromCategoryId(this.contextCategory.ctxCategoryId).subscribe(value => {
      this.contextSchemes = value;
      if (this.contextSchemes.length > 0) {
        const A = [];
        this.contextSchemes.forEach(ctxScheme => {
          A.push(ctxScheme.guid);
        });
        // change of the list in order to get displayed by the snackbar, replace all the global comas by newlines
        const displayedList: String = A.toString().replace(/,/g, ',\n ');
        this.listDisplayed = A;
        this.openDialogContextCategory();
      } else {
        this.openDialogContextCategoryDiscard();
      }
    });
  }

}

@Component({
  selector: 'srt-dialog-content-context-category-dialog-detail',
  templateUrl: 'dialog-content-context-category-detail-dialog.html',
})
export class DialogContentContextCategoryDialogDetailComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}

@Component({
  selector: 'srt-dialog-content-context-category-dialog-detail',
  templateUrl: 'dialog-discard-context-category-detail-dialog.html',
})
export class DialogDiscardContextCategoryDialogDetailComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

}
