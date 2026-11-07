import {EditedRows} from './edited-rows';

interface Row {
  id: number;
  name: string;
  edited: boolean;
}

describe('EditedRows', () => {
  const row = (id: number, edited = false): Row => ({id, name: 'row ' + id, edited});
  let edits: EditedRows<Row>;

  beforeEach(() => edits = new EditedRows<Row>(r => '' + r.id, r => r.edited));

  it('gives back the edited row, not the loaded one, when its page is shown again', () => {
    const page1 = [row(1, true), row(2)];
    const page2 = edits.carry(page1, [row(3), row(4)]);
    const back = edits.carry(page2, [row(1), row(2)]);

    expect(back[0]).toBe(page1[0]);
    expect(back[1]).not.toBe(page1[1]);
  });

  it('keeps the edit when a sort or a search brings the row to another page', () => {
    const edited = row(5, true);
    edits.carry([edited], [row(9)]);

    expect(edits.carry([row(9)], [row(8), row(5)])[1]).toBe(edited);
  });

  it('lists the edited rows of every page, the current one included', () => {
    const left = row(1, true);
    const page2 = edits.carry([left, row(2)], [row(3), row(4)]);
    page2[1].edited = true;

    expect(edits.edited(page2).map(r => r.id)).toEqual([1, 4]);
  });

  it('lets a row go that was edited back to what it was', () => {
    const r = row(1, true);
    edits.remember([r]);
    r.edited = false;

    expect(edits.edited([r])).toEqual([]);
    expect(edits.carry([r], [row(1)])[0]).not.toBe(r);
  });

  it('adds the edited rows that are off the page to the rows to check, each once', () => {
    const left = row(1, true);
    const page = edits.carry([left], [row(2), row(3)]);

    expect(edits.withEdited(page).map(r => r.id)).toEqual([2, 3, 1]);
    expect(edits.withEdited([left, row(2)]).map(r => r.id)).toEqual([1, 2]);
  });

  it('counts the edited rows', () => {
    const page = edits.carry([row(1, true)], [row(2, true), row(3)]);

    expect(edits.count(page)).toBe(2);
  });

  it('forgets everything on clear', () => {
    edits.remember([row(1, true)]);
    edits.clear();

    expect(edits.edited([])).toEqual([]);
  });
});
