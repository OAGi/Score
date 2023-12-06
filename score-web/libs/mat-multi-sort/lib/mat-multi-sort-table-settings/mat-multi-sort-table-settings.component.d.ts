import { ElementRef, OnInit, TemplateRef, ViewContainerRef } from '@angular/core';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { TableData } from '../table-data';
import { Overlay, OverlayRef, ScrollStrategy, ViewportRuler } from '@angular/cdk/overlay';
import * as i0 from "@angular/core";
export declare class MatMultiSortTableSettingsComponent implements OnInit {
    private overlay;
    private viewContainerRef;
    private viewportRuler;
    _tableData: TableData<any>;
    sort: {
        id: string;
        name: string;
        direction: string;
    }[];
    overlayRef: OverlayRef;
    private templateRef;
    buttonRef: ElementRef;
    sortIndicatorRef: TemplateRef<any>;
    sortToolTip: string;
    closeDialogOnChoice: boolean;
    scrollStrategy: ScrollStrategy;
    set tableData(tableData: TableData<any>);
    constructor(overlay: Overlay, viewContainerRef: ViewContainerRef, viewportRuler: ViewportRuler);
    ngOnInit(): void;
    openDialog(): void;
    drop(event: CdkDragDrop<string[]>): void;
    toggle(): void;
    dropSort(event: CdkDragDrop<string[]>): void;
    getSort(): {
        id: string;
        name: string;
        direction: string;
    }[];
    remove(id: string): void;
    updateDirection(id: string): void;
    private updateSort;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatMultiSortTableSettingsComponent, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatMultiSortTableSettingsComponent, "mat-multi-sort-table-settings", never, { "sortToolTip": { "alias": "sortToolTip"; "required": false; }; "closeDialogOnChoice": { "alias": "closeDialogOnChoice"; "required": false; }; "scrollStrategy": { "alias": "scrollStrategy"; "required": false; }; "tableData": { "alias": "tableData"; "required": false; }; }, {}, ["sortIndicatorRef"], ["*"], false, never>;
}
//# sourceMappingURL=mat-multi-sort-table-settings.component.d.ts.map