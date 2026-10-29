import { AbstractControl, FormControl, ValidationErrors } from '@angular/forms';
import { fakeAsync, tick } from '@angular/core/testing';
import { Observable, of, throwError } from 'rxjs';

import { AuthService } from '@service/auth.service';
import { UserService } from '@service/user.service';
import { usernameAvailabilityValidator } from './username-availbility.validator';
import { AvailableResponse } from '@type/available-response.type';

describe('usernameAvailabilityValidator', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let userService: jasmine.SpyObj<UserService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>(
      'AuthService',
      ['checkUsernameAvailability'],
    );

    userService = jasmine.createSpyObj<UserService>(
      'UserService',
      ['checkUsernameAvailability'],
    );
  });

  function runValidator(
    service: AuthService | UserService,
    control: AbstractControl,
  ): Observable<ValidationErrors | null> {
    return usernameAvailabilityValidator(service)(control) as Observable<
      ValidationErrors | null
    >;
  }

  it('should return null when the username has fewer than 3 characters', fakeAsync(() => {
    const control = new FormControl(' ab ');
    let result: ValidationErrors | null | undefined;

    runValidator(authService, control).subscribe((value) => {
      result = value;
    });

    tick(400);

    expect(result).toBeNull();
    expect(authService.checkUsernameAvailability).not.toHaveBeenCalled();
  }));

  it('should trim the username before checking availability', fakeAsync(() => {
    authService.checkUsernameAvailability.and.returnValue(
      of({ available: true }),
    );

    const control = new FormControl('  John_Doe  ');

    runValidator(authService, control).subscribe();

    tick(400);

    expect(authService.checkUsernameAvailability).toHaveBeenCalledOnceWith(
      'John_Doe',
    );
  }));

  it('should wait 400 ms before calling the service', fakeAsync(() => {
    authService.checkUsernameAvailability.and.returnValue(
      of({ available: true }),
    );

    runValidator(authService, new FormControl('John_Doe')).subscribe();

    tick(399);

    expect(authService.checkUsernameAvailability).not.toHaveBeenCalled();

    tick(1);

    expect(authService.checkUsernameAvailability).toHaveBeenCalledOnceWith(
      'John_Doe',
    );
  }));

  it('should return null when the username is available', fakeAsync(() => {
    const response: AvailableResponse = { available: true };
    authService.checkUsernameAvailability.and.returnValue(of(response));

    let result: ValidationErrors | null | undefined;

    runValidator(authService, new FormControl('John_Doe')).subscribe((value) => {
      result = value;
    });

    tick(400);

    expect(result).toBeNull();
  }));

  it('should return usernameTaken when the username is unavailable', fakeAsync(() => {
    const response: AvailableResponse = { available: false };
    authService.checkUsernameAvailability.and.returnValue(of(response));

    let result: ValidationErrors | null | undefined;

    runValidator(authService, new FormControl('John_Doe')).subscribe((value) => {
      result = value;
    });

    tick(400);

    expect(result).toEqual({ usernameTaken: true });
  }));

  it('should return usernameCheckFailed when the service errors', fakeAsync(() => {
    const error = new Error('Server error');

    authService.checkUsernameAvailability.and.returnValue(
      throwError(() => error),
    );

    spyOn(console, 'error');

    let result: ValidationErrors | null | undefined;

    runValidator(authService, new FormControl('John_Doe')).subscribe((value) => {
      result = value;
    });

    tick(400);

    expect(result).toEqual({ usernameCheckFailed: true });
    expect(console.error).toHaveBeenCalled();
  }));

  it('should also work with UserService', fakeAsync(() => {
    userService.checkUsernameAvailability.and.returnValue(
      of({ available: true }),
    );

    let result: ValidationErrors | null | undefined;

    runValidator(userService, new FormControl('John_Doe')).subscribe((value) => {
      result = value;
    });

    tick(400);

    expect(userService.checkUsernameAvailability).toHaveBeenCalledOnceWith(
      'John_Doe',
    );
    expect(result).toBeNull();
  }));

  it('should not call the service if the validation subscription is cancelled', fakeAsync(() => {
    authService.checkUsernameAvailability.and.returnValue(
      of({ available: true }),
    );

    const subscription = runValidator(
      authService,
      new FormControl('John_Doe'),
    ).subscribe();

    subscription.unsubscribe();

    tick(400);

    expect(authService.checkUsernameAvailability).not.toHaveBeenCalled();
  }));
});