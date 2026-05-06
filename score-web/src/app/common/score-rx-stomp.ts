import {Injectable} from '@angular/core';
import {HttpClient, HttpContext} from '@angular/common/http';
import {RxStomp, RxStompState} from '@stomp/rx-stomp';
import {catchError, filter, map, of, Subscription, switchMap, take, timer} from 'rxjs';
import {scoreRxStompConfig} from './score-rx-stomp-config';
import {SUPPRESS_ERROR_ALERT} from '../authentication/auth.service';

interface GatewayHealthResponse {
  ready?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class RxStompService extends RxStomp {
  private healthPingSubscription?: Subscription;
  private connectionStateSubscription?: Subscription;
  private shouldReconnect = false;

  public constructor (private http: HttpClient) {
    super();
    this.connectionStateSubscription = this.connectionState$.subscribe(state => {
      if (state === RxStompState.OPEN) {
        this.stopHealthPingLoop();
        return;
      }

      if (state === RxStompState.CLOSED && this.shouldReconnect && !this.active) {
        this.startHealthPingLoop();
      }
    });
  }

  override activate(): void {
    this.shouldReconnect = true;
    this.startHealthPingLoop();
  }

  override deactivate(options?: { force?: boolean }): Promise<void> {
    this.shouldReconnect = false;
    this.stopHealthPingLoop();
    return super.deactivate(options);
  }

  private startHealthPingLoop(): void {
    if (this.active || this.healthPingSubscription) {
      return;
    }

    this.healthPingSubscription = timer(0, 500).pipe(
      switchMap(() => this.checkGatewayHealth()),
      filter(Boolean),
      take(1)
    ).subscribe(() => {
      this.stopHealthPingLoop();
      if (this.shouldReconnect && !this.active) {
        super.activate();
      }
    });
  }

  private stopHealthPingLoop(): void {
    this.healthPingSubscription?.unsubscribe();
    this.healthPingSubscription = undefined;
  }

  private checkGatewayHealth() {
    return this.http.get<GatewayHealthResponse | null>('/api/health', {
      context: new HttpContext().set(SUPPRESS_ERROR_ALERT, true)
    }).pipe(
      map(response => response?.ready ?? true),
      catchError(() => of(false))
    );
  }
}

export function rxStompServiceFactory(http: HttpClient) {
  const rxStomp = new RxStompService(http);
  rxStomp.configure(scoreRxStompConfig);
  rxStomp.activate();
  return rxStomp;
}
