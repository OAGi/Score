import {ChangeDetectionStrategy, Component, inject, Input} from '@angular/core';
import {DatePipe, NgStyle} from '@angular/common';
import {RouterLink} from '@angular/router';
import {MatCardModule} from '@angular/material/card';
import {WebPageInfoService} from '../../basis/basis.service';
import {ScoreCommonModule} from '../score-common.module';

/** A label and its value; `placeholder` is what is shown, dimmed, when there is no value (a pair without either is left out). */
type Pair = {label: string, value: string | number | undefined, placeholder?: string};

/** What the card shows of an item the user is working on. */
export interface WorkItemCard {
  /** The kind of item (ACC, BCCP, ...), as a label on the border at the top left; none for an item that has no kind to tell. */
  type?: string;
  title: string;
  link: string;
  state: string;
  /** The class of the state badge, which the global stylesheet colours: `cc-state` or `bie-state`. */
  stateClass: 'cc-state' | 'bie-state';
  /** At the right end of the line of the badges, in the header (e.g. the revision). */
  meta: Pair[];
  /** What the item is, briefly, in the middle (e.g. the module and the definition). A pair without a value is left out. */
  details: Pair[];
  /** When the item was last updated, shown as how long ago (the exact time is its tooltip). */
  updatedWhen?: Date;
}

/**
 * An item the user is working on, as a card with the border and the type of the cards of the home page: its name with its type and its
 * state badge (as the lists show it) at the left, its revision and update at the right end, and a few of its details below.
 */
@Component({
  selector: 'score-work-item-card',
  standalone: true,
  imports: [RouterLink, DatePipe, NgStyle, MatCardModule, ScoreCommonModule],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <mat-card class="work-item">
      <div class="component-card-header">
        @if (item.type) {
          <span class="type">{{ item.type }}</span>
        }
        <a class="card-name" [routerLink]="item.link">{{ item.title }}</a>
        <div class="line">
          <span class="{{ item.stateClass }} {{ item.state }}" [ngStyle]="stateStyle">{{ item.state | separate }}</span>
          <div class="meta">
            @for (pair of shown(item.meta); track pair.label) {
              <div>{{ pair.label }} <b>{{ pair.value }}</b></div>
            }
            @if (item.updatedWhen) {
              <div [attr.title]="item.updatedWhen | date: 'yyyy-MM-dd HH:mm'">Updated {{ item.updatedWhen | dateAgo }}</div>
            }
          </div>
        </div>
      </div>
      <div class="component-card-body">
        @if (shown(item.details).length) {
          <dl class="details">
            @for (pair of shown(item.details); track pair.label) {
              <div class="pair"><dt>{{ pair.label }}</dt>@if (hasValue(pair)) {<dd>{{ pair.value }}</dd>} @else {<dd class="empty">{{ pair.placeholder }}</dd>}</div>
            }
          </dl>
        }
      </div>
    </mat-card>
  `,
  styles: [`
    :host { display: block; min-width: 0; }
    .work-item { position: relative; overflow: visible; display: flex; flex-direction: column; height: 100%; box-sizing: border-box; padding: 0; color: #24292e; background-color: #fff;
      border: 1px solid #e1e4e8; border-radius: 12px; --mat-card-elevated-container-shape: 12px;
      font-size: 13px; line-height: 20px;
      box-shadow: 0 1px 2px rgba(27, 31, 35, .04), 0 4px 12px rgba(27, 31, 35, .04); }
    .component-card-header { padding: 12px 12px 10px; border-bottom: 1px solid #eef0f2; }
    .card-name { display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2; overflow: hidden; overflow-wrap: anywhere;
      padding: 6px 0 4px; color: #24292e; font-size: 14px; font-weight: 400; line-height: 1.3; text-decoration: none; }
    .line { display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 6px 12px; }
    .type { position: absolute; top: -10px; left: 12px; z-index: 1; padding: 0 6px; background-color: #fff; color: #24292e;
      font-size: 12px; font-weight: 500; line-height: 20px; }
    .meta { text-align: right; color: rgba(0, 0, 0, .6); }
    .meta b { color: #24292e; font-weight: 600; }
    .component-card-body { display: flex; flex: 1 1 auto; flex-direction: column; padding: 8px 12px 10px; }
    .details { margin: 0; }
    .pair { margin-bottom: 8px; }
    .empty { color: rgba(0, 0, 0, .38); font-style: italic; }
    dt { color: rgba(0, 0, 0, .6); }
    dd { margin: 2px 0 0; min-width: 0; display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 3; overflow: hidden;
      overflow-wrap: anywhere; }
  `]
})
export class WorkItemCardComponent {
  private webPageInfo = inject(WebPageInfoService);

  @Input({required: true}) item: WorkItemCard;

  /** The colours of the state as the configuration has them now: read when it is drawn, as the configuration may arrive or change later. */
  get stateStyle(): Record<string, string> {
    const colors = this.webPageInfo.getComponentStateColorSet(this.item.state);
    return colors ? {'background-color': colors.background, 'color': colors.font} : {};
  }

  hasValue(pair: Pair): boolean {
    return pair.value !== undefined && pair.value !== null && pair.value !== '';
  }

  shown(pairs: Pair[]): Pair[] {
    return (pairs ?? []).filter(pair => this.hasValue(pair) || !!pair.placeholder);
  }
}
