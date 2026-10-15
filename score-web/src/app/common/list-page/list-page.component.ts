import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {MatCardModule} from '@angular/material/card';
import {BasePageComponent} from '../base-page/base-page.component';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

/**
 * Common shell of a list screen, derived from `BasePageComponent` (title + action buttons) with filter, column selector, table and paginator.
 *
 * Content projection slots:
 *  - `[listTitle]`     optional replacement of the plain title (e.g. a title with a library selector)
 *  - `[listActions]`   buttons shown at the right of the title
 *  - `[listHeader]`    optional content above the filter, inside the card (e.g. a notice)
 *  - `[listFilter]`    search bar / filter area
 *  - `[listColumns]`   column selector(s)
 *  - `[listExtra]`     anything rendered after the table card content (e.g. context menus)
 *  - `[listToolbar]`   optional replacement of the whole row above the table (default: the right-aligned `[listColumns]` row)
 *  - `[listFooter]`    content below the card, inside the section (e.g. an action group)
 *  - `[listTable]`     the table and its paginator (rendered inside the elevated table wrapper)
 */
@Component({
  selector: 'score-list-page',
  templateUrl: './list-page.component.html',
  styleUrl: './list-page.component.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [BasePageComponent, MatCardModule, MatProgressSpinnerModule]
})
export class ListPageComponent {
  /** Plain title; leave it `null` when the title is given through `[listTitle]`. */
  @Input() title: string | null = null;
  @Input() subtitle = '';
  @Input() loading = false;
  /** Class(es) of the title toolbar. */
  @Input() toolbarClass = 'bg-white';
  /** Pushes `[listActions]` to the right of the title. */
  @Input() spacer = true;
  /** Renders the row above the table (the `[listColumns]` selector or a `[listToolbar]`). */
  @Input() showColumns = true;
  @Input() tableWrapperClass = 'mat-elevation-z1 table-wrapper mt-3';
}
