'use client';

import { useEffect, useState } from 'react';
import { resolveBackendApiBase } from '@/lib/openapi';

type AuthenticationDoc = {
  title?: string;
  scheme?: string;
  description?: string;
  how_to?: string[];
  curl_example?: string;
  api_base_url?: string;
};

type Props = {
  authentication?: AuthenticationDoc;
};

type ShellCodeBlockProps = {
  code: string;
  title?: string;
  className?: string;
};

const LOOPBACK_API_BASE_PATTERN = /https?:\/\/(?:127\.0\.0\.1|localhost):5555(?:\/api)?/i;

function shellTokenClass(token: string): string {
  if (token.startsWith('${') || /^\$[A-Z_][A-Z0-9_]*$/.test(token)) return 'syntax-token-env';
  if (/^https?:\/\//.test(token)) return 'syntax-token-url';
  if (/^"(?:[^"\\]|\\.)*"$/.test(token) || /^'(?:[^'\\]|\\.)*'$/.test(token)) return 'syntax-token-string';
  if (/^--?[a-zA-Z][\w-]*$/.test(token)) return 'syntax-token-option';
  if (/^(curl|export|jq)$/.test(token)) return token === 'export' ? 'syntax-token-keyword' : 'syntax-token-method';
  if (/^\d+$/.test(token)) return 'syntax-token-number';
  if (/^[|\\{}[\](),:]$/.test(token)) return 'syntax-token-punct';
  return 'syntax-token-plain';
}

