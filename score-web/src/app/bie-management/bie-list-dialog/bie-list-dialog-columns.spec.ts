import {readFileSync} from 'node:fs';
import {join} from 'node:path';
import {BIE_REUSE_REPORT_TABLE} from '../bie-report/bie-report.table';

/** score-table-view throws unless the template defines every column of the table definition. */
describe('bie-list-dialog template', () => {
  const html = readFileSync(
    join(process.cwd(), 'src/app/bie-management/bie-list-dialog/bie-list-dialog.component.html'), 'utf8');

  for (const column of BIE_REUSE_REPORT_TABLE.columns) {
    it(`defines the ${column.id} column`, () => {
      expect(html).toContain(`scoreColumnDef="${column.id}"`);
    });
  }
});
