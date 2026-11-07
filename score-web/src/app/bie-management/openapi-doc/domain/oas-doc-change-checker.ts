import {signal} from '@angular/core';
import {catchError, debounceTime, EMPTY, map, Observable, Subject, Subscription, switchMap} from 'rxjs';
import {OasDocChangeResult, OasDocChangeSet, OasDocViolation} from './oas-doc-change';

/**
 * - `idle`: nothing is changed, so there is nothing to check;
 * - `checking`: something was changed and the server has not answered yet, so the change cannot be saved yet;
 * - `valid`: the server says that what is changed can be saved;
 * - `invalid`: it says that it cannot, and `violations` says why and where;
 * - `failed`: the server could not be asked, so nothing is known.
 */
export type OasDocChangeStatus = 'idle' | 'checking' | 'valid' | 'invalid' | 'failed';

export interface OasDocChangeCheckerOptions {
  /** What the user has changed, or `null` when nothing. */
  build: () => OasDocChangeSet | null;
  /** Asks the server. */
  check: (changeSet: OasDocChangeSet) => Observable<OasDocChangeResult>;
  /**
   * Takes the answer in. It is `false` when the page has changed since the question was asked (the answer is of what it was
   * before), and then the answer is not taken: another question is on its way.
   */
  onResult: (result: OasDocChangeResult, changeSet: OasDocChangeSet) => boolean;
  /** How long the user has to pause before the server is asked. */
  debounceMs?: number;
}

/**
 * Asks the server, whenever the user has changed something, whether it can be saved, and keeps the answer. The page holds no
 * rule: Update is there when the answer is `valid`, and what the server says is wrong is shown where it is.
 */
export class OasDocChangeChecker {
  readonly status = signal<OasDocChangeStatus>('idle');
  readonly violations = signal<OasDocViolation[]>([]);

  private readonly requests = new Subject<void>();
  private readonly subscription: Subscription;
  /** Raised by `reset`, so that an answer to a question of before is not taken. */
  private epoch = 0;

  constructor(private readonly options: OasDocChangeCheckerOptions) {
    this.subscription = this.requests.pipe(
      debounceTime(options.debounceMs ?? 400),
      switchMap(() => this.ask())
    ).subscribe();
  }

  /** Something was changed: it cannot be saved until the server has said that it can. */
  request(): void {
    this.status.set('checking');
    this.requests.next();
  }

  /** The server refused to save what was checked (it changed since): it is as if the check had said so. */
  refuse(violations: OasDocViolation[]): void {
    this.status.set('invalid');
    this.violations.set(violations);
  }

  /** The page is loaded or saved: there is no change, and no answer to wait for. */
  reset(): void {
    this.epoch++;
    this.status.set('idle');
    this.violations.set([]);
  }

  destroy(): void {
    this.subscription.unsubscribe();
  }

  private ask(): Observable<unknown> {
    const changeSet = this.options.build();
    if (!changeSet) {
      this.status.set('idle');
      this.violations.set([]);
      return EMPTY;
    }
    const epoch = this.epoch;
    return this.options.check(changeSet).pipe(
      map(result => {
        if (epoch === this.epoch && this.options.onResult(result, changeSet)) {
          this.status.set(result.valid ? 'valid' : 'invalid');
          this.violations.set(result.violations || []);
        }
      }),
      catchError(() => {
        if (epoch === this.epoch) {
          this.status.set('failed');
        }
        return EMPTY;
      })
    );
  }
}
