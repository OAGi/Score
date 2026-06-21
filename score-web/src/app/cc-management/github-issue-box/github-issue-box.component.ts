import {Component, HostListener, Input, OnInit, OnChanges, SimpleChanges, inject} from '@angular/core';

import {FormsModule} from '@angular/forms';
import {GithubIntegrationService, GithubStatus, LinkedIssue} from '../domain/github-integration.service';

/**
 * GitHub box shown on a Core Component detail (issue #1533).
 * Visible while the integration is enabled and either the component is in WIP or has linked issues.
 * - Not connected: shows a "Connect GitHub" button (full-page OAuth redirect; returns to this page).
 * - Connected & owner & WIP: lets the owner link/unlink GitHub issues.
 * - Otherwise: shows linked issues read-only.
 */
@Component({
  standalone: true,
  selector: 'score-github-issue-box',
  imports: [FormsModule],
  templateUrl: './github-issue-box.component.html',
  styleUrls: ['./github-issue-box.component.css'],
})
export class GithubIssueBoxComponent implements OnInit, OnChanges {
  @Input() manifestId!: number;
  @Input() ccType = 'acc';
  @Input() state = '';
  @Input() ownerLoginId = '';
  @Input() currentUser = '';
  /**
   * Whether this box is the page's own editable component (so the owner may link/unlink in WIP).
   * Set to false when the box shows a component reached by expanding the tree on another component's
   * page — then it is read-only (linked issues are displayed, but no Connect/input/Link/unlink UI).
   */
  @Input() editable = true;

  private service = inject(GithubIntegrationService);
  status?: GithubStatus;
  loading = true;
  issuesLoading = false;

  issues: LinkedIssue[] = [];
  newIssue = '';
  busy = false;
  error = '';
  /**
   * True from the moment the viewer clicks "Connect GitHub" until the full-page OAuth redirect
   * navigates away. While set, a blocking overlay (the GitHub mark animated as a loading indicator)
   * covers the whole page so no other action can be triggered during the (server round-trip)
   * transition. Reset on a bfcache restore — see {@link onPageShow} — so the overlay never sticks if
   * the viewer hits Back from GitHub.
   */
  connecting = false;

  /**
   * Connected viewer's GitHub permissions for THIS box, probed best-effort after the issues load
   * (issue #1533): whether each linked issue's repo is accessible (a prerequisite for posting status
   * comments) and whether the configured project board is writable. Until {@link accessLoaded} the
   * warnings stay hidden so nothing flashes; on any probe failure they stay hidden (never cry wolf).
   */
  private accessLoaded = false;
  private projectConfigured = false;
  private projectWritable?: boolean;
  /** owner/repo of this box's linked issues the connected viewer can't access — so can't comment. */
  private inaccessibleRepos: string[] = [];

