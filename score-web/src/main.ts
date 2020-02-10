import './polyfills';

import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {enableProdMode} from '@angular/core';
import {environment} from './environments/environment';
import {ScoreWebModule} from './app/score-web.module';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(ScoreWebModule)
  .catch(err => console.log(err));
