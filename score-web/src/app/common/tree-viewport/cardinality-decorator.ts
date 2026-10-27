import {TreeNodeDecoration, TreeNodeDecorator} from './tree-decorator';
import {UserToken} from '../../authentication/domain/auth';
import {loadBooleanProperty, saveBooleanProperty} from '../utility';

/** What a node needs to have for its cardinality to be shown. */
export interface CardinalityNode {
  cardinalityMin?: number;
  cardinalityMax?: number;
}

/** Where the choice of a user to hide the cardinality is kept. */
export interface CardinalityPreference {
  userToken: () => UserToken;
  key: string;
}

/**
 * Shows `min..max` after the name of a node, and whether it is shown ("Hide cardinality"), which is remembered for the
 * user when a preference is given.
 */
export class CardinalityDecorator<N extends CardinalityNode> implements TreeNodeDecorator<N> {

  private _hidden = false;

  constructor(private readonly preference?: CardinalityPreference) {
  }

  get hidden(): boolean {
    return this._hidden;
  }

  set hidden(hidden: boolean) {
    this._hidden = hidden;
    if (this.preference) {
      saveBooleanProperty(this.preference.userToken(), this.preference.key, hidden);
    }
  }

  /** Takes the choice the user made before, once the user is known. */
  restore() {
    this._hidden = this.preference ? loadBooleanProperty(this.preference.userToken(), this.preference.key, false) : false;
  }

  decorationsFor(node: N): TreeNodeDecoration[] {
    if (this._hidden || node.cardinalityMin === undefined || node.cardinalityMax === undefined) {
      return [];
    }
    return [{
      text: node.cardinalityMin + '..' + (node.cardinalityMax === -1 ? '∞' : node.cardinalityMax),
      cssClass: 'node-cardinality'
    }];
  }
}
