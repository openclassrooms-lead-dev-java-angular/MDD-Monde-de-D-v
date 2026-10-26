import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { credentialsInterceptor } from './credentials.interceptor';

describe('credentialsInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([credentialsInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should set withCredentials to true for GET requests', () => {
    http.get('/api/profile').subscribe();

    const request = httpMock.expectOne('/api/profile');

    expect(request.request.withCredentials).toBeTrue();

    request.flush({});
  });

  it('should set withCredentials to true for POST requests', () => {
    const body = { username: 'test-user' };

    http.post('/api/profile', body).subscribe();

    const request = httpMock.expectOne('/api/profile');

    expect(request.request.method).toBe('POST');
    expect(request.request.withCredentials).toBeTrue();
    expect(request.request.body).toEqual(body);

    request.flush({});
  });

  it('should set withCredentials to true for PUT requests', () => {
    const body = { username: 'updated-user' };

    http.put('/api/profile', body).subscribe();

    const request = httpMock.expectOne('/api/profile');

    expect(request.request.method).toBe('PUT');
    expect(request.request.withCredentials).toBeTrue();
    expect(request.request.body).toEqual(body);

    request.flush({});
  });

  it('should set withCredentials to true for DELETE requests', () => {
    http.delete('/api/profile').subscribe();

    const request = httpMock.expectOne('/api/profile');

    expect(request.request.method).toBe('DELETE');
    expect(request.request.withCredentials).toBeTrue();

    request.flush({});
  });

  it('should preserve the request URL and method', () => {
    http.get('/api/topics?page=2').subscribe();

    const request = httpMock.expectOne('/api/topics?page=2');

    expect(request.request.urlWithParams).toBe('/api/topics?page=2');
    expect(request.request.method).toBe('GET');
    expect(request.request.withCredentials).toBeTrue();

    request.flush([]);
  });
});
