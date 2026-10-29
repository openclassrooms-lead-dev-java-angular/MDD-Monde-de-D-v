import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { CommentService } from './comment.service';
import { environment } from '@env/environment';
import { ArticleComment } from '@model/article-comment.model';

describe('CommentService', () => {
  let service: CommentService;
  let httpTestingController: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/${environment.apiRoute.comment}`;

  const mockComment = {
    content: 'This is a test comment',
    username: 'test-user',
    createdAt: new Date('2026-01-15T10:00:00Z'),
  } as ArticleComment;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CommentService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(CommentService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('postComment', () => {
    it('should send a POST request to the correct URL', () => {
      const articleSlug = 'my-article';
      const content = 'This is a test comment';

      service.postComment(articleSlug, content).subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/${articleSlug}/comments`,
      );

      expect(req.request.method).toBe('POST');

      req.flush(mockComment);
    });

    it('should send the comment content in the request body', () => {
      const articleSlug = 'my-article';
      const content = 'This is a test comment';

      service.postComment(articleSlug, content).subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/${articleSlug}/comments`,
      );

      expect(req.request.body).toEqual({
        content,
      });

      req.flush(mockComment);
    });

    it('should return the created comment', () => {
      const articleSlug = 'my-article';
      const content = 'This is a test comment';

      service.postComment(articleSlug, content).subscribe((response) => {
        expect(response).toEqual(mockComment);
        expect(response.content).toBe(content);
        expect(response.username).toBe('test-user');
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/${articleSlug}/comments`,
      );

      req.flush(mockComment);
    });

    it('should use the provided article slug in the URL', () => {
      const articleSlug = 'another-article';

      service.postComment(articleSlug, 'A comment').subscribe();

      const req = httpTestingController.expectOne(
        `${apiUrl}/${articleSlug}/comments`,
      );

      expect(req.request.url).toBe(`${apiUrl}/another-article/comments`);

      req.flush(mockComment);
    });

    it('should propagate an HTTP error', () => {
      let receivedError: unknown;

      service.postComment('my-article', 'A comment').subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error: unknown) => {
          receivedError = error;

          expect((error as { status: number }).status).toBe(500);
        },
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/my-article/comments`,
      );

      req.flush('Server error', {
        status: 500,
        statusText: 'Internal Server Error',
      });

      expect(receivedError).toBeTruthy();
    });

    it('should propagate an unauthorized error', () => {
      service.postComment('my-article', 'A comment').subscribe({
        next: () => fail('Expected an HTTP error'),
        error: (error: { status: number }) => {
          expect(error.status).toBe(401);
        },
      });

      const req = httpTestingController.expectOne(
        `${apiUrl}/my-article/comments`,
      );

      req.flush('Unauthorized', {
        status: 401,
        statusText: 'Unauthorized',
      });
    });
  });
});
