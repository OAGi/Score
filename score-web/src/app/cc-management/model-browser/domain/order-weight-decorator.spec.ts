import {OrderWeightDecorator} from './order-weight-decorator';
import {UserToken} from '../../../authentication/domain/auth';

describe('OrderWeightDecorator', () => {

  const KEY = 'Test-Settings-Hide-Order-Weights';
  const token = {username: 'order-weight-spec-user'} as UserToken;
  const preference = (defaultHidden?: () => boolean) => ({userToken: () => token, key: KEY, defaultHidden});
  const weights = new Map<string, number | undefined | null>();
  const weightOf = (node: { name: string }) => weights.get(node.name);

  beforeEach(() => {
    localStorage.clear();
    weights.clear();
  });

  it('shows the weight as a badge with a tooltip', () => {
    weights.set('a', 7);

    expect(new OrderWeightDecorator(weightOf).decorationsFor({name: 'a'}))
      .toEqual([{text: '7', cssClass: 'order-weight-badge ms-4', tooltip: 'Order weight'}]);
  });

  it('shows a weight of zero, but nothing for a node without a weight', () => {
    weights.set('zero', 0);
    weights.set('none', null);
    const decorator = new OrderWeightDecorator(weightOf);

    expect(decorator.decorationsFor({name: 'zero'})[0].text).toBe('0');
    expect(decorator.decorationsFor({name: 'none'})).toEqual([]);
    expect(decorator.decorationsFor({name: 'unknown'})).toEqual([]);
  });

  it('shows nothing while it is hidden', () => {
    weights.set('a', 1);
    const decorator = new OrderWeightDecorator(weightOf);

    decorator.hidden = true;
    expect(decorator.decorationsFor({name: 'a'})).toEqual([]);
    decorator.hidden = false;
    expect(decorator.decorationsFor({name: 'a'}).length).toBe(1);
  });

  it('takes the default for a user who never chose, and the choice once there is one', () => {
    const first = new OrderWeightDecorator(weightOf, preference(() => true));
    first.restore();
    expect(first.hidden).toBe(true);

    first.hidden = false;
    const later = new OrderWeightDecorator(weightOf, preference(() => true));
    later.restore();
    expect(later.hidden).toBe(false);
  });

  it('takes the default without a preference', () => {
    const decorator = new OrderWeightDecorator(weightOf, {userToken: () => token, key: KEY});
    decorator.restore();
    expect(decorator.hidden).toBe(false);
  });
});
