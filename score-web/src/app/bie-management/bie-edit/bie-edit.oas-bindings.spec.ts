import {vi} from 'vitest';
import {of} from 'rxjs';
import {BieEditComponent} from './bie-edit.component';
import {OasDocChangeResult} from '../openapi-doc/domain/oas-doc-change';

/**
 * The OpenAPI panel of a BIE holds no rule about the bindings: it says what the user changed of them, document by document,
 * and the server says whether it can be saved and what it makes of the verb or the array indicator that was changed. The
 * methods are invoked against a hand-built `this` (no Angular TestBed).
 */
describe('BieEditComponent OpenAPI bindings (#1519)', () => {
  const proto: any = BieEditComponent.prototype;
  const binding = (oasDocId: number, oasOperationId: number, extra: any = {}): any =>
    ({oasDocId, oasOperationId, messageBody: 'Response', operationId: 'op' + oasOperationId, isChanged: true, hashCode: 1, isNew: false,
      rowId: '' + oasOperationId,
      touched: [] as string[], touch(field: string) { this.touched.push(field); }, ...extra});
  const result = (extra: Partial<OasDocChangeResult>): OasDocChangeResult => ({valid: true, violations: [], httpHeaders: [], ...extra});

  function panel(bindings: any[], status: string = 'valid', violations: any[] = []): any {
    const c: any = Object.create(proto);
    c.rootNode = {bieForOasDocList: bindings};
    c.removedOasBindings = [];
    c.openAPIService = {checkChanges: vi.fn(() => of(result({})))};
    c.oasChecker = {status: () => status, violations: () => violations};
    return c;
  }

  describe('check', () => {
    it('asks the server once for each document that has a change, and puts the answers together', () => {
      const c = panel([binding(1, 11), binding(2, 21), binding(1, 12)]);
      c.openAPIService.checkChanges = vi.fn((oasDocId: number) => of(oasDocId === 2
        ? result({valid: false, violations: [{scope: 'OPERATION', oasOperationId: 21, message: 'used'}]})
        : result({operations: [{oasOperationId: 11, sentMessageBody: 'Response', operationId: 'x', resourceName: '/x', messageBody: 'Response'}]})));
      let answer: OasDocChangeResult | undefined;

      c.checkOasBindings(c.buildOasChangeSet())
        .subscribe((r: OasDocChangeResult) => answer = r);

      expect(c.openAPIService.checkChanges).toHaveBeenCalledTimes(2);
      expect(c.openAPIService.checkChanges.mock.calls.map((call: any) => [call[0], call[1].operations.length])).toEqual([[1, 2], [2, 1]]);
      expect(answer!.valid).toBe(false);
      expect(answer!.violations.map(v => v.oasOperationId)).toEqual([21]);
      expect(answer!.operations!.length).toBe(1);
    });

    it('has an empty change set when no binding is changed: the server judges the bindings as they are', () => {
      const c = panel([binding(1, 11, {isChanged: false})]);

      expect(c.buildOasChangeSet()).toEqual({operations: [], headers: {upserts: [], removed: []}, added: [], removed: []});
    });

    it('does not take an answer to what the bindings were before the user changed them again', () => {
      const row = binding(1, 11, {hashCode: 1});
      const c = panel([row]);
      c.buildOasChangeSet();
      row.hashCode = 2;

      expect(c.takeOasCheckResult(result({}), {operations: [row], headers: {upserts: [], removed: []}})).toBe(false);
    });

    it('takes what the server made of the names into the rows, and the answer is not a change of the user\'s', () => {
      const row = binding(1, 11, {touched: ['arrayIndicator'], hashCode: 1, resourceName: '/a'});
      const c = panel([row]);
      c.buildOasChangeSet();
      const answer = result({operations: [{oasOperationId: 11, sentMessageBody: 'Response', operationId: 'queryAList',
        resourceName: '/a-list', messageBody: 'Response'}]});

      expect(c.takeOasCheckResult(answer, {operations: [row], headers: {upserts: [], removed: []}})).toBe(true);

      expect(row.operationId).toBe('queryAList');
      expect(row.resourceName).toBe('/a-list');
      expect(row.touched).toEqual([]);
    });
  });

  describe('what the user changes', () => {
    it('says that the verb or the array indicator was changed, and leaves the rest to the server', () => {
      const row = binding(1, 11, {verb: 'GET', messageBody: 'Request'});
      const c = panel([row]);

      c.onOasVerbChange(row);
      c.onOasArrayChange(row);

      expect(row.touched).toEqual(['verb', 'arrayIndicator']);
      expect(row.messageBody).toBe('Request');
    });
  });

  describe('what the server says is wrong', () => {
    const violation = (extra: any) => ({scope: 'OPERATION', message: 'wrong', ...extra});

    it('is shown at the cell of the binding it is about', () => {
      const c = panel([], 'invalid', [violation({oasOperationId: 11, field: 'operationId', message: 'The operation ID is used.'}),
        violation({oasOperationId: 12, field: 'resourceName', message: 'Path must start with "/".'})]);

      expect(c.isOasOperationIdDuplicate({oasOperationId: 11, messageBody: 'Response'})).toBe(true);
      expect(c.isOasOperationIdDuplicate({oasOperationId: 12, messageBody: 'Response'})).toBe(false);
      expect(c.oasViolationMessage({oasOperationId: 11, messageBody: 'Response'}, 'operationId')).toBe('The operation ID is used.');
      expect(c.isOasBodySlotDuplicate({oasOperationId: 12, messageBody: 'Response'})).toBe(true);
      expect(c.oasViolationMessage({oasOperationId: 12, messageBody: 'Response'}, 'resourceName', 'messageBody')).toBe('Path must start with "/".');
    });

    it('marks a blank Operation ID, and one that the server refused, as an error', () => {
      const c = panel([], 'invalid', [violation({oasOperationId: 11, field: 'operationId'})]);
      const isError = (row: any) => c.oasOperationIdErrorStateMatcher(row).isErrorState();

      expect(isError({oasOperationId: 11, operationId: 'a'})).toBe(true);
      expect(isError({oasOperationId: 12, operationId: ' '})).toBe(true);
      expect(isError({oasOperationId: 12, operationId: 'a'})).toBe(false);
    });

    it('keeps Update away until the server has said that what is changed can be saved', () => {
      expect(panel([binding(1, 11)], 'valid').hasOasErrors()).toBe(false);
      for (const status of ['checking', 'invalid', 'failed', 'idle']) {
        expect(panel([binding(1, 11)], status).hasOasErrors()).toBe(true);
      }
      expect(panel([binding(1, 11, {operationId: ' '})], 'valid').hasOasErrors()).toBe(true);
    });
  });
});
