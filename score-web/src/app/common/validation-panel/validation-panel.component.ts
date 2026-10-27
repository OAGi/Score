import {ChangeDetectionStrategy, Component, EventEmitter, HostListener, Input, Output} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {
  countOf,
  locationAddress,
  ValidationLocation,
  ValidationSession,
  ValidationSeverity,
  Violation
} from '../../validation/validation';

/**
 * The problems of a validation, as a panel at the bottom of the page. It knows nothing of what is validated: a row
 * is a message with the nodes it is about, and what a click on a node does is up to the page ({@link locationSelect}).
 */
@Component({
  selector: 'score-validation-panel',
  templateUrl: './validation-panel.component.html',
  styleUrls: ['./validation-panel.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatButtonModule, MatIconModule, MatTooltipModule]
})
export class ValidationPanelComponent {

  @Input({required: true}) session: ValidationSession;
  @Input() title = 'Validation Results';
  /** The height of the panel; the page owns it, so that it can leave the same room free below its content. */
  @Input() height = 240;

  @Output() locationSelect = new EventEmitter<ValidationLocation>();
  @Output() closed = new EventEmitter<void>();
  @Output() heightChange = new EventEmitter<number>();

  readonly minHeight = 96;

  activeIndex = -1;

  private visited = new Map<Violation, Set<string>>();
  private resizing = false;

  get violations(): Violation[] {
    return this.session.report?.violations ?? [];
  }

  count(severity: ValidationSeverity): number {
    return countOf(this.session.report, severity);
  }

  get summary(): string {
    return (['ERROR', 'WARNING', 'INFO'] as ValidationSeverity[])
      .filter(s => this.count(s) > 0)
      .map(s => this.count(s) + ' ' + (s === 'ERROR' ? 'error' : s === 'WARNING' ? 'warning' : 'info') + (this.count(s) > 1 && s !== 'INFO' ? 's' : ''))
      .join(' · ');
  }

  select(violation: Violation, location: ValidationLocation, $event?: Event) {
    $event?.stopPropagation();
    const address = locationAddress(location);
    if (!this.visited.has(violation)) {
      this.visited.set(violation, new Set());
    }
    this.visited.get(violation).add(address);
    this.locationSelect.emit(location);
  }

  selectFirst(violation: Violation) {
    if (violation.locations.length > 0) {
      this.select(violation, violation.locations[0]);
    }
  }

  /** Whether the violation is about the node whose marker the user clicked. */
  isFocused(violation: Violation): boolean {
    return !!this.session.focusAddress && violation.locations.some(l => locationAddress(l) === this.session.focusAddress);
  }

  isVisited(violation: Violation, location: ValidationLocation): boolean {
    return !!this.visited.get(violation)?.has(locationAddress(location));
  }


  onKey(event: KeyboardEvent) {
    const rows = this.violations;
    if (rows.length === 0) {
      return;
    }
    if (event.key === 'ArrowDown') {
      this.activeIndex = Math.min(rows.length - 1, this.activeIndex + 1);
    } else if (event.key === 'ArrowUp') {
      this.activeIndex = Math.max(0, this.activeIndex - 1);
    } else if (event.key === 'Enter' && this.activeIndex >= 0) {
      this.selectFirst(rows[this.activeIndex]);
    } else if (event.key === 'Escape') {
      this.closed.emit();
    } else {
      return;
    }
    event.preventDefault();
  }

  startResize(event: MouseEvent) {
    this.resizing = true;
    event.preventDefault();
  }

  @HostListener('document:mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    if (this.resizing) {
      this.heightChange.emit(Math.max(this.minHeight, Math.min(window.innerHeight - 120, window.innerHeight - event.clientY)));
    }
  }

  @HostListener('document:mouseup')
  onMouseUp() {
    this.resizing = false;
  }
}
