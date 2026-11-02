import {
  base64urlDecode,
  base64urlEncode,
  decodeCreationOptions,
  decodeRequestOptions,
  encodeAssertion,
  encodeAttestation
} from './webauthn-codec';

function bytes(...values: number[]): ArrayBuffer {
  return new Uint8Array(values).buffer;
}

function toArray(buffer: ArrayBuffer): number[] {
  return Array.from(new Uint8Array(buffer));
}

describe('webauthn-codec', () => {

  describe('base64url', () => {
    it('encodes without padding and with the URL-safe alphabet', () => {
      // 0xfb 0xff 0xbf encodes to "+/+/" in plain base64
      expect(base64urlEncode(bytes(0xfb, 0xff, 0xbf))).toBe('-_-_');
      expect(base64urlEncode(bytes(1))).toBe('AQ');
      expect(base64urlEncode(bytes(1, 2))).toBe('AQI');
      expect(base64urlEncode(bytes())).toBe('');
    });

    it('round-trips every byte value', () => {
      const all = Array.from({length: 256}, (_, i) => i);
      expect(toArray(base64urlDecode(base64urlEncode(bytes(...all))))).toEqual(all);
    });

    it('decodes unpadded input', () => {
      expect(toArray(base64urlDecode('AQI'))).toEqual([1, 2]);
      expect(toArray(base64urlDecode('-_-_'))).toEqual([0xfb, 0xff, 0xbf]);
    });

    it('encodes a typed-array view by its own bytes, not the whole backing buffer', () => {
      const backing = new Uint8Array([9, 9, 1, 2, 9]);
      expect(base64urlEncode(backing.subarray(2, 4))).toBe('AQI');
    });
  });

  describe('server options to browser options', () => {
    it('decodes the sign-in challenge and allowed credentials', () => {
      const decoded = decodeRequestOptions({
        challenge: 'AQI', rpId: 'localhost', userVerification: 'required',
        allowCredentials: [{type: 'public-key', id: 'AwQ'}]
      });
      expect(toArray(decoded.challenge as ArrayBuffer)).toEqual([1, 2]);
      expect(decoded.rpId).toBe('localhost');
      expect(decoded.userVerification).toBe('required');
      expect(toArray(decoded.allowCredentials[0].id as ArrayBuffer)).toEqual([3, 4]);
      expect(decoded.allowCredentials[0].type).toBe('public-key');
    });

    it('treats missing allowed credentials as an empty list (discoverable sign-in)', () => {
      expect(decodeRequestOptions({challenge: 'AQI'}).allowCredentials).toEqual([]);
    });

    it('decodes the registration challenge, user handle and excluded credentials', () => {
      const decoded = decodeCreationOptions({
        challenge: 'AQI', rp: {id: 'localhost', name: 'connectCenter'},
        user: {id: 'BQY', name: 'alice', displayName: 'Alice'},
        excludeCredentials: [{type: 'public-key', id: 'Bw'}]
      });
      expect(toArray(decoded.challenge as ArrayBuffer)).toEqual([1, 2]);
      expect(toArray(decoded.user.id as ArrayBuffer)).toEqual([5, 6]);
      expect(decoded.user.name).toBe('alice');
      expect(toArray(decoded.excludeCredentials[0].id as ArrayBuffer)).toEqual([7]);
    });

    it('treats missing excluded credentials as an empty list', () => {
      expect(decodeCreationOptions({challenge: 'AQI', user: {id: 'BQY'}}).excludeCredentials).toEqual([]);
    });

    it('asks the browser to offer a phone, then a security key, before this device', () => {
      const decoded: any = decodeCreationOptions({challenge: 'AQI', user: {id: 'BQY'}});
      expect(decoded.hints).toEqual(['hybrid', 'security-key', 'client-device']);
    });
  });

  describe('browser credential to server body', () => {
    it('encodes an assertion with the type under credType, as the server reads it', () => {
      const credential = {
        id: 'abc', type: 'public-key', rawId: bytes(1, 2), authenticatorAttachment: 'platform',
        getClientExtensionResults: () => ({}),
        response: {
          authenticatorData: bytes(3), clientDataJSON: bytes(4), signature: bytes(5), userHandle: bytes(6)
        }
      } as unknown as PublicKeyCredential;

      expect(encodeAssertion(credential)).toEqual({
        id: 'abc',
        rawId: 'AQI',
        response: {authenticatorData: 'Aw', clientDataJSON: 'BA', signature: 'BQ', userHandle: 'Bg'},
        credType: 'public-key',
        clientExtensionResults: {},
        authenticatorAttachment: 'platform'
      });
    });

    it('omits the user handle when the authenticator returns none', () => {
      const credential = {
        id: 'abc', type: 'public-key', rawId: bytes(1), getClientExtensionResults: () => ({}),
        response: {authenticatorData: bytes(3), clientDataJSON: bytes(4), signature: bytes(5), userHandle: null}
      } as unknown as PublicKeyCredential;
      expect(encodeAssertion(credential).response.userHandle).toBeUndefined();
    });

    it('encodes an attestation under publicKey with the label', () => {
      const credential = {
        id: 'abc', type: 'public-key', rawId: bytes(1, 2), authenticatorAttachment: 'cross-platform',
        getClientExtensionResults: () => ({credProps: {rk: true}}),
        response: {attestationObject: bytes(7), clientDataJSON: bytes(8), getTransports: () => ['usb']}
      } as unknown as PublicKeyCredential;

      expect(encodeAttestation(credential, 'YubiKey')).toEqual({
        publicKey: {
          credential: {
            id: 'abc',
            rawId: 'AQI',
            response: {attestationObject: 'Bw', clientDataJSON: 'CA', transports: ['usb']},
            type: 'public-key',
            clientExtensionResults: {credProps: {rk: true}},
            authenticatorAttachment: 'cross-platform'
          },
          label: 'YubiKey'
        }
      });
    });

    it('tolerates browsers without getTransports', () => {
      const credential = {
        id: 'abc', type: 'public-key', rawId: bytes(1), getClientExtensionResults: () => ({}),
        response: {attestationObject: bytes(7), clientDataJSON: bytes(8)}
      } as unknown as PublicKeyCredential;
      expect(encodeAttestation(credential, 'x').publicKey.credential.response.transports).toEqual([]);
    });
  });

});
