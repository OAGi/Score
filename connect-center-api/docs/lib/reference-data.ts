import {
  fetchOpenApi,
  resolveBackendApiBase,
  getAccountOperation,
  getAgencyIdListOperation,
  getBusinessInformationEntityOperation,
  getBusinessContextOperation,
  getCodeListOperation,
  getCoreComponentOperation,
  getContextCategoryOperation,
  getContextSchemeOperation,
  getDataTypeOperation,
  getLibraryOperation,
  getNamespaceOperation,
  getReleaseOperation,
  getTagOperation,
  getXbtOperation,
  HttpMethod,
  OpenApiSpec,
} from './openapi';
import { buildExampleFromSchema } from './schema-example';
import { flattenSchemaFields } from './schema-traversal';

export type ResourceKey =
  | 'context_categories'
  | 'context_schemes'
  | 'business_contexts'
  | 'accounts'
  | 'libraries'
  | 'namespaces'
  | 'releases'
  | 'core_components'
  | 'business_information_entities'
  | 'data_types'
  | 'tags'
  | 'xbts'
  | 'code_lists'
  | 'agency_id_lists';

// Keep this union in sync with the Score reference project so the shared sidebar
// subgroup definitions typecheck, even if this backend only supports a subset.
export type MethodKey =
  | 'create'
  | 'update'
  | 'delete'
  | 'list'
  | 'retrieve'
  | 'retrieve_value_by_id'
  | 'update_value_by_id'
  | 'delete_value_by_id'
  | 'who_am_i'
  | 'create_value'
  | 'update_value'
  | 'delete_value'
  | 'get_acc'
  | 'get_asccp'
  | 'get_bccp'
  | 'get_top_level_asbiep_list'
  | 'get_top_level_asbiep'
  | 'get_asbie_by_asbie_id'
  | 'get_asbie_by_based_ascc_manifest_id'
  | 'get_bbie_by_bbie_id'
  | 'get_bbie_by_based_bcc_manifest_id'
  | 'create_top_level_asbiep'
  | 'update_top_level_asbiep'
  | 'update_top_level_asbiep_state'
  | 'delete_top_level_asbiep'
  | 'transfer_top_level_asbiep_ownership'
  | 'assign_biz_ctx_to_top_level_asbiep'
  | 'unassign_biz_ctx_from_top_level_asbiep'
  | 'create_asbie'
  | 'update_asbie'
  | 'create_bbie'
  | 'update_bbie'
  | 'create_bbie_sc'
  | 'update_bbie_sc'
  | 'reuse_top_level_asbiep'
  | 'remove_reused_top_level_asbiep';

export type MethodType = 'GET' | 'POST' | 'PUT' | 'DELETE';

export type BodyType = 'JSON' | 'Query' | 'Path' | 'None';

export type ParamDoc = {
  name: string;
  type: string;
  required: boolean;
  description: string;
  enum_values?: string[];
  is_multi_select?: boolean;
  order_by_columns?: string[];
};

export type MethodSummary = {
  key: MethodKey;
  title: string;
  method: MethodType;
  endpoint: string;
};

export type ResourceDoc = {
  resource: ResourceKey;
  title: string;
  methods: MethodSummary[];
  authentication?: {
    title: string;
    scheme: string;
    description: string;
    how_to: string[];
    curl_example: string;
    api_base_url: string;
  };
};

export type MethodDoc = {
  key: MethodKey;
  title: string;
  summary: string;
  method: MethodType;
  response_status: number;
  endpoint: string;
  alternate_endpoints: string[];
  body_label: string;
  body_type: BodyType;
  body_params: ParamDoc[];
  returns_summary: string;
  return_fields: ParamDoc[];
  request_example: string;
  response_example: string;
};

export type AuthenticationDoc = NonNullable<ResourceDoc['authentication']>;
export type SidebarResource = Pick<ResourceDoc, 'resource' | 'title' | 'methods'>;

type OpenApiOperation = NonNullable<NonNullable<OpenApiSpec['paths']>[string]>[string];

function toMethodType(method: HttpMethod): MethodType {
  return method.toUpperCase() as MethodType;
}

