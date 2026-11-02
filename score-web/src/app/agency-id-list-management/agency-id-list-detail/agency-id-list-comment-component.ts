import {MatSidenav} from '@angular/material/sidenav';
import {AgencyIdListDetails} from '../domain/agency-id-list';
import {AgencyIdListService} from '../domain/agency-id-list.service';
import {Comment} from '../../cc-management/domain/core-component-node';

export class AgencyIdListCommentControl {
  constructor(
    private sidenav: MatSidenav,
    private service: AgencyIdListService,
  ) {}

  comments: Comment[] = [];
  commentMessage: string = null;
  commentReference: string = null;
  replyCommentId: number = null;
  replyMessage: string = null;
  replyOpened = false;

  toggleCommentSlide(agencyIdList: AgencyIdListDetails): void {
    this.commentReference = 'AGENCY_ID_LIST-' + agencyIdList.agencyIdListManifestId;

    if (this.sidenav.opened) {
      this.closeCommentSlide();
    } else {
      this.service.getComments(this.commentReference).subscribe(comments => {
        this.comments = comments;
        this.sidenav.open();
      });
    }
  }

  closeCommentSlide() {
    if (!this.sidenav || !this.sidenav.opened) {
      return;
    }

    this.comments = [];
    this.commentMessage = null;
    this.commentReference = null;
    this.replyCommentId = null;
    this.replyMessage = null;
    this.replyOpened = false;

    this.sidenav.close();
  }

  openReply(comment: Comment) {
    this.replyCommentId = comment.commentId;
    this.replyMessage = null;
    this.replyOpened = true;
  }

  closeReply() {
    this.replyMessage = null;
    this.replyCommentId = null;
    this.replyOpened = false;
  }

  /** Whether the text has something besides white space; a blank comment, reply or edit is not posted. */
  hasText(text?: string | null): boolean {
    return !!text?.trim();
  }

  addComment(comment?: Comment) {
    let targetComment = null;
    let message = this.commentMessage;
    if (comment) {
      targetComment = comment.prevCommentId ? comment.prevCommentId : comment.commentId;
      message = this.replyMessage;
    }
    if (!this.hasText(message)) {
      return;
    }

    this.service.postComment(this.commentReference, message, targetComment).subscribe(_ => {
      this.service.getComments(this.commentReference).subscribe(comments => {
        this.comments = comments;
        if (this.commentReference && this.replyCommentId && this.replyMessage.length > 0) {
          this.closeReply();
        } else {
          this.commentMessage = '';
        }
      });
    });
  }

  cancelEditComment(comment: Comment) {
    comment.textTemp = comment.text;
    comment.isEditing = false;
  }

  openEditComment(comment: Comment) {
    comment.textTemp = comment.text;
    comment.isEditing = true;
  }

  editComment(comment: Comment) {
    if (!this.hasText(comment.textTemp)) {
      return;
    }
    this.service.editComment(comment.commentId, comment.textTemp).subscribe(_ => {
      comment.text = comment.textTemp;
      comment.isEditing = false;
    });
  }

  deleteComment(comment: Comment) {
    this.service.deleteComment(comment).subscribe(_ => {
      this.service.getComments(this.commentReference).subscribe(comments => {
        this.comments = comments;
      });
    });
  }
}
