import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { HttpResponse } from '@angular/common/http';
import { of, Subject, throwError } from 'rxjs';

import { RegisterComponent } from './register.component';
import { AuthService } from '@service/auth.service';
import { ValidationErrorService } from '@service/validation.service';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { AvailableResponse } from '@type/available-response.type';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;

  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let validationErrorServiceSpy: jasmine.SpyObj<ValidationErrorService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const validEmail = 'test@example.com';
  const validUsername = 'newuser';
  const validPassword = 'StrongPass1!';

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', [
      'register',
      'checkUsernameAvailability',
    ]);

    validationErrorServiceSpy = jasmine.createSpyObj<ValidationErrorService>(
      'ValidationErrorService',
      ['getErrors'],
    );

    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate']);

    authServiceSpy.register.and.returnValue(
      of(new HttpResponse<void>({ status: 200 })),
    );
    authServiceSpy.checkUsernameAvailability.and.returnValue(
      of({ available: true }),
    );

    validationErrorServiceSpy.getErrors.and.returnValue([]);

    routerSpy.navigate.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        {
          provide: ValidationErrorService,
          useValue: validationErrorServiceSpy,
        },
        { provide: Router, useValue: routerSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function setValidForm(): void {
    component.registerForm.controls.email.setValue(validEmail);
    component.registerForm.controls.username.setValue(validUsername);
    component.registerForm.controls.password.setValue(validPassword);
  }

  function settleUsernameValidator(): void {
    tick(400);
    fixture.detectChanges();
  }

  describe('Initialization', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize the registration form with empty values', () => {
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

    it('should initialize all form controls', () => {
      expect(component.registerForm.controls.email).toBeTruthy();
      expect(component.registerForm.controls.username).toBeTruthy();
      expect(component.registerForm.controls.password).toBeTruthy();
    });

    it('should render the registration title', () => {
      const title: HTMLElement = fixture.nativeElement.querySelector('h1');

      expect(title.textContent?.trim()).toBe('Inscription');
    });

    it('should render the three input fields', () => {
      const inputs: NodeListOf<HTMLInputElement> =
        fixture.nativeElement.querySelectorAll('input');

      expect(inputs.length).toBe(3);
      expect(inputs[0].type).toBe('email');
      expect(inputs[1].type).toBe('text');
      expect(inputs[2].type).toBe('password');
    });

    it('should render the registration button', () => {
      const buttonDebugElement = fixture.debugElement.query(
        By.directive(ButtonComponent),
      );

      expect(buttonDebugElement).toBeTruthy();
      expect(buttonDebugElement.componentInstance.label()).toBe('S’inscrire');
      expect(buttonDebugElement.componentInstance.type()).toBe('submit');
    });

    it('should disable the registration button when the form is invalid', () => {
      const buttonDebugElement = fixture.debugElement.query(
        By.directive(ButtonComponent),
      );

      expect(buttonDebugElement.componentInstance.disabled()).toBeTrue();
    });
  });

  describe('Email validation', () => {
    const emailControl = () => component.registerForm.controls.email;

    it('should require an email', () => {
      emailControl().setValue('');

      expect(emailControl().hasError('required')).toBeTrue();
    });

    it('should reject an invalid email', () => {
      emailControl().setValue('invalid-email');

      expect(emailControl().hasError('email')).toBeTrue();
    });

    it('should accept a valid email', () => {
      emailControl().setValue(validEmail);

      expect(emailControl().hasError('email')).toBeFalse();
      expect(emailControl().hasError('required')).toBeFalse();
    });

    it('should reject an email longer than 50 characters', () => {
      emailControl().setValue(`${'a'.repeat(40)}@example.com`);

      expect(emailControl().hasError('maxlength')).toBeTrue();
    });

    it('should accept an email of at most 50 characters', () => {
      emailControl().setValue('a'.repeat(38) + '@example.com');

      expect(emailControl().hasError('maxlength')).toBeFalse();
    });

    it('should expose email validation errors through ValidationErrorService', () => {
      const expectedErrors = ['Adresse e-mail obligatoire'];

      validationErrorServiceSpy.getErrors.and.returnValue(expectedErrors);

      expect(component.emailErrors).toEqual(expectedErrors);
      expect(validationErrorServiceSpy.getErrors).toHaveBeenCalledWith(
        emailControl(),
        'email',
      );
    });
  });

  describe('Username validation', () => {
    const usernameControl = () => component.registerForm.controls.username;

    it('should require a username', () => {
      usernameControl().setValue('');

      expect(usernameControl().hasError('required')).toBeTrue();
    });

    it('should reject a username shorter than 3 characters', () => {
      usernameControl().setValue('ab');

      expect(usernameControl().hasError('minlength')).toBeTrue();
    });

    it('should accept a username with 3 characters', fakeAsync(() => {
      usernameControl().setValue('abc');
      settleUsernameValidator();

      expect(usernameControl().hasError('minlength')).toBeFalse();
    }));

    it('should reject a username longer than 20 characters', () => {
      usernameControl().setValue('a'.repeat(21));

      expect(usernameControl().hasError('maxlength')).toBeTrue();
    });

    it('should accept a username with 20 characters', fakeAsync(() => {
      usernameControl().setValue('a'.repeat(20));
      settleUsernameValidator();

      expect(usernameControl().hasError('maxlength')).toBeFalse();
    }));

    it('should check username availability asynchronously', fakeAsync(() => {
      usernameControl().setValue(validUsername);

      settleUsernameValidator();

      expect(authServiceSpy.checkUsernameAvailability).toHaveBeenCalledWith(
        validUsername,
      );
    }));

    it('should expose username validation errors through ValidationErrorService', () => {
      const expectedErrors = ['Nom d’utilisateur indisponible'];

      validationErrorServiceSpy.getErrors.and.returnValue(expectedErrors);

      expect(component.usernameErrors).toEqual(expectedErrors);
      expect(validationErrorServiceSpy.getErrors).toHaveBeenCalledWith(
        usernameControl(),
        'username',
      );
    });

    it('should report whether the username control is pending', fakeAsync(() => {
      const availabilitySubject = new Subject<AvailableResponse>();

      authServiceSpy.checkUsernameAvailability.and.returnValue(
        availabilitySubject.asObservable(),
      );

      usernameControl().setValue(validUsername);

      // Advance past the validator debounce period.
      tick(400);
      fixture.detectChanges();

      expect(component.isUsernameLoading).toBeTrue();

      availabilitySubject.next({ available: true });
      availabilitySubject.complete();
      tick();
      fixture.detectChanges();

      expect(component.isUsernameLoading).toBeFalse();
    }));

    it('should display the username loading spinner while validation is pending', fakeAsync(() => {
      const availabilitySubject = new Subject<AvailableResponse>();

      authServiceSpy.checkUsernameAvailability.and.returnValue(
        availabilitySubject.asObservable(),
      );

      usernameControl().setValue(validUsername);

      tick(400);
      fixture.detectChanges();

      expect(component.isUsernameLoading).toBeTrue();

      const spinner: HTMLElement | null =
        fixture.nativeElement.querySelector('mat-spinner');

      expect(spinner).not.toBeNull();

      availabilitySubject.next({ available: true });
      availabilitySubject.complete();
      tick();
      fixture.detectChanges();
    }));
  });

  describe('Password validation', () => {
    const passwordControl = () => component.registerForm.controls.password;

    it('should require a password', () => {
      passwordControl().setValue('');

      expect(passwordControl().hasError('required')).toBeTrue();
    });

    it('should reject a password shorter than 8 characters', () => {
      passwordControl().setValue('Ab1!xyz');

      expect(passwordControl().hasError('minlength')).toBeTrue();
    });

    it('should require an uppercase letter', () => {
      passwordControl().setValue('strongpass1!');

      expect(passwordControl().hasError('pattern')).toBeTrue();
    });

    it('should require a lowercase letter', () => {
      passwordControl().setValue('STRONGPASS1!');

      expect(passwordControl().hasError('pattern')).toBeTrue();
    });

    it('should require a digit', () => {
      passwordControl().setValue('StrongPass!');

      expect(passwordControl().hasError('pattern')).toBeTrue();
    });

    it('should require a special character', () => {
      passwordControl().setValue('StrongPass1');

      expect(passwordControl().hasError('pattern')).toBeTrue();
    });

    it('should accept a password satisfying all requirements', () => {
      passwordControl().setValue(validPassword);

      expect(passwordControl().valid).toBeTrue();
    });

    it('should expose password validation errors through ValidationErrorService', () => {
      const expectedErrors = ['Mot de passe invalide'];

      validationErrorServiceSpy.getErrors.and.returnValue(expectedErrors);

      expect(component.passwordErrors).toEqual(expectedErrors);
      expect(validationErrorServiceSpy.getErrors).toHaveBeenCalledWith(
        passwordControl(),
        'password',
      );
    });
  });

  describe('submit()', () => {
    it('should mark all controls as touched when the form is invalid', () => {
      spyOn(component.registerForm, 'markAllAsTouched');

      component.submit();

      expect(component.registerForm.markAllAsTouched).toHaveBeenCalled();
      expect(authServiceSpy.register).not.toHaveBeenCalled();
      expect(component.isLoading()).toBeFalse();
    });

    it('should clear the existing error message before submitting', () => {
      component.errorMessage.set('Previous error');

      component.submit();

      expect(component.errorMessage()).toBe('');
    });

    it('should not submit while the form is pending', fakeAsync(() => {
      const availabilitySubject = new Subject<AvailableResponse>();

      authServiceSpy.checkUsernameAvailability.and.returnValue(
        availabilitySubject.asObservable(),
      );

      setValidForm();

      tick(400);
      fixture.detectChanges();

      expect(component.registerForm.pending).toBeTrue();

      component.submit();

      expect(authServiceSpy.register).not.toHaveBeenCalled();
      expect(component.isLoading()).toBeFalse();

      availabilitySubject.next({ available: true });
      availabilitySubject.complete();
      tick();
    }));

    it('should not submit while a registration is already in progress', fakeAsync(() => {
      const registrationSubject = new Subject<never>();

      authServiceSpy.register.and.returnValue(
        registrationSubject.asObservable(),
      );

      setValidForm();
      settleUsernameValidator();

      expect(component.registerForm.valid).toBeTrue();

      component.submit();

      expect(component.isLoading()).toBeTrue();

      component.submit();

      expect(authServiceSpy.register).toHaveBeenCalledTimes(1);

      registrationSubject.complete();
      tick();

      expect(component.isLoading()).toBeFalse();
    }));

    it('should call register with the form values when the form is valid', fakeAsync(() => {
      setValidForm();
      settleUsernameValidator();

      expect(component.registerForm.valid).toBeTrue();

      component.submit();

      expect(authServiceSpy.register).toHaveBeenCalledWith({
        email: validEmail,
        username: validUsername,
        password: validPassword,
      });
    }));

    it('should set loading to true while the registration request is pending', fakeAsync(() => {
      const registrationSubject = new Subject<never>();

      authServiceSpy.register.and.returnValue(
        registrationSubject.asObservable(),
      );

      setValidForm();
      settleUsernameValidator();

      component.submit();

      expect(component.isLoading()).toBeTrue();

      registrationSubject.complete();
      tick();

      expect(component.isLoading()).toBeFalse();
    }));

    it('should navigate to the login page after successful registration', fakeAsync(() => {
      authServiceSpy.register.and.returnValue(
        of(new HttpResponse<void>({ status: 200 })),
      );

      setValidForm();
      settleUsernameValidator();

      component.submit();
      tick();

      expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
      expect(component.isLoading()).toBeFalse();
    }));

    it('should display an error message if registration fails', fakeAsync(() => {
      const registrationError = {
        error: { message: 'Registration failed' },
      };

      spyOn(console, 'error');

      authServiceSpy.register.and.returnValue(
        throwError(() => registrationError),
      );

      setValidForm();
      settleUsernameValidator();

      component.submit();
      tick();
      fixture.detectChanges();

      expect(component.errorMessage()).toBe(
        "Une erreur est survenue lors de l'inscription.",
      );
      expect(component.isLoading()).toBeFalse();
      expect(console.error).toHaveBeenCalledWith(
        'Erreur lors de l’inscription :',
        registrationError,
      );
      expect(routerSpy.navigate).not.toHaveBeenCalled();
    }));

    it('should display the error message in the template', fakeAsync(() => {
      authServiceSpy.register.and.returnValue(
        throwError(() => new Error('Registration failed')),
      );

      setValidForm();
      settleUsernameValidator();

      component.submit();
      tick();
      fixture.detectChanges();

      const alert: HTMLElement | null =
        fixture.nativeElement.querySelector('[role="alert"]');

      expect(alert).not.toBeNull();
      expect(alert?.textContent).toContain(
        "Une erreur est survenue lors de l'inscription.",
      );
    }));

    it('should update the submit button label while loading', fakeAsync(() => {
      const registrationSubject = new Subject<never>();

      authServiceSpy.register.and.returnValue(
        registrationSubject.asObservable(),
      );

      setValidForm();
      settleUsernameValidator();

      component.submit();
      fixture.detectChanges();

      const buttonDebugElement = fixture.debugElement.query(
        By.directive(ButtonComponent),
      );

      expect(buttonDebugElement.componentInstance.label()).toBe('Création...');

      registrationSubject.complete();
      tick();
      fixture.detectChanges();

      expect(buttonDebugElement.componentInstance.label()).toBe('S’inscrire');
    }));
  });

  describe('goBack()', () => {
    it('should navigate to the home page', () => {
      component.goBack();

      expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should navigate home when the back button is clicked', () => {
      const backButton: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.register__back-button',
      );

      backButton.click();

      expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    });
  });

  describe('Template interactions', () => {
    it('should render the email, username and password labels', () => {
      const labels: string[] = Array.from(
        fixture.nativeElement.querySelectorAll(
          'label',
        ) as NodeListOf<HTMLLabelElement>,
      ).map((label) => label.textContent?.trim() ?? '');

      expect(labels).toContain('Adresse e-mail');
      expect(labels).toContain("Nom d'utilisateur");
      expect(labels).toContain('Mot de passe');
    });

    it('should set aria-busy while the username is being checked', fakeAsync(() => {
      const availabilitySubject = new Subject<AvailableResponse>();

      authServiceSpy.checkUsernameAvailability.and.returnValue(
        availabilitySubject.asObservable(),
      );

      component.registerForm.controls.username.setValue(validUsername);

      tick(400);
      fixture.detectChanges();

      const usernameInput: HTMLInputElement =
        fixture.nativeElement.querySelector('#registerUsername');

      expect(usernameInput.getAttribute('aria-busy')).toBe('true');

      availabilitySubject.next({ available: true });
      availabilitySubject.complete();
      tick();
      fixture.detectChanges();

      expect(usernameInput.getAttribute('aria-busy')).toBe('false');
    }));

    it('should disable the submit button while the form is invalid', () => {
      fixture.detectChanges();

      const buttonDebugElement = fixture.debugElement.query(
        By.directive(ButtonComponent),
      );

      expect(buttonDebugElement.componentInstance.disabled()).toBeTrue();
    });

    it('should enable the submit button when the form is valid', fakeAsync(() => {
      setValidForm();
      settleUsernameValidator();

      fixture.detectChanges();

      const buttonDebugElement = fixture.debugElement.query(
        By.directive(ButtonComponent),
      );

      expect(component.registerForm.valid).toBeTrue();
      expect(buttonDebugElement.componentInstance.disabled()).toBeFalse();
    }));

    it('should disable the submit button while registration is in progress', fakeAsync(() => {
      const registrationSubject = new Subject<never>();

      authServiceSpy.register.and.returnValue(
        registrationSubject.asObservable(),
      );

      setValidForm();
      settleUsernameValidator();

      component.submit();
      fixture.detectChanges();

      const buttonDebugElement = fixture.debugElement.query(
        By.directive(ButtonComponent),
      );

      expect(buttonDebugElement.componentInstance.disabled()).toBeTrue();

      registrationSubject.complete();
      tick();
      fixture.detectChanges();
    }));
  });
});
