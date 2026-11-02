import {booleanAttribute, ChangeDetectionStrategy, Component, computed, input, output, ViewEncapsulation} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {NgxMatSelectSearchModule} from 'ngx-mat-select-search';
import {ReleaseSummary} from '../../release-management/domain/release';

/**
 * The branch (release) selector that the lists put in front of their search box: one branch to choose, or with `multiple`
 * several (with a check box to select or unselect all of them). `selected` is the branch, or the array of branches with
 * `multiple`, and `selectedChange` brings the same type back. `readonly` shows the branch that is not to be changed.
 */
@Component({
  selector: 'score-branch-selector',
  templateUrl: './branch-selector.component.html',
  styleUrl: './branch-selector.component.css',
  encapsulation: ViewEncapsulation.None,
  imports: [FormsModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, NgxMatSelectSearchModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BranchSelectorComponent<T extends ReleaseSummary | ReleaseSummary[] = ReleaseSummary> {

  readonly releases = input<ReleaseSummary[]>([]);
  /** The branch that is chosen, or the branches (`multiple`). */
  readonly selected = input<T | undefined>();
  readonly multiple = input(false, {transform: booleanAttribute});
  readonly readonly = input(false, {transform: booleanAttribute});
  /** Whether the branches can be searched for. */
  readonly searchable = input(true);
  readonly required = input(false, {transform: booleanAttribute});
  readonly width = input('11rem');

  readonly selectedChange = output<T>();

  readonly single = computed(() => this.selected() as ReleaseSummary | undefined);
  readonly selectedList = computed(() => (this.selected() as ReleaseSummary[] | undefined) ?? []);

  readonly filterCtrl = new FormControl<string>('', {nonNullable: true});
  private readonly filterText = toSignal(this.filterCtrl.valueChanges, {initialValue: ''});
  readonly filtered = computed(() => {
    const text = (this.filterText() ?? '').toLowerCase();
    return text ? this.releases().filter(e => e.releaseNum.toLowerCase().includes(text)) : this.releases();
  });

  readonly compareRelease = (a?: ReleaseSummary, b?: ReleaseSummary) => a?.releaseId === b?.releaseId;

  toggleAll(selectAll: boolean) {
    this.selectedChange.emit((selectAll ? [...this.releases()] : []) as T);
  }
}
