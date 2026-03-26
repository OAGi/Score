'use client';

import { useMemo, useState } from 'react';

import { Button } from '@/components/ui/button';
import { MethodDoc } from '@/lib/reference-data';

type Props = {
  doc: MethodDoc;
};

function buildMarkdown(doc: MethodDoc): string {
  const endpoints =
    doc.alternate_endpoints.length === 0
      ? [`**Endpoint:** \`${doc.endpoint}\``]
      : [
          '**Endpoints:**',
          `- \`${doc.endpoint}\``,
          ...doc.alternate_endpoints.map((endpoint) => `- \`${endpoint}\``),
        ];

  const bodyParams =
    doc.body_params.length === 0
      ? '- None'
      : doc.body_params
          .map(
            (field) =>
              `- \`${field.name}\` (${field.type}, ${field.required ? 'required' : 'optional'}): ${field.description}`,
          )
          .join('\n');

  const returnFields =
    doc.return_fields.length === 0
      ? '- None'
      : doc.return_fields
          .map((field) => `- \`${field.name}\` (${field.type}): ${field.description}`)
          .join('\n');

  return [
    `# ${doc.title}`,
    '',
    `**Method:** \`${doc.method}\``,
    ...endpoints,
    '',
    doc.summary,
    '',
    `## ${doc.body_label} (${doc.body_type})`,
    '',
    bodyParams,
    '',
    '## Returns',
    '',
    doc.returns_summary,
    '',
    returnFields,
    '',
    '## Example Request',
    '',
    '```http',
    doc.request_example,
    '```',
    '',
    '## Example Response',
    '',
    '```json',
    doc.response_example,
    '```',
    '',
  ].join('\n');
}

async function copyText(value: string): Promise<void> {
  if (typeof navigator !== 'undefined' && navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(value);
    return;
  }

  if (typeof document !== 'undefined') {
    const textarea = document.createElement('textarea');
    textarea.value = value;
    textarea.setAttribute('readonly', '');
    textarea.style.position = 'absolute';
    textarea.style.left = '-9999px';
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
  }
}

export function MethodMarkdownActions({ doc }: Props) {
  const markdown = useMemo(() => buildMarkdown(doc), [doc]);
  const [copied, setCopied] = useState(false);

  return (
    <div className="flex items-center gap-2">
      <Button
        size="sm"
        variant="outline"
        onClick={async () => {
          try {
            await copyText(markdown);
            setCopied(true);
            window.setTimeout(() => setCopied(false), 1200);
          } catch {
            setCopied(false);
          }
        }}
      >
        {copied ? 'Copied' : 'Copy Markdown'}
      </Button>
      <Button
        size="sm"
        variant="outline"
        onClick={() => {
          const blob = new Blob([markdown], { type: 'text/markdown;charset=utf-8' });
          const url = URL.createObjectURL(blob);
          const win = window.open(url, '_blank');
          if (!win) {
            URL.revokeObjectURL(url);
            return;
          }
          window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
        }}
      >
        View as Markdown
      </Button>
    </div>
  );
}
