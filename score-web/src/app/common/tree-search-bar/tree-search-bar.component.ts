import {DecimalPipe} from '@angular/common';
import {ChangeDetectorRef, Component, DestroyRef, EventEmitter, inject, Input, Output} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Observable, Subscription} from 'rxjs';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatIconButtonSizesModule} from 'mat-icon-button-sizes';

/** The part of a tree searcher (CcFlatNodeDataSourceSearcher, BieFlatNodeDataSourceSearcher, ...) the search bar reads. */
export interface TreeSearcher {
  inputKeyword: string;
  searchKeyword: string;
  resultCount: number;
  /** What `searchedItemCount` counts (empty when the count is not worth showing). */
  searchedUnit: string;
  searchIndex: number;
  searchedItemCount: number;
  fullSearched: boolean;
  isSearching: boolean;
  /** Matches found by the search in progress, not yet in `searchResult`. */
  pendingMatchCount: number;
  /** Emits when the figures above change without a user action (a running search's progress, its end). */
  progress: Observable<void>;

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
  imports: [DecimalPipe, FormsModule, MatButtonModule, MatIconButtonSizesModule, MatFormFieldModule, MatIconModule, MatInputModule, MatTooltipModule]
})
export class TreeSearchBarComponent {

  @Input() set searcher(searcher: TreeSearcher) {
    this._searcher = searcher;
    this.progressSubscription?.unsubscribe();
    this.progressSubscription = searcher?.progress
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.cdr.markForCheck());
  }

  get searcher(): TreeSearcher {
    return this._searcher;
  }

  /** Name of the selected node, shown in the field label (`Search > name`). */
  @Input() label: string | undefined;

  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);
  private _searcher: TreeSearcher;
  private progressSubscription: Subscription | undefined;

  @Output() searchRequest = new EventEmitter<TreeSearchRequest>();
  @Output() moveRequest = new EventEmitter<number>();

  onSearch(keyword: string, backward = false, force = false) {
    this.searchRequest.emit({keyword, backward, force});
  }
}
