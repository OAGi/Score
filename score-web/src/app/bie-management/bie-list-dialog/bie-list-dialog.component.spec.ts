import {readFileSync} from 'node:fs';
import {join} from 'node:path';
import {BieListDialogComponent} from './bie-list-dialog.component';
import {base64Encode} from '../../common/utility';

describe('BieListDialogComponent', () => {
  it('should be defined', () => {
    expect(BieListDialogComponent).toBeTruthy();
  });

  it('links the reusing BIE with the display path as a base64 query', () => {
    const link = BieListDialogComponent.prototype.getRouteLink.call({},
      {reusingTopLevelAsbiepId: 12, displayPath: 'a > b'} as any);
    expect(link).toBe('/profile_bie/12?q=' + base64Encode('a > b'));
  });

  it('reads the deprecated properties that the reuse report rows have', () => {
    const html = readFileSync(
      join(process.cwd(), 'src/app/bie-management/bie-list-dialog/bie-list-dialog.component.html'), 'utf8');
    expect(html).not.toMatch(/element\.deprecated/);
    expect(html).toContain('element.reusingDeprecated');
    expect(html).toContain('element.reusedDeprecated');
  });
});
