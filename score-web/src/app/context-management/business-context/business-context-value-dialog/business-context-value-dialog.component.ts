import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {BusinessContextValue} from '../domain/business-context';
import {
  ContextScheme,
  ContextSchemeListRequest,
  ContextSchemeValue,
  ContextSchemeValueListRequest
} from '../../context-scheme/domain/context-scheme';
import {ContextSchemeService} from '../../context-scheme/domain/context-scheme.service';
import {hashCode} from '../../../common/utility';
import {forkJoin, ReplaySubject} from 'rxjs';
import {ContextCategoryService} from '../../context-category/domain/context-category.service';
import {ContextCategory, ContextCategoryListRequest} from '../../context-category/domain/context-category';
import {PageRequest} from '../../../basis/basis';
import {FormControl} from '@angular/forms';
import {finalize} from 'rxjs/operators';

@Component({
  selector: 'score-business-context-value-dialog',
  templateUrl: './business-context-value-dialog.component.html',
  styleUrls: ['./business-context-value-dialog.component.css']
})
export class BusinessContextValueDialogComponent implements OnInit {

  isLoading;
  isAddAction;
  actionName;
  hashCode;

  ctxCategories: ContextCategory[] = [];
  ctxCategoryListFilterCtrl: FormControl = new FormControl();
  filteredCtxCategoryList: ReplaySubject<ContextCategory[]> = new ReplaySubject<ContextCategory[]>(1);

  ctxSchemes: ContextScheme[] = [];
  ctxSchemeListFilterCtrl: FormControl = new FormControl();
  filteredCtxSchemeList: ReplaySubject<ContextScheme[]> = new ReplaySubject<ContextScheme[]>(1);

  ctxSchemeValues: ContextSchemeValue[] = [];
  ctxSchemeValueListFilterCtrl: FormControl = new FormControl();
  filteredCtxSchemeValueList: ReplaySubject<ContextSchemeValue[]> = new ReplaySubject<ContextSchemeValue[]>(1);

  constructor(
    public dialogRef: MatDialogRef<BusinessContextValueDialogComponent>,
    private contextCategoryService: ContextCategoryService,
    private contextSchemeService: ContextSchemeService,
    @Inject(MAT_DIALOG_DATA) public bizCtxValue: BusinessContextValue) {
  }

  get ctxCategory(): ContextCategory {
    if (this.bizCtxValue.ctxCategoryId) {
      for (const ctxCategory of this.ctxCategories) {
        if (ctxCategory.ctxCategoryId === this.bizCtxValue.ctxCategoryId) {
          return ctxCategory;
        }
      }
    }
    return new ContextCategory();
  }

  get ctxScheme(): ContextScheme {
    if (this.bizCtxValue.ctxCategoryId && this.bizCtxValue.ctxSchemeId) {
      for (const ctxScheme of this.ctxSchemes) {
        if (ctxScheme.ctxCategoryId === this.bizCtxValue.ctxCategoryId &&
          ctxScheme.ctxSchemeId === this.bizCtxValue.ctxSchemeId) {
          return ctxScheme;
        }
      }
    }
    return new ContextScheme();
  }

  get ctxSchemeValue(): ContextSchemeValue {
    if (this.bizCtxValue.ctxCategoryId && this.bizCtxValue.ctxSchemeId && this.bizCtxValue.ctxSchemeValueId) {
      for (const ctxSchemeValue of this.ctxSchemeValues) {
        if (ctxSchemeValue.ownerCtxSchemeId === this.bizCtxValue.ctxSchemeId &&
          ctxSchemeValue.ctxSchemeValueId === this.bizCtxValue.ctxSchemeValueId) {
          return ctxSchemeValue;
        }
      }
    }
    return new ContextSchemeValue();
  }

  onClick(): void {
    this.dialogRef.close(this.bizCtxValue);
  }

  isChanged() {
    return this.hashCode !== hashCode(this.bizCtxValue);
  }

