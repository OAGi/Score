import {readFileSync} from 'node:fs';
import {join} from 'node:path';
import {TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {of, Subject} from 'rxjs';
import {AuthService} from '../../authentication/auth.service';
import {WebPageInfoService} from '../../basis/basis.service';
import {BieEditService} from '../../bie-management/bie-edit/domain/bie-edit.service';
import {CcNodeService} from '../../cc-management/domain/core-component-node.service';
import {BusinessTermUsage} from '../domain/business-term';
import {BusinessTermService} from '../domain/business-term.service';
import {BusinessTermWhereUsedDialogComponent, UsageItem, UsageTree} from './business-term-where-used-dialog.component';

const DIR = 'src/app/business-term-management/business-term-where-used-dialog/business-term-where-used-dialog.component';

function usage(id: number, when: string, state = 'WIP'): BusinessTermUsage {
  return {
    topLevelAsbiepId: id, propertyTerm: 'Purchase Order', version: '1.0', state, releaseNum: '10.12',
    owner: {userId: 1, loginId: 'owner', username: 'Owner'} as any,
    lastUpdated: {who: {userId: 2, loginId: 'updater', username: 'Updater'} as any, when: new Date(when)},
    nodes: [
      {bieType: 'BBIE', bieId: 11, path: 'ASBIEP-1>ABIE-1>BBIE-11', hashPath: 'bbie-hash',
        assignments: [{assignedBizTermId: 101, typeCode: 'Synonym', primaryIndicator: true}]},
      {bieType: 'ASBIE', bieId: 12, path: 'ASBIEP-1>ABIE-1>ASBIE-12', hashPath: 'asbie-hash',
        assignments: [{assignedBizTermId: 102, typeCode: null, primaryIndicator: false}]}
    ]
  };
}

describe('BusinessTermWhereUsedDialogComponent', () => {
  let getUsages: ReturnType<typeof vi.fn>;

  function create(usages: BusinessTermUsage[] | Subject<BusinessTermUsage[]> = []): BusinessTermWhereUsedDialogComponent {
    getUsages = vi.fn(() => Array.isArray(usages) ? of(usages) : usages);
    TestBed.configureTestingModule({
      providers: [
        {provide: BusinessTermService, useValue: {getBusinessTermUsages: getUsages}},
        {provide: BieEditService, useValue: {}},
        {provide: CcNodeService, useValue: {}},
        {provide: AuthService, useValue: {getUserToken: () => ({})}},
        {provide: WebPageInfoService, useValue: {getComponentStateColorSet: () => ({background: '#000', font: '#fff'})}},
        {provide: MAT_DIALOG_DATA, useValue: {businessTermId: 5, businessTerm: 'Order Number'}},
      ]
    });
    return TestBed.runInInjectionContext(() => new BusinessTermWhereUsedDialogComponent());
  }

  /** The assigned nodes as the tree makes them: the ASBIEP and BBIEP of a node carry the hash path of the ASBIE and BBIE. */
  function tree(u: BusinessTermUsage, rows = 3): UsageTree {
    return {
      usage: u,
      dataSource: {data: new Array(rows)} as any,
      assigned: new Map(u.nodes.map(n => [n.bieType + '|' + n.hashPath, n.assignments]))
    };
  }

  afterEach(() => TestBed.resetTestingModule());

  // ----- the BIEs of the list -----

  it('asks for the BIEs of the term of the row, and says it is loading until they arrive', () => {
    const later = new Subject<BusinessTermUsage[]>();
    const dialog = create(later);
    dialog.ngOnInit();

    expect(getUsages).toHaveBeenCalledWith(5);
    expect(dialog.loading()).toBe(true);
    expect(dialog.title).toBe('Where Used');

    later.next([usage(1, '2026-01-01T00:00:00')]);
    later.complete();
    expect(dialog.loading()).toBe(false);
  });

  it('shows the number of BIEs in the title, in the singular for one', () => {
    const one = create([usage(1, '2026-01-01T00:00:00')]);
    one.ngOnInit();
    expect(one.title).toBe('Where Used: 1 BIE');
    TestBed.resetTestingModule();

    const three = create([1, 2, 3].map(i => usage(i, '2026-01-0' + i + 'T00:00:00')));
    three.ngOnInit();
    expect(three.title).toBe('Where Used: 3 BIEs');
    expect(three.items().length).toBe(3);
  });

  it('says nothing was found when the term is assigned to no BIE', () => {
    const dialog = create([]);
    dialog.ngOnInit();

    expect(dialog.items()).toEqual([]);
    expect(dialog.title).toBe('Nothing found');
  });

  it('keeps the order of the server, the most recently updated first', () => {
    const dialog = create([usage(30, '2026-03-05T13:45:00'), usage(20, '2026-02-01T08:30:00'), usage(10, '2026-01-10T09:00:00')]);
    dialog.ngOnInit();

    expect(dialog.items().map(item => item.usage.topLevelAsbiepId)).toEqual([30, 20, 10]);
  });

  it('keeps the owner, the state and the time of the last update of each BIE for the row', () => {
    const dialog = create([usage(30, '2026-03-05T13:45:00', 'Production'), usage(10, '2026-01-10T09:00:00', 'QA')]);
    dialog.ngOnInit();

    const [first, second] = dialog.items();
    expect(first.usage.state).toBe('Production');
    expect(first.usage.owner.loginId).toBe('owner');
    expect(first.usage.lastUpdated.who.loginId).toBe('updater');
    expect(first.usage.lastUpdated.when.toISOString()).toBe(new Date('2026-03-05T13:45:00').toISOString());
    expect(second.usage.state).toBe('QA');
  });

  // ----- the tree of a BIE -----

  it('loads the tree of a BIE once, when its row is expanded', () => {
    const dialog = create([usage(1, '2026-01-01T00:00:00')]);
    dialog.ngOnInit();
    const item = dialog.items()[0];
    const drawn = tree(item.usage);
    const toTree = vi.spyOn(dialog as any, 'toTree').mockReturnValue(of(drawn));

    dialog.expand(item);
    dialog.expand(item);

    expect(toTree).toHaveBeenCalledTimes(1);
    expect(item.tree).toBe(drawn);
    expect(item.loading).toBe(false);
  });

  it('does not load a BIE again while it is loading', () => {
    const dialog = create([usage(1, '2026-01-01T00:00:00')]);
    dialog.ngOnInit();
    const item = dialog.items()[0];
    const loaded = new Subject<UsageTree>();
    const toTree = vi.spyOn(dialog as any, 'toTree').mockReturnValue(loaded);

    dialog.expand(item);
    expect(item.loading).toBe(true);
    dialog.expand(item);
    expect(toTree).toHaveBeenCalledTimes(1);

    loaded.next(tree(item.usage));
    loaded.complete();
    expect(item.loading).toBe(false);
  });

  it('gives the tree a height of its rows, and no more than a cap, so that a long tree scrolls in itself', () => {
    const dialog = create();

    expect(dialog.height(tree(usage(1, '2026-01-01T00:00:00'), 3))).toBe(3 * 36);
    expect(dialog.height(tree(usage(1, '2026-01-01T00:00:00'), 500))).toBe(420);
  });

  // ----- the badges -----

  it('finds the assignments of a node through the hash path of its ASBIE or BBIE', () => {
    const dialog = create();
    const drawn = tree(usage(1, '2026-01-01T00:00:00'));

    const bbie = dialog.assignmentsOf(drawn, {bieType: 'BBIEP', bbieHashPath: 'bbie-hash'} as any);
    const asbie = dialog.assignmentsOf(drawn, {bieType: 'ASBIEP', asbieHashPath: 'asbie-hash'} as any);

    expect(bbie).toEqual([{assignedBizTermId: 101, typeCode: 'Synonym', primaryIndicator: true}]);
    expect(asbie).toEqual([{assignedBizTermId: 102, typeCode: null, primaryIndicator: false}]);
  });

  it('has no badge on the way to an assigned node, nor on a node of another kind', () => {
    const dialog = create();
    const drawn = tree(usage(1, '2026-01-01T00:00:00'));

    expect(dialog.assignmentsOf(drawn, {bieType: 'ABIE'} as any)).toEqual([]);
    expect(dialog.assignmentsOf(drawn, {bieType: 'ASBIEP', asbieHashPath: 'not-assigned'} as any)).toEqual([]);
    expect(dialog.assignmentsOf(drawn, {bieType: 'BBIEP', bbieHashPath: 'asbie-hash'} as any)).toEqual([]);
  });

  it('keeps every assignment of a node, each with its own Preferred flag and Type Code', () => {
    const dialog = create();
    const u = usage(1, '2026-01-01T00:00:00');
    u.nodes = [{
      bieType: 'BBIE', bieId: 11, path: 'p', hashPath: 'h', assignments: [
        {assignedBizTermId: 1, typeCode: 'Synonym', primaryIndicator: true},
        {assignedBizTermId: 2, typeCode: 'Legal', primaryIndicator: false}]
    }];

    const badges = dialog.assignmentsOf(tree(u), {bieType: 'BBIEP', bbieHashPath: 'h'} as any);

    expect(badges.map(b => [b.typeCode, b.primaryIndicator])).toEqual([['Synonym', true], ['Legal', false]]);
  });

  // ----- going to the node -----

  describe('clicking a node', () => {
    let open: ReturnType<typeof vi.spyOn>;
    let dialog: BusinessTermWhereUsedDialogComponent;
    let item: UsageItem;

    beforeEach(() => {
      open = vi.spyOn(window, 'open').mockImplementation(() => null);
      dialog = create();
      item = {usage: usage(77, '2026-01-01T00:00:00'), loading: false};
      item.tree = tree(item.usage);
    });

    afterEach(() => open.mockRestore());

    it('opens the BIE at the node that has the term, in a new tab', () => {
      dialog.open(item, {level: 3, bieType: 'BBIEP', bbieHashPath: 'bbie-hash', queryPath: 'ABIE-1>BBIE-11'} as any);

      expect(open).toHaveBeenCalledWith('/profile_bie/77/ABIE-1>BBIE-11', '_blank', 'noopener');
    });

    it('opens the BIE at an ASBIE that has the term', () => {
      dialog.open(item, {level: 2, bieType: 'ASBIEP', asbieHashPath: 'asbie-hash', queryPath: 'ABIE-1>ASBIE-12'} as any);

      expect(open).toHaveBeenCalledWith('/profile_bie/77/ABIE-1>ASBIE-12', '_blank', 'noopener');
    });

    it('opens the BIE itself from the top-level node', () => {
      dialog.open(item, {level: 0, bieType: 'ASBIEP', queryPath: 'ASBIEP-1'} as any);

      expect(open).toHaveBeenCalledWith('/profile_bie/77', '_blank', 'noopener');
    });

    it('goes nowhere from a node that only leads to one that has the term', () => {
      dialog.open(item, {level: 1, bieType: 'ABIE', queryPath: 'ABIE-1'} as any);
      dialog.open(item, {level: 2, bieType: 'ASBIEP', asbieHashPath: 'on-the-way', queryPath: 'ABIE-1>ASBIE-2'} as any);

      expect(open).not.toHaveBeenCalled();
    });
  });

  // ----- how it looks: the template and the style sheet -----

  describe('page', () => {
    const html = readFileSync(join(process.cwd(), DIR + '.html'), 'utf8');
    const css = readFileSync(join(process.cwd(), DIR + '.css'), 'utf8');
    const ts = readFileSync(join(process.cwd(), DIR + '.ts'), 'utf8');

    it('draws the tree with the sheet and the decorations of the tree of the BIE editor', () => {
      expect(ts).toContain("'../../common/tree-viewport/tree-viewport.component.css'");
      expect(html).toContain('<score-tree-viewport');
      expect(ts).toContain('new CardinalityDecorator');
      expect(ts).toContain('new BieStatusDecorator');
      expect(ts).toContain("BieFlatNodeDatabase");
      expect(ts).toContain("BieFlatNodeDataSource");

      // Not one rule of the dialog changes the size, the weight or the font of a row of the tree: only the colour of the text.
      const rowRules = [...css.matchAll(/\.bt-usage-tree \.mat-tree-node\s*\{([^}]*)\}/g)].map(m => m[1]);
      expect(rowRules.length).toBe(1);
      expect(rowRules[0]).toMatch(/color:/);
      expect(rowRules[0]).not.toMatch(/font|height|padding|margin|line-height/);
    });

    it('shows the badge of an assignment with a star for the preferred one, and the Type Code', () => {
      expect(html).toContain('@for (assignment of assignmentsOf(tree, node); track assignment.assignedBizTermId)');
      expect(html).toMatch(/@if \(assignment\.primaryIndicator\)[\s\S]*?star/);
      expect(html).toContain('Preferred business term');
      expect(html).toMatch(/@if \(assignment\.typeCode\)[\s\S]*?\{\{ assignment\.typeCode \}\}/);
      expect(html).toContain('{{ data.businessTerm }}');
    });

    it('shows the release, the state, who updated the BIE and when, as the BIE list does', () => {
      expect(html).toContain('{{ item.usage.releaseNum }}');
      expect(html).toContain("item.usage.lastUpdated.when | date: 'yyyy-MM-dd HH:mm'");
      expect(html).toContain('{{ item.usage.lastUpdated.who.loginId }}');
      expect(html).toContain('{{ usage.state }}');
      expect(html).toContain('webPageInfo.getComponentStateColorSet(usage.state)');
    });

    it('scrolls the body of the dialog only: the title stays and the dialog does not scroll as a whole', () => {
      expect(css).toMatch(/score-dialog-page\.bt-usage-dialog\s*\{[^}]*display:\s*flex[^}]*flex-direction:\s*column[^}]*max-height:\s*90vh/);
      expect(css).toMatch(/\.dialog-body\.mat-mdc-dialog-content\s*\{[^}]*min-height:\s*0[^}]*overflow-y:\s*auto/);
      // The trees are as high as their rows (up to a cap): nothing in the page scrolls on its own other than the body.
      const overflow = css.match(/overflow(-y)?:\s*(auto|scroll)/g) ?? [];
      expect(overflow.length).toBe(1);
    });

    it('puts who updated it and when above the tree, at the left, in a narrow dialog', () => {
      const narrow = css.slice(css.indexOf('@container (max-width: 480px)'));
      expect(narrow).toContain('position: static');
      expect(narrow).toMatch(/\.bt-usage-header\s*\{[^}]*justify-content:\s*flex-start/);
      expect(narrow).toMatch(/\.bt-usage-updated\s*\{[^}]*text-align:\s*left/);
      // And at the right end of the root row of the tree when it is wide.
      expect(css).toMatch(/\.bt-usage-tree \.bt-usage-header\s*\{[^}]*position:\s*absolute[^}]*right:\s*8px/);
      // It follows the width of the dialog, not the window.
      expect(css).toMatch(/\.bt-usage\s*\{[^}]*container-type:\s*inline-size/);
    });
  });
});
