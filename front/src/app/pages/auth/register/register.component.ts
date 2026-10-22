import { Component, inject } from '@angular/core';
import {
  AbstractControl,
  AsyncValidatorFn,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '@service/auth.service';
import {
  catchError,
  finalize,
  map,
  Observable,
  of,
  switchMap,
  take,
  timer,
} from 'rxjs';
import { AvailableResponse } from '@type/available-response.type';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ButtonComponent,
    ReactiveFormsModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly registerForm = this.fb.group({
    email: this.fb.control('', [
      Validators.required,
      Validators.email,
      Validators.maxLength(50),
    ]),
    username: this.fb.control(
      '',
      [Validators.required, Validators.minLength(3), Validators.maxLength(20)],
      [this.usernameAvailabilityValidator()],
    ),
    password: this.fb.control('', [
      Validators.required,
      Validators.minLength(8),
      Validators.pattern(/[A-Z]/),
      Validators.pattern(/[a-z]/),
      Validators.pattern(/\d/),
      Validators.pattern(/[^a-zA-Z0-9]/),
    ]),
  });

  isLoading = false;
  errorMessage = '';

  get emailErrors(): string[] {
    const control = this.registerForm.controls.email;

    if (!control.touched && !control.dirty) {
      return [];
    }

    if (control.hasError('required')) {
      return ["L'adresse e-mail est obligatoire."];
    }

    if (control.hasError('email')) {
      return ["L'adresse e-mail n'est pas valide."];
    }

    if (control.hasError('maxlength')) {
      return ['Maximum 50 caractères.'];
    }

    return [];
  }

  get usernameErrors(): string[] {
    const control = this.registerForm.controls.username;

    if (!control.touched && !control.dirty) {
      return [];
    }

    if (control.hasError('required')) {
      return ["Le nom d'utilisateur est obligatoire."];
    }

    if (control.hasError('minlength')) {
      return ['Le nom d’utilisateur doit contenir au moins 3 caractères.'];
    }

    if (control.hasError('maxlength')) {
      return ['Maximum 20 caractères.'];
    }

    if (control.hasError('usernameTaken')) {
      return ['Ce nom d’utilisateur est déjà utilisé.'];
    }

    return [];
  }

  get passwordErrors(): string[] {
    const control = this.registerForm.controls.password;

    if (!control.touched && !control.dirty) {
      return [];
    }

    const password = control.value;
    const errors: string[] = [];

    if (control.hasError('required')) {
      return ['Le mot de passe est obligatoire.'];
    }

    if (control.hasError('minlength')) {
      errors.push('Minimum 8 caractères.');
    }

    if (!/[A-Z]/.test(password)) {
      errors.push('Au moins une lettre majuscule.');
    }

    if (!/[a-z]/.test(password)) {
      errors.push('Au moins une lettre minuscule.');
    }

    if (!/\d/.test(password)) {
      errors.push('Au moins un chiffre.');
    }

    if (!/[^a-zA-Z0-9]/.test(password)) {
      errors.push('Au moins un caractère spécial.');
    }

    if (control.hasError('usernameCheckFailed')) {
      return ['Impossible de vérifier le nom d’utilisateur. Réessayez.'];
    }

    return errors;
  }

  get isUsernameLoading(): boolean {
    return this.registerForm.controls.username.pending;
  }

  protected usernameAvailabilityValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      const username = (control.value as string).trim();

      if (username.length < 3) {
        return of(null);
      }

      return timer(400).pipe(
        switchMap(() => this.authService.checkUsernameAvailability(username)),
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
  }

  submit(): void {
    this.errorMessage = '';

    if (
      this.registerForm.invalid ||
      this.registerForm.pending ||
      this.isLoading
    ) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;

    const { email, username, password } = this.registerForm.getRawValue();

    this.authService
      .register({ email, username, password })
      .pipe(
        finalize(() => {
          this.isLoading = false;
        }),
      )
      .subscribe({
        next: () => {
          void this.router.navigate(['/login']);
        },
        error: (error: { error?: { message?: string } }) => {
          console.error('Erreur lors de l’inscription :', error);

          this.errorMessage =
            error.error?.message ??
            "Une erreur est survenue lors de l'inscription.";
        },
      });
  }

  goBack(): void {
    void this.router.navigate(['/']);
  }
}