function inferResponseStatus(method: MethodType, responses?: OpenApiOperation['responses']): number {
  if (responses) {
    const preferred = Object.keys(responses)
      .filter((code) => /^\d{3}$/.test(code))
      .map((code) => Number(code))
      .filter((code) => code >= 200 && code < 400)
      .sort((a, b) => a - b);
    if (preferred.length > 0) return preferred[0];
  }

  if (method === 'POST') return 201;
  if (method === 'DELETE') return 204;
  return 200;
}

type OpenApiMediaContent = Record<
  string,
  { schema?: unknown; example?: unknown; examples?: Record<string, { value?: unknown }> }
>;

function extractJsonExample(content: OpenApiMediaContent | undefined): unknown | null {
  if (!content) return null;
  const json = content['application/json'];
  if (!json) return null;
  if (json.example !== undefined) return json.example;
  const examples = json.examples ? Object.values(json.examples) : [];
  for (const ex of examples) {
    if (ex && typeof ex === 'object' && 'value' in ex) return ex.value ?? null;
  }
  return null;
}

function toPrettyJson(value: unknown): string {
  if (value === null || value === undefined) return '';
  if (typeof value === 'string') return value;
  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return String(value);
  }
}

function extractAllowedOrderByColumns(description: string | undefined): string[] | undefined {
  if (!description) return undefined;
  const match = description.match(/Allowed columns:\s*([^\n\r.]*)/i);
  if (!match || !match[1]) return undefined;
  const columns = match[1]
    .split(',')
    .map((value) => value.trim())
    .filter(Boolean);
  return columns.length > 0 ? columns : undefined;
}

function sortParamsRequiredFirst<T extends ParamDoc>(params: T[]): T[] {
  return [...params]
    .map((param, index) => ({ param, index }))
    .sort((a, b) => {
      if (a.param.required !== b.param.required) {
        return a.param.required ? -1 : 1;
      }
      return a.index - b.index;
    })
    .map(({ param }) => param);
}

type ParamSchemaLike = {
  type?: string;
  enum?: unknown[];
  items?: { enum?: unknown[] };
  anyOf?: Array<{ type?: string; enum?: unknown[]; items?: { enum?: unknown[] } }>;
  [key: string]: unknown;
};

function normalizeParamSchema(raw: unknown): ParamSchemaLike {
  const schema = (raw as ParamSchemaLike | undefined) ?? {};
  if (Array.isArray(schema.anyOf) && schema.anyOf.length > 0) {
    const preferred = schema.anyOf.find((item) => item?.type && item.type !== 'null') ?? schema.anyOf[0];
    return {
      ...schema,
      type: schema.type ?? preferred?.type,
      enum: schema.enum ?? preferred?.enum,
      items: schema.items ?? preferred?.items,
    };
  }
  return schema;
}

function buildFallbackRequestExample(method: MethodType, endpoint: string): string {
  const base = resolveBackendApiBase();
  const url = `${base}${endpoint}`;
  const auth = `-H "Authorization: Bearer $ACCESS_TOKEN"`;
  if (method === 'GET') return `curl ${auth} "${url}"`;
  if (method === 'DELETE') return `curl -X DELETE "${url}" \\\n  ${auth}`;
  return `curl -X ${method} "${url}" \\\n  ${auth} \\\n  -H "Content-Type: application/json" \\\n  -d '{}'`;
}

