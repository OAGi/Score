import {Pipe, PipeTransform} from '@angular/core';
import * as CryptoJS from 'crypto-js';
import {isString} from 'util';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';

export function base64Encode(str): string {
  return CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(str));
}

export function base64Decode(str): string {
  return CryptoJS.enc.Base64.parse(str).toString(CryptoJS.enc.Utf8);
}

export function md5(str): string {
  return CryptoJS.MD5(str).toString();
}

export function sha1(str): string {
  return CryptoJS.SHA1(str).toString();
}

export function sha256(str): string {
  return CryptoJS.SHA256(str).toString();
}

export function hashCode(obj): string {
  return (typeof obj.hashCode === 'function') ? obj.hashCode() :
    md5(JSON.stringify(obj, (name, val) => {
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
