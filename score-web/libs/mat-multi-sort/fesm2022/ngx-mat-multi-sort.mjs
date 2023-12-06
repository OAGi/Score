import * as i0 from '@angular/core';
import { Directive, Component, ViewEncapsulation, ChangeDetectionStrategy, Optional, Inject, Input, HostListener, HostBinding, ViewChild, ContentChild, NgModule } from '@angular/core';
import * as i1 from '@angular/material/sort';
import { MatSort, MatSortHeader, matSortAnimations } from '@angular/material/sort';
import * as i3 from '@angular/cdk/a11y';
import * as i2 from '@angular/common';
import { CommonModule } from '@angular/common';
import * as i3$1 from '@angular/cdk/drag-drop';
import { moveItemInArray, DragDropModule } from '@angular/cdk/drag-drop';
import * as i1$1 from '@angular/cdk/overlay';
import { BlockScrollStrategy } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import * as i4 from '@angular/material/icon';
import { MatIconModule } from '@angular/material/icon';
import * as i5 from '@angular/material/checkbox';
import { MatCheckboxModule } from '@angular/material/checkbox';
import * as i6 from '@angular/forms';
import { FormsModule } from '@angular/forms';
import * as i7 from '@angular/material/chips';
import { MatChipsModule } from '@angular/material/chips';
import * as i8 from '@angular/material/tooltip';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { MatCommonModule } from '@angular/material/core';
import { MatDividerModule } from '@angular/material/divider';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialogModule } from '@angular/material/dialog';
import { Subject, BehaviorSubject, delay, filter, tap } from 'rxjs';
import { DataSource } from '@angular/cdk/collections';

class MatMultiSort extends MatSort {
    constructor() {
        super(...arguments);
        this.start = 'asc';
        this.actives = [];
        this.directions = [];
    }
    ngOnInit() {
        super.ngOnInit();
    }
    sort(sortable) {
        this.updateMultipleSorts(sortable);
        super.sort(sortable);
    }
    updateMultipleSorts(sortable) {
        const i = this.actives.findIndex(activeId => activeId === sortable.id);
        if (this.isActive(sortable)) {
            if (this.activeDirection(sortable) === (sortable.start ? sortable.start : this.start)) {
                this.directions.splice(i, 1, this.getNextSortDirection(sortable));
            }
            else {
                this.actives.splice(i, 1);
                this.directions.splice(i, 1);
            }
        }
        else {
            this.actives.push(sortable.id);
            this.directions.push(sortable.start ? sortable.start : this.start);
        }
    }
    isActive(sortable) {
        const i = this.actives.findIndex(activeId => activeId === sortable.id);
        return i > -1;
    }
    activeDirection(sortable) {
        const i = this.actives.findIndex(activeId => activeId === sortable.id);
        return this.directions[i] || (sortable.start ? sortable.start : this.start);
    }
    static { this.ɵfac = /*@__PURE__*/ (() => { let ɵMatMultiSort_BaseFactory; return function MatMultiSort_Factory(t) { return (ɵMatMultiSort_BaseFactory || (ɵMatMultiSort_BaseFactory = i0.ɵɵgetInheritedFactory(MatMultiSort)))(t || MatMultiSort); }; })(); }
    static { this.ɵdir = /*@__PURE__*/ i0.ɵɵdefineDirective({ type: MatMultiSort, selectors: [["", "matMultiSort", ""]], exportAs: ["matMultiSort"], features: [i0.ɵɵInheritDefinitionFeature] }); }
}
(() => { (typeof ngDevMode === "undefined" || ngDevMode) && i0.ɵsetClassMetadata(MatMultiSort, [{
        type: Directive,
        args: [{
                selector: '[matMultiSort]',
                exportAs: 'matMultiSort'
            }]
    }], null, null); })();

