import {Component, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatTable} from '@angular/material/table';
import {Observable} from 'rxjs';
import {WebPageInfoService} from '../../basis/basis.service';
import {StateDependencyRelation, StateDependencyTarget} from '../domain/state-dependency-target';

/**
 * Data contract passed into the dependency dialog.
 */
export interface BieStateDependencyDialogData {
  state: string;
  rootTopLevelAsbiepIds: number[];
  targets: StateDependencyTarget[];
  validateSelection?: (selectedTopLevelAsbiepIds: number[]) => Observable<StateDependencyTarget[]>;
}

@Component({
  standalone: false,
  selector: 'score-bie-state-dependency-dialog',
  templateUrl: './bie-state-dependency-dialog.component.html',
  styleUrls: ['./bie-state-dependency-dialog.component.css']
})
export class BieStateDependencyDialogComponent {

  displayedColumns: string[] = ['select', 'displayName', 'dependencies', 'businessContexts', 'version', 'status', 'remark', 'state'];
  isValidating = false;
  private validationRequestId = 0;
  @ViewChild(MatTable) table?: MatTable<StateDependencyTarget>;

  constructor(
    public dialogRef: MatDialogRef<BieStateDependencyDialogComponent>,
    public webPageInfo: WebPageInfoService,
    @Inject(MAT_DIALOG_DATA) public data: BieStateDependencyDialogData) {
    this.applyTargets(this.data.targets || []);
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    if (this.hasInvalidTargets() || this.isValidating) {
      return;
    }
    this.dialogRef.close(this.getSelectedDependencyIds());
  }

  trackByTopLevelAsbiepId(_index: number, target: StateDependencyTarget): number {
    return target.topLevelAsbiepId;
  }

  /**
   * Root BIEs are already part of the user's requested state change and should
   * not be deselected from the dependency dialog.
   */
  isRootTarget(target: StateDependencyTarget): boolean {
    return (this.data.rootTopLevelAsbiepIds || []).includes(target.topLevelAsbiepId);
  }

  /**
   * A checkbox can be toggled only when the row is updateable by ownership and
   * is not one of the originally requested root BIEs. Validation conflicts are
   * shown as messages, but they should not erase the user's ability to
   * re-select a row to resolve the conflict.
   */
  isToggleable(target: StateDependencyTarget): boolean {
    return !this.isRootTarget(target) && target.dependencyUpdateAllowed;
  }

  isSelectable(target: StateDependencyTarget): boolean {
    return this.isToggleable(target);
  }

  hasTargets(): boolean {
    return this.data.targets.length > 0;
  }

  selectionHint(): string {
    if (!this.hasTargets()) {
      return 'No associated BIEs will be updated.';
    }
    return `This list shows associated BIEs by reuse or inheritance. Checked records will also be updated to '${this.data.state}'.`;
  }

  allSelected(): boolean {
    const selectableTargets = this.data.targets.filter(target => this.isToggleable(target));
    return selectableTargets.length > 0 && selectableTargets.every(target => target.checked);
  }

  someSelected(): boolean {
    return this.data.targets.some(target => this.isToggleable(target) && target.checked) && !this.allSelected();
  }

  toggleAll(checked: boolean): void {
    const nextSelectedTopLevelAsbiepIds = checked ?
      this.data.targets.filter(target => this.isToggleable(target)).map(target => target.topLevelAsbiepId) :
      [];
    this.validateSelection(nextSelectedTopLevelAsbiepIds);
  }

  onTargetCheckedChange(target: StateDependencyTarget, checked: boolean): void {
    if (!this.isToggleable(target)) {
      return;
    }
    target.checked = checked;
    this.validateSelection(this.getSelectedDependencyIds());
  }

  value(text?: string): string {
    return text && text.trim().length > 0 ? text : '';
  }

  relationText(relation: StateDependencyRelation): string {
    return this.value(relation?.label);
  }

  relationDependencyText(relation?: StateDependencyRelation): string {
    switch (relation?.dependency) {
      case 'REUSES':
        return 'Reuses';
      case 'REUSED_BY':
        return 'Reused By';
      case 'INHERITS_FROM':
        return 'Inherits From';
      case 'IS_A_BASED_OF':
        return 'Is a Base Of';
      default:
        return '';
    }
  }

  relationSummaryText(relation?: StateDependencyRelation): string {
    const relationLabel = this.relationText(relation);
    const dependencyText = this.relationDependencyText(relation);
    return dependencyText ? `${dependencyText}: ${relationLabel}` : relationLabel;
  }

  relationList(values?: StateDependencyRelation[]): StateDependencyRelation[] {
    return values && values.length > 0 ? values : [];
  }

  primaryRelation(values?: StateDependencyRelation[]): StateDependencyRelation | undefined {
    return this.relationList(values)[0];
  }

  hasMoreRelations(values?: StateDependencyRelation[]): boolean {
    return this.relationList(values).length > 1;
  }

  moreRelationsCount(values?: StateDependencyRelation[]): number {
    return Math.max(this.relationList(values).length - 1, 0);
  }

