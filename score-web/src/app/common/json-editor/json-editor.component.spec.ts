import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule} from '@angular/forms';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {diagnosticCount, forceLinting} from '@codemirror/lint';
import {JsonEditorComponent} from './json-editor.component';

// CodeMirror measures text with ranges, which jsdom does not lay out
const rangeProto = Range.prototype as any;
if (!rangeProto.getClientRects) {
  rangeProto.getClientRects = () => ({length: 0, item: () => null, [Symbol.iterator]: function* () {
  }});
}
if (!rangeProto.getBoundingClientRect) {
  rangeProto.getBoundingClientRect = () => ({x: 0, y: 0, width: 0, height: 0, top: 0, left: 0, right: 0, bottom: 0});
}

@Component({
  standalone: true,
  imports: [JsonEditorComponent, FormsModule],
  template: `<score-json-editor label="Schema (JSON)" [ngModel]="text()" (ngModelChange)="text.set($event)" placeholder="{}"/>`
})
class HostComponent {
  text = signal<string>('{"type":"string"}');
}

/** The editor of JSON text works as a form field: the value is the text. */
describe('JsonEditorComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;
  let editor: JsonEditorComponent;

  /** ngModel writes the value of the model to its control in a later task. */
  async function settle() {
    fixture.detectChanges();
    await new Promise(resolve => setTimeout(resolve));
    fixture.detectChanges();
  }

  async function setUp() {
    TestBed.configureTestingModule({imports: [HostComponent, NoopAnimationsModule]});
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable(); // ngModel writes its first value asynchronously
    fixture.detectChanges();
    editor = fixture.debugElement.children[0].componentInstance;
  }

  /** What the user does: replace the text of the editor. */
  function type(text: string) {
    const view = (editor as any).view;
    view.dispatch({changes: {from: 0, to: view.state.doc.length, insert: text}});
    fixture.detectChanges();
  }

  beforeEach(setUp);

  it('shows the value of the model', () => {
    expect(editor.text).toBe('{"type":"string"}');
    expect(fixture.nativeElement.querySelector('.cm-content').textContent).toContain('"type"');
  });

  it('shows the label', () => {
    expect(fixture.nativeElement.querySelector('.json-editor-label').textContent).toContain('Schema (JSON)');
  });

  it('gives the model what is typed', () => {
    type('{"a":1}');
    expect(host.text()).toBe('{"a":1}');
  });

  it('shows a value written by the model, and does not tell the model about it', async () => {
    host.text.set('[1]');
    await settle();

    expect(editor.text).toBe('[1]');
    expect(host.text()).toBe('[1]');
  });

  it('takes null and undefined for a blank text', async () => {
    host.text.set(null as any);
    await settle();
    expect(editor.text).toBe('');
  });

  it('says that the text is JSON, or why it is not, and says nothing for a blank one', () => {
    expect(editor.status).toEqual({text: 'Valid JSON', error: false});
    type('{"a":');
    expect(editor.status?.error).toBe(true);
    type('   ');
    expect(editor.status).toBeUndefined();
  });

  it('marks a text that is not JSON, and an empty editor is not a mistake', async () => {
    const view = (editor as any).view;
    const lint = async () => {
      forceLinting(view);
      await new Promise(resolve => setTimeout(resolve)); // the linter answers in a later task
      return diagnosticCount(view.state);
    };
    expect(await lint()).toBe(0);
    type('{"a":');
    expect(await lint()).toBeGreaterThan(0);
    type('');
    expect(await lint()).toBe(0);
    type('{"a":');
    expect(await lint()).toBeGreaterThan(0);
    type('  \n ');
    expect(await lint()).toBe(0);
  });

  /** A key of the keyboard, as the editor receives it. */
  function press(key: string, keyCode: number, shiftKey = false) {
    const view = (editor as any).view;
    view.contentDOM.dispatchEvent(new KeyboardEvent('keydown', {key, keyCode, shiftKey, bubbles: true, cancelable: true}));
  }

  it('puts a tab character in the text when Tab is pressed, and keeps the focus', () => {
    const view = (editor as any).view;
    view.dispatch({changes: {from: 0, to: view.state.doc.length, insert: 'ab'}, selection: {anchor: 1}});
    press('Tab', 9);

    expect(editor.text).toBe('a\tb');
    expect(host.text()).toBe('a\tb');
  });

  it('indents less with Shift+Tab', () => {
    const view = (editor as any).view;
    view.dispatch({changes: {from: 0, to: view.state.doc.length, insert: '    x'}, selection: {anchor: 5}});
    press('Tab', 9, true);

    expect(editor.text.length).toBeLessThan(5);
  });

  it('formats the text, which the model receives', () => {
    type('{"a":[1,2]}');
    editor.format();

    expect(host.text()).toBe('{\n  "a": [\n    1,\n    2\n  ]\n}');
  });

  it('puts the text on one line', () => {
    type('{\n  "a": 1\n}');
    editor.minify();
    expect(host.text()).toBe('{"a":1}');
  });

  it('leaves a text that is not JSON as it is when it is asked to format, and says why', () => {
    type('{"a":');
    editor.format();

    expect(host.text()).toBe('{"a":');
    expect(editor.status?.error).toBe(true);
  });

  it('formats with its button', () => {
    type('{"a":1}');
    const buttons: HTMLButtonElement[] = Array.from(fixture.nativeElement.querySelectorAll('.json-editor-action'));
    buttons[0].click();
    expect(host.text()).toBe('{\n  "a": 1\n}');
  });

  it('cannot be edited, nor formatted, while disabled', async () => {
    editor.setDisabledState(true);
    fixture.detectChanges();

    const view = (editor as any).view;
    expect(view.state.readOnly).toBe(true);
    expect(view.contentDOM.getAttribute('contenteditable')).toBe('false');
    const buttons: HTMLButtonElement[] = Array.from(fixture.nativeElement.querySelectorAll('.json-editor-action'));
    expect(buttons.every(b => b.disabled)).toBe(true);

    editor.setDisabledState(false);
    expect(view.state.readOnly).toBe(false);
  });

  it('frees the editor when it is destroyed', () => {
    const view = (editor as any).view;
    const destroy = vi.spyOn(view, 'destroy');
    fixture.destroy();
    expect(destroy).toHaveBeenCalled();
  });
});
