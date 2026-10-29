import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

import { HomeComponent } from './home.component';
import { ButtonComponent } from '../../shared/components/button/button.component';
import { provideRouter } from '@angular/router';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('template', () => {
    it('should display the home container', () => {
      const home = fixture.nativeElement.querySelector('.home');

      expect(home).not.toBeNull();
    });

    it('should display the logo with the correct source and alt text', () => {
      const logo: HTMLImageElement =
        fixture.nativeElement.querySelector('.home__logo');

      expect(logo).not.toBeNull();
      expect(logo.getAttribute('src')).toBe('/assets/logo_p6.png');
      expect(logo.getAttribute('alt')).toBe('logo');
    });

    it('should display the buttons container', () => {
      const buttonsContainer =
        fixture.nativeElement.querySelector('.home__buttons');

      expect(buttonsContainer).not.toBeNull();
    });

    it('should display the login button', () => {
      const buttons = fixture.debugElement.queryAll(
        By.directive(ButtonComponent),
      );

      expect(buttons.length).toBe(2);
      expect(buttons[0].componentInstance.label()).toBe('Se connecter');
      expect(buttons[0].componentInstance.routerLink()).toBe('/login');
    });

    it('should display the register button', () => {
      const buttons = fixture.debugElement.queryAll(
        By.directive(ButtonComponent),
      );

      expect(buttons.length).toBe(2);
      expect(buttons[1].componentInstance.label()).toBe("S'inscrire");
      expect(buttons[1].componentInstance.routerLink()).toBe('/register');
    });

    it('should configure both buttons as outlined links', () => {
      const buttons = fixture.debugElement.queryAll(
        By.directive(ButtonComponent),
      );

      expect(buttons.length).toBe(2);

      buttons.forEach((button) => {
        const instance = button.componentInstance;

        expect(instance.type()).toBe('link');
        expect(instance.outlined()).toBeTrue();
        expect(instance.fontWeight()).toBe(600);
      });
    });
  });
});
