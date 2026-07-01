import {autoDetect, buildImportRows, isMappingComplete, resolveUri, synthesizeUri} from './business-term-import.mapping';
import {ColumnMapping} from './business-term-import.model';

/**
 * #1754: auto-detect / column-mapping coverage for the export formats real commercial Business
 * Glossary tools produce. Header sets below are the verified, real-world export columns (Collibra,
 * Alation, Microsoft Purview, Informatica CDGC, IBM Knowledge Catalog, erwin, Atlan, data.world,
 * SAP Information Steward, Google Dataplex). Each tool's term-name column must auto-map and a usable
 * URI strategy must be picked, so an import needs at most a single "base URL" entry — never manual
 * column mapping.
 */
describe('autoDetect — commercial Business Glossary exports', () => {

  it('native template -> NATIVE, URI column, mapping complete with zero input', () => {
    const headers = ['businessTerm', 'externalReferenceUri', 'externalReferenceId', 'definition', 'comment'];
    const {format, mapping} = autoDetect(headers);
    expect(format).toBe('NATIVE');
    expect(mapping.fieldToColumn.businessTerm).toBe('businessTerm');
    expect(mapping.uriMode).toBe('COLUMN');
    expect(mapping.uriColumn).toBe('externalReferenceUri');
    expect(isMappingComplete(mapping)).toBe(true);
  });

  it('Collibra -> COLLIBRA; Name (not Full name), ID, synthesize from ID', () => {
    const headers = ['ID', 'Name', 'Full name', 'Asset type', 'Domain', 'Community', 'Status',
      'Definition', 'Descriptive Example', 'Generalization'];
    const {format, mapping} = autoDetect(headers);
    expect(format).toBe('COLLIBRA');
    expect(mapping.fieldToColumn.businessTerm).toBe('Name');       // not 'Full name'
    expect(mapping.fieldToColumn.externalReferenceId).toBe('ID');
    expect(mapping.fieldToColumn.definition).toBe('Definition');
    expect(mapping.fieldToColumn.comment).toBe('Descriptive Example'); // #1754: comment <- Descriptive Example
    expect(mapping.uriMode).toBe('SYNTHESIZE');
    expect(mapping.uriIdColumn).toBe('ID');
  });

  it('Alation -> ALATION; title/description/id', () => {
    const headers = ['id', 'title', 'description', 'glossaries', 'template', 'steward:groupprofile', 'Status', 'action'];
    const {format, mapping} = autoDetect(headers);
    expect(format).toBe('ALATION');
    expect(mapping.fieldToColumn.businessTerm).toBe('title');
    expect(mapping.fieldToColumn.definition).toBe('description');
    expect(mapping.fieldToColumn.externalReferenceId).toBe('id');
    expect(mapping.uriIdColumn).toBe('id');
  });

  it('Microsoft Purview -> PURVIEW; no id -> synthesize from the term name', () => {
    const headers = ['Name', 'Nick Name', 'Status', 'Definition', 'IsDefinitionRichText', 'Acronym',
      'Resources', 'Related Terms', 'Synonyms', 'Stewards', 'Experts', 'Parent Term'];
    const {format, mapping} = autoDetect(headers);
    expect(format).toBe('PURVIEW');
    expect(mapping.fieldToColumn.businessTerm).toBe('Name');
    expect(mapping.fieldToColumn.definition).toBe('Definition');
    expect(mapping.uriMode).toBe('SYNTHESIZE');
    expect(mapping.uriIdColumn).toBe('Name');                      // fallback to the term column
  });

  it('Informatica CDGC -> GENERIC; "Core:"/"Business:" prefixes are stripped for matching', () => {
    const headers = ['Core: Reference ID', 'Core: Name', 'Business: Description', 'Parent Reference Id'];
    const {format, mapping} = autoDetect(headers);
    expect(format).toBe('GENERIC');
    expect(mapping.fieldToColumn.businessTerm).toBe('Core: Name');
    expect(mapping.fieldToColumn.externalReferenceId).toBe('Core: Reference ID');
    expect(mapping.fieldToColumn.definition).toBe('Business: Description');
  });

  it('IBM Knowledge Catalog -> GENERIC; Artifact ID maps to the id field', () => {
    const headers = ['Name', 'Artifact Type', 'Category', 'Artifact ID', 'Description', 'Tags',
      'Stewards', 'Classifications', 'Related Terms', 'Business Start', 'Business End'];
    const {mapping} = autoDetect(headers);
    expect(mapping.fieldToColumn.businessTerm).toBe('Name');
    expect(mapping.fieldToColumn.externalReferenceId).toBe('Artifact ID');
    expect(mapping.fieldToColumn.definition).toBe('Description');
    expect(mapping.uriIdColumn).toBe('Artifact ID');
  });

  it('erwin -> GENERIC; prefers Definition over Description, comment from Notes', () => {
    const headers = ['Catalog Name', 'Path', 'Business Term', 'Definition', 'Description', 'Notes',
      'Acronym', 'Governance Responsibilities', 'Classification', 'User Defined 1', 'User Defined 2'];
    const {mapping} = autoDetect(headers);
    expect(mapping.fieldToColumn.businessTerm).toBe('Business Term');
    expect(mapping.fieldToColumn.definition).toBe('Definition');   // priority over 'Description'
    expect(mapping.fieldToColumn.comment).toBe('Notes');
  });

  it('Atlan -> GENERIC; qualifiedName as id, userDescription as definition', () => {
    const headers = ['typeName', 'name', 'qualifiedName', 'anchor', 'userDescription', 'categories',
      'certificateStatus', 'certificateStatusMessage', 'ownerUsers', 'ownerGroups', 'atlanTags',
      'links', 'readme', 'synonyms', 'seeAlso'];
    const {mapping} = autoDetect(headers);
    expect(mapping.fieldToColumn.businessTerm).toBe('name');
    expect(mapping.fieldToColumn.externalReferenceId).toBe('qualifiedName');
    expect(mapping.fieldToColumn.definition).toBe('userDescription');
  });

  it('data.world -> GENERIC; "Resource (IRI)" recognized as the URI column', () => {
    const headers = ['Name', 'Description', 'Summary', 'Status', 'Resource (IRI)'];
    const {mapping} = autoDetect(headers);
    expect(mapping.fieldToColumn.businessTerm).toBe('Name');
    expect(mapping.uriMode).toBe('COLUMN');
    expect(mapping.uriColumn).toBe('Resource (IRI)');
    expect(isMappingComplete(mapping)).toBe(true);                 // ready with zero input
  });

  it('SAP Information Steward -> GENERIC; Technical Name as id', () => {
    const headers = ['Technical Name', 'Name', 'Categories', 'Description', 'Keywords', 'Synonyms',
      'Author', 'Approver', 'Observer', 'Status', 'Related Terms',
      'Name Paths of Associated Objects', 'Type Paths of Associated Objects'];
    const {mapping} = autoDetect(headers);
    expect(mapping.fieldToColumn.businessTerm).toBe('Name');
    expect(mapping.fieldToColumn.externalReferenceId).toBe('Technical Name');
    expect(mapping.fieldToColumn.definition).toBe('Description');
  });

  it('Google Dataplex -> GENERIC; term_display_name as the term name', () => {
    const headers = ['term_display_name', 'description', 'steward', 'tagged_assets', 'synonyms',
      'related_terms', 'belongs_to_category'];
    const {format, mapping} = autoDetect(headers);
    expect(format).toBe('GENERIC');
    expect(mapping.fieldToColumn.businessTerm).toBe('term_display_name');
    expect(mapping.fieldToColumn.definition).toBe('description');
  });

  it('every commercial export auto-maps the term column (no manual mapping required)', () => {
    const headerSets = [
      ['ID', 'Name', 'Full name', 'Asset type', 'Domain', 'Community', 'Status', 'Definition'],
      ['id', 'title', 'description', 'glossaries', 'template', 'steward:groupprofile', 'Status'],
      ['Name', 'Nick Name', 'Status', 'Definition', 'IsDefinitionRichText', 'Acronym'],
      ['Core: Reference ID', 'Core: Name', 'Business: Description', 'Parent Reference Id'],
      ['Name', 'Artifact Type', 'Category', 'Artifact ID', 'Description'],
      ['Catalog Name', 'Path', 'Business Term', 'Definition', 'Description', 'Notes'],
      ['typeName', 'name', 'qualifiedName', 'anchor', 'userDescription'],
      ['Name', 'Description', 'Summary', 'Status', 'Resource (IRI)'],
      ['Technical Name', 'Name', 'Categories', 'Description'],
      ['term_display_name', 'description', 'steward'],
    ];
    for (const headers of headerSets) {
      const {mapping} = autoDetect(headers);
      expect(mapping.fieldToColumn.businessTerm, headers.join(",")).not.toBeNull();
      // A URI strategy always exists: either a URI column, or a column to synthesize from.
      const hasUriStrategy = mapping.uriMode === 'COLUMN'
        ? !!mapping.uriColumn
        : !!mapping.uriIdColumn;
      expect(hasUriStrategy, headers.join(",")).toBe(true);
    }
  });
});

