import {vi} from 'vitest';
import {of, Subject, throwError} from 'rxjs';
import {OasDocChangeChecker} from './oas-doc-change-checker';
import {OasDocChangeResult, OasDocChangeSet} from './oas-doc-change';

describe('OasDocChangeChecker', () => {
  const changeSet: OasDocChangeSet = {operations: [], headers: {upserts: [], removed: []}};
  const valid: OasDocChangeResult = {valid: true, violations: [], httpHeaders: []};
  const invalid: OasDocChangeResult = {
    valid: false, httpHeaders: [], violations: [{scope: 'OPERATION', oasOperationId: 7, field: 'operationId', message: 'used'}]
  };

  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  function checker(overrides: Partial<ConstructorParameters<typeof OasDocChangeChecker>[0]> = {}) {
    const check = vi.fn(() => of(valid));
    const onResult = vi.fn(() => true);
    const c = new OasDocChangeChecker({build: () => changeSet, check, onResult, debounceMs: 100, ...overrides});
    return {c, check, onResult};
  }

  it('cannot be saved from the moment something is changed until the server says that it can', () => {
    const {c} = checker();

    expect(c.status()).toBe('idle');
    c.request();
    expect(c.status()).toBe('checking');
    vi.advanceTimersByTime(100);
    expect(c.status()).toBe('valid');
  });

  it('asks once for a burst of changes, with what the page is then', () => {
    const {c, check} = checker();

    c.request();
    vi.advanceTimersByTime(50);
    c.request();
    vi.advanceTimersByTime(50);
    expect(check).not.toHaveBeenCalled();
    vi.advanceTimersByTime(50);

    expect(check).toHaveBeenCalledTimes(1);
  });

  it('keeps what the server says is wrong, and where', () => {
    const {c} = checker({check: () => of(invalid)});

    c.request();
    vi.advanceTimersByTime(100);

    expect(c.status()).toBe('invalid');
    expect(c.violations()[0].oasOperationId).toBe(7);
  });

  it('is idle when there is nothing changed, and does not ask', () => {
    const {c, check} = checker({build: () => null});

    c.request();
    vi.advanceTimersByTime(100);

    expect(c.status()).toBe('idle');
    expect(check).not.toHaveBeenCalled();
  });

  it('does not take an answer that the page says is of what it was before', () => {
    const {c} = checker({onResult: () => false});

    c.request();
    vi.advanceTimersByTime(100);

    expect(c.status()).toBe('checking'); // another question is on its way
  });

  it('does not take the answer to a question of before a reset', () => {
    const answer = new Subject<OasDocChangeResult>();
    const {c} = checker({check: () => answer});
    c.request();
    vi.advanceTimersByTime(100);

    c.reset();
    answer.next(valid);

    expect(c.status()).toBe('idle');
  });

  it('knows nothing when the server cannot be asked', () => {
    const {c} = checker({check: () => throwError(() => new Error('down'))});

    c.request();
    vi.advanceTimersByTime(100);

    expect(c.status()).toBe('failed');
  });

  it('forgets the violations when the page is saved or loaded', () => {
    const {c} = checker({check: () => of(invalid)});
    c.request();
    vi.advanceTimersByTime(100);

    c.reset();

    expect(c.violations()).toEqual([]);
  });
});
