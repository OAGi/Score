'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

import { Badge } from '@/components/ui/badge';
import { methodHref, SidebarResource } from '@/lib/reference-data';
import { getDocsBasePrefix, stripDocsBasePrefix } from '@/lib/runtime-paths';
import { getOverviewActiveState } from '@/lib/sidebar-activation';

type Props = {
  resources: SidebarResource[];
  mobile?: boolean;
};

type SidebarSubgroup = {
  id: string;
  label: string;
  methodKeys: SidebarResource['methods'][number]['key'][];
  defaultExpanded: boolean;
};

const resourceSubgroups: Record<string, SidebarSubgroup[]> = {
  context_schemes: [
    {
      id: 'values',
      label: 'Context Scheme Value',
      methodKeys: [
        'retrieve_value_by_id',
        'create_value',
        'update_value',
        'delete_value',
      ],
      defaultExpanded: false,
    },
  ],
  business_contexts: [
    {
      id: 'values',
      label: 'Business Context Value',
      methodKeys: [
        'retrieve_value_by_id',
        'create_value',
        'update_value',
        'delete_value',
      ],
      defaultExpanded: false,
    },
  ],
  core_components: [
    {
      id: 'acc',
      label: 'ACC',
      methodKeys: ['get_acc'],
      defaultExpanded: true,
    },
    {
      id: 'asccp',
      label: 'ASCCP',
      methodKeys: ['get_asccp'],
      defaultExpanded: true,
    },
    {
      id: 'bccp',
      label: 'BCCP',
      methodKeys: ['get_bccp'],
      defaultExpanded: true,
    },
  ],
  business_information_entities: [
    {
      id: 'asbie',
      label: 'ASBIE',
      methodKeys: [
        'get_asbie_by_asbie_id',
        'get_asbie_by_based_ascc_manifest_id',
        'create_asbie',
        'update_asbie',
        'reuse_top_level_asbiep',
        'remove_reused_top_level_asbiep',
      ],
      defaultExpanded: true,
    },
    {
      id: 'bbie',
      label: 'BBIE',
      methodKeys: [
        'get_bbie_by_bbie_id',
        'get_bbie_by_based_bcc_manifest_id',
        'create_bbie',
        'update_bbie',
      ],
      defaultExpanded: true,
    },
    {
      id: 'bbie-sc',
      label: 'BBIE_SC',
      methodKeys: ['create_bbie_sc', 'update_bbie_sc'],
      defaultExpanded: true,
    },
  ],
};

function buildInitialExpandedSubgroups(
  pathname: string,
  resources: SidebarResource[],
): Record<string, boolean> {
  const next: Record<string, boolean> = {};
  for (const resource of resources) {
    const groups = resourceSubgroups[resource.resource] ?? [];
    const existingMethodKeys = new Set(resource.methods.map((method) => method.key));
    for (const group of groups) {
      const groupMethods = group.methodKeys.filter((key) => existingMethodKeys.has(key));
      if (groupMethods.length === 0) {
        continue;
      }
      const groupKey = `${resource.resource}-${group.id}`;
      const hasActiveMethod = groupMethods.some(
        (methodKey) => pathname === methodHref(methodKey, resource.resource),
      );
      next[groupKey] = hasActiveMethod;
    }
  }
  return next;
}

