import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {BusinessContextValueDialogComponent} from './business-context-value-dialog.component';

describe('BusinessContextValueDialogComponent', () => {
  it('should be defined', () => {
    expect(BusinessContextValueDialogComponent).toBeTruthy();
  });

  it('filters Context Scheme Values when a value has no Meaning', () => {
    const component: any = Object.create(BusinessContextValueDialogComponent.prototype);
    component.businessContextValue = {contextSchemeId: 's1'};
    component.contextSchemeValueList = [
      {contextSchemeId: 's1', value: 'A', meaning: null},
      {contextSchemeId: 's1', value: 'B', meaning: 'Beta'},
      {contextSchemeId: 's2', value: 'C', meaning: 'Gamma'}
    ];
    component.contextSchemeValueListFilterCtrl = new FormControl();
    component.filteredContextSchemeValueList = new ReplaySubject(1);
    let shown: any[] = [];
    component.filteredContextSchemeValueList.subscribe((list: any[]) => shown = list);

    component.resetCtxSchemeValues();
    expect(shown.map(e => e.value)).toEqual(['A', 'B']);

    component.contextSchemeValueListFilterCtrl.setValue('zzz');
    expect(() => component.filterCtxSchemeValues()).not.toThrow();
    expect(shown).toEqual([]);

    component.contextSchemeValueListFilterCtrl.setValue('bet');
    component.filterCtxSchemeValues();
    expect(shown.map(e => e.value)).toEqual(['B']);
  });
});
