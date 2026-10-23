import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';

@Component({
  standalone: false,
  selector: 'score-footer',
  templateUrl: './footer.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnInit {

  constructor() {
  }

  ngOnInit() {
  }

  currentYear() {
    return new Date().getFullYear().toString(10).substring(2);
  }
}
