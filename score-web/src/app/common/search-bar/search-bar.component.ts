import {Component, EventEmitter, Input, Output, ViewEncapsulation, ChangeDetectionStrategy} from '@angular/core';

@Component({
  standalone: false,
  selector: 'score-search-bar',
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  encapsulation: ViewEncapsulation.None,
})
export class SearchBarComponent {

  private value = '';

  @Input() showAdvancedSearch = false;
  @Input() placeholder = 'Search';
  @Input() disabled = false;  // Add disabled input
  @Input() advancedSearch = true;
  /** The branch selector is wide (more than one, say): it goes to a line above the search box on a tablet too, not just on a phone. */
  @Input() wideBranchSelector = false;

  @Input()
  set model(value: string) {
    this.value = value;
    this.modelChange.emit(this.value);
  }

  get model(): string {
    return this.value;
  }

  @Output() modelChange = new EventEmitter<string>();
  @Output() searchTriggered = new EventEmitter<void>();

  onSearch(): void {
    this.searchTriggered.emit();
  }

  onModelChange(value: string): void {
    if (!this.disabled) {
      this.modelChange.emit(value);
    }
  }

  toggleAdvancedSearch(): void {
    this.showAdvancedSearch = !this.showAdvancedSearch;
  }

}