  ngOnInit(): void {
    this.service.getStatus().subscribe({
      next: (s) => {
        this.status = s;
        this.loading = false;
        // Linked issues are component data: load them whenever the integration is enabled,
        // regardless of whether the current viewer has connected their own GitHub account.
        if (s.enabled) {
          this.loadIssues();
        }
      },
      error: () => {
        this.status = {enabled: false, connected: false};
        this.loading = false;
      },
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Tree navigation reuses this instance with a different node bound — reload that node's issues.
    // (The initial load is driven by getStatus() in ngOnInit, which runs after the first change.)
    const changed = changes['manifestId'] || changes['ccType'];
    if (changed && !changed.firstChange && this.status?.enabled) {
      this.loadIssues();
    }
  }

  /**
   * Show the box whenever the integration is enabled — read-only in every state (it always displays
   * the linked issues, or "No issues linked."). Linking is additionally gated on the page's own
   * component being in WIP and owned by the viewer (see {@link canEdit} + {@link isOwner}).
   */
  get show(): boolean {
    return !!this.status?.enabled;
  }

  /**
   * Linking/unlinking is only allowed on the page's own component while it is in WIP (the backend also
   * enforces owner + WIP). Read-only boxes (tree-expanded other components) are never editable.
   */
  get canEdit(): boolean {
    return this.editable && this.state === 'WIP';
  }

  get connected(): boolean {
    return !!this.status?.connected;
  }

  get isOwner(): boolean {
    return !!this.currentUser && this.currentUser === this.ownerLoginId;
  }

  get connectHref(): string {
    return this.service.connectUrl(window.location.href);
  }

  /**
   * Start the full-page OAuth connect redirect. The anchor still performs the navigation, so a
   * modifier/middle click that opens a new tab keeps working (the early return leaves this tab as-is);
   * for a plain left-click that navigates this tab we first raise a blocking overlay so the viewer
   * can't trigger other actions while the connect round-trip is in flight.
   */
  onConnectClick(event: MouseEvent): void {
    if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
      return;
    }
    this.connecting = true;
  }

  /**
   * Drop the blocking overlay when the page is restored from the bfcache — e.g. the viewer clicked
   * Connect, landed on GitHub's authorize page, then hit the browser Back button. Without this the
   * restored page would keep the overlay (and stay blocked) until a manual reload.
   */
  @HostListener('window:pageshow', ['$event'])
  onPageShow(event: PageTransitionEvent): void {
    if (event.persisted) {
      this.connecting = false;
    }
  }

  /**
   * Red warnings shown beside Disconnect when the connected viewer lacks a GitHub permission this box
   * needs (issue #1533): commenting on a linked issue's repo, and/or updating the configured project
   * board. Empty until the permissions are probed, when not connected, or when nothing is missing.
   */
  get permissionWarnings(): string[] {
    if (!this.accessLoaded || !this.connected) {
      return [];
    }
    const warnings: string[] = [];
    if (this.inaccessibleRepos.length === 1) {
      warnings.push('No permission to comment on ' + this.inaccessibleRepos[0]);
    } else if (this.inaccessibleRepos.length > 1) {
      warnings.push('No permission to comment on the linked repositories');
    }
    if (this.showProjectWriteWarning) {
      warnings.push('No permission to update the project board');
    }
    return warnings;
  }

  /**
   * Mirrors the state-change dialog: warn about board writability only when project sync is available
   * for this viewer (they hold the {@code project} scope) and the configured board is definitely not
   * writable for them. Best-effort on the backend, so an unknown result never produces a false warning.
   */
  private get showProjectWriteWarning(): boolean {
    return !!this.status?.projectSyncAvailable && this.accessLoaded
      && this.projectConfigured && this.projectWritable === false;
  }

  disconnect(): void {
    this.service.disconnect().subscribe(() => {
      if (this.status) {
        this.status.connected = false;
        this.status.login = undefined;
      }
      // The warnings are about the now-disconnected viewer's token, so drop them.
      this.clearAccessStatus();
      // Do NOT clear this.issues: linked issues are component data and stay visible (read-only)
      // whether or not the viewer is connected. Clearing them here was wrong — it showed
      // "No issues linked." until a reload re-fetched the still-present links.
    });
  }

  private loadIssues(): void {
    // Single call: returns the cached list immediately (page not blocked); the backend refreshes from
    // GitHub in the background, so changes appear on the next view. While the call is in flight we show
    // a "Loading issues…" state instead of a misleading "No issues linked.".
    this.issuesLoading = true;
    // Reset synchronously so a node switch (ngOnChanges reuses this instance) never shows the previous
    // node's warning during the in-flight fetch; loadAccessStatus re-probes once the issues resolve.
    this.clearAccessStatus();
    this.service.listIssues(this.ccType, this.manifestId).subscribe({
      next: (list) => {
        this.issues = list || [];
        this.issuesLoading = false;
        this.loadAccessStatus();
      },
      error: () => {
        this.issues = [];
        this.issuesLoading = false;
        // Still probe board writability — it doesn't depend on the (failed) issue list.
        this.loadAccessStatus();
      },
    });
  }

  /**
   * Probe the connected viewer's GitHub permissions for THIS box (issue #1533): which linked issues'
   * repos they can't access (so can't comment on), and — independently of any issues — whether the
   * configured project board is writable. Runs only for the connected viewer (the warning is about
   * their token) and only when there is something to check (issues linked and/or board sync available),
   * so a viewer with no linked issues and no board never triggers a needless GitHub call. Best-effort:
   * on failure the warnings simply stay hidden.
   */
  private loadAccessStatus(): void {
    this.clearAccessStatus();
    // Only the owner of the page's own (editable) box probes + warns: they are the one who links
    // issues and triggers the state changes whose comments/board moves the missing permission would
    // block, so the warning is theirs to act on. This also avoids a GitHub call per read-only
    // tree-expanded box and spares non-owner viewers an irrelevant warning about their own token.
    if (!this.connected || !this.editable || !this.isOwner
        || (this.issues.length === 0 && !this.status?.projectSyncAvailable)) {
      return;
    }
    const refs = this.issues.map((i) => ({owner: i.repoOwner, repo: i.repoName, number: i.issueNumber}));
    this.service.getProjectAccessStatus(refs).subscribe({
      next: (s) => {
        this.projectConfigured = !!s?.projectConfigured;
        this.projectWritable = s?.projectWritable;
        const denied = new Set<string>();
        for (const item of (s?.items || [])) {
          if (!item.repoAccessible) {
            denied.add(item.owner + '/' + item.repo);
          }
        }
        this.inaccessibleRepos = [...denied];
        this.accessLoaded = true;
      },
      error: () => {
        // Leave the warnings hidden on a transient failure rather than warning falsely.
        this.accessLoaded = true;
      },
    });
  }

  private clearAccessStatus(): void {
    this.accessLoaded = false;
    this.projectConfigured = false;
    this.projectWritable = undefined;
    this.inaccessibleRepos = [];
  }

  addIssue(): void {
    const parsed = this.parseInput(this.newIssue);
    if (!parsed) {
      this.error = 'Enter an issue number, owner/repo#number, or a GitHub issue URL.';
      return;
    }
    this.error = '';
    this.busy = true;
    this.service
      .linkIssue({
        ccType: this.ccType,
        manifestId: this.manifestId,
        issueNumber: parsed.number,
        repoOwner: parsed.owner,
        repoName: parsed.repo,
      })
      .subscribe({
        next: (list) => {
          this.issues = list || [];
          this.newIssue = '';
          this.busy = false;
        },
        error: () => {
          this.error = 'Failed to link the issue. Check the number/repository and that you have access.';
          this.busy = false;
        },
      });
  }

  remove(issue: LinkedIssue): void {
    this.busy = true;
    this.service.unlinkIssue(this.ccType, issue.linkId).subscribe({
      next: () => {
        this.issues = this.issues.filter((i) => i.linkId !== issue.linkId);
        this.busy = false;
      },
      error: () => (this.busy = false),
    });
  }

  label(issue: LinkedIssue): string {
    return issue.repoOwner + '/' + issue.repoName + '#' + issue.issueNumber;
  }

  /** Black or white text for a GitHub label background color (6-hex, no #). */
  labelTextColor(color?: string): string {
    if (!color || color.length < 6) {
      return '#1f2328';
    }
    const r = parseInt(color.substring(0, 2), 16);
    const g = parseInt(color.substring(2, 4), 16);
    const b = parseInt(color.substring(4, 6), 16);
    const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
    return luminance > 0.6 ? '#1f2328' : '#ffffff';
  }

  /** Names of the labels beyond the first {@code shown}, for the "+N" tooltip. */
  moreLabelsTooltip(issue: LinkedIssue, shown: number): string {
    return (issue.labels || []).slice(shown).map((l) => l.name).join(', ');
  }

  private parseInput(value: string): {number: number; owner?: string; repo?: string} | null {
    if (!value) {
      return null;
    }
    const s = value.trim();
    let m = s.match(/github\.com\/([^/]+)\/([^/]+)\/issues\/(\d+)/i);
    if (m) {
      return {owner: m[1], repo: m[2], number: parseInt(m[3], 10)};
    }
    m = s.match(/^([^/\s]+)\/([^/#\s]+)#(\d+)$/);
    if (m) {
      return {owner: m[1], repo: m[2], number: parseInt(m[3], 10)};
    }
    m = s.match(/^#?(\d+)$/);
    if (m) {
      return {number: parseInt(m[1], 10)};
    }
    return null;
  }
}
