import { TestBed } from '@angular/core/testing';
import {
  provideHttpClient,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { ArticleService } from './article.service';
import { environment } from '@env/environment.prod';
import { Article, ArticleNew } from '@model/article.model';
import { Page } from '@type/page.type';
import { SortDirection } from '@type/sort-direction.type';

describe('ArticleService', () => {
  let service: ArticleService;
  let httpTestingController: HttpTestingController;

  const apiUrl =
    `${environment.apiUrl}/${environment.apiRoute.article}`;

  const mockArticle = {
    publicId: 'article-1',
    title: 'Test article',
    content: 'This is the article content',
    username: 'test-user',
    createdAt: '2026-01-15T10:00:00Z',
    topic: {
      publicId: 'topic-1',
      name: 'Angular',
    },
    comments: [],
  } as unknown as Article;

  const mockArticleNew = {
    title: 'New article',
    content: 'This is a new article',
    topicPublicId: 'topic-1',
  } as ArticleNew;

  const mockPage: Page<Article> = {
    content: [mockArticle],
    totalElements: 1,
    totalPages: 1,
    size: 10,
    number: 0,
  } as Page<Article>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ArticleService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(ArticleService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // fetchArticleByPublicId

  describe('fetchArticleByPublicId', () => {
    it('should send a GET request to the correct URL', () => {
      service.fetchArticleByPublicId('article-1').subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/article-1`,
      );

      expect(req.request.method).toBe('GET');
      expect(req.request.url).toBe(`${apiUrl}/article-1`);

      req.flush(mockArticle);
    });

    it('should return the requested article', () => {
      service.fetchArticleByPublicId('article-1').subscribe((response) => {
        expect(response).toEqual(mockArticle);
        expect(response.publicId).toBe('article-1');
        expect(response.title).toBe('Test article');
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/article-1`,
      );

      req.flush(mockArticle);
    });

    it('should use the provided public ID in the URL', () => {
      const publicId = 'another-article';

      service.fetchArticleByPublicId(publicId).subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/${publicId}`,
      );

      expect(req.request.url).toBe(`${apiUrl}/another-article`);

      req.flush(mockArticle);
    });

    it('should propagate an HTTP error', () => {
      service.fetchArticleByPublicId('article-1').subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error: { status: number }) => {
          expect(error.status).toBe(404);
        },
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/article-1`,
      );

      req.flush(
        { message: 'Article not found' },
        {
          status: 404,
          statusText: 'Not Found',
        },
      );
    });
  });

  // getArticlesFeed

  describe('getArticlesFeed', () => {
    it('should send a GET request to the feed endpoint', () => {
      service.getArticlesFeed(0, 10, 'desc' as SortDirection).subscribe();

      const req = httpTestingController.expectOne(
        (request) => request.url === `${apiUrl}/feed`,
      );

      expect(req.request.method).toBe('GET');

      req.flush(mockPage);
    });

    it('should send the page parameter', () => {
      service.getArticlesFeed(2, 10, 'desc' as SortDirection).subscribe();

      const req = httpTestingController.expectOne(
        (request) => request.url === `${apiUrl}/feed`,
      );

      expect(req.request.params.get('page')).toBe('2');

      req.flush(mockPage);
    });

    it('should send the size parameter', () => {
      service.getArticlesFeed(0, 20, 'desc' as SortDirection).subscribe();

      const req = httpTestingController.expectOne(
        (request) => request.url === `${apiUrl}/feed`,
      );

      expect(req.request.params.get('size')).toBe('20');

      req.flush(mockPage);
    });

    it('should send the sort parameter using the requested direction', () => {
      service.getArticlesFeed(0, 10, 'desc' as SortDirection).subscribe();

      const req = httpTestingController.expectOne(
        (request) => request.url === `${apiUrl}/feed`,
      );

      expect(req.request.params.get('sort')).toBe('createdAt,desc');

      req.flush(mockPage);
    });

    it('should support ascending sort direction', () => {
      service.getArticlesFeed(0, 10, 'asc' as SortDirection).subscribe();

      const req = httpTestingController.expectOne(
        (request) => request.url === `${apiUrl}/feed`,
      );

      expect(req.request.params.get('sort')).toBe('createdAt,asc');

      req.flush(mockPage);
    });

    it('should return the requested page of articles', () => {
      service.getArticlesFeed(0, 10, 'desc' as SortDirection).subscribe(
        (response) => {
          expect(response).toEqual(mockPage);
          expect(response.content).toEqual([mockArticle]);
          expect(response.totalElements).toBe(1);
        },
      );

      const req = httpTestingController.expectOne(
        (request) =>
          request.url === `${apiUrl}/feed` &&
          request.params.get('page') === '0' &&
          request.params.get('size') === '10' &&
          request.params.get('sort') === 'createdAt,desc',
      );

      req.flush(mockPage);
    });

    it('should propagate an HTTP error', () => {
      service.getArticlesFeed(0, 10, 'desc' as SortDirection).subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error: { status: number }) => {
          expect(error.status).toBe(500);
        },
      });

      const req = httpTestingController.expectOne(
        (request) => request.url === `${apiUrl}/feed`,
      );

      req.flush(
        { message: 'Server error' },
        {
          status: 500,
          statusText: 'Internal Server Error',
        },
      );
    });
  });

  // postArticle

  describe('postArticle', () => {
    it('should send a POST request to the article endpoint', () => {
      service.postArticle(mockArticleNew).subscribe();

      const req = httpTestingController.expectOne(apiUrl);

      expect(req.request.method).toBe('POST');
      expect(req.request.url).toBe(apiUrl);

      req.flush(mockArticle);
    });

    it('should send the new article in the request body', () => {
      service.postArticle(mockArticleNew).subscribe();

      const req = httpTestingController.expectOne(apiUrl);

      expect(req.request.body).toEqual(mockArticleNew);

      req.flush(mockArticle);
    });

    it('should return the created article', () => {
      service.postArticle(mockArticleNew).subscribe((response) => {
        expect(response).toEqual(mockArticle);
        expect(response.publicId).toBe('article-1');
        expect(response.title).toBe('Test article');
      });

      const req = httpTestingController.expectOne(apiUrl);

      req.flush(mockArticle);
    });

    it('should propagate an HTTP error', () => {
      service.postArticle(mockArticleNew).subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error: { status: number }) => {
          expect(error.status).toBe(400);
        },
      });

      const req = httpTestingController.expectOne(apiUrl);

      req.flush(
        { message: 'Invalid article data' },
        {
          status: 400,
          statusText: 'Bad Request',
        },
      );
    });
  });
});