import {faCircleExclamation, faRecycle, faSitemap} from '@fortawesome/free-solid-svg-icons';
import {TreeNodeDecoration, TreeNodeDecorator} from '../../common/tree-viewport/tree-decorator';
import {BieFlatNode} from './bie-flat-tree';
import {BieUpliftSourceFlatNode, BieUpliftTargetFlatNode} from '../bie-uplift/domain/bie-uplift';

const STATUS_ICON = 'node-status-icon ps-3 pointer';

/** What the BIE editor tells of a node besides its name: a BIE it reuses, and a BIE it inherits. */
export class BieStatusDecorator implements TreeNodeDecorator<BieFlatNode> {

  /** @param openReusedBie opens the BIE the node reuses. */
  constructor(private readonly openReusedBie: (node: BieFlatNode, event: MouseEvent) => void) {
  }

  decorationsFor(node: BieFlatNode): TreeNodeDecoration[] {
    const decorations: TreeNodeDecoration[] = [];
    if (node.reused) {
      decorations.push({
        faIcon: faRecycle, tooltip: 'Reused', cssClass: STATUS_ICON,
        onClick: event => this.openReusedBie(node, event)
      });
    }
    if (node.inherited) {
      decorations.push({faIcon: faSitemap, tooltip: 'Inherited', cssClass: STATUS_ICON});
    }
    return decorations;
  }
}

/** What the source BIE of an uplift tells of a node: that it reuses a BIE. */
export class BieUpliftSourceStatusDecorator implements TreeNodeDecorator<BieUpliftSourceFlatNode> {

  decorationsFor(node: BieUpliftSourceFlatNode): TreeNodeDecoration[] {
    return node.reused ? [{faIcon: faRecycle, tooltip: 'Reused', cssClass: STATUS_ICON}] : [];
  }
}

/** What the target BIE of an uplift tells of a node: the BIE it reuses, or that the one it must reuse is not chosen. */
export class BieUpliftTargetStatusDecorator implements TreeNodeDecorator<BieUpliftTargetFlatNode> {

  /** @param chooseReused lets the user choose the BIE the node reuses. */
  constructor(private readonly chooseReused: (node: BieUpliftTargetFlatNode) => void) {
  }

  decorationsFor(node: BieUpliftTargetFlatNode): TreeNodeDecoration[] {
    const decorations: TreeNodeDecoration[] = [];
    const chosen = node.reusedTopLevelAsbiepId !== undefined && node.reusedTopLevelAsbiepId !== null;
    if (node.source && node.source.reused && !chosen) {
      decorations.push({
        faIcon: faCircleExclamation, tooltip: 'Select BIE', cssClass: STATUS_ICON + ' warn',
        onClick: () => this.chooseReused(node)
      });
    }
    if (chosen) {
      decorations.push({
        faIcon: faRecycle, tooltip: 'Reused', cssClass: STATUS_ICON,
        onClick: () => this.chooseReused(node)
      });
    }
    return decorations;
  }
}
