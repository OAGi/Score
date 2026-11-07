import {BieForOasDoc, OasDoc, OasDocHttpHeader, OasHttpHeaderDirection, OasParameterLocation} from './openapi-doc';
import {headerKey} from './oas-doc-http-header-preset';
import {locationOf} from './oas-doc-http-header-form';
import {hashCode} from '../../../common/utility';

/**
 * What the user has changed of an OpenAPI Document and has not saved: the page only says what was changed, and asks the
 * server whether it can be saved (`check`) and to save it. It holds no rule about the document.
 */
export interface OasDocChangeSet {
  /** The fields, Security Schemes and Security Requirements of the document, when one of them was changed. */
  document?: OasDoc;
  /** The operations (rows of the table) that were edited, of every page. */
  operations: BieForOasDoc[];
  headers: OasDocHeaderChanges;
}

export interface OasDocHeaderChanges {
  /** Added or changed entries. */
  upserts: OasDocHttpHeader[];
  /** Deleted entries, including a registered one that the user did not want. */
  removed: OasDocHeaderKey[];
}

/** What tells one entry from another (the operation is `null` for an entry of the whole document). */
export interface OasDocHeaderKey {
  oasOperationId: number | null;
  direction: OasHttpHeaderDirection;
  in: OasParameterLocation;
  name: string;
}

export interface OasDocViolation {
  scope: 'DOCUMENT' | 'OPERATION' | 'HEADER';
  oasOperationId?: number;
  /** `Request` or `Response`, when the rule is broken by one of the two rows of an operation. */
  messageBody?: string;
  field?: string;
  message: string;
}

/** What the server made of an operation whose verb or array indicator the user changed. */
export interface OasDocDerivedOperation {
  oasOperationId: number;
  /** The Message Body the row had when it was sent: which row it is. */
  sentMessageBody: string;
  operationId: string;
  resourceName: string;
  /** The Message Body it has now (a GET has no Request body). */
  messageBody: string;
}

export interface OasDocChangeResult {
  valid: boolean;
  violations: OasDocViolation[];
  /** What the entries of the document would be if the change set were saved (the registered ones included). */
  httpHeaders: OasDocHttpHeader[];
  operations?: OasDocDerivedOperation[];
}

/** An operation that is about to be added to the document, as far as the user has said it. */
export interface OasOperationCandidate {
  topLevelAsbiepId?: number;
  propertyTerm?: string;
  verb: string;
  messageBody: string;
  arrayIndicator: boolean;
  /** Only when the user gave one (an operation without a BIE); else the server makes it from the BIE. */
  resourceName?: string;
  operationId?: string;
}

/** What the server makes of the candidate, and what is wrong with adding it. */
export interface OasOperationProposal {
  resourceName: string;
  operationId: string;
  violations: OasDocViolation[];
}

/**
 * The Operation ID, the Resource Name and the Message Body that the server made of the verb or the array indicator that the user
 * changed are those of the rows; what was asked of the server is answered, so the rows are not touched any more.
 */
export function applyDerivedOperations(derivedOperations: OasDocDerivedOperation[] | undefined, rows: BieForOasDoc[]): void {
  for (const derived of derivedOperations || []) {
    const row = rows.find(candidate =>
      candidate.oasOperationId === derived.oasOperationId && candidate.messageBody === derived.sentMessageBody);
    if (row) {
      row.operationId = derived.operationId;
      row.resourceName = derived.resourceName;
      row.messageBody = derived.messageBody;
    }
  }
  rows.forEach(row => row.touched = []);
}

export function headerKeyOf(header: OasDocHttpHeader): OasDocHeaderKey {
  return {oasOperationId: header.oasOperationId || null, direction: header.direction, in: locationOf(header), name: header.name};
}

/**
 * The entries of the document that the user added, changed or deleted since they were last saved. The page holds the list that
 * it shows; this works out what was done to it, so that the page can say it to the server. A registered entry (one that the
 * server made for an operation) is the page's like any other once it is shown: when the user deletes it, it is a deletion, and
 * it is remembered as one until the change is saved, so that the server does not register it again.
 */
export class OasDocHeaderEdits {
  /** What the user saw last: the entries as saved, or as the server answered to the last check. */
  private shown = new Map<string, OasDocHttpHeader>();
  private readonly upserts = new Map<string, OasDocHttpHeader>();
  private readonly removed = new Map<string, OasDocHeaderKey>();

  /** The entries are saved (or loaded): there is no change. */
  reset(saved: OasDocHttpHeader[] | undefined): void {
    this.shown = OasDocHeaderEdits.byKey(saved);
    this.upserts.clear();
    this.removed.clear();
  }

  /** Take in what the user did to the list since the last time: an entry that is new or differs was upserted, one that is gone was removed. */
  observe(current: OasDocHttpHeader[] | undefined): void {
    const now = OasDocHeaderEdits.byKey(current);
    for (const [key, header] of now) {
      const before = this.shown.get(key);
      if (!before || hashCode(before) !== hashCode(header)) {
        this.upserts.set(key, copy(header));
        this.removed.delete(key);
      }
    }
    for (const [key, header] of this.shown) {
      if (!now.has(key)) {
        this.upserts.delete(key);
        this.removed.set(key, headerKeyOf(header));
      }
    }
    this.shown = now;
  }

  /** The server has said what the entries would be: that is what the user sees now. */
  adopt(resulting: OasDocHttpHeader[]): void {
    this.shown = OasDocHeaderEdits.byKey(resulting);
  }

  changes(): OasDocHeaderChanges {
    return {upserts: [...this.upserts.values()], removed: [...this.removed.values()]};
  }

  private static byKey(headers: OasDocHttpHeader[] | undefined): Map<string, OasDocHttpHeader> {
    // a copy: the dialog changes an entry in place, and what was seen has to stay as it was to tell what was changed
    return new Map((headers || []).map(header => [headerKey(header), copy(header)] as [string, OasDocHttpHeader]));
  }
}

function copy(header: OasDocHttpHeader): OasDocHttpHeader {
  return JSON.parse(JSON.stringify(header));
}