// #1754: the "base URL + ID" URI-synthesis path — used whenever a source (Collibra, Purview, …)
// ships no single URI column — plus the row projection and the SYNTHESIZE completeness gate. These
// were exercised only end-to-end before; the unit coverage below pins the exact join/encoding rules.
describe('synthesizeUri — base URL + id', () => {

  it('joins base and id with a single slash when the base has no trailing slash', () => {
    expect(synthesizeUri('https://glossary.example.com/term', 'ACC-1'))
      .toBe('https://glossary.example.com/term/ACC-1');
  });

  it('does not double the slash when the base already ends with one', () => {
    expect(synthesizeUri('https://glossary.example.com/term/', 'ACC-1'))
      .toBe('https://glossary.example.com/term/ACC-1');
  });

  it('percent-encodes the id so a URI-unsafe id still yields a valid URI', () => {
    expect(synthesizeUri('https://x.example.com/', 'a b/c?d'))
      .toBe('https://x.example.com/' + encodeURIComponent('a b/c?d'));
  });

  it('trims surrounding whitespace on both the base and the id', () => {
    expect(synthesizeUri('  https://x.example.com/  ', '  42  '))
      .toBe('https://x.example.com/42');
  });

  it('returns empty when either the base or the id is blank', () => {
    expect(synthesizeUri('', '42')).toBe('');
    expect(synthesizeUri('https://x.example.com/', '')).toBe('');
    expect(synthesizeUri('   ', '  ')).toBe('');
  });
});

