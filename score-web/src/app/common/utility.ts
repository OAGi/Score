import {Pipe, PipeTransform} from '@angular/core';
import {Md5} from 'ts-md5';
import {isString} from 'util';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';

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

export function initFilter(formControl: FormControl,
                           filteredSubject: ReplaySubject<string[]>,
                           list: string[]) {
  formControl.valueChanges
    .subscribe(() => filter(formControl, filteredSubject, list));
  filteredSubject.next(list.slice());
}

export function filter(formControl: FormControl,
                       filteredSubject: ReplaySubject<string[]>,
                       list: string[]) {
  let search = formControl.value;
  if (!search) {
    filteredSubject.next(list.slice());
    return;
  } else {
    search = search.toLowerCase();
  }
  filteredSubject.next(
    list.filter(e => e.toLowerCase().indexOf(search) > -1)
  );
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
