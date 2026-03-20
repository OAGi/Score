import { Component, QueryList, ViewChildren, inject } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatTable} from '@angular/material/table';
import {Observable} from 'rxjs';
import {WebPageInfoService} from '../../basis/basis.service';
import {
  StateDependencyIssue,
  StateDependencyRelation,
  StateDependencySelection,
  StateDependencyTarget
} from '../domain/state-dependency-target';

/**
 * Data contract passed into the dependency dialog.
 */
export interface BieStateDependencyDialogData {
  state: string;
  rootTopLevelAsbiepIds: number[];
  targets: StateDependencyTarget[];
  validateSelection?: (selection: StateDependencySelection) => Observable<StateDependencyTarget[]>;
}

@Component({
  standalone: false,
  selector: 'score-bie-state-dependency-dialog',
  templateUrl: './bie-state-dependency-dialog.component.html',
  styleUrls: ['./bie-state-dependency-dialog.component.css']
})
export class BieStateDependencyDialogComponent {
  dialogRef = inject<MatDialogRef<BieStateDependencyDialogComponent>>(MatDialogRef);
  webPageInfo = inject(WebPageInfoService);
  data = inject<BieStateDependencyDialogData>(MAT_DIALOG_DATA);

  bieDisplayedColumns: string[] = ['select', 'den', 'dependencies', 'businessContexts', 'version', 'status', 'remark', 'owner', 'state'];
  codeListDisplayedColumns: string[] = ['select', 'name', 'dependencies', 'agencyId', 'version', 'owner', 'state'];
  isValidating = false;
  private validationRequestId = 0;
  @ViewChildren(MatTable) tables?: QueryList<MatTable<StateDependencyTarget>>;

  constructor() {
    this.applyTargets(this.data.targets || []);
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    if (this.hasInvalidTargets() || this.isValidating) {
      return;
    }
    this.dialogRef.close(this.getSelection());
  }

  trackByNodeKey(_index: number, target: StateDependencyTarget): string {
    return target.nodeKey;
  }

  /**
   * Root BIEs are already part of the user's requested state change and should
   * not be deselected from the dependency dialog.
   */
  isRootTarget(target: StateDependencyTarget): boolean {
    return target.nodeType === 'BIE' &&
      (this.data.rootTopLevelAsbiepIds || []).includes(target.topLevelAsbiepId);
  }

  /**
   * A checkbox can be toggled only when the row is updateable by ownership and
   * is not one of the originally requested root BIEs. Validation conflicts are
   * shown as messages, but they should not erase the user's ability to
   * re-select a row to resolve the conflict.
   */
  isToggleable(target: StateDependencyTarget): boolean {
    return !this.isRootTarget(target) && target.selectable;
  }

  isSelectable(target: StateDependencyTarget): boolean {
    return this.isToggleable(target);
  }

  hasTargets(): boolean {
    return this.data.targets.length > 0;
  }

  hasBieTargets(): boolean {
    return this.bieTargets().length > 0;
  }

  hasCodeListTargets(): boolean {
    return this.codeListTargets().length > 0;
  }

  bieTargets(): StateDependencyTarget[] {
    return this.data.targets.filter(target => target.nodeType !== 'CODE_LIST');
  }

  codeListTargets(): StateDependencyTarget[] {
    return this.data.targets.filter(target => target.nodeType === 'CODE_LIST');
  }

  selectionHint(): string {
    if (!this.hasTargets()) {
      return 'No associated BIEs will be updated.';
    }

    if (this.hasCodeListTargets()) {
      return `Checked BIE records and eligible assigned code lists will also be updated to '${this.data.state}'. Assigned code lists are shown separately.`;
    }

    return `This list shows associated BIEs. Checked BIE records will also be updated to '${this.data.state}'.`;
  }

  allSelected(): boolean {
    const selectableTargets = this.bieTargets().filter(target => this.isToggleable(target));
    return selectableTargets.length > 0 && selectableTargets.every(target => target.checked);
  }

  someSelected(): boolean {
    return this.bieTargets().some(target => this.isToggleable(target) && target.checked) && !this.allSelected();
  }

