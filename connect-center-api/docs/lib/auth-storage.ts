export const playgroundAuthStorageKey = 'connectcenter-playground-auth';
export const pendingOidcAuthStorageKey = 'connectcenter-playground-pending-oidc';

export function clearOidcSessionState(): void {
  if (typeof window === 'undefined') {
    return;
  }

  try {
    window.sessionStorage.removeItem(pendingOidcAuthStorageKey);
    window.sessionStorage.removeItem(playgroundAuthStorageKey);
  } catch {
    // ignore
  }
}
