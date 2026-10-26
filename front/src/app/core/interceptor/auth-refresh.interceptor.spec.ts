import {
  HttpClient,
  HttpErrorResponse,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { AuthService } from '@service/auth.service';
import { UserStateService } from '@service/user-state.service';
import { authRefreshInterceptor } from './auth-refresh.interceptor';

describe('authRefreshInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: jasmine.SpyObj<AuthService>;
  let userStateService: jasmine.SpyObj<UserStateService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', [
      'refreshToken',
    ]);

    userStateService = jasmine.createSpyObj<UserStateService>(
      'UserStateService',
      ['clearUser'],
    );

    authService.refreshToken.and.returnValue(
      of(undefined) as ReturnType<AuthService['refreshToken']>,
    );

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authRefreshInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService },
        { provide: UserStateService, useValue: userStateService },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('excluded URLs', () => {
    it('should not refresh the token for login, logout, or refresh URLs', () => {
      const excludedUrls = [
        '/api/login',
        '/api/logout',
        '/api/refresh',
        '/api/login?returnUrl=%2Fprofile',
        '/api/logout/',
      ];

      excludedUrls.forEach((url) => {
        http.get(url).subscribe();

        const request = httpMock.expectOne(url);
        request.flush({});
      });

      expect(authService.refreshToken).not.toHaveBeenCalled();
      expect(userStateService.clearUser).not.toHaveBeenCalled();
    });

    it('should not exclude URLs that only contain an excluded word as part of another path segment', () => {
      http.get('/api/login-history').subscribe();

      const request = httpMock.expectOne('/api/login-history');
      request.flush({});

      expect(authService.refreshToken).not.toHaveBeenCalled();
    });
  });

  describe('HTTP errors', () => {
    it('should propagate errors other than 401 without refreshing the token', (done) => {
      http.get('/api/profile').subscribe({
        next: () => {
          fail('The request should fail');
          done();
        },
        error: (error: HttpErrorResponse) => {
          expect(error.status).toBe(500);
          expect(authService.refreshToken).not.toHaveBeenCalled();
          expect(userStateService.clearUser).not.toHaveBeenCalled();
          done();
        },
      });

      httpMock
        .expectOne('/api/profile')
        .flush(
          { message: 'Server error' },
          { status: 500, statusText: 'Internal Server Error' },
        );
    });

    it('should propagate a 401 error if the refresh succeeds but the retried request fails', (done) => {
      http.get('/api/profile').subscribe({
        next: () => {
          fail('The request should fail');
          done();
        },
        error: (error: HttpErrorResponse) => {
          expect(error.status).toBe(401);
          expect(authService.refreshToken).toHaveBeenCalledTimes(1);
          expect(userStateService.clearUser).toHaveBeenCalledTimes(1);
          done();
        },
      });

      httpMock
        .expectOne('/api/profile')
        .flush(
          { message: 'Unauthorized' },
          { status: 401, statusText: 'Unauthorized' },
        );

      const retriedRequest = httpMock.expectOne('/api/profile');

      expect(retriedRequest.request.withCredentials).toBeTrue();

      retriedRequest.flush(
        { message: 'Unauthorized' },
        { status: 401, statusText: 'Unauthorized' },
      );
    });
  });

  describe('token refresh', () => {
    it('should refresh the token and retry the request with credentials', (done) => {
      http.get('/api/profile').subscribe({
        next: (response) => {
          expect(response).toEqual({ name: 'Test User' });
          expect(authService.refreshToken).toHaveBeenCalledTimes(1);
          expect(userStateService.clearUser).not.toHaveBeenCalled();
          done();
        },
        error: (error) => {
          fail(`The request should succeed: ${error}`);
          done();
        },
      });

      const initialRequest = httpMock.expectOne('/api/profile');

      expect(initialRequest.request.withCredentials).toBeFalse();

      initialRequest.flush(
        { message: 'Unauthorized' },
        { status: 401, statusText: 'Unauthorized' },
      );

      const retriedRequest = httpMock.expectOne('/api/profile');

      expect(retriedRequest.request.withCredentials).toBeTrue();

      retriedRequest.flush({ name: 'Test User' });
    });

    it('should clear the user and propagate the error if refreshing the token fails', (done) => {
      const refreshError = new HttpErrorResponse({
        status: 401,
        statusText: 'Unauthorized',
      });

      authService.refreshToken.and.returnValue(
        throwError(() => refreshError) as ReturnType<
          AuthService['refreshToken']
        >,
      );

      http.get('/api/profile').subscribe({
        next: () => {
          fail('The request should fail');
          done();
        },
        error: (error) => {
          expect(error).toBe(refreshError);
          expect(authService.refreshToken).toHaveBeenCalledTimes(1);
          expect(userStateService.clearUser).toHaveBeenCalledTimes(1);
          done();
        },
      });

      httpMock
        .expectOne('/api/profile')
        .flush(
          { message: 'Unauthorized' },
          { status: 401, statusText: 'Unauthorized' },
        );
    });

    it('should share a single refresh request between concurrent 401 responses', (done) => {
      const refreshSubject = new Subject<unknown>();

      authService.refreshToken.and.returnValue(
        refreshSubject.asObservable() as ReturnType<
          AuthService['refreshToken']
        >,
      );

      let completedRequests = 0;

      const onSuccess = () => {
        completedRequests += 1;

        if (completedRequests === 2) {
          expect(authService.refreshToken).toHaveBeenCalledTimes(1);
          expect(userStateService.clearUser).not.toHaveBeenCalled();
          done();
        }
      };

      http.get('/api/profile/1').subscribe({
        next: onSuccess,
        error: (error) => {
          fail(`The first request should succeed: ${error}`);
          done();
        },
      });

      http.get('/api/profile/2').subscribe({
        next: onSuccess,
        error: (error) => {
          fail(`The second request should succeed: ${error}`);
          done();
        },
      });

      httpMock
        .expectOne('/api/profile/1')
        .flush({}, { status: 401, statusText: 'Unauthorized' });

      httpMock
        .expectOne('/api/profile/2')
        .flush({}, { status: 401, statusText: 'Unauthorized' });

      expect(authService.refreshToken).toHaveBeenCalledTimes(1);

      refreshSubject.next(undefined);
      refreshSubject.complete();

      const retriedRequests = httpMock.match((request) =>
        ['/api/profile/1', '/api/profile/2'].includes(request.url),
      );

      expect(retriedRequests.length).toBe(2);

      retriedRequests.forEach((request) => {
        expect(request.request.withCredentials).toBeTrue();
        request.flush({ success: true });
      });
    });
  });
});
