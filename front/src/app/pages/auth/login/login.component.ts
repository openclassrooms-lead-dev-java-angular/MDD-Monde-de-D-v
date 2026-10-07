import { Component, inject } from '@angular/core';
import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { AuthService } from '@service/auth.service';
import { UserStateService } from '@service/user-state.service';
import { UserService } from '@service/user.service';
import { switchMap } from 'rxjs';
import { LoginForm } from 'src/app/core/types/LoginForm.type';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { InputComponent } from 'src/app/shared/components/input/input.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ButtonComponent,
    InputComponent,
    ReactiveFormsModule,
    MatIconModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly userStateService = inject(UserStateService);
  private readonly userService = inject(UserService);

  readonly loginForm = this.fb.group<LoginForm>({
    usernameOrEmail: this.fb.control('', [Validators.required]),
    password: this.fb.control('', [Validators.required]),
  });

  isLoading = false;
  errorMessage = '';

  submit(): void {
    console.log(this.loginForm.value);
    this.errorMessage = '';

    if (this.loginForm.invalid || this.loginForm.pending) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;

    const formValue = this.loginForm.getRawValue();

    this.authService
      .login({
        usernameOrEmail: formValue.usernameOrEmail,
        password: formValue.password,
      })
      .pipe(
        switchMap(() => this.userService.getMe())
      )
      .subscribe({
        next: (user) => {
          this.userStateService.setUser(user);
          this.isLoading = false;

          this.router.navigate(['/feed']);
        },

        error: (error) => {
          console.error('Login error:', error);

          this.isLoading = false;
          this.errorMessage =
            'Erreur lors de la connexion. Veuillez réessayer.';
        },
      });
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  get usernameOrEmailErrors(): string[] {
    const control = this.loginForm.controls.usernameOrEmail;

    if (!control.touched) {
      return [];
    }

    const errors: string[] = [];

    if (control.hasError('required')) {
      errors.push("L'email est obligatoire.");
    }

    return errors;
  }

  get passwordErrors(): string[] {
    const control = this.loginForm.controls.password;

    if (!control.touched) {
      return [];
    }

    const errors: string[] = [];

    if (control.hasError('required')) {
      errors.push('Le mot de passe est obligatoire.');
    }

    return errors;
  }
}
