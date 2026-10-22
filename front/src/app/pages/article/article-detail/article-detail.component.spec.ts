import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { of, Subject, throwError } from 'rxjs';

import { ArticleDetailComponent } from './article-detail.component';
import { ArticleService } from '@service/article.service';
import { CommentService } from '@service/comment.service';
import { ArticleComment } from '@model/article-comment.model';
import { Article } from '@model/article.model';

describe('ArticleDetailComponent', () => {
  let component: ArticleDetailComponent;
  let fixture: ComponentFixture<ArticleDetailComponent>;

  let articleService: jasmine.SpyObj<ArticleService>;
  let commentService: jasmine.SpyObj<CommentService>;

  const articleSlug = 'test-article';

  const mockComment = {
    content: 'Mon premier commentaire',
  } as ArticleComment;

  const mockArticle = {
    slug: articleSlug,
    title: 'Mon article de test',
    content: '<p>Contenu de mon article</p>',
    createdAt: new Date('2026-10-21'),
    username: 'test-user',
    updatedAt: new Date('2026-10-21'),
    topic: {
      slug: 'angular',
      name: 'Angular',
      description: 'Discussions autour d’Angular.',
      subscribed: false,
      createdAt: new Date('2025-01-01T00:00:00Z'),
      updatedAt: new Date('2025-01-02T00:00:00Z'),
    },
    comments: [],
  } as Article;

  beforeEach(async () => {
    articleService = jasmine.createSpyObj<ArticleService>('ArticleService', [
      'getArticleBySlug',
    ]);

    commentService = jasmine.createSpyObj<CommentService>('CommentService', [
      'postComment',
    ]);

    articleService.getArticleBySlug.and.returnValue(of(mockArticle));
    commentService.postComment.and.returnValue(of(mockComment));

    await TestBed.configureTestingModule({
      imports: [ArticleDetailComponent],
      providers: [
        {
          provide: ArticleService,
          useValue: articleService,
        },
        {
          provide: CommentService,
          useValue: commentService,
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({
                slug: articleSlug,
              }),
            },
          },
        },
      ],
    })
      .overrideComponent(ArticleDetailComponent, {
        add: {
          imports: [RouterLink, MatFormFieldModule],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ArticleDetailComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    fixture.destroy();
  });

  describe('initialization', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should retrieve the article using the route slug', () => {
      fixture.detectChanges();

      expect(articleService.getArticleBySlug).toHaveBeenCalledOnceWith(
        articleSlug,
      );
    });

    it('should display the article after loading', () => {
      fixture.detectChanges();

      const compiled: HTMLElement = fixture.nativeElement;

      expect(compiled.querySelector('h1')?.textContent).toContain(
        mockArticle.title,
      );

      expect(compiled.querySelector('.article__content')?.innerHTML).toContain(
        'Contenu de mon article',
      );
    });

    it('should display the article metadata', () => {
      fixture.detectChanges();

      const compiled: HTMLElement = fixture.nativeElement;

      expect(compiled.querySelector('.article__meta')?.textContent).toContain(
        mockArticle.createdAt,
      );

      expect(compiled.querySelector('.article__meta')?.textContent).toContain(
        mockArticle.username,
      );

      expect(compiled.querySelector('.article__meta')?.textContent).toContain(
        mockArticle.topic.name,
      );
    });

    it('should display the loading indicator while retrieving the article', () => {
      const articleSubject = new Subject<Article>();

      articleService.getArticleBySlug.and.returnValue(
        articleSubject.asObservable(),
      );

      fixture.detectChanges();

      const compiled: HTMLElement = fixture.nativeElement;

      expect(compiled.querySelector('.article-detail__loading')).toBeTruthy();

      expect(compiled.textContent).toContain('Chargement de l’article.');

      articleSubject.next(mockArticle);
      articleSubject.complete();

      fixture.detectChanges();

      expect(compiled.querySelector('.article-detail__loading')).toBeFalsy();

      expect(compiled.querySelector('.article')).toBeTruthy();
    });

    it('should display the not-found message when the article request fails', () => {
      spyOn(console, 'error');

      articleService.getArticleBySlug.and.returnValue(
        throwError(() => new Error('Article not found')),
      );

      fixture.detectChanges();

      const compiled: HTMLElement = fixture.nativeElement;

      expect(compiled.querySelector('.article-detail__empty')).toBeTruthy();

      expect(compiled.textContent).toContain('Article introuvable');
    });
  });

  describe('comments display', () => {
    it('should display the empty message when the article has no comments', () => {
      fixture.detectChanges();

      const compiled: HTMLElement = fixture.nativeElement;

      expect(compiled.querySelector('.comments__empty')).toBeTruthy();

      expect(compiled.textContent).toContain(
        'Il n’y a pas de commentaire, soyez le premier à en écrire un.',
      );
    });

    it('should display the article comments', () => {
      const articleWithComments = {
        ...mockArticle,
        comments: [mockComment],
      } as Article;

      articleService.getArticleBySlug.and.returnValue(of(articleWithComments));

      fixture.detectChanges();

      const compiled: HTMLElement = fixture.nativeElement;

      expect(compiled.querySelector('app-comment')).toBeTruthy();
      expect(compiled.querySelector('.comments__empty')).toBeFalsy();
    });
  });

  describe('comment form', () => {
    it('should disable the submit button when the form is empty', () => {
      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        'button[type="submit"]',
      );

      expect(button.disabled).toBeTrue();
    });

    it('should enable the submit button when the comment is valid', () => {
      fixture.detectChanges();

      const textarea: HTMLTextAreaElement =
        fixture.nativeElement.querySelector('#comment-content');

      textarea.value = 'Mon nouveau commentaire';
      textarea.dispatchEvent(new Event('input'));

      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        'button[type="submit"]',
      );

      expect(button.disabled).toBeFalse();
    });

    it('should not submit an empty comment', () => {
      fixture.detectChanges();

      const form: HTMLFormElement = fixture.nativeElement.querySelector('form');

      form.dispatchEvent(
        new Event('submit', {
          bubbles: true,
          cancelable: true,
        }),
      );

      expect(commentService.postComment).not.toHaveBeenCalled();
    });

    it('should submit a valid comment using the article slug', () => {
      fixture.detectChanges();

      const textarea: HTMLTextAreaElement =
        fixture.nativeElement.querySelector('#comment-content');

      textarea.value = 'Mon nouveau commentaire';
      textarea.dispatchEvent(new Event('input'));

      fixture.detectChanges();

      const form: HTMLFormElement = fixture.nativeElement.querySelector('form');

      form.dispatchEvent(
        new Event('submit', {
          bubbles: true,
          cancelable: true,
        }),
      );

      expect(commentService.postComment).toHaveBeenCalledOnceWith(
        articleSlug,
        'Mon nouveau commentaire',
      );
    });

    it('should reset the form after successfully submitting a comment', () => {
      fixture.detectChanges();

      const textarea: HTMLTextAreaElement =
        fixture.nativeElement.querySelector('#comment-content');

      textarea.value = 'Mon nouveau commentaire';
      textarea.dispatchEvent(new Event('input'));

      fixture.detectChanges();

      const form: HTMLFormElement = fixture.nativeElement.querySelector('form');

      form.dispatchEvent(
        new Event('submit', {
          bubbles: true,
          cancelable: true,
        }),
      );

      fixture.detectChanges();

      expect(textarea.value).toBe('');
    });

    it('should append the new comment to the article', () => {
      fixture.detectChanges();

      const textarea: HTMLTextAreaElement =
        fixture.nativeElement.querySelector('#comment-content');

      textarea.value = 'Mon nouveau commentaire';
      textarea.dispatchEvent(new Event('input'));

      fixture.detectChanges();

      const form: HTMLFormElement = fixture.nativeElement.querySelector('form');

      form.dispatchEvent(
        new Event('submit', {
          bubbles: true,
          cancelable: true,
        }),
      );

      fixture.detectChanges();

      const comments: HTMLElement[] = Array.from(
        fixture.nativeElement.querySelectorAll('app-comment'),
      );

      expect(comments.length).toBe(1);
      expect(
        fixture.nativeElement.querySelector('.comments__empty'),
      ).toBeFalsy();
    });

    it('should stop loading after a comment submission error', () => {
      spyOn(console, 'error');

      commentService.postComment.and.returnValue(
        throwError(() => new Error('Comment submission failed')),
      );

      fixture.detectChanges();

      const textarea: HTMLTextAreaElement =
        fixture.nativeElement.querySelector('#comment-content');

      textarea.value = 'Mon nouveau commentaire';
      textarea.dispatchEvent(new Event('input'));

      fixture.detectChanges();

      const form: HTMLFormElement = fixture.nativeElement.querySelector('form');

      form.dispatchEvent(
        new Event('submit', {
          bubbles: true,
          cancelable: true,
        }),
      );

      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        'button[type="submit"]',
      );

      expect(button.disabled).toBeFalse();
    });
  });
});