const _c0$1 = ["mat-multi-sort-header", ""];
function MatMultiSortHeaderComponent_div_3_Template(rf, ctx) { if (rf & 1) {
    const _r3 = i0.ɵɵgetCurrentView();
    i0.ɵɵelementStart(0, "div", 4);
    i0.ɵɵlistener("@arrowPosition.start", function MatMultiSortHeaderComponent_div_3_Template_div_animation_arrowPosition_start_0_listener() { i0.ɵɵrestoreView(_r3); const ctx_r2 = i0.ɵɵnextContext(); return i0.ɵɵresetView(ctx_r2._disableViewStateAnimation = true); })("@arrowPosition.done", function MatMultiSortHeaderComponent_div_3_Template_div_animation_arrowPosition_done_0_listener() { i0.ɵɵrestoreView(_r3); const ctx_r4 = i0.ɵɵnextContext(); return i0.ɵɵresetView(ctx_r4._disableViewStateAnimation = false); });
    i0.ɵɵelement(1, "div", 5);
    i0.ɵɵelementStart(2, "div", 6);
    i0.ɵɵelement(3, "div", 7)(4, "div", 8)(5, "div", 9);
    i0.ɵɵelementEnd()();
} if (rf & 2) {
    const ctx_r0 = i0.ɵɵnextContext();
    i0.ɵɵproperty("@arrowOpacity", ctx_r0._getArrowViewState())("@arrowPosition", ctx_r0._getArrowViewState())("@allowChildren", ctx_r0._getArrowDirectionState());
    i0.ɵɵadvance(2);
    i0.ɵɵproperty("@indicator", ctx_r0._getArrowDirectionState());
    i0.ɵɵadvance(1);
    i0.ɵɵproperty("@leftPointer", ctx_r0._getArrowDirectionState());
    i0.ɵɵadvance(1);
    i0.ɵɵproperty("@rightPointer", ctx_r0._getArrowDirectionState());
} }
function MatMultiSortHeaderComponent_div_4_Template(rf, ctx) { if (rf & 1) {
    i0.ɵɵelementStart(0, "div");
    i0.ɵɵtext(1);
    i0.ɵɵelementEnd();
} if (rf & 2) {
    const ctx_r1 = i0.ɵɵnextContext();
    i0.ɵɵadvance(1);
    i0.ɵɵtextInterpolate(ctx_r1._sortId());
} }
const _c1$1 = ["*"];
class MatMultiSortHeaderComponent extends MatSortHeader {
    constructor(_intl, changeDetectorRef, _sort, _columnDef, _focusMonitor, _elementRef) {
        super(_intl, changeDetectorRef, _sort, _columnDef, _focusMonitor, _elementRef);
        this._intl = _intl;
        this._sort = _sort;
        this._columnDef = _columnDef;
        this.start = 'asc';
    }
    __setIndicatorHintVisible(visible) {
        super._setIndicatorHintVisible(visible);
    }
    _handleClick() {
        this._sort.direction = this.getSortDirection();
        super._handleClick();
    }
    _isSorted() {
        return this._sort.actives.findIndex(activeId => activeId === this.id) > -1;
    }
    _sortId() {
        return this._sort.actives.findIndex(activeId => activeId === this.id) + 1;
    }
    _updateArrowDirection() {
        this._arrowDirection = this.getSortDirection();
    }
    _getAriaSortAttribute() {
        if (!this._isSorted()) {
            return 'none';
        }
        return this.getSortDirection() === 'asc' ? 'ascending' : 'descending';
    }
    _renderArrow() {
        return !this._isDisabled() || this._isSorted();
    }
    getSortDirection() {
        const i = this._sort.actives.findIndex(activeIds => activeIds === this.id);
        const direction = this._sort.directions[i];
        return this._isSorted() ? direction : (this.start || this._sort.start);
    }
    static { this.ɵfac = function MatMultiSortHeaderComponent_Factory(t) { return new (t || MatMultiSortHeaderComponent)(i0.ɵɵdirectiveInject(i1.MatSortHeaderIntl), i0.ɵɵdirectiveInject(i0.ChangeDetectorRef), i0.ɵɵdirectiveInject(MatMultiSort, 8), i0.ɵɵdirectiveInject('C2_SORT_HEADER_COLUMN_DEF', 8), i0.ɵɵdirectiveInject(i3.FocusMonitor), i0.ɵɵdirectiveInject(i0.ElementRef)); }; }
    static { this.ɵcmp = /*@__PURE__*/ i0.ɵɵdefineComponent({ type: MatMultiSortHeaderComponent, selectors: [["", "mat-multi-sort-header", ""]], hostVars: 1, hostBindings: function MatMultiSortHeaderComponent_HostBindings(rf, ctx) { if (rf & 1) {
            i0.ɵɵlistener("mouseenter", function MatMultiSortHeaderComponent_mouseenter_HostBindingHandler() { return ctx.__setIndicatorHintVisible(true); })("longpress", function MatMultiSortHeaderComponent_longpress_HostBindingHandler() { return ctx.__setIndicatorHintVisible(true); })("mouseleave", function MatMultiSortHeaderComponent_mouseleave_HostBindingHandler() { return ctx.__setIndicatorHintVisible(false); });
        } if (rf & 2) {
            i0.ɵɵattribute("aria-sort", ctx._getAriaSortAttribute);
        } }, inputs: { id: ["mat-multi-sort-header", "id"] }, exportAs: ["matMultiSortHeader"], features: [i0.ɵɵInheritDefinitionFeature], attrs: _c0$1, ngContentSelectors: _c1$1, decls: 5, vars: 6, consts: [[1, "mat-sort-header-container"], [1, "mat-sort-header-content"], ["class", "mat-sort-header-arrow", 4, "ngIf"], [4, "ngIf"], [1, "mat-sort-header-arrow"], [1, "mat-sort-header-stem"], [1, "mat-sort-header-indicator"], [1, "mat-sort-header-pointer-left"], [1, "mat-sort-header-pointer-right"], [1, "mat-sort-header-pointer-middle"]], template: function MatMultiSortHeaderComponent_Template(rf, ctx) { if (rf & 1) {
            i0.ɵɵprojectionDef();
            i0.ɵɵelementStart(0, "div", 0)(1, "div", 1);
            i0.ɵɵprojection(2);
            i0.ɵɵelementEnd();
            i0.ɵɵtemplate(3, MatMultiSortHeaderComponent_div_3_Template, 6, 6, "div", 2)(4, MatMultiSortHeaderComponent_div_4_Template, 2, 1, "div", 3);
            i0.ɵɵelementEnd();
        } if (rf & 2) {
            i0.ɵɵclassProp("mat-sort-header-sorted", ctx._isSorted())("mat-sort-header-position-before", ctx.arrowPosition == "before");
            i0.ɵɵadvance(3);
            i0.ɵɵproperty("ngIf", ctx._renderArrow());
            i0.ɵɵadvance(1);
            i0.ɵɵproperty("ngIf", ctx._isSorted());
        } }, dependencies: [i2.NgIf], styles: [".mat-sort-header-container{display:flex;cursor:pointer;align-items:center}.mat-sort-header-disabled .mat-sort-header-container{cursor:default}.mat-sort-header-position-before{flex-direction:row-reverse}.mat-sort-header-content{text-align:center;display:flex;align-items:center}.mat-sort-header-arrow{height:12px;width:12px;min-width:12px;position:relative;display:flex;opacity:0}.mat-sort-header-arrow,[dir=rtl] .mat-sort-header-position-before .mat-sort-header-arrow{margin:0 0 0 6px}.mat-sort-header-position-before .mat-sort-header-arrow,[dir=rtl] .mat-sort-header-arrow{margin:0 6px 0 0}.mat-sort-header-stem{background:currentColor;height:10px;width:2px;margin:auto;display:flex;align-items:center}.mat-sort-header-indicator{width:100%;height:2px;display:flex;align-items:center;position:absolute;top:0;left:0}.mat-sort-header-pointer-middle{margin:auto;height:2px;width:2px;background:currentColor;transform:rotate(45deg)}.mat-sort-header-pointer-left,.mat-sort-header-pointer-right{background:currentColor;width:6px;height:2px;position:absolute;top:0}.mat-sort-header-pointer-left{transform-origin:right;left:0}.mat-sort-header-pointer-right{transform-origin:left;right:0}\n"], encapsulation: 2, data: { animation: [
                matSortAnimations.indicator,
                matSortAnimations.leftPointer,
                matSortAnimations.rightPointer,
                matSortAnimations.arrowOpacity,
                matSortAnimations.arrowPosition,
                matSortAnimations.allowChildren
            ] }, changeDetection: 0 }); }
}
(() => { (typeof ngDevMode === "undefined" || ngDevMode) && i0.ɵsetClassMetadata(MatMultiSortHeaderComponent, [{
        type: Component,
        args: [{ selector: '[mat-multi-sort-header]', exportAs: 'matMultiSortHeader', encapsulation: ViewEncapsulation.None, changeDetection: ChangeDetectionStrategy.OnPush, animations: [
                    matSortAnimations.indicator,
                    matSortAnimations.leftPointer,
                    matSortAnimations.rightPointer,
                    matSortAnimations.arrowOpacity,
                    matSortAnimations.arrowPosition,
                    matSortAnimations.allowChildren
                ], template: "<div class=\"mat-sort-header-container\"\n     [class.mat-sort-header-sorted]=\"_isSorted()\"\n     [class.mat-sort-header-position-before]=\"arrowPosition == 'before'\">\n\n  <div class=\"mat-sort-header-content\">\n    <ng-content></ng-content>\n  </div>\n\n  <!-- Disable animations while a current animation is running -->\n  <div class=\"mat-sort-header-arrow\"\n       *ngIf=\"_renderArrow()\"\n       [@arrowOpacity]=\"_getArrowViewState()\"\n       [@arrowPosition]=\"_getArrowViewState()\"\n       [@allowChildren]=\"_getArrowDirectionState()\"\n       (@arrowPosition.start)=\"_disableViewStateAnimation = true\"\n       (@arrowPosition.done)=\"_disableViewStateAnimation = false\">\n    <div class=\"mat-sort-header-stem\"></div>\n    <div class=\"mat-sort-header-indicator\" [@indicator]=\"_getArrowDirectionState()\">\n      <div class=\"mat-sort-header-pointer-left\" [@leftPointer]=\"_getArrowDirectionState()\"></div>\n      <div class=\"mat-sort-header-pointer-right\" [@rightPointer]=\"_getArrowDirectionState()\"></div>\n      <div class=\"mat-sort-header-pointer-middle\"></div>\n    </div>\n  </div>\n  <div *ngIf=\"_isSorted()\">{{_sortId()}}</div>\n</div>\n", styles: [".mat-sort-header-container{display:flex;cursor:pointer;align-items:center}.mat-sort-header-disabled .mat-sort-header-container{cursor:default}.mat-sort-header-position-before{flex-direction:row-reverse}.mat-sort-header-content{text-align:center;display:flex;align-items:center}.mat-sort-header-arrow{height:12px;width:12px;min-width:12px;position:relative;display:flex;opacity:0}.mat-sort-header-arrow,[dir=rtl] .mat-sort-header-position-before .mat-sort-header-arrow{margin:0 0 0 6px}.mat-sort-header-position-before .mat-sort-header-arrow,[dir=rtl] .mat-sort-header-arrow{margin:0 6px 0 0}.mat-sort-header-stem{background:currentColor;height:10px;width:2px;margin:auto;display:flex;align-items:center}.mat-sort-header-indicator{width:100%;height:2px;display:flex;align-items:center;position:absolute;top:0;left:0}.mat-sort-header-pointer-middle{margin:auto;height:2px;width:2px;background:currentColor;transform:rotate(45deg)}.mat-sort-header-pointer-left,.mat-sort-header-pointer-right{background:currentColor;width:6px;height:2px;position:absolute;top:0}.mat-sort-header-pointer-left{transform-origin:right;left:0}.mat-sort-header-pointer-right{transform-origin:left;right:0}\n"] }]
    }], () => [{ type: i1.MatSortHeaderIntl }, { type: i0.ChangeDetectorRef }, { type: MatMultiSort, decorators: [{
                type: Optional
            }] }, { type: undefined, decorators: [{
                type: Inject,
                args: ['C2_SORT_HEADER_COLUMN_DEF']
            }, {
                type: Optional
            }] }, { type: i3.FocusMonitor }, { type: i0.ElementRef }], { id: [{
            type: Input,
            args: ['mat-multi-sort-header']
        }], __setIndicatorHintVisible: [{
            type: HostListener,
            args: ['mouseenter', ['true']]
        }, {
            type: HostListener,
            args: ['longpress', ['true']]
        }, {
            type: HostListener,
            args: ['mouseleave', ['false']]
        }], _getAriaSortAttribute: [{
            type: HostBinding,
            args: ['attr.aria-sort']
        }] }); })();
