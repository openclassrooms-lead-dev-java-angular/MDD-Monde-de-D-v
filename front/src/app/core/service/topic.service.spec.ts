import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { TopicService } from './topic.service';
import { environment } from '@env/environment.prod';
import { Page } from '@type/page.type';
import { Topic, TopicOption } from '@model/topic.model';

describe('TopicService', () => {
  let service: TopicService;
  let httpTestingController: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/${environment.apiRoute.topic}`;

  const mockTopic = {
    publicId: 'topic-1',
    name: 'Angular',
  } as Topic;

  const mockTopicOption = {
    publicId: 'topic-1',
    name: 'Angular',
  } as TopicOption;

  const mockPage: Page<Topic> = {
    content: [mockTopic],
    totalElements: 1,
    totalPages: 1,
    size: 10,
    number: 0,
  } as Page<Topic>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TopicService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(TopicService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // fetchTopics

  describe('fetchTopics', () => {
    it('should send a GET request with page and size parameters', () => {
      service.fetchTopics(0, 10).subscribe((response) => {
        expect(response).toEqual(mockPage);
      });

      const req = httpTestingController.expectOne(
        (request) => request.url === apiUrl,
      );

      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('10');

      req.flush(mockPage);
    });

    it('should return the requested page of topics', () => {
      service.fetchTopics(1, 5).subscribe((response) => {
        expect(response.content).toEqual([mockTopic]);
        expect(response.totalElements).toBe(1);
      });

      const req = httpTestingController.expectOne(
        (request) =>
          request.url === apiUrl &&
          request.params.get('page') === '1' &&
          request.params.get('size') === '5',
      );

      req.flush(mockPage);
    });

    it('should propagate an HTTP error', () => {
      service.fetchTopics(0, 10).subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error) => {
          expect(error.status).toBe(500);
        },
      });

      const req = httpTestingController.expectOne(
        (request) => request.url === apiUrl,
      );

      req.flush('Server error', {
        status: 500,
        statusText: 'Internal Server Error',
      });
    });
  });

  // fetchTopicOptions

  describe('fetchTopicOptions', () => {
    it('should send a GET request to the options endpoint', () => {
      const mockOptions = [mockTopicOption];

      service.fetchTopicOptions().subscribe((response) => {
        expect(response).toEqual(mockOptions);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/options`);

      expect(req.request.method).toBe('GET');

      req.flush(mockOptions);
    });

    it('should return an empty array when no options are available', () => {
      service.fetchTopicOptions().subscribe((response) => {
        expect(response).toEqual([]);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/options`);

      req.flush([]);
    });

    it('should propagate an HTTP error', () => {
      service.fetchTopicOptions().subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error) => {
          expect(error.status).toBe(500);
        },
      });

      const req = httpTestingController.expectOne(`${apiUrl}/options`);

      req.flush('Server error', {
        status: 500,
        statusText: 'Internal Server Error',
      });
    });
  });

  // subscribe

  describe('subscribe', () => {
    it('should send a POST request to subscribe to a topic', () => {
      const publicId = 'topic-1';

      service.subscribe(publicId).subscribe((response) => {
        expect(response).toBeNull();
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/${publicId}/subscribe`,
      );

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({});

      req.flush(null);
    });

    it('should propagate an HTTP error', () => {
      service.subscribe('topic-1').subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error) => {
          expect(error.status).toBe(401);
        },
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/topic-1/subscribe`,
      );

      req.flush('Unauthorized', {
        status: 401,
        statusText: 'Unauthorized',
      });
    });
  });

  // unsubscribe

  describe('unsubscribe', () => {
    it('should send a DELETE request to unsubscribe from a topic', () => {
      const publicId = 'topic-1';

      service.unsubscribe(publicId).subscribe((response) => {
        expect(response).toBeNull();
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/${publicId}/subscribe`,
      );

      expect(req.request.method).toBe('DELETE');
      expect(req.request.body).toBeNull();

      req.flush(null);
    });

    it('should propagate an HTTP error', () => {
      service.unsubscribe('topic-1').subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error) => {
          expect(error.status).toBe(404);
        },
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/topic-1/subscribe`,
      );

      req.flush('Not found', {
        status: 404,
        statusText: 'Not Found',
      });
    });
  });

  // fetchSubscribed

  describe('fetchSubscribed', () => {
    it('should send a GET request to the subscribed endpoint with pagination parameters', () => {
      service.fetchSubscribed(0, 10).subscribe((response) => {
        expect(response).toEqual(mockPage);
      });

      const req = httpTestingController.expectOne(
        (request) =>
          request.url === `${apiUrl}/subscribed` &&
          request.params.get('page') === '0' &&
          request.params.get('size') === '10',
      );

      expect(req.request.method).toBe('GET');

      req.flush(mockPage);
    });

    it('should support different pagination values', () => {
      service.fetchSubscribed(2, 20).subscribe((response) => {
        expect(response).toEqual(mockPage);
      });

      const req = httpTestingController.expectOne(
        (request) => request.url === `${apiUrl}/subscribed`,
      );

      expect(req.request.params.get('page')).toBe('2');
      expect(req.request.params.get('size')).toBe('20');

      req.flush(mockPage);
    });

    it('should propagate an HTTP error', () => {
      service.fetchSubscribed(0, 10).subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error) => {
          expect(error.status).toBe(500);
        },
      });

      const req = httpTestingController.expectOne(
        (request) => request.url === `${apiUrl}/subscribed`,
      );

      req.flush('Server error', {
        status: 500,
        statusText: 'Internal Server Error',
      });
    });
  });
});
