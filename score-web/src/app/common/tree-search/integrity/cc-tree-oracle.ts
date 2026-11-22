import {CcRelease, OAcc, OAscc, OAsccp, OBcc, OBccp, ODt, ODtSc} from './cc-sql-oracle';

/**
 * The component tree as the screens show it, unfolded from the database rows, so that what a search counts in it can be
 * counted without the application's own search structures. The rules are those of the tree (`CcFlatNodeDatabase`): the
 * base ACC is the first child of an ACC, an ASCC shows its ASCCP (whose children are the ACC it is the role of), a
 * User Extension Group is replaced by what its ACC holds, a BCC shows its BCCP (an attribute has no children, an element
 * has the supplementary components of its data type that can occur), and an ASCCP that is among its own ancestors is not
 * expanded.
 */
export interface ONode {
  type: 'ACC' | 'ASCCP' | 'BCCP' | 'DT' | 'DT_SC';
  acc?: OAcc;
  asccp?: OAsccp;
  bccp?: OBccp;
  dt?: ODt;
  sc?: ODtSc;
  /** The association that ties the node to its parent. */
  ascc?: OAscc;
  bcc?: OBcc;
  /** What the rows of a BIE below this place say about the places below it (see `RowTrail`). */
  trail?: RowTrail;
  /** The row of the BIE that is this place's own. */
  row?: BieRow;
}

/** What a row of a BIE keeps about a place: its cardinality (the BIE's own where it is used), the fixed and the default value. */
export interface BieRow {
  used: boolean;
  min: number;
  max: number;
  fixed?: string;
  defaultValue?: string;
}

/**
 * The rows of a BIE by the path of the place each is the row of (`ASCCP-1>ACC-2>ASCC-3>...`), so that the unfolding can say, for a
 * place, what the BIE keeps for it. A place the unfolding does not reach (a path through a User Extension Group, or one the
 * graph no longer has) is never visited, and so its row counts for nothing.
 */
export class RowTrail {
  readonly next = new Map<string, RowTrail>();
  row?: BieRow;

  static of(rows: {path: string, row: BieRow}[]): RowTrail {
    const root = new RowTrail();
    for (const {path, row} of rows) {
      let at = root;
      for (const token of path.split('>')) {
        let child = at.next.get(token);
        if (!child) {
          child = new RowTrail();
          at.next.set(token, child);
        }
        at = child;
      }
      at.row = row;
    }
    return root;
  }
}

const COMPONENT_TYPES = ['Base', 'Semantics', 'Extension', 'SemanticGroup', 'UserExtensionGroup', 'Embedded', 'OAGIS10Nouns',
  'OAGIS10BODs', 'BOD', 'Verb', 'Noun', 'Choice', 'AttributeGroup'];

export type Rows = Record<string, string | number | boolean | readonly string[] | undefined>;

export class CcTreeOracle {

  private readonly memo = new Map<string, number>();
  steps = 0;

  /** @param blockAccs whether an ACC is also not expanded below itself (the search engine's rule), besides an ASCCP (the tree's) */
  constructor(private readonly data: CcRelease, private readonly blockAccs = false, private readonly budget = 30_000_000,
              /** whether a semantic group is expanded even when it is among its own ancestors (the BIE trees do not look for a cycle in it) */
              private readonly openGroups = false) {
  }

  /** The root node of a tree. */
  root(type: 'ACC' | 'ASCCP' | 'BCCP' | 'DT', id: number): ONode {
    switch (type) {
      case 'ACC': return {type, acc: this.data.accs.get(id)};
      case 'ASCCP': return {type, asccp: this.data.asccps.get(id)};
      case 'BCCP': return {type, bccp: this.data.bccps.get(id), dt: this.data.dts.get(this.data.bccps.get(id).bdt)};
      default: return {type, dt: this.data.dts.get(id)};
    }
  }

  /** The root of a tree that knows the rows of a BIE. */
  rootWith(node: ONode, trail: RowTrail): ONode {
    const next = trail.next.get(this.tokens(node)[0]);
    return next ? {...node, trail: next} : node;
  }

  /** The path tokens of what a place adds to the path of its parent. */
  private tokens(node: ONode): string[] {
    switch (node.type) {
      case 'ASCCP': return node.ascc ? ['ASCC-' + node.ascc.id, 'ASCCP-' + node.asccp.id] : ['ASCCP-' + node.asccp.id];
      case 'ACC': return ['ACC-' + node.acc.id];
      case 'BCCP': return node.bcc ? ['BCC-' + node.bcc.id, 'BCCP-' + node.bccp.id] : ['BCCP-' + node.bccp.id];
      case 'DT_SC': return ['DT-' + node.dt.id, 'DT_SC-' + node.sc.id];
      default: return ['DT-' + node.dt.id];
    }
  }

