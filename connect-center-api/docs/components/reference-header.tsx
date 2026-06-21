'use client';

import { Menu } from 'lucide-react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';

import { DocsSidebar } from '@/components/docs-sidebar';
import { usePlaygroundAuth } from '@/components/playground-auth';
import { Button } from '@/components/ui/button';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { fetchSidebarResources, SidebarResource } from '@/lib/reference-data';
import { stripDocsBasePrefix } from '@/lib/runtime-paths';
import { ThemeToggle } from '@/components/theme-toggle';

export function ReferenceHeader() {
  const pathname = usePathname();
  const normalizedPathname = stripDocsBasePrefix(pathname);
  const canonicalPathname =
    normalizedPathname !== '/' ? normalizedPathname.replace(/\/+$/, '') : '/';
  const [mobileOpen, setMobileOpen] = useState(false);
  const [accountMenuOpen, setAccountMenuOpen] = useState(false);
  const [sidebarResources, setSidebarResources] = useState<SidebarResource[]>([]);
  const { isAuthenticated, isReady, logOut, user } = usePlaygroundAuth();

  // Close any open menus when the route changes. Handled during render
  // (React's "adjust state when a prop changes" pattern) rather than in an
  // effect, so it avoids an extra commit/render pass.
  const [prevPathname, setPrevPathname] = useState(pathname);
  if (prevPathname !== pathname) {
    setPrevPathname(pathname);
    setMobileOpen(false);
    setAccountMenuOpen(false);
  }

  useEffect(() => {
    if (typeof window === 'undefined') {
      return;
    }
    const mediaQuery = window.matchMedia('(min-width: 1280px)');

    const syncSidebarByViewport = () => {
      if (mediaQuery.matches) {
        document.body.classList.remove('docs-sidebar-collapsed');
      } else {
        document.body.classList.add('docs-sidebar-collapsed');
        setMobileOpen(false);
      }
    };

    syncSidebarByViewport();
    mediaQuery.addEventListener('change', syncSidebarByViewport);
    return () => {
      mediaQuery.removeEventListener('change', syncSidebarByViewport);
    };
  }, []);

  useEffect(() => {
    let cancelled = false;
    fetchSidebarResources()
      .then((resources) => {
        if (cancelled) return;
        setSidebarResources(resources);
      })
      .catch(() => {
        if (cancelled) return;
        setSidebarResources([]);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const isOverview =
    canonicalPathname === '/' ||
    canonicalPathname === '/overview' ||
    canonicalPathname === '/resources' ||
    canonicalPathname.startsWith('/resources/');
  const isHome = canonicalPathname === '/';
  const isLogInPage = canonicalPathname === '/login';
  const accountLabel = user?.loginId ?? 'Authenticated';
  const accountAvatarText =
    (user?.username ?? user?.loginId ?? 'Authenticated').trim().charAt(0).toUpperCase() || 'A';
  const toggleNavigationPanel = () => {
    if (!isHome && typeof window !== 'undefined' && window.innerWidth >= 1280) {
      document.body.classList.toggle('docs-sidebar-collapsed');
      return;
    }
    setMobileOpen((prev) => !prev);
  };

  return (
    <header className="fixed left-0 right-0 top-0 z-40 h-14 border-b border-border bg-white dark:border-[#333333] dark:bg-black">
      <div className="relative flex h-14 w-full items-center gap-3 px-3 sm:px-4 lg:px-5">
        <div className="inline-flex min-w-0 items-center gap-2">
          <button
            type="button"
            aria-label="Open navigation menu"
            onClick={toggleNavigationPanel}
            className="inline-flex h-9 w-9 items-center justify-center rounded-md border border-border text-[#374151] transition hover:bg-[#f3f4f6] dark:border-[#333333] dark:text-[#cbd5e1] dark:hover:bg-[#111111]"
          >
            <Menu className="h-5 w-5" />
          </button>

          <Link href="/" className="inline-flex w-fit items-center whitespace-nowrap">
            <span className="font-brand flex flex-col gap-0.5 leading-[1.05] text-[#111827] dark:text-[#f3f4f6]">
              <span className="text-[1.2rem] font-bold tracking-[0.03em]">connectCenter</span>
              <span className="text-[0.8rem] font-bold tracking-[0.08em]">Developers</span>
            </span>
          </Link>
        </div>

        <div className="ml-auto flex items-center justify-end">
          {isReady ? (
            isAuthenticated ? (
              <Popover open={accountMenuOpen} onOpenChange={setAccountMenuOpen}>
                <PopoverTrigger asChild>
                  <button
                    type="button"
                    className="mr-3 hidden h-[32px] items-center gap-2 rounded-full border border-border px-2 pr-4 text-xs font-semibold text-[#111827] transition hover:bg-[#f3f4f6] dark:bg-[#050505] dark:text-[#f3f4f6] dark:hover:bg-[#111111] sm:inline-flex"
                    aria-label="Open account menu"
                  >
                    <span className="inline-flex h-6 w-6 items-center justify-center rounded-full bg-[#0f172a] text-xs font-extrabold text-white dark:bg-white dark:text-black">
                      {accountAvatarText}
                    </span>
                    <span>{accountLabel}</span>
                  </button>
                </PopoverTrigger>
                <PopoverContent align="end" className="mr-3 w-auto min-w-[220px] rounded-2xl border border-border bg-white p-1.5 dark:bg-black">
                  <div className="px-3 py-2">
                    <div className="text-sm font-semibold text-[#111827] dark:text-[#f3f4f6]">
                      {user?.username ?? user?.loginId ?? 'Authenticated'}
                    </div>
                    <div className="mt-1 text-xs text-[#6b7280] dark:text-[#94a3b8]">
                      {user?.roles.length ? user.roles.join(' • ') : 'No roles'}
                    </div>
                  </div>
                  <div className="my-1 border-t border-border" />
                  <button
                    type="button"
                    onClick={() => {
                      setAccountMenuOpen(false);
                      logOut();
                    }}
                    className="inline-flex h-9 w-full items-center rounded-xl px-3 text-sm font-medium text-[#111827] transition hover:bg-[#f3f4f6] dark:text-[#f3f4f6] dark:hover:bg-[#111111]"
                  >
                    Log out
                  </button>
                </PopoverContent>
              </Popover>
            ) : (
              !isLogInPage ? (
                <Link
                  href={{ pathname: '/login', query: canonicalPathname !== '/' ? { returnTo: canonicalPathname } : undefined }}
                  className="mr-3 hidden items-center rounded-md border border-border px-3 py-2 text-xs font-medium text-[#111827] transition hover:bg-[#f3f4f6] dark:text-[#f3f4f6] dark:hover:bg-[#111111] sm:inline-flex"
                >
                  Log in
                </Link>
              ) : null
            )
          ) : null}
          <ThemeToggle />
        </div>
      </div>

      {isOverview && mobileOpen ? (
        <>
          <button
            type="button"
            aria-label="Close navigation menu"
            onClick={() => setMobileOpen(false)}
            className={`fixed inset-0 top-14 z-40 bg-black/30 ${isHome ? '' : 'xl:hidden'}`}
          />
          <aside
            className={`fixed left-0 top-14 z-50 h-[calc(100vh-56px)] w-[min(336px,92vw)] overflow-y-auto border-r border-border bg-white shadow-[0_20px_45px_-30px_rgba(15,23,42,0.55)] ${
              isHome ? '' : 'xl:hidden'
            } dark:border-[#1f2937] dark:bg-black`}
          >
            <DocsSidebar resources={sidebarResources} mobile />
          </aside>
        </>
      ) : null}
    </header>
  );
}
