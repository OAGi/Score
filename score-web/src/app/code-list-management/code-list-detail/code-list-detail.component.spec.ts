import {CodeListDetailComponent} from './code-list-detail.component';
import {of, ReplaySubject} from 'rxjs';
import {AgencyIdListSummary, AgencyIdListValueSummary} from '../../agency-id-list-management/domain/agency-id-list';
import {CodeListDetails, CodeListValueDetails} from '../domain/code-list';

describe('CodeListDetailComponent', () => {
  it('should be defined', () => {
    expect(CodeListDetailComponent).toBeTruthy();
  });

  it('clears a selected Agency ID List Value when the list changes or is cleared', () => {
    const component = Object.create(CodeListDetailComponent.prototype) as CodeListDetailComponent;
    const listValue = new AgencyIdListValueSummary();
    listValue.agencyIdListValueManifestId = 11;
    listValue.agencyIdListManifestId = 1;
    const otherListValue = new AgencyIdListValueSummary();
    otherListValue.agencyIdListValueManifestId = 12;
    otherListValue.agencyIdListManifestId = 2;
    component.allAgencyIdListValues = [listValue, otherListValue];
    component.filteredAgencyListValues = new ReplaySubject<AgencyIdListValueSummary[]>(1);
    component.codeList = new CodeListDetails();
    component.codeList.agencyIdListValue.agencyIdListValueManifestId = 11;
    component.agencyIdList = new AgencyIdListSummary();
    component.agencyIdList.agencyIdListManifestId = 1;

    component.onAgencyIdListChange();
    expect(component.codeList.agencyIdListValue.agencyIdListValueManifestId).toBe(11);
    expect(component.currentAgencyIdListValues.map(value => value.agencyIdListValueManifestId)).toEqual([11]);
    let filteredValues: AgencyIdListValueSummary[] = [];
    component.filteredAgencyListValues.subscribe(values => filteredValues = values);
    expect(filteredValues.map(value => value.agencyIdListValueManifestId)).toEqual([11]);

    component.agencyIdList = new AgencyIdListSummary();
    component.agencyIdList.agencyIdListManifestId = 2;
    component.onAgencyIdListChange();
    expect(component.codeList.agencyIdListValue.agencyIdListValueManifestId).toBeUndefined();
    expect(component.currentAgencyIdListValues.map(value => value.agencyIdListValueManifestId)).toEqual([12]);

    component.codeList.agencyIdListValue.agencyIdListValueManifestId = 11;
    component.agencyIdList = undefined;
    component.onAgencyIdListChange();
    expect(component.codeList.agencyIdListValue.agencyIdListValueManifestId).toBeUndefined();
  });

  it('allows an empty Agency ID List pair but requires a value from the selected list', () => {
    const component = Object.create(CodeListDetailComponent.prototype) as CodeListDetailComponent;
    component.isUpdating = false;
    component.allAgencyIdListValues = [];
    const codeList = new CodeListDetails();
    codeList.name = 'Custom list';
    codeList.listId = 'custom-list';
    codeList.versionId = '1';

    component.agencyIdList = undefined;
    expect(component.isDisabled(codeList)).toBe(false);

    component.agencyIdList = new AgencyIdListSummary();
    component.agencyIdList.agencyIdListManifestId = 7;
    expect(component.isDisabled(codeList)).toBe(true);

    const value = new AgencyIdListValueSummary();
    value.agencyIdListValueManifestId = 11;
    value.agencyIdListManifestId = 7;
    component.allAgencyIdListValues = [value];
    codeList.agencyIdListValue.agencyIdListValueManifestId = 11;
    expect(component.isDisabled(codeList)).toBe(false);
  });

  it('blocks Code List update and state changes when Namespace is missing', () => {
    const component = Object.create(CodeListDetailComponent.prototype) as CodeListDetailComponent;
    const open = vi.fn();
    (component as any).snackBar = {open};
    component.isUpdating = false;
    component.hashCode = 'dirty';
    component.codeList = new CodeListDetails();
    component.codeList.state = 'WIP';
    component.codeList.access = 'CanEdit';
    component.codeList.name = 'Custom list';
    component.codeList.listId = 'custom-list';
    component.codeList.versionId = '1';
    component.codeList.definition.content = 'definition';

    component.update();
    expect(open).toHaveBeenLastCalledWith('Namespace is required', '', {duration: 3000});

    open.mockClear();
    component.codeList.state = 'QA';
    component.updateState('WIP');
    expect(open).toHaveBeenLastCalledWith('Namespace is required', '', {duration: 3000});
  });

  describe('the Code List value dialog', () => {
    function open(existing: string, typed: string, valueToEdit?: CodeListValueDetails) {
      const component = Object.create(CodeListDetailComponent.prototype) as CodeListDetailComponent;
      const row = new CodeListValueDetails();
      row.guid = 'g1';
      row.value = existing;
      component.values = [row];
      component.codeList = new CodeListDetails();
      component.codeList.valueList = component.values;
      const result = new CodeListValueDetails();
      result.guid = valueToEdit ? valueToEdit.guid : undefined as any;
      result.value = typed;
      const snackBar = {open: vi.fn()};
      (component as any).snackBar = snackBar;
      (component as any).dialog = {open: () => ({afterClosed: () => of(result)})};
      (component as any).view = () => undefined;
      component.isEditable = () => true;
      component.openDialog(valueToEdit);
      return {component, snackBar};
    }

    it('rejects an added Code that differs from an existing one only by surrounding spaces', () => {
      const {component, snackBar} = open('EA', 'EA ');
      expect(snackBar.open).toHaveBeenCalled();
      expect(component.values.length).toBe(1);
    });

    it('adds the Code without its surrounding spaces', () => {
      const {component, snackBar} = open('EA', ' KG ');
      expect(snackBar.open).not.toHaveBeenCalled();
      expect(component.values.map(v => v.value)).toEqual(['EA', 'KG']);
    });

    it('rejects an edited Code that becomes a duplicate of another value after trimming', () => {
      const other = new CodeListValueDetails();
      other.guid = 'g2';
      other.value = 'KG';
      const {component, snackBar} = open('EA', 'EA ', other);
      expect(snackBar.open).toHaveBeenCalled();
      expect(component.values.length).toBe(1);
    });
  });
});
