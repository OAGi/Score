import { getRuntimeConfiguredApiBase, resolveBrowserApiBase } from './runtime-paths';

export type OpenApiSpec = {
  openapi?: string;
  info?: {
    title?: string;
    version?: string;
  };
  components?: {
    schemas?: Record<string, unknown>;
    securitySchemes?: Record<string, unknown>;
  };
  paths?: Record<
    string,
    Record<
      string,
      {
        summary?: string;
        description?: string;
        operationId?: string;
        tags?: string[];
        // Vendor extension used by connectCenter to power the custom reference pages.
        // This stays in OpenAPI JSON, but Swagger UI ignores it.
        'x-reference'?: unknown;
        'x-alternative-endpoint-for'?: string;
        parameters?: Array<{
          name: string;
          in: 'query' | 'path' | 'header' | 'cookie';
          required?: boolean;
          schema?: { type?: string; format?: string; enum?: unknown[]; items?: unknown };
          description?: string;
          example?: unknown;
        }>;
        requestBody?: {
          required?: boolean;
          content?: Record<string, { schema?: unknown; example?: unknown; examples?: Record<string, { value?: unknown }> }>;
        };
        responses?: Record<
          string,
          {
            description?: string;
            content?: Record<string, { schema?: unknown; example?: unknown; examples?: Record<string, { value?: unknown }> }>;
          }
        >;
      }
    >
  >;
};

export type HttpMethod = 'get' | 'post' | 'put' | 'delete';

let openApiSpecCache: OpenApiSpec | null = null;
let openApiSpecPromise: Promise<OpenApiSpec> | null = null;

export function resolveBackendApiBase(): string {
  if (typeof window !== 'undefined') {
    const runtimeConfigured = getRuntimeConfiguredApiBase();
    if (runtimeConfigured) {
      return runtimeConfigured;
    }
  }

  const configured = (process.env.NEXT_PUBLIC_BACKEND_API_BASE ?? '').trim();
  if (configured) {
    return configured.replace(/\/+$/, '');
  }

  if (typeof window !== 'undefined') {
    return resolveBrowserApiBase();
  }

  return 'http://127.0.0.1:5555/api';
}

export async function fetchOpenApi(): Promise<OpenApiSpec> {
  if (openApiSpecCache) {
    return openApiSpecCache;
  }
  if (openApiSpecPromise) {
    return openApiSpecPromise;
  }

  const apiBase = resolveBackendApiBase();
  openApiSpecPromise = (async () => {
    const response = await fetch(`${apiBase}/openapi.json`, { cache: 'no-store' });
    if (!response.ok) {
      throw new Error(`Failed to fetch ${apiBase}/openapi.json: ${response.status}`);
    }
    const spec = (await response.json()) as OpenApiSpec;
    openApiSpecCache = spec;
    return spec;
  })();

  try {
    return await openApiSpecPromise;
  } finally {
    openApiSpecPromise = null;
  }
}

export type OperationLookup = {
  path: string;
  method: HttpMethod;
  operation: NonNullable<NonNullable<OpenApiSpec['paths']>[string]>[string];
};

