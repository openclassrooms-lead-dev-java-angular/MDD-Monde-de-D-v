import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '@env/environment.prod';
import { TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { RegisterRequest } from '@type/register-request.type';
import { LoginRequest } from '@type/login-request.type';
import { AvailableResponse } from '@type/available-response.type';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const baseUrl = `${environment.apiUrl}/${environment.apiRoute.auth}`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('register', () => {
    it('should send a POST request with the registration payload', () => {
      const registerRequest = {
        username: 'john.doe',
        email: 'john@example.com',
        password: 'Password123!',
      } as RegisterRequest;

      service.register(registerRequest).subscribe((response) => {
        expect(response).toBeInstanceOf(HttpResponse);
        expect(response.status).toBe(201);
        expect(response.body).toBeNull();
      });

      const req = httpMock.expectOne(`${baseUrl}/register`);

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(registerRequest);
      expect(req.request.responseType).toBe('json');

      req.flush(null, {
        status: 201,
        statusText: 'Created',
      });
    });
  });

  describe('login', () => {
    it('should send a POST request with credentials enabled', () => {
      const loginRequest = {
        usernameOrEmail: 'john.doe',
        password: 'Password123!',
      } as LoginRequest;

      service.login(loginRequest).subscribe((response) => {
        expect(response).toBeInstanceOf(HttpResponse);
        expect(response.status).toBe(200);
        expect(response.body).toBeNull();
      });

      const req = httpMock.expectOne(`${baseUrl}/login`);

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(loginRequest);
      expect(req.request.withCredentials).toBeTrue();

      req.flush(null, {
        status: 200,
        statusText: 'OK',
      });
    });
  });

  describe('checkUsernameAvailability', () => {
    it('should send a GET request with the username query parameter', () => {
      const username = 'john.doe';
      const expectedResponse = {
        available: true,
      } as AvailableResponse;

      service.checkUsernameAvailability(username).subscribe((response) => {
        expect(response).toEqual(expectedResponse);
      });

      const req = httpMock.expectOne(
        (request) =>
          request.url === `${baseUrl}/username-available` &&
          request.params.get('username') === username,
      );

      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('username')).toBe(username);

      req.flush(expectedResponse);
    });
  });

  describe('logout', () => {
    it('should send a POST request with an empty body and credentials enabled', () => {
      service.logout().subscribe((response) => {
        expect(response).toBeInstanceOf(HttpResponse);
        expect(response.status).toBe(200);
        expect(response.body).toBeNull();
      });

      const req = httpMock.expectOne(`${baseUrl}/logout`);

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({});
      expect(req.request.withCredentials).toBeTrue();

      req.flush(null, {
        status: 200,
        statusText: 'OK',
      });
    });
  });

  describe('refreshToken', () => {
    it('should send a POST request with an empty body and credentials enabled', () => {
      service.refreshToken().subscribe((response) => {
        expect(response).toBeInstanceOf(HttpResponse);
        expect(response.status).toBe(200);
        expect(response.body).toBeNull();
      });

      const req = httpMock.expectOne(`${baseUrl}/refresh`);

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({});
      expect(req.request.withCredentials).toBeTrue();

      req.flush(null, {
        status: 200,
        statusText: 'OK',
      });
    });
  });
});
