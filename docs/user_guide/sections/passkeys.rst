Passkey Sign-in
===============

A passkey lets you sign in to connectCenter with your fingerprint, face, screen lock, or a security key instead of typing your password.
Passkeys are an addition: nothing changes for you until an administrator turns passkeys on.

Passkeys are for accounts that sign in with a username and password.
If your organization signs you in through a single sign-on provider, you use that provider's own sign-in, including any passkeys it offers, and connectCenter shows no passkey options.

Signing in with a passkey
-------------------------

When passkey sign-in is on, and your browser supports passkeys, the sign-in page first asks for your username only, with a *Continue* button.

1. Type your username and click *Continue*.
   If your account has a passkey, your browser opens its passkey prompt right away.
   Confirm with your fingerprint, face, or screen lock, or choose where your passkey is, such as your phone or a security key.
   If your account has no passkey, the page asks for your password.

2. If you dismiss the prompt, the page offers *Continue* (try the passkey again) and *Try another way*.
   *Try another way* asks for your password.

3. Or click the username field and pick a passkey from your browser's list, or click "Sign in with a passkey" below the form.
   Your browser shows its own passkey chooser; you do not need to type a username.

If connectCenter cannot verify the passkey, the sign-in page says so and you can use your password.

The "Sign in with a passkey" button appears under the same "or" divider as any single sign-on buttons, so the sign-in page never shows two dividers.

Adding a passkey
----------------

You can add a passkey right after signing in, or at any time from your account settings.

Right after signing in
~~~~~~~~~~~~~~~~~~~~~~

When you sign in with your password and you do not have a passkey yet, connectCenter offers to create one in a dialog titled "Sign in faster with a passkey".
The offer appears only when passkey sign-in is on, your browser supports passkeys, and this device has a screen lock or biometrics.

- Click "Create a passkey" and confirm in the browser's prompt.
  The passkey is created without a name of its own; the list shows where it is kept (for example "Apple Passwords").
  You can rename it later.
  The dialog then confirms "Passkey added" and offers "Manage passkeys".
- Click "Not now" to be asked again in 30 days.
- Click "Don't ask again" to stop the offer for your account in this browser.
- Closing the dialog with Escape or a click outside counts as "Not now".

If you dismiss the browser's prompt, nothing is added and you can try again from the dialog.
The offer is never shown if you already have a passkey, and it is not shown after a sign-in with a passkey or through single sign-on.
Your choice is remembered per account in the browser you used, so another browser may ask once more.

From your account settings
~~~~~~~~~~~~~~~~~~~~~~~~~~

The *Passkeys* page under *Account* is always available while passkey sign-in is on and you are signed in with a username and password or a passkey.
Open it from *Settings*, then *Account*, then *Passkeys*.

To add a passkey there:

1. Open *Settings*, then *Account*, then *Passkeys*.

2. Click "Add a passkey" and confirm in the browser's prompt.
   The passkey gets no name of its own; use the pencil button afterwards if you want to give it one.

Each device or security key can hold its own passkey, so you can add several.
A passkey that your browser or password manager syncs between your devices is added once and works on all of them.
Each passkey also shows where it is kept (for example "Bitwarden" or "Apple Passwords"; "Unknown" when your browser does not say), and when it was last used and from which browser and system ("Not used yet" until its first sign-in).
The browser is read from the browser's own identification, so a browser that presents itself as Chrome, such as Brave, is shown as Chrome.
It is worth adding a second passkey as a backup.

Renaming and removing passkeys
------------------------------

The *Passkeys* box lists your passkeys with the date each was added.
Use the pencil to rename one, or the bin to remove one.

Removing a passkey deletes it from connectCenter, so it can no longer be used to sign in.
It does not delete the passkey from your device, and connectCenter cannot do that for you; if you want it gone there too, delete it in your device or password manager settings.
Likewise, if you delete a passkey from your device, connectCenter does not find out, so remove it from the list as well.

You cannot remove your only sign-in method: an account without a password keeps at least one passkey.

A passkey marked "Cannot be used here" was added for a different RP ID than the one connectCenter now uses (for example after the server moved to a new domain).
Your browser will not offer it.
Remove it and add a new one.

.. Screenshot to add — capture media/passkeys_account.png (the Passkeys box with two passkeys, one from a
   password manager) and media/passkeys_sign_in.png (the sign-in page with the passkey button),
   then re-enable image directives here.

Administrator setup
-------------------

Passkey sign-in is configured when the application is deployed, not in the Application settings page.
It has no separate on/off switch: it is on exactly when an RP ID and at least one allowed origin are set, and off when they are empty.
Nothing needs to change for an existing installation; without these values it behaves exactly as before.

Set these in ``application.yml`` or, for a Docker deployment, as environment variables of the backend container:

