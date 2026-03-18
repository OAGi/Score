import { Component, OnInit, inject } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {SettingsApplicationSettingsService} from '../settings-application-settings/domain/settings-application-settings.service';

@Component({
  standalone: false,
  selector: 'score-email-validation',
  templateUrl: 'email-validation.component.html',
  styleUrls: ['email-validation.component.css']
})
export class EmailValidationComponent implements OnInit {
  private service = inject(SettingsApplicationSettingsService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);


  loading: boolean;

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
