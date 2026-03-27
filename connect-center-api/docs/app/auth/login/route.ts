import { randomUUID } from 'crypto';

import { NextRequest, NextResponse } from 'next/server';

import { sanitizeReturnTo } from '@/lib/auth-utils';
import { fetchOidcDiscovery, getOidcServerConfig, resolveOidcCallbackUrl, resolvePublicDocsUrl } from '@/lib/oidc';

const stateCookieName = 'connectcenter-oidc-state';
const returnToCookieName = 'connectcenter-oidc-return-to';

function buildLogInUrl(request: NextRequest, returnTo: string, authError?: string): URL {
  const pathname = request.nextUrl.pathname.replace(/\/auth\/login\/?$/, '/login/');
  const url = new URL(resolvePublicDocsUrl(request, pathname));
  url.searchParams.set('returnTo', returnTo);
  if (authError) {
    url.searchParams.set('authError', authError);
  }
  return url;
}

export async function GET(request: NextRequest) {
  const returnTo = sanitizeReturnTo(request.nextUrl.searchParams.get('returnTo'));

  try {
    const config = getOidcServerConfig();
    const discovery = await fetchOidcDiscovery(config);
    const callbackUrl = resolveOidcCallbackUrl(request);
    const authorizationUrl = new URL(discovery.authorization_endpoint);
    const state = randomUUID();

    authorizationUrl.searchParams.set('response_type', 'code');
    authorizationUrl.searchParams.set('client_id', config.clientId);
    authorizationUrl.searchParams.set('redirect_uri', callbackUrl);
    authorizationUrl.searchParams.set('scope', 'openid profile email');
    authorizationUrl.searchParams.set('state', state);

    if (config.audience) {
      authorizationUrl.searchParams.set('audience', config.audience);
    }

    const response = NextResponse.redirect(authorizationUrl);
    const secure = request.nextUrl.protocol === 'https:';
    response.cookies.set(stateCookieName, state, { httpOnly: true, sameSite: 'lax', secure, path: '/' });
    response.cookies.set(returnToCookieName, returnTo, { httpOnly: true, sameSite: 'lax', secure, path: '/' });
    return response;
  } catch (error) {
    const authError = error instanceof Error ? error.message : 'OIDC login could not be started.';
    return NextResponse.redirect(buildLogInUrl(request, returnTo, authError));
  }
}
