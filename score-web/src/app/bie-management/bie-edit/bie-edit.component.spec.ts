import {vi} from 'vitest';
import {of, Subject} from 'rxjs';
import {BieEditComponent} from './bie-edit.component';
import {BusinessContext} from '../../context-management/business-context/domain/business-context';

describe('BieEditComponent', () => {
  it('should be defined', () => {
    expect(BieEditComponent).toBeTruthy();
  });
});

/**
 * Issue #1610 (parity with the OpenAPI Document editor's #1610 banner): the BIE-root "OpenAPI Document
 * Information" panel warns, per binding card, that a DELETE Request body is ignored when that binding's
 * owning OpenAPI Document targets OpenAPI 3.0.x. The check is pure, so bind the prototype method to a
 * minimal `this` and a plain binding bag (no Angular wiring required). The warning is read-only on the
 * BIE screen — the OpenAPI Version itself is changed on the OpenAPI Document screen.
 */
function deleteBodyIgnored(verb: string, messageBody: string, openAPIVersion: any): boolean {
  const ctx: any = {isOasBindingDeleteBodyIgnored: BieEditComponent.prototype.isOasBindingDeleteBodyIgnored};
  return ctx.isOasBindingDeleteBodyIgnored({verb, messageBody, openAPIVersion});
}

describe('BieEditComponent.isOasBindingDeleteBodyIgnored (#1610 per-card warning)', () => {
  it('is true for a DELETE + Request binding whose document targets 3.0.3', () => {
    expect(deleteBodyIgnored('DELETE', 'Request', '3.0.3')).toBe(true);
  });

  it('is false for the SAME binding when the document targets 3.1.1 (the body is honored)', () => {
    expect(deleteBodyIgnored('DELETE', 'Request', '3.1.1')).toBe(false);
  });

  it('treats any 3.1.x version (e.g. 3.1.0, padded) as honored', () => {
    expect(deleteBodyIgnored('DELETE', 'Request', '3.1.0')).toBe(false);
    expect(deleteBodyIgnored('DELETE', 'Request', '  3.1.1 ')).toBe(false);
  });

  it('is false for a DELETE + Response binding on 3.0.3 (only a Request body is dropped)', () => {
    expect(deleteBodyIgnored('DELETE', 'Response', '3.0.3')).toBe(false);
  });

  it('is false for a non-DELETE Request binding on 3.0.3 (e.g. POST)', () => {
    expect(deleteBodyIgnored('POST', 'Request', '3.0.3')).toBe(false);
  });

  it('warns when the version is missing/blank (not a 3.1 prefix)', () => {
    expect(deleteBodyIgnored('DELETE', 'Request', '')).toBe(true);
    expect(deleteBodyIgnored('DELETE', 'Request', undefined)).toBe(true);
  });
});

function businessContext(businessContextId: number, name: string): BusinessContext {
  return {businessContextId, name, guid: 'bc-' + businessContextId} as BusinessContext;
}

