import {ChangeDetectionStrategy, Component, inject, OnInit, viewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {CodelistListDialogComponent} from '../codelist-list-dialog/codelist-list-dialog.component';
import {ContextSchemeService} from '../domain/context-scheme.service';
import {ContextSchemeCreateRequest, ContextSchemeSummary, ContextSchemeValue} from '../domain/context-scheme';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ContextSchemeValueDialogComponent} from '../context-scheme-value-dialog/context-scheme-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {CONTEXT_SCHEME_VALUE_TABLE, contextSchemeValueMatches} from '../domain/context-scheme-value.table';
import {createTableColumns, inMemoryLoad, ScoreTableViewComponent} from '../../../common/score-table';
import {CodeList, CodeListDetails, CodeListListEntry, CodeListValueDetails} from '../../../code-list-management/domain/code-list';
import {v4 as uuid} from 'uuid';
import {forkJoin, ReplaySubject} from 'rxjs';
import {FormControl} from '@angular/forms';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {ContextCategoryService} from '../../context-category/domain/context-category.service';
import {ContextCategorySummary} from '../../context-category/domain/context-category';
import {CodeListService} from '../../../code-list-management/domain/code-list.service';

@Component({
  standalone: false,
  selector: 'score-context-scheme-create',
  templateUrl: './context-scheme-create.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./context-scheme-create.component.css']
})
export class ContextSchemeCreateComponent implements OnInit {
  private service = inject(ContextSchemeService);
  private contextCategoryService = inject(ContextCategoryService);
  private codeListService = inject(CodeListService);
  private location = inject(Location);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);


  title = 'Create Context Scheme';
  ctxCategories: ContextCategorySummary[];
  ctxCategoriesFilterCtrl: FormControl = new FormControl();
  filteredCtxCategories: ReplaySubject<ContextCategorySummary[]> = new ReplaySubject<ContextCategorySummary[]>(1);

  contextSchemes: ContextSchemeSummary[];
  codeLists: CodeList[];
  currCodeList: CodeList;
  codeListFilterCtrl: FormControl = new FormControl();
  filteredCodeLists: ReplaySubject<CodeList[]> = new ReplaySubject<CodeList[]>(1);
  disabled: boolean;

  contextSchemeCreateRequest: ContextSchemeCreateRequest;

  readonly columns = createTableColumns(CONTEXT_SCHEME_VALUE_TABLE);
  readonly view = viewChild.required<ScoreTableViewComponent<ContextSchemeValue>>(ScoreTableViewComponent);
  /** The guids of the selected values. */
  selection = new SelectionModel<string>(true, []);

  /** The values that the table lists. */
  values: ContextSchemeValue[] = [];
  readonly load = inMemoryLoad<ContextSchemeValue>(() => this.values, {},
    (row, params) => contextSchemeValueMatches(row, params.get('search')));
  readonly allRows = () => this.values;
  readonly rowKey = (row: ContextSchemeValue) => row.guid;

  ngOnInit() {
    this.disabled = false;
    this.contextSchemeCreateRequest = new ContextSchemeCreateRequest();

    forkJoin([
      this.contextCategoryService.getContextCategorySummaries(),
      this.service.getContextSchemeSummaries()
    ]).subscribe(([contextCategorySummries, contextSchemeSummaries]) => {
      this.ctxCategories = contextCategorySummries;
      this.filteredCtxCategories.next(this.ctxCategories.slice());
      this.codeLists = [];
      this.contextSchemes = contextSchemeSummaries;
      this.filteredCodeLists.next(this.codeLists.slice());
    });

    this.ctxCategoriesFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterCtxCategories();
      });

  }

  filterCtxCategories() {
    let search = this.ctxCategoriesFilterCtrl.value;
    if (!search) {
      this.filteredCtxCategories.next(this.ctxCategories.slice());
      return;
    } else {
      search = search.toLowerCase();
    }
    this.filteredCtxCategories.next(
      this.ctxCategories.filter(contextCategory => contextCategory.name.toLowerCase().indexOf(search) > -1)
    );
  }

  isDisabled() {
    return (this.disabled) ||
      (this.contextSchemeCreateRequest.contextCategoryId === undefined || this.contextSchemeCreateRequest.contextCategoryId <= 0) ||
      (this.contextSchemeCreateRequest.schemeName === undefined || this.contextSchemeCreateRequest.schemeName === '') ||
      (this.contextSchemeCreateRequest.schemeId === undefined || this.contextSchemeCreateRequest.schemeId === '') ||
      (this.contextSchemeCreateRequest.schemeAgencyId === undefined || this.contextSchemeCreateRequest.schemeAgencyId === '') ||
      (this.contextSchemeCreateRequest.schemeVersionId === undefined || this.contextSchemeCreateRequest.schemeVersionId === '');
  }

  openDialog(contextSchemeValue?: ContextSchemeValue) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = contextSchemeValue || new class implements ContextSchemeValue {
      contextSchemeValueId: number;
      guid: string;
      value: string;
      meaning: string;
      used: boolean;
      ownerContextSchemeId: number;
    };

    const isAddAction: boolean = (contextSchemeValue === undefined);
    if (!isAddAction) {
      dialogConfig.data = JSON.parse(JSON.stringify(contextSchemeValue));
    }
    this.disabled = true;
    const dialogRef = this.dialog.open(ContextSchemeValueDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined && result.value !== undefined && result.value !== '') {
        if (this.isDuplicateContextSchemeValue(result)) {
          this.snackBar.open(result.value + ' already exist', '', {
            duration: 3000,
          });
          this.disabled = false;
          return;
        }
        if (isAddAction) {
          const data = this.values;
          result.guid = uuid();
          data.push(result);
          this._updateDataSource(data);
        } else {
          const newData = [];
          this.values.forEach(row => {
            if (row.guid === result.guid) {
              newData.push(result);
            } else {
              newData.push(row);
            }
          });
          this._updateDataSource(newData);
        }
      }
      this.disabled = false;
    });
  }

  /**
   * Whether the given (possibly edited) value collides with another row's value.
   * The row being edited is excluded via its guid; otherwise editing a value in place
   * (e.g. changing only its Meaning) is wrongly flagged as a duplicate of itself. (Issue #1744 class)
   * A newly added value has no guid yet, so it is compared against every existing row.
   */
  isDuplicateContextSchemeValue(result: ContextSchemeValue): boolean {
    return this.values.some(value => value.guid !== result.guid && value.value === result.value);
  }

  _updateDataSource(data: ContextSchemeValue[]) {
    this.values = data;
    this.contextSchemeCreateRequest.contextSchemeValueList = data;
    this.view()?.reload();
  }

  removeSchemeValues() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Remove Context Scheme Value?';
    dialogConfig.data.content = ['Are you sure you want to remove the context scheme value?'];
    dialogConfig.data.action = 'Remove';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          const newData = [];
          this.values.forEach(row => {
            if (!this.selection.isSelected(row.guid)) {
              newData.push(row);
            }
          });
          this.selection.clear();

          this._updateDataSource(newData);
        }
      });
  }

  back() {
    this.location.back();
  }

  openDialogContextSchemeCreate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another context scheme with the triplet (schemeID, AgencyID, Version) already exist!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

  openDialogContextSchemeUpdateCreate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'The context scheme already has a variable with the same properties';
    dialogConfig.data.content = [
      'Are you sure you want to create the context scheme?'
    ];
    dialogConfig.data.action = 'Create anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.doCreate();
        }
      });
  }

  doCreate() {
    this.service.create(this.contextSchemeCreateRequest).subscribe(_ => {
      this.snackBar.open('Created', '', {
        duration: 3000,
      });
      this.router.navigateByUrl('/context_management/context_scheme');
    });
  }

  create() {
    this.checkUniqueness((_) => {
      this.checkSchemeAgencyName((_) => {
        this.doCreate();
      });
    });
  }

  checkUniqueness(callbackFn?) {
    this.service.checkUniqueness(this.contextSchemeCreateRequest.schemeId,
        this.contextSchemeCreateRequest.schemeAgencyId,
        this.contextSchemeCreateRequest.schemeVersionId).subscribe(resp => {
      if (resp) {
        this.openDialogContextSchemeCreate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  checkSchemeAgencyName(callbackFn?) {
    this.service.checkNameUniqueness(this.contextSchemeCreateRequest.schemeName,
        this.contextSchemeCreateRequest.schemeId,
        this.contextSchemeCreateRequest.schemeAgencyId,
        this.contextSchemeCreateRequest.schemeVersionId).subscribe(resp => {
      if (resp) {
        this.openDialogContextSchemeUpdateCreate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  convertCodeListValuesIntoContextSchemeValues(codeListValues: CodeListValueDetails[]): ContextSchemeValue[] {
    let contextSchemeValueList: ContextSchemeValue[];
    contextSchemeValueList = [];
    for (const codeListValue of codeListValues) {
      let contextSchemeValue: ContextSchemeValue;
      contextSchemeValue = new ContextSchemeValue();
      contextSchemeValue.meaning = codeListValue.meaning;
      contextSchemeValue.value = codeListValue.value;
      contextSchemeValue.guid = uuid();
      contextSchemeValueList.push(contextSchemeValue);
    }
    return contextSchemeValueList;
  }

  openCodeListDialog() {
    if (this.isDirty()) {
      const dialogConfig = this.confirmDialogService.newConfig();
      dialogConfig.data.header = 'Confirmation';
      dialogConfig.data.content = ['All existing values will be removed and replaced with values from the code list.'];
      dialogConfig.data.action = 'Continue';

      this.confirmDialogService.open(dialogConfig).afterClosed()
        .subscribe(result => {
          if (result) {
            this._loadFromCodeList();
          }
        });
    } else {
      this._loadFromCodeList();
    }
  }

  _loadFromCodeList() {
    const codeListdialogConfig = {
      data: {},
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    };
    codeListdialogConfig.width = window.innerWidth + 'px';

    const dialogRef = this.dialog.open(CodelistListDialogComponent, codeListdialogConfig);
    dialogRef.afterClosed().subscribe((codeList: CodeListListEntry) => {
      if (codeList) {
        this.codeListService.getCodeListDetails(codeList.codeListManifestId).subscribe((codeListDetails: CodeListDetails) => {
          this.contextSchemeCreateRequest.schemeId = codeListDetails.listId.toString();
          this.contextSchemeCreateRequest.schemeAgencyId = codeListDetails.agencyIdListValue.value.toString();
          this.contextSchemeCreateRequest.schemeVersionId = codeListDetails.versionId.toString();
          this._updateDataSource([]);
          this._updateDataSource(this.convertCodeListValuesIntoContextSchemeValues(codeListDetails.valueList));
        });
      }
    });
  }

  isDirty(): boolean {
    return this.contextSchemeCreateRequest.schemeId && this.contextSchemeCreateRequest.schemeId.length > 0
      || this.contextSchemeCreateRequest.schemeAgencyId && this.contextSchemeCreateRequest.schemeAgencyId.length > 0
      || this.contextSchemeCreateRequest.schemeVersionId && this.contextSchemeCreateRequest.schemeVersionId.length > 0
      || this.contextSchemeCreateRequest.contextSchemeValueList && this.contextSchemeCreateRequest.contextSchemeValueList.length > 0;
  }
}
