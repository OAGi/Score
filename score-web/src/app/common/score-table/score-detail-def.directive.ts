import {Directive, inject, TemplateRef} from '@angular/core';

/**
 * The detail row of a `score-table-view`, which opens under a row when it is clicked:
 *
 * ```html
 * <ng-container *scoreDetailDef="let element"> ... </ng-container>
 * ```
 */
@Directive({selector: '[scoreDetailDef]'})
export class ScoreDetailDefDirective {
  readonly template = inject<TemplateRef<{ $implicit: any }>>(TemplateRef);
}
