import {computed, inject, Signal, signal, WritableSignal} from '@angular/core';
import {AuthService} from '../../authentication/auth.service';
import {UserToken} from '../../authentication/domain/auth';
import {loadProperty, saveProperty} from '../utility';
import {TableColumnId, TableDef, TableFixedColumnId} from './table-def';

/** The shape that is persisted: the same as `TableColumnsProperty`. */
interface StoredColumn {
  name: string;
  selected: boolean;
  width: number | string;
}

export interface TableColumnItem {
  /** Label, shown in the column selector. */
  name: string;
  selected: boolean;
}

/** A column of a defined table, as obtained from `TableColumnsState.column()`. */
export class TableColumnRef {
  constructor(readonly id: string) {
  }
}

/**
 * Column settings (order, visibility, width) of a table, persisted to localStorage per user.
 * Keeps the key and the stored format that the former preferences service used.
 */
export class TableColumnsState<D extends TableDef = TableDef> {

  private readonly stored: WritableSignal<StoredColumn[]>;

  /** Selector binding: label, selected, width in the user's order. */
  readonly list: Signal<StoredColumn[]>;

  /** Ids of the selected columns, in the user's order. */
  readonly displayed: Signal<string[]>;

  constructor(readonly def: D, private readonly userToken: UserToken) {
    this.stored = signal(this.load());
    this.list = this.stored.asReadonly();
    this.displayed = computed(() =>
      this.stored().filter(c => c.selected).map(c => this.idOfLabel(c.name)).filter((id): id is string => !!id));
  }

  /**
   * The column to give a cell to: `*scoreCellDef="columns.column('name'); let row"`.
   * The id is checked against the definition when the template is compiled.
   */
  column(id: TableColumnId<D> | TableFixedColumnId<D>): TableColumnRef {
    return new TableColumnRef(id);
  }

  width(id: TableColumnId<D> | TableFixedColumnId<D>): number | string {
    const label = this.def.columns.find(c => c.id === id)?.label;
    if (!label) {
      return this.def.fixed?.find(c => c.id === id)?.width;
    }
    return this.stored().find(c => c.name === label)?.width;
  }

  /** Ids of the fixed columns at a side. */
  fixed(position: 'start' | 'end'): string[] {
    return (this.def.fixed ?? []).filter(c => c.position === position).map(c => c.id);
  }

  /** Called by the column selector: new order/visibility. Widths are kept. */
  update(items: TableColumnItem[]) {
    this.persist(items.map(item => ({
      name: item.name,
      selected: item.selected,
      width: this.stored().find(c => c.name === item.name)?.width ?? this.defaultColumns().find(c => c.name === item.name)?.width ?? 0
    })));
  }

  reset() {
    this.persist(this.defaultColumns());
  }

  /** Called by `score-table-column-resize`. Unknown columns (e.g. a neighbour that is not selectable) are ignored. */
  resize(id: string, width: number | string) {
    const label = this.def.columns.find(c => c.id === id)?.label;
    if (!label || !this.stored().some(c => c.name === label)) {
      return;
    }
    this.persist(this.stored().map(c => c.name === label ? {...c, width} : c));
  }

  private idOfLabel(label: string): string | undefined {
    return this.def.columns.find(c => c.label === label)?.id;
  }

  private defaultColumns(): StoredColumn[] {
    return this.def.columns.map(c => ({name: c.label, selected: c.selected ?? true, width: c.width}));
  }

  private load(): StoredColumn[] {
    const defaults = this.defaultColumns();
    if (!this.userToken) {
      return defaults;
    }
    const stored: StoredColumn[] = JSON.parse(loadProperty(this.userToken, this.def.storageKey, JSON.stringify(defaults)));
    // The same rule as before: stored settings of a different column count are dropped.
    return stored.length === defaults.length ? stored : defaults;
  }

  private persist(columns: StoredColumn[]) {
    this.stored.set(columns);
    saveProperty(this.userToken, this.def.storageKey, JSON.stringify(columns));
  }
}

/** Creates the column state of a table. Must be called in an injection context (a field initializer). */
export function createTableColumns<const D extends TableDef>(def: D): TableColumnsState<D> {
  return new TableColumnsState(def, inject(AuthService).getUserToken());
}
