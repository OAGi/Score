import {Component, ElementRef, OnInit, QueryList, ViewChildren, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatChipsModule} from '@angular/material/chips';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MarkdownComponent} from 'ngx-markdown';
import {GithubIntegrationService, GithubStatus, LinkedIssue} from '../domain/github-integration.service';
import {LogService} from '../../log-management/domain/log.service';
import {candidatePost, revertPost} from './github-status-post-renderer';
import {MarkdownAction, applyMarkdownAction} from './markdown-toolbar';

/**
 * Whether a state change routes through {@link StateChangeDialogComponent}: only the
 * Draft -> Candidate and Candidate -> WIP transitions have a GitHub status post (issue #1533).
 * Every other transition keeps the generic confirm dialog, even when the GitHub integration is
 * enabled.
 */
export function usesStateChangeDialog(state: string, toState: string): boolean {
  return (state === 'Draft' && toState === 'Candidate')
    || (state === 'Candidate' && toState === 'WIP');
}

/** One component whose state is about to change. {@code ccType} is lowercase (acc, asccp, bccp, dt, code_list, agency_id_list). */
export interface StateChangeDialogTarget {
  ccType: string;
  manifestId: number;
  /** Display name (den / name) of the component. */
  name: string;
  /** CURRENT state of the component. */
  state: string;
}

export interface StateChangeDialogData {
  header: string;
  content: string[];
  actionLabel: string;
  toState: string;
  targets: StateChangeDialogTarget[];
}

export interface StateChangeDialogResult {
  confirmed: true;
  /** Non-blank comments only, keyed '<ccType>:<manifestId>'. A missing key means: post nothing. */
  comments: { [key: string]: string };
}

/**
 * Inline 16x16 Octicon path data (@primer/octicons v19.28.1, MIT) — the exact glyphs of GitHub's
 * comment-box toolbar, embedded so no icon dependency is added. Rendered with fill="currentColor".
 */
