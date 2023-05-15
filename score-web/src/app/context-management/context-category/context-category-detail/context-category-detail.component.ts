import {Component, HostListener, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {switchMap} from 'rxjs/operators';
import {ContextCategoryService} from '../domain/context-category.service';
import {ContextCategory} from '../domain/context-category';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {hashCode} from '../../../common/utility';
import {ContextScheme} from '../../context-scheme/domain/context-scheme';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';

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
              private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService) {
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

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent($event: KeyboardEvent) {
    const charCode = $event.key?.toLowerCase();

    // Handle 'Ctrl/Command+S'
    const metaOrCtrlKeyPressed = $event.metaKey || $event.ctrlKey;
    if (metaOrCtrlKeyPressed && charCode === 's') {
      $event.preventDefault();
      $event.stopPropagation();

      this.update();
    }
  }

  get updateDisabled(): boolean {
    return !this.isChanged() || this.isDisabled(this.contextCategory);
  }

  update() {
    if (this.updateDisabled) {
      return;
    }

    this.service.update(this.contextCategory).subscribe(_ => {
      this.hashCode = hashCode(this.contextCategory);
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/context_management/context_category');
    });
  }

  openDialogContextCategory(listDisplayed: string[]) {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'The context category cannot be deleted!';
    dialogConfig.data.content = [
      'The context schemes with the following IDs depend on it. They need to be deleted first.'
    ];
    dialogConfig.data.list = listDisplayed;

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

  openDialogContextCategoryDiscard() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Context Category?';
    dialogConfig.data.content = [
      'Are you sure you want to discard the context category?',
      'The context category will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(this.contextCategory.contextCategoryId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.router.navigateByUrl('/context_management/context_category');
          });
        }
      });
  }

  discard() {
    this.service.getContextSchemeFromCategoryId(this.contextCategory.contextCategoryId).subscribe(value => {
      this.contextSchemes = value;
      if (this.contextSchemes.length > 0) {
        const listDisplayed = [];
        this.contextSchemes.forEach(contextScheme => {
          listDisplayed.push(contextScheme.guid);
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
