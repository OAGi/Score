import {Injectable, inject} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom, Observable} from 'rxjs';
import {AttrValue} from '../../common/tree-search/search-expression';
import {ServerPredicate} from '../../common/tree-search/server-predicate';

/** The component a tree is rooted at, which tells the backend the release to search in. */
export interface CcSearchRoot {
  type: 'ACC' | 'ASCCP' | 'BCCP' | 'DT';
  manifestId: number;
}

/** The components of a release a predicate holds for, by type (manifest ids). */
export interface CcSearchNodes {
  accs: number[];
  asccs: number[];
  asccps: number[];
  bccs: number[];
  bccps: number[];
  dts: number[];
  dtScs: number[];
}

export interface CcSearchResponse {
  matches: Record<string, CcSearchNodes>;
}

/** Asks the backend about the definitions of the components of a tree. */
export type CcServerSearch = (root: CcSearchRoot, predicates: ServerPredicate[], caseSensitive: boolean) => Promise<CcSearchResponse>;

@Injectable({providedIn: 'root'})
export class CcSearchService {

  private http = inject(HttpClient);

  searchNodes(root: CcSearchRoot, predicates: ServerPredicate[], caseSensitive: boolean): Observable<CcSearchResponse> {
    return this.http.post<CcSearchResponse>('/api/core-components/search-nodes', {
      accManifestId: root.type === 'ACC' ? root.manifestId : null,
      asccpManifestId: root.type === 'ASCCP' ? root.manifestId : null,
      bccpManifestId: root.type === 'BCCP' ? root.manifestId : null,
      dtManifestId: root.type === 'DT' ? root.manifestId : null,
      predicates: predicates.map(p => ({
        id: p.id, attribute: p.attribute, operator: p.operator, value: p.value, caseSensitive
      }))
    });
  }
}

export type CcType = 'acc' | 'ascc' | 'asccp' | 'bcc' | 'bccp' | 'dt' | 'dtSc';

/** A component (or association) of the release, as the backend names it. */
export interface CcKey {
  type: CcType;
  manifestId: number;
}

/**
 * Which of the components a node shows have the text a search asks about. Every attribute is answered about the part of
 * the node it is about.
 */
export interface CcPlace {
  /** What the node is about: the association that ties it to its parent when it has one, else its component. */
  own?: CcKey;
  association?: CcKey;
  /** The ASCCP or BCCP a node shows (a component that has a definition of its own next to its association's). */
  component?: CcKey;
  /** The type of the node: the data type of a BCCP. */
  type?: CcKey;
  /** Where the node takes its fixed value and default value from, when it is not the one that ties it to its parent (a BIE tree: the BCCP, the DT_SC). */
  valueConstraint?: CcKey;
}

const TYPE_OF_FIELD: Record<CcType, keyof CcSearchNodes> = {
  acc: 'accs', ascc: 'asccs', asccp: 'asccps', bcc: 'bccs', bccp: 'bccps', dt: 'dts', dtSc: 'dtScs'
};

/** The answer to the predicates of the latest search, as a tree reads it. */
export class CcServerMarks {

  private readonly found = new Map<string, Set<string>>();

  constructor(readonly predicates: ServerPredicate[], response: CcSearchResponse, readonly caseSensitive: boolean) {
    for (const predicate of predicates) {
      const keys = new Set<string>();
      const nodes = response.matches[predicate.id];
      for (const type of Object.keys(TYPE_OF_FIELD) as CcType[]) {
        for (const manifestId of nodes?.[TYPE_OF_FIELD[type]] ?? []) {
          keys.add(type + ':' + manifestId);
        }
      }
      this.found.set(predicate.id, keys);
    }
  }

  /** Whether one of the predicates is about one of the attributes. */
  asksAbout(attributes: ReadonlySet<string>): boolean {
    return this.predicates.some(p => attributes.has(p.attribute));
  }

  /** The flags of the predicates that hold for the place, but those about the attributes in `except`. */
  flagsOf(place: CcPlace, except?: ReadonlySet<string>): Record<string, AttrValue> {
    const flags: Record<string, AttrValue> = {};
    for (const predicate of this.predicates) {
      if (except?.has(predicate.attribute)) {
        continue;
      }
      const key = this.keyFor(predicate.attribute, place);
      if (key && this.found.get(predicate.id)?.has(key.type + ':' + key.manifestId)) {
        flags[predicate.flag] = true;
      }
    }
    return flags;
  }

  private keyFor(attribute: string, place: CcPlace): CcKey | undefined {
    switch (attribute) {
      case 'association_definition':
        return place.association;
      case 'component_definition':
      case 'value_domain':
      case 'value_domain_restriction':
        return place.component;
      case 'type_definition':
        return place.type;
      case 'fixed_value':
      case 'default_value':
        return place.valueConstraint ?? place.own;
      default:
        return place.own;
    }
  }
}

/** The search of the backend as the trees ask for it. */
export function ccServerSearchOf(service: CcSearchService): CcServerSearch {
  return (root, predicates, caseSensitive) => firstValueFrom(service.searchNodes(root, predicates, caseSensitive));
}
