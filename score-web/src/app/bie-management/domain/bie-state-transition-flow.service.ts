import { Injectable, inject } from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable, of} from 'rxjs';
import {catchError, map, switchMap} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {
  BieStateDependencyDialogComponent,
  BieStateDependencyDialogData
} from '../bie-state-dependency-dialog/bie-state-dependency-dialog.component';
import {StateDependencySelection, StateDependencyTarget} from './state-dependency-target';

/**
 * Request contract for the shared state transition dialog workflow.
 *
 * <p>Both the single-BIE edit page and the bulk BIE list page follow the same
 * sequence: load dependency rows, show the simple confirm dialog when no rows
 * are present, otherwise open the dependency dialog and return the dependency
 * rows selected by the user.</p>
 */
export interface BieStateTransitionFlowRequest {
  state: string;
  rootTopLevelAsbiepIds: number[];
  loadDependencies: () => Observable<StateDependencyTarget[]>;
  validateSelection: (selection: StateDependencySelection) => Observable<StateDependencyTarget[]>;
  normalizeTargets?: (targets: StateDependencyTarget[]) => StateDependencyTarget[];
  confirmationHeader?: string;
  confirmationContent?: string[];
  confirmationAction?: string;
}

/**
 * Shared UI workflow for BIE state transition confirmation and dependency
 * selection.
 */
@Injectable({
  providedIn: 'root'
})
export class BieStateTransitionFlowService {
  private dialog = inject(MatDialog);
  private confirmDialogService = inject(ConfirmDialogService);

  /**
   * Resolves the dependency selection required for a state transition.
   *
   * <p>The initial dialog load uses the same validation endpoint as checkbox
   * changes so the UI receives one normalized dependency snapshot for the
   * current root selection. When no dependency rows are returned, this falls
   * back to the existing confirm dialog and returns an empty dependency
   * selection on approval. When dependency rows exist, the shared dependency
   * dialog is opened and the selected dependency ids are returned.</p>
   */
  requestDependencySelection(request: BieStateTransitionFlowRequest): Observable<StateDependencySelection | undefined> {
    const initialSelection: StateDependencySelection = {
      topLevelAsbiepIds: [],
      codeListManifestIds: []
    };

    return request.validateSelection(initialSelection).pipe(
      map(targets => request.normalizeTargets ? request.normalizeTargets(targets || []) : (targets || [])),
      switchMap(validatedTargets => {
        if (!this.hasBlockingIssues(validatedTargets || [])) {
          return this.openSimpleConfirmation(request);
        }

        const dialogData: BieStateDependencyDialogData = {
          state: request.state,
          rootTopLevelAsbiepIds: request.rootTopLevelAsbiepIds,
          targets: validatedTargets,
          validateSelection: request.validateSelection
        };

        return this.dialog.open(BieStateDependencyDialogComponent, {
          width: '90vw',
          maxWidth: '90vw',
          data: dialogData
        }).afterClosed();
      }),
      catchError(() => request.loadDependencies().pipe(
        map(targets => request.normalizeTargets ? request.normalizeTargets(targets || []) : (targets || [])),
        switchMap(targets => {
          if (!this.hasBlockingIssues(targets || [])) {
            return this.openSimpleConfirmation(request);
          }

          const dialogData: BieStateDependencyDialogData = {
            state: request.state,
            rootTopLevelAsbiepIds: request.rootTopLevelAsbiepIds,
            targets,
            validateSelection: request.validateSelection
          };

          return this.dialog.open(BieStateDependencyDialogComponent, {
            width: '90vw',
            maxWidth: '90vw',
            data: dialogData
          }).afterClosed();
        })
      ))
    );
  }

  /**
   * Preserves the original simple confirm dialog for transitions without any
   * visible dependency rows.
   */
  private openSimpleConfirmation(
    request: BieStateTransitionFlowRequest,
    selection: StateDependencySelection = {topLevelAsbiepIds: [], codeListManifestIds: []}
  ): Observable<StateDependencySelection | undefined> {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = request.confirmationHeader || this.confirmationHeader(request.state);
    dialogConfig.data.content = request.confirmationContent || this.confirmationContent(request.state);
    dialogConfig.data.action = request.confirmationAction || this.confirmationAction(request.state);

    return this.confirmDialogService.open(dialogConfig).afterClosed().pipe(
      map(result => result ? selection : undefined)
    );
  }

  /**
   * Returns whether the current dependency snapshot contains any blocking row
   * that needs the dedicated state-transition dialog.
   */
  private hasBlockingIssues(targets: StateDependencyTarget[]): boolean {
    return (targets || []).some(target => (target.issues || []).length > 0);
  }

  private confirmationHeader(state: string): string {
    return state === 'Discard'
      ? 'Discard BIE?'
      : ('Update state to \'' + state + '\'?');
  }

  private confirmationContent(state: string): string[] {
    return state === 'Discard'
      ? [
        'Are you sure you want to discard this BIE?',
        'This BIE will be permanently removed.'
      ]
      : ['Are you sure you want to update the state to \'' + state + '\'?'];
  }

  private confirmationAction(state: string): string {
    return state === 'Discard' ? 'Discard' : 'Update';
  }

}
