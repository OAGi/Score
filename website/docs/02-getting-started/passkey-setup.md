---
title: Passkey Setup
sidebar_position: 3
---

Available from connectCenter **3.6.0**. Passkey (WebAuthn) sign-in lets local (username and
password) accounts sign in with a fingerprint, face, screen lock or security key. It is
**off by default** and entirely optional: without the settings below, sign-in behaves exactly as
before.

There is no database or UI switch. Passkey sign-in is on **exactly when** an RP ID and at least
one allowed origin are set, and off when they are empty. Set them as environment variables of the
`backend` service:

```yaml
backend:
  environment:
    - SCORE_PASSKEY_RP_ID=score.example.org
    - SCORE_PASSKEY_ALLOWED_ORIGINS=https://score.example.org
    - SCORE_APP_SECRET_KEY=<output of: openssl rand -base64 32>
```

- `SCORE_PASSKEY_RP_ID` is the domain passkeys are bound to. **Choose it once** — changing it later
  makes every passkey registered for the old value stop working.
- `SCORE_PASSKEY_ALLOWED_ORIGINS` lists the addresses users open in the browser, with scheme and
  port. Plain `http` is accepted only for `localhost`.
- `SCORE_APP_SECRET_KEY` is recommended: use the same value on every backend instance.
- The values are validated at startup; a mistake stops the application with an error that says why.
- Behind a reverse proxy, pass the browser's `Origin` header on unchanged and set
  `X-Forwarded-For` (nginx: `proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;`) — see
  [Put it behind a reverse proxy](./deployment.md#put-it-behind-a-reverse-proxy-tls).
- The passkey tables are created by the `V3_6_0` Flyway migration; there is nothing to run by hand.

The full reference — every setting (rate limits, trusted proxies, client-IP headers), the security
notes, and removing a user's passkeys — is in
[Passkey Sign-in → Administrator setup](../04-user-guide/08-passkey-sign-in.md#administrator-setup)
in the User Guide. Users register and use passkeys as described on the same page.
