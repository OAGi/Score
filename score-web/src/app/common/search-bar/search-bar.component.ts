import {Component, EventEmitter, Input, Output, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'score-search-bar',
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class SearchBarComponent {

  private value = '';

  @Input() showAdvancedSearch = false;
  @Input() placeholder = 'Search';
  @Input() disabled = false;  // Add disabled input
  @Input() advancedSearch = true;

  @Input()
  set model(value: string) {
    this.value = value;
    this.modelChange.emit(this.value);
  }

  get model(): string {
    return this.value;
  }

  @Output() modelChange = new EventEmitter<string>();
  @Output() search = new EventEmitter<string>();

  onSearch(): void {
    this.search.emit();
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
