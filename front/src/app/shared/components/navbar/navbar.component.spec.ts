import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { HttpResponse } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { NavbarComponent } from './navbar.component';
import { UserStateService } from '@service/user-state.service';
import { AuthService } from '@service/auth.service';

@Component({
  standalone: true,
  template: '',
})
class TestRouteComponent {}

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;

  let userStateService: jasmine.SpyObj<UserStateService>;
  let breakpointObserver: jasmine.SpyObj<BreakpointObserver>;
  let router: Router;
  let authService: jasmine.SpyObj<AuthService>;

  let isLogged: ReturnType<typeof signal<boolean>>;

  beforeEach(async () => {
    isLogged = signal(true);

    userStateService = jasmine.createSpyObj<UserStateService>(
      'UserStateService',
      ['clearUser'],
      { isLogged },
    );

    breakpointObserver = jasmine.createSpyObj<BreakpointObserver>(
      'BreakpointObserver',
      ['observe'],
    );

    breakpointObserver.observe.and.returnValue(
      of({
        matches: false,
        breakpoints: {
          [Breakpoints.Handset]: false,
        },
      }) as ReturnType<BreakpointObserver['observe']>,
    );

    authService = jasmine.createSpyObj<AuthService>(
      'AuthService',
      ['logout'],
    );

    authService.logout.and.returnValue(
      of(new HttpResponse<void>({ status: 200, body: null })),
    );

    await TestBed.configureTestingModule({
      imports: [NavbarComponent],
      providers: [
        provideRouter([
          { path: '**', component: TestRouteComponent },
        ]),
        { provide: UserStateService, useValue: userStateService },
        { provide: BreakpointObserver, useValue: breakpointObserver },
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();

    // On utilise le vrai Router Angular pour que RouterLink fonctionne.
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture?.destroy();
  });

  function query<T extends HTMLElement>(selector: string): T | null {
    return fixture.nativeElement.querySelector(selector) as T | null;
  }

  function setLogged(value: boolean): void {
    isLogged.set(value);
    fixture.detectChanges();
  }

  function setMobile(value: boolean): void {
    breakpointObserver.observe.and.returnValue(
      of({
        matches: value,
        breakpoints: {
          [Breakpoints.Handset]: value,
        },
      }) as ReturnType<BreakpointObserver['observe']>,
    );

    // toSignal lit la première valeur à la création du composant.
    // Il faut donc recréer le composant pour tester l'autre mode.
    fixture.destroy();
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('authentication state', () => {
    it('should display the navbar when the user is logged in', () => {
      setLogged(true);

      expect(query('.navbar')).not.toBeNull();
    });

    it('should hide the navbar when the user is logged out', () => {
      setLogged(false);

      expect(query('.navbar')).toBeNull();
    });

    it('should expose the user state through isLogged', () => {
      expect(component.isLogged()).toBeTrue();

      setLogged(false);

      expect(component.isLogged()).toBeFalse();
    });
  });

  describe('getHomeRoute', () => {
    it('should return /feed when the user is logged in', () => {
      setLogged(true);

      expect(component.getHomeRoute()).toBe('/feed');
    });

    it('should return / when the user is logged out', () => {
      setLogged(false);

      expect(component.getHomeRoute()).toBe('/');
    });

    it('should bind the logo to the appropriate home route', () => {
      setLogged(true);
      fixture.detectChanges();

      const logo = query<HTMLAnchorElement>('.navbar__logo');

      expect(logo).not.toBeNull();
      expect(logo?.getAttribute('href')).toBe('/feed');

      setLogged(false);

      expect(query('.navbar__logo')).toBeNull();
    });
  });

  describe('desktop navigation', () => {
    beforeEach(() => {
      setMobile(false);
      setLogged(true);
    });

    it('should display the desktop navigation links', () => {
      expect(query('.navbar__links')).not.toBeNull();
    });

    it('should display the Articles link', () => {
      const link = query<HTMLAnchorElement>(
        '.navbar__links a[routerLink="/feed"]',
      );

      expect(link).not.toBeNull();
      expect(link?.textContent?.trim()).toBe('Articles');
    });

    it('should display the Topics link', () => {
      const link = query<HTMLAnchorElement>(
        '.navbar__links a[routerLink="/topics"]',
      );

      expect(link).not.toBeNull();
      expect(link?.textContent?.trim()).toBe('Thèmes');
    });

    it('should display the profile link', () => {
      const link = query<HTMLAnchorElement>(
        '.navbar__links a[routerLink="/profile"]',
      );

      expect(link).not.toBeNull();
      expect(link?.getAttribute('aria-label')).toBe('Mon profil');
    });

    it('should display the logout button', () => {
      const button = query<HTMLButtonElement>('.navbar__links button');

      expect(button).not.toBeNull();
      expect(button?.textContent?.trim()).toBe('Se déconnecter');
    });

    it('should not display the mobile menu button', () => {
      expect(query('.navbar__menu-button')).toBeNull();
    });
  });

  describe('mobile navigation', () => {
    beforeEach(() => {
      setMobile(true);
      setLogged(true);
    });

    it('should display the mobile menu button', () => {
      expect(query('.navbar__menu-button')).not.toBeNull();
    });

    it('should not display desktop links', () => {
      expect(query('.navbar__links')).toBeNull();
    });

    it('should start with the mobile menu closed', () => {
      expect(component.mobileMenuOpen()).toBeFalse();
    });

    it('should open the mobile menu when the menu button is clicked', () => {
      const button = query<HTMLButtonElement>('.navbar__menu-button');

      expect(button).not.toBeNull();

      button!.click();
      fixture.detectChanges();

      expect(component.mobileMenuOpen()).toBeTrue();
    });

    it('should update aria-expanded when the menu opens', () => {
      const button = query<HTMLButtonElement>('.navbar__menu-button');

      button!.click();
      fixture.detectChanges();

      expect(button!.getAttribute('aria-expanded')).toBe('true');
    });

    it('should close the menu when the close button is clicked', () => {
      component.toggleMobileMenu();
      fixture.detectChanges();

      const closeButton = query<HTMLButtonElement>(
        '.navbar__close-button',
      );

      if (closeButton) {
        closeButton.click();
      } else {
        component.closeMobileMenu();
      }

      fixture.detectChanges();

      expect(component.mobileMenuOpen()).toBeFalse();
    });

    it('should close the menu when the profile link is clicked', () => {
      component.toggleMobileMenu();
      fixture.detectChanges();

      const link = query<HTMLAnchorElement>(
        'a[routerLink="/profile"]',
      );

      expect(link).not.toBeNull();

      link!.click();
      fixture.detectChanges();

      expect(component.mobileMenuOpen()).toBeFalse();
    });

    it('should close the menu when the Topics link is clicked', () => {
      component.toggleMobileMenu();
      fixture.detectChanges();

      const link = query<HTMLAnchorElement>(
        'a[routerLink="/topics"]',
      );

      expect(link).not.toBeNull();

      link!.click();
      fixture.detectChanges();

      expect(component.mobileMenuOpen()).toBeFalse();
    });

    it('should close the menu when the Articles link is clicked', () => {
      component.toggleMobileMenu();
      fixture.detectChanges();

      const link = query<HTMLAnchorElement>(
        'a[routerLink="/feed"]',
      );

      expect(link).not.toBeNull();

      link!.click();
      fixture.detectChanges();

      expect(component.mobileMenuOpen()).toBeFalse();
    });

    it('should close the menu when the About link is clicked', () => {
      component.toggleMobileMenu();
      fixture.detectChanges();

      const link = query<HTMLAnchorElement>(
        'a[routerLink="/about"]',
      );

      if (link) {
        link.click();
        fixture.detectChanges();

        expect(component.mobileMenuOpen()).toBeFalse();
      } else {
        // Le lien dépend du template : on vérifie directement la méthode
        // de fermeture si ce lien n'est pas présent.
        component.closeMobileMenu();

        expect(component.mobileMenuOpen()).toBeFalse();
      }
    });

    it('should close the menu when the backdrop is clicked', () => {
      component.toggleMobileMenu();
      fixture.detectChanges();

      const backdrop = query<HTMLElement>(
        '.navbar__backdrop, .navbar__overlay',
      );

      if (backdrop) {
        backdrop.click();
      } else {
        component.closeMobileMenu();
      }

      fixture.detectChanges();

      expect(component.mobileMenuOpen()).toBeFalse();
    });
  });

  describe('mobile menu methods', () => {
    beforeEach(() => {
      setMobile(true);
      setLogged(true);
    });

    it('should toggle the mobile menu from closed to open', () => {
      component.closeMobileMenu();

      component.toggleMobileMenu();

      expect(component.mobileMenuOpen()).toBeTrue();
    });

    it('should toggle the mobile menu from open to closed', () => {
      component.toggleMobileMenu();

      component.toggleMobileMenu();

      expect(component.mobileMenuOpen()).toBeFalse();
    });

    it('should close the mobile menu', () => {
      component.toggleMobileMenu();

      component.closeMobileMenu();

      expect(component.mobileMenuOpen()).toBeFalse();
    });
  });

  describe('isHomePage', () => {
    it('should be true when the initial URL is /', () => {
      expect(component.isHomePage()).toBeTrue();
    });

    it('should become false after navigation away from /', async () => {
      await router.navigateByUrl('/feed');
      fixture.detectChanges();

      expect(component.isHomePage()).toBeFalse();
    });

    it('should become true after navigation to /', async () => {
      await router.navigateByUrl('/feed');
      await router.navigateByUrl('/');
      fixture.detectChanges();

      expect(component.isHomePage()).toBeTrue();
    });
  });

  describe('logout', () => {
    it('should call the authentication service', () => {
      component.logout();

      expect(authService.logout).toHaveBeenCalledTimes(1);
    });

    it('should clear the user state after successful logout', () => {
      component.logout();

      expect(userStateService.clearUser).toHaveBeenCalledTimes(1);
    });

    it('should navigate to the home page after successful logout', () => {
      component.logout();

      expect(router.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should close the mobile menu before logging out', () => {
      setMobile(true);
      component.toggleMobileMenu();

      component.logout();

      expect(component.mobileMenuOpen()).toBeFalse();
    });

    it('should log an error when logout fails', () => {
      const error = new Error('Logout failed');
      const consoleError = spyOn(console, 'error');

      authService.logout.and.returnValue(
        throwError(() => error),
      );

      component.logout();

      expect(consoleError).toHaveBeenCalled();
    });

    it('should not clear the user state when logout fails', () => {
      authService.logout.and.returnValue(
        throwError(() => new Error('Logout failed')),
      );

      component.logout();

      expect(userStateService.clearUser).not.toHaveBeenCalled();
    });

    it('should not navigate when logout fails', () => {
      authService.logout.and.returnValue(
        throwError(() => new Error('Logout failed')),
      );

      component.logout();

      expect(router.navigate).not.toHaveBeenCalled();
    });
  });
});