function buildMethodDocFromOperation(
  spec: OpenApiSpec,
  methodKey: MethodKey,
  lookup: NonNullable<ReturnType<typeof getContextCategoryOperation>>,
): MethodDoc {
  const op = lookup.operation;
  const method = toMethodType(lookup.method);
  const endpoint = lookup.path;
  const response_status = inferResponseStatus(method, op.responses);

  const allParams = op.parameters ?? [];
  const pathParams: ParamDoc[] = allParams
    .filter((p) => p.in === 'path')
    .map((param) => {
      const schema = normalizeParamSchema(param.schema);
      return {
        name: param.name,
        type: schema?.type ?? 'string',
        required: Boolean(param.required),
        description: param.description ?? 'Path parameter.',
        enum_values:
          Array.isArray(schema?.enum) && schema.enum.length > 0 ? schema.enum.map((value) => String(value)) : undefined,
        is_multi_select: false,
        order_by_columns: undefined,
      };
    });

  const queryParams: ParamDoc[] = allParams
    .filter((p) => p.in === 'query')
    .map((param) => {
      const schema = normalizeParamSchema(param.schema);
      return {
        name: param.name,
        type: schema?.type ?? 'string',
        required: Boolean(param.required),
        description: param.description ?? 'Query parameter.',
        enum_values:
          Array.isArray(schema?.enum) && schema.enum.length > 0
            ? schema.enum.map((value) => String(value))
            : Array.isArray(schema?.items?.enum) && schema.items.enum.length > 0
              ? schema.items.enum.map((value) => String(value))
              : schema?.type === 'boolean'
                ? ['true', 'false']
              : undefined,
        is_multi_select: Boolean(schema?.['x-comma-separated']) || schema?.type === 'array',
        order_by_columns: param.name === 'order_by' ? extractAllowedOrderByColumns(param.description) : undefined,
      };
    });

  const sortedPathParams = sortParamsRequiredFirst(pathParams);
  const sortedQueryParams = sortParamsRequiredFirst(queryParams);

  const hasQueryParams = sortedQueryParams.length > 0;
  const hasPathParams = sortedPathParams.length > 0;
  const body_type: BodyType = op.requestBody ? 'JSON' : hasQueryParams ? 'Query' : hasPathParams ? 'Path' : 'None';

  const body_label =
    body_type === 'JSON'
      ? 'Body Parameters'
      : body_type === 'Query'
        ? 'Query Parameters'
        : body_type === 'Path'
          ? 'Path Parameters'
          : 'Parameters';

  const requestSchema = op.requestBody?.content?.['application/json']?.schema as Record<string, unknown> | undefined;
  const requestBodyFields: ParamDoc[] = requestSchema
    ? flattenSchemaFields({ schema: requestSchema, spec })
        .map((f) => ({
          name: f.name,
          type: f.type,
          required: f.required,
          description: f.description,
          enum_values: undefined,
          is_multi_select: false,
          order_by_columns: undefined,
        }))
    : [];
  const sortedRequestBodyFields = sortParamsRequiredFirst(requestBodyFields);

  const body_params =
    body_type === 'JSON'
      ? [...sortedPathParams, ...sortedQueryParams, ...sortedRequestBodyFields]
      : body_type === 'Query'
        ? [...sortedPathParams, ...sortedQueryParams]
        : body_type === 'Path'
          ? sortedPathParams
          : [];

  const responseContent = op.responses?.[String(response_status)]?.content ?? op.responses?.default?.content ?? undefined;
  const responseSchema = (responseContent?.['application/json']?.schema ?? null) as Record<string, unknown> | null;
  const return_fields: ParamDoc[] = responseSchema
    ? flattenSchemaFields({ schema: responseSchema, spec })
        .map((f) => ({
          name: f.name,
          type: f.type,
          required: f.required,
          description: f.description,
          enum_values: undefined,
          is_multi_select: false,
          order_by_columns: undefined,
        }))
    : [];

  const returns_summary =
    op.responses?.[String(response_status)]?.description ??
    op.responses?.default?.description ??
    '';

  let request_example = buildFallbackRequestExample(method, endpoint);
  if (body_type === 'JSON' && requestSchema) {
    const exampleValue = buildExampleFromSchema({ schema: requestSchema, spec });
    const pretty = toPrettyJson(exampleValue);
    if (pretty) {
      const escaped = pretty.replaceAll("'", "\\'");
      request_example = `curl -X ${method} "${resolveBackendApiBase()}${endpoint}" \\\n  -H "Authorization: Bearer $ACCESS_TOKEN" \\\n  -H "Content-Type: application/json" \\\n  -d '${escaped}'`;
    }
  }

  const responseExample = responseContent ? extractJsonExample(responseContent as OpenApiMediaContent) : null;
  const response_example =
    response_status === 204
      ? ''
      : responseExample !== null && responseExample !== undefined
        ? toPrettyJson(responseExample)
        : responseSchema
          ? toPrettyJson(buildExampleFromSchema({ schema: responseSchema, spec })) || '{}'
          : '';

  return {
    key: methodKey,
    title: op.summary ?? `${methodKey}`,
    summary: op.description ?? op.summary ?? '',
    method,
    response_status,
    endpoint,
    alternate_endpoints: [],
    body_label,
    body_type,
    body_params,
    returns_summary,
    return_fields,
    request_example,
    response_example,
  };
}

function getLookupByPathAndMethod(
  spec: OpenApiSpec,
  path: string,
  method: HttpMethod,
): { path: string; method: HttpMethod; operation: NonNullable<NonNullable<OpenApiSpec['paths']>[string]>[string] } | null {
  const operation = spec.paths?.[path]?.[method];
  if (!operation) {
    return null;
  }
  return { path, method, operation };
}