describe('BieEditComponent business context assignment', () => {
  function businessContextCtx(overrides: any = {}): any {
    const ctx: any = {
      topLevelAsbiepId: 1001,
      businessContexts: [businessContext(1, 'Shared Context')],
      allBusinessContexts: [
        businessContext(1, 'Shared Context'),
        businessContext(2, 'Shared Context'),
        businessContext(3, 'Trading Partner')
      ],
      businessContextUpdating: false,
      businessContextCtrl: {setValue: vi.fn()},
      businessContextInput: {nativeElement: {value: 'Shared Context'}},
      bizCtxService: {
        assign: vi.fn(),
        unassign: vi.fn()
      },
      snackBar: {open: vi.fn()},
      ...overrides
    };
    ctx._filter = BieEditComponent.prototype._filter;
    ctx.addBusinessContext = BieEditComponent.prototype.addBusinessContext;
    ctx.removeBusinessContext = BieEditComponent.prototype.removeBusinessContext;
    return ctx;
  }

  it('filters already assigned business contexts by id, not by name', () => {
    const ctx = businessContextCtx();

    expect(ctx._filter().map((e: BusinessContext) => e.businessContextId)).toEqual([2, 3]);
    expect(ctx._filter('Shared').map((e: BusinessContext) => e.businessContextId)).toEqual([2]);
  });

  it('sets the updating flag while an assignment request is in flight', () => {
    const selected = businessContext(2, 'Shared Context');
    const assignment$ = new Subject<void>();
    const ctx = businessContextCtx();
    ctx.bizCtxService.assign.mockReturnValue(assignment$);

    ctx.addBusinessContext({option: {value: selected}});

    expect(ctx.bizCtxService.assign).toHaveBeenCalledWith(1001, selected);
    expect(ctx.businessContextUpdating).toBe(true);
    expect(ctx.businessContexts.map((e: BusinessContext) => e.businessContextId)).toEqual([1]);

    assignment$.next();

    expect(ctx.businessContexts.map((e: BusinessContext) => e.businessContextId)).toEqual([1, 2]);
    expect(ctx.businessContextUpdating).toBe(false);
    expect(ctx.businessContextInput.nativeElement.value).toBe('');
    expect(ctx.businessContextCtrl.setValue).toHaveBeenCalledWith(null);
    expect(ctx.snackBar.open).toHaveBeenCalledWith('Updated', '', {duration: 3000});
  });

  it('ignores duplicate assignment requests for the same business context id', () => {
    const ctx = businessContextCtx();

    ctx.addBusinessContext({option: {value: businessContext(1, 'Shared Context')}});

    expect(ctx.bizCtxService.assign).not.toHaveBeenCalled();
    expect(ctx.businessContexts.map((e: BusinessContext) => e.businessContextId)).toEqual([1]);
  });

  it('does not unassign the last remaining business context', () => {
    const ctx = businessContextCtx();

    ctx.removeBusinessContext(ctx.businessContexts[0]);

    expect(ctx.bizCtxService.unassign).not.toHaveBeenCalled();
    expect(ctx.businessContexts.map((e: BusinessContext) => e.businessContextId)).toEqual([1]);
    expect(ctx.businessContextUpdating).toBe(false);
  });

  it('unassigns a non-last business context and removes it from the chip list on success', () => {
    const unassign$ = new Subject<void>();
    const ctx = businessContextCtx({
      businessContexts: [businessContext(1, 'Shared Context'), businessContext(2, 'Trading Partner')]
    });
    ctx.bizCtxService.unassign.mockReturnValue(unassign$);

    ctx.removeBusinessContext(businessContext(1, 'Shared Context'));

    expect(ctx.bizCtxService.unassign).toHaveBeenCalledWith(1001,
      expect.objectContaining({businessContextId: 1}));
    expect(ctx.businessContextUpdating).toBe(true);

    unassign$.next();

    expect(ctx.businessContexts.map((e: BusinessContext) => e.businessContextId)).toEqual([2]);
    expect(ctx.businessContextUpdating).toBe(false);
    expect(ctx.snackBar.open).toHaveBeenCalledWith('Updated', '', {duration: 3000});
  });

  it('resets the updating flag and does not append the context when the assign request errors', () => {
    const assignment$ = new Subject<void>();
    const ctx = businessContextCtx();
    ctx.bizCtxService.assign.mockReturnValue(assignment$);

    ctx.addBusinessContext({option: {value: businessContext(2, 'Shared Context')}});
    expect(ctx.businessContextUpdating).toBe(true);

    assignment$.error(new Error('assign failed'));

    expect(ctx.businessContextUpdating).toBe(false);
    expect(ctx.businessContexts.map((e: BusinessContext) => e.businessContextId)).toEqual([1]);
  });
});

