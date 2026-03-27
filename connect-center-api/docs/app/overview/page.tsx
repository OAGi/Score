'use client';

import { useEffect, useState } from 'react';

import { DocsSidebar } from '@/components/docs-sidebar';
import { ReferenceOverviewContent } from '@/components/reference-overview-content';
import { buildFallbackAuthentication, fetchContextCategoryResource, fetchSidebarResources, SidebarResource } from '@/lib/reference-data';

const STATIC_FALLBACK_API_BASE = 'http://127.0.0.1:5555/api';

export default function ReferenceOverviewPage() {
  const [sidebarResources, setSidebarResources] = useState<SidebarResource[]>([]);
  const [auth, setAuth] = useState(() => buildFallbackAuthentication(STATIC_FALLBACK_API_BASE));

  useEffect(() => {
    let cancelled = false;
    Promise.all([fetchContextCategoryResource(), fetchSidebarResources()])
      .then(([resource, resources]) => {
        if (cancelled) return;
        setAuth(resource.authentication ?? buildFallbackAuthentication());
        setSidebarResources(resources);
      })
      .catch(() => {
        if (cancelled) return;
        setAuth(buildFallbackAuthentication());
        setSidebarResources([
          {
            resource: 'libraries',
            title: 'Library',
            methods: [
              { key: 'list', method: 'GET', title: 'List libraries', endpoint: '/libraries' },
              { key: 'retrieve', method: 'GET', title: 'Retrieve a library', endpoint: '/libraries/{library_id}' },
            ],
          },
          {
            resource: 'releases',
            title: 'Release',
            methods: [
              { key: 'list', method: 'GET', title: 'List releases', endpoint: '/releases' },
              { key: 'retrieve', method: 'GET', title: 'Retrieve a release', endpoint: '/releases/{release_id}' },
            ],
          },
          {
            resource: 'namespaces',
            title: 'Namespace',
            methods: [
              { key: 'list', method: 'GET', title: 'List namespaces', endpoint: '/namespaces' },
              { key: 'retrieve', method: 'GET', title: 'Retrieve a namespace', endpoint: '/namespaces/{namespace_id}' },
            ],
          },
          {
            resource: 'accounts',
            title: 'Account',
            methods: [
              { key: 'list', method: 'GET', title: 'List app users', endpoint: '/accounts' },
              { key: 'retrieve', method: 'GET', title: 'Get app user by ID', endpoint: '/accounts/{app_user_id}' },
              { key: 'who_am_i', method: 'GET', title: 'Get current authenticated app user', endpoint: '/me' },
            ],
          },
          {
            resource: 'context_categories',
            title: 'Context Category',
            methods: [
              { key: 'create', method: 'POST', title: 'Create a context category', endpoint: '/context-categories' },
              { key: 'update', method: 'POST', title: 'Update a context category', endpoint: '/context-categories/{context_category_id}' },
              { key: 'delete', method: 'DELETE', title: 'Delete a context category', endpoint: '/context-categories/{context_category_id}' },
              { key: 'list', method: 'GET', title: 'List context categories', endpoint: '/context-categories' },
              { key: 'retrieve', method: 'GET', title: 'Retrieve a context category', endpoint: '/context-categories/{context_category_id}' },
            ],
          },
        ]);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="flex h-[calc(100vh-56px)] overflow-hidden">
      <DocsSidebar resources={sidebarResources} />

      <main id="reference-overview-scroll" className="min-w-0 flex-1 overflow-y-auto bg-[radial-gradient(circle_at_15%_0%,#eef5ff_0%,#ffffff_58%)] dark:bg-[radial-gradient(circle_at_15%_0%,#0b1220_0%,#000000_58%)]">
        <div className="px-4 py-8 pb-[40vh] md:px-6 lg:px-8">
          <div className="mb-8 rounded-2xl border border-[#dbe3ec] bg-white/95 p-6 shadow-[0_26px_60px_-42px_rgba(15,23,42,0.55)] dark:border-[#1f2937] dark:bg-black/90 md:p-7">
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-[#64748b] dark:text-[#94a3b8]">API Reference</p>
            <h1 className="mt-2 text-3xl font-semibold tracking-tight text-[#0f172a] font-title dark:text-white">Overview</h1>
            <p className="mt-3 text-3xl text-sm text-[#475569] dark:text-[#cbd5e1]">
              Manage libraries, releases, namespaces, core components, business information entities, context categories, and more through a unified REST interface. Learn how to authenticate, explore available resources, and follow the BOM profiling walkthrough to build your first profile end-to-end.
            </p>
          </div>

          <ReferenceOverviewContent authentication={auth} />
        </div>
      </main>
    </div>
  );
}
