import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { ArticleCardComponent } from './article-card.component';
import { Article } from '@model/article.model';
import { Topic } from '@model/topic.model';

describe('ArticleCardComponent', () => {
  let component: ArticleCardComponent;
  let fixture: ComponentFixture<ArticleCardComponent>;

  const mockTopic: Topic = {
    slug: 'angular',
    name: 'Angular',
    description: 'Discussions autour d’Angular.',
    subscribed: false,
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-02T00:00:00Z'),
  };

  const mockArticle = {
    slug: 'mon-premier-article',
    title: 'Mon premier article',
    topic: mockTopic,
    createdAt: new Date('2026-10-15T10:00:00.000Z'),
    username: 'john.doe',
    content: 'Voici le contenu de mon premier article.',
    updatedAt: new Date('2026-10-15T10:00:00.000Z'),
  } as Article;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ArticleCardComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(ArticleCardComponent);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('article', mockArticle);
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('article rendering', () => {
    it('should display the article title', () => {
      const title: HTMLElement = fixture.nativeElement.querySelector(
        '.article-card__title',
      );

      expect(title).not.toBeNull();
      expect(title.textContent?.trim()).toBe(mockArticle.title);
    });

    it('should display the article author', () => {
      const author: HTMLElement = fixture.nativeElement.querySelector(
        '.article-card__author',
      );

      expect(author).not.toBeNull();
      expect(author.textContent?.trim()).toBe(mockArticle.username);
    });

    it('should display the article content', () => {
      const content: HTMLElement = fixture.nativeElement.querySelector(
        '.article-card__content',
      );

      expect(content).not.toBeNull();
      expect(content.textContent?.trim()).toBe(mockArticle.content);
    });

    it('should display the article date in dd/MM/yyyy format', () => {
      const date: HTMLElement =
        fixture.nativeElement.querySelector('.article-date');

      expect(date).not.toBeNull();

      const expectedDate = new Intl.DateTimeFormat('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        timeZone: 'UTC',
      }).format(new Date(mockArticle.createdAt));

      expect(date.textContent?.trim()).toBe(expectedDate);
    });
  });

  describe('navigation', () => {
    it('should link to the article details page using its slug', () => {
      const link: HTMLAnchorElement =
        fixture.nativeElement.querySelector('.article-link');

      expect(link).not.toBeNull();
      expect(link.getAttribute('href')).toBe(`/articles/${mockArticle.slug}`);
    });

    it('should contain the article card inside the link', () => {
      const link: HTMLAnchorElement =
        fixture.nativeElement.querySelector('.article-link');

      const article = link.querySelector('article.article-card');

      expect(article).not.toBeNull();
    });
  });

  describe('truncate pipe', () => {
    it('should render the content through the truncate pipe', () => {
      const content: HTMLElement = fixture.nativeElement.querySelector(
        '.article-card__content',
      );

      expect(content.textContent?.trim()).toBe(mockArticle.content);
    });

    it('should truncate content longer than 100 characters', () => {
      const longContent = 'A'.repeat(150);

      fixture.componentRef.setInput('article', {
        ...mockArticle,
        content: longContent,
      });

      fixture.detectChanges();

      const content: HTMLElement = fixture.nativeElement.querySelector(
        '.article-card__content',
      );

      expect(content.textContent?.trim().length).toBeLessThan(150);
    });
  });

  describe('input', () => {
    it('should render updated article data when the input changes', () => {
      const updatedArticle = {
        ...mockArticle,
        slug: 'article-modifie',
        title: 'Article modifié',
        username: 'jane.doe',
      };

      fixture.componentRef.setInput('article', updatedArticle);
      fixture.detectChanges();

      expect(
        fixture.nativeElement
          .querySelector('.article-card__title')
          .textContent.trim(),
      ).toBe('Article modifié');

      expect(
        fixture.nativeElement
          .querySelector('.article-card__author')
          .textContent.trim(),
      ).toBe('jane.doe');

      expect(
        fixture.nativeElement
          .querySelector('.article-link')
          .getAttribute('href'),
      ).toBe('/articles/article-modifie');
    });
  });
});
