import {Injectable} from '@angular/core';
import {HttpClient, HttpContext} from '@angular/common/http';
import {RxStomp, RxStompState} from '@stomp/rx-stomp';
import {catchError, filter, map, of, Subscription, switchMap, take, timer} from 'rxjs';
import {scoreRxStompConfig} from './score-rx-stomp-config';
import {SUPPRESS_ERROR_ALERT} from '../authentication/auth.service';

interface GatewayHealthResponse {
  ready?: boolean;
}

const HEALTH_PING_INTERVAL_MS = 500;
const STABLE_CONNECTION_MS = 10000;
const MAX_RECONNECT_DELAY_MS = 30000;

@Injectable({
  providedIn: 'root',
})
export class RxStompService extends RxStomp {
  private healthPingSubscription?: Subscription;
  private connectionStateSubscription?: Subscription;
  private shouldReconnect = false;
  private reconnecting = false;
  private connectedAt = 0;
  private consecutiveFailures = 0;

  public constructor (private http: HttpClient) {
    super();
    this.connectionStateSubscription = this.connectionState$.subscribe(state => {
      if (state === RxStompState.OPEN) {
        this.connectedAt = Date.now();
        this.stopHealthPingLoop();
        return;
      }

      if (state === RxStompState.CLOSED && this.shouldReconnect) {
        this.noteConnectionLost();
        this.reconnect();
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

  /**
   * A connection that did not stay open for STABLE_CONNECTION_MS counts as a failure, which backs the next reconnect
   * off so that a gateway that is healthy but refuses the WebSocket is not hit in a tight loop.
   */
  private noteConnectionLost(): void {
    const stable = this.connectedAt > 0 && Date.now() - this.connectedAt >= STABLE_CONNECTION_MS;
    this.consecutiveFailures = stable ? 0 : this.consecutiveFailures + 1;
    this.connectedAt = 0;
  }

  private reconnectDelay(): number {
    return this.consecutiveFailures <= 1 ? 0 :
      Math.min(MAX_RECONNECT_DELAY_MS, 1000 * 2 ** (this.consecutiveFailures - 2));
  }

  /**
   * The client stays ACTIVE when its WebSocket closes unexpectedly (it only schedules a reconnect with a delay, and the
   * delay is 0), so it is deactivated first for the health ping loop to be able to activate it again.
   */
  private reconnect(): void {
    if (this.reconnecting || this.healthPingSubscription) {
      return;
    }
    this.reconnecting = true;
    super.deactivate().finally(() => {
      this.reconnecting = false;
      if (this.shouldReconnect) {
        this.startHealthPingLoop();
      }
    });
  }

  private startHealthPingLoop(): void {
    if (this.active || this.healthPingSubscription) {
      return;
    }

    this.healthPingSubscription = timer(this.reconnectDelay(), HEALTH_PING_INTERVAL_MS).pipe(
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
