import {Pipe, PipeTransform} from '@angular/core';
import {Md5} from 'ts-md5';
import {isNumber, isString} from 'util';

export function hashCode(obj): string {
  return (typeof obj.hashCode === 'function') ? obj.hashCode() :
    <string>Md5.hashStr(JSON.stringify(obj, (name, val) => {
    if (name === '$hashCode') {
      return undefined;
    }
    if (val === '') {
      return null;
    }

    return val;
  }));
}

@Pipe({name: 'unbounded'})
export class UnboundedPipe implements PipeTransform {
  transform(value): string {
    if (!value || value === 'unbounded') {
      return value;
    }
    if (isString(value)) {
      value = value.trim();
    }
    return (value === -1 || value === '-1') ? 'unbounded' : '' + value;
  }
}
