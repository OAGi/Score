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

// ---------------------------------------------------------------------------
// Operation ID + Resource Name construction rules (issue #1732)
//
// SINGLE SOURCE OF TRUTH. Both OpenAPI editors (oas-doc-detail, bie-edit) and both Add dialogs
// (bie-oas-doc-add-dialog, oas-doc-assign-dialog) build the operationId and the resource path from
// here, so the naming rule can never drift between copies. The frontend owns these strings; the
// backend stores them verbatim.
//
// The array 'List' / '-list' marker is driven PURELY by the arrayIndicator boolean and the immutable
// property term — never inferred by inspecting the existing string. That is what avoids the whole
// "is this trailing 'List' part of the name or the array marker?" ambiguity: a property term that
// itself ends in 'List' (e.g. "Price List") keeps its own 'List' AND still gets the array marker, so
// the single form (queryPriceList) and the array form (queryPriceListList) stay distinct and unique.
// ---------------------------------------------------------------------------

const OPERATION_ID_VERB_WORDS: { [verb: string]: string } = {
  GET: 'query', POST: 'create', PUT: 'replace', PATCH: 'update', DELETE: 'delete',
  OPTIONS: 'options', HEAD: 'head', TRACE: 'trace'
};
// Verb words that may already lead an operationId (incl. the legacy 'get' word).
const KNOWN_OPERATION_ID_VERB_WORDS =
  ['query', 'create', 'replace', 'update', 'delete', 'options', 'head', 'trace', 'get'];

export function operationIdVerbWord(verb: string): string {
  return OPERATION_ID_VERB_WORDS[verb] || '';
}

function capitalizeFirst(value: string): string {
  return value ? value.charAt(0).toUpperCase() + value.slice(1) : value;
}

// The BIE-name segment of an operationId: the property term with spaces removed and the first char
// capitalised (e.g. "Price List" -> "PriceList"). A term that itself ends in "List" keeps it.
export function operationName(bieName: string): string {
  return capitalizeFirst((bieName || '').replace(/\s/g, ''));
}

// operationId rule: <verb-word><BieName>[List]. The trailing "List" is the ARRAY MARKER, appended
// solely from arrayIndicator — intentionally doubled for a name ending in "List" so the single and
// array forms remain distinct (e.g. GET "Price List" array -> "queryPriceListList").
export function buildOperationId(verb: string, bieName: string, isArray: boolean): string {
  const word = operationIdVerbWord(verb);
  if (!word) {
    return '';
  }
  return word + operationName(bieName) + (isArray ? 'List' : '');
}

// Recover the BIE-name segment from an existing operationId. Used ONLY where no property term is
// available (issue #1730 bodyless operations), so a manually entered operationId's name survives a
// verb swap. The array MARKER "List" is stripped only when the array indicator says one is present,
// so a name that genuinely ends in "List" is never truncated.
export function extractBieName(operationId: string, isArray: boolean): string {
  let name = (operationId || '').trim();
  // Drop a legacy '<businessContext>_' prefix if present (the new format has no underscore).
  const underscore = name.lastIndexOf('_');
  if (underscore >= 0) {
    name = name.substring(underscore + 1);
  }
  // Strip a leading verb word so it can be replaced.
  for (const word of KNOWN_OPERATION_ID_VERB_WORDS) {
    const next = name.charAt(word.length);
    if (name.length > word.length && name.startsWith(word)
        && next !== next.toLowerCase() && next === next.toUpperCase()) {
      name = name.substring(word.length);
      break;
    }
  }
  // Strip the array marker only when the array indicator says one is present.
  if (isArray && name.endsWith('List')) {
    name = name.substring(0, name.length - 'List'.length);
  }
  return name;
}

// Recompute the operationId after a verb or array change. Prefers the immutable property term as the
// name source (so a term ending in "List" is never confused with the array marker); falls back to
// parsing the existing id when there is no property term (bodyless operations).
export function recomputeOperationId(
  verb: string, oldOperationId: string, isArray: boolean, propertyTerm?: string): string {
  if (!operationIdVerbWord(verb)) {
    return oldOperationId;
  }
  const name = (propertyTerm && propertyTerm.trim())
    ? propertyTerm
    : extractBieName(oldOperationId, isArray);
  return buildOperationId(verb, name, isArray);
}

// The last path segment of a resource path: the property term lower-cased with spaces -> dashes, plus
// the '-list' array marker. Mirrors how the backend derives the path when a BIE is added to a document.
export function resourcePathLeaf(propertyTerm: string, isArray: boolean): string {
  const term = (propertyTerm || '').replace(/\s/g, '-').toLowerCase();
  return isArray ? term + '-list' : term;
}

// Full resource path rule: /<business-context>/<document-version>/<leaf>. The version segment is
// omitted when the document has no version. Used by the Add dialogs' path preview.
export function buildResourcePath(
  businessContextName: string, version: string, propertyTerm: string, isArray: boolean): string {
  const bc = (businessContextName || '').toLowerCase().replace(/ /g, '-');
  const leaf = resourcePathLeaf(propertyTerm, isArray);
  return version ? `/${bc}/${version}/${leaf}` : `/${bc}/${leaf}`;
}

// Re-apply the array '-list' marker to an existing resource path by rebuilding ONLY its last segment
// from the property term. Preserves the /<bc>/<version> prefix (so a customised prefix survives) while
// guaranteeing the leaf marker exactly tracks the array indicator — even when the property term itself
// ends in "list" (e.g. "Price List" -> "price-list-list").
export function applyResourceArrayMarker(resourceName: string, propertyTerm: string, isArray: boolean): string {
  const path = resourceName || '';
  const slash = path.lastIndexOf('/');
  const prefix = slash >= 0 ? path.substring(0, slash + 1) : '';
  return prefix + resourcePathLeaf(propertyTerm, isArray);
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
