import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {MatCardModule} from '@angular/material/card';
import {BasePageComponent} from '../base-page/base-page.component';

/**
 * Common shell of a list screen, derived from `BasePageComponent` (title + action buttons) around a `score-table-view`, which brings
 * its own filter, columns, table and paginator.
 *
 * Content projection slots:
 *  - `[listTitle]`     optional replacement of the plain title (e.g. a title with a library selector)
 *  - `[listActions]`   buttons shown at the right of the title
 *  - `[listHeader]`    optional content above the table view, inside the card (e.g. a notice)
 *  - (default)         the `score-table-view`
 *  - `[listExtra]`     anything rendered after the table view (e.g. context menus)
 *  - `[listFooter]`    content below the card, inside the section
 *  - `[listActionBar]` buttons of the action group below the card (e.g. Back / Next of a wizard step); the group is hidden when empty
 */
@Component({
  selector: 'score-list-page',
  templateUrl: './list-page.component.html',
  styleUrl: './list-page.component.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [BasePageComponent, MatCardModule]
})
export class ListPageComponent {
  /** Plain title; leave it `null` when the title is given through `[listTitle]`. */
  @Input() title: string | null = null;
  @Input() subtitle = '';
  /** Class(es) of the title toolbar; a sub-title is stacked under the title on top of them. */
  @Input() toolbarClass = 'bg-white';
  /** Pushes `[listActions]` to the right of the title. */
  @Input() spacer = true;

  get effectiveToolbarClass(): string {
    return this.subtitle ? `${this.toolbarClass} d-flex flex-column align-items-start` : this.toolbarClass;
  }
}
