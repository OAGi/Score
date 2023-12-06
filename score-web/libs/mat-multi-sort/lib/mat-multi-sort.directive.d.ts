import { OnChanges, OnInit } from '@angular/core';
import { MatSort, MatSortable, SortDirection } from '@angular/material/sort';
import * as i0 from "@angular/core";
export declare class MatMultiSort extends MatSort implements OnInit, OnChanges {
    start: "asc" | "desc";
    actives: string[];
    directions: SortDirection[];
    ngOnInit(): void;
    sort(sortable: MatSortable): void;
    updateMultipleSorts(sortable: MatSortable): void;
    isActive(sortable: MatSortable): boolean;
    activeDirection(sortable: MatSortable): 'asc' | 'desc';
    static ɵfac: i0.ɵɵFactoryDeclaration<MatMultiSort, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatMultiSort, "[matMultiSort]", ["matMultiSort"], {}, {}, never, never, false, never>;
}
//# sourceMappingURL=mat-multi-sort.directive.d.ts.map