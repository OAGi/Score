import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatCardModule} from '@angular/material/card';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

/**
 * The part shared by every screen: the page section with a title toolbar. It knows nothing about the body,
 * so it can be used as is by a screen with a unique layout, and it is the base of the list / detail / wizard pages.
 *
 * Content projection slots:
 *  - `[baseTitle]`    custom title content (rendered after the plain `title` when that is set)
 *  - `[baseActions]`  elements shown at the right of the title
 *  - default          the body of the screen
 */
@Component({
  selector: 'score-base-page',
  templateUrl: './base-page.component.html',
  styleUrl: './base-page.component.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatToolbarModule, MatCardModule, MatProgressSpinnerModule]
})
export class BasePageComponent {
  /** Plain title; leave it `null` when the title is given through `[baseTitle]`. */
  @Input() title: string | null = null;
  @Input() subtitle = '';
  /** Shows a spinner over the whole section. */
  @Input() loading = false;
  @Input() toolbarClass = 'bg-white';
  /** Pushes `[baseActions]` to the right of the title. */
  @Input() spacer = true;
  /** Renders the title toolbar; a screen with its own header can switch it off. */
  @Input() showToolbar = true;
}