const OCTICON_PATHS: { [name: string]: string } = {
  heading: 'M3.75 2a.75.75 0 0 1 .75.75V7h7V2.75a.75.75 0 0 1 1.5 0v10.5a.75.75 0 0 1-1.5 0V8.5h-7v4.75a.75.75 0 0 1-1.5 0V2.75A.75.75 0 0 1 3.75 2Z',
  bold: 'M4 2h4.5a3.501 3.501 0 0 1 2.852 5.53A3.499 3.499 0 0 1 9.5 14H4a1 1 0 0 1-1-1V3a1 1 0 0 1 1-1Zm1 7v3h4.5a1.5 1.5 0 0 0 0-3Zm3.5-2a1.5 1.5 0 0 0 0-3H5v3Z',
  italic: 'M6 2.75A.75.75 0 0 1 6.75 2h6.5a.75.75 0 0 1 0 1.5h-2.505l-3.858 9H9.25a.75.75 0 0 1 0 1.5h-6.5a.75.75 0 0 1 0-1.5h2.505l3.858-9H6.75A.75.75 0 0 1 6 2.75Z',
  quote: 'M1.75 2.5h10.5a.75.75 0 0 1 0 1.5H1.75a.75.75 0 0 1 0-1.5Zm4 5h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1 0-1.5Zm0 5h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1 0-1.5ZM2.5 7.75v6a.75.75 0 0 1-1.5 0v-6a.75.75 0 0 1 1.5 0Z',
  code: 'm11.28 3.22 4.25 4.25a.75.75 0 0 1 0 1.06l-4.25 4.25a.749.749 0 0 1-1.275-.326.749.749 0 0 1 .215-.734L13.94 8l-3.72-3.72a.749.749 0 0 1 .326-1.275.749.749 0 0 1 .734.215Zm-6.56 0a.751.751 0 0 1 1.042.018.751.751 0 0 1 .018 1.042L2.06 8l3.72 3.72a.749.749 0 0 1-.326 1.275.749.749 0 0 1-.734-.215L.47 8.53a.75.75 0 0 1 0-1.06Z',
  link: 'm7.775 3.275 1.25-1.25a3.5 3.5 0 1 1 4.95 4.95l-2.5 2.5a3.5 3.5 0 0 1-4.95 0 .751.751 0 0 1 .018-1.042.751.751 0 0 1 1.042-.018 1.998 1.998 0 0 0 2.83 0l2.5-2.5a2.002 2.002 0 0 0-2.83-2.83l-1.25 1.25a.751.751 0 0 1-1.042-.018.751.751 0 0 1-.018-1.042Zm-4.69 9.64a1.998 1.998 0 0 0 2.83 0l1.25-1.25a.751.751 0 0 1 1.042.018.751.751 0 0 1 .018 1.042l-1.25 1.25a3.5 3.5 0 1 1-4.95-4.95l2.5-2.5a3.5 3.5 0 0 1 4.95 0 .751.751 0 0 1-.018 1.042.751.751 0 0 1-1.042.018 1.998 1.998 0 0 0-2.83 0l-2.5 2.5a1.998 1.998 0 0 0 0 2.83Z',
  listUnordered: 'M5.75 2.5h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1 0-1.5Zm0 5h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1 0-1.5Zm0 5h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1 0-1.5ZM2 14a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm1-6a1 1 0 1 1-2 0 1 1 0 0 1 2 0ZM2 4a1 1 0 1 1 0-2 1 1 0 0 1 0 2Z',
  listOrdered: 'M5 3.25a.75.75 0 0 1 .75-.75h8.5a.75.75 0 0 1 0 1.5h-8.5A.75.75 0 0 1 5 3.25Zm0 5a.75.75 0 0 1 .75-.75h8.5a.75.75 0 0 1 0 1.5h-8.5A.75.75 0 0 1 5 8.25Zm0 5a.75.75 0 0 1 .75-.75h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1-.75-.75ZM.924 10.32a.5.5 0 0 1-.851-.525l.001-.001.001-.002.002-.004.007-.011c.097-.144.215-.273.348-.384.228-.19.588-.392 1.068-.392.468 0 .858.181 1.126.484.259.294.377.673.377 1.038 0 .987-.686 1.495-1.156 1.845l-.047.035c-.303.225-.522.4-.654.597h1.357a.5.5 0 0 1 0 1H.5a.5.5 0 0 1-.5-.5c0-1.005.692-1.52 1.167-1.875l.035-.025c.531-.396.8-.625.8-1.078a.57.57 0 0 0-.128-.376C1.806 10.068 1.695 10 1.5 10a.658.658 0 0 0-.429.163.835.835 0 0 0-.144.153ZM2.003 2.5V6h.503a.5.5 0 0 1 0 1H.5a.5.5 0 0 1 0-1h.503V3.308l-.28.14a.5.5 0 0 1-.446-.895l1.003-.5a.5.5 0 0 1 .723.447Z',
  tasklist: 'M2 2h4a1 1 0 0 1 1 1v4a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V3a1 1 0 0 1 1-1Zm4.655 8.595a.75.75 0 0 1 0 1.06L4.03 14.28a.75.75 0 0 1-1.06 0l-1.5-1.5a.749.749 0 0 1 .326-1.275.749.749 0 0 1 .734.215l.97.97 2.095-2.095a.75.75 0 0 1 1.06 0ZM9.75 2.5h5.5a.75.75 0 0 1 0 1.5h-5.5a.75.75 0 0 1 0-1.5Zm0 5h5.5a.75.75 0 0 1 0 1.5h-5.5a.75.75 0 0 1 0-1.5Zm0 5h5.5a.75.75 0 0 1 0 1.5h-5.5a.75.75 0 0 1 0-1.5Zm-7.25-9v3h3v-3Z',
};

/**
 * State-change confirmation dialog used instead of the generic ConfirmDialog when the GitHub
 * integration is enabled and the transition is Draft -> Candidate or Candidate -> WIP (see
 * {@link usesStateChangeDialog}; issue #1533). Shows each target component's linked GitHub issues
 * and — for a GitHub-connected user on a target with linked issues — a per-component Markdown
 * comment box pre-filled with the rendered status post (the component's change summary). The user
 * edits the post freely; the backend posts the final text to the linked issues verbatim. Clearing
 * the box means nothing is posted for that component.
 */
@Component({
  standalone: true,
  selector: 'score-state-change-dialog',
  imports: [CommonModule, FormsModule, MatDialogModule,
    MatButtonModule, MatChipsModule, MatIconModule, MatTooltipModule, MarkdownComponent],
  providers: [LogService],
  templateUrl: './state-change-dialog.component.html',
  styleUrls: ['./state-change-dialog.component.css'],
})
export class StateChangeDialogComponent implements OnInit {
  /** The box holds the whole rendered post; GitHub's hard limit on a comment is 65,536. */
  readonly maxCommentLength = 60000;

