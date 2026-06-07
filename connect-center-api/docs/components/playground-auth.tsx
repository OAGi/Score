'use client';

import { createContext, ReactNode, useCallback, useContext, useEffect, useMemo, useState } from 'react';

import { playgroundAuthStorageKey } from '@/lib/auth-storage';
import { resolveBackendApiBase } from '@/lib/openapi';

type AuthMethod = 'basic' | 'bearer';

type PlaygroundAuthUser = {
  appUserId: number;
  loginId: string;
  username: string;
  roles: string[];
  organization: string | null;
  email: string | null;
};

type PlaygroundAuthState = {
  isReady: boolean;
  authMethod: AuthMethod | null;
  loginId: string;
  user: PlaygroundAuthUser | null;
  authorizationHeader: string | null;
  isAuthenticated: boolean;
  logInWithAccessToken: (accessToken: string) => Promise<void>;
  logInWithBasic: (loginId: string, password: string) => Promise<void>;
  logOut: () => void;
};

const PlaygroundAuthContext = createContext<PlaygroundAuthState | null>(null);

type StoredAuth = {
  authMethod?: AuthMethod;
  loginId?: string;
  accessToken?: string;
  basicAuthorization?: string;
  user?: PlaygroundAuthUser;
};

function isPlaygroundAuthUser(value: unknown): value is PlaygroundAuthUser {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const candidate = value as Record<string, unknown>;
  return (
    typeof candidate.appUserId === 'number' &&
    typeof candidate.loginId === 'string' &&
    typeof candidate.username === 'string' &&
    Array.isArray(candidate.roles) &&
    candidate.roles.every((role) => typeof role === 'string') &&
    (typeof candidate.organization === 'string' || candidate.organization === null || candidate.organization === undefined) &&
    (typeof candidate.email === 'string' || candidate.email === null || candidate.email === undefined)
  );
}

function readStoredAuth(): StoredAuth {
  if (typeof window === 'undefined') {
    return {};
  }
  try {
    const raw = window.sessionStorage.getItem(playgroundAuthStorageKey);
    if (!raw) return {};
    const parsed = JSON.parse(raw) as Record<string, unknown>;
    if (!parsed || typeof parsed !== 'object') return {};
    const storedAuthMethod = parsed.authMethod;
    const authMethod =
      storedAuthMethod === 'basic' || storedAuthMethod === 'bearer'
        ? storedAuthMethod
        : typeof parsed.accessToken === 'string' && parsed.accessToken.trim()
          ? 'bearer'
          : undefined;
    return {
      authMethod,
      loginId: typeof parsed.loginId === 'string' ? parsed.loginId : undefined,
      accessToken: typeof parsed.accessToken === 'string' ? parsed.accessToken : undefined,
      basicAuthorization: typeof parsed.basicAuthorization === 'string' ? parsed.basicAuthorization : undefined,
      user: isPlaygroundAuthUser(parsed.user) ? parsed.user : undefined,
    };
  } catch {
    return {};
  }
}

function writeStoredAuth(value: StoredAuth): void {
  if (typeof window === 'undefined') {
    return;
  }
  try {
    const hasAny = Boolean(
      value.authMethod &&
      ((value.authMethod === 'bearer' && value.accessToken) || (value.authMethod === 'basic' && value.basicAuthorization)),
    );
    if (!hasAny) {
      window.sessionStorage.removeItem(playgroundAuthStorageKey);
      return;
    }
    window.sessionStorage.setItem(playgroundAuthStorageKey, JSON.stringify(value));
  } catch {
    // ignore
  }
}

function toBasicAuthBase64(value: string): string {
  const bytes = new TextEncoder().encode(value);
  let binary = '';
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return btoa(binary);
}

function buildBasicAuthorizationHeader(loginId: string, password: string): string {
  return `Basic ${toBasicAuthBase64(`${loginId}:${password}`)}`;
}

function getAuthorizationHeader(
  authMethod: AuthMethod | null,
  accessToken: string,
  basicAuthorization: string,
): string | null {
  if (authMethod === 'bearer') {
    const token = accessToken.trim();
    return token ? `Bearer ${token}` : null;
  }
  if (authMethod === 'basic') {
    const normalized = basicAuthorization.trim();
    return normalized || null;
  }
  return null;
}

function extractAuthErrorMessage(payload: unknown, status: number): string {
  if (!payload || typeof payload !== 'object') {
    return `Log in failed (${status}).`;
  }

  const record = payload as Record<string, unknown>;
  const detail = record.detail;
  if (detail && typeof detail === 'object') {
    const detailRecord = detail as Record<string, unknown>;
    const message = typeof detailRecord.message === 'string' ? detailRecord.message : null;
    const cause = typeof detailRecord.cause === 'string' ? detailRecord.cause : null;
    if (message && cause) {
      return `${message} ${cause}`;
    }
    if (message) {
      return message;
    }
  }

  if (typeof record.message === 'string') {
    return record.message;
  }

  return `Log in failed (${status}).`;
}

