import {ChangeDetectionStrategy, Component, inject, OnInit, signal, ViewEncapsulation} from '@angular/core';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {forkJoin, Observable, of} from 'rxjs';
import {finalize, map, switchMap} from 'rxjs/operators';
import {WebPageInfoService} from '../../basis/basis.service';
import {AuthService} from '../../authentication/auth.service';
import {BieEditAbieNode} from '../../bie-management/bie-edit/domain/bie-edit-node';
import {BieEditService} from '../../bie-management/bie-edit/domain/bie-edit.service';
import {
  AsbiepFlatNode,
  BbiepFlatNode,
  BieFlatNode,
  BieFlatNodeDatabase,
  BieFlatNodeDataSource
} from '../../bie-management/domain/bie-flat-tree';
import {BieStatusDecorator} from '../../bie-management/domain/bie-tree-decorators';
import {CcNodeService} from '../../cc-management/domain/core-component-node.service';
import {CardinalityDecorator} from '../../common/tree-viewport/cardinality-decorator';
import {TreeNodeDecorator} from '../../common/tree-viewport/tree-decorator';
import {TreeNodeFilter} from '../../common/tree-viewport/tree-filter';
import {BusinessTermUsage, BusinessTermUsageAssignment, BusinessTermUsageNode} from '../domain/business-term';
import {BusinessTermService} from '../domain/business-term.service';

export interface BusinessTermWhereUsedDialogData {
  businessTermId: number;
  businessTerm: string;
}

const ASSIGNED_FILTER = 'businessTermAssigned';
const ROW_HEIGHT = 36;
const MAX_TREE_HEIGHT = 420;

/** The key of an assigned node in the tree: the row of the ASBIE or BBIE that the business term is assigned to. */
function rowKey(bieType: string, hashPath: string): string {
  return bieType + '|' + hashPath;
}

function keyOfNode(node: BieFlatNode): string | undefined {
  switch (node.bieType) {
    case 'ASBIEP':
      return rowKey('ASBIE', (node as AsbiepFlatNode).asbieHashPath);
    case 'BBIEP':
      return rowKey('BBIE', (node as BbiepFlatNode).bbieHashPath);
    default:
      return undefined;
  }
}

/** Only the nodes that the business term is assigned to stay, and the way to them. */
class AssignedNodeFilter implements TreeNodeFilter<BieFlatNode> {
  readonly id = ASSIGNED_FILTER;
  active = false;
  readonly keepAncestors = true;

  constructor(private readonly assigned: ReadonlyMap<string, BusinessTermUsageAssignment[]>) {
  }

  accepts(node: BieFlatNode): boolean {
    const key = keyOfNode(node);
    return key !== undefined && this.assigned.has(key);
  }
}

/** The BIE of a top-level ASBIEP drawn as the BIE editor draws it, with only what the business term is assigned to. */
export interface UsageTree {
  usage: BusinessTermUsage;
  dataSource: BieFlatNodeDataSource<BieFlatNode>;
  assigned: ReadonlyMap<string, BusinessTermUsageAssignment[]>;
}

/** A top-level BIE that the business term is assigned in: drawn as one row until the user expands it, and then loaded. */
export interface UsageItem {
  usage: BusinessTermUsage;
  loading: boolean;
  tree?: UsageTree;
}

