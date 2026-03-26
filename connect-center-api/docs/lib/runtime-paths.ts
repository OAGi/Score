const DEV_BACKEND_ORIGIN = (process.env.NEXT_PUBLIC_BACKEND_ORIGIN ?? 'http://127.0.0.1:5555').replace(/\/+$/, '');
const PROD_API_FALLBACK = 'http://127.0.0.1:5555/api';

declare global {
  interface Window {
    __CONNECTCENTER_RUNTIME__?: {
      backendApiBase?: string;
      oidc?: {
        enabled?: boolean;
        providerLabel?: string;
      };
    };
  }
}

function normalizeApiBase(value: string | undefined): string | null {
  const normalized = (value ?? '').trim().replace(/\/+$/, '');
  return normalized || null;
}

export function getDocsBasePrefix(pathname: string): string {
  const normalized = (pathname || '/').split('?')[0].split('#')[0];
  const match = normalized.match(/^(.*?\/docs)(?:\/.*)?$/);
  return match?.[1] ?? '';
}

export function stripDocsBasePrefix(pathname: string): string {
  const basePrefix = getDocsBasePrefix(pathname);
  const stripped = basePrefix ? pathname.slice(basePrefix.length) || '/' : pathname || '/';
  return stripped.startsWith('/') ? stripped : `/${stripped}`;
}

export function getRuntimeConfiguredApiBase(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }

  return normalizeApiBase(window.__CONNECTCENTER_RUNTIME__?.backendApiBase);
}

export function resolveBrowserApiBase(): string {
  if (typeof window === 'undefined') {
    return PROD_API_FALLBACK;
  }

  const configured = getRuntimeConfiguredApiBase();
  if (configured) {
    return configured;
  }

  if (window.location.port === '3001') {
    return `${DEV_BACKEND_ORIGIN}/api`;
  }

  const docsBasePrefix = getDocsBasePrefix(window.location.pathname);
  if (docsBasePrefix) {
    return `${window.location.origin}${docsBasePrefix.replace(/\/docs$/, '/api')}`;
  }

  return `${window.location.origin}/api`;
}

export function resolveDocsPath(path: string): string {
  const normalized = path.startsWith('/') ? path : `/${path}`;
  if (typeof window === 'undefined') {
    return normalized;
  }

  const docsBasePrefix = getDocsBasePrefix(window.location.pathname);
  return `${docsBasePrefix}${normalized}`;
}

export function getRuntimeOidcConfig(): { enabled: boolean; providerLabel: string } {
  if (typeof window === 'undefined') {
    return { enabled: false, providerLabel: 'Single Sign-On' };
  }

  return {
    enabled: Boolean(window.__CONNECTCENTER_RUNTIME__?.oidc?.enabled),
    providerLabel: window.__CONNECTCENTER_RUNTIME__?.oidc?.providerLabel || 'Single Sign-On',
  };
}
