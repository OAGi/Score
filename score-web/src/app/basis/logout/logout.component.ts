import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import {AuthService} from '../../authentication/auth.service';

@Component({
  standalone: false,
  selector: 'score-logout',
  templateUrl: './logout.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./logout.component.css']
})
export class LogoutComponent implements OnInit {
  auth = inject(AuthService);


  ngOnInit(): void {
    this.auth.logout();
  }
}
