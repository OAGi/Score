/**
 * Shared copy + sentinel for the "Cancel revision" flow (issue #1533 follow-up).
 *
 * Cancelling a revision permanently discards every change made in the in-progress (WIP) revision
 * and reverts the component to its previously released revision. Two things are layered on the
 * cancel confirmation:
 *
 *  - a strong data-loss warning ({@link cancelRevisionContent}), shown in BOTH the generic
 *    ConfirmDialog (GitHub off / not connected) and the GitHub {@code StateChangeDialogComponent}
 *    (GitHub on), so the warning is never lost regardless of GitHub status; and
 *  - when GitHub is enabled, a pre-filled comment that is posted to the component's linked issues.
 *
 * Cancel is NOT a {@code CcState.canMove} transition, so it cannot reuse {@code usesStateChangeDialog};
 * the detail components instead pass {@link CANCEL_REVISION_TO_STATE} as the dialog's {@code toState}
 * so the comment box is pre-filled by {@code cancelPost()} (see github-status-post-renderer).
 *
 * Developers "revise" a Published component; end-users "amend" a Production one — the wording follows
 * the same developer/end-user split the rest of the cancel UI already uses.
 */
export const CANCEL_REVISION_TO_STATE = 'CancelRevision';

export function cancelRevisionHeader(isDeveloper: boolean): string {
  return isDeveloper ? 'Cancel this revision?' : 'Cancel this amendment?';
}

export function cancelRevisionContent(isDeveloper: boolean): string[] {
  const what = isDeveloper ? 'revision' : 'amendment';
  return [
    'Are you sure you want to cancel this ' + what + '?',
    'Warning: all work done in this ' + what + ' will be permanently removed and cannot be recovered.',
  ];
}
