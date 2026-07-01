import {Component, OnDestroy, ViewChild, inject} from '@angular/core';
import {MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatTableDataSource} from '@angular/material/table';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatStepper} from '@angular/material/stepper';
import {SelectionModel} from '@angular/cdk/collections';
import {HttpErrorResponse} from '@angular/common/http';
import {saveAs} from 'file-saver';
import {Subscription} from 'rxjs';
import {finalize} from 'rxjs/operators';

import {
  BatchImportResult,
  BusinessTermImportRowRequest,
  ColumnMapping,
  ImportRow,
  MAX_UPLOAD_SIZE_BYTES,
  ParsedFile,
  TargetField,
  UriMode,
} from './business-term-import.model';
import {autoDetect, buildImportRows, isMappingComplete} from './business-term-import.mapping';
import {isValidUri, validateImportRow} from './business-term-import.validation';
import {BusinessTermService} from '../domain/business-term.service';

type ImportPhase = 'upload' | 'map' | 'preview' | 'result';

@Component({
  standalone: false,
  selector: 'score-business-term-import-dialog',
  templateUrl: './business-term-import-dialog.component.html',
  styleUrls: ['./business-term-import-dialog.component.scss'],
})
export class BusinessTermImportDialogComponent implements OnDestroy {
  private service = inject(BusinessTermService);
  private snackBar = inject(MatSnackBar);
  private dialogRef = inject(MatDialogRef<BusinessTermImportDialogComponent>);

  phase: ImportPhase = 'upload';
  readonly phases: { key: ImportPhase; label: string }[] = [
    {key: 'upload', label: 'Upload file'},
    {key: 'map', label: 'Map columns'},
    {key: 'preview', label: 'Review & select'},
    {key: 'result', label: 'Result'},
  ];

  // --- Step 1: file selection ---
  selectedFile: File | null = null;
  fileName = '';
  isDragOver = false;
  parsing = false;
  parseError: string | null = null;
  parsedFile: ParsedFile | null = null;
  selectedSheet: string | null = null;
  readonly maxUploadSizeMb = MAX_UPLOAD_SIZE_BYTES / (1024 * 1024);
  readonly acceptTypes =
    '.csv,.tsv,.xlsx,text/csv,text/tab-separated-values,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';

  // --- Step 2: column mapping ---
  mapping: ColumnMapping | null = null;
  readonly targetFields: { key: TargetField; label: string; required?: boolean }[] = [
    {key: 'businessTerm', label: 'Business Term', required: true},
    {key: 'externalReferenceId', label: 'External Reference ID'},
    {key: 'definition', label: 'Definition'},
    {key: 'comment', label: 'Comment'},
  ];

  // --- Step 3: preview ---
  rows: ImportRow[] = [];
  dataSource = new MatTableDataSource<ImportRow>();
  selection = new SelectionModel<ImportRow>(true, []);
  displayedColumns =
    ['select', 'status', 'businessTerm', 'externalReferenceUri', 'externalReferenceId', 'definition', 'comment'];
  validCount = 0;
  reviewCount = 0;
  selectedCount = 0;

  // --- Step 4: result ---
  importing = false;
  result: BatchImportResult | null = null;
  readonly resultColumns = ['businessTerm', 'externalReferenceUri', 'outcome', 'message'];

  private selectionSub: Subscription;
  private importSub?: Subscription;
  private parseSub?: Subscription;

  @ViewChild(MatPaginator) set matPaginator(paginator: MatPaginator) {
    if (paginator) {
      this.dataSource.paginator = paginator;
    }
  }

  // The stepper may create hidden step content before the review step is selected,
  // so attach the sort through a setter, mirroring the paginator above.
  @ViewChild(MatSort) set matSort(sort: MatSort) {
    if (sort) {
      this.dataSource.sort = sort;
    }
  }

  @ViewChild(MatStepper, {static: true}) private stepper: MatStepper;

