'use client';

import { ReactNode, useEffect, useState } from 'react';

import { DocsSidebar } from '@/components/docs-sidebar';
import { fetchSidebarResources, SidebarResource } from '@/lib/reference-data';

export default function ResourceLayout({ children }: { children: ReactNode }) {
  const [resources, setResources] = useState<SidebarResource[]>([]);

  useEffect(() => {
    let cancelled = false;
    fetchSidebarResources()
      .then((value) => {
        if (cancelled) return;
        setResources(value);
      })
      .catch(() => {
        if (cancelled) return;
        setResources([]);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="flex h-[calc(100vh-56px)] overflow-hidden">
      <DocsSidebar resources={resources} />
      <div className="min-w-0 flex-1 overflow-y-auto bg-white dark:bg-black">
        {children}
      </div>
    </div>
  );
}
