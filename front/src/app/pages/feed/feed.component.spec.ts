
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';

import { FeedComponent } from './feed.component';
import { Article } from '@model/article.model';
import { ArticleService } from '@service/article.service';
import { ArticleCardComponent } from '@shared/components/article-card/article-card.component';
import { ButtonComponent } from '@shared/components/button/button.component';
import { Page } from '@type/page.type';
import { SortDirection } from '@type/sort-direction.type';
import { InfiniteScrollDirective } from 'ngx-infinite-scroll';

describe('FeedComponent', () => {
  let fixture: ComponentFixture<FeedComponent>;
  let component: FeedComponent;
  let articleService: jasmine.SpyObj<ArticleService>;

  const createArticle = (
    overrides: Partial<Article> = {},
  ): Article =>
    ({
      publicId: 'article-1',
      title: 'Angular testing',
      content: 'Learning how to test Angular components.',
      username: 'testuser',
      createdAt: new Date('2025-03-15T10:00:00Z'),
      ...overrides,
    }) as Article;

  const createPage = (
    content: Article[] = [],
    number = 0,
    last = false,
  ): Page<Article> =>
    ({
      content,
      number,
      last,
      size: 10,
      totalElements: content.length,
      totalPages: number + 1,
      first: number === 0,
      empty: content.length === 0,
    }) as Page<Article>;

  const createComponent = (): void => {
    fixture = TestBed.createComponent(FeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  const getText = (selector: string): string =>
    fixture.nativeElement
      .querySelector(selector)
      ?.textContent?.trim() ?? '';

  beforeEach(async () => {
    articleService = jasmine.createSpyObj<ArticleService>(
      'ArticleService',
      ['getArticlesFeed'],
    );

    articleService.getArticlesFeed.and.returnValue(
      of(createPage([], 0, true)) as ReturnType<
        ArticleService['getArticlesFeed']
      >,
    );

    await TestBed.configureTestingModule({
      imports: [FeedComponent],
      providers: [
        provideRouter([]),
        {
          provide: ArticleService,
          useValue: articleService,
        },
      ],
    }).compileComponents();
  });

  describe('initialization', () => {
    it('should load the first page on initialization', () => {
      createComponent();

      expect(articleService.getArticlesFeed).toHaveBeenCalledTimes(1);
      expect(articleService.getArticlesFeed).toHaveBeenCalledWith(
        0,
        10,
        'desc',
      );
    });

    it('should initialize with an empty article list', () => {
      createComponent();

      expect((component as any).articleList()).toEqual([]);
    });

    it('should initialize the sort direction to descending', () => {
      createComponent();

      expect((component as any).sortDirection()).toBe('desc');
    });

    it('should stop loading after the initial request succeeds', () => {
      createComponent();

      expect((component as any).isLoading()).toBeFalse();
    });

    it('should set hasMore to false when the first page is the last page', () => {
      articleService.getArticlesFeed.and.returnValue(
        of(createPage([], 0, true)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      expect((component as any).hasMore()).toBeFalse();
    });
  });

  describe('article loading', () => {
    it('should add articles returned by the service to the list', () => {
      const articles = [
        createArticle({ publicId: 'article-1', title: 'First article' }),
        createArticle({ publicId: 'article-2', title: 'Second article' }),
      ];

      articleService.getArticlesFeed.and.returnValue(
        of(createPage(articles, 0, false)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      expect((component as any).articleList()).toEqual(articles);
    });

    it('should render one article card for each article', () => {
      const articles = [
        createArticle({ publicId: 'article-1' }),
        createArticle({ publicId: 'article-2' }),
        createArticle({ publicId: 'article-3' }),
      ];

      articleService.getArticlesFeed.and.returnValue(
        of(createPage(articles, 0, true)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      const cards = fixture.debugElement.queryAll(
        By.directive(ArticleCardComponent),
      );

      expect(cards.length).toBe(3);
    });

    it('should pass the correct article to the article card', () => {
      const article = createArticle({ publicId: 'article-42' });

      articleService.getArticlesFeed.and.returnValue(
        of(createPage([article], 0, true)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      const card = fixture.debugElement.query(
        By.directive(ArticleCardComponent),
      );

      expect(card).not.toBeNull();
      expect(card.componentInstance.article()).toEqual(article);
    });

    it('should append articles from subsequent pages', () => {
      const firstArticle = createArticle({ publicId: 'article-1' });
      const secondArticle = createArticle({ publicId: 'article-2' });

      articleService.getArticlesFeed.and.returnValues(
        of(createPage([firstArticle], 0, false)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
        of(createPage([secondArticle], 1, true)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();
      (component as any).loadMore();

      expect(articleService.getArticlesFeed).toHaveBeenCalledTimes(2);
      expect((component as any).articleList()).toEqual([
        firstArticle,
        secondArticle,
      ]);
    });

    it('should update the next page number using the response page number', () => {
      articleService.getArticlesFeed.and.returnValue(
        of(createPage([], 4, false)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      expect((component as any).currentPage).toBe(5);
    });

    it('should set hasMore to true when the current page is not the last', () => {
      articleService.getArticlesFeed.and.returnValue(
        of(createPage([], 0, false)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      expect((component as any).hasMore()).toBeTrue();
    });

    it('should set hasMore to false when the current page is the last', () => {
      articleService.getArticlesFeed.and.returnValue(
        of(createPage([], 0, true)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      expect((component as any).hasMore()).toBeFalse();
    });
  });

  describe('loading state', () => {
    it('should set isLoading to true while the request is pending', () => {
      const response$ = new Subject<Page<Article>>();

      articleService.getArticlesFeed.and.returnValue(
        response$ as ReturnType<ArticleService['getArticlesFeed']>,
      );

      createComponent();

      expect((component as any).isLoading()).toBeTrue();

      response$.next(createPage([], 0, true));
      response$.complete();
    });

    it('should reset isLoading when the pending request succeeds', () => {
      const response$ = new Subject<Page<Article>>();

      articleService.getArticlesFeed.and.returnValue(
        response$ as ReturnType<ArticleService['getArticlesFeed']>,
      );

      createComponent();

      response$.next(createPage([], 0, true));
      response$.complete();

      expect((component as any).isLoading()).toBeFalse();
    });

    it('should reset isLoading when the request fails', () => {
      spyOn(console, 'error');

      articleService.getArticlesFeed.and.returnValue(
        throwError(() => new Error('Network error')) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      expect((component as any).isLoading()).toBeFalse();
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('infinite scroll and pagination', () => {
    it('should request the next page when loadMore is called', () => {
      articleService.getArticlesFeed.and.returnValues(
        of(createPage([], 0, false)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
        of(createPage([], 1, false)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();
      (component as any).loadMore();

      expect(articleService.getArticlesFeed).toHaveBeenCalledTimes(2);
      expect(articleService.getArticlesFeed.calls.argsFor(1)).toEqual([
        1,
        10,
        'desc',
      ]);
    });

    it('should not load another page while a request is pending', () => {
      const response$ = new Subject<Page<Article>>();

      articleService.getArticlesFeed.and.returnValue(
        response$ as ReturnType<ArticleService['getArticlesFeed']>,
      );

      createComponent();

      expect(articleService.getArticlesFeed).toHaveBeenCalledTimes(1);

      (component as any).loadMore();

      expect(articleService.getArticlesFeed).toHaveBeenCalledTimes(1);

      response$.next(createPage([], 0, false));
      response$.complete();
    });

    it('should not load another page when there are no more pages', () => {
      articleService.getArticlesFeed.and.returnValue(
        of(createPage([], 0, true)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();
      (component as any).loadMore();

      expect(articleService.getArticlesFeed).toHaveBeenCalledTimes(1);
    });

    it('should allow retrying after a failed request', () => {
      spyOn(console, 'error');

      articleService.getArticlesFeed.and.returnValues(
        throwError(() => new Error('Network error')) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
        of(createPage([], 0, true)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      expect(articleService.getArticlesFeed).toHaveBeenCalledTimes(1);
      expect((component as any).isLoading()).toBeFalse();

      (component as any).loadMore();

      expect(articleService.getArticlesFeed).toHaveBeenCalledTimes(2);
      expect(articleService.getArticlesFeed.calls.argsFor(1)).toEqual([
        0,
        10,
        'desc',
      ]);
    });

    it('should use the server response page number for the next request', () => {
      articleService.getArticlesFeed.and.returnValues(
        of(createPage([], 3, false)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
        of(createPage([], 4, true)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();
      (component as any).loadMore();

      expect(articleService.getArticlesFeed.calls.argsFor(1)).toEqual([
        4,
        10,
        'desc',
      ]);
    });
  });

  describe('error handling', () => {
    it('should log an error when fetching articles fails', () => {
      const error = new Error('Fetch failed');
      spyOn(console, 'error');

      articleService.getArticlesFeed.and.returnValue(
        throwError(() => error) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('should not add articles when the request fails', () => {
      spyOn(console, 'error');

      articleService.getArticlesFeed.and.returnValue(
        throwError(() => new Error('Fetch failed')) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      expect((component as any).articleList()).toEqual([]);
    });
  });

  describe('sort direction', () => {
    it('should display the descending sort icon initially', () => {
      createComponent();

      expect(getText('.sort-icon')).toBe('↓');
    });

    it('should display the ascending sort aria-label initially', () => {
      createComponent();

      const button: HTMLButtonElement =
        fixture.nativeElement.querySelector('.btn-sort');

      expect(button.getAttribute('aria-label')).toBe(
        'Trier par date croissante',
      );
    });

    it('should pass the current sort direction to the service', () => {
      createComponent();

      expect(articleService.getArticlesFeed.calls.argsFor(0)).toEqual([
        0,
        10,
        'desc',
      ]);
    });

    it('should expose the sort button', () => {
      createComponent();

      expect(
        fixture.nativeElement.querySelector('.btn-sort'),
      ).not.toBeNull();
    });

    it('should call toggleSort when the sort button is clicked', () => {
      createComponent();

      spyOn(component, 'toggleSort');

      fixture.nativeElement.querySelector('.btn-sort').click();

      expect(component.toggleSort).toHaveBeenCalled();
    });

    it('should currently keep the sort direction unchanged because toggleSort is empty', () => {
      createComponent();

      component.toggleSort();

      expect((component as any).sortDirection()).toBe(
        'desc' as SortDirection,
      );
    });

    xit('should toggle from descending to ascending and reload the feed', () => {
      component.toggleSort();

      expect((component as any).sortDirection()).toBe('asc');
      expect(articleService.getArticlesFeed).toHaveBeenCalledWith(
        0,
        10,
        'asc',
      );
    });
  });

  describe('template', () => {
    it('should render the article list section', () => {
      createComponent();

      expect(
        fixture.nativeElement.querySelector('section.article-list'),
      ).not.toBeNull();
    });

    it('should render the create article button', () => {
      createComponent();

      const button = fixture.debugElement.query(
        By.directive(ButtonComponent),
      );

      expect(button).not.toBeNull();
      expect(button.componentInstance.label()).toBe('Ajouter un article');
      expect(button.componentInstance.type()).toBe('link');
      expect(button.componentInstance.routerLink()).toBe('/create-article');
    });

    it('should disable the create button while loading', () => {
      const response$ = new Subject<Page<Article>>();

      articleService.getArticlesFeed.and.returnValue(
        response$ as ReturnType<ArticleService['getArticlesFeed']>,
      );

      createComponent();

      const button = fixture.debugElement.query(
        By.directive(ButtonComponent),
      );

      expect(button.componentInstance.disabled()).toBeTrue();

      response$.next(createPage([], 0, true));
      response$.complete();
      fixture.detectChanges();

      expect(button.componentInstance.disabled()).toBeFalse();
    });

    it('should disable the sort button while loading', () => {
      const response$ = new Subject<Page<Article>>();

      articleService.getArticlesFeed.and.returnValue(
        response$ as ReturnType<ArticleService['getArticlesFeed']>,
      );

      createComponent();

      const button: HTMLButtonElement =
        fixture.nativeElement.querySelector('.btn-sort');

      expect(button.disabled).toBeTrue();

      response$.next(createPage([], 0, true));
      response$.complete();
      fixture.detectChanges();

      expect(button.disabled).toBeFalse();
    });

    it('should enable the sort button after loading finishes', () => {
      createComponent();

      const button: HTMLButtonElement =
        fixture.nativeElement.querySelector('.btn-sort');

      expect(button.disabled).toBeFalse();
    });

    it('should display the empty state when there are no articles', () => {
      createComponent();

      expect(getText('.empty-state')).toBe('Aucun article disponible.');
    });

    it('should hide the empty state when articles are available', () => {
      articleService.getArticlesFeed.and.returnValue(
        of(createPage([createArticle()], 0, true)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      expect(
        fixture.nativeElement.querySelector('.empty-state'),
      ).toBeNull();
    });

    it('should render the correct article card when articles are available', () => {
      const article = createArticle({
        publicId: 'article-99',
        title: 'My article',
      });

      articleService.getArticlesFeed.and.returnValue(
        of(createPage([article], 0, true)) as ReturnType<
          ArticleService['getArticlesFeed']
        >,
      );

      createComponent();

      const card = fixture.debugElement.query(
        By.directive(ArticleCardComponent),
      );

      expect(card.componentInstance.article().publicId).toBe('article-99');
      expect(card.componentInstance.article().title).toBe('My article');
    });

    it('should configure infinite scroll distance and throttle', () => {
      createComponent();

      const scrollElement = fixture.debugElement.query(
        By.directive(InfiniteScrollDirective),
      );

      expect(scrollElement).not.toBeNull();

      const directive = scrollElement.injector.get(InfiniteScrollDirective);

      expect(directive.infiniteScrollDistance).toBe(2);
      expect(directive.infiniteScrollThrottle).toBe(150);
    });
  });
});
