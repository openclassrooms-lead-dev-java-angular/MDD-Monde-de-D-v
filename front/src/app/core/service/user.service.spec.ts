import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { UserService } from './user.service';
import { environment } from '@env/environment';
import { User } from '@model/user.model';

describe('UserService', () => {
  let service: UserService;
  let httpTestingController: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/${environment.apiRoute.user}`;

  const mockUser = {
    username: 'John_Doe',
    email: 'john.doe@example.com',
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-02T00:00:00Z'),
  } as User;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UserService,
        provideHttpClientTesting
      ],
    });

    service = TestBed.inject(UserService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  describe('getMe', () => {
    it('should send a GET request to the /me endpoint', () => {
      service.getMe().subscribe();

      const req = httpTestingController.expectOne(`${apiUrl}/me`);

      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();

      req.flush(mockUser);
    });

    it('should return the current user', () => {
      let result: User | undefined;

      service.getMe().subscribe((user) => {
        result = user;
      });

      const req = httpTestingController.expectOne(`${apiUrl}/me`);

      req.flush(mockUser);

      expect(result).toEqual(mockUser);
    });

    it('should propagate HTTP errors', () => {
      let receivedError: unknown;

      service.getMe().subscribe({
        error: (error: unknown) => {
          receivedError = error;
        },
      });

      const req = httpTestingController.expectOne(`${apiUrl}/me`);

      req.flush('Internal Server Error', {
        status: 500,
        statusText: 'Internal Server Error',
      });

      expect(receivedError).toBeDefined();
    });
  });

  describe('updateUser', () => {
    it('should send a PUT request to the user endpoint', () => {
      service.updateUser(mockUser).subscribe();

      const req = httpTestingController.expectOne(apiUrl);

      expect(req.request.method).toBe('PUT');
      expect(req.request.withCredentials).toBeTrue();

      req.flush(mockUser);
    });

    it('should send the user as the request body', () => {
      service.updateUser(mockUser).subscribe();

      const req = httpTestingController.expectOne(apiUrl);

      expect(req.request.body).toEqual(mockUser);

      req.flush(mockUser);
    });

    it('should return the updated user', () => {
      let result: User | undefined;

      service.updateUser(mockUser).subscribe((user) => {
        result = user;
      });

      const req = httpTestingController.expectOne(apiUrl);

      req.flush(mockUser);

      expect(result).toEqual(mockUser);
    });

    it('should propagate HTTP errors', () => {
      let receivedError: unknown;

      service.updateUser(mockUser).subscribe({
        error: (error: unknown) => {
          receivedError = error;
        },
      });

      const req = httpTestingController.expectOne(apiUrl);

      req.flush('Bad Request', {
        status: 400,
        statusText: 'Bad Request',
      });

      expect(receivedError).toBeDefined();
    });
  });
});