function resolveCanonicalLookup(
  spec: OpenApiSpec,
  lookup: NonNullable<ReturnType<typeof getContextCategoryOperation>>,
): NonNullable<ReturnType<typeof getContextCategoryOperation>> {
  const canonicalPath = lookup.operation['x-alternative-endpoint-for'];
  if (!canonicalPath) {
    return lookup;
  }
  return getLookupByPathAndMethod(spec, canonicalPath, lookup.method) ?? lookup;
}

function collectAlternativeEndpoints(
  spec: OpenApiSpec,
  lookup: NonNullable<ReturnType<typeof getContextCategoryOperation>>,
): string[] {
  const canonicalLookup = resolveCanonicalLookup(spec, lookup);
  const alternateEndpoints: string[] = [];

  for (const [path, pathItem] of Object.entries(spec.paths ?? {})) {
    const operation = pathItem?.[canonicalLookup.method];
    if (!operation || path === canonicalLookup.path) {
      continue;
    }
    if (operation['x-alternative-endpoint-for'] === canonicalLookup.path) {
      alternateEndpoints.push(path);
    }
  }

  return alternateEndpoints;
}

function buildReferenceMethodDoc(
  spec: OpenApiSpec,
  methodKey: MethodKey,
  lookup: NonNullable<ReturnType<typeof getContextCategoryOperation>>,
): MethodDoc {
  const canonicalLookup = resolveCanonicalLookup(spec, lookup);
  const doc = buildMethodDocFromOperation(spec, methodKey, canonicalLookup);
  const alternate_endpoints = collectAlternativeEndpoints(spec, lookup);

  if (alternate_endpoints.length === 0) {
    return doc;
  }

  return {
    ...doc,
    alternate_endpoints,
  };
}

export async function fetchContextCategoryResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<Extract<MethodKey, 'create' | 'update' | 'delete' | 'list' | 'retrieve'>> = [
    'list',
    'retrieve',
    'create',
    'update',
    'delete',
  ];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getContextCategoryOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'context_categories',
    title: 'Context Category',
    methods,
    authentication: {
      ...buildFallbackAuthentication(),
      api_base_url: resolveBackendApiBase(),
      curl_example: `curl -H "Authorization: Bearer $ACCESS_TOKEN" ${resolveBackendApiBase()}/context-categories?limit=1&offset=0`,
    },
  };
}

export async function fetchContextCategoryMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getContextCategoryOperation(spec, key);
  if (!lookup) return null;
  return buildMethodDocFromOperation(spec, key, lookup);
}

export async function fetchContextSchemeResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<
    Extract<
      MethodKey,
      | 'create'
      | 'update'
      | 'delete'
      | 'list'
      | 'retrieve'
      | 'create_value'
      | 'update_value'
      | 'delete_value'
      | 'retrieve_value_by_id'
    >
  > = [
    'list',
    'retrieve',
    'create',
    'update',
    'delete',
    'create_value',
    'update_value',
    'delete_value',
    'retrieve_value_by_id',
  ];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getContextSchemeOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'context_schemes',
    title: 'Context Scheme',
    methods,
    authentication: {
      ...buildFallbackAuthentication(),
      api_base_url: resolveBackendApiBase(),
      curl_example: `curl -H "Authorization: Bearer $ACCESS_TOKEN" ${resolveBackendApiBase()}/context-schemes?limit=1&offset=0`,
    },
  };
}

export async function fetchContextSchemeMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getContextSchemeOperation(spec, key);
  if (!lookup) return null;
  return buildReferenceMethodDoc(spec, key, lookup);
}

export async function fetchBusinessContextResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<
    Extract<
      MethodKey,
      | 'create'
      | 'update'
      | 'delete'
      | 'list'
      | 'retrieve'
      | 'create_value'
      | 'update_value'
      | 'delete_value'
      | 'retrieve_value_by_id'
    >
  > = [
    'list',
    'retrieve',
    'create',
    'update',
    'delete',
    'create_value',
    'update_value',
    'delete_value',
    'retrieve_value_by_id',
  ];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getBusinessContextOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'business_contexts',
    title: 'Business Context',
    methods,
    authentication: {
      ...buildFallbackAuthentication(),
      api_base_url: resolveBackendApiBase(),
      curl_example: `curl -H "Authorization: Bearer $ACCESS_TOKEN" ${resolveBackendApiBase()}/business-contexts?limit=1&offset=0`,
    },
  };
}

