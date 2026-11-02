import {readFileSync} from 'node:fs';
import {join} from 'node:path';

/** The Verb and Noun rows of the Create BOD dialog are ASCCPs, so they link to the ASCCP page. */
describe('create-bod-dialog template', () => {
  const html = readFileSync(
    join(process.cwd(), 'src/app/cc-management/cc-list/create-bod-dialog/create-bod-dialog.component.html'), 'utf8');

  it('never links to the ACC page', () => {
    expect(html).not.toContain('core_component/acc/');
  });

  it('links every DEN and compact row to the ASCCP page (Verb and Noun)', () => {
    expect(html.match(/core_component\/asccp\/' \+ element\.manifestId/g)?.length).toBe(4);
  });
});
