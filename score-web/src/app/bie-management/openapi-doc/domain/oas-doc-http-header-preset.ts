import {OasDoc, OasDocHttpHeader, OasHttpHeaderDirection} from './openapi-doc';
import {locationOf} from './oas-doc-http-header-form';

/**
 * Issue #1791: the HTTP header presets of an OpenAPI Document. A preset is only known here: the server stores
 * and generates whatever header definitions it is given, so a header added to this list needs no server change.
 *
 * Offering a preset is a macro: it fills the headers of the operations that the document has now. An operation that
 * is added later has none until the preset is offered again, and one that already has the header is left as it is.
 */
export interface OasDocHttpHeaderPreset {
  name: string;
  /**
   * The header definitions as they are sent to the server, without an operation (the one it is offered to is set):
   * one per direction, since a request and a response differ in description, example and required.
   */
  headers: OasDocHttpHeader[];
}

// W3C Trace Context: https://www.w3.org/TR/trace-context/#traceparent-header
const TRACEPARENT_DESCRIPTION = 'Standard composite identifier allowing for end-to-end correlation across multiple ' +
  'systems and services involved in the transaction. Specified by OTel Trace Context and W3C, the format is ' +
  '<version>-<trace-id>-<parent-id>-<trace-flags>.';
const TRACEPARENT_PATTERN = '^[0-9a-f]{2}-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$';

export const TRACEPARENT_PRESET: OasDocHttpHeaderPreset = {
  name: 'traceparent',
  headers: [
    {
      // where the API client generates the traceparent
      name: 'traceparent',
      description: TRACEPARENT_DESCRIPTION + ' The <parent-id> is for this specific API call, and the <trace-id> ' +
        'is the same as the one in the response header of the operation that was called before.',
      schema: {
        type: 'string',
        pattern: TRACEPARENT_PATTERN,
        example: '00-4bf92f3577b34da6a3ce929d0e0e4736-a1b2c3d4e5f60718-01'
      },
      direction: 'REQUEST',
      in: 'header',
      required: false
    },
    {
      // where the API returns the traceparent
      name: 'traceparent',
      description: TRACEPARENT_DESCRIPTION,
      schema: {
        type: 'string',
        pattern: TRACEPARENT_PATTERN,
        example: '00-4bf92f3577b34da6a3ce929d0e0e4736-b2c3d4e5f6071829-01'
      },
      direction: 'RESPONSE',
      required: true
    }
  ]
};

/** What taking a preset away did: the headers that were removed. */
export interface OasDocHttpHeaderWithdrawal {
  removed: number;
  /** Of them, the request parameters and the response headers. */
  requestParameters: number;
  responseHeaders: number;
}

/** What offering a preset did: the operations that got the header, and those that had it already. */
export interface OasDocHttpHeaderOffer {
  added: number[];
  skipped: number[];
  /** The entries that were added: the request parameters and the response headers. */
  requestParameters: number;
  responseHeaders: number;
}

/** The row of the operations table, as far as the headers are concerned. */
export interface OasDocHttpHeaderRow {
  oasOperationId: number;
  /** The headers of the operation of the row: the objects of {@code OasDoc.httpHeaders} that apply to it. */
  httpHeaders: OasDocHttpHeader[];
}

function isOfPreset(header: OasDocHttpHeader, preset: OasDocHttpHeaderPreset): boolean {
  // a parameter of that name in the query, the path or a cookie is another one
  return header.name?.toLowerCase() === preset.name.toLowerCase() && locationOf(header) === 'header';
}

/** Whether the operation has this header of the preset (of its direction): of its own, or one of the whole document. */
function hasPresetHeader(oasDoc: OasDoc, presetHeader: OasDocHttpHeader, oasOperationId: number): boolean {
  return (oasDoc.httpHeaders || []).some(header => header.direction === presetHeader.direction
    && locationOf(header) === locationOf(presetHeader)
    && header.name?.toLowerCase() === presetHeader.name.toLowerCase()
    && (!header.oasOperationId || header.oasOperationId === oasOperationId));
}

/** Whether the operation has every header of the preset, both the request parameter and the response header. */
export function hasPreset(oasDoc: OasDoc, preset: OasDocHttpHeaderPreset, oasOperationId: number): boolean {
  return preset.headers.every(presetHeader => hasPresetHeader(oasDoc, presetHeader, oasOperationId));
}

/** Whether any header of the preset is in the document, of an operation or of the whole document. */
export function hasAnyPreset(oasDoc: OasDoc, preset: OasDocHttpHeaderPreset): boolean {
  return (oasDoc.httpHeaders || []).some(header => isOfPreset(header, preset));
}

/** Whether there is an operation that does not have the header of the preset (there are operations at all). */
export function lacksPreset(oasDoc: OasDoc, preset: OasDocHttpHeaderPreset, oasOperationIds: number[]): boolean {
  return oasOperationIds.some(oasOperationId => !hasPreset(oasDoc, preset, oasOperationId));
}

/** Keep the headers in one order, so that taking them away and offering them again is no change of the document. */
export function sortHeaders(oasDoc: OasDoc): void {
  oasDoc.httpHeaders = (oasDoc.httpHeaders || []).sort((a, b) =>
    (a.oasOperationId || 0) - (b.oasOperationId || 0) || a.name.toLowerCase().localeCompare(b.name.toLowerCase())
    || a.direction.localeCompare(b.direction));
}

/**
 * Offer the headers of the preset to the operations: each one gets a header of its own, a copy of the preset's, for
 * each direction in which it has none. The headers that are there are left as they are.
 */
export function offerPreset(oasDoc: OasDoc, preset: OasDocHttpHeaderPreset, oasOperationIds: number[]): OasDocHttpHeaderOffer {
  const offer: OasDocHttpHeaderOffer = {added: [], skipped: [], requestParameters: 0, responseHeaders: 0};
  oasDoc.httpHeaders = oasDoc.httpHeaders || [];
  for (const oasOperationId of new Set(oasOperationIds)) {
    const missing = preset.headers.filter(presetHeader => !hasPresetHeader(oasDoc, presetHeader, oasOperationId));
    if (missing.length === 0) {
      offer.skipped.push(oasOperationId);
    } else {
      // a copy, so that a document never shares the schema object of the preset
      missing.forEach(presetHeader => oasDoc.httpHeaders.push({...JSON.parse(JSON.stringify(presetHeader)), oasOperationId}));
      offer.requestParameters += missing.filter(presetHeader => presetHeader.direction === 'REQUEST').length;
      offer.responseHeaders += missing.filter(presetHeader => presetHeader.direction === 'RESPONSE').length;
      offer.added.push(oasOperationId);
    }
  }
  sortHeaders(oasDoc);
  return offer;
}

/** Take the header of the preset away from every operation, and from the whole document. The other headers stay. */
export function withdrawPreset(oasDoc: OasDoc, preset: OasDocHttpHeaderPreset): OasDocHttpHeaderWithdrawal {
  const headers = oasDoc.httpHeaders || [];
  const kept = headers.filter(header => !isOfPreset(header, preset));
  oasDoc.httpHeaders = kept;
  const gone = headers.filter(header => !kept.includes(header));
  return {
    removed: gone.length,
    requestParameters: gone.filter(header => header.direction === 'REQUEST').length,
    responseHeaders: gone.filter(header => header.direction === 'RESPONSE').length
  };
}

/** What tells a header from another: its operation, direction, location and name (a header name is case-insensitive). */
export function headerKey(header: OasDocHttpHeader): string {
  const location = locationOf(header);
  return [header.oasOperationId || 0, header.direction, location, location === 'header' ? header.name.toLowerCase() : header.name].join('\n');
}

/**
 * The server registers request parameters by itself when the operations change (sinceLastDateTime of a GET that returns
 * an array, the path parameters of a Resource Path). Take what the server has: the headers of the page are replaced by
 * them when the page has no change of its own; when it has, they are kept, and only what the server has added is added,
 * so that the next Update does not take it away again.
 */
export function adoptServerHeaders(oasDoc: OasDoc, server: OasDocHttpHeader[], pageHasChanges: boolean): void {
  if (!pageHasChanges) {
    oasDoc.httpHeaders = server;
  } else {
    const known = new Set((oasDoc.httpHeaders || []).map(headerKey));
    oasDoc.httpHeaders = [...(oasDoc.httpHeaders || []), ...server.filter(header => !known.has(headerKey(header)))];
  }
  sortHeaders(oasDoc);
}

/**
 * Connect the rows to the headers of the document: the headers of a row are the very objects of the document, so that
 * what is changed through a row is a change of the document, and the document is what is saved. The rows of one
 * operation (a request and a response) share their headers.
 */
export function linkHeadersToRows(oasDoc: OasDoc, rows: OasDocHttpHeaderRow[]): void {
  const headers = oasDoc.httpHeaders || [];
  const byOperation = new Map<number, OasDocHttpHeader[]>();
  for (const row of rows) {
    if (!byOperation.has(row.oasOperationId)) {
      byOperation.set(row.oasOperationId,
        headers.filter(header => !header.oasOperationId || header.oasOperationId === row.oasOperationId));
    }
    row.httpHeaders = byOperation.get(row.oasOperationId) as OasDocHttpHeader[];
  }
}

/** The headers of one direction, for a cell of the table. */
export function headersOf(headers: OasDocHttpHeader[] | undefined, direction: OasHttpHeaderDirection): OasDocHttpHeader[] {
  return (headers || []).filter(header => header.direction === direction);
}