  /** The child as the BIE of the rows shows it: with the cardinality its row sets, and the rows below it. */
  private shown(child: ONode, trail: RowTrail): ONode {
    const tokens = this.tokens(child);
    const first = trail.next.get(tokens[0]);
    if (!first) {
      return child;
    }
    const second = tokens.length > 1 ? first.next.get(tokens[1]) : undefined;
    const row = (child.type === 'DT_SC' ? second : first)?.row;
    const below = tokens.length > 1 ? second : first;
    if (!row && !below) {
      return child;
    }
    const shown: ONode = {...child, trail: below, row};
    if (row?.used) {
      if (child.ascc) { shown.ascc = {...child.ascc, min: row.min, max: row.max}; }
      if (child.bcc) { shown.bcc = {...child.bcc, min: row.min, max: row.max}; }
      if (child.type === 'DT_SC') { shown.sc = {...child.sc, min: row.min, max: row.max}; }
    }
    return shown;
  }

  /** The children of a place, as the BIE of the rows shows them (the children of the tree when it knows no rows). */
  kids(node: ONode, ancestors: readonly number[] = []): ONode[] {
    const children = this.children(node, ancestors);
    return node.trail ? children.map(child => this.shown(child, node.trail)) : children;
  }

  private isUserExtensionGroup(asccp: OAsccp): boolean {
    return (asccp.propertyTerm ?? '').endsWith('User Extension Group');
  }

  children(node: ONode, ancestors: readonly number[] = []): ONode[] {
    switch (node.type) {
      case 'ASCCP': {
        const acc = this.data.accs.get(node.asccp.roleOfAcc);
        return acc ? [{type: 'ACC', acc}] : [];
      }
      case 'ACC': {
        const result: ONode[] = [];
        if (node.acc.basedId !== undefined && this.data.accs.has(node.acc.basedId)) {
          result.push({type: 'ACC', acc: this.data.accs.get(node.acc.basedId)});
        }
        for (const ascc of this.data.asccsOfAcc.get(node.acc.id) ?? []) {
          const asccp = this.data.asccps.get(ascc.toAsccp);
          if (!asccp) {
            continue;
          }
          if (this.isUserExtensionGroup(asccp)) {
            const uegAcc = this.data.accs.get(asccp.roleOfAcc);
            if (uegAcc) {
              result.push(...this.children({type: 'ACC', acc: uegAcc}, ancestors));
            }
          } else {
            result.push({type: 'ASCCP', asccp, ascc});
          }
        }
        for (const bcc of this.data.bccsOfAcc.get(node.acc.id) ?? []) {
          const bccp = this.data.bccps.get(bcc.toBccp);
          if (bccp) {
            result.push({type: 'BCCP', bccp, dt: this.data.dts.get(bccp.bdt), bcc});
          }
        }
        return result;
      }
      case 'BCCP':
        if (node.bcc?.entityType === 0) {
          return [];
        }
        return (node.dt ? this.data.scsOfDt.get(node.dt.id) ?? [] : []).filter(sc => node.bcc ? sc.max > 0 : true)
          .map(sc => ({type: 'DT_SC', sc, dt: node.dt} as ONode));
      case 'DT':
        return (this.data.scsOfDt.get(node.dt.id) ?? []).map(sc => ({type: 'DT_SC', sc, dt: node.dt} as ONode));
      default:
        return [];
    }
  }

  /** What makes a node the component it shows (the rule "not expanded below itself" goes by it), if the rule applies to it. */
  private identity(node: ONode): string | undefined {
    if (node.type === 'ASCCP') {
      return 'P' + node.asccp.id;
    }
    return node.type === 'ACC' && this.blockAccs ? 'A' + node.acc.id : undefined;
  }

  private isGroup(node: ONode): boolean {
    return node.type === 'ASCCP' && COMPONENT_TYPES[this.data.accs.get(node.asccp.roleOfAcc)?.componentType]?.endsWith('Group');
  }

  private readonly reachMemo = new Map<string, Set<string>>();

  /** The identities of the nodes that can occur below a node, however it is reached. */
  private reach(node: ONode): Set<string> {
    const key = this.key(node);
    let found = this.reachMemo.get(key);
    if (!found) {
      found = new Set<string>();
      this.reachMemo.set(key, found);
      const visited = new Set<string>();
      const stack = [node];
      while (stack.length > 0) {
        const n = stack.pop();
        const k = this.key(n);
        if (visited.has(k)) {
          continue;
        }
        visited.add(k);
        const id = this.identity(n);
        if (id !== undefined) {
          found.add(id);
        }
        stack.push(...this.children(n, []));
      }
      const own = this.identity(node);
      // a node is below itself only when something below it shows the same component
      if (own !== undefined && !this.reachesItself(node)) {
        found.delete(own);
      }
    }
    return found;
  }

