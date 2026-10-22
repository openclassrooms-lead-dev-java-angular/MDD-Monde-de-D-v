import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

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

  describe('getTopics', () => {
    it('should retrieve a paginated list of topics', () => {
      const page = 0;
      const size = 10;

      const mockResponse = {
        content: [
          {
            name: 'Java',
            slug: 'java',
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

      service.getTopics(page, size).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne((request) => request.url === baseUrl);

      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('10');

      req.flush(mockResponse);
    });

    it('should send the requested pagination parameters', () => {
      service.getTopics(2, 5).subscribe();

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

  describe('getTopicList', () => {
    it('should retrieve the topic options', () => {
      const mockResponse: TopicOption[] = [
        { name: 'Java', slug: 'java' },
        { name: 'Angular', slug: 'angular' },
      ];

      service.getTopicList().subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/options`);

      expect(req.request.method).toBe('GET');

      req.flush(mockResponse);
    });
  });

  describe('subscribe', () => {
    it('should subscribe to a topic', () => {
      const slug = 'java';

      service.subscribe(slug).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${slug}/subscribe`);

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({});

      req.flush(null);
    });
  });

  describe('unsubscribe', () => {
    it('should unsubscribe from a topic', () => {
      const slug = 'java';

      service.unsubscribe(slug).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${slug}/subscribe`);

      expect(req.request.method).toBe('DELETE');

      req.flush(null);
    });
  });

  describe('getSubscribed', () => {
    it('should retrieve the subscribed topics', () => {
      const mockResponse: Page<Topic[]> = {
        content: [
          [
            { name: 'Java', slug: 'java' },
            { name: 'Angular', slug: 'angular' },
          ],
        ],
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0,
        numberOfElements: 1,
        first: true,
        last: true,
        empty: false,
      } as Page<Topic[]>;

      service.getSubscribed().subscribe((response) => {
        expect(response).toEqual(mockResponse);
        expect(response.content).toEqual(mockResponse.content);
      });

      const req = httpMock.expectOne(`${baseUrl}/subscribed`);

      expect(req.request.method).toBe('GET');

      req.flush(mockResponse);
    });

    it('should retrieve an empty page when the user has no subscribed topics', () => {
      const mockResponse: Page<Topic[]> = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 10,
        number: 0,
        numberOfElements: 0,
        first: true,
        last: true,
        empty: true,
      } as Page<Topic[]>;

      service.getSubscribed().subscribe((response) => {
        expect(response).toEqual(mockResponse);
        expect(response.content).toEqual([]);
        expect(response.empty).toBe(true);
      });

      const req = httpMock.expectOne(`${baseUrl}/subscribed`);

      expect(req.request.method).toBe('GET');

      req.flush(mockResponse);
    });
  });
});
