import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {BusinessContextValue} from '../domain/business-context';
import {SimpleContextCategory, SimpleContextScheme, SimpleContextSchemeValue} from '../../context-scheme/domain/context-scheme';
import {ContextSchemeService} from '../../context-scheme/domain/context-scheme.service';
import {hashCode} from '../../../common/utility';

@Component({
  selector: 'srt-business-context-value-dialog',
  templateUrl: './business-context-value-dialog.component.html',
  styleUrls: ['./business-context-value-dialog.component.css']
})
export class BusinessContextValueDialogComponent implements OnInit {

  isAddAction;
  actionName;
  hashCode;

  ctxCategories: SimpleContextCategory[] = [];
  ctxSchemes: SimpleContextScheme[] = [];
  ctxSchemeValues: SimpleContextSchemeValue[] = [];

  constructor(
    public dialogRef: MatDialogRef<BusinessContextValueDialogComponent>,
    private contextSchemeService: ContextSchemeService,
    @Inject(MAT_DIALOG_DATA) public bizCtxValue: BusinessContextValue) {
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

    this.contextSchemeService.getSimpleContextCategories().subscribe(ctxCategories => {
      this.ctxCategories = ctxCategories;

      if (this.bizCtxValue.ctxSchemeValueId !== undefined) {
        this.ctxCategories.forEach(row => {
          if (row.ctxCategoryId === this.bizCtxValue.ctxCategoryId) {
            this.bizCtxValue.ctxCategoryName = row.name;
          }
        });

        this.contextSchemeService.getSimpleContextSchemeByCtxCategoryId(this.bizCtxValue.ctxCategoryId)
          .subscribe(ctxSchemes => {
            this.ctxSchemes = ctxSchemes;

            this.ctxSchemes.forEach(row => {
              if (row.ctxSchemeId === this.bizCtxValue.ctxSchemeId) {
                this.bizCtxValue.ctxSchemeName = row.schemeName;
              }
            });

            this.contextSchemeService.getSimpleContextSchemeValues(this.bizCtxValue.ctxSchemeId)
              .subscribe(ctxSchemeValues => {
                this.ctxSchemeValues = ctxSchemeValues;

                this.ctxSchemeValues.forEach(row => {
                  if (row.ctxSchemeValueId === this.bizCtxValue.ctxSchemeValueId) {
                    this.bizCtxValue.ctxSchemeValue = row.value;

                    this.hashCode = hashCode(this.bizCtxValue);
                  }
                });
              });
          });
      }
    });
  }

  isDisabled() {
    return (this.bizCtxValue.ctxSchemeValue === undefined || this.bizCtxValue.ctxSchemeValue === '');
  }

  onCtxCategoryChange() {
    this.ctxCategories.forEach(row => {
      if (row.ctxCategoryId === this.bizCtxValue.ctxCategoryId) {
        this.bizCtxValue.ctxCategoryName = row.name;
      }
    });

    this.bizCtxValue.ctxSchemeId = undefined;
    this.ctxSchemes = [];
    this.bizCtxValue.ctxSchemeValueId = undefined;
    this.ctxSchemeValues = [];

    this.contextSchemeService.getSimpleContextSchemeByCtxCategoryId(this.bizCtxValue.ctxCategoryId)
      .subscribe(resp => this.ctxSchemes = resp);
  }

  onCtxSchemeChange() {
    this.ctxSchemes.forEach(row => {
      if (row.ctxSchemeId === this.bizCtxValue.ctxSchemeId) {
        this.bizCtxValue.ctxSchemeName = row.schemeName;
      }
    });

    this.bizCtxValue.ctxSchemeValueId = undefined;
    this.ctxSchemeValues = [];

    this.contextSchemeService.getSimpleContextSchemeValues(this.bizCtxValue.ctxSchemeId)
      .subscribe(resp => this.ctxSchemeValues = resp);
  }

  onCtxSchemeValueChange() {
    this.ctxSchemeValues.forEach(row => {
      if (row.ctxSchemeValueId === this.bizCtxValue.ctxSchemeValueId) {
        this.bizCtxValue.ctxSchemeValue = row.value;
      }
    });
  }
}