/** The BIEs that a business term is assigned to: the tree of each, as in the BIE editor, down to the assigned nodes. */
@Component({
  standalone: false,
  selector: 'score-business-term-where-used-dialog',
  templateUrl: './business-term-where-used-dialog.component.html',
  // The tree's own sheet (the one `score-tree-viewport` brings along, not view-encapsulated) is also the one of the rows drawn
  // before any tree is: it is added to the page when the first tree is made, which is too late for them.
  styleUrls: ['../../common/tree-viewport/tree-viewport.component.css', './business-term-where-used-dialog.component.css'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.Eager
})
export class BusinessTermWhereUsedDialogComponent implements OnInit {
  private service = inject(BusinessTermService);
  private bieEditService = inject(BieEditService);
  private ccNodeService = inject(CcNodeService);
  private auth = inject(AuthService);
  readonly webPageInfo = inject(WebPageInfoService);
  readonly data = inject<BusinessTermWhereUsedDialogData>(MAT_DIALOG_DATA);

  readonly items = signal<UsageItem[]>([]);
  readonly loading = signal(true);

  /** The same as the tree of the BIE editor shows after the names of the nodes. */
  readonly cardinality = new CardinalityDecorator<BieFlatNode>(
    {userToken: () => this.auth.getUserToken(), key: 'BIE-Settings-Hide-Cardinality'});
  readonly decorators: TreeNodeDecorator<BieFlatNode>[] = [
    // The top-level ASBIEP is always 1..1, and its row is drawn before the BIE is loaded, without it.
    {decorationsFor: node => node.level === 0 ? [] : this.cardinality.decorationsFor(node)},
    new BieStatusDecorator((node, event) => event.stopPropagation())];

  get title(): string {
    if (this.loading()) {
      return 'Where Used';
    }
    const count = this.items().length;
    return count ? 'Where Used: ' + count + ' BIE' + (count === 1 ? '' : 's') : 'Nothing found';
  }

  ngOnInit(): void {
    this.cardinality.restore();
    this.service.getBusinessTermUsages(this.data.businessTermId).pipe(
      finalize(() => this.loading.set(false))
    ).subscribe(usages => this.items.set(usages.map(usage => ({usage, loading: false}))));
  }

  /** Loads the BIE of the row, and draws it in place of the row. */
  expand(item: UsageItem): void {
    if (item.loading || item.tree) {
      return;
    }
    item.loading = true;
    this.toTree(item.usage).pipe(finalize(() => item.loading = false)).subscribe(tree => item.tree = tree);
  }

  /** Opens the BIE at the node in a new tab: its top-level ASBIEP opens the BIE, a node that the term is assigned to opens at the node. */
  open(item: UsageItem, node: BieFlatNode): void {
    if (node.level > 0 && this.assignmentsOf(item.tree, node).length === 0) {
      return;
    }
    const url = '/profile_bie/' + item.usage.topLevelAsbiepId + (node.level > 0 ? '/' + node.queryPath : '');
    window.open(url, '_blank', 'noopener');
  }

  height(tree: UsageTree): number {
    return Math.min(tree.dataSource.data.length * ROW_HEIGHT, MAX_TREE_HEIGHT);
  }

  assignmentsOf(tree: UsageTree, node: BieFlatNode): BusinessTermUsageAssignment[] {
    const key = keyOfNode(node);
    return key === undefined ? [] : (tree.assigned.get(key) ?? []);
  }

  private toTree(usage: BusinessTermUsage): Observable<UsageTree> {
    const id = usage.topLevelAsbiepId;
    return forkJoin([
      this.bieEditService.getGraphNode(id),
      this.bieEditService.getUsedBieList(id),
      this.bieEditService.getRefBieList(id),
      this.bieEditService.getRootNode(id)
    ]).pipe(
      switchMap(([ccGraph, usedBieList, refBieList, root]) => {
        const rootNode = new BieEditAbieNode(root);
        rootNode.reset();
        const database = new BieFlatNodeDatabase<BieFlatNode>(ccGraph, rootNode, id, usedBieList, refBieList);
        const based = rootNode.basedTopLevelAsbiepId
          ? this.bieEditService.getUsedBieList(rootNode.basedTopLevelAsbiepId).pipe(
            map(baseUsedBieList => database.setBaseUsedBieList(baseUsedBieList)))
          : of(undefined);
        return based.pipe(map(() => database));
      }),
      map(database => this.draw(usage, database))
    );
  }

  /** Loads the tree down to the assigned nodes, then lets only them and the way to them stay, all expanded. */
  private draw(usage: BusinessTermUsage, database: BieFlatNodeDatabase<BieFlatNode>): UsageTree {
    const assigned = new Map<string, BusinessTermUsageAssignment[]>(
      usage.nodes.map((node: BusinessTermUsageNode) => [rowKey(node.bieType, node.hashPath), node.assignments]));

    const dataSource = new BieFlatNodeDataSource<BieFlatNode>(database, this.bieEditService, this.ccNodeService);
    dataSource.init();

    // The filters keep the way to an assigned node only where the children are loaded, so load the branches that lead to one.
    const paths = usage.nodes.map(node => node.path);
    const load = (node: BieFlatNode) => {
      if (!paths.some(path => path.startsWith(node.path + '>'))) {
        return;
      }
      database.children(node).forEach(load);
    };
    load(dataSource.data[0]);

    database.filters.add(new AssignedNodeFilter(assigned));
    database.filters.setActive(ASSIGNED_FILTER, true);

    // Expand what leads to an assigned node: the rows are what the filter lets stay, so each one that has children is on the way.
    for (let i = 0; i < dataSource.data.length; i++) {
      const node = dataSource.data[i];
      if (dataSource.isExpandable(node) && !dataSource.isExpanded(node)) {
        dataSource.expand(node);
      }
    }
    return {usage, dataSource, assigned};
  }
}