export function getContextCategoryOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  // Map docs method keys to actual OpenAPI path+method.
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    list: { path: '/context-categories', method: 'get' },
    create: { path: '/context-categories', method: 'post' },
    retrieve: { path: '/context-categories/{context_category_id}', method: 'get' },
    update: { path: '/context-categories/{context_category_id}', method: 'post' },
    delete: { path: '/context-categories/{context_category_id}', method: 'delete' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getContextSchemeOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    list: { path: '/context-schemes', method: 'get' },
    create: { path: '/context-schemes', method: 'post' },
    retrieve: { path: '/context-schemes/{ctx_scheme_id}', method: 'get' },
    update: { path: '/context-schemes/{ctx_scheme_id}', method: 'post' },
    delete: { path: '/context-schemes/{ctx_scheme_id}', method: 'delete' },
    create_value: { path: '/context-schemes/{ctx_scheme_id}/values', method: 'post' },
    retrieve_value: { path: '/context-schemes/{ctx_scheme_id}/values/{ctx_scheme_value_id}', method: 'get' },
    update_value: { path: '/context-schemes/{ctx_scheme_id}/values/{ctx_scheme_value_id}', method: 'post' },
    delete_value: { path: '/context-schemes/{ctx_scheme_id}/values/{ctx_scheme_value_id}', method: 'delete' },
    retrieve_value_by_id: { path: '/context-scheme-values/{ctx_scheme_value_id}', method: 'get' },
    update_value_by_id: { path: '/context-scheme-values/{ctx_scheme_value_id}', method: 'post' },
    delete_value_by_id: { path: '/context-scheme-values/{ctx_scheme_value_id}', method: 'delete' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getBusinessContextOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    list: { path: '/business-contexts', method: 'get' },
    create: { path: '/business-contexts', method: 'post' },
    retrieve: { path: '/business-contexts/{biz_ctx_id}', method: 'get' },
    update: { path: '/business-contexts/{biz_ctx_id}', method: 'post' },
    delete: { path: '/business-contexts/{biz_ctx_id}', method: 'delete' },
    create_value: { path: '/business-contexts/{biz_ctx_id}/values', method: 'post' },
    retrieve_value: { path: '/business-contexts/{biz_ctx_id}/values/{biz_ctx_value_id}', method: 'get' },
    update_value: { path: '/business-contexts/{biz_ctx_id}/values/{biz_ctx_value_id}', method: 'post' },
    delete_value: { path: '/business-contexts/{biz_ctx_id}/values/{biz_ctx_value_id}', method: 'delete' },
    retrieve_value_by_id: { path: '/business-context-values/{biz_ctx_value_id}', method: 'get' },
    update_value_by_id: { path: '/business-context-values/{biz_ctx_value_id}', method: 'post' },
    delete_value_by_id: { path: '/business-context-values/{biz_ctx_value_id}', method: 'delete' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getLibraryOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    list: { path: '/libraries', method: 'get' },
    retrieve: { path: '/libraries/{library_id}', method: 'get' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getAccountOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    list: { path: '/accounts', method: 'get' },
    retrieve: { path: '/accounts/{app_user_id}', method: 'get' },
    who_am_i: { path: '/me', method: 'get' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getNamespaceOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    list: { path: '/namespaces', method: 'get' },
    retrieve: { path: '/namespaces/{namespace_id}', method: 'get' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getReleaseOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    list: { path: '/releases', method: 'get' },
    retrieve: { path: '/releases/{release_id}', method: 'get' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getDataTypeOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    list: { path: '/data-types', method: 'get' },
    retrieve: { path: '/data-types/{dt_manifest_id}', method: 'get' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getTagOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    list: { path: '/tags', method: 'get' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getXbtOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    retrieve: { path: '/xbts/{xbt_manifest_id}', method: 'get' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getCodeListOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    list: { path: '/code-lists', method: 'get' },
    retrieve: { path: '/code-lists/{code_list_manifest_id}', method: 'get' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getAgencyIdListOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    list: { path: '/agency-id-lists', method: 'get' },
    retrieve: { path: '/agency-id-lists/{agency_id_list_manifest_id}', method: 'get' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getCoreComponentOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    list: { path: '/core-components', method: 'get' },
    get_acc: { path: '/core-components/acc/{acc_manifest_id}', method: 'get' },
    get_asccp: { path: '/core-components/asccp/{asccp_manifest_id}', method: 'get' },
    get_bccp: { path: '/core-components/bccp/{bccp_manifest_id}', method: 'get' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}

export function getBusinessInformationEntityOperation(spec: OpenApiSpec, key: string): OperationLookup | null {
  const mapping: Record<string, { path: string; method: HttpMethod }> = {
    get_top_level_asbiep_list: { path: '/business-information-entities', method: 'get' },
    get_top_level_asbiep: { path: '/business-information-entities/{top_level_asbiep_id}', method: 'get' },
    create_top_level_asbiep: { path: '/business-information-entities', method: 'post' },
    update_top_level_asbiep: { path: '/business-information-entities/{top_level_asbiep_id}', method: 'post' },
    update_top_level_asbiep_state: {
      path: '/business-information-entities/{top_level_asbiep_id}/state',
      method: 'post',
    },
    delete_top_level_asbiep: { path: '/business-information-entities/{top_level_asbiep_id}', method: 'delete' },
    transfer_top_level_asbiep_ownership: {
      path: '/business-information-entities/{top_level_asbiep_id}/ownership',
      method: 'post',
    },
    assign_biz_ctx_to_top_level_asbiep: {
      path: '/business-information-entities/{top_level_asbiep_id}/business-contexts',
      method: 'post',
    },
    unassign_biz_ctx_from_top_level_asbiep: {
      path: '/business-information-entities/{top_level_asbiep_id}/business-contexts',
      method: 'delete',
    },
    get_asbie_by_asbie_id: {
      path: '/business-information-entities/{top_level_asbiep_id}/asbies/{asbie_id}',
      method: 'get',
    },
    get_asbie_by_based_ascc_manifest_id: {
      path: '/business-information-entities/{top_level_asbiep_id}/asbies',
      method: 'get',
    },
    create_asbie: { path: '/business-information-entities/{top_level_asbiep_id}/asbies', method: 'post' },
    update_asbie: {
      path: '/business-information-entities/{top_level_asbiep_id}/asbies/{asbie_id}',
      method: 'post',
    },
    reuse_top_level_asbiep: {
      path: '/business-information-entities/{top_level_asbiep_id}/asbies/{asbie_id}/reuse',
      method: 'post',
    },
    remove_reused_top_level_asbiep: {
      path: '/business-information-entities/{top_level_asbiep_id}/asbies/{asbie_id}/reuse',
      method: 'delete',
    },
    get_bbie_by_bbie_id: {
      path: '/business-information-entities/{top_level_asbiep_id}/bbies/{bbie_id}',
      method: 'get',
    },
    get_bbie_by_based_bcc_manifest_id: {
      path: '/business-information-entities/{top_level_asbiep_id}/bbies',
      method: 'get',
    },
    create_bbie: { path: '/business-information-entities/{top_level_asbiep_id}/bbies', method: 'post' },
    update_bbie: {
      path: '/business-information-entities/{top_level_asbiep_id}/bbies/{bbie_id}',
      method: 'post',
    },
    create_bbie_sc: { path: '/business-information-entities/bbie-scs', method: 'post' },
    update_bbie_sc: { path: '/business-information-entities/bbie-scs/{bbie_sc_id}', method: 'post' },
  };

  const target = mapping[key];
  if (!target) return null;
  const op = spec.paths?.[target.path]?.[target.method];
  if (!op) return null;
  return { path: target.path, method: target.method, operation: op };
}
