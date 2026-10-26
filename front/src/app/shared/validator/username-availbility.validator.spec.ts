import { FormControl, ValidationErrors } from '@angular/forms';
import { fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '@service/auth.service';
import { AvailableResponse } from '@type/available-response.type';


describe('usernameAvailabilityValidator', () => {
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', [
      'checkUsernameAvailability',
    ]);
  });

  it('should return null when the username has fewer than 3 characters', () => {
    const validator = usernameAvailabilityValidator(authService);
    const control = new FormControl('ab');
    let result: ValidationErrors | null | undefined;

    validator(control).subscribe((value) => {
      result = value;
    });

    expect(result).toBeNull();
    expect(authService.checkUsernameAvailability).not.toHaveBeenCalled();
  });

  it('should return null when the username is empty', () => {
    const validator = usernameAvailabilityValidator(authService);
    const control = new FormControl('');

    let result: ValidationErrors | null | undefined;

    validator(control).subscribe((value) => {
      result = value;
    });

    expect(result).toBeNull();
    expect(authService.checkUsernameAvailability).not.toHaveBeenCalled();
  });

  it('should wait 400 milliseconds before checking username availability', fakeAsync(() => {
    authService.checkUsernameAvailability.and.returnValue(
      of({ available: true } as AvailableResponse),
    );

    const validator = usernameAvailabilityValidator(authService);
    const control = new FormControl('alice');

    let result: ValidationErrors | null | undefined;

    validator(control).subscribe((value) => {
      result = value;
    });

    tick(399);

    expect(authService.checkUsernameAvailability).not.toHaveBeenCalled();

    tick(1);

    expect(authService.checkUsernameAvailability).toHaveBeenCalledOnceWith(
      'alice',
    );
    expect(result).toBeNull();
  }));

  it('should return null when the username is available', fakeAsync(() => {
    authService.checkUsernameAvailability.and.returnValue(
      of({ available: true } as AvailableResponse),
    );

    const validator = usernameAvailabilityValidator(authService);
    const control = new FormControl('alice');

    let result: ValidationErrors | null | undefined;

    validator(control).subscribe((value) => {
      result = value;
    });

    tick(400);

    expect(result).toBeNull();
    expect(authService.checkUsernameAvailability).toHaveBeenCalledOnceWith(
      'alice',
    );
  }));

  it('should return usernameTaken when the username is already taken', fakeAsync(() => {
    authService.checkUsernameAvailability.and.returnValue(
      of({ available: false } as AvailableResponse),
    );

    const validator = usernameAvailabilityValidator(authService);
    const control = new FormControl('alice');

    let result: ValidationErrors | null | undefined;

    validator(control).subscribe((value) => {
      result = value;
    });

    tick(400);

    expect(result).toEqual({ usernameTaken: true });
  }));

  it('should trim whitespace before checking username availability', fakeAsync(() => {
    authService.checkUsernameAvailability.and.returnValue(
      of({ available: true } as AvailableResponse),
    );

    const validator = usernameAvailabilityValidator(authService);
    const control = new FormControl('  alice  ');

    validator(control).subscribe();

    tick(400);

    expect(authService.checkUsernameAvailability).toHaveBeenCalledOnceWith(
      'alice',
    );
  }));

  it('should return usernameCheckFailed when the service throws an error', fakeAsync(() => {
    const serviceError = new Error('Network error');
    const consoleErrorSpy = spyOn(console, 'error');

    authService.checkUsernameAvailability.and.returnValue(
      throwError(() => serviceError),
    );

    const validator = usernameAvailabilityValidator(authService);
    const control = new FormControl('alice');

    let result: ValidationErrors | null | undefined;

    validator(control).subscribe((value) => {
      result = value;
    });

    tick(400);

    expect(result).toEqual({ usernameCheckFailed: true });
    expect(consoleErrorSpy).toHaveBeenCalledWith(
      'Erreur de vérification du nom utilisateur :',
      serviceError,
    );
  }));

  it('should check the username only once per validation', fakeAsync(() => {
    authService.checkUsernameAvailability.and.returnValue(
      of({ available: true } as AvailableResponse),
    );

    const validator = usernameAvailabilityValidator(authService);
    const control = new FormControl('alice');

    let emissionCount = 0;

    validator(control).subscribe(() => {
      emissionCount++;
    });

    tick(400);

    expect(emissionCount).toBe(1);
    expect(authService.checkUsernameAvailability).toHaveBeenCalledTimes(1);
  }));
});
