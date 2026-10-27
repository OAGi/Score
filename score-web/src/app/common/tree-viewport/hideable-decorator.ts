import {TreeNodeDecoration, TreeNodeDecorator} from './tree-decorator';
import {UserToken} from '../../authentication/domain/auth';
import {loadBooleanProperty, saveBooleanProperty} from '../utility';

/** Where the choice of a user to hide what a decorator shows is kept. */
export interface HidePreference {
  userToken: () => UserToken;
  key: string;
  /** Whether it is hidden for a user who never chose; shown when not given. */
  defaultHidden?: () => boolean;
}

/**
 * A decorator the user can hide ("Hide cardinality"); the choice is remembered for the user when a preference is
 * given. A subclass only says what it shows.
 */
export abstract class HideableDecorator<N> implements TreeNodeDecorator<N> {

  private _hidden = false;

  protected constructor(private readonly preference?: HidePreference) {
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
    const defaultHidden = this.preference?.defaultHidden?.() ?? false;
    this._hidden = this.preference
      ? loadBooleanProperty(this.preference.userToken(), this.preference.key, defaultHidden) : defaultHidden;
  }

  decorationsFor(node: N): TreeNodeDecoration[] {
    return this._hidden ? [] : this.shownFor(node);
  }

  /** What it shows for a node while it is not hidden. */
  protected abstract shownFor(node: N): TreeNodeDecoration[];
}