export async function fetchBusinessContextMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getBusinessContextOperation(spec, key);
  if (!lookup) return null;
  return buildReferenceMethodDoc(spec, key, lookup);
}

export async function fetchLibraryResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<Extract<MethodKey, 'list' | 'retrieve'>> = ['list', 'retrieve'];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getLibraryOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'libraries',
    title: 'Library',
    methods,
    authentication: {
      ...buildFallbackAuthentication(),
      api_base_url: resolveBackendApiBase(),
      curl_example: `curl -H "Authorization: Bearer $ACCESS_TOKEN" ${resolveBackendApiBase()}/libraries?limit=1&offset=0`,
    },
  };
}

export async function fetchLibraryMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getLibraryOperation(spec, key);
  if (!lookup) return null;
  return buildMethodDocFromOperation(spec, key, lookup);
}

export async function fetchAccountResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<Extract<MethodKey, 'list' | 'retrieve' | 'who_am_i'>> = ['list', 'retrieve', 'who_am_i'];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getAccountOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'accounts',
    title: 'Account',
    methods,
    authentication: {
      ...buildFallbackAuthentication(),
      api_base_url: resolveBackendApiBase(),
      curl_example: `curl -H "Authorization: Bearer $ACCESS_TOKEN" ${resolveBackendApiBase()}/me`,
    },
  };
}

export async function fetchAccountMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getAccountOperation(spec, key);
  if (!lookup) return null;
  return buildMethodDocFromOperation(spec, key, lookup);
}

export async function fetchNamespaceResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<Extract<MethodKey, 'list' | 'retrieve'>> = ['list', 'retrieve'];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getNamespaceOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'namespaces',
    title: 'Namespace',
    methods,
    authentication: {
      ...buildFallbackAuthentication(),
      api_base_url: resolveBackendApiBase(),
      curl_example: `curl -H "Authorization: Bearer $ACCESS_TOKEN" ${resolveBackendApiBase()}/namespaces?limit=1&offset=0`,
    },
  };
}

export async function fetchNamespaceMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getNamespaceOperation(spec, key);
  if (!lookup) return null;
  return buildMethodDocFromOperation(spec, key, lookup);
}

export async function fetchReleaseResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<Extract<MethodKey, 'list' | 'retrieve'>> = ['list', 'retrieve'];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getReleaseOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'releases',
    title: 'Release',
    methods,
    authentication: {
      ...buildFallbackAuthentication(),
      api_base_url: resolveBackendApiBase(),
      curl_example: `curl -H "Authorization: Bearer $ACCESS_TOKEN" ${resolveBackendApiBase()}/releases?limit=1&offset=0`,
    },
  };
}

export async function fetchReleaseMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getReleaseOperation(spec, key);
  if (!lookup) return null;
  return buildMethodDocFromOperation(spec, key, lookup);
}

export async function fetchDataTypeResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<Extract<MethodKey, 'list' | 'retrieve'>> = ['list', 'retrieve'];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getDataTypeOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'data_types',
    title: 'Data Type',
    methods,
  };
}

export async function fetchDataTypeMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getDataTypeOperation(spec, key);
  if (!lookup) return null;
  return buildMethodDocFromOperation(spec, key, lookup);
}

export async function fetchTagResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<Extract<MethodKey, 'list'>> = ['list'];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getTagOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'tags',
    title: 'Tag',
    methods,
  };
}

export async function fetchTagMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getTagOperation(spec, key);
  if (!lookup) return null;
  return buildMethodDocFromOperation(spec, key, lookup);
}

export async function fetchXbtResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<Extract<MethodKey, 'retrieve'>> = ['retrieve'];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getXbtOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'xbts',
    title: 'XBT (XML Built-in Type)',
    methods,
  };
}

export async function fetchXbtMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getXbtOperation(spec, key);
  if (!lookup) return null;
  return buildMethodDocFromOperation(spec, key, lookup);
}

export async function fetchCodeListResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<Extract<MethodKey, 'list' | 'retrieve'>> = ['list', 'retrieve'];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getCodeListOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'code_lists',
    title: 'Code List',
    methods,
  };
}

