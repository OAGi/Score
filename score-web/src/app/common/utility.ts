import {Pipe, PipeTransform} from '@angular/core';
import * as CryptoJS from 'crypto-js';
import {FormControl} from '@angular/forms';
import {ReplaySubject} from 'rxjs';
import {UserToken} from '../authentication/domain/auth';

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

export function hashCode4Array(...objs): number {
  let hash = 1;
  for (const obj of objs) {
    hash = 31 * hash + hashCode4Object(obj);
    if (Math.abs(hash) >= 1000000000) {
      hash = hashCode4String(hashCode(hash));
    }
  }
  return hashCode4String(hashCode(hash));
}

export function hashCode4Object(obj: any): number {
  if (!obj) {
    return 0;
  }

  if (Array.isArray(obj)) {
    return hashCode4Array(...obj);
  }

  switch (typeof obj) {
    case 'string':
      return hashCode4String(obj);
    case 'boolean':
      return (obj) ? 1231 : 1237;
    case 'number':
      return (obj) ? obj as number : 0;
    default:
      return (typeof obj.hashCode === 'number') ? obj.hashCode : hashCode4String(hashCode(obj));
  }
}

export function hashCode4String(s: string): number {
  let hash = 0, i, chr;
  for (i = 0; i < s.length; i++) {
    chr = s.charCodeAt(i);
    hash = ((hash << 5) - hash) + chr;
    hash |= 0; // Convert to 32bit integer
  }
  return hash;
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

export type MapperFunction<T, R> = (source: T) => R;

export function initFilter<T>(formControl: FormControl,
                              filteredSubject: ReplaySubject<T[]>,
                              list: T[],
                              toStrFunc?: MapperFunction<T, string>) {
  formControl.valueChanges
    .subscribe(() => filter(formControl, filteredSubject, list, toStrFunc));
  filteredSubject.next(list.slice());
}

export function filter<T>(formControl: FormControl,
                          filteredSubject: ReplaySubject<T[]>,
                          list: T[],
                          toStrFunc?: MapperFunction<T, string>) {
  if (!toStrFunc) {
    toStrFunc = (s) => s.toString();
  }
  let search = formControl.value;
  if (!search) {
    filteredSubject.next(list.slice());
  } else {
    search = search.toLowerCase();
    filteredSubject.next(
      list.filter(e => !!e).filter(e => toStrFunc(e).toLowerCase().indexOf(search) > -1)
    );
  }
}

export function loadBooleanProperty(userToken: UserToken, key: string, defaultValue: boolean): boolean {
  if (!userToken || !key) {
    return defaultValue;
  }

  const itemKey = 'X-Score-' + key + '[' + userToken.username + ']';
  try {
    return JSON.parse(atob(localStorage.getItem(itemKey))).value;
  } catch (ignore) {
    return defaultValue;
  }
}

export function saveBooleanProperty(userToken: UserToken, key: string, value: boolean) {
  if (!userToken || !key) {
    return;
  }
  const itemKey = 'X-Score-' + key + '[' + userToken.username + ']';
  localStorage.setItem(itemKey, btoa(JSON.stringify({value})));
}

export function loadBranch(userToken: UserToken, type: string): number | undefined {
  if (!userToken || !type) {
    return undefined;
  }
  const key = 'X-Score-Branch-Selection[' + userToken.username + ', ' + type + ']';
  let value;
  try {
    value = JSON.parse(atob(localStorage.getItem(key)));
    return value['releaseId'];
  } catch (ignore) {
    return undefined;
  }
}

export function saveBranch(userToken: UserToken, type: string, releaseId: number) {
  if (!userToken || !type) {
    return;
  }
  const key = 'X-Score-Branch-Selection[' + userToken.username + ', ' + type + ']';
  localStorage.setItem(key, btoa(JSON.stringify({'releaseId': releaseId})));
}

export function truncate(str: string, len: number): string {
  if (str.length <= len) {
    return str;
  }
  return str.substring(0, len - 3) + '...';
}

export function compare(a: string, b: string): number {
  if (!a) {
    return 1;
  } else if (!b) {
    return -1;
  } else if (!a && !b) {
    return 0;
  } else {
    return a.localeCompare(b);
  }
}

export function toCamelCase(value: string): string {
  if (!value) {
    return value;
  }
  if (value.length === 1) {
    value = value.toUpperCase();
  } else {
    value = value.split(' ').map(e => e.trim()).filter(e => e.length > 0).map(e => {
      if (e.length === 1) {
        return e.toUpperCase();
      } else {
        return e.charAt(0).toUpperCase() + e.slice(1);
      }
    }).join(' ');
    value = value.replace(/([a-z])([A-Z])/g, '$1 $2').trim();
  }
  return value;
}

export function emptyToUndefined(value: string): string {
  if (!value) {
    return undefined;
  }
  value = value.trim();
  return value.length === 0 ? undefined : value;
}

@Pipe({name: 'unbounded'})
export class UnboundedPipe implements PipeTransform {
  transform(value): string {
    if (!value || value === 'unbounded') {
      return value;
    }
    if (typeof value === 'string') {
      value = value.trim();
    }
    return (value === -1 || value === '-1') ? 'unbounded' : '' + value;
  }
}

@Pipe({name: 'highlight'})
export class HighlightSearch implements PipeTransform {

  transform(value: any, keyword: string): any {
    if (!keyword) {
      return value;
    }
    const re = new RegExp(keyword, 'gi');
    return value.replace(re, '<mark>$&</mark>');
  }
}

@Pipe({name: 'dateAgo', pure: true})
export class DateAgoPipe implements PipeTransform {

  transform(value: number[], args?: any): any {
    const intervals = {
      'year': 31536000,
      'month': 2592000,
      'week': 604800,
      'day': 86400,
      'hour': 3600,
      'minute': 60,
      'second': 1
    };
    if (value && value.length === 7) {
      const now = new Date();
      let seconds = 0;
      seconds += (now.getFullYear() - value[0]) * intervals['year'];
      seconds += (now.getMonth() + 1 - value[1]) * intervals['month'];
      seconds += (now.getDate() - value[2]) * intervals['day'];
      seconds += (now.getHours() - value[3]) * intervals['hour'];
      seconds += (now.getMinutes() - value[4]) * intervals['minute'];
      seconds += (now.getSeconds() - value[5]) * intervals['second'];
      if (seconds < 29) {
        return 'Just now';
      }
      let counter;
      // tslint:disable-next-line:forin
      for (const i in intervals) {
        counter = Math.floor(seconds / intervals[i]);
        if (counter > 0) {
          if (counter === 1) {
            return counter + ' ' + i + ' ago'; // singular (1 day ago)
          } else {
            return counter + ' ' + i + 's ago'; // plural (2 days ago)
          }
        }
      }
    } else {
      return 'Just now';
    }
    return value;
  }
}

@Pipe({name: 'undefined', pure: true})
export class UndefinedPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    if (value === undefined || value === null) {
      return '';
    }
    return value;
  }
}

@Pipe({name: 'separate', pure: true})
export class SeparatePipe implements PipeTransform {

  transform(value: string, args?: any): any {
    if (!value) {
      return '';
    }

    return value.replace(/[A-Z][a-z]/g, (str) => ' ' + str).trim();
  }
}

@Pipe({
  name: 'sort'
})
export class ArraySortPipe implements PipeTransform {
  transform(array: any[], field: string): any[] {
    array.sort((a: any, b: any) => {
      if (a[field] < b[field]) {
        return -1;
      } else if (a[field] > b[field]) {
        return 1;
      } else {
        return 0;
      }
    });
    return array;
  }
}

@Pipe({
  name: 'truncate'
})
export class TruncatePipe implements PipeTransform {

  transform(value: string, length: number): any {
    return truncate(value, length);
  }

}
