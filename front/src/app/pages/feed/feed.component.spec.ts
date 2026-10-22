import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, of, Subject, throwError } from 'rxjs';

import { FeedComponent } from './feed.component';
import { ArticleService } from '@service/article.service';
import { Article } from '@model/article.model';
import { Page } from '@type/page.type';

describe('FeedComponent', () => {
  let component: FeedComponent;
  let fixture: ComponentFixture<FeedComponent>;
  let articleService: jasmine.SpyObj<ArticleService>;

  const mockArticles = [
    {
      slug: 'article-1',
      title: 'Premier article',
    },
    {
      slug: 'article-2',
      title: 'Deuxième article',
    },
  ] as Article[];

  const createPage = (
    content: Article[] = mockArticles,
    number = 0,
    last = false,
  ): Page<Article> => ({
    content,
    number,
    numberOfElements: number,
    last,
    totalPages: last ? number + 1 : number + 2,
    totalElements: content.length,
    size: 10,
    first: number === 0,
    empty: content.length === 0,
  });

  const setup = (response: Observable<any> = of(createPage())): void => {
    articleService.getArticlesFeed.and.returnValue(response);

    fixture = TestBed.createComponent(FeedComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  };

  beforeEach(async () => {
    articleService = jasmine.createSpyObj<ArticleService>('ArticleService', [
      'getArticlesFeed',
    ]);

    await TestBed.configureTestingModule({
      imports: [FeedComponent],
      providers: [
        {
          provide: ArticleService,
          useValue: articleService,
        },
      ],
    })
      .overrideComponent(FeedComponent, {
        set: {
          imports: [],
          schemas: [CUSTOM_ELEMENTS_SCHEMA],
        },
      })
      .compileComponents();
  });

  describe('initialization', () => {
    it('should create the component', () => {
      setup();

      expect(component).toBeTruthy();
    });

    it('should load the first page on initialization', () => {
      setup();

      expect(articleService.getArticlesFeed).toHaveBeenCalledOnceWith(
        0,
        10,
        'desc',
      );
    });

    it('should initialize the loading state to false after the response', () => {
      setup();

      expect((component as any).isLoading()).toBeFalse();
    });
  });

  describe('loadPage', () => {
    it('should display the articles returned by the service', () => {
      setup(of(createPage()));

      const articleCards =
        fixture.nativeElement.querySelectorAll('app-article-card');

      expect(articleCards.length).toBe(2);
    });

    it('should append articles instead of replacing existing articles', () => {
      const secondPage = [
        {
          slug: 'article-3',
          title: 'Troisième article',
        },
      ] as Article[];

      articleService.getArticlesFeed.and.returnValues(
        of(createPage(mockArticles, 0, false)),
        of(createPage(secondPage, 1, true)),
      );

      fixture = TestBed.createComponent(FeedComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      (component as any).loadMore();
      fixture.detectChanges();

      const articleCards =
        fixture.nativeElement.querySelectorAll('app-article-card');

      expect(articleCards.length).toBe(3);
    });

    it('should request the next page after a successful response', () => {
      articleService.getArticlesFeed.and.returnValues(
        of(createPage(mockArticles, 0, false)),
        of(createPage([], 1, true)),
      );

      fixture = TestBed.createComponent(FeedComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      (component as any).loadMore();

      expect(articleService.getArticlesFeed).toHaveBeenCalledTimes(2);
      expect(articleService.getArticlesFeed).toHaveBeenCalledWith(
        1,
        10,
        'desc',
      );
    });

    it('should update hasMore when the last page is reached', () => {
      setup(of(createPage(mockArticles, 0, true)));

      expect((component as any).hasMore()).toBeFalse();
    });

    it('should keep hasMore true when more pages are available', () => {
      setup(of(createPage(mockArticles, 0, false)));

      expect((component as any).hasMore()).toBeTrue();
    });
  });

  describe('loadMore', () => {
    it('should not load another page when no more pages are available', () => {
      setup(of(createPage(mockArticles, 0, true)));

      (component as any).loadMore();

      expect(articleService.getArticlesFeed).toHaveBeenCalledTimes(1);
    });

    it('should not trigger another request while loading', () => {
      const pendingRequest = new Subject<any>();

      setup(pendingRequest.asObservable());

      expect((component as any).isLoading()).toBeTrue();

      (component as any).loadMore();

      expect(articleService.getArticlesFeed).toHaveBeenCalledTimes(1);

      pendingRequest.next(createPage());
      pendingRequest.complete();
    });
  });

  describe('loading state', () => {
    it('should set isLoading to true while the request is pending', () => {
      const pendingRequest = new Subject<any>();

      setup(pendingRequest.asObservable());

      expect((component as any).isLoading()).toBeTrue();

      pendingRequest.next(createPage());
      pendingRequest.complete();

      expect((component as any).isLoading()).toBeFalse();
    });

    it('should reset isLoading when the request succeeds', () => {
      setup(of(createPage()));

      expect((component as any).isLoading()).toBeFalse();
    });

    it('should reset isLoading when the request fails', () => {
      spyOn(console, 'error');

      setup(throwError(() => new Error('Erreur serveur')));

      expect((component as any).isLoading()).toBeFalse();
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('template', () => {
    it('should display the empty state when no articles are available', () => {
      setup(of(createPage([], 0, true)));

      const emptyState: HTMLElement | null =
        fixture.nativeElement.querySelector('.empty-state');

      expect(emptyState).not.toBeNull();
      expect(emptyState?.textContent).toContain('Aucun article disponible.');
    });

    it('should not display the empty state when articles exist', () => {
      setup(of(createPage()));

      const emptyState = fixture.nativeElement.querySelector('.empty-state');

      expect(emptyState).toBeNull();
    });

    it('should display the create article button', () => {
      setup();

      const createButton = fixture.nativeElement.querySelector('app-button');

      expect(createButton).not.toBeNull();
      expect(createButton.getAttribute('label')).toBe('Ajouter un article');
    });

    it('should display the sort button', () => {
      setup();

      const sortButton = fixture.nativeElement.querySelector('.btn-sort');

      expect(sortButton).not.toBeNull();
      expect(sortButton.getAttribute('aria-label')).toBe(
        'Trier par date croissante',
      );
    });

    it('should display the descending sort icon by default', () => {
      setup();

      const sortIcon = fixture.nativeElement.querySelector('.sort-icon');

      expect(sortIcon.textContent.trim()).toBe('↓');
    });
  });
});
