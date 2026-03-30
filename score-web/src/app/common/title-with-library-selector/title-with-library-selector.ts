import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewEncapsulation} from '@angular/core';
import {LibrarySummary} from '../../library-management/domain/library';
import {ColumnSelectorModule} from '../column-selector/column-selector.module';


@Component({
  selector: 'score-title-with-library-selector',
  templateUrl: './title-with-library-selector.html',
  styleUrls: ['./title-with-library-selector.css'],
  encapsulation: ViewEncapsulation.None,
  imports: [
    ColumnSelectorModule
],
  standalone: true
})
export class TitleWithLibrarySelector implements OnInit, OnChanges {

  @Input() title: string;
  @Input() subtitle: string;
  @Input() libraries: { library: LibrarySummary, selected: boolean }[] = [];
  @Output() libraryChange = new EventEmitter<LibrarySummary | undefined>();

  filterLibraries: {
    name: string;
    selected: boolean;
  }[] = [];

  ngOnInit(): void {
    this.initializeFilterLibraries();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.libraries) {
      this.initializeFilterLibraries();
    }
  }

  private initializeFilterLibraries(): void {
    this.filterLibraries = this.libraries.map(e => ({
      name: e.library.name,
      selected: e.selected
    }));
  }

  get selectedLibrary(): LibrarySummary | undefined {
    const selectedLibraries = this.libraries.filter(e => e.selected).map(e => e.library);
    return selectedLibraries.length > 0 ? selectedLibraries[0] : undefined;
  }

  onFilterLibraryChange(updatedColumns: { name: string; selected: boolean }[]) {
    const selectedLibraryName = updatedColumns.find(c => c.selected)?.name;
    let selectedLibrary: LibrarySummary | undefined;

    this.libraries.forEach(library => {
      library.selected = library.library.name === selectedLibraryName;
      if (library.selected) {
        selectedLibrary = library.library;
      }
    });

    this.libraryChange.emit(selectedLibrary);
  }

}
