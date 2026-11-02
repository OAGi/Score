import {Directive, inject, input, TemplateRef} from '@angular/core';

/**
 * How a `score-table-view` draws a row when it is too narrow for its columns: this template instead of the cells of the columns
 * (the checkbox column stays). Without it the table is drawn with its columns at any width.
 *
 * ```html
 * <ng-container *scoreCompactDef="let element"> ... </ng-container>
 * ```
 * The fixed columns (e.g. a row menu) stay beside the compact row, unless they are named in `hide`: those whose controls the compact
 * row has taken over.
 *
 * ```html
 * <ng-container *scoreCompactDef="let element; hide: ['verb']"> ... </ng-container>
 * ```
 */
@Directive({selector: '[scoreCompactDef]'})
export class ScoreCompactDefDirective {
  readonly template = inject<TemplateRef<{ $implicit: any }>>(TemplateRef);
  /** Ids of the fixed columns that are not shown beside the compact row. */
  readonly hide = input<readonly string[]>([], {alias: 'scoreCompactDefHide'});
}
