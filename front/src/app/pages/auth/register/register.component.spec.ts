import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { Router } from '@angular/router';
import { AuthService } from '@service/auth.service';
import { of, throwError } from 'rxjs';
import { HttpResponse } from '@angular/common/http';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;

  let router: jasmine.SpyObj<Router>;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    authService = jasmine.createSpyObj<AuthService>('AuthService', [
      'register',
      'checkUsernameAvailability',
    ]);

    authService.register.and.returnValue(
      of(new HttpResponse<void>({ status: 201 })),
    );

    authService.checkUsernameAvailability.and.returnValue(
      of({ available: true }),
    );

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        { provide: Router, useValue: router },
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('registerForm', () => {
    it('should be invalid when empty', () => {
      expect(component.registerForm.invalid).toBeTrue();
    });

    it('should be valid with valid values', fakeAsync(() => {
      component.registerForm.setValue({
        email: 'john@example.com',
        username: 'john',
        password: 'Password1!',
      });

      tick(400);
      fixture.detectChanges();

      expect(component.registerForm.valid).toBeTrue();
    }));
  });

  describe('emailErrors', () => {
    it('should return no error initially', () => {
      expect(component.emailErrors).toEqual([]);
    });

    it('should return required error', () => {
      const control = component.registerForm.controls.email;
      control.markAsTouched();

      expect(component.emailErrors).toEqual([
        "L'adresse e-mail est obligatoire.",
      ]);
    });

    it('should return invalid email error', () => {
      const control = component.registerForm.controls.email;
      control.setValue('invalid-email');
      control.markAsTouched();

      expect(component.emailErrors).toEqual([
        "L'adresse e-mail n'est pas valide.",
      ]);
    });

    it('should return maxlength error', () => {
      const control = component.registerForm.controls.email;
      control.setValue(`${'a'.repeat(45)}@example.com`);
      control.markAsTouched();

      expect(component.emailErrors).toEqual(['Maximum 50 caractères.']);
    });
  });

  describe('usernameErrors', () => {
    it('should return required error', () => {
      const control = component.registerForm.controls.username;
      control.markAsTouched();

      expect(component.usernameErrors).toEqual([
        "Le nom d'utilisateur est obligatoire.",
      ]);
    });

    it('should return minlength error', () => {
      const control = component.registerForm.controls.username;
      control.setValue('ab');
      control.markAsTouched();

      expect(component.usernameErrors).toEqual([
        'Le nom d’utilisateur doit contenir au moins 3 caractères.',
      ]);
    });

    it('should return maxlength error', () => {
      const control = component.registerForm.controls.username;
      control.setValue('a'.repeat(21));
      control.markAsTouched();

      expect(component.usernameErrors).toEqual(['Maximum 20 caractères.']);
    });

    it('should return username taken error', () => {
      const control = component.registerForm.controls.username;
      control.setErrors({ usernameTaken: true });
      control.markAsTouched();

      expect(component.usernameErrors).toEqual([
        'Ce nom d’utilisateur est déjà utilisé.',
      ]);
    });
  });

  describe('passwordErrors', () => {
    it('should return required error', () => {
      const control = component.registerForm.controls.password;
      control.markAsTouched();

      expect(component.passwordErrors).toEqual([
        'Le mot de passe est obligatoire.',
      ]);
    });

    it('should return all missing constraints', () => {
      const control = component.registerForm.controls.password;
      control.setValue('abc');
      control.markAsTouched();

      expect(component.passwordErrors).toEqual([
        'Minimum 8 caractères.',
        'Au moins une lettre majuscule.',
        'Au moins un chiffre.',
        'Au moins un caractère spécial.',
      ]);
    });

    it('should return no error with a valid password', () => {
      const control = component.registerForm.controls.password;
      control.setValue('Password1!');
      control.markAsTouched();

      expect(component.passwordErrors).toEqual([]);
    });
  });

  describe('usernameAvailabilityValidator', () => {
    it('should not call API for username shorter than 3 characters', fakeAsync(() => {
      const control = component.registerForm.controls.username;

      control.setValue('ab');
      tick(400);

      expect(authService.checkUsernameAvailability).not.toHaveBeenCalled();
      expect(control.errors).toBeNull();
    }));

    it('should validate an available username', fakeAsync(() => {
      authService.checkUsernameAvailability.and.returnValue(
        of({ available: true }),
      );

      const control = component.registerForm.controls.username;
      control.setValue('john');

      tick(400);
      fixture.detectChanges();

      expect(authService.checkUsernameAvailability).toHaveBeenCalledOnceWith(
        'john',
      );
      expect(control.errors).toBeNull();
    }));

    it('should invalidate an unavailable username', fakeAsync(() => {
      authService.checkUsernameAvailability.and.returnValue(
        of({ available: false }),
      );

      const control = component.registerForm.controls.username;
      control.setValue('john');

      tick(400);
      fixture.detectChanges();

      expect(control.hasError('usernameTaken')).toBeTrue();
    }));

    it('should ignore API errors', fakeAsync(() => {
      spyOn(console, 'error');

      authService.checkUsernameAvailability.and.returnValue(
        throwError(() => new Error('Network')),
      );

      const control = component.registerForm.controls.username;
      control.setValue('john');

      tick(400);
      fixture.detectChanges();

      expect(console.error).toHaveBeenCalled();
      expect(control.errors).toBeNull();
    }));
  });

  describe('submit', () => {
    beforeEach(fakeAsync(() => {
      component.registerForm.setValue({
        email: 'john@example.com',
        username: 'john',
        password: 'Password1!',
      });

      tick(400);
      fixture.detectChanges();
    }));

    it('should not submit when form is invalid', () => {
      component.registerForm.controls.email.setValue('');

      component.submit();

      expect(authService.register).not.toHaveBeenCalled();
      expect(component.registerForm.controls.email.touched).toBeTrue();
    });

    it('should not submit when form is pending', () => {
      component.registerForm.controls.username.markAsPending();

      component.submit();

      expect(authService.register).not.toHaveBeenCalled();
    });

    it('should not submit when already loading', () => {
      component.isLoading = true;

      component.submit();

      expect(authService.register).not.toHaveBeenCalled();
    });

    it('should call register with form values', () => {
      component.submit();

      expect(authService.register).toHaveBeenCalledOnceWith({
        email: 'john@example.com',
        username: 'john',
        password: 'Password1!',
      });
    });

    it('should navigate to login after successful registration', () => {
      component.submit();

      expect(router.navigate).toHaveBeenCalledOnceWith(['/login']);
      expect(component.isLoading).toBeFalse();
    });

    it('should display backend error message', () => {
      authService.register.and.returnValue(
        throwError(() => ({
          error: {
            message: 'Email déjà utilisé',
          },
        })),
      );

      spyOn(console, 'error');

      component.submit();

      expect(component.errorMessage).toBe('Email déjà utilisé');
      expect(component.isLoading).toBeFalse();
      expect(console.error).toHaveBeenCalled();
    });

    it('should display default error message', () => {
      authService.register.and.returnValue(throwError(() => ({})));

      spyOn(console, 'error');

      component.submit();

      expect(component.errorMessage).toBe(
        "Une erreur est survenue lors de l'inscription.",
      );
    });

    it('should clear previous error message', () => {
      component.errorMessage = 'Ancienne erreur';

      component.submit();

      expect(component.errorMessage).toBe('');
    });
  });

  describe('goBack', () => {
    it('should navigate to home', () => {
      component.goBack();

      expect(router.navigate).toHaveBeenCalledOnceWith(['/']);
    });
  });
});
