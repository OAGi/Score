import {booleanAttribute, ChangeDetectionStrategy, Component, inject, input} from '@angular/core';
import {DatePipe, NgStyle, NgTemplateOutlet} from '@angular/common';
import {RouterLink} from '@angular/router';
import {MatChip} from '@angular/material/chips';
import {WebPageInfoService} from '../../basis/basis.service';
import {ScoreCommonModule} from '../../common/score-common.module';

/** What of a core component (or data type) the compact row shows. The list endpoints give more; this is all it reads. */
export interface CcCompactRowElement {
  guid: string;
  state: string;
  deprecated?: boolean;
  tagList?: { name: string; backgroundColor: string; textColor: string }[];
  log?: { revisionNum: number };
  owner?: { loginId: string };
  module?: string;
  definition?: { content?: string };
  lastUpdated: { when: Date | string | number; who: { loginId: string } };
}

/**
 * A core component as one block, for a `*scoreCompactDef` of a table that is too narrow for its columns: type and state on the left;
 * name (DEN), GUID, revision and owner beside them, and, on the right, when and by whom it was updated.
 *
 * ```html
 * <ng-container *scoreCompactDef="let element">
 *   <score-cc-compact-row [element]="element" [label]="element.den" [link]="'core_component/acc/' + element.manifestId" external/>
 * </ng-container>
 * ```
 * What is put inside the element (e.g. a value domain) is shown beside the owner (a list without owners has no room for it). The module
 * and the definition follow it, on the same line while the width allows: the definition goes first, then the module, as it narrows.
 */
@Component({
  selector: 'score-cc-compact-row',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, NgStyle, NgTemplateOutlet, RouterLink, MatChip, ScoreCommonModule],
  templateUrl: './cc-compact-row.component.html',
  styleUrl: './cc-compact-row.component.css'
})
export class CcCompactRowComponent {
  protected readonly webPageInfo = inject(WebPageInfoService);

  readonly element = input.required<CcCompactRowElement>();
  /** The name: the DEN, or what the list calls it. */
  readonly label = input.required<string>();
  /** Where the name leads to. */
  readonly link = input<string>();
  /** The link opens in a new tab (a dialog must not be left). */
  readonly external = input(false, {transform: booleanAttribute});
  /** The kind of component (ACC, ASCCP, BCCP, ...), in a chip above the state. */
  readonly type = input<string>();
  /** Marks the component as new in its library. */
  readonly isNew = input(false);
  /** `false` for a list that has no revision and owner to show. */
  readonly showOwner = input(true);
}
