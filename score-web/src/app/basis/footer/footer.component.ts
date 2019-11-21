import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'srt-footer',
  templateUrl: './footer.component.html',
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
