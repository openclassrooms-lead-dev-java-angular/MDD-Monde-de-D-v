import { ArticleComment } from '@model/article-comment.model';
import { CommentService } from './comment.service';
import { environment } from '@env/environment';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

describe('CommentService', () => {
  let service: CommentService;
  let httpTestingController: HttpTestingController;

  const articleSlug = 'my-article';
  const commentContent = 'This is a test comment';

  const mockComment = {
    content: commentContent,
  } as ArticleComment;

  const expectedUrl = `${environment.apiUrl}/${environment.apiRoute.comment}/${articleSlug}/comments`;

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
      service.postComment(articleSlug, commentContent).subscribe();

      const req = httpTestingController.expectOne(expectedUrl);

      expect(req.request.method).toBe('POST');

      req.flush(mockComment);
    });

    it('should send the comment content in the request body', () => {
      service.postComment(articleSlug, commentContent).subscribe();

      const req = httpTestingController.expectOne(expectedUrl);

      expect(req.request.body).toEqual({
        content: commentContent,
      });

      req.flush(mockComment);
    });

    it('should return the comment received from the API', () => {
      let response: ArticleComment | undefined;

      service.postComment(articleSlug, commentContent).subscribe({
        next: (comment) => {
          response = comment;
        },
      });

      const req = httpTestingController.expectOne(expectedUrl);

      req.flush(mockComment);

      expect(response).toEqual(mockComment);
    });

    it('should propagate an HTTP error', () => {
      let receivedError: unknown;

      service.postComment(articleSlug, commentContent).subscribe({
        error: (error: unknown) => {
          receivedError = error;
        },
      });

      const req = httpTestingController.expectOne(expectedUrl);

      req.flush(
        { message: 'Internal server error' },
        {
          status: 500,
          statusText: 'Internal Server Error',
        },
      );

      expect(receivedError).toBeTruthy();
    });
  });
});
