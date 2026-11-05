import {firstValueFrom, of} from 'rxjs';
import {CodeListDetails, CodeListValueDetails} from '../../../code-list-management/domain/code-list';
import {enterValues, longIdentifiers, resolveCodeListLoad} from './context-scheme-code-list-load';

function details(over: { listId?: string; agency?: string; versionId?: string; values?: string[] } = {}): CodeListDetails {
  const d = new CodeListDetails();
  d.listId = over.listId ?? 'L1';
  d.agencyIdListValue.value = over.agency ?? 'A1';
  d.versionId = over.versionId ?? '1';
  d.valueList = (over.values ?? ['a', 'b']).map(v => ({value: v, meaning: v + '!'} as CodeListValueDetails));
  return d;
}

/** A confirm dialog service whose dialogs close with the given results, in the order they open. */
function dialogService(...results: any[]) {
  const opened: any[] = [];
  return {
    opened,
    newConfig: () => ({data: {} as any}),
    open: (config: any) => {
      opened.push(config.data);
      return {afterClosed: () => of(results.shift())};
    },
  } as any;
}

const resolve = (svc: any, d: CodeListDetails) => firstValueFrom(resolveCodeListLoad(svc, d));

describe('longIdentifiers', () => {
  it('accepts identifiers at the limit and names the ones over it', () => {
    expect(longIdentifiers(details({listId: 'x'.repeat(45)}))).toEqual([]);
    const long = longIdentifiers(details({versionId: 'v'.repeat(46)}));
    expect(long.length).toBe(1);
    expect(long[0]).toContain('Version');
  });
});

describe('enterValues', () => {
  const values = (...texts: string[]) => texts.map(v => ({value: v, meaning: ''} as CodeListValueDetails));

  it('skips what could not be typed in: empty, over 45 and repeated', () => {
    const r = enterValues(values('ok', '', 'z'.repeat(46), 'ok', 'two'), false);
    expect(r.kept.map(v => v.value)).toEqual(['ok', 'two']);
    expect(r.skipped.map(s => s.kind)).toEqual(['empty', 'long', 'duplicate']);
  });

  it('cuts a long value to 45 when trimming, and keeps its meaning', () => {
    const r = enterValues([{value: 'z'.repeat(60), meaning: 'm'} as CodeListValueDetails], true);
    expect(r.kept[0].value).toBe('z'.repeat(45));
    expect(r.kept[0].meaning).toBe('m');
    expect(r.skipped).toEqual([]);
  });

  it('skips a value whose cut text is already in the scheme', () => {
    const r = enterValues(values('z'.repeat(45), 'z'.repeat(50)), true);
    expect(r.kept.length).toBe(1);
    expect(r.skipped.map(s => s.kind)).toEqual(['duplicate']);
  });

  it('still skips empty and repeated values when trimming', () => {
    const r = enterValues(values('', 'a', 'a'), true);
    expect(r.kept.map(v => v.value)).toEqual(['a']);
    expect(r.skipped.length).toBe(2);
  });
});

describe('resolveCodeListLoad', () => {
  it('loads everything without asking when it all fits', async () => {
    const svc = dialogService();
    const load = await resolve(svc, details());
    expect(load!.schemeId).toBe('L1');
    expect(load!.values.map(v => v.value)).toEqual(['a', 'b']);
    expect(svc.opened.length).toBe(0);
  });

  it('asks to trim the identifiers that are too long, and cuts them to 45 when allowed', async () => {
    const svc = dialogService(true);
    const load = await resolve(svc, details({listId: 'x'.repeat(61), versionId: 'v'.repeat(46)}));
    expect(svc.opened[0].action).toBe('Trim and continue');
    expect(svc.opened[0].list.length).toBe(2);
    expect(load!.schemeId).toBe('x'.repeat(45));
    expect(load!.schemeVersionId).toBe('v'.repeat(45));
    expect(load!.schemeAgencyId).toBe('A1');
  });

  it('loads nothing when trimming the identifiers is declined, and asks nothing more', async () => {
    const svc = dialogService(undefined);
    expect(await resolve(svc, details({listId: 'x'.repeat(46), values: ['z'.repeat(50)]}))).toBeNull();
    expect(svc.opened.length).toBe(1);
  });

  it('asks about the values after the identifiers were trimmed', async () => {
    const svc = dialogService(true, undefined);
    expect(await resolve(svc, details({listId: 'x'.repeat(46), values: ['z'.repeat(50)]}))).toBeNull();
    expect(svc.opened.length).toBe(2);
  });

  it('offers to trim long values, with no way to skip them, and trims them on the action', async () => {
    const svc = dialogService(true);
    const load = await resolve(svc, details({values: ['ok', 'z'.repeat(50)]}));
    expect(svc.opened[0].action).toBe('Trim and continue');
    expect(svc.opened[0].alternativeAction).toBeUndefined();
    expect(load!.values.map(v => v.value)).toEqual(['ok', 'z'.repeat(45)]);
  });

  it('loads nothing when the values dialog is cancelled', async () => {
    expect(await resolve(dialogService(undefined), details({values: ['ok', 'z'.repeat(50)]}))).toBeNull();
  });

  it('offers only to continue when the values are empty or repeated, as there is nothing to trim', async () => {
    const svc = dialogService(true);
    const load = await resolve(svc, details({values: ['ok', 'ok', '']}));
    expect(svc.opened[0].action).toBe('Continue');
    expect(svc.opened[0].content[0]).not.toContain('longer than');
    expect(svc.opened[0].alternativeAction).toBeUndefined();
    expect(load!.values.map(v => v.value)).toEqual(['ok']);
  });
});
