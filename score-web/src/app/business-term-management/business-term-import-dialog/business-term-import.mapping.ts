import {
  ColumnMapping,
  DetectedFormat,
  ImportRow,
  TargetField,
  UriMode,
} from './business-term-import.model';
import {isValidUri} from './business-term-import.validation';

/**
 * Header aliases used to auto-map common Enterprise Data Platform exports onto connectCenter fields.
 * Matching is on a normalized header (lower-cased, separators removed). The required external
 * reference URI is handled separately (see {@link autoDetect}) because most EDP exports lack a
 * single URI column.
 */
// Each alias list is PRIORITY-ORDERED (see autoDetect's findByPriority): the first alias that
// uniquely matches a column wins, so a canonical header (e.g. "Definition") is preferred over a
// looser synonym ("Description") when a source carries both. Aliases cover the common Enterprise
// Data Platform exports — Collibra, Alation, Microsoft Purview, Informatica CDGC, IBM Knowledge
// Catalog, erwin, Atlan, data.world, SAP Information Steward, Google Dataplex.
const FIELD_ALIASES: Record<TargetField, string[]> = {
  businessTerm: ['businessterm', 'name', 'title', 'term', 'termname', 'termdisplayname',
    'displayname', 'preferredterm', 'glossaryterm', 'businessname'],
  externalReferenceId: ['externalreferenceid', 'id', 'assetid', 'artifactid', 'termid',
    'technicalname', 'qualifiedname', 'referenceid', 'guid', 'uuid', 'identifier'],
  definition: ['definition', 'businessdefinition', 'description', 'businessdescription',
    'userdescription', 'shortdescription', 'longdescription', 'meaning', 'desc'],
  comment: ['comment', 'comments', 'note', 'notes', 'remark', 'remarks', 'descriptiveexample',
    'annotation'],
};

const URI_ALIASES = ['externalreferenceuri', 'uri', 'url', 'link', 'asseturl', 'weburl',
  'resourceiri', 'iri', 'permalink', 'href'];

function normalizeHeader(header: string): string {
  // Lower-case, then strip a leading "group:" qualifier (Informatica prefixes headers like
  // "Core: Name" / "Business: Description"), then drop ALL non-alphanumerics so punctuation-laden
  // headers such as "Resource (IRI)", "Artifact ID", or "term_display_name" still match an alias.
  return (header ?? '')
    .toLowerCase()
    .trim()
    .replace(/^[a-z]+:\s*/, '')
    .replace(/[^a-z0-9]/g, '');
}

/**
 * Inspect the source headers and propose a default mapping. A target is left unmapped when no
 * column matches, or when more than one column matches (ambiguous — the user picks). When no URI
 * column is present the strategy defaults to SYNTHESIZE (base URL + id column).
 */
export function autoDetect(headers: string[]): { format: DetectedFormat; mapping: ColumnMapping } {
  const norm = headers.map(h => ({raw: h, n: normalizeHeader(h)}));

  // Walk the aliases in priority order; the first alias that matches exactly one column wins. This
  // both prefers the canonical header over looser synonyms and auto-maps even when a less-specific
  // synonym is also present (e.g. a source with both "Definition" and "Description").
  const findByPriority = (aliases: string[]): string | null => {
    for (const alias of aliases) {
      const matches = norm.filter(x => x.n === alias);
      if (matches.length === 1) {
        return matches[0].raw;
      }
    }
    return null;
  };

  const fieldToColumn: Record<TargetField, string | null> = {
    businessTerm: findByPriority(FIELD_ALIASES.businessTerm),
    externalReferenceId: findByPriority(FIELD_ALIASES.externalReferenceId),
    definition: findByPriority(FIELD_ALIASES.definition),
    comment: findByPriority(FIELD_ALIASES.comment),
  };

  const uriColumn = findByPriority(URI_ALIASES);
  const uriMode: UriMode = uriColumn ? 'COLUMN' : 'SYNTHESIZE';
  const idLike = norm.find(x => x.n === 'id' || x.n.endsWith('id'));
  // When the source has no URI column, synthesize one. Prefer a mapped id, then any id-like column,
  // and finally fall back to the business-term column so a glossary export that ships neither a URL
  // nor an id (e.g. Microsoft Purview) can still form a stable "base URL + term" reference.
  const uriIdColumn = (uriMode === 'SYNTHESIZE')
    ? (fieldToColumn.externalReferenceId ?? (idLike ? idLike.raw : null) ?? fieldToColumn.businessTerm)
    : null;

  const mapping: ColumnMapping = {
    fieldToColumn,
    uriMode,
    uriColumn,
    uriBase: '',
    uriIdColumn,
  };

  const ns = norm.map(x => x.n);
  const has = (...keys: string[]) => keys.some(k => ns.includes(k));
  let format: DetectedFormat = 'UNKNOWN';
  if (ns.includes('businessterm') && ns.includes('externalreferenceuri')) {
    format = 'NATIVE';
  } else if (ns.includes('title') && has('glossaries', 'template', 'groupprofile')) {
    format = 'ALATION';
  } else if (ns.includes('name') && has('assettype', 'community', 'domain')) {
    format = 'COLLIBRA';
  } else if (ns.includes('name') && ns.includes('nickname') && has('isdefinitionrichtext', 'acronym')) {
    format = 'PURVIEW';
  } else if (fieldToColumn.businessTerm) {
    // Headers we don't recognize as a specific product, but the term-name column auto-mapped, so the
    // import will still work — surface it as a recognized generic catalog export rather than "unknown".
    format = 'GENERIC';
  }

  return {format, mapping};
}

/** Build a URI from a base URL + id (used when the source has no URI column). */
export function synthesizeUri(base: string, id: string): string {
  const b = (base ?? '').trim();
  const i = (id ?? '').trim();
  if (!b || !i) {
    return '';
  }
  const sep = b.endsWith('/') ? '' : '/';
  return b + sep + encodeURIComponent(i);
}

/** Resolve a row's external reference URI from the chosen strategy. */
export function resolveUri(mapping: ColumnMapping, source: Record<string, string>): string {
  if (mapping.uriMode === 'COLUMN') {
    return mapping.uriColumn ? (source[mapping.uriColumn] ?? '') : '';
  }
  return synthesizeUri(mapping.uriBase, mapping.uriIdColumn ? (source[mapping.uriIdColumn] ?? '') : '');
}

/** Project the parsed source rows onto editable {@link ImportRow}s using the mapping. */
export function buildImportRows(sourceRows: Record<string, string>[], mapping: ColumnMapping): ImportRow[] {
  const col = (name: string | null, src: Record<string, string>) => (name ? (src[name] ?? '') : '');
  return sourceRows.map((src, idx) => ({
    id: idx,
    rowIndex: idx + 1,
    businessTerm: col(mapping.fieldToColumn.businessTerm, src),
    externalReferenceUri: resolveUri(mapping, src),
    externalReferenceId: col(mapping.fieldToColumn.externalReferenceId, src),
    definition: col(mapping.fieldToColumn.definition, src),
    comment: col(mapping.fieldToColumn.comment, src),
    errors: [],
    userDeselected: false,
  }));
}

/** Whether the mapping has enough information to build a preview (name + a usable URI strategy). */
export function isMappingComplete(mapping: ColumnMapping | null): boolean {
  if (!mapping || !mapping.fieldToColumn.businessTerm) {
    return false;
  }
  if (mapping.uriMode === 'COLUMN') {
    return !!mapping.uriColumn;
  }
  return !!(mapping.uriBase && mapping.uriBase.trim()) && isValidUri(mapping.uriBase.trim()) && !!mapping.uriIdColumn;
}