export async function fetchCodeListMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getCodeListOperation(spec, key);
  if (!lookup) return null;
  return buildMethodDocFromOperation(spec, key, lookup);
}

export async function fetchAgencyIdListResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<Extract<MethodKey, 'list' | 'retrieve'>> = ['list', 'retrieve'];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getAgencyIdListOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'agency_id_lists',
    title: 'Agency ID List',
    methods,
  };
}

export async function fetchAgencyIdListMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getAgencyIdListOperation(spec, key);
  if (!lookup) return null;
  return buildMethodDocFromOperation(spec, key, lookup);
}

export async function fetchCoreComponentResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<Extract<MethodKey, 'list' | 'get_acc' | 'get_asccp' | 'get_bccp'>> = [
    'list',
    'get_acc',
    'get_asccp',
    'get_bccp',
  ];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getCoreComponentOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'core_components',
    title: 'Core Component',
    methods,
  };
}

export async function fetchCoreComponentMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getCoreComponentOperation(spec, key);
  if (!lookup) return null;
  return buildMethodDocFromOperation(spec, key, lookup);
}

export async function fetchBusinessInformationEntityResource(): Promise<ResourceDoc> {
  const spec = await fetchOpenApi();
  const methodOrder: Array<Extract<MethodKey, MethodKey>> = [
    'get_top_level_asbiep_list',
    'get_top_level_asbiep',
    'create_top_level_asbiep',
    'update_top_level_asbiep',
    'update_top_level_asbiep_state',
    'transfer_top_level_asbiep_ownership',
    'delete_top_level_asbiep',
    'assign_biz_ctx_to_top_level_asbiep',
    'unassign_biz_ctx_from_top_level_asbiep',
    'get_asbie_by_asbie_id',
    'get_asbie_by_based_ascc_manifest_id',
    'create_asbie',
    'update_asbie',
    'reuse_top_level_asbiep',
    'remove_reused_top_level_asbiep',
    'get_bbie_by_bbie_id',
    'get_bbie_by_based_bcc_manifest_id',
    'create_bbie',
    'update_bbie',
    'create_bbie_sc',
    'update_bbie_sc',
  ];

  const methods: MethodSummary[] = [];
  for (const key of methodOrder) {
    const lookup = getBusinessInformationEntityOperation(spec, key);
    if (!lookup) continue;
    methods.push({
      key,
      title: lookup.operation.summary ?? lookup.operation.operationId ?? key,
      method: toMethodType(lookup.method),
      endpoint: lookup.path,
    });
  }

  return {
    resource: 'business_information_entities',
    title: 'Business Information Entity',
    methods,
  };
}

export async function fetchBusinessInformationEntityMethod(method: string): Promise<MethodDoc | null> {
  const spec = await fetchOpenApi();
  const key = method as MethodKey;
  const lookup = getBusinessInformationEntityOperation(spec, key);
  if (!lookup) return null;
  return buildReferenceMethodDoc(spec, key, lookup);
}

export async function fetchReferenceResource(resource: ResourceKey): Promise<ResourceDoc> {
  if (resource === 'context_categories') {
    return fetchContextCategoryResource();
  }
  if (resource === 'context_schemes') {
    return fetchContextSchemeResource();
  }
  if (resource === 'business_contexts') {
    return fetchBusinessContextResource();
  }
  if (resource === 'libraries') {
    return fetchLibraryResource();
  }
  if (resource === 'accounts') {
    return fetchAccountResource();
  }
  if (resource === 'namespaces') {
    return fetchNamespaceResource();
  }
  if (resource === 'releases') {
    return fetchReleaseResource();
  }
  if (resource === 'data_types') {
    return fetchDataTypeResource();
  }
  if (resource === 'tags') {
    return fetchTagResource();
  }
  if (resource === 'xbts') {
    return fetchXbtResource();
  }
  if (resource === 'code_lists') {
    return fetchCodeListResource();
  }
  if (resource === 'agency_id_lists') {
    return fetchAgencyIdListResource();
  }
  if (resource === 'core_components') {
    return fetchCoreComponentResource();
  }
  if (resource === 'business_information_entities') {
    return fetchBusinessInformationEntityResource();
  }
  throw new Error(`Unsupported resource: ${resource}`);
}

