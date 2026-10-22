import { HttpResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '@service/auth.service';
import { UserService } from '@service/user.service';
import { UserStateService } from '@service/user-state.service';
import { User } from '@model/user.model';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  let authService: jasmine.SpyObj<AuthService>;
  let userService: jasmine.SpyObj<UserService>;
  let userStateService: jasmine.SpyObj<UserStateService>;
  let router: jasmine.SpyObj<Router>;

  const mockUser: User = {
    email: 'jdupont@example.com',
    username: 'jdupont',
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-02T00:00:00Z'),
  };

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['login']);

    userService = jasmine.createSpyObj<UserService>('UserService', ['getMe']);

    userStateService = jasmine.createSpyObj<UserStateService>(
      'UserStateService',
      ['setUser'],
    );

    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    router.navigate.and.resolveTo(true);

    authService.login.and.returnValue(
      of(new HttpResponse<void>({ status: 200 })),
    );

    userService.getMe.and.returnValue(of(mockUser));

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: UserService, useValue: userService },
        { provide: UserStateService, useValue: userStateService },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('form validation', () => {
    it('should initialize the form as invalid', () => {
      expect(component.loginForm.invalid).toBeTrue();
    });

    it('should require a username or email', () => {
      const control = component.loginForm.controls.usernameOrEmail;

      control.setValue('');

      expect(control.hasError('required')).toBeTrue();
    });

    it('should require a password', () => {
      const control = component.loginForm.controls.password;

      control.setValue('');

      expect(control.hasError('required')).toBeTrue();
    });

    it('should be valid when all required fields are provided', () => {
      component.loginForm.setValue({
        usernameOrEmail: 'jdupont@example.com',
        password: 'Password123!',
      });

      expect(component.loginForm.valid).toBeTrue();
    });
  });

  describe('submit', () => {
    it('should not call the authentication service when the form is invalid', () => {
      component.submit();

      expect(authService.login).not.toHaveBeenCalled();
      expect(userService.getMe).not.toHaveBeenCalled();
    });

    it('should authenticate the user with the form values', () => {
      component.loginForm.setValue({
        usernameOrEmail: 'jdupont@example.com',
        password: 'Password123!',
      });

      component.submit();

      expect(authService.login).toHaveBeenCalledOnceWith({
        usernameOrEmail: 'jdupont@example.com',
        password: 'Password123!',
      });
    });

    it('should retrieve the current user after successful authentication', () => {
      component.loginForm.setValue({
        usernameOrEmail: 'jdupont@example.com',
        password: 'Password123!',
      });

      component.submit();

      expect(userService.getMe).toHaveBeenCalled();
    });

    it('should store the user and navigate to the feed after successful login', () => {
      component.loginForm.setValue({
        usernameOrEmail: 'jdupont@example.com',
        password: 'Password123!',
      });

      component.submit();

      expect(userStateService.setUser).toHaveBeenCalledWith(mockUser);
      expect(router.navigate).toHaveBeenCalledWith(['/feed']);
    });

    it('should reset the loading state after successful login', () => {
      component.loginForm.setValue({
        usernameOrEmail: 'jdupont@example.com',
        password: 'Password123!',
      });

      component.submit();

      expect(component.isLoading).toBeFalse();
    });

    it('should display an error when authentication fails', () => {
      authService.login.and.returnValue(
        throwError(() => new Error('Authentication failed')),
      );

      component.loginForm.setValue({
        usernameOrEmail: 'jdupont@example.com',
        password: 'WrongPassword!',
      });

      component.submit();

      expect(component.errorMessage).toBeTruthy();
      expect(userService.getMe).not.toHaveBeenCalled();
    });

    it('should display an error when retrieving the user fails', () => {
      userService.getMe.and.returnValue(
        throwError(() => new Error('Unable to retrieve user')),
      );

      component.loginForm.setValue({
        usernameOrEmail: 'jdupont@example.com',
        password: 'Password123!',
      });

      component.submit();

      expect(component.errorMessage).toBeTruthy();
      expect(userStateService.setUser).not.toHaveBeenCalled();
      expect(router.navigate).not.toHaveBeenCalled();
    });

    it('should reset the loading state when authentication fails', () => {
      authService.login.and.returnValue(
        throwError(() => new Error('Authentication failed')),
      );

      component.loginForm.setValue({
        usernameOrEmail: 'jdupont@example.com',
        password: 'WrongPassword!',
      });

      component.submit();

      expect(component.isLoading).toBeFalse();
    });

    it('should clear the previous error when submitting again', () => {
      component.errorMessage = 'Previous error';

      component.loginForm.setValue({
        usernameOrEmail: 'jdupont@example.com',
        password: 'Password123!',
      });

      component.submit();

      expect(component.errorMessage).toBe('');
    });

    it('should not submit when the form is pending', () => {
      component.loginForm.setValue({
        usernameOrEmail: 'jdupont@example.com',
        password: 'Password123!',
      });

      component.loginForm.markAsPending();

      component.submit();

      expect(authService.login).not.toHaveBeenCalled();
    });

    it('should not submit when a login is already in progress', () => {
      component.loginForm.setValue({
        usernameOrEmail: 'jdupont@example.com',
        password: 'Password123!',
      });

      component.isLoading = true;

      component.submit();

      expect(authService.login).not.toHaveBeenCalled();
    });
  });

  describe('goBack', () => {
    it('should navigate to the home page', () => {
      component.goBack();

      expect(router.navigate).toHaveBeenCalledWith(['/']);
    });
  });
});
