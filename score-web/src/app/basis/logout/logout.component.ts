import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';

@Component({
  standalone: false,
  selector: 'score-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.css']
})
export class LogoutComponent implements OnInit {

  constructor(public auth: AuthService) {
  }

  ngOnInit(): void {
    this.auth.logout();
  }
}
