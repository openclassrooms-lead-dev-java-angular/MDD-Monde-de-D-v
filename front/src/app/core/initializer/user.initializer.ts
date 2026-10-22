import { inject } from '@angular/core';
import { UserStateService } from '@service/user-state.service';
import { UserService } from '@service/user.service';
import { firstValueFrom } from 'rxjs';

export function initializeUser(): Promise<void> {
  const userService = inject(UserService);
  const userStateService = inject(UserStateService);

  return firstValueFrom(userService.getMe())
    .then((user) => {
      userStateService.setUser(user);
    })
    .catch(() => {
      userStateService.clearUser();
    });
}
