import {Attribute, Component, OnInit} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';

@Component({
  selector: 'score-settings-menu',
  templateUrl: './settings-menu.component.html',
  styleUrls: ['./settings-menu.component.css']
})
export class SettingsMenuComponent implements OnInit {

  constructor(@Attribute('active') public active: string,
              private auth: AuthService) {
  }

  ngOnInit(): void {
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

}
