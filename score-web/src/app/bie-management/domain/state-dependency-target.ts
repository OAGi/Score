/**
 * Lightweight relation metadata used by the dependency dialog.
 */
export class StateDependencyRelation {
  topLevelAsbiepId: number;
  dependency: string;
  label: string;
  guid: string;
}

/**
 * Client-side representation of a dependency row returned by the state
 * dependency preview and validation endpoints.
 */
export class StateDependencyTarget {
  topLevelAsbiepId: number;
  dependencyTopLevelAsbiepIds: number[];
  requiredDependencyTopLevelAsbiepIds: number[];
  dependencies: StateDependencyRelation[];
  edgeDistance: number;
  propertyTerm: string;
  displayName: string;
  guid: string;
  businessContexts: string[];
  version: string;
  status: string;
  remark: string;
  state: string;
  dependencyUpdateAllowed: boolean;
  dependencyUpdateMessage: string;
  stateTransitionAllowed: boolean;
  stateTransitionMessage: string;
  checked: boolean;
  selectionConflict: boolean;
  selectionConflictMessage: string;
}
