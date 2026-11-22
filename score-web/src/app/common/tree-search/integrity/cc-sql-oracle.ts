import {sql} from './integrity-env';

/**
 * The core components of one release read straight from the database, by SQL that does not use the application's own
 * queries: what the checks hold the backend's graph, and the searches over it, against.
 */

/** A text column as the database holds it: undefined for NULL. */
type Col = string | undefined;

export interface OAcc {
  id: number; basedId?: number; den: Col; objectClassTerm: Col; objectClassQualifier: Col; componentType: number; state: Col;
  guid: string; deprecated: boolean; abstract: boolean; tags: string[];
}

export interface OAscc {
  id: number; fromAcc: number; toAsccp: number; min: number; max: number; deprecated: boolean; state: Col;
}

export interface OAsccp {
  id: number; roleOfAcc: number; den: Col; propertyTerm: Col; state: Col; guid: string; deprecated: boolean; reusable: boolean;
  nillable: boolean; tags: string[];
}

export interface OBcc {
  id: number; fromAcc: number; toBccp: number; min: number; max: number; entityType: number; state: Col; deprecated: boolean; nillable: boolean;
}

export interface OBccp {
  id: number; bdt: number; den: Col; propertyTerm: Col; state: Col; guid: string; deprecated: boolean; nillable: boolean; tags: string[]; fixedValue: Col; defaultValue: Col;
}

export interface ODt {
  id: number; den: Col; dataTypeTerm: Col; qualifier: Col; state: Col; guid: string; commonlyUsed: boolean; deprecated: boolean; tags: string[];
}

export interface ODtSc {
  id: number; ownerDt: number; objectClassTerm: Col; propertyTerm: Col; representationTerm: Col; min: number; max: number;
  guid: string; deprecated: boolean; fixedValue: Col; defaultValue: Col;
}

const text = (s: string): Col => s === 'NULL' ? undefined : s;
const flag = (s: string): boolean => s === '1';

function tagsOf(table: string, key: string, release: number): Map<number, string[]> {
  const tags = new Map<number, string[]>();
  for (const [id, name] of sql(`select t.${key}, g.name from ${table} t join tag g on g.tag_id = t.tag_id join ${table.replace('_tag', '')} m ` +
    `on m.${key} = t.${key} where m.release_id = ${release} order by 1, 2`)) {
    const list = tags.get(Number(id)) ?? [];
    list.push(name);
    tags.set(Number(id), list);
  }
  return tags;
}

/** All the core components of a release. */
export class CcRelease {

  readonly accs = new Map<number, OAcc>();
  readonly asccs = new Map<number, OAscc>();
  readonly asccps = new Map<number, OAsccp>();
  readonly bccs = new Map<number, OBcc>();
  readonly bccps = new Map<number, OBccp>();
  readonly dts = new Map<number, ODt>();
  readonly dtScs = new Map<number, ODtSc>();
  readonly asccsOfAcc = new Map<number, OAscc[]>();
  readonly bccsOfAcc = new Map<number, OBcc[]>();
  readonly scsOfDt = new Map<number, ODtSc[]>();

