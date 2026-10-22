import { Component, inject, signal } from '@angular/core';
import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '@service/auth.service';
import { finalize } from 'rxjs';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { ValidationErrorService } from '@service/validation.service';
import { usernameAvailabilityValidator } from '@shared/validator/username-availbility.validator';

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
  private readonly validationErrorService = inject(ValidationErrorService);

  readonly registerForm = this.fb.group({
    email: this.fb.control('', [
      Validators.required,
      Validators.email,
      Validators.maxLength(50),
    ]),
    username: this.fb.control(
      '',
      [Validators.required, Validators.minLength(3), Validators.maxLength(20)],
      [usernameAvailabilityValidator(this.authService)],
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

  isLoading = signal<boolean>(false);
  errorMessage = signal<string>('');

  get emailErrors(): string[] {
    return this.validationErrorService.getErrors(
      this.registerForm.controls.email,
      'email',
    );
  }

  get usernameErrors(): string[] {
    return this.validationErrorService.getErrors(
      this.registerForm.controls.username,
      'username',
    );
  }

  get passwordErrors(): string[] {
    return this.validationErrorService.getErrors(
      this.registerForm.controls.password,
      'password',
    );
  }

  get isUsernameLoading(): boolean {
    return this.registerForm.controls.username.pending;
  }

  submit(): void {
    this.errorMessage.set('');

    if (
      this.registerForm.invalid ||
      this.registerForm.pending ||
      this.isLoading()
    ) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const { email, username, password } = this.registerForm.getRawValue();

    this.authService
      .register({ email, username, password })
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
        }),
      )
      .subscribe({
        next: () => {
          void this.router.navigate(['/login']);
        },
        error: (error: { error?: { message?: string } }) => {
          console.error('Erreur lors de l’inscription :', error);

          this.errorMessage.set(
            "Une erreur est survenue lors de l'inscription.",
          );
        },
      });
  }

  goBack(): void {
    void this.router.navigate(['/']);
  }
}
