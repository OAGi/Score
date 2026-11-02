---
title: What's New
sidebar_position: 1
toc_max_heading_level: 2
---

What's new in the **3.6** line — the current published release is **3.6.0**. connectCenter
follows [Semantic Versioning](./release-numbering.md); each change below links to its GitHub
issue. The same history — including releases before 3.6.0 — is maintained on the
[Release Detail wiki](https://github.com/OAGi/Score/wiki/Release-Detail) and the
[GitHub releases](https://github.com/OAGi/Score/releases). Documentation for the previous line is
available from the version selector (**3.5**).

## 3.6.0

This is a platform and usability release. The backend moves to Spring Boot 4.1 on Java 21 and the
front end to Angular 22; users can sign in with a **passkey**; every tree screen gains an
**XPath-like search**; an **ACC validator** catches duplicate properties and other problems before
a release; core-component reads are served from a cache so large libraries respond faster; and an
empty MariaDB can now initialize itself. It also hardens the API's authorization, CORS and
WebSocket handling, and fixes a large number of input-validation and consistency defects.

### Enhancements

#### Sign-in

- [Issue #1815 - Sign in with a passkey (WebAuthn)](https://github.com/OAGi/Score/issues/1815)

  > Local accounts can sign in with a fingerprint, face, screen lock or security key, add and
  > remove passkeys from **Settings → Account → Passkeys**, and are told by message whenever the
  > passkeys of their account change. The feature is **off by default** and is turned on by
  > setting an RP ID and allowed origins on the server — see
  > [Passkey Setup](../02-getting-started/passkey-setup.md) and
  > [Passkey Sign-in](../04-user-guide/08-passkey-sign-in.md). The sign-in page itself was
  > redesigned.

#### Core components

- [Issue #1528 - Add Duplicate Property validation Rule to a Menu Item on the ellipse of the root component](https://github.com/OAGi/Score/issues/1528)

  > A read-only **Validate** menu item on the root node of an ACC (and of ASCCP, BCCP, BDT and
  > OAGi Extension pages) lists problems — duplicate properties, circular base ACCs, attribute
  > rules, deprecated references — in a panel, with markers on the affected tree nodes. Before
  > a base ACC is set, a property is appended or a property is refactored, the effect on the
  > other ACCs is checked and only the *newly introduced* problems are shown. See
  > [Validate an ACC](../04-user-guide/core-component-management/developer/05-acc-management.md#validate-an-acc).

- [Issue #1817 - Search the trees with XPath-like expressions through their component graph in a Web Worker](https://github.com/OAGi/Score/issues/1817)

  > The search box above the Core Component, Model Browser, BIE editor and BIE uplift trees
  > accepts path expressions (`Sender//Code`, `*[type = ACC and state = Published]`,
  > `*[count(child::*) > 5]`) with suggestions while you type, searches node definitions, and
  > runs in the background so large trees stay responsive. See
  > [Search with path expressions](../04-user-guide/core-component-management/03-search-and-browse-cc-library.md#search-with-path-expressions).

#### BIE packages and expressions

- [Issue #1733 - Populate Backward Compatibility Indicator and Revision Reason Text in BIE Package Manifest](https://github.com/OAGi/Score/issues/1733)

  > The backward compatibility indicator is now defined by explicit rules (reported separately
  > for XML Schema and JSON Schema), pairs each BIE with the BIE it replaces by id, then by
  > source, then by root ASCCP, and lists BIEs new in the package as `false`. See
  > [Backward compatibility indicator](../04-user-guide/bie-management/07-manage-bie-package.md#backward-compatibility-indicator).

- [Issue #1870 - BIE Express page ignores the configured BIE Schema filename expression](https://github.com/OAGi/Score/issues/1870)

  > The configured filename expressions are applied on the BIE Express page and on BIE Package
  > export, and the settings page offers editable samples with backend-driven suggestions.

- OpenAPI: nillable BIEs of OpenAPI 3.0 are marked `nullable` instead of an unconstrained
  `oneOf` branch, a nillable ASBIE that reuses another BIE is marked nullable, an OpenAPI
  Document title that is not a valid file name can be generated, and the **Terms of Service,
  Contact URL, Contact Email and License URL** fields are validated.

#### Settings and navigation

- [Issue #1865 - Split Application settings into sub-menu pages](https://github.com/OAGi/Score/issues/1865)
- [Issue #1866 - Add a compact layout for narrow screens to every table of score-web](https://github.com/OAGi/Score/issues/1866)

  > Application settings is now four pages (**Features**, **SMTP**, **Filename Expressions**,
  > **Web Page**) in the settings menu, and every table switches to a compact card layout on a
  > narrow screen, with a responsive side drawer for navigation.

#### Deployment and operations

- [Issue #1814 - Add a Flyway baseline so an empty MariaDB initializes itself](https://github.com/OAGi/Score/issues/1814)

  > The backend can create the whole schema in an empty MariaDB (11.x, 12.x and 13.x are
  > verified) — the `srt-repo` image is no longer required. See
  > [Using your own MariaDB](../02-getting-started/deployment.md#using-your-own-mariadb).

- Credentialed cross-origin access is now an allow-list (`SCORE_CORS_ALLOWED_ORIGINS`) — see
  [Cross-origin requests](../02-getting-started/deployment.md#cross-origin-requests-cors).

### Performance

- [Issue #1818 - Serve the CC records from a shared cache invalidated by a trigger-written change log](https://github.com/OAGi/Score/issues/1818)

  > Core-component records are cached in the JVM (Caffeine by default; Redis can be selected with
  > `spring.cache.type`) and forgotten, across backend instances, from a change log written by
  > database triggers. A record that changes also forgets the records derived from it.

- [Issue #1820 - Make the levenshtein function faster without changing its result](https://github.com/OAGi/Score/issues/1820)
- [Issue #1821 - Add default primitive lookup indexes for the data type list](https://github.com/OAGi/Score/issues/1821)
- [Issue #1822 - Read less from the database when generating a BIE expression](https://github.com/OAGi/Score/issues/1822)
- [Issue #1823 - Make Where Used of a component cheaper](https://github.com/OAGi/Score/issues/1823)
- [Issue #1824 - Load the CC list with fewer queries and cheaper count and page queries](https://github.com/OAGi/Score/issues/1824)
- [Issue #1825 - Resolve the used flag of Agency ID List and Code List values in one query](https://github.com/OAGi/Score/issues/1825)
- [Issue #1869 - XML Schema generation of a BIE in Inverse Mode runs out of memory because the whole DOM is kept](https://github.com/OAGi/Score/issues/1869)

  > The XML Schema is now written while it is generated instead of building the whole DOM, which
  > keeps memory use low for very large BIEs. Responses and static files are also compressed.

### Bug fixes

- [Issue #1813 - BIE Package XML Schema Generation Produces an Invalid Schema When an Indicator Element Appears In Multiple Files Defining the Schema](https://github.com/OAGi/Score/issues/1813)
  — global types shared by the files of a package are declared once.
- **Security hardening.** State-changing requests from other origins are rejected and sign-out
  is accepted only as `POST`; the session cookie is `SameSite=Lax`; the WebSocket endpoint
  requires a session and limits subscriptions; an open redirect in the GitHub connect flow was
  closed; PlantUML runs in its sandbox profile; and many endpoints now check the caller's role,
  ownership and state (Module Set, Release, Tenant, Tag, Core Component purge and append, Code
  List and Agency ID List purge and revision cancel, BIE Package moves, account enable/disable).
- **Input validation.** Blank or whitespace-only names, versions and identifiers are rejected for
  Libraries, Module Sets, Releases, Context Schemes, Code Lists, Agency ID List values, Tags,
  BIE Packages and Core Component comments; a circular base-ACC chain is refused; URLs of a
  Business Term are opened only when they are `http` or `https`.
- **Consistency.** A discarded library deletes its namespaces, a discarded BIE removes its
  business-term assignments, a Code List derived from another records its base code list, the
  values a base code list overwrites are reported on uplift, several Core Components purge
  together in a dependency-respecting order, and the Home page counts now match their links.
- Multi-tenant mode: an Admin sees the BIEs and business contexts of every tenant, and a
  business context connected to several tenants is listed once.

### Platform updates

- **Backend:** Spring Boot 3.5 → **4.1** (Spring Framework 7, Spring Security 7, Jackson 3) on
  **Java 21** and **Tomcat 11.0** ([Issue #1750](https://github.com/OAGi/Score/issues/1750)); jOOQ 3.21.
- **Front end:** Angular 21 → **22**, TypeScript 6.0, and the shared page, table, dialog and tree
  components that every screen is now built on
  ([Issue #1749](https://github.com/OAGi/Score/issues/1749),
  [#1816](https://github.com/OAGi/Score/issues/1816),
  [#1819](https://github.com/OAGi/Score/issues/1819)). The web image runs on unprivileged nginx.
- Docker images are built for `linux/amd64` and `linux/arm64`.
- Library updates for Trivy and `npm audit` findings.

### Upgrade note

Before proceeding with the upgrade, we strongly recommend **backing up your data**. As with
earlier 3.5 releases, database version management is handled by Flyway: on first startup the
backend applies the `V3_6_0` migration — which adds the passkey tables and lookup indexes — so you
do not need to apply a database migration SQL script manually. See
[Upgrading connectCenter](../05-operations/upgrade.md#upgrading-to-36x) for the platform changes to
check first.

### Docker environment

If you are running connectCenter in a Docker environment, update the following image tags:

- `srt-web` to `3.6.0`
- `srt-http-gateway` to `3.6.0`
- `srt-repo` to `3.6.0`

See [Installation with Docker](../02-getting-started/installation-docker.md) for an example
`docker-compose.yml`.