describe('resolveUri — per-row URI from the chosen strategy', () => {
  const base: ColumnMapping = {
    fieldToColumn: {businessTerm: 'Name', externalReferenceId: 'ID', definition: null, comment: null},
    uriMode: 'COLUMN', uriColumn: 'URL', uriBase: '', uriIdColumn: null,
  };

  it('COLUMN mode reads the mapped URI column', () => {
    expect(resolveUri({...base, uriMode: 'COLUMN', uriColumn: 'URL'},
      {URL: 'https://a.example.com/1', ID: '1'})).toBe('https://a.example.com/1');
  });

  it('COLUMN mode yields empty when the column is unmapped or absent from the row', () => {
    expect(resolveUri({...base, uriMode: 'COLUMN', uriColumn: null}, {URL: 'x'})).toBe('');
    expect(resolveUri({...base, uriMode: 'COLUMN', uriColumn: 'URL'}, {})).toBe('');
  });

  it('SYNTHESIZE mode builds the URI from the base URL and the id column', () => {
    expect(resolveUri({...base, uriMode: 'SYNTHESIZE', uriBase: 'https://g.example.com/t/', uriIdColumn: 'ID'},
      {ID: 'ACC-9', Name: 'Foo'})).toBe('https://g.example.com/t/ACC-9');
  });

  it('SYNTHESIZE mode yields empty when the base URL or the id value is missing', () => {
    expect(resolveUri({...base, uriMode: 'SYNTHESIZE', uriBase: '', uriIdColumn: 'ID'}, {ID: '9'})).toBe('');
    expect(resolveUri({...base, uriMode: 'SYNTHESIZE', uriBase: 'https://g.example.com/', uriIdColumn: 'ID'},
      {ID: ''})).toBe('');
  });
});

