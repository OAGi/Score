import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';

@Component({
  standalone: false,
  selector: 'score-join',
  templateUrl: './join.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./join.component.css']
})
export class JoinComponent implements OnInit {

  constructor() {
  }

  ngOnInit() {
  }

}
