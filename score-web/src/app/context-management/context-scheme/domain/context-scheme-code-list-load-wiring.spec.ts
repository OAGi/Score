import {vi} from 'vitest';
import {of} from 'rxjs';
import {CodeListDetails, CodeListValueDetails} from '../../../code-list-management/domain/code-list';
import {ContextSchemeCreateComponent} from '../context-scheme-create/context-scheme-create.component';
import {ContextSchemeDetailComponent} from '../context-scheme-detail/context-scheme-detail.component';

function details(over: { listId?: string; agency?: string; versionId?: string; values?: string[] } = {}): CodeListDetails {
  const d = new CodeListDetails();
  d.codeListManifestId = 7;
  d.listId = over.listId ?? 'L1';
  d.agencyIdListValue.value = over.agency ?? 'A1';
  d.versionId = over.versionId ?? '1';
  d.valueList = (over.values ?? ['a', 'b']).map(v => ({value: v, meaning: v + '!'} as CodeListValueDetails));
  return d;
}

/** The part of a component `_loadFromCodeList` works with, with the dialogs answering as given. */
function ctxFor(component: { prototype: any }, requestKey: string, codeList: CodeListDetails, confirmResults: any[]): any {
  const opened: any[] = [];
  const ctx: any = {
    [requestKey]: {schemeId: 'old', schemeAgencyId: 'old', schemeVersionId: 'old', contextSchemeValueList: []},
    dialog: {open: () => ({afterClosed: () => of({codeListManifestId: 7})})},
    codeListService: {getCodeListDetails: () => of(codeList)},
    confirmDialogService: {
      newConfig: () => ({data: {} as any}),
      open: (config: any) => {
        opened.push(config.data);
        return {afterClosed: () => of(confirmResults.shift())};
      },
    },
    selection: {select: vi.fn()},
    _updateDataSource: vi.fn(),
    opened,
  };
  ctx.convertCodeListValuesIntoContextSchemeValues = component.prototype.convertCodeListValuesIntoContextSchemeValues;
  ctx._loadFromCodeList = component.prototype._loadFromCodeList;
  return ctx;
}

const SCREENS: [string, { prototype: any }, string][] = [
  ['create', ContextSchemeCreateComponent, 'contextSchemeCreateRequest'],
  ['detail', ContextSchemeDetailComponent, 'contextSchemeUpdateRequest'],
];

describe.each(SCREENS)('loading a Code List on the %s screen', (_screen, component, requestKey) => {
  const lastValues = (ctx: any) => ctx._updateDataSource.mock.calls.at(-1)[0].map((v: any) => v.value);

  it('puts the identifiers and the values in the form without asking when they all fit', () => {
    const ctx = ctxFor(component, requestKey, details(), []);
    ctx._loadFromCodeList();
    expect(ctx[requestKey].schemeId).toBe('L1');
    expect(ctx[requestKey].schemeAgencyId).toBe('A1');
    expect(ctx[requestKey].schemeVersionId).toBe('1');
    expect(lastValues(ctx)).toEqual(['a', 'b']);
    expect(ctx.opened.length).toBe(0);
  });

  it('puts the trimmed identifiers in the form when the user agrees to trim them', () => {
    const ctx = ctxFor(component, requestKey, details({versionId: 'v'.repeat(52)}), [true]);
    ctx._loadFromCodeList();
    expect(ctx[requestKey].schemeVersionId).toBe('v'.repeat(45));
    expect(ctx[requestKey].schemeId).toBe('L1');
  });

  it('leaves the form as it was when the user cancels, without touching the values', () => {
    const ctx = ctxFor(component, requestKey, details({versionId: 'v'.repeat(52)}), [undefined]);
    ctx._loadFromCodeList();
    expect(ctx[requestKey].schemeId).toBe('old');
    expect(ctx[requestKey].schemeVersionId).toBe('old');
    expect(ctx._updateDataSource).not.toHaveBeenCalled();
  });

  it('loads the values with the long one trimmed and the repeated and empty ones left out', () => {
    const ctx = ctxFor(component, requestKey, details({values: ['a', 'a', '', 'z'.repeat(60)]}), [true]);
    ctx._loadFromCodeList();
    expect(lastValues(ctx)).toEqual(['a', 'z'.repeat(45)]);
    expect(ctx[requestKey].schemeId).toBe('L1');
  });

  it('changes nothing, not even the identifiers, when the values dialog is cancelled', () => {
    const ctx = ctxFor(component, requestKey, details({values: ['a', 'z'.repeat(60)]}), [undefined]);
    ctx._loadFromCodeList();
    expect(ctx[requestKey].schemeId).toBe('old');
    expect(ctx._updateDataSource).not.toHaveBeenCalled();
  });

  it('gives every loaded value its own guid', () => {
    const ctx = ctxFor(component, requestKey, details(), []);
    ctx._loadFromCodeList();
    const guids = ctx._updateDataSource.mock.calls.at(-1)[0].map((v: any) => v.guid);
    expect(new Set(guids).size).toBe(2);
    expect(guids.every((g: string) => !!g)).toBe(true);
  });
});

describe('loading a Code List on the detail screen', () => {
  it('selects the loaded values, as the table of that screen keys its selection by guid', () => {
    const ctx = ctxFor(ContextSchemeDetailComponent, 'contextSchemeUpdateRequest', details(), []);
    ctx._loadFromCodeList();
    expect(ctx.selection.select).toHaveBeenCalledTimes(2);
  });
});
