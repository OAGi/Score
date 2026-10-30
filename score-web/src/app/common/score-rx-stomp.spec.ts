import {HttpClient} from '@angular/common/http';
import {RxStomp, RxStompState} from '@stomp/rx-stomp';
import {of} from 'rxjs';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {RxStompService} from './score-rx-stomp';

describe('RxStompService reconnect', () => {
  let service: RxStompService;
  let activate: ReturnType<typeof vi.spyOn>;
  let deactivate: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    vi.useFakeTimers();
    // The client stays ACTIVE after an unexpected close, which is what stops a reconnect that waits for !active.
    let clientActive = false;
    activate = vi.spyOn(RxStomp.prototype, 'activate').mockImplementation(() => {
      clientActive = true;
    });
    deactivate = vi.spyOn(RxStomp.prototype, 'deactivate').mockImplementation(async () => {
      clientActive = false;
    });
    vi.spyOn(RxStomp.prototype, 'active', 'get').mockImplementation(() => clientActive);
    const http = {get: () => of({ready: true})} as unknown as HttpClient;
    service = new RxStompService(http);
    service.activate();
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  const changeState = (state: RxStompState) => (service as any)._changeState(state);

  it('connects once the gateway is healthy', async () => {
    await vi.advanceTimersByTimeAsync(0);

    expect(activate).toHaveBeenCalledTimes(1);
  });

  it('deactivates and activates the client again after the connection is lost', async () => {
    await vi.advanceTimersByTimeAsync(0);
    changeState(RxStompState.OPEN);
    activate.mockClear();

    changeState(RxStompState.CLOSED);
    await vi.advanceTimersByTimeAsync(600);

    expect(deactivate).toHaveBeenCalledTimes(1);
    expect(activate).toHaveBeenCalledTimes(1);
  });

  it('does not reconnect after an explicit deactivate', async () => {
    await vi.advanceTimersByTimeAsync(0);
    await service.deactivate();
    activate.mockClear();
    deactivate.mockClear();

    changeState(RxStompState.CLOSED);
    await vi.advanceTimersByTimeAsync(600);

    expect(activate).not.toHaveBeenCalled();
  });
});
