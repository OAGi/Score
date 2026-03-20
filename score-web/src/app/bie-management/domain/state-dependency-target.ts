/**
 * Lightweight relation metadata used by the dependency dialog.
 */
export class StateDependencyRelation {
  nodeKey: string;
  nodeType: string;
  topLevelAsbiepId: number;
  codeListManifestId: number;
  dependency: string;
  label: string;
  guid: string;
}

export type StateDependencyIssueType = 'OWNERSHIP' | 'STATE_COMPATIBILITY' | 'DEPENDENCY_CONFLICT';

/**
 * One blocking issue attached to a dependency row.
 */
export class StateDependencyIssue {
  type: StateDependencyIssueType;
  message: string;
}

/**
 * Client-side representation of a dependency row returned by the state
 * dependency preview and validation endpoints.
 */
export class StateDependencyTarget {
  nodeKey: string;
  nodeType: string;
  topLevelAsbiepId: number;
  codeListManifestId: number;
  dependencyTopLevelAsbiepIds: number[];
  dependencies: StateDependencyRelation[];
  edgeDistance: number;
  den: string;
  propertyTerm: string;
  displayName: string;
  name: string;
  guid: string;
  ownerLoginId: string;
  agencyId: string;
  agencyIdName: string;
  businessContexts: string[];
  version: string;
  status: string;
  remark: string;
  state: string;
  selectable: boolean;
  stateChangeAvailable: boolean;
  checked: boolean;
  issues: StateDependencyIssue[];
}

/**
 * Checkbox selection payload sent back to the dependency-validation endpoint
 * and later reused for the final state-update request.
 */
export interface StateDependencySelection {
  topLevelAsbiepIds: number[];
  codeListManifestIds: number[];
}
