import { Injectable, inject } from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable, of} from 'rxjs';
import {catchError, map, switchMap} from 'rxjs/operators';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {
  BieStateDependencyDialogComponent,
  BieStateDependencyDialogData
} from '../bie-state-dependency-dialog/bie-state-dependency-dialog.component';
import {StateDependencyTarget} from './state-dependency-target';

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
  validateSelection: (selectedTopLevelAsbiepIds: number[]) => Observable<StateDependencyTarget[]>;
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


  private static readonly NO_UPDATE_MESSAGE = 'This BIE will not be updated.';

  /**
   * Resolves the dependency selection required for a state transition.
   *
   * <p>When no dependency rows are returned, this falls back to the existing
   * confirm dialog and returns an empty dependency selection on approval. When
   * dependency rows exist, the shared dependency dialog is opened and the
   * selected dependency ids are returned.</p>
   */
  requestDependencySelection(request: BieStateTransitionFlowRequest): Observable<number[] | undefined> {
    return request.loadDependencies().pipe(
      map(targets => request.normalizeTargets ? request.normalizeTargets(targets || []) : (targets || [])),
      map(targets => this.filterActionableTargets(targets || [])),
      switchMap(targets => {
        if (targets.length === 0) {
          return this.openSimpleConfirmation(request.state);
        }

        const initialSelectedTopLevelAsbiepIds = this.getInitiallySelectedDependencyIds(
          targets,
          request.rootTopLevelAsbiepIds
        );

        return request.validateSelection(initialSelectedTopLevelAsbiepIds).pipe(
          map(validatedTargets => request.normalizeTargets ? request.normalizeTargets(validatedTargets || []) : (validatedTargets || [])),
          map(validatedTargets => this.filterActionableTargets(validatedTargets || [])),
          catchError(() => of(targets)),
          switchMap(validatedTargets => {
            const dialogData: BieStateDependencyDialogData = {
              state: request.state,
              rootTopLevelAsbiepIds: request.rootTopLevelAsbiepIds,
              targets: validatedTargets,
              validateSelection: request.validateSelection
            };

            return this.dialog.open(BieStateDependencyDialogComponent, {
              width: '1200px',
              maxWidth: '95vw',
              data: dialogData
            }).afterClosed();
          })
        );
      })
    );
  }

  /**
   * Preserves the original simple confirm dialog for transitions without any
   * visible dependency rows.
   */
  private openSimpleConfirmation(state: string): Observable<number[] | undefined> {
    const dialogConfig = this.confirmDialogService.newConfig();
    dialogConfig.data.header = 'Update state to \'' + state + '\'?';
    dialogConfig.data.content = ['Are you sure you want to update the state to \'' + state + '\'?'];
    dialogConfig.data.action = 'Update';

    return this.confirmDialogService.open(dialogConfig).afterClosed().pipe(
      map(result => result ? [] : undefined)
    );
  }

  /**
   * Hides dependency rows that would not be updated and do not currently
   * contribute any conflict. When only those rows are returned, the shared
   * flow should fall back to the simple confirmation dialog.
   */
  private filterActionableTargets(targets: StateDependencyTarget[]): StateDependencyTarget[] {
    return targets.filter(target =>
      target.selectionConflict === true ||
      target.stateTransitionAllowed === false ||
      target.dependencyUpdateMessage !== BieStateTransitionFlowService.NO_UPDATE_MESSAGE
    );
  }

  private getInitiallySelectedDependencyIds(
    targets: StateDependencyTarget[],
    rootTopLevelAsbiepIds: number[]
  ): number[] {
    const rootIdSet = new Set(rootTopLevelAsbiepIds || []);
    return (targets || [])
      .filter(target => !rootIdSet.has(target.topLevelAsbiepId))
      .filter(target => target.dependencyUpdateAllowed !== false && target.checked !== false)
      .map(target => target.topLevelAsbiepId);
  }
}
