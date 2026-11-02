import {TreeNodeDecoration} from './tree-decorator';
import {HideableDecorator, HidePreference} from './hideable-decorator';

/** What a node needs to have for its cardinality to be shown. */
export interface CardinalityNode {
  cardinalityMin?: number;
  cardinalityMax?: number;
}

/** Where the choice of a user to hide the cardinality is kept. */
export type CardinalityPreference = HidePreference;

/**
 * Shows `min..max` after the name of a node, and whether it is shown ("Hide cardinality"), which is remembered for the
 * user when a preference is given.
 */
export class CardinalityDecorator<N extends CardinalityNode> extends HideableDecorator<N> {

  constructor(preference?: CardinalityPreference) {
    super(preference);
  }

  protected shownFor(node: N): TreeNodeDecoration[] {
    if (node.cardinalityMin === undefined || node.cardinalityMax === undefined) {
      return [];
    }
    return [{
      text: node.cardinalityMin + '..' + (node.cardinalityMax === -1 ? '∞' : node.cardinalityMax),
      cssClass: 'node-cardinality'
    }];
  }
}
