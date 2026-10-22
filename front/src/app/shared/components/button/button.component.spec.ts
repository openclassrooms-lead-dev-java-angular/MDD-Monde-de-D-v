import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { ButtonComponent } from './button.component';

describe('ButtonComponent', () => {
  let component: ButtonComponent;
  let fixture: ComponentFixture<ButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ButtonComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(ButtonComponent);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('label', 'Test button');
    fixture.detectChanges();
  });

  function getButton(): HTMLButtonElement | null {
    return fixture.nativeElement.querySelector('button');
  }

  function getLink(): HTMLAnchorElement | null {
    return fixture.nativeElement.querySelector('a');
  }

  describe('initialization', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should use the default input values', () => {
      expect(component.type()).toBe('button');
      expect(component.disabled()).toBeFalse();
      expect(component.outlined()).toBeFalse();
      expect(component.fontWeight()).toBe(600);
      expect(component.action()).toBeUndefined();
      expect(component.variant()).toBeUndefined();
      expect(component.routerLink()).toBeUndefined();
      expect(component.alt()).toBeUndefined();
    });

    it('should display the provided label', () => {
      expect(getButton()?.textContent?.trim()).toBe('Test button');
    });

    it('should update the label when the input changes', () => {
      fixture.componentRef.setInput('label', 'Updated label');
      fixture.detectChanges();

      expect(getButton()?.textContent?.trim()).toBe('Updated label');
    });
  });

  describe('button rendering', () => {
    it('should render a flat button by default', () => {
      const button = getButton();

      expect(button).not.toBeNull();
      expect(getLink()).toBeNull();
      expect(
        button?.classList.contains('mat-mdc-unelevated-button'),
      ).toBeTrue();
    });

    it('should render a stroked button when outlined is true', () => {
      fixture.componentRef.setInput('outlined', true);
      fixture.detectChanges();

      const button = getButton();

      expect(button).not.toBeNull();
      expect(button?.classList.contains('mat-mdc-outlined-button')).toBeTrue();
    });

    it('should render a button with type button by default', () => {
      expect(getButton()?.type).toBe('button');
    });

    it('should render a submit button when type is submit', () => {
      fixture.componentRef.setInput('type', 'submit');
      fixture.detectChanges();

      expect(getButton()?.type).toBe('submit');
    });

    it('should render a reset button when type is reset', () => {
      fixture.componentRef.setInput('type', 'reset');
      fixture.detectChanges();

      expect(getButton()?.type).toBe('reset');
    });
  });

  describe('link rendering', () => {
    beforeEach(() => {
      fixture.componentRef.setInput('type', 'link');
      fixture.componentRef.setInput('routerLink', '/articles');
      fixture.detectChanges();
    });

    it('should render an anchor instead of a button', () => {
      expect(getLink()).not.toBeNull();
      expect(getButton()).toBeNull();
    });

    it('should navigate to the provided router link', () => {
      expect(getLink()?.getAttribute('href')).toBe('/articles');
    });

    it('should display the provided label', () => {
      expect(getLink()?.textContent?.trim()).toBe('Test button');
    });

    it('should apply the alt input as the aria-label', () => {
      fixture.componentRef.setInput('alt', 'Open articles');
      fixture.detectChanges();

      expect(getLink()?.getAttribute('aria-label')).toBe('Open articles');
    });

    it('should apply the alt input as the title', () => {
      fixture.componentRef.setInput('alt', 'Open articles');
      fixture.detectChanges();

      expect(getLink()?.getAttribute('title')).toBe('Open articles');
    });

    it('should not set aria-label when alt is undefined', () => {
      expect(getLink()?.getAttribute('aria-label')).toBeNull();
    });

    it('should apply the font weight', () => {
      fixture.componentRef.setInput('fontWeight', 700);
      fixture.detectChanges();

      expect(getLink()?.style.fontWeight).toBe('700');
    });
  });

  describe('variantClass', () => {
    it('should return an empty string when variant is undefined', () => {
      expect(component.variantClass()).toBe('');
    });

    it('should return the corresponding CSS class', () => {
      fixture.componentRef.setInput('variant', 'primary');
      fixture.detectChanges();

      expect(component.variantClass()).toBe('button-primary');
    });

    it('should update the CSS class when the variant changes', () => {
      fixture.componentRef.setInput('variant', 'primary');
      fixture.detectChanges();

      expect(getButton()?.classList.contains('button-primary')).toBeTrue();

      fixture.componentRef.setInput('variant', 'secondary');
      fixture.detectChanges();

      expect(getButton()?.classList.contains('button-secondary')).toBeTrue();
      expect(getButton()?.classList.contains('button-primary')).toBeFalse();
    });
  });

  describe('styling', () => {
    it('should apply the default font weight', () => {
      expect(getButton()?.style.fontWeight).toBe('600');
    });

    it('should apply a custom font weight', () => {
      fixture.componentRef.setInput('fontWeight', 400);
      fixture.detectChanges();

      expect(getButton()?.style.fontWeight).toBe('400');
    });

    it('should apply the variant class to the button', () => {
      fixture.componentRef.setInput('variant', 'primary');
      fixture.detectChanges();

      expect(getButton()?.classList.contains('button-primary')).toBeTrue();
    });

    it('should apply the variant class to the link', () => {
      fixture.componentRef.setInput('type', 'link');
      fixture.componentRef.setInput('routerLink', '/articles');
      fixture.componentRef.setInput('variant', 'primary');
      fixture.detectChanges();

      expect(getLink()?.classList.contains('button-primary')).toBeTrue();
    });
  });

  describe('action', () => {
    it('should call the action when the button is clicked', () => {
      const action = jasmine.createSpy('action');

      fixture.componentRef.setInput('action', action);
      fixture.detectChanges();

      getButton()?.click();

      expect(action).toHaveBeenCalledTimes(1);
    });

    it('should not throw when action is undefined', () => {
      expect(() => getButton()?.click()).not.toThrow();
    });

    it('should call the action only once per click', () => {
      const action = jasmine.createSpy('action');

      fixture.componentRef.setInput('action', action);
      fixture.detectChanges();

      getButton()?.click();
      getButton()?.click();

      expect(action).toHaveBeenCalledTimes(2);
    });

    it('should not execute the action when the link is clicked', () => {
      const action = jasmine.createSpy('action');

      fixture.componentRef.setInput('type', 'link');
      fixture.componentRef.setInput('routerLink', '/articles');
      fixture.componentRef.setInput('action', action);
      fixture.detectChanges();

      getLink()?.click();

      expect(action).not.toHaveBeenCalled();
    });
  });
});
