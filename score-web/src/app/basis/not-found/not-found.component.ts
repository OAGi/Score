import {Component, ChangeDetectionStrategy} from '@angular/core';

@Component({
  standalone: false,
  selector: 'score-not-found',
  templateUrl: './not-found.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./not-found.component.css']
})
export class NotFoundComponent {
}
