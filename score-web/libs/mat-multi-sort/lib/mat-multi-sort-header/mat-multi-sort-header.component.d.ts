import { ChangeDetectorRef, ElementRef } from '@angular/core';
import { MatSortHeader, MatSortHeaderIntl } from '@angular/material/sort';
import { MatMultiSort } from '../mat-multi-sort.directive';
import { FocusMonitor } from '@angular/cdk/a11y';
import * as i0 from "@angular/core";
/** Column definition associated with a `MatSortHeader`. */
interface C2MatSortHeaderColumnDef {
    name: string;
}
export declare class MatMultiSortHeaderComponent extends MatSortHeader {
    _intl: MatSortHeaderIntl;
    _sort: MatMultiSort;
    _columnDef: C2MatSortHeaderColumnDef;
    start: "asc" | "desc";
    id: string;
    constructor(_intl: MatSortHeaderIntl, changeDetectorRef: ChangeDetectorRef, _sort: MatMultiSort, _columnDef: C2MatSortHeaderColumnDef, _focusMonitor: FocusMonitor, _elementRef: ElementRef<HTMLElement>);
    __setIndicatorHintVisible(visible: string | boolean): void;
    _handleClick(): void;
    _isSorted(): boolean;
    _sortId(): number;
    _updateArrowDirection(): void;
    _getAriaSortAttribute(): "none" | "ascending" | "descending";
    _renderArrow(): boolean;
    getSortDirection(): 'asc' | 'desc' | '';
    static ɵfac: i0.ɵɵFactoryDeclaration<MatMultiSortHeaderComponent, [null, null, { optional: true; }, { optional: true; }, null, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatMultiSortHeaderComponent, "[mat-multi-sort-header]", ["matMultiSortHeader"], { "id": { "alias": "mat-multi-sort-header"; "required": false; }; }, {}, never, ["*"], false, never>;
}
export {};
//# sourceMappingURL=mat-multi-sort-header.component.d.ts.map