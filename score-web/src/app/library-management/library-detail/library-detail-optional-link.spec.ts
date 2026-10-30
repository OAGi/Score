import {FormControl, Validators} from '@angular/forms';
import {LibraryDetailComponent} from './library-detail.component';

/** A library can be created without a Link, so the detail page lets it be updated without one. */
describe('LibraryDetailComponent.isDisabled', () => {
  const link = (value: string | null) => new FormControl(value, Validators.pattern('\\w+:(\\/?\\/?)[^\\s]+'));
  const isDisabled = (library: Record<string, unknown>, uriForm: FormControl) =>
    LibraryDetailComponent.prototype.isDisabled.call({library, uriForm});
  const library = {name: 'L', organization: 'O', domain: 'D'};

  it('is enabled for a library without a link', () => {
    expect(isDisabled({...library, link: null}, link(null))).toBe(false);
  });

  it('is still disabled for a link that is not a URI', () => {
    expect(isDisabled({...library, link: 'not a uri'}, link('not a uri'))).toBe(true);
  });

  it('is enabled for a valid link', () => {
    expect(isDisabled({...library, link: 'https://example.com'}, link('https://example.com'))).toBe(false);
  });

  it('is disabled when the name is missing', () => {
    expect(isDisabled({...library, name: '', link: null}, link(null))).toBe(true);
  });

  it('keeps the Link field optional in the template', async () => {
    const {readFileSync} = await import('node:fs');
    const {join} = await import('node:path');
    const html = readFileSync(join(process.cwd(), 'src/app/library-management/library-detail/library-detail.component.html'), 'utf8');
    expect(html).not.toMatch(/<input matInput required\s+\[formControl\]="uriForm"/);
  });
});
