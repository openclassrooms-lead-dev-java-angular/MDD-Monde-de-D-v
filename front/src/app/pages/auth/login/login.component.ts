import { Component, inject } from '@angular/core';
import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '@service/auth.service';
import { UserStateService } from '@service/user-state.service';
import { UserService } from '@service/user.service';
import { finalize, switchMap } from 'rxjs';
import { LoginForm } from '@type/login-form.type';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ButtonComponent,
    ReactiveFormsModule,
    MatInputModule,
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

  get usernameOrEmailErrors(): string[] {
    const control = this.loginForm.controls.usernameOrEmail;

    if (!control.touched && !control.dirty) {
      return [];
    }

    return control.hasError('required')
      ? ["L'email ou le nom d'utilisateur est obligatoire."]
      : [];
  }

  get passwordErrors(): string[] {
    const control = this.loginForm.controls.password;

    if (!control.touched && !control.dirty) {
      return [];
    }

    return control.hasError('required')
      ? ['Le mot de passe est obligatoire.']
      : [];
  }

  submit(): void {
    this.errorMessage = '';

    if (this.loginForm.invalid || this.loginForm.pending || this.isLoading) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;

    const formValue = this.loginForm.getRawValue();

    this.authService
      .login(formValue)
      .pipe(
        switchMap(() => this.userService.getMe()),
        finalize(() => {
          this.isLoading = false;
        }),
      )
      .subscribe({
        next: (user) => {
          this.userStateService.setUser(user);
          void this.router.navigate(['/feed']);
        },
        error: (error: unknown) => {
          console.error('Login error:', error);

          this.errorMessage =
            'Erreur lors de la connexion. Vérifiez vos identifiants et réessayez.';
        },
      });
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
