import { DataSource } from '@angular/cdk/collections';
import { BehaviorSubject } from 'rxjs';
import { MatMultiSort } from './mat-multi-sort.directive';
export declare class MatMultiSortTableDataSource<T> extends DataSource<T> {
    private _data;
    private clientSideSorting;
    sort: MatMultiSort;
    constructor(sort: MatMultiSort, clientSideSorting?: boolean);
    set data(data: T[]);
    get data(): T[];
    connect(): BehaviorSubject<T[]>;
    disconnect(): void;
    orderData(): void;
    sortData(data: T[], actives: string[], directions: string[]): T[];
    _sortData(d1: T, d2: T, params: string[], dirs: string[]): number;
}
//# sourceMappingURL=mat-multi-sort-data-source.d.ts.map