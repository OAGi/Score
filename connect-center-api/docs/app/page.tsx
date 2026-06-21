import Link from 'next/link';
import { Boxes, Braces, GitBranch, Sparkles, Tags } from 'lucide-react';

export const dynamic = 'force-dynamic';

function resolveOpenApiDocumentUrl(): string {
  const publicBase = (process.env.PUBLIC_API_BASE_URL ?? 'http://127.0.0.1:5555').trim().replace(/\/+$/, '');
  const apiBase = publicBase.endsWith('/api') ? publicBase : `${publicBase}/api`;
  return `${apiBase}/openapi.json`;
}

export default function HomePage() {
  const openApiDocumentUrl = resolveOpenApiDocumentUrl();

  return (
    <main className="min-h-[calc(100vh-56px)] bg-[radial-gradient(circle_at_20%_0%,#eef5ff_0%,#ffffff_56%)] dark:bg-[radial-gradient(circle_at_20%_0%,#0b1220_0%,#000000_56%)]">
      <div className="mx-auto max-w-7xl px-6 py-12 md:py-16">
        <div className="flex justify-center py-8 md:py-14">
          <div className="max-w-5xl text-center">
            <p className="inline-flex items-center gap-2 text-xs font-semibold uppercase tracking-[0.18em] text-[#64748b] dark:text-[#94a3b8]">
              <Sparkles className="h-3.5 w-3.5" />
              Developer Guide
            </p>
            <h1 className="mt-4 text-4xl font-semibold tracking-tight text-[#0f172a] dark:text-white md:text-5xl">
              connectCenter Developers
            </h1>
            <p className="mx-auto mt-5 max-w-4xl text-base leading-7 text-[#475569] dark:text-[#cbd5e1]">
              connectCenter manages canonical core components, business information entities, business contexts, and
              related release data. The API reference uses the same terms as the user guide, so you can move from
              model concepts to concrete endpoints without guessing how the repository is organized.
            </p>
            <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
              <Link
                href="/overview"
                className="inline-flex items-center rounded-md bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground transition hover:bg-primary/90 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
              >
                API Reference
              </Link>
              <a
                href={openApiDocumentUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center rounded-md border border-[#cbd5e1] bg-white px-4 py-2.5 text-sm font-semibold text-[#1e293b] transition hover:border-[#64748b] hover:text-[#0f172a] focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-[#93c5fd] focus-visible:ring-offset-2 dark:border-[#334155] dark:bg-black dark:text-[#cbd5e1] dark:hover:border-[#64748b] dark:hover:text-white"
              >
                OpenAPI Document
              </a>
            </div>
          </div>
        </div>

        <section className="mt-8 grid gap-4 md:grid-cols-2">
          <article className="rounded-xl border border-border bg-white p-5 shadow-[0_10px_25px_-20px_rgba(15,23,42,0.45)] dark:border-[#333333] dark:bg-black">
            <h2 className="inline-flex items-center gap-2 text-base font-semibold text-[#111827]">
              <span className="inline-flex h-7 w-7 items-center justify-center rounded-md bg-[#e2e8f0] text-[#0f172a] dark:bg-[#334155] dark:text-[#f3f4f6]">
                <Boxes className="h-4 w-4" />
              </span>
              <span className="dark:text-[#f3f4f6]">Core Components</span>
            </h2>
            <p className="mt-2 text-sm leading-6 text-[#4b5563] dark:text-[#94a3b8]">
              Use core-component endpoints to inspect ACCs, ASCCPs, and BCCPs as the canonical, context-independent
              model artifacts in a release.
            </p>
          </article>
          <article className="rounded-xl border border-border bg-white p-5 shadow-[0_10px_25px_-20px_rgba(15,23,42,0.45)] dark:border-[#333333] dark:bg-black">
            <h2 className="inline-flex items-center gap-2 text-base font-semibold text-[#111827]">
              <span className="inline-flex h-7 w-7 items-center justify-center rounded-md bg-[#e2e8f0] text-[#0f172a] dark:bg-[#334155] dark:text-[#f3f4f6]">
                <GitBranch className="h-4 w-4" />
              </span>
              <span className="dark:text-[#f3f4f6]">Business Information Entities</span>
            </h2>
            <p className="mt-2 text-sm leading-6 text-[#4b5563] dark:text-[#94a3b8]">
              Traverse top-level BIEs, profile ASBIE and BBIE content, manage reuse, and apply value-domain
              restrictions in the same tree structure described in the user guide.
            </p>
          </article>
          <article className="rounded-xl border border-border bg-white p-5 shadow-[0_10px_25px_-20px_rgba(15,23,42,0.45)] dark:border-[#333333] dark:bg-black">
            <h2 className="inline-flex items-center gap-2 text-base font-semibold text-[#111827]">
              <span className="inline-flex h-7 w-7 items-center justify-center rounded-md bg-[#e2e8f0] text-[#0f172a] dark:bg-[#334155] dark:text-[#f3f4f6]">
                <Tags className="h-4 w-4" />
              </span>
              <span className="dark:text-[#f3f4f6]">Context Management</span>
            </h2>
            <p className="mt-2 text-sm leading-6 text-[#4b5563] dark:text-[#94a3b8]">
              Create Context Categories, Context Schemes, and Business Contexts in dependency order before starting a
              new top-level BIE profile.
            </p>
          </article>
          <article className="rounded-xl border border-border bg-white p-5 shadow-[0_10px_25px_-20px_rgba(15,23,42,0.45)] dark:border-[#333333] dark:bg-black">
            <h2 className="inline-flex items-center gap-2 text-base font-semibold text-[#111827]">
              <span className="inline-flex h-7 w-7 items-center justify-center rounded-md bg-[#e2e8f0] text-[#0f172a] dark:bg-[#334155] dark:text-[#f3f4f6]">
                <Braces className="h-4 w-4" />
              </span>
              <span className="dark:text-[#f3f4f6]">Model Expressions</span>
            </h2>
            <p className="mt-2 text-sm leading-6 text-[#4b5563] dark:text-[#94a3b8]">
              connectCenter profiles can be exported or represented in formats used by implementation teams, including{' '}
              <a
                href="https://www.w3.org/XML/Schema"
                target="_blank"
                rel="noreferrer"
                className="font-medium text-[#111827] underline decoration-[#94a3b8] underline-offset-4 hover:decoration-[#111827] dark:text-[#e2e8f0] dark:hover:decoration-[#e2e8f0]"
              >
                XML Schema
              </a>
              , {' '}
              <a
                href="https://json-schema.org/"
                target="_blank"
                rel="noreferrer"
                className="font-medium text-[#111827] underline decoration-[#94a3b8] underline-offset-4 hover:decoration-[#111827] dark:text-[#e2e8f0] dark:hover:decoration-[#e2e8f0]"
              >
                JSON Schema
              </a>
              ,{' '}
              <a
                href="https://spec.openapis.org/oas"
                target="_blank"
                rel="noreferrer"
                className="font-medium text-[#111827] underline decoration-[#94a3b8] underline-offset-4 hover:decoration-[#111827] dark:text-[#e2e8f0] dark:hover:decoration-[#e2e8f0]"
              >
                OpenAPI v3
              </a>
              ,{' '}
              <a
                href="https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=office"
                target="_blank"
                rel="noreferrer"
                className="font-medium text-[#111827] underline decoration-[#94a3b8] underline-offset-4 hover:decoration-[#111827] dark:text-[#e2e8f0] dark:hover:decoration-[#e2e8f0]"
              >
                Open Document Format
              </a>
              , and{' '}
              <a
                href="https://avro.apache.org/docs/"
                target="_blank"
                rel="noreferrer"
                className="font-medium text-[#111827] underline decoration-[#94a3b8] underline-offset-4 hover:decoration-[#111827] dark:text-[#e2e8f0] dark:hover:decoration-[#e2e8f0]"
              >
                Avro Schema
              </a>
              .
            </p>
          </article>
        </section>
      </div>
    </main>
  );
}
