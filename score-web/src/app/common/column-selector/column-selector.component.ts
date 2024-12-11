import {Component, EventEmitter, Input, Output, ViewEncapsulation} from '@angular/core';
import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';

@Component({
  selector: 'score-column-selector',
  templateUrl: './column-selector.component.html',
  styleUrls: ['./column-selector.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class ColumnSelectorComponent {
  @Input() title: string = 'Columns';
  @Input() classes: string[];
  @Input() draggable: boolean = true;
  @Input() multiple: boolean = true;
  @Input() columns: { name: string; selected: boolean }[] = [];
  @Output() columnsChange = new EventEmitter<{ name: string; selected: boolean }[]>();
  @Output() onReset = new EventEmitter<void>();

  overlayVisible = false;

  handleOverlayClick(event: MouseEvent): void {
    const targetElement = event.target as HTMLElement;

    // Determine the column based on the clicked element
    const columnElement = targetElement.closest('.column');
    if (!columnElement) return;

    const columnIndex = Array.from(columnElement.parentElement!.children).indexOf(columnElement);
    const column = this.columns[columnIndex];

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
    this.columnsChange.emit(this.columns);
  }

  selectSingleColumn(selectedColumn: { name: string; selected: boolean }): void {
    this.columns.forEach(col => (col.selected = false)); // Deselect all
    selectedColumn.selected = true; // Select the current column
    this.closeOverlay();
    this.columnsChange.emit(this.columns);
  }

  drop(event: CdkDragDrop<{ name: string; selected: boolean }[]>) {
    if (this.draggable) {
      moveItemInArray(this.columns, event.previousIndex, event.currentIndex);
      this.columnsChange.emit(this.columns);
    }
  }

  toggleOverlay() {
    this.overlayVisible = !this.overlayVisible;
  }

  closeOverlay() {
    this.overlayVisible = false;
  }

  onResetButtonClick() {
    this.onReset.emit();
  }
}
