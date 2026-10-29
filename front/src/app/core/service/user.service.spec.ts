import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { UserService } from './user.service';
import { environment } from '@env/environment';
import { User } from '@model/user.model';
import { AvailableResponse } from '@type/available-response.type';
import { NewUserType } from '@type/new-user.type';

describe('UserService', () => {
  let service: UserService;
  let httpTestingController: HttpTestingController;

  const path = `${environment.apiUrl}/${environment.apiRoute.user}`;

  const mockUser: User = {
    email: 'john@example.com',
    username: 'John_Doe',
    createdAt: new Date('2025-01-01T10:00:00.000Z'),
    updatedAt: new Date('2025-01-02T10:00:00.000Z'),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(UserService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getMe', () => {
    it('should send a GET request to retrieve the current user', () => {
      let result: User | undefined;

      service.getMe().subscribe((user) => {
        result = user;
      });

      const request = httpTestingController.expectOne(`${path}/me`);

      expect(request.request.method).toBe('GET');
      expect(request.request.withCredentials).toBeTrue();

      request.flush(mockUser);

      expect(result).toEqual(mockUser);
    });

    it('should propagate an HTTP error', () => {
      let receivedError: unknown;

      service.getMe().subscribe({
        error: (error: unknown) => {
          receivedError = error;
        },
      });

      const request = httpTestingController.expectOne(`${path}/me`);

      request.flush('Server error', {
        status: 500,
        statusText: 'Internal Server Error',
      });

      expect(receivedError).toBeTruthy();
    });
  });

  describe('checkUsernameAvailability', () => {
    it('should send a GET request with the username query parameter', () => {
      const username = 'John_Doe';
      const response: AvailableResponse = { available: true };
      let result: AvailableResponse | undefined;

      service.checkUsernameAvailability(username).subscribe((value) => {
        result = value;
      });

      const request = httpTestingController.expectOne(
        (req) =>
          req.url === `${path}/username-available` &&
          req.params.get('username') === username,
      );

      expect(request.request.method).toBe('GET');

      request.flush(response);

      expect(result).toEqual(response);
    });

    it('should return false when the username is unavailable', () => {
      const response: AvailableResponse = { available: false };
      let result: AvailableResponse | undefined;

      service.checkUsernameAvailability('John_Doe').subscribe((value) => {
        result = value;
      });

      const request = httpTestingController.expectOne(
        (req) =>
          req.url === `${path}/username-available` &&
          req.params.get('username') === 'John_Doe',
      );

      request.flush(response);

      expect(result).toEqual({ available: false });
    });

    it('should propagate an HTTP error', () => {
      let receivedError: unknown;

      service.checkUsernameAvailability('John_Doe').subscribe({
        error: (error: unknown) => {
          receivedError = error;
        },
      });

      const request = httpTestingController.expectOne(
        (req) => req.url === `${path}/username-available`,
      );

      request.flush('Server error', {
        status: 500,
        statusText: 'Internal Server Error',
      });

      expect(receivedError).toBeTruthy();
    });
  });

  describe('updateUser', () => {
    it('should send a PATCH request with the updated user', () => {
      const updatedUser: NewUserType = {
        email: 'john.updated@example.com',
        username: 'John_Updated',
        password: 'Password/1234',
      };

      const updatedResponse: User = {
        ...mockUser,
        ...updatedUser,
        updatedAt: new Date('2025-01-03T10:00:00.000Z'),
      };

      let result: User | undefined;

      service.updateUser(updatedUser).subscribe((user) => {
        result = user;
      });

      const request = httpTestingController.expectOne(`${path}/me`);

      expect(request.request.method).toBe('PATCH');
      expect(request.request.body).toEqual(updatedUser);
      expect(request.request.withCredentials).toBeTrue();

      request.flush(updatedResponse);

      expect(result).toEqual(updatedResponse);
    });

    it('should propagate an HTTP error', () => {
      const updatedUser: NewUserType = {
        email: 'john.updated@example.com',
        username: 'John_Updated',
        password: 'Password/1234',
      };

      let receivedError: unknown;

      service.updateUser(updatedUser).subscribe({
        error: (error: unknown) => {
          receivedError = error;
        },
      });

      const request = httpTestingController.expectOne(`${path}/me`);

      request.flush('Server error', {
        status: 500,
        statusText: 'Internal Server Error',
      });

      expect(receivedError).toBeTruthy();
    });
  });
});
