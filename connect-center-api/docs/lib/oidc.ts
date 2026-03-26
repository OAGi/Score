import 'server-only';

import { existsSync, readFileSync } from 'fs';
import { isIP } from 'net';
import path from 'path';
import { NextRequest } from 'next/server';

type OidcRuntimeConfig = {
  enabled: boolean;
  providerLabel: string;
};

type OidcServerConfig = {
  audience: string | null;
  clientId: string;
  clientSecret: string | null;
  configurationUrl: string;
  issuerUri: string | null;
};

type OidcDiscovery = {
  authorization_endpoint: string;
  token_endpoint: string;
};

function normalizeUrl(value: string | undefined): string | null {
  const normalized = (value ?? '').trim().replace(/\/+$/, '');
  return normalized || null;
}

function _isLoopbackHostname(hostname: string | null): boolean {
  // Keep loopback detection aligned with the backend settings helper.
  if (!hostname) {
    return false;
  }
  const normalizedHostname = hostname.toLowerCase();
  if (normalizedHostname === 'localhost') {
    return true;
  }
  const version = isIP(normalizedHostname);
  if (version === 4) {
    return normalizedHostname.startsWith('127.');
  }
  if (version === 6) {
    return normalizedHostname === '::1';
  }
  return false;
}

function shouldUseRequestOrigin(request: NextRequest, publicDocsBaseUrl: string | null): boolean {
  if (!publicDocsBaseUrl) {
    return false;
  }

  try {
    const configured = new URL(publicDocsBaseUrl);
    const current = new URL(request.url);
    return _isLoopbackHostname(configured.hostname) && _isLoopbackHostname(current.hostname);
  } catch {
    return false;
  }
}

let fallbackEnvCache: Record<string, string> | null = null;

function parseDotEnv(content: string): Record<string, string> {
  const parsed: Record<string, string> = {};
  for (const rawLine of content.split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line || line.startsWith('#')) {
      continue;
    }

    const separatorIndex = line.indexOf('=');
    if (separatorIndex < 0) {
      continue;
    }

    const key = line.slice(0, separatorIndex).trim();
    let value = line.slice(separatorIndex + 1).trim();
    if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
      value = value.slice(1, -1);
    }
    if (key) {
      parsed[key] = value;
    }
  }
  return parsed;
}

function getFallbackEnv(): Record<string, string> {
  if (fallbackEnvCache) {
    return fallbackEnvCache;
  }

  const candidatePaths = [
    path.resolve(process.cwd(), '../.env'),
    path.resolve(process.cwd(), '.env'),
  ];

  for (const candidatePath of candidatePaths) {
    if (!existsSync(candidatePath)) {
      continue;
    }

    try {
      fallbackEnvCache = parseDotEnv(readFileSync(candidatePath, 'utf8'));
      return fallbackEnvCache;
    } catch {
      // ignore and try the next path
    }
  }

  fallbackEnvCache = {};
  return fallbackEnvCache;
}

function getEnvValue(name: string): string {
  return (process.env[name] ?? getFallbackEnv()[name] ?? '').trim();
}

function inferProviderLabel(providerName: string | null, issuerUri: string | null): string {
  const normalizedProvider = (providerName ?? '').trim();
  if (normalizedProvider) {
    const normalizedKey = normalizedProvider.toLowerCase();
    if (normalizedKey === 'auth0') {
      return 'Auth0';
    }
    if (normalizedKey === 'okta') {
      return 'Okta';
    }
    if (normalizedKey === 'google') {
      return 'Google';
    }
    if (normalizedKey === 'microsoft') {
      return 'Microsoft';
    }
    return normalizedProvider;
  }

  if (issuerUri) {
    try {
      const host = new URL(issuerUri).hostname.toLowerCase();
      if (host.endsWith('auth0.com')) {
        return 'Auth0';
      }
      if (host.includes('okta')) {
        return 'Okta';
      }
      if (host.includes('microsoft') || host.includes('login.live.com') || host.includes('login.microsoftonline.com')) {
        return 'Microsoft';
      }
      if (host.includes('google')) {
        return 'Google';
      }
    } catch {
      // ignore
    }
  }

  return 'Single Sign-On';
}

