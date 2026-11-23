import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  forwardRef,
  inject,
  input,
  OnDestroy,
  viewChild
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';
import {MatIconButtonSizesModule} from 'mat-icon-button-sizes';
import {Compartment, EditorState, Extension} from '@codemirror/state';
import {EditorView, keymap, lineNumbers, placeholder} from '@codemirror/view';
import {defaultKeymap, history, historyKeymap, indentLess, insertTab} from '@codemirror/commands';
import {bracketMatching, defaultHighlightStyle, indentOnInput, syntaxHighlighting} from '@codemirror/language';
import {closeBrackets, closeBracketsKeymap} from '@codemirror/autocomplete';
import {json, jsonParseLinter} from '@codemirror/lang-json';
import {linter, lintGutter} from '@codemirror/lint';
import {formatJson, JsonFormatResult, minifyJson} from './json-format';

const jsonLint = jsonParseLinter();

/**
 * An editor of JSON text, for a form: it works with `ngModel` and reactive forms, the value being the text.
 * It colors the JSON, closes brackets and quotes, indents, tells a mistake where it is (under the text and in the
 * gutter), and formats the text (Shift+Alt+F) or puts it on one line.
 *
 * ```html
 * <score-json-editor label="Schema (JSON)" [(ngModel)]="schemaText" [minLines]="6"/>
 * ```
 *
 * It does not decide what a valid value is: a blank text, or a JSON of another kind than the field takes, is for the form
 * to tell. The editor only tells whether the text is JSON.
 */
@Component({
  selector: 'score-json-editor',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatIconButton, MatIconButtonSizesModule, MatIcon, MatTooltip],
  templateUrl: './json-editor.component.html',
  styleUrl: './json-editor.component.css',
  providers: [{provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => JsonEditorComponent), multi: true}]
})
export class JsonEditorComponent implements ControlValueAccessor, AfterViewInit, OnDestroy {
  private cdr = inject(ChangeDetectorRef);

  readonly label = input<string>('');
  readonly placeholder = input<string>('');
  /** The lines the editor has at least, and the lines it grows to before it scrolls. */
  readonly minLines = input(4);
  readonly maxLines = input(16);
  readonly lineNumbers = input(true);

  private readonly host = viewChild.required<ElementRef<HTMLElement>>('host');

  private view?: EditorView;
  /** The editor can be disabled after it was made: what makes it not editable is a compartment. */
  private readonly editable = new Compartment();
  /** The form is writing a value: the editor shows it, and does not tell the form that it was changed. */
  private writing = false;
  private value = '';
  disabled = false;
  focused = false;
  /** What the editor says about the text: that it is JSON, or where it is not. */
  status?: { text: string; error: boolean };

  private onChange: (value: string) => void = () => {
  };
  private onTouched: () => void = () => {
  };

  ngAfterViewInit(): void {
    this.view = new EditorView({
      parent: this.host().nativeElement,
      state: EditorState.create({doc: this.value, extensions: this.extensions()})
    });
    this.refreshStatus();
  }

  ngOnDestroy(): void {
    this.view?.destroy();
  }

  private extensions(): Extension[] {
    const lineHeight = 18;
    const extensions: Extension[] = [
      this.editable.of(this.editableExtensions()),
      history(),
      indentOnInput(),
      bracketMatching(),
      closeBrackets(),
      syntaxHighlighting(defaultHighlightStyle),
      json(),
      // an empty editor is not a mistake: the form tells whether a value is required
      linter(view => (view.state.doc.toString().trim() ? jsonLint(view) : [])),
      lintGutter(),
      EditorView.lineWrapping,
      EditorState.tabSize.of(2),
      EditorView.theme({
        '.cm-content': {minHeight: this.minLines() * lineHeight + 'px'},
        '.cm-scroller': {maxHeight: this.maxLines() * lineHeight + 'px', overflow: 'auto', lineHeight: lineHeight + 'px'}
      }),
      keymap.of([
        {key: 'Shift-Alt-f', run: () => (this.format(), true)},
        // Tab is a tab character of the text, as in an editor of code; Ctrl-m (Alt-Shift-m on macOS), or Esc and then Tab,
        // leaves the editor for the next control
        {key: 'Tab', run: insertTab, shift: indentLess},
        ...closeBracketsKeymap,
        ...defaultKeymap,
        ...historyKeymap
      ]),
      EditorView.updateListener.of(update => {
        if (update.docChanged) {
          this.value = update.state.doc.toString();
          if (!this.writing) {
            this.onChange(this.value);
          }
          this.refreshStatus();
        }
        if (update.focusChanged) {
          this.focused = update.view.hasFocus;
          if (!this.focused) {
            this.onTouched();
          }
          this.cdr.markForCheck();
        }
      })
    ];
    if (this.lineNumbers()) {
      extensions.push(lineNumbers());
    }
    if (this.placeholder()) {
      extensions.push(placeholder(this.placeholder()));
    }
    return extensions;
  }

  /** The text of the editor. */
  get text(): string {
    return this.view ? this.view.state.doc.toString() : this.value;
  }

  private replaceText(text: string) {
    if (!this.view || text === this.text) {
      return;
    }
    this.view.dispatch({changes: {from: 0, to: this.view.state.doc.length, insert: text}});
  }

  /** Indent the text, if it is JSON; if it is not, say why and leave it as it is. */
  format(): void {
    this.apply(formatJson(this.text));
  }

  /** Put the text on one line, if it is JSON. */
  minify(): void {
    this.apply(minifyJson(this.text));
  }

  private apply(result: JsonFormatResult) {
    if (result.error !== undefined) {
      this.status = {text: result.error, error: true};
      this.cdr.markForCheck();
      return;
    }
    this.replaceText(result.text ?? '');
    this.refreshStatus();
  }

  private refreshStatus() {
    const text = this.text;
    if (!text.trim()) {
      this.status = undefined;
    } else {
      try {
        JSON.parse(text);
        this.status = {text: 'Valid JSON', error: false};
      } catch (e) {
        this.status = {text: (e as Error).message, error: true};
      }
    }
    this.cdr.markForCheck();
  }

  private editableExtensions(): Extension {
    return [EditorView.editable.of(!this.disabled), EditorState.readOnly.of(this.disabled)];
  }

  // ControlValueAccessor

  writeValue(value: string | null | undefined): void {
    this.value = value ?? '';
    this.writing = true;
    try {
      this.replaceText(this.value);
    } finally {
      this.writing = false;
    }
    this.refreshStatus();
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(disabled: boolean): void {
    this.disabled = disabled;
    this.view?.dispatch({effects: this.editable.reconfigure(this.editableExtensions())});
    this.cdr.markForCheck();
  }
}