(() => { (typeof ngDevMode === "undefined" || ngDevMode) && i0.ɵsetClassDebugInfo(MatMultiSortHeaderComponent, { className: "MatMultiSortHeaderComponent", filePath: "lib\\mat-multi-sort-header\\mat-multi-sort-header.component.ts", lineNumber: 38 }); })();

const _c0 = ["sortIndicator"];
const _c1 = ["templateRef"];
const _c2 = ["settingsMenu"];
const _c3 = (a0, a1) => ({ direction: a0, columnName: a1 });
function MatMultiSortTableSettingsComponent_mat_chip_3_ng_container_1_Template(rf, ctx) { if (rf & 1) {
    i0.ɵɵelementContainer(0, 12);
} if (rf & 2) {
    const item_r4 = i0.ɵɵnextContext().$implicit;
    const ctx_r5 = i0.ɵɵnextContext();
    i0.ɵɵproperty("ngTemplateOutlet", ctx_r5.sortIndicatorRef)("ngTemplateOutletContext", i0.ɵɵpureFunction2(2, _c3, item_r4.direction, item_r4.name));
} }
function MatMultiSortTableSettingsComponent_mat_chip_3_div_2_Template(rf, ctx) { if (rf & 1) {
    i0.ɵɵelementStart(0, "div");
    i0.ɵɵtext(1);
    i0.ɵɵelementStart(2, "div", 13);
    i0.ɵɵtext(3);
    i0.ɵɵelementEnd()();
} if (rf & 2) {
    const item_r4 = i0.ɵɵnextContext().$implicit;
    const ctx_r6 = i0.ɵɵnextContext();
    i0.ɵɵadvance(1);
    i0.ɵɵtextInterpolate1(" ", item_r4.name, ": ");
    i0.ɵɵadvance(1);
    i0.ɵɵproperty("matTooltip", ctx_r6.sortToolTip);
    i0.ɵɵadvance(1);
    i0.ɵɵtextInterpolate1(" ", item_r4.direction, " ");
} }
function MatMultiSortTableSettingsComponent_mat_chip_3_Template(rf, ctx) { if (rf & 1) {
    const _r10 = i0.ɵɵgetCurrentView();
    i0.ɵɵelementStart(0, "mat-chip", 8);
    i0.ɵɵlistener("removed", function MatMultiSortTableSettingsComponent_mat_chip_3_Template_mat_chip_removed_0_listener() { const restoredCtx = i0.ɵɵrestoreView(_r10); const item_r4 = restoredCtx.$implicit; const ctx_r9 = i0.ɵɵnextContext(); return i0.ɵɵresetView(ctx_r9.remove(item_r4.id)); })("click", function MatMultiSortTableSettingsComponent_mat_chip_3_Template_mat_chip_click_0_listener() { const restoredCtx = i0.ɵɵrestoreView(_r10); const item_r4 = restoredCtx.$implicit; const ctx_r11 = i0.ɵɵnextContext(); return i0.ɵɵresetView(ctx_r11.updateDirection(item_r4.id)); });
    i0.ɵɵtemplate(1, MatMultiSortTableSettingsComponent_mat_chip_3_ng_container_1_Template, 1, 5, "ng-container", 9)(2, MatMultiSortTableSettingsComponent_mat_chip_3_div_2_Template, 4, 3, "div", 10);
    i0.ɵɵelementStart(3, "mat-icon", 11);
    i0.ɵɵtext(4, "cancel");
    i0.ɵɵelementEnd()();
} if (rf & 2) {
    const ctx_r0 = i0.ɵɵnextContext();
    i0.ɵɵadvance(1);
    i0.ɵɵproperty("ngIf", ctx_r0.sortIndicatorRef);
    i0.ɵɵadvance(1);
    i0.ɵɵproperty("ngIf", !ctx_r0.sortIndicatorRef);
} }
function MatMultiSortTableSettingsComponent_ng_template_8_div_1_Template(rf, ctx) { if (rf & 1) {
    const _r15 = i0.ɵɵgetCurrentView();
    i0.ɵɵelementStart(0, "div", 16)(1, "mat-icon");
    i0.ɵɵtext(2, "drag_indicator");
    i0.ɵɵelementEnd();
    i0.ɵɵelementStart(3, "mat-checkbox", 17);
    i0.ɵɵlistener("ngModelChange", function MatMultiSortTableSettingsComponent_ng_template_8_div_1_Template_mat_checkbox_ngModelChange_3_listener($event) { const restoredCtx = i0.ɵɵrestoreView(_r15); const column_r13 = restoredCtx.$implicit; return i0.ɵɵresetView(column_r13.isActive = $event); })("change", function MatMultiSortTableSettingsComponent_ng_template_8_div_1_Template_mat_checkbox_change_3_listener() { i0.ɵɵrestoreView(_r15); const ctx_r16 = i0.ɵɵnextContext(2); return i0.ɵɵresetView(ctx_r16.toggle()); });
    i0.ɵɵtext(4);
    i0.ɵɵelementEnd()();
} if (rf & 2) {
    const column_r13 = ctx.$implicit;
    i0.ɵɵadvance(3);
    i0.ɵɵproperty("ngModel", column_r13.isActive);
    i0.ɵɵadvance(1);
    i0.ɵɵtextInterpolate(column_r13.name);
} }
function MatMultiSortTableSettingsComponent_ng_template_8_Template(rf, ctx) { if (rf & 1) {
    const _r18 = i0.ɵɵgetCurrentView();
    i0.ɵɵelementStart(0, "div", 14);
    i0.ɵɵlistener("cdkDropListDropped", function MatMultiSortTableSettingsComponent_ng_template_8_Template_div_cdkDropListDropped_0_listener($event) { i0.ɵɵrestoreView(_r18); const ctx_r17 = i0.ɵɵnextContext(); return i0.ɵɵresetView(ctx_r17.drop($event)); });
    i0.ɵɵtemplate(1, MatMultiSortTableSettingsComponent_ng_template_8_div_1_Template, 5, 2, "div", 15);
    i0.ɵɵelementEnd();
} if (rf & 2) {
    const ctx_r2 = i0.ɵɵnextContext();
    i0.ɵɵadvance(1);
    i0.ɵɵproperty("ngForOf", ctx_r2._tableData.columns);
} }
const _c4 = ["*"];
class MatMultiSortTableSettingsComponent {
    set tableData(tableData) {
        this._tableData = tableData;
    }
    constructor(overlay, viewContainerRef, viewportRuler) {
        this.overlay = overlay;
        this.viewContainerRef = viewContainerRef;
        this.viewportRuler = viewportRuler;
        this.sort = [];
        this.sortToolTip = '';
        this.closeDialogOnChoice = true;
        this.scrollStrategy = new BlockScrollStrategy(this.viewportRuler, document);
    }
    ngOnInit() {
        this.sort = this.getSort();
        this._tableData.sortObservable.subscribe(() => this.sort = this.getSort());
        this._tableData.onColumnsChange().subscribe(() => this.sort = this.getSort());
    }
    openDialog() {
        const button = this.buttonRef.nativeElement;
        const positionStrategyBuilder = this.overlay.position();
        const positionStrategy = positionStrategyBuilder
            .flexibleConnectedTo(button)
            .withFlexibleDimensions(true)
            .withViewportMargin(10)
            .withGrowAfterOpen(true)
            .withPush(true)
            .withPositions([{
                originX: 'end',
                originY: 'bottom',
                overlayX: 'end',
                overlayY: 'top'
            }]);
        this.overlayRef = this.overlay.create({
            hasBackdrop: true,
            backdropClass: 'cdk-overlay-transparent-backdrop',
            panelClass: 'column-overlay',
            positionStrategy,
            scrollStrategy: this.scrollStrategy
        });
        const templatePortal = new TemplatePortal(this.templateRef, this.viewContainerRef);
        this.overlayRef.attach(templatePortal);
        this.overlayRef.backdropClick().subscribe(() => {
            this.overlayRef.dispose();
        });
    }
    drop(event) {
        moveItemInArray(this._tableData.columns, event.previousIndex, event.currentIndex);
        this._tableData.displayedColumns = this._tableData.columns.filter(c => c.isActive).map(c => c.id);
        this._tableData.storeTableSettings();
    }
    toggle() {
        this._tableData.displayedColumns = this._tableData.columns.filter(c => {
            if (!c.isActive) {
                this.sort = this.sort.filter(s => s.id !== c.id);
            }
            return c.isActive;
        }).map(c => c.id);
        this.updateSort();
        if (this.closeDialogOnChoice) {
            this.overlayRef.dispose();
        }
    }
    dropSort(event) {
        moveItemInArray(this.sort, event.previousIndex, event.currentIndex);
        this.updateSort();
    }
    getSort() {
        const sorting = [];
        for (let i = 0; i < this._tableData.sortParams.length; i++) {
            sorting.push({
                id: this._tableData.sortParams[i],
                name: this._tableData.columns.find(c => c.id === this._tableData.sortParams[i]).name,
                direction: this._tableData.sortDirs[i]
            });
        }
        return sorting;
    }
    remove(id) {
        this.sort = this.sort.filter(v => v.id !== id);
        this.updateSort();
    }
    updateDirection(id) {
        const i = this.sort.findIndex(v => v.id === id);
        if (this.sort[i].direction === 'asc') {
            this.sort[i].direction = 'desc';
        }
        else {
            this.sort[i].direction = 'asc';
        }
        this.updateSort();
    }
    updateSort() {
        this._tableData.sortParams = this.sort.map(v => v.id);
        this._tableData.sortDirs = this.sort.map(v => v.direction);
        this._tableData.updateSortHeaders();
    }
    static { this.ɵfac = function MatMultiSortTableSettingsComponent_Factory(t) { return new (t || MatMultiSortTableSettingsComponent)(i0.ɵɵdirectiveInject(i1$1.Overlay), i0.ɵɵdirectiveInject(i0.ViewContainerRef), i0.ɵɵdirectiveInject(i1$1.ViewportRuler)); }; }
    static { this.ɵcmp = /*@__PURE__*/ i0.ɵɵdefineComponent({ type: MatMultiSortTableSettingsComponent, selectors: [["mat-multi-sort-table-settings"]], contentQueries: function MatMultiSortTableSettingsComponent_ContentQueries(rf, ctx, dirIndex) { if (rf & 1) {
            i0.ɵɵcontentQuery(dirIndex, _c0, 5);
        } if (rf & 2) {
            let _t;
            i0.ɵɵqueryRefresh(_t = i0.ɵɵloadQuery()) && (ctx.sortIndicatorRef = _t.first);
        } }, viewQuery: function MatMultiSortTableSettingsComponent_Query(rf, ctx) { if (rf & 1) {
            i0.ɵɵviewQuery(_c1, 7);
            i0.ɵɵviewQuery(_c2, 5);
        } if (rf & 2) {
            let _t;
            i0.ɵɵqueryRefresh(_t = i0.ɵɵloadQuery()) && (ctx.templateRef = _t.first);
            i0.ɵɵqueryRefresh(_t = i0.ɵɵloadQuery()) && (ctx.buttonRef = _t.first);
        } }, inputs: { sortToolTip: "sortToolTip", closeDialogOnChoice: "closeDialogOnChoice", scrollStrategy: "scrollStrategy", tableData: "tableData" }, ngContentSelectors: _c4, decls: 10, vars: 1, consts: [[1, "table-settings"], [1, "table-settings-sort"], ["cdkDropList", "", "cdkDropListOrientation", "horizontal", 1, "drag-chip-list", 3, "cdkDropListDropped"], ["class", "drag-chip", "cdkDrag", "", 3, "removed", "click", 4, "ngFor", "ngForOf"], [2, "flex", "1 1 auto"], [1, "table-settings-menu", 3, "click"], ["settingsMenu", ""], ["templateRef", ""], ["cdkDrag", "", 1, "drag-chip", 3, "removed", "click"], [3, "ngTemplateOutlet", "ngTemplateOutletContext", 4, "ngIf"], [4, "ngIf"], ["matChipRemove", ""], [3, "ngTemplateOutlet", "ngTemplateOutletContext"], [1, "sorting", 3, "matTooltip"], ["cdkDropList", "", 1, "column-list", 3, "cdkDropListDropped"], ["class", "column-item", "cdkDrag", "", 4, "ngFor", "ngForOf"], ["cdkDrag", "", 1, "column-item"], [3, "ngModel", "ngModelChange", "change"]], template: function MatMultiSortTableSettingsComponent_Template(rf, ctx) { if (rf & 1) {
            i0.ɵɵprojectionDef();
            i0.ɵɵelementStart(0, "div", 0)(1, "div", 1)(2, "mat-chip-list", 2);
            i0.ɵɵlistener("cdkDropListDropped", function MatMultiSortTableSettingsComponent_Template_mat_chip_list_cdkDropListDropped_2_listener($event) { return ctx.dropSort($event); });
            i0.ɵɵtemplate(3, MatMultiSortTableSettingsComponent_mat_chip_3_Template, 5, 2, "mat-chip", 3);
            i0.ɵɵelementEnd()();
            i0.ɵɵelement(4, "div", 4);
            i0.ɵɵelementStart(5, "div", 5, 6);
            i0.ɵɵlistener("click", function MatMultiSortTableSettingsComponent_Template_div_click_5_listener() { return ctx.openDialog(); });
            i0.ɵɵprojection(7, 0, ["#menuRef", ""]);
            i0.ɵɵelementEnd()();
            i0.ɵɵtemplate(8, MatMultiSortTableSettingsComponent_ng_template_8_Template, 2, 1, "ng-template", null, 7, i0.ɵɵtemplateRefExtractor);
        } if (rf & 2) {
            i0.ɵɵadvance(3);
            i0.ɵɵproperty("ngForOf", ctx.sort);
        } }, dependencies: [i2.NgForOf, i2.NgIf, i2.NgTemplateOutlet, i3$1.CdkDropList, i3$1.CdkDrag, i4.MatIcon, i5.MatCheckbox, i6.NgControlStatus, i6.NgModel, i7.MatChip, i7.MatChipRemove, i8.MatTooltip], styles: [".table-settings[_ngcontent-%COMP%]{display:flex}.table-settings[_ngcontent-%COMP%]   .table-settings-menu[_ngcontent-%COMP%]{margin:8px 16px}.table-settings-sort[_ngcontent-%COMP%]{margin:auto 0}.sorting[_ngcontent-%COMP%]{display:inline-block;margin:0 6px;color:#757575}.sorting[_ngcontent-%COMP%]:hover{cursor:pointer}.drag-chip[_ngcontent-%COMP%]{border:solid 1px rgba(0,0,0,.12);background-color:#fff}.drag-chip[_ngcontent-%COMP%]:hover{cursor:move;background-color:#fff}.drag-chip[_ngcontent-%COMP%]:hover:after{opacity:0}.drag-chip[_ngcontent-%COMP%]:focus:after{opacity:0}.drag-chip-list.cdk-drop-list-dragging[_ngcontent-%COMP%]   .drag-chip[_ngcontent-%COMP%]:not(.cdk-drag-placeholder){transition:transform .25s cubic-bezier(0,0,.2,1)}.column-list[_ngcontent-%COMP%]{max-height:70vh;overflow:auto;border-radius:4px;padding:1rem;box-shadow:0 11px 15px -7px #0003,0 24px 38px 3px #00000024,0 9px 46px 8px #0000001f;background-color:#fff;color:#000000de}.column-item[_ngcontent-%COMP%]{height:48px;display:flex;justify-content:flex-start;align-items:center;margin:1px;padding:0 16px 0 8px}.column-item[_ngcontent-%COMP%]   mat-icon[_ngcontent-%COMP%]{margin-right:16px}.column-item[_ngcontent-%COMP%]   mat-checkbox[_ngcontent-%COMP%]{line-height:48px;color:#000000de;font-size:14px;font-weight:400}.column-item[_ngcontent-%COMP%]:hover{cursor:move;border-top:solid 1px rgba(0,0,0,.12);border-bottom:solid 1px rgba(0,0,0,.12)}.cdk-drag-preview[_ngcontent-%COMP%]{box-sizing:border-box;border-radius:4px;box-shadow:0 5px 5px -3px #0003,0 8px 10px 1px #00000024,0 3px 14px 2px #0000001f}.cdk-drag-placeholder[_ngcontent-%COMP%]{opacity:0}.cdk-drag-animating[_ngcontent-%COMP%]{transition:transform .25s cubic-bezier(0,0,.2,1)}.column-item[_ngcontent-%COMP%]:last-child{border:none}.column-list.cdk-drop-list-dragging[_ngcontent-%COMP%]   .column-item[_ngcontent-%COMP%]:not(.cdk-drag-placeholder){transition:transform .25s cubic-bezier(0,0,.2,1)}"] }); }
}
(() => { (typeof ngDevMode === "undefined" || ngDevMode) && i0.ɵsetClassMetadata(MatMultiSortTableSettingsComponent, [{
        type: Component,
        args: [{ selector: 'mat-multi-sort-table-settings', template: "<div class=\"table-settings\">\n    <div class=\"table-settings-sort\">\n        <mat-chip-list class=\"drag-chip-list\" cdkDropList cdkDropListOrientation='horizontal'\n            (cdkDropListDropped)=\"dropSort($event)\">\n            <mat-chip class=\"drag-chip\" *ngFor=\"let item of sort\" cdkDrag (removed)=\"remove(item.id)\"\n                (click)=\"updateDirection(item.id)\">\n                <ng-container *ngIf=\"sortIndicatorRef\"\n                              [ngTemplateOutlet]=\"sortIndicatorRef\"\n                              [ngTemplateOutletContext]=\"{direction:item.direction, columnName: item.name }\">\n                </ng-container>\n                <div *ngIf=\"!sortIndicatorRef\">\n                    {{item.name}}:\n                    <div class=\"sorting\" [matTooltip]=\"sortToolTip\">\n                        {{item.direction}}\n                    </div>\n                </div>\n                <mat-icon matChipRemove>cancel</mat-icon>\n            </mat-chip>\n        </mat-chip-list>\n    </div>\n    <div style=\"flex: 1 1 auto;\"></div>\n    <div #settingsMenu (click)=\"openDialog()\" class=\"table-settings-menu\">\n        <ng-content #menuRef></ng-content>\n    </div>\n</div>\n\n<ng-template #templateRef>\n  <div cdkDropList class=\"column-list\" (cdkDropListDropped)=\"drop($event)\">\n    <div class=\"column-item\" *ngFor=\"let column of _tableData.columns\" cdkDrag>\n      <mat-icon>drag_indicator</mat-icon>\n      <mat-checkbox [(ngModel)]=\"column.isActive\" (change)=\"toggle()\">{{column.name}}</mat-checkbox>\n    </div>\n  </div>\n</ng-template>\n", styles: [".table-settings{display:flex}.table-settings .table-settings-menu{margin:8px 16px}.table-settings-sort{margin:auto 0}.sorting{display:inline-block;margin:0 6px;color:#757575}.sorting:hover{cursor:pointer}.drag-chip{border:solid 1px rgba(0,0,0,.12);background-color:#fff}.drag-chip:hover{cursor:move;background-color:#fff}.drag-chip:hover:after{opacity:0}.drag-chip:focus:after{opacity:0}.drag-chip-list.cdk-drop-list-dragging .drag-chip:not(.cdk-drag-placeholder){transition:transform .25s cubic-bezier(0,0,.2,1)}.column-list{max-height:70vh;overflow:auto;border-radius:4px;padding:1rem;box-shadow:0 11px 15px -7px #0003,0 24px 38px 3px #00000024,0 9px 46px 8px #0000001f;background-color:#fff;color:#000000de}.column-item{height:48px;display:flex;justify-content:flex-start;align-items:center;margin:1px;padding:0 16px 0 8px}.column-item mat-icon{margin-right:16px}.column-item mat-checkbox{line-height:48px;color:#000000de;font-size:14px;font-weight:400}.column-item:hover{cursor:move;border-top:solid 1px rgba(0,0,0,.12);border-bottom:solid 1px rgba(0,0,0,.12)}.cdk-drag-preview{box-sizing:border-box;border-radius:4px;box-shadow:0 5px 5px -3px #0003,0 8px 10px 1px #00000024,0 3px 14px 2px #0000001f}.cdk-drag-placeholder{opacity:0}.cdk-drag-animating{transition:transform .25s cubic-bezier(0,0,.2,1)}.column-item:last-child{border:none}.column-list.cdk-drop-list-dragging .column-item:not(.cdk-drag-placeholder){transition:transform .25s cubic-bezier(0,0,.2,1)}\n"] }]
    }], () => [{ type: i1$1.Overlay }, { type: i0.ViewContainerRef }, { type: i1$1.ViewportRuler }], { templateRef: [{
            type: ViewChild,
            args: ['templateRef', { static: true }]
        }], buttonRef: [{
            type: ViewChild,
            args: ['settingsMenu']
        }], sortIndicatorRef: [{
            type: ContentChild,
            args: ['sortIndicator', { static: false }]
        }], sortToolTip: [{
            type: Input
        }], closeDialogOnChoice: [{
            type: Input
        }], scrollStrategy: [{
            type: Input
        }], tableData: [{
            type: Input
        }] }); })();
