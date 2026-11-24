import {describe, expect, it} from 'vitest';
import {WorkItemCardComponent} from './work-item-card.component';

describe('WorkItemCardComponent', () => {
  it('leaves out the pairs that have no value, and keeps a zero', () => {
    const component = Object.create(WorkItemCardComponent.prototype) as WorkItemCardComponent;
    const shown = component.shown([{label: 'Rev.', value: 1}, {label: 'Module', value: undefined}, {label: 'Version', value: ''}, {label: 'Zero', value: 0}]);
    expect(shown.map(p => p.label)).toEqual(['Rev.', 'Zero']);
  });

  it('keeps a pair that has no value but has a placeholder, to say that there is none', () => {
    const component = Object.create(WorkItemCardComponent.prototype) as WorkItemCardComponent;
    const pair = {label: 'Definition', value: undefined, placeholder: 'No definition'};
    expect(component.shown([pair])).toEqual([pair]);
    expect(component.hasValue(pair)).toBe(false);
  });
});
