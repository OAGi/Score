import {IconDefinition} from '@fortawesome/free-solid-svg-icons';

/**
 * One small thing a feature shows on the row of a node, after its name: an icon with a tooltip, or a text, and what a
 * click on it does. It is data, so the tree draws every feature in the same way and a feature never touches the tree.
 * What it shows is the first of {@link text}, {@link faIcon} and {@link icon} that is given.
 */
export interface TreeNodeDecoration {
  /** The name of a Material icon. */
  icon?: string;
  /** A Font Awesome icon, for the rows that have always shown one. */
  faIcon?: IconDefinition;
  /** A plain text, such as the cardinality of the node. */
  text?: string;
  color?: string;
  tooltip?: string;
  tooltipClass?: string;
  /** 1 when not given; a feature dims what may be outdated. */
  opacity?: number;
  /** Makes the decoration findable (a test, a style); the tree does not read them. */
  id?: string;
  /** Classes of the decoration; a text and a Font Awesome icon bring their own spacing through them. */
  cssClass?: string;
  /** What a click on the decoration does; a decoration without it is only shown. A click does not select the row. */
  onClick?: (event: MouseEvent) => void;
}

/**
 * How a feature attaches to a tree of any kind of node (core components, BIEs, ...): it answers, for a node, what it
 * shows there. A screen hands the decorators it has to the tree, and their order is the order on the row; a new
 * feature is one more decorator, and neither the tree nor the screens that have no use for it change.
 */
export interface TreeNodeDecorator<N> {
  /** What the feature shows on the row of the node; nothing for a node it has nothing to say about. */
  decorationsFor(node: N): TreeNodeDecoration[];
}
