import { inject, Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { UserStateService } from '@service/user-state.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  private router = inject(Router);
  private userStateService = inject(UserStateService);

  public canActivate(): boolean | UrlTree {
    return this.userStateService.isLogged() ? true : this.router.createUrlTree(['/']);
  }
}
