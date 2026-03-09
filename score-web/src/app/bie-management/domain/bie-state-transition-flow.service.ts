import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable, of} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
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

  constructor(private dialog: MatDialog,
              private confirmDialogService: ConfirmDialogService) {
  }

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
      switchMap(targets => {
        if (targets.length === 0) {
          return this.openSimpleConfirmation(request.state);
        }

        const dialogData: BieStateDependencyDialogData = {
          state: request.state,
          rootTopLevelAsbiepIds: request.rootTopLevelAsbiepIds,
          targets,
          validateSelection: request.validateSelection
        };

        return this.dialog.open(BieStateDependencyDialogComponent, {
          width: '1200px',
          maxWidth: '95vw',
          data: dialogData
        }).afterClosed();
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
}
