import './polyfills';

import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {enableProdMode} from '@angular/core';
import {environment} from './environments/environment';
import {SrtWebappModule} from './app/srt-webapp.module';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(SrtWebappModule)
  .catch(err => console.log(err));
