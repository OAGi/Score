import './globals.css';

import Script from 'next/script';
import type { ReactNode } from 'react';

import { PlaygroundAuthProvider } from '@/components/playground-auth';
import { ReferenceHeader } from '@/components/reference-header';
import { getOidcRuntimeConfig } from '@/lib/oidc';

export const dynamic = 'force-dynamic';

export const metadata = {
  title: 'connectCenter Developers',
  description: 'connectCenter API reference docs',
};

function resolveRuntimeBackendApiBase(): string {
  const publicBase = (process.env.PUBLIC_API_BASE_URL ?? 'http://127.0.0.1:5555').trim().replace(/\/+$/, '');
  if (!publicBase) {
    return 'http://127.0.0.1:5555/api';
  }
  return publicBase.endsWith('/api') ? publicBase : `${publicBase}/api`;
}

export default function RootLayout({ children }: { children: ReactNode }) {
  const oidc = getOidcRuntimeConfig();
  const runtimeConfigJson = JSON.stringify({
    backendApiBase: resolveRuntimeBackendApiBase(),
    oidc,
  });

  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        <Script id="connectcenter-theme-init" strategy="beforeInteractive">
          {`(function(){try{var key='connectcenter-theme';var saved=localStorage.getItem(key);var mql=window.matchMedia('(prefers-color-scheme: dark)');var isDark=(saved==='dark')||(saved!=='light'&&mql.matches);var root=document.documentElement;root.dataset.theme=(saved==='light'||saved==='dark')?saved:'system';root.classList.toggle('dark',isDark);root.style.colorScheme=isDark?'dark':'light';}catch(e){}})();`}
        </Script>
        <Script id="connectcenter-runtime-config" strategy="beforeInteractive">
          {`window.__CONNECTCENTER_RUNTIME__=${runtimeConfigJson};`}
        </Script>
      </head>
      <body>
        <PlaygroundAuthProvider>
          <ReferenceHeader />
          <div className="pt-14">{children}</div>
        </PlaygroundAuthProvider>
      </body>
    </html>
  );
}
