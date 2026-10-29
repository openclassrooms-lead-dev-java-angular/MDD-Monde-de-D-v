
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MatSelectHarness } from '@angular/material/select/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';

import { ArticleFormComponent } from './article-form.component';
import { ArticleService } from '@service/article.service';
import { TopicService } from '@service/topic.service';
import { Article, ArticleNew } from '@model/article.model';
import { Topic, TopicOption } from '@model/topic.model';

describe('ArticleFormComponent', () => {
  let component: ArticleFormComponent;
  let fixture: ComponentFixture<ArticleFormComponent>;
  let articleServiceSpy: jasmine.SpyObj<ArticleService>;
  let topicServiceSpy: jasmine.SpyObj<TopicService>;
  let navigateSpy: jasmine.Spy;

  const mockTopics: TopicOption[] = [
    { publicId: 'topic-1', name: 'JavaScript' },
    { publicId: 'topic-2', name: 'Angular' },
  ];

  const mockTopic: Topic = {
    publicId: '1235-qsfd-12345',
    name: 'Angular',
    description: 'Learn Angular development.',
    subscribed: false,
    createdAt: new Date('2026-01-01T10:00:00Z'),
    updatedAt: new Date('2026-01-01T10:00:00Z'),
  };

  const mockArticle: Article = {
    publicId: 'article-123',
    title: 'Test article',
    content: 'Article content',
    username: 'testuser',
    topic: mockTopic,
    createdAt: new Date('2026-01-01T10:00:00Z'),
    updatedAt: new Date('2026-01-01T10:00:00Z'),
  };

  const validArticle: ArticleNew = {
    topicPublicId: 'topic-1',
    title: 'Test article',
    content: 'Test content',
  };

  beforeEach(async () => {
    articleServiceSpy = jasmine.createSpyObj<ArticleService>(
      'ArticleService',
      ['postArticle'],
    );

    topicServiceSpy = jasmine.createSpyObj<TopicService>(
      'TopicService',
      ['fetchTopicOptions'],
    );

    topicServiceSpy.fetchTopicOptions.and.returnValue(of(mockTopics));
    articleServiceSpy.postArticle.and.returnValue(of(mockArticle));

    await TestBed.configureTestingModule({
      imports: [ArticleFormComponent],
      providers: [
        provideRouter([]),
        { provide: ArticleService, useValue: articleServiceSpy },
        { provide: TopicService, useValue: topicServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ArticleFormComponent);
    component = fixture.componentInstance;

    navigateSpy = spyOn(TestBed.inject(Router), 'navigate')
      .and.returnValue(Promise.resolve(true));
  });

  function setValidForm(): void {
    component['articleForm'].setValue(validArticle);
    fixture.detectChanges();
  }

  function submitForm(): void {
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;

    form.dispatchEvent(
      new Event('submit', { bubbles: true, cancelable: true }),
    );

    fixture.detectChanges();
  }

  function initializeComponent(): void {
    fixture.detectChanges();
    expect(component.isLoading()).toBeFalse();
  }

  describe('initialization', () => {
    it('should create', () => {
      initializeComponent();
      expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
      expect(component.topics()).toEqual([]);
      expect(component.isLoading()).toBeFalse();
      expect(component.errorMessage()).toBe('');
      expect(component.submitting()).toBeFalse();
    });

    it('should load topic options on init', () => {
      initializeComponent();

      expect(topicServiceSpy.fetchTopicOptions).toHaveBeenCalledTimes(1);
      expect(component.topics()).toEqual(mockTopics);
    });

    it('should stop loading after topics are fetched', () => {
      initializeComponent();
      expect(component.isLoading()).toBeFalse();
    });

    it('should stop loading when fetching topics fails', () => {
      const consoleErrorSpy = spyOn(console, 'error');

      topicServiceSpy.fetchTopicOptions.and.returnValue(
        throwError(() => new Error('Topic loading failed')),
      );

      fixture.detectChanges();

      expect(component.isLoading()).toBeFalse();
      expect(component.topics()).toEqual([]);
      expect(consoleErrorSpy).toHaveBeenCalled();
    });

    it('should display the loaded topics', async () => {
      initializeComponent();

      const loader = TestbedHarnessEnvironment.loader(fixture);
      const select = await loader.getHarness(MatSelectHarness);

      await select.open();

      const options = await select.getOptions();
      const texts = await Promise.all(
        options.map((option) => option.getText()),
      );

      expect(texts).toContain('JavaScript');
      expect(texts).toContain('Angular');
    });

    it('should render the title and back link', () => {
      initializeComponent();

      const element = fixture.nativeElement as HTMLElement;

      expect(element.querySelector('h1')?.textContent).toContain(
        'Créer un nouvel article',
      );

      const backLink = element.querySelector(
        'a[aria-label="Retour aux articles"]',
      );

      expect(backLink).toBeTruthy();
      expect(backLink?.getAttribute('routerLink')).toBe('/feed');
    });
  });

  describe('form validation', () => {
    beforeEach(() => initializeComponent());

    it('should have an invalid form initially', () => {
      expect(component['articleForm'].invalid).toBeTrue();
    });

    it('should require a topic', () => {
      expect(
        component['articleForm'].controls.topicPublicId.hasError('required'),
      ).toBeTrue();
    });

    it('should require a title', () => {
      expect(
        component['articleForm'].controls.title.hasError('required'),
      ).toBeTrue();
    });

    it('should require content', () => {
      expect(
        component['articleForm'].controls.content.hasError('required'),
      ).toBeTrue();
    });

    it('should reject a title longer than 150 characters', () => {
      const control = component['articleForm'].controls.title;
      control.setValue('a'.repeat(151));

      expect(control.hasError('maxlength')).toBeTrue();
    });

    it('should accept a title of exactly 150 characters', () => {
      const control = component['articleForm'].controls.title;
      control.setValue('a'.repeat(150));

      expect(control.valid).toBeTrue();
    });

    it('should reject content longer than 10000 characters', () => {
      const control = component['articleForm'].controls.content;
      control.setValue('a'.repeat(10001));

      expect(control.hasError('maxlength')).toBeTrue();
    });

    it('should accept content of exactly 10000 characters', () => {
      const control = component['articleForm'].controls.content;
      control.setValue('a'.repeat(10000));

      expect(control.valid).toBeTrue();
    });

    it('should be valid when all fields are valid', () => {
      setValidForm();
      expect(component['articleForm'].valid).toBeTrue();
    });
  });

  describe('validation messages', () => {
    beforeEach(() => initializeComponent());

    it('should not show title errors when untouched and pristine', () => {
      expect(component['titleErrors']).toEqual([]);
    });

    it('should show the required title error when touched', () => {
      component['articleForm'].controls.title.markAsTouched();

      expect(component['titleErrors']).toContain('Le titre est obligatoire.');
    });

    it('should show the required content error when touched', () => {
      component['articleForm'].controls.content.markAsTouched();

      expect(component['contentErrors']).toContain(
        'Le contenu est obligatoire.',
      );
    });

    it('should show the topic required error when touched', () => {
      component['articleForm'].controls.topicPublicId.markAsTouched();

      expect(component['topicErrors']).toContain(
        'Veuillez sélectionner un thème.',
      );
    });

    it('should not show the topic error before the control is touched', () => {
      expect(component['topicErrors']).toEqual([]);
    });

    it('should show the title maxlength error', () => {
      const control = component['articleForm'].controls.title;
      control.setValue('a'.repeat(151));
      control.markAsTouched();
      fixture.detectChanges();

      expect(component['titleErrors']).toContain(
        'Le titre ne doit pas dépasser 150 caractères.',
      );
    });

    it('should show the content maxlength error', () => {
      const control = component['articleForm'].controls.content;
      control.setValue('a'.repeat(10001));
      control.markAsTouched();
      fixture.detectChanges();

      expect(component['contentErrors']).toContain(
        'Le contenu ne doit pas dépasser 10 000 caractères.',
      );
    });
  });

  describe('submitForm', () => {
    beforeEach(() => initializeComponent());

    it('should not submit when the form is invalid', () => {
      submitForm();

      expect(articleServiceSpy.postArticle).not.toHaveBeenCalled();
      expect(component['articleForm'].controls.title.touched).toBeTrue();
      expect(component['articleForm'].controls.content.touched).toBeTrue();
      expect(
        component['articleForm'].controls.topicPublicId.touched,
      ).toBeTrue();
    });

    it('should not submit while submitting is true', () => {
      fixture.componentRef.setInput('submitting', true);
      setValidForm();

      submitForm();

      expect(articleServiceSpy.postArticle).not.toHaveBeenCalled();
    });

    it('should not submit while loading', () => {
      setValidForm();
      component.isLoading.set(true);

      submitForm();

      expect(articleServiceSpy.postArticle).not.toHaveBeenCalled();
    });

    it('should clear the previous error before submitting', () => {
      component.errorMessage.set('Previous error');

      submitForm();

      expect(component.errorMessage()).toBe('');
    });

    it('should send the form data to ArticleService', () => {
      setValidForm();
      submitForm();

      expect(articleServiceSpy.postArticle).toHaveBeenCalledOnceWith(
        validArticle,
      );
    });

    it('should navigate to the created article', () => {
      setValidForm();
      submitForm();

      expect(articleServiceSpy.postArticle).toHaveBeenCalledOnceWith(
        validArticle,
      );

      expect(navigateSpy).toHaveBeenCalledOnceWith([
        '/articles',
        mockArticle.publicId,
      ]);
    });

    it('should stop loading after successful creation', () => {
      setValidForm();
      submitForm();

      expect(component.isLoading()).toBeFalse();
    });

    it('should display the server error message when creation fails', () => {
      const consoleErrorSpy = spyOn(console, 'error');

      articleServiceSpy.postArticle.and.returnValue(
        throwError(
          () =>
            new HttpErrorResponse({
              error: { message: 'Article creation failed' },
              status: 400,
            }),
        ),
      );

      setValidForm();
      submitForm();

      expect(component.errorMessage()).toBe('Article creation failed');
      expect(navigateSpy).not.toHaveBeenCalled();
      expect(component.isLoading()).toBeFalse();
      expect(consoleErrorSpy).toHaveBeenCalled();
    });

    it('should display a fallback error when the server message is missing', () => {
      spyOn(console, 'error');

      articleServiceSpy.postArticle.and.returnValue(
        throwError(
          () =>
            new HttpErrorResponse({
              error: {},
              status: 500,
            }),
        ),
      );

      setValidForm();
      submitForm();

      expect(component.errorMessage()).toBe(
        'Une erreur est survenue lors de la création de l’article.',
      );
    });

    it('should not navigate when creation fails', () => {
      spyOn(console, 'error');

      articleServiceSpy.postArticle.and.returnValue(
        throwError(() => new Error('Unexpected error')),
      );

      setValidForm();
      submitForm();

      expect(navigateSpy).not.toHaveBeenCalled();
    });
  });

  describe('template integration', () => {
    beforeEach(() => initializeComponent());

    it('should render title and content fields', () => {
      const element = fixture.nativeElement as HTMLElement;

      expect(element.querySelector('#article-title')).toBeTruthy();
      expect(element.querySelector('#article-content')).toBeTruthy();
    });

    it('should update the title control when typing', () => {
      const input = fixture.nativeElement.querySelector(
        '#article-title',
      ) as HTMLInputElement;

      input.value = 'A new article';
      input.dispatchEvent(new Event('input'));
      fixture.detectChanges();

      expect(component['articleForm'].controls.title.value).toBe(
        'A new article',
      );
    });

    it('should update the content control when typing', () => {
      const textarea = fixture.nativeElement.querySelector(
        '#article-content',
      ) as HTMLTextAreaElement;

      textarea.value = 'Article body';
      textarea.dispatchEvent(new Event('input'));
      fixture.detectChanges();

      expect(component['articleForm'].controls.content.value).toBe(
        'Article body',
      );
    });

    it('should display the error message', () => {
      component.errorMessage.set('Unable to create article');
      fixture.detectChanges();

      const alert = fixture.nativeElement.querySelector(
        '[role="alert"]',
      ) as HTMLElement;

      expect(alert).toBeTruthy();
      expect(alert.textContent).toContain('Unable to create article');
    });

    it('should hide the error message when empty', () => {
      component.errorMessage.set('');
      fixture.detectChanges();

      expect(
        fixture.nativeElement.querySelector('[role="alert"]'),
      ).toBeNull();
    });
  });
});
