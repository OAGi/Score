import { PHASE_DEVELOPMENT_SERVER } from 'next/constants.js';

const PROD_DOCS_BASE_PATH = '/services/docs';

/** @type {(phase: string) => import('next').NextConfig} */
export default function nextConfig(phase) {
  const isDev = phase === PHASE_DEVELOPMENT_SERVER;
  const extraAllowedDevOrigins =
    process.env.NEXT_ALLOWED_DEV_ORIGINS?.split(',').map((s) => s.trim()).filter(Boolean) ?? [];

  return {
    reactStrictMode: true,

    // In prod we build a standalone Next server so docs can run behind a
    // separate port and read runtime environment from the container.
    ...(isDev ? {} : { output: 'standalone' }),

    // Production docs are published behind the reverse-proxy path prefix.
    ...(isDev ? {} : { basePath: PROD_DOCS_BASE_PATH }),

    // Keep path generation consistent in both dev and prod.
    trailingSlash: true,

    // Keep next/image compatible without an external optimization server.
    images: { unoptimized: true },

    // Only needed in dev: proxy backend paths to FastAPI so the docs app can
    // read `/openapi.json` and hit API routes without CORS setup.
    ...(isDev
      ? {
          allowedDevOrigins: ['127.0.0.1', '::1', ...extraAllowedDevOrigins],
          async rewrites() {
            const backendOrigin = process.env.NEXT_PUBLIC_BACKEND_ORIGIN ?? 'http://127.0.0.1:5555';
            return [
              { source: '/openapi.json', destination: `${backendOrigin}/api/openapi.json` },
              { source: '/api/health', destination: `${backendOrigin}/api/health` },
              { source: '/api/health/db', destination: `${backendOrigin}/api/health/db` },
              { source: '/api/context-categories/:path*', destination: `${backendOrigin}/api/context-categories/:path*` },
              { source: '/api/context-schemes/:path*', destination: `${backendOrigin}/api/context-schemes/:path*` },
              { source: '/api/business-contexts/:path*', destination: `${backendOrigin}/api/business-contexts/:path*` },
              { source: '/api/accounts/:path*', destination: `${backendOrigin}/api/accounts/:path*` },
              { source: '/api/me', destination: `${backendOrigin}/api/me` },
            ];
          },
        }
      : {}),
  };
}
