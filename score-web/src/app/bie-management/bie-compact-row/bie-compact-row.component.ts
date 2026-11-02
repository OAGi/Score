import {booleanAttribute, ChangeDetectionStrategy, Component, inject, input} from '@angular/core';
import {DatePipe, NgStyle} from '@angular/common';
import {RouterLink} from '@angular/router';
import {MatChip} from '@angular/material/chips';
import {MatTooltip} from '@angular/material/tooltip';
import {WebPageInfoService} from '../../basis/basis.service';

/** What of a BIE the compact row shows. The list endpoints give more; this is all it reads. */
export interface BieCompactRowElement {
  guid: string;
  den: string;
  displayName?: string;
  state: string;
  deprecated?: boolean;
  deprecatedReason?: string;
  deprecatedRemark?: string;
  release: { releaseNum: string };
  owner: { loginId: string };
  version?: string;
  status?: string;
  bizTerm?: string;
  remark?: string;
  businessContextList?: { businessContextId: number; name: string }[];
  /** None for a BIE that is not known to have been updated: the update is then left out. */
  lastUpdated?: { when: Date | string | number; who?: { loginId: string } };
}

/**
 * A BIE as one block, for a `*scoreCompactDef` of a table that is too narrow for its columns: state and branch on the left; name,
 * GUID and owner beside them. The rest is shown as far as the width allows, and goes, the narrower it gets, in this order: remark,
 * business term, business contexts, update, status, version.
 *
 * ```html
 * <ng-container *scoreCompactDef="let element">
 *   <score-bie-compact-row [element]="element" [link]="'/profile_bie/' + element.topLevelAsbiepId" external/>
 * </ng-container>
 * ```
 * What is put inside the element (e.g. the controls of a row) is shown under all that, across the width.
 */
@Component({
  selector: 'score-bie-compact-row',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, NgStyle, RouterLink, MatChip, MatTooltip],
  templateUrl: './bie-compact-row.component.html',
  styleUrl: './bie-compact-row.component.css'
})
export class BieCompactRowComponent {
  protected readonly webPageInfo = inject(WebPageInfoService);

  readonly element = input.required<BieCompactRowElement>();
  /** Where the name leads to; none for a BIE that the user cannot open. */
  readonly link = input<string>();
  /** Why the name is not a link. */
  readonly restriction = input<string>();
  /** The link opens in a new tab (a dialog must not be left). */
  readonly external = input(false, {transform: booleanAttribute});

  protected deprecatedTooltip(element: BieCompactRowElement): string {
    return (element.deprecatedReason ? '• ' + element.deprecatedReason + '\n' : '')
      + (element.deprecatedRemark ? '• ' + element.deprecatedRemark : '');
  }
}
