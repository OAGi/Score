import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

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

  themes: ['@docusaurus/theme-mermaid'],

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
          editUrl: 'https://github.com/OAGi/Score/tree/develop/website',
          showLastUpdateTime: true,
          // Single-version site: docs/ documents the released 3.5.2 (the commit this
          // branch is cut from). If per-release snapshots are needed again, cut them
          // with: npm run docusaurus docs:version X.Y.Z
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
        {to: '/reference/whats-new', label: "What's New", position: 'left'},
        {
          href: 'https://github.com/OAGi/Score',
          label: 'GitHub',
          position: 'right',
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