function tokenizeShellLine(line: string): Array<{ text: string; className: string }> {
  const pattern =
    /(\$\{[^}]+\}|\$[A-Z_][A-Z0-9_]*|https?:\/\/[^\s"']+|"(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'|--?[a-zA-Z][\w-]*|\b(?:curl|export|jq)\b|\b\d+\b|[|\\{}[\](),:])/g;

  const out: Array<{ text: string; className: string }> = [];
  let last = 0;
  let match: RegExpExecArray | null;

  while ((match = pattern.exec(line)) !== null) {
    const start = match.index;
    if (start > last) {
      out.push({ text: line.slice(last, start), className: 'syntax-token-plain' });
    }
    const token = match[0];
    out.push({ text: token, className: shellTokenClass(token) });
    last = start + token.length;
  }
  if (last < line.length) {
    out.push({ text: line.slice(last), className: 'syntax-token-plain' });
  }
  if (out.length === 0) {
    out.push({ text: '', className: 'syntax-token-plain' });
  }
  return out;
}

function ShellCodeBlock({ code, title = 'cURL', className = 'mt-2' }: ShellCodeBlockProps) {
  const lines = code.replace(/\r\n/g, '\n').split('\n');
  return (
    <div className={`${className} rounded-xl border border-border bg-white shadow-[0_18px_35px_-30px_rgba(15,23,42,0.7)]`}>
      <div className="flex items-center justify-between gap-2 border-b border-border/80 px-4 py-3">
        <div className="text-sm font-semibold text-[#111827] dark:text-[#f3f4f6]">{title}</div>
      </div>
      <div className="syntax-highlight overflow-auto rounded-b-xl bg-[#f8fafc] p-4 dark:bg-[#0b1220]">
        <pre className="m-0 text-xs leading-6">
          <code>
            {lines.map((line, index) => (
              <span key={`${index}-${line}`} className="grid grid-cols-[2.5rem_minmax(0,1fr)] items-start gap-3">
                <span className="syntax-line-number select-none text-right">{index + 1}</span>
                <span className="min-w-0 whitespace-pre-wrap break-words">
                  {tokenizeShellLine(line).map((token, tokenIndex) => (
                    <span key={`${index}-${tokenIndex}`} className={token.className}>
                      {token.text || '\u00A0'}
                    </span>
                  ))}
                </span>
              </span>
            ))}
          </code>
        </pre>
      </div>
    </div>
  );
}

function normalizeApiBaseUrl(raw: string | undefined): string {
  const fallback = resolveBackendApiBase();
  const value = (raw ?? '').trim();
  if (!value) {
    return fallback;
  }
  return value.replace(/\/+$/, '');
}

function usesLoopbackApiBase(raw: string | undefined): boolean {
  return LOOPBACK_API_BASE_PATTERN.test((raw ?? '').trim());
}

function replaceLoopbackApiBase(raw: string | undefined, apiBaseUrl: string): string {
  const value = (raw ?? '').trim();
  if (!value) {
    return value;
  }
  return value.replace(LOOPBACK_API_BASE_PATTERN, apiBaseUrl);
}

function useResolvedApiBaseUrl(raw: string | undefined): string {
  const [apiBaseUrl, setApiBaseUrl] = useState(() => normalizeApiBaseUrl(raw));

  useEffect(() => {
    if (!usesLoopbackApiBase(raw)) {
      return;
    }
    // Resolve the loopback API base from the live browser location at runtime;
    // this depends on window and cannot be computed during SSR. The functional
    // update is a no-op when unchanged, so it does not cause cascading renders.
    const runtimeApiBase = resolveBackendApiBase();
    // eslint-disable-next-line react-hooks/set-state-in-effect -- runtime window-based resolution, guarded to a single state change
    setApiBaseUrl((current) => (current === runtimeApiBase ? current : runtimeApiBase));
  }, [raw]);

  return apiBaseUrl;
}

function getOpenApiUrl(apiBaseUrl: string): string {
  return `${apiBaseUrl}/openapi.json`;
}

function getApiPath(apiBaseUrl: string): string {
  try {
    const parsed = new URL(apiBaseUrl);
    return parsed.pathname || '/';
  } catch {
    const marker = apiBaseUrl.indexOf('://');
    if (marker >= 0) {
      const firstSlash = apiBaseUrl.indexOf('/', marker + 3);
      if (firstSlash >= 0) {
        return apiBaseUrl.slice(firstSlash) || '/';
      }
      return '/';
    }
    return apiBaseUrl.startsWith('/') ? apiBaseUrl : `/${apiBaseUrl}`;
  }
}

function IntroductionSection({ apiBaseUrl }: { apiBaseUrl: string }) {
  const openApiUrl = getOpenApiUrl(apiBaseUrl);

  return (
    <section id="introduction" className="mb-5 scroll-mt-4 rounded-xl border border-border bg-white px-5 py-5 shadow-[0_22px_50px_-38px_rgba(15,23,42,0.6)]">
      <h2 className="text-xl font-semibold text-[#0f172a]">Introduction</h2>
      <p className="mt-3 text-sm leading-7 text-[#374151]">
        The connectCenter API is a REST interface for managing platform resources such as context categories.
        All endpoints return JSON and use standard HTTP status codes.
      </p>
      <p className="mt-3 text-sm leading-7 text-[#374151]">
        Base URL: <code className="rounded bg-[#f3f4f6] px-1.5 py-0.5 text-[12px]">{apiBaseUrl}</code>
      </p>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        You can get the complete OpenAPI documentation for these APIs at{' '}
        <a
          href={openApiUrl}
          target="_blank"
          rel="noreferrer"
          className="font-medium text-[#2563eb] underline decoration-[#93c5fd] underline-offset-4 hover:decoration-[#2563eb] dark:text-[#93c5fd] dark:decoration-[#3b82f6] dark:hover:decoration-[#93c5fd]"
        >
          {openApiUrl}
        </a>
        .
      </p>
      <ul className="mt-4 list-disc space-y-1 pl-5 text-sm text-[#4b5563]">
        <li>Read reference data from endpoints such as <code>/libraries</code>, <code>/releases</code>, <code>/namespaces</code>, and <code>/core-components</code>.</li>
        <li>Create, update, and delete resources via <code>/context-categories</code>, <code>/context-schemes</code>, <code>/business-contexts</code>, and <code>/business-information-entities</code>.</li>
        <li>Send JSON request bodies with <code>Content-Type: application/json</code>.</li>
        <li>Use OpenID Bearer tokens as the primary authentication method.</li>
        <li>Use HTTP Basic auth as fallback when Bearer token authentication is unavailable.</li>
      </ul>

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Request Format</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Send request payloads as UTF-8 JSON. Field names use snake_case to match database-backed resources.
        For update operations, only editable fields should be provided. Unknown fields are ignored or rejected
        depending on endpoint validation rules.
      </p>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Required headers for most write operations:
      </p>
      <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-[#4b5563]">
        <li><code>Authorization: Bearer &lt;access_token&gt;</code> (preferred)</li>
        <li><code>Authorization: Basic &lt;base64(login_id:password)&gt;</code> (fallback)</li>
        <li><code>Content-Type: application/json</code></li>
        <li><code>Accept: application/json</code></li>
      </ul>

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Response Format</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Successful responses return JSON objects for single-resource calls and JSON arrays for list endpoints.
        Timestamps are returned in UTC ISO-8601 format. Boolean flags are returned as JSON booleans.
      </p>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Typical status codes:
      </p>
      <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-[#4b5563]">
        <li><code>200 OK</code>: Read or update succeeded.</li>
        <li><code>201 Created</code>: Create succeeded.</li>
        <li><code>204 No Content</code>: Delete succeeded without body.</li>
        <li><code>400 Bad Request</code>: Validation failed.</li>
        <li><code>401 Unauthorized</code>: Missing or invalid credentials.</li>
        <li><code>404 Not Found</code>: Resource does not exist.</li>
      </ul>

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Pagination</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        List endpoints support offset pagination using <code>offset</code> and <code>limit</code>. Start with{' '}
        <code>offset=0</code> and increase by the previous <code>limit</code> value to retrieve the next page.
        Keep <code>limit</code> moderate for stable performance.
      </p>
      <ShellCodeBlock
        className="mt-3"
        title="Pagination Example"
        code={`curl -s \\
  "${apiBaseUrl}/libraries?offset=0&limit=20" \\
  -H "Authorization: Bearer $ACCESS_TOKEN"`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Error Handling</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Errors are returned as JSON. Authentication failures return <code>401</code>. Validation errors return
        {' '}<code>400</code> with details about the invalid fields. For server-side failures, retry only when safe,
        and log request identifiers if available.
      </p>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Client applications should treat non-2xx status codes as failed operations and surface clear messages to
        users when writing data.
      </p>

    </section>
  );
}

function GettingStartedSection({ apiBaseUrl }: { apiBaseUrl: string }) {
  return (
    <section id="getting-started" className="mb-5 scroll-mt-4 rounded-xl border border-border bg-white px-5 py-5 shadow-[0_22px_50px_-38px_rgba(15,23,42,0.6)]">
      <h2 className="text-xl font-semibold text-[#0f172a]">Getting Started</h2>
      <p className="mt-3 text-sm leading-7 text-[#374151]">
        This walkthrough shows real-world OAGIS BOM profiling practice. You will take the canonical OAGIS
        {' '}<code>BOM</code> model and define a constrained profile for one use case.
      </p>

      <h3 className="mt-8 text-base font-semibold text-[#111827]">What You Are Building</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        You will create an example profile named <code>Automotive mBOM – ISA-95 Production Execution</code> with:
      </p>
      <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-[#4b5563]">
        <li>One release to read from</li>
        <li>Context categories for industry, business process, and BOM lifecycle stage</li>
        <li>Context schemes and values for Automotive (AIAG) + ISA-95 Production Execution + Manufacturing BOM</li>
        <li>One business context combining those values into a reusable profile identity</li>
        <li>A BIE tree profiling the canonical BOM model with required and optional nodes</li>
      </ul>

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Quick Setup</h3>
      <ShellCodeBlock
        className="mt-3"
        title="Set Access Token"
        code={`export ACCESS_TOKEN='YOUR_BEARER_TOKEN'`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Step 1) Find the library</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        First, confirm the library exists.
      </p>
      <ShellCodeBlock
        title="Find Library"
        code={`curl -s \\
  "${apiBaseUrl}/libraries?name=connectSpec&offset=0&limit=20" \\
  -H "Authorization: Bearer $ACCESS_TOKEN"`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Find Library Result"
        code={`{
  "total_items": 1,
  "offset": 0,
  "limit": 20,
  "items": [
    {
      "library_id": 3,
      "name": "connectSpec"
    }
  ]
}`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Step 2) Pick a release</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Choose the release you want to build on (example: <code>10.12</code>).
      </p>
      <ShellCodeBlock
        title="Find Release"
        code={`curl -s \\
  "${apiBaseUrl}/releases?release_num=10.12&offset=0&limit=20" \\
  -H "Authorization: Bearer $ACCESS_TOKEN"`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Find Release Result"
        code={`{
  "total_items": 8,
  "offset": 0,
  "limit": 20,
  "items": [
    {
      "release_id": 69,
      "release_num": "10.12.7"
    }
  ]
}`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Step 3) Create context categories</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Three categories define the profiling dimensions: the industry vertical, the manufacturing process, and the BOM lifecycle stage.
      </p>
      <ShellCodeBlock
        title="Create Category: Industry"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/context-categories" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"name":"Industry","description":"Industry vertical for which this BOM profile applies"}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create Category: Industry Result"
        code={`{
  "context_category_id": 16
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Create Category: Business Process"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/context-categories" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"name":"Business Process","description":"Manufacturing business process context per ISA-95"}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create Category: Business Process Result"
        code={`{
  "context_category_id": 17
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Create Category: BOM Lifecycle Stage"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/context-categories" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"name":"BOM Lifecycle Stage","description":"BOM type and lifecycle stage in product development"}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create Category: BOM Lifecycle Stage Result"
        code={`{
  "context_category_id": 18
}`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Step 4) Add schemes and values</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Create one scheme per category and add one value to each.
      </p>
      <ShellCodeBlock
        title="Create Scheme: Industry"
        code={`# Category: Industry (replace <CATEGORY_ID_INDUSTRY>)
curl -s -X POST \\
  "${apiBaseUrl}/context-schemes" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"ctx_category_id":<CATEGORY_ID_INDUSTRY>,"scheme_id":"Industry","scheme_name":"AIAG Industry Classification","scheme_agency_id":"AIAG","scheme_version_id":"3.0"}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create Scheme: Industry Result"
        code={`{
  "ctx_scheme_id": 9
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Create Value: Automotive"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/context-schemes/<SCHEME_ID_INDUSTRY>/values" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"value":"Automotive","meaning":"Automotive manufacturing and supply chain (AIAG)"}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create Value: Automotive Result"
        code={`{
  "ctx_scheme_value_id": 10,
  "value": "Automotive"
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Create Scheme: Business Process"
        code={`# Category: Business Process (replace <CATEGORY_ID_PROCESS>)
curl -s -X POST \\
  "${apiBaseUrl}/context-schemes" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"ctx_category_id":<CATEGORY_ID_PROCESS>,"scheme_id":"ManufacturingProcess","scheme_name":"ISA-95 Manufacturing Operations","scheme_agency_id":"ISA","scheme_version_id":"2.0"}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create Scheme: Business Process Result"
        code={`{
  "ctx_scheme_id": 10
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Create Value: Production Execution"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/context-schemes/<SCHEME_ID_PROCESS>/values" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"value":"ProductionExecution","meaning":"Production execution per ISA-95 Level 3, ERP-to-MES BOM synchronization"}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create Value: Production Execution Result"
        code={`{
  "ctx_scheme_value_id": 11,
  "value": "ProductionExecution"
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Create Scheme: BOM Lifecycle Stage"
        code={`# Category: BOM Lifecycle Stage (replace <CATEGORY_ID_BOM>)
curl -s -X POST \\
  "${apiBaseUrl}/context-schemes" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"ctx_category_id":<CATEGORY_ID_BOM>,"scheme_id":"BOMLifecycle","scheme_name":"BOM Lifecycle Stage","scheme_agency_id":"OAGi","scheme_version_id":"10.12"}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create Scheme: BOM Lifecycle Stage Result"
        code={`{
  "ctx_scheme_id": 11
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Create Value: Manufacturing BOM"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/context-schemes/<SCHEME_ID_BOM>/values" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"value":"ManufacturingBOM","meaning":"As-planned manufacturing bill of materials for production execution"}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create Value: Manufacturing BOM Result"
        code={`{
  "ctx_scheme_value_id": 12,
  "value": "ManufacturingBOM"
}`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Step 5) Create one business context</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        This combines the three values into one reusable profile identity.
      </p>
      <ShellCodeBlock
        title="Create Business Context"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/business-contexts" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"name":"Automotive mBOM – ISA-95 Production Execution"}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create Business Context Result"
        code={`{
  "biz_ctx_id": 8
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Attach Value: Automotive"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/business-contexts/<BIZ_CTX_ID>/values" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"ctx_scheme_value_id":<VALUE_ID_INDUSTRY>}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Attach Value: Automotive Result"
        code={`{
  "biz_ctx_value_id": 9
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Attach Value: Production Execution"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/business-contexts/<BIZ_CTX_ID>/values" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"ctx_scheme_value_id":<VALUE_ID_PROCESS>}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Attach Value: Production Execution Result"
        code={`{
  "biz_ctx_value_id": 10
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Attach Value: Manufacturing BOM"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/business-contexts/<BIZ_CTX_ID>/values" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"ctx_scheme_value_id":<VALUE_ID_BOM>}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Attach Value: Manufacturing BOM Result"
        code={`{
  "biz_ctx_value_id": 11
}`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Step 6) Find the BOM ASCCP manifest</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Look up the ASCCP for the canonical BOM component in the release you picked. The manifest ID is needed to create the BIE.
      </p>
      <ShellCodeBlock
        title="Find BOM ASCCP"
        code={`curl -s \\
  "${apiBaseUrl}/core-components?release_id=<RELEASE_ID>&types=ASCCP&den=BOM.+BOM&offset=0&limit=10" \\
  -H "Authorization: Bearer $ACCESS_TOKEN"`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Find BOM ASCCP Result"
        code={`{
  "total_items": 1,
  "offset": 0,
  "limit": 10,
  "items": [
    {
      "component_type": "ASCCP",
      "manifest_id": 688111,
      "den": "BOM. BOM",
      "definition": "Bill of Material"
    }
  ]
}`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Step 7) Create a Top-Level ASBIEP</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Instantiate the BOM ASCCP as a Business Information Entity (BIE) and assign the business context created in Step 5.
        The response includes <code>top_level_asbiep_id</code>, the root <code>abie_id</code>, and the full set of
        relationships pre-populated from the canonical BOM model. Every relationship starts with <code>is_used: false</code>{' '}
        and a <code>null</code> ID — you activate them in the next steps.
      </p>
      <ShellCodeBlock
        title="Create Top-Level ASBIEP"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/business-information-entities" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"asccp_manifest_id":688111,"biz_ctx_list":[<BIZ_CTX_ID>]}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create Top-Level ASBIEP Result"
        code={`{
  "top_level_asbiep_id": 5,
  "state": "WIP",
  "asbiep": {
    "asbiep_id": 9,
    "based_asccp_manifest": { "asccp_manifest_id": 688111, "den": "BOM. BOM" },
    "role_of_abie": {
      "abie_id": 9,
      "relationships": [
        {
          "component_type": "BBIE",
          "bbie_id": null,
          "based_bcc": { "bcc_manifest_id": 372918, "den": "BOM. Type Code. Code" },
          "is_used": false
        },
        {
          "component_type": "BBIE",
          "bbie_id": null,
          "based_bcc": { "bcc_manifest_id": 372919, "den": "BOM. Action Code. Action Code Content_ Code" },
          "is_used": false
        },
        {
          "component_type": "ASBIE",
          "asbie_id": null,
          "based_ascc": { "ascc_manifest_id": 772588, "den": "BOM. BOM Header. BOM Header" },
          "is_used": false
        },
        {
          "component_type": "ASBIE",
          "asbie_id": null,
          "based_ascc": { "ascc_manifest_id": 772596, "den": "BOM. BOM Item Data. BOM Item Data" },
          "is_used": false
        }
      ]
    }
  }
}`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Step 8) Profile association nodes (ASBIE)</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Create an ASBIE for each association you want to include in the profile, then set its usage and cardinality.
        Use <code>from_abie_id</code> from the BIE tree (the root <code>abie_id</code> from Step 7) and the
        {' '}<code>based_ascc_manifest_id</code> from the relationship list. For example, activate the BOM Header
        association using <code>ascc_manifest_id: 772588</code>.
      </p>
      <ShellCodeBlock
        title="Create ASBIE (BOM Header)"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/business-information-entities/<TOP_LEVEL_ASBIEP_ID>/asbies" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"from_abie_id":<ABIE_ID>,"based_ascc_manifest_id":772588}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create ASBIE Result"
        code={`{
  "asbie_id": 301,
  "asbiep": {
    "asbiep_id": 121,
    "based_asccp_manifest": { "den": "BOM Header. BOM Header" },
    "role_of_abie": { "abie_id": 201 }
  }
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Mark ASBIE as Required"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/business-information-entities/<TOP_LEVEL_ASBIEP_ID>/asbies/<ASBIE_ID>" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"is_used":true,"cardinality_min":1,"cardinality_max":1}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Mark ASBIE as Required Result"
        code={`{
  "asbie_id": 301,
  "updates": ["is_used", "cardinality_min", "cardinality_max"]
}`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Step 9) Profile field nodes (BBIE)</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Create a BBIE for each data field you want to include, then set its usage and cardinality.
        Use <code>from_abie_id</code> of the parent ABIE (the root or a nested one from Step 8) and the
        {' '}<code>based_bcc_manifest_id</code> from the relationship list. For example, activate the Type Code
        field on the root BOM ABIE using <code>bcc_manifest_id: 372918</code>.
      </p>
      <ShellCodeBlock
        title="Create BBIE (BOM Type Code)"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/business-information-entities/<TOP_LEVEL_ASBIEP_ID>/bbies" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"from_abie_id":<ABIE_ID>,"based_bcc_manifest_id":372918}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create BBIE Result"
        code={`{
  "bbie_id": 401,
  "bbiep": {
    "bbiep_id": 501,
    "based_bccp_manifest": { "den": "Type Code. Code" }
  }
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Mark BBIE as Required"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/business-information-entities/<TOP_LEVEL_ASBIEP_ID>/bbies/<BBIE_ID>" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"is_used":true,"cardinality_min":1}'`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Add BBIE Example and Primitive Restriction"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/business-information-entities/<TOP_LEVEL_ASBIEP_ID>/bbies/<BBIE_ID>" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"definition":"Primary BOM type code.","example":"MBOM","xbt_manifest_id":13353}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Mark BBIE as Required Result"
        code={`{
  "bbie_id": 401,
  "updates": ["is_used", "cardinality_min"]
}`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Step 10) Add supplementary components (BBIE_SC)</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Optionally add BBIE supplementary components to constrain facets or data type attributes on a BBIE.
        Use the <code>based_dt_sc_manifest_id</code> from the data type details.
      </p>
      <ShellCodeBlock
        title="Create BBIE_SC"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/business-information-entities/bbie-scs" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"bbie_id":<BBIE_ID>,"based_dt_sc_manifest_id":<DT_SC_MANIFEST_ID>}'`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Create BBIE_SC Result"
        code={`{
  "bbie_sc_id": 501,
  "updates": ["bbie_sc_id"]
}`}
      />
      <ShellCodeBlock
        className="mt-3"
        title="Update BBIE_SC Definition, Example, and Remark"
        code={`curl -s -X POST \\
  "${apiBaseUrl}/business-information-entities/bbie-scs/<BBIE_SC_ID>" \\
  -H "Authorization: Bearer $ACCESS_TOKEN" \\
  -H "Content-Type: application/json" \\
  -d '{"definition":"Language code for the BOM text.","example":"en","remark":"Supports multilingual content."}'`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Step 11) Verify the complete BIE profile</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        Read the top-level ASBIEP back and confirm the business context, ASBIEs, and BBIEs are all attached correctly.
      </p>
      <ShellCodeBlock
        title="Retrieve Top-Level ASBIEP"
        code={`curl -s \\
  "${apiBaseUrl}/business-information-entities/<TOP_LEVEL_ASBIEP_ID>" \\
  -H "Authorization: Bearer $ACCESS_TOKEN"`}
      />
      <ShellCodeBlock
        className="mt-2"
        title="Retrieve Top-Level ASBIEP Result"
        code={`{
  "top_level_asbiep_id": 5,
  "state": "WIP",
  "is_deprecated": false,
  "business_contexts": [
    { "biz_ctx_id": 8, "name": "Automotive mBOM – ISA-95 Production Execution" }
  ],
  "asbiep": {
    "asbiep_id": 9,
    "based_asccp_manifest": { "asccp_manifest_id": 688111, "den": "BOM. BOM" },
    "role_of_abie": {
      "abie_id": 9,
      "relationships": [
        {
          "component_type": "BBIE",
          "bbie_id": 401,
          "based_bcc": { "bcc_manifest_id": 372918, "den": "BOM. Type Code. Code" },
          "is_used": true,
          "cardinality_min": 1,
          "cardinality_max": 1
        },
        {
          "component_type": "BBIE",
          "bbie_id": null,
          "based_bcc": { "bcc_manifest_id": 372919, "den": "BOM. Action Code. Action Code Content_ Code" },
          "is_used": false,
          "cardinality_min": 0,
          "cardinality_max": 1
        },
        {
          "component_type": "ASBIE",
          "asbie_id": 301,
          "based_ascc": { "ascc_manifest_id": 772588, "den": "BOM. BOM Header. BOM Header" },
          "is_used": true,
          "cardinality_min": 1,
          "cardinality_max": 1
        },
        {
          "component_type": "ASBIE",
          "asbie_id": null,
          "based_ascc": { "ascc_manifest_id": 772596, "den": "BOM. BOM Item Data. BOM Item Data" },
          "is_used": false,
          "cardinality_min": 0,
          "cardinality_max": -1
        }
      ]
    }
  }
}`}
      />

      <p className="mt-4 text-sm leading-7 text-[#374151]">
        You now have a complete OAGIS BOM BIE profile scoped to the Automotive mBOM – ISA-95 Production Execution business context.
        Repeat Steps 8–10 for each additional association or field you want to include, and iterate the business context
        for other variants such as Engineering BOM (eBOM), Configurable BOM (cBOM), or As-Built BOM.
      </p>
    </section>
  );
}

function AuthenticationSection({ auth, apiBaseUrl }: { auth?: AuthenticationDoc; apiBaseUrl: string }) {
  const whoAmIUrl = `${apiBaseUrl}/me`;
  const rawApiPath = getApiPath(apiBaseUrl);
  const apiPathPrefix = rawApiPath === '/' ? '' : rawApiPath.replace(/\/+$/, '');
  const displayApiPath = apiPathPrefix || '/';
  const usersMePath = `${apiPathPrefix}/me`;
  const bearerCurlExample = replaceLoopbackApiBase(auth?.curl_example, apiBaseUrl) || `curl -H "Authorization: Bearer ACCESS_TOKEN" ${whoAmIUrl}`;

  return (
    <section id="authentication" className="scroll-mt-4 rounded-xl border border-border bg-white px-5 py-5 shadow-[0_22px_50px_-38px_rgba(15,23,42,0.6)]">
      <h2 className="text-xl font-semibold text-[#0f172a]">{auth?.title ?? 'Authentication'}</h2>
      <p className="mt-1 text-sm font-medium text-[#6b7280]">
        {auth?.scheme ?? 'OpenID Connect Bearer (preferred) + HTTP Basic (fallback)'}
      </p>

      <p className="mt-4 text-sm leading-7 text-[#374151]">
        {auth?.description ??
          'The connectCenter API uses OAuth 2.0 / OpenID Connect Bearer tokens as the primary authentication method. HTTP Basic authentication is supported as a fallback.'}
      </p>

      <ul className="mt-4 list-disc space-y-1 pl-5 text-sm text-[#4b5563]">
        {(auth?.how_to ?? [
          'Use Authorization: Bearer ACCESS_TOKEN whenever possible.',
          'If a bearer token is not available, use HTTP Basic credentials.',
          'Authentication is required for API endpoints, including who_am_i.',
        ]).map((step) => (
          <li key={step}>{step}</li>
        ))}
      </ul>

      <ShellCodeBlock
        className="mt-5"
        title="Bearer Auth cURL"
        code={bearerCurlExample}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Bearer Token Verification</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        API bearer tokens are validated against the configured OpenID Connect issuer. The token must be structurally
        valid, not expired, and acceptable for the configured audience before the request is allowed.
      </p>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        The API uses issuer metadata and published signing keys to verify the bearer token, then links the verified
        identity to a connectCenter user account before the request proceeds.
      </p>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        In practical terms, API callers only need to send a valid bearer token in the Authorization header. The
        verification and account-linking steps happen inside the service.
      </p>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        HTTP Basic fallback is intended for compatibility and development workflows where bearer tokens are not yet
        available.
      </p>

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Header Examples</h3>
      <div className="mt-3 rounded-lg border border-border bg-[#f8fafc] p-4">
        <pre className="overflow-x-auto text-xs leading-6 text-[#334155]">
          <code>{`Authorization: Bearer eyJhbGciOi...<access_token>`}</code>
        </pre>
      </div>
      <p className="mt-2 text-xs text-[#6b7280]">
        Use Bearer token authentication first. If needed, use Basic fallback:
      </p>
      <ShellCodeBlock
        className="mt-3"
        title="Basic Auth cURL"
        code={`curl -u "LOGIN_ID:PASSWORD" ${whoAmIUrl}`}
      />

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Basic Fallback Notes</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        If your password contains shell-sensitive characters such as <code>!</code>, <code>$</code>, or quotes, wrap
        the full credential string in single quotes when generating the Basic credential value.
      </p>
      <ShellCodeBlock
        className="mt-3"
        title="Escaped Basic Auth cURL"
        code={`curl -H "Authorization: Basic $(printf '%s' 'LOGIN_ID:MyP@ssw0rd!2026' | base64)" ${whoAmIUrl}`}
      />
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        In Bash, unquoted <code>!</code> can trigger history expansion and cause errors like
        {' '}<code>event not found</code>.
      </p>

      <h3 className="mt-8 text-base font-semibold text-[#111827]">When Authentication Is Required</h3>
      <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-[#4b5563]">
        <li>All API endpoints under <code>{displayApiPath}</code> require authentication.</li>
        <li>This includes account endpoints such as <code>{`GET ${usersMePath}`}</code> (<code>who_am_i</code>).</li>
        <li>Use Bearer tokens as the default approach for every endpoint.</li>
      </ul>

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Common Failures</h3>
      <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-[#4b5563]">
        <li><code>401 Unauthorized</code>: missing credentials.</li>
        <li><code>401 Unauthorized</code>: invalid or unlinked OpenID Bearer token.</li>
        <li><code>401 Unauthorized</code>: invalid Basic login ID or password.</li>
      </ul>
      <div className="mt-3 rounded-lg border border-border bg-[#f8fafc] p-4">
        <pre className="overflow-x-auto text-xs leading-6 text-[#334155]">
          <code>{`HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer, Basic
Content-Type: application/json

{"cause":"Authentication failed","message":"Authentication is required. Provide an OpenID Bearer token or Basic credentials."}`}</code>
        </pre>
      </div>

      <h3 className="mt-8 text-base font-semibold text-[#111827]">Learn More</h3>
      <p className="mt-2 text-sm leading-7 text-[#374151]">
        For protocol details, refer to OAuth 2.0, Bearer Token usage, OpenID Connect Core, and OpenID Discovery:
      </p>
      <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-[#4b5563]">
        <li>
          <a
            href="https://datatracker.ietf.org/doc/html/rfc6749"
            target="_blank"
            rel="noreferrer"
            className="text-[#2563eb] hover:underline"
          >
            OAuth 2.0 Authorization Framework (RFC 6749)
          </a>
        </li>
        <li>
          <a
            href="https://datatracker.ietf.org/doc/html/rfc6750"
            target="_blank"
            rel="noreferrer"
            className="text-[#2563eb] hover:underline"
          >
            OAuth 2.0 Bearer Token Usage (RFC 6750)
          </a>
        </li>
        <li>
          <a
            href="https://openid.net/specs/openid-connect-core-1_0.html"
            target="_blank"
            rel="noreferrer"
            className="text-[#2563eb] hover:underline"
          >
            OpenID Connect Core 1.0
          </a>
        </li>
        <li>
          <a
            href="https://openid.net/specs/openid-connect-discovery-1_0.html"
            target="_blank"
            rel="noreferrer"
            className="text-[#2563eb] hover:underline"
          >
            OpenID Connect Discovery 1.0
          </a>
        </li>
        <li>
          <a
            href="https://datatracker.ietf.org/doc/html/rfc7617"
            target="_blank"
            rel="noreferrer"
            className="text-[#2563eb] hover:underline"
          >
            HTTP Basic Authentication (RFC 7617)
          </a>
        </li>
      </ul>

    </section>
  );
}

export function ReferenceOverviewContent({ authentication }: Props) {
  const apiBaseUrl = useResolvedApiBaseUrl(authentication?.api_base_url);

  useEffect(() => {
    const scrollToHashTarget = () => {
      const hash = window.location.hash || '';
      if (!hash || hash === '#introduction') {
        return;
      }

      const root = document.getElementById('reference-overview-scroll');
      const target = document.getElementById(hash.replace('#', ''));
      if (!root || !target) {
        return;
      }

      const targetTop =
        target.getBoundingClientRect().top - root.getBoundingClientRect().top + root.scrollTop;
      root.scrollTo({ top: Math.max(0, targetTop - 12), behavior: 'auto' });
    };

    // Retry once after layout settles for direct hash entries.
    scrollToHashTarget();
    const retryId = window.setTimeout(scrollToHashTarget, 50);
    window.addEventListener('hashchange', scrollToHashTarget);
    return () => {
      window.clearTimeout(retryId);
      window.removeEventListener('hashchange', scrollToHashTarget);
    };
  }, []);

  return (
    <>
      <IntroductionSection apiBaseUrl={apiBaseUrl} />
      <GettingStartedSection apiBaseUrl={apiBaseUrl} />
      <AuthenticationSection auth={authentication} apiBaseUrl={apiBaseUrl} />
    </>
  );
}
