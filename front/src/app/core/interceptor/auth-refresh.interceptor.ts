import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import {
  catchError,
  finalize,
  Observable,
  shareReplay,
  switchMap,
  throwError,
} from 'rxjs';

import { AuthService } from '@service/auth.service';
import { UserStateService } from '@service/user-state.service';

let refreshRequest$: Observable<unknown> | null = null;

export const authRefreshInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const userStateService = inject(UserStateService);
  const router = inject(Router);

  const excludedUrl = /\/(login|logout|refresh)(?:[/?#]|$)/i;

  if (excludedUrl.test(req.url)) {
    return next(req);
  }

  return next(req).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401) {
        return throwError(() => error);
      }

      if (!refreshRequest$) {
        refreshRequest$ = authService.refreshToken().pipe(
          finalize(() => {
            refreshRequest$ = null;
          }),
          shareReplay({ bufferSize: 1, refCount: false }),
        );
      }

      return refreshRequest$.pipe(
        switchMap(() => next(req.clone({ withCredentials: true }))),
        catchError((refreshError: unknown) => {
          userStateService.clearUser();

          return throwError(() => refreshError);
        }),
      );
    }),
  );
};
