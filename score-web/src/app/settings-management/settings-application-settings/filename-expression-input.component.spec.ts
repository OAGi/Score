import {vi} from 'vitest';
import {FilenameExpressionInputComponent} from './filename-expression-input.component';

describe('FilenameExpressionInputComponent', () => {
  const proto = FilenameExpressionInputComponent.prototype as any;

  function input(text: string, caret: number, active = true): any {
    const element: any = {value: text, selectionStart: caret, setSelectionRange: vi.fn()};
    return {element, active};
  }

  function ctx(text = '', caret = text.length, overrides: any = {}): any {
    const {element} = input(text, caret);
    return {
      field: {nativeElement: element},
      candidates: [],
      selectedAt: 0,
      autocomplete: {openPanel: vi.fn(), closePanel: vi.fn()},
      cdr: {markForCheck: vi.fn()},
      valueChange: {emit: vi.fn()},
      request$: {next: vi.fn()},
      value: '',
      onValueChange: proto.onValueChange,
      requestSuggestions: proto.requestSuggestions,
      show: proto.show,
      ...overrides
    };
  }

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  it('asks for the suggestions at the caret, not at the end of the text', () => {
    const c = ctx('{BIE Pro}', 8);

    proto.requestSuggestions.call(c);

    expect(c.request$.next).toHaveBeenCalledWith({text: '{BIE Pro}', caret: 8});
  });

  it('turns each suggestion into the whole expression it leads to, with the caret after what was inserted', () => {
    const c = ctx('a{BIE Pro}b', 9);
    vi.spyOn(document, 'activeElement', 'get').mockReturnValue(c.field.nativeElement);

    proto.show.call(c, {
      from: 2, to: 9,
      items: [
        {label: 'BIE Property Term', insert: 'BIE Property Term', detail: 'd1', caretBack: 0},
        {label: 'separator(\'\')', insert: 'separator(\'\')', detail: 'd2', caretBack: 2}
      ]
    });

    expect(c.candidates[0]).toEqual({
      label: 'BIE Property Term', detail: 'd1',
      expression: 'a{BIE Property Term}b', caret: 'a{BIE Property Term'.length
    });
    expect(c.candidates[1].caret).toBe('a{separator(\''.length);
    expect(c.autocomplete.openPanel).toHaveBeenCalled();
  });

  it('shows nothing when the field has lost the focus before the answer came', () => {
    const c = ctx('{');
    vi.spyOn(document, 'activeElement', 'get').mockReturnValue(document.body);

    proto.show.call(c, {from: 1, to: 1, items: [{label: 'x', insert: 'x', detail: '', caretBack: 0}]});

    expect(c.candidates).toEqual([]);
    expect(c.autocomplete.openPanel).not.toHaveBeenCalled();
  });

  it('does not open the panel for an answer without suggestions', () => {
    const c = ctx('abc');
    vi.spyOn(document, 'activeElement', 'get').mockReturnValue(c.field.nativeElement);

    proto.show.call(c, {from: 0, to: 0, items: []});

    expect(c.candidates).toEqual([]);
    expect(c.autocomplete.openPanel).not.toHaveBeenCalled();
  });

  it('emits the new value as the user types or chooses', () => {
    const c = ctx();

    proto.onValueChange.call(c, '{BIE ID}');

    expect(c.value).toBe('{BIE ID}');
    expect(c.valueChange.emit).toHaveBeenCalledWith('{BIE ID}');
  });

  it('puts the caret where the suggestion says and asks for what follows after one is chosen', () => {
    vi.useFakeTimers();
    const c = ctx('{BIE Version:');
    c.candidates = [{label: 'separator(\'\')', detail: '', expression: '{BIE Version:separator(\'\')', caret: 25}];

    proto.onSelected.call(c, {option: {value: '{BIE Version:separator(\'\')'}});
    expect(c.valueChange.emit).toHaveBeenCalledWith('{BIE Version:separator(\'\')');
    vi.runAllTimers();

    expect(c.field.nativeElement.setSelectionRange).toHaveBeenCalledWith(25, 25);
    expect(c.request$.next).toHaveBeenCalled();
  });

  it('keeps the Enter that chose a suggestion from also adding a line break', () => {
    const c = ctx();
    const event = {preventDefault: vi.fn()};

    proto.onEnter.call({selectedAt: Date.now()}, event);
    expect(event.preventDefault).toHaveBeenCalledTimes(1);

    proto.onEnter.call({selectedAt: Date.now() - 1000}, event);
    expect(event.preventDefault).toHaveBeenCalledTimes(1);
    expect(c).toBeDefined();
  });

  it('puts the suggestions away once the focus is elsewhere', () => {
    vi.useFakeTimers();
    const c = ctx('x');
    c.candidates = [{label: 'a', detail: '', expression: 'a', caret: 1}];
    vi.spyOn(document, 'activeElement', 'get').mockReturnValue(document.body);

    proto.onBlur.call(c);
    vi.advanceTimersByTime(200);

    expect(c.candidates).toEqual([]);
    expect(c.autocomplete.closePanel).toHaveBeenCalled();
  });

  it('keeps the suggestions while the focus stays in the field (a click on a suggestion)', () => {
    vi.useFakeTimers();
    const c = ctx('x');
    c.candidates = [{label: 'a', detail: '', expression: 'a', caret: 1}];
    vi.spyOn(document, 'activeElement', 'get').mockReturnValue(c.field.nativeElement);

    proto.onBlur.call(c);
    vi.advanceTimersByTime(200);

    expect(c.candidates.length).toBe(1);
  });
});
