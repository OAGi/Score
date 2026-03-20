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
        const actionableTargets = this.filterActionableTargets(validatedTargets || []);
        if (actionableTargets.length === 0) {
          return this.openSimpleConfirmation(request.state);
        }

        const dialogData: BieStateDependencyDialogData = {
          state: request.state,
          rootTopLevelAsbiepIds: request.rootTopLevelAsbiepIds,
          targets: actionableTargets,
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
          const actionableTargets = this.filterActionableTargets(targets || []);
          if (actionableTargets.length === 0) {
            return this.openSimpleConfirmation(request.state);
          }

          const dialogData: BieStateDependencyDialogData = {
            state: request.state,
            rootTopLevelAsbiepIds: request.rootTopLevelAsbiepIds,
            targets: actionableTargets,
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
    state: string,
    selection: StateDependencySelection = {topLevelAsbiepIds: [], codeListManifestIds: []}
  ): Observable<StateDependencySelection | undefined> {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Update state to \'' + state + '\'?';
    dialogConfig.data.content = ['Are you sure you want to update the state to \'' + state + '\'?'];
    dialogConfig.data.action = 'Update';

    return this.confirmDialogService.open(dialogConfig).afterClosed().pipe(
      map(result => result ? selection : undefined)
    );
  }

  /**
   * Keeps rows that require user attention in the dependency dialog.
   *
   * <p>Issue-free code-list rows are excluded here because they do not require
   * an explicit dependency selection from the user.</p>
   */
  private filterActionableTargets(targets: StateDependencyTarget[]): StateDependencyTarget[] {
    return targets.filter(target =>
      (target.issues || []).length > 0 ||
      target.nodeType !== 'CODE_LIST'
    );
  }

}
