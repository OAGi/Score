import {ChangeDetectionStrategy, Component, Input, inject} from '@angular/core';
import {Router} from '@angular/router';
import {StateProgressBarItem, stateProgressBarQuery} from '../state-progress-bar/state-progress-bar';

/* As in Angular Material Blocks' donut charts: the ring is about 40% of the outer radius (58) thick. */
const STROKE = 23;
const RADIUS = 58 - STROKE / 2;
/* The gap between two segments. */
const GAP = 1.5;
const CIRCUMFERENCE = 2 * Math.PI * RADIUS;

export interface DonutSegment {
  name: string;
  item: StateProgressBarItem;
  dash: string;
  offset: number;
}

/** A ring split by state, with the total in the middle. */
@Component({
  selector: 'score-state-donut',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <svg class="donut" viewBox="0 0 120 120" role="img" [attr.aria-label]="label + ' ' + total">
      <circle class="track" cx="60" cy="60" [attr.r]="radius"/>
      @for (segment of segments; track segment.name) {
        <circle class="segment" cx="60" cy="60" [attr.r]="radius"
                [attr.stroke]="segment.item.style.bg_color"
                [attr.stroke-dasharray]="segment.dash"
                [attr.stroke-dashoffset]="segment.offset"
                [class.link]="!segment.item.disabled"
                [attr.role]="segment.item.disabled ? null : 'link'"
                [attr.tabindex]="segment.item.disabled ? null : 0"
                (click)="open(segment.item)" (keydown.enter)="open(segment.item)"
                transform="rotate(-90 60 60)">
          <title>{{ segment.name }}</title>
        </circle>
      }
      <text class="total" x="60" y="60" text-anchor="middle" dominant-baseline="central">{{ total }}</text>
    </svg>
  `,
  styles: [`
    :host { display: inline-block; }
    .donut { font-family: inherit; width: 132px; height: 132px; display: block; }
    circle { fill: none; stroke-width: 23; }
    .track { stroke: #cfd8dc; }
    /* A segment opens the list of its state, as the chip of the state does. */
    .segment.link { cursor: pointer; }
    .segment.link:hover { opacity: .85; }
    /* The number as in Angular Material Blocks' donut charts (16px). */
    .total { font-size: 14.5px; font-weight: 400; fill: #24292e; }
  `]
})
export class StateDonutComponent {
  private readonly router = inject(Router);

  readonly radius = RADIUS;

  /** What the donut is of, for those who cannot see it. */
  @Input() label = '';

  segments: DonutSegment[] = [];
  total = 0;

  @Input() set data(items: StateProgressBarItem[] | undefined) {
    const present = (items ?? []).filter(e => e.value > 0);
    this.total = present.reduce((sum, e) => sum + e.value, 0);
    let covered = 0;
    this.segments = present.map(e => {
      const length = (e.value / this.total) * CIRCUMFERENCE;
      const gap = present.length > 1 ? GAP : 0;
      const drawn = Math.max(length - gap, 0);
      const segment = {
        name: `${e.name} (${e.value})`,
        item: e,
        dash: `${drawn} ${CIRCUMFERENCE - drawn}`,
        offset: -covered
      };
      covered += length;
      return segment;
    });
  }

  open(item: StateProgressBarItem) {
    if (!item.disabled) {
      this.router.navigate([item.href[0]], {queryParams: {q: stateProgressBarQuery(item)}});
    }
  }
}