  /**
   * The Write tab's Markdown formatting toolbar, in GitHub's button set, order AND grouping
   * (minus the GitHub-server-specific @-mention / reference / saved-replies buttons): the live
   * github.com comment box renders heading/bold/italic/quote/code/link | lists as two groups with
   * a thin divider between them. Icons are GitHub's own 16px Octicons ({@link OCTICON_PATHS}).
   * Each button runs the matching pure edit of {@link applyMarkdownAction} on the target's
   * textarea.
   */
  readonly toolbarGroups: ReadonlyArray<ReadonlyArray<{ action: MarkdownAction; path: string; tooltip: string }>> = [
    [
      {action: 'heading', path: OCTICON_PATHS.heading, tooltip: 'Add heading text'},
      {action: 'bold', path: OCTICON_PATHS.bold, tooltip: 'Add bold text (Ctrl/Cmd + B)'},
      {action: 'italic', path: OCTICON_PATHS.italic, tooltip: 'Add italic text (Ctrl/Cmd + I)'},
      {action: 'quote', path: OCTICON_PATHS.quote, tooltip: 'Add a quote (Ctrl/Cmd + Shift + .)'},
      {action: 'code', path: OCTICON_PATHS.code, tooltip: 'Add code (Ctrl/Cmd + E)'},
      {action: 'link', path: OCTICON_PATHS.link, tooltip: 'Add a link (Ctrl/Cmd + K)'},
    ],
    [
      {action: 'unordered-list', path: OCTICON_PATHS.listUnordered, tooltip: 'Add an unordered list (Ctrl/Cmd + Shift + 8)'},
      {action: 'ordered-list', path: OCTICON_PATHS.listOrdered, tooltip: 'Add an ordered list (Ctrl/Cmd + Shift + 7)'},
      {action: 'task-list', path: OCTICON_PATHS.tasklist, tooltip: 'Add a task list'},
    ],
  ];

  /**
   * The comment textareas (one per target on bulk), found back by their data-key. Always in
   * the DOM once their editor renders — Preview only hides them — so the query is stable and
   * each element's undo stack and selection persist across tab toggles.
   */
  @ViewChildren('commentInput')
  private commentInputs!: QueryList<ElementRef<HTMLTextAreaElement>>;

  /** All live Preview panes, found back by their data-key (focused by the Cmd/Ctrl+Shift+P toggle). */
  @ViewChildren('previewPane')
  private previewPanes!: QueryList<ElementRef<HTMLElement>>;

  dialogRef = inject<MatDialogRef<StateChangeDialogComponent, StateChangeDialogResult | undefined>>(MatDialogRef);
  data = inject<StateChangeDialogData>(MAT_DIALOG_DATA);
  private githubService = inject(GithubIntegrationService);
  private logService = inject(LogService);

  status?: GithubStatus;
  issuesLoading = true;
  private issuesByKey = new Map<string, LinkedIssue[]>();
  comments: { [key: string]: string } = {};
  /** Targets whose pre-fill (change summary) is still being fetched. */
  private prefillLoadingKeys = new Set<string>();
  /** Targets whose pre-fill failed — the box stays empty with a warning, confirm still allowed. */
  private prefillFailedKeys = new Set<string>();
  /** Targets whose comment editor shows the rendered Markdown preview instead of the textarea. */
  private previewKeys = new Set<string>();

  ngOnInit(): void {
    this.githubService.getStatus().subscribe({
      next: (status) => {
        this.status = status;
        this.lookupIssues();
      },
      error: (err) => {
        // Graceful degradation: still allow confirm, just without issues/comments.
        console.error('Failed to load the GitHub integration status', err);
        this.status = {enabled: false, connected: false};
        this.issuesLoading = false;
      },
    });
  }

  private lookupIssues(): void {
    this.githubService.lookupIssues(
      this.data.targets.map(t => ({ccType: t.ccType, manifestId: t.manifestId}))
    ).subscribe({
      next: (results) => {
        for (const result of (results || [])) {
          this.issuesByKey.set(result.ccType + ':' + result.manifestId, result.issues || []);
        }
        this.issuesLoading = false;
        this.prefillComments();
      },
      error: (err) => {
        // Graceful degradation: still allow confirm, just without issues/comments.
        console.error('Failed to look up linked GitHub issues', err);
        this.issuesLoading = false;
      },
    });
  }

