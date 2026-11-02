import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatDialogModule} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';

/**
 * Common shell of a dialog built around a `score-table-view`, so that every such dialog has the same paddings, title and close
 * button. The same shell is used by a list screen (`score-list-page`), where the title and the action bar are the page's.
 *
 * Content projection slots:
 *  - `[dialogTitle]`   optional replacement of the plain title (e.g. a title with a library selector)
 *  - (default)         the body, usually the `score-table-view`; use `.dialog-section-title` for the title of a section in it
 *  - `[dialogActions]` buttons of the action bar below the body, aligned to the left, Cancel/Close first and the main action last, as the Back/Next of a `score-list-page` wizard; the bar is hidden when empty
 *
 * The close button (X) closes the dialog without a result.
 */
@Component({
  selector: 'score-dialog-page',
  templateUrl: './dialog-page.component.html',
  styleUrl: './dialog-page.component.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatCardModule, MatDialogModule, MatButtonModule, MatIconModule]
})
export class DialogPageComponent {
  /** Plain title; leave it `null` when the title is given through `[dialogTitle]`. */
  @Input() title: string | null = null;
  /** Stacked right under the title. */
  @Input() subtitle = '';
  @Input() closable = true;
}
