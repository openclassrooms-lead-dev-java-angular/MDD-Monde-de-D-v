import { DatePipe, Location } from '@angular/common';
import { Component, input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, convertToParamMap, RouterLink } from '@angular/router';
import { By } from '@angular/platform-browser';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Observable, of, throwError } from 'rxjs';

import { ArticleDetailComponent } from './article-detail.component';
import { Article } from '@model/article.model';
import { ArticleComment } from '@model/article-comment.model';
import { ArticleService } from '@service/article.service';
import { CommentService } from '@service/comment.service';
import { CommentComponent } from '@shared/components/comment/comment.component';

// Mock child component with the same required input contract.
@Component({
  selector: 'app-comment',
  standalone: true,
  template: `
    <div class="comment">
      <div class="comment__username">{{ comment().username }}</div>
      <div class="comment__content">{{ comment().content }}</div>
    </div>
  `,
})
class MockCommentComponent {
  readonly comment = input.required<ArticleComment>();
}

describe('ArticleDetailComponent', () => {
  let fixture: ComponentFixture<ArticleDetailComponent>;
  let component: ArticleDetailComponent;
  let articleServiceSpy: jasmine.SpyObj<ArticleService>;
  let commentServiceSpy: jasmine.SpyObj<CommentService>;
  let locationSpy: jasmine.SpyObj<Location>;

  const mockComment = {
    publicId: 'comment-1',
    content: 'This is a test comment',
    username: 'test-user',
    createdAt: new Date('2026-01-15T10:00:00Z'),
  } as ArticleComment;

  const mockArticle = {
    publicId: 'article-1',
    title: 'Test article',
    content: 'This is the article content',
    username: 'article-author',
    createdAt: new Date('2026-01-10T10:00:00Z'),
    topic: {
      publicId: 'topic-1',
      name: 'Angular',
    },
    comments: [mockComment],
  } as unknown as Article;

  beforeEach(async () => {
    articleServiceSpy = jasmine.createSpyObj<ArticleService>('ArticleService', [
      'fetchArticleByPublicId',
    ]);

    commentServiceSpy = jasmine.createSpyObj<CommentService>('CommentService', [
      'postComment',
    ]);

    locationSpy = jasmine.createSpyObj<Location>('Location', ['back']);

    articleServiceSpy.fetchArticleByPublicId.and.returnValue(of(mockArticle));

    commentServiceSpy.postComment.and.returnValue(of(mockComment));

    await TestBed.configureTestingModule({
      imports: [
        ArticleDetailComponent,
        MockCommentComponent,
        ReactiveFormsModule,
        MatFormFieldModule,
        RouterLink,
        DatePipe,
      ],
      providers: [
        {
          provide: ArticleService,
          useValue: articleServiceSpy,
        },
        {
          provide: CommentService,
          useValue: commentServiceSpy,
        },
        {
          provide: Location,
          useValue: locationSpy,
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({
                publicId: 'article-1',
                slug: 'article-1',
              }),
            },
          },
        },
      ],
    })
      .overrideComponent(ArticleDetailComponent, {
        remove: {
          imports: [CommentComponent],
        },
        add: {
          imports: [MockCommentComponent, MatFormFieldModule, RouterLink],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ArticleDetailComponent);
    component = fixture.componentInstance;
  });

  // Component creation

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  // Article loading

  it('should load the article using the route parameter', () => {
    fixture.detectChanges();

    expect(articleServiceSpy.fetchArticleByPublicId).toHaveBeenCalledWith(
      'article-1',
    );
  });

  it('should display the article title and content', () => {
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain('Test article');
    expect(text).toContain('This is the article content');
    expect(text).toContain('article-author');
  });

  it('should display the article topic', () => {
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain('Angular');
  });

  it('should handle an article loading error', () => {
    articleServiceSpy.fetchArticleByPublicId.and.returnValue(
      throwError(() => new Error('Unable to load article')),
    );

    fixture.detectChanges();

    expect(articleServiceSpy.fetchArticleByPublicId).toHaveBeenCalled();
  });

  it('should support a request that is still pending', () => {
    articleServiceSpy.fetchArticleByPublicId.and.returnValue(
      new Observable<Article>(() => {
        // Keep the request pending.
      }),
    );

    fixture.detectChanges();

    expect(articleServiceSpy.fetchArticleByPublicId).toHaveBeenCalled();
  });

  // Comments

  it('should render the article comments', () => {
    fixture.detectChanges();

    const comments = fixture.debugElement.queryAll(
      By.directive(MockCommentComponent),
    );

    expect(comments.length).toBe(1);
  });

  it('should pass the required comment input to the child component', () => {
    fixture.detectChanges();

    const commentDebugElement = fixture.debugElement.query(
      By.directive(MockCommentComponent),
    );

    const commentComponent =
      commentDebugElement.componentInstance as MockCommentComponent;

    expect(commentComponent.comment()).toEqual(mockComment);
  });

  it('should handle an article without comments', () => {
    articleServiceSpy.fetchArticleByPublicId.and.returnValue(
      of({
        ...mockArticle,
        comments: [],
      } as Article),
    );

    fixture.detectChanges();

    const comments = fixture.debugElement.queryAll(
      By.directive(MockCommentComponent),
    );

    expect(comments.length).toBe(0);
  });

  // Comment form

  it('should create the comment form', () => {
    fixture.detectChanges();

    expect(component['commentForm']).toBeDefined();
  });

  it('should reject an empty comment', () => {
    fixture.detectChanges();

    const form = component['commentForm'];

    form.controls['comment'].setValue('');

    expect(form.invalid).toBeTrue();
  });

  it('should accept a non-empty comment', () => {
    fixture.detectChanges();

    const form = component['commentForm'];

    form.controls['comment'].setValue('A valid comment');

    expect(form.valid).toBeTrue();
  });

  // Comment submission

  it('should submit a valid comment', () => {
    fixture.detectChanges();

    const form = component['commentForm'];

    form.controls['comment'].setValue('A valid comment');

    component.submitForm();

    expect(commentServiceSpy.postComment).toHaveBeenCalledWith(
      'article-1',
      'A valid comment',
    );
  });

  it('should not submit an invalid comment', () => {
    fixture.detectChanges();

    const form = component['commentForm'];

    form.controls['comment'].setValue('');

    component.submitForm();

    expect(commentServiceSpy.postComment).not.toHaveBeenCalled();
  });

  it('should handle a comment submission error', () => {
    commentServiceSpy.postComment.and.returnValue(
      throwError(() => new Error('Unable to post comment')),
    );

    fixture.detectChanges();

    const form = component['commentForm'];

    form.controls['comment'].setValue('A valid comment');

    component.submitForm();

    expect(commentServiceSpy.postComment).toHaveBeenCalled();
  });

  // Navigation

  it('should navigate back when goBack is called', () => {
    fixture.detectChanges();

    component.goBack();

    expect(locationSpy.back).toHaveBeenCalled();
  });
});
