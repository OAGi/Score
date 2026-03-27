import { HTMLAttributes } from 'react';

import { cn } from '@/lib/utils';

export function Badge({ className, ...props }: HTMLAttributes<HTMLSpanElement>) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-md border border-border px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide',
        className,
      )}
      {...props}
    />
  );
}

