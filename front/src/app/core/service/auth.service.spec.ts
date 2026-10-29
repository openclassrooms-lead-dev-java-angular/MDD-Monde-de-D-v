import { TestBed } from '@angular/core/testing';
import {
  HttpResponse,
  provideHttpClient,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { AuthService } from './auth.service';
import { environment } from '@env/environment';
import { RegisterRequest } from '@type/register-request.type';
import { LoginRequest } from '@type/login-request.type';
import { AvailableResponse } from '@type/available-response.type';


describe('AuthService', () => {
  let service: AuthService;
  let httpTestingController: HttpTestingController;

  const apiUrl =
    `${environment.apiUrl}/${environment.apiRoute.auth}`;

  const mockRegisterRequest = {
    username: 'test-user',
    email: 'test@example.com',
    password: 'Password123!',
  } as RegisterRequest;

  const mockLoginRequest = {
    usernameOrEmail: 'test-user',
    password: 'Password123!',
  } as LoginRequest;

  const mockAvailableResponse = {
    available: true,
  } as AvailableResponse;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // register

  describe('register', () => {
    it('should send a POST request to the register endpoint', () => {
      service.register(mockRegisterRequest).subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/register`,
      );

      expect(req.request.method).toBe('POST');
      expect(req.request.url).toBe(`${apiUrl}/register`);

      req.flush(null, {
        status: 201,
        statusText: 'Created',
      });
    });

    it('should send the registration request in the body', () => {
      service.register(mockRegisterRequest).subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/register`,
      );

      expect(req.request.body).toEqual(mockRegisterRequest);

      req.flush(null, {
        status: 201,
        statusText: 'Created',
      });
    });

    it('should return the full HTTP response', () => {
      let response: HttpResponse<void> | undefined;

      service.register(mockRegisterRequest).subscribe((result) => {
        response = result;
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/register`,
      );

      req.flush(null, {
        status: 201,
        statusText: 'Created',
      });

      expect(response).toBeDefined();
      expect(response?.status).toBe(201);
      expect(response?.body).toBeNull();
    });

    it('should propagate an HTTP error', () => {
      service.register(mockRegisterRequest).subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error: { status: number }) => {
          expect(error.status).toBe(400);
        },
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/register`,
      );

      req.flush(
        { message: 'Invalid registration data' },
        {
          status: 400,
          statusText: 'Bad Request',
        },
      );
    });
  });

  // login

  describe('login', () => {
    it('should send a POST request to the login endpoint', () => {
      service.login(mockLoginRequest).subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/login`,
      );

      expect(req.request.method).toBe('POST');

      req.flush(null);
    });

    it('should send the login request in the body', () => {
      service.login(mockLoginRequest).subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/login`,
      );

      expect(req.request.body).toEqual(mockLoginRequest);

      req.flush(null);
    });

    it('should send credentials with the request', () => {
      service.login(mockLoginRequest).subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/login`,
      );

      expect(req.request.withCredentials).toBeTrue();

      req.flush(null);
    });

    it('should return the full HTTP response', () => {
      let response: HttpResponse<void> | undefined;

      service.login(mockLoginRequest).subscribe((result) => {
        response = result;
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/login`,
      );

      req.flush(null, {
        status: 200,
        statusText: 'OK',
      });

      expect(response).toBeDefined();
      expect(response?.status).toBe(200);
      expect(response?.body).toBeNull();
    });

    it('should propagate an unauthorized error', () => {
      service.login(mockLoginRequest).subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error: { status: number }) => {
          expect(error.status).toBe(401);
        },
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/login`,
      );

      req.flush(
        { message: 'Invalid credentials' },
        {
          status: 401,
          statusText: 'Unauthorized',
        },
      );
    });
  });

  // checkUsernameAvailability

  describe('checkUsernameAvailability', () => {
    it('should send a GET request to the username availability endpoint', () => {
      service.checkUsernameAvailability('test-user').subscribe();

      const req = httpTestingController.expectOne(
        (request) =>
          request.url === `${apiUrl}/username-available`,
      );

      expect(req.request.method).toBe('GET');

      req.flush(mockAvailableResponse);
    });

    it('should send the username as a query parameter', () => {
      service.checkUsernameAvailability('test-user').subscribe();

      const req = httpTestingController.expectOne(
        (request) =>
          request.url === `${apiUrl}/username-available`,
      );

      expect(req.request.params.get('username')).toBe('test-user');

      req.flush(mockAvailableResponse);
    });

    it('should return the availability response', () => {
      let response: AvailableResponse | undefined;

      service.checkUsernameAvailability('test-user').subscribe((result) => {
        response = result;
      });

      const req = httpTestingController.expectOne(
        (request) =>
          request.url === `${apiUrl}/username-available`,
      );

      req.flush(mockAvailableResponse);

      expect(response).toEqual(mockAvailableResponse);
    });

    it('should support an unavailable username', () => {
      const unavailableResponse = {
        available: false,
      } as AvailableResponse;

      service.checkUsernameAvailability('existing-user').subscribe(
        (response) => {
          expect(response).toEqual(unavailableResponse);
        },
      );

      const req = httpTestingController.expectOne(
        (request) =>
          request.url === `${apiUrl}/username-available` &&
          request.params.get('username') === 'existing-user',
      );

      req.flush(unavailableResponse);
    });

    it('should propagate an HTTP error', () => {
      service.checkUsernameAvailability('test-user').subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error: { status: number }) => {
          expect(error.status).toBe(500);
        },
      });

      const req = httpTestingController.expectOne(
        (request) =>
          request.url === `${apiUrl}/username-available`,
      );

      req.flush(
        { message: 'Server error' },
        {
          status: 500,
          statusText: 'Internal Server Error',
        },
      );
    });
  });

  // logout

  describe('logout', () => {
    it('should send a POST request to the logout endpoint', () => {
      service.logout().subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/logout`,
      );

      expect(req.request.method).toBe('POST');

      req.flush(null);
    });

    it('should send an empty object in the request body', () => {
      service.logout().subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/logout`,
      );

      expect(req.request.body).toEqual({});

      req.flush(null);
    });

    it('should send credentials with the request', () => {
      service.logout().subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/logout`,
      );

      expect(req.request.withCredentials).toBeTrue();

      req.flush(null);
    });

    it('should return the full HTTP response', () => {
      let response: HttpResponse<void> | undefined;

      service.logout().subscribe((result) => {
        response = result;
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/logout`,
      );

      req.flush(null, {
        status: 200,
        statusText: 'OK',
      });

      expect(response).toBeDefined();
      expect(response?.status).toBe(200);
      expect(response?.body).toBeNull();
    });

    it('should propagate an HTTP error', () => {
      service.logout().subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error: { status: number }) => {
          expect(error.status).toBe(401);
        },
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/logout`,
      );

      req.flush(
        { message: 'Unauthorized' },
        {
          status: 401,
          statusText: 'Unauthorized',
        },
      );
    });
  });

  // refreshToken

  describe('refreshToken', () => {
    it('should send a POST request to the refresh endpoint', () => {
      service.refreshToken().subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/refresh`,
      );

      expect(req.request.method).toBe('POST');

      req.flush(null);
    });

    it('should send an empty object in the request body', () => {
      service.refreshToken().subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/refresh`,
      );

      expect(req.request.body).toEqual({});

      req.flush(null);
    });

    it('should send credentials with the request', () => {
      service.refreshToken().subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/refresh`,
      );

      expect(req.request.withCredentials).toBeTrue();

      req.flush(null);
    });

    it('should return the full HTTP response', () => {
      let response: HttpResponse<void> | undefined;

      service.refreshToken().subscribe((result) => {
        response = result;
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/refresh`,
      );

      req.flush(null, {
        status: 200,
        statusText: 'OK',
      });

      expect(response).toBeDefined();
      expect(response?.status).toBe(200);
      expect(response?.body).toBeNull();
    });

    it('should propagate an HTTP error', () => {
      service.refreshToken().subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error: { status: number }) => {
          expect(error.status).toBe(401);
        },
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/refresh`,
      );

      req.flush(
        { message: 'Refresh token expired' },
        {
          status: 401,
          statusText: 'Unauthorized',
        },
      );
    });
  });
});