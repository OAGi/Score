import './polyfills';

import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {enableProdMode, provideZoneChangeDetection} from '@angular/core';
import {environment} from './environments/environment';
import {ScoreWebModule} from './app/score-web.module';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(ScoreWebModule, { applicationProviders: [provideZoneChangeDetection()], })
  .catch(err => console.log(err));
