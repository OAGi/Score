import {Injectable} from '@angular/core';
import {MatSidenav} from '@angular/material/sidenav';
import {
  CcAccNodeDetail,
  CcAsccpNodeDetail,
  CcBccpNodeDetail,
  CcBdtScNodeDetail,
  CcDtNodeDetail,
  CcNodeDetail,
  Comment
} from './core-component-node';
import {CcNodeService} from './core-component-node.service';

@Injectable()
export class CommentControl {

  comments: Comment[] = [];
  commentMessage: string = null;
  commentReference: string = null;
  replyCommentId: number = null;
  replyMessage: string = null;
  replyOpened = false;

  constructor(private sidenav: MatSidenav,
              private service: CcNodeService) {
  }

  /* For type casting of detail property */
  isAccDetail(detail: CcNodeDetail): boolean {
    return (detail !== undefined) && (detail.type.toUpperCase() === 'ACC');
  }

  asAccDetail(detail: CcNodeDetail): CcAccNodeDetail {
    return detail as CcAccNodeDetail;
  }

  isAsccpDetail(detail: CcNodeDetail): boolean {
    return (detail !== undefined) && (detail.type.toUpperCase() === 'ASCCP');
  }

  asAsccpDetail(detail: CcNodeDetail): CcAsccpNodeDetail {
    return detail as CcAsccpNodeDetail;
  }

  isBccpDetail(detail: CcNodeDetail): boolean {
    return (detail !== undefined) && (detail.type.toUpperCase() === 'BCCP');
  }

  asBccpDetail(detail: CcNodeDetail): CcBccpNodeDetail {
    return detail as CcBccpNodeDetail;
  }

  isBdtDetail(detail: CcNodeDetail): boolean {
    return (detail !== undefined) && (detail.type.toUpperCase() === 'DT');
  }

  asBdtDetail(detail: CcNodeDetail): CcDtNodeDetail {
    return detail as CcDtNodeDetail;
  }

  isBdtScDetail(detail: CcNodeDetail): boolean {
    return (detail !== undefined) && (detail.type.toUpperCase() === 'DT_SC');
  }

  asBdtScDetail(detail: CcNodeDetail): CcBdtScNodeDetail {
    return detail as CcBdtScNodeDetail;
  }

  toggleCommentSlide(type: string, detail: CcNodeDetail): void {
    if (!detail) {
      return;
    }
    switch (type.toUpperCase()) {
      case 'ACC':
        this.commentReference = type + '-' + this.asAccDetail(detail).manifestId;
        break;
      case 'ASCC':
        this.commentReference = type + '-' + this.asAsccpDetail(detail).ascc.manifestId;
        break;
      case 'ASCCP':
        this.commentReference = type + '-' + this.asAsccpDetail(detail).asccp.manifestId;
        break;
      case 'BCC':
        this.commentReference = type + '-' + this.asBccpDetail(detail).bcc.manifestId;
        break;
      case 'BCCP':
        this.commentReference = type + '-' + this.asBccpDetail(detail).bccp.manifestId;
        break;
      case 'DT':
        this.commentReference = type + '-' + this.asBdtDetail(detail).manifestId;
        break;
      case 'DT_SC':
        this.commentReference = type + '-' + this.asBdtScDetail(detail).manifestId;
        break;
      default:
        return;
    }

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

  addComment(comment?: Comment) {
    let targetComment = null;
    let message = this.commentMessage;
    if (comment) {
      targetComment = comment.prevCommentId ? comment.prevCommentId : comment.commentId;
      message = this.replyMessage;
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
