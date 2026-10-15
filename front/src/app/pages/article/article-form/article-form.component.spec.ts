import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { ArticleFormComponent } from './article-form.component';
import { ArticleService } from '@service/article.service';
import { TopicService } from '@service/topic.service';
import { Router } from '@angular/router';

describe('ArticleFormComponent', () => {
  let fixture: ComponentFixture<ArticleFormComponent>;
  let component: ArticleFormComponent;

  let articleService: jasmine.SpyObj<ArticleService>;
  let topicService: jasmine.SpyObj<TopicService>;
  let router: jasmine.SpyObj<Router>;

  const mockTopics = [
    { slug: 'tech', name: 'Technology' },
    { slug: 'news', name: 'News' },
  ];

  beforeEach(async () => {
    articleService = jasmine.createSpyObj<ArticleService>('ArticleService', [
      'postArticle',
      'availableSlug',
    ]);

    topicService = jasmine.createSpyObj<TopicService>('TopicService', [
      'getTopicList',
    ]);

    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    topicService.getTopicList.and.returnValue(of(mockTopics));
    articleService.availableSlug.and.returnValue(of({ available: true }));

    articleService.postArticle.and.returnValue(
      of({ slug: 'my-article' }) as ReturnType<ArticleService['postArticle']>,
    );

    router.navigate.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      imports: [ArticleFormComponent],
      providers: [
        { provide: ArticleService, useValue: articleService },
        { provide: TopicService, useValue: topicService },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ArticleFormComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  function fillValidForm(): void {
    const form = (component as any).articleForm;

    form.controls.topicSlug.setValue('tech');
    form.controls.title.setValue('My article');
    form.controls.slug.setValue('my-article');
    form.controls.content.setValue('My article content');
  }

  describe('initialization', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should load the topic list', () => {
      expect(topicService.getTopicList).toHaveBeenCalled();
      expect(component.topics()).toEqual(mockTopics);
      expect(component.isLoading()).toBeFalse();
    });

    it('should display the form title', () => {
      const title: HTMLElement = fixture.nativeElement.querySelector(
        '.article-form__title',
      );

      expect(title.textContent).toContain('Create a new article');
    });
  });

  describe('form validation', () => {
    it('should invalidate the form when all fields are empty', () => {
      const form = (component as any).articleForm;

      expect(form.invalid).toBeTrue();
      expect(form.controls.topicSlug.hasError('required')).toBeTrue();
      expect(form.controls.title.hasError('required')).toBeTrue();
      expect(form.controls.slug.hasError('required')).toBeTrue();
      expect(form.controls.content.hasError('required')).toBeTrue();
    });

    it('should display an error when the title exceeds 150 characters', () => {
      const form = (component as any).articleForm;
      const control = form.controls.title;

      control.setValue('a'.repeat(151));
      control.markAsTouched();

      expect(control.hasError('maxlength')).toBeTrue();
      expect((component as any).titleErrors).toContain(
        'The title must not exceed 150 characters.',
      );
    });

    it('should not display title errors before user interaction', () => {
      const form = (component as any).articleForm;

      form.controls.title.setValue('');

      expect((component as any).titleErrors).toEqual([]);
    });

    it('should display an error when the content is empty after interaction', () => {
      const form = (component as any).articleForm;
      const control = form.controls.content;

      control.setValue('');
      control.markAsTouched();

      expect((component as any).contentErrors).toContain(
        'Content is required.',
      );
    });

    it('should display an error when the content exceeds 10,000 characters', () => {
      const form = (component as any).articleForm;
      const control = form.controls.content;

      control.setValue('a'.repeat(10001));
      control.markAsTouched();

      expect(control.hasError('maxlength')).toBeTrue();
      expect((component as any).contentErrors).toContain(
        'The content must not exceed 10,000 characters.',
      );
    });

    it('should reject a slug containing invalid characters', () => {
      const form = (component as any).articleForm;
      const control = form.controls.slug;

      control.setValue('invalid slug !');
      control.markAsTouched();

      expect(control.hasError('pattern')).toBeTrue();
      expect((component as any).slugErrors).toContain(
        'Use only letters, numbers, hyphens, and underscores.',
      );
    });

    it('should reject a slug that is already taken', fakeAsync(() => {
      articleService.availableSlug.and.returnValue(of({ available: false }));

      const form = (component as any).articleForm;
      const control = form.controls.slug;

      control.setValue('taken-slug');
      tick(300);
      control.markAsTouched();

      expect(control.hasError('slugTaken')).toBeTrue();
      expect((component as any).slugErrors).toContain(
        'This URL is already in use.',
      );
    }));

    it('should display an error when the slug availability check fails', fakeAsync(() => {
      spyOn(console, 'error');

      articleService.availableSlug.and.returnValue(
        throwError(() => new Error('Network error')),
      );

      const form = (component as any).articleForm;
      const control = form.controls.slug;

      control.setValue('my-slug');
      tick(300);
      control.markAsTouched();

      expect(control.hasError('slugCheckFailed')).toBeTrue();
      expect((component as any).slugErrors).toContain(
        'Unable to check slug availability.',
      );
    }));
  });

  describe('form submission', () => {
    it('should mark all fields as touched when the form is invalid', () => {
      const form = (component as any).articleForm;

      (component as any).submitForm();

      expect(form.controls.topicSlug.touched).toBeTrue();
      expect(form.controls.title.touched).toBeTrue();
      expect(form.controls.slug.touched).toBeTrue();
      expect(form.controls.content.touched).toBeTrue();

      expect(articleService.postArticle).not.toHaveBeenCalled();
    });

    it('should create an article when the form is valid', fakeAsync(() => {
      fillValidForm();
      tick(300);

      (component as any).submitForm();

      expect(articleService.postArticle).toHaveBeenCalledWith({
        topicSlug: 'tech',
        title: 'My article',
        slug: 'my-article',
        content: 'My article content',
      });

      expect(router.navigate).toHaveBeenCalledWith(['/articles', 'my-article']);

      expect(component.isLoading()).toBeFalse();
    }));

    it('should not create an article when submission is already in progress', fakeAsync(() => {
      fillValidForm();
      tick(300);

      fixture.componentRef.setInput('submitting', true);

      (component as any).submitForm();

      expect(articleService.postArticle).not.toHaveBeenCalled();
    }));

    it('should display the server error message when article creation fails', fakeAsync(() => {
      spyOn(console, 'error');

      articleService.postArticle.and.returnValue(
        throwError(() => ({
          error: { message: 'Unable to create this article.' },
        })),
      );

      fillValidForm();
      tick(300);

      (component as any).submitForm();

      expect(component.errorMessage()).toBe('Unable to create this article.');
      expect(component.isLoading()).toBeFalse();
    }));

    it('should display a generic error when the server provides no message', fakeAsync(() => {
      spyOn(console, 'error');

      articleService.postArticle.and.returnValue(
        throwError(() => new Error('Network error')),
      );

      fillValidForm();
      tick(300);

      (component as any).submitForm();

      expect(component.errorMessage()).toBe(
        'An error occurred while creating the article.',
      );
    }));
  });

  describe('slug autocomplete', () => {
    it('should generate a slug from the title when the slug is empty', () => {
      const form = (component as any).articleForm;

      form.controls.title.setValue('My First Article');
      form.controls.slug.setValue('');

      (component as any).autocompleteSlug();

      expect(form.controls.slug.value).toBe('My-First-Article');
    });

    it('should not replace an existing custom slug', () => {
      const form = (component as any).articleForm;

      form.controls.title.setValue('Another title');
      form.controls.slug.setValue('custom-slug');

      (component as any).autocompleteSlug();

      expect(form.controls.slug.value).toBe('custom-slug');
    });

    it('should remove accents when generating a slug', () => {
      const form = (component as any).articleForm;

      form.controls.title.setValue('Été à Noël');
      form.controls.slug.setValue('');

      (component as any).autocompleteSlug();

      expect(form.controls.slug.value).toBe('Ete-a-Noel');
    });
  });
});
