import { Directive } from '@angular/core';
import { MatSort } from '@angular/material/sort';
import * as i0 from "@angular/core";
export class MatMultiSort extends MatSort {
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
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoibWF0LW11bHRpLXNvcnQuZGlyZWN0aXZlLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXMiOlsiLi4vLi4vLi4vLi4vcHJvamVjdHMvbWF0LW11bHRpLXNvcnQvc3JjL2xpYi9tYXQtbXVsdGktc29ydC5kaXJlY3RpdmUudHMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUEsT0FBTyxFQUFFLFNBQVMsRUFBcUIsTUFBTSxlQUFlLENBQUM7QUFDN0QsT0FBTyxFQUFFLE9BQU8sRUFBOEIsTUFBTSx3QkFBd0IsQ0FBQzs7QUFNN0UsTUFBTSxPQUFPLFlBQWEsU0FBUSxPQUFPO0lBSnpDOztRQU1FLFVBQUssR0FBRyxLQUF1QixDQUFDO1FBRWhDLFlBQU8sR0FBYSxFQUFFLENBQUM7UUFDdkIsZUFBVSxHQUFvQixFQUFFLENBQUM7S0FxQ2xDO0lBbkNDLFFBQVE7UUFDTixLQUFLLENBQUMsUUFBUSxFQUFFLENBQUM7SUFDbkIsQ0FBQztJQUVELElBQUksQ0FBQyxRQUFxQjtRQUN4QixJQUFJLENBQUMsbUJBQW1CLENBQUMsUUFBUSxDQUFDLENBQUM7UUFDbkMsS0FBSyxDQUFDLElBQUksQ0FBQyxRQUFRLENBQUMsQ0FBQztJQUN2QixDQUFDO0lBRUQsbUJBQW1CLENBQUMsUUFBcUI7UUFDdkMsTUFBTSxDQUFDLEdBQUcsSUFBSSxDQUFDLE9BQU8sQ0FBQyxTQUFTLENBQUMsUUFBUSxDQUFDLEVBQUUsQ0FBQyxRQUFRLEtBQUssUUFBUSxDQUFDLEVBQUUsQ0FBQyxDQUFDO1FBRXZFLElBQUksSUFBSSxDQUFDLFFBQVEsQ0FBQyxRQUFRLENBQUMsRUFBRTtZQUMzQixJQUFJLElBQUksQ0FBQyxlQUFlLENBQUMsUUFBUSxDQUFDLEtBQUssQ0FBQyxRQUFRLENBQUMsS0FBSyxDQUFDLENBQUMsQ0FBQyxRQUFRLENBQUMsS0FBSyxDQUFDLENBQUMsQ0FBQyxJQUFJLENBQUMsS0FBSyxDQUFDLEVBQUU7Z0JBQ3JGLElBQUksQ0FBQyxVQUFVLENBQUMsTUFBTSxDQUFDLENBQUMsRUFBRSxDQUFDLEVBQUUsSUFBSSxDQUFDLG9CQUFvQixDQUFDLFFBQVEsQ0FBQyxDQUFDLENBQUM7YUFDbkU7aUJBQU07Z0JBQ0wsSUFBSSxDQUFDLE9BQU8sQ0FBQyxNQUFNLENBQUMsQ0FBQyxFQUFFLENBQUMsQ0FBQyxDQUFDO2dCQUMxQixJQUFJLENBQUMsVUFBVSxDQUFDLE1BQU0sQ0FBQyxDQUFDLEVBQUUsQ0FBQyxDQUFDLENBQUM7YUFDOUI7U0FDRjthQUFNO1lBQ0wsSUFBSSxDQUFDLE9BQU8sQ0FBQyxJQUFJLENBQUMsUUFBUSxDQUFDLEVBQUUsQ0FBQyxDQUFDO1lBQy9CLElBQUksQ0FBQyxVQUFVLENBQUMsSUFBSSxDQUFDLFFBQVEsQ0FBQyxLQUFLLENBQUMsQ0FBQyxDQUFDLFFBQVEsQ0FBQyxLQUFLLENBQUMsQ0FBQyxDQUFDLElBQUksQ0FBQyxLQUFLLENBQUMsQ0FBQztTQUNwRTtJQUNILENBQUM7SUFFRCxRQUFRLENBQUMsUUFBcUI7UUFDNUIsTUFBTSxDQUFDLEdBQUcsSUFBSSxDQUFDLE9BQU8sQ0FBQyxTQUFTLENBQUMsUUFBUSxDQUFDLEVBQUUsQ0FBQyxRQUFRLEtBQUssUUFBUSxDQUFDLEVBQUUsQ0FBQyxDQUFDO1FBQ3ZFLE9BQU8sQ0FBQyxHQUFHLENBQUMsQ0FBQyxDQUFDO0lBQ2hCLENBQUM7SUFFRCxlQUFlLENBQUMsUUFBcUI7UUFDbkMsTUFBTSxDQUFDLEdBQUcsSUFBSSxDQUFDLE9BQU8sQ0FBQyxTQUFTLENBQUMsUUFBUSxDQUFDLEVBQUUsQ0FBQyxRQUFRLEtBQUssUUFBUSxDQUFDLEVBQUUsQ0FBQyxDQUFDO1FBQ3ZFLE9BQU8sSUFBSSxDQUFDLFVBQVUsQ0FBQyxDQUFDLENBQUMsSUFBSSxDQUFDLFFBQVEsQ0FBQyxLQUFLLENBQUMsQ0FBQyxDQUFDLFFBQVEsQ0FBQyxLQUFLLENBQUMsQ0FBQyxDQUFDLElBQUksQ0FBQyxLQUFLLENBQUMsQ0FBQztJQUM5RSxDQUFDO29OQXhDVSxZQUFZLFNBQVosWUFBWTtvRUFBWixZQUFZOztpRkFBWixZQUFZO2NBSnhCLFNBQVM7ZUFBQztnQkFDVCxRQUFRLEVBQUUsZ0JBQWdCO2dCQUMxQixRQUFRLEVBQUUsY0FBYzthQUN6QiIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCB7IERpcmVjdGl2ZSwgT25DaGFuZ2VzLCBPbkluaXQgfSBmcm9tICdAYW5ndWxhci9jb3JlJztcbmltcG9ydCB7IE1hdFNvcnQsIE1hdFNvcnRhYmxlLCBTb3J0RGlyZWN0aW9uIH0gZnJvbSAnQGFuZ3VsYXIvbWF0ZXJpYWwvc29ydCc7XG5cbkBEaXJlY3RpdmUoe1xuICBzZWxlY3RvcjogJ1ttYXRNdWx0aVNvcnRdJyxcbiAgZXhwb3J0QXM6ICdtYXRNdWx0aVNvcnQnXG59KVxuZXhwb3J0IGNsYXNzIE1hdE11bHRpU29ydCBleHRlbmRzIE1hdFNvcnQgaW1wbGVtZW50cyBPbkluaXQsIE9uQ2hhbmdlcyB7XG5cbiAgc3RhcnQgPSAnYXNjJyBhcyAnYXNjJyB8ICdkZXNjJztcblxuICBhY3RpdmVzOiBzdHJpbmdbXSA9IFtdO1xuICBkaXJlY3Rpb25zOiBTb3J0RGlyZWN0aW9uW10gPSBbXTtcblxuICBuZ09uSW5pdCgpOiB2b2lkIHtcbiAgICBzdXBlci5uZ09uSW5pdCgpO1xuICB9XG5cbiAgc29ydChzb3J0YWJsZTogTWF0U29ydGFibGUpOiB2b2lkIHtcbiAgICB0aGlzLnVwZGF0ZU11bHRpcGxlU29ydHMoc29ydGFibGUpO1xuICAgIHN1cGVyLnNvcnQoc29ydGFibGUpO1xuICB9XG5cbiAgdXBkYXRlTXVsdGlwbGVTb3J0cyhzb3J0YWJsZTogTWF0U29ydGFibGUpOiB2b2lkIHtcbiAgICBjb25zdCBpID0gdGhpcy5hY3RpdmVzLmZpbmRJbmRleChhY3RpdmVJZCA9PiBhY3RpdmVJZCA9PT0gc29ydGFibGUuaWQpO1xuXG4gICAgaWYgKHRoaXMuaXNBY3RpdmUoc29ydGFibGUpKSB7XG4gICAgICBpZiAodGhpcy5hY3RpdmVEaXJlY3Rpb24oc29ydGFibGUpID09PSAoc29ydGFibGUuc3RhcnQgPyBzb3J0YWJsZS5zdGFydCA6IHRoaXMuc3RhcnQpKSB7XG4gICAgICAgIHRoaXMuZGlyZWN0aW9ucy5zcGxpY2UoaSwgMSwgdGhpcy5nZXROZXh0U29ydERpcmVjdGlvbihzb3J0YWJsZSkpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgdGhpcy5hY3RpdmVzLnNwbGljZShpLCAxKTtcbiAgICAgICAgdGhpcy5kaXJlY3Rpb25zLnNwbGljZShpLCAxKTtcbiAgICAgIH1cbiAgICB9IGVsc2Uge1xuICAgICAgdGhpcy5hY3RpdmVzLnB1c2goc29ydGFibGUuaWQpO1xuICAgICAgdGhpcy5kaXJlY3Rpb25zLnB1c2goc29ydGFibGUuc3RhcnQgPyBzb3J0YWJsZS5zdGFydCA6IHRoaXMuc3RhcnQpO1xuICAgIH1cbiAgfVxuXG4gIGlzQWN0aXZlKHNvcnRhYmxlOiBNYXRTb3J0YWJsZSkge1xuICAgIGNvbnN0IGkgPSB0aGlzLmFjdGl2ZXMuZmluZEluZGV4KGFjdGl2ZUlkID0+IGFjdGl2ZUlkID09PSBzb3J0YWJsZS5pZCk7XG4gICAgcmV0dXJuIGkgPiAtMTtcbiAgfVxuXG4gIGFjdGl2ZURpcmVjdGlvbihzb3J0YWJsZTogTWF0U29ydGFibGUpOiAnYXNjJyB8ICdkZXNjJyB7XG4gICAgY29uc3QgaSA9IHRoaXMuYWN0aXZlcy5maW5kSW5kZXgoYWN0aXZlSWQgPT4gYWN0aXZlSWQgPT09IHNvcnRhYmxlLmlkKTtcbiAgICByZXR1cm4gdGhpcy5kaXJlY3Rpb25zW2ldIHx8IChzb3J0YWJsZS5zdGFydCA/IHNvcnRhYmxlLnN0YXJ0IDogdGhpcy5zdGFydCk7XG4gIH1cblxufVxuIl19