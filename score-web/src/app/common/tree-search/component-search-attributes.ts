import {AttrValue} from './search-expression';
import {TEXT_ATTRIBUTE} from './search-eval';

/** How often a node may occur under its parent: the cardinality of the ASCC, BCC or DT_SC that ties it there (max -1: unbounded). */
export interface NodeCardinality {
  min?: number;
  max?: number;
}

/** `cardinality_min` and `cardinality_max` of the node; an unbounded maximum is infinite, so that `cardinality_max < 10` leaves it out. */
export function cardinalityAttributes(cardinality?: NodeCardinality): Record<string, AttrValue> {
  const known = cardinality && cardinality.min !== undefined && cardinality.max !== undefined;
  return {
    cardinality_min: known ? cardinality.min : undefined,
    cardinality_max: known ? (cardinality.max < 0 ? Infinity : cardinality.max) : undefined
  };
}

/**
 * What makes a node with a cardinality another record in the tree's graph: the same component under another
 * association is the same node with another cardinality, which a search must be able to tell apart.
 */
export function cardinalityKey(cardinality?: NodeCardinality): string {
  return cardinality && cardinality.min !== undefined && cardinality.max !== undefined ?
    '#' + cardinality.min + '..' + (cardinality.max < 0 ? '*' : cardinality.max) : '';
}

/** The part of a core component (as the CC graph describes it) a search can ask about; each kind has its own columns. */
export interface ComponentTerms {
  objectClassTerm?: string;
  propertyTerm?: string;
  den?: string;
  dataTypeTerm?: string;
  representationTerm?: string;
  qualifier?: string;
  componentType?: string;
  state?: string;
  guid?: string;
  deprecated?: boolean;
  definition?: string;
}

/** The association (ASCC or BCC) that ties a component to its parent, which has a definition of its own (and a BCC an entity type). */
export interface AssociationTerms {
  entityType?: string;
  definition?: string;
}

/**
 * What a search predicate may ask about a node of a tree: its `type`, the columns of the core component it shows under
 * the names the database gives them (`object_class_term` of an ACC, `property_term` of an ASCCP or BCCP, `den`,
 * `data_type_term`, `representation_term`, `component_type`, ...: a component has only its own kind's, and a test on
 * a column it has not is false), and the `entity_type` of the BCC that ties it to its parent. `text` is what the
 * node reads in the tree, which is what `text()` answers. The definitions a row shows come from `definitionAttributes`.
 */
export function componentSearchAttributes(
  type: string, text: string, component?: ComponentTerms, association?: AssociationTerms,
  cardinality?: NodeCardinality): Record<string, AttrValue> {
  return {
    ...cardinalityAttributes(cardinality),
    [TEXT_ATTRIBUTE]: text, type,
    object_class_term: component?.objectClassTerm, property_term: component?.propertyTerm, den: component?.den,
    data_type_term: component?.dataTypeTerm, representation_term: component?.representationTerm, qualifier: component?.qualifier,
    component_type: component?.componentType, state: component?.state, guid: component?.guid, deprecated: component?.deprecated,
    entity_type: association?.entityType
  };
}

/**
 * The definitions a row shows, each asked for by its own name (`definition`, `association_definition`, ...): the value of a
 * part is its definition, empty when it has none, and a part the row does not have is not there at all, so that
 * `definition = ''` finds the nodes that have a part without a definition and no others.
 */
export function definitionAttributes(parts: Record<string, { definition?: string } | undefined>): Record<string, AttrValue> {
  const attributes: Record<string, AttrValue> = {};
  for (const name of Object.keys(parts)) {
    attributes[name] = parts[name] ? parts[name].definition ?? '' : undefined;
  }
  return attributes;
}

/**
 * What makes the nodes of a component differ by the association that ties them to their parent: the same component under
 * another association is another node when the associations are defined differently.
 */
export function associationKey(association?: AssociationTerms): string {
  return association?.definition ? '$' + association.definition : '';
}
