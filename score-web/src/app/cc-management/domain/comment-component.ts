import {Injectable} from '@angular/core';
import {MatSidenav} from '@angular/material/sidenav';
import {CcAccNodeInfo, CcAsccpNodeInfo, CcBccpNodeInfo, CcDtNodeInfo, CcDtScNodeInfo, CcNodeInfo, Comment} from './core-component-node';
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
  isAccDetail(detail: CcNodeInfo): boolean {
    return (detail !== undefined) && (detail.type.toUpperCase() === 'ACC');
  }

  asAccDetail(detail: CcNodeInfo): CcAccNodeInfo {
    return detail as CcAccNodeInfo;
  }

  isAsccpDetail(detail: CcNodeInfo): boolean {
    return (detail !== undefined) && (detail.type.toUpperCase() === 'ASCCP');
  }

  asAsccpDetail(detail: CcNodeInfo): CcAsccpNodeInfo {
    return detail as CcAsccpNodeInfo;
  }

  isBccpDetail(detail: CcNodeInfo): boolean {
    return (detail !== undefined) && (detail.type.toUpperCase() === 'BCCP');
  }

  asBccpDetail(detail: CcNodeInfo): CcBccpNodeInfo {
    return detail as CcBccpNodeInfo;
  }

  isDtDetail(detail: CcNodeInfo): boolean {
    return (detail !== undefined) && (detail.type.toUpperCase() === 'DT');
  }

  asDtDetail(detail: CcNodeInfo): CcDtNodeInfo {
    return detail as CcDtNodeInfo;
  }

  isDtScDetail(detail: CcNodeInfo): boolean {
    return (detail !== undefined) && (detail.type.toUpperCase() === 'DT_SC');
  }

  asDtScDetail(detail: CcNodeInfo): CcDtScNodeInfo {
    return detail as CcDtScNodeInfo;
  }

  toggleCommentSlide(type: string, detail: CcNodeInfo): void {
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
        this.commentReference = type + '-' + this.asDtDetail(detail).manifestId;
        break;
      case 'DT_SC':
        this.commentReference = type + '-' + this.asDtScDetail(detail).manifestId;
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