export function DocsSidebar({ resources, mobile = false }: Props) {
  const pathname = usePathname();
  const basePrefix = getDocsBasePrefix(pathname);
  const normalizedPathname = stripDocsBasePrefix(pathname);
  // `trailingSlash: true` can produce pathnames ending in `/`.
  const canonicalPathname =
    normalizedPathname !== '/' ? normalizedPathname.replace(/\/+$/, '') : '/';
  const onOverviewPage = canonicalPathname === '/overview';
  const [hash, setHash] = useState('');
  const [activeOverviewSection, setActiveOverviewSection] = useState<'introduction' | 'getting-started' | 'authentication'>('introduction');
  const [expandedResources, setExpandedResources] = useState<Record<string, boolean>>(() =>
    Object.fromEntries(
      resources.map((resource) => [
        resource.resource,
        canonicalPathname === `/resources/${resource.resource}` ||
          resource.methods.some((method) => methodHref(method.key, resource.resource) === canonicalPathname),
      ]),
    ),
  );
  const [expandedSubgroups, setExpandedSubgroups] = useState<Record<string, boolean>>(
    () => buildInitialExpandedSubgroups(canonicalPathname, resources),
  );

  useEffect(() => {
    setExpandedResources(() => {
      const next: Record<string, boolean> = {};
      for (const resource of resources) {
        next[resource.resource] =
          canonicalPathname === `/resources/${resource.resource}` ||
          resource.methods.some((method) => methodHref(method.key, resource.resource) === canonicalPathname);
      }
      return next;
    });
  }, [resources, canonicalPathname]);

  useEffect(() => {
    const updateHash = () => setHash(window.location.hash || '');
    updateHash();
    window.addEventListener('hashchange', updateHash);
    return () => window.removeEventListener('hashchange', updateHash);
  }, []);

  useEffect(() => {
    const fallback = getOverviewActiveState(canonicalPathname, hash);
    if (!onOverviewPage) {
      if (fallback.authenticationActive) {
        setActiveOverviewSection('authentication');
      } else if (fallback.gettingStartedActive) {
        setActiveOverviewSection('getting-started');
      } else {
        setActiveOverviewSection('introduction');
      }
      return;
    }

    // Reflect hash immediately, but keep scroll listener active so state updates while scrolling.
    if (hash === '#authentication') {
      setActiveOverviewSection('authentication');
    }
    if (hash === '#getting-started') {
      setActiveOverviewSection('getting-started');
    }
    if (hash === '#introduction') {
      setActiveOverviewSection('introduction');
    }

    const scrollRoot = document.getElementById('reference-overview-scroll');
    const intro = document.getElementById('introduction');
    const gettingStarted = document.getElementById('getting-started');
    const auth = document.getElementById('authentication');

    if (!scrollRoot || !intro || !gettingStarted || !auth) {
      if (fallback.authenticationActive) {
        setActiveOverviewSection('authentication');
      } else if (fallback.gettingStartedActive) {
        setActiveOverviewSection('getting-started');
      } else {
        setActiveOverviewSection('introduction');
      }
      return;
    }

    const sectionTop = (element: HTMLElement) =>
      element.getBoundingClientRect().top - scrollRoot.getBoundingClientRect().top + scrollRoot.scrollTop;

    const updateByScroll = () => {
      const atBottom =
        Math.ceil(scrollRoot.scrollTop + scrollRoot.clientHeight) >= Math.floor(scrollRoot.scrollHeight) - 2;
      if (atBottom) {
        setActiveOverviewSection('authentication');
        return;
      }

      const marker = scrollRoot.scrollTop + 96;
      const gettingStartedTop = sectionTop(gettingStarted);
      const authTop = sectionTop(auth);
      if (marker >= authTop) {
        setActiveOverviewSection('authentication');
        return;
      }
      if (marker >= gettingStartedTop) {
        setActiveOverviewSection('getting-started');
        return;
      }
      setActiveOverviewSection('introduction');
    };

    updateByScroll();
    scrollRoot.addEventListener('scroll', updateByScroll, { passive: true });
    return () => scrollRoot.removeEventListener('scroll', updateByScroll);
  }, [canonicalPathname, hash, onOverviewPage]);

  useEffect(() => {
    setExpandedSubgroups(buildInitialExpandedSubgroups(canonicalPathname, resources));
  }, [canonicalPathname, resources]);

  const introductionActive = onOverviewPage && activeOverviewSection === 'introduction';
  const gettingStartedActive = onOverviewPage && activeOverviewSection === 'getting-started';
  const authenticationActive = onOverviewPage && activeOverviewSection === 'authentication';
  const navBase =
    'block rounded-md px-2.5 py-1.5 text-[13px] leading-5 transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#93c5fd] focus-visible:ring-offset-1 focus-visible:ring-offset-white dark:focus-visible:ring-[#60a5fa] dark:focus-visible:ring-offset-black';
  const activeNavClass = 'bg-[#dbeafe] font-semibold text-[#0f172a] shadow-[inset_0_0_0_1px_rgba(59,130,246,0.25)] dark:bg-[#172554] dark:text-white dark:shadow-[inset_0_0_0_1px_rgba(96,165,250,0.35)]';
  const inactiveNavClass = 'bg-transparent text-[#374151] hover:bg-[#f4f4f5] hover:text-[#111827] dark:text-[#cbd5e1] dark:hover:bg-[#111111] dark:hover:text-white';
  const methodBadgeClass = {
    GET: 'border-blue-200 bg-blue-50 text-blue-700 dark:border-blue-400/60 dark:bg-blue-500/15 dark:text-blue-200',
    POST: 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-400/60 dark:bg-emerald-500/15 dark:text-emerald-200',
    PUT: 'border-amber-200 bg-amber-50 text-amber-700 dark:border-amber-400/70 dark:bg-amber-500/15 dark:text-amber-200',
    DELETE: 'border-rose-200 bg-rose-50 text-rose-700 dark:border-rose-400/70 dark:bg-rose-500/15 dark:text-rose-200',
  } as const;
  const getDocsBasePrefixFromLocation = () => {
    if (typeof window === 'undefined') {
      return basePrefix;
    }
    return getDocsBasePrefix(window.location.pathname);
  };

  const scrollOverviewToSection = (section: 'introduction' | 'getting-started' | 'authentication') => {
    const root = document.getElementById('reference-overview-scroll');
    if (!root) {
      return;
    }
    const docsBasePrefix = getDocsBasePrefixFromLocation();

    if (section === 'introduction') {
      root.scrollTo({ top: 0, behavior: 'smooth' });
      window.history.replaceState(null, '', `${docsBasePrefix}/overview/`);
      return;
    }

    const target = document.getElementById(section);
    if (!target) {
      return;
    }
    const targetTop =
      target.getBoundingClientRect().top - root.getBoundingClientRect().top + root.scrollTop;
    root.scrollTo({ top: Math.max(0, targetTop - 12), behavior: 'smooth' });
    window.history.replaceState(null, '', `${docsBasePrefix}/overview/#${section}`);
  };

  const rootClassName = mobile
    ? 'docs-sidebar-root block h-full w-full shrink-0 overflow-y-auto border-r border-border bg-[linear-gradient(180deg,#ffffff_0%,#fbfdff_100%)] dark:border-[#1f2937] dark:bg-[linear-gradient(180deg,#000000_0%,#060b14_100%)]'
    : 'docs-sidebar-root hidden h-[calc(100vh-56px)] w-[336px] shrink-0 overflow-y-auto border-r border-border bg-[linear-gradient(180deg,#ffffff_0%,#fbfdff_100%)] dark:border-[#1f2937] dark:bg-[linear-gradient(180deg,#000000_0%,#060b14_100%)] xl:block';

  return (
    <aside className={rootClassName}>
      <nav className="px-3 py-3">
        <div className="mb-4">
          <p className="mb-1 px-2 py-1.5 text-[11px] font-semibold uppercase tracking-[0.12em] text-[#9ca3af] dark:text-[#9ca3af]">API Reference</p>
          <Link
            href="/overview"
            onClick={(event) => {
              setActiveOverviewSection('introduction');
              if (onOverviewPage) {
                event.preventDefault();
                scrollOverviewToSection('introduction');
              }
            }}
            className={[
              `${navBase} mt-0.5`,
              introductionActive
                ? activeNavClass
                : inactiveNavClass,
            ].join(' ')}
          >
            Introduction
          </Link>
          <Link
            href="/overview#getting-started"
            onClick={(event) => {
              setActiveOverviewSection('getting-started');
              if (onOverviewPage) {
                event.preventDefault();
                scrollOverviewToSection('getting-started');
              }
            }}
            className={[
              `${navBase} mt-0.5`,
              gettingStartedActive
                ? activeNavClass
                : inactiveNavClass,
            ].join(' ')}
          >
            Getting Started
          </Link>
          <Link
            href="/overview#authentication"
            onClick={(event) => {
              setActiveOverviewSection('authentication');
              if (onOverviewPage) {
                event.preventDefault();
                scrollOverviewToSection('authentication');
              }
            }}
            className={[
              `${navBase} mt-0.5`,
              authenticationActive
                ? activeNavClass
                : inactiveNavClass,
            ].join(' ')}
          >
            Authentication
          </Link>
        </div>

        <div className="border-t border-border pt-3 dark:border-[#1f2937]">
          <p className="px-2 py-1.5 text-[11px] font-semibold uppercase tracking-[0.12em] text-[#9ca3af] dark:text-[#9ca3af]">Platform APIs</p>
        </div>

        {resources.map((resource) => {
          const expanded = expandedResources[resource.resource] ?? true;
          const resourceHref = `/resources/${resource.resource}`;
          const resourceActive = canonicalPathname === resourceHref;
          const groups = (resourceSubgroups[resource.resource] ?? [])
            .map((group) => ({
              ...group,
              methods: group.methodKeys
                .map((methodKey) => resource.methods.find((method) => method.key === methodKey))
                .filter((method): method is SidebarResource['methods'][number] => Boolean(method)),
            }))
            .filter((group) => group.methods.length > 0);
          const groupedMethodKeys = new Set(groups.flatMap((group) => group.methodKeys));
          const regularMethods = resource.methods.filter((method) => !groupedMethodKeys.has(method.key));
          return (
            <div key={resource.resource} className="mt-2">
              <div className="flex items-center gap-1">
                <Link
                  href={resourceHref}
                  className={[
                    'min-w-0 flex-1 rounded-md px-2 py-1 text-left text-[13px] font-semibold transition',
                    resourceActive
                      ? 'bg-[#dbeafe] text-[#0f172a] shadow-[inset_0_0_0_1px_rgba(59,130,246,0.25)] dark:bg-[#172554] dark:text-white dark:shadow-[inset_0_0_0_1px_rgba(96,165,250,0.35)]'
                      : 'text-[#111827] hover:bg-[#f4f4f5] dark:text-white dark:hover:bg-[#111111]',
                  ].join(' ')}
                >
                  {resource.title}
                </Link>
                <button
                  type="button"
                  onClick={() =>
                    setExpandedResources((prev) => ({
                      ...prev,
                      [resource.resource]: !(prev[resource.resource] ?? true),
                    }))
                  }
                  aria-label={`Toggle ${resource.title} methods`}
                  aria-expanded={expanded}
                  aria-controls={`${resource.resource}-methods`}
                  className="inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-md text-[#6b7280] transition hover:bg-[#f4f4f5] dark:text-[#cbd5e1] dark:hover:bg-[#111111]"
                >
                  <svg
                    className={[
                      'h-3.5 w-3.5 transition-transform',
                      expanded ? 'rotate-90' : 'rotate-0',
                    ].join(' ')}
                    viewBox="0 0 20 20"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                  >
                    <path d="M8 5L13 10L8 15" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </button>
              </div>

              {expanded ? (
                <div id={`${resource.resource}-methods`} className="mt-2 space-y-0.5">
                  <ul className="space-y-0.5">
                    {regularMethods.map((method) => {
                      const href = methodHref(method.key, resource.resource);
                      const active = canonicalPathname === href;

                      return (
                        <li key={`${resource.resource}-${method.key}`}>
                          <Link
                            href={href}
                            className={[
                              `${navBase} flex items-center gap-2`,
                              active ? activeNavClass : inactiveNavClass,
                            ].join(' ')}
                          >
                            <Badge
                              className={[
                                'w-[56px] shrink-0 justify-center px-1.5 py-0.5 text-[9px] leading-4',
                                methodBadgeClass[method.method],
                              ].join(' ')}
                            >
                              {method.method}
                            </Badge>
                            <span className="min-w-0 whitespace-normal leading-5">{method.title}</span>
                          </Link>
                        </li>
                      );
                    })}
                  </ul>

                  {groups.map((group) => {
                    const groupKey = `${resource.resource}-${group.id}`;
                    const groupExpanded = expandedSubgroups[groupKey] ?? group.defaultExpanded;
                    return (
                      <div key={groupKey} className="mt-2">
                        <button
                          type="button"
                          onClick={() =>
                            setExpandedSubgroups((prev) => ({ ...prev, [groupKey]: !(prev[groupKey] ?? group.defaultExpanded) }))
                          }
                          aria-expanded={groupExpanded}
                          aria-controls={`${groupKey}-methods`}
                          className={[
                            `${navBase} flex w-full items-center justify-between text-left`,
                            inactiveNavClass,
                          ].join(' ')}
                        >
                          <span>{group.label}</span>
                          <svg
                            className={[
                              'h-3.5 w-3.5 text-[#6b7280] transition-transform dark:text-[#cbd5e1]',
                              groupExpanded ? 'rotate-90' : 'rotate-0',
                            ].join(' ')}
                            viewBox="0 0 20 20"
                            fill="none"
                            xmlns="http://www.w3.org/2000/svg"
                            aria-hidden="true"
                          >
                            <path d="M8 5L13 10L8 15" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
                          </svg>
                        </button>
                        {groupExpanded ? (
                          <ul id={`${groupKey}-methods`} className="mt-1 space-y-0.5 pl-3">
                            {group.methods.map((method) => {
                              const href = methodHref(method.key, resource.resource);
                              const active = canonicalPathname === href;

                              return (
                                <li key={`${resource.resource}-${method.key}`}>
                                  <Link
                                    href={href}
                                    className={[
                                      `${navBase} flex items-center gap-2`,
                                      active ? activeNavClass : inactiveNavClass,
                                    ].join(' ')}
                                  >
                                    <Badge
                                      className={[
                                        'w-[56px] shrink-0 justify-center px-1.5 py-0.5 text-[9px] leading-4',
                                        methodBadgeClass[method.method],
                                      ].join(' ')}
                                    >
                                      {method.method}
                                    </Badge>
                                    <span className="min-w-0 whitespace-normal leading-5">{method.title}</span>
                                  </Link>
                                </li>
                              );
                            })}
                          </ul>
                        ) : null}
                      </div>
                    );
                  })}
                </div>
              ) : null}
            </div>
          );
        })}
      </nav>
    </aside>
  );
}
