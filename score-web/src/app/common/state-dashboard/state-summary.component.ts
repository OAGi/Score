import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {RouterLink} from '@angular/router';
import {MatChipsModule} from '@angular/material/chips';
import {StateProgressBarItem, stateProgressBarQuery} from '../state-progress-bar/state-progress-bar';
import {StateDonutComponent} from './state-donut.component';

/** A donut of the items by state, with a chip per state under it: for everyone and, when given, for the signed-in user. */
@Component({
  selector: 'score-state-summary',
  standalone: true,
  imports: [RouterLink, MatChipsModule, StateDonutComponent],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './state-summary.component.html',
  styleUrl: './state-summary.component.css'
})
export class StateSummaryComponent {
  /** The heading above the donut of everything. */
  @Input() totalTitle = 'Total';
  @Input() total: StateProgressBarItem[] = [];
  /** What the signed-in user owns; a second donut. Omit for a summary of everything only. */
  @Input() mineTitle = 'Mine';
  @Input() mine: StateProgressBarItem[] | undefined;

  get figures(): {title: string, items: StateProgressBarItem[]}[] {
    return [{title: this.totalTitle, items: this.total ?? []}, ...(this.mine ? [{title: this.mineTitle, items: this.mine}] : [])];
  }

  query(item: StateProgressBarItem): string {
    return stateProgressBarQuery(item);
  }
}
