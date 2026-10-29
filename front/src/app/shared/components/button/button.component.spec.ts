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

    fixture.componentRef.setInput('label', 'Click me');

    fixture.detectChanges();
  });

  function query<T extends HTMLElement>(selector: string): T | null {
    return fixture.nativeElement.querySelector(selector) as T | null;
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('default inputs', () => {
    it('should use button as the default type', () => {
      expect(component.type()).toBe('button');
    });

    it('should be enabled by default', () => {
      expect(component.disabled()).toBeFalse();
    });

    it('should use a font weight of 600 by default', () => {
      expect(component.fontWeight()).toBe(600);
    });

    it('should use false as the default outlined value', () => {
      expect(component.outlined()).toBeFalse();
    });

    it('should render a button by default', () => {
      expect(query('button')).not.toBeNull();
      expect(query('a')).toBeNull();
    });

    it('should display the provided label', () => {
      expect(query<HTMLButtonElement>('button')?.textContent?.trim())
        .toBe('Click me');
    });

    it('should use a flat button by default', () => {
      const button = query<HTMLButtonElement>('button');

      expect(button).not.toBeNull();
      expect(button?.classList).toContain('mat-mdc-unelevated-button');
    });

    it('should not apply a variant class when no variant is provided', () => {
      expect(component.variantClass()).toBe('');
    });
  });

  describe('label input', () => {
    it('should update the displayed label', () => {
      fixture.componentRef.setInput('label', 'Save changes');
      fixture.detectChanges();

      expect(query<HTMLButtonElement>('button')?.textContent?.trim())
        .toBe('Save changes');
    });

    it('should render an empty label', () => {
      fixture.componentRef.setInput('label', '');
      fixture.detectChanges();

      expect(query<HTMLButtonElement>('button')?.textContent?.trim())
        .toBe('');
    });
  });

  describe('button type', () => {
    it('should render a button with type button', () => {
      fixture.componentRef.setInput('type', 'button');
      fixture.detectChanges();

      expect(query<HTMLButtonElement>('button')?.type).toBe('button');
    });

    it('should render a button with type submit', () => {
      fixture.componentRef.setInput('type', 'submit');
      fixture.detectChanges();

      expect(query<HTMLButtonElement>('button')?.type).toBe('submit');
    });

    it('should render a link when type is link', () => {
      fixture.componentRef.setInput('type', 'link');
      fixture.componentRef.setInput('routerLink', '/profile');
      fixture.detectChanges();

      expect(query<HTMLAnchorElement>('a')).not.toBeNull();
      expect(query('button')).toBeNull();
    });
  });

  describe('disabled input', () => {
    it('should enable the button when disabled is false', () => {
      fixture.componentRef.setInput('disabled', false);
      fixture.detectChanges();

      expect(query<HTMLButtonElement>('button')?.disabled).toBeFalse();
    });

    it('should disable the button when disabled is true', () => {
      fixture.componentRef.setInput('disabled', true);
      fixture.detectChanges();

      expect(query<HTMLButtonElement>('button')?.disabled).toBeTrue();
    });

    it('should not execute the action when the button is disabled', () => {
      const action = jasmine.createSpy('action');

      fixture.componentRef.setInput('action', action);
      fixture.componentRef.setInput('disabled', true);
      fixture.detectChanges();

      query<HTMLButtonElement>('button')?.click();

      expect(action).not.toHaveBeenCalled();
    });
  });

  describe('outlined input', () => {
    it('should use a flat button when outlined is false', () => {
      fixture.componentRef.setInput('outlined', false);
      fixture.detectChanges();

      const button = query<HTMLButtonElement>('button');

      expect(button?.classList).toContain('mat-mdc-unelevated-button');
      expect(button?.classList).not.toContain('mat-mdc-outlined-button');
    });

    it('should use a stroked button when outlined is true', () => {
      fixture.componentRef.setInput('outlined', true);
      fixture.detectChanges();

      const button = query<HTMLButtonElement>('button');

      expect(button?.classList).toContain('mat-mdc-outlined-button');
      expect(button?.classList).not.toContain('mat-mdc-unelevated-button');
    });

    it('should render a stroked button when outlined and type submit are set', () => {
      fixture.componentRef.setInput('outlined', true);
      fixture.componentRef.setInput('type', 'submit');
      fixture.detectChanges();

      const button = query<HTMLButtonElement>('button');

      expect(button?.type).toBe('submit');
      expect(button?.classList).toContain('mat-mdc-outlined-button');
    });
  });

  describe('variant input', () => {
    it('should return an empty class when variant is undefined', () => {
      fixture.componentRef.setInput('variant', undefined);

      expect(component.variantClass()).toBe('');
    });

    it('should return button-primary for the primary variant', () => {
      fixture.componentRef.setInput('variant', 'primary');

      expect(component.variantClass()).toBe('button-primary');
    });

    it('should return button-secondary for the secondary variant', () => {
      fixture.componentRef.setInput('variant', 'secondary');

      expect(component.variantClass()).toBe('button-secondary');
    });

    it('should return button-outlined for the outlined variant', () => {
      fixture.componentRef.setInput('variant', 'outlined');

      expect(component.variantClass()).toBe('button-outlined');
    });

    it('should apply the primary variant class to the button', () => {
      fixture.componentRef.setInput('variant', 'primary');
      fixture.detectChanges();

      expect(query<HTMLButtonElement>('button')?.classList)
        .toContain('button-primary');
    });

    it('should update the variant class when the input changes', () => {
      fixture.componentRef.setInput('variant', 'primary');
      fixture.detectChanges();

      expect(query<HTMLButtonElement>('button')?.classList)
        .toContain('button-primary');

      fixture.componentRef.setInput('variant', 'secondary');
      fixture.detectChanges();

      const button = query<HTMLButtonElement>('button');

      expect(button?.classList).toContain('button-secondary');
      expect(button?.classList).not.toContain('button-primary');
    });
  });

  describe('action input', () => {
    it('should execute the action when the button is clicked', () => {
      const action = jasmine.createSpy('action');

      fixture.componentRef.setInput('action', action);
      fixture.detectChanges();

      query<HTMLButtonElement>('button')?.click();

      expect(action).toHaveBeenCalledTimes(1);
    });

    it('should not throw when no action is provided', () => {
      fixture.componentRef.setInput('action', undefined);
      fixture.detectChanges();

      expect(() => query<HTMLButtonElement>('button')?.click())
        .not.toThrow();
    });

    it('should execute the updated action after the input changes', () => {
      const firstAction = jasmine.createSpy('firstAction');
      const secondAction = jasmine.createSpy('secondAction');

      fixture.componentRef.setInput('action', firstAction);
      fixture.detectChanges();

      query<HTMLButtonElement>('button')?.click();

      fixture.componentRef.setInput('action', secondAction);
      fixture.detectChanges();

      query<HTMLButtonElement>('button')?.click();

      expect(firstAction).toHaveBeenCalledTimes(1);
      expect(secondAction).toHaveBeenCalledTimes(1);
    });

    it('should not execute the action when a link is clicked', () => {
      const action = jasmine.createSpy('action');

      fixture.componentRef.setInput('type', 'link');
      fixture.componentRef.setInput('routerLink', '/profile');
      fixture.componentRef.setInput('action', action);
      fixture.detectChanges();

      query<HTMLAnchorElement>('a')?.click();

      expect(action).not.toHaveBeenCalled();
    });
  });

  describe('fontWeight input', () => {
    it('should apply the default font weight', () => {
      const button = query<HTMLButtonElement>('button');

      expect(button?.style.fontWeight).toBe('600');
    });

    it('should apply a custom font weight to a button', () => {
      fixture.componentRef.setInput('fontWeight', 700);
      fixture.detectChanges();

      expect(query<HTMLButtonElement>('button')?.style.fontWeight)
        .toBe('700');
    });

    it('should update the font weight when the input changes', () => {
      fixture.componentRef.setInput('fontWeight', 400);
      fixture.detectChanges();

      expect(query<HTMLButtonElement>('button')?.style.fontWeight)
        .toBe('400');

      fixture.componentRef.setInput('fontWeight', 800);
      fixture.detectChanges();

      expect(query<HTMLButtonElement>('button')?.style.fontWeight)
        .toBe('800');
    });
  });

  describe('link mode', () => {
    beforeEach(() => {
      fixture.componentRef.setInput('type', 'link');
      fixture.componentRef.setInput('routerLink', '/topics');
      fixture.detectChanges();
    });

    it('should display the label inside an anchor', () => {
      expect(query<HTMLAnchorElement>('a')?.textContent?.trim())
        .toBe('Click me');
    });

    it('should bind the routerLink to the anchor', () => {
      const anchor = query<HTMLAnchorElement>('a');

      expect(anchor?.getAttribute('href')).toBe('/topics');
    });

    it('should apply the alt input as the aria-label', () => {
      fixture.componentRef.setInput('alt', 'Browse topics');
      fixture.detectChanges();

      expect(query<HTMLAnchorElement>('a')?.getAttribute('aria-label'))
        .toBe('Browse topics');
    });

    it('should apply the alt input as the title', () => {
      fixture.componentRef.setInput('alt', 'Browse topics');
      fixture.detectChanges();

      expect(query<HTMLAnchorElement>('a')?.title).toBe('Browse topics');
    });

    it('should apply the variant class to the anchor', () => {
      fixture.componentRef.setInput('variant', 'secondary');
      fixture.detectChanges();

      expect(query<HTMLAnchorElement>('a')?.classList)
        .toContain('button-secondary');
    });

    it('should apply the font weight to the anchor', () => {
      fixture.componentRef.setInput('fontWeight', 700);
      fixture.detectChanges();

      expect(query<HTMLAnchorElement>('a')?.style.fontWeight).toBe('700');
    });

    it('should not render a button element', () => {
      expect(query('button')).toBeNull();
    });
  });

  describe('input combinations', () => {
    it('should support a disabled outlined secondary button', () => {
      fixture.componentRef.setInput('outlined', true);
      fixture.componentRef.setInput('disabled', true);
      fixture.componentRef.setInput('variant', 'secondary');
      fixture.detectChanges();

      const button = query<HTMLButtonElement>('button');

      expect(button?.disabled).toBeTrue();
      expect(button?.classList).toContain('mat-mdc-outlined-button');
      expect(button?.classList).toContain('button-secondary');
    });

    it('should support a submit button with a custom action', () => {
      const action = jasmine.createSpy('action');

      fixture.componentRef.setInput('type', 'submit');
      fixture.componentRef.setInput('action', action);
      fixture.detectChanges();

      const button = query<HTMLButtonElement>('button');

      expect(button?.type).toBe('submit');

      button?.click();

      expect(action).toHaveBeenCalledTimes(1);
    });
  });
});