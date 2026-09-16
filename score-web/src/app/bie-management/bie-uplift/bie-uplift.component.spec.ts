import {BieUpliftComponent, BieUpliftSourceFlatNodeDatabase, BieUpliftTargetFlatNodeDatabase} from './bie-uplift.component';
import {BieUpliftMap, BieUpliftSourceFlatNode, BieUpliftTargetFlatNode, MatchInfo} from './domain/bie-uplift';
import {AsbiepFlatNode, BbiepFlatNode, BieFlatNode, BieFlatNodeDataSource, BieFlatNodeDatabase} from '../domain/bie-flat-tree';
import {BieEditAbieNode, UsedBie} from '../bie-edit/domain/bie-edit-node';
import {of, Subject} from 'rxjs';
import {vi} from 'vitest';
import {sha256} from '../../common/utility';

type TestNode = {
  level: number;
  type: string;
  bieType: string;
  parent?: TestNode;
  isGroup: boolean;
  fixed: boolean;
  used: boolean;
  expandable: boolean;
  queryPath: string;
  source?: TestNode;
  target?: TestNode;
  reused?: boolean;
  reusedTopLevelAsbiepId?: number;
  locked?: boolean;
  children: TestNode[];
};

function createNode(level: number, type: string, parent?: TestNode, isGroup = false): TestNode {
  return {
    level,
    type,
    bieType: type,
    parent,
    isGroup,
    fixed: false,
    used: true,
    expandable: false,
    queryPath: '/' + type + '-' + level,
    source: undefined,
    target: undefined,
    reused: false,
    reusedTopLevelAsbiepId: undefined,
    locked: false,
    children: []
  };
}

describe('BieUpliftComponent', () => {
  it('should be defined', () => {
    expect(BieUpliftComponent).toBeTruthy();
  });

  it('shows an inherited-ACC element at each reused Party occurrence with canonical persisted usage', () => {
    const path = 'ASCCP-223067>ACC-261633>ACC-261531>ACC-261530>BCC-131600';
    const database = new BieUpliftSourceFlatNodeDatabase<BieUpliftSourceFlatNode>(null, null, 866, [
      new UsedBie({type: 'BBIE', manifestId: 131600, ownerTopLevelAsbiepId: 861,
        hashPath: sha256(path), used: true, cardinalityMax: 1})
    ], []);
    const dataSource = new BieFlatNodeDataSource(database, null, null);
    dataSource.hideUnused = true;
    const parties = [0, 1].map(index => {
      const party = new AsbiepFlatNode();
      party.name = 'Party';
      party.level = 2;
      party._queryPath = `BOM/Occurrence${index}/Party`;
      party.asccpNode = {manifestId: 223067} as any;
      party.accNode = {manifestId: 261633, componentType: 'Normal'} as any;
      party.reused = true;
      party.topLevelAsbiepId = 861;
      party.rootNode = new BieEditAbieNode();
      const identifier = new BbiepFlatNode();
      identifier.name = 'Identifier';
      identifier.level = 3;
      identifier.parent = party;
      identifier.topLevelAsbiepId = 861;
      identifier.bccNode = {manifestId: 131600, entityType: 'Element'} as any;
      identifier.bccpNode = {manifestId: 100} as any;
      identifier.bdtNode = {manifestId: 200} as any;
      identifier.intermediateAccNodes = [261633, 261531, 261530]
        .map(manifestId => ({type: 'ACC', manifestId} as any));
      database.afterBbiepFlatNode(identifier);
      expect(identifier.bbiePath).toBe(path);
      expect(identifier.used).toBe(true);
      party.children = [identifier];
      return new BieUpliftSourceFlatNode(party);
    });
    dataSource.data = parties.slice();
    const load = vi.spyOn(database, 'loadChildren');

    parties.forEach(party => dataSource.toggleNode(party, true));

    expect(dataSource.data.map(node => node.queryPath)).toEqual([
      'BOM/Occurrence0/Party', 'BOM/Occurrence0/Party/Identifier',
      'BOM/Occurrence1/Party', 'BOM/Occurrence1/Party/Identifier'
    ]);
    expect(load).not.toHaveBeenCalled();
  });

  it('preserves automatic mappings for repeated reused-BIE occurrences', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceOneRaw = createNode(1, 'ASBIEP');
    const sourceTwoRaw = createNode(1, 'ASBIEP');
    const targetOneRaw = createNode(1, 'ASBIEP');
    const targetTwoRaw = createNode(1, 'ASBIEP');
    const sourceOnePath = 'ASCCP-10>ACC-20>ASCC-11';
    const sourceTwoPath = 'ASCCP-10>ACC-21>ASCC-11';
    const targetOnePath = 'ASCCP-30>ACC-40>ASCC-31';
    const targetTwoPath = 'ASCCP-30>ACC-41>ASCC-31';
    (sourceOneRaw as any).asbiePath = sourceOnePath;
    (sourceTwoRaw as any).asbiePath = sourceTwoPath;
    (targetOneRaw as any).asbiePath = targetOnePath;
    (targetTwoRaw as any).asbiePath = targetTwoPath;
    (sourceOneRaw as any).asccNode = {manifestId: 11};
    (sourceTwoRaw as any).asccNode = {manifestId: 11};
    (targetOneRaw as any).asccNode = {manifestId: 31};
    (targetTwoRaw as any).asccNode = {manifestId: 31};

    const sourceOne = new BieUpliftSourceFlatNode(sourceOneRaw as unknown as BieFlatNode);
    const sourceTwo = new BieUpliftSourceFlatNode(sourceTwoRaw as unknown as BieFlatNode);
    const targetOne = new BieUpliftTargetFlatNode(targetOneRaw as unknown as BieFlatNode);
    const targetTwo = new BieUpliftTargetFlatNode(targetTwoRaw as unknown as BieFlatNode);
    const upliftMap = new BieUpliftMap({
      asbiePathList: [
        {bieId: 100, source: {path: sourceOnePath, context: 'one'},
          target: {path: targetOnePath, context: 'one'}},
        {bieId: 100, source: {path: sourceTwoPath, context: 'two'},
          target: {path: targetTwoPath, context: 'two'}}
      ]
    });

    component.initMapping([sourceOne, sourceTwo], [targetOne, targetTwo], upliftMap);

    expect(sourceOne.target).toBe(targetOne);
    expect(sourceTwo.target).toBe(targetTwo);
    expect(sourceOne.systemTarget).toBe(targetOne);
    expect(sourceTwo.systemTarget).toBe(targetTwo);
    expect(sourceOne.context).toBe('one');
    expect(sourceTwo.context).toBe('two');
    expect(sourceOne.asbiePath).toBe(sourceOnePath);
    expect(sourceTwo.asbiePath).toBe(sourceTwoPath);
    expect(targetOne.asbiePath).toBe(targetOnePath);
    expect(targetTwo.asbiePath).toBe(targetTwoPath);
  });

  it.each([
    ['Attribute', 'Element', false],
    ['Element', 'Attribute', false],
    ['Attribute', 'Attribute', true],
    ['Element', 'Element', true]
  ])('checks BBIE entity kinds %s → %s', (sourceKind, targetKind, allowed) => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRaw = new BbiepFlatNode();
    const targetRaw = new BbiepFlatNode();
    sourceRaw.bccNode = {entityType: sourceKind} as any;
    targetRaw.bccNode = {entityType: targetKind} as any;
    sourceRaw.bdtNode = targetRaw.bdtNode = {componentId: 1} as any;
    const source = new BieUpliftSourceFlatNode(sourceRaw);
    const target = new BieUpliftTargetFlatNode(targetRaw);
    component.sourceSelectedNode = source;
    vi.spyOn(component as any, 'hasMappedParentPair').mockReturnValue(true);
    const attach = vi.spyOn(component as any, 'attachMapping').mockImplementation(() => {});
    vi.spyOn(component as any, 'refreshUnmatchedSources').mockImplementation(() => {});
    const open = vi.fn();
    (component as any).confirmDialogService = {open};

    expect(component.canMatch(target)).toBe(allowed);
    if (!allowed) {
      expect(component.getMatchDisabledReason(target)).toContain('attributes and elements');
    }
    component.checkMatch({}, target);
    expect(attach).toHaveBeenCalledTimes(allowed ? 1 : 0);
    expect(open).not.toHaveBeenCalled();
  });

  it('does not allow mapping a child when its source parent is unmapped', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ABIE');
    const sourceParent = createNode(1, 'ASBIEP', sourceRoot);
    const sourceChild = createNode(2, 'BBIEP', sourceParent);
    const targetRoot = createNode(0, 'ASBIEP');
    const targetParent = createNode(1, 'ASBIEP', targetRoot);
    const targetChild = createNode(2, 'BBIEP', targetParent);

    component.sourceSelectedNode = sourceChild as unknown as BieUpliftSourceFlatNode;

    expect(component.canMatch(targetChild as unknown as BieUpliftTargetFlatNode)).toBe(false);
  });

  it('does not allow mapping a child into an unmapped target parent', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ASBIEP');
    const sourceParent = createNode(1, 'ASBIEP', sourceRoot);
    const sourceChild = createNode(2, 'BBIEP', sourceParent);
    const targetRoot = createNode(0, 'ASBIEP');
    const targetParent = createNode(1, 'ASBIEP', targetRoot);
    const targetChild = createNode(2, 'BBIEP', targetParent);
    sourceParent.target = targetParent;

    component.sourceSelectedNode = sourceChild as unknown as BieUpliftSourceFlatNode;

    expect(component.canMatch(targetChild as unknown as BieUpliftTargetFlatNode)).toBe(false);
  });

  it('allows mapping a child when both structural parents are mapped', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ASBIEP');
    const sourceParent = createNode(1, 'ASBIEP', sourceRoot);
    const sourceChild = createNode(2, 'BBIEP', sourceParent);
    const targetRoot = createNode(0, 'ASBIEP');
    const targetParent = createNode(1, 'ASBIEP', targetRoot);
    const targetChild = createNode(2, 'BBIEP', targetParent);
    sourceParent.target = targetParent;
    targetParent.source = sourceParent;

    component.sourceSelectedNode = sourceChild as unknown as BieUpliftSourceFlatNode;

    expect(component.canMatch(targetChild as unknown as BieUpliftTargetFlatNode)).toBe(true);
  });

  it('allows a User Extension child to map below an already mapped target ancestor', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ASBIEP');
    const sourceExtension = createNode(1, 'ASBIEP', sourceRoot);
    sourceExtension.name = 'Extension';
    const sourceChild = createNode(2, 'BBIEP', sourceExtension);
    const targetRoot = createNode(0, 'ASBIEP');
    const targetParent = createNode(1, 'ASBIEP', targetRoot);
    const targetChild = createNode(2, 'BBIEP', targetParent);
    targetParent.source = sourceExtension;

    component.sourceSelectedNode = sourceChild as unknown as BieUpliftSourceFlatNode;

    expect(component.canMatch(targetChild as unknown as BieUpliftTargetFlatNode)).toBe(true);
  });

  it('TC_29_1_TA_5_f (M21): rejects an Extension child below an unrelated mapped source branch', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ASBIEP');
    const sourceExtension = createNode(1, 'ASBIEP', sourceRoot);
    sourceExtension.name = 'Extension';
    const sourceChild = createNode(2, 'BBIEP', sourceExtension);
    const targetRoot = createNode(0, 'ASBIEP');
    const targetParent = createNode(1, 'ASBIEP', targetRoot);
    const targetChild = createNode(2, 'BBIEP', targetParent);
    targetParent.source = createNode(1, 'ASBIEP');

    component.sourceSelectedNode = sourceChild as unknown as BieUpliftSourceFlatNode;

    expect(component.canMatch(targetChild as unknown as BieUpliftTargetFlatNode)).toBe(false);
  });

  it.each([
    ['rejects a User Extension child when the BIE node types differ', 'ASBIEP', undefined,
      'Source and target node types must match'],
    ['rejects a User Extension BBIE when the entity types differ', 'BBIEP', 'Attribute',
      'BBIE attributes and elements cannot be mapped to each other']
  ])('%s', (_description, targetType, targetEntityType, reason) => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ASBIEP');
    const sourceExtension = createNode(1, 'ASBIEP', sourceRoot);
    sourceExtension.name = 'Extension';
    const sourceChild = createNode(2, 'BBIEP', sourceExtension);
    const targetRoot = createNode(0, 'ASBIEP');
    const targetParent = createNode(1, 'ASBIEP', targetRoot);
    const targetChild = createNode(2, targetType, targetParent);
    (sourceChild as any)._node = {bccNode: {entityType: 'Element'}};
    (targetChild as any)._node = {bccNode: {entityType: targetEntityType || 'Element'}};
    targetParent.source = sourceExtension;
    sourceExtension.target = targetParent;

    component.sourceSelectedNode = sourceChild as unknown as BieUpliftSourceFlatNode;

    expect(component.canMatch(targetChild as unknown as BieUpliftTargetFlatNode)).toBe(false);
    expect(component.getMatchDisabledReason(targetChild as unknown as BieUpliftTargetFlatNode)).toBe(reason);
  });

  it('rejects a User Extension child inside a target reuse reference', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ASBIEP');
    const sourceExtension = createNode(1, 'ASBIEP', sourceRoot);
    sourceExtension.name = 'Extension';
    const sourceChild = createNode(2, 'BBIEP', sourceExtension);
    const targetRoot = createNode(0, 'ASBIEP');
    const targetParent = createNode(1, 'ASBIEP', targetRoot);
    targetParent.reusedTopLevelAsbiepId = 11;
    const targetChild = createNode(2, 'BBIEP', targetParent);
    (sourceChild as any)._node = {bccNode: {entityType: 'Element'}};
    (targetChild as any)._node = {bccNode: {entityType: 'Element'}};
    component.sourceSelectedNode = sourceChild as unknown as BieUpliftSourceFlatNode;
    (component as any).targetDataSource = {data: [targetRoot, targetParent, targetChild]};

    expect(component.canMatch(targetChild as unknown as BieUpliftTargetFlatNode)).toBe(false);
    expect(component.getMatchDisabledReason(targetChild as unknown as BieUpliftTargetFlatNode))
      .toBe('Already mapped by the selected reuse BIE');
  });

  it('asks for confirmation before mapping different role ACC components', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRaw = createNode(1, 'ASBIEP');
    const targetRaw = createNode(1, 'ASBIEP');
    (sourceRaw as any).accNode = {manifestId: 11, guid: 'source-guid'};
    (targetRaw as any).accNode = {manifestId: 22, guid: 'target-guid'};
    const source = new BieUpliftSourceFlatNode(sourceRaw as unknown as BieFlatNode);
    const target = new BieUpliftTargetFlatNode(targetRaw as unknown as BieFlatNode);
    const open = vi.fn().mockReturnValue({afterClosed: () => of(true)});
    (component as any).confirmDialogService = {
      newConfig: () => ({data: {}}),
      open
    };
    (component as any).hasMappedParentPair = vi.fn(() => true);
    component.sourceSelectedNode = source;

    component.checkMatch({}, target);

    expect(open).toHaveBeenCalledOnce();
    expect(target.source).toBe(source);
  });

  it('confirms before replacing an existing incompatible target mapping', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const previousSourceRaw = createNode(1, 'ASBIEP');
    const sourceRaw = createNode(1, 'ASBIEP');
    const targetRaw = createNode(1, 'ASBIEP');
    (previousSourceRaw as any).accNode = {manifestId: 10, componentId: 100, guid: 'previous-guid'};
    (sourceRaw as any).accNode = {manifestId: 11, componentId: 101, guid: 'source-guid'};
    (targetRaw as any).accNode = {manifestId: 12, componentId: 102, guid: 'target-guid'};
    const previousSource = new BieUpliftSourceFlatNode(previousSourceRaw as unknown as BieFlatNode);
    const source = new BieUpliftSourceFlatNode(sourceRaw as unknown as BieFlatNode);
    const target = new BieUpliftTargetFlatNode(targetRaw as unknown as BieFlatNode);
    previousSource.target = target;
    target.source = previousSource;
    const open = vi.fn().mockReturnValue({afterClosed: () => of(false)});
    (component as any).confirmDialogService = {
      newConfig: () => ({data: {}}),
      open
    };
    (component as any).hasMappedParentPair = vi.fn(() => true);
    component.sourceSelectedNode = source;

    component.checkMatch({}, target);

    expect(open).toHaveBeenCalledOnce();
    expect(target.source).toBe(previousSource);
    expect(source.target).toBeUndefined();
  });

  it('restores the target checkbox when an incompatible mapping is cancelled', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRaw = createNode(1, 'ASBIEP');
    const targetRaw = createNode(1, 'ASBIEP');
    (sourceRaw as any).accNode = {manifestId: 11, guid: 'source-guid'};
    (targetRaw as any).accNode = {manifestId: 22, guid: 'target-guid'};
    const source = new BieUpliftSourceFlatNode(sourceRaw as unknown as BieFlatNode);
    const target = new BieUpliftTargetFlatNode(targetRaw as unknown as BieFlatNode);
    const checkbox = {checked: true};
    (component as any).confirmDialogService = {
      newConfig: () => ({data: {}}),
      open: vi.fn().mockReturnValue({afterClosed: () => of(false)})
    };
    (component as any).hasMappedParentPair = vi.fn(() => true);
    component.sourceSelectedNode = source;

    component.checkMatch({source: checkbox}, target);

    expect(target.source).toBeUndefined();
    expect(checkbox.checked).toBe(false);
  });

  it('keeps the source selected when confirmation resolves asynchronously', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const originalRaw = createNode(1, 'ASBIEP');
    const changedRaw = createNode(1, 'ASBIEP');
    const targetRaw = createNode(1, 'ASBIEP');
    (originalRaw as any).accNode = {manifestId: 11, componentId: 101, guid: 'original-guid'};
    (changedRaw as any).accNode = {manifestId: 12, componentId: 102, guid: 'changed-guid'};
    (targetRaw as any).accNode = {manifestId: 13, componentId: 103, guid: 'target-guid'};
    const originalSource = new BieUpliftSourceFlatNode(originalRaw as unknown as BieFlatNode);
    const changedSource = new BieUpliftSourceFlatNode(changedRaw as unknown as BieFlatNode);
    const target = new BieUpliftTargetFlatNode(targetRaw as unknown as BieFlatNode);
    const result = new Subject<boolean>();
    const open = vi.fn().mockReturnValue({afterClosed: () => result});
    (component as any).confirmDialogService = {
      newConfig: () => ({data: {}}),
      open
    };
    (component as any).hasMappedParentPair = vi.fn(() => true);
    component.sourceSelectedNode = originalSource;

    component.checkMatch({}, target);
    component.sourceSelectedNode = changedSource;
    result.next(true);
    result.complete();

    expect(target.source).toBe(originalSource);
    expect(changedSource.target).toBeUndefined();
  });

  it('allows role ACCs with the same stable component id across releases', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRaw = createNode(1, 'ASBIEP');
    const targetRaw = createNode(1, 'ASBIEP');
    (sourceRaw as any).accNode = {manifestId: 11, componentId: 101, guid: 'source-guid'};
    (targetRaw as any).accNode = {manifestId: 22, componentId: 101, guid: 'target-guid'};
    const source = new BieUpliftSourceFlatNode(sourceRaw as unknown as BieFlatNode);
    const target = new BieUpliftTargetFlatNode(targetRaw as unknown as BieFlatNode);
    const open = vi.fn();
    (component as any).confirmDialogService = {
      newConfig: () => ({data: {}}),
      open
    };
    (component as any).hasMappedParentPair = vi.fn(() => true);
    component.sourceSelectedNode = source;

    component.checkMatch({}, target);

    expect(open).not.toHaveBeenCalled();
    expect(target.source).toBe(source);
  });

  it('uses the DT component id rather than the BCCP manifest id', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRaw = createNode(1, 'BBIEP');
    const targetRaw = createNode(1, 'BBIEP');
    (sourceRaw as any).bdtNode = {manifestId: 31, componentId: 301, guid: 'source-guid'};
    (targetRaw as any).bdtNode = {manifestId: 42, componentId: 301, guid: 'target-guid'};
    const source = new BieUpliftSourceFlatNode(sourceRaw as unknown as BieFlatNode);
    const target = new BieUpliftTargetFlatNode(targetRaw as unknown as BieFlatNode);

    expect((component as any).getMappingCompatibilityReason(source, target)).toBe('');
  });

  it('uses the DT_SC component id rather than its owner DT id', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRaw = createNode(1, 'BBIE_SC');
    const targetRaw = createNode(1, 'BBIE_SC');
    (sourceRaw as any).bdtNode = {manifestId: 31, componentId: 301, guid: 'owner-source'};
    (targetRaw as any).bdtNode = {manifestId: 32, componentId: 302, guid: 'owner-target'};
    (sourceRaw as any).bdtScNode = {manifestId: 41, componentId: 401, guid: 'source-sc'};
    (targetRaw as any).bdtScNode = {manifestId: 42, componentId: 401, guid: 'target-sc'};
    const source = new BieUpliftSourceFlatNode(sourceRaw as unknown as BieFlatNode);
    const target = new BieUpliftTargetFlatNode(targetRaw as unknown as BieFlatNode);

    expect((component as any).getMappingCompatibilityReason(source, target)).toBe('');
  });

  it('asks for confirmation when legacy nodes only share a manifest id', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRaw = createNode(1, 'ASBIEP');
    const targetRaw = createNode(1, 'ASBIEP');
    (sourceRaw as any).accNode = {manifestId: 11};
    (targetRaw as any).accNode = {manifestId: 11};
    const source = new BieUpliftSourceFlatNode(sourceRaw as unknown as BieFlatNode);
    const target = new BieUpliftTargetFlatNode(targetRaw as unknown as BieFlatNode);
    const open = vi.fn().mockReturnValue({afterClosed: () => of(false)});
    (component as any).confirmDialogService = {
      newConfig: () => ({data: {}}),
      open
    };
    (component as any).hasMappedParentPair = vi.fn(() => true);
    component.sourceSelectedNode = source;

    component.checkMatch({}, target);

    expect((component as any).getMappingCompatibilityReason(source, target)).toContain('role ACC');
    expect(open).toHaveBeenCalledOnce();
    expect(target.source).toBeUndefined();
  });

  it('does not treat null component ids as a match', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRaw = createNode(1, 'ASBIEP');
    const targetRaw = createNode(1, 'ASBIEP');
    (sourceRaw as any).accNode = {manifestId: 11, componentId: null};
    (targetRaw as any).accNode = {manifestId: 11, componentId: null};
    const source = new BieUpliftSourceFlatNode(sourceRaw as unknown as BieFlatNode);
    const target = new BieUpliftTargetFlatNode(targetRaw as unknown as BieFlatNode);

    expect((component as any).getMappingCompatibilityReason(source, target)).toContain('role ACC');
  });

  it('compares DT_SC itself instead of its owner DT', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRaw = createNode(1, 'BBIE_SC');
    const targetRaw = createNode(1, 'BBIE_SC');
    (sourceRaw as any).bdtNode = {manifestId: 31, guid: 'same-owner'};
    (targetRaw as any).bdtNode = {manifestId: 31, guid: 'same-owner'};
    (sourceRaw as any).bdtScNode = {manifestId: 41, guid: 'source-sc'};
    (targetRaw as any).bdtScNode = {manifestId: 42, guid: 'target-sc'};
    const source = new BieUpliftSourceFlatNode(sourceRaw as unknown as BieFlatNode);
    const target = new BieUpliftTargetFlatNode(targetRaw as unknown as BieFlatNode);

    expect((component as any).getMappingCompatibilityReason(source, target)).toContain('DT_SC');

    (targetRaw as any).bdtScNode.guid = 'source-sc';
    expect((component as any).getMappingCompatibilityReason(source, target)).toBe('');
  });

  it('rejects mapping a child under a different mapped parent', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ABIE');
    const sourceParent = createNode(1, 'ASBIEP', sourceRoot);
    const sourceChild = createNode(2, 'BBIEP', sourceParent);
    const targetRoot = createNode(0, 'ABIE');
    const targetParent = createNode(1, 'ASBIEP', targetRoot);
    const otherTargetParent = createNode(1, 'ASBIEP', targetRoot);
    const targetChild = createNode(2, 'BBIEP', otherTargetParent);
    sourceParent.target = targetParent;
    targetParent.source = sourceParent;

    component.sourceSelectedNode = sourceChild as unknown as BieUpliftSourceFlatNode;

    expect(component.canMatch(targetChild as unknown as BieUpliftTargetFlatNode)).toBe(false);
  });

  it('allows a system-mapped source node to be manually remapped', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ABIE');
    const sourceParent = createNode(1, 'ASBIEP', sourceRoot);
    const sourceChild = createNode(2, 'BBIEP', sourceParent);
    sourceChild.fixed = true;
    const targetRoot = createNode(0, 'ASBIEP');
    const targetParent = createNode(1, 'ASBIEP', targetRoot);
    const targetChild = createNode(2, 'BBIEP', targetParent);
    sourceParent.target = targetParent;
    targetParent.source = sourceParent;
    sourceChild.target = createNode(2, 'BBIEP', targetParent);
    sourceChild.target.source = sourceChild;

    component.sourceSelectedNode = sourceChild as unknown as BieUpliftSourceFlatNode;
    expect(component.canMatch(targetChild as unknown as BieUpliftTargetFlatNode)).toBe(true);

    (component as any).attachMapping(
      sourceChild as unknown as BieUpliftSourceFlatNode,
      targetChild as unknown as BieUpliftTargetFlatNode);

    expect(sourceChild.fixed).toBe(false);
    expect(sourceChild.target).toBe(targetChild);
    expect(targetChild.source).toBe(sourceChild);

    (sourceChild as any).bccNode = {manifestId: 1};
    (sourceRoot as any).path = 'ASCCP-10>ACC-11';
    (sourceParent as any).asccNode = {manifestId: 12};
    (sourceParent as any).asccpNode = {manifestId: 13};
    (sourceParent as any).accNode = {manifestId: 14};
    (targetChild as any).bccNode = {manifestId: 2};
    const reportSource = new BieUpliftSourceFlatNode(sourceChild as unknown as BieFlatNode);
    reportSource.target = new BieUpliftTargetFlatNode(targetChild as unknown as BieFlatNode);
    const report = new MatchInfo(reportSource);
    expect(report.match).toBe('Manual');
  });

  it('restores the system classification when a mapping returns to its initial target', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRaw = createNode(1, 'ASBIEP');
    const initialTargetRaw = createNode(1, 'ASBIEP');
    const alternateTargetRaw = createNode(1, 'ASBIEP');
    (sourceRaw as any).asbiePath = 'SOURCE';
    (initialTargetRaw as any).asbiePath = 'INITIAL-TARGET';
    (alternateTargetRaw as any).asbiePath = 'ALTERNATE-TARGET';
    (sourceRaw as any).asccNode = {manifestId: 1};
    (initialTargetRaw as any).asccNode = {manifestId: 2};
    (alternateTargetRaw as any).asccNode = {manifestId: 3};

    const source = new BieUpliftSourceFlatNode(sourceRaw as unknown as BieFlatNode);
    const initialTarget = new BieUpliftTargetFlatNode(initialTargetRaw as unknown as BieFlatNode);
    const alternateTarget = new BieUpliftTargetFlatNode(alternateTargetRaw as unknown as BieFlatNode);

    source.systemTarget = initialTarget;
    source.target = initialTarget;
    source.fixed = true;
    initialTarget.source = source;

    (component as any).attachMapping(source, alternateTarget);
    expect(source.fixed).toBe(false);

    (component as any).attachMapping(source, initialTarget);
    expect(source.fixed).toBe(true);
    expect(new MatchInfo(source).match).toBe('System');
  });

  it('refreshes unmatched sources after mapping, unmapping, and remapping', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const source = new BieUpliftSourceFlatNode(createNode(1, 'BBIEP') as unknown as BieFlatNode);
    const targetA = createNode(1, 'BBIEP');
    const targetB = createNode(1, 'BBIEP');
    component.sourceSelectedNode = source;
    (component as any).sourceDataSource = {data: [source]};
    (component as any).targetDataSource = {data: [targetA, targetB]};
    (component as any).refreshUnmatchedSources();

    expect(component.unmatchedSource).toEqual([source]);

    component.checkMatch({}, targetA as unknown as BieUpliftTargetFlatNode);
    expect(component.unmatchedSource).toEqual([]);

    component.checkMatch({}, targetA as unknown as BieUpliftTargetFlatNode);
    expect(component.unmatchedSource).toEqual([source]);

    component.checkMatch({}, targetB as unknown as BieUpliftTargetFlatNode);
    expect(component.unmatchedSource).toEqual([]);
    expect(source.target).toBe(targetB);
  });

  it('removes a reused source from unmatched sources after reuse selection', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const source = new BieUpliftSourceFlatNode(createNode(1, 'ASBIEP') as unknown as BieFlatNode);
    const target = new BieUpliftTargetFlatNode(createNode(1, 'ASBIEP') as unknown as BieFlatNode);
    (source as any)._node.reused = true;
    source.target = target;
    target.reusedTopLevelAsbiepId = 99;
    (component as any).sourceDataSource = {data: [source]};

    (component as any).refreshUnmatchedSources();

    expect(source.isMapped).toBe(true);
    expect(component.unmatchedSource).toEqual([]);
  });

  it('keeps a manual mapping when selecting a reuse BIE is cancelled', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceReuse = createNode(1, 'ASBIEP');
    sourceReuse.reused = true;
    const sourceChild = createNode(2, 'BBIEP', sourceReuse);
    const targetReuse = createNode(1, 'ASBIEP');
    const targetChild = createNode(2, 'BBIEP', targetReuse);
    (targetReuse as any)._node = {asccpNode: {manifestId: 1}};
    sourceReuse.target = targetReuse;
    targetReuse.source = sourceReuse;
    sourceChild.target = targetChild;
    targetChild.source = sourceChild;

    (component as any).dialog = {open: vi.fn().mockReturnValue({afterClosed: () => of(undefined)})};
    (component as any).hasMappedParentPair = vi.fn(() => true);

    component.matchReused(targetReuse as unknown as BieUpliftTargetFlatNode);

    expect(sourceChild.target).toBe(targetChild);
    expect(targetChild.source).toBe(sourceChild);
    expect(targetReuse.reusedTopLevelAsbiepId).toBeUndefined();
  });

  it('applies a selected reuse BIE only after its data has loaded', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = new BieUpliftSourceFlatNode(
      createNode(0, 'ASBIEP') as unknown as BieFlatNode);
    const sourceReuse = new BieUpliftSourceFlatNode(
      createNode(1, 'ASBIEP', (sourceRoot as any)._node) as unknown as BieFlatNode);
    const targetRoot = new BieUpliftTargetFlatNode(
      createNode(0, 'ASBIEP') as unknown as BieFlatNode);
    const targetReuse = new BieUpliftTargetFlatNode(
      createNode(1, 'ASBIEP', (targetRoot as any)._node) as unknown as BieFlatNode);
    sourceReuse._node.reused = true;
    (targetReuse as any)._node.asccpNode = {manifestId: 1};
    sourceRoot.children = [sourceReuse];
    targetRoot.children = [targetReuse];
    sourceReuse.target = targetReuse;
    targetReuse.source = sourceReuse;
    const rootNode = new BieEditAbieNode();

    (component as any).dialog = {open: vi.fn().mockReturnValue({afterClosed: () => of(42)})};
    (component as any).hasMappedParentPair = vi.fn(() => true);
    (component as any).bieEditService = {
      getRootNode: vi.fn(() => of(rootNode)),
      getUsedBieList: vi.fn(() => of([])),
      getRefBieList: vi.fn(() => of([]))
    };
    (component as any).targetDataSource = {
      database: {
        appendUsedBieList: vi.fn(),
        appendRefBieList: vi.fn(),
        setBaseUsedBieList: vi.fn(),
        appendBaseUsedBieList: vi.fn()
      },
      data: [targetRoot],
      isExpanded: vi.fn(() => false),
      expand: vi.fn(),
      dataChange: {next: vi.fn()}
    };
    (component as any).sourceDataSource = {
      data: [sourceRoot],
      expand: vi.fn(),
      dataChange: {next: vi.fn()}
    };

    component.matchReused(targetReuse as unknown as BieUpliftTargetFlatNode);

    expect(targetReuse.reusedTopLevelAsbiepId).toBe(42);
    expect((targetReuse as any)._node.topLevelAsbiepId).toBe(42);
  });

  it.each([false, true])('reloads target descendants when changing reuse with expanded=%s', expanded => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const rawNode = createNode(1, 'ASBIEP') as unknown as BieFlatNode;
    const node = new BieUpliftTargetFlatNode(rawNode);
    const staleChild = new BieUpliftTargetFlatNode(createNode(2, 'BBIEP') as unknown as BieFlatNode);
    rawNode.children = [staleChild.self];
    node.children = [staleChild];
    const database = Object.create(BieUpliftTargetFlatNodeDatabase.prototype) as BieUpliftTargetFlatNodeDatabase<any>;
    const freshChild = createNode(2, 'BBIEP') as unknown as BieFlatNode;
    const load = vi.spyOn(BieFlatNodeDatabase.prototype, 'loadChildren').mockImplementation(raw => {
      raw.children = [freshChild];
    });
    // Keep real database.children/loadChildren behavior, isolating only view ordering.
    vi.spyOn(database as any, 'sortByViewOrder').mockImplementation((_id, children) => children);
    const expand = vi.fn(() => database.children(node));
    (component as any).targetDataSource = {
      isExpanded: () => expanded,
      collapse: vi.fn(),
      expand
    };

    try {
      for (const selection of [753, 752, undefined]) {
        if (selection === undefined) {
          (component as any).clearTargetReuseNode(node);
        } else {
          (component as any).prepareTargetReuseNode(node, selection, new BieEditAbieNode());
        }
        const children = database.children(node);
        expect(children[0].self).toBe(freshChild);
        expect(children).not.toContain(staleChild);
      }
      expect(load).toHaveBeenCalledTimes(3);
      expect(expand).toHaveBeenCalledTimes(expanded ? 1 : 0);
    } finally {
      vi.restoreAllMocks();
    }
  });

  it('does not scope reuse candidates by the source inheritance family', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = new BieUpliftSourceFlatNode(
      createNode(0, 'ASBIEP') as unknown as BieFlatNode);
    const sourceReuse = new BieUpliftSourceFlatNode(
      createNode(1, 'ASBIEP', (sourceRoot as any)._node) as unknown as BieFlatNode);
    const targetRoot = new BieUpliftTargetFlatNode(
      createNode(0, 'ASBIEP') as unknown as BieFlatNode);
    const targetReuse = new BieUpliftTargetFlatNode(
      createNode(1, 'ASBIEP', (targetRoot as any)._node) as unknown as BieFlatNode);
    sourceReuse._node.reused = true;
    sourceReuse._node.basedTopLevelAsbiepId = 17;
    sourceReuse._node.inherited = true;
    (targetReuse as any)._node.asccpNode = {manifestId: 1};
    sourceRoot.children = [sourceReuse];
    targetRoot.children = [targetReuse];
    sourceReuse.target = targetReuse;
    targetReuse.source = sourceReuse;
    const rootNode = new BieEditAbieNode();
    rootNode.basedTopLevelAsbiepId = 29;

    const open = vi.fn().mockReturnValue({afterClosed: () => of(42)});
    (component as any).dialog = {open};
    (component as any).hasMappedParentPair = vi.fn(() => true);
    (component as any).bieEditService = {
      getRootNode: vi.fn(() => of(rootNode)),
      getUsedBieList: vi.fn(() => of([])),
      getRefBieList: vi.fn(() => of([]))
    };
    (component as any).targetDataSource = {
      database: {
        appendUsedBieList: vi.fn(),
        appendRefBieList: vi.fn(),
        setBaseUsedBieList: vi.fn(),
        appendBaseUsedBieList: vi.fn()
      },
      data: [targetRoot],
      isExpanded: vi.fn(() => false),
      expand: vi.fn(),
      dataChange: {next: vi.fn()}
    };
    (component as any).sourceDataSource = {
      data: [sourceRoot],
      expand: vi.fn(),
      dataChange: {next: vi.fn()}
    };
    (component as any).sourceLibraryId = 3;
    (component as any).targetReleaseId = 4;
    (component as any).topLevelAsbiepId = 5;

    component.matchReused(targetReuse as unknown as BieUpliftTargetFlatNode);

    const dialogConfig = open.mock.calls[0][1];
    expect(dialogConfig.data).toMatchObject({
      asccpManifestId: 1,
      libraryId: 3,
      releaseId: 4,
      topLevelAsbiepId: 5
    });
    expect(dialogConfig.data).not.toHaveProperty('basedTopLevelAsbiepId');
    expect((targetReuse as any)._node.basedTopLevelAsbiepId).toBe(29);
    expect((targetReuse as any)._node.inherited).toBe(true);
  });

  it('TC_29_1_TA_5_d (R17): does not scope standalone reused candidates by the referenced source id', () => {
    const component = Object.create(BieUpliftComponent.prototype) as any;
    const sourceRaw: any = createNode(1, 'ASBIEP');
    sourceRaw.reused = true;
    sourceRaw.basedTopLevelAsbiepId = 17;
    const targetRaw: any = createNode(1, 'ASBIEP');
    targetRaw.asccpNode = {manifestId: 1};
    const source = new BieUpliftSourceFlatNode(sourceRaw as BieFlatNode);
    const target = new BieUpliftTargetFlatNode(targetRaw as BieFlatNode);
    source.target = target;
    target.source = source;

    const open = vi.fn().mockReturnValue({afterClosed: () => of(undefined)});
    component.dialog = {open};
    component.hasMappedParentPair = vi.fn(() => true);
    component.sourceLibraryId = 3;
    component.targetReleaseId = 4;
    component.topLevelAsbiepId = 5;

    component.matchReused(target);

    expect(open.mock.calls[0][1].data).not.toHaveProperty('basedTopLevelAsbiepId');
  });

  it('hides source checkboxes for descendants mapped by a selected reuse BIE', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceReuse = createNode(1, 'ASBIEP');
    const source = createNode(2, 'BBIEP', sourceReuse);
    const targetReuse = createNode(1, 'ASBIEP');
    const target = createNode(2, 'BBIEP', targetReuse);
    sourceReuse.target = targetReuse;
    targetReuse.source = sourceReuse;
    source.target = target;
    (component as any).mappedTargetBySource = new Map([[sourceReuse, targetReuse]]);
    targetReuse.reusedTopLevelAsbiepId = 99;

    expect(component.isSourceNodeCheckable(source as unknown as BieUpliftSourceFlatNode)).toBe(false);

    targetReuse.reusedTopLevelAsbiepId = undefined;
    expect(component.isSourceNodeCheckable(source as unknown as BieUpliftSourceFlatNode)).toBe(true);

  });

  it('clears manual descendant mappings when a reuse BIE is selected', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = new BieUpliftSourceFlatNode(createNode(0, 'ASBIEP') as unknown as BieFlatNode);
    const sourceReuse = new BieUpliftSourceFlatNode(createNode(1, 'ASBIEP', (sourceRoot as any)._node) as unknown as BieFlatNode);
    const sourceChild = new BieUpliftSourceFlatNode(createNode(2, 'BBIEP', (sourceReuse as any)._node) as unknown as BieFlatNode);
    const targetRoot = new BieUpliftTargetFlatNode(createNode(0, 'ASBIEP') as unknown as BieFlatNode);
    const targetReuse = new BieUpliftTargetFlatNode(createNode(1, 'ASBIEP', (targetRoot as any)._node) as unknown as BieFlatNode);
    const targetManualChild = new BieUpliftTargetFlatNode(
      createNode(2, 'BBIEP', (targetReuse as any)._node) as unknown as BieFlatNode);
    const targetChild = new BieUpliftTargetFlatNode(createNode(2, 'BBIEP', (targetReuse as any)._node) as unknown as BieFlatNode);

    sourceRoot.children = [sourceReuse];
    sourceReuse.children = [sourceChild];
    targetRoot.children = [targetReuse];
    targetReuse.children = [targetManualChild, targetChild];
    sourceReuse.target = targetReuse;
    targetReuse.source = sourceReuse;
    sourceChild.target = targetManualChild;
    targetManualChild.source = sourceChild;

    component.sourceDataSource = {
      data: [sourceRoot],
      expand: vi.fn(),
      dataChange: {next: vi.fn()}
    } as unknown as BieFlatNodeDataSource<BieUpliftSourceFlatNode>;
    component.targetDataSource = {
      data: [targetRoot],
      isExpanded: vi.fn(() => false),
      expand: vi.fn((node: BieUpliftTargetFlatNode) => node.children = [targetChild]),
      dataChange: {next: vi.fn()}
    } as unknown as BieFlatNodeDataSource<BieUpliftTargetFlatNode>;

    (component as any).registerMapping(sourceReuse, targetReuse);
    (component as any).registerMapping(sourceChild, targetManualChild);
    (component as any).applyReuseSelection(targetReuse, 99, new BieEditAbieNode());

    expect(sourceChild.target).toBeUndefined();
    expect(targetChild.source).toBeUndefined();
    expect(targetReuse.reusedTopLevelAsbiepId).toBe(99);
    expect(targetManualChild.source).toBeUndefined();
    expect(component.isSourceNodeCheckable(sourceChild)).toBe(false);
    expect(component.isTargetNodeCheckable(targetChild)).toBe(false);
  });

  it('keeps the selected reuse association checkable while hiding its descendants', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const targetRoot = new BieUpliftTargetFlatNode(createNode(0, 'ASBIEP') as unknown as BieFlatNode);
    const targetReuseRaw = createNode(1, 'ASBIEP', (targetRoot as any)._node);
    const targetChildRaw = createNode(2, 'BBIEP', targetReuseRaw);
    const targetReuse = new BieUpliftTargetFlatNode(targetReuseRaw as unknown as BieFlatNode);
    const targetChild = new BieUpliftTargetFlatNode(targetChildRaw as unknown as BieFlatNode);
    targetRoot.children = [targetReuse];
    targetReuse.children = [targetChild];
    targetReuse.reusedTopLevelAsbiepId = 256;

    (component as any).targetWrapperByRawNode = new Map([[targetReuseRaw, targetReuse]]);

    expect(component.isTargetNodeCheckable(targetReuse)).toBe(true);
    expect(component.isTargetNodeCheckable(targetChild)).toBe(false);

    component.sourceSelectedNode = targetChild as unknown as BieUpliftSourceFlatNode;
    (component as any).hasMappedParentPair = vi.fn(() => true);
    const attachMapping = vi.spyOn(component as any, 'attachMapping');
    component.checkMatch({}, targetChild);
    expect(attachMapping).not.toHaveBeenCalled();
  });

  it('shows persisted inline and inherited children of a selected reuse without loading unused CC subtrees', () => {
    const database = Object.create(BieUpliftTargetFlatNodeDatabase.prototype) as BieUpliftTargetFlatNodeDatabase<any>;
    const selectedReuse = createNode(1, 'ASBIEP');
    selectedReuse.reused = true;
    (selectedReuse as any).topLevelAsbiepId = 256;
    const usedField = createNode(2, 'BBIEP', selectedReuse);
    const inlineParty = createNode(2, 'ASBIEP', selectedReuse);
    const inheritedField = createNode(2, 'BBIEP', selectedReuse);
    inheritedField.used = false;
    (inheritedField as any).inherited = true;
    const unusedField = createNode(2, 'BBIEP', selectedReuse);
    unusedField.used = false;
    const unusedAssociation = createNode(2, 'ASBIEP', selectedReuse);
    unusedAssociation.used = false;
    unusedAssociation.expandable = true;
    // A CC graph can lead back to this association. Its subtree must remain
    // unloaded: filtering a selected BIE uses persisted usage, not CC reachability.
    const loadChildren = vi.spyOn(database, 'loadChildren').mockImplementation(node => {
      node.children = [node];
    });
    const probeDescendants = vi.spyOn(database, 'hasUsedOrInheritedDescendant');
    const children = [usedField, inlineParty, inheritedField, unusedField, unusedAssociation] as unknown as BieFlatNode[];
    const baseChildren = vi.spyOn(BieFlatNodeDatabase.prototype, 'children').mockReturnValue(children);

    try {
      expect(database.children(selectedReuse as unknown as BieFlatNode).map(node => node.self))
        .toEqual([usedField, inlineParty, inheritedField]);
      expect(inlineParty.reused).toBe(false);
      expect(unusedAssociation.children).toEqual([]);
      expect(loadChildren).not.toHaveBeenCalled();
      expect(probeDescendants).not.toHaveBeenCalled();

      // The same policy applies below an inline association in the reference.
      const partyField = createNode(3, 'BBIEP', inlineParty);
      const unusedPartyField = createNode(3, 'BBIEP', inlineParty);
      unusedPartyField.used = false;
      baseChildren.mockReturnValue([partyField, unusedPartyField] as unknown as BieFlatNode[]);
      expect(database.children(inlineParty as unknown as BieFlatNode).map(node => node.self))
        .toEqual([partyField]);
      expect(loadChildren).not.toHaveBeenCalled();
      expect(probeDescendants).not.toHaveBeenCalled();
    } finally {
      baseChildren.mockRestore();
      loadChildren.mockRestore();
      probeDescendants.mockRestore();
    }
  });

  it('retains unused mapping candidates outside a selected reuse', () => {
    const database = Object.create(BieUpliftTargetFlatNodeDatabase.prototype) as BieUpliftTargetFlatNodeDatabase<any>;
    const regularTarget = createNode(1, 'ASBIEP');
    const usedChild = createNode(2, 'BBIEP', regularTarget);
    const unusedChild = createNode(2, 'ASBIEP', regularTarget);
    unusedChild.used = false;
    const children = [usedChild, unusedChild] as unknown as BieFlatNode[];
    const baseChildren = vi.spyOn(BieFlatNodeDatabase.prototype, 'children').mockReturnValue(children);

    try {
      expect(database.children(regularTarget as unknown as BieFlatNode).map(node => node.self)).toEqual(children);
    } finally {
      baseChildren.mockRestore();
    }
  });

  it.each([
    ['source', BieUpliftSourceFlatNodeDatabase, BieUpliftSourceFlatNode],
    ['target', BieUpliftTargetFlatNodeDatabase, BieUpliftTargetFlatNode]
  ])('loads %s uplift children from the raw node before wrapping them',
    (_side, DatabaseType, WrapperType) => {
      const database = Object.create(DatabaseType.prototype) as BieFlatNodeDatabase<any>;
      const rawNode = createNode(1, 'BBIEP') as unknown as BieFlatNode;
      const wrappedNode = new WrapperType(rawNode);
      const baseLoadChildren = vi.spyOn(BieFlatNodeDatabase.prototype as any, 'loadChildren')
        .mockImplementation((node: BieFlatNode) => {
          node.children = [createNode(2, 'BBIE_SC', node as any) as unknown as BieFlatNode];
        });

      database.loadChildren(wrappedNode as any);

      expect(baseLoadChildren).toHaveBeenCalledWith(rawNode);
      expect(wrappedNode.children).toHaveLength(1);
      expect(wrappedNode.children[0]).toBeInstanceOf(WrapperType);
      baseLoadChildren.mockRestore();
    });

  it.each([
    ['source', BieUpliftSourceFlatNodeDatabase, BieUpliftSourceFlatNode],
    ['target', BieUpliftTargetFlatNodeDatabase, BieUpliftTargetFlatNode]
  ])('keeps %s children wrapped when the flattened database returns raw nodes',
    (_side, DatabaseType, WrapperType) => {
      const database = Object.create(DatabaseType.prototype) as BieFlatNodeDatabase<any>;
      const rawParent = createNode(0, 'ABIE') as unknown as BieFlatNode;
      const rawChild = createNode(1, 'BBIEP', rawParent) as unknown as BieFlatNode;
      const baseChildren = vi.spyOn(BieFlatNodeDatabase.prototype as any, 'children')
        .mockReturnValue([rawChild]);

      const children = database.children(new WrapperType(rawParent) as any);

      expect(children).toHaveLength(1);
      expect(children[0]).toBeInstanceOf(WrapperType);
      expect(children[0].self).toBe(rawChild);
      baseChildren.mockRestore();
    });

  it('preserves persisted BIE-Sc usage and paths for wrapped source and target nodes', () => {
    const parent = {path: 'ASCCP-1>ACC-2'} as BieFlatNode;
    const bccNode = {type: 'BCC', manifestId: 100, state: 'Published', entityType: 'Element',
      cardinalityMin: 0, cardinalityMax: 1, deprecated: false} as any;
    const bccpNode = {type: 'BCCP', manifestId: 200, state: 'Published', deprecated: false} as any;
    const bdtNode = {type: 'DT', manifestId: 300, state: 'Published', deprecated: false} as any;
    const bdtScNode = {type: 'DT_SC', manifestId: 400, propertyTerm: 'Scheme Agency',
      representationTerm: 'Identifier', state: 'Published', cardinalityMin: 0, cardinalityMax: 1,
      deprecated: false} as any;
    const graph = {
      graph: {
        nodes: {
          'BCC-100': bccNode,
          'BCCP-200': bccpNode,
          'DT-300': bdtNode,
          'DT_SC-400': bdtScNode
        },
        edges: {
          'BCC-100': {targets: ['BCCP-200']},
          'BCCP-200': {targets: ['DT-300']},
          'DT-300': {targets: ['DT_SC-400']}
        }
      }
    } as any;
    const rawParent = new BbiepFlatNode();
    rawParent.name = 'Identifier';
    rawParent.level = 1;
    rawParent.parent = parent;
    rawParent.topLevelAsbiepId = 10;
    rawParent.bccNode = bccNode;
    rawParent.bccpNode = bccpNode;
    rawParent.bdtNode = bdtNode;
    (rawParent as any)._used = true;
    const persistedPath = [rawParent.path, 'DT_SC-400'].join('>');
    const usedBbieSc = {
      bieId: 123,
      cardinalityMax: 1,
      cardinalityMin: 0,
      hashPath: sha256(persistedPath),
      manifestId: 400,
      ownerTopLevelAsbiepId: 10,
      type: 'BBIE_SC',
      used: true
    } as any;

    const sourceDatabase = new BieUpliftSourceFlatNodeDatabase(graph, {} as any, 10, [usedBbieSc], []);
    const sourceParent = new BieUpliftSourceFlatNode(rawParent);
    sourceDatabase.loadChildren(sourceParent as any);
    const sourceChild = sourceParent.children[0];

    expect(sourceChild.name).toBe('Scheme Agency Identifier');
    expect(sourceChild.used).toBe(true);
    expect(sourceChild.bieId).toBe(123);
    expect(sourceChild._node.parent).toBe(rawParent);
    expect(sourceChild._node.hashPath).toBe(sha256(persistedPath));

    rawParent.children = [];
    const targetDatabase = new BieUpliftTargetFlatNodeDatabase(graph, {} as any, 10, [usedBbieSc], []);
    const targetParent = new BieUpliftTargetFlatNode(rawParent);
    targetDatabase.loadChildren(targetParent as any);
    const targetChild = targetParent.children[0];

    expect(targetChild.path).toBe(persistedPath);
    expect(targetChild.used).toBe(true);
    expect(targetChild._node.parent).toBe(rawParent);
  });

  it('marks source descendants used by an inherited base before hiding unused nodes', () => {
    const parent = {path: 'ASCCP-1>ACC-2'} as BieFlatNode;
    const bccNode = {type: 'BCC', manifestId: 100, state: 'Published', entityType: 'Element',
      cardinalityMin: 0, cardinalityMax: 1, deprecated: false} as any;
    const bccpNode = {type: 'BCCP', manifestId: 200, state: 'Published', deprecated: false} as any;
    const bdtNode = {type: 'DT', manifestId: 300, state: 'Published', deprecated: false} as any;
    const bdtScNode = {type: 'DT_SC', manifestId: 400, propertyTerm: 'Scheme Agency',
      representationTerm: 'Identifier', state: 'Published', cardinalityMin: 0, cardinalityMax: 1,
      deprecated: false} as any;
    const graph = {
      graph: {
        nodes: {
          'BCC-100': bccNode,
          'BCCP-200': bccpNode,
          'DT-300': bdtNode,
          'DT_SC-400': bdtScNode
        },
        edges: {
          'BCC-100': {targets: ['BCCP-200']},
          'BCCP-200': {targets: ['DT-300']},
          'DT-300': {targets: ['DT_SC-400']}
        }
      }
    } as any;
    const rawParent = new BbiepFlatNode();
    rawParent.name = 'Identifier';
    rawParent.level = 1;
    rawParent.parent = parent;
    rawParent.topLevelAsbiepId = 10;
    rawParent.basedTopLevelAsbiepId = 20;
    rawParent.bccNode = bccNode;
    rawParent.bccpNode = bccpNode;
    rawParent.bdtNode = bdtNode;
    (rawParent as any)._used = true;
    const persistedPath = [rawParent.path, 'DT_SC-400'].join('>');
    const baseUsedBieSc = {
      bieId: 123,
      cardinalityMax: 1,
      cardinalityMin: 0,
      hashPath: sha256(persistedPath),
      manifestId: 400,
      ownerTopLevelAsbiepId: 20,
      type: 'BBIE_SC',
      used: true
    } as any;

    const database = new BieUpliftSourceFlatNodeDatabase(graph, {} as any, 10, [], []);
    database.setBaseUsedBieList([baseUsedBieSc]);
    const dataSource = new BieFlatNodeDataSource(database, null as any, null as any);
    dataSource.hideUnused = true;
    const wrappedParent = new BieUpliftSourceFlatNode(rawParent);
    database.loadChildren(wrappedParent as any);

    const child = wrappedParent.children[0];
    expect(child.inherited).toBe(true);
    expect(child.basedTopLevelAsbiepId).toBe(20);
    expect(wrappedParent.children).toHaveLength(1);
  });

  it('serializes the selected reuse BIE reference into the uplift request', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const target = {
      path: '/BOM/BOM Item Data',
      parents: [],
      reusedTopLevelAsbiepId: 17,
      _node: {asccNode: {manifestId: 20}}
    };
    const source = {
      level: 1,
      reused: true,
      locked: false,
      type: 'ASBIEP',
      bieId: 15,
      path: '/BOM/BOM Item Data',
      upliftPath: '/BOM/BOM Item Data',
      _node: {asccNode: {manifestId: 10}},
      target
    };
    const createUpliftBie = vi.fn(() => of({topLevelAsbiepId: 99}));
    (component as any).sourceDataSource = {data: [source]};
    (component as any).targetDataSource = {data: []};
    (component as any).bieUpliftService = {createUpliftBie};
    (component as any).auth = {getUserToken: vi.fn(() => 'test-token')};
    (component as any).router = {navigateByUrl: vi.fn()};
    component.topLevelAsbiepId = 1;
    component.targetAsccpManifestId = 2;

    component.createUpliftBIE();

    expect(createUpliftBie).toHaveBeenCalledWith(
      1,
      2,
      [expect.objectContaining({refTopLevelAsbiepId: 17})]
    );
  });

  it('TC_29_1_TA_7 (M33/V19) serializes an unmatched choice as a negative override', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const raw = createNode(1, 'BBIEP');
    (raw as any).bccNode = {manifestId: 10};
    const source = new BieUpliftSourceFlatNode(raw as unknown as BieFlatNode);
    const originalTarget = {} as BieUpliftTargetFlatNode;
    source.target = originalTarget;
    source.systemTarget = originalTarget;
    source.fixed = true;
    originalTarget.source = source;
    (component as any).detachTargetMappings(originalTarget);
    const createUpliftBie = vi.fn(() => of({topLevelAsbiepId: 99}));
    (component as any).sourceDataSource = {data: [source]};
    (component as any).targetDataSource = {data: []};
    (component as any).bieUpliftService = {createUpliftBie};
    (component as any).auth = {getUserToken: vi.fn(() => 'test-token')};
    (component as any).router = {navigateByUrl: vi.fn()};
    component.topLevelAsbiepId = 1;
    component.targetAsccpManifestId = 2;

    component.createUpliftBIE();

    expect(createUpliftBie.mock.calls[0][2]).toEqual([
      expect.objectContaining({
        sourcePath: source.upliftPath,
        targetPath: undefined,
        suppressAutoMapping: true
      })
    ]);
  });

  it('TC_29_1_TA_5_e (R17) does not warn for an unmatched reuse association', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const unmatchedRaw = createNode(1, 'ASBIEP');
    unmatchedRaw.reused = true;
    const mappedRaw = createNode(1, 'ASBIEP');
    mappedRaw.reused = true;
    const mappedTarget = createNode(1, 'ASBIEP') as unknown as BieUpliftTargetFlatNode;
    (mappedTarget as any).reusedTopLevelAsbiepId = undefined;
    const unmatched = new BieUpliftSourceFlatNode(unmatchedRaw as unknown as BieFlatNode);
    const mapped = new BieUpliftSourceFlatNode(mappedRaw as unknown as BieFlatNode);
    mapped.target = mappedTarget;
    (component as any).sourceDataSource = {data: [unmatched, mapped]};

    expect((component as any).collectUnselectedReuseNodes()).toEqual([mapped]);
  });

  it('uses the full uplift path for descendants of an unselected reuse', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ABIE');
    (sourceRoot as any).path = 'ASCCP-1>ACC-2';
    const sourceReuse = createNode(1, 'ASBIEP', sourceRoot);
    sourceReuse.reused = true;
    (sourceReuse as any).asccNode = {manifestId: 3};
    (sourceReuse as any).asccpNode = {manifestId: 4};
    (sourceReuse as any).accNode = {manifestId: 9};
    const sourceChild = createNode(2, 'BBIEP', sourceReuse);
    (sourceChild as any).bccNode = {manifestId: 5};
    (sourceChild as any).bccpNode = {manifestId: 6};
    (sourceChild as any).bdtNode = {manifestId: 7};
    const sourceChildSc = createNode(3, 'BBIE_SC', sourceChild);
    (sourceChildSc as any).bdtScNode = {manifestId: 8};

    const wrappedChild = new BieUpliftSourceFlatNode(sourceChild as unknown as BieFlatNode);
    const wrappedChildSc = new BieUpliftSourceFlatNode(sourceChildSc as unknown as BieFlatNode);

    expect(wrappedChild.upliftPath).toBe('ASCCP-1>ACC-2>ASCC-3>ASCCP-4>ACC-9>BCC-5');
    expect(wrappedChildSc.upliftPath).toBe(
      'ASCCP-1>ACC-2>ASCC-3>ASCCP-4>ACC-9>BCC-5>BCCP-6>DT-7>DT_SC-8');
  });

  it('restores the occurrence prefix when a reused subtree has its own ABIE node', () => {
    const sourceRoot = createNode(0, 'ABIE');
    (sourceRoot as any).path = 'ASCCP-1>ACC-2';
    const sourceReuse = createNode(1, 'ASBIEP', sourceRoot);
    sourceReuse.reused = true;
    (sourceReuse as any).asccNode = {manifestId: 3};
    (sourceReuse as any).asccpNode = {manifestId: 4};
    (sourceReuse as any).accNode = {manifestId: 9};
    const reusedAbie = createNode(2, 'ABIE', sourceReuse);
    (reusedAbie as any).path = 'ASCCP-4>ACC-9';
    const sourceChild = createNode(3, 'BBIEP', reusedAbie);
    (sourceChild as any).bccNode = {manifestId: 5};
    (sourceChild as any).bccpNode = {manifestId: 6};
    (sourceChild as any).bdtNode = {manifestId: 7};
    const sourceChildSc = createNode(4, 'BBIE_SC', sourceChild);
    (sourceChildSc as any).bdtScNode = {manifestId: 8};

    expect(new BieUpliftSourceFlatNode(sourceChild as unknown as BieFlatNode).upliftPath)
      .toBe('ASCCP-1>ACC-2>ASCC-3>ASCCP-4>ACC-9>BCC-5');
    expect(new BieUpliftSourceFlatNode(sourceChildSc as unknown as BieFlatNode).upliftPath)
      .toBe('ASCCP-1>ACC-2>ASCC-3>ASCCP-4>ACC-9>BCC-5>BCCP-6>DT-7>DT_SC-8');
  });

  it('TC_29_1_TA_5_d (P25): serializes a reused target with its canonical occurrence path', () => {
    const component = Object.create(BieUpliftComponent.prototype) as any;
    const targetRoot = createNode(0, 'ABIE');
    (targetRoot as any).path = 'ASCCP-1>ACC-2';
    const targetReuse = createNode(1, 'ASBIEP', targetRoot);
    targetReuse.reused = true;
    (targetReuse as any).asccNode = {manifestId: 3};
    (targetReuse as any).asccpNode = {manifestId: 4};
    (targetReuse as any).accNode = {manifestId: 9};
    const targetChild = createNode(2, 'BBIEP', targetReuse);
    (targetChild as any).bccNode = {manifestId: 5};
    (targetChild as any).bccpNode = {manifestId: 6};
    (targetChild as any).bdtNode = {manifestId: 7};

    const wrappedTarget = new BieUpliftTargetFlatNode(targetChild as unknown as BieFlatNode);

    expect(component.targetRequestPath(wrappedTarget)).toBe(
      'ASCCP-1>ACC-2>ASCC-3>ASCCP-4>ACC-9>BCC-5');
  });

  it('TC_29_1_TA_5_d (P25): report validation uses the same canonical reused target path', () => {
    const sourceRoot = createNode(0, 'ABIE');
    (sourceRoot as any).path = 'ASCCP-1>ACC-2';
    const source = createNode(1, 'BBIEP', sourceRoot);
    (source as any).bccNode = {manifestId: 5};
    const targetRoot = createNode(0, 'ABIE');
    (targetRoot as any).path = 'ASCCP-1>ACC-2';
    const targetReuse = createNode(1, 'ASBIEP', targetRoot);
    targetReuse.reused = true;
    (targetReuse as any).asccNode = {manifestId: 3};
    (targetReuse as any).asccpNode = {manifestId: 4};
    (targetReuse as any).accNode = {manifestId: 9};
    const target = createNode(2, 'BBIEP', targetReuse);
    (target as any).bccNode = {manifestId: 5};
    (target as any).bccpNode = {manifestId: 6};
    (target as any).bdtNode = {manifestId: 7};

    const sourceWrapper = new BieUpliftSourceFlatNode(source as unknown as BieFlatNode);
    sourceWrapper.target = new BieUpliftTargetFlatNode(target as unknown as BieFlatNode);

    expect(new MatchInfo(sourceWrapper).targetPath).toBe(
      'ASCCP-1>ACC-2>ASCC-3>ASCCP-4>ACC-9>BCC-5');
  });

  it('keeps intermediate account nodes in the uplift path', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ABIE');
    (sourceRoot as any).path = 'ASCCP-1>ACC-2';
    const sourceReuse = createNode(1, 'ASBIEP', sourceRoot);
    (sourceReuse as any).asccNode = {manifestId: 3};
    (sourceReuse as any).asccpNode = {manifestId: 4};
    (sourceReuse as any).accNode = {manifestId: 9};
    (sourceReuse as any).intermediateAccNodes = [{type: 'ACC', manifestId: 10}];
    const sourceChild = createNode(2, 'BBIEP', sourceReuse);
    (sourceChild as any).bccNode = {manifestId: 5};
    (sourceChild as any).intermediateAccNodes = [{type: 'ACC', manifestId: 11}];

    const wrappedReuse = new BieUpliftSourceFlatNode(sourceReuse as unknown as BieFlatNode);
    const wrappedChild = new BieUpliftSourceFlatNode(sourceChild as unknown as BieFlatNode);

    expect(wrappedReuse.upliftPath).toBe('ASCCP-1>ACC-2>ACC-10>ASCC-3');
    expect(wrappedChild.upliftPath).toBe(
      'ASCCP-1>ACC-2>ACC-10>ASCC-3>ASCCP-4>ACC-9>ACC-11>BCC-5');
  });

  it('does not duplicate an account already present at an uplift path boundary', () => {
    const sourceRoot = createNode(0, 'ABIE');
    (sourceRoot as any).path = 'ASCCP-1>ACC-2';
    const sourceReuse = createNode(1, 'ASBIEP', sourceRoot);
    (sourceReuse as any).asccNode = {manifestId: 3};
    (sourceReuse as any).asccpNode = {manifestId: 4};
    (sourceReuse as any).accNode = {manifestId: 2};
    (sourceReuse as any).intermediateAccNodes = [{type: 'ACC', manifestId: 2}];

    expect(new BieUpliftSourceFlatNode(sourceReuse as unknown as BieFlatNode).upliftPath)
      .toBe('ASCCP-1>ACC-2>ASCC-3');
  });

  it('serializes manual mappings below an unselected reuse with uplift paths', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ABIE');
    (sourceRoot as any).path = 'ASCCP-1>ACC-2';
    const sourceReuse = createNode(1, 'ASBIEP', sourceRoot);
    sourceReuse.reused = true;
    (sourceReuse as any).asccNode = {manifestId: 3};
    (sourceReuse as any).asccpNode = {manifestId: 4};
    (sourceReuse as any).accNode = {manifestId: 9};
    const sourceParty = createNode(2, 'ASBIEP', sourceReuse);
    sourceParty.locked = true;
    (sourceParty as any).asccNode = {manifestId: 10};
    (sourceParty as any).asccpNode = {manifestId: 11};
    (sourceParty as any).accNode = {manifestId: 12};
    const sourceIdentifier = createNode(3, 'ASBIEP', sourceParty);
    sourceIdentifier.locked = true;
    (sourceIdentifier as any).asccNode = {manifestId: 13};
    (sourceIdentifier as any).asccpNode = {manifestId: 14};
    (sourceIdentifier as any).accNode = {manifestId: 15};
    const sourceTypeCode = createNode(3, 'BBIEP', sourceParty);
    sourceTypeCode.locked = true;
    (sourceTypeCode as any).bccNode = {manifestId: 16};
    const sourceSchemeAgencyIdentifier = createNode(3, 'BBIEP', sourceParty);
    sourceSchemeAgencyIdentifier.locked = true;
    (sourceSchemeAgencyIdentifier as any).bccNode = {manifestId: 19};

    const makeTarget = (type: string, path: string, manifestId: number): BieUpliftTargetFlatNode => {
      const target = {
        path,
        parents: [],
        reusedTopLevelAsbiepId: undefined,
        _node: {asccNode: {manifestId}, bccNode: {manifestId}}
      };
      if (type === 'ASBIEP') {
        target._node.asccNode = {manifestId};
      } else {
        target._node.bccNode = {manifestId};
      }
      return target as unknown as BieUpliftTargetFlatNode;
    };
    const wrappedSourceReuse = new BieUpliftSourceFlatNode(sourceReuse as unknown as BieFlatNode);
    const wrappedParty = new BieUpliftSourceFlatNode(sourceParty as unknown as BieFlatNode);
    const wrappedIdentifier = new BieUpliftSourceFlatNode(sourceIdentifier as unknown as BieFlatNode);
    const wrappedTypeCode = new BieUpliftSourceFlatNode(sourceTypeCode as unknown as BieFlatNode);
    const wrappedSchemeAgencyIdentifier = new BieUpliftSourceFlatNode(
      sourceSchemeAgencyIdentifier as unknown as BieFlatNode);
    wrappedParty.target = makeTarget('ASBIEP', 'TARGET>ACCOUNT', 101);
    wrappedIdentifier.target = makeTarget('ASBIEP', 'TARGET>ACCOUNT>IDENTIFIER', 102);
    wrappedTypeCode.target = makeTarget('BBIEP', 'TARGET>ACCOUNT>ACTION', 201);
    wrappedSchemeAgencyIdentifier.target = makeTarget('BBIEP', 'TARGET>ACCOUNT>SCHEME', 202);

    const createUpliftBie = vi.fn((_topLevelAsbiepId: number, _targetAsccpManifestId: number,
                                  _matched: unknown[]) => of({topLevelAsbiepId: 99}));
    (component as any).sourceDataSource = {
      data: [wrappedSourceReuse, wrappedParty, wrappedIdentifier, wrappedTypeCode, wrappedSchemeAgencyIdentifier]
    };
    (component as any).targetDataSource = {data: []};
    (component as any).bieUpliftService = {createUpliftBie};
    (component as any).auth = {getUserToken: vi.fn(() => 'test-token')};
    (component as any).router = {navigateByUrl: vi.fn()};
    component.topLevelAsbiepId = 1;
    component.targetAsccpManifestId = 2;

    component.createUpliftBIE();

    const mappings = createUpliftBie.mock.calls[0][2];
    expect(mappings).toEqual(expect.arrayContaining([
      expect.objectContaining({
        sourcePath: wrappedIdentifier.upliftPath,
        targetPath: 'TARGET>ACCOUNT>IDENTIFIER'
      }),
      expect.objectContaining({
        sourcePath: wrappedTypeCode.upliftPath,
        targetPath: 'TARGET>ACCOUNT>ACTION'
      }),
      expect.objectContaining({
        sourcePath: wrappedSchemeAgencyIdentifier.upliftPath,
        targetPath: 'TARGET>ACCOUNT>SCHEME'
      })
    ]));
  });

  it('serializes a selected nested reuse beside a custom mapping under another unselected reuse', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ABIE');
    (sourceRoot as any).path = 'ASCCP-1>ACC-2';

    const selectedReuse = createNode(1, 'ASBIEP', sourceRoot);
    selectedReuse.reused = true;
    (selectedReuse as any).asccNode = {manifestId: 3};
    (selectedReuse as any).asccpNode = {manifestId: 4};
    (selectedReuse as any).accNode = {manifestId: 5};

    const customReuse = createNode(1, 'ASBIEP', sourceRoot);
    customReuse.reused = true;
    (customReuse as any).asccNode = {manifestId: 6};
    (customReuse as any).asccpNode = {manifestId: 7};
    (customReuse as any).accNode = {manifestId: 8};
    const customChild = createNode(2, 'BBIEP', customReuse);
    customChild.locked = true;
    (customChild as any).bccNode = {manifestId: 9};

    const wrappedRoot = new BieUpliftSourceFlatNode(sourceRoot as unknown as BieFlatNode);
    const wrappedSelectedReuse = new BieUpliftSourceFlatNode(selectedReuse as unknown as BieFlatNode);
    const wrappedCustomReuse = new BieUpliftSourceFlatNode(customReuse as unknown as BieFlatNode);
    const wrappedCustomChild = new BieUpliftSourceFlatNode(customChild as unknown as BieFlatNode);
    wrappedRoot.children = [wrappedSelectedReuse, wrappedCustomReuse];
    wrappedCustomReuse.children = [wrappedCustomChild];

    const selectedTarget = {
      path: 'TARGET>SELECTED',
      parents: [],
      reusedTopLevelAsbiepId: 44,
      _node: {asccNode: {manifestId: 14}}
    } as unknown as BieUpliftTargetFlatNode;
    const customTarget = {
      path: 'TARGET>CUSTOM>FIELD',
      parents: [],
      _node: {bccNode: {manifestId: 19}}
    } as unknown as BieUpliftTargetFlatNode;
    wrappedSelectedReuse.target = selectedTarget;
    selectedTarget.source = wrappedSelectedReuse;
    wrappedCustomChild.target = customTarget;

    const createUpliftBie = vi.fn((_topLevelAsbiepId: number, _targetAsccpManifestId: number,
                                  _matched: unknown[]) => of({topLevelAsbiepId: 99}));
    (component as any).sourceDataSource = {data: [wrappedRoot]};
    (component as any).targetDataSource = {data: []};
    (component as any).bieUpliftService = {createUpliftBie};
    (component as any).auth = {getUserToken: vi.fn(() => 'test-token')};
    (component as any).router = {navigateByUrl: vi.fn()};
    component.topLevelAsbiepId = 1;
    component.targetAsccpManifestId = 2;

    component.createUpliftBIE();

    const mappings = createUpliftBie.mock.calls[0][2];
    expect(mappings).toEqual(expect.arrayContaining([
      expect.objectContaining({
        targetPath: 'TARGET>SELECTED',
        refTopLevelAsbiepId: 44
      }),
      expect.objectContaining({
        sourcePath: wrappedCustomChild.upliftPath,
        targetPath: 'TARGET>CUSTOM>FIELD'
      })
    ]));
  });

  it('serializes a manual mapping even when the source child is locked by a reuse parent', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ABIE');
    (sourceRoot as any).path = 'ASCCP-1>ACC-2';
    const sourceReuse = createNode(1, 'ASBIEP', sourceRoot);
    sourceReuse.reused = true;
    (sourceReuse as any).asccNode = {manifestId: 3};
    (sourceReuse as any).asccpNode = {manifestId: 4};
    (sourceReuse as any).accNode = {manifestId: 9};

    const sourceChild = createNode(2, 'BBIEP', sourceReuse);
    sourceChild.locked = true;
    (sourceChild as any).bccNode = {manifestId: 5};
    const targetChild = {
      path: 'TARGET>ACCOUNT>ACTION',
      parents: [],
      _node: {bccNode: {manifestId: 6}}
    };
    const wrappedSourceRoot = new BieUpliftSourceFlatNode(sourceRoot as unknown as BieFlatNode);
    const wrappedSourceReuse = new BieUpliftSourceFlatNode(sourceReuse as unknown as BieFlatNode);
    const wrappedSourceChild = new BieUpliftSourceFlatNode(sourceChild as unknown as BieFlatNode);
    wrappedSourceRoot.children = [wrappedSourceReuse];
    wrappedSourceReuse.children = [wrappedSourceChild];
    wrappedSourceChild.target = targetChild as unknown as BieUpliftTargetFlatNode;

    const createUpliftBie = vi.fn((_topLevelAsbiepId: number, _targetAsccpManifestId: number,
                                  _matched: unknown[]) => of({topLevelAsbiepId: 99}));
    (component as any).sourceDataSource = {data: [wrappedSourceRoot]};
    (component as any).targetDataSource = {data: []};
    (component as any).bieUpliftService = {createUpliftBie};
    (component as any).auth = {getUserToken: vi.fn(() => 'test-token')};
    (component as any).router = {navigateByUrl: vi.fn()};
    component.topLevelAsbiepId = 1;
    component.targetAsccpManifestId = 2;

    component.createUpliftBIE();

    expect(createUpliftBie.mock.calls[0][2]).toEqual(expect.arrayContaining([
      expect.objectContaining({
        sourcePath: wrappedSourceChild.upliftPath,
        targetPath: 'TARGET>ACCOUNT>ACTION'
      })
    ]));
  });

  it('retains a manually mapped source node when its parent is collapsed', () => {
    const sourceRoot = createNode(0, 'ABIE');
    const sourceReuse = createNode(1, 'ASBIEP', sourceRoot);
    sourceReuse.reused = true;
    const sourceChild = createNode(2, 'BBIEP', sourceReuse);
    sourceChild.locked = true;
    const wrappedSourceChild = new BieUpliftSourceFlatNode(sourceChild as unknown as BieFlatNode);

    expect(wrappedSourceChild.isChanged).toBe(false);
    wrappedSourceChild.target = {} as BieUpliftTargetFlatNode;

    expect(wrappedSourceChild.isChanged).toBe(true);
  });

  it('does not discard mapped supplementary children through the real collapse path', () => {
    const sourceBbiep = createNode(0, 'BBIEP');
    sourceBbiep.expandable = true;
    const sourceBbieSc = createNode(1, 'BBIE_SC', sourceBbiep);
    sourceBbieSc.locked = true;
    const wrappedSourceBbiep = new BieUpliftSourceFlatNode(sourceBbiep as unknown as BieFlatNode);
    const wrappedSourceBbieSc = new BieUpliftSourceFlatNode(sourceBbieSc as unknown as BieFlatNode);
    (sourceBbiep as any).addChangeListener = vi.fn();
    (sourceBbiep as any).removeChangeListener = vi.fn();
    (sourceBbieSc as any).addChangeListener = vi.fn();
    (sourceBbieSc as any).removeChangeListener = vi.fn();
    wrappedSourceBbiep.children = [wrappedSourceBbieSc];
    wrappedSourceBbieSc.target = {} as BieUpliftTargetFlatNode;

    const database = {
      children: () => [wrappedSourceBbieSc]
    };
    const dataSource = new BieFlatNodeDataSource<BieUpliftSourceFlatNode>(
      database as unknown as BieFlatNodeDataSource<BieUpliftSourceFlatNode>['database'], null as any, null as any);
    dataSource.dataChange.next([wrappedSourceBbiep]);
    expect(wrappedSourceBbieSc.isChanged).toBe(true);

    dataSource.toggleNode(wrappedSourceBbiep, true);
    expect((database.children() as BieUpliftSourceFlatNode[])[0].isChanged).toBe(true);
    dataSource.toggleNode(wrappedSourceBbiep, false);

    expect(wrappedSourceBbiep.children).toEqual([wrappedSourceBbieSc]);
  });

  it('reports manual mappings below an unselected reuse even when the tree is collapsed', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ABIE');
    (sourceRoot as any).path = 'ASCCP-1>ACC-2';
    const sourceReuse = createNode(1, 'ASBIEP', sourceRoot);
    sourceReuse.reused = true;
    (sourceReuse as any).asccNode = {manifestId: 3};
    (sourceReuse as any).asccpNode = {manifestId: 4};
    (sourceReuse as any).accNode = {manifestId: 9};
    const sourceChild = createNode(2, 'BBIEP', sourceReuse);
    sourceChild.locked = true;
    (sourceChild as any).bccNode = {manifestId: 5};

    const targetRoot = createNode(0, 'ABIE');
    const targetReuse = createNode(1, 'ASBIEP', targetRoot);
    (targetReuse as any).asbiePath = 'TARGET>ACCOUNT>PARTY';
    (targetReuse as any).asccNode = {manifestId: 7};
    const targetChild = createNode(2, 'BBIEP', targetReuse);
    (targetChild as any).bbiePath = 'TARGET>ACCOUNT>ACTION';
    (targetChild as any).bccNode = {manifestId: 6};

    const wrappedSourceRoot = new BieUpliftSourceFlatNode(sourceRoot as unknown as BieFlatNode);
    const wrappedSourceReuse = new BieUpliftSourceFlatNode(sourceReuse as unknown as BieFlatNode);
    const wrappedSourceChild = new BieUpliftSourceFlatNode(sourceChild as unknown as BieFlatNode);
    const wrappedTargetRoot = new BieUpliftTargetFlatNode(targetRoot as unknown as BieFlatNode);
    const wrappedTargetReuse = new BieUpliftTargetFlatNode(targetReuse as unknown as BieFlatNode);
    const wrappedTargetChild = new BieUpliftTargetFlatNode(targetChild as unknown as BieFlatNode);
    wrappedSourceRoot.children = [wrappedSourceReuse];
    wrappedSourceReuse.children = [wrappedSourceChild];
    wrappedTargetRoot.children = [wrappedTargetReuse];
    wrappedTargetReuse.children = [wrappedTargetChild];
    wrappedSourceReuse.target = wrappedTargetReuse;
    wrappedTargetReuse.source = wrappedSourceReuse;
    wrappedSourceChild.target = wrappedTargetChild;
    wrappedTargetChild.source = wrappedSourceChild;

    const open = vi.fn().mockReturnValue({afterClosed: () => of(false)});
    (component as any).dialog = {open};
    (component as any).sourceDataSource = {data: [wrappedSourceRoot]};
    (component as any).targetDataSource = {data: [wrappedTargetRoot]};

    component.report();

    const reports = open.mock.calls[0][1].data.matches as MatchInfo[];
    expect(reports.some(report => report.match === 'Manual' && report.targetPath === 'TARGET>ACCOUNT>ACTION'))
      .toBe(true);
  });

  it('does not serialize a locked node that has no reused ancestor', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ABIE');
    const sourceChild = createNode(1, 'BBIEP', sourceRoot);
    sourceChild.locked = true;
    sourceChild.target = createNode(1, 'BBIEP') as TestNode;
    const wrappedSourceChild = new BieUpliftSourceFlatNode(sourceChild as unknown as BieFlatNode);

    expect((component as any).shouldSerializeSourceNode(wrappedSourceChild)).toBe(false);
  });

  it('does not serialize descendants already covered by a selected reuse reference', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ABIE');
    const sourceReuse = createNode(1, 'ASBIEP', sourceRoot);
    sourceReuse.reused = true;
    const sourceChild = createNode(2, 'BBIEP', sourceReuse);
    sourceChild.locked = true;
    const targetChild = createNode(2, 'BBIEP');
    targetChild.reusedTopLevelAsbiepId = 256;
    sourceChild.target = targetChild;
    const wrappedSourceChild = new BieUpliftSourceFlatNode(sourceChild as unknown as BieFlatNode);

    expect((component as any).shouldSerializeSourceNode(wrappedSourceChild)).toBe(false);
  });

  it('does not serialize a nested reused node below a selected reuse reference', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRootRaw = createNode(0, 'ABIE');
    const sourceReuseRaw = createNode(1, 'ASBIEP', sourceRootRaw);
    const nestedReuseRaw = createNode(2, 'ASBIEP', sourceReuseRaw);
    sourceReuseRaw.reused = true;
    nestedReuseRaw.reused = true;
    const sourceRoot = new BieUpliftSourceFlatNode(sourceRootRaw as unknown as BieFlatNode);
    const sourceReuse = new BieUpliftSourceFlatNode(sourceReuseRaw as unknown as BieFlatNode);
    const nestedReuse = new BieUpliftSourceFlatNode(nestedReuseRaw as unknown as BieFlatNode);
    const targetReuse = createNode(1, 'ASBIEP');
    targetReuse.reusedTopLevelAsbiepId = 42;
    sourceReuse.target = targetReuse as unknown as BieUpliftTargetFlatNode;

    (component as any).sourceDataSource = {data: [sourceRoot, sourceReuse, nestedReuse]};

    expect((component as any).shouldSerializeSourceNode(nestedReuse)).toBe(false);
  });

  it('omits selected reuse descendants from the serialized uplift request', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ABIE');
    const sourceReuse = createNode(1, 'ASBIEP', sourceRoot);
    sourceReuse.reused = true;
    (sourceReuse as any).asccNode = {manifestId: 3};
    (sourceReuse as any).asccpNode = {manifestId: 4};
    (sourceReuse as any).accNode = {manifestId: 9};
    const nestedReuse = createNode(2, 'ASBIEP', sourceReuse);
    nestedReuse.reused = true;
    (nestedReuse as any).asccNode = {manifestId: 10};
    (nestedReuse as any).asccpNode = {manifestId: 11};
    (nestedReuse as any).accNode = {manifestId: 12};

    const targetReuse = {
      path: 'TARGET>REUSE',
      parents: [],
      reusedTopLevelAsbiepId: 42,
      _node: {asccNode: {manifestId: 13}}
    } as unknown as BieUpliftTargetFlatNode;
    const targetNestedReuse = {
      path: 'TARGET>REUSE>NESTED',
      parents: [],
      reusedTopLevelAsbiepId: 42,
      _node: {asccNode: {manifestId: 14}}
    } as unknown as BieUpliftTargetFlatNode;
    const wrappedSourceReuse = new BieUpliftSourceFlatNode(sourceReuse as unknown as BieFlatNode);
    const wrappedNestedReuse = new BieUpliftSourceFlatNode(nestedReuse as unknown as BieFlatNode);
    wrappedSourceReuse.target = targetReuse;
    wrappedNestedReuse.target = targetNestedReuse;

    const createUpliftBie = vi.fn((_topLevelAsbiepId: number, _targetAsccpManifestId: number,
                                  _matched: unknown[]) => of({topLevelAsbiepId: 99}));
    (component as any).sourceDataSource = {data: [wrappedSourceReuse, wrappedNestedReuse]};
    (component as any).targetDataSource = {data: []};
    (component as any).bieUpliftService = {createUpliftBie};
    (component as any).auth = {getUserToken: vi.fn(() => 'test-token')};
    (component as any).router = {navigateByUrl: vi.fn()};
    component.topLevelAsbiepId = 1;
    component.targetAsccpManifestId = 2;

    component.createUpliftBIE();

    expect(createUpliftBie.mock.calls[0][2]).toEqual([
      expect.objectContaining({
        sourcePath: wrappedSourceReuse.upliftPath,
        targetPath: 'TARGET>REUSE',
        refTopLevelAsbiepId: 42
      })
    ]);
  });

  it('clears descendant mappings when a mapped parent is replaced', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ASBIEP');
    const sourceParent = createNode(1, 'ASBIEP', sourceRoot);
    const sourceChild = createNode(2, 'BBIEP', sourceParent);
    const targetRoot = createNode(0, 'ASBIEP');
    const targetParentA = createNode(1, 'ASBIEP', targetRoot);
    const targetChildA = createNode(2, 'BBIEP', targetParentA);
    const targetParentB = createNode(1, 'ASBIEP', targetRoot);
    sourceRoot.target = targetRoot;
    sourceParent.target = targetParentA;
    targetParentA.source = sourceParent;
    sourceChild.target = targetChildA;
    targetChildA.source = sourceChild;
    // Simulate both sides of a collapsed subtree: the mapped child is no longer reachable through
    // either parent's children, but its logical parent references remain available.
    sourceParent.children = [];
    targetParentA.children = [];

    const registerMapping = (component as unknown as {
      registerMapping: (source: TestNode, target: TestNode) => void
    }).registerMapping.bind(component);
    registerMapping(sourceParent, targetParentA);
    registerMapping(sourceChild, targetChildA);

    component.sourceSelectedNode = sourceParent as unknown as BieUpliftSourceFlatNode;
    component.checkMatch({}, targetParentB as unknown as BieUpliftTargetFlatNode);

    expect(sourceChild.target).toBeUndefined();
    expect(targetChildA.source).toBeUndefined();
    expect(targetParentA.source).toBeUndefined();
    expect(sourceParent.target).toBe(targetParentB);
    expect(targetParentB.source).toBe(sourceParent);
  });

  it('restores descendants when the same source and target parent are remapped', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ASBIEP');
    const sourceParent = createNode(1, 'ASBIEP', sourceRoot);
    const sourceChild = createNode(2, 'BBIEP', sourceParent);
    const targetRoot = createNode(0, 'ASBIEP');
    const targetParent = createNode(1, 'ASBIEP', targetRoot);
    const targetChild = createNode(2, 'BBIEP', targetParent);
    sourceRoot.target = targetRoot;
    targetRoot.source = sourceRoot;
    sourceParent.target = targetParent;
    targetParent.source = sourceParent;
    sourceChild.target = targetChild;
    targetChild.source = sourceChild;
    (component as any).registerMapping(sourceParent, targetParent);
    (component as any).registerMapping(sourceChild, targetChild);
    component.sourceSelectedNode = sourceParent as unknown as BieUpliftSourceFlatNode;

    component.checkMatch({}, targetParent as unknown as BieUpliftTargetFlatNode);
    expect(sourceChild.target).toBeUndefined();
    expect(targetChild.source).toBeUndefined();

    component.checkMatch({}, targetParent as unknown as BieUpliftTargetFlatNode);
    expect(sourceParent.target).toBe(targetParent);
    expect(targetParent.source).toBe(sourceParent);
    expect(sourceChild.target).toBe(targetChild);
    expect(targetChild.source).toBe(sourceChild);
  });

  it('validates nested reuse parents from materialized, non-visible candidates', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ASBIEP');
    const sourceReuse = createNode(1, 'ASBIEP', sourceRoot);
    const sourceNestedReuse = createNode(2, 'ASBIEP', sourceReuse);
    const sourceParty = createNode(3, 'BBIEP', sourceNestedReuse);
    const targetRoot = createNode(0, 'ASBIEP');
    const targetReuse = createNode(1, 'ASBIEP', targetRoot);
    const targetNestedReuse = createNode(2, 'ASBIEP', targetReuse);
    const targetParty = createNode(3, 'BBIEP', targetNestedReuse);
    sourceReuse.target = targetReuse;
    targetReuse.source = sourceReuse;
    sourceNestedReuse.target = targetNestedReuse;
    targetNestedReuse.source = sourceNestedReuse;
    component.sourceSelectedNode = sourceParty as unknown as BieUpliftSourceFlatNode;

    const hasMappedParentPair = (component as unknown as {
      hasMappedParentPair: (source: TestNode, target: TestNode, sourceCandidates: TestNode[],
                             targetCandidates: TestNode[]) => boolean
    }).hasMappedParentPair.bind(component);

    expect(hasMappedParentPair(
      sourceParty,
      targetParty,
      [sourceRoot, sourceReuse, sourceNestedReuse, sourceParty],
      [targetRoot, targetReuse, targetNestedReuse, targetParty]
    )).toBe(true);
  });

  it('ignores group containers while checking structural parents', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const sourceRoot = createNode(0, 'ASBIEP');
    const sourceParent = createNode(1, 'ASBIEP', sourceRoot);
    const sourceGroup = createNode(2, 'ASBIEP', sourceParent, true);
    const sourceChild = createNode(3, 'BBIEP', sourceGroup);
    const targetRoot = createNode(0, 'ASBIEP');
    const targetParent = createNode(1, 'ASBIEP', targetRoot);
    const targetGroup = createNode(2, 'ASBIEP', targetParent, true);
    const targetChild = createNode(3, 'BBIEP', targetGroup);
    sourceParent.target = targetParent;
    targetParent.source = sourceParent;

    component.sourceSelectedNode = sourceChild as unknown as BieUpliftSourceFlatNode;

    expect(component.canMatch(targetChild as unknown as BieUpliftTargetFlatNode)).toBe(true);
  });

  it('resolves mapped parents through uplift wrapper nodes', () => {
    const component = Object.create(BieUpliftComponent.prototype) as BieUpliftComponent;
    const rawSourceRoot = createNode(0, 'ASBIEP');
    const rawSourceParent = createNode(1, 'ASBIEP', rawSourceRoot);
    const rawSourceChild = createNode(2, 'BBIEP', rawSourceParent);
    const rawTargetRoot = createNode(0, 'ASBIEP');
    const rawTargetParent = createNode(1, 'ASBIEP', rawTargetRoot);
    const rawTargetChild = createNode(2, 'BBIEP', rawTargetParent);
    const sourceRoot = new BieUpliftSourceFlatNode(rawSourceRoot as unknown as BieFlatNode);
    const sourceParent = new BieUpliftSourceFlatNode(rawSourceParent as unknown as BieFlatNode);
    const sourceChild = new BieUpliftSourceFlatNode(rawSourceChild as unknown as BieFlatNode);
    const targetRoot = new BieUpliftTargetFlatNode(rawTargetRoot as unknown as BieFlatNode);
    const targetParent = new BieUpliftTargetFlatNode(rawTargetParent as unknown as BieFlatNode);
    const targetChild = new BieUpliftTargetFlatNode(rawTargetChild as unknown as BieFlatNode);
    sourceParent.target = targetParent;
    targetParent.source = sourceParent;

    component.sourceDataSource = {data: [sourceRoot, sourceParent, sourceChild]} as unknown as
      BieFlatNodeDataSource<BieUpliftSourceFlatNode>;
    component.targetDataSource = {data: [targetRoot, targetParent, targetChild]} as unknown as
      BieFlatNodeDataSource<BieUpliftTargetFlatNode>;
    component.sourceSelectedNode = sourceChild;

    expect(component.canMatch(targetChild)).toBe(true);
  });

  it('TC_29_1_TA_7 (V19): clears descendant system state when a parent mapping is removed', () => {
    const component: any = Object.create(BieUpliftComponent.prototype);
    const source: any = {fixed: true, locked: false, isGroup: false};
    const target: any = {source};
    source.target = target;

    component.clearMappingPair(source, target);

    expect(source.fixed).toBe(false);
    expect(component.shouldSerializeSourceNode(source)).toBe(true);
  });

  it('TC_29_1_TA_5_c (P03): recomputes target-only parent rows on retry', () => {
    const component: any = Object.create(BieUpliftComponent.prototype);
    const source: any = {type: 'ABIE', bieType: 'ABIE', bieId: 1,
      upliftPath: 'ASCCP-1>ACC-1', target: {path: 'ASCCP-2>ACC-2', parents: []},
      fixed: false, locked: false, isGroup: false};
    const staleTarget: any = {emptyRequired: true, isGroup: false};
    component.getLoadedNodes = (dataSource: any) => dataSource.data;
    component.sourceDataSource = {data: [source]};
    component.targetDataSource = {data: [staleTarget]};
    component.bieUpliftService = {createUpliftBie: vi.fn(() => of({topLevelAsbiepId: 99}))};
    component.auth = {getUserToken: vi.fn(() => 'token')};
    component.router = {navigateByUrl: vi.fn()};
    component.topLevelAsbiepId = 1;
    component.targetAsccpManifestId = 2;

    component.createUpliftBIE();
    source.target = undefined;
    component.createUpliftBIE();

    expect(staleTarget.emptyRequired).toBe(false);
    expect(component.bieUpliftService.createUpliftBie.mock.calls[1][2]).toHaveLength(1);
  });

  it('TC_29_1_TA_5_f (V19): restores descendant system classification after parent restore', () => {
    const component: any = Object.create(BieUpliftComponent.prototype);
    const parentSource: any = {};
    const parentTarget: any = {};
    const childSource: any = {fixed: false, target: undefined};
    const childTarget: any = {source: undefined};
    component.mappedTargetBySource = new Map();
    component.detachedDescendantMappings = new Map([
      [parentSource, new Map([[parentTarget, [{source: childSource, target: childTarget, fixed: true}]]])]
    ]);

    component.restoreDescendantMappings(parentSource, parentTarget);

    expect(childSource.fixed).toBe(true);
    expect(childSource.target).toBe(childTarget);
    expect(childTarget.source).toBe(childSource);
  });
});