  codeListAllSelected(): boolean {
    const selectableTargets = this.codeListTargets().filter(target => this.isToggleable(target));
    return selectableTargets.length > 0 && selectableTargets.every(target => target.checked);
  }

  codeListSomeSelected(): boolean {
    return this.codeListTargets().some(target => this.isToggleable(target) && target.checked) && !this.codeListAllSelected();
  }

  toggleAll(checked: boolean): void {
    const nextSelectedTopLevelAsbiepIds = checked ?
      this.bieTargets().filter(target => this.isToggleable(target)).map(target => target.topLevelAsbiepId) :
      [];
    this.validateSelection({
      topLevelAsbiepIds: nextSelectedTopLevelAsbiepIds,
      codeListManifestIds: this.getSelectedCodeListManifestIds()
    });
  }

  toggleAllCodeLists(checked: boolean): void {
    const nextSelectedCodeListManifestIds = checked ?
      this.codeListTargets().filter(target => this.isToggleable(target)).map(target => target.codeListManifestId) :
      [];
    this.validateSelection({
      topLevelAsbiepIds: this.getSelectedDependencyIds(),
      codeListManifestIds: nextSelectedCodeListManifestIds
    });
  }

  onTargetCheckedChange(target: StateDependencyTarget, checked: boolean): void {
    if (!this.isToggleable(target)) {
      return;
    }
    target.checked = checked;
    this.validateSelection(this.getSelection());
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
        return 'Reused by';
      case 'INHERITS_FROM':
        return 'Inherits from';
      case 'IS_A_BASED_OF':
        return 'Base of';
      case 'USES_CODE_LIST':
        return 'Uses code list';
      case 'USED_BY_BIE':
        return 'Used by';
      default:
        return '';
    }
  }

  relationSummaryText(relation?: StateDependencyRelation): string {
    const relationLabel = this.relationText(relation);
    switch (relation?.dependency) {
      case 'REUSES':
        return relationLabel ? `Reuses ${relationLabel}` : 'Reuses';
      case 'REUSED_BY':
        return relationLabel ? `Reused by ${relationLabel}` : 'Reused by';
      case 'INHERITS_FROM':
        return relationLabel ? `Inherits from ${relationLabel}` : 'Inherits from';
      case 'IS_A_BASED_OF':
        return relationLabel ? `Base of ${relationLabel}` : 'Base of';
      case 'USES_CODE_LIST':
        return relationLabel ? `Uses code list ${relationLabel}` : 'Uses code list';
      case 'USED_BY_BIE':
        return relationLabel ? `Used by ${relationLabel}` : 'Used by';
      default: {
        const dependencyText = this.relationDependencyText(relation);
        return relationLabel ? `${dependencyText} ${relationLabel}`.trim() : dependencyText;
      }
    }
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
    return this.data.targets.some(target => this.issues(target).length > 0);
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

    const titleCompare = this.targetTitle(left)
      .localeCompare(this.targetTitle(right));
    if (titleCompare !== 0) {
      return titleCompare;
    }

    return this.value(left.nodeKey || left.guid).localeCompare(this.value(right.nodeKey || right.guid));
  }

  private targetTitle(target: StateDependencyTarget): string {
    if (target.nodeType === 'CODE_LIST') {
      return this.value(target.name);
    }
    return this.value(target.den || target.displayName || target.propertyTerm);
  }

  targetLink(target: StateDependencyTarget): string | null {
    if (target.nodeType === 'BIE' && target.topLevelAsbiepId != null) {
      return `/profile_bie/${target.topLevelAsbiepId}`;
    }
    if (target.nodeType === 'CODE_LIST' && target.codeListManifestId != null) {
      return `/code_list/${target.codeListManifestId}`;
    }
    return null;
  }

  relationLink(relation?: StateDependencyRelation): string | null {
    if (!relation) {
      return null;
    }
    if (relation.nodeType === 'BIE' && relation.topLevelAsbiepId != null) {
      return `/profile_bie/${relation.topLevelAsbiepId}`;
    }
    if (relation.nodeType === 'CODE_LIST' && relation.codeListManifestId != null) {
      return `/code_list/${relation.codeListManifestId}`;
    }
    return null;
  }

  agencyIdTooltip(target: StateDependencyTarget): string {
    return this.value(target.agencyIdName);
  }

  issues(target: StateDependencyTarget): StateDependencyIssue[] {
    return target.issues || [];
  }

  hasIssues(target: StateDependencyTarget): boolean {
    return this.issues(target).length > 0;
  }

  private validateSelection(selection: StateDependencySelection): void {
    if (!this.data.validateSelection) {
      this.applyCheckedState(selection);
      return;
    }

    // Ignore stale responses when the user changes selection faster than the
    // server can validate the previous request.
    const requestId = ++this.validationRequestId;
    this.isValidating = true;
    this.data.validateSelection(selection).subscribe({
      next: (targets) => {
        if (requestId !== this.validationRequestId) {
          return;
        }
        this.applyTargets(this.filterActionableTargets(targets || []));
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
      .filter(target => target.nodeType === 'BIE')
      .map(target => target.topLevelAsbiepId);
  }

  private getSelectedCodeListManifestIds(): number[] {
    return this.data.targets
      .filter(target => this.isToggleable(target) && target.checked)
      .filter(target => target.nodeType === 'CODE_LIST')
      .map(target => target.codeListManifestId);
  }

  private getSelection(): StateDependencySelection {
    return {
      topLevelAsbiepIds: this.getSelectedDependencyIds(),
      codeListManifestIds: this.getSelectedCodeListManifestIds()
    };
  }

  private applyCheckedState(selection: StateDependencySelection): void {
    const selectedIdSet = new Set(selection.topLevelAsbiepIds || []);
    const selectedCodeListIdSet = new Set(selection.codeListManifestIds || []);
    const nextTargets = this.data.targets
      .map(target => ({
        ...target,
        checked: this.isRootTarget(target) || (this.isToggleable(target) && (
          target.nodeType === 'CODE_LIST'
            ? selectedCodeListIdSet.has(target.codeListManifestId)
            : selectedIdSet.has(target.topLevelAsbiepId)))
      }))
      .sort((left, right) => this.compareTargets(left, right));

    this.data.targets.splice(0, this.data.targets.length, ...nextTargets);
    this.renderTables();
  }

  private applyTargets(targets: StateDependencyTarget[]): void {
    const normalizedTargets = (targets || [])
      .map(target => this.normalizeTarget(target))
      .sort((left, right) => this.compareTargets(left, right));
    const currentTargetMap = new Map(this.data.targets.map(target => [target.nodeKey, target]));

    const nextTargets = normalizedTargets.map(target => {
      const currentTarget = currentTargetMap.get(target.nodeKey);
      if (!currentTarget) {
        return target;
      }
      Object.assign(currentTarget, target);
      return currentTarget;
    });

    // Keep the same array instance so mat-table updates rows in place instead
    // of tearing down the whole table between validation responses.
    this.data.targets.splice(0, this.data.targets.length, ...nextTargets);
    this.renderTables();
  }

  /**
   * Keeps only rows that still require user attention inside the dialog.
   *
   * <p>This mirrors the initial filtering performed before the dialog opens so
   * subsequent validation responses do not suddenly introduce issue-free
   * code-list rows.</p>
   */
  private filterActionableTargets(targets: StateDependencyTarget[]): StateDependencyTarget[] {
    return (targets || []).filter(target =>
      (target.issues || []).length > 0 ||
      target.nodeType !== 'CODE_LIST'
    );
  }

  private normalizeTarget(target: StateDependencyTarget): StateDependencyTarget {
    const selectable = target.selectable !== false;
    return {
      ...target,
      nodeKey: target.nodeKey || `${target.nodeType || 'BIE'}:${target.topLevelAsbiepId ?? target.codeListManifestId ?? 'unknown'}`,
      nodeType: target.nodeType || 'BIE',
      dependencyTopLevelAsbiepIds: target.dependencyTopLevelAsbiepIds || [],
      requiredDependencyTopLevelAsbiepIds: target.requiredDependencyTopLevelAsbiepIds || [],
      dependencies: target.dependencies || [],
      selectable,
      checked: this.isRootTarget(target) || (selectable && target.checked !== false),
      issues: target.issues || []
    };
  }

  private renderTables(): void {
    this.tables?.forEach(table => table.renderRows());
  }
}
