import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
    })
      .overrideComponent(HomeComponent, {
        set: {
          imports: [],
          schemas: [CUSTOM_ELEMENTS_SCHEMA],
        },
      })
      .compileComponents();

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
      const buttons =
        fixture.nativeElement.querySelectorAll('app-button');

      expect(buttons.length).toBe(2);
      expect(buttons[0].getAttribute('label')).toBe('Se connecter');
      expect(buttons[0].getAttribute('routerLink')).toBe('/login');
    });

    it('should display the register button', () => {
      const buttons =
        fixture.nativeElement.querySelectorAll('app-button');

      expect(buttons[1].getAttribute('label')).toBe("S'inscrire");
      expect(buttons[1].getAttribute('routerLink')).toBe('/register');
    });

    it('should configure both buttons as outlined links', () => {
      const buttons =
        fixture.nativeElement.querySelectorAll('app-button');

      expect(buttons.length).toBe(2);

      buttons.forEach((button: HTMLElement) => {
        expect(button.getAttribute('type')).toBe('link');
        expect(button.getAttribute('backgroundColor')).toBe('#fff');
        expect(button.getAttribute('outlined')).toBe('true');
      });
    });
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
      const buttons = fixture.nativeElement.querySelectorAll('app-button');

      expect(buttons.length).toBe(2);
      expect(buttons[0].getAttribute('label')).toBe('Se connecter');
      expect(buttons[0].getAttribute('routerLink')).toBe('/login');
    });

    it('should display the register button', () => {
      const buttons = fixture.nativeElement.querySelectorAll('app-button');

      expect(buttons[1].getAttribute('label')).toBe("S'inscrire");
      expect(buttons[1].getAttribute('routerLink')).toBe('/register');
    });

    it('should configure both buttons as outlined links', () => {
      const buttons = fixture.nativeElement.querySelectorAll('app-button');

      expect(buttons.length).toBe(2);

      buttons.forEach((button: HTMLElement) => {
        expect(button.getAttribute('type')).toBe('link');
        expect(button.getAttribute('backgroundColor')).toBe('#fff');
        expect(button.getAttribute('outlined')).toBe('true');
      });
    });
  });
});
