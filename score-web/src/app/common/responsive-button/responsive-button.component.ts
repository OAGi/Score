import {booleanAttribute, ChangeDetectionStrategy, Component, input, output} from '@angular/core';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';
import {RouterLink} from '@angular/router';

/**
 * A button of the toolbar of a page that keeps to its place on a narrow screen. On a wide one it is a button with its text ("New Module
 * Set", "Discard"); on a narrow one, where the text would not fit beside the title (see `max-width` in the stylesheet), an icon button, which
 * the text is the tooltip of: `+` for a button that makes a record (the one of the other lists), a trash can for one that discards.
 *
 * ```html
 * <score-responsive-button label="New Module Set" (pressed)="create()"/>
 * <score-responsive-button label="New Account" link="create"/>
 * <score-responsive-button label="Discard" icon="delete" color="warn" [disabled]="!selection.hasValue()" (pressed)="discard()"/>
 * ```
 */
@Component({
  selector: 'score-responsive-button',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButton, MatIconButton, MatIcon, MatTooltip, RouterLink],
  templateUrl: './responsive-button.component.html',
  styleUrl: './responsive-button.component.css'
})
export class ResponsiveButtonComponent {
  /** What the button says. */
  readonly label = input.required<string>();
  /** The icon of the narrow screen. */
  readonly icon = input('add');
  /** `warn` for a button that discards. */
  readonly color = input<'primary' | 'warn'>('primary');
  readonly disabled = input(false, {transform: booleanAttribute});
  /** Where the button leads to (a router link, as the ones of the page are), instead of `pressed`. */
  readonly link = input<string>();
  /** The button is pressed (not for one that has a `link`). */
  readonly pressed = output<void>();
}
