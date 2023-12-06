import { Subject, BehaviorSubject } from 'rxjs';
import { MatMultiSortTableDataSource } from './mat-multi-sort-data-source';
import { PageEvent } from '@angular/material/paginator';
export declare class TableData<T> {
    private _dataSource;
    private readonly _columns;
    private _displayedColumns;
    pageSize: number;
    pageIndex: number;
    private _pageSizeOptions;
    private _totalElements;
    private _sortParams;
    private _sortDirs;
    private _key;
    private _nextObservable;
    private _previousObservable;
    private _sizeObservable;
    private _sortObservable;
    private _displayedSortDirs?;
    private _displayedSortParams?;
    private _sortHeadersObservable;
    constructor(columns: {
        id: string;
        name: string;
        isActive?: boolean;
    }[], options?: {
        defaultSortParams?: string[];
        defaultSortDirs?: string[];
        pageSizeOptions?: number[];
        totalElements?: number;
        localStorageKey?: string;
    });
    onSortEvent(): void;
    onPaginationEvent($event: PageEvent): void;
    updateSortHeaders(): void;
    private subscribeSortHeaders;
    private init;
    private _clientSideSort;
    private _isLocalStorageSettingsValid;
    storeTableSettings(): void;
    set totalElements(totalElements: number);
    get totalElements(): number;
    set displayedColumns(displayedColumns: string[]);
    get displayedColumns(): string[];
    set dataSource(dataSource: MatMultiSortTableDataSource<T>);
    get dataSource(): MatMultiSortTableDataSource<T>;
    set data(data: T[]);
    set columns(v: {
        id: string;
        name: string;
        isActive?: boolean;
    }[]);
    onColumnsChange(): BehaviorSubject<{
        id: string;
        name: string;
        isActive?: boolean;
    }[]>;
    updateColumnNames(v: {
        id: string;
        name: string;
    }[]): void;
    get nextObservable(): Subject<any>;
    get previousObservable(): Subject<any>;
    get sizeObservable(): Subject<any>;
    get sortObservable(): Subject<any>;
    get sortParams(): string[];
    get sortDirs(): string[];
    get columns(): {
        id: string;
        name: string;
        isActive?: boolean;
    }[];
    get pageSizeOptions(): number[];
    set sortParams(v: string[]);
    set sortDirs(v: string[]);
}
//# sourceMappingURL=table-data.d.ts.map