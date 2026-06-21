import { ButtonHTMLAttributes } from 'react';

import { cn } from '@/lib/utils';

type Variant = 'solid' | 'outline' | 'ghost';

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: Variant;
  size?: 'sm' | 'md';
};

const variants: Record<Variant, string> = {
  // ShadCN-style: drive appearance via CSS variables (primary/background/accent/etc).
  solid: 'bg-primary text-primary-foreground hover:bg-primary/90',
  outline: 'border border-input bg-background hover:bg-accent hover:text-accent-foreground',
  ghost: 'hover:bg-accent hover:text-accent-foreground',
};

const sizes = {
  sm: 'h-8 px-3 text-xs',
  md: 'h-9 px-3.5 text-sm',
};

export function Button({ className, variant = 'solid', size = 'md', ...props }: Props) {
  return (
    <button
      className={cn(
        'inline-flex items-center justify-center rounded-md font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-50 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background',
        variants[variant],
        sizes[size],
        className,
      )}
      {...props}
    />
  );
}
