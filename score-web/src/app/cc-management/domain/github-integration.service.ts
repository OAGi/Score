import {Injectable, inject} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, shareReplay, tap, map} from 'rxjs';

export interface GithubStatus {
  enabled: boolean;
  connected: boolean;
  login?: string;
}

export interface GithubLabel {
  name: string;
  color?: string;
  description?: string;
}

export interface GithubAssignee {
  login: string;
  htmlUrl?: string;
  avatarUrl?: string;
}

export interface GithubMilestone {
  title?: string;
  state?: string;
}

export interface LinkedIssue {
  linkId: string;
  repoOwner: string;
  repoName: string;
  issueNumber: number;
  htmlUrl?: string;
  title?: string;
  state?: string;
  type?: string;
  milestone?: GithubMilestone;
  labels?: GithubLabel[];
  assignees?: GithubAssignee[];
}

export interface LinkIssueRequest {
  ccType: string;
  manifestId: number;
  issueNumber: number;
  repoOwner?: string;
  repoName?: string;
}

/** One component to look up linked issues for. {@code ccType} is lowercase (acc, asccp, bccp, dt, code_list, agency_id_list). */
export interface IssueLookupTarget {
  ccType: string;
  manifestId: number;
}

export interface IssueLookupResult {
  ccType: string;
  manifestId: number;
  issues: LinkedIssue[];
}

/**
 * Per-user GitHub connection + issue linking (issue #1533). Talks to the backend integration
 * endpoints (proxied as /api/integration/github/*). OAuth tokens live server-side (Redis);
 * the SPA only sees connection status and the linked issues.
 */
@Injectable({providedIn: 'root'})
export class GithubIntegrationService {
  private http = inject(HttpClient);
  private status$?: Observable<GithubStatus>;
  private enabledCache$?: Observable<boolean>;

  /**
   * Per-user connection status, including whether the integration is enabled (SCORE_GITHUB_ENABLED).
   * Cached for the session so the page's enabled-gate and the (possibly several) boxes share ONE
   * request; the cache is reset on disconnect so a later read reflects the change, and on error so
   * a transient /status failure doesn't stick for the whole session.
   */
  getStatus(): Observable<GithubStatus> {
    if (!this.status$) {
      this.status$ = this.http.get<GithubStatus>('/api/integration/github/status').pipe(
        tap({
          error: () => {
            this.status$ = undefined;
            this.enabledCache$ = undefined;
          }
        }),
        shareReplay(1));
    }
    return this.status$;
  }

  /**
   * Whether the GitHub integration is enabled (SCORE_GITHUB_ENABLED). A page gates the whole GitHub
   * box (and its issue calls) on this, so nothing renders / no issues are fetched when it is off.
   * Shares the single cached status request above.
   */
  enabled$(): Observable<boolean> {
    if (!this.enabledCache$) {
      this.enabledCache$ = this.getStatus().pipe(map(s => !!s.enabled), shareReplay(1));
    }
    return this.enabledCache$;
  }

  disconnect(): Observable<void> {
    return this.http.delete<void>('/api/integration/github/connection')
      .pipe(tap(() => this.status$ = undefined));
  }

  /** Full-page redirect target that starts the OAuth connect flow and returns to {@code returnUrl}. */
  connectUrl(returnUrl: string): string {
    return '/api/integration/github/connect?returnUrl=' + encodeURIComponent(returnUrl);
  }

  /**
   * Linked issues. The backend returns the cache immediately and refreshes from GitHub in the
   * background, so this single call does not block and changes appear on the next view.
   */
  listIssues(ccType: string, manifestId: number): Observable<LinkedIssue[]> {
    return this.http.get<LinkedIssue[]>(
      '/api/integration/github/issues?ccType=' + ccType + '&manifestId=' + manifestId);
  }

  /**
   * Cache-only bulk lookup of the linked issues of several components at once (no GitHub call,
   * no refresh) — used by the state-change dialog to show the issues a status comment would go to.
   */
  lookupIssues(targets: IssueLookupTarget[]): Observable<IssueLookupResult[]> {
    return this.http.post<IssueLookupResult[]>('/api/integration/github/issues/lookup',
      targets.map(t => ({ccType: t.ccType, manifestId: t.manifestId})));
  }

  linkIssue(request: LinkIssueRequest): Observable<LinkedIssue[]> {
    return this.http.post<LinkedIssue[]>('/api/integration/github/issues', request);
  }

  unlinkIssue(ccType: string, linkId: string): Observable<void> {
    return this.http.delete<void>('/api/integration/github/issues/' + linkId + '?ccType=' + ccType);
  }
}
