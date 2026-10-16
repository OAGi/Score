import {ChangeDetectionStrategy, Component, HostListener, inject, OnInit, viewChild} from '@angular/core';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {CodelistListDialogComponent} from '../codelist-list-dialog/codelist-list-dialog.component';
import {ContextSchemeService} from '../domain/context-scheme.service';
import {ContextSchemeSummary, ContextSchemeUpdateRequest, ContextSchemeValue} from '../domain/context-scheme';
import {BusinessContextService} from '../../business-context/domain/business-context.service';
import {MatCheckboxChange} from '@angular/material/checkbox';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ContextSchemeValueDialogComponent} from '../context-scheme-value-dialog/context-scheme-value-dialog.component';
import {SelectionModel} from '@angular/cdk/collections';
import {CONTEXT_SCHEME_VALUE_TABLE, contextSchemeValueMatches} from '../domain/context-scheme-value.table';
import {createTableColumns, inMemoryLoad, ScoreTableViewComponent} from '../../../common/score-table';
import {hashCode} from '../../../common/utility';
import {CodeList, CodeListDetails, CodeListListEntry, CodeListValueDetails} from '../../../code-list-management/domain/code-list';
import {forkJoin, ReplaySubject} from 'rxjs';
import {v4 as uuid} from 'uuid';
import {FormControl} from '@angular/forms';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';
import {ContextCategoryService} from '../../context-category/domain/context-category.service';
import {ContextCategorySummary} from '../../context-category/domain/context-category';
import {CodeListService} from '../../../code-list-management/domain/code-list.service';
import {finalize} from 'rxjs/operators';
import {Title} from '@angular/platform-browser';
import {setAppTitleIfPresent} from '../../../common/app-title.strategy';

