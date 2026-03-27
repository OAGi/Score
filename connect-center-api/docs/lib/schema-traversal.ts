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

export type FlattenedField = {
  name: string;
  type: string;
  required: boolean;
  description: string;
};

function isObjectSchema(schema: JsonSchema): boolean {
  const t = schema.type;
  if (t === 'object') return true;
  if (Array.isArray(t) && t.includes('object')) return true;
  return Boolean(schema.properties);
}

function isArraySchema(schema: JsonSchema): boolean {
  const t = schema.type;
  if (t === 'array') return true;
  if (Array.isArray(t) && t.includes('array')) return true;
  return Boolean(schema.items);
}

function normalizeTypeToken(token: string): string {
  if (token === 'integer') return 'integer';
  if (token === 'number') return 'number';
  if (token === 'boolean') return 'boolean';
  if (token === 'string') return 'string';
  if (token === 'null') return 'null';
  if (token === 'object') return 'object';
  if (token === 'array') return 'array';
  return token;
}

function schemaTypeString(schema: JsonSchema, spec: OpenApiSpecWithSchemas, visitedRefs: Set<string>): string {
  if (schema.$ref) {
    const resolved = resolveRef(schema.$ref, spec, visitedRefs);
    if (resolved) return schemaTypeString(resolved, spec, visitedRefs);
    return 'object';
  }

  if (schema.enum && schema.enum.length > 0) {
    // Preserve underlying type if present, otherwise call it a string enum.
    const base = schema.type ? schemaTypeString({ type: schema.type } as JsonSchema, spec, visitedRefs) : 'string';
    return base;
  }

  if (schema.type) {
    if (Array.isArray(schema.type)) {
      const parts = schema.type.map((t) => normalizeTypeToken(t));
      return Array.from(new Set(parts)).join(' | ');
    }
    return normalizeTypeToken(schema.type);
  }

  const union = schema.anyOf ?? schema.oneOf;
  if (union && union.length > 0) {
    const parts = union
      .map((s) => schemaTypeString(s, spec, new Set(visitedRefs)))
      .flatMap((t) => t.split(' | ').map((x) => x.trim()))
      .filter(Boolean);
    return Array.from(new Set(parts)).join(' | ') || 'object';
  }

  if (schema.allOf && schema.allOf.length > 0) {
    // allOf is usually "object-ish"; represent it as object.
    return 'object';
  }

  if (isArraySchema(schema)) {
    const itemType = schema.items ? schemaTypeString(schema.items, spec, new Set(visitedRefs)) : 'unknown';
    return `array[${itemType}]`;
  }

  if (isObjectSchema(schema)) return 'object';

  return 'unknown';
}

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

