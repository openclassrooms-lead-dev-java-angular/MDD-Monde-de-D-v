import { computed, Injectable, signal } from '@angular/core';
import { User } from '../model/user.model';

@Injectable({
  providedIn: 'root',
})
export class UserStateService {
  private readonly _user = signal<User | null>(null);

  readonly user = this._user.asReadonly();

  readonly isLogged = computed(() => this._user() !== null);

  clearUser(): void {
    this._user.set(null);
  }

  setUser(user: User): void {
    this._user.set(user);
  }
}
