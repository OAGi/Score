import {Component, OnInit} from '@angular/core';
import {switchMap} from 'rxjs/operators';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {SettingsApplicationSettingsService} from '../settings-application-settings/domain/settings-application-settings.service';

@Component({
  selector: 'score-email-validation',
  templateUrl: 'email-validation.component.html',
  styleUrls: ['email-validation.component.css']
})
export class EmailValidationComponent implements OnInit {

  loading: boolean;

  constructor(private service: SettingsApplicationSettingsService,
              private router: Router,
              private route: ActivatedRoute,
              private snackBar: MatSnackBar) {

  }

  ngOnInit(): void {
    this.loading = true;

    const q = this.route.snapshot.queryParamMap.get('q');

    this.service.validateEmail(q).subscribe(_ => {
      this.snackBar.open('Verified', '', {
        duration: 3000,
      });

      this.loading = false;
      this.router.navigateByUrl('/');
    }, error => {
      this.loading = false;
      this.router.navigateByUrl('/');
    });
  }

}