  /**
   * Pre-fills each eligible target's comment box with the rendered GitHub status post: the
   * component's change summary (GET /logs/change-summary) run through the post renderer —
   * {@link candidatePost} for Draft -> Candidate, {@link revertPost} for Candidate -> WIP.
   * The dialog's lowercase ccType maps to the backend CcType enum name by uppercasing
   * (acc -> ACC, code_list -> CODE_LIST, …). On failure the box stays empty with a warning;
   * the user may still write the comment by hand or cancel.
   */
  private prefillComments(): void {
    for (const target of this.data.targets) {
      if (!this.commentEligible(target)) {
        continue;
      }
      const key = this.key(target);
      this.prefillLoadingKeys.add(key);
      this.logService.getChangeSummary(target.ccType.toUpperCase(), target.manifestId).subscribe({
        next: (summary) => {
          this.comments[key] = (this.data.toState === 'WIP')
            ? revertPost(summary, 'WIP')
            : candidatePost(summary);
          this.prefillLoadingKeys.delete(key);
        },
        error: (err) => {
          console.error('Failed to pre-fill the GitHub status post for ' + key, err);
          this.prefillLoadingKeys.delete(key);
          this.prefillFailedKeys.add(key);
        },
      });
    }
  }

  key(target: StateChangeDialogTarget): string {
    return target.ccType + ':' + target.manifestId;
  }

  /**
   * Label of the type badge in front of the target's name — the same <mat-chip> badge the
   * core-components list page shows in its Type column. acc/asccp/bccp/dt just uppercase
   * (the dialog's dt target does not know CDT vs BDT, so it stays the generic 'DT');
   * code_list / agency_id_list have no badge on that page, so they get a same-styled
   * neutral label.
   */
  typeLabel(target: StateChangeDialogTarget): string {
    switch (target.ccType) {
      case 'code_list':
        return 'Code List';
      case 'agency_id_list':
        return 'Agency ID List';
      default:
        return target.ccType.toUpperCase();
    }
  }

  issuesOf(target: StateChangeDialogTarget): LinkedIssue[] {
    return this.issuesByKey.get(this.key(target)) || [];
  }

  issueLabel(issue: LinkedIssue): string {
    return issue.repoOwner + '/' + issue.repoName + '#' + issue.issueNumber;
  }

  /**
   * A comment may be posted for a target when the viewer's GitHub account is connected and the
   * target has at least one linked issue. Every target is transition-eligible by construction:
   * the call sites only open this dialog for the transitions in {@link usesStateChangeDialog}.
   */
  commentEligible(target: StateChangeDialogTarget): boolean {
    return !!this.status?.connected && this.issuesOf(target).length > 0;
  }

  prefillLoading(target: StateChangeDialogTarget): boolean {
    return this.prefillLoadingKeys.has(this.key(target));
  }

  prefillFailed(target: StateChangeDialogTarget): boolean {
    return this.prefillFailedKeys.has(this.key(target));
  }

  commentLength(target: StateChangeDialogTarget): number {
    return (this.comments[this.key(target)] || '').length;
  }

  showPreview(target: StateChangeDialogTarget): boolean {
    return this.previewKeys.has(this.key(target));
  }

  setPreview(target: StateChangeDialogTarget, preview: boolean): void {
    if (preview) {
      this.previewKeys.add(this.key(target));
    } else {
      this.previewKeys.delete(this.key(target));
    }
  }

  /**
   * Runs one toolbar action against the target's textarea: the pure edit computes the new
   * text + selection, {@link replaceText} swaps the value in as ONE undoable edit (its native
   * input event carries the change through ngModel into {@link comments}, keeping the char
   * counter, the Preview tab and the dialog result in sync), and the computed selection is
   * restored. In Preview mode editing shortcuts stay no-ops (as on GitHub): the textarea still
   * exists then — kept alive, display:none'd, to preserve its undo stack — but a hidden element
   * cannot take focus, so {@link replaceText}'s execCommand would miss it and degrade to the
   * undo-wiping value-assignment fallback; the guard returns before that can happen.
   */
  applyToolbarAction(target: StateChangeDialogTarget, action: MarkdownAction): void {
    const textarea = this.showPreview(target) ? undefined : this.textareaOf(target);
    if (!textarea) {
      return;
    }
    const result = applyMarkdownAction(action, {
      text: textarea.value,
      selectionStart: textarea.selectionStart,
      selectionEnd: textarea.selectionEnd,
    });
    this.replaceText(textarea, result.text);
    textarea.setSelectionRange(result.selectionStart, result.selectionEnd);
  }