  constructor() {
    // Sort case-insensitively on the bound row field (cells are editable text inputs).
    this.dataSource.sortingDataAccessor = (row, columnId) => {
      const value = (row as unknown as Record<string, unknown>)[columnId];
      return typeof value === 'string' ? value.toLowerCase() : (value as string | number);
    };
    this.selectionSub = this.selection.changed.subscribe(() => {
      this.selectedCount = this.selection.selected.length;
    });
  }

  ngOnDestroy() {
    this.selectionSub?.unsubscribe();
    this.importSub?.unsubscribe();
    this.parseSub?.unsubscribe();
  }

  get currentPhaseIndex(): number {
    return this.phases.findIndex(p => p.key === this.phase);
  }

  // ===== Step 1: file selection =====

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.acceptFile(files[0]);
    }
  }

  onFileInput(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files[0];
    // Clear so re-selecting the SAME file still fires `change` (e.g. retry after an error).
    input.value = '';
    if (file) {
      this.acceptFile(file);
    }
  }

  private acceptFile(file: File) {
    const lower = file.name.toLowerCase();
    let rejection: string | null = null;
    if (!(lower.endsWith('.csv') || lower.endsWith('.tsv') || lower.endsWith('.xlsx'))) {
      rejection = 'Unsupported file type. Upload a .csv, .tsv, or .xlsx file.';
    } else if (file.size > MAX_UPLOAD_SIZE_BYTES) {
      rejection = `The file exceeds the ${this.maxUploadSizeMb} MB limit.`;
    }
    if (rejection) {
      // #1754: don't discard an already-valid selection over a bad replacement pick: the tile would
      // keep showing the old file while the inline error described the rejected one (and Next
      // would silently grey out). Keep the current file and report via snackbar; only fall back
      // to the inline error when nothing is selected yet (drop-zone still visible).
      if (this.selectedFile) {
        this.snackBar.open(rejection, '', {duration: 5000});
      } else {
        this.parseError = rejection;
      }
      return;
    }
    this.parseError = null;
    this.selectedFile = file;
    this.fileName = file.name;
    this.parsedFile = null;
    this.selectedSheet = null;
    this.mapping = null;
    this.parseFile();
  }

  private parseFile() {
    if (!this.selectedFile) {
      return;
    }
    const formData = new FormData();
    formData.append('file', this.selectedFile);
    if (this.selectedSheet) {
      formData.append('sheet', this.selectedSheet);
    }
    // Drop any prior in-flight parse (e.g. a fast worksheet switch) so its late response can't
    // clobber the current file's state. Unsubscribe BEFORE flipping `parsing` on: finalize() also
    // runs on unsubscribe, so cancelling a still-in-flight parse after setting parsing=true would
    // immediately reset it to false again — hiding the spinner and re-enabling Next on the stale
    // parsedFile while the new parse is still running (#1754).
    this.parseSub?.unsubscribe();
    this.parsing = true;
    this.parseError = null;
    this.parseSub = this.service.parseFile(formData).pipe(
      finalize(() => this.parsing = false)
    ).subscribe({
      next: (parsed) => {
        this.parsedFile = parsed;
        // Reflect the worksheet the server actually parsed (first non-empty sheet by default) so the
        // sheet picker stays in sync; changing it triggers onSheetChange() -> re-parse.
        this.selectedSheet = parsed.selectedSheet ?? this.selectedSheet;
        if (!parsed.headers || parsed.headers.length === 0) {
          this.parseError = 'No columns were found in the file.';
          this.parsedFile = null;
          return;
        }
        if (!parsed.rows || parsed.rows.length === 0) {
          this.parseError = 'No data rows were found in the file.';
          this.parsedFile = null;
          return;
        }
        // The source format is auto-detected only to drive the column mapping; it is intentionally
        // not surfaced in the UI (the user is asked to verify the mapping instead).
        this.mapping = autoDetect(parsed.headers).mapping;
        // #1754: a valid parse with an unambiguous (single) worksheet advances straight to
        // mapping (no extra "Next" click). Multi-sheet workbooks stay here so the user can pick
        // the worksheet first.
        if (parsed.sheetNames.length <= 1) {
          this.parsing = false;
          this.goToMap();
        }
      },
      error: (err) => {
        this.parseError = this.errorMessage(err, 'The file could not be parsed.');
        this.parsedFile = null;
      },
    });
  }

  onSheetChange() {
    this.parseFile();
  }

  get canProceedFromUpload(): boolean {
    return !!this.parsedFile && !this.parseError && !this.parsing;
  }

  // ----- Selected-file tile (replaces the drop-zone while a file is chosen) -----

  /** Type token used to tint the file tile; defaults to 'csv' for .csv and unknowns. */
  get fileTypeClass(): 'csv' | 'tsv' | 'xlsx' {
    const lower = this.fileName.toLowerCase();
    if (lower.endsWith('.xlsx')) {
      return 'xlsx';
    }
    if (lower.endsWith('.tsv')) {
      return 'tsv';
    }
    return 'csv';
  }

  get fileTypeLabel(): string {
    switch (this.fileTypeClass) {
      case 'xlsx':
        return 'Excel';
      case 'tsv':
        return 'TSV';
      default:
        return 'CSV';
    }
  }

  get fileTypeIcon(): string {
    return this.fileTypeClass === 'xlsx' ? 'grid_on' : 'description';
  }

  /** Remove the chosen file and reset every parse-derived step so the drop-zone returns. */
  removeFile() {
    this.parseSub?.unsubscribe();
    this.selectedFile = null;
    this.fileName = '';
    this.parsing = false;
    this.parseError = null;
    this.parsedFile = null;
    this.selectedSheet = null;
    this.mapping = null;
    // A file may have been removed after stepping into preview; clear that state too.
    this.rows = [];
    this.dataSource.data = [];
    this.selection.clear();
    this.validCount = 0;
    this.reviewCount = 0;
  }

  // ===== Step 2: column mapping =====

  get sourceHeaders(): string[] {
    return this.parsedFile ? this.parsedFile.headers : [];
  }

  setUriMode(mode: UriMode) {
    if (this.mapping) {
      this.mapping.uriMode = mode;
    }
  }

  get uriBaseInvalid(): boolean {
    if (!this.mapping || this.mapping.uriMode !== 'SYNTHESIZE') {
      return false;
    }
    const base = (this.mapping.uriBase || '').trim();
    return base.length > 0 && !isValidUri(base);
  }

  get canProceedFromMapping(): boolean {
    return isMappingComplete(this.mapping);
  }

  // ===== Step 3: preview =====

  private buildPreview() {
    if (!this.parsedFile || !this.mapping) {
      return;
    }
    this.rows = buildImportRows(this.parsedFile.rows, this.mapping);
    this.selection.clear();
    this.revalidateAll();
    this.dataSource.data = this.rows;
  }

  onRowEdited() {
    this.revalidateAll();
  }

  private revalidateAll() {
    const seen = new Set<string>();
    for (const row of this.rows) {
      const errors = validateImportRow(row);
      const uri = row.externalReferenceUri ?? '';
      // Only a row that will actually be submitted (otherwise-valid AND not user-deselected) claims a
      // URI, so neither an invalid row nor a deselected row falsely flags a later row as a duplicate.
      // This mirrors the server, which dedupes only the rows it receives.
      if (errors.length === 0 && uri.length > 0 && !row.userDeselected) {
        if (seen.has(uri)) {
          errors.push('Duplicate external reference URI in this import.');
        } else {
          seen.add(uri);
        }
      }
      row.errors = errors;
      if (errors.length > 0) {
        this.selection.deselect(row);
      } else if (!row.userDeselected) {
        this.selection.select(row);
      }
    }
    this.validCount = this.rows.filter(r => r.errors.length === 0).length;
    this.reviewCount = this.rows.length - this.validCount;
  }

  isRowInvalid(row: ImportRow): boolean {
    return row.errors.length > 0;
  }

  rowTooltip(row: ImportRow): string {
    return row.errors.join('\n');
  }

  toggleRow(row: ImportRow) {
    if (this.selection.isSelected(row)) {
      this.selection.deselect(row);
      row.userDeselected = true;
    } else if (row.errors.length === 0) {
      this.selection.select(row);
      row.userDeselected = false;
    }
    // Re-validate so releasing/claiming a duplicate URI updates the other rows' flags immediately.
    this.revalidateAll();
  }

  isAllValidSelected(): boolean {
    const valid = this.rows.filter(r => r.errors.length === 0);
    return valid.length > 0 && valid.every(r => this.selection.isSelected(r));
  }

  masterToggle() {
    const valid = this.rows.filter(r => r.errors.length === 0);
    if (this.isAllValidSelected()) {
      valid.forEach(r => {
        this.selection.deselect(r);
        r.userDeselected = true;
      });
    } else {
      valid.forEach(r => {
        this.selection.select(r);
        r.userDeselected = false;
      });
    }
    // Re-validate so intra-batch duplicate flags reflect the new selection state.
    this.revalidateAll();
  }

  trackByRowId(_index: number, row: ImportRow): number {
    return row.id;
  }

  // ===== Navigation =====

  goToMap() {
    if (this.canProceedFromUpload) {
      this.stepForward();
    }
  }

  backToUpload() {
    this.stepBack();
  }

  goToPreview() {
    if (this.canProceedFromMapping) {
      this.stepForward();
    }
  }

  backToMap() {
    this.stepBack();
  }

  isStepEditable(index: number): boolean {
    return !this.result && index < this.currentPhaseIndex;
  }

  onStepSelectionChange(index: number) {
    const targetPhase = this.phases[index]?.key;
    if (!targetPhase || (this.result && targetPhase !== 'result')) {
      return;
    }

    this.activatePhase(targetPhase);
  }

  private stepForward() {
    const selectedStep = this.stepper.selected;
    if (selectedStep) {
      selectedStep.completed = true;
    }
    this.stepper.next();
    this.activateCurrentStepperPhase();
  }

  private stepBack() {
    this.stepper.previous();
    this.activateCurrentStepperPhase();
  }

  private activateCurrentStepperPhase() {
    const targetPhase = this.phases[this.stepper.selectedIndex]?.key;
    if (targetPhase) {
      this.activatePhase(targetPhase);
    }
  }

  private activatePhase(phase: ImportPhase) {
    if (phase === 'preview' && this.phase !== 'preview') {
      this.buildPreview();
    }
    this.phase = phase;
  }

  // ===== Step 3 -> import =====

  importSelected() {
    const selected = this.selection.selected;
    if (selected.length === 0) {
      return;
    }
    const rows: BusinessTermImportRowRequest[] = selected
      .slice()
      .sort((a, b) => a.rowIndex - b.rowIndex)
      .map(r => ({
        rowIndex: r.rowIndex,
        businessTerm: r.businessTerm,
        externalReferenceId: r.externalReferenceId,
        externalReferenceUri: r.externalReferenceUri,
        definition: r.definition,
        comment: r.comment,
      }));

    this.importing = true;
    this.importSub = this.service.importBatch(rows).pipe(
      finalize(() => this.importing = false)
    ).subscribe({
      next: (result) => {
        this.result = result;
        this.stepForward();
      },
      error: (err) => {
        this.snackBar.open(this.errorMessage(err, 'The import failed.'), '', {duration: 5000});
      },
    });
  }

  get importedCount(): number {
    return this.result ? this.result.createdCount + this.result.updatedCount : 0;
  }

  // ===== Misc =====

  downloadTemplate() {
    this.service.downloadCSV().subscribe((buffer) => {
      const data: Blob = new Blob([buffer], {type: 'text/csv;charset=utf-8'});
      saveAs(data, 'businessTermTemplateWithExample.csv');
    });
  }

  close() {
    // Cancelling mid-flight (via the top-right X, Cancel, ESC, or backdrop) must abort any in-flight
    // parse or import so a late response can't act on a disposed dialog.
    this.parseSub?.unsubscribe();
    this.importSub?.unsubscribe();
    this.dialogRef.close(this.result ?? undefined);
  }

  private errorMessage(err: unknown, fallback: string): string {
    if (err instanceof HttpErrorResponse) {
      const header = err.headers?.get('X-Error-Message');
      if (header) {
        return header;
      }
      if (typeof err.error === 'string' && err.error) {
        return err.error;
      }
    }
    return fallback;
  }
}
