import {afterNextRender, booleanAttribute, ChangeDetectionStrategy, Component, computed, DestroyRef, ElementRef, inject, input, signal, viewChild} from '@angular/core';
import {DatePipe, NgStyle, NgTemplateOutlet} from '@angular/common';
import {RouterLink} from '@angular/router';
import {MatChip} from '@angular/material/chips';
import {MatTooltip} from '@angular/material/tooltip';
import {WebPageInfoService} from '../../basis/basis.service';

/** A badge in the column at the left of the row: a state (coloured by what the application says of it) or a chip. */
export interface RecordBadge {
  text: string;
  /** `state`, by default a chip. */
  kind?: 'state' | 'chip';
  /** For a `state`: the class of its span (`cc-state`, `bie-state`, `cl-state`, `release-state`), which the global stylesheet colours. */
  cssClass?: string;
  /** For a `state`: whose colours it takes. */
  colors?: 'component' | 'release';
  /** For a `state`: the state itself, which names the colour (`text` may be a spelled-out form of it). */
  state?: string;
  tooltip?: string;
  /** For a `state`: its own colours (a mark of the screen, not a state of the application), instead of those of `colors`. */
  background?: string;
  font?: string;
}

/**
 * A value of the line under the name. The narrower the row, the fewer there are: `priority` 4 goes first (below 700px), then 3 (600),
 * 2 (500) and 1 (400); `0`, the default, stays. The first value stays whatever its priority: it is the one that is most of the record.
 * The values are shown whole, but the last of those shown, which is cut with an ellipsis when the line is full.
 */
export interface RecordMeta {
  label?: string;
  value: string;
  /** Makes the value a link (router link). */
  link?: string;
  /** Makes the value a link to an address outside of the application (opened in a new tab). */
  href?: string;
  /** The value in bold, as a person is. */
  strong?: boolean;
  priority?: 0 | 1 | 2 | 3 | 4;
}

/** What `score-record-compact-row` shows of a record. */
export interface CompactRecord {
  badges?: readonly RecordBadge[];
  /** Marks the record as new. */
  isNew?: boolean;
  title: string;
  /** What the title is, before it (e.g. `Value`), when it does not say itself. */
  titleLabel?: string;
  /** Where the title leads to. */
  link?: string;
  /** The query parameters of that link. */
  linkQueryParams?: Record<string, string | number>;
  /** Why the title is not a link, for a record that cannot be opened. */
  restriction?: string;
  /** The title in bold (e.g. an unread message). */
  titleStrong?: boolean;
  /** The title dimmed (e.g. a message that has been read). */
  titleMuted?: boolean;
  /** Next to the title, e.g. a display name. */
  titleTag?: string;
  /** Badges beside the title instead of in the column at the left (a mark of the record, e.g. the default one). */
  titleBadges?: readonly RecordBadge[];
  /** A muted line under the title, e.g. a GUID. */
  subtitle?: string;
  /** What the muted line is, before it (e.g. `Meaning`). */
  subtitleLabel?: string;
  meta?: readonly RecordMeta[];
  /** When and by whom; left out, when not known. `label`: what the time is of (`Updated`, by default). */
  updated?: { when: Date | string | number; by?: string; label?: string };
  /** When and by whom it was made: of the same look as the update, under the values (the update has the right). */
  created?: { when: Date | string | number; by?: string; label?: string };
}

/**
 * A record as one block, for a `*scoreCompactDef` of a table that is too narrow for its columns: badges on the left; title, a muted line,
 * and the values (as far as the width allows) beside them; when and by whom it was updated at the right, and, the narrower it gets, under
 * the values. What the screen needs is a `CompactRecord`, made once for a row (`memoizeRow`).
 *
 * ```html
 * <ng-container *scoreCompactDef="let element">
 *   <score-record-compact-row [record]="compactRecord(element)"/>
 * </ng-container>
 * ```
 * What is put inside the element (e.g. the controls of a row) is shown under all that, across the width. What is put there with the attribute
 * `actions` (e.g. a button) is shown at the right end of the row, in the middle.
 */
@Component({
  selector: 'score-record-compact-row',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, NgStyle, NgTemplateOutlet, RouterLink, MatChip, MatTooltip],
  templateUrl: './record-compact-row.component.html',
  styleUrl: './record-compact-row.component.css'
})
export class RecordCompactRowComponent {
  protected readonly webPageInfo = inject(WebPageInfoService);

  /** The width (px) at which each priority is shown. */
  private static readonly WIDTH_OF_PRIORITY = [0, 400, 500, 600, 700];

  readonly record = input.required<CompactRecord>();
  /** The link opens in a new tab (a dialog must not be left). */
  readonly external = input(false, {transform: booleanAttribute});

  private readonly body = viewChild<ElementRef<HTMLElement>>('body');
  /** The width of the block that holds the text: unknown (and so, all of the values shown) until it is measured. */
  private readonly width = signal(Number.POSITIVE_INFINITY);

  /** The values that the width has room for: the first always, and those whose priority the width allows. */
  protected readonly meta = computed(() => {
    const width = this.width();
    return (this.record().meta ?? []).filter((item, index) =>
      index === 0 || width >= RecordCompactRowComponent.WIDTH_OF_PRIORITY[item.priority ?? 0]);
  });

  /**
   * The lines of text of the grid (the title, the muted line, the values, the creation). The update spans them, at their foot: beside the
   * last two of them (the values are a line as soon as there is one, because the first of them is always shown).
   */
  protected readonly textRows = computed(() => 1 + (this.record().subtitle ? 1 : 0) + (this.meta().length ? 1 : 0) + (this.record().created ? 1 : 0));

  /** A record that is only a title has nothing under it: it is centred in its row (which may be taller than it, with a button beside it). */
  protected readonly titleOnly = computed(() => {
    const record = this.record();
    return !record.subtitle && !this.meta().length && !record.updated;
  });

  /** A record that is a title and the update, and nothing else: its one line is in the middle of the two lines of the update. */
  protected readonly titleAndUpdate = computed(() => {
    const record = this.record();
    return !record.subtitle && !this.meta().length && !!(record.updated || record.created);
  });

  constructor() {
    const destroyRef = inject(DestroyRef);
    afterNextRender(() => {
      const element = this.body()?.nativeElement;
      if (element && typeof ResizeObserver !== 'undefined') {
        const observer = new ResizeObserver(() => this.width.set(element.clientWidth));
        observer.observe(element);
        destroyRef.onDestroy(() => observer.disconnect());
      }
    });
  }

  protected colorSet(badge: RecordBadge): { background?: string; font?: string } {
    if (badge.background) {
      return {background: badge.background, font: badge.font};
    }
    const state = badge.state ?? badge.text;
    return badge.colors === 'release' ? this.webPageInfo.getReleaseStateColorSet(state) : this.webPageInfo.getComponentStateColorSet(state);
  }
}
