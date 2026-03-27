import { NextRequest, NextResponse } from 'next/server';

import { sanitizeReturnTo } from '@/lib/auth-utils';
import { pendingOidcAuthStorageKey } from '@/lib/auth-storage';
import { fetchOidcDiscovery, getOidcServerConfig, resolveOidcCallbackUrl, resolvePublicDocsUrl } from '@/lib/oidc';

const stateCookieName = 'connectcenter-oidc-state';
const returnToCookieName = 'connectcenter-oidc-return-to';

function buildLogInUrl(request: NextRequest, returnTo: string, authError?: string): URL {
  const pathname = request.nextUrl.pathname.replace(/\/auth\/callback\/?$/, '/login/');
  const url = new URL(resolvePublicDocsUrl(request, pathname));
  url.searchParams.set('returnTo', returnTo);
  if (authError) {
    url.searchParams.set('authError', authError);
  }
  return url;
}

function buildSuccessHtml(accessToken: string, redirectUrl: string, returnTo: string): string {
  const pendingPayload = JSON.stringify({ accessToken, returnTo });
  const storageKeyLiteral = JSON.stringify(pendingOidcAuthStorageKey);
  const payloadLiteral = JSON.stringify(pendingPayload);
  const redirectLiteral = JSON.stringify(redirectUrl);

  return `<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Completing login...</title>
  </head>
  <body>
    <script>
      try {
        window.sessionStorage.setItem(${storageKeyLiteral}, ${payloadLiteral});
      } catch (error) {
        console.error('Failed to store OIDC token in sessionStorage.', error);
      }
      window.location.replace(${redirectLiteral});
    </script>
  </body>
</html>`;
}

export async function GET(request: NextRequest) {
  const code = (request.nextUrl.searchParams.get('code') ?? '').trim();
  const state = (request.nextUrl.searchParams.get('state') ?? '').trim();
  const expectedState = request.cookies.get(stateCookieName)?.value ?? '';
  const returnTo = sanitizeReturnTo(request.cookies.get(returnToCookieName)?.value ?? request.nextUrl.searchParams.get('returnTo'));

  const clearCookies = (response: NextResponse) => {
    response.cookies.delete(stateCookieName);
    response.cookies.delete(returnToCookieName);
    return response;
  };

  if (!code || !state || !expectedState || state !== expectedState) {
    return clearCookies(NextResponse.redirect(buildLogInUrl(request, returnTo, 'OIDC login could not be completed.')));
  }

  try {
    const config = getOidcServerConfig();
    const discovery = await fetchOidcDiscovery(config);
    const callbackUrl = resolveOidcCallbackUrl(request);
    const body = new URLSearchParams({
      grant_type: 'authorization_code',
      code,
      client_id: config.clientId,
      redirect_uri: callbackUrl,
    });

    if (config.clientSecret) {
      body.set('client_secret', config.clientSecret);
    }

    const tokenResponse = await fetch(discovery.token_endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: body.toString(),
      cache: 'no-store',
    });

    const payload = (await tokenResponse.json()) as Record<string, unknown>;
    const accessToken = typeof payload.access_token === 'string' ? payload.access_token : '';

    if (!tokenResponse.ok || !accessToken) {
      throw new Error('The OIDC provider did not return a usable access token.');
    }

    const logInUrl = buildLogInUrl(request, returnTo);
    logInUrl.searchParams.set('oidc', '1');
    const response = new NextResponse(buildSuccessHtml(accessToken, logInUrl.toString(), returnTo), {
      headers: {
        'Content-Type': 'text/html; charset=utf-8',
        'Cache-Control': 'no-store',
      },
    });
    return clearCookies(response);
  } catch (error) {
    const authError = error instanceof Error ? error.message : 'OIDC login could not be completed.';
    return clearCookies(NextResponse.redirect(buildLogInUrl(request, returnTo, authError)));
  }
}
