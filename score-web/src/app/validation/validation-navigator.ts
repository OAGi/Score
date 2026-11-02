import {ValidationLocation} from './validation';

/** What the panel needs from a screen to take the user to a node; a screen of any kind of object can implement it. */
export interface NodeNavigator {
  /** Shows the node the location is about; false when the node is not there any more. */
  navigateTo(location: ValidationLocation): boolean;
}
