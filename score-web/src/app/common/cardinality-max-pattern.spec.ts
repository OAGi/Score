import {FormControl, Validators} from '@angular/forms';
import {CARDINALITY_MAX_PATTERN} from './cardinality-max-pattern';

describe('CARDINALITY_MAX_PATTERN', () => {
  const validate = (value: string) => Validators.pattern(CARDINALITY_MAX_PATTERN)(new FormControl(value));

  it('accepts a non-negative integer, -1 and unbounded', () => {
    for (const value of ['0', '1', '25', '-1', 'unbounded']) {
      expect(validate(value)).toBeNull();
    }
  });

  it('rejects text that only starts or ends with a valid alternative', () => {
    for (const value of ['abc-1', '5abc', 'xunbounded', 'unboundedx', '-1x', '-2', '1.5']) {
      expect(validate(value)).not.toBeNull();
    }
  });
});
