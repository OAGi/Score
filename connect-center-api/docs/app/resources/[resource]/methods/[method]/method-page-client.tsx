'use client';

import { useEffect, useMemo, useState } from 'react';

import { MethodReference } from '@/components/method-reference';
import { fetchReferenceMethod, MethodDoc, MethodKey, ResourceKey } from '@/lib/reference-data';

const RESOURCE_TITLES: Record<string, string> = {
  accounts: 'Accounts',
  libraries: 'Libraries',
  releases: 'Releases',
  namespaces: 'Namespaces',
  core_components: 'Core Components',
  business_information_entities: 'Business Information Entity',
  data_types: 'Data Types',
  xbts: 'XBTs (XML Built-in Types)',
  code_lists: 'Code Lists',
  agency_id_lists: 'Agency ID Lists',
  tags: 'Tags',
  context_categories: 'Context Categories',
  context_schemes: 'Context Schemes',
  business_contexts: 'Business Contexts',
};

function isResourceKey(resource: string): resource is ResourceKey {
  return resource in RESOURCE_TITLES;
}

export function MethodPageClient({ resource, method }: { resource: string; method: MethodKey }) {
  const [doc, setDoc] = useState<MethodDoc | null>(null);
  const [notFound, setNotFound] = useState(false);
  const resourceTitle = useMemo(() => RESOURCE_TITLES[resource] ?? resource, [resource]);

  useEffect(() => {
    let cancelled = false;
    setDoc(null);
    setNotFound(false);
    if (!isResourceKey(resource)) {
      setNotFound(true);
      return;
    }
    fetchReferenceMethod(resource, method)
      .then((value) => {
        if (cancelled) return;
        if (!value) {
          setNotFound(true);
          return;
        }
        setDoc(value);
      })
      .catch(() => {
        if (cancelled) return;
        setNotFound(true);
      });

    return () => {
      cancelled = true;
    };
  }, [resource, method]);

  if (notFound) {
    return (
      <div className="mx-auto max-w-[980px] px-4 py-8 md:px-6 lg:px-8">
        <h1 className="text-2xl font-semibold tracking-tight text-[#0f172a]">Not found</h1>
        <p className="mt-2 text-sm text-[#4b5563]">This method documentation is not available.</p>
      </div>
    );
  }

  if (!doc) {
    return (
      <div className="mx-auto max-w-[980px] px-4 py-8 md:px-6 lg:px-8">
        <div className="h-6 w-64 animate-pulse rounded bg-[#f3f4f6]" />
        <div className="mt-4 h-4 w-96 animate-pulse rounded bg-[#f3f4f6]" />
        <div className="mt-8 h-48 w-full animate-pulse rounded-xl border border-border bg-white" />
      </div>
    );
  }

  return <MethodReference doc={doc} resource={resource as ResourceKey} resourceTitle={resourceTitle} />;
}