  constructor(readonly release: number) {
    const accTags = tagsOf('acc_manifest_tag', 'acc_manifest_id', release);
    const asccpTags = tagsOf('asccp_manifest_tag', 'asccp_manifest_id', release);
    const bccpTags = tagsOf('bccp_manifest_tag', 'bccp_manifest_id', release);
    const dtTags = tagsOf('dt_manifest_tag', 'dt_manifest_id', release);

    for (const r of sql(`select m.acc_manifest_id, m.based_acc_manifest_id, m.den, a.object_class_term, a.object_class_qualifier, ` +
      `a.oagis_component_type, a.state, a.guid, a.is_deprecated, a.is_abstract from acc_manifest m join acc a on a.acc_id = m.acc_id ` +
      `where m.release_id = ${release}`)) {
      this.accs.set(Number(r[0]), {
        id: Number(r[0]), basedId: r[1] === 'NULL' ? undefined : Number(r[1]), den: text(r[2]), objectClassTerm: text(r[3]),
        objectClassQualifier: text(r[4]), componentType: Number(r[5]), state: text(r[6]), guid: r[7], deprecated: flag(r[8]),
        abstract: flag(r[9]), tags: accTags.get(Number(r[0])) ?? []
      });
    }
    for (const r of sql(`select m.ascc_manifest_id, m.from_acc_manifest_id, m.to_asccp_manifest_id, a.cardinality_min, a.cardinality_max, ` +
      `a.is_deprecated, a.state from ascc_manifest m join ascc a on a.ascc_id = m.ascc_id where m.release_id = ${release}`)) {
      const ascc: OAscc = {id: Number(r[0]), fromAcc: Number(r[1]), toAsccp: Number(r[2]), min: Number(r[3]), max: Number(r[4]), deprecated: flag(r[5]), state: text(r[6])};
      this.asccs.set(ascc.id, ascc);
      this.asccsOfAcc.set(ascc.fromAcc, [...(this.asccsOfAcc.get(ascc.fromAcc) ?? []), ascc]);
    }
    for (const r of sql(`select m.asccp_manifest_id, m.role_of_acc_manifest_id, m.den, a.property_term, a.state, a.guid, a.is_deprecated, ` +
      `a.reusable_indicator, a.is_nillable from asccp_manifest m join asccp a on a.asccp_id = m.asccp_id where m.release_id = ${release}`)) {
      this.asccps.set(Number(r[0]), {
        id: Number(r[0]), roleOfAcc: Number(r[1]), den: text(r[2]), propertyTerm: text(r[3]), state: text(r[4]), guid: r[5],
        deprecated: flag(r[6]), reusable: flag(r[7]), nillable: flag(r[8]), tags: asccpTags.get(Number(r[0])) ?? []
      });
    }
    for (const r of sql(`select m.bcc_manifest_id, m.from_acc_manifest_id, m.to_bccp_manifest_id, a.cardinality_min, a.cardinality_max, ` +
      `a.entity_type, a.state, a.is_deprecated, a.is_nillable from bcc_manifest m join bcc a on a.bcc_id = m.bcc_id where m.release_id = ${release}`)) {
      const bcc: OBcc = {id: Number(r[0]), fromAcc: Number(r[1]), toBccp: Number(r[2]), min: Number(r[3]), max: Number(r[4]),
        entityType: Number(r[5]), state: text(r[6]), deprecated: flag(r[7]), nillable: flag(r[8])};
      this.bccs.set(bcc.id, bcc);
      this.bccsOfAcc.set(bcc.fromAcc, [...(this.bccsOfAcc.get(bcc.fromAcc) ?? []), bcc]);
    }
    for (const r of sql(`select m.bccp_manifest_id, m.bdt_manifest_id, m.den, a.property_term, a.state, a.guid, a.is_deprecated, a.is_nillable, a.fixed_value, a.default_value ` +
      `from bccp_manifest m join bccp a on a.bccp_id = m.bccp_id where m.release_id = ${release}`)) {
      this.bccps.set(Number(r[0]), {
        id: Number(r[0]), bdt: Number(r[1]), den: text(r[2]), propertyTerm: text(r[3]), state: text(r[4]), guid: r[5],
        deprecated: flag(r[6]), nillable: flag(r[7]), tags: bccpTags.get(Number(r[0])) ?? [],
        fixedValue: text(r[8]), defaultValue: text(r[9])
      });
    }
    for (const r of sql(`select m.dt_manifest_id, m.den, a.data_type_term, a.qualifier, a.state, a.guid, a.commonly_used, a.is_deprecated ` +
      `from dt_manifest m join dt a on a.dt_id = m.dt_id where m.release_id = ${release}`)) {
      this.dts.set(Number(r[0]), {
        id: Number(r[0]), den: text(r[1]), dataTypeTerm: text(r[2]), qualifier: text(r[3]), state: text(r[4]), guid: r[5],
        commonlyUsed: flag(r[6]), deprecated: flag(r[7]), tags: dtTags.get(Number(r[0])) ?? []
      });
    }
    for (const r of sql(`select m.dt_sc_manifest_id, m.owner_dt_manifest_id, a.object_class_term, a.property_term, a.representation_term, ` +
      `a.cardinality_min, a.cardinality_max, a.guid, a.is_deprecated, a.fixed_value, a.default_value from dt_sc_manifest m join dt_sc a on a.dt_sc_id = m.dt_sc_id ` +
      `where m.release_id = ${release}`)) {
      const sc: ODtSc = {id: Number(r[0]), ownerDt: Number(r[1]), objectClassTerm: text(r[2]), propertyTerm: text(r[3]),
        representationTerm: text(r[4]), min: Number(r[5]), max: Number(r[6]), guid: r[7], deprecated: flag(r[8]),
        fixedValue: text(r[9]), defaultValue: text(r[10])};
      this.dtScs.set(sc.id, sc);
      this.scsOfDt.set(sc.ownerDt, [...(this.scsOfDt.get(sc.ownerDt) ?? []), sc]);
    }
  }

  /** The components reachable from a root, by kind (manifest ids): what its graph has to contain. */
  reachable(rootType: 'ACC' | 'ASCCP' | 'BCCP' | 'DT', rootId: number): {accs: Set<number>, asccs: Set<number>, asccps: Set<number>,
    bccs: Set<number>, bccps: Set<number>, dts: Set<number>, dtScs: Set<number>} {
    const seen = {accs: new Set<number>(), asccs: new Set<number>(), asccps: new Set<number>(), bccs: new Set<number>(),
      bccps: new Set<number>(), dts: new Set<number>(), dtScs: new Set<number>()};
    const visitAcc = (id: number) => {
      if (seen.accs.has(id) || !this.accs.has(id)) {
        return;
      }
      seen.accs.add(id);
      const acc = this.accs.get(id);
      if (acc.basedId !== undefined) {
        visitAcc(acc.basedId);
      }
      for (const ascc of this.asccsOfAcc.get(id) ?? []) {
        seen.asccs.add(ascc.id);
        visitAsccp(ascc.toAsccp);
      }
      for (const bcc of this.bccsOfAcc.get(id) ?? []) {
        seen.bccs.add(bcc.id);
        visitBccp(bcc.toBccp);
      }
    };
    const visitAsccp = (id: number) => {
      if (seen.asccps.has(id) || !this.asccps.has(id)) {
        return;
      }
      seen.asccps.add(id);
      visitAcc(this.asccps.get(id).roleOfAcc);
    };
    const visitBccp = (id: number) => {
      if (seen.bccps.has(id) || !this.bccps.has(id)) {
        return;
      }
      seen.bccps.add(id);
      visitDt(this.bccps.get(id).bdt);
    };
    const visitDt = (id: number) => {
      if (seen.dts.has(id) || !this.dts.has(id)) {
        return;
      }
      seen.dts.add(id);
      for (const sc of this.scsOfDt.get(id) ?? []) {
        seen.dtScs.add(sc.id);
      }
    };
    switch (rootType) {
      case 'ACC': visitAcc(rootId); break;
      case 'ASCCP': visitAsccp(rootId); break;
      case 'BCCP': visitBccp(rootId); break;
      default: visitDt(rootId);
    }
    return seen;
  }
}
