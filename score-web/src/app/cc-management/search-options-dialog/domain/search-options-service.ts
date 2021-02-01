import {Injectable} from '@angular/core';
import {SearchOptions} from './search-options';

@Injectable()
export class SearchOptionsService {

  SEARCH_OPTIONS_KEY = 'X-Score-SearchOptions';

  loadOptions(): SearchOptions {
    let options;
    try {
      options = JSON.parse(atob(localStorage.getItem(this.SEARCH_OPTIONS_KEY)));
    } catch (ignore) {
      options = new SearchOptions();
      options.excludeDataTypeSupplementaryComponents = true;
      this.saveOptions(options);
    }
    return options;
  }

  saveOptions(options: SearchOptions) {
    localStorage.setItem(this.SEARCH_OPTIONS_KEY, btoa(JSON.stringify(options)));
  }

}