  /**
   * Replaces the textarea's whole value with {@code text} as ONE native-undo-stack edit:
   * select everything, then a single {@code execCommand('insertText')}. Assigning
   * {@code textarea.value} would wipe the browser's undo history (the reported Ctrl+Z bug);
   * execCommand is deprecated but remains the only undo-integrated way to edit a textarea
   * programmatically — GitHub's own &#64;github/markdown-toolbar-element relies on it. It also
   * fires a real bubbling 'input' event, so ngModel picks the change up natively. insertText
   * rejects an empty string in some engines, so clearing goes through 'delete' instead. Only
   * when execCommand fails (returns false / throws / leaves the wrong value) does the old
   * value-assignment + manual input event run as a non-undoable fallback.
   */
  private replaceText(textarea: HTMLTextAreaElement, text: string): void {
    textarea.focus();
    textarea.select();
    let done = false;
    try {
      done = text.length > 0
        ? document.execCommand('insertText', false, text)
        : document.execCommand('delete', false);
    } catch {
      done = false;
    }
    if (!done || textarea.value !== text) {
      textarea.value = text;
      textarea.dispatchEvent(new Event('input', {bubbles: true}));
    }
  }

  /**
   * GitHub's comment-box keyboard shortcuts (docs.github.com > Accessibility > Keyboard
   * shortcuts, "Comments" section) — the editor-scoped ones only:
   * Cmd/Ctrl+B bold, +I italic, +E code, +K link, Cmd/Ctrl+Enter confirm ("submit comment"),
   * Cmd/Ctrl+Shift+P Write/Preview toggle, +Shift+7 ordered list, +Shift+8 unordered list,
   * +Shift+. quote. Bound on the editor container rather than the textarea so the Preview
   * pane (focused by the toggle) still reaches Cmd/Ctrl+Shift+P to flip back to Write.
   * Shift turns the printable keys into layout-dependent characters ('7' -> '&', '8' -> '*'),
   * so the Shift shortcuts match on the physical {@code event.code}.
   */
  onCommentKeydown(event: KeyboardEvent, target: StateChangeDialogTarget): void {
    if (!(event.metaKey || event.ctrlKey) || event.altKey) {
      return;
    }
    if (event.shiftKey) {
      switch (event.code) {
        case 'KeyP':
          event.preventDefault();
          this.togglePreview(target);
          return;
        case 'Digit7':
          event.preventDefault();
          this.applyToolbarAction(target, 'ordered-list');
          return;
        case 'Digit8':
          event.preventDefault();
          this.applyToolbarAction(target, 'unordered-list');
          return;
        case 'Period':
          event.preventDefault();
          this.applyToolbarAction(target, 'quote');
          return;
      }
      return;
    }
    switch (event.key.toLowerCase()) {
      case 'b':
        event.preventDefault();
        this.applyToolbarAction(target, 'bold');
        return;
      case 'i':
        event.preventDefault();
        this.applyToolbarAction(target, 'italic');
        return;
      case 'e':
        event.preventDefault();
        this.applyToolbarAction(target, 'code');
        return;
      case 'k':
        event.preventDefault();
        this.applyToolbarAction(target, 'link');
        return;
      case 'enter':
        // GitHub's "submit comment". The dialog's Update button is never disabled (it is
        // clickable in every state the dialog can be in), so the shortcut mirrors it 1:1.
        event.preventDefault();
        this.onConfirm();
        return;
    }
  }

  /**
   * The Cmd/Ctrl+Shift+P toggle between the Write and Preview tabs of one target. Focus
   * follows the visible pane: the Preview pane (made focusable just for this) when previewing
   * — so pressing the shortcut again still reaches {@link onCommentKeydown} and returns to
   * Write — and the textarea when writing again. No selection bookkeeping is needed: the
   * textarea stays alive (hidden) under Preview, so it keeps its own caret/selection and
   * focusing it back simply reveals them. The focus runs after change detection, which must
   * first create the Preview pane (&#64;if) resp. drop the textarea's display:none class —
   * a hidden element refuses focus.
   */
  private togglePreview(target: StateChangeDialogTarget): void {
    const toPreview = !this.showPreview(target);
    this.setPreview(target, toPreview);
    setTimeout(() => {
      if (toPreview) {
        this.previewPaneOf(target)?.focus();
      } else {
        this.textareaOf(target)?.focus();
      }
    });
  }

  private textareaOf(target: StateChangeDialogTarget): HTMLTextAreaElement | undefined {
    const key = this.key(target);
    return this.commentInputs?.find(ref => ref.nativeElement.dataset.key === key)?.nativeElement;
  }

  private previewPaneOf(target: StateChangeDialogTarget): HTMLElement | undefined {
    const key = this.key(target);
    return this.previewPanes?.find(ref => ref.nativeElement.dataset.key === key)?.nativeElement;
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    const comments: { [key: string]: string } = {};
    for (const target of this.data.targets) {
      const comment = this.comments[this.key(target)];
      if (comment && comment.trim().length > 0) {
        comments[this.key(target)] = comment;
      }
    }
    this.dialogRef.close({confirmed: true, comments});
  }
}
