import {of, throwError} from 'rxjs';
import {vi} from 'vitest';
import {MessageViewComponent} from './message-view.component';

describe('MessageViewComponent', () => {
  it('should be defined', () => {
    expect(MessageViewComponent).toBeTruthy();
  });

  function open(error: any) {
    const ctx: any = {
      route: {paramMap: of({get: () => '7'})},
      messageService: {getMessage: () => throwError(() => error)},
      snackBar: {open: vi.fn()},
      router: {navigateByUrl: vi.fn()}
    };
    MessageViewComponent.prototype.ngOnInit.call(ctx);
    return ctx;
  }

  it('goes back to the messages with a message when the message does not exist', () => {
    const ctx = open({status: 400});
    expect(ctx.snackBar.open).toHaveBeenCalledWith('The requested message is unavailable.', '', {duration: 3000});
    expect(ctx.router.navigateByUrl).toHaveBeenCalledWith('/message');
    expect(ctx.loading).toBe(false);
  });

  it('says so when the message belongs to someone else', () => {
    const ctx = open({status: 403});
    expect(ctx.snackBar.open).toHaveBeenCalledWith('You do not have access permission.', '', {duration: 3000});
    expect(ctx.router.navigateByUrl).toHaveBeenCalledWith('/message');
  });
});
