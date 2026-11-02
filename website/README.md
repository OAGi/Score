# connectCenter Documentation Site

The consolidated documentation site for **connectCenter** (formerly Score), built with
[Docusaurus](https://docusaurus.io/) 3. It replaces the outdated content scattered across the
[OAGi/Score GitHub wiki](https://github.com/OAGi/Score/wiki) and folds in the full user guide.

## Structure

```
website/
├── docs/                 ← all documentation content (Markdown). Edited via PRs.
│   ├── 01-introduction/      What connectCenter is, key concepts
│   ├── 02-getting-started/   Docker install, dev environment, first steps
│   ├── 03-architecture/      Modules, tech stack, database, deployment
│   ├── 04-user-guide/        Full user guide (CC management, BIE management, admin, ...)
│   ├── 05-operations/        Backup/restore, upgrade, migration, admin SQL
│   ├── 06-contributing/      Prerequisites, dev setup, issue management, community
│   └── 07-reference/         Release notes, version numbering, feature list
├── static/img/user-guide/    User-guide images (copied from docs/user_guide/media)
├── docusaurus.config.ts      Site config (title, navbar, footer)
└── sidebars.ts               Sidebar (auto-generated from the docs/ folder structure)
```

Sources: the User Guide pages were converted from the Sphinx/reStructuredText guide in
`../docs/user_guide`; the Getting Started / Architecture / Operations / Contributing / Reference
pages were authored from the codebase and config of the **v3.6.0** release (the 3.5.x snapshot from **v3.5.3**); the wiki content
was rewritten for the current product.

## Local development

```bash
npm install
npm start            # dev server at http://localhost:3000/Score/ (live reload)
```

## Build & preview

```bash
npm run build        # static site into build/  (fails on any broken link or anchor)
npm run serve        # serve the production build locally
```

The build uses `onBrokenLinks: 'throw'` and `onBrokenAnchors: 'throw'`, so a broken internal
link or anchor fails the build.

## Versioning

The site has **one docs version per minor line**, labelled `X.Y`, and the navbar dropdown
switches between them:

| Version | Source | URL |
|---|---|---|
| **3.6** (latest) | `docs/` | `/Score/` |
| **3.5** | `versioned_docs/version-3.5.x/` | `/Score/3.5/` |

A **patch** release does not create a version: update the docs of its line in place (image tags,
dependency versions, and the cumulative `07-reference/whats-new.md`). When a new **minor** line
starts, snapshot the finished docs and begin the next line in `docs/`:

```bash
npm run docusaurus docs:version 3.6    # copies docs/ -> versioned_docs/version-3.6
```

then add the new label for `current` (with `path: ''`) and a `path: '3.6'` for the snapshot in
`docusaurus.config.ts`. Keep the total under ~10 versions to control build time. Note that
`07-reference/whats-new.md` is version-specific (hand-authored per version) — never regenerate it
from another version's copy.

## Deployment to GitHub Pages

The site lives on the **`gh-pages` branch** (source, not built output) and is published to
https://OAGi.github.io/Score/ by `.github/workflows/deploy-website.yml`, which builds the site
and deploys it via the Pages Actions flow on every push to `gh-pages` that touches `website/**`.
The repo's **Settings → Pages → Source** must stay on **GitHub Actions**.

`url`/`baseUrl` in `docusaurus.config.ts` are set for a GitHub **project page**
(`https://OAGi.github.io/Score/`). For a custom domain at the root, set `baseUrl: '/'` and add a
`CNAME` file under `static/`.