export async function fetchReferenceMethod(resource: ResourceKey, method: string): Promise<MethodDoc | null> {
  if (resource === 'context_categories') {
    return fetchContextCategoryMethod(method);
  }
  if (resource === 'context_schemes') {
    return fetchContextSchemeMethod(method);
  }
  if (resource === 'business_contexts') {
    return fetchBusinessContextMethod(method);
  }
  if (resource === 'libraries') {
    return fetchLibraryMethod(method);
  }
  if (resource === 'accounts') {
    return fetchAccountMethod(method);
  }
  if (resource === 'namespaces') {
    return fetchNamespaceMethod(method);
  }
  if (resource === 'releases') {
    return fetchReleaseMethod(method);
  }
  if (resource === 'data_types') {
    return fetchDataTypeMethod(method);
  }
  if (resource === 'tags') {
    return fetchTagMethod(method);
  }
  if (resource === 'xbts') {
    return fetchXbtMethod(method);
  }
  if (resource === 'code_lists') {
    return fetchCodeListMethod(method);
  }
  if (resource === 'agency_id_lists') {
    return fetchAgencyIdListMethod(method);
  }
  if (resource === 'core_components') {
    return fetchCoreComponentMethod(method);
  }
  if (resource === 'business_information_entities') {
    return fetchBusinessInformationEntityMethod(method);
  }
  return null;
}

export async function fetchSidebarResources(): Promise<SidebarResource[]> {
  const categories = await fetchContextCategoryResource();
  const schemes = await fetchContextSchemeResource();
  const bizCtxs = await fetchBusinessContextResource();
  const libraries = await fetchLibraryResource();
  const accounts = await fetchAccountResource();
  const releases = await fetchReleaseResource();
  const namespaces = await fetchNamespaceResource();
  const coreComponents = await fetchCoreComponentResource();
  const dataTypes = await fetchDataTypeResource();
  const tags = await fetchTagResource();
  const businessInformationEntities = await fetchBusinessInformationEntityResource();
  const xbts = await fetchXbtResource();
  const codeLists = await fetchCodeListResource();
  const agencyIdLists = await fetchAgencyIdListResource();
  return [
    { resource: accounts.resource, title: accounts.title, methods: accounts.methods },
    { resource: libraries.resource, title: libraries.title, methods: libraries.methods },
    { resource: releases.resource, title: releases.title, methods: releases.methods },
    { resource: namespaces.resource, title: namespaces.title, methods: namespaces.methods },
    { resource: coreComponents.resource, title: coreComponents.title, methods: coreComponents.methods },
    { resource: dataTypes.resource, title: dataTypes.title, methods: dataTypes.methods },
    { resource: xbts.resource, title: xbts.title, methods: xbts.methods },
    { resource: codeLists.resource, title: codeLists.title, methods: codeLists.methods },
    { resource: agencyIdLists.resource, title: agencyIdLists.title, methods: agencyIdLists.methods },
    { resource: tags.resource, title: tags.title, methods: tags.methods },
    {
      resource: businessInformationEntities.resource,
      title: businessInformationEntities.title,
      methods: businessInformationEntities.methods,
    },
    { resource: categories.resource, title: categories.title, methods: categories.methods },
    { resource: schemes.resource, title: schemes.title, methods: schemes.methods },
    { resource: bizCtxs.resource, title: bizCtxs.title, methods: bizCtxs.methods },
  ];
}

export function buildFallbackAuthentication(apiBaseUrl = resolveBackendApiBase()): AuthenticationDoc {
  return {
    title: 'Authentication',
    scheme: 'OpenID Connect Bearer (preferred) + HTTP Basic (fallback)',
    description:
      'The connectCenter API uses OAuth 2.0 / OpenID Connect Bearer tokens as the primary authentication method. HTTP Basic authentication is supported as a fallback.',
    how_to: [
      'Use Authorization: Bearer ACCESS_TOKEN whenever possible.',
      'If a bearer token is not available, use HTTP Basic credentials.',
      'Do not send credentials over plaintext HTTP in production.',
    ],
    curl_example: `curl -H "Authorization: Bearer $ACCESS_TOKEN" ${apiBaseUrl}/context-categories?limit=1&offset=0`,
    api_base_url: apiBaseUrl,
  };
}

export function methodHref(method: string, resource: ResourceKey = 'context_categories'): string {
  return `/resources/${resource}/methods/${method}`;
}
