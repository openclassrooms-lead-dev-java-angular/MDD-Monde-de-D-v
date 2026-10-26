import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { Router } from '@angular/router';
import { AuthService } from '@service/auth.service';
import { of, Subject, throwError } from 'rxjs';
import { ValidationErrorService } from '@service/validation.service';
import { HttpResponse } from '@angular/common/http';
import { AvailableResponse } from '@type/available-response.type';

describe('RegisterComponent', () => {
  let fixture: ComponentFixture<RegisterComponent>;
  let component: RegisterComponent;

  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let validationErrorService: jasmine.SpyObj<ValidationErrorService>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', [
      'register',
      'checkUsernameAvailability',
    ]);

    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    validationErrorService = jasmine.createSpyObj<ValidationErrorService>(
      'ValidationErrorService',
      ['getErrors'],
    );

    // Use typed observable mocks without depending on the concrete response models.
    authService.register.and.returnValue(
      of(new HttpResponse<void>({ body: null })),
    );
    authService.checkUsernameAvailability.and.returnValue(
      of({ available: true } as AvailableResponse),
    );

    router.navigate.and.returnValue(Promise.resolve(true));

    validationErrorService.getErrors.and.returnValue([]);

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        {
          provide: ValidationErrorService,
          useValue: validationErrorService,
        },
      ],
    })
      .overrideComponent(RegisterComponent, {
        set: {
          template: '',
          imports: [],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
  });

  function initializeComponent(): void {
    fixture.detectChanges();
  }

  function prepareValidForm(): void {
    const { email, username, password } = component.registerForm.controls;

    username.clearAsyncValidators();
    username.setValue('johnDoe');

    email.setValue('john@example.com');

    password.setValue('Password123!');
    password.updateValueAndValidity();
  }

  describe('creation', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize the form with empty values', () => {
      expect(component.registerForm.getRawValue()).toEqual({
        email: '',
        username: '',
        password: '',
      });
    });

    it('should initialize loading and error signals', () => {
      expect(component.isLoading()).toBeFalse();
      expect(component.errorMessage()).toBe('');
    });
  });

  describe('email validation', () => {
    it('should reject an empty email', () => {
      const email = component.registerForm.controls.email;

      email.setValue('');

      expect(email.hasError('required')).toBeTrue();
    });

    it('should reject an invalid email format', () => {
      const email = component.registerForm.controls.email;

      email.setValue('invalid-email');

      expect(email.hasError('email')).toBeTrue();
    });

    it('should reject an email longer than 50 characters', () => {
      const email = component.registerForm.controls.email;

      email.setValue(`${'a'.repeat(40)}@example.com`);

      expect(email.hasError('maxlength')).toBeTrue();
    });

    it('should accept a valid email', () => {
      const email = component.registerForm.controls.email;

      email.setValue('john@example.com');

      expect(email.valid).toBeTrue();
    });
  });

  describe('username validation', () => {
    it('should reject an empty username', () => {
      const username = component.registerForm.controls.username;

      username.clearAsyncValidators();
      username.setValue('');

      expect(username.hasError('required')).toBeTrue();
    });

    it('should reject a username shorter than 3 characters', () => {
      const username = component.registerForm.controls.username;

      username.clearAsyncValidators();
      username.setValue('ab');

      expect(username.hasError('minlength')).toBeTrue();
    });

    it('should reject a username longer than 20 characters', () => {
      const username = component.registerForm.controls.username;

      username.clearAsyncValidators();
      username.setValue('a'.repeat(21));

      expect(username.hasError('maxlength')).toBeTrue();
    });

    it('should accept a username with a valid length', () => {
      const username = component.registerForm.controls.username;

      username.clearAsyncValidators();
      username.setValue('johnDoe');

      expect(username.valid).toBeTrue();
    });

    it('should expose the pending state of the username validator', () => {
      component.registerForm.controls.username.markAsPending();

      expect(component.isUsernameLoading).toBeTrue();
    });

    it('should return false when username validation is not pending', () => {
      const username = component.registerForm.controls.username;

      username.clearAsyncValidators();
      username.setValue('johnDoe');

      expect(component.isUsernameLoading).toBeFalse();
    });
  });

  describe('password validation', () => {
    it('should reject an empty password', () => {
      const password = component.registerForm.controls.password;

      password.setValue('');

      expect(password.hasError('required')).toBeTrue();
    });

    it('should reject a password shorter than 8 characters', () => {
      const password = component.registerForm.controls.password;

      password.setValue('Ab1!xyz');

      expect(password.hasError('minlength')).toBeTrue();
    });

    it('should require an uppercase letter', () => {
      const password = component.registerForm.controls.password;

      password.setValue('password123!');

      expect(password.hasError('pattern')).toBeTrue();
    });

    it('should require a lowercase letter', () => {
      const password = component.registerForm.controls.password;

      password.setValue('PASSWORD123!');

      expect(password.hasError('pattern')).toBeTrue();
    });

    it('should require a digit', () => {
      const password = component.registerForm.controls.password;

      password.setValue('PasswordTest!');

      expect(password.hasError('pattern')).toBeTrue();
    });

    it('should require a special character', () => {
      const password = component.registerForm.controls.password;

      password.setValue('Password123');

      expect(password.hasError('pattern')).toBeTrue();
    });

    it('should accept a password satisfying all requirements', () => {
      const password = component.registerForm.controls.password;

      password.setValue('Password123!');

      expect(password.valid).toBeTrue();
    });
  });

  describe('validation error getters', () => {
    it('should return email errors', () => {
      validationErrorService.getErrors.and.returnValue(['Email invalide']);

      expect(component.emailErrors).toEqual(['Email invalide']);

      expect(validationErrorService.getErrors).toHaveBeenCalledWith(
        component.registerForm.controls.email,
        'email',
      );
    });

    it('should return username errors', () => {
      validationErrorService.getErrors.and.returnValue([
        'Nom utilisateur invalide',
      ]);

      expect(component.usernameErrors).toEqual(['Nom utilisateur invalide']);

      expect(validationErrorService.getErrors).toHaveBeenCalledWith(
        component.registerForm.controls.username,
        'username',
      );
    });

    it('should return password errors', () => {
      validationErrorService.getErrors.and.returnValue([
        'Mot de passe invalide',
      ]);

      expect(component.passwordErrors).toEqual(['Mot de passe invalide']);

      expect(validationErrorService.getErrors).toHaveBeenCalledWith(
        component.registerForm.controls.password,
        'password',
      );
    });
  });

  describe('submit', () => {
    beforeEach(() => {
      initializeComponent();
      prepareValidForm();
    });

    it('should register the user with the form values', () => {
      component.submit();

      expect(authService.register).toHaveBeenCalledOnceWith({
        email: 'john@example.com',
        username: 'johnDoe',
        password: 'Password123!',
      });
    });

    it('should clear the previous error message', () => {
      component.errorMessage.set('Previous error');

      component.submit();

      expect(component.errorMessage()).toBe('');
    });

    it('should not submit an invalid form', () => {
      component.registerForm.controls.email.setValue('');

      component.submit();

      expect(authService.register).not.toHaveBeenCalled();
      expect(component.registerForm.controls.email.touched).toBeTrue();
    });

    it('should not submit when the form is pending', () => {
      component.registerForm.controls.username.markAsPending();

      component.submit();

      expect(authService.register).not.toHaveBeenCalled();
    });

    it('should not submit while another request is in progress', () => {
      component.isLoading.set(true);

      component.submit();

      expect(authService.register).not.toHaveBeenCalled();
    });

    it('should set loading while the request is pending', () => {
      const request = new Subject<never>();

      authService.register.and.returnValue(request.asObservable());

      component.submit();

      expect(component.isLoading()).toBeTrue();

      request.complete();

      expect(component.isLoading()).toBeFalse();
    });

    it('should navigate to the login page after successful registration', () => {
      component.submit();

      expect(router.navigate).toHaveBeenCalledOnceWith(['/login']);
    });

    it('should reset loading after successful registration', () => {
      component.submit();

      expect(component.isLoading()).toBeFalse();
    });

    it('should display an error message when registration fails', () => {
      spyOn(console, 'error');

      authService.register.and.returnValue(
        throwError(() => ({
          error: { message: 'Server error' },
        })),
      );

      component.submit();

      expect(component.errorMessage()).toBe(
        "Une erreur est survenue lors de l'inscription.",
      );

      expect(component.isLoading()).toBeFalse();
      expect(console.error).toHaveBeenCalled();
    });

    it('should not navigate after registration fails', () => {
      authService.register.and.returnValue(
        throwError(() => new Error('Server error')),
      );

      component.submit();

      expect(router.navigate).not.toHaveBeenCalled();
    });
  });

  describe('goBack', () => {
    it('should navigate to the home page', () => {
      component.goBack();

      expect(router.navigate).toHaveBeenCalledOnceWith(['/']);
    });
  });

  describe('username validation boundaries', () => {
    it('should accept a username with exactly 3 characters', () => {
      const username = component.registerForm.controls.username;

      username.clearAsyncValidators();
      username.setValue('abc');

      expect(username.valid).toBeTrue();
    });

    it('should accept a username with exactly 20 characters', () => {
      const username = component.registerForm.controls.username;

      username.clearAsyncValidators();
      username.setValue('a'.repeat(20));

      expect(username.valid).toBeTrue();
    });
  });

  describe('submit additional cases', () => {
    beforeEach(() => {
      prepareValidForm();
    });

    it('should mark all controls as touched when the form is invalid', () => {
      component.registerForm.controls.email.setValue('');

      component.submit();

      expect(component.registerForm.controls.email.touched).toBeTrue();
      expect(component.registerForm.controls.username.touched).toBeTrue();
      expect(component.registerForm.controls.password.touched).toBeTrue();
    });

    it('should not submit twice while registration is in progress', () => {
      const request = new Subject<never>();
      authService.register.and.returnValue(request.asObservable());

      component.submit();
      component.submit();

      expect(authService.register).toHaveBeenCalledTimes(1);

      request.complete();
    });

    it('should clear the previous error even when the form is invalid', () => {
      component.errorMessage.set('Previous error');
      component.registerForm.controls.email.setValue('');

      component.submit();

      expect(component.errorMessage()).toBe('');
      expect(authService.register).not.toHaveBeenCalled();
    });
  });
});
