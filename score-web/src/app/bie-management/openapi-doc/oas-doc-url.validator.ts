import {AbstractControl, ValidationErrors} from '@angular/forms';

/** An empty value is valid; otherwise it must be an absolute http or https URL. */
export function httpUrlValidator(control: AbstractControl): ValidationErrors | null {
  const value = (control.value ?? '').toString().trim();
  if (!value) {
    return null;
  }
  try {
    const url = new URL(value);
    return (url.protocol === 'http:' || url.protocol === 'https:') && !!url.hostname ? null : {url: true};
  } catch {
    return {url: true};
  }
}
