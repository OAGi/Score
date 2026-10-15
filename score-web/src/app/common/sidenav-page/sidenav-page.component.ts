import {ChangeDetectionStrategy, Component, Input, ViewChild} from '@angular/core';
import {MatSidenav} from '@angular/material/sidenav';
import {Comment} from '../../cc-management/domain/core-component-node';

/** What the comment drawer needs from a comment control (shared by the CC, code list and agency ID list controls). */
export interface CommentDrawerControl {
  comments: Comment[];
  commentMessage: string;
  replyCommentId: number;
  replyMessage: string;
  replyOpened: boolean;
  closeCommentSlide(): void;
  deleteComment(comment: Comment): void;
  openEditComment(comment: Comment): void;
  editComment(comment: Comment): void;
  cancelEditComment(comment: Comment): void;
  openReply(comment: Comment): void;
  closeReply(): void;
  addComment(comment?: Comment): void;
}

/**
 * A screen with a comment drawer (`mat-sidenav`) beside the content, derived from `BasePageComponent`:
 * the content is rendered in a base page whose title toolbar is optional.
 *
 * Content projection slots:
 *  - `[sidenavTitle]`    custom title content (when `title` is not used)
 *  - `[sidenavActions]`  elements shown at the right of the title
 *  - default             the body of the screen
 *
 * The host component reaches the drawer through the `sidenav` property of this component.
 */
@Component({
  selector: 'score-sidenav-page',
  standalone: false,
  templateUrl: './sidenav-page.component.html',
  styleUrl: './sidenav-page.component.css',
  changeDetection: ChangeDetectionStrategy.Eager
})
export class SidenavPageComponent {
  @ViewChild('sidenav', {static: true}) sidenav: MatSidenav;

  /** Renders the comment drawer content; without it the drawer stays empty. */
  @Input() commentControl: CommentDrawerControl | null = null;
  @Input() currentUser = '';
  /** Plain title; leave it `null` when the title is given through `[sidenavTitle]`. */
  @Input() title: string | null = null;
  @Input() subtitle = '';
  @Input() toolbarClass = 'bg-white';
  @Input() spacer = true;
  /** Renders the title toolbar. */
  @Input() showToolbar = true;
  /** Shows a spinner over the page section. */
  @Input() loading = false;
  /** Shows the indeterminate progress bar at the top of the content. */
  @Input() progress = false;
}
