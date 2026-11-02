import {faCircleExclamation, faRecycle, faSitemap} from '@fortawesome/free-solid-svg-icons';
import {BieStatusDecorator, BieUpliftSourceStatusDecorator, BieUpliftTargetStatusDecorator} from './bie-tree-decorators';

describe('BieStatusDecorator', () => {

  it('shows nothing for a node that reuses and inherits nothing', () => {
    expect(new BieStatusDecorator(() => {}).decorationsFor({} as any)).toEqual([]);
  });

  it('shows the reused icon, which opens the reused BIE with the click', () => {
    const opened: any[] = [];
    const node = {reused: true} as any;
    const [reused] = new BieStatusDecorator((n, event) => opened.push([n, event])).decorationsFor(node);
    const event = {} as MouseEvent;

    reused.onClick!(event);

    expect(reused.faIcon).toBe(faRecycle);
    expect(reused.tooltip).toBe('Reused');
    expect(opened).toEqual([[node, event]]);
  });

  it('shows the inherited icon, which is only shown', () => {
    const [inherited] = new BieStatusDecorator(() => {}).decorationsFor({inherited: true} as any);

    expect(inherited.faIcon).toBe(faSitemap);
    expect(inherited.tooltip).toBe('Inherited');
    expect(inherited.onClick).toBeUndefined();
  });

  it('shows the reused icon before the inherited one', () => {
    const decorations = new BieStatusDecorator(() => {}).decorationsFor({reused: true, inherited: true} as any);

    expect(decorations.map(d => d.tooltip)).toEqual(['Reused', 'Inherited']);
  });

  it('gives both icons the spacing and the pointer of a status icon', () => {
    const decorations = new BieStatusDecorator(() => {}).decorationsFor({reused: true, inherited: true} as any);

    decorations.forEach(d => expect(d.cssClass).toBe('node-status-icon ps-3 pointer'));
  });
});

describe('BieUpliftSourceStatusDecorator', () => {

  it('shows the reused icon only for a node that reuses a BIE', () => {
    const decorator = new BieUpliftSourceStatusDecorator();

    expect(decorator.decorationsFor({reused: false} as any)).toEqual([]);
    const [reused] = decorator.decorationsFor({reused: true} as any);
    expect(reused.faIcon).toBe(faRecycle);
    expect(reused.onClick).toBeUndefined();
  });
});

describe('BieUpliftTargetStatusDecorator', () => {

  const chosen: any[] = [];
  const decorator = new BieUpliftTargetStatusDecorator(node => chosen.push(node));

  beforeEach(() => chosen.length = 0);

  it('shows nothing for a node whose source does not reuse a BIE', () => {
    expect(decorator.decorationsFor({source: {reused: false}} as any)).toEqual([]);
    expect(decorator.decorationsFor({} as any)).toEqual([]);
  });

  it('asks to select the BIE when the source reuses one and none is chosen', () => {
    const node = {source: {reused: true}} as any;
    const decorations = decorator.decorationsFor(node);

    expect(decorations.length).toBe(1);
    expect(decorations[0].faIcon).toBe(faCircleExclamation);
    expect(decorations[0].tooltip).toBe('Select BIE');
    expect(decorations[0].cssClass).toBe('node-status-icon ps-3 pointer warn');

    decorations[0].onClick!({} as MouseEvent);
    expect(chosen).toEqual([node]);
  });

  it('asks to select the BIE when the chosen one is null', () => {
    const decorations = decorator.decorationsFor({source: {reused: true}, reusedTopLevelAsbiepId: null} as any);

    expect(decorations[0].tooltip).toBe('Select BIE');
  });

  it('shows the reused icon, which lets the user change the BIE, once one is chosen', () => {
    const node = {source: {reused: true}, reusedTopLevelAsbiepId: 7} as any;
    const decorations = decorator.decorationsFor(node);

    expect(decorations.length).toBe(1);
    expect(decorations[0].faIcon).toBe(faRecycle);
    expect(decorations[0].tooltip).toBe('Reused');

    decorations[0].onClick!({} as MouseEvent);
    expect(chosen).toEqual([node]);
  });

  it('shows the reused icon for a chosen BIE even when the source reuses nothing', () => {
    const decorations = decorator.decorationsFor({source: {reused: false}, reusedTopLevelAsbiepId: 7} as any);

    expect(decorations.map(d => d.tooltip)).toEqual(['Reused']);
  });
});
