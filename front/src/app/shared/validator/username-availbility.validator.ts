import {
  AbstractControl,
  AsyncValidatorFn,
  ValidationErrors,
} from '@angular/forms';
import { AuthService } from '@service/auth.service';
import { AvailableResponse } from '@type/available-response.type';
import { catchError, map, Observable, of, switchMap, take, timer } from 'rxjs';

export const usernameAvailabilityValidator = (
  authService: AuthService,
): AsyncValidatorFn => {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const username = (control.value as string).trim();

    if (username.length < 3) {
      return of(null);
    }

    return timer(400).pipe(
      switchMap(() => authService.checkUsernameAvailability(username)),
      map((response: AvailableResponse) =>
        response.available ? null : { usernameTaken: true },
      ),
      catchError((error: unknown) => {
        console.error('Erreur de vérification du nom utilisateur :', error);

        return of({ usernameCheckFailed: true });
      }),
      take(1),
    );
  };
};
