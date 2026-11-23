import {ChangeDetectionStrategy, Component, forwardRef, inject, signal, ViewEncapsulation} from '@angular/core';
import {MatFormField} from '@angular/material/form-field';
import {MatIconButton} from '@angular/material/button';
import {MatOption} from '@angular/material/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSelect, MatSelectTrigger} from '@angular/material/select';
import {MatTooltip} from '@angular/material/tooltip';
import {AuthService} from '../../authentication/auth.service';
import {loadProperty, saveProperty} from '../utility';

/** The most items a user can ask for in one page by typing a size in. */
export const MAX_PAGE_SIZE = 1000;

/** The key of the size a user typed in, kept in the local storage of the browser for the user (so it is there on every page, and after signing in again). */
export const CUSTOM_PAGE_SIZE_KEY = 'Paginator-CustomPageSize';

/**
 * A `MatPaginator` with a field for a size of one's own at the end of the list of the page sizes. It is used in place of `<mat-paginator>`
 * (the same inputs, output and look): only the tag differs.
 *
 * The template and the styles are copies of those of `MatPaginator` (a component brings neither to a subclass), changed where the
 * field is. A size typed in must be a positive whole number, and one above `MAX_PAGE_SIZE` is `MAX_PAGE_SIZE`.
 */
@Component({
  selector: 'score-paginator',
  exportAs: 'scorePaginator',
  templateUrl: './score-paginator.component.html',
  styleUrl: './score-paginator.component.css',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {'class': 'mat-mdc-paginator', 'role': 'group'},
  imports: [MatFormField, MatSelect, MatSelectTrigger, MatOption, MatIconButton, MatTooltip],
  providers: [{provide: MatPaginator, useExisting: forwardRef(() => ScorePaginator)}],
})
export class ScorePaginator extends MatPaginator {

  private readonly auth = inject(AuthService, {optional: true});

  protected readonly maxPageSize = MAX_PAGE_SIZE;

  /** The value of the select while the size is a typed one: it is never one of the sizes, and does not change with the typed size. */
  protected readonly customValue = 'custom';

  /** The size the user typed in, kept for the user: it stays in the field of the list while another size is the table's, until it is removed. */
  private readonly customSize = signal<number | null>(this.loadCustomSize());

  /** The sizes of the list, in order. The size the table has is not among them when it is a typed one: it is the one of the field. */
  _listedPageSizes(): number[] {
    return [...this.pageSizeOptions].sort((a, b) => a - b);
  }

  /** Whether the size the table has is a typed one, not one of the list. */
  _isCustomPageSize(): boolean {
    return !this.pageSizeOptions.includes(this.pageSize);
  }

  /** The size the field shows: the table's, when it is a typed one (the URL may bring one that was not kept), else the kept one. */
  _customFieldSize(): number | null {
    return this._isCustomPageSize() ? this.pageSize : this.customSize();
  }

  /**
   * Takes the size typed in the field (a positive whole number, at most `MAX_PAGE_SIZE`), keeps it for the user, and makes it the table's.
   * What is not one is dropped: the field shows its size again. Closes the list, when given (Enter), and applies only what was changed
   * otherwise (leaving the field).
   */
  _applyCustomPageSize(field: HTMLInputElement, select?: MatSelect) {
    const value = field.value.trim();
    const requested = Number(value);
    const valid = value !== '' && Number.isInteger(requested) && requested >= 1;
    const size = Math.min(requested, MAX_PAGE_SIZE);
    if (valid && (select || size !== this._customFieldSize())) {
      if (!this.pageSizeOptions.includes(size)) {
        this.keepCustomSize(size);
      }
      if (size !== this.pageSize) {
        this._changePageSize(size);
      }
    }
    field.value = this._customFieldSize()?.toString() ?? '';
    select?.close();
  }

  /** A click on the row of the size of one's own, outside of its number: the size is the table's. With no size yet, there is nothing to take. */
  _selectCustomPageSize(select: MatSelect) {
    const size = this._customFieldSize();
    if (size === null) {
      return;
    }
    if (size !== this.pageSize) {
      this._changePageSize(size);
    }
    select.close();
  }

  /** Removes the kept size. When it is the table's, the table goes back to the first size of the list. */
  _removeCustomPageSize(field: HTMLInputElement) {
    this.keepCustomSize(null);
    if (this._isCustomPageSize() && this.pageSizeOptions.length > 0) {
      this._changePageSize(this.pageSizeOptions[0]);
    }
    field.value = '';
  }

  private keepCustomSize(size: number | null) {
    this.customSize.set(size);
    saveProperty(this.auth?.getUserToken(), CUSTOM_PAGE_SIZE_KEY, size === null ? '' : String(size));
  }

  private loadCustomSize(): number | null {
    const size = Number(loadProperty(this.auth?.getUserToken(), CUSTOM_PAGE_SIZE_KEY, ''));
    return Number.isInteger(size) && size >= 1 ? Math.min(size, MAX_PAGE_SIZE) : null;
  }
}
