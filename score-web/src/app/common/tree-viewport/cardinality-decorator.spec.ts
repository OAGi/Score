import {CardinalityDecorator} from './cardinality-decorator';
import {UserToken} from '../../authentication/domain/auth';

describe('CardinalityDecorator', () => {

  const KEY = 'Test-Settings-Hide-Cardinality';
  const token = {username: 'cardinality-spec-user'} as UserToken;
  const preference = {userToken: () => token, key: KEY};

  beforeEach(() => localStorage.clear());

  it('shows min..max as text, in the class the tree styles', () => {
    const decorator = new CardinalityDecorator<any>();

    expect(decorator.decorationsFor({cardinalityMin: 0, cardinalityMax: 3}))
      .toEqual([{text: '0..3', cssClass: 'node-cardinality'}]);
  });

  it('shows an unbounded maximum (-1) as the infinity sign', () => {
    const decorator = new CardinalityDecorator<any>();

    expect(decorator.decorationsFor({cardinalityMin: 1, cardinalityMax: -1})[0].text).toBe('1..∞');
  });

  it('shows nothing for a node that has no cardinality', () => {
    const decorator = new CardinalityDecorator<any>();

    expect(decorator.decorationsFor({})).toEqual([]);
    expect(decorator.decorationsFor({cardinalityMin: 0})).toEqual([]);
    expect(decorator.decorationsFor({cardinalityMax: 1})).toEqual([]);
  });

  it('shows a cardinality of zero (0..0)', () => {
    const decorator = new CardinalityDecorator<any>();

    expect(decorator.decorationsFor({cardinalityMin: 0, cardinalityMax: 0})[0].text).toBe('0..0');
  });

  it('shows nothing while it is hidden, and again once it is shown', () => {
    const decorator = new CardinalityDecorator<any>();
    const node = {cardinalityMin: 0, cardinalityMax: 1};

    decorator.hidden = true;
    expect(decorator.decorationsFor(node)).toEqual([]);

    decorator.hidden = false;
    expect(decorator.decorationsFor(node).length).toBe(1);
  });

  it('is shown at first', () => {
    expect(new CardinalityDecorator<any>(preference).hidden).toBe(false);
  });

  it('remembers for the user that it is hidden', () => {
    new CardinalityDecorator<any>(preference).hidden = true;

    const later = new CardinalityDecorator<any>(preference);
    later.restore();

    expect(later.hidden).toBe(true);
  });

  it('remembers for the user that it is shown again', () => {
    const decorator = new CardinalityDecorator<any>(preference);
    decorator.hidden = true;
    decorator.hidden = false;

    const later = new CardinalityDecorator<any>(preference);
    later.restore();

    expect(later.hidden).toBe(false);
  });

  it('does not take the choice of another user', () => {
    new CardinalityDecorator<any>(preference).hidden = true;

    const other = new CardinalityDecorator<any>(
      {userToken: () => ({username: 'someone-else'} as UserToken), key: KEY});
    other.restore();

    expect(other.hidden).toBe(false);
  });

  it('forgets what it was told to hide when it is restored for a user who never chose', () => {
    const decorator = new CardinalityDecorator<any>(preference);
    decorator.hidden = true;
    localStorage.clear();

    decorator.restore();

    expect(decorator.hidden).toBe(false);
  });

  it('keeps nothing without a preference, and restoring shows it', () => {
    const decorator = new CardinalityDecorator<any>();
    decorator.hidden = true;

    decorator.restore();

    expect(decorator.hidden).toBe(false);
    expect(localStorage.length).toBe(0);
  });
});
