import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Location } from '@angular/common';
import { provideLocationMocks } from '@angular/common/testing';
import { provideRouter, Router } from '@angular/router';

import { ArticleCardComponent } from './article-card.component';
import { Article } from '@model/article.model';

describe('ArticleCardComponent', () => {
  let component: ArticleCardComponent;
  let fixture: ComponentFixture<ArticleCardComponent>;
  let router: Router;
  let location: Location;

  const mockArticle: Article = {
    publicId: 'article-123',
    title: 'Introduction to Angular',
    content:
      'Angular is a framework for building web applications. It provides components, services, dependency injection, routing, and reactive programming features.',
    username: 'John_Doe',
    createdAt: new Date('2025-03-15T12:00:00.000Z'),
  } as Article;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ArticleCardComponent],
      providers: [
        provideRouter([
          { path: 'articles/:id', component: ArticleCardComponent },
        ]),
        provideLocationMocks(),
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    location = TestBed.inject(Location);

    fixture = TestBed.createComponent(ArticleCardComponent);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('article', mockArticle);

    fixture.detectChanges();
  });

  function query<T extends HTMLElement>(selector: string): T | null {
    return fixture.nativeElement.querySelector(selector) as T | null;
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('article input', () => {
    it('should display the article title', () => {
      const title = query<HTMLElement>('.article-card__title');

      expect(title).not.toBeNull();
      expect(title?.textContent?.trim()).toBe('Introduction to Angular');
    });

    it('should display the article author', () => {
      const author = query<HTMLElement>('.article-card__author');

      expect(author).not.toBeNull();
      expect(author?.textContent?.trim()).toBe('John_Doe');
    });

    it('should update the title when the article input changes', () => {
      fixture.componentRef.setInput('article', {
        ...mockArticle,
        title: 'Updated article title',
      });
      fixture.detectChanges();

      expect(
        query<HTMLElement>('.article-card__title')?.textContent?.trim(),
      ).toBe('Updated article title');
    });

    it('should update the author when the article input changes', () => {
      fixture.componentRef.setInput('article', {
        ...mockArticle,
        username: 'Jane_Doe',
      });
      fixture.detectChanges();

      expect(
        query<HTMLElement>('.article-card__author')?.textContent?.trim(),
      ).toBe('Jane_Doe');
    });

    it('should update the content when the article input changes', () => {
      fixture.componentRef.setInput('article', {
        ...mockArticle,
        content: 'Updated article content.',
      });
      fixture.detectChanges();

      expect(
        query<HTMLElement>('.article-card__content')?.textContent?.trim(),
      ).toBe('Updated article content.');
    });
  });

  describe('template structure', () => {
    it('should render the article link', () => {
      expect(query<HTMLAnchorElement>('a.article-link')).not.toBeNull();
    });

    it('should render the article element inside the link', () => {
      const link = query<HTMLAnchorElement>('a.article-link');

      expect(link?.querySelector('article.article-card')).not.toBeNull();
    });

    it('should render the title, metadata and content', () => {
      const article = query<HTMLElement>('.article-card');

      expect(article?.querySelector('.article-card__title')).not.toBeNull();
      expect(article?.querySelector('.article-card__meta')).not.toBeNull();
      expect(article?.querySelector('.article-card__content')).not.toBeNull();
    });

    it('should render the date and author inside the metadata container', () => {
      const metadata = query<HTMLElement>('.article-card__meta');

      expect(metadata?.querySelector('.article-date')).not.toBeNull();
      expect(metadata?.querySelector('.article-card__author')).not.toBeNull();
    });
  });

  describe('date formatting', () => {
    it('should display the date in dd/MM/yyyy format', () => {
      const date = query<HTMLElement>('.article-date');

      expect(date?.textContent?.trim()).toBe('15/03/2025');
    });

    it('should update the displayed date when the article changes', () => {
      fixture.componentRef.setInput('article', {
        ...mockArticle,
        createdAt: new Date('2024-12-25T12:00:00.000Z'),
      });
      fixture.detectChanges();

      expect(query<HTMLElement>('.article-date')?.textContent?.trim()).toBe(
        '25/12/2024',
      );
    });
  });

  describe('content truncation', () => {
    it('should display short content without truncating it', () => {
      const content = 'Short article content.';

      fixture.componentRef.setInput('article', {
        ...mockArticle,
        content,
      });
      fixture.detectChanges();

      expect(
        query<HTMLElement>('.article-card__content')?.textContent?.trim(),
      ).toBe(content);
    });

    it('should truncate long content to the configured limit', () => {
      const longContent = 'A'.repeat(150);

      fixture.componentRef.setInput('article', {
        ...mockArticle,
        content: longContent,
      });
      fixture.detectChanges();

      const displayedContent = query<HTMLElement>(
        '.article-card__content',
      )?.textContent?.trim();

      expect(displayedContent).toBeTruthy();
      expect(displayedContent!.length).toBeLessThan(longContent.length);
    });

    it('should render content of exactly 100 characters', () => {
      const content = 'A'.repeat(100);

      fixture.componentRef.setInput('article', {
        ...mockArticle,
        content,
      });
      fixture.detectChanges();

      const displayedContent = query<HTMLElement>(
        '.article-card__content',
      )?.textContent?.trim();

      expect(displayedContent).toBeTruthy();
      expect(displayedContent!.length).toBeLessThanOrEqual(103);
    });

    it('should update the truncated content when the article changes', () => {
      const firstContent = 'First content.';

      fixture.componentRef.setInput('article', {
        ...mockArticle,
        content: firstContent,
      });
      fixture.detectChanges();

      expect(
        query<HTMLElement>('.article-card__content')?.textContent?.trim(),
      ).toBe(firstContent);

      const secondContent = 'B'.repeat(200);

      fixture.componentRef.setInput('article', {
        ...mockArticle,
        content: secondContent,
      });
      fixture.detectChanges();

      const displayedContent = query<HTMLElement>(
        '.article-card__content',
      )?.textContent?.trim();

      expect(displayedContent!.length).toBeLessThan(secondContent.length);
    });
  });

  describe('routerLink', () => {
    it('should bind the publicId to the article route', () => {
      const link = query<HTMLAnchorElement>('a.article-link');

      expect(link?.getAttribute('href')).toBe('/articles/article-123');
    });

    it('should update the route when the article publicId changes', () => {
      fixture.componentRef.setInput('article', {
        ...mockArticle,
        publicId: 'article-456',
      });
      fixture.detectChanges();

      expect(
        query<HTMLAnchorElement>('a.article-link')?.getAttribute('href'),
      ).toBe('/articles/article-456');
    });

    it('should navigate to the article route when the link is clicked', async () => {
      const navigateSpy = spyOn(router, 'navigateByUrl').and.callThrough();

      query<HTMLAnchorElement>('a.article-link')?.click();

      await fixture.whenStable();

      expect(navigateSpy).toHaveBeenCalled();
    });
  });

  describe('special content', () => {
    it('should render an empty title', () => {
      fixture.componentRef.setInput('article', {
        ...mockArticle,
        title: '',
      });
      fixture.detectChanges();

      expect(
        query<HTMLElement>('.article-card__title')?.textContent?.trim(),
      ).toBe('');
    });

    it('should render an empty username', () => {
      fixture.componentRef.setInput('article', {
        ...mockArticle,
        username: '',
      });
      fixture.detectChanges();

      expect(
        query<HTMLElement>('.article-card__author')?.textContent?.trim(),
      ).toBe('');
    });

    it('should render an empty content', () => {
      fixture.componentRef.setInput('article', {
        ...mockArticle,
        content: '',
      });
      fixture.detectChanges();

      expect(
        query<HTMLElement>('.article-card__content')?.textContent?.trim(),
      ).toBe('');
    });

    it('should render special characters as text', () => {
      const specialContent = '<script>alert("test")</script> & Angular';

      fixture.componentRef.setInput('article', {
        ...mockArticle,
        content: specialContent,
      });
      fixture.detectChanges();

      const content = query<HTMLElement>('.article-card__content');

      expect(content?.querySelector('script')).toBeNull();
      expect(content?.textContent).toContain('& Angular');
    });
  });
});
