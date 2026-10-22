import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { ArticleService } from '@service/article.service';
import { environment } from '@env/environment.prod';
import { Article, ArticleNew } from '@model/article.model';
import { Page } from '@type/page.type';
import { AvailableResponse } from '@type/available-response.type';

describe('ArticleService', () => {
  let service: ArticleService;
  let httpMock: HttpTestingController;

  const baseUrl = `${environment.apiUrl}/${environment.apiRoute.article}`;

  const mockArticle = {
    slug: 'test-article',
    title: 'Test article',
    content: 'Test content',
  } as Article;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ArticleService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(ArticleService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getArticleBySlug', () => {
    it('should send a GET request for the article slug', () => {
      service.getArticleBySlug('test-article').subscribe((article) => {
        expect(article).toEqual(mockArticle);
      });

      const req = httpMock.expectOne(`${baseUrl}/test-article`);

      expect(req.request.method).toBe('GET');

      req.flush(mockArticle);
    });
  });

  describe('getArticlesFeed', () => {
    it('should send a GET request with the correct pagination and sort parameters', () => {
      const mockPage = {
        content: [mockArticle],
        number: 0,
        size: 10,
        last: true,
        totalElements: 1,
        totalPages: 1,
      } as Page<Article>;

      service.getArticlesFeed(0, 10, 'desc').subscribe((page) => {
        expect(page).toEqual(mockPage);
      });

      const req = httpMock.expectOne(
        (request) => request.url === `${baseUrl}/feed`,
      );

      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('10');
      expect(req.request.params.get('sort')).toBe('createdAt,desc');

      req.flush(mockPage);
    });

    it('should support ascending sort direction', () => {
      service.getArticlesFeed(1, 5, 'asc').subscribe();

      const req = httpMock.expectOne(
        (request) => request.url === `${baseUrl}/feed`,
      );

      expect(req.request.params.get('page')).toBe('1');
      expect(req.request.params.get('size')).toBe('5');
      expect(req.request.params.get('sort')).toBe('createdAt,asc');

      req.flush({
        content: [],
        number: 1,
        size: 5,
        last: true,
        totalElements: 0,
        totalPages: 0,
      });
    });

    it('should return an empty page when the API has no articles', () => {
      const emptyPage = {
        content: [],
        number: 0,
        size: 10,
        last: true,
        totalElements: 0,
        totalPages: 0,
        first: true,
        empty: true,
        sort: {
          empty: true,
          sorted: false,
          unsorted: true,
        },
        numberOfElements: 0,
      } as Page<Article>;

      service.getArticlesFeed(0, 10, 'desc').subscribe((page) => {
        expect(page.content).toEqual([]);
        expect(page.last).toBeTrue();
      });

      const req = httpMock.expectOne(
        (request) => request.url === `${baseUrl}/feed`,
      );

      req.flush(emptyPage);
    });
  });

  describe('postArticle', () => {
    it('should send a POST request with the article payload', () => {
      const newArticle = {
        title: 'New article',
        slug: 'new-article',
        content: 'New article content',
        topicSlug: 'technology',
      } as ArticleNew;

      const createdArticle = {
        ...mockArticle,
        ...newArticle,
      } as Article;

      service.postArticle(newArticle).subscribe((article) => {
        expect(article).toEqual(createdArticle);
      });

      const req = httpMock.expectOne(baseUrl);

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newArticle);

      req.flush(createdArticle);
    });
  });

  describe('availableSlug', () => {
    it('should send a GET request to check slug availability', () => {
      const mockResponse = {
        available: true,
      } as AvailableResponse;

      service.availableSlug('new-article').subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/available-slug/new-article`);

      expect(req.request.method).toBe('GET');

      req.flush(mockResponse);
    });

    it('should return false when the slug is already taken', () => {
      const mockResponse = {
        available: false,
      } as AvailableResponse;

      service.availableSlug('existing-article').subscribe((response) => {
        expect(response.available).toBeFalse();
      });

      const req = httpMock.expectOne(
        `${baseUrl}/available-slug/existing-article`,
      );

      req.flush(mockResponse);
    });
  });

  describe('HTTP errors', () => {
    it('should propagate an error when fetching an article fails', () => {
      service.getArticleBySlug('missing-article').subscribe({
        next: () => fail('Expected the request to fail'),
        error: (error) => {
          expect(error.status).toBe(404);
        },
      });

      const req = httpMock.expectOne(`${baseUrl}/missing-article`);

      req.flush('Article not found', {
        status: 404,
        statusText: 'Not Found',
      });
    });

    it('should propagate an error when creating an article fails', () => {
      const newArticle = {
        title: 'New article',
        slug: 'new-article',
        content: 'Content',
        topicSlug: 'technology',
      } as ArticleNew;

      service.postArticle(newArticle).subscribe({
        next: () => fail('Expected the request to fail'),
        error: (error) => {
          expect(error.status).toBe(400);
        },
      });

      const req = httpMock.expectOne(baseUrl);

      req.flush('Invalid article', {
        status: 400,
        statusText: 'Bad Request',
      });
    });
  });
});