  private reachesItself(node: ONode): boolean {
    const own = this.identity(node);
    const visited = new Set<string>();
    const stack = this.children(node, []);
    while (stack.length > 0) {
      const n = stack.pop();
      if (this.identity(n) === own) {
        return true;
      }
      const k = this.key(n);
      if (visited.has(k)) {
        continue;
      }
      visited.add(k);
      stack.push(...this.children(n, []));
    }
    return false;
  }

  private key(node: ONode): string {
    return node.type + ':' + (node.sc?.id ?? node.acc?.id ?? node.asccp?.id ?? node.bccp?.id ?? node.dt?.id)
      + (node.ascc ? '/' + node.ascc.id : '') + (node.bcc ? '/' + node.bcc.id : '');
  }

  /** How many nodes of the tree satisfy the test (the root included). */
  count(root: ONode, test: (node: ONode, blocked: boolean) => boolean): number {
    this.memo.clear();
    this.steps = 0;
    const walk = (node: ONode, ancestors: readonly string[]): number => {
      if (++this.steps > this.budget) {
        throw new Error('The oracle ran out of steps');
      }
      const own = this.identity(node);
      const blocked = own !== undefined && ancestors.includes(own) && !(this.openGroups && this.isGroup(node));
      const relevant = blocked ? [] : ancestors.filter(a => a === own || this.reach(node).has(a)).sort();
      // a place with rows of a BIE below it is not the same as the other places of its component
      const special = !!node.trail || !!node.row;
      const memoKey = this.key(node) + (blocked ? '!' : '') + '|' + relevant.join(',');
      const known = special ? undefined : this.memo.get(memoKey);
      if (known !== undefined) {
        return known;
      }
      let total = test(node, blocked) ? 1 : 0;
      if (!blocked) {
        const next = own !== undefined ? [...ancestors, own] : ancestors;
        for (const child of this.kids(node, [])) {
          total += walk(child, next);
        }
      }
      if (!special) {
        this.memo.set(memoKey, total);
      }
      return total;
    };
    return walk(root, []);
  }
}

/** What a search predicate may ask about a node, from the rows (the meaning each attribute is documented to have). */
export function viewOf(node: ONode): Rows {
  const component = node.type === 'DT_SC' ? node.sc : node.acc ?? node.asccp ?? node.bccp ?? node.dt;
  const association = node.ascc ?? node.bcc ?? node.sc;
  const card = node.ascc ?? node.bcc ?? node.sc;
  return {
    type: node.type,
    object_class_term: node.acc?.objectClassTerm ?? node.sc?.objectClassTerm,
    property_term: node.asccp?.propertyTerm ?? node.bccp?.propertyTerm ?? node.sc?.propertyTerm,
    den: node.acc?.den ?? node.asccp?.den ?? node.bccp?.den ?? (node.type === 'DT' ? node.dt.den : undefined),
    data_type_term: node.dt && node.type === 'DT' ? node.dt.dataTypeTerm : undefined,
    representation_term: node.sc?.representationTerm,
    qualifier: node.type === 'DT' ? node.dt.qualifier : undefined,
    component_type: node.acc ? COMPONENT_TYPES[node.acc.componentType] : undefined,
    state: node.type === 'DT_SC' ? node.dt?.state : (component as {state?: string})?.state,
    guid: (component as {guid?: string})?.guid,
    deprecated: !!((component as {deprecated?: boolean})?.deprecated || (node.ascc ?? node.bcc)?.deprecated
      || (node.type === 'BCCP' && node.dt?.deprecated)),
    tag: (component as {tags?: string[]})?.tags,
    object_class_qualifier: node.acc?.objectClassQualifier ? node.acc.objectClassQualifier : undefined,
    abstract: node.acc?.abstract,
    reusable: node.asccp?.reusable,
    commonly_used: node.type === 'DT' ? node.dt.commonlyUsed : undefined,
    nillable: node.asccp || node.bccp ? !!(node.asccp?.nillable || node.bccp?.nillable || node.bcc?.nillable) : undefined,
    entity_type: node.bcc ? (node.bcc.entityType === 0 ? 'Attribute' : 'Element') : undefined,
    cardinality_min: card?.min,
    cardinality_max: card ? (card.max < 0 ? Infinity : card.max) : undefined,
    $association: association ? 1 : 0
  };
}
