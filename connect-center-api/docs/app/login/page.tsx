'use client';

import Link from 'next/link';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { FormEvent, startTransition, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { ArrowRight, KeyRound, LoaderCircle, LogOut, ShieldCheck } from 'lucide-react';

import { usePlaygroundAuth } from '@/components/playground-auth';
import { Button } from '@/components/ui/button';
import { sanitizeReturnTo } from '@/lib/auth-utils';
import { clearOidcSessionState, pendingOidcAuthStorageKey } from '@/lib/auth-storage';
import { getRuntimeOidcConfig, resolveDocsPath } from '@/lib/runtime-paths';

const defaultOidcConfig = { enabled: false, providerLabel: 'Single Sign-On' };

export default function LoginPage() {
  const pathname = usePathname();
  const router = useRouter();
  const searchParams = useSearchParams();
  const returnTo = useMemo(() => sanitizeReturnTo(searchParams.get('returnTo')), [searchParams]);
  const authError = searchParams.get('authError');
  const handledPendingOidcRef = useRef(false);
  const {
    authMethod,
    isAuthenticated,
    isReady,
    loginId: storedLoginId,
    logInWithAccessToken,
    logInWithBasic,
    logOut,
    user,
  } = usePlaygroundAuth();
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [hasMounted, setHasMounted] = useState(false);
  const [submittingMode, setSubmittingMode] = useState<'oidc' | 'basic' | null>(null);
  const [oidcConfig, setOidcConfig] = useState(defaultOidcConfig);
  const oidcLoginHref = useMemo(() => {
    const params = new URLSearchParams({ returnTo });
    return `${resolveDocsPath('/auth/login')}?${params.toString()}`;
  }, [returnTo]);
  const continueToDocs = useCallback(() => {
    startTransition(() => {
      router.push(returnTo);
    });
  }, [router, returnTo]);

  useEffect(() => {
    setHasMounted(true);
  }, []);

  useEffect(() => {
    if (!loginId && storedLoginId) {
      setLoginId(storedLoginId);
    }
  }, [loginId, storedLoginId]);

  useEffect(() => {
    const refreshOidcConfig = () => {
      setOidcConfig(getRuntimeOidcConfig());
    };

    refreshOidcConfig();
    window.addEventListener('pageshow', refreshOidcConfig);
    window.addEventListener('popstate', refreshOidcConfig);

    return () => {
      window.removeEventListener('pageshow', refreshOidcConfig);
      window.removeEventListener('popstate', refreshOidcConfig);
    };
  }, []);

  useEffect(() => {
    if (!authError) {
      return;
    }
    clearOidcSessionState();
    logOut();
    setErrorMessage(authError);
  }, [authError, logOut]);

  useEffect(() => {
    if (!isReady || handledPendingOidcRef.current) {
      return;
    }

    handledPendingOidcRef.current = true;

    if (typeof window === 'undefined') {
      return;
    }

    const pendingRaw = window.sessionStorage.getItem(pendingOidcAuthStorageKey);
    if (!pendingRaw) {
      return;
    }

    let pendingAccessToken = '';
    try {
      const parsed = JSON.parse(pendingRaw) as Record<string, unknown>;
      pendingAccessToken = typeof parsed.accessToken === 'string' ? parsed.accessToken : '';
    } catch {
      pendingAccessToken = '';
    }

    window.sessionStorage.removeItem(pendingOidcAuthStorageKey);
    if (!pendingAccessToken) {
      clearOidcSessionState();
      logOut();
      setErrorMessage('OIDC login completed, but no access token was returned.');
      return;
    }

    setSubmittingMode('oidc');
    logInWithAccessToken(pendingAccessToken)
      .then(() => {
        continueToDocs();
      })
      .catch((error) => {
        clearOidcSessionState();
        logOut();
        setErrorMessage(error instanceof Error ? error.message : 'OIDC login failed.');
      })
      .finally(() => {
        setSubmittingMode(null);
      });
  }, [isReady, logInWithAccessToken, logOut, continueToDocs]);

  const handleBasicSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setErrorMessage('');
    setSubmittingMode('basic');
    try {
      await logInWithBasic(loginId, password);
      setPassword('');
      continueToDocs();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Log in failed.');
    } finally {
      setSubmittingMode(null);
    }
  };

  if (!hasMounted) {
    return (
      <main className="min-h-[calc(100vh-56px)] bg-[radial-gradient(circle_at_top,#f8fafc_0%,#eef2ff_40%,#ffffff_100%)] dark:bg-[radial-gradient(circle_at_top,#0b1120_0%,#020617_55%,#000000_100%)]">
        <div className="mx-auto flex min-h-[calc(100vh-56px)] w-full max-w-6xl items-center px-4 py-10 sm:px-6 lg:px-8">
          <div className="mx-auto w-full max-w-md">
            <div className="rounded-[28px] border border-border bg-[linear-gradient(180deg,#f8fafc_0%,#ffffff_100%)] p-6 dark:bg-[linear-gradient(180deg,#050505_0%,#000000_100%)]">
              <div className="flex items-center gap-3">
                <span className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-[#111827] text-white dark:bg-white dark:text-black">
                  <KeyRound className="h-4 w-4" />
                </span>
                <div className="h-5 w-64 animate-pulse rounded bg-[#e5e7eb] dark:bg-[#1f2937]" />
              </div>
              <div className="mt-6 grid gap-4">
                <div className="grid gap-2">
                  <div className="h-3 w-16 animate-pulse rounded bg-[#e5e7eb] dark:bg-[#1f2937]" />
                  <div className="h-11 animate-pulse rounded-2xl bg-[#f3f4f6] dark:bg-[#111827]" />
                </div>
                <div className="grid gap-2">
                  <div className="h-3 w-20 animate-pulse rounded bg-[#e5e7eb] dark:bg-[#1f2937]" />
                  <div className="h-11 animate-pulse rounded-2xl bg-[#f3f4f6] dark:bg-[#111827]" />
                </div>
                <div className="mt-2 h-11 animate-pulse rounded-2xl bg-[#111827]/10 dark:bg-white/10" />
              </div>
            </div>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-[calc(100vh-56px)] bg-[radial-gradient(circle_at_16%_0%,#e8f2ff_0%,#ffffff_55%)] dark:bg-[radial-gradient(circle_at_18%_0%,#0b1220_0%,#000000_58%)]">
      <div className="mx-auto max-w-6xl px-4 py-12 md:px-6 lg:px-8">
        <div className="mx-auto max-w-2xl rounded-[32px] border border-[#dbe3ec] bg-white/95 p-6 shadow-[0_30px_90px_-52px_rgba(15,23,42,0.6)] dark:border-[#1f2937] dark:bg-black/90 md:p-8">
          {isAuthenticated ? (
            <div className="mb-6 rounded-2xl border border-emerald-200 bg-emerald-50/80 p-4 dark:border-emerald-500/40 dark:bg-emerald-500/10">
              <div className="flex flex-wrap items-center justify-between gap-4">
                <div>
                  <p className="text-sm font-semibold text-[#0f172a] dark:text-white">
                    Signed in as {user?.username ?? storedLoginId}
                  </p>
                  <p className="mt-1 text-xs text-[#4b5563] dark:text-[#cbd5e1]">
                    {user?.roles?.length ? user.roles.join(', ') : 'Authenticated session'}
                    {user?.organization ? ` • ${user.organization}` : ''}
                  </p>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                  <Button type="button" size="sm" onClick={continueToDocs}>
                    Continue
                    <ArrowRight className="ml-2 h-3.5 w-3.5" />
                  </Button>
                  <Button
                    type="button"
                    size="sm"
                    variant="outline"
                    onClick={() => {
                      logOut();
                      setErrorMessage('');
                      setPassword('');
                    }}
                  >
                    <LogOut className="mr-2 h-3.5 w-3.5" />
                    Log out
                  </Button>
                </div>
              </div>
            </div>
          ) : null}

          {errorMessage ? (
            <div className="mb-6 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-500/40 dark:bg-rose-500/10 dark:text-rose-200">
              {errorMessage}
            </div>
          ) : null}

          <form
            className="rounded-[28px] border border-border bg-[linear-gradient(180deg,#f8fafc_0%,#ffffff_100%)] p-6 dark:bg-[linear-gradient(180deg,#050505_0%,#000000_100%)]"
            onSubmit={handleBasicSubmit}
          >
            <div className="flex items-center gap-3">
              <span className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-[#111827] text-white dark:bg-white dark:text-black">
                <KeyRound className="h-4 w-4" />
              </span>
              <div>
                <h2 className="text-lg font-semibold text-[#111827] dark:text-white">Log in to connectCenter Developers</h2>
              </div>
            </div>

            {oidcConfig.enabled ? (
              <>
                <a
                  href={oidcLoginHref}
                  className="mt-6 inline-flex h-12 w-full items-center justify-center rounded-2xl border border-[#d7dee7] bg-white px-4 text-sm font-semibold text-[#111827] transition hover:border-[#111827] hover:bg-[#f8fafc] dark:border-[#333333] dark:bg-black dark:text-white dark:hover:border-white dark:hover:bg-[#111111]"
                >
                  {submittingMode === 'oidc' ? (
                    <>
                      <LoaderCircle className="mr-2 h-4 w-4 animate-spin" />
                      Completing login...
                    </>
                  ) : (
                    `Continue with ${oidcConfig.providerLabel}`
                  )}
                </a>

                <div className="my-6 flex items-center gap-3">
                  <div className="h-px flex-1 bg-border" />
                  <span className="text-[11px] font-semibold uppercase tracking-[0.18em] text-[#94a3b8]">or</span>
                  <div className="h-px flex-1 bg-border" />
                </div>
              </>
            ) : null}

            <div className="grid gap-4">
              <div className="grid gap-2">
                <label htmlFor="login-login-id" className="text-xs font-medium text-[#4b5563] dark:text-[#cbd5e1]">
                  Login ID
                </label>
                <input
                  id="login-login-id"
                  name="login_id"
                  value={loginId}
                  onChange={(event) => setLoginId(event.target.value)}
                  autoComplete="username"
                  placeholder="Login ID"
                  className="h-10 rounded-xl border border-border bg-white px-3 text-sm outline-hidden focus:ring-2 focus:ring-[#cbd5e1] dark:bg-black"
                />
              </div>
              <div className="grid gap-2">
                <label htmlFor="login-password" className="text-xs font-medium text-[#4b5563] dark:text-[#cbd5e1]">
                  Password
                </label>
                <input
                  id="login-password"
                  name="password"
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  autoComplete="current-password"
                  placeholder="Password"
                  className="h-10 rounded-xl border border-border bg-white px-3 text-sm outline-hidden focus:ring-2 focus:ring-[#cbd5e1] dark:bg-black"
                />
              </div>
            </div>
            <Button type="submit" className="mt-6 h-11 w-full rounded-2xl" disabled={submittingMode !== null || !isReady}>
              {submittingMode === 'basic' ? 'Logging in...' : 'Log in'}
            </Button>
          </form>

          <div className="mt-6 flex flex-wrap items-center gap-3 text-sm">
            <Link href={returnTo} className="text-[#0f172a] underline decoration-[#94a3b8] underline-offset-4 hover:decoration-[#0f172a] dark:text-[#e2e8f0] dark:hover:decoration-[#e2e8f0]">
              Back to docs
            </Link>
          </div>
        </div>
      </div>
    </main>
  );
}