(() => { (typeof ngDevMode === "undefined" || ngDevMode) && i0.ɵsetClassDebugInfo(MatMultiSortTableSettingsComponent, { className: "MatMultiSortTableSettingsComponent", filePath: "lib\\mat-multi-sort-table-settings\\mat-multi-sort-table-settings.component.ts", lineNumber: 13 }); })();

class MatMultiSortModule {
    static { this.ɵfac = function MatMultiSortModule_Factory(t) { return new (t || MatMultiSortModule)(); }; }
    static { this.ɵmod = /*@__PURE__*/ i0.ɵɵdefineNgModule({ type: MatMultiSortModule }); }
    static { this.ɵinj = /*@__PURE__*/ i0.ɵɵdefineInjector({ imports: [CommonModule,
            MatCommonModule,
            MatDividerModule,
            DragDropModule,
            MatIconModule,
            MatCheckboxModule,
            MatMenuModule,
            MatButtonModule,
            FormsModule,
            MatChipsModule,
            MatTooltipModule,
            MatDialogModule] }); }
}
(() => { (typeof ngDevMode === "undefined" || ngDevMode) && i0.ɵsetClassMetadata(MatMultiSortModule, [{
        type: NgModule,
        args: [{
                declarations: [
                    MatMultiSortHeaderComponent,
                    MatMultiSort,
                    MatMultiSortTableSettingsComponent,
                ],
                exports: [
                    MatMultiSortHeaderComponent,
                    MatMultiSort,
                    MatMultiSortTableSettingsComponent
                ],
                imports: [
                    CommonModule,
                    MatCommonModule,
                    MatDividerModule,
                    DragDropModule,
                    MatIconModule,
                    MatCheckboxModule,
                    MatMenuModule,
                    MatButtonModule,
                    FormsModule,
                    MatChipsModule,
                    MatTooltipModule,
                    MatDialogModule
                ]
            }]
    }], null, null); })();
