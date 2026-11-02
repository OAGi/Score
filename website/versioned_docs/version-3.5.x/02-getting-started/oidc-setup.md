---
title: OIDC Setup
sidebar_position: 2
---

connectCenter can sign users in through an external **OpenID Connect (OIDC)** provider,
configured at install time. This is optional — local accounts work without it — but external
add-ons such as the
[external API](./installation-docker.md#optional-adding-the-external-api) rely on JWT
validation against an OIDC provider and therefore require it. The configuration of the
Identity Provider (IdP) is inserted into the `oauth2_app` and `oauth2_app_scope` tables in
the database.

This page is the reference for those two tables, with a worked example. It assumes you have a
running deployment as set up in [Installation with Docker](./installation-docker.md).

## The `oauth2_app` table

One row per provider.

| Column | Type | Required | Description |
|---|---|---|---|
| `oauth2_app_id` | `bigint unsigned` | — | Primary key (auto-increment). |
| `provider_name` | `varchar(100)` | Yes | connectCenter's internal name of the provider. It must be unique, and it appears in the redirection URI path. |
| `issuer_uri` | `varchar(200)` | Option a | The provider's base (issuer) endpoint, for providers that support the Well-Known URI Discovery mechanism — the provider publishes its metadata at `{issuer_uri}/.well-known/openid-configuration`. |
| `authorization_uri` | `varchar(200)` | Option b | Authorization endpoint, for providers that do not support Well-Known URI Discovery. |
| `token_uri` | `varchar(200)` | Option b | Token endpoint. |
| `user_info_uri` | `varchar(200)` | Option b | User Info endpoint. |
| `jwk_set_uri` | `varchar(200)` | Option b | JSON Web Key Set endpoint. |
| `redirect_uri` | `varchar(200)` | Yes | The redirection URI to which the IdP sends the result of the client sign-in request through the OAuth 2.0 flows, in the form `{scheme}://{hostname}/api/oauth2/code/{provider_name}` — `scheme` is `http` or `https` (optionally with a port number) and `hostname` is the hostname of connectCenter. |
| `end_session_endpoint` | `varchar(200)` | No | The provider's end-session (sign-out) endpoint. When set, it is added to the client registration's provider metadata for RP-initiated logout. |
| `client_id` | `varchar(200)` | Yes | The client ID issued by the IdP for the registered application. |
| `client_secret` | `varchar(200)` | Yes | The client secret issued by the IdP for the registered application. |
| `client_authentication_method` | `varchar(50)` | Yes | Client authentication method; possible values are `basic` or `post`. |
| `authorization_grant_type` | `varchar(50)` | Yes | Authorization grant type; possible values are `authorization_code`, `client_credentials`, `password`, etc. |
| `prompt` | `varchar(20)` | No | For IdPs that support the prompt parameter, display options during the sign-in process. Typical values are `none`, `login`, and `consent`. |
| `display_provider_name` | `varchar(100)` | No | Label of the provider's button on the connectCenter sign-in page. |
| `background_color` | `varchar(50)` | No | Background color of the sign-in button. |
| `font_color` | `varchar(50)` | No | Font color of the sign-in button. |
| `display_order` | `int` | No (default `0`) | Order of the sign-in button among providers. |
| `is_disabled` | `tinyint(1)` | No (default `0`) | If set to `1` (true), the button for this provider is not shown. |

For the IdP endpoints, configure **either** option a — `issuer_uri` alone, letting connectCenter
discover the rest of the provider's metadata — **or** option b — the four individual endpoints.
The redirect URI value may need to be registered in your IdP configuration; see your IdP
documentation for the endpoint URLs and the supported authentication-method, grant-type, and
prompt values.

## The `oauth2_app_scope` table

One row per scope granted to the application.

| Column | Type | Required | Description |
|---|---|---|---|
| `oauth2_app_scope_id` | `bigint unsigned` | — | Primary key (auto-increment). |
| `oauth2_app_id` | `bigint unsigned` | Yes | Foreign key referencing `oauth2_app`.`oauth2_app_id`. |
| `scope` | `varchar(100)` | Yes | An OpenID Connect scope, such as `openid`, `profile`, `email`, or `phone`. |

## Example: registering Google as a provider

```sql
INSERT INTO `oauth2_app`
(`provider_name`,
 `issuer_uri`,
 `redirect_uri`,
 `client_id`,
 `client_secret`,
 `client_authentication_method`,
 `authorization_grant_type`,
 `display_provider_name`, `background_color`, `font_color`)
VALUES
('google',
 'https://accounts.google.com',
 'https://example.score.com/api/oauth2/code/google',
 '<client-id-issued-by-google>',
 '<client-secret-issued-by-google>',
 'post',
 'authorization_code',
 'Google', '#df4930', '#ffffff');

INSERT INTO `oauth2_app_scope` (`oauth2_app_id`, `scope`)
VALUES
((SELECT `oauth2_app_id` FROM `oauth2_app` WHERE `provider_name` = 'google'), 'openid'),
((SELECT `oauth2_app_id` FROM `oauth2_app` WHERE `provider_name` = 'google'), 'profile'),
((SELECT `oauth2_app_id` FROM `oauth2_app` WHERE `provider_name` = 'google'), 'email');
```

Whenever a property is changed in the database, the application — specifically the
`http-gateway` (backend) service — must be restarted to load the changed information:

```bash
docker compose restart backend
```

Once users sign in through the provider, their sign-in requests appear as pending SSO accounts
that an administrator approves — see
[Administration → Using Single Sign-On](../04-user-guide/administration/08-using-single-sign-on.md).

:::note[Resource-server JWT validation]
The backend can also validate bearer tokens directly. Set the `RS_JWK_SET_URI` environment
variable on the `backend` service to your provider's `jwks_uri` (its
`resource-server.jwk-set-uri` setting). This is required when you add the
[external API service](./installation-docker.md#optional-adding-the-external-api).
:::
