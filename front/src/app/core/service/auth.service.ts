import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { environment } from '@env/environment.prod';
import { RegisterRequest } from '@model/register-request.model';
import { AvailableResponse } from '@model/available-response.model';
import { LoginRequest } from '@model/login-request.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly pathService = `${environment.apiUrl}/${environment.apiRoute.auth}`;

  private httpClient = inject(HttpClient);

  public register(
    registerRequest: RegisterRequest,
  ): Observable<HttpResponse<void>> {
    console.log(`${this.pathService}/register`);
    return this.httpClient.post<void>(
      `${this.pathService}/register`,
      registerRequest,
      {
        observe: 'response',
      },
    );
  }

  public login(loginRequest: LoginRequest): Observable<HttpResponse<void>> {
    return this.httpClient.post<void>(
      `${this.pathService}/login`,
      loginRequest,
      {
        withCredentials: true,
        observe: 'response',
      },
    );
  }

  public checkUsernameAvailability(
    username: string,
  ): Observable<AvailableResponse> {
    return this.httpClient.get<AvailableResponse>(
      `${this.pathService}/username-available`,
      {
        params: {
          username,
        },
      },
    );
  }

  public logout(): Observable<HttpResponse<void>> {
    return this.httpClient.post<void>(
      `${this.pathService}/logout`,
      {},
      {
        withCredentials: true,
        observe: 'response',
      },
    );
  }
}
