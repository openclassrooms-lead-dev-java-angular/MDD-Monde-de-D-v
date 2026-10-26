
import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { csrfInterceptor } from './csrf.interceptor';

describe('csrfInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  const cookieName = 'XSRF-TOKEN';

  const setCsrfCookie = (value: string): void => {
    document.cookie = `${cookieName}=${value}; path=/`;
  };

  const clearCsrfCookie = (): void => {
    document.cookie = `${cookieName}=; Max-Age=0; path=/`;
  };

  beforeEach(() => {
    clearCsrfCookie();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(
          withInterceptors([csrfInterceptor]),
        ),
        provideHttpClientTesting(),
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    clearCsrfCookie();
  });

  describe('unsafe HTTP methods', () => {
    it('should add the CSRF header to POST requests when the cookie exists', () => {
      setCsrfCookie('test-csrf-token');

      http.post('/api/profile', { username: 'test-user' }).subscribe();

      const request = httpMock.expectOne('/api/profile');

      expect(request.request.headers.get('X-XSRF-TOKEN')).toBe(
        'test-csrf-token',
      );

      request.flush({});
    });

    it('should add the CSRF header to PUT requests', () => {
      setCsrfCookie('test-csrf-token');

      http.put('/api/profile', { username: 'updated-user' }).subscribe();

      const request = httpMock.expectOne('/api/profile');

      expect(request.request.headers.get('X-XSRF-TOKEN')).toBe(
        'test-csrf-token',
      );

      request.flush({});
    });

    it('should add the CSRF header to PATCH requests', () => {
      setCsrfCookie('test-csrf-token');

      http.patch('/api/profile', { username: 'updated-user' }).subscribe();

      const request = httpMock.expectOne('/api/profile');

      expect(request.request.headers.get('X-XSRF-TOKEN')).toBe(
        'test-csrf-token',
      );

      request.flush({});
    });

    it('should add the CSRF header to DELETE requests', () => {
      setCsrfCookie('test-csrf-token');

      http.delete('/api/profile').subscribe();

      const request = httpMock.expectOne('/api/profile');

      expect(request.request.headers.get('X-XSRF-TOKEN')).toBe(
        'test-csrf-token',
      );

      request.flush({});
    });
  });

  describe('safe HTTP methods', () => {
    it('should not add the CSRF header to GET requests', () => {
      setCsrfCookie('test-csrf-token');

      http.get('/api/profile').subscribe();

      const request = httpMock.expectOne('/api/profile');

      expect(request.request.headers.has('X-XSRF-TOKEN')).toBeFalse();

      request.flush({});
    });

    it('should not add the CSRF header to HEAD requests', () => {
      setCsrfCookie('test-csrf-token');

      http.head('/api/profile').subscribe();

      const request = httpMock.expectOne('/api/profile');

      expect(request.request.headers.has('X-XSRF-TOKEN')).toBeFalse();

      request.flush(null);
    });

    it('should not add the CSRF header to OPTIONS requests', () => {
      setCsrfCookie('test-csrf-token');

      http.options('/api/profile').subscribe();

      const request = httpMock.expectOne('/api/profile');

      expect(request.request.headers.has('X-XSRF-TOKEN')).toBeFalse();

      request.flush({});
    });

    it('should not add the CSRF header to TRACE requests', () => {
      setCsrfCookie('test-csrf-token');

      http.request('TRACE', '/api/profile').subscribe();

      const request = httpMock.expectOne('/api/profile');

      expect(request.request.headers.has('X-XSRF-TOKEN')).toBeFalse();

      request.flush({});
    });
  });

  describe('missing CSRF cookie', () => {
    it('should not add the CSRF header when the cookie is missing', () => {
      http.post('/api/profile', { username: 'test-user' }).subscribe();

      const request = httpMock.expectOne('/api/profile');

      expect(request.request.headers.has('X-XSRF-TOKEN')).toBeFalse();

      request.flush({});
    });
  });

  describe('cookie decoding', () => {
    it('should decode the cookie value before adding it to the header', () => {
      setCsrfCookie('token%20with%20spaces');

      http.post('/api/profile', {}).subscribe();

      const request = httpMock.expectOne('/api/profile');

      expect(request.request.headers.get('X-XSRF-TOKEN')).toBe(
        'token with spaces',
      );

      request.flush({});
    });
  });
});
