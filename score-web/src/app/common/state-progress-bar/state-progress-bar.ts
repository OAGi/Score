import {HttpParams} from '@angular/common/http';
import {base64Encode} from '../utility';

export interface StateProgressBarItem {
  name: string;
  value: number;
  href: any[];
  disabled: boolean;
  style: {
    bg_color: string,
    text_color: string
  };
}

/** The `q` query parameter that the list pages read their filter from. */
export function encodeListQuery(params: {key: string, value: any}[]): string {
  let httpParams = new HttpParams();
  for (const param of params) {
    httpParams = httpParams.set(param.key, param.value);
  }
  return base64Encode(httpParams.toString());
}

/** The `q` of the list that the item opens, or an empty string when the item carries no filter. */
export function stateProgressBarQuery(item: StateProgressBarItem): string {
  return item.href.length === 1 ? '' : encodeListQuery(item.href[1]);
}
