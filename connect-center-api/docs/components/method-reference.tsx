'use client';

import { Braces, ChevronRight } from 'lucide-react';
import Link from 'next/link';
import { ReactNode, useEffect, useState } from 'react';

import { MethodMarkdownActions } from '@/components/method-markdown-actions';
import { CopyButton } from '@/components/copy-button';
import { usePlaygroundAuth } from '@/components/playground-auth';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import { MultiSelect } from '@/components/ui/multi-select';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Separator } from '@/components/ui/separator';
import { resolveBackendApiBase } from '@/lib/openapi';
import { MethodDoc, ResourceKey } from '@/lib/reference-data';

type FieldFilter = 'all' | 'required' | 'optional';

function FieldFilterSelect({
  value,
  onChange,
}: {
  value: FieldFilter;
  onChange: (value: FieldFilter) => void;
}) {
  return (
    <Select value={value} onValueChange={(next) => onChange(next as FieldFilter)}>
      <SelectTrigger className="h-7 !w-auto min-w-[78px] !flex !justify-between px-2 text-[11px] text-[#111827] !outline-none !ring-0 !focus:outline-none !focus:ring-0 !focus:ring-offset-0 !focus-visible:outline-none !focus-visible:ring-0 !focus-visible:ring-offset-0 dark:text-[#e5e7eb]">
        <SelectValue placeholder="Filter" />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="all">All</SelectItem>
        <SelectItem value="required">Required</SelectItem>
        <SelectItem value="optional">Optional</SelectItem>
      </SelectContent>
    </Select>
  );
}

function MethodBadge({ method }: { method: MethodDoc['method'] }) {
  const classes = {
    GET: 'border-blue-200 bg-blue-50 text-blue-700 dark:border-blue-400/60 dark:bg-blue-500/15 dark:text-blue-200',
    POST: 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-400/60 dark:bg-emerald-500/15 dark:text-emerald-200',
    PUT: 'border-amber-200 bg-amber-50 text-amber-700 dark:border-amber-400/70 dark:bg-amber-500/15 dark:text-amber-200',
    PATCH: 'border-violet-200 bg-violet-50 text-violet-700 dark:border-violet-400/70 dark:bg-violet-500/15 dark:text-violet-200',
    DELETE: 'border-rose-200 bg-rose-50 text-rose-700 dark:border-rose-400/70 dark:bg-rose-500/15 dark:text-rose-200',
  };

  return <Badge className={classes[method]}>{method}</Badge>;
}

function NestedTreeBody({ children }: { children: ReactNode }) {
  return (
    <div className="border-t border-border pl-4">
      <div className="ml-2 border-border">
        <div className="rounded-lg bg-white">{children}</div>
      </div>
    </div>
  );
}

function ExpandableFieldBox({
  name,
  type,
  description,
  children,
  className = 'mt-3',
  chevronLeftClassName = '-left-3',
  required,
}: {
  name: string;
  type: string;
  description: string;
  children?: ReactNode;
  className?: string;
  chevronLeftClassName?: string;
  required?: boolean;
}) {
  return (
    <details className={`${className} group overflow-visible rounded-lg bg-white dark:bg-[#050505]`}>
      <summary className="relative cursor-pointer list-none px-4 py-3 [&::-webkit-details-marker]:hidden">
        <ChevronRight
          className={`absolute ${chevronLeftClassName} top-3.5 h-4 w-4 text-[#94a3b8] transition-transform group-open:rotate-90`}
        />
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <span className="font-mono text-[12px] text-[#0f172a]">{name}</span>
            <span className="rounded border border-border bg-[#f8fafc] px-2 py-0.5 text-[11px] text-[#6b7280]">{type}</span>
            {typeof required === 'boolean' ? (
              <span className="ml-auto rounded-full border border-border px-2 py-0.5 text-[11px] text-[#6b7280]">
                {required ? 'required' : 'optional'}
              </span>
            ) : null}
          </div>
          <p className="mt-1 text-sm text-[#4b5563]">{description}</p>
        </div>
      </summary>
      {children ? <NestedTreeBody>{children}</NestedTreeBody> : null}
    </details>
  );
}

