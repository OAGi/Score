import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatMenu, MatMenuModule} from '@angular/material/menu';
import {MatIconButtonSizesModule} from 'mat-icon-button-sizes';

/**
 * The settings (gear) button of a tree-detail screen's `[treeDetailActions]`, opening the screen's own `mat-menu`.
 * Put it directly in the `.actions` row (it renders no box of its own, so the button is the row's flex item).
 */
@Component({
  selector: 'score-tree-settings-button',
  template: `
    <button mat-icon-button mat-small-icon-button [id]="buttonId" [matMenuTriggerFor]="menu">
      <mat-icon>settings</mat-icon>
    </button>`,
  changeDetection: ChangeDetectionStrategy.Eager,
  host: {style: 'display: contents'},
  imports: [MatButtonModule, MatIconButtonSizesModule, MatIconModule, MatMenuModule]
})
export class TreeSettingsButtonComponent {

  /** The menu to open. */
  @Input({required: true}) menu!: MatMenu;
  /** Element id of the button. */
  @Input() buttonId = 'tree-settings';
}
