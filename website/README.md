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
pages were authored from the codebase and config of the **v3.5.2** release; the wiki content
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

The site is **single-version**: the `docs/` tree documents the released **3.5.2** (the commit
this branch is based on). There is no version dropdown and no `versioned_docs/` snapshots.

If per-release snapshots are needed again later, Docusaurus versioning can be re-enabled by
cutting a snapshot (`npm run docusaurus docs:version X.Y.Z`) and restoring the
`docsVersionDropdown` navbar item. Keep the total under ~10 versions to control build time, and
note that `07-reference/whats-new.md` is version-specific (hand-authored per version).

## Deployment to GitHub Pages

The site lives on the **`gh-pages` branch** (source, not built output) and is published to
https://OAGi.github.io/Score/ by `.github/workflows/deploy-website.yml`, which builds the site
and deploys it via the Pages Actions flow on every push to `gh-pages` that touches `website/**`.
The repo's **Settings → Pages → Source** must stay on **GitHub Actions**.

`url`/`baseUrl` in `docusaurus.config.ts` are set for a GitHub **project page**
(`https://OAGi.github.io/Score/`). For a custom domain at the root, set `baseUrl: '/'` and add a
`CNAME` file under `static/`.
