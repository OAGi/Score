import {of, throwError} from 'rxjs';
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
  describe('opening an assignment that cannot be loaded', () => {
    function open(query: any, result: any) {
      const component = Object.create(AssignedBusinessTermDetailComponent.prototype) as any;
      component.route = {snapshot: {queryParams: query}};
      component.service = {getAssignedBusinessTerm: vi.fn(() => result)};
      component.titleService = {setTitle: vi.fn()};
      component.snackBar = {open: vi.fn()};
      component.router = {navigateByUrl: vi.fn()};
      component.ngOnInit();
      return component;
    }

    const list = '/business_term_management/assign_business_term';

    it('goes back to the list when the link has no type and id', () => {
      const component = open({}, of(undefined));
      expect(component.service.getAssignedBusinessTerm).not.toHaveBeenCalled();
      expect(component.snackBar.open).toHaveBeenCalledWith(
        'The requested business term assignment is unavailable.', '', {duration: 3000});
      expect(component.router.navigateByUrl).toHaveBeenCalledWith(list);
    });

    it.each([404, 400])('says the assignment is unavailable for %i', status => {
      const component = open({type: 'ASBIE', id: '5'}, throwError(() => ({status})));
      expect(component.snackBar.open).toHaveBeenCalledWith(
        'The requested business term assignment is unavailable.', '', {duration: 3000});
      expect(component.router.navigateByUrl).toHaveBeenCalledWith(list);
    });

    it('says there is no access permission for 403', () => {
      const component = open({type: 'ASBIE', id: '5'}, throwError(() => ({status: 403})));
      expect(component.snackBar.open).toHaveBeenCalledWith('You do not have access permission.', '', {duration: 3000});
      expect(component.router.navigateByUrl).toHaveBeenCalledWith(list);
    });
  });
});
