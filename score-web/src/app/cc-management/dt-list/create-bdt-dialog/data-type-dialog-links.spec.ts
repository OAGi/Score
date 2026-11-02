import {readFileSync} from 'node:fs';
import {join} from 'node:path';

const template = (path: string) => readFileSync(join(process.cwd(), 'src/app', path), 'utf8');

/** Data Type links in dialogs point at the routed Data Type page (`data_type/:manifestId`). */
describe('Data Type links in dialogs', () => {
  const templates = [
    'cc-management/dt-list/create-bdt-dialog/create-bdt-dialog.component.html',
    'cc-management/cc-list/create-bccp-dialog/create-bccp-dialog.component.html',
    'release-management/release-detail/release-whats-changed-dialog/release-whats-changed-dialog.component.html',
  ];

  for (const path of templates) {
    it(`${path.split('/').pop()} has no link to the unrouted core_component/dt page`, () => {
      expect(template(path)).not.toMatch(/core_component\/dt\//);
    });
  }

  it('the Select based DT dialog links to data_type/<manifestId>', () => {
    const html = template('cc-management/dt-list/create-bdt-dialog/create-bdt-dialog.component.html');
    expect(html).toContain("'data_type/' + element.manifestId");
  });

  it('the What\'s changed dialog links new and revised data types to /data_type/<manifestId>', () => {
    const html = template('release-management/release-detail/release-whats-changed-dialog/release-whats-changed-dialog.component.html');
    expect(html).toContain('href="/data_type/{{ newDT.manifestId }}"');
    expect(html).toContain('href="/data_type/{{ revisedDT.manifestId }}"');
  });
});
