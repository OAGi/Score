import {of} from 'rxjs';
import {AssignedBusinessTermDetailComponent} from './assigned-business-term-detail.component';

describe('AssignedBusinessTermDetailComponent', () => {
  it('should be defined', () => {
    expect(AssignedBusinessTermDetailComponent).toBeTruthy();
  });

  describe('preferred business term overwrite warning', () => {
    function setup(primaryList: { assignedBizTermId: number }[]) {
      const component = Object.create(AssignedBusinessTermDetailComponent.prototype) as any;
      component.service = {findIfPrimaryExist: vi.fn(() => of({list: primaryList, length: primaryList.length}))};
      component.confirmDialogService = {
        newConfig: () => ({data: {}}),
        open: vi.fn(() => ({afterClosed: () => of(false)}))
      };
      component.doUpdate = vi.fn();
      return component;
    }

    const edited = {assignedBizTermId: 5, bieId: 1, bieType: 'ASBIE', primaryIndicator: true} as any;

    it('updates without a warning when the edited assignment is the only preferred one', () => {
      const component = setup([{assignedBizTermId: 5}]);
      component.checkIfPrimaryWillBeOverwrittenAndDoUpdate(edited);
      expect(component.confirmDialogService.open).not.toHaveBeenCalled();
      expect(component.doUpdate).toHaveBeenCalledWith(edited);
    });

    it('warns when another assignment of the node is preferred', () => {
      const component = setup([{assignedBizTermId: 5}, {assignedBizTermId: 6}]);
      component.checkIfPrimaryWillBeOverwrittenAndDoUpdate(edited);
      expect(component.confirmDialogService.open).toHaveBeenCalled();
      expect(component.doUpdate).not.toHaveBeenCalled();
    });
  });
});
