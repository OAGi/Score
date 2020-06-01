import {Component, Input, OnInit} from '@angular/core';
import {StateProgressBarItem} from './state-progress-bar';
import {HttpParams} from '@angular/common/http';
import {Base64} from 'js-base64';

@Component({
  selector: 'score-state-progress-bar',
  templateUrl: './state-progress-bar.component.html',
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
    if (item.href.length == 1) {
      return '';
    }
    let params = new HttpParams();
    for (const param of item.href[1]) {
      params = params.set(param['key'], param['value']);
    }
    return Base64.encode(params.toString());
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
