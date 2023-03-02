import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';
import {SettingsApplicationSettingsService} from './domain/settings-application-settings.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'score-settings-application-settings',
  templateUrl: './settings-application-settings.component.html',
  styleUrls: ['./settings-application-settings.component.css']
})
export class SettingsApplicationSettingsComponent implements OnInit {

  title = 'Application settings';

  constructor(private auth: AuthService,
              private settingsService: SettingsApplicationSettingsService,
              private confirmDialogService: ConfirmDialogService,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
  }

  get isTenantEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.tenant.enabled;
  }

  get isBusinessTermEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.businessTerm.enabled;
  }

  updateTenantConfiguration(value: boolean) {
    const dialogConfig = this.confirmDialogService.newConfig();
    if (value) {
      dialogConfig.data.header = 'Enable multi-tenant mode?';
      dialogConfig.data.content = ['Are you sure you want to switch Score instance to multi-tenant mode?',
        'In the multi-tenant mode, some functionalities such as creating user extensions, BIE reuse, ',
        'and BIE uplifting will be unavailable.'];
      dialogConfig.data.action = 'Enable';
    } else {
      dialogConfig.data.header = 'Disable multi-tenant mode?';
      dialogConfig.data.content = ['Are you sure you want to switch Score instance to single tenant mode?',
        'You may lose some data in regard to tenants.'];
      dialogConfig.data.action = 'Disable';
    }

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.settingsService.updateTenantConfiguration(value).subscribe(_ => {
            this.auth.reloadUserToken().subscribe(userToken => {
              if (userToken.tenant.enabled === value) {
                this.snackBar.open('Updated', '', {
                  duration: 3000,
                });
              }
            });
          });
        }
      });
  }

  updateBusinessTermConfiguration(value: boolean) {
    this.settingsService.updateBusinessTermConfiguration(value).subscribe(_ => {
      this.auth.reloadUserToken().subscribe(userToken => {
        if (userToken.businessTerm.enabled === value) {
          this.snackBar.open('Updated', '', {
            duration: 3000,
          });
        }
      });
    });
  }

}