  ngOnInit() {
    this.isAddAction = (this.bizCtxValue.guid === undefined);
    if (this.isAddAction) {
      this.actionName = 'Add';
    } else {
      this.actionName = 'Edit';
    }

    const contextCategoryListRequest = new ContextCategoryListRequest();
    contextCategoryListRequest.page = new PageRequest(
      null, 'asc', 0, 0);

    const contextSchemeListRequest = new ContextSchemeListRequest();
    contextSchemeListRequest.page = new PageRequest(
      null, 'asc', 0, 0);

    const contextSchemeValueListRequest = new ContextSchemeValueListRequest();
    contextSchemeValueListRequest.page = new PageRequest(
      null, 'asc', 0, 0);

    this.isLoading = true;
    forkJoin([
      this.contextCategoryService.getContextCategoryList(contextCategoryListRequest),
      this.contextSchemeService.getContextSchemeList(contextSchemeListRequest),
      this.contextSchemeService.getContextSchemeValueList(contextSchemeValueListRequest),
    ]).pipe(finalize(() => {
      this.isLoading = false;
    })).subscribe(([contextCategoryPage, contextSchemePage, contextSchemeValuePage]) => {
      this.ctxCategories = contextCategoryPage.list;
      this.resetCtxCategories();

      this.ctxSchemes = contextSchemePage.list;
      this.resetCtxSchemes();

      this.ctxSchemeValues = contextSchemeValuePage.list;
      this.resetCtxSchemeValues();

      this.hashCode = hashCode(this.bizCtxValue);
    });
  }

  resetCtxCategories() {
    this.ctxCategoryListFilterCtrl.valueChanges
      .subscribe(() => {
        let search = this.ctxCategoryListFilterCtrl.value;
        if (!search) {
          this.filteredCtxCategoryList.next(this.ctxCategories.slice());
          return;
        } else {
          search = search.toLowerCase();
        }
        this.filteredCtxCategoryList.next(
          this.ctxCategories.filter(e => e.name.toLowerCase().indexOf(search) > -1)
        );
      });
    this.filteredCtxCategoryList.next(this.ctxCategories.slice());
  }

  resetCtxSchemes() {
    const ctxScheme = this.ctxSchemes
      .filter(e => e.ctxCategoryId === this.bizCtxValue.ctxCategoryId);

    this.ctxSchemeListFilterCtrl.valueChanges
      .subscribe(() => {
        let search = this.ctxSchemeListFilterCtrl.value;
        if (!search) {
          this.filteredCtxSchemeList.next(ctxScheme.slice());
          return;
        } else {
          search = search.toLowerCase();
        }
        this.filteredCtxSchemeList.next(
          ctxScheme.filter(e => e.schemeName.toLowerCase().indexOf(search) > -1)
        );
      });
    this.filteredCtxSchemeList.next(ctxScheme.slice());
  }

  resetCtxSchemeValues() {
    const ctxSchemeValues = this.ctxSchemeValues
      .filter(e => e.ownerCtxSchemeId === this.bizCtxValue.ctxSchemeId);

    this.ctxSchemeValueListFilterCtrl.valueChanges
      .subscribe(() => {
        let search = this.ctxCategoryListFilterCtrl.value;
        if (!search) {
          this.filteredCtxSchemeValueList.next(ctxSchemeValues.slice());
          return;
        } else {
          search = search.toLowerCase();
        }
        this.filteredCtxSchemeValueList.next(
          ctxSchemeValues.filter(e => e.value.toLowerCase().indexOf(search) > -1)
        );
      });
    this.filteredCtxSchemeValueList.next(ctxSchemeValues.slice());
  }

  isDisabled() {
    return (this.bizCtxValue.ctxSchemeValue === undefined || this.bizCtxValue.ctxSchemeValue === '');
  }

  onCtxCategoryChange() {
    this.bizCtxValue.ctxCategoryName = this.ctxCategory.name;
    this.bizCtxValue.ctxSchemeId = undefined;
    this.resetCtxSchemes();
    this.bizCtxValue.ctxSchemeValue = undefined;
    this.resetCtxSchemeValues();
  }

  onCtxSchemeChange() {
    this.bizCtxValue.ctxSchemeName = this.ctxScheme.schemeName;
    this.bizCtxValue.ctxSchemeValue = undefined;
    this.resetCtxSchemeValues();
  }

  onCtxSchemeValueChange() {
    this.bizCtxValue.ctxSchemeValue = this.ctxSchemeValue.value;
  }
}
