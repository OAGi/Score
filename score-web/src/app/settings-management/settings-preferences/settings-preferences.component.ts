import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmDialogService} from '../../common/confirm-dialog/confirm-dialog.service';
import {DomSanitizer} from '@angular/platform-browser';
import {PreferencesInfo} from './domain/preferences';
import {SettingsPreferencesService} from './domain/settings-preferences.service';

@Component({
  selector: 'score-settings-preferences',
  templateUrl: './settings-preferences.component.html',
  styleUrls: ['./settings-preferences.component.css']
})
export class SettingsPreferencesComponent implements OnInit {

  preferencesInfo: PreferencesInfo;

  title = 'Preferences';
  loading = false;

  constructor(private auth: AuthService,
              private sanitizer: DomSanitizer,
              private confirmDialogService: ConfirmDialogService,
              private preferencesService: SettingsPreferencesService,
              private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.loading = true;

    this.preferencesService.load(this.auth.getUserToken()).subscribe(preferencesInfo => {
      this.preferencesInfo = preferencesInfo;
      this.loading = false;
    }, error => {
      this.loading = false;
    });
  }

  toggleSelection(column: {
    name: string,
    selected: boolean
  }) {
    column.selected = !column.selected;
  }

  updatePreferences() {
    this.preferencesService.update(this.auth.getUserToken(), this.preferencesInfo).subscribe(_ => {
      this.snackBar.open('Updated', '', {
        duration: 3000,
      });
    });
  }

}
