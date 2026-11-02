import {of} from 'rxjs';
import {AccDetailComponent} from './acc-detail.component';

/** Changing the Entity Type of a BCC compares the entity type string, and no longer throws. */
describe('AccDetailComponent.onChangeEntityType', () => {
  const run = (bcc: Record<string, unknown>, confirmResult = true) => {
    const confirmDialogService = {
      newConfig: () => ({data: {} as Record<string, unknown>}),
      open: vi.fn(() => ({afterClosed: () => of(confirmResult)})),
    };
    const self = {entityTypeChanged: new Map<number, boolean>(), confirmDialogService};
    const nodeDetail = {bcc: {manifestId: 1, ...bcc}};
    AccDetailComponent.prototype.onChangeEntityType.call(self, nodeDetail as never);
    return {bcc: nodeDetail.bcc as Record<string, unknown>, confirmDialogService};
  };

  it('asks before clearing the value constraints when switching to Element', () => {
    const {bcc, confirmDialogService} = run({entityType: 'Element', fixedValue: 'ABC', fixedOrDefault: 'fixed'});
    expect(confirmDialogService.open).toHaveBeenCalled();
    expect(bcc['fixedValue']).toBe('');
    expect(bcc['fixedOrDefault']).toBe('none');
  });

  it('clears the value constraints at once when there are none to lose', () => {
    const {bcc, confirmDialogService} = run({entityType: 'Element', fixedValue: '', defaultValue: ''});
    expect(confirmDialogService.open).not.toHaveBeenCalled();
    expect(bcc['fixedOrDefault']).toBe('none');
  });

  it('limits the cardinality to 0..1 when switching to Attribute', () => {
    const {bcc} = run({entityType: 'Attribute', cardinalityMin: 0, cardinalityMax: 5});
    expect(bcc['cardinalityMax']).toBe(1);
  });
});
