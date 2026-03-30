import {inject, Injectable} from '@angular/core';
import {Title} from '@angular/platform-browser';
import {RouterStateSnapshot, TitleStrategy} from '@angular/router';

export const APP_TITLE = 'connectCenter';

export function formatAppTitle(routeTitle?: string): string {
  return routeTitle ? `${routeTitle} | ${APP_TITLE}` : APP_TITLE;
}

export function setAppTitleIfPresent(title: Title, value?: string | null, suffix = ''): void {
  const normalizedValue = value?.trim();
  if (!normalizedValue) {
    return;
  }

  const normalizedSuffix = suffix.trim();
  const routeTitle = normalizedSuffix ? `${normalizedValue} ${normalizedSuffix}` : normalizedValue;
  title.setTitle(formatAppTitle(routeTitle));
}

@Injectable()
export class AppTitleStrategy extends TitleStrategy {
  private title = inject(Title);

  override updateTitle(routerState: RouterStateSnapshot): void {
    const routeTitle = this.buildTitle(routerState);
    this.title.setTitle(formatAppTitle(routeTitle));
  }
}