export function getOidcRuntimeConfig(): OidcRuntimeConfig {
  const configurationUrl = normalizeUrl(getEnvValue('OAUTH2_CONFIGURATION_URL'));
  const issuerUri = normalizeUrl(getEnvValue('OAUTH2_ISSUER_URI'));
  const clientId = getEnvValue('OAUTH2_CLIENT_ID');
  const providerLabel = inferProviderLabel(getEnvValue('OAUTH2_PROVIDER_NAME') || null, issuerUri);

  return {
    enabled: Boolean(clientId && (configurationUrl || issuerUri)),
    providerLabel,
  };
}

export function getOidcServerConfig(): OidcServerConfig {
  const configurationUrl = normalizeUrl(getEnvValue('OAUTH2_CONFIGURATION_URL'));
  const issuerUri = normalizeUrl(getEnvValue('OAUTH2_ISSUER_URI'));
  const clientId = getEnvValue('OAUTH2_CLIENT_ID');
  const clientSecret = getEnvValue('OAUTH2_CLIENT_SECRET') || null;
  const audience = getEnvValue('OAUTH2_AUDIENCE') || null;

  const resolvedConfigurationUrl = configurationUrl || (issuerUri ? `${issuerUri}/.well-known/openid-configuration` : null);
  if (!clientId || !resolvedConfigurationUrl) {
    throw new Error('OIDC is not configured for the docs app.');
  }

  return {
    audience,
    clientId,
    clientSecret,
    configurationUrl: resolvedConfigurationUrl,
    issuerUri,
  };
}

export async function fetchOidcDiscovery(config: OidcServerConfig): Promise<OidcDiscovery> {
  const response = await fetch(config.configurationUrl, { cache: 'no-store' });
  if (!response.ok) {
    throw new Error(`Failed to fetch OIDC discovery metadata (${response.status}).`);
  }

  const payload = (await response.json()) as Record<string, unknown>;
  const authorizationEndpoint = payload.authorization_endpoint;
  const tokenEndpoint = payload.token_endpoint;

  if (typeof authorizationEndpoint !== 'string' || !authorizationEndpoint) {
    throw new Error('OIDC discovery metadata is missing authorization_endpoint.');
  }
  if (typeof tokenEndpoint !== 'string' || !tokenEndpoint) {
    throw new Error('OIDC discovery metadata is missing token_endpoint.');
  }

  return {
    authorization_endpoint: authorizationEndpoint,
    token_endpoint: tokenEndpoint,
  };
}

export function resolvePublicDocsUrl(request: NextRequest, pathname: string): string {
  const publicDocsBaseUrl = normalizeUrl(getEnvValue('PUBLIC_DOCS_BASE_URL'));
  if (shouldUseRequestOrigin(request, publicDocsBaseUrl)) {
    return new URL(pathname, request.url).toString();
  }

  if (publicDocsBaseUrl) {
    const relativePath = pathname.replace(/^\/+/, '');
    return new URL(relativePath, `${publicDocsBaseUrl}/`).toString();
  }

  return new URL(pathname, request.url).toString();
}

export function resolveOidcCallbackUrl(request: NextRequest): string {
  const publicDocsBaseUrl = normalizeUrl(getEnvValue('PUBLIC_DOCS_BASE_URL'));
  const callbackPath = request.nextUrl.pathname
    .replace(/\/auth\/login\/?$/, '/auth/callback')
    .replace(/\/auth\/callback\/?$/, '/auth/callback');

  if (shouldUseRequestOrigin(request, publicDocsBaseUrl)) {
    return new URL(callbackPath, request.url).toString();
  }

  if (publicDocsBaseUrl) {
    return `${publicDocsBaseUrl}/auth/callback`;
  }

  return resolvePublicDocsUrl(request, callbackPath);
}