(function () { (typeof ngJitMode === "undefined" || ngJitMode) && i0.ɵɵsetNgModuleScope(MatMultiSortModule, { declarations: [MatMultiSortHeaderComponent,
        MatMultiSort,
        MatMultiSortTableSettingsComponent], imports: [CommonModule,
        MatCommonModule,
        MatDividerModule,
        DragDropModule,
        MatIconModule,
        MatCheckboxModule,
        MatMenuModule,
        MatButtonModule,
        FormsModule,
        MatChipsModule,
        MatTooltipModule,
        MatDialogModule], exports: [MatMultiSortHeaderComponent,
        MatMultiSort,
        MatMultiSortTableSettingsComponent] }); })();

class Settings {
    constructor(key) {
        this._key = key;
        this._columns = [];
        this._sortParams = [];
        this._sortDirs = [];
    }
    load() {
        const value = JSON.parse(localStorage.getItem(this._key));
        if (value) {
            this._columns = value._columns || [];
            this._sortDirs = value._sortDirs || [];
            this._sortParams = value._sortParams || [];
        }
    }
    save() {
        const settingsString = JSON.stringify(this);
        localStorage.setItem(this._key, settingsString);
    }
    get columns() {
        return this._columns;
    }
    get sortParams() {
        return this._sortParams;
    }
    get sortDirs() {
        return this._sortDirs;
    }
    get key() {
        return this._key;
    }
    set columns(columns) {
        this._columns = columns;
    }
    set sortParams(sortParams) {
        this._sortParams = sortParams;
    }
    set sortDirs(sortDirs) {
        this._sortDirs = sortDirs;
    }
}

