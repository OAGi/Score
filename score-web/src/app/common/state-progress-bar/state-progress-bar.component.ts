import {Component, Input, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {StateProgressBarItem, stateProgressBarQuery} from './state-progress-bar';

@Component({
  standalone: false,
  selector: 'score-state-progress-bar',
  templateUrl: './state-progress-bar.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./state-progress-bar.component.css']
})
export class StateProgressBarComponent implements OnInit {

  @Input()
  data: StateProgressBarItem[] = [];

  constructor() {
  }

  ngOnInit() {
  }

  getParams(item: StateProgressBarItem): string {
    return stateProgressBarQuery(item);
  }

  getWidth(item: StateProgressBarItem): number {
    if (item.value <= 0.0) {
      return 0.0;
    }
    return (item.value / this.sum) * 100.0;
  }

  get sum(): number {
    if (this.data) {
      return this.data.map(e => e.value).reduce((acc, cur) => acc + cur, 0);
    }
    return 0;
  }

}
