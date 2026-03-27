'use client';

import { useEffect, useMemo, useState } from 'react';
import type { CSSProperties } from 'react';

type Theme = 'light' | 'dark';
type ThemeMode = 'system' | Theme;

const STORAGE_KEY = 'connectcenter-theme';
const THEME_ORDER: ThemeMode[] = ['system', 'light', 'dark'];

function getSystemTheme(): Theme {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function resolveTheme(mode: ThemeMode): Theme {
  return mode === 'system' ? getSystemTheme() : mode;
}

function applyTheme(mode: ThemeMode) {
  const theme = resolveTheme(mode);
  const isDark = theme === 'dark';

  document.documentElement.dataset.theme = theme;
  document.documentElement.classList.toggle('dark', isDark);
  document.documentElement.style.colorScheme = theme;
}

function detectInitialTheme(): { mode: ThemeMode; theme: Theme } {
  if (typeof window === 'undefined') {
    return { mode: 'light', theme: 'light' };
  }

  const saved = window.localStorage.getItem(STORAGE_KEY);
  if (saved === 'light' || saved === 'dark' || saved === 'system') {
    return { mode: saved, theme: resolveTheme(saved) };
  }

  return { mode: 'system', theme: getSystemTheme() };
}

type ThemeTransitionContext = {
  element?: HTMLElement | null;
  pointerClientX?: number;
  pointerClientY?: number;
};

function clamp01(value: number): number {
  if (Number.isNaN(value)) return 0.5;
  if (value <= 0) return 0;
  if (value >= 1) return 1;
  return value;
}

function prefersReducedMotion(): boolean {
  if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
    return false;
  }
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches ?? false;
}

function cleanupThemeTransition(root: HTMLElement) {
  root.classList.remove('theme-transition');
  root.style.removeProperty('--theme-switch-x');
  root.style.removeProperty('--theme-switch-y');
}

function startThemeTransition(nextMode: ThemeMode, context?: ThemeTransitionContext) {
  if (typeof document === 'undefined') {
    applyTheme(nextMode);
    return;
  }

  const root = document.documentElement;
  const doc = document as Document & { startViewTransition?: (cb: () => void) => { finished?: Promise<void> } };

  if (doc.startViewTransition && !prefersReducedMotion()) {
    let x = 0.5;
    let y = 0.5;
    if (context?.pointerClientX !== undefined && context?.pointerClientY !== undefined && typeof window !== 'undefined') {
      x = clamp01(context.pointerClientX / window.innerWidth);
      y = clamp01(context.pointerClientY / window.innerHeight);
    } else if (context?.element && typeof window !== 'undefined') {
      const rect = context.element.getBoundingClientRect();
      if (rect.width > 0 && rect.height > 0) {
        x = clamp01((rect.left + rect.width / 2) / window.innerWidth);
        y = clamp01((rect.top + rect.height / 2) / window.innerHeight);
      }
    }

    root.style.setProperty('--theme-switch-x', `${x * 100}%`);
    root.style.setProperty('--theme-switch-y', `${y * 100}%`);
    root.classList.add('theme-transition');

    try {
      const transition = doc.startViewTransition(() => applyTheme(nextMode));
      transition?.finished?.finally(() => cleanupThemeTransition(root));
    } catch {
      cleanupThemeTransition(root);
      applyTheme(nextMode);
    }
    return;
  }

  applyTheme(nextMode);
  cleanupThemeTransition(root);
}

type ThemeToggleProps = {
  size?: 'compact' | 'default';
  className?: string;
};