function parseAuthUser(payload: unknown): PlaygroundAuthUser {
  if (!payload || typeof payload !== 'object') {
    throw new Error('Authentication succeeded but the user profile response was invalid.');
  }

  const record = payload as Record<string, unknown>;
  const appUserId = record.app_user_id;
  const loginId = record.login_id;
  const username = record.username;
  const roles = record.roles;
  const organization = record.organization;
  const email = record.email;

  if (
    typeof appUserId !== 'number' ||
    typeof loginId !== 'string' ||
    typeof username !== 'string' ||
    !Array.isArray(roles) ||
    !roles.every((role) => typeof role === 'string')
  ) {
    throw new Error('Authentication succeeded but the user profile response was invalid.');
  }

  return {
    appUserId,
    loginId,
    username,
    roles,
    organization: typeof organization === 'string' ? organization : null,
    email: typeof email === 'string' ? email : null,
  };
}

async function fetchCurrentUserProfile(authorizationHeader: string): Promise<PlaygroundAuthUser> {
  const response = await fetch(`${resolveBackendApiBase()}/me`, {
    method: 'GET',
    headers: {
      Accept: 'application/json',
      Authorization: authorizationHeader,
    },
    cache: 'no-store',
  });

  const raw = await response.text();
  const parsed = raw ? (() => {
    try {
      return JSON.parse(raw) as unknown;
    } catch {
      return raw;
    }
  })() : null;

  if (!response.ok) {
    throw new Error(extractAuthErrorMessage(parsed, response.status));
  }

  return parseAuthUser(parsed);
}

export function PlaygroundAuthProvider({ children }: { children: ReactNode }) {
  const [isReady, setIsReady] = useState(false);
  const [authMethod, setAuthMethod] = useState<AuthMethod | null>(null);
  const [loginId, setLoginId] = useState('');
  const [accessToken, setAccessToken] = useState('');
  const [basicAuthorization, setBasicAuthorization] = useState('');
  const [user, setUser] = useState<PlaygroundAuthUser | null>(null);

  useEffect(() => {
    // Hydrate auth state from storage on mount. Storage is browser-only and
    // cannot be read during SSR, so this one-time synchronous setState is
    // intentional.
    const stored = readStoredAuth();
    // eslint-disable-next-line react-hooks/set-state-in-effect -- one-time client hydration from session/local storage
    setAuthMethod(stored.authMethod ?? null);
    setLoginId(stored.loginId ?? '');
    setAccessToken(stored.accessToken ?? '');
    setBasicAuthorization(stored.basicAuthorization ?? '');
    setUser(stored.user ?? null);
    setIsReady(true);

    try {
      const raw = window.sessionStorage.getItem(playgroundAuthStorageKey);
      if (!raw) return;
      const parsed = JSON.parse(raw) as Record<string, unknown>;
      if (parsed && typeof parsed === 'object' && 'password' in parsed) {
        delete parsed.password;
        window.sessionStorage.setItem(playgroundAuthStorageKey, JSON.stringify(parsed));
      }
    } catch {
      // ignore
    }
  }, []);

  useEffect(() => {
    if (!isReady) {
      return;
    }

    writeStoredAuth({
      authMethod: authMethod ?? undefined,
      loginId,
      accessToken,
      basicAuthorization,
      user: user ?? undefined,
    });
  }, [accessToken, authMethod, basicAuthorization, isReady, loginId, user]);

  const logInWithAccessToken = useCallback(async (nextAccessToken: string) => {
    const normalizedAccessToken = nextAccessToken.trim();
    if (!normalizedAccessToken) {
      throw new Error('Access token is required.');
    }

    const authorizationHeader = `Bearer ${normalizedAccessToken}`;
    const profile = await fetchCurrentUserProfile(authorizationHeader);
    setAuthMethod('bearer');
    setLoginId(profile.loginId);
    setAccessToken(normalizedAccessToken);
    setBasicAuthorization('');
    setUser(profile);
  }, []);

  const logInWithBasic = useCallback(async (nextLoginId: string, password: string) => {
    const normalizedLoginId = nextLoginId.trim();
    if (!normalizedLoginId || !password) {
      throw new Error('Login ID and password are required.');
    }

    const authorizationHeader = buildBasicAuthorizationHeader(normalizedLoginId, password);
    const profile = await fetchCurrentUserProfile(authorizationHeader);
    setAuthMethod('basic');
    setLoginId(profile.loginId || normalizedLoginId);
    setAccessToken('');
    setBasicAuthorization(authorizationHeader);
    setUser(profile);
  }, []);

  const logOut = useCallback(() => {
    setAuthMethod(null);
    setLoginId('');
    setAccessToken('');
    setBasicAuthorization('');
    setUser(null);
  }, []);

  const authorizationHeader = getAuthorizationHeader(authMethod, accessToken, basicAuthorization);
  const isAuthenticated = Boolean(authorizationHeader);

  const value = useMemo(
    () => ({
      isReady,
      authMethod,
      loginId,
      user,
      authorizationHeader,
      isAuthenticated,
      logInWithAccessToken,
      logInWithBasic,
      logOut,
    }),
    [authMethod, authorizationHeader, isAuthenticated, isReady, logInWithAccessToken, logInWithBasic, logOut, loginId, user],
  );

  return <PlaygroundAuthContext.Provider value={value}>{children}</PlaygroundAuthContext.Provider>;
}

export function usePlaygroundAuth(): PlaygroundAuthState {
  const ctx = useContext(PlaygroundAuthContext);
  if (!ctx) {
    throw new Error('usePlaygroundAuth must be used within PlaygroundAuthProvider');
  }
  return ctx;
}
