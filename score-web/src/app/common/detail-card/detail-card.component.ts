import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatCardModule} from '@angular/material/card';

/**
 * A padded card section of a detail screen, with an optional sub-heading above the card.
 * Elements marked `cardActions` are rendered below the card content (e.g. a `mat-card-actions`).
 */
@Component({
  selector: 'score-detail-card',
  templateUrl: './detail-card.component.html',
  styleUrl: './detail-card.component.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatToolbarModule, MatCardModule]
})
export class DetailCardComponent {
  @Input() heading = '';
  /** Extra class(es) of the card, e.g. a bottom margin. */
  @Input() cardClass = '';
}
