import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';

import { AuthGuard } from './auth.guard';
import { UserStateService } from '@service/user-state.service';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let router: jasmine.SpyObj<Router>;
  let userStateService: jasmine.SpyObj<UserStateService>;

  beforeEach(() => {
    router = jasmine.createSpyObj<Router>('Router', ['createUrlTree']);
    userStateService = jasmine.createSpyObj<UserStateService>(
      'UserStateService',
      ['isLogged'],
    );

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: Router, useValue: router },
        { provide: UserStateService, useValue: userStateService },
      ],
    });

    guard = TestBed.inject(AuthGuard);
  });

  it('should allow navigation when the user is logged in', () => {
    userStateService.isLogged.and.returnValue(true);

    const result = guard.canActivate();

    expect(result).toBeTrue();
    expect(userStateService.isLogged).toHaveBeenCalledTimes(1);
    expect(router.createUrlTree).not.toHaveBeenCalled();
  });

  it('should return a UrlTree redirecting to the home page when the user is not logged in', () => {
    const redirectUrlTree = {} as UrlTree;

    userStateService.isLogged.and.returnValue(false);
    router.createUrlTree.and.returnValue(redirectUrlTree);

    const result = guard.canActivate();

    expect(result).toBe(redirectUrlTree);
    expect(router.createUrlTree).toHaveBeenCalledOnceWith(['/']);
    expect(userStateService.isLogged).toHaveBeenCalledTimes(1);
  });
});