class TableData {
    // TODO refactor
    constructor(columns, options) {
        this._nextObservable = new Subject();
        this._previousObservable = new Subject();
        this._sizeObservable = new Subject();
        this._sortObservable = new Subject();
        this._sortHeadersObservable = new Subject();
        this._columns = new BehaviorSubject(columns.map(c => { if (c.isActive === undefined) {
            c.isActive = true;
        } return c; }));
        this._displayedColumns = this._columns.value.filter(c => c.isActive).map(c => c.id);
        if (options) {
            if (options.pageSizeOptions && options.pageSizeOptions.length < 1) {
                throw Error('Array of pageSizeOptions must contain at least one entry');
            }
            if (options.defaultSortParams) {
                options.defaultSortParams.map(s => {
                    if (!this._displayedColumns.includes(s)) {
                        throw Error(`Provided sort parameter "${s}" is not a column.`);
                    }
                });
            }
            this._sortParams = options.defaultSortParams || [];
            this._sortDirs = options.defaultSortDirs || [];
            if (this._sortParams.length !== this._sortDirs.length) {
                this._sortDirs = this._sortParams.map(() => 'asc');
            }
            this._totalElements = options.totalElements || 0;
            this._pageSizeOptions = options.pageSizeOptions || [10, 20, 50, 100];
            this._key = options.localStorageKey;
        }
        else {
            this._pageSizeOptions = [10, 20, 50, 100];
            this._sortParams = [];
            this._sortDirs = [];
        }
        this.init();
    }
    onSortEvent() {
        this._sortParams = this._dataSource.sort['actives'];
        this._sortDirs = this._dataSource.sort['directions'];
        this._clientSideSort();
        this._sortObservable.next();
        this.storeTableSettings();
    }
    onPaginationEvent($event) {
        const tmpPageSize = this.pageSize;
        this.pageSize = $event.pageSize;
        this.pageIndex = $event.pageIndex;
        if (tmpPageSize !== this.pageSize) {
            this._sizeObservable.next();
        }
        else if ($event.previousPageIndex && $event.previousPageIndex < $event.pageIndex) {
            this._nextObservable.next();
        }
        else if ($event.previousPageIndex && $event.previousPageIndex > $event.pageIndex) {
            this._previousObservable.next();
        }
    }
    updateSortHeaders() {
        // Dirty hack to display default sort column(s)
        const temp = Object.assign([], this._displayedColumns);
        this._sortHeadersObservable.next([]);
        this._sortHeadersObservable.next(temp);
        this._clientSideSort();
        this._sortObservable.next();
        this.storeTableSettings();
        1;
    }
    // this fixes an infine loop of rerendering
    subscribeSortHeaders() {
        this._sortHeadersObservable.pipe(delay(0), 
        // ignore when there is no update in the sort (params or dirs)
        filter(() => this._displayedSortDirs !== this.sortDirs && this._displayedSortParams !== this.sortParams), tap((column) => {
            // update the displayed sort when it is not the empty array
            if (column.length > 0) {
                this._displayedSortDirs = this.sortDirs;
                this._displayedSortParams = this.sortParams;
            }
        })).subscribe(columns => this._displayedColumns = columns);
    }
    init() {
        this.subscribeSortHeaders();
        if (this._key) {
            const settings = new Settings(this._key);
            settings.load();
            if (this._isLocalStorageSettingsValid(settings)) {
                this.columns = settings.columns;
                this._sortDirs = settings.sortDirs;
                this._sortParams = settings.sortParams;
            }
            else {
                console.warn("Stored tableSettings are invalid. Using default");
            }
        }
    }
    _clientSideSort() {
        this._dataSource.orderData();
    }
    _isLocalStorageSettingsValid(settings) {
        // check if number of columns matching
        if (settings.columns.length !== this._columns.value.length) {
            return false;
        }
        // check if columns are the same
        for (var column of settings.columns) {
            var match = this._columns.value.filter(c => c.id == column.id && c.name == column.name);
            if (match === undefined) {
                return false;
            }
        }
        return true;
    }
    storeTableSettings() {
        console.log("Store");
        if (this._key) {
            const settings = new Settings(this._key);
            settings.columns = this._columns.value;
            settings.sortParams = this._sortParams;
            settings.sortDirs = this._sortDirs;
            settings.save();
        }
    }
    set totalElements(totalElements) {
        this._totalElements = totalElements;
    }
    get totalElements() {
        return this._totalElements;
    }
    set displayedColumns(displayedColumns) {
        this._displayedColumns = displayedColumns;
        this._columns.next(this._columns.value.map(c => {
            if (this._displayedColumns.includes(c.id)) {
                c.isActive = true;
            }
            else
                c.isActive = false;
            return c;
        }));
    }
    get displayedColumns() {
        return this._displayedColumns;
    }
    set dataSource(dataSource) {
        this._dataSource = dataSource;
        if (this._sortParams.length > 0) {
            this._dataSource.sort.actives = this._sortParams;
            this._dataSource.sort.directions = this._sortDirs.map(v => v);
            this.updateSortHeaders();
        }
    }
    get dataSource() {
        return this._dataSource;
    }
    set data(data) {
        this._dataSource.data = data;
        this._clientSideSort();
    }
    set columns(v) {
        this._columns.next(v.map(c => { if (c.isActive === undefined) {
            c.isActive = true;
        } return c; }));
    }
    onColumnsChange() {
        return this._columns;
    }
    updateColumnNames(v) {
        const dict = {};
        v.forEach(c => dict[c.id] = c.name);
        this._columns.next(this._columns.value.map(c => { c.name = dict[c.id] || c.name; return c; }));
    }
    get nextObservable() {
        return this._nextObservable;
    }
    get previousObservable() {
        return this._previousObservable;
    }
    get sizeObservable() {
        return this._sizeObservable;
    }
    get sortObservable() {
        return this._sortObservable;
    }
    get sortParams() {
        return this._sortParams;
    }
    get sortDirs() {
        return this._sortDirs;
    }
    get columns() {
        return this._columns.value;
    }
    get pageSizeOptions() {
        return this._pageSizeOptions;
    }
    set sortParams(v) {
        this._sortParams = v;
        this._dataSource.sort.actives = this._sortParams;
    }
    set sortDirs(v) {
        this._sortDirs = v;
        this._dataSource.sort.directions = this._sortDirs.map(elem => elem);
    }
}

