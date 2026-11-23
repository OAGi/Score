import {Observable, of} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {CodeListDetails, CodeListValueDetails} from '../../../code-list-management/domain/code-list';
import {ConfirmDialogService} from '../../../common/confirm-dialog/confirm-dialog.service';

/**
 * SCHEMA-LENGTH(ctx_scheme): the longest Scheme ID, Agency ID, Version and value that can be typed on the context
 * scheme screens (their input maxlength), which loading a Code List follows.
 *
 * The identifiers are the lengths of `ctx_scheme.scheme_id`, `scheme_agency_id` and `scheme_version_id` (varchar(45)).
 * A value is limited to 45 by the dialog of the value, which is narrower than `ctx_scheme_value.value` (varchar(100)).
 * When these columns change, change every place found by searching for SCHEMA-LENGTH(ctx_scheme): the maxlength of
 * the inputs of the create, detail and value screens, and the checks of `ContextSchemeCommandService`.
 */
const MAX_LENGTH = 45;

type SkipKind = 'empty' | 'long' | 'duplicate';

interface SkippedValue {
  kind: SkipKind;
  text: string;
}

/** What loading a code list puts in a context scheme. */
export interface CodeListLoad {
  schemeId: string;
  schemeAgencyId: string;
  schemeVersionId: string;
  values: CodeListValueDetails[];
}

/** The identifiers of the code list that are longer than the context scheme takes. */
export function longIdentifiers(details: CodeListDetails): string[] {
  return identifiersOf(details)
    .filter(([, text]) => text.length > MAX_LENGTH)
    .map(([label, text]) => `${label} (${text.length} characters): ${text}`);
}

function identifiersOf(details: CodeListDetails): [string, string][] {
  return [
    ['List ID', (details.listId ?? '').toString()],
    ['Agency ID List Value', (details.agencyIdListValue?.value ?? '').toString()],
    ['Version', (details.versionId ?? '').toString()],
  ];
}

/**
 * Sorts the values the way entering them by hand would: a value is not empty, is not longer than the input allows
 * and is not already in the scheme. With `trim`, a long value is cut to the length that the input allows instead of
 * being skipped (and is skipped only when its cut text is already in the scheme).
 */
export function enterValues(values: CodeListValueDetails[], trim: boolean): { kept: CodeListValueDetails[]; skipped: SkippedValue[] } {
  const kept: CodeListValueDetails[] = [];
  const skipped: SkippedValue[] = [];
  const taken = new Set<string>();
  for (const v of values ?? []) {
    let text = v.value ?? '';
    const isLong = text.length > MAX_LENGTH;
    if (text === '') {
      skipped.push({kind: 'empty', text: '(empty value): cannot be empty'});
      continue;
    }
    if (isLong && !trim) {
      skipped.push({kind: 'long', text: `${text}: ${text.length} characters`});
      continue;
    }
    text = text.substring(0, MAX_LENGTH);
    if (taken.has(text)) {
      skipped.push({kind: 'duplicate', text: `${text}: already exists`});
      continue;
    }
    taken.add(text);
    kept.push(isLong ? {...v, value: text} : v);
  }
  return {kept, skipped};
}

/**
 * Decides what the context scheme takes of the code list. Loading a code list stands for typing it in by hand, so
 * what the screens would not take is put to the user, who chooses to cut it to the length of the input or to cancel. It emits what to load, or `null` when nothing is to be loaded.
 */
export function resolveCodeListLoad(confirmDialogService: ConfirmDialogService,
                                    details: CodeListDetails): Observable<CodeListLoad | null> {
  return resolveIdentifiers(confirmDialogService, details).pipe(
    switchMap(ids => ids
      ? resolveValues(confirmDialogService, details.valueList).pipe(map(values => values ? {...ids, values} : null))
      : of(null)));
}

function resolveIdentifiers(confirmDialogService: ConfirmDialogService,
                            details: CodeListDetails): Observable<{ schemeId: string; schemeAgencyId: string; schemeVersionId: string } | null> {
  const [listId, agencyId, versionId] = identifiersOf(details).map(([, text]) => text);
  const cut = () => ({
    schemeId: listId.substring(0, MAX_LENGTH),
    schemeAgencyId: agencyId.substring(0, MAX_LENGTH),
    schemeVersionId: versionId.substring(0, MAX_LENGTH),
  });
  const long = longIdentifiers(details);
  if (long.length === 0) {
    return of(cut());
  }

  const config = confirmDialogService.newConfig();
  config.data.header = 'Cannot load the code list';
  config.data.content = [
    `These exceed ${MAX_LENGTH} characters, the longest a context scheme takes. Trim them to ${MAX_LENGTH} characters and load the code list anyway?`,
  ];
  config.data.list = long;
  config.data.action = 'Trim and continue';
  return confirmDialogService.open(config).afterClosed().pipe(map(result => result ? cut() : null));
}

function resolveValues(confirmDialogService: ConfirmDialogService,
                       values: CodeListValueDetails[]): Observable<CodeListValueDetails[] | null> {
  const asIs = enterValues(values, false);
  if (asIs.skipped.length === 0) {
    return of(asIs.kept);
  }

  const hasLong = asIs.skipped.some(s => s.kind === 'long');
  const config = confirmDialogService.newConfig();
  config.data.header = 'Some values cannot be loaded';
  const reasons = hasLong ? `empty, longer than ${MAX_LENGTH} characters, or already in the list` : 'empty or already in the list';
  const cannot = `${asIs.skipped.length} value(s) cannot be entered in a context scheme (${reasons}).`;
  config.data.list = asIs.skipped.map(s => s.text);
  if (hasLong) {
    config.data.content = [`${cannot} Trim the long values to ${MAX_LENGTH} characters and load the code list anyway? Empty and repeated values are left out.`];
    config.data.action = 'Trim and continue';
  } else {
    config.data.content = [`${cannot} Load the other ${asIs.kept.length} value(s) and leave these out?`];
    config.data.action = 'Continue';
  }
  return confirmDialogService.open(config).afterClosed().pipe(
    map(result => result ? enterValues(values, true).kept : null));
}