.. list-table::
   :header-rows: 1
   :widths: 24 28 48

   * - Environment variable
     - ``application.yml``
     - Meaning
   * - ``SCORE_PASSKEY_RP_ID``
     - ``score.passkey.rp-id``
     - The domain passkeys are tied to, for example ``score.example.org``.
       Use a bare host name (no scheme, port, or path).
       Use the full host name of this instance, or a parent domain such as ``example.org`` if every host under it should share passkeys.
   * - ``SCORE_PASSKEY_ALLOWED_ORIGINS``
     - ``score.passkey.allowed-origins``
     - The addresses users open in the browser, with scheme and port, for example ``https://score.example.org``.
       Separate several with commas or spaces (or put each on its own line); a semicolon is not a separator.
       Every origin must be written out in full: wildcards such as ``https://*.example.org`` are not accepted.
       Every origin must be under the RP ID.
       Plain ``http`` is accepted only for ``localhost``.
   * - ``SCORE_PASSKEY_RP_NAME``
     - ``score.passkey.rp-name``
     - Optional. The name your browser shows when it asks about the passkey. It defaults to the RP ID.
   * - ``SCORE_PASSKEY_RATE_LIMIT_PER_IP``
     - ``score.passkey.rate-limit.per-ip-per-minute``
     - Optional. How many times a minute one client address may ask the sign-in page's passkey endpoints for sign-in options or look up a username.
       The default is 60.
   * - ``SCORE_PASSKEY_RATE_LIMIT_PER_USERNAME``
     - ``score.passkey.rate-limit.per-username-per-minute``
     - Optional. How many times a minute one username may be looked up, whichever address asks. The default is 10.
   * - ``SCORE_PASSKEY_CLIENT_IP_HEADERS``
     - ``score.passkey.rate-limit.client-ip-headers``
     - Optional. Where to find the client's address when connectCenter is behind a proxy or CDN: the request headers to read, in order of preference, separated by commas.
       The first header that holds an address is used.
       The default is ``X-Forwarded-For``.
       For example ``CF-Connecting-IP,True-Client-IP,X-Forwarded-For`` for an instance behind Cloudflare or Akamai.
       Leave it empty to always use the address of the connection.
   * - ``SCORE_PASSKEY_TRUSTED_PROXIES``
     - ``score.passkey.rate-limit.trusted-proxies``
     - Optional. The proxies whose headers are believed: addresses or CIDR ranges separated by commas, for example ``10.0.0.0/8,172.16.0.0/12``, or ``*`` for any.
       The default is the private networks (``10.0.0.0/8``, ``172.16.0.0/12``, ``192.168.0.0/16``, ``127.0.0.0/8`` and similar).
       A request from any other address is counted at its own address and its headers are ignored.

For example, in ``docker-compose.yml``:

.. code-block:: yaml

   backend:
     environment:
       - SCORE_PASSKEY_RP_ID=score.example.org
       - SCORE_PASSKEY_ALLOWED_ORIGINS=https://score.example.org

To allow several addresses, list each one:

.. code-block:: yaml

   backend:
     environment:
       - SCORE_PASSKEY_RP_ID=example.org
       - SCORE_PASSKEY_ALLOWED_ORIGINS=https://score.example.org https://test.score.example.org

For local development, ``SCORE_PASSKEY_RP_ID=localhost`` with ``SCORE_PASSKEY_ALLOWED_ORIGINS=http://localhost:4200`` works over plain ``http``.

The values are checked when the application starts.
A mistake, such as an origin that is not under the RP ID, stops the application from starting and the error says why, so it is caught during the deployment.

Passkeys are bound to the RP ID by the browser, and the server cannot change that.
If you change the RP ID later, every passkey registered for the old one stops working.
Users can still sign in with their password and add new passkeys, and the old passkeys are kept until removed.
Each instance keeps its own passkeys, so changing the host name of an instance means users register again.
To delete the leftovers, an administrator can call ``DELETE /passkey-admin/stale``, which removes every passkey registered for an RP ID other than the configured one.

Security notes for administrators:

* Adding a passkey is accepted only from a page opened at one of the allowed origins.
  A request that names another origin is refused, so a page elsewhere on the same site cannot add a passkey to a signed-in user's account.
  If connectCenter is behind a reverse proxy, the proxy must pass the browser's ``Origin`` header on unchanged.
* The sign-in page's passkey endpoints can be called by anyone, so they are rate limited, per client address and, for the username lookup, per username.
  Someone over the limit gets a "Too many requests" answer (HTTP 429) and the sign-in page falls back to the password.
  The limits are counted separately by each backend instance.
* The limits count requests per client address.
  Behind a proxy, connectCenter reads the address from the headers named in ``SCORE_PASSKEY_CLIENT_IP_HEADERS``, and only when the request comes from a proxy listed in ``SCORE_PASSKEY_TRUSTED_PROXIES``.
  If the proxy does not pass the address on, every request looks like it comes from the proxy and all users share one limit; fix the proxy (for nginx, ``proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;``) or raise ``SCORE_PASSKEY_RATE_LIMIT_PER_IP``.
  Only list headers that your own edge proxy sets or overwrites.
  A header that clients can send and the proxy passes on unchanged, such as ``CF-Connecting-IP`` on an instance that is not behind Cloudflare, can be forged to get around the limit.
  A list such as ``X-Forwarded-For`` is read from the right, skipping trusted proxies, so addresses a client adds itself are not believed.
  The setting affects only these limits, not the rest of the application.
* The username lookup does not tell whether a username exists: for a name that has no account it gives a made-up answer, so asking about a name proves nothing.
* Signing in with a passkey starts a new session, as signing in with a password does.
* Passkey sign-in applies the same account checks as a password sign-in.

To turn passkeys off, unset the variables (or empty them) and restart.
Passkey sign-in and registration stop; registered passkeys are kept, and setting the same RP ID again brings them back.
People who are already signed in stay signed in, and password and single sign-on keep working throughout.

If your single sign-on provider offers its own passkeys, you can use those instead and leave these values empty.
Requiring a passkey at the provider is configured there, not in connectCenter.

Removing a user's passkeys
~~~~~~~~~~~~~~~~~~~~~~~~~~

If a user loses a device, an administrator can remove all of that user's passkeys and, if needed, end their current sessions.
This is available through the application's API (``DELETE /passkey-admin/users/{appUserId}``, with ``invalidateSessions=true`` to end the sessions); there is no button for it in the user interface yet.
Only an administrator can use ``/passkey-admin``; anyone else is refused.

Deleting a user's account also deletes that user's passkeys, so an account can be deleted whether or not the user ever added a passkey, or started to.
