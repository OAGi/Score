/**
 * Conversions between the JSON that Spring Security's WebAuthn endpoints speak (binary values as base64url
 * strings) and the browser's WebAuthn API (binary values as ArrayBuffers). Kept free of Angular so it is easy to
 * unit test.
 */

export function base64urlEncode(buffer: ArrayBuffer | ArrayBufferView): string {
  const bytes = ArrayBuffer.isView(buffer)
    ? new Uint8Array(buffer.buffer, buffer.byteOffset, buffer.byteLength)
    : new Uint8Array(buffer);
  let binary = '';
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
}

export function base64urlDecode(value: string): ArrayBuffer {
  const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes.buffer;
}

/** Turns the server's request options (sign-in) into the shape navigator.credentials.get() expects. */
export function decodeRequestOptions(options: any): PublicKeyCredentialRequestOptions {
  return {
    ...options,
    challenge: base64urlDecode(options.challenge),
    allowCredentials: (options.allowCredentials || []).map((credential: any) => ({
      ...credential,
      id: base64urlDecode(credential.id)
    }))
  };
}

/**
 * Where the browser should offer to save a new passkey, most preferred first. Without this, Chromium-based browsers
 * open on "this device", which on a Mac without Touch ID means a passkey that is unlocked with the computer's
 * password. A phone or a security key avoids that; "this device" stays one choice away. Browsers may ignore it.
 */
export const CREATION_HINTS = ['hybrid', 'security-key', 'client-device'];

/** Turns the server's creation options (registration) into the shape navigator.credentials.create() expects. */
export function decodeCreationOptions(options: any): PublicKeyCredentialCreationOptions {
  return {
    ...options,
    hints: options.hints ?? CREATION_HINTS,
    user: {...options.user, id: base64urlDecode(options.user.id)},
    challenge: base64urlDecode(options.challenge),
    excludeCredentials: (options.excludeCredentials || []).map((credential: any) => ({
      ...credential,
      id: base64urlDecode(credential.id)
    }))
  };
}

/** The body Spring Security's POST /login/webauthn expects. Note the server reads the type as {@code credType}. */
export function encodeAssertion(credential: PublicKeyCredential): any {
  const response = credential.response as AuthenticatorAssertionResponse;
  return {
    id: credential.id,
    rawId: base64urlEncode(credential.rawId),
    response: {
      authenticatorData: base64urlEncode(response.authenticatorData),
      clientDataJSON: base64urlEncode(response.clientDataJSON),
      signature: base64urlEncode(response.signature),
      userHandle: response.userHandle ? base64urlEncode(response.userHandle) : undefined
    },
    credType: credential.type,
    clientExtensionResults: credential.getClientExtensionResults(),
    authenticatorAttachment: credential.authenticatorAttachment
  };
}

/** The body Spring Security's POST /webauthn/register expects. */
export function encodeAttestation(credential: PublicKeyCredential, label: string): any {
  const response = credential.response as AuthenticatorAttestationResponse;
  return {
    publicKey: {
      credential: {
        id: credential.id,
        rawId: base64urlEncode(credential.rawId),
        response: {
          attestationObject: base64urlEncode(response.attestationObject),
          clientDataJSON: base64urlEncode(response.clientDataJSON),
          transports: response.getTransports ? response.getTransports() : []
        },
        type: credential.type,
        clientExtensionResults: credential.getClientExtensionResults(),
        authenticatorAttachment: credential.authenticatorAttachment
      },
      label
    }
  };
}
