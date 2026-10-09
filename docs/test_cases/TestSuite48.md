# Test Suite 48

**Passkey Sign-in**

This suite covers signing in to connectCenter with a passkey (WebAuthn) and the server-side rules around it. Passkey sign-in is configured with `score.passkey.rp-id` and `score.passkey.allowed-origins` (environment variables `SCORE_PASSKEY_RP_ID` and `SCORE_PASSKEY_ALLOWED_ORIGINS`) and is on exactly when both are set. The browser cases use a virtual authenticator that behaves like a platform authenticator with a screen lock (a discoverable credential, user verification succeeding). Test Case 48.2 drives the REST API with the cookies of a signed-in browser. Cases that need passkeys are skipped on an instance without them; the "nothing is offered" case needs an instance without them.

## Test Case 48.1

**Passkey sign-in, registration and the passkey offer**

Pre-condition: An instance with passkey sign-in configured for the address the test opens, and a local end user account with a password. A virtual authenticator is available to the browser. For the account-deletion assertion an Admin account is also available.

### Test Assertion:

#### Test Assertion #48.1.1
A signed-in user can add a passkey in the account settings, sign out, and sign in with it. The signed-in account is the one that registered the passkey, and the session reports the `passkey` sign-in method.

#### Test Assertion #48.1.2
A session started with a passkey can use the application as a password session can: the Password page and the Passkeys page work.

#### Test Assertion #48.1.3
A passkey removed on the server can no longer sign in. The sign-in page shows a message about the passkey and the user stays on the sign-in page.

#### Test Assertion #48.1.4
On an instance without passkey configuration the sign-in page shows no passkey button and no "or" separator, and the username field's autocomplete is `username`.

#### Test Assertion #48.1.5
Signing in with a password is unaffected by passkeys and reports the `basic` sign-in method.

#### Test Assertion #48.1.6
After a password sign-in the user is offered a passkey. Creating it from the offer works, and the user can then sign in with it.

#### Test Assertion #48.1.7
Choosing "Not now" on the offer keeps the offer away on the next password sign-in.

#### Test Assertion #48.1.8
An account that already has a passkey is not offered another one after a password sign-in.

#### Test Assertion #48.1.9
Signing in with a passkey replaces the session ID the sign-in page was using before sign-in; the session cookie after sign-in differs from the one before.

#### Test Assertion #48.1.10
An Admin can delete an account that has a passkey. The account, its passkeys and its WebAuthn identity are all gone afterwards.

### Test Step Pre-condition:
1. Passkey sign-in is configured on the instance (except for step 4).
2. A virtual authenticator is attached to the browser, and the test account can sign in with its password.
3. For step 10 an Admin account exists.

### Test Step:
1. Sign in with the password, add a passkey in the account settings, sign out and click the passkey button on the sign-in page. Verify the signed-in account and the `passkey` method. (Assertion [#1](#test-assertion-4811))
2. Sign in with a passkey, then open the Password and Passkeys pages. Verify both work. (Assertion [#2](#test-assertion-4812))
3. Add a passkey, remove it on the server, sign out and click the passkey button. Verify the message and that the user is not signed in. (Assertion [#3](#test-assertion-4813))
4. Open the sign-in page of an instance without passkey configuration. Verify there is no passkey button or separator. (Assertion [#4](#test-assertion-4814))
5. Sign in with the password. Verify the account and the `basic` method. (Assertion [#5](#test-assertion-4815))
6. Sign in with the password, accept the offer, then sign out and sign in with the passkey. (Assertion [#6](#test-assertion-4816))
7. Sign in with the password, choose "Not now", sign out and sign in with the password again. Verify no offer appears. (Assertion [#7](#test-assertion-4817))
8. Create a passkey from the offer, sign out and sign in with the password again. Verify no offer appears. (Assertion [#8](#test-assertion-4818))
9. Sign out so that the sign-in page has started a session, note the session cookie, sign in with the passkey and note the cookie again. Verify they differ. (Assertion [#9](#test-assertion-4819))
10. Add a passkey, sign out, sign in as an Admin and delete the user's account. Verify the account and its passkey records no longer exist. (Assertion [#10](#test-assertion-48110))

## Test Case 48.2

**Server-side passkey rules**

Pre-condition: An instance with passkey sign-in configured for the address the test opens, a local end user account, and an Admin account. Requests carry the session cookies of a browser that signed in, and the `Origin` header is set explicitly where noted.

### Test Assertion:

#### Test Assertion #48.2.1
Starting a passkey registration (`POST /webauthn/register/options`) with an `Origin` that is not one of the allowed origins, including `null`, is refused with 403, and no WebAuthn identity is stored.

#### Test Assertion #48.2.2
The same request with the application's own origin succeeds and returns registration options with a challenge.

#### Test Assertion #48.2.3
An Admin can delete an account whose passkey registration was started but never finished. The account and the WebAuthn identity are both removed.

#### Test Assertion #48.2.4
Looking up a username (`POST /info/passkey/lookup`) is answered for a first request and for another username, and a username looked up repeatedly within a minute is eventually refused with 429.

#### Test Assertion #48.2.5
A non-Admin cannot remove passkeys through `DELETE /passkey-admin/users/{appUserId}` or `DELETE /passkey-admin/stale`; the request is refused with a 4xx answer. An Admin can remove a user's passkeys and gets the number removed.

### Test Step Pre-condition:
1. Passkey sign-in is configured on the instance (not needed for steps 4 and 5).
2. A local end user account and an Admin account exist.

### Test Step:
1. Sign in as the end user and start a registration with `Origin: https://evil.example.com` and with `Origin: null`. Verify 403 and that no WebAuthn identity was stored. (Assertion [#1](#test-assertion-4821))
2. Start a registration with the application's own origin. Verify 200 and a challenge in the answer. (Assertion [#2](#test-assertion-4822))
3. Start a registration as the end user, sign in as the Admin and delete the end user's account. Verify the account and the identity are gone. (Assertion [#3](#test-assertion-4823))
4. Look up a random username, look it up again up to fifteen times, then look up another random username. Verify a 429 appears for the repeated one and the other one is answered. (Assertion [#4](#test-assertion-4824))
5. As the end user call both `/passkey-admin` endpoints and verify they are refused; as the Admin call `DELETE /passkey-admin/users/{appUserId}` and verify the answer reports the number removed. (Assertion [#5](#test-assertion-4825))

## Known coverage gaps

- A disabled or locked account being refused at passkey sign-in is covered by a unit test only. The application does not yet refuse such accounts at password sign-in either, so there is no end-to-end behavior to assert.
- The made-up answer for unknown usernames is covered by unit tests. An end-to-end check would need many accounts to see the answer track the share of accounts with a passkey.
- The per-address limit is not exercised end to end, because a shared instance would let other tests trip it; it is covered by unit tests.
- Adding a passkey from a signed-in session still needs no fresh sign-in. The origin check stops other sites, but a stolen session could add one.
