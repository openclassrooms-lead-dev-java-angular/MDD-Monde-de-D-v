import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { ApplicationConfig, provideAppInitializer } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { credentialsInterceptor } from '@interceptor/credential.interceptor';
import { initializeUser } from '@initializer/user.initializer';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([credentialsInterceptor])),
    provideAppInitializer(initializeUser),
  ],
};
