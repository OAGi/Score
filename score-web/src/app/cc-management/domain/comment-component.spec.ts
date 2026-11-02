import {of} from 'rxjs';
import {CommentControl} from './comment-component';

/** A comment, reply or edit with nothing but white space is not posted. */
describe('CommentControl blank text', () => {
  const create = () => {
    const service = {postComment: vi.fn(() => of(null)), getComments: () => of([]), editComment: vi.fn(() => of(null))};
    const control = new CommentControl({} as never, service as never);
    control.commentReference = 'ACC-1';
    return {control: control as CommentControl, service};
  };

  it.each([null, undefined, '', '   ', '\n\t'])('hasText is false for %j', text => {
    expect(create().control.hasText(text)).toBe(false);
  });

  it('posts nothing for a blank comment', () => {
    const {control, service} = create();
    control.commentMessage = '   ';
    control.addComment();
    expect(service.postComment).not.toHaveBeenCalled();
  });

  it('posts nothing for an untouched comment box or a blank reply', () => {
    const {control, service} = create();
    control.addComment();
    control.replyMessage = ' ';
    control.addComment({commentId: 1} as never);
    expect(service.postComment).not.toHaveBeenCalled();
  });

  it('posts a comment with text', () => {
    const {control, service} = create();
    control.commentMessage = 'hello';
    control.addComment();
    expect(service.postComment).toHaveBeenCalledWith('ACC-1', 'hello', null);
  });

  it('does not save an edit that is blank', () => {
    const {control, service} = create();
    control.editComment({commentId: 1, textTemp: '  '} as never);
    expect(service.editComment).not.toHaveBeenCalled();
  });
});
