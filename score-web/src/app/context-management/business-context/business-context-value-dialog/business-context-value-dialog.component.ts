import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {BusinessContextValue} from '../domain/business-context';
import {ContextSchemeSummary, ContextSchemeValueListRequest, ContextSchemeValueSummary} from '../../context-scheme/domain/context-scheme';
import {ContextSchemeService} from '../../context-scheme/domain/context-scheme.service';
import {hashCode} from '../../../common/utility';
import {forkJoin, ReplaySubject} from 'rxjs';
import {ContextCategoryService} from '../../context-category/domain/context-category.service';
import {ContextCategorySummary} from '../../context-category/domain/context-category';
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

  contextCategoryList: ContextCategorySummary[] = [];
  contextCategoryListFilterCtrl: FormControl = new FormControl();
  filteredContextCategoryList: ReplaySubject<ContextCategorySummary[]> = new ReplaySubject<ContextCategorySummary[]>(1);

  contextSchemeList: ContextSchemeSummary[] = [];
  contextSchemeListFilterCtrl: FormControl = new FormControl();
  filteredContextSchemeList: ReplaySubject<ContextSchemeSummary[]> = new ReplaySubject<ContextSchemeSummary[]>(1);

  contextSchemeValueList: ContextSchemeValueSummary[] = [];
  contextSchemeValueListFilterCtrl: FormControl = new FormControl();
  filteredContextSchemeValueList: ReplaySubject<ContextSchemeValueSummary[]> = new ReplaySubject<ContextSchemeValueSummary[]>(1);

  constructor(
    public dialogRef: MatDialogRef<BusinessContextValueDialogComponent>,
    private contextCategoryService: ContextCategoryService,
    private contextSchemeService: ContextSchemeService,
    @Inject(MAT_DIALOG_DATA) public businessContextValue: BusinessContextValue) {
  }

  get contextCategory(): ContextCategorySummary {
    if (this.businessContextValue.contextCategoryId) {
      for (const contextCategory of this.contextCategoryList) {
        if (contextCategory.contextCategoryId === this.businessContextValue.contextCategoryId) {
          return contextCategory;
        }
      }
    }
    return new ContextCategorySummary();
  }

  get contextScheme(): ContextSchemeSummary {
    if (this.businessContextValue.contextCategoryId &&
      this.businessContextValue.contextSchemeId) {

      for (const contextScheme of this.contextSchemeList) {
        if (contextScheme.contextCategory.contextCategoryId === this.businessContextValue.contextCategoryId &&
          contextScheme.contextSchemeId === this.businessContextValue.contextSchemeId) {
          return contextScheme;
        }
      }
    }
    return new ContextSchemeSummary();
  }

  get contextSchemeValue(): ContextSchemeValueSummary {
    if (this.businessContextValue.contextCategoryId &&
      this.businessContextValue.contextSchemeId &&
      this.businessContextValue.contextSchemeValueId) {

      for (const contextSchemeValue of this.contextSchemeValueList) {
        if (contextSchemeValue.contextSchemeId === this.businessContextValue.contextSchemeId &&
          contextSchemeValue.contextSchemeValueId === this.businessContextValue.contextSchemeValueId) {
          return contextSchemeValue;
        }
      }
    }
    return new ContextSchemeValueSummary();
  }

  onClick(): void {
    this.dialogRef.close(this.businessContextValue);
  }

  isChanged() {
    return this.hashCode !== hashCode(this.businessContextValue);
  }

  ngOnInit() {
    this.isAddAction = (this.businessContextValue.guid === undefined);
    if (this.isAddAction) {
      this.actionName = 'Add';
    } else {
      this.actionName = 'Edit';
    }

    const contextSchemeValueListRequest = new ContextSchemeValueListRequest();
    contextSchemeValueListRequest.page = new PageRequest(
      undefined, 'asc', -1, -1);

    this.isLoading = true;
    forkJoin([
      this.contextCategoryService.getContextCategorySummaries(),
      this.contextSchemeService.getContextSchemeSummaries(),
      this.contextSchemeService.getContextSchemeValueSummaries()
    ]).pipe(finalize(() => {
      this.isLoading = false;
    })).subscribe(([contextCategories, contextSchemes, contextSchemeValues]) => {
      this.contextCategoryList = contextCategories;
      this.resetCtxCategories();

      this.contextSchemeList = contextSchemes;
      this.resetCtxSchemes();

      this.contextSchemeValueList = contextSchemeValues;
      this.resetCtxSchemeValues();

      this.hashCode = hashCode(this.businessContextValue);
    });

    this.contextCategoryListFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterCtxCategories();
      });

    this.contextSchemeListFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterCtxSchemes();
      });

    this.contextSchemeValueListFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterCtxSchemeValues();
      });
  }

  filterCtxCategories() {
    let search = this.contextCategoryListFilterCtrl.value;
    if (!search) {
      this.filteredContextCategoryList.next(this.contextCategoryList.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredContextCategoryList.next(
      this.contextCategoryList.filter(contextCategory => contextCategory.name.toLowerCase().indexOf(search) > -1)
    );
  }

  filterCtxSchemes() {
    let search = this.contextSchemeListFilterCtrl.value;
    if (!search) {
      this.filteredContextSchemeList.next(this.contextSchemeList.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredContextSchemeList.next(
        this.contextSchemeList.filter(contextScheme =>
            contextScheme.schemeName.toLowerCase().indexOf(search) > -1 ||
            contextScheme.schemeId.toLowerCase().indexOf(search) > -1 ||
            contextScheme.schemeAgencyId.toLowerCase().indexOf(search) > -1)
    );
  }

  filterCtxSchemeValues() {
    let search = this.contextSchemeValueListFilterCtrl.value;
    if (!search) {
      this.filteredContextSchemeValueList.next(this.contextSchemeValueList.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredContextSchemeValueList.next(
        this.contextSchemeValueList.filter(contextSchemeValue =>
            contextSchemeValue.value.toLowerCase().indexOf(search) > -1 ||
            contextSchemeValue.meaning.toLowerCase().indexOf(search) > -1)
    );
  }

  resetCtxCategories() {
    this.contextCategoryListFilterCtrl.valueChanges
      .subscribe(() => {
        let search = this.contextCategoryListFilterCtrl.value;
        if (!search) {
          this.filteredContextCategoryList.next(this.contextCategoryList.slice());
          return;
        } else {
          search = search.toLowerCase();
        }
        this.filteredContextCategoryList.next(
          this.contextCategoryList.filter(e => e.name.toLowerCase().indexOf(search) > -1)
        );
      });
    this.filteredContextCategoryList.next(this.contextCategoryList.slice());
  }

  resetCtxSchemes() {
    const contextScheme = this.contextSchemeList
      .filter(e => e.contextCategory.contextCategoryId === this.businessContextValue.contextCategoryId);

    this.contextSchemeListFilterCtrl.valueChanges
      .subscribe(() => {
        let search = this.contextSchemeListFilterCtrl.value;
        if (!search) {
          this.filteredContextSchemeList.next(contextScheme.slice());
          return;
        } else {
          search = search.toLowerCase();
        }
        this.filteredContextSchemeList.next(
            contextScheme.filter(e =>
                e.schemeName.toLowerCase().indexOf(search) > -1 ||
                e.schemeId.toLowerCase().indexOf(search) > -1 ||
                e.schemeAgencyId.toLowerCase().indexOf(search) > -1)
        );
      });
    this.filteredContextSchemeList.next(contextScheme.slice());
  }

  resetCtxSchemeValues() {
    const contextSchemeValueList = this.contextSchemeValueList
      .filter(e => e.contextSchemeId === this.businessContextValue.contextSchemeId);

    this.contextSchemeValueListFilterCtrl.valueChanges
      .subscribe(() => {
        let search = this.contextSchemeValueListFilterCtrl.value;
        if (!search) {
          this.filteredContextSchemeValueList.next(contextSchemeValueList.slice());
          return;
        } else {
          search = search.toLowerCase();
        }
        this.filteredContextSchemeValueList.next(
            contextSchemeValueList.filter(e =>
                e.value.toLowerCase().indexOf(search) > -1 ||
                e.meaning.toLowerCase().indexOf(search) > -1)
        );
      });
    this.filteredContextSchemeValueList.next(contextSchemeValueList.slice());
  }

  isDisabled() {
    return (this.businessContextValue.contextSchemeValue === undefined || this.businessContextValue.contextSchemeValue === '');
  }

  onCtxCategoryChange() {
    this.businessContextValue.contextCategoryName = this.contextCategory.name;
    this.businessContextValue.contextSchemeId = undefined;
    this.resetCtxSchemes();
    this.businessContextValue.contextSchemeValue = undefined;
    this.resetCtxSchemeValues();
  }

  onCtxSchemeChange() {
    this.businessContextValue.contextSchemeName = this.contextScheme.schemeName;
    this.businessContextValue.contextSchemeValue = undefined;
    this.resetCtxSchemeValues();
  }

  onCtxSchemeValueChange() {
    this.businessContextValue.contextSchemeValue = this.contextSchemeValue.value;
    this.businessContextValue.contextSchemeValueMeaning = this.contextSchemeValue.meaning;
  }
}
