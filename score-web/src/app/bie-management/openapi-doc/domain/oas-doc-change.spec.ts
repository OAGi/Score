import {OasDocHeaderEdits, headerKeyOf} from './oas-doc-change';
import {OasDocHttpHeader} from './openapi-doc';

describe('OasDocHeaderEdits', () => {
  const query = (oasOperationId: number, name: string, description = ''): OasDocHttpHeader =>
    ({oasOperationId, name, in: 'query', direction: 'REQUEST', required: false, description, schema: {type: 'string'}} as OasDocHttpHeader);

  let edits: OasDocHeaderEdits;
  beforeEach(() => edits = new OasDocHeaderEdits());

  it('has no change for the entries as they were saved', () => {
    const saved = [query(1, 'a')];
    edits.reset(saved);
    edits.observe(saved);

    expect(edits.changes()).toEqual({upserts: [], removed: []});
  });

  it('says an entry that is new or differs was upserted, and one that is gone was removed', () => {
    edits.reset([query(1, 'kept'), query(1, 'changed', 'x'), query(1, 'gone')]);

    edits.observe([query(1, 'kept'), query(1, 'changed', 'y'), query(1, 'new')]);

    const {upserts, removed} = edits.changes();
    expect(upserts.map(h => h.name)).toEqual(['changed', 'new']);
    expect(removed).toEqual([headerKeyOf(query(1, 'gone'))]);
  });

  it('sees a change that was made to the very object, as the dialog makes it', () => {
    const entry = query(1, 'a', 'x');
    edits.reset([entry]);

    entry.description = 'edited in place';
    edits.observe([entry]);

    expect(edits.changes().upserts.map(h => h.description)).toEqual(['edited in place']);
  });

  it('says a renamed entry is a removal of the old one and an addition of the new one', () => {
    const entry = query(1, 'old');
    edits.reset([entry]);

    entry.name = 'renamed';
    edits.observe([entry]);

    expect(edits.changes().upserts.map(h => h.name)).toEqual(['renamed']);
    expect(edits.changes().removed.map(k => k.name)).toEqual(['old']);
  });

  it('remembers that the user deleted an entry the server registered, after the server answered', () => {
    edits.reset([]);
    const registered = query(1, 'sinceLastDateTime');
    edits.adopt([registered]); // the server's answer to a check: it is what the page shows

    edits.observe([]); // the user deleted it
    edits.adopt([]); // the server's answer, which does not register it again

    expect(edits.changes().removed.map(k => k.name)).toEqual(['sinceLastDateTime']);
    expect(edits.changes().upserts).toEqual([]);
  });

  it('forgets the removal when the user adds the entry again (the same entry is then an upsert of itself, which is no change)', () => {
    edits.reset([query(1, 'a')]);
    edits.observe([]);
    edits.observe([query(1, 'a')]);

    expect(edits.changes().removed).toEqual([]);
    expect(edits.changes().upserts.map(h => h.name)).toEqual(['a']);
  });

  it('forgets the addition of an entry that is deleted before it was saved', () => {
    edits.reset([]);
    edits.observe([query(1, 'b')]);
    edits.observe([]);

    expect(edits.changes().upserts).toEqual([]);
    expect(edits.changes().removed.map(k => k.name)).toEqual(['b']);
  });

  it('does not count what the server registered as the user\'s change', () => {
    edits.reset([]);
    const registered = query(1, 'id');

    edits.adopt([registered]);
    edits.observe([registered]);

    expect(edits.changes()).toEqual({upserts: [], removed: []});
  });

  it('starts again after a save', () => {
    edits.reset([query(1, 'a')]);
    edits.observe([]);

    edits.reset([]);

    expect(edits.changes()).toEqual({upserts: [], removed: []});
  });
});