/**
 * Issue #1312: a non-owner may open a WIP BIE read-only (access === 'CanView'), like QA/Production.
 * The editor keys read-only vs editable entirely off the backend-returned `state` + `access`, so these
 * pure predicates/handlers are testable by binding the prototype members to a minimal fake `this`.
 */
describe('BieEditComponent #1312 read-only WIP view', () => {
  const isReadOnlyWipViewer = (state: string, access: string, isAdmin = false): boolean =>
    Object.getOwnPropertyDescriptor(BieEditComponent.prototype, 'isReadOnlyWipViewer')!.get!
      .call({state, access, auth: {isAdmin: () => isAdmin}});

  describe('shouldRedirectFromWipEditor (editor-init guard)', () => {
    it('does NOT redirect a non-owner viewing a WIP BIE read-only (CanView)', () => {
      expect(BieEditComponent.shouldRedirectFromWipEditor('WIP', 'CanView', false)).toBe(false);
    });
    it('does NOT redirect the owner (CanEdit)', () => {
      expect(BieEditComponent.shouldRedirectFromWipEditor('WIP', 'CanEdit', false)).toBe(false);
    });
    it('redirects when access is Prohibited or Unprepared', () => {
      expect(BieEditComponent.shouldRedirectFromWipEditor('WIP', 'Prohibited', false)).toBe(true);
      expect(BieEditComponent.shouldRedirectFromWipEditor('WIP', 'Unprepared', false)).toBe(true);
    });
    it('never redirects an administrator', () => {
      expect(BieEditComponent.shouldRedirectFromWipEditor('WIP', 'Prohibited', true)).toBe(false);
    });
    it('does not apply to non-WIP states', () => {
      expect(BieEditComponent.shouldRedirectFromWipEditor('QA', 'CanView', false)).toBe(false);
    });
  });

  describe('isReadOnlyWipViewer', () => {
    it('is true for any non-owner (CanView) viewing a WIP BIE, and only then', () => {
      expect(isReadOnlyWipViewer('WIP', 'CanView')).toBe(true);
      expect(isReadOnlyWipViewer('WIP', 'CanEdit')).toBe(false); // owner
      expect(isReadOnlyWipViewer('QA', 'CanView')).toBe(false);
      expect(isReadOnlyWipViewer('Production', 'CanView')).toBe(false);
    });
    it('does NOT exempt administrators: a non-owner admin also views a WIP BIE read-only', () => {
      expect(isReadOnlyWipViewer('WIP', 'CanView', /* isAdmin */ true)).toBe(true);
      // An owner keeps editing regardless of the admin role (access === 'CanEdit').
      expect(isReadOnlyWipViewer('WIP', 'CanEdit', /* isAdmin */ true)).toBe(false);
    });
  });

  it('copyToDefinition is a no-op for a read-only WIP viewer (isEditable === false)', () => {
    const copyToDefinition = BieEditComponent.prototype.copyToDefinition;
    const target: any = {definition: 'original'};
    // Read-only viewer: isEditable(selectedNode) is false, so nothing is copied.
    copyToDefinition.call({selectedNode: {}, isEditable: () => false, snackBar: {open: vi.fn()}},
      'copied', target);
    expect(target.definition).toBe('original');
    // Owner/editor: isEditable true, so the copy proceeds.
    const snackBar = {open: vi.fn()};
    copyToDefinition.call({selectedNode: {}, isEditable: () => true, snackBar}, 'copied', target);
    expect(target.definition).toBe('copied');
    expect(snackBar.open).toHaveBeenCalled();
  });

  it('isBusinessContextRemovable is false for a read-only WIP viewer even with multiple contexts', () => {
    const get = Object.getOwnPropertyDescriptor(BieEditComponent.prototype, 'isBusinessContextRemovable')!.get!;
    expect(get.call({isReadOnlyWipViewer: false, businessContextUpdating: false,
      businessContexts: [businessContext(1, 'A'), businessContext(2, 'B')]})).toBe(true);
    expect(get.call({isReadOnlyWipViewer: true, businessContextUpdating: false,
      businessContexts: [businessContext(1, 'A'), businessContext(2, 'B')]})).toBe(false);
  });

  it('isBusinessTermEditable is false for a read-only WIP viewer', () => {
    const editable = BieEditComponent.prototype.isBusinessTermEditable;
    const node: any = {used: true, locked: false, isCycle: false};
    expect(editable.call({isReadOnlyWipViewer: false}, node)).toBe(true);
    expect(editable.call({isReadOnlyWipViewer: true}, node)).toBe(false);
  });

  it('addBusinessContext / removeBusinessContext are no-ops for a read-only WIP viewer', () => {
    const bizCtxService = {assign: vi.fn(), unassign: vi.fn()};
    const ctx: any = {
      isReadOnlyWipViewer: true,
      businessContextUpdating: false,
      businessContexts: [businessContext(1, 'A'), businessContext(2, 'B')],
      bizCtxService,
      topLevelAsbiepId: 1001,
      addBusinessContext: BieEditComponent.prototype.addBusinessContext,
      removeBusinessContext: BieEditComponent.prototype.removeBusinessContext
    };

    ctx.addBusinessContext({option: {value: businessContext(3, 'C')}});
    ctx.removeBusinessContext(businessContext(1, 'A'));

    expect(bizCtxService.assign).not.toHaveBeenCalled();
    expect(bizCtxService.unassign).not.toHaveBeenCalled();
  });
});