export function flattenSchemaFields(opts: {
  schema: unknown;
  spec: OpenApiSpecWithSchemas;
  // Root fields are flattened without a leading container name.
  prefix?: string;
  maxDepth?: number;
}): FlattenedField[] {
  const { schema, spec, prefix = '', maxDepth = 8 } = opts;

  const out: FlattenedField[] = [];
  const fieldIndex = new Map<string, number>();

  const mergeTypeStrings = (a: string, b: string): string => {
    const parse = (value: string) => value.split('|').map((v) => v.trim()).filter(Boolean);
    const merged = Array.from(new Set([...parse(a), ...parse(b)]));
    return merged.join(' | ');
  };

  const pushField = (field: FlattenedField) => {
    if (!field.name) return;
    const index = fieldIndex.get(field.name);
    if (index === undefined) {
      fieldIndex.set(field.name, out.length);
      out.push(field);
      return;
    }
    const existing = out[index];
    out[index] = {
      ...existing,
      type: mergeTypeStrings(existing.type, field.type),
      required: existing.required && field.required,
      description: existing.description || field.description,
    };
  };

  const walk = (current: JsonSchema, namePrefix: string, depth: number) => {
    if (depth > maxDepth) return;

    const visited = new Set<string>();
    let node: JsonSchema = current;
    if (node.$ref) {
      node = resolveRef(node.$ref, spec, visited) ?? node;
    }
    if (node.allOf && node.allOf.length > 0) {
      node = mergeAllOf(node, spec, visited);
    }

    // If this is a union with null, prefer the non-null branch for structure,
    // but keep the type string as a union.
    const union = node.anyOf ?? node.oneOf;
    if (union && union.length > 0) {
      const nonNull = union.find((s) => {
        const t = s.type;
        if (t === 'null') return false;
        if (Array.isArray(t) && t.length === 1 && t[0] === 'null') return false;
        return true;
      });
      if (nonNull) {
        node = { ...nonNull, description: node.description ?? nonNull.description };
      }
    }

    // Resolve refs after union selection (e.g., nullable $ref schemas).
    if (node.$ref) {
      const resolved = resolveRef(node.$ref, spec, visited);
      if (resolved) {
        node = { ...resolved, description: node.description ?? resolved.description };
      }
    }
    if (node.allOf && node.allOf.length > 0) {
      node = mergeAllOf(node, spec, visited);
    }

    if (isObjectSchema(node)) {
      const req = new Set<string>(node.required ?? []);
      for (const [propName, propSchema] of Object.entries(node.properties ?? {})) {
        const fullName = `${namePrefix}${propName}`;
        const isRequired = req.has(propName);
        pushField({
          name: fullName,
          type: schemaTypeString(propSchema, spec, new Set<string>()),
          required: isRequired,
          description: String(propSchema.description ?? ''),
        });

        // Recurse for nested objects/arrays-of-objects.
        const shouldRecurse =
          Boolean(propSchema.$ref) ||
          Boolean(propSchema.allOf && propSchema.allOf.length > 0) ||
          Boolean(propSchema.anyOf && propSchema.anyOf.length > 0) ||
          Boolean(propSchema.oneOf && propSchema.oneOf.length > 0) ||
          isObjectSchema(propSchema) ||
          isArraySchema(propSchema);
        if (shouldRecurse) {
          walk(propSchema, `${fullName}.`, depth + 1);
        }
      }
      return;
    }

    if (isArraySchema(node)) {
      const item = node.items;
      if (!item) return;
      const arrayName = namePrefix.endsWith('.') ? namePrefix.slice(0, -1) : namePrefix;
      const base = arrayName.endsWith('[]') ? arrayName : `${arrayName}[]`;

      // If the caller is walking an array property, `base` is already present as a field.
      // Here we only walk nested fields (for object items).
      const resolvedItem = item.$ref ? resolveRef(item.$ref, spec, new Set<string>()) ?? item : item;
      const itemUnion = resolvedItem.anyOf ?? resolvedItem.oneOf;

      // Support array items defined as oneOf/anyOf object schemas.
      // Example: relationships: { type: array, items: { oneOf: [Ascc, Bcc] } }
      if (itemUnion && itemUnion.length > 0) {
        for (let variantIndex = 0; variantIndex < itemUnion.length; variantIndex += 1) {
          const variant = itemUnion[variantIndex];
          const resolvedVariant = variant.$ref ? resolveRef(variant.$ref, spec, new Set<string>()) ?? variant : variant;
          if (!isObjectSchema(resolvedVariant)) continue;
          const variantName =
            (typeof variant.$ref === 'string' && variant.$ref.startsWith('#/components/schemas/')
              ? variant.$ref.slice('#/components/schemas/'.length)
              : undefined) ??
            (typeof resolvedVariant.title === 'string' ? resolvedVariant.title : undefined) ??
            `Variant${variantIndex + 1}`;
          const variantNodeName = `${base}.oneOf(${variantName})`;
          pushField({
            name: variantNodeName,
            type: 'object',
            required: false,
            description: `One-of variant: ${variantName}`,
          });

          const itemReq = new Set<string>(resolvedVariant.required ?? []);
          for (const [propName, propSchema] of Object.entries(resolvedVariant.properties ?? {})) {
            const fullName = `${variantNodeName}.${propName}`;
            pushField({
              name: fullName,
              type: schemaTypeString(propSchema, spec, new Set<string>()),
              required: itemReq.has(propName),
              description: String(propSchema.description ?? ''),
            });
            const shouldRecurse =
              Boolean(propSchema.$ref) ||
              Boolean(propSchema.allOf && propSchema.allOf.length > 0) ||
              Boolean(propSchema.anyOf && propSchema.anyOf.length > 0) ||
              Boolean(propSchema.oneOf && propSchema.oneOf.length > 0) ||
              isObjectSchema(propSchema) ||
              isArraySchema(propSchema);
            if (shouldRecurse) {
              walk(propSchema, `${fullName}.`, depth + 1);
            }
          }
        }
        return;
      }

      if (isObjectSchema(resolvedItem)) {
        const itemReq = new Set<string>(resolvedItem.required ?? []);
        for (const [propName, propSchema] of Object.entries(resolvedItem.properties ?? {})) {
          const fullName = `${base}.${propName}`;
          pushField({
            name: fullName,
            type: schemaTypeString(propSchema, spec, new Set<string>()),
            required: itemReq.has(propName),
            description: String(propSchema.description ?? ''),
          });
          const shouldRecurse =
            Boolean(propSchema.$ref) ||
            Boolean(propSchema.allOf && propSchema.allOf.length > 0) ||
            Boolean(propSchema.anyOf && propSchema.anyOf.length > 0) ||
            Boolean(propSchema.oneOf && propSchema.oneOf.length > 0) ||
            isObjectSchema(propSchema) ||
            isArraySchema(propSchema);
          if (shouldRecurse) {
            walk(propSchema, `${fullName}.`, depth + 1);
          }
        }
      }
    }
  };

  if (!schema || typeof schema !== 'object') return [];
  walk(schema as JsonSchema, prefix, 0);
  return out;
}
