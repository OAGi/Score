import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {RouterLink} from '@angular/router';
import {encodeListQuery} from '../state-progress-bar/state-progress-bar';

export interface RankingState {
  state: string;
  background: string;
  font: string;
}

export interface RankingRow {
  username: string;
  total: number;
}

/** The users with the most items first, each as one bar split by state. */
@Component({
  selector: 'score-user-state-ranking',
  standalone: true,
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './user-state-ranking.component.html',
  styleUrl: './user-state-ranking.component.css'
})
export class UserStateRankingComponent {
  /** The list page that a user or a state opens. */
  @Input() route = '/core_component';
  /** Filter that every link carries, such as the release. */
  @Input() baseParams: {key: string, value: any}[] = [];
  @Input() states: RankingState[] = [];
  @Input() disabled = false;
  @Input() limit = 5;
  /** What is said when no user has an item. */
  @Input() emptyText = 'No items yet';

  expanded = false;
  sorted: RankingRow[] = [];
  maxTotal = 0;

  @Input() set rows(rows: RankingRow[] | undefined) {
    this.sorted = [...(rows ?? [])]
      .filter(e => e.total > 0)
      .sort((a, b) => (b.total - a.total) || a.username.localeCompare(b.username));
    this.maxTotal = this.sorted.length > 0 ? this.sorted[0].total : 0;
  }

  /** The states that at least one user has, so that the legend names only what the bars show. */
  get usedStates(): RankingState[] {
    return this.states.filter(s => this.sorted.some(row => this.count(row, s.state) > 0));
  }

  get shown(): RankingRow[] {
    return this.expanded ? this.sorted : this.sorted.slice(0, this.limit);
  }

  count(row: RankingRow, state: string): number {
    return (row as any)[state] || 0;
  }

  width(row: RankingRow, state: string): number {
    return this.maxTotal > 0 ? (this.count(row, state) / this.maxTotal) * 100 : 0;
  }

  query(username: string, state?: string): string {
    const params: {key: string, value: any}[] = state ? [{key: 'states', value: state}] : [];
    params.push({key: 'ownerLoginIdList', value: username}, ...this.baseParams);
    return encodeListQuery(params);
  }
}