/**
 * Issue #1755: warn before an "Used" un-check clears descendants. The un-check guard is pure enough
 * to test by binding the prototype method to a plain fake `this` (no Angular wiring), matching the
 * fixtures used above. The dialog fires ONLY when un-checking would actually clear a used descendant
 * (hasUsedDescendant), not merely because the node is expandable; children lazy-load, so the guard
 * materializes them on demand. We still keep the message generic (no count/list of affected fields).
 */
describe('BieEditComponent.toggleTreeUsed un-check guard (#1755)', () => {
  function leaf(name: string, used: boolean): any {
    return {name, used, inverseMode: false, expandable: false, isGroup: false, children: []};
  }

  function toggleCtx(overrides: any = {}): any {
    const afterClosed$ = overrides.afterClosed$ ?? of(true);
    const dialogConfig = {data: {}};
    const ctx: any = {
      isUsable: () => true,
      used: BieEditComponent.prototype.used,
      hasUsedDescendant: (BieEditComponent.prototype as any).hasUsedDescendant,
      dataSource: {database: {loadChildren: vi.fn()}},
      assignVersionToVersionIdIfPossible: vi.fn(),
      confirmDialogService: {
        newConfig: vi.fn().mockReturnValue(dialogConfig),
        open: vi.fn().mockReturnValue({afterClosed: () => afterClosed$})
      },
      _dialogConfig: dialogConfig,
      ...overrides
    };
    ctx.toggleTreeUsed = BieEditComponent.prototype.toggleTreeUsed;
    return ctx;
  }

  it('does nothing when the node is not usable', () => {
    const ctx = toggleCtx({isUsable: () => false});
    const node: any = {name: 'Messages', used: true, inverseMode: false, expandable: true};

    ctx.toggleTreeUsed(node);

    expect(node.used).toBe(true);
    expect(ctx.confirmDialogService.open).not.toHaveBeenCalled();
    expect(ctx.assignVersionToVersionIdIfPossible).not.toHaveBeenCalled();
  });

  it('checks (used=true) without a dialog and re-assigns the version', () => {
    const ctx = toggleCtx();
    const node: any = {name: 'Messages', used: false, inverseMode: false, expandable: true};

    ctx.toggleTreeUsed(node);

    expect(node.used).toBe(true);
    expect(ctx.confirmDialogService.open).not.toHaveBeenCalled();
    expect(ctx.assignVersionToVersionIdIfPossible).toHaveBeenCalledTimes(1);
  });

  it('un-checks a leaf node (no descendants) silently', () => {
    const ctx = toggleCtx();
    const node: any = {name: 'Message Identifier', used: true, inverseMode: false, expandable: false};

    ctx.toggleTreeUsed(node);

    expect(node.used).toBe(false);
    expect(ctx.confirmDialogService.open).not.toHaveBeenCalled();
  });

  it('opens a simple confirm dialog (no count/list) before un-checking a node with a used descendant', () => {
    const ctx = toggleCtx({afterClosed$: of(true)});
    const node: any = {name: 'Messages', used: true, inverseMode: false, expandable: true,
      isGroup: false, children: [leaf('Message Identifier', true)]};

    ctx.toggleTreeUsed(node);

    expect(ctx.confirmDialogService.open).toHaveBeenCalledTimes(1);
    const data = ctx._dialogConfig.data;
    expect(data.header).toBe('Unchecking will clear used descendants');
    expect(data.content).toEqual([
      'Unchecking "Messages" will also clear "Used" on its used descendants.',
      'Do you want to continue?'
    ]);
    expect(data.list).toBeUndefined(); // no enumeration
    expect(data.action).toBe('Uncheck anyway');
    // Confirmed => the cascade runs.
    expect(node.used).toBe(false);
  });

  it('leaves the tree untouched when the user cancels the dialog', () => {
    const ctx = toggleCtx({afterClosed$: of(false)});
    const node: any = {name: 'Messages', used: true, inverseMode: false, expandable: true,
      isGroup: false, children: [leaf('Message Identifier', true)]};

    ctx.toggleTreeUsed(node);

    expect(ctx.confirmDialogService.open).toHaveBeenCalledTimes(1);
    expect(node.used).toBe(true); // model unchanged
    expect(ctx.assignVersionToVersionIdIfPossible).not.toHaveBeenCalled();
  });

  it('restores the clicked checkbox on cancel (mat-checkbox already flipped itself visually)', () => {
    const ctx = toggleCtx({afterClosed$: of(false)});
    const node: any = {name: 'Messages', used: true, inverseMode: false, expandable: true,
      isGroup: false, children: [leaf('Message Identifier', true)]};
    // Material toggled the checkbox to unchecked on click; the model stayed true.
    const checkbox: any = {checked: false};

    ctx.toggleTreeUsed(node, undefined, checkbox);

    expect(node.used).toBe(true);
    expect(checkbox.checked).toBe(true); // re-synced to the (unchanged) model value
  });

  it('un-checks a node whose descendants are all unused silently (no dialog) (#1755)', () => {
    const ctx = toggleCtx();
    // Expandable, but every descendant is already un-used, so nothing would be cleared.
    const node: any = {name: 'Messages', used: true, inverseMode: false, expandable: true,
      isGroup: false, children: [leaf('Message Identifier', false), leaf('Note', false)]};

    ctx.toggleTreeUsed(node);

    expect(ctx.confirmDialogService.open).not.toHaveBeenCalled();
    expect(node.used).toBe(false);
  });

  it('materializes collapsed children on demand and warns when a used descendant is found (#1755)', () => {
    const usedChild = leaf('Message Identifier', true);
    const loadChildren = vi.fn().mockImplementation((n: any) => { n.children = [usedChild]; });
    const ctx = toggleCtx({dataSource: {database: {loadChildren}}, afterClosed$: of(true)});
    // Collapsed: children not loaded yet (empty), so the guard must load them to find the used one.
    const node: any = {name: 'Messages', used: true, inverseMode: false, expandable: true,
      isGroup: false, children: []};

    ctx.toggleTreeUsed(node);

    expect(loadChildren).toHaveBeenCalledWith(node);
    expect(ctx.confirmDialogService.open).toHaveBeenCalledTimes(1);
    expect(node.used).toBe(false);
  });
});
