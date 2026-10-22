import {DecimalPipe} from '@angular/common';
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, ElementRef, EventEmitter, inject, Input, Output, ViewChild} from '@angular/core';
import {MatAutocompleteModule, MatAutocompleteSelectedEvent, MatAutocompleteTrigger} from '@angular/material/autocomplete';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Observable, Subscription} from 'rxjs';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatTooltipModule} from '@angular/material/tooltip';
import {SearchAttribute, SearchSuggestion, suggestSearch} from '../tree-search/search-suggest';
import {MatIconButtonSizesModule} from 'mat-icon-button-sizes';

/** What the search bar needs to scroll to a node: the tree viewport (or a derivative of it). */
export interface TreeScrollTarget<N = any> {
  scrollToIndex(index: number, options?: {delay?: number, smooth?: boolean}): void;

  /** Scroll to the node and put the cursor on it. */
  scrollToNode(node: N, delay?: number): void;
}

/** The part of a tree searcher (CcFlatNodeDataSourceSearcher, BieFlatNodeDataSourceSearcher, ...) the search bar uses. */
export interface TreeSearcher<N = any> {
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
  /** The node attributes a search expression may ask about; the field suggests them as the user types. */
  searchAttributes?: SearchAttribute[];
  /** Why the keyword is not a valid search expression, when it is not. */
  expressionError?: string;
  /** Emits when the figures above change without a user action (a running search's progress, its end). */
  progress: Observable<void>;

  resetSearch(): void;

  /** Searches below the node (the whole tree when none), or steps to the next/previous match; emits the match's row. */
  search(keyword: string, selectedNode: N | undefined, backward?: boolean, force?: boolean): Observable<number>;

  /** Steps from the current match by `val` matches; emits the match's row. */
  go(val: number): Observable<number>;

  /** The node shown in a row. */
  nodeAt(index: number): N;
}

@Component({
  selector: 'score-tree-search-bar',
  templateUrl: './tree-search-bar.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './tree-search-bar.component.css',
  imports: [DecimalPipe, FormsModule, MatAutocompleteModule, MatButtonModule, MatIconButtonSizesModule, MatFormFieldModule, MatIconModule, MatInputModule, MatTooltipModule]
})
/**
 * The search field of a tree with the buttons that step through the matches. It runs the searches itself and brings
 * the match into view in the tree, so a screen only supplies the searcher and the tree: stepping to a match also
 * selects it (`nodeSelect`), and a search is scoped to `selectedNode`.
 */
export class TreeSearchBarComponent<N = any> {

  @Input() set searcher(searcher: TreeSearcher<N>) {
    this._searcher = searcher;
    this.progressSubscription?.unsubscribe();
    this.progressSubscription = searcher?.progress
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.cdr.markForCheck());
  }

  get searcher(): TreeSearcher<N> {
    return this._searcher;
  }

  /** Name of the selected node, shown in the field label (`Search > name`). */
  @Input() label: string | undefined;

  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);
  private _searcher: TreeSearcher<N>;
  private progressSubscription: Subscription | undefined;

  /** The tree the matches are scrolled into view in. */
  @Input() tree: TreeScrollTarget<N> | undefined;
  /** The node the search is scoped to (the whole tree when none). */
  @Input() selectedNode: N | undefined | null;

  /** A match was stepped to with the arrow buttons; the screen shows it as the selected node. */
  @Output() nodeSelect = new EventEmitter<N>();

  @ViewChild('searchInput') searchInput: ElementRef<HTMLInputElement> | undefined;
  @ViewChild(MatAutocompleteTrigger) autocomplete: MatAutocompleteTrigger | undefined;

  /** What the field suggests at the caret, each as the whole expression that choosing it leads to. */
  suggestions: { suggestion: SearchSuggestion, expression: string, caret: number }[] = [];
  private selectedAt = 0;

  /** Works out the suggestions for the text before the caret; call it whenever the text or the caret moved. */
  updateSuggestions() {
    const input = this.searchInput?.nativeElement;
    const attributes = this.searcher?.searchAttributes ?? [];
    if (!input || attributes.length === 0 || this.searcher.isSearching) {
      this.suggestions = [];
      return;
    }
    const text = input.value;
    const {from, to, items} = suggestSearch(text, input.selectionStart ?? text.length, attributes);
    this.suggestions = items.map(suggestion => ({
      suggestion,
      expression: text.substring(0, from) + suggestion.insert + text.substring(to),
      caret: from + suggestion.insert.length
    }));
  }

  onSuggestionSelected(event: MatAutocompleteSelectedEvent) {
    this.selectedAt = Date.now();
    const chosen = this.suggestions.find(s => s.expression === event.option.value);
    // the autocomplete has put the expression in the field; the caret goes after what was inserted, and the next
    // suggestions (say, the operators after an attribute) follow at once
    setTimeout(() => {
      const input = this.searchInput?.nativeElement;
      if (chosen && input) {
        input.setSelectionRange(chosen.caret, chosen.caret);
      }
      this.updateSuggestions();
      if (this.suggestions.length > 0) {
        this.autocomplete?.openPanel();
      }
      this.cdr.markForCheck();
    });
  }

  /** Enter that only chose a suggestion does not start the search. */
  onEnter(keyword: string) {
    if (Date.now() - this.selectedAt > 300) {
      this.onSearch(keyword);
    }
  }

  /** The field lost the focus (a click on a suggestion keeps it, so the field is looked at a moment later). */
  onBlur() {
    setTimeout(() => {
      if (document.activeElement !== this.searchInput?.nativeElement) {
        this.hideSuggestions();
      }
    }, 150);
  }

  /** Puts the suggestions away until the user types or clicks in the field again. */
  hideSuggestions() {
    this.suggestions = [];
    this.autocomplete?.closePanel();
    this.cdr.markForCheck();
  }

  onSearch(keyword: string, backward = false, force = false) {
    // the field is disabled while the search runs, which takes the focus away without a blur
    this.hideSuggestions();
    this.searcher.search(keyword, this.selectedNode ?? undefined, backward, force)
      .subscribe(index => this.tree?.scrollToIndex(index, {delay: 500, smooth: true}));
  }

  onMove(val: number) {
    this.searcher.go(val).subscribe(index => {
      this.nodeSelect.emit(this.searcher.nodeAt(index));
      this.tree?.scrollToIndex(index, {smooth: true});
    });
  }
}
