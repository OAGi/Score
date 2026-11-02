import {readFileSync} from 'node:fs';
import {join} from 'node:path';
import {TransferOwnershipListComponent} from './transfer-ownership-list.component';

describe('TransferOwnershipListComponent', () => {
  it('should be defined', () => {
    expect(TransferOwnershipListComponent).toBeTruthy();
  });

  /** The rows are AccountListEntry, which carries the account name as `username` and has no `name`. */
  it('shows the account name from the username field in the Name column', () => {
    const html = readFileSync(join(process.cwd(),
      'src/app/account-management/take-ownership-list/transfer-ownership-list.component.html'), 'utf8');
    const nameColumn = /<ng-container scoreColumnDef="name">[\s\S]*?<\/ng-container>/.exec(html)?.[0] ?? '';
    expect(nameColumn).toContain('{{ element.username }}');
    expect(nameColumn).not.toContain('element.name');
  });
});
