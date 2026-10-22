import { Component, inject, signal } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { UserStateService } from '@service/user-state.service';
import { filter, map, startWith } from 'rxjs';
import { AuthService } from '@service/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
})
export class NavbarComponent {
  private readonly userState = inject(UserStateService);
  private readonly breakpointObserver = inject(BreakpointObserver);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  readonly isLogged = this.userState.isLogged;

  readonly mobileMenuOpen = signal(false);

  readonly isMobile = toSignal(
    this.breakpointObserver
      .observe([Breakpoints.Handset])
      .pipe(map((state) => state.matches)),
    { initialValue: false },
  );

  readonly isHomePage = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects === '/'),
      startWith(this.router.url === '/'),
    ),
    { initialValue: this.router.url === '/' },
  );

  getHomeRoute(): string {
    return this.isLogged() ? '/feed' : '/';
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen.update((open) => !open);
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen.set(false);
  }

  logout(): void {
    this.closeMobileMenu();
    this.authService.logout().subscribe({
      next: () => {
        this.userState.clearUser();
        this.router.navigate(['/']);
      },
      error: (error) => console.error('Logout error:', error),
    });
  }
}