export function ThemeToggle({ size = 'compact', className }: ThemeToggleProps) {
  const [theme, setTheme] = useState<Theme>('light');
  const [mode, setMode] = useState<ThemeMode>('light');
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const initial = detectInitialTheme();
    setMode(initial.mode);
    setTheme(initial.theme);
    applyTheme(initial.mode);
    setReady(true);
  }, []);

  useEffect(() => {
    // If the user hasn't explicitly chosen a theme, keep following the OS setting live.
    if (mode !== 'system') return;
    const mql = window.matchMedia('(prefers-color-scheme: dark)');

    const onChange = () => {
      const next = getSystemTheme();
      setTheme(next);
      applyTheme('system');
    };

    // Safari uses addListener/removeListener; modern browsers use addEventListener.
    if (typeof mql.addEventListener === 'function') {
      mql.addEventListener('change', onChange);
      return () => mql.removeEventListener('change', onChange);
    }

    mql.addListener(onChange);
    return () => mql.removeListener(onChange);
  }, [mode]);

  const setThemeMode = (nextMode: ThemeMode, context?: ThemeTransitionContext) => {
    if (nextMode === mode) {
      return;
    }
    const resolved = resolveTheme(nextMode);
    setMode(nextMode);
    setTheme(resolved);
    startThemeTransition(nextMode, context);
    window.localStorage.setItem(STORAGE_KEY, nextMode);
  };

  const themeIndex = useMemo(() => Math.max(0, THEME_ORDER.indexOf(mode)), [mode]);
  const sizeClass = size === 'default' ? 'theme-toggle--default' : 'theme-toggle--compact';
  const combinedClassName = ['theme-toggle', sizeClass, className].filter(Boolean).join(' ');

  return (
    <div className={combinedClassName} style={{ '--theme-index': themeIndex } as CSSProperties}>
      <div className="theme-toggle__track" role="group" aria-label="Theme">
        <span className="theme-toggle__indicator" />
        <button
          type="button"
          className={`theme-toggle__button ${mode === 'system' ? 'active' : ''}`}
          onClick={(event) =>
            setThemeMode('system', { element: event.currentTarget, pointerClientX: event.clientX, pointerClientY: event.clientY })
          }
          aria-pressed={mode === 'system'}
          aria-label="System theme"
          title="System"
        >
          <SystemIcon />
        </button>
        <button
          type="button"
          className={`theme-toggle__button ${mode === 'light' ? 'active' : ''}`}
          onClick={(event) =>
            setThemeMode('light', { element: event.currentTarget, pointerClientX: event.clientX, pointerClientY: event.clientY })
          }
          aria-pressed={mode === 'light'}
          aria-label="Light theme"
          title="Light"
        >
          <SunIcon />
        </button>
        <button
          type="button"
          className={`theme-toggle__button ${mode === 'dark' ? 'active' : ''}`}
          onClick={(event) =>
            setThemeMode('dark', { element: event.currentTarget, pointerClientX: event.clientX, pointerClientY: event.clientY })
          }
          aria-pressed={mode === 'dark'}
          aria-label="Dark theme"
          title="Dark"
        >
          <MoonIcon />
        </button>
      </div>
      <span className="sr-only">
        {ready ? `Theme set to ${mode === 'system' ? `${theme} (system)` : mode}` : 'Theme toggle'}
      </span>
    </div>
  );
}

function SystemIcon() {
  return (
    <svg className="theme-icon" viewBox="0 0 24 24" aria-hidden="true">
      <rect width="20" height="14" x="2" y="3" rx="2" />
      <line x1="8" x2="16" y1="21" y2="21" />
      <line x1="12" x2="12" y1="17" y2="21" />
    </svg>
  );
}

function SunIcon() {
  return (
    <svg className="theme-icon" viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v2" />
      <path d="M12 20v2" />
      <path d="m4.93 4.93 1.41 1.41" />
      <path d="m17.66 17.66 1.41 1.41" />
      <path d="M2 12h2" />
      <path d="M20 12h2" />
      <path d="m6.34 17.66-1.41 1.41" />
      <path d="m19.07 4.93-1.41 1.41" />
    </svg>
  );
}

function MoonIcon() {
  return (
    <svg className="theme-icon" viewBox="0 0 24 24" aria-hidden="true">
      <path
        d="M20.985 12.486a9 9 0 1 1-9.473-9.472c.405-.022.617.46.402.803a6 6 0 0 0 8.268 8.268c.344-.215.825-.004.803.401"
      />
    </svg>
  );
}