function PropertyList({ fields, requiredFilter }: { fields: MethodDoc['body_params']; requiredFilter: FieldFilter }) {
  if (fields.length === 0) {
    return <p className="text-sm text-muted-foreground">No parameters.</p>;
  }

  const matchesRequiredFilter = (required?: boolean): boolean => {
    if (requiredFilter === 'all') {
      return true;
    }
    if (typeof required !== 'boolean') {
      return false;
    }
    return requiredFilter === 'required' ? required : !required;
  };

  const topLevelFields = fields.filter((field) => !field.name.includes('.'));
  const sourceRootFields = topLevelFields.length > 0 ? topLevelFields : fields;
  const rootFields = sourceRootFields.filter((field) => matchesRequiredFilter(field.required));

  return (
    <div className="divide-y divide-border rounded-lg bg-white dark:bg-[#050505]">
      {rootFields.map((field) => {
        const prefix = childFieldPrefix(field.name, field.type);
        const children = fields.filter((candidate) => candidate.name.startsWith(prefix) && matchesRequiredFilter(candidate.required));
        const expandable = (isObjectLikeType(field.type) || isArrayType(field.type)) && children.length > 0;

        if (!expandable) {
          return (
            <div key={field.name} className="px-4 py-3">
              <div className="flex items-center gap-2">
                <span className="font-mono text-[12px] text-[#0f172a]">{field.name}</span>
                <span className="rounded border border-border bg-[#f8fafc] px-2 py-0.5 text-[11px] text-[#6b7280]">
                  {field.type}
                </span>
                <span className="ml-auto rounded-full border border-border px-2 py-0.5 text-[11px] text-[#6b7280]">
                  {field.required ? 'required' : 'optional'}
                </span>
              </div>
              <p className="mt-1 text-sm text-[#4b5563]">{field.description}</p>
            </div>
          );
        }

        return (
          <details key={field.name} className="group overflow-visible">
            <summary className="relative cursor-pointer list-none px-4 py-3 [&::-webkit-details-marker]:hidden">
              <ChevronRight className="absolute -left-3 top-3.5 h-4 w-4 text-[#94a3b8] transition-transform group-open:rotate-90" />
              <div className="flex items-center gap-2">
                <span className="font-mono text-[12px] text-[#0f172a]">{field.name}</span>
                <span className="rounded border border-border bg-[#f8fafc] px-2 py-0.5 text-[11px] text-[#6b7280]">
                  {field.type}
                </span>
                <span className="ml-auto rounded-full border border-border px-2 py-0.5 text-[11px] text-[#6b7280]">
                  {field.required ? 'required' : 'optional'}
                </span>
              </div>
            </summary>
            <p className="mt-2 text-sm text-[#4b5563]">{field.description}</p>
            <NestedTreeBody>
              {children.map((child, index) => (
                <div key={child.name} className={index === 0 ? 'px-4 py-3' : 'border-t border-border px-4 py-3'}>
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-[12px] text-[#0f172a]">{childFieldLabel(field.name, field.type, child.name)}</span>
                    <span className="rounded border border-border bg-[#f8fafc] px-2 py-0.5 text-[11px] text-[#6b7280]">
                      {child.type}
                    </span>
                    <span className="ml-auto rounded-full border border-border px-2 py-0.5 text-[11px] text-[#6b7280]">
                      {child.required ? 'required' : 'optional'}
                    </span>
                  </div>
                  <p className="mt-1 text-sm text-[#4b5563]">{child.description}</p>
                </div>
              ))}
            </NestedTreeBody>
          </details>
        );
      })}
    </div>
  );
}

type ReturnField = MethodDoc['return_fields'][number];

type ReturnFieldNode = {
  label: string;
  order: number;
  field?: ReturnField;
  children: ReturnFieldNode[];
  childMap: Map<string, ReturnFieldNode>;
};

function ReturnsList({
  fields,
  summary,
  requiredFilter,
}: {
  fields: MethodDoc['return_fields'];
  summary: string;
  requiredFilter: FieldFilter;
}) {
  const [selectedOneOf, setSelectedOneOf] = useState<Record<string, string>>({});

  if (fields.length === 0) {
    return <p className="text-sm text-[#4b5563]">{summary}</p>;
  }

  const knownPaths = new Set(fields.map((field) => field.name));
  const canonicalKnownPaths = new Set(
    fields.map((field) => field.name.replaceAll('[]', '')),
  );

  const normalizeSegments = (name: string): string[] => {
    const rawSegments = name.split('.');
    return rawSegments.map((segment, index) => {
      if (!segment.endsWith('[]')) {
        return segment;
      }

      const singular = segment.slice(0, -2);
      const rawPrefix = rawSegments.slice(0, index);
      const singularPrefix = rawPrefix.map((part) => (part.endsWith('[]') ? part.slice(0, -2) : part));
      const singularPath = [...singularPrefix, singular].join('.');
      const canonicalPrefix = rawPrefix.map((part) => part.replace(/\[\]$/, ''));
      const canonicalSingularPath = [...canonicalPrefix, singular].join('.');

      return knownPaths.has(singularPath) || canonicalKnownPaths.has(canonicalSingularPath)
        ? singular
        : segment;
    });
  };

  const roots: ReturnFieldNode[] = [];
  const rootMap = new Map<string, ReturnFieldNode>();
  let orderCounter = 0;

  const getOrCreateNode = (
    label: string,
    siblings: ReturnFieldNode[],
    siblingMap: Map<string, ReturnFieldNode>,
  ): ReturnFieldNode => {
    const existing = siblingMap.get(label);
    if (existing) {
      return existing;
    }
    const created: ReturnFieldNode = {
      label,
      order: orderCounter++,
      children: [],
      childMap: new Map<string, ReturnFieldNode>(),
    };
    siblingMap.set(label, created);
    siblings.push(created);
    return created;
  };

  for (const field of fields) {
    const segments = normalizeSegments(field.name);
    let currentSiblings = roots;
    let currentMap = rootMap;
    let currentNode: ReturnFieldNode | null = null;

    for (const segment of segments) {
      currentNode = getOrCreateNode(segment, currentSiblings, currentMap);
      currentSiblings = currentNode.children;
      currentMap = currentNode.childMap;
    }

    if (currentNode) {
      currentNode.field = field;
    }
  }

  const getNodeType = (node: ReturnFieldNode): string => {
    if (node.field?.type) {
      return node.field.type;
    }
    return node.children.length > 0 ? 'object' : 'string';
  };

  const getNodeDescription = (node: ReturnFieldNode): string => {
    if (node.field?.description) {
      return node.field.description;
    }
    if (node.children.length > 0) {
      return `Nested fields of ${node.label}.`;
    }
    return '';
  };

  const renderLeaf = (node: ReturnFieldNode) => (
    <div className="px-4 py-3">
      <div className="flex items-center gap-2">
        <span className="font-mono text-[12px] text-[#0f172a]">{node.label}</span>
        <span className="rounded border border-border bg-[#f8fafc] px-2 py-0.5 text-[11px] text-[#6b7280]">
          {getNodeType(node)}
        </span>
        {typeof node.field?.required === 'boolean' ? (
          <span className="ml-auto rounded-full border border-border px-2 py-0.5 text-[11px] text-[#6b7280]">
            {node.field.required ? 'required' : 'optional'}
          </span>
        ) : null}
      </div>
      {getNodeDescription(node) ? <p className="mt-1 text-sm text-[#4b5563]">{getNodeDescription(node)}</p> : null}
    </div>
  );

  const getVariantName = (label: string): string | null => {
    const match = /^oneOf\((.+)\)$/.exec(label);
    return match ? match[1] : null;
  };

  const matchesRequiredFilter = (required?: boolean): boolean => {
    if (requiredFilter === 'all') {
      return true;
    }
    if (typeof required !== 'boolean') {
      return false;
    }
    return requiredFilter === 'required' ? required : !required;
  };

  const nodeMatchesFilter = (node: ReturnFieldNode): boolean => {
    if (requiredFilter === 'all') {
      return true;
    }
    if (typeof node.field?.required === 'boolean') {
      return matchesRequiredFilter(node.field.required);
    }
    return node.children.some((child) => nodeMatchesFilter(child));
  };

  const renderNode = (node: ReturnFieldNode, depth: number, nodePath: string): ReactNode => {
    if (!nodeMatchesFilter(node)) {
      return null;
    }
    const objectLike = /\bobject\b/i.test(getNodeType(node));
    const sortedChildren = [...node.children].sort((a, b) => a.order - b.order);
    const variantChildren = sortedChildren.filter((child) => getVariantName(child.label));
    const hasOneOfVariants = variantChildren.length > 0 && variantChildren.length === sortedChildren.length;

    if (node.children.length === 0 && !objectLike) {
      return renderLeaf(node);
    }

    if (hasOneOfVariants) {
      const selectedLabel = selectedOneOf[nodePath] ?? variantChildren[0].label;
      const activeVariant = variantChildren.find((child) => child.label === selectedLabel) ?? variantChildren[0];
      return (
        <ExpandableFieldBox
          name={node.label}
          type={getNodeType(node)}
          description={getNodeDescription(node)}
          className="mt-0"
          chevronLeftClassName={depth === 0 ? '-left-3' : '-left-2'}
          required={node.field?.required}
        >
          <div className="px-4 py-3">
            <div className="flex flex-wrap items-center gap-2 text-xs text-[#6b7280]">
              {variantChildren.map((variant, index) => {
                const variantName = getVariantName(variant.label) ?? variant.label;
                const selected = variant.label === activeVariant.label;
                return (
                  <div key={`${nodePath}-${variant.label}`} className="contents">
                    {index > 0 ? <span>or</span> : null}
                    <label className="flex cursor-pointer items-center gap-2 rounded border border-border px-2 py-1 font-mono text-[#0f172a]">
                      <Checkbox
                        checked={selected}
                        onCheckedChange={(checked) => {
                          if (checked) {
                            setSelectedOneOf((prev) => ({ ...prev, [nodePath]: variant.label }));
                          }
                        }}
                      />
                      <span>{variantName}</span>
                    </label>
                  </div>
                );
              })}
            </div>
          </div>
          <div className="rounded-lg bg-white dark:bg-[#050505]">
            {activeVariant.children.length > 0 ? (
                [...activeVariant.children]
                  .sort((a, b) => a.order - b.order)
                  .filter((child) => nodeMatchesFilter(child))
                  .map((child, index) => (
                  <div key={`${activeVariant.label}-${child.label}-${index}`} className="border-t border-border">
                    {renderNode(child, depth + 1, `${nodePath}.${activeVariant.label}.${child.label}`)}
                  </div>
                ))
            ) : (
              <div className="border-t border-border px-4 py-3 text-sm text-[#6b7280]">No nested fields documented.</div>
            )}
          </div>
        </ExpandableFieldBox>
      );
    }

    return (
      <ExpandableFieldBox
        name={node.label}
        type={getNodeType(node)}
        description={getNodeDescription(node)}
        className="mt-0"
        chevronLeftClassName={depth === 0 ? '-left-3' : '-left-2'}
        required={node.field?.required}
      >
        <div className="rounded-lg bg-white dark:bg-[#050505]">
          {sortedChildren.length > 0 ? (
            sortedChildren
              .filter((child) => nodeMatchesFilter(child))
              .map((child, index) => (
              <div key={`${node.label}-${child.label}-${index}`} className={index === 0 ? '' : 'border-t border-border'}>
                {renderNode(child, depth + 1, `${nodePath}.${child.label}`)}
              </div>
              ))
          ) : (
            <div className="px-4 py-3 text-sm text-[#6b7280]">No nested fields documented.</div>
          )}
        </div>
      </ExpandableFieldBox>
    );
  };

  const sortedRoots = [...roots].sort((a, b) => a.order - b.order);
  const visibleRoots = sortedRoots.filter((node) => nodeMatchesFilter(node));

  return (
    <div className="rounded-lg bg-white dark:bg-[#050505]">
      <div className="px-4 py-2 text-xs font-medium text-[#6b7280]">{summary}</div>
      {visibleRoots.map((node, index) => (
        <div key={`${node.label}-${index}`} className="border-t border-border">
          {renderNode(node, 0, node.label)}
        </div>
      ))}
    </div>
  );
}

function ParamsCard({
  title,
  badge,
  description,
  fields,
}: {
  title: string;
  badge: string;
  description?: string;
  fields: MethodDoc['body_params'];
}) {
  const [requiredFilter, setRequiredFilter] = useState<FieldFilter>('all');

  return (
    <Card className="border-0 bg-transparent shadow-none">
      <CardHeader className="px-0 pb-2 pt-0">
        <div className="flex items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2 text-[15px]">
            <span>{title}</span>
            <Badge className="gap-1 border-blue-200 bg-blue-50 text-blue-700 dark:border-blue-400/60 dark:bg-blue-500/15 dark:text-blue-200">
              <Braces className="h-3 w-3" />
              {badge}
            </Badge>
          </CardTitle>
          <FieldFilterSelect value={requiredFilter} onChange={setRequiredFilter} />
        </div>
        {description ? <CardDescription className="text-xs">{description}</CardDescription> : null}
      </CardHeader>
      <CardContent className="px-0 pb-0">
        <PropertyList fields={fields} requiredFilter={requiredFilter} />
      </CardContent>
    </Card>
  );
}

type CodeKind = 'HTTP' | 'JSON';
type TokenClass = 'plain' | 'key' | 'string' | 'number' | 'keyword' | 'method' | 'option' | 'url' | 'env' | 'punct';

type Token = {
  text: string;
  className: TokenClass;
};

const tokenClassNames: Record<TokenClass, string> = {
  plain: 'syntax-token-plain',
  key: 'syntax-token-key',
  string: 'syntax-token-string',
  number: 'syntax-token-number',
  keyword: 'syntax-token-keyword',
  method: 'syntax-token-method',
  option: 'syntax-token-option',
  url: 'syntax-token-url',
  env: 'syntax-token-env',
  punct: 'syntax-token-punct',
};

function tokenizeLine(line: string, kind: CodeKind): Token[] {
  const regexByKind: Record<CodeKind, RegExp> = {
    JSON: /("(?:\\.|[^"\\])*"\s*:?)|(-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)|\b(?:true|false|null)\b|([{}\[\],:])/g,
    HTTP:
      /\b(?:curl|GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)\b|--?[a-zA-Z-]+|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|https?:\/\/[^\s'"]+|\$[A-Z0-9_]+|[{}\[\],:]/g,
  };

  const matcher = regexByKind[kind];
  const tokens: Token[] = [];
  let cursor = 0;

  const classify = (match: string): TokenClass => {
    if (kind === 'JSON') {
      if (match.startsWith('"')) {
        return match.trimEnd().endsWith(':') ? 'key' : 'string';
      }
      if (/^-?\d/.test(match)) return 'number';
      if (/^(true|false|null)$/.test(match)) return 'keyword';
      if (/^[{}\[\],:]$/.test(match)) return 'punct';
      return 'plain';
    }

    if (/^(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)$/i.test(match) || match === 'curl') return 'method';
    if (/^--?/.test(match)) return 'option';
    if (/^https?:\/\//.test(match)) return 'url';
    if (/^\$[A-Z0-9_]+$/.test(match)) return 'env';
    if (match.startsWith('"') || match.startsWith("'")) return 'string';
    if (/^[{}\[\],:]$/.test(match)) return 'punct';
    return 'plain';
  };

  for (const hit of line.matchAll(matcher)) {
    const value = hit[0];
    const index = hit.index ?? 0;
    if (index > cursor) {
      tokens.push({ text: line.slice(cursor, index), className: 'plain' });
    }
    tokens.push({ text: value, className: classify(value) });
    cursor = index + value.length;
  }

  if (cursor < line.length) {
    tokens.push({ text: line.slice(cursor), className: 'plain' });
  }

  if (tokens.length === 0) {
    return [{ text: line || ' ', className: 'plain' }];
  }

  return tokens;
}

function renderHighlightedLine(line: string, kind: CodeKind): ReactNode {
  return tokenizeLine(line, kind).map((token, tokenIndex) => (
    <span key={`${token.text}-${tokenIndex}`} className={tokenClassNames[token.className]}>
      {token.text}
    </span>
  ));
}

function CodePane({ title, code, kind }: { title: string; code: string; kind: string }) {
  const lines = code.split('\n');
  const syntaxKind: CodeKind = kind === 'JSON' ? 'JSON' : 'HTTP';

  return (
    <div className="rounded-xl border border-border bg-white">
      <div className="flex items-center justify-between gap-2 px-4 py-3">
        <div className="text-sm font-semibold text-[#111827]">{title}</div>
        <div className="flex items-center gap-2">
          <Button size="sm" variant="outline" className="h-7 rounded-full px-2.5 text-[11px]">
            {kind}
          </Button>
          <CopyButton value={code} className="h-7 px-2" />
        </div>
      </div>
      <Separator />
      <div className="syntax-highlight overflow-auto rounded-b-xl bg-[#f8fafc] p-4 dark:bg-[#0b1220]">
        <pre className="m-0 text-xs leading-6">
          <code>
            {lines.map((line, index) => (
              <div key={`${index}-${line}`} className="grid grid-cols-[2.5rem_minmax(0,1fr)] gap-3">
                <span className="syntax-line-number select-none text-right">{index + 1}</span>
                <span className="min-w-0 whitespace-pre-wrap break-words">{renderHighlightedLine(line || ' ', syntaxKind)}</span>
              </div>
            ))}
          </code>
        </pre>
      </div>
    </div>
  );
}

const playgroundApiBase = resolveBackendApiBase();

function isObjectLikeType(type: string): boolean {
  return /\bobject\b/i.test(type);
}

function isArrayType(type: string): boolean {
  return /\barray\b/i.test(type);
}

function childFieldPrefix(fieldName: string, fieldType: string): string {
  return isArrayType(fieldType) ? `${fieldName}[].` : `${fieldName}.`;
}

function childFieldLabel(parentName: string, parentType: string, childName: string): string {
  return childName.slice(childFieldPrefix(parentName, parentType).length);
}

function getStructuredBodyFields(fields: MethodDoc['body_params']): MethodDoc['body_params'] {
  return fields.filter((field) => {
    const prefix = childFieldPrefix(field.name, field.type);
    const hasChildren = fields.some((candidate) => candidate.name.startsWith(prefix));
    if ((isObjectLikeType(field.type) || isArrayType(field.type)) && hasChildren) {
      return false;
    }
    return true;
  });
}

function buildInitialBodyValues(fields: MethodDoc['body_params']): Record<string, string> {
  return Object.fromEntries(getStructuredBodyFields(fields).map((field) => [field.name, '']));
}

function setNestedBodyValue(target: Record<string, unknown>, path: string, value: unknown) {
  const segments = path.split('.');
  let current: Record<string, unknown> = target;

  for (let index = 0; index < segments.length; index += 1) {
    const segment = segments[index];
    const isArraySegment = segment.endsWith('[]');
    const key = segment.replace(/\[\]$/, '');
    const isLast = index === segments.length - 1;

    if (isLast) {
      if (isArraySegment) {
        current[key] = Array.isArray(value) ? value : [value];
      } else {
        current[key] = value;
      }
      return;
    }

    if (isArraySegment) {
      const existing = current[key];
      if (!Array.isArray(existing) || existing.length === 0 || typeof existing[0] !== 'object' || existing[0] === null) {
        current[key] = [{}];
      }
      const arrayValue = current[key] as Record<string, unknown>[];
      current = arrayValue[0] as Record<string, unknown>;
      continue;
    }

    const existing = current[key];
    if (typeof existing !== 'object' || existing === null || Array.isArray(existing)) {
      current[key] = {};
    }
    current = current[key] as Record<string, unknown>;
  }
}

function parseStructuredBodyValue(field: MethodDoc['body_params'][number], rawValue: string): { value?: unknown; error?: string } {
  const trimmed = rawValue.trim();
  if (!trimmed) {
    return {};
  }

  if (/\bboolean\b/i.test(field.type)) {
    if (trimmed === 'true') return { value: true };
    if (trimmed === 'false') return { value: false };
    return { error: `Field "${field.name}" must be either true or false.` };
  }

  if (/\binteger\b/i.test(field.type)) {
    if (!/^-?\d+$/.test(trimmed)) {
      return { error: `Field "${field.name}" must be a whole number.` };
    }
    return { value: Number.parseInt(trimmed, 10) };
  }

  if (/\bnumber\b/i.test(field.type)) {
    const parsed = Number(trimmed);
    if (Number.isNaN(parsed)) {
      return { error: `Field "${field.name}" must be a valid number.` };
    }
    return { value: parsed };
  }

  if (isArrayType(field.type) || isObjectLikeType(field.type)) {
    try {
      const parsed = JSON.parse(trimmed);
      if (isArrayType(field.type) && !Array.isArray(parsed)) {
        return { error: `Field "${field.name}" must be a JSON array.` };
      }
      if (isObjectLikeType(field.type) && !isArrayType(field.type) && (typeof parsed !== 'object' || parsed === null || Array.isArray(parsed))) {
        return { error: `Field "${field.name}" must be a JSON object.` };
      }
      return { value: parsed };
    } catch {
      return {
        error: `Field "${field.name}" must be valid JSON ${isArrayType(field.type) ? 'array' : 'object'} content.`,
      };
    }
  }

  return { value: rawValue };
}

function buildStructuredBodyPayload(
  fields: MethodDoc['body_params'],
  values: Record<string, string>,
): { payload?: Record<string, unknown>; error?: string } {
  const payload: Record<string, unknown> = {};

  for (const field of getStructuredBodyFields(fields)) {
    const rawValue = values[field.name] ?? '';
    if (!rawValue.trim()) {
      if (field.required) {
        return { error: `Body parameter "${field.name}" is required.` };
      }
      continue;
    }

    const { value, error } = parseStructuredBodyValue(field, rawValue);
    if (error) {
      return { error };
    }
    if (value !== undefined) {
      setNestedBodyValue(payload, field.name, value);
    }
  }

  return { payload };
}

function ApiPlayground({
  doc,
  pathParams,
  queryParams,
  bodyParams,
}: {
  doc: MethodDoc;
  pathParams: MethodDoc['body_params'];
  queryParams: MethodDoc['body_params'];
  bodyParams: MethodDoc['body_params'];
}) {
  const { authorizationHeader } = usePlaygroundAuth();
  const [pathValues, setPathValues] = useState<Record<string, string>>(
    Object.fromEntries(
      pathParams.map((field) => [
        field.name,
        field.name === 'ctx_category_id' || field.name === 'context_category_id'
          ? '1'
          : '',
      ]),
    ),
  );
  const [queryValues, setQueryValues] = useState<Record<string, string>>(
    Object.fromEntries(
      queryParams.map((field) => [
        field.name,
        field.name === 'offset'
          ? '0'
          : field.name === 'limit'
            ? '10'
            : '',
      ]),
    ),
  );
  const [orderBySelections, setOrderBySelections] = useState<Record<string, string[]>>(
    Object.fromEntries(
      queryParams
        .filter((field) => field.name === 'order_by' && field.order_by_columns && field.order_by_columns.length > 0)
        .map((field) => [field.name, []]),
    ),
  );
  const [orderByDirections, setOrderByDirections] = useState<Record<string, Record<string, 'asc' | 'desc'>>>({});
  const [bodyValues, setBodyValues] = useState<Record<string, string>>(buildInitialBodyValues(bodyParams));
  const [loading, setLoading] = useState(false);
  const [requestUrl, setRequestUrl] = useState('');
  const [statusLine, setStatusLine] = useState('');
  const [responseText, setResponseText] = useState('');
  const [clientError, setClientError] = useState('');

  const methodSupportsBody = doc.method === 'POST' || doc.method === 'PUT';
  const structuredBodyFields = getStructuredBodyFields(bodyParams);
  const bodyFieldSignature = structuredBodyFields
    .map((field) => `${field.name}:${field.type}:${field.required ? '1' : '0'}`)
    .join('|');
  const showStructuredBodyForm = methodSupportsBody && structuredBodyFields.length > 0;
  const structuredBodyPreview = JSON.stringify(buildStructuredBodyPayload(bodyParams, bodyValues).payload ?? {}, null, 2);
  const showDividerBeforeSubmit = !authorizationHeader || pathParams.length > 0 || queryParams.length > 0 || methodSupportsBody;

  useEffect(() => {
    setBodyValues(buildInitialBodyValues(bodyParams));
  }, [bodyFieldSignature]);

  const serializeOrderBy = (columns: string[], directionMap: Record<string, 'asc' | 'desc'>): string => {
    return columns
      .map((column) => `${directionMap[column] === 'desc' ? '-' : '+'}${column}`)
      .join(',');
  };

  const runRequest = async () => {
    setClientError('');
    setResponseText('');
    setStatusLine('');

    if (!authorizationHeader) {
      setClientError('Log in to use the playground.');
      return;
    }

    let path = doc.endpoint;
    for (const field of pathParams) {
      const value = (pathValues[field.name] || '').trim();
      if (!value) {
        setClientError(`Path parameter "${field.name}" is required.`);
        return;
      }
      path = path.replace(`{${field.name}}`, encodeURIComponent(value));
    }

    const url = new URL(`${playgroundApiBase}${path}`);
    queryParams.forEach((field) => {
      const value = (queryValues[field.name] || '').trim();
      if (value) {
        url.searchParams.set(field.name, value);
      }
    });
    setRequestUrl(url.toString());

    const headers = new Headers({ Accept: 'application/json' });
    headers.set('Authorization', authorizationHeader);

    let payload: string | undefined;
    if (methodSupportsBody) {
      const { payload: bodyPayload, error } = buildStructuredBodyPayload(bodyParams, bodyValues);
      if (error) {
        setClientError(error);
        return;
      }
      payload = JSON.stringify(bodyPayload ?? {});
      headers.set('Content-Type', 'application/json');
    }

    setLoading(true);
    try {
      const response = await fetch(url.toString(), {
        method: doc.method,
        headers,
        body: payload,
      });
      const raw = await response.text();
      setStatusLine(`${response.status} ${response.statusText}`);

      if (!raw) {
        setResponseText('{}');
      } else {
        try {
          setResponseText(JSON.stringify(JSON.parse(raw), null, 2));
        } catch {
          setResponseText(raw);
        }
      }
    } catch (error) {
      setClientError(error instanceof Error ? error.message : 'Request failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="rounded-xl border border-border bg-white">
      <div className="px-4 py-3">
        <h5 className="text-sm font-semibold text-[#111827]">Playground</h5>
        <p className="mt-1 text-xs text-[#6b7280]">Run this endpoint directly from docs.</p>
      </div>
      <Separator />

      <form
        className="space-y-3 px-4 py-4"
        onSubmit={(event) => {
          event.preventDefault();
          runRequest();
        }}
      >
        {!authorizationHeader ? (
          <div className="rounded-lg border border-border bg-[#f8fafc] p-3">
            <p className="text-xs font-semibold uppercase tracking-[0.08em] text-[#64748b]">Authentication</p>
            <Link
              href="/login"
              className="mt-3 inline-flex items-center rounded-md bg-primary px-3 py-2 text-xs font-medium text-primary-foreground transition hover:bg-primary/90"
            >
              Log in for Playground
            </Link>
          </div>
        ) : null}

        {pathParams.length > 0 ? (
          <div className="grid gap-2">
            <p className="text-xs font-medium text-[#4b5563]">Path Parameters</p>
            {pathParams.map((field) => (
              field.enum_values && field.enum_values.length > 0 ? (
                <MultiSelect
                  key={field.name}
                  options={field.enum_values.map((enumValue) => ({ label: enumValue, value: enumValue }))}
                  values={(pathValues[field.name] || '').split(',').filter(Boolean)}
                  onValuesChange={(selectedValues) =>
                    setPathValues((prev) => ({
                      ...prev,
                      [field.name]: field.is_multi_select ? selectedValues.join(',') : (selectedValues[0] ?? ''),
                    }))
                  }
                  maxSelected={field.is_multi_select ? undefined : 1}
                  placeholder={field.name}
                />
              ) : (
                <input
                  key={field.name}
                  id={`path-${field.name}`}
                  name={`path_${field.name}`}
                  value={pathValues[field.name] || ''}
                  onChange={(event) =>
                    setPathValues((prev) => ({
                      ...prev,
                      [field.name]: event.target.value,
                    }))
                  }
                  placeholder={field.name}
                  className="h-9 rounded-md border border-border px-3 text-sm outline-none focus:ring-2 focus:ring-[#cbd5e1]"
                />
              )
            ))}
          </div>
        ) : null}

        {queryParams.length > 0 ? (
          <div className="grid gap-2">
            <p className="text-xs font-medium text-[#4b5563]">Query Parameters</p>
            {queryParams.map((field) => (
              field.name === 'order_by' && field.order_by_columns && field.order_by_columns.length > 0 ? (
                <div key={field.name} className="grid gap-2 rounded-md border border-border p-3">
                  <p className="text-xs font-medium text-[#4b5563]">{field.name}</p>
                  <MultiSelect
                    options={field.order_by_columns.map((column) => ({ label: column, value: column }))}
                    values={orderBySelections[field.name] || []}
                    onValuesChange={(selectedColumns) => {
                      setOrderBySelections((prev) => ({
                        ...prev,
                        [field.name]: selectedColumns,
                      }));
                      setOrderByDirections((prev) => {
                        const currentDirections = prev[field.name] || {};
                        const nextDirections: Record<string, 'asc' | 'desc'> = {};
                        selectedColumns.forEach((column) => {
                          nextDirections[column] = currentDirections[column] ?? 'asc';
                        });
                        setQueryValues((prevValues) => ({
                          ...prevValues,
                          [field.name]: serializeOrderBy(selectedColumns, nextDirections),
                        }));
                        return {
                          ...prev,
                          [field.name]: nextDirections,
                        };
                      });
                    }}
                    placeholder={field.name}
                  />
                  {(orderBySelections[field.name] || []).map((column) => (
                    <div key={`${field.name}-${column}`} className="grid gap-1">
                      <p className="text-[11px] font-medium text-[#64748b]">{column}</p>
                      <Select
                        value={(orderByDirections[field.name] || {})[column] ?? 'asc'}
                        onValueChange={(next) => {
                          const direction = next === 'desc' ? 'desc' : 'asc';
                          setOrderByDirections((prev) => {
                            const nextFieldDirections: Record<string, 'asc' | 'desc'> = {
                              ...(prev[field.name] || {}),
                              [column]: direction,
                            };
                            const selectedColumns = orderBySelections[field.name] || [];
                            setQueryValues((prevValues) => ({
                              ...prevValues,
                              [field.name]: serializeOrderBy(selectedColumns, nextFieldDirections),
                            }));
                            return {
                              ...prev,
                              [field.name]: nextFieldDirections,
                            };
                          });
                        }}
                      >
                        <SelectTrigger className="h-9 w-full text-sm">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="asc">ASC</SelectItem>
                          <SelectItem value="desc">DESC</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  ))}
                </div>
              ) : field.enum_values && field.enum_values.length > 0 && field.is_multi_select ? (
                <MultiSelect
                  key={field.name}
                  options={field.enum_values.map((enumValue) => ({ label: enumValue, value: enumValue }))}
                  values={(queryValues[field.name] || '').split(',').filter(Boolean)}
                  onValuesChange={(selectedValues) =>
                    setQueryValues((prev) => ({
                      ...prev,
                      [field.name]: selectedValues.join(','),
                    }))
                  }
                  placeholder={field.name}
                />
              ) : field.enum_values && field.enum_values.length > 0 ? (
                <MultiSelect
                  key={field.name}
                  options={field.enum_values.map((enumValue) => ({ label: enumValue, value: enumValue }))}
                  values={queryValues[field.name] ? [queryValues[field.name]] : []}
                  onValuesChange={(selectedValues) =>
                    setQueryValues((prev) => ({
                      ...prev,
                      [field.name]: selectedValues[0] ?? '',
                    }))
                  }
                  maxSelected={1}
                  placeholder={field.name}
                />
              ) : (
                <input
                  key={field.name}
                  id={`query-${field.name}`}
                  name={`query_${field.name}`}
                  value={queryValues[field.name] || ''}
                  onChange={(event) =>
                    setQueryValues((prev) => ({
                      ...prev,
                      [field.name]: event.target.value,
                    }))
                  }
                  placeholder={field.name}
                  className="h-9 rounded-md border border-border px-3 text-sm outline-none focus:ring-2 focus:ring-[#cbd5e1]"
                />
              )
            ))}
          </div>
        ) : null}

        {methodSupportsBody ? (
          <div className="grid gap-2">
            <label className="text-xs font-medium text-[#4b5563]">JSON Body</label>
            {showStructuredBodyForm ? (
              <>
                <div className="grid gap-3 rounded-md border border-border p-3">
                  {structuredBodyFields.map((field) => {
                    const isJsonField = isArrayType(field.type) || isObjectLikeType(field.type);
                    const value = bodyValues[field.name] || '';

                    return (
                      <div key={field.name} className="grid gap-1.5">
                        <div className="flex items-center gap-2">
                          <label htmlFor={`body-${field.name}`} className="text-xs font-medium text-[#334155]">
                            {field.name}
                          </label>
                          <span className="rounded border border-border bg-[#f8fafc] px-2 py-0.5 text-[11px] text-[#6b7280]">
                            {field.type}
                          </span>
                          <span className="ml-auto text-[11px] text-[#6b7280]">
                            {field.required ? 'required' : 'optional'}
                          </span>
                        </div>
                        {field.description ? <p className="text-[11px] text-[#6b7280]">{field.description}</p> : null}
                        {isJsonField ? (
                          <textarea
                            id={`body-${field.name}`}
                            name={`body_${field.name}`}
                            value={value}
                            onChange={(event) =>
                              setBodyValues((prev) => ({
                                ...prev,
                                [field.name]: event.target.value,
                              }))
                            }
                            rows={4}
                            placeholder={isArrayType(field.type) ? '[]' : '{}'}
                            className="w-full rounded-md border border-border px-3 py-2 font-mono text-xs outline-none focus:ring-2 focus:ring-[#cbd5e1]"
                          />
                        ) : (
                          <input
                            id={`body-${field.name}`}
                            name={`body_${field.name}`}
                            value={value}
                            onChange={(event) =>
                              setBodyValues((prev) => ({
                                ...prev,
                                [field.name]: event.target.value,
                              }))
                            }
                            placeholder={field.name}
                            className="h-9 rounded-md border border-border px-3 text-sm outline-none focus:ring-2 focus:ring-[#cbd5e1]"
                          />
                        )}
                      </div>
                    );
                  })}
                </div>
                <div className="rounded-md bg-[#f8fafc] px-3 py-2">
                  <p className="mb-2 text-[11px] font-medium uppercase tracking-[0.08em] text-[#64748b]">Generated JSON Body</p>
                  <pre className="m-0 overflow-auto font-mono text-xs leading-6 text-[#334155]">
                    <code>{structuredBodyPreview}</code>
                  </pre>
                </div>
              </>
            ) : (
              <p className="rounded-md border border-dashed border-border px-3 py-2 text-xs text-[#6b7280]">
                Structured body inputs are not available for this schema yet.
              </p>
            )}
          </div>
        ) : null}

        {showDividerBeforeSubmit ? <Separator /> : null}

        <Button
          type="submit"
          size="sm"
          variant="outline"
          className="border border-input"
          disabled={loading || !authorizationHeader}
        >
          {loading ? 'Sending...' : `${doc.method} ${doc.endpoint}`}
        </Button>

        {requestUrl ? (
          <div className="rounded-md bg-[#f8fafc] px-3 py-2 text-xs text-[#334155]">
            <div className="font-semibold">{doc.method} {requestUrl}</div>
            {statusLine ? <div className="mt-1 text-[#0f172a]">{statusLine}</div> : null}
          </div>
        ) : null}

        {clientError ? <p className="text-xs text-[#dc2626]">{clientError}</p> : null}

        {responseText ? (
          <div className="syntax-highlight overflow-auto rounded-md bg-[#f8fafc] p-3">
            <pre className="m-0 text-xs leading-6">
              <code>{responseText}</code>
            </pre>
          </div>
        ) : null}
      </form>
    </div>
  );
}

type Props = {
  doc: MethodDoc;
  resource?: ResourceKey;
  resourceTitle?: string;
};

export function MethodReference({
  doc,
  resource = 'context_categories',
  resourceTitle = 'Context Categories',
}: Props) {
  const [returnsFilter, setReturnsFilter] = useState<FieldFilter>('all');
  const endpointOptions = [doc.endpoint, ...doc.alternate_endpoints];
  const [selectedEndpoint, setSelectedEndpoint] = useState(doc.endpoint);
  const [endpointMenuOpen, setEndpointMenuOpen] = useState(false);

  useEffect(() => {
    setSelectedEndpoint(doc.endpoint);
    setEndpointMenuOpen(false);
  }, [doc.endpoint, doc.alternate_endpoints]);

  const statusInExample = doc.response_example.match(/\bHTTP\/\d(?:\.\d)?\s+(\d{3})\b/);
  const inferredStatus =
    (statusInExample ? Number(statusInExample[1]) : null) ??
    (doc.method === 'POST' ? 201 : doc.method === 'DELETE' ? 204 : 200);
  const responseStatus = doc.response_status ?? inferredStatus;
  const responseKind: 'HTTP' | 'JSON' = doc.response_example.trimStart().startsWith('HTTP/') ? 'HTTP' : 'JSON';
  const selectedEndpointPathParams = Array.from(selectedEndpoint.matchAll(/\{([^}]+)\}/g), (match) => match[1]);
  const selectedPathNameSet = new Set(selectedEndpointPathParams);
  const allEndpointPathNames = new Set(
    endpointOptions.flatMap((endpoint) => Array.from(endpoint.matchAll(/\{([^}]+)\}/g), (match) => match[1])),
  );
  const pathParams = doc.body_params.filter((field) => selectedPathNameSet.has(field.name));
  const nonPathParams = doc.body_params.filter((field) => !allEndpointPathNames.has(field.name));
  const showSplitParams = pathParams.length > 0 && nonPathParams.length > 0;
  const queryParams = doc.body_type === 'Query' ? nonPathParams : [];
  const bodyParams = doc.body_type === 'JSON' ? nonPathParams : [];
  const formatEndpointDisplay = (endpoint: string): string =>
    queryParams.length > 0
      ? `${endpoint}?${queryParams.map((field) => `${field.name}={${field.name}}`).join('&')}`
      : endpoint;
  const selectedEndpointDisplay = formatEndpointDisplay(selectedEndpoint);
  const selectedRequestExample =
    doc.request_example.replace(doc.endpoint, selectedEndpointDisplay);
  const playgroundDoc =
    selectedEndpoint === doc.endpoint
      ? doc
      : {
          ...doc,
          endpoint: selectedEndpoint,
          request_example: selectedRequestExample,
        };

  return (
    <main className="w-full">
      <div className="px-4 py-7 md:px-6 lg:px-8">
        <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center text-xs text-[#6b7280]">
            <Link href="/overview" className="hover:text-[#111827]">
              API Reference
            </Link>
            <ChevronRight className="mx-1 h-3.5 w-3.5" />
            <Link href={`/resources/${resource}`} className="hover:text-[#111827]">
              {resourceTitle}
            </Link>
            <ChevronRight className="mx-1 h-3.5 w-3.5" />
            <span className="text-[#111827]">{doc.title}</span>
          </div>

          <div className="flex items-center gap-2">
            <MethodMarkdownActions doc={doc} />
          </div>
        </div>

        <div className="mb-6 grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_460px]">
          <div className="rounded-xl bg-white lg:col-start-1 lg:row-start-1">
            <div className="py-5">
              <h1 className="text-[30px] font-semibold tracking-tight text-[#0f172a]">{doc.title}</h1>
              <div className="mt-3 flex flex-wrap items-center gap-2">
                <MethodBadge method={doc.method} />
                <div className="flex min-w-0 flex-1 items-center gap-2">
                  <Select
                    value={selectedEndpoint}
                    onValueChange={setSelectedEndpoint}
                    open={endpointOptions.length > 1 ? endpointMenuOpen : false}
                    onOpenChange={(open) => setEndpointMenuOpen(endpointOptions.length > 1 ? open : false)}
                  >
                    <SelectTrigger
                      className={`h-9 max-w-full min-w-[340px] flex-1 bg-[#f8fafc] font-mono text-[12px] text-[#334155] !outline-none !ring-0 !shadow-none !focus:outline-none !focus:ring-0 !focus:ring-offset-0 !focus:shadow-none !focus-visible:outline-none !focus-visible:ring-0 !focus-visible:ring-offset-0 !focus-visible:shadow-none [--tw-ring-shadow:0_0_#0000] [--tw-ring-offset-shadow:0_0_#0000] [--tw-ring-offset-width:0px] ${
                        endpointOptions.length === 1 ? 'cursor-default' : ''
                      }`}
                      style={{ boxShadow: 'none', outline: 'none' }}
                    >
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {endpointOptions.map((endpoint) => (
                        <SelectItem key={endpoint} value={endpoint} className="font-mono text-[12px]">
                          {formatEndpointDisplay(endpoint)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <CopyButton value={selectedEndpointDisplay} className="h-9 px-3" />
                </div>
              </div>
            </div>
          </div>

          <div className="lg:col-start-1 lg:row-start-2">
            <Separator />

            <section className="space-y-6 py-5">
              <p className="text-sm text-[#374151]">{doc.summary}</p>

              {showSplitParams ? (
                <>
                  <ParamsCard
                    title="Path Parameters"
                    badge="Path"
                    description="Parameters included in the endpoint path."
                    fields={pathParams}
                  />
                  <ParamsCard
                    title={doc.body_type === 'Query' ? 'Query Parameters' : 'Body Parameters'}
                    badge={doc.body_type}
                    description={
                      doc.body_type === 'Query'
                        ? 'Parameters supplied in the query string.'
                        : 'JSON request body fields.'
                    }
                    fields={nonPathParams}
                  />
                </>
              ) : (
                <ParamsCard
                  title={doc.body_label}
                  badge={doc.body_type}
                  fields={doc.body_type === 'Path' ? pathParams : doc.body_params}
                />
              )}

              <Card className="border-0 bg-transparent shadow-none">
                <CardHeader className="px-0 pb-2 pt-0">
                  <div className="flex items-center justify-between gap-3">
                    <CardTitle className="text-[15px]">Returns</CardTitle>
                    <FieldFilterSelect value={returnsFilter} onChange={setReturnsFilter} />
                  </div>
                </CardHeader>
                <CardContent className="px-0 pb-0">
                  <ReturnsList
                    fields={doc.return_fields}
                    summary={doc.returns_summary}
                    requiredFilter={returnsFilter}
                  />
                </CardContent>
              </Card>
            </section>
          </div>

          <aside className="mt-0 space-y-4 lg:col-start-2 lg:row-start-2 lg:px-0">
            <CodePane title={doc.title} code={selectedRequestExample} kind="HTTP" />
            <div>
              <h5 className="mb-2 text-xs font-semibold uppercase tracking-wide text-[#6b7280]">Returns Examples</h5>
              <CodePane title={`${responseStatus} response`} code={doc.response_example} kind={responseKind} />
            </div>
            <ApiPlayground
              key={`${doc.key}:${selectedEndpoint}`}
              doc={playgroundDoc}
              pathParams={pathParams}
              queryParams={queryParams}
              bodyParams={bodyParams}
            />
          </aside>
        </div>
      </div>
    </main>
  );
}
