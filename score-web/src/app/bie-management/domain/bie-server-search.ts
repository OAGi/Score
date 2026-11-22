import {Observable} from 'rxjs';
import {ServerPredicate, serverFlagName} from '../../common/tree-search/server-predicate';

/** A place of a BIE that the backend found text in: the row that holds it and where in the BIE it is. */
export interface BieSearchNodeKey {
  /** abie, asbie, asbiep, bbie, bbiep or bbie_sc. */
  table: string;
  hashPath: string;
  path?: string;
}

/** What the backend answers for a tree search: the places each predicate (by its id) holds for. */
export interface BieSearchResponse {
  matches: Record<string, BieSearchNodeKey[]>;
}

/** Asks the backend about the predicates of a search in one BIE. */
export type BieServerSearch = (predicates: ServerPredicate[], caseSensitive: boolean) => Promise<BieSearchResponse>;

/** Asks the backend for the values of an attribute that the BIE holds and that contain the text. */
export type BieServerValues = (attribute: string, text: string) => Observable<string[]>;

/** The attributes of a BIE tree whose values the search bar offers after `attribute =`. */
export const BIE_SUGGESTED_ATTRIBUTES: ReadonlySet<string> = new Set(['business_term', 'value_domain']);

/** The answer to the predicates of the latest search, as the tree reads it. */
export class BieServerMarks {

  /** For each row (`table|hashPath`), the flags of the predicates it satisfies. */
  private readonly flagsByRow = new Map<string, string[]>();
  /** Every path that leads to a matched place (`a`, `a>b` for `a>b`), so a walk knows which branches to follow. */
  private readonly onTheWay = new Set<string>();

  constructor(readonly predicates: ServerPredicate[], response: BieSearchResponse, readonly caseSensitive: boolean) {
    for (const predicate of predicates) {
      for (const key of response.matches[predicate.id] ?? []) {
        const row = key.table + '|' + key.hashPath;
        const flags = this.flagsByRow.get(row) ?? [];
        flags.push(predicate.flag);
        this.flagsByRow.set(row, flags);
        const steps = (key.path ?? '').split('>');
        for (let i = 1; key.path && i <= steps.length; i++) {
          this.onTheWay.add(steps.slice(0, i).join('>'));
        }
      }
    }
  }

  /** The flags of the predicates that hold for the rows (a place has a row in several tables). */
  flagsOf(rows: {table: string, hashPath: string}[]): string[] {
    const flags = new Set<string>();
    for (const row of rows) {
      this.flagsByRow.get(row.table + '|' + row.hashPath)?.forEach(flag => flags.add(flag));
    }
    return [...flags];
  }

  /** Whether a matched place is at or below the path. */
  leadsToMatch(path: string): boolean {
    return this.onTheWay.has(path);
  }

  get isEmpty(): boolean {
    return this.flagsByRow.size === 0;
  }
}

/** The attributes of a BIE tree that the BIE search of the backend answers (the text set on the BIE itself). */
export const BIE_TEXT_ATTRIBUTES: ReadonlySet<string> =
  new Set(['definition', 'remark', 'example', 'fixed_value', 'default_value', 'business_term', 'value_domain_restriction', 'value_domain']);

/** The attributes of a BIE tree that are the texts of the core components it profiles, which the component search answers. */
export const BIE_COMPONENT_TEXT_ATTRIBUTES: ReadonlySet<string> =
  new Set(['association_definition', 'component_definition', 'type_definition']);

/**
 * The attributes of a BIE tree about the value domain of a BBIE or a BBIE_SC. What the BIE stores answers for the
 * places that have a row (BIE_TEXT_ATTRIBUTES); a place that has none starts with the default value domain of its data
 * type, which the component search answers. So these are searched in both, and a place is judged by one of them.
 */
export const BIE_VALUE_DOMAIN_ATTRIBUTES: ReadonlySet<string> = new Set(['value_domain_restriction', 'value_domain']);

/**
 * Asked besides the value domain predicates of a search: every place whose BIE has a row with a value domain. Such a
 * place is judged by its row and never by the default of its data type, and the places above it have to be told apart.
 */
export const BIE_VALUE_DOMAIN_ROW_PREDICATE: ServerPredicate =
  {id: 'value_domain_row', flag: serverFlagName('value_domain_row'), attribute: 'value_domain', operator: 'CONTAINS', value: ''};

/**
 * The attributes of a BBIEP or a BBIE_SC about the value constraint: a fixed value or a default value. A place whose BIE
 * stores one says that one; a place whose BIE stores none (it has no row, or its row has neither) says the one of the
 * core component the screen shows in its place: the BCCP of a BBIEP, the supplementary component of a BBIE_SC.
 */
export const BIE_VALUE_CONSTRAINT_ATTRIBUTES: ReadonlySet<string> = new Set(['fixed_value', 'default_value']);

/**
 * Asked besides the predicates of a search about a value constraint: every place whose BIE row stores a fixed value, and every
 * one that stores a default value. A place with either is judged by its row, and never by the value of its core component.
 */
export const BIE_VALUE_CONSTRAINT_ROW_PREDICATES: readonly ServerPredicate[] = [
  {id: 'fixed_value_row', flag: serverFlagName('fixed_value_row'), attribute: 'fixed_value', operator: 'CONTAINS', value: ''},
  {id: 'default_value_row', flag: serverFlagName('default_value_row'), attribute: 'default_value', operator: 'CONTAINS', value: ''}
];

/** The attributes the component search answers too, for the places the BIE stores nothing about. */
export const BIE_CC_FALLBACK_ATTRIBUTES: ReadonlySet<string> =
  new Set([...BIE_VALUE_DOMAIN_ATTRIBUTES, ...BIE_VALUE_CONSTRAINT_ATTRIBUTES]);

const NONE: ReadonlySet<string> = new Set();
const BOTH_KINDS: ReadonlySet<string> = BIE_CC_FALLBACK_ATTRIBUTES;

/** The attributes of the component search that a place does not answer, because its BIE row does. */
export function attributesOfRow(flags: Record<string, unknown> | undefined): ReadonlySet<string> | undefined {
  if (!flags) {
    return undefined;
  }
  const domain = !!flags[BIE_VALUE_DOMAIN_ROW_PREDICATE.flag];
  const constraint = BIE_VALUE_CONSTRAINT_ROW_PREDICATES.some(p => !!flags[p.flag]);
  if (domain && constraint) {
    return BOTH_KINDS;
  }
  return domain ? BIE_VALUE_DOMAIN_ATTRIBUTES : constraint ? BIE_VALUE_CONSTRAINT_ATTRIBUTES : NONE;
}
