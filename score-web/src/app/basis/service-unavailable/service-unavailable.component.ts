import {HttpClient, HttpContext, HttpErrorResponse} from '@angular/common/http';
import {Component, OnDestroy, OnInit, inject} from '@angular/core';
import {Router} from '@angular/router';
import {ActivatedRoute} from '@angular/router';
import {catchError, map, switchMap, takeUntil} from 'rxjs/operators';
import {of, Subject, timer} from 'rxjs';
import {SUPPRESS_ERROR_ALERT} from '../../authentication/auth.service';

type GatewayHealthState = 'ready' | 'warming' | 'unavailable';

@Component({
  standalone: false,
  selector: 'score-service-unavailable',
  templateUrl: './service-unavailable.component.html',
  styleUrls: ['./service-unavailable.component.css']
})
export class ServiceUnavailableComponent implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroy$ = new Subject<void>();
  isWarmingUp = false;

  ngOnInit(): void {
    if (this.reason !== 'gateway') {
      return;
    }

    timer(0, 3000).pipe(
      switchMap(() => this.checkGatewayHealth()),
      takeUntil(this.destroy$)
    ).subscribe(state => {
      if (state === 'ready') {
        this.router.navigateByUrl('/');
        return;
      }

      this.isWarmingUp = state === 'warming';
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get statusCode(): string {
    return this.route.snapshot.queryParamMap.get('status') ||
      (this.reason === 'service' ? '503' : '500');
  }

  get eyebrow(): string {
    if (this.reason === 'service') {
      return 'Temporary platform outage';
    }
    return this.isWarmingUp ? 'Server warm-up' : 'Connectivity interruption';
  }

  get title(): string {
    if (this.reason === 'service') {
      return 'Gateway Service Unavailable';
    }
    return this.isWarmingUp ? 'The server is warming up' : 'Gateway Connection Failure';
  }

  get message(): string {
    if (this.reason === 'service') {
      return 'The platform is up, but the gateway layer is temporarily unavailable. Give it a moment and try again.';
    }
    return this.isWarmingUp
      ? 'score-http is starting or restarting. This page will keep checking the gateway and return you to the application once it is ready.'
      : 'The application cannot reach the gateway right now. This is usually temporary and often resolves after a quick retry.';
  }

  get reason(): string {
    return this.route.snapshot.queryParamMap.get('reason') || 'gateway';
  }

  private checkGatewayHealth() {
    return this.http.get('/api/health', {
      context: new HttpContext().set(SUPPRESS_ERROR_ALERT, true)
    }).pipe(
      map((): GatewayHealthState => 'ready'),
      catchError(error => of(this.healthErrorState(error)))
    );
  }

  private healthErrorState(error: unknown): GatewayHealthState {
    if (!(error instanceof HttpErrorResponse)) {
      return 'unavailable';
    }

    const bodyStatus = typeof error.error?.status === 'string' ? error.error.status : undefined;
    if (error.status === 503 && !!bodyStatus) {
      return 'warming';
    }

    return 'unavailable';
  }
}
