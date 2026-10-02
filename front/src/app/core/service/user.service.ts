import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '@env/environment.prod';
import { User } from '../model/user.model';
import { Observable } from 'rxjs';

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
}
