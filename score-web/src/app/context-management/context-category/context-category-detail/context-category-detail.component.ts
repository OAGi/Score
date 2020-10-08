import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {switchMap} from 'rxjs/operators';
import {ContextCategoryService} from '../domain/context-category.service';
import {ContextCategory} from '../domain/context-category';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {hashCode} from '../../../common/utility';
import {ContextScheme} from '../../context-scheme/domain/context-scheme';
import {ConfirmDialogConfig} from '../../../common/confirm-dialog/confirm-dialog.domain';
import {ConfirmDialogComponent} from '../../../common/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'score-context-category-detail',
  templateUrl: './context-category-detail.component.html',
  styleUrls: ['./context-category-detail.component.css']
})
export class ContextCategoryDetailComponent implements OnInit {

  title = 'Edit Context Category';
  contextCategory: ContextCategory;
  hashCode;
  contextSchemes: ContextScheme[];

  constructor(private service: ContextCategoryService,
              private location: Location,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private dialog: MatDialog) {
  }

  ngOnInit() {
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
        duration: 3000,
      });
      this.router.navigateByUrl('/context_management/context_category');
    });
  }

  openDialogContextCategory(listDisplayed: string[]) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'The context category cannot be deleted!';
    dialogConfig.data.content = [
      'The context schemes with the following IDs depend on it. They need to be deleted first.'
    ];
    dialogConfig.data.list = listDisplayed;

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed().subscribe(_ => {});
  }

  openDialogContextCategoryDiscard() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = ['confirm-dialog'];
    dialogConfig.autoFocus = false;
    dialogConfig.data = new ConfirmDialogConfig();
    dialogConfig.data.header = 'Discard Context Category?';
    dialogConfig.data.content = [
      'Are you sure you want to discard the context category?',
      'The context category will be permanently removed.'
    ];

    dialogConfig.data.action = 'Discard';

    this.dialog.open(ConfirmDialogComponent, dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(this.contextCategory.ctxCategoryId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
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
        const listDisplayed = [];
        this.contextSchemes.forEach(ctxScheme => {
          listDisplayed.push(ctxScheme.guid);
        });
        // change of the list in order to get displayed by the snackbar, replace all the global comas by newlines
        const displayedList: string = listDisplayed.toString().replace(/,/g, ',\n ');

        this.openDialogContextCategory(listDisplayed);
      } else {
        this.openDialogContextCategoryDiscard();
      }
    });
  }

}
