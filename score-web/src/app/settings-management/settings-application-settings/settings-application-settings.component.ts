import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';
import {SettingsApplicationSettingsService} from './domain/settings-application-settings.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {AboutService} from '../../basis/about/domain/about.service';
import {SignInPageInfo} from '../../basis/about/domain/about';

@Component({
  selector: 'score-settings-application-settings',
  templateUrl: './settings-application-settings.component.html',
  styleUrls: ['./settings-application-settings.component.css']
})
export class SettingsApplicationSettingsComponent implements OnInit {

  title = 'Application settings';
  signInPageInfo: SignInPageInfo;

  constructor(private auth: AuthService,
              private aboutService: AboutService,
              private settingsService: SettingsApplicationSettingsService,
              private confirmDialogService: ConfirmDialogService,
              private snackBar: MatSnackBar) {
    aboutService.getSignInPageInfo().subscribe(resp => {
      this.signInPageInfo = resp;
    });
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

  get isBIEInverseModeEnabled(): boolean {
    const userToken = this.auth.getUserToken();
    return userToken.bie.inverseMode;
  }

  updateTenantConfiguration(value: boolean) {
    const dialogConfig = this.confirmDialogService.newConfig();
    if (value) {
      dialogConfig.data.header = 'Enable multi-tenant mode?';
      dialogConfig.data.content = ['Are you sure you want to switch Score instance to multi-tenant mode?',
        'In the multi-tenant mode, some functionalities such as creating user extensions, BIE reuse, ',
        'BIE uplifting, and business term function will be unavailable.'];
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
    const dialogConfig = this.confirmDialogService.newConfig();
    if (value) {
      dialogConfig.data.header = 'Enable business term function?';
      dialogConfig.data.content = ['Are you sure you want to enable the business term function?',
        'You may lose legacy business term data in BIEs.'];
      dialogConfig.data.action = 'Enable';
    } else {
      dialogConfig.data.header = 'Disable business term function?';
      dialogConfig.data.content = ['Are you sure you want to disable the business term function?',
        'You may lose some data in regard to business terms.'];
      dialogConfig.data.action = 'Disable';
    }

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
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
      });
  }

  updateBIEInverseModeConfiguration(value: boolean) {
    const dialogConfig = this.confirmDialogService.newConfig();
    if (value) {
      dialogConfig.data.header = 'Enable BIE inverse mode?';
      dialogConfig.data.content = ['Are you sure you want to enable the BIE inverse mode?',
        'The BIE expression could be failed if system does not have enough memory to express a large size of BIEs.'];
      dialogConfig.data.action = 'Enable';
    } else {
      dialogConfig.data.header = 'Disable BIE inverse mode?';
      dialogConfig.data.content = ['Are you sure you want to disable the BIE inverse mode?'];
      dialogConfig.data.action = 'Disable';
    }

    this.confirmDialogService.open(dialogConfig).afterClosed()
      .subscribe(result => {
        if (result) {
          this.settingsService.updateBIEInverseModeConfiguration(value).subscribe(_ => {
            this.auth.reloadUserToken().subscribe(userToken => {
              if (userToken.bie.inverseMode === value) {
                this.snackBar.open('Updated', '', {
                  duration: 3000,
                });
              }
            });
          });
        }
      });
  }

  updateSignInPageInfoConfiguration() {
    this.settingsService.updateConfiguration(this.signInPageInfo.paramKey, this.signInPageInfo.statement).subscribe(_ => {
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    });
  }

}
