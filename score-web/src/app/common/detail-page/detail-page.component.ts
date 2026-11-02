import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {NgTemplateOutlet} from '@angular/common';
import {BasePageComponent} from '../base-page/base-page.component';

/**
 * Common shell of a detail (or create) screen, derived from `BasePageComponent` (title, toolbar) with content sections and the bottom action buttons.
 *
 * Content projection slots:
 *  - `[detailTitle]`    optional replacement of the plain title (e.g. a title with a library selector)
 *  - `[detailToolbar]`  optional elements shown at the right of the title
 *  - default            the content, normally one or more `<score-detail-card>`
 *  - `[detailActions]`  buttons of the bottom action group (rendered when `showActions` is true)
 *  - `[detailAfterActions]` content rendered below the action group
 */
@Component({
  selector: 'score-detail-page',
  templateUrl: './detail-page.component.html',
  styleUrl: './detail-page.component.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [NgTemplateOutlet, BasePageComponent]
})
export class DetailPageComponent {
  /** Plain title; leave it `null` when the title is given through `[detailTitle]`. */
  @Input() title: string | null = null;
  @Input() subtitle = '';
  @Input() toolbarClass = 'bg-white';
  /** Pushes `[detailToolbar]` to the right of the title. */
  @Input() spacer = true;
  @Input() loading = false;
  @Input() showActions = true;
  /** Bootstrap column class of the action group. */
  @Input() actionsColumn = 'col-12';
  /** Wraps the action buttons in a `.actions` div; some screens place them directly in the column. */
  @Input() actionsWrapper = true;
}