  relationTooltip(values?: StateDependencyRelation[]): string {
    return this.relationList(values)
      .slice(1)
      .map(relation => this.relationSummaryText(relation))
      .join(', ');
  }

  dependenciesRelations(target: StateDependencyTarget): StateDependencyRelation[] {
    return this.relationList(target.dependencies);
  }

  hasInvalidTargets(): boolean {
    return this.data.targets.some(target => target.selectionConflict || !target.stateTransitionAllowed);
  }

  validationSummary(): string {
    if ((this.data.rootTopLevelAsbiepIds || []).length > 1) {
      return `Selected BIEs cannot move to '${this.data.state}'. Resolve the conflicting records to continue.`;
    }
    return `This BIE cannot move to '${this.data.state}'. Resolve the conflicting records to continue.`;
  }

  private compareTargets(left: StateDependencyTarget, right: StateDependencyTarget): number {
    const levelCompare = (left.edgeDistance || Number.MAX_SAFE_INTEGER) - (right.edgeDistance || Number.MAX_SAFE_INTEGER);
    if (levelCompare !== 0) {
      return levelCompare;
    }

    const displayNameCompare = this.value(left.displayName || left.propertyTerm)
      .localeCompare(this.value(right.displayName || right.propertyTerm));
    if (displayNameCompare !== 0) {
      return displayNameCompare;
    }

    return this.value(left.guid).localeCompare(this.value(right.guid));
  }

  private validateSelection(selectedTopLevelAsbiepIds: number[]): void {
    if (!this.data.validateSelection) {
      this.applyCheckedState(selectedTopLevelAsbiepIds);
      return;
    }

    // Ignore stale responses when the user changes selection faster than the
    // server can validate the previous request.
    const requestId = ++this.validationRequestId;
    this.isValidating = true;
    this.data.validateSelection(selectedTopLevelAsbiepIds).subscribe({
      next: (targets) => {
        if (requestId !== this.validationRequestId) {
          return;
        }
        this.applyTargets(targets || []);
        this.isValidating = false;
      },
      error: () => {
        if (requestId !== this.validationRequestId) {
          return;
        }
        this.isValidating = false;
      }
    });
  }

  /**
   * Only user-toggleable dependency rows should be sent back to the validator
   * and final update request. Root BIEs are already part of the original state
   * update action and must not be treated as dialog selections.
   */
  private getSelectedDependencyIds(): number[] {
    return this.data.targets
      .filter(target => this.isToggleable(target) && target.checked)
      .map(target => target.topLevelAsbiepId);
  }

  private applyCheckedState(selectedTopLevelAsbiepIds: number[]): void {
    const selectedIdSet = new Set(selectedTopLevelAsbiepIds);
    const nextTargets = this.data.targets
      .map(target => ({
        ...target,
        checked: this.isRootTarget(target) || (this.isToggleable(target) && selectedIdSet.has(target.topLevelAsbiepId)),
        selectionConflict: this.isToggleable(target) && !selectedIdSet.has(target.topLevelAsbiepId),
        selectionConflictMessage: this.isToggleable(target) && !selectedIdSet.has(target.topLevelAsbiepId)
          ? 'This BIE must be updated together.'
          : undefined
      }))
      .sort((left, right) => this.compareTargets(left, right));

    this.data.targets.splice(0, this.data.targets.length, ...nextTargets);
    this.table?.renderRows();
  }

  private applyTargets(targets: StateDependencyTarget[]): void {
    const normalizedTargets = (targets || [])
      .map(target => this.normalizeTarget(target))
      .sort((left, right) => this.compareTargets(left, right));
    const currentTargetMap = new Map(this.data.targets.map(target => [target.topLevelAsbiepId, target]));

    const nextTargets = normalizedTargets.map(target => {
      const currentTarget = currentTargetMap.get(target.topLevelAsbiepId);
      if (!currentTarget) {
        return target;
      }
      Object.assign(currentTarget, target);
      return currentTarget;
    });

    // Keep the same array instance so mat-table updates rows in place instead
    // of tearing down the whole table between validation responses.
    this.data.targets.splice(0, this.data.targets.length, ...nextTargets);
    this.table?.renderRows();
  }

  private normalizeTarget(target: StateDependencyTarget): StateDependencyTarget {
    const dependencyUpdateAllowed = target.dependencyUpdateAllowed !== false;
    return {
      ...target,
      dependencyTopLevelAsbiepIds: target.dependencyTopLevelAsbiepIds || [],
      requiredDependencyTopLevelAsbiepIds: target.requiredDependencyTopLevelAsbiepIds || [],
      dependencies: target.dependencies || [],
      dependencyUpdateAllowed,
      dependencyUpdateMessage: target.dependencyUpdateMessage,
      stateTransitionAllowed: target.stateTransitionAllowed !== false,
      stateTransitionMessage: target.stateTransitionMessage,
      checked: this.isRootTarget(target) || (dependencyUpdateAllowed && target.checked !== false),
      selectionConflict: target.selectionConflict === true,
      selectionConflictMessage: target.selectionConflictMessage
    };
  }
}
