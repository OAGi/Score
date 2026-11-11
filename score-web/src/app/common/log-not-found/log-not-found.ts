import {HttpContext, HttpParams} from '@angular/common/http';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MonoTypeOperatorFunction, Observable, throwError} from 'rxjs';
import {catchError, switchMap} from 'rxjs/operators';
import {LOG_NOT_FOUND_HANDLED} from '../../authentication/auth.service';
import {ConfirmDialogComponent} from '../confirm-dialog/confirm-dialog.component';
import {ConfirmDialogConfig} from '../confirm-dialog/confirm-dialog.domain';

/**
 * The history (log) a revision goes on from, or a cancel goes back to, can be missing, which only happens when it was
 * removed from the database. Every component that is revised and cancelled (core components, data types, code lists,
 * agency ID lists) asks the user the same way, and repeats the request with the permission to write a new history.
 */

/** The options of the request that revises or cancels: the permission, and that the caller asks the user itself. */
export function logOptions(recreateLog: boolean) {
  return {
    params: recreateLog ? new HttpParams().set('recreateLog', 'true') : new HttpParams(),
    context: new HttpContext().set(LOG_NOT_FOUND_HANDLED, true)
  };
}

/**
 * When the server says that the history cannot be found, the user is asked whether a new history may be written; if
 * so, the request is repeated by {@code again} with that permission. If not, the request fails as it did.
 */
export function askToWriteNewLog<T>(dialog: MatDialog, again: () => Observable<T>): MonoTypeOperatorFunction<T> {
  return catchError(error => {
    if (error?.headers?.get('x-error-code') !== 'LOG_NOT_FOUND') {
      return throwError(() => error);
    }
    const config = new MatDialogConfig();
    config.panelClass = ['confirm-dialog'];
    config.autoFocus = false;
    config.data = new ConfirmDialogConfig();
    config.data.header = 'Log information not found';
    config.data.content = [
      'The log information of this component cannot be found.',
      'Do you want to create a new log and continue?'];
    config.data.action = 'Create and continue';
    return dialog.open(ConfirmDialogComponent, config).afterClosed().pipe(
      switchMap(result => result ? again() : throwError(() => error)));
  });
}
