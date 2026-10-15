import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, RouterLink, convertToParamMap } from '@angular/router';
import { Subject, throwError } from 'rxjs';

import { ArticleDetailComponent } from './article-detail.component';
import { ArticleService } from '@service/article.service';
import { Article } from '@model/article.model';

describe('ArticleDetailComponent', () => {
  let component: ArticleDetailComponent;
  let fixture: ComponentFixture<ArticleDetailComponent>;
  let articleService: jasmine.SpyObj<ArticleService>;
  let articleSubject: Subject<Article>;

  const date = new Date('2026-10-15');

  const mockTopic = {
    slug: 'topic-example',
    name: 'topic example',
    description: 'topic description',
    subscribed: true,
    createdAt: date,
    updatedAt: date,
  };
  const mockArticle = {
    title: 'My first article',
    topic: mockTopic,
    slug: 'my-first-article',
    content: '<p>My article content</p>',
    createdAt: date,
    updatedAt: date,
    username: 'fred',
  } as Article;

  beforeEach(async () => {
    articleService = jasmine.createSpyObj<ArticleService>('ArticleService', [
      'getArticleBySlug',
    ]);

    articleSubject = new Subject<Article>();

    articleService.getArticleBySlug.and.returnValue(
      articleSubject.asObservable(),
    );

    await TestBed.configureTestingModule({
      imports: [ArticleDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({
                slug: 'my-first-article',
              }),
            },
          },
        },
        {
          provide: ArticleService,
          useValue: articleService,
        },
      ],
    })
      .overrideComponent(ArticleDetailComponent, {
        set: {
          imports: [RouterLink],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ArticleDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should retrieve the article using the route slug', () => {
    fixture.detectChanges();

    expect(articleService.getArticleBySlug).toHaveBeenCalledWith(
      'my-first-article',
    );
  });

  it('should display the loading indicator while the request is pending', () => {
    fixture.detectChanges();

    const loading = fixture.nativeElement.querySelector(
      '.article-detail__loading',
    );

    expect(loading).toBeTruthy();
    expect(loading.textContent).toContain('Loading article...');
  });

  it('should display the article details after loading', () => {
    fixture.detectChanges();

    articleSubject.next(mockArticle);
    fixture.detectChanges();

    const title = fixture.nativeElement.querySelector('.article__title');

    const content = fixture.nativeElement.querySelector('.article__content');

    const meta = fixture.nativeElement.querySelector('.article__meta');

    expect(title.textContent).toContain('My first article');
    expect(content.textContent).toContain('My article content');
    expect(meta.textContent).toContain('2026-10-15');
    expect(meta.textContent).toContain('fred');

    expect(
      fixture.nativeElement.querySelector('.article-detail__loading'),
    ).toBeNull();
  });

  it('should display a message when the article cannot be found', () => {
    articleService.getArticleBySlug.and.returnValue(
      throwError(() => new Error('Article not found')),
    );

    fixture.detectChanges();

    const empty = fixture.nativeElement.querySelector('.article-detail__empty');

    expect(empty).toBeTruthy();
    expect(empty.textContent).toContain('Article not found');
    expect(empty.textContent).toContain(
      'The article you are looking for does not exist or is no longer available.',
    );
  });

  it('should stop loading when an error occurs', () => {
    spyOn(console, 'error');

    articleService.getArticleBySlug.and.returnValue(
      throwError(() => new Error('Server error')),
    );

    fixture.detectChanges();

    expect(console.error).toHaveBeenCalled();
    expect(
      fixture.nativeElement.querySelector('.article-detail__loading'),
    ).toBeNull();
  });

  it('should render the article HTML content', () => {
    fixture.detectChanges();

    articleSubject.next({
      ...mockArticle,
      content: '<p>Formatted <strong>content</strong></p>',
    } as Article);

    fixture.detectChanges();

    const content = fixture.nativeElement.querySelector('.article__content');

    expect(content.querySelector('strong')).toBeTruthy();
    expect(content.textContent).toContain('content');
  });
});
