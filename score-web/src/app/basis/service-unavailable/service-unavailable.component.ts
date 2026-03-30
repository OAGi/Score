import {HttpClient, HttpContext} from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import {Router} from '@angular/router';
import {ActivatedRoute} from '@angular/router';
import {catchError} from 'rxjs/operators';
import {of} from 'rxjs';
import {SUPPRESS_ERROR_ALERT} from '../../authentication/auth.service';

@Component({
  standalone: false,
  selector: 'score-service-unavailable',
  templateUrl: './service-unavailable.component.html',
  styleUrls: ['./service-unavailable.component.css']
})
export class ServiceUnavailableComponent implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  ngOnInit(): void {
    if (this.reason !== 'gateway') {
      return;
    }

    this.http.get('/api/info/oauth2-providers', {
      context: new HttpContext().set(SUPPRESS_ERROR_ALERT, true)
    }).pipe(
      catchError(() => of(undefined))
    ).subscribe(resp => {
      if (resp !== undefined) {
        this.router.navigateByUrl('/');
      }
    });
  }

  get statusCode(): string {
    return this.reason === 'service' ? '503' : '504';
  }

  get eyebrow(): string {
    return this.reason === 'service'
      ? 'Temporary platform outage'
      : 'Connectivity interruption';
  }

  get title(): string {
    return this.reason === 'service'
      ? 'Gateway Service Unavailable'
      : 'Gateway Connection Failure';
  }

  get message(): string {
    return this.reason === 'service'
      ? 'The platform is up, but the gateway layer is temporarily unavailable. Give it a moment and try again.'
      : 'The application cannot reach the gateway right now. This is usually temporary and often resolves after a quick retry.';
  }

  get reason(): string {
    return this.route.snapshot.queryParamMap.get('reason') || 'gateway';
  }
}
