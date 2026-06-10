import {Component, Input, OnInit, OnChanges, SimpleChanges, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
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
  imports: [CommonModule, FormsModule],
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

  disconnect(): void {
    this.service.disconnect().subscribe(() => {
      if (this.status) {
        this.status.connected = false;
        this.status.login = undefined;
      }
      this.issues = [];
    });
  }

  private loadIssues(): void {
    // Single call: returns the cached list immediately (page not blocked); the backend refreshes from
    // GitHub in the background, so changes appear on the next view. While the call is in flight we show
    // a "Loading issues…" state instead of a misleading "No issues linked.".
    this.issuesLoading = true;
    this.service.listIssues(this.ccType, this.manifestId).subscribe({
      next: (list) => {
        this.issues = list || [];
        this.issuesLoading = false;
      },
      error: () => {
        this.issues = [];
        this.issuesLoading = false;
      },
    });
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
