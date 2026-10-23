export class PasskeyInfo {
  enabled: boolean;
}

export class PasskeySummary {
  id: string;
  /** The name the user gave it; null or empty when it has none. */
  label: string | null;
  creationTimestamp: string;
  lastUpdateTimestamp: string;
  backupEligible: boolean;
  backedUp: boolean;
  transports: string[];
  rpId: string;
  /** False when the passkey was registered for another relying party ID, so the browser will not offer it. */
  usable: boolean;
  userHandle: string;
  /** When the passkey last signed someone in, and the User-Agent of that request; absent until it was used. */
  lastUsedTimestamp?: string;
  lastUsedUserAgent?: string;
  /** The passkey manager or authenticator holding the passkey (e.g. "Bitwarden"), when it could be told. */
  provider?: string;
  /** The provider's (light) icon as a base64 SVG data URI, when it has one. */
  providerIcon?: string;
  /** The dark variant of the same icon; unused until the app has a dark theme. */
  providerIconDark?: string;
}

/** Thrown when the user dismissed the browser's passkey prompt or the request was superseded. Not an error. */
export class PasskeyCancelledError extends Error {
  constructor() {
    super('The passkey request was cancelled.');
    this.name = 'PasskeyCancelledError';
  }
}
