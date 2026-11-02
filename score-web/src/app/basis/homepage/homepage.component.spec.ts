import {readFileSync} from 'node:fs';
import {join} from 'node:path';
import {HomepageComponent} from './homepage.component';

describe('HomepageComponent', () => {
  it('should be defined', () => {
    expect(HomepageComponent).toBeTruthy();
  });

  it('links a Data Type to the routed Data Type page, not to core_component/dt', () => {
    const html = readFileSync(join(process.cwd(), 'src/app/basis/homepage/homepage.component.html'), 'utf8');
    expect(html).not.toMatch(/core_component\/\{\{[^}]*'dt'/);
    expect(html).toContain("element.type.endsWith('DT') ? '/data_type'");
  });

  it('does not disable the owner link of the Core Components by users table by the BIE branch', () => {
    const html = readFileSync(join(process.cwd(), 'src/app/basis/homepage/homepage.component.html'), 'utf8');
    const start = html.indexOf('<ng-container matColumnDef="username">');
    const end = html.indexOf('</ng-container>', start);
    const column = html.substring(start, end);
    expect(column).toContain("key: 'ownerLoginIdList'");
    expect(column).not.toContain('[class.disabled]');
  });
});
