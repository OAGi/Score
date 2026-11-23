/**
 * Keeps the rows that the user has edited in place while a table that the server pages shows another page.
 *
 * A server-paged table holds the page it shows, and a page that is left is gone with the edits made in it. The screens whose rows are
 * all in memory (see `inMemoryLoad`) do not have the problem, as the rows are the screen's own. `EditedRows` is for the others: it
 * remembers each edited row of the page that is left, gives the same row back when its page is shown again (a sort, a search or the
 * paginator can bring it back), and says which rows are edited whatever page they are on, which is what an Update has to save.
 *
 * ```ts
 * readonly edits = new EditedRows<Row>(row => row.id, row => row.isChanged);
 * load = (params) => service.page(params).pipe(tap(resp => {
 *   resp.list = this.edits.carry(this.rows, resp.list.map(toRow)); // the page that is left, the page that comes
 *   this.rows = resp.list;
 * }));
 * save() { service.update(this.edits.edited(this.rows)).subscribe(() => { this.edits.clear(); view.reload(); }); }
 * ```
 * The table itself is not touched: it shows what the loader gives. `clear` whenever the rows are reloaded from the server on purpose
 * (after a save, a discard or a deletion); a row that the server no longer has would otherwise stay edited.
 */
export class EditedRows<T> {
  private readonly held = new Map<string, T>();

  /**
   * @param key identifies a row across pages (the same row has the same key whenever it is loaded)
   * @param isEdited whether the user has changed the row since it was loaded
   */
  constructor(private readonly key: (row: T) => string, private readonly isEdited: (row: T) => boolean) {
  }

  /** The number of edited rows, on the page `page` or off it. */
  count(page: readonly T[]): number {
    return this.edited(page).length;
  }

  /** Takes in the rows of the page that the table shows now: the edited ones are kept, and one that was edited back to what it was is let go. */
  remember(page: readonly T[]): void {
    for (const row of page) {
      if (this.isEdited(row)) {
        this.held.set(this.key(row), row);
      } else {
        this.held.delete(this.key(row));
      }
    }
  }

  /** The rows of the page that comes, with the edited rows that were held in place of the loaded ones. */
  carry(previousPage: readonly T[], nextPage: readonly T[]): T[] {
    this.remember(previousPage);
    return nextPage.map(row => this.held.get(this.key(row)) ?? row);
  }

  /** Every edited row, of the page `page` and of the pages that were left: what is to be saved. */
  edited(page: readonly T[]): T[] {
    this.remember(page);
    return [...this.held.values()];
  }

  /** The rows of the page, and the edited rows that are not on it: what a check across the whole table looks at. */
  withEdited(page: readonly T[]): T[] {
    const onPage = new Set(page.map(row => this.key(row)));
    return [...page, ...this.edited(page).filter(row => !onPage.has(this.key(row)))];
  }

  /** Lets every row go (the rows were saved, or are to be read again from the server). */
  clear(): void {
    this.held.clear();
  }
}
