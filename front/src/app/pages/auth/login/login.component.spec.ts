import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter, Router } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthService } from '@service/auth.service';
import { UserStateService } from '@service/user-state.service';
import { UserService } from '@service/user.service';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { User } from '@model/user.model';
import { HttpResponse } from '@angular/common/http';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let routerSpy: jasmine.SpyObj<Router>;

  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let userStateService: UserStateService;

  const mockUser = {
    username: 'testuser',
    email: 'test@example.com',
  } as User;

  const validCredentials = {
    usernameOrEmail: 'test@example.com',
    password: 'StrongPass1!',
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', [
      'login',
    ]);

    userServiceSpy = jasmine.createSpyObj<UserService>('UserService', [
      'getMe',
    ]);

    authServiceSpy.login.and.returnValue(
      of(new HttpResponse<void>({ status: 200 })),
    );

    userServiceSpy.getMe.and.returnValue(of(mockUser));

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: UserService, useValue: userServiceSpy },
        UserStateService,
      ],
    }).compileComponents();

    const router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    // Conserve cette variable si tes tests utilisent routerSpy.navigate.
    routerSpy = router as jasmine.SpyObj<Router>;

    userStateService = TestBed.inject(UserStateService);

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  function setValidForm(): void {
    component.loginForm.setValue(validCredentials);
  }

  describe('Initialization', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize the form with empty values', () => {
      expect(component.loginForm.getRawValue()).toEqual({
        usernameOrEmail: '',
        password: '',
      });
    });

    it('should initialize isLoading to false', () => {
      expect(component.isLoading).toBeFalse();
    });

    it('should initialize errorMessage to an empty string', () => {
      expect(component.errorMessage()).toBe('');
    });

    it('should initialize the form as invalid', () => {
      expect(component.loginForm.invalid).toBeTrue();
    });

    it('should render the login title', () => {
      const title: HTMLElement = fixture.nativeElement.querySelector('h1');

      expect(title.textContent?.trim()).toBe('Connexion');
    });

    it('should render the username or email input', () => {
      const input: HTMLInputElement =
        fixture.nativeElement.querySelector('#usernameOrEmail');

      expect(input).toBeTruthy();
      expect(input.type).toBe('text');
      expect(input.getAttribute('autocomplete')).toBe('username');
    });

    it('should render the password input', () => {
      const input: HTMLInputElement =
        fixture.nativeElement.querySelector('#password');

      expect(input).toBeTruthy();
      expect(input.type).toBe('password');
      expect(input.getAttribute('autocomplete')).toBe('current-password');
    });

    it('should render the submit button', () => {
      const button = fixture.debugElement.query(By.directive(ButtonComponent));

      expect(button).toBeTruthy();
      expect(button.componentInstance.label()).toBe('Se connecter');
      expect(button.componentInstance.type()).toBe('submit');
    });

    it('should disable the submit button when the form is invalid', () => {
      const button = fixture.debugElement.query(By.directive(ButtonComponent));

      expect(button.componentInstance.disabled()).toBeTrue();
    });
  });

  describe('Form validation', () => {
    describe('usernameOrEmail', () => {
      it('should be required', () => {
        const control = component.loginForm.controls.usernameOrEmail;

        control.setValue('');

        expect(control.hasError('required')).toBeTrue();
      });

      it('should be valid when a username is provided', () => {
        const control = component.loginForm.controls.usernameOrEmail;

        control.setValue('testuser');

        expect(control.valid).toBeTrue();
      });

      it('should not display errors before the control is touched or dirty', () => {
        const control = component.loginForm.controls.usernameOrEmail;

        control.setValue('');

        expect(component.usernameOrEmailErrors).toEqual([]);
      });

      it('should display the required error when touched', () => {
        const control = component.loginForm.controls.usernameOrEmail;

        control.setValue('');
        control.markAsTouched();

        expect(component.usernameOrEmailErrors).toEqual([
          "L'email ou le nom d'utilisateur est obligatoire.",
        ]);
      });

      it('should return no errors for a valid touched control', () => {
        const control = component.loginForm.controls.usernameOrEmail;

        control.setValue('testuser');
        control.markAsTouched();

        expect(component.usernameOrEmailErrors).toEqual([]);
      });
    });

    describe('password', () => {
      it('should be required', () => {
        const control = component.loginForm.controls.password;

        control.setValue('');

        expect(control.hasError('required')).toBeTrue();
      });

      it('should be valid when a password is provided', () => {
        const control = component.loginForm.controls.password;

        control.setValue('password123');

        expect(control.valid).toBeTrue();
      });

      it('should not display errors before the control is touched or dirty', () => {
        const control = component.loginForm.controls.password;

        control.setValue('');

        expect(component.passwordErrors).toEqual([]);
      });

      it('should display the required error when touched', () => {
        const control = component.loginForm.controls.password;

        control.setValue('');
        control.markAsTouched();

        expect(component.passwordErrors).toEqual([
          'Le mot de passe est obligatoire.',
        ]);
      });

      it('should return no errors for a valid touched control', () => {
        const control = component.loginForm.controls.password;

        control.setValue('password123');
        control.markAsTouched();

        expect(component.passwordErrors).toEqual([]);
      });
    });

    it('should make the form valid when both fields are provided', () => {
      setValidForm();

      expect(component.loginForm.valid).toBeTrue();
    });

    it('should display the username or email error in the template', () => {
      const control = component.loginForm.controls.usernameOrEmail;

      control.setValue('');
      control.markAsTouched();
      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain(
        "L'email ou le nom d'utilisateur est obligatoire.",
      );
    });

    it('should display the password error in the template', () => {
      const control = component.loginForm.controls.password;

      control.setValue('');
      control.markAsTouched();
      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain(
        'Le mot de passe est obligatoire.',
      );
    });
  });

  describe('submit()', () => {
    it('should clear the previous error message', () => {
      component.errorMessage.set('Previous error');

      component.submit();

      expect(component.errorMessage()).toBe('');
    });

    it('should mark all controls as touched when the form is invalid', () => {
      spyOn(component.loginForm, 'markAllAsTouched');

      component.submit();

      expect(component.loginForm.markAllAsTouched).toHaveBeenCalled();
      expect(authServiceSpy.login).not.toHaveBeenCalled();
      expect(component.isLoading).toBeFalse();
    });

    it('should not call login when the form is invalid', () => {
      component.loginForm.controls.usernameOrEmail.setValue('testuser');

      component.submit();

      expect(authServiceSpy.login).not.toHaveBeenCalled();
    });

    it('should call login with the form values', () => {
      setValidForm();

      component.submit();

      expect(authServiceSpy.login).toHaveBeenCalledWith(validCredentials);
    });

    it('should call getMe only after login succeeds', () => {
      setValidForm();

      component.submit();

      expect(authServiceSpy.login).toHaveBeenCalledTimes(1);
      expect(userServiceSpy.getMe).toHaveBeenCalledTimes(1);
    });

    it('should update UserStateService with the returned user', () => {
      setValidForm();

      component.submit();

      expect(userStateService.getUser()).toEqual(mockUser);
      expect(userStateService.user()).toEqual(mockUser);
      expect(userStateService.isLogged()).toBeTrue();
    });

    it('should navigate to the feed after successful login', () => {
      setValidForm();

      component.submit();

      expect(routerSpy.navigate).toHaveBeenCalledWith(['/feed']);
    });

    it('should reset isLoading after successful login', () => {
      setValidForm();

      component.submit();

      expect(component.isLoading).toBeFalse();
    });

    it('should keep the user state empty before a successful login', () => {
      expect(userStateService.getUser()).toBeNull();
      expect(userStateService.isLogged()).toBeFalse();
    });

    it('should display an error when login fails', () => {
      const loginError = new Error('Invalid credentials');
      spyOn(console, 'log');

      authServiceSpy.login.and.returnValue(throwError(() => loginError));

      setValidForm();
      component.submit();
      fixture.detectChanges();

      expect(component.errorMessage()).toBe(
        'Erreur lors de la connexion. Vérifiez vos identifiants et réessayez.',
      );
      expect(component.isLoading).toBeFalse();
      expect(userServiceSpy.getMe).not.toHaveBeenCalled();
      expect(userStateService.getUser()).toBeNull();
      expect(routerSpy.navigate).not.toHaveBeenCalled();
      expect(console.log).toHaveBeenCalledWith('LOGIN ERROR', loginError);
    });

    it('should display an error when getMe fails', () => {
      const userError = new Error('Unable to retrieve current user');
      spyOn(console, 'log');

      userServiceSpy.getMe.and.returnValue(throwError(() => userError));

      setValidForm();
      component.submit();
      fixture.detectChanges();

      expect(component.errorMessage()).toBe(
        'Erreur lors de la connexion. Vérifiez vos identifiants et réessayez.',
      );
      expect(component.isLoading).toBeFalse();
      expect(userStateService.getUser()).toBeNull();
      expect(routerSpy.navigate).not.toHaveBeenCalled();
      expect(console.log).toHaveBeenCalledWith('LOGIN ERROR', userError);
    });

    it('should reset isLoading after an asynchronous error', () => {
      const loginSubject = new Subject<never>();
      spyOn(console, 'log');

      authServiceSpy.login.and.returnValue(loginSubject.asObservable());

      setValidForm();
      component.submit();

      expect(component.isLoading).toBeTrue();

      loginSubject.error(new Error('Network error'));

      expect(component.isLoading).toBeFalse();
    });

    it('should not start another login while a request is pending', () => {
      const loginSubject = new Subject<never>();

      authServiceSpy.login.and.returnValue(loginSubject.asObservable());

      setValidForm();
      component.submit();
      component.submit();

      expect(authServiceSpy.login).toHaveBeenCalledTimes(1);

      loginSubject.complete();
    });
  });

  describe('Error message template', () => {
    it('should not render an alert when there is no error', () => {
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
    });

    it('should render the error message when an error exists', () => {
      component.errorMessage.set(
        'Erreur lors de la connexion. Vérifiez vos identifiants et réessayez.',
      );
      fixture.detectChanges();

      const alert: HTMLElement =
        fixture.nativeElement.querySelector('[role="alert"]');

      expect(alert).toBeTruthy();
      expect(alert.textContent).toContain(
        'Erreur lors de la connexion. Vérifiez vos identifiants et réessayez.',
      );
    });
  });

  describe('Submit button', () => {
    it('should be disabled when the form is invalid', () => {
      fixture.detectChanges();

      const button = fixture.debugElement.query(By.directive(ButtonComponent));

      expect(button.componentInstance.disabled()).toBeTrue();
    });

    it('should be enabled when the form is valid and not loading', () => {
      setValidForm();
      fixture.detectChanges();

      const button = fixture.debugElement.query(By.directive(ButtonComponent));

      expect(button.componentInstance.disabled()).toBeFalse();
    });
    
  });

  describe('goBack()', () => {
    it('should navigate to the home page', () => {
      component.goBack();

      expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should navigate home when the back button is clicked', () => {
      const backButton: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.login__back-button',
      );

      backButton.click();

      expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    });
  });
});
