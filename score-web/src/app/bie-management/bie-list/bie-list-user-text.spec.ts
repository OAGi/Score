import {readdirSync, readFileSync, statSync} from 'node:fs';
import {join} from 'node:path';

const htmlFiles = (dir: string): string[] =>
  readdirSync(dir).flatMap(name => {
    const path = join(dir, name);
    return statSync(path).isDirectory() ? htmlFiles(path) : path.endsWith('.html') ? [path] : [];
  });

/**
 * Version, Status, Business Term and Remark are text the user typed. Binding them with [innerHTML] swallows
 * tag-like input such as "note <b>important</b> text", so the BIE tables show them as plain text.
 */
describe('BIE table cells for user-entered text', () => {
  const files = htmlFiles(join(process.cwd(), 'src/app'));
  const bound = /\[innerHTML\]="element\.(version|status|bizTerm|remark)"/;

  it('are not bound through [innerHTML]', () => {
    const offenders = files.filter(path => bound.test(readFileSync(path, 'utf8')));
    expect(offenders).toEqual([]);
  });

  it('are shown by text interpolation in the BIE list', () => {
    const html = readFileSync(join(process.cwd(), 'src/app/bie-management/bie-list/bie-list.component.html'), 'utf8');
    for (const field of ['version', 'status', 'bizTerm', 'remark']) {
      expect(html).toContain(`{{ element.${field} }}`);
    }
  });
});
