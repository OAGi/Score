import {TreeNodeDecoration} from '../../../common/tree-viewport/tree-decorator';
import {HideableDecorator, HidePreference} from '../../../common/tree-viewport/hideable-decorator';

/**
 * Issue #1638: a sibling with a custom order weight shows it as a small badge after its name. Whether it is shown
 * ("Hide order weights") is a per-user preference, hidden at first for end-users and shown for developers (the screen
 * gives the default). Managing the weights (drag handle, Order Weight, Reset) stays developer-only, apart from this.
 * The weight of a node is looked up by the screen that knows its view parent.
 */
export class OrderWeightDecorator<N> extends HideableDecorator<N> {

  constructor(private readonly weightOf: (node: N) => number | undefined | null, preference?: HidePreference) {
    super(preference);
  }

  protected shownFor(node: N): TreeNodeDecoration[] {
    const weight = this.weightOf(node);
    if (weight === undefined || weight === null) {
      return [];
    }
    return [{text: String(weight), cssClass: 'order-weight-badge ms-4', tooltip: 'Order weight'}];
  }
}
