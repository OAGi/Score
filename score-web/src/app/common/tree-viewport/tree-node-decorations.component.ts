import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {TreeNodeDecoration, TreeNodeDecorator} from './tree-decorator';

/**
 * Draws what the decorators of a tree show on the row of a node. A tree puts it in the row where the decorations
 * belong (the row template of each kind of tree), and gives it the node and the decorators.
 */
@Component({
  selector: 'score-tree-decorations',
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatIconModule, MatTooltipModule],
  template: `
    @for (decoration of decorations(); track $index) {
      @if (decoration.onClick) {
        <button type="button" class="tree-decoration ps-2 d-inline-flex align-items-center align-self-center {{ decoration.cssClass }}"
                [attr.id]="decoration.id" style="border: 0; background: transparent; cursor: pointer; padding-top: 0; padding-bottom: 0;"
                [style.opacity]="decoration.opacity ?? 1" (click)="decoration.onClick(); $event.stopPropagation()">
          <mat-icon style="width: 18px; height: 18px; font-size: 18px; line-height: 18px;" [style.color]="decoration.color"
                    [matTooltip]="decoration.tooltip ?? ''" [matTooltipClass]="decoration.tooltipClass ?? ''">{{ decoration.icon }}</mat-icon>
        </button>
      } @else {
        <span class="tree-decoration ps-2 d-inline-flex align-items-center align-self-center {{ decoration.cssClass }}"
              [attr.id]="decoration.id" [style.opacity]="decoration.opacity ?? 1">
          <mat-icon style="width: 18px; height: 18px; font-size: 18px; line-height: 18px;" [style.color]="decoration.color"
                    [matTooltip]="decoration.tooltip ?? ''" [matTooltipClass]="decoration.tooltipClass ?? ''">{{ decoration.icon }}</mat-icon>
        </span>
      }
    }
  `
})
export class TreeNodeDecorationsComponent<N> {

  @Input({required: true}) node: N;
  @Input() decorators: TreeNodeDecorator<N>[] = [];

  decorations(): TreeNodeDecoration[] {
    return this.decorators.flatMap(decorator => decorator.decorationsFor(this.node));
  }
}
