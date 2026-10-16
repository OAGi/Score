import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatIconButtonSizesModule} from 'mat-icon-button-sizes';

/** The part of a tree searcher (CcFlatNodeDataSourceSearcher, BieFlatNodeDataSourceSearcher, ...) the search bar reads. */
export interface TreeSearcher {
  inputKeyword: string;
  searchKeyword: string;
  searchResult: unknown[];
  searchIndex: number;
  searchedItemCount: number;
  fullSearched: boolean;
  isSearching: boolean;

  resetSearch(): void;
}

export interface TreeSearchRequest {
  keyword: string;
  backward: boolean;
  force: boolean;
}

@Component({
  selector: 'score-tree-search-bar',
  templateUrl: './tree-search-bar.component.html',
  styleUrl: './tree-search-bar.component.css',
  imports: [FormsModule, MatButtonModule, MatIconButtonSizesModule, MatFormFieldModule, MatIconModule, MatInputModule,
    MatProgressSpinnerModule, MatTooltipModule]
})
export class TreeSearchBarComponent {

  @Input() searcher: TreeSearcher;
  /** Name of the selected node, shown in the field label (`Search > name`). */
  @Input() label: string | undefined;

  @Output() searchRequest = new EventEmitter<TreeSearchRequest>();
  @Output() moveRequest = new EventEmitter<number>();

  onSearch(keyword: string, backward = false, force = false) {
    this.searchRequest.emit({keyword, backward, force});
  }
}
