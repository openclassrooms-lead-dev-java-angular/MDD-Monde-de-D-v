import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import {
  ApplicationConfig,
  LOCALE_ID,
  provideAppInitializer,
} from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { initializeUser } from '@initializer/user.initializer';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';

import { credentialsInterceptor } from '@interceptor/credential.interceptor';
import { authRefreshInterceptor } from '@interceptor/auth-refresh.interceptor';

registerLocaleData(localeFr);

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([
      credentialsInterceptor,
      authRefreshInterceptor
  ])),
    provideAppInitializer(initializeUser),
    { provide: LOCALE_ID, useValue: 'fr-FR' },
  ],
};
