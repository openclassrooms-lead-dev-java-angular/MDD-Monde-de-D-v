import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import {
  ApplicationConfig,
  LOCALE_ID,
  provideAppInitializer,
} from '@angular/core';
import {
  provideHttpClient,
  withInterceptors,
  withXsrfConfiguration,
} from '@angular/common/http';
import { initializeUser } from '@initializer/user.initializer';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';

import { credentialsInterceptor } from '@interceptor/credentials.interceptor';
import { authRefreshInterceptor } from '@interceptor/auth-refresh.interceptor';
import { csrfInterceptor } from '@interceptor/csrf.interceptor';

registerLocaleData(localeFr);

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([
        credentialsInterceptor,
        authRefreshInterceptor,
        csrfInterceptor,
      ]),
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN',
      }),
    ),
    provideAppInitializer(initializeUser),
    {
      provide: LOCALE_ID,
      useValue: 'fr-FR',
    },
  ],
};
