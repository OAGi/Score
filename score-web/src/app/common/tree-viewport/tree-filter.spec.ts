import {describe, expect, it, vi} from 'vitest';
import {TreeFilterChange, TreeFilterSet, TreeNodeFilter} from './tree-filter';

interface N {
  name: string;
  on?: boolean;
  children: N[];
}

const node = (name: string, on = false, children: N[] = []): N => ({name, on, children});

function filter(id: string, keepAncestors = false): TreeNodeFilter<N> {
  return {id, active: false, keepAncestors, accepts: n => !!n.on};
}

function setOf(options: {exempt?: (n: N) => boolean, identityOf?: (n: N) => unknown} = {}) {
  return new TreeFilterSet<N>({childrenOf: n => n.children, ...options});
}

describe('TreeFilterSet', () => {

  it('shows everything while no filter is on', () => {
    const set = setOf();
    set.add(filter('f'));
    const children = [node('a'), node('b', true)];
    expect(set.active).toBe(false);
    expect(set.visibleChildren(children)).toBe(children);
  });

  it('keeps only the accepted nodes of a filter that does not keep ancestors', () => {
    const set = setOf();
    set.add(filter('f'));
    set.setActive('f', true);
    const kept = node('b', true);
    const hiddenParent = node('a', false, [node('c', true)]);
    expect(set.visibleChildren([hiddenParent, kept])).toEqual([kept]);
  });

  it('keeps the way to an accepted node below a node when the filter keeps ancestors', () => {
    const set = setOf();
    set.add(filter('f', true));
    set.setActive('f', true);
    const way = node('a', false, [node('b', false, [node('c', true)])]);
    const dead = node('d', false, [node('e')]);
    expect(set.visibleChildren([way, dead])).toEqual([way]);
  });

  it('shows a node only when every filter that is on lets it stay', () => {
    const set = setOf();
    set.add({id: 'first', active: false, accepts: n => n.name !== 'x'});
    set.add({id: 'second', active: false, accepts: n => n.name !== 'y'});
    const [x, y, z] = [node('x'), node('y'), node('z')];
    set.setActive('first', true);
    expect(set.visibleChildren([x, y, z])).toEqual([y, z]);
    set.setActive('second', true);
    expect(set.visibleChildren([x, y, z])).toEqual([z]);
    set.setActive('first', false);
    expect(set.visibleChildren([x, y, z])).toEqual([x, z]);
  });

  it('shows an exempt node whatever the filters say, but does not let it pass them', () => {
    const exempt = node('found');
    const set = setOf({exempt: n => n === exempt});
    set.add(filter('f'));
    set.setActive('f', true);
    expect(set.visible(exempt)).toBe(true);
    expect(set.passes(exempt)).toBe(false);
  });

  it('answers for a filter that is off through satisfies', () => {
    const set = setOf();
    const f = filter('f', true);
    set.add(f);
    expect(set.satisfies(f, node('a', false, [node('b', true)]))).toBe(true);
    expect(set.satisfies(f, node('a', false, [node('b')]))).toBe(false);
  });

  it('survives a node that leads back to itself', () => {
    const set = setOf();
    const f = filter('f', true);
    set.add(f);
    const loop = node('loop');
    loop.children.push(loop);
    expect(set.satisfies(f, loop)).toBe(false);
    loop.on = true;
    expect(set.satisfies(f, loop)).toBe(true);
  });

  it('tells nodes apart by the identity it is given', () => {
    const place = node('a');
    const wrapper = {name: 'wrapper', children: [] as N[]};
    const set = setOf({identityOf: n => n === wrapper ? place : n});
    const f = filter('f', true);
    set.add(f);
    // The wrapper and what it wraps are one place: it is visited once, and the walk ends.
    place.children.push(wrapper);
    wrapper.children.push(place);
    expect(set.satisfies(f, place)).toBe(false);
    place.children.push(node('on', true));
    expect(set.satisfies(f, place)).toBe(true);
  });

  it('never loads a branch: it only looks at the children it is given', () => {
    const childrenOf = vi.fn((n: N) => n.children);
    const set = new TreeFilterSet<N>({childrenOf});
    set.add(filter('f', true));
    set.setActive('f', true);
    expect(set.visible(node('leaf'))).toBe(false);
    expect(childrenOf).toHaveBeenCalledTimes(1);
  });

  it('announces a filter that is switched on or off, once', () => {
    const set = setOf();
    const f = filter('f');
    set.add(f);
    const changes: TreeFilterChange<N>[] = [];
    set.changes$.subscribe(change => changes.push(change));

    expect(set.setActive('f', true)).toBe(true);
    expect(set.setActive('f', true)).toBe(false);
    expect(set.setActive('f', false)).toBe(true);

    expect(changes).toEqual([{filter: f, active: true}, {filter: f, active: false}]);
    expect(set.isActive('f')).toBe(false);
  });

  it('refuses a second filter with the same id and a switch of an unknown filter', () => {
    const set = setOf();
    set.add(filter('f'));
    expect(() => set.add(filter('f'))).toThrow();
    expect(() => set.setActive('other', true)).toThrow();
  });
});
