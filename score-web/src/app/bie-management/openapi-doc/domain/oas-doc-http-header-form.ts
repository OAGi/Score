import {OasDocHttpHeader, OasHttpHeaderDirection, OasParameterLocation, OasSchemaObject} from './openapi-doc';

/**
 * Issue #1791: what the dialog of an HTTP header of an OpenAPI Document lets one enter, and what it makes of it. The
 * server checks the same again; this is for telling the user before a round trip.
 */

// The token of RFC 9110, the characters a header name is made of.
export const HEADER_NAME_PATTERN = /^[!#$%&'*+.^_`|~0-9A-Za-z-]+$/;

export const PARAMETER_LOCATIONS: { value: OasParameterLocation; label: string }[] = [
  {value: 'query', label: 'query'},
  {value: 'header', label: 'header'},
  {value: 'path', label: 'path'},
  {value: 'cookie', label: 'cookie'}
];

// OpenAPI: such a request parameter, or a response header named Content-Type, SHALL be ignored.
const IGNORED_REQUEST_HEADERS = ['accept', 'content-type', 'authorization'];
const IGNORED_RESPONSE_HEADER = 'content-type';

// The name of a path parameter is a template expression of the path: {name}.
export const PATH_PARAMETER_NAME_PATTERN = /^[^{}/\s]+$/;

/** Where the parameter or header is: a response header is always in a header, a request parameter says it. */
export function locationOf(header: { direction: OasHttpHeaderDirection; in?: OasParameterLocation }): OasParameterLocation {
  return header.direction === 'REQUEST' ? (header.in ?? 'header') : 'header';
}

/** The styles OpenAPI offers for each location. A header has the one style, simple. */
export const STYLES_BY_LOCATION: Record<OasParameterLocation, string[]> = {
  query: ['form', 'spaceDelimited', 'pipeDelimited', 'deepObject'],
  path: ['matrix', 'label', 'simple'],
  header: ['simple'],
  cookie: ['form']
};

/**
 * The explode a style has when none is given (OpenAPI): true for form, false for the others. For deepObject it is false,
 * which OpenAPI does not define: it needs an explode of true.
 */
export function defaultExplode(style: string): boolean {
  return style === 'form';
}

/** The style a value has when none is given: the first of the styles of its location. */
export function defaultStyle(location: OasParameterLocation): string {
  return STYLES_BY_LOCATION[location][0];
}

// type "/" subtype (RFC 6838); the parameters of a media type are not kept
export const MEDIA_TYPE_PATTERN = /^[A-Za-z0-9][A-Za-z0-9!#$&^_.+-]*\/[A-Za-z0-9*][A-Za-z0-9!#$&^_.+*-]*$/;

export const DEFAULT_CONTENT_MEDIA_TYPE = 'text/plain';

export type Serialization = 'schema' | 'content';

export function serializationOf(header: { contentMediaType?: string }): Serialization {
  return header.contentMediaType ? 'content' : 'schema';
}

// What is URL-safe in a value: the unreserved characters of RFC 3986, and a percent-encoded triple.
const URL_SAFE_VALUE = /^(?:[A-Za-z0-9\-._~]|%[0-9A-Fa-f]{2})*$/;

/** The values the parameter is given in its example, in the example of its schema, or by an enum, a const or a default. */
function sampleValues(schema: OasSchemaObject | undefined, example: any): string[] {
  const candidates: any[] = [example, schema?.['example'], schema?.['const'], schema?.['default'],
    ...(Array.isArray(schema?.['enum']) ? schema?.['enum'] : [])];
  return candidates.filter(value => typeof value === 'string');
}

/**
 * What is wrong with the value as OpenAPI 3.0.4 will have it, though it is valid: it does not recommend serializing a cookie
 * with a schema, nor a header with one whose value has parameters (name=value after a ;) or characters that are not URL-safe.
 * The advice is for the value as it is given: a header whose values are URL-safe has none, and a value of the content
 * serialization has none either.
 */
export function serializationWarnings(location: OasParameterLocation, serialization: Serialization,
                                      schema: OasSchemaObject | undefined, example: any): string[] {
  if (serialization !== 'schema') {
    return [];
  }
  if (location === 'cookie') {
    return ['OpenAPI does not recommend serializing a cookie with a schema. Use content, such as text/plain.'];
  }
  if (location === 'header') {
    const unsafe = sampleValues(schema, example).find(value => value.includes(';') || !URL_SAFE_VALUE.test(value));
    if (unsafe !== undefined) {
      return ['The value "' + unsafe + '" has ' + (unsafe.includes(';')
          ? 'parameters (name=value after a ;)' : 'characters that are not URL-safe')
        + ', and OpenAPI does not recommend serializing such a header with a schema. Use content, such as text/plain.'];
    }
  }
  return [];
}

/** The JSON type of a value, as a schema names it. */
function typeOfValue(value: any): string {
  return Array.isArray(value) ? 'array' : (value === null ? 'null' : typeof value);
}

/**
 * What is valid but probably not meant: a header named Cookie (OpenAPI: use the cookie location), and an example that is
 * not of the type the schema says (OpenAPI: the example SHOULD match the schema).
 */
export function valueWarnings(header: {name: string; direction: OasHttpHeaderDirection | undefined; in?: OasParameterLocation},
                              schema: OasSchemaObject | undefined, example: any, serialization: Serialization): string[] {
  const warnings: string[] = [];
  if (header.direction === 'REQUEST' && (header.in ?? 'header') === 'header' && (header.name || '').trim().toLowerCase() === 'cookie') {
    warnings.push('A header parameter named Cookie has an undefined effect in OpenAPI: use a parameter in the cookie location.');
  }
  const types = schemaTypes(schema);
  if (example !== undefined && types.length && serialization === 'schema') {
    const actual = typeOfValue(example);
    const fits = types.some(type => type === actual || (type === 'number' && actual === 'number')
      || (type === 'integer' && actual === 'number' && Number.isInteger(example)));
    if (!fits) {
      warnings.push('The example is a ' + actual + ', but the schema says ' + types.join(' or ') + '.');
    }
  }
  return warnings;
}

/**
 * What follows from a change of the location: the style and allowReserved that it does not have are dropped, and the
 * explode goes back to the default of the style, which the location may have changed.
 */
export function adaptToLocation(header: OasDocHttpHeader): void {
  const location = locationOf(header);
  delete header.explode;
  if (header.style && !STYLES_BY_LOCATION[location].includes(header.style)) {
    delete header.style;
  }
  if (location !== 'query' || header.direction !== 'REQUEST') {
    delete header.allowReserved;
  }
}

export interface OasDocHttpHeaderDraft {
  name: string;
  in?: OasParameterLocation;
  style?: string;
  explode?: boolean;
  allowReserved?: boolean;
  contentMediaType?: string;
  schemaText: string;
  exampleText: string;
  direction: OasHttpHeaderDirection | undefined;
}

/** What the dialog knows of the place the header is added to. */
export interface OasDocHttpHeaderContext {
  /** The path of the operation as the generated document has it (with the {version} of the document put in). */
  resourcePath?: string;
  /** The OpenAPI version of the document: 3.0 or 3.1 (or 3.0.4, 3.1.2). */
  openApiVersion?: string;
}

/** The types a style is defined for (OpenAPI, Style Values); the styles that are not here are for every type. */
const STYLE_TYPES: Record<string, string[]> = {
  spaceDelimited: ['array', 'object'],
  pipeDelimited: ['array', 'object'],
  deepObject: ['object']
};

export function isOpenApi30(openApiVersion: string | undefined): boolean {
  return !!openApiVersion && openApiVersion.trim().startsWith('3.0');
}

/** The types the schema says its value has, when it says so with the type keyword. */
export function schemaTypes(schema: OasSchemaObject | undefined): string[] {
  const type = schema?.['type'];
  return typeof type === 'string' ? [type] : (Array.isArray(type) ? type.map(String) : []);
}

/** The first field that is next to a $ref anywhere in the schema (OpenAPI 3.0 ignores it), or undefined. */
function fieldNextToReference(node: any): string | undefined {
  if (Array.isArray(node)) {
    return node.map(fieldNextToReference).find(found => found !== undefined);
  }
  if (node !== null && typeof node === 'object') {
    if (typeof node['$ref'] === 'string') {
      const sibling = Object.keys(node).find(key => key !== '$ref' && !key.startsWith('x-'));
      if (sibling !== undefined) {
        return sibling;
      }
    }
    return Object.values(node).map(fieldNextToReference).find(found => found !== undefined);
  }
  return undefined;
}

/**
 * What is wrong with the schema as the Schema Object of OpenAPI 3.0 has it, which differs from 3.1: it has no list of types
 * and no null type (it has nullable), and a Reference Object ($ref) has no other fields. The server checks the whole Schema
 * Object against the JSON Schema of the version; this tells the usual ones at once.
 */
export function schemaVersionError(schema: OasSchemaObject | undefined, openApiVersion: string | undefined): string | undefined {
  if (!schema || !isOpenApi30(openApiVersion)) {
    return undefined;
  }
  const type = schema['type'];
  if (Array.isArray(type)) {
    return 'OpenAPI 3.0 has no list of types in a schema: use one type and "nullable": true.';
  }
  if (type === 'null') {
    return 'OpenAPI 3.0 has no null type: use "nullable": true.';
  }
  const sibling = fieldNextToReference(schema);
  if (sibling !== undefined) {
    return 'In OpenAPI 3.0 a $ref has no other fields (they are ignored): "' + sibling + '".';
  }
  return undefined;
}

export interface OasDocHttpHeaderErrors {
  name?: string;
  direction?: string;
  serialization?: string;
  schema?: string;
  example?: string;
  duplicate?: string;
}

/** A header for an operation that is being added: a string, a request parameter or a response header. */
export function newHttpHeader(direction: OasHttpHeaderDirection, oasOperationId?: number): OasDocHttpHeader {
  return {
    oasOperationId,
    name: '',
    description: '',
    schema: {type: 'string'},
    direction,
    ...(direction === 'REQUEST' ? {in: 'query' as OasParameterLocation} : {}),
    required: false
  };
}

/** The name of what the direction makes of a header, as the user sees it. */
export function directionLabel(direction: OasHttpHeaderDirection): string {
  return direction === 'REQUEST' ? 'HTTP Request Parameter' : 'HTTP Response Header';
}

export function schemaToText(schema: OasSchemaObject | undefined): string {
  return JSON.stringify(schema ?? {type: 'string'}, null, 2);
}

export function exampleToText(example: any): string {
  return example === undefined || example === null ? '' : JSON.stringify(example);
}

/** The schema a text holds: a JSON object, which is a Schema Object (or, in OpenAPI 3.0, a Reference Object). */
export function parseSchemaText(text: string): { schema?: OasSchemaObject; error?: string } {
  if (!text || !text.trim()) {
    return {error: 'The schema is required.'};
  }
  let parsed: any;
  try {
    parsed = JSON.parse(text);
  } catch {
    return {error: 'The schema is not valid JSON.'};
  }
  if (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed)) {
    return {error: 'The schema must be a JSON object, such as {"type": "string"} or {"$ref": "#/components/schemas/X"}.'};
  }
  if ('$ref' in parsed && !(typeof parsed.$ref === 'string' && parsed.$ref.trim())) {
    return {error: 'The $ref of the schema must be a non-blank string.'};
  }
  return {schema: parsed};
}

/** The example a text holds: any JSON value, or nothing when the text is blank. */
export function parseExampleText(text: string): { example?: any; error?: string } {
  if (!text || !text.trim()) {
    return {};
  }
  let parsed: any;
  try {
    parsed = JSON.parse(text);
  } catch {
    return {error: 'The example must be a JSON value: "text" in quotes, a number, true, false, an array or an object.'};
  }
  return parsed === null ? {error: 'Leave the example empty for none.'} : {example: parsed};
}

/** What is wrong with the draft, given the other headers that are in the document. */
export function validateHeaderDraft(draft: OasDocHttpHeaderDraft, others: OasDocHttpHeader[],
                                    oasOperationId?: number, context: OasDocHttpHeaderContext = {}): OasDocHttpHeaderErrors {
  const errors: OasDocHttpHeaderErrors = {};
  const name = (draft.name || '').trim();
  const in_ = draft.direction === 'REQUEST' ? (draft.in ?? 'header') : 'header';
  if (!name) {
    errors.name = 'The name is required.';
  } else if (in_ === 'header' && !HEADER_NAME_PATTERN.test(name)) {
    errors.name = 'The name may have letters, digits and ! # $ % & \' * + - . ^ _ ` | ~ only.';
  } else if (in_ === 'header' && draft.direction === 'REQUEST' && IGNORED_REQUEST_HEADERS.includes(name.toLowerCase())) {
    errors.name = 'OpenAPI ignores a request parameter named Accept, Content-Type or Authorization.';
  } else if (in_ === 'header' && draft.direction === 'RESPONSE' && name.toLowerCase() === IGNORED_RESPONSE_HEADER) {
    errors.name = 'OpenAPI ignores a response header named Content-Type.';
  } else if (in_ === 'path' && !PATH_PARAMETER_NAME_PATTERN.test(name)) {
    errors.name = 'The name of a path parameter is a {name} of the path: no braces, slashes or spaces.';
  }
  if (in_ === 'path' && draft.direction === 'REQUEST' && name && !errors.name && oasOperationId
    && context.resourcePath !== undefined && !context.resourcePath.includes('{' + name + '}')) {
    errors.name = 'The path of this operation, ' + (context.resourcePath || '(none)') + ', has no {' + name + '}.';
  }
  const parsedSchema = parseSchemaText(draft.schemaText).schema;
  const serialization = serializationError(draft, in_, parsedSchema);
  if (serialization) {
    errors.serialization = serialization;
  }
  if (!draft.direction) {
    errors.direction = 'Choose a request parameter or a response header.';
  } else if (draft.direction === 'REQUEST' && draft.in && !PARAMETER_LOCATIONS.some(l => l.value === draft.in)) {
    errors.direction = 'The parameter is in the query, a header, the path or a cookie.';
  }
  const schema = parseSchemaText(draft.schemaText);
  if (schema.error) {
    errors.schema = schema.error;
  } else {
    const versionError = schemaVersionError(schema.schema, context.openApiVersion);
    if (versionError) {
      errors.schema = versionError;
    }
  }
  const example = parseExampleText(draft.exampleText);
  if (example.error) {
    errors.example = example.error;
  }
  if (name && !errors.direction) {
    // a header name ignores case; the name of a query parameter, a path parameter or a cookie does not
    const sameName = (other: OasDocHttpHeader) => in_ === 'header'
      ? other.name.trim().toLowerCase() === name.toLowerCase() : other.name.trim() === name;
    const clash = others.find(other => (other.oasOperationId || 0) === (oasOperationId || 0)
      && other.direction === draft.direction && locationOf(other) === in_ && sameName(other));
    if (clash) {
      errors.duplicate = 'This ' + (oasOperationId ? 'operation' : 'document') + ' has a '
        + (draft.direction === 'REQUEST' ? 'request parameter in ' + in_ : 'response header') + ' of this name already.';
    }
  }
  return errors;
}

/** What is wrong with how the value is serialized: a media type, or a style, which exclude each other. */
function serializationError(draft: OasDocHttpHeaderDraft, location: OasParameterLocation,
                            schema?: OasSchemaObject): string | undefined {
  if (!STYLES_BY_LOCATION[location]) {
    return undefined; // a location that does not exist is said by the location, not by what depends on it
  }
  const mediaType = (draft.contentMediaType ?? '').trim();
  if (draft.contentMediaType !== undefined && draft.contentMediaType !== null) {
    if (!mediaType) {
      return 'The media type is required, such as text/plain.';
    }
    if (!MEDIA_TYPE_PATTERN.test(mediaType)) {
      return 'The media type is a type/subtype, such as text/plain or application/json.';
    }
    return undefined;
  }
  if (draft.style && !STYLES_BY_LOCATION[location].includes(draft.style)) {
    return 'The style ' + draft.style + ' is not one of a ' + location + ': ' + STYLES_BY_LOCATION[location].join(', ') + '.';
  }
  const wanted = STYLE_TYPES[draft.style ?? defaultStyle(location)];
  const types = schemaTypes(schema);
  if (wanted && types.length && !types.some(type => wanted.includes(type))) {
    return 'The style ' + (draft.style ?? defaultStyle(location)) + ' is for a value of type ' + wanted.join(' or ')
      + ', but the schema is of type ' + types.join(' or ') + '.';
  }
  if ((draft.style ?? defaultStyle(location)) === 'deepObject' && draft.explode !== true) {
    return 'OpenAPI does not define deepObject without explode: choose true.';
  }
  if (draft.allowReserved && !(location === 'query' && draft.direction === 'REQUEST')) {
    return 'allowReserved only applies to a parameter in the query.';
  }
  return undefined;
}

export function hasErrors(errors: OasDocHttpHeaderErrors): boolean {
  return Object.keys(errors).length > 0;
}
