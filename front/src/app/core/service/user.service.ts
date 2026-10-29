import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '@env/environment.prod';
import { User } from '../model/user.model';
import { Observable } from 'rxjs';
import { NewUserType } from '@type/new-user.type';
import { AvailableResponse } from '@type/available-response.type';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly path = `${environment.apiUrl}/${environment.apiRoute.user}`;

  private httpClient = inject(HttpClient);

  public getMe(): Observable<User> {
    return this.httpClient.get<User>(`${this.path}/me`, {
      withCredentials: true,
    });
  }

  public checkUsernameAvailability(
    username: string,
  ): Observable<AvailableResponse> {
    return this.httpClient.get<AvailableResponse>(
      `${this.path}/username-available`,
      {
        params: {
          username,
        },
      },
    );
  }

  public updateUser(user: NewUserType): Observable<User> {
    return this.httpClient.patch<User>(`${this.path}/me`, user, {
      withCredentials: true,
    });
  }
}
