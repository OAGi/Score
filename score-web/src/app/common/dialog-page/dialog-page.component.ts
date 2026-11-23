import {AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, Input, OnDestroy, ViewChild} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatDialogModule} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {MatIconButtonSizesModule} from 'mat-icon-button-sizes';

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
 *
 * `actionsShown`: for a dialog whose main action needs a selection. On a screen narrower than a desktop (under 1024px) the action bar then sticks to the bottom of
 * the dialog, slides up when `actionsShown` is true and down when it is false, so that it is reachable without scrolling to the end
 * of a long list. On a desktop it is always shown in place. Leave it `null` (default) for the plain, always-shown bar.
 */
@Component({
  selector: 'score-dialog-page',
  templateUrl: './dialog-page.component.html',
  styleUrl: './dialog-page.component.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  host: {
    '[class.actions-conditional]': 'actionsShown !== null',
    '[class.actions-shown]': 'actionsShown === true',
    '[class.actions-floating]': 'floating'
  },
  // MatIconButtonSizesModule: what makes `mat-small-icon-button` of the close button the small button of the app
  imports: [MatCardModule, MatDialogModule, MatButtonModule, MatIconModule, MatIconButtonSizesModule]
})
export class DialogPageComponent implements AfterViewInit, OnDestroy {
  /** Plain title; leave it `null` when the title is given through `[dialogTitle]`. */
  @Input() title: string | null = null;
  /** Stacked right under the title. */
  @Input() subtitle = '';
  @Input() closable = true;
  /** Whether the action bar is shown on a screen narrower than a desktop (see above); `null`: always shown. */
  @Input() actionsShown: boolean | null = null;

  /** The bar floats over the content (its own place is below the visible area), so it casts a shadow; once the end of the content is reached, where the sentinel after the bar is, it does not. */
  floating = false;

  @ViewChild('sentinel', {static: true}) private sentinel!: ElementRef<HTMLElement>;
  private observer?: IntersectionObserver;

  ngAfterViewInit(): void {
    if (typeof IntersectionObserver === 'undefined') {
      return;
    }
    this.observer = new IntersectionObserver(([entry]) => this.floating = !entry.isIntersecting);
    this.observer.observe(this.sentinel.nativeElement);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}
