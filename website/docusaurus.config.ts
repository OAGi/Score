import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';
import type {PluginOptions as SearchLocalPluginOptions} from '@easyops-cn/docusaurus-search-local';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {
  title: 'connectCenter',
  tagline: 'CCS-based library development, profiling, and schema generation',
  favicon: 'img/favicon.ico',

  // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
  future: {
    v4: true, // Improve compatibility with the upcoming Docusaurus v4
  },

  // Production URL and base path.
  // NOTE: configured for a GitHub project page (https://OAGi.github.io/Score/).
  // If a custom domain is used at the root, set baseUrl to '/' and add a CNAME.
  // Raw HTML <img> tags in docs (used where the user guide needs explicit
  // width/height) are NOT baseUrl-processed by Docusaurus, so they hard-code
  // src="/Score/img/..." — update them together with baseUrl.
  url: 'https://OAGi.github.io',
  baseUrl: '/Score/',

  // GitHub pages deployment config.
  organizationName: 'OAGi', // GitHub org/user name.
  projectName: 'Score', // Repo name.

  // Fail the build on any broken internal link or anchor so regressions are caught
  // before publishing.
  onBrokenLinks: 'throw',
  onBrokenAnchors: 'throw',
  markdown: {
    // Treat .md as CommonMark (lenient with <, {, >) and .mdx as MDX. This keeps
    // bulk-converted user-guide prose robust against stray special characters.
    format: 'detect',
    mermaid: true,
    hooks: {
      onBrokenMarkdownLinks: 'warn',
    },
  },

  themes: [
    '@docusaurus/theme-mermaid',
    // Offline/local search: the index is generated at build time and served as
    // static files, so it works on GitHub Pages without an external service
    // (unlike Algolia DocSearch). The search box only functions on a production
    // build (`npm run build && npm run serve`), not with `npm run start`.
    [
      '@easyops-cn/docusaurus-search-local',
      {
        // Docs are served at the site root (see routeBasePath above).
        docsRouteBasePath: '/',
        // The blog is disabled; without this the plugin fails to resolve it.
        indexBlog: false,
        // Content-hash the index files so browsers can cache them long-term.
        hashed: true,
        highlightSearchTermsOnTargetPage: true,
      } satisfies SearchLocalPluginOptions,
    ],
  ],

  // Even if you don't use internationalization, this sets useful metadata (html lang).
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          // Documentation is the whole site; serve docs at the root.
          routeBasePath: '/',
          editUrl: 'https://github.com/OAGi/Score/tree/gh-pages/website',
          showLastUpdateTime: true,
          // One docs version per MINOR line, labelled "X.Y" (a patch release
          // updates the docs of its line in place).
          //   docs/               -> 3.6, the latest line, served at the site root
          //   versioned_docs/...  -> 3.5 snapshot (internal name 3.5.x), served under /3.5/
          // When the next minor line is started, snapshot the finished docs with
          //   npm run docusaurus docs:version 3.6
          // then add the new label for `current` below and set its `path` to ''.
          lastVersion: 'current',
          versions: {
            current: {
              label: '3.6',
              path: '',
            },
            '3.5.x': {
              label: '3.5',
              path: '3.5',
            },
          },
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    colorMode: {
      respectPrefersColorScheme: true,
    },
    // The reference Sphinx user guide exposes deep sub-sections (h4/h5) in its
    // navigation; surface the same depth in the on-page table of contents.
    tableOfContents: {
      minHeadingLevel: 2,
      maxHeadingLevel: 5,
    },
    // Brand = the same image score-web renders in its toolbar (the
    // score.pages.navbar.brand configuration seeded by score-repo/docker/oagis.sql,
    // extracted to static/img/navbar-brand.svg). The image contains the
    // "connectCenter" wordmark, so there is no separate navbar title text;
    // the favicon is the connectCenter product favicon from score-web.
    navbar: {
      logo: {
        alt: 'connectCenter',
        src: 'img/navbar-brand.svg',
        width: 170,
        height: 26,
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docsSidebar',
          position: 'left',
          label: 'Documentation',
        },
        {
          // A doc link (not a fixed path) so it follows the version selected
          // in the dropdown: each version has its own What's New.
          type: 'doc',
          docId: 'reference/whats-new',
          label: "What's New",
          position: 'left',
        },
        {
          // Switches between the 3.6 and 3.5 documentation (see the
          // docs.versions comment above); stays on the same page when the
          // target version has it.
          type: 'docsVersionDropdown',
          position: 'right',
        },
        {
          href: 'https://github.com/OAGi/Score',
          position: 'right',
          // Icon-only link; the glyph is drawn by .header-github-link in
          // custom.css, so the accessible name must come from aria-label.
          className: 'header-github-link',
          'aria-label': 'GitHub repository',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Documentation',
          items: [
            {label: 'Introduction', to: '/'},
            {label: 'Getting Started', to: '/getting-started/installation-docker'},
            {label: 'User Guide', to: '/category/user-guide'},
          ],
        },
        {
          title: 'Project',
          items: [
            {label: 'GitHub', href: 'https://github.com/OAGi/Score'},
            {label: 'Open Applications Group (OAGi)', href: 'https://oagi.org'},
            {label: 'NIST', href: 'https://www.nist.gov/services-resources/software/score-standards-life-cycle-management-tool'},
          ],
        },
        {
          title: 'Standards',
          items: [
            {label: 'UN/CEFACT CCTS', href: 'https://unece.org/trade/uncefact/ccts'},
            {label: 'ISO 15000-5 (CCS)', href: 'https://www.iso.org/standard/61433.html'},
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Open Applications Group (OAGi). Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['bash', 'json', 'sql', 'java', 'yaml', 'xml-doc'],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
