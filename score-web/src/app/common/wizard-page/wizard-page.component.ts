import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {ListPageComponent} from '../list-page/list-page.component';

/**
 * A step of a wizard whose body is a list: the `ListPageComponent` shell with a sub-title under the title
 * and an action group (Back / Next ...) below the card.
 *
 * Content projection slots:
 *  - `[wizardTitle]`    optional replacement of the title and sub-title (e.g. a title with a library selector)
 *  - (default)          the `score-table-view`
 *  - `[listExtra]`      forwarded to the list shell
 *  - `[wizardActions]`  buttons of the bottom action group
 */
@Component({
  selector: 'score-wizard-page',
  templateUrl: './wizard-page.component.html',
  styleUrl: './wizard-page.component.css',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [ListPageComponent]
})
export class WizardPageComponent {
  /** Plain title; leave it `null` when the title is given through `[wizardTitle]`. */
  @Input() title: string | null = null;
  @Input() subtitle = '';
  @Input() toolbarClass = 'bg-white d-flex flex-column align-items-start';
  @Input() spacer = false;
}