describe('buildImportRows — Collibra-style SYNTHESIZE projection', () => {

  it('maps name/id/definition and synthesizes the URI from base URL + ID for every row', () => {
    const {mapping} = autoDetect(['ID', 'Name', 'Full name', 'Asset type', 'Domain', 'Community', 'Definition']);
    mapping.uriBase = 'https://collibra.example.com/term/';
    const rows = buildImportRows([
      {ID: 'C-1', Name: 'Customer', 'Full name': 'X > Customer', 'Asset type': 'Business Term',
        Domain: 'Sales', Community: 'Comm', Definition: 'A buyer'},
      {ID: 'C-2', Name: 'Invoice', 'Full name': 'X > Invoice', 'Asset type': 'Business Term',
        Domain: 'Fin', Community: 'Comm', Definition: 'A bill'},
    ], mapping);

    expect(rows).toHaveLength(2);
    expect(rows[0]).toMatchObject({
      id: 0, rowIndex: 1, businessTerm: 'Customer', externalReferenceId: 'C-1', definition: 'A buyer',
      externalReferenceUri: 'https://collibra.example.com/term/C-1', errors: [], userDeselected: false,
    });
    expect(rows[1].businessTerm).toBe('Invoice');
    expect(rows[1].externalReferenceUri).toBe('https://collibra.example.com/term/C-2');
  });

  it('leaves unmapped optional fields blank and reads the URI column in COLUMN mode', () => {
    const mapping: ColumnMapping = {
      fieldToColumn: {businessTerm: 'Name', externalReferenceId: null, definition: null, comment: null},
      uriMode: 'COLUMN', uriColumn: 'URL', uriBase: '', uriIdColumn: null,
    };
    const rows = buildImportRows([{Name: 'Foo', URL: 'https://a.example.com/1'}], mapping);
    expect(rows[0]).toMatchObject({
      businessTerm: 'Foo', externalReferenceUri: 'https://a.example.com/1',
      externalReferenceId: '', definition: '', comment: '',
    });
  });
});

describe('isMappingComplete — SYNTHESIZE branch requires a valid base URL + id column', () => {
  const syn = (over: Partial<ColumnMapping>): ColumnMapping => ({
    fieldToColumn: {businessTerm: 'Name', externalReferenceId: 'ID', definition: null, comment: null},
    uriMode: 'SYNTHESIZE', uriColumn: null, uriBase: 'https://g.example.com/', uriIdColumn: 'ID', ...over,
  });

  it('complete when name + valid base URL + id column are all present', () => {
    expect(isMappingComplete(syn({}))).toBe(true);
  });

  it('incomplete without a base URL (empty or whitespace)', () => {
    expect(isMappingComplete(syn({uriBase: ''}))).toBe(false);
    expect(isMappingComplete(syn({uriBase: '   '}))).toBe(false);
  });

  it('incomplete when the base URL is not a valid URI', () => {
    expect(isMappingComplete(syn({uriBase: 'has space'}))).toBe(false);
  });

  it('incomplete without an id column to append', () => {
    expect(isMappingComplete(syn({uriIdColumn: null}))).toBe(false);
  });

  it('incomplete without a business-term column', () => {
    expect(isMappingComplete(syn({
      fieldToColumn: {businessTerm: null, externalReferenceId: 'ID', definition: null, comment: null},
    }))).toBe(false);
  });

  it('a null mapping is never complete', () => {
    expect(isMappingComplete(null)).toBe(false);
  });
});
