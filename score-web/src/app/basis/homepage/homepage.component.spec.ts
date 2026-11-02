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

  it('opens the Core Component list of the users and states table on the Working release', () => {
    const html = readFileSync(join(process.cwd(), 'src/app/basis/homepage/homepage.component.html'), 'utf8');
    const start = html.indexOf('<ng-container matColumnDef="username">');
    const end = html.indexOf('<tr mat-header-row', start);
    const columns = html.substring(start, end);
    expect(columns.match(/q: ccQ\(/g)?.length).toBe(5);
    expect(columns).not.toMatch(/q: q\(/);

    const self = {workingRelease: {releaseId: 7}, q: HomepageComponent.prototype.q};
    const withRelease = HomepageComponent.prototype.ccQ.call(self, [{key: 'states', value: 'WIP'}]);
    expect(atob(withRelease)).toBe('states=WIP&releaseId=7');
    self.workingRelease = undefined;
    expect(atob(HomepageComponent.prototype.ccQ.call(self, [{key: 'states', value: 'WIP'}]))).toBe('states=WIP');
  });
});
