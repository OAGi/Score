import {ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, EventEmitter, inject, Input, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {TextFieldModule} from '@angular/cdk/text-field';
import {FormsModule} from '@angular/forms';
import {MatAutocompleteModule, MatAutocompleteSelectedEvent, MatAutocompleteTrigger} from '@angular/material/autocomplete';
import {EMPTY, Subject} from 'rxjs';
import {catchError, debounceTime, switchMap, takeUntil} from 'rxjs/operators';
import {FilenameExpressionSuggestions, SettingsApplicationSettingsService} from './domain/settings-application-settings.service';

interface Candidate {
  label: string;
  detail: string;
  /** The whole expression that choosing it leads to. */
  expression: string;
  caret: number;
}

/**
 * A filename expression field that suggests what may come at the caret. It does not know the grammar: the backend,
 * which also validates the expression, answers with the completions, so the two cannot disagree.
 */
@Component({
  selector: 'score-filename-expression-input',
  templateUrl: './filename-expression-input.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [FormsModule, MatAutocompleteModule, TextFieldModule]
})
export class FilenameExpressionInputComponent implements OnInit, OnDestroy {

  @Input() type: 'bie-schema' | 'bie-package-schema' = 'bie-schema';
  /** Whether the text is the duplicate handler expression, which has its own, smaller set of placeholders. */
  @Input() duplicateHandler = false;
  @Input() minRows = 3;
  @Input() maxRows = 8;
  @Input() invalid = false;
  @Input() value = '';
  @Output() valueChange = new EventEmitter<string>();

  @ViewChild('field') field: ElementRef<HTMLTextAreaElement> | undefined;
  @ViewChild(MatAutocompleteTrigger) autocomplete: MatAutocompleteTrigger | undefined;

  candidates: Candidate[] = [];
  private selectedAt = 0;
  private readonly settingsService = inject(SettingsApplicationSettingsService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly request$ = new Subject<{ text: string, caret: number }>();
  private readonly destroy$ = new Subject<void>();

  ngOnInit() {
    this.request$.pipe(
      debounceTime(150),
      // a newer request supersedes the one in flight, so a late answer never overwrites the suggestions for the caret now
      switchMap(({text, caret}) => this.settingsService
        .suggestFilenameExpression(this.type, text, caret, this.duplicateHandler)
        .pipe(catchError(() => {
          this.candidates = [];
          return EMPTY;
        }))),
      takeUntil(this.destroy$)
    ).subscribe(suggestions => this.show(suggestions));
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onValueChange(value: string) {
    this.value = value;
    this.valueChange.emit(value);
  }

  /** Asks the backend for the suggestions at the caret; call it whenever the text or the caret moved. */
  requestSuggestions() {
    const input = this.field?.nativeElement;
    if (input) {
      this.request$.next({text: input.value, caret: input.selectionStart ?? input.value.length});
    }
  }

  private show(suggestions: FilenameExpressionSuggestions) {
    const input = this.field?.nativeElement;
    if (!input || document.activeElement !== input) {
      return;
    }
    const text = input.value;
    this.candidates = suggestions.items.map(item => ({
      label: item.label,
      detail: item.detail,
      expression: text.substring(0, suggestions.from) + item.insert + text.substring(suggestions.to),
      caret: suggestions.from + item.insert.length - item.caretBack
    }));
    if (this.candidates.length > 0) {
      this.autocomplete?.openPanel();
    }
    this.cdr.markForCheck();
  }

  onSelected(event: MatAutocompleteSelectedEvent) {
    this.selectedAt = Date.now();
    const chosen = this.candidates.find(c => c.expression === event.option.value);
    this.onValueChange(event.option.value);
    // the autocomplete has put the expression in the field; the caret goes after what was inserted, and the next
    // suggestions follow at once
    setTimeout(() => {
      const input = this.field?.nativeElement;
      if (chosen && input) {
        input.setSelectionRange(chosen.caret, chosen.caret);
      }
      this.requestSuggestions();
    });
  }

  /** Enter that only chose a suggestion must not also add a line break. */
  onEnter(event: Event) {
    if (Date.now() - this.selectedAt < 300) {
      event.preventDefault();
    }
  }

  onBlur() {
    setTimeout(() => {
      if (document.activeElement !== this.field?.nativeElement) {
        this.candidates = [];
        this.autocomplete?.closePanel();
        this.cdr.markForCheck();
      }
    }, 150);
  }
}
