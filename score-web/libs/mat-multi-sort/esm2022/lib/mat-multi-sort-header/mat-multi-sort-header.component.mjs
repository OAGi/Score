import { Component, Input, Optional, Inject, HostListener, HostBinding, ViewEncapsulation, ChangeDetectionStrategy } from '@angular/core';
import { matSortAnimations, MatSortHeader } from '@angular/material/sort';
import * as i0 from "@angular/core";
import * as i1 from "@angular/material/sort";
import * as i2 from "../mat-multi-sort.directive";
import * as i3 from "@angular/cdk/a11y";
import * as i4 from "@angular/common";
const _c0 = ["mat-multi-sort-header", ""];
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
const _c1 = ["*"];
export class MatMultiSortHeaderComponent extends MatSortHeader {
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
    static { this.ɵfac = function MatMultiSortHeaderComponent_Factory(t) { return new (t || MatMultiSortHeaderComponent)(i0.ɵɵdirectiveInject(i1.MatSortHeaderIntl), i0.ɵɵdirectiveInject(i0.ChangeDetectorRef), i0.ɵɵdirectiveInject(i2.MatMultiSort, 8), i0.ɵɵdirectiveInject('C2_SORT_HEADER_COLUMN_DEF', 8), i0.ɵɵdirectiveInject(i3.FocusMonitor), i0.ɵɵdirectiveInject(i0.ElementRef)); }; }
    static { this.ɵcmp = /*@__PURE__*/ i0.ɵɵdefineComponent({ type: MatMultiSortHeaderComponent, selectors: [["", "mat-multi-sort-header", ""]], hostVars: 1, hostBindings: function MatMultiSortHeaderComponent_HostBindings(rf, ctx) { if (rf & 1) {
            i0.ɵɵlistener("mouseenter", function MatMultiSortHeaderComponent_mouseenter_HostBindingHandler() { return ctx.__setIndicatorHintVisible(true); })("longpress", function MatMultiSortHeaderComponent_longpress_HostBindingHandler() { return ctx.__setIndicatorHintVisible(true); })("mouseleave", function MatMultiSortHeaderComponent_mouseleave_HostBindingHandler() { return ctx.__setIndicatorHintVisible(false); });
        } if (rf & 2) {
            i0.ɵɵattribute("aria-sort", ctx._getAriaSortAttribute);
        } }, inputs: { id: ["mat-multi-sort-header", "id"] }, exportAs: ["matMultiSortHeader"], features: [i0.ɵɵInheritDefinitionFeature], attrs: _c0, ngContentSelectors: _c1, decls: 5, vars: 6, consts: [[1, "mat-sort-header-container"], [1, "mat-sort-header-content"], ["class", "mat-sort-header-arrow", 4, "ngIf"], [4, "ngIf"], [1, "mat-sort-header-arrow"], [1, "mat-sort-header-stem"], [1, "mat-sort-header-indicator"], [1, "mat-sort-header-pointer-left"], [1, "mat-sort-header-pointer-right"], [1, "mat-sort-header-pointer-middle"]], template: function MatMultiSortHeaderComponent_Template(rf, ctx) { if (rf & 1) {
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
        } }, dependencies: [i4.NgIf], styles: [".mat-sort-header-container{display:flex;cursor:pointer;align-items:center}.mat-sort-header-disabled .mat-sort-header-container{cursor:default}.mat-sort-header-position-before{flex-direction:row-reverse}.mat-sort-header-content{text-align:center;display:flex;align-items:center}.mat-sort-header-arrow{height:12px;width:12px;min-width:12px;position:relative;display:flex;opacity:0}.mat-sort-header-arrow,[dir=rtl] .mat-sort-header-position-before .mat-sort-header-arrow{margin:0 0 0 6px}.mat-sort-header-position-before .mat-sort-header-arrow,[dir=rtl] .mat-sort-header-arrow{margin:0 6px 0 0}.mat-sort-header-stem{background:currentColor;height:10px;width:2px;margin:auto;display:flex;align-items:center}.mat-sort-header-indicator{width:100%;height:2px;display:flex;align-items:center;position:absolute;top:0;left:0}.mat-sort-header-pointer-middle{margin:auto;height:2px;width:2px;background:currentColor;transform:rotate(45deg)}.mat-sort-header-pointer-left,.mat-sort-header-pointer-right{background:currentColor;width:6px;height:2px;position:absolute;top:0}.mat-sort-header-pointer-left{transform-origin:right;left:0}.mat-sort-header-pointer-right{transform-origin:left;right:0}\n"], encapsulation: 2, data: { animation: [
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
    }], () => [{ type: i1.MatSortHeaderIntl }, { type: i0.ChangeDetectorRef }, { type: i2.MatMultiSort, decorators: [{
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
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoibWF0LW11bHRpLXNvcnQtaGVhZGVyLmNvbXBvbmVudC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzIjpbIi4uLy4uLy4uLy4uLy4uL3Byb2plY3RzL21hdC1tdWx0aS1zb3J0L3NyYy9saWIvbWF0LW11bHRpLXNvcnQtaGVhZGVyL21hdC1tdWx0aS1zb3J0LWhlYWRlci5jb21wb25lbnQudHMiLCIuLi8uLi8uLi8uLi8uLi9wcm9qZWN0cy9tYXQtbXVsdGktc29ydC9zcmMvbGliL21hdC1tdWx0aS1zb3J0LWhlYWRlci9tYXQtbXVsdGktc29ydC1oZWFkZXIuY29tcG9uZW50Lmh0bWwiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUEsT0FBTyxFQUNMLFNBQVMsRUFDVCxLQUFLLEVBRUwsUUFBUSxFQUNSLE1BQU0sRUFDTixZQUFZLEVBQ1osV0FBVyxFQUNYLGlCQUFpQixFQUNqQix1QkFBdUIsRUFFeEIsTUFBTSxlQUFlLENBQUM7QUFDdkIsT0FBTyxFQUFFLGlCQUFpQixFQUFFLGFBQWEsRUFBcUIsTUFBTSx3QkFBd0IsQ0FBQzs7Ozs7Ozs7O0lDSDNGLDhCQU1nRTtJQUQzRCwrUEFBcUQsSUFBSSxLQUFDLGdQQUNOLEtBQUssS0FEQztJQUU3RCx5QkFBd0M7SUFDeEMsOEJBQWdGO0lBQzlFLHlCQUEyRixhQUFBLGFBQUE7SUFHN0YsaUJBQU0sRUFBQTs7O0lBVkgsMkRBQXNDLCtDQUFBLG9EQUFBO0lBTUYsZUFBd0M7SUFBeEMsNkRBQXdDO0lBQ25DLGVBQTBDO0lBQTFDLCtEQUEwQztJQUN6QyxlQUEyQztJQUEzQyxnRUFBMkM7OztJQUkxRiwyQkFBeUI7SUFBQSxZQUFhO0lBQUEsaUJBQU07OztJQUFuQixlQUFhO0lBQWIsc0NBQWE7OztBRGN4QyxNQUFNLE9BQU8sMkJBQTRCLFNBQVEsYUFBYTtJQUk1RCxZQUFtQixLQUF3QixFQUN6QyxpQkFBb0MsRUFDakIsS0FBbUIsRUFDa0IsVUFBb0MsRUFDNUYsYUFBMkIsRUFDM0IsV0FBb0M7UUFDcEMs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LGlCQUdmLGlCQUFpQixDQUFDLElBQUksbUJBQ3BCLHVCQUF1QixDQUFDLE1BQU0sY0FDbkM7b0JBQ1YsaUJBQWlCLENBQUMsU0FBUztvQkFDM0IsaUJBQWlCLENBQUMsV0FBVztvQkFDN0IsaUJBQWlCLENBQUMsWUFBWTtvQkFDOUIsaUJBQWlCLENBQUMsWUFBWTtvQkFDOUIsaUJBQWlCLENBQUMsYUFBYTtvQkFDL0IsaUJBQWlCLENBQUMsYUFBYTtpQkFDaEM7O3NCQVFFLFFBQVE7O3NCQUNSLE1BQU07dUJBQUMsMkJBQTJCOztzQkFBRyxRQUFRO3lFQUxoQixFQUFFO2tCQUFqQyxLQUFLO21CQUFDLHVCQUF1QjtZQWM5Qix5QkFBeUI7a0JBSHhCLFlBQVk7bUJBQUMsWUFBWSxFQUFFLENBQUMsTUFBTSxDQUFDOztrQkFDbkMsWUFBWTttQkFBQyxXQUFXLEVBQUUsQ0FBQyxNQUFNLENBQUM7O2tCQUNsQyxZQUFZO21CQUFDLFlBQVksRUFBRSxDQUFDLE9BQU8sQ0FBQztZQXVCckMscUJBQXFCO2tCQURwQixXQUFXO21CQUFDLGdCQUFnQjs7a0ZBckNsQiwyQkFBMkIiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQge1xuICBDb21wb25lbnQsXG4gIElucHV0LFxuICBDaGFuZ2VEZXRlY3RvclJlZixcbiAgT3B0aW9uYWwsXG4gIEluamVjdCxcbiAgSG9zdExpc3RlbmVyLFxuICBIb3N0QmluZGluZyxcbiAgVmlld0VuY2Fwc3VsYXRpb24sXG4gIENoYW5nZURldGVjdGlvblN0cmF0ZWd5LFxuICBFbGVtZW50UmVmXG59IGZyb20gJ0Bhbmd1bGFyL2NvcmUnO1xuaW1wb3J0IHsgbWF0U29ydEFuaW1hdGlvbnMsIE1hdFNvcnRIZWFkZXIsIE1hdFNvcnRIZWFkZXJJbnRsIH0gZnJvbSAnQGFuZ3VsYXIvbWF0ZXJpYWwvc29ydCc7XG5pbXBvcnQgeyBNYXRNdWx0aVNvcnQgfSBmcm9tICcuLi9tYXQtbXVsdGktc29ydC5kaXJlY3RpdmUnO1xuaW1wb3J0IHsgRm9jdXNNb25pdG9yIH0gZnJvbSAnQGFuZ3VsYXIvY2RrL2ExMXknO1xuXG4vKiogQ29sdW1uIGRlZmluaXRpb24gYXNzb2NpYXRlZCB3aXRoIGEgYE1hdFNvcnRIZWFkZXJgLiAqL1xuaW50ZXJmYWNlIEMyTWF0U29ydEhlYWRlckNvbHVtbkRlZiB7XG4gIG5hbWU6IHN0cmluZztcbn1cblxuQENvbXBvbmVudCh7XG4gIHNlbGVjdG9yOiAnW21hdC1tdWx0aS1zb3J0LWhlYWRlcl0nLFxuICBleHBvcnRBczogJ21hdE11bHRpU29ydEhlYWRlcicsXG4gIHRlbXBsYXRlVXJsOiAnLi9tYXQtbXVsdGktc29ydC1oZWFkZXIuY29tcG9uZW50Lmh0bWwnLFxuICBzdHlsZVVybHM6IFsnLi9tYXQtbXVsdGktc29ydC1oZWFkZXIuY29tcG9uZW50LnNjc3MnXSxcbiAgZW5jYXBzdWxhdGlvbjogVmlld0VuY2Fwc3VsYXRpb24uTm9uZSxcbiAgY2hhbmdlRGV0ZWN0aW9uOiBDaGFuZ2VEZXRlY3Rpb25TdHJhdGVneS5PblB1c2gsXG4gIGFuaW1hdGlvbnM6IFtcbiAgICBtYXRTb3J0QW5pbWF0aW9ucy5pbmRpY2F0b3IsXG4gICAgbWF0U29ydEFuaW1hdGlvbnMubGVmdFBvaW50ZXIsXG4gICAgbWF0U29ydEFuaW1hdGlvbnMucmlnaHRQb2ludGVyLFxuICAgIG1hdFNvcnRBbmltYXRpb25zLmFycm93T3BhY2l0eSxcbiAgICBtYXRTb3J0QW5pbWF0aW9ucy5hcnJvd1Bvc2l0aW9uLFxuICAgIG1hdFNvcnRBbmltYXRpb25zLmFsbG93Q2hpbGRyZW5cbiAgXVxufSlcbmV4cG9ydCBjbGFzcyBNYXRNdWx0aVNvcnRIZWFkZXJDb21wb25lbnQgZXh0ZW5kcyBNYXRTb3J0SGVhZGVyIHtcbiAgc3RhcnQgPSAnYXNjJyBhcyAnYXNjJyB8ICdkZXNjJztcbiAgQElucHV0KCdtYXQtbXVsdGktc29ydC1oZWFkZXInKSBpZCE6IHN0cmluZztcblxuICBjb25zdHJ1Y3RvcihwdWJsaWMgX2ludGw6IE1hdFNvcnRIZWFkZXJJbnRsLFxuICAgIGNoYW5nZURldGVjdG9yUmVmOiBDaGFuZ2VEZXRlY3RvclJlZixcbiAgICBAT3B0aW9uYWwoKSBwdWJsaWMgX3NvcnQ6IE1hdE11bHRpU29ydCxcbiAgICBASW5qZWN0KCdDMl9TT1JUX0hFQURFUl9DT0xVTU5fREVGJykgQE9wdGlvbmFsKCkgcHVibGljIF9jb2x1bW5EZWY6IEMyTWF0U29ydEhlYWRlckNvbHVtbkRlZixcbiAgICBfZm9jdXNNb25pdG9yOiBGb2N1c01vbml0b3IsXG4gICAgX2VsZW1lbnRSZWY6IEVsZW1lbnRSZWY8SFRNTEVsZW1lbnQ+KSB7XG4gICAgc3VwZXIoX2ludGwsIGNoYW5nZURldGVjdG9yUmVmLCBfc29ydCwgX2NvbHVtbkRlZiwgX2ZvY3VzTW9uaXRvciwgX2VsZW1lbnRSZWYpO1xuICB9XG5cbiAgQEhvc3RMaXN0ZW5lcignbW91c2VlbnRlcicsIFsndHJ1ZSddKVxuICBASG9zdExpc3RlbmVyKCdsb25ncHJlc3MnLCBbJ3RydWUnXSlcbiAgQEhvc3RMaXN0ZW5lcignbW91c2VsZWF2ZScsIFsnZmFsc2UnXSlcbiAgX19zZXRJbmRpY2F0b3JIaW50VmlzaWJsZSh2aXNpYmxlOiBzdHJpbmcgfCBib29sZWFuKSB7XG4gICAgc3VwZXIuX3NldEluZGljYXRvckhpbnRWaXNpYmxlKHZpc2libGUgYXMgYm9vbGVhbik7XG4gIH1cblxuICBfaGFuZGxlQ2xpY2soKSB7XG4gICAgdGhpcy5fc29ydC5kaXJlY3Rpb24gPSB0aGlzLmdldFNvcnREaXJlY3Rpb24oKTtcbiAgICBzdXBlci5faGFuZGxlQ2xpY2soKTtcbiAgfVxuXG4gIF9pc1NvcnRlZCgpIHtcbiAgICByZXR1cm4gdGhpcy5fc29ydC5hY3RpdmVzLmZpbmRJbmRleChhY3RpdmVJZCA9PiBhY3RpdmVJZCA9PT0gdGhpcy5pZCkgPiAtMTtcbiAgfVxuXG4gIF9zb3J0SWQoKSB7XG4gICAgcmV0dXJuIHRoaXMuX3NvcnQuYWN0aXZlcy5maW5kSW5kZXgoYWN0aXZlSWQgPT4gYWN0aXZlSWQgPT09IHRoaXMuaWQpICsgMTtcbiAgfVxuXG4gIF91cGRhdGVBcnJvd0RpcmVjdGlvbigpIHtcbiAgICB0aGlzLl9hcnJvd0RpcmVjdGlvbiA9IHRoaXMuZ2V0U29ydERpcmVjdGlvbigpO1xuICB9XG5cbiAgQEhvc3RCaW5kaW5nKCdhdHRyLmFyaWEtc29ydCcpXG4gIF9nZXRBcmlhU29ydEF0dHJpYnV0ZSgpIHtcbiAgICBpZiAoIXRoaXMuX2lzU29ydGVkKCkpIHtcbiAgICAgIHJldHVybiAnbm9uZSc7XG4gICAgfVxuXG4gICAgcmV0dXJuIHRoaXMuZ2V0U29ydERpcmVjdGlvbigpID09PSAnYXNjJyA/ICdhc2NlbmRpbmcnIDogJ2Rlc2NlbmRpbmcnO1xuICB9XG5cbiAgX3JlbmRlckFycm93KCkge1xuICAgIHJldHVybiAhdGhpcy5faXNEaXNhYmxlZCgpIHx8IHRoaXMuX2lzU29ydGVkKCk7XG4gIH1cblxuICBnZXRTb3J0RGlyZWN0aW9uKCk6ICdhc2MnIHwgJ2Rlc2MnIHwgJycge1xuICAgIGNvbnN0IGkgPSB0aGlzLl9zb3J0LmFjdGl2ZXMuZmluZEluZGV4KGFjdGl2ZUlkcyA9PiBhY3RpdmVJZHMgPT09IHRoaXMuaWQpO1xuICAgIGNvbnN0IGRpcmVjdGlvbiA9IHRoaXMuX3NvcnQuZGlyZWN0aW9uc1tpXTtcbiAgICByZXR1cm4gdGhpcy5faXNTb3J0ZWQoKSA/IGRpcmVjdGlvbiA6ICh0aGlzLnN0YXJ0IHx8IHRoaXMuX3NvcnQuc3RhcnQpO1xuICB9XG5cbn1cbiIsIjxkaXYgY2xhc3M9XCJtYXQtc29ydC1oZWFkZXItY29udGFpbmVyXCJcbiAgICAgW2NsYXNzLm1hdC1zb3J0LWhlYWRlci1zb3J0ZWRdPVwiX2lzU29ydGVkKClcIlxuICAgICBbY2xhc3MubWF0LXNvcnQtaGVhZGVyLXBvc2l0aW9uLWJlZm9yZV09XCJhcnJvd1Bvc2l0aW9uID09ICdiZWZvcmUnXCI+XG5cbiAgPGRpdiBjbGFzcz1cIm1hdC1zb3J0LWhlYWRlci1jb250ZW50XCI+XG4gICAgPG5nLWNvbnRlbnQ+PC9uZy1jb250ZW50PlxuICA8L2Rpdj5cblxuICA8IS0tIERpc2FibGUgYW5pbWF0aW9ucyB3aGlsZSBhIGN1cnJlbnQgYW5pbWF0aW9uIGlzIHJ1bm5pbmcgLS0+XG4gIDxkaXYgY2xhc3M9XCJtYXQtc29ydC1oZWFkZXItYXJyb3dcIlxuICAgICAgICpuZ0lmPVwiX3JlbmRlckFycm93KClcIlxuICAgICAgIFtAYXJyb3dPcGFjaXR5XT1cIl9nZXRBcnJvd1ZpZXdTdGF0ZSgpXCJcbiAgICAgICBbQGFycm93UG9zaXRpb25dPVwiX2dldEFycm93Vmlld1N0YXRlKClcIlxuICAgICAgIFtAYWxsb3dDaGlsZHJlbl09XCJfZ2V0QXJyb3dEaXJlY3Rpb25TdGF0ZSgpXCJcbiAgICAgICAoQGFycm93UG9zaXRpb24uc3RhcnQpPVwiX2Rpc2FibGVWaWV3U3RhdGVBbmltYXRpb24gPSB0cnVlXCJcbiAgICAgICAoQGFycm93UG9zaXRpb24uZG9uZSk9XCJfZGlzYWJsZVZpZXdTdGF0ZUFuaW1hdGlvbiA9IGZhbHNlXCI+XG4gICAgPGRpdiBjbGFzcz1cIm1hdC1zb3J0LWhlYWRlci1zdGVtXCI+PC9kaXY+XG4gICAgPGRpdiBjbGFzcz1cIm1hdC1zb3J0LWhlYWRlci1pbmRpY2F0b3JcIiBbQGluZGljYXRvcl09XCJfZ2V0QXJyb3dEaXJlY3Rpb25TdGF0ZSgpXCI+XG4gICAgICA8ZGl2IGNsYXNzPVwibWF0LXNvcnQtaGVhZGVyLXBvaW50ZXItbGVmdFwiIFtAbGVmdFBvaW50ZXJdPVwiX2dldEFycm93RGlyZWN0aW9uU3RhdGUoKVwiPjwvZGl2PlxuICAgICAgPGRpdiBjbGFzcz1cIm1hdC1zb3J0LWhlYWRlci1wb2ludGVyLXJpZ2h0XCIgW0ByaWdodFBvaW50ZXJdPVwiX2dldEFycm93RGlyZWN0aW9uU3RhdGUoKVwiPjwvZGl2PlxuICAgICAgPGRpdiBjbGFzcz1cIm1hdC1zb3J0LWhlYWRlci1wb2ludGVyLW1pZGRsZVwiPjwvZGl2PlxuICAgIDwvZGl2PlxuICA8L2Rpdj5cbiAgPGRpdiAqbmdJZj1cIl9pc1NvcnRlZCgpXCI+e3tfc29ydElkKCl9fTwvZGl2PlxuPC9kaXY+XG4iXX0=