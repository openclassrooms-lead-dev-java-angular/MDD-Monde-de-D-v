import { Component, inject } from '@angular/core';
import {
  AbstractControl,
  AsyncValidatorFn,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { AuthService } from '@service/auth.service';
import { MatIconModule } from '@angular/material/icon';
import {
  catchError,
  debounceTime,
  map,
  Observable,
  of,
  switchMap,
  take,
} from 'rxjs';
import { InputComponent } from 'src/app/shared/components/input/input.component';
import { RegisterForm } from 'src/app/core/types/RegisterForm.type';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    InputComponent,
    ButtonComponent,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly registerForm = this.fb.group<RegisterForm>({
    email: this.fb.control('', [
      Validators.required,
      Validators.email,
      Validators.maxLength(50),
    ]),
    username: this.fb.control(
      '',
      [Validators.required, Validators.maxLength(20)],
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

  private usernameAvailabilityValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      const username = control.value?.trim();
      console.log('username : ' + username);
      if (!username || username.length < 3) {
        return of(null);
      }

      return of(username).pipe(
        debounceTime(400),
        switchMap((res) => this.authService.checkUsernameAvailability(res)),
        map((res) => (res.available ? null : { usernameTaken: true })),
        catchError(() => of(null)),
        take(1),
      );
    };
  }

  submit(): void {
    this.errorMessage = '';

    if (this.registerForm.invalid || this.registerForm.pending) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;

    const formValue = this.registerForm.getRawValue();

    this.authService
      .register({
        email: formValue.email,
        username: formValue.username,
        password: formValue.password,
      })
      .subscribe({
        next: () => {
          this.router.navigate(['/login']);
        },
        error: (error) => {
          this.errorMessage =
            error.error?.message ||
            "Une erreur est survenue lors de l'inscription.";
          this.isLoading = false;
        },
      });
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  get emailErrors(): string[] {
    const control = this.registerForm.controls.email;

    if (!control.touched) {
      return [];
    }

    const errors: string[] = [];

    if (control.hasError('required')) {
      errors.push("L'email est obligatoire.");
    }

    if (control.hasError('email')) {
      errors.push("L'email n'est pas valide.");
    }

    if (control.hasError('maxlength')) {
      errors.push('Maximum 50 caractères.');
    }

    return errors;
  }
  get usernameErrors(): string[] {
    const control = this.registerForm.controls.username;

    if (!control.touched) {
      return [];
    }

    const errors: string[] = [];

    if (control.hasError('required')) {
      errors.push("Le nom d'utilisateur est obligatoire.");
    }

    if (control.hasError('maxlength')) {
      errors.push('Maximum 20 caractères.');
    }

    if (control.hasError('usernameTaken')) {
      errors.push('Ce nom d’utilisateur est déjà utilisé.');
    }

    return errors;
  }

  get passwordErrors(): string[] {
    const control = this.registerForm.controls.password;

    if (!control.touched) {
      return [];
    }

    const errors: string[] = [];

    if (control.hasError('required')) {
      errors.push('Le mot de passe est obligatoire.');
    }

    if (control.hasError('minlength')) {
      errors.push('Minimum 8 caractères.');
    }

    return errors;
  }
}
