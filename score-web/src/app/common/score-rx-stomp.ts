import {Injectable} from '@angular/core';
import {RxStomp} from '@stomp/rx-stomp';
import {scoreRxStompConfig} from './score-rx-stomp-config';

@Injectable({
  providedIn: 'root',
})
export class RxStompService extends RxStomp {
  public constructor() {
    super();
  }
}

export function rxStompServiceFactory() {
  const rxStomp = new RxStompService();
  rxStomp.configure(scoreRxStompConfig);
  rxStomp.activate();
  return rxStomp;
}
