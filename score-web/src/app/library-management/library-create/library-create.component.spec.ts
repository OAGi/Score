import {FormControl} from '@angular/forms';
import {LibraryCreateComponent} from './library-create.component';

describe('LibraryCreateComponent', () => {
  it('should be defined', () => {
    expect(LibraryCreateComponent).toBeTruthy();
  });

  describe('isDisabled', () => {
    const isDisabled = (library: Record<string, unknown>) =>
      Object.getOwnPropertyDescriptor(LibraryCreateComponent.prototype, 'isDisabled')!.get!.call({
        library,
        linkForm: new FormControl(null),
        namespaceUriForm: new FormControl('urn:x')
      });
    const library = {name: 'L', organization: 'O', domain: 'D', type: 'T'};

    it('is enabled when every required field is filled', () => {
      expect(isDisabled(library)).toBe(false);
    });

    it('is disabled when the type is missing or blank', () => {
      for (const type of [undefined, null, '', '   ']) {
        expect(isDisabled({...library, type})).toBe(true);
      }
    });
  });
});
