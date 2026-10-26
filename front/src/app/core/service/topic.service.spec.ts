import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';

import { TopicService } from './topic.service';
import { environment } from '../../../environments/environment';
import { Topic, TopicOption } from '@model/topic.model';
import { Page } from '@type/page.type';

describe('TopicService', () => {
  let service: TopicService;
  let httpMock: HttpTestingController;

  const baseUrl = `${environment.apiUrl}/${environment.apiRoute.topic}`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TopicService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(TopicService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('fetchTopics', () => {
    it('should retrieve a paginated list of topics', () => {
      const page = 0;
      const size = 10;

      const mockResponse = {
        content: [
          {
            name: 'Java',
            publicId: 'java',
            description: '',
            subscribed: false,
            createdAt: new Date(),
            updatedAt: new Date(),
          },
        ],
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0,
        numberOfElements: 0,
        first: true,
        last: true,
        empty: false,
      } as Page<Topic>;

      service.fetchTopics(page, size).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne((request) => request.url === baseUrl);

      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('10');

      req.flush(mockResponse);
    });

    it('should send the requested pagination parameters', () => {
      service.fetchTopics(2, 5).subscribe();

      const req = httpMock.expectOne((request) => request.url === baseUrl);

      expect(req.request.params.keys()).toEqual(['page', 'size']);
      expect(req.request.params.get('page')).toBe('2');
      expect(req.request.params.get('size')).toBe('5');

      req.flush({
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 5,
        number: 2,
        first: false,
        last: true,
        empty: true,
      });
    });
  });

  describe('fetchTopicList', () => {
    it('should retrieve the topic options', () => {
      const mockResponse: TopicOption[] = [
        { name: 'Java', publicId: 'java' },
        { name: 'Angular', publicId: 'angular' },
      ];

      service.fetchTopicOptions().subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/options`);

      expect(req.request.method).toBe('GET');

      req.flush(mockResponse);
    });
  });

  describe('subscribe', () => {
    it('should subscribe to a topic', () => {
      const publicId = 'java';

      service.subscribe(publicId).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${publicId}/subscribe`);

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({});

      req.flush(null);
    });
  });

  describe('unsubscribe', () => {
    it('should unsubscribe from a topic', () => {
      const publicId = 'java';

      service.unsubscribe(publicId).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${publicId}/subscribe`);

      expect(req.request.method).toBe('DELETE');

      req.flush(null);
    });
  });

  describe('fetchSubscribed', () => {
    it('should retrieve the subscribed topics', () => {
      const mockResponse: Page<Topic> = {
        content: [
          {
            name: 'Java',
            publicId: 'java',
            description: '',
            subscribed: false,
            createdAt: new Date(),
            updatedAt: new Date(),
          },
          {
            name: 'Angular',
            publicId: 'angular',
            description: '',
            subscribed: false,
            createdAt: new Date(),
            updatedAt: new Date(),
          },
        ],
        totalPages: 1,
        totalElements: 1,
        size: 10,
        number: 0,
        first: true,
        last: true,
        empty: false,
        numberOfElements: 1,
      } as Page<Topic>;

      service.fetchSubscribed(0, 10).subscribe((response) => {
        expect(response).toEqual(mockResponse);
        expect(response.content).toEqual(mockResponse.content);
      });

      const req = httpMock.expectOne(`${baseUrl}/subscribed`);

      expect(req.request.method).toBe('GET');

      req.flush(mockResponse);
    });

    it('should retrieve an empty page when the user has no subscribed topics', () => {
      const mockResponse: Page<Topic> = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 10,
        number: 0,
        numberOfElements: 0,
        first: true,
        last: true,
        empty: true,
      } as Page<Topic>;

      service.fetchSubscribed(0, 10).subscribe((response) => {
        expect(response).toEqual(mockResponse);
        expect(response.content).toEqual([]);
        expect(response.empty).toBe(true);
      });

      const req = httpMock.expectOne(`${baseUrl}/subscribed`);

      expect(req.request.method).toBe('GET');

      req.flush(mockResponse);
    });
  });

  it('should propagate an HTTP error when fetching topics fails', () => {
    let receivedError: HttpErrorResponse | undefined;

    service.fetchTopics(0, 10).subscribe({
      error: (error: HttpErrorResponse) => {
        receivedError = error;
      },
    });

    const req = httpMock.expectOne((request) => request.url === baseUrl);

    req.flush(
      { message: 'Server error' },
      { status: 500, statusText: 'Internal Server Error' },
    );

    expect(receivedError).toBeDefined();
    expect(receivedError?.status).toBe(500);
  });
});
