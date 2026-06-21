import {Injectable, inject} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, shareReplay, tap, map} from 'rxjs';

export interface GithubStatus {
  enabled: boolean;
  connected: boolean;
  login?: string;
  /**
   * Whether a Projects board fieldOption sync would actually run for this user (issue #1533, Feature 2):
   * fieldOption sync is configured AND the user is connected with the {@code project} OAuth scope. Used to
   * only promise a board move the backend can keep.
   */
  projectSyncAvailable?: boolean;
  /**
   * The CcState -> board fieldOption mapping (e.g. {@code {WIP: 'Implementing', ...}}), straight from the
   * backend ProjectFieldOptions, so the dialog fieldOption preview has a single source of truth and never drifts.
   */
  fieldOptionByState?: { [state: string]: string };
  /** The initial/reset fieldOption (e.g. 'New') a cancelled revision resets the card to. */
  defaultFieldOption?: string;
}

/**
 * One option of the board's fieldOption (single-select) field: its display name and the GitHub color the
 * board assigns it (enum {@code GRAY|BLUE|GREEN|YELLOW|ORANGE|RED|PINK|PURPLE}, possibly absent), so the
 * dialog can render each fieldOption in its board color (issue #1533, Feature 2).
 */
export interface ProjectFieldOption {
  name: string;
  color?: string;
}

/**
 * The project board's fieldOption (single-select) field, fetched on demand by the state-change dialog for
 * the fieldOption-override dropdown (issue #1533, Feature 2). {@code options} is every option of the field
 * in board order; empty when fieldOption sync is not available for the viewer or the field is unresolved.
 * {@code projectTitle} is the owning Projects v2 board's title, shown beside the fieldOption transition.
 */
export interface ProjectField {
  projectTitle?: string;
  name?: string;
  options: ProjectFieldOption[];
}

/**
 * Whether one linked issue's repository is accessible to the connected user (a prerequisite for
 * commenting), for the state-change dialog (issue #1533). The issue's current board fieldOption is
 * intentionally NOT fetched — resolving it is slow — so the dialog shows only the destination fieldOption.
 */
export interface IssueRepoAccess {
  owner: string;
  repo: string;
  number: number;
  repoAccessible: boolean;
}

/**
 * The connected user's access to the configured project board, for a set of issues (issue #1533) — what
 * the dialog needs for its permission warnings, NOT the board's contents. Used to warn when the user
 * lacks GitHub permission to apply a fieldOption move.
 */
export interface ProjectAccessStatus {
  projectConfigured: boolean;
  /** Whether the connected user can READ the configured board. */
  projectAccessible: boolean;
  /** Best-effort: whether the connected user can WRITE the board (org membership). Drives the warning. */
  projectWritable: boolean;
  /** Each requested issue's repository accessibility. */
  items: IssueRepoAccess[];
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

  /**
   * The project board's fieldOption field name + options for the override dropdown (issue #1533, Feature 2).
   * Hits GitHub's GraphQL API server-side (cached there), so the dialog calls it on demand — never on
   * every page like {@link getStatus}. Returns empty options when fieldOption sync is unavailable.
   */
  getProjectField(): Observable<ProjectField> {
    return this.http.get<ProjectField>('/api/integration/github/project-field');
  }

  /**
   * Live board state for the given linked issues (issue #1533): each issue's current fieldOption + repo access,
   * and whether the connected user can read the project board. Hits GitHub per issue (on demand), so the
   * dialog calls it once with all its linked issues. Best-effort on the backend.
   */
  getProjectAccessStatus(issues: {owner: string; repo: string; number: number}[]): Observable<ProjectAccessStatus> {
    return this.http.post<ProjectAccessStatus>('/api/integration/github/issues/project-access-status', issues);
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
