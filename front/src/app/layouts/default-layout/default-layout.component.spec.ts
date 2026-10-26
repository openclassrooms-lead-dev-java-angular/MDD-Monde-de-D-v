import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { DefaultLayoutComponent } from './default-layout.component';
import { Component } from '@angular/core';
import { NavbarComponent } from '@shared/components/navbar/navbar.component';

describe('DefaultLayoutComponent', () => {
  let fixture: ComponentFixture<DefaultLayoutComponent>;
  let component: DefaultLayoutComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DefaultLayoutComponent],
      providers: [provideRouter([])],
    })
      .overrideComponent(DefaultLayoutComponent, {
        remove: { imports: [NavbarComponent] },
        add: {
          imports: [NavbarStubComponent],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(DefaultLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the navbar and router outlet', () => {
    expect(fixture.nativeElement.querySelector('app-navbar')).toBeTruthy();

    expect(fixture.nativeElement.querySelector('router-outlet')).toBeTruthy();
  });
});

@Component({
  selector: 'app-navbar',
  standalone: true,
  template: '',
})
class NavbarStubComponent {}
