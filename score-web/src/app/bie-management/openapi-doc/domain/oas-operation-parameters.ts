import {OasDoc, OasDocHttpHeader} from './openapi-doc';
import {locationOf} from './oas-doc-http-header-form';

/**
 * Issue #1791: the request parameters that the system registers for an operation, as entries of the operation in the
 * request parameters of the document: {@code sinceLastDateTime} for a GET that returns an array, and a path parameter for
 * each <code>{name}</code> of its Resource Path. The server does the same when the operations are saved; this keeps the
 * table showing it while the operations are edited, and the Update then saves what the table shows.
 *
 * Only a change of the operation does it: an entry that was deleted does not come back until the operation changes into
 * that case again, and one the operation had before is not touched.
 */
export interface OperationParameterState {
  /** Whether the operation is a GET that returns an array. */
  sinceLastDateTime: boolean;
  /** The names of the path parameters of the path, in the order they appear, each once. */
  pathParameters: string[];
}

export const SINCE_LAST_DATE_TIME = 'sinceLastDateTime';

/** The row of the operations table, as far as the parameters are concerned. */
export interface OperationRow {
  oasOperationId: number;
  verb: string;
  arrayIndicator: boolean;
  resourceName: string;
}

/** The names of the path parameters of a path, with the <code>{version}</code> replaced by the version of the document. */
export function pathParameterNames(resourceName: string | undefined, documentVersion?: string): string[] {
  const path = documentVersion ? (resourceName || '').split('{version}').join(documentVersion) : (resourceName || '');
  const names: string[] = [];
  for (const match of path.matchAll(/\{([^{}]+)}/g)) {
    const name = match[1].trim();
    if (name && !names.includes(name)) {
      names.push(name);
    }
  }
  return names;
}

/** What the rows of one operation (its Request and Response) say about it. */
export function stateOfRows(rows: OperationRow[], documentVersion?: string): OperationParameterState {
  const first = rows[0];
  return {
    sinceLastDateTime: !!first && first.verb?.toUpperCase() === 'GET' && rows.some(row => row.arrayIndicator),
    pathParameters: pathParameterNames(first?.resourceName, documentVersion)
  };
}

function headerOf(oasOperationId: number, in_: 'query' | 'path', name: string, fields: Partial<OasDocHttpHeader>): OasDocHttpHeader {
  return {
    oasOperationId, name, in: in_, direction: 'REQUEST', schema: {type: 'string'}, required: false, ...fields
  } as OasDocHttpHeader;
}

/** Make the entries of the operation follow its change from {@code before} to {@code after}. */
export function reconcileOperationParameters(oasDoc: OasDoc, oasOperationId: number,
                                             before: OperationParameterState, after: OperationParameterState): void {
  oasDoc.httpHeaders = oasDoc.httpHeaders || [];
  const isRequest = (header: OasDocHttpHeader, in_: string, name: string) =>
    header.direction === 'REQUEST' && locationOf(header) === in_ && header.name === name;
  const has = (in_: string, name: string) => oasDoc.httpHeaders.some(header =>
    isRequest(header, in_, name) && (!header.oasOperationId || header.oasOperationId === oasOperationId));
  const own = (in_: string, name: string) => oasDoc.httpHeaders.filter(header =>
    isRequest(header, in_, name) && header.oasOperationId === oasOperationId);
  const remove = (headers: OasDocHttpHeader[]) => {
    oasDoc.httpHeaders = oasDoc.httpHeaders.filter(header => !headers.includes(header));
  };

  if (after.sinceLastDateTime && !before.sinceLastDateTime) {
    if (!has('query', SINCE_LAST_DATE_TIME)) {
      oasDoc.httpHeaders.push(headerOf(oasOperationId, 'query', SINCE_LAST_DATE_TIME, {
        description: 'Returns resources that have been updated since the last time the endpoint has been called',
        schema: {type: 'string', format: 'date-time'}
      }));
    }
  } else if (before.sinceLastDateTime && !after.sinceLastDateTime) {
    remove(own('query', SINCE_LAST_DATE_TIME));
  }

  const removed = before.pathParameters.filter(name => !after.pathParameters.includes(name));
  const added = after.pathParameters.filter(name => !before.pathParameters.includes(name));
  for (let i = 0; i < Math.max(removed.length, added.length); i++) {
    const from = removed[i];
    const to = added[i];
    if (from !== undefined && to !== undefined) {
      // the name was changed: the entry follows it, whatever the user made of it
      const entries = own('path', from);
      if (has('path', to) || entries.length === 0) {
        remove(entries);
      } else {
        entries.forEach(entry => entry.name = to);
      }
    } else if (from !== undefined) {
      // a path parameter that is not in the path would make the document refer to nothing
      remove(own('path', from));
    } else if (!has('path', to)) {
      oasDoc.httpHeaders.push(headerOf(oasOperationId, 'path', to, {description: '', required: true}));
    }
  }
}

/**
 * Drop the path parameters that the path of their operation does not have, for the operations of the rows. The rows are
 * what is saved; a path parameter that belongs to a Resource Path edit that was not kept (the page was changed before the
 * Update, or the Update was refused) would make the document refer to nothing. Returns whether it dropped any.
 */
export function dropStrayPathParameters(oasDoc: OasDoc, rows: OperationRow[], documentVersion?: string): boolean {
  const namesOfOperation = new Map<number, string[]>();
  for (const row of rows) {
    if (row.oasOperationId && !namesOfOperation.has(row.oasOperationId)) {
      namesOfOperation.set(row.oasOperationId, pathParameterNames(row.resourceName, documentVersion));
    }
  }
  const before = (oasDoc.httpHeaders || []).length;
  oasDoc.httpHeaders = (oasDoc.httpHeaders || []).filter(header => {
    const names = header.oasOperationId ? namesOfOperation.get(header.oasOperationId) : undefined;
    return !(names && header.direction === 'REQUEST' && locationOf(header) === 'path' && !names.includes(header.name));
  });
  return oasDoc.httpHeaders.length !== before;
}