class MatMultiSortTableDataSource extends DataSource {
    constructor(sort, clientSideSorting = false) {
        super();
        this._data = new BehaviorSubject([]);
        this.sort = sort;
        this.clientSideSorting = clientSideSorting;
    }
    set data(data) {
        this._data.next(data);
    }
    get data() {
        return this._data.value;
    }
    connect() {
        return this._data;
    }
    disconnect() {
        this._data.complete();
    }
    orderData() {
        this._data.next(this.sortData(this._data.value, this.sort.actives, this.sort.directions));
    }
    sortData(data, actives, directions) {
        const _data = Object.assign(new Array(), data);
        if (this.clientSideSorting) {
            return _data.sort((i1, i2) => {
                return this._sortData(i1, i2, actives, directions);
            });
        }
        return _data;
    }
    _sortData(d1, d2, params, dirs) {
        // @ts-ignore -- need a typesafe way to express these accessor operations, ts-ignore could be a solution
        // if there's not a suitable solution offered by typescript
        if (d1[params[0]] > d2[params[0]]) {
            return dirs[0] === 'asc' ? 1 : -1;
            // @ts-ignore
        }
        else if (d1[params[0]] < d2[params[0]]) {
            return dirs[0] === 'asc' ? -1 : 1;
        }
        else {
            if (params.length > 1) {
                params = params.slice(1, params.length);
                dirs = dirs.slice(1, dirs.length);
                return this._sortData(d1, d2, params, dirs);
            }
            else {
                return 0;
            }
        }
    }
}

/*
 * Public API Surface of mat-multi-sort
 */

/**
 * Generated bundle index. Do not edit.
 */

export { MatMultiSort, MatMultiSortHeaderComponent, MatMultiSortModule, MatMultiSortTableDataSource, MatMultiSortTableSettingsComponent, TableData };
//# sourceMappingURL=ngx-mat-multi-sort.mjs.map
