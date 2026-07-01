import {ErrorStateMatcher} from '@angular/material/core';

/**
 * Shared OpenAPI operation-uniqueness validation.
 *
 * One place owns the "operationId must be unique" and "one Request + one Response per (Resource Name,
 * Verb)" rules so that BOTH the OpenAPI Document editor (oas-doc-detail) and the BIE-root OpenAPI
 * bindings panel (bie-edit, issue #1519) enforce them identically.
 *
 * Issue #1757: an operationId is a duplicate only when it spans 2+ DISTINCT operations. A single
 * operation surfaces as up to two rows — a Request body and a Response body — that legitimately share
 * one operationId (and one oas_operation), so those must collapse to ONE operation. The distinct-
 * operation identity is (Resource Name, Verb) — NOT the oas_operation_id row PK, and NOT the message
 * body. Keying on the row PK is exactly the "confusing the Operation ID with the table's
 * oas_operation_id" bug: a legacy endpoint whose Request and Response live in two separate
 * oas_operation rows would otherwise be flagged as a false duplicate.
 */

// The minimal row shape the validation needs. BieForOasDoc (a persisted document row) and the
// BIE-root binding rows both satisfy it structurally, so neither has to be imported here.
export interface OasOperationRow {
  oasDocId: number;
  resourceName: string;
  verb: string;
  messageBody: string;
  operationId: string;
}

// (document, operationId) — an operationId's uniqueness scope is a single document. Including the
// document id lets a validator span multiple documents (the BIE-root panel) without cross-document
// collisions; within a single-document editor every row shares the same id, so it is a no-op there.
export function oasOperationIdKey(row: OasOperationRow): string {
  return row.oasDocId + '|' + (row.operationId || '').trim();
}

// (Resource Name, Verb) — the identity of ONE OpenAPI operation (endpoint) within a document. The
// message body is deliberately excluded so an operation's Request row and Response row are the same
// operation.
export function oasOperationKey(row: OasOperationRow): string {
  return (row.resourceName || '').trim() + '|' + (row.verb || '');
}

// (document, Resource Name, Verb, Message Body) — one body slot. A 2nd Request (or 2nd Response) on
// one (Resource Name, Verb) is a true duplicate (issue #1492 Option 2).
export function oasBodySlotKey(row: OasOperationRow): string {
  return row.oasDocId + '|' + (row.resourceName || '').trim() + '|' + (row.verb || '') + '|' + (row.messageBody || '');
}

// Adds or removes a trailing suffix — the operationId 'List' marker and the resource path '-list'
// marker that track the array indicator (#1732).
export function applyOasSuffix(value: string, suffix: string, present: boolean): string {
  const current = value || '';
  if (present) {
    return current.endsWith(suffix) ? current : current + suffix;
  }
  return current.endsWith(suffix) ? current.substring(0, current.length - suffix.length) : current;
}

export class OasOperationValidator {
  // operationId keys (oasOperationIdKey) that map to 2+ distinct operations within a document.
  duplicateOperationIds = new Set<string>();
  // body-slot keys (oasBodySlotKey) that occur more than once.
  duplicateBodySlots = new Set<string>();

  // Recomputes both duplicate sets from the given rows. Cheap and idempotent — call it after any edit.
  // Note: callers often work with a server-paginated slice, so this validates within the loaded rows;
  // the backend 400 remains the source of truth.
  recompute(rows: OasOperationRow[]): void {
    // operationId uniqueness: group rows by (document, operationId), then count the DISTINCT operations
    // (Resource Name, Verb) each spans. Only 2+ distinct operations is a real duplicate.
    const operationsByOperationId = new Map<string, Set<string>>();
    for (const row of (rows || [])) {
      if (!(row.operationId || '').trim()) {
        continue; // the "required" check owns empties, not uniqueness
      }
      const idKey = oasOperationIdKey(row);
      let operations = operationsByOperationId.get(idKey);
      if (!operations) {
        operations = new Set<string>();
        operationsByOperationId.set(idKey, operations);
      }
      operations.add(oasOperationKey(row));
    }
    this.duplicateOperationIds = new Set<string>(
      Array.from(operationsByOperationId.entries())
        .filter(([, operations]) => operations.size > 1).map(([idKey]) => idKey));

    // body-slot uniqueness: each (Resource Name, Verb, Message Body) may appear at most once.
    const bodySlotCounts = new Map<string, number>();
    for (const row of (rows || [])) {
      if (!row.verb || !row.messageBody) {
        continue;
      }
      const key = oasBodySlotKey(row);
      bodySlotCounts.set(key, (bodySlotCounts.get(key) || 0) + 1);
    }
    this.duplicateBodySlots = new Set<string>(
      Array.from(bodySlotCounts.entries()).filter(([, count]) => count > 1).map(([key]) => key));
  }

  isDuplicateOperationId(row: OasOperationRow): boolean {
    return this.duplicateOperationIds.has(oasOperationIdKey(row));
  }

  isDuplicateBodySlot(row: OasOperationRow): boolean {
    return this.duplicateBodySlots.has(oasBodySlotKey(row));
  }

  // The operationId cell is in error when it is empty (required) OR collides with another operation.
  // A fresh per-row matcher (like bodySlotErrorStateMatcher) so Material re-reads the row's live value.
  operationIdErrorStateMatcher(row: OasOperationRow): ErrorStateMatcher {
    return {
      isErrorState: (): boolean => !(row.operationId || '').trim() || this.isDuplicateOperationId(row)
    };
  }

  bodySlotErrorStateMatcher(row: OasOperationRow): ErrorStateMatcher {
    return {
      isErrorState: (): boolean => this.isDuplicateBodySlot(row)
    };
  }
}
