import {
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { Observable } from 'rxjs';

const getCookie = (name: string): string | null => {
  if (typeof document === 'undefined') {
    return null;
  }

  const prefix = `${name}=`;
  const cookie = document.cookie
    .split('; ')
    .find((entry) => entry.startsWith(prefix));

  return cookie ? decodeURIComponent(cookie.slice(prefix.length)) : null;
};

export const csrfInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> => {
  const csrfToken = getCookie('XSRF-TOKEN');

  const isInsaveMethod = !['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(
    req.method.toUpperCase(),
  );

  if (!csrfToken || !isInsaveMethod) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: {
        'X-XSRF-TOKEN': csrfToken,
      },
    }),
  );
};
