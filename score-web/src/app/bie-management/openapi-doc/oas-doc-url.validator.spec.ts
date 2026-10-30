import {FormControl, Validators} from '@angular/forms';
import {httpUrlValidator} from './oas-doc-url.validator';

describe('OpenAPI document URL and email validation', () => {
  const urlError = (value: string | null) => httpUrlValidator(new FormControl(value));
  const emailError = (value: string) => Validators.email(new FormControl(value));

  it.each([
    '', null,
    'https://example.com/terms',
    'https://Example.com/terms',
    'https://example.com/terms?x=1#top',
    'http://example.technology/~user/a%20b',
    'https://example.com:8443/path',
  ])('accepts the URL %j', value => {
    expect(urlError(value)).toBeNull();
  });

  it.each(['example', 'not a url', 'ftp://example.com', 'javascript:alert(1)', 'https://'])('rejects the URL %j', value => {
    expect(urlError(value)).toEqual({url: true});
  });

  it.each(['', 'a+b@example.com', 'first.last@mail2.example.co.uk', 'A_B-c%d@Example.com'])('accepts the email %j', value => {
    expect(emailError(value)).toBeNull();
  });

  it.each(['plain', '@example.com', 'a@'])('rejects the email %j', value => {
    expect(emailError(value)).not.toBeNull();
  });
});
