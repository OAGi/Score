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
});
