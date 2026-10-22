import { TestBed } from '@angular/core/testing';

import { UserStateService } from './user-state.service';
import { User } from '@model/user.model';

describe('UserStateService', () => {
  let service: UserStateService;

  const mockUser = {
    username: 'John_Doe',
    email: 'john.doe@example.com',
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-02T00:00:00Z'),
  } as User;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UserStateService],
    });

    service = TestBed.inject(UserStateService);
  });

  describe('initial state', () => {
    it('should initialize user to null', () => {
      expect(service.user()).toBeNull();
    });

    it('should initialize isLogged to false', () => {
      expect(service.isLogged()).toBeFalse();
    });
  });

  describe('setUser', () => {
    it('should set the current user', () => {
      service.setUser(mockUser);

      expect(service.user()).toEqual(mockUser);
    });

    it('should set isLogged to true when a user is set', () => {
      service.setUser(mockUser);

      expect(service.isLogged()).toBeTrue();
    });

    it('should replace the current user when another user is set', () => {
      const anotherUser = {
        ...mockUser,
        id: '456',
        email: 'jane.doe@example.com',
      } as User;

      service.setUser(mockUser);
      service.setUser(anotherUser);

      expect(service.user()).toEqual(anotherUser);
      expect(service.isLogged()).toBeTrue();
    });
  });

  describe('clearUser', () => {
    it('should reset the current user to null', () => {
      service.setUser(mockUser);

      service.clearUser();

      expect(service.user()).toBeNull();
    });

    it('should set isLogged to false after clearing the user', () => {
      service.setUser(mockUser);

      service.clearUser();

      expect(service.isLogged()).toBeFalse();
    });

    it('should keep isLogged false when clearing an already empty state', () => {
      service.clearUser();

      expect(service.user()).toBeNull();
      expect(service.isLogged()).toBeFalse();
    });
  });

  describe('reactivity', () => {
    it('should update isLogged reactively when the user changes', () => {
      expect(service.isLogged()).toBeFalse();

      service.setUser(mockUser);

      expect(service.isLogged()).toBeTrue();

      service.clearUser();

      expect(service.isLogged()).toBeFalse();
    });
  });
});
