/**
 * Describes a browser and system from a user agent string ("Chrome 154.0.0.0 on macOS"), for showing where a passkey
 * was last used. New passkeys get no name of their own.
 */

export interface BrowserDescription {
  browser?: string;
  version?: string;
  os?: string;
}

/** What the user agent string alone says. Chromium browsers report only the major version here. */
export function describeFromUserAgent(userAgent: string): BrowserDescription {
  const ua = userAgent || '';
  const versionOf = (pattern: RegExp) => (ua.match(pattern) || [])[1];

  let browser: string | undefined;
  let version: string | undefined;
  if (/Edg\//.test(ua)) {
    browser = 'Edge';
    version = versionOf(/Edg\/([\d.]+)/);
  } else if (/OPR\//.test(ua)) {
    browser = 'Opera';
    version = versionOf(/OPR\/([\d.]+)/);
  } else if (/Vivaldi\//.test(ua)) {
    browser = 'Vivaldi';
    version = versionOf(/Vivaldi\/([\d.]+)/);
  } else if (/Firefox\//.test(ua)) {
    browser = 'Firefox';
    version = versionOf(/Firefox\/([\d.]+)/);
  } else if (/(Chrome|CriOS)\//.test(ua)) {
    browser = 'Chrome';
    version = versionOf(/(?:Chrome|CriOS)\/([\d.]+)/);
  } else if (/Safari\//.test(ua)) {
    browser = 'Safari';
    version = versionOf(/Version\/([\d.]+)/);
  }

  const os = /Windows/.test(ua) ? 'Windows'
    : /(iPhone|iPad)/.test(ua) ? 'iOS'
      : /Android/.test(ua) ? 'Android'
        : /Mac OS X/.test(ua) ? 'macOS'
          : /Linux/.test(ua) ? 'Linux'
            : undefined;

  return {browser, version, os};
}

/** Puts the parts together, leaving out what is not known. */
export function buildPasskeyLabel(description: BrowserDescription, suffix = ''): string {
  const browser = [description.browser, description.version].filter(part => !!part).join(' ');
  const where = browser && description.os ? browser + ' on ' + description.os : (browser || description.os || 'Passkey');
  return suffix ? where + ' · ' + suffix : where;
}

/** "Chrome 154 on macOS" from a user agent alone, for showing where a passkey was last used. Empty if unknown. */
export function describeUserAgent(userAgent: string | undefined): string {
  const description = describeFromUserAgent(userAgent || '');
  return description.browser || description.os ? buildPasskeyLabel(description, '') : '';
}
