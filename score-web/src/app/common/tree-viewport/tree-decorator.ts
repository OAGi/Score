/**
 * One small thing a feature shows on the row of a node, after its name: an icon with a tooltip, and what a click on it
 * does. It is data, so the tree draws every feature in the same way and a feature never touches the tree.
 */
export interface TreeNodeDecoration {
  /** The name of the icon. */
  icon: string;
  color?: string;
  tooltip?: string;
  tooltipClass?: string;
  /** 1 when not given; a feature dims what may be outdated. */
  opacity?: number;
  /** Makes the decoration findable (a test, a style); the tree does not read them. */
  id?: string;
  cssClass?: string;
  /** What a click on the decoration does; a decoration without it is only shown. A click does not select the row. */
  onClick?: () => void;
}

/**
 * How a feature attaches to a tree of any kind of node (core components, BIEs, ...): it answers, for a node, what it
 * shows there. A screen hands the decorators it has to the tree; a new feature is one more decorator, and neither the
 * tree nor the screens that have no use for it change.
 */
export interface TreeNodeDecorator<N> {
  /** What the feature shows on the row of the node; nothing for a node it has nothing to say about. */
  decorationsFor(node: N): TreeNodeDecoration[];
}
