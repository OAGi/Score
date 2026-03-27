type JsonSchema = Record<string, unknown> & {
  $ref?: string;
  type?: string | string[];
  title?: string;
  description?: string;
  properties?: Record<string, JsonSchema>;
  required?: string[];
  items?: JsonSchema;
  enum?: unknown[];
  anyOf?: JsonSchema[];
  oneOf?: JsonSchema[];
  allOf?: JsonSchema[];
  additionalProperties?: boolean | JsonSchema;
  examples?: unknown[];
  example?: unknown;
};

type OpenApiSpecWithSchemas = {
  components?: {
    schemas?: Record<string, unknown>;
  };
};

function resolveRef(ref: string, spec: OpenApiSpecWithSchemas, visitedRefs: Set<string>): JsonSchema | null {
  if (!ref.startsWith('#/components/schemas/')) return null;
  if (visitedRefs.has(ref)) return null;
  visitedRefs.add(ref);
  const key = ref.slice('#/components/schemas/'.length);
  const schema = spec.components?.schemas?.[key];
  return schema && typeof schema === 'object' ? (schema as JsonSchema) : null;
}

function mergeAllOf(schema: JsonSchema, spec: OpenApiSpecWithSchemas, visitedRefs: Set<string>): JsonSchema {
  const merged: JsonSchema = { ...schema };
  const all = schema.allOf ?? [];

  const outProps: Record<string, JsonSchema> = { ...(schema.properties ?? {}) };
  const required = new Set<string>(schema.required ?? []);

  for (const part of all) {
    const resolved = part.$ref ? resolveRef(part.$ref, spec, visitedRefs) ?? part : part;
    const normalized = resolved.allOf ? mergeAllOf(resolved, spec, visitedRefs) : resolved;
    for (const [name, prop] of Object.entries(normalized.properties ?? {})) {
      if (!(name in outProps)) outProps[name] = prop;
    }
    for (const req of normalized.required ?? []) required.add(req);
  }

  if (Object.keys(outProps).length > 0) merged.properties = outProps;
  if (required.size > 0) merged.required = Array.from(required);
  merged.type = merged.type ?? 'object';
  return merged;
}

function normalizeSchema(schema: JsonSchema, spec: OpenApiSpecWithSchemas, visitedRefs: Set<string>): JsonSchema {
  let node = schema;
  if (node.$ref) node = resolveRef(node.$ref, spec, visitedRefs) ?? node;
  if (node.allOf && node.allOf.length > 0) node = mergeAllOf(node, spec, visitedRefs);

  const union = node.anyOf ?? node.oneOf;
  if (union && union.length > 0) {
    const nonNull = union.find((s) => {
      const t = s.type;
      if (t === 'null') return false;
      if (Array.isArray(t) && t.length === 1 && t[0] === 'null') return false;
      return true;
    });
    if (nonNull) node = { ...nonNull, description: node.description ?? nonNull.description };
  }

  return node;
}

function defaultScalarExample(schema: JsonSchema): unknown {
  if (schema.enum && schema.enum.length > 0) return schema.enum[0];

  const t = schema.type;
  const typeToken = Array.isArray(t) ? t.find((x) => x !== 'null') : t;

  if (typeToken === 'integer') return 1;
  if (typeToken === 'number') return 1;
  if (typeToken === 'boolean') return true;
  if (typeToken === 'string') return 'string';
  if (typeToken === 'null') return null;
  return null;
}

export function buildExampleFromSchema(opts: {
  schema: unknown;
  spec: OpenApiSpecWithSchemas;
  maxDepth?: number;
}): unknown {
  const { schema, spec, maxDepth = 6 } = opts;
  if (!schema || typeof schema !== 'object') return null;

  const walk = (node: JsonSchema, depth: number, visitedRefs: Set<string>): unknown => {
    if (depth > maxDepth) return null;
    const normalized = normalizeSchema(node, spec, visitedRefs);

    if (normalized.example !== undefined) return normalized.example;
    if (Array.isArray(normalized.examples) && normalized.examples.length > 0) return normalized.examples[0];

    const t = normalized.type;
    const typeToken = Array.isArray(t) ? t.find((x) => x !== 'null') : t;

    if (typeToken === 'object' || normalized.properties) {
      const out: Record<string, unknown> = {};
      const req = new Set<string>(normalized.required ?? []);
      const props = normalized.properties ?? {};
      for (const [key, propSchema] of Object.entries(props)) {
        // Include required fields, plus optional fields that have examples.
        const include =
          req.has(key) ||
          propSchema.example !== undefined ||
          (Array.isArray(propSchema.examples) && propSchema.examples.length > 0);
        if (!include) continue;
        out[key] = walk(propSchema, depth + 1, new Set(visitedRefs));
      }
      return out;
    }

    if (typeToken === 'array' || normalized.items) {
      const item = normalized.items;
      if (!item) return [];
      return [walk(item, depth + 1, new Set(visitedRefs))];
    }

    return defaultScalarExample(normalized);
  };

  return walk(schema as JsonSchema, 0, new Set<string>());
}

