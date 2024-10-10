import {Component, EventEmitter, Input, Output, ViewEncapsulation} from '@angular/core';
import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';
import {MatCheckboxChange} from '@angular/material/checkbox';

@Component({
  selector: 'score-column-selector',
  templateUrl: './column-selector.component.html',
  styleUrls: ['./column-selector.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class ColumnSelectorComponent {
  @Input() columns: { name: string; selected: boolean }[] = [];
  @Output() columnsChange = new EventEmitter<{ name: string; selected: boolean }[]>();

  // Control the overlay visibility
  overlayVisible = false;

  toggleColumnSelection() {
    this.columnsChange.emit(this.columns);
  }

  drop(event: CdkDragDrop<{ name: string; selected: boolean }[]>) {
    moveItemInArray(this.columns, event.previousIndex, event.currentIndex);
    this.columnsChange.emit(this.columns);
  }

  toggleOverlay() {
    this.overlayVisible = !this.overlayVisible;
  }
}