@Component({
  standalone: false,
  selector: 'score-context-scheme-detail',
  templateUrl: './context-scheme-detail.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./context-scheme-detail.component.css']
})
export class ContextSchemeDetailComponent implements OnInit {
  private service = inject(ContextSchemeService);
  private contextCategoryService = inject(ContextCategoryService);
  private codeListService = inject(CodeListService);
  private businessContextService = inject(BusinessContextService);
  private location = inject(Location);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);
  private titleService = inject(Title);


  title = 'Edit Context Scheme';
  isUpdating: boolean;

  ctxCategories: ContextCategorySummary[];
  ctxCategoriesFilterCtrl: FormControl = new FormControl();
  filteredCtxCategories: ReplaySubject<ContextCategorySummary[]> = new ReplaySubject<ContextCategorySummary[]>(1);

  codeLists: CodeList[];
  currCodeList: CodeList;
  codeListFilterCtrl: FormControl = new FormControl();
  filteredCodeLists: ReplaySubject<CodeList[]> = new ReplaySubject<CodeList[]>(1);

  contextSchemes: ContextSchemeSummary[];
  contextSchemeUpdateRequest: ContextSchemeUpdateRequest;
  hashCode;
  businessContext;
  disabled: boolean;

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
  readonly rowSelectable = (row: ContextSchemeValue) => !row.used;

  ngOnInit() {
    this.contextSchemeUpdateRequest = new ContextSchemeUpdateRequest();
    this.contextSchemeUpdateRequest.used = true;
    const contextSchemeId = this.route.snapshot.params.id;

    this.isUpdating = true;
    forkJoin([
      this.contextCategoryService.getContextCategorySummaries(),
      this.service.getContextSchemeSummaries(),
      this.service.getContextSchemeDetails(contextSchemeId),
      this.service.getContextSchemeValues(contextSchemeId)
    ]).subscribe(([contextCategorySummries, contextSchemeSummaries,
                    contextScheme, contextSchemeValues]) => {

      if (!contextScheme) {
        this.redirectToContextSchemeList();
        return;
      }

      this.ctxCategories = contextCategorySummries;
      this.filteredCtxCategories.next(this.ctxCategories.slice());
      this.codeLists = [];
      this.contextSchemes = contextSchemeSummaries;

      this.contextSchemeUpdateRequest.contextSchemeId = contextScheme.contextSchemeId;
      this.contextSchemeUpdateRequest.schemeName = contextScheme.schemeName;
      this.contextSchemeUpdateRequest.schemeId = contextScheme.schemeId;
      this.contextSchemeUpdateRequest.schemeAgencyId = contextScheme.schemeAgencyId;
      this.contextSchemeUpdateRequest.schemeVersionId = contextScheme.schemeVersionId;
      this.contextSchemeUpdateRequest.description = contextScheme.description;

      this.contextSchemeUpdateRequest.contextCategoryId = contextScheme.contextCategory.contextCategoryId;
      this.contextSchemeUpdateRequest.codeListId = (!!contextScheme.codeList) ? contextScheme.codeList.codeListId : undefined;

      this.contextSchemeUpdateRequest.contextSchemeValueList = contextSchemeValues;
      this.contextSchemeUpdateRequest.used = contextScheme.used;
      setAppTitleIfPresent(this.titleService, this.contextSchemeUpdateRequest.schemeName, 'Context Scheme');

      this.hashCode = hashCode(this.contextSchemeUpdateRequest);
      this.values = this.contextSchemeUpdateRequest.contextSchemeValueList;
      this.view().reload();
      this.filteredCodeLists.next(this.codeLists.slice());

      this.isUpdating = false;
    }, err => {
      this.isUpdating = false;
      let errorMessage;
      if (err.status === 404) {
        this.redirectToContextSchemeList();
        return;
      } else if (err.status === 403) {
        errorMessage = 'You do not have access permission.';
      } else {
        errorMessage = 'Something\'s wrong.';
      }
      this.snackBar.open(errorMessage, '', {
        duration: 3000
      });
      this.router.navigateByUrl('/context_management/context_scheme');
      return;
    });

    this.ctxCategoriesFilterCtrl.valueChanges
      .subscribe(() => {
        this.filterCtxCategories();
      });

  }

  private redirectToContextSchemeList() {
    this.isUpdating = false;
    this.snackBar.open('The requested context scheme is unavailable.', '', {
      duration: 3000
    });
    this.router.navigateByUrl('/context_management/context_scheme');
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

  isChanged() {
    return this.hashCode !== hashCode(this.contextSchemeUpdateRequest);
  }

  isDisabled(contextScheme: ContextSchemeUpdateRequest) {
    return (this.disabled) || (this.isUpdating) ||
      (contextScheme.contextCategoryId === undefined || contextScheme.contextCategoryId <= 0) ||
      (contextScheme.schemeName === undefined || contextScheme.schemeName === '') ||
      (contextScheme.schemeId === undefined || contextScheme.schemeId === '') ||
      (contextScheme.schemeAgencyId === undefined || contextScheme.schemeAgencyId === '') ||
      (contextScheme.schemeVersionId === undefined || contextScheme.schemeVersionId === '');
  }

  openDialog(contextSchemeValue?: ContextSchemeValue) {
    const dialogConfig = new MatDialogConfig();
    this.disabled = true;
    dialogConfig.data = new ContextSchemeValue();

    if (contextSchemeValue) { // deep copy
      dialogConfig.data = JSON.parse(JSON.stringify(contextSchemeValue));
    }
    const isAddAction: boolean = (contextSchemeValue === undefined);
    const dialogRef = this.dialog.open(ContextSchemeValueDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined && result.value !== undefined && result.value !== '') {
        for (const value of this.values) {
          if (value.guid !== result.guid && value.value === result.value) {
            this.snackBar.open(result.value + ' already exist', '', {
              duration: 3000,
            });
            this.disabled = false;
            return;
          }
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


  _updateDataSource(data: ContextSchemeValue[]) {
    this.values = data;
    this.contextSchemeUpdateRequest.contextSchemeValueList = data;
    this.view()?.reload();
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
    return !this.isChanged() || this.isDisabled(this.contextSchemeUpdateRequest);
  }

  update() {
    if (this.updateDisabled) {
      return;
    }

    this.checkUniqueness((_) => {
      this.checkSchemeAgencyName((_) => {
        this.doUpdate();
      });
    });
  }

  checkUniqueness(callbackFn?) {
    this.service.checkUniqueness(this.contextSchemeUpdateRequest.schemeId,
        this.contextSchemeUpdateRequest.schemeAgencyId,
        this.contextSchemeUpdateRequest.schemeVersionId,
        this.contextSchemeUpdateRequest.contextSchemeId).subscribe(resp => {
      if (resp) {
        this.openDialogContextSchemeUpdate();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  checkSchemeAgencyName(callbackFn?) {
    this.service.checkNameUniqueness(this.contextSchemeUpdateRequest.schemeName,
        this.contextSchemeUpdateRequest.schemeId,
        this.contextSchemeUpdateRequest.schemeAgencyId,
        this.contextSchemeUpdateRequest.schemeVersionId,
        this.contextSchemeUpdateRequest.contextSchemeId).subscribe(resp => {
      if (resp) {
        this.openDialogContextSchemeUpdateIgnore();
        return;
      }
      return callbackFn && callbackFn();
    });
  }

  doUpdate() {
    this.isUpdating = true;

    this.service.update(this.contextSchemeUpdateRequest).pipe(finalize(() => {
      this.isUpdating = false;
    })).subscribe(_ => {
      this.hashCode = hashCode(this.contextSchemeUpdateRequest);
      setAppTitleIfPresent(this.titleService, this.contextSchemeUpdateRequest.schemeName, 'Context Scheme');
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    });
  }

  convertCodeListValuesIntoContextSchemeValues(codeListValues: CodeListValueDetails[]): ContextSchemeValue[] {
    let contextSchemeValueList: ContextSchemeValue[];
    contextSchemeValueList = [];
    for (const codelistvalue of codeListValues) {
      let contextSchemeValue: ContextSchemeValue;
      contextSchemeValue = new ContextSchemeValue();
      contextSchemeValue.meaning = codelistvalue.meaning;
      contextSchemeValue.value = codelistvalue.value;
      // The table keys its rows (and so the selection) by guid.
      contextSchemeValue.guid = uuid();
      contextSchemeValueList.push(contextSchemeValue);
      this.selection.select(contextSchemeValue.guid);
    }
    return contextSchemeValueList;
  }

  openDialogContextSchemeUpdate() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Invalid parameters';
    dialogConfig.data.content = [
      'Another context scheme with the triplet (schemeID, AgencyID, Version) already exist!'
    ];

    this.confirmDialogService.open(dialogConfig).afterClosed().subscribe(_ => {});
  }

  openDialogContextSchemeUpdateIgnore() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'The context scheme already has a variable with the same properties';
    dialogConfig.data.content = [
      'Are you sure you want to update the context scheme?'
    ];
    dialogConfig.data.action = 'Update anyway';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.doUpdate();
        }
      });
  }

  discard() {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Discard Context Scheme?';
    dialogConfig.data.content = [
      'Are you sure you want to discard this context scheme?',
      'The context scheme will be permanently removed.'
    ];
    dialogConfig.data.action = 'Discard';

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.service.delete(this.contextSchemeUpdateRequest.contextSchemeId).subscribe(_ => {
            this.snackBar.open('Discarded', '', {
              duration: 3000,
            });
            this.router.navigateByUrl('/context_management/context_scheme');
          });
        }
      });
  }

  resetCreateForm(event: MatCheckboxChange) {
    if (!event.checked) {
      this.contextSchemeUpdateRequest.codeListId = undefined;
      this.selection.clear();
      this._updateDataSource([]);
    }

    this.contextSchemeUpdateRequest.schemeId = undefined;
    this.contextSchemeUpdateRequest.schemeAgencyId = undefined;
    this.contextSchemeUpdateRequest.schemeVersionId = undefined;
  }

  canImport(): boolean {
    if (this.contextSchemeUpdateRequest.used) {
      return false;
    }
    if (this.contextSchemeUpdateRequest.contextSchemeValueList &&
        this.contextSchemeUpdateRequest.contextSchemeValueList.filter(e => e.used).length > 0) {
      return false;
    }
    return true;
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
    const codeListDialogConfig = {
      data: {},
      width: '100%',
      maxWidth: '100%',
      height: '100%',
      maxHeight: '100%',
      autoFocus: false
    };
    codeListDialogConfig.width = window.innerWidth + 'px';

    const dialogRef = this.dialog.open(CodelistListDialogComponent, codeListDialogConfig);
    dialogRef.afterClosed().subscribe((codeList: CodeListListEntry) => {
      if (codeList) {
        this.codeListService.getCodeListDetails(codeList.codeListManifestId).subscribe((codeListDetails: CodeListDetails) => {
          this.contextSchemeUpdateRequest.schemeId = codeListDetails.listId.toString();
          this.contextSchemeUpdateRequest.schemeAgencyId = codeListDetails.agencyIdListValue.value.toString();
          this.contextSchemeUpdateRequest.schemeVersionId = codeListDetails.versionId.toString();
          this._updateDataSource([]);
          this._updateDataSource(this.convertCodeListValuesIntoContextSchemeValues(codeListDetails.valueList));
        });
      }
    });
  }

  isDirty(): boolean {
    return this.contextSchemeUpdateRequest.schemeId && this.contextSchemeUpdateRequest.schemeId.length > 0
      || this.contextSchemeUpdateRequest.schemeAgencyId && this.contextSchemeUpdateRequest.schemeAgencyId.length > 0
      || this.contextSchemeUpdateRequest.schemeVersionId && this.contextSchemeUpdateRequest.schemeVersionId.length > 0
      || this.contextSchemeUpdateRequest.contextSchemeValueList && this.contextSchemeUpdateRequest.contextSchemeValueList.length > 0;
  }

}
