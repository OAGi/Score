import {vi} from 'vitest';
import {OasDocDetailComponent} from './oas-doc-detail.component';

/**
 * Issue #1610 unit tests for the OpenAPI-document editor.
 *
 * Focus areas:
 *   1) OpenAPI version detection — isOpenApi31() decides whether a DELETE request body is honored,
 *      and gates the 3.0.3-only "ignored DELETE body" banner.
 *   2) DELETE operation handling at the UI level — a DELETE may now carry a Request body in any
 *      version (only GET is reverted to Response), and 3.0.3 documents surface a warning banner.
 *
 * These follow the repo's domain-test style (see bie-flat-tree.spec.ts): no Angular TestBed. The
 * component uses inject() in its field initializers, so it cannot be `new`-ed outside an injection
 * context; instead each method under test is invoked against a hand-built `this` carrying only the
 * state that method touches. This keeps the test pinned to the commit's logic, not Angular wiring.
 */

type Row = { verb: string; messageBody: string };

function tableWith(rows: Row[]): any {
  return {dataSource: {data: rows}};
}

/** A `this` context exposing the version-detection + banner methods bound to a shared data bag. */
function detailCtx(openAPIVersion: any, rows: Row[] = []): any {
  const ctx: any = {
    oasDoc: openAPIVersion === null ? null : {openAPIVersion},
    table: tableWith(rows),
  };
  ctx.isOpenApi31 = OasDocDetailComponent.prototype.isOpenApi31;
  ctx.hasIgnoredDeleteRequestBody = OasDocDetailComponent.prototype.hasIgnoredDeleteRequestBody;
  return ctx;
}

describe('OasDocDetailComponent', () => {
  it('should be defined', () => {
    expect(OasDocDetailComponent).toBeTruthy();
  });
});

describe('OasDocDetailComponent.isOpenApi31 (#1610)', () => {
  it('is true for 3.1.x versions', () => {
    expect(detailCtx('3.1.1').isOpenApi31()).toBe(true);
    expect(detailCtx('3.1.0').isOpenApi31()).toBe(true);
    expect(detailCtx('3.1').isOpenApi31()).toBe(true);
  });

  it('trims surrounding whitespace before matching', () => {
    expect(detailCtx('  3.1.1 ').isOpenApi31()).toBe(true);
  });

  it('is false for 3.0.3 and other non-3.1 versions', () => {
    expect(detailCtx('3.0.3').isOpenApi31()).toBe(false);
    expect(detailCtx('3.0.0').isOpenApi31()).toBe(false);
    // a "3.1" substring that is not the version prefix must not match
    expect(detailCtx('2.3.1').isOpenApi31()).toBe(false);
  });

  it('is false when the version or the document is missing', () => {
    expect(detailCtx('').isOpenApi31()).toBe(false);
    expect(detailCtx(undefined).isOpenApi31()).toBe(false);
    expect(detailCtx(null).isOpenApi31()).toBe(false);
  });
});

describe('OasDocDetailComponent.hasIgnoredDeleteRequestBody (#1610 banner)', () => {
  it('is true for a 3.0.3 document that has a DELETE + Request operation', () => {
    const ctx = detailCtx('3.0.3', [{verb: 'DELETE', messageBody: 'Request'}]);
    expect(ctx.hasIgnoredDeleteRequestBody()).toBe(true);
  });

  it('is false for the SAME data when the document targets 3.1.1 (the body is honored)', () => {
    const ctx = detailCtx('3.1.1', [{verb: 'DELETE', messageBody: 'Request'}]);
    expect(ctx.hasIgnoredDeleteRequestBody()).toBe(false);
  });

  it('is false on 3.0.3 when no DELETE carries a Request body', () => {
    expect(detailCtx('3.0.3', [{verb: 'DELETE', messageBody: 'Response'}]).hasIgnoredDeleteRequestBody()).toBe(false);
    expect(detailCtx('3.0.3', [{verb: 'POST', messageBody: 'Request'}]).hasIgnoredDeleteRequestBody()).toBe(false);
  });

  it('is false on 3.0.3 when the operation list is empty', () => {
    expect(detailCtx('3.0.3', []).hasIgnoredDeleteRequestBody()).toBe(false);
  });

  it('does not throw when the table/dataSource is not yet loaded', () => {
    const ctx: any = {oasDoc: {openAPIVersion: '3.0.3'}, table: undefined};
    ctx.isOpenApi31 = OasDocDetailComponent.prototype.isOpenApi31;
    ctx.hasIgnoredDeleteRequestBody = OasDocDetailComponent.prototype.hasIgnoredDeleteRequestBody;
    expect(ctx.hasIgnoredDeleteRequestBody()).toBe(false);
  });
});

describe('OasDocDetailComponent.onChange verb handling (#1610)', () => {
  function verbCtx(): any {
    const ctx: any = {updateOperationIdForVerb: vi.fn()};
    ctx.onChange = OasDocDetailComponent.prototype.onChange;
    return ctx;
  }

  it('reverts a GET operation that still has a Request body back to Response', () => {
    const ctx = verbCtx();
    const source = {verb: 'GET', messageBody: 'Request'};
    ctx.onChange('verb', source);
    expect(source.messageBody).toBe('Response');
    expect(ctx.updateOperationIdForVerb).toHaveBeenCalledWith(source);
  });

  it('KEEPS a Request body on a DELETE operation (honored in 3.1, dropped-with-banner in 3.0.3)', () => {
    const ctx = verbCtx();
    const source = {verb: 'DELETE', messageBody: 'Request'};
    ctx.onChange('verb', source);
    expect(source.messageBody).toBe('Request');
    expect(ctx.updateOperationIdForVerb).toHaveBeenCalledWith(source);
  });

  it('leaves a non-GET Request body untouched (e.g. POST) and still resyncs the operationId', () => {
    const ctx = verbCtx();
    const source = {verb: 'POST', messageBody: 'Request'};
    ctx.onChange('verb', source);
    expect(source.messageBody).toBe('Request');
    expect(ctx.updateOperationIdForVerb).toHaveBeenCalledWith(source);
  });
});

describe('OasDocDetailComponent.generate guards unsaved changes (#1610)', () => {
  function generateCtx(changed: boolean): any {
    const ctx: any = {
      isChanged: () => changed,
      snackBar: {open: vi.fn()},
      openAPIService: {generateOpenAPI: vi.fn().mockReturnValue({subscribe: vi.fn()})},
      oasDoc: {oasDocId: 123},
      request: {page: {pageIndex: 0}},
      loading: false,
    };
    ctx.generate = OasDocDetailComponent.prototype.generate;
    return ctx;
  }

  it('blocks generation and prompts to Update when there are unsaved changes', () => {
    const ctx = generateCtx(true);
    ctx.generate();
    expect(ctx.openAPIService.generateOpenAPI).not.toHaveBeenCalled();
    expect(ctx.loading).toBe(false);
    expect(ctx.snackBar.open).toHaveBeenCalledTimes(1);
    const message = ctx.snackBar.open.mock.calls[0][0] as string;
    expect(message).toContain('unsaved changes');
    expect(message).toContain('Update');
  });

  it('generates against the persisted document when there are no unsaved changes', () => {
    const ctx = generateCtx(false);
    ctx.generate();
    expect(ctx.snackBar.open).not.toHaveBeenCalled();
    expect(ctx.openAPIService.generateOpenAPI).toHaveBeenCalledWith(123, ctx.request.page);
    expect(ctx.loading).toBe(true);
  });
});
