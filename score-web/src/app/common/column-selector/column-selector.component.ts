import {Component, EventEmitter, Input, Output, ViewEncapsulation, ChangeDetectionStrategy} from '@angular/core';
import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';
import {TableColumnsState} from '../score-table/table-columns-state';

@Component({
  standalone: false,
  selector: 'score-column-selector',
  templateUrl: './column-selector.component.html',
  styleUrls: ['./column-selector.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  encapsulation: ViewEncapsulation.None,
})
export class ColumnSelectorComponent {
  @Input() title: string = 'Columns';
  @Input() classes: string[];
  @Input() draggable: boolean = true;
  @Input() multiple: boolean = true;
  /** Column state of a defined table (`defineTable`). Takes the place of `columns`/`columnsChange`/`onReset`. */
  @Input() state?: TableColumnsState;
  @Input() columns: { name: string; selected: boolean }[] = [];
  @Output() columnsChange = new EventEmitter<{ name: string; selected: boolean }[]>();
  @Output() onReset = new EventEmitter<void>();

  overlayVisible = false;

  get items(): { name: string; selected: boolean }[] {
    return this.state ? this.state.list() : this.columns;
  }

  get resettable(): boolean {
    return !!this.state || this.onReset.observed;
  }

  handleOverlayClick(event: MouseEvent): void {
    const targetElement = event.target as HTMLElement;

    // Determine the column based on the clicked element
    const columnElement = targetElement.closest('.column');
    if (!columnElement) return;

    const columnIndex = Array.from(columnElement.parentElement!.children).indexOf(columnElement);
    const column = this.items[columnIndex];

    if (this.multiple) {
      // Toggle selection for checkboxes
      column.selected = !column.selected;
      this.toggleColumnSelection(column);
    } else {
      // Single selection: Check if the column is already selected
      if (column.selected) {
        // Do nothing if the selected column is clicked again
        return;
      }
      // Select the radio button column
      this.selectSingleColumn(column);
    }

    event.stopPropagation();
  }

  toggleColumnSelection(selectedColumn: { name: string; selected: boolean }) {
    this.emitChange();
  }

  selectSingleColumn(selectedColumn: { name: string; selected: boolean }): void {
    this.items.forEach(col => (col.selected = false)); // Deselect all
    selectedColumn.selected = true; // Select the current column
    this.closeOverlay();
    this.emitChange();
  }

  drop(event: CdkDragDrop<{ name: string; selected: boolean }[]>) {
    if (this.draggable) {
      moveItemInArray(this.items, event.previousIndex, event.currentIndex);
      this.emitChange();
    }
  }

  toggleOverlay() {
    this.overlayVisible = !this.overlayVisible;
  }

  closeOverlay() {
    this.overlayVisible = false;
  }

  onResetButtonClick() {
    if (this.state) {
      this.state.reset();
    } else {
      this.onReset.emit();
    }
  }

  private emitChange() {
    if (this.state) {
      this.state.update(this.items);
    } else {
      this.columnsChange.emit(this.columns);
    }
  }
}
