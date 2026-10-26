import { Component, OnInit, Input, inject, ChangeDetectionStrategy } from '@angular/core';
import {AuthService} from '../../authentication/auth.service';

@Component({
  standalone: false,
  selector: 'score-settings-menu',
  templateUrl: './settings-menu.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./settings-menu.component.css']
})
export class SettingsMenuComponent implements OnInit {
  /** The highlighted entry: Account's pages (PersonalInfo, Password, Passkeys), Preferences or an ApplicationSettings:<section> page. */
  @Input() active: string | null = null;
  private auth = inject(AuthService);


  ngOnInit(): void {
  }

  /** Account is highlighted on all of its pages. */
  get accountActive(): boolean {
    return ['PersonalInfo', 'Password', 'Passkeys'].includes(this.active);
  }

  readonly applicationSettingsItems = [
    {key: 'features', label: 'Application Features'},
    {key: 'smtp', label: 'SMTP Settings'},
    {key: 'filename-expressions', label: 'Filename Expressions'},
    {key: 'web-page', label: 'Web Page Settings'}
  ];

  /** Application settings is highlighted on all of its pages. */
  get applicationSettingsActive(): boolean {
    return !!this.active && this.active.startsWith('ApplicationSettings');
  }

  /** A local account has a password to change; one that signs in through single sign-on does not. */
  get hasPassword(): boolean {
    const authentication = this.auth.getUserToken().authentication;
    return authentication === 'basic' || authentication === 'passkey';
  }

  get hasPasskeys(): boolean {
    return this.auth.canUsePasskeys();
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

}
