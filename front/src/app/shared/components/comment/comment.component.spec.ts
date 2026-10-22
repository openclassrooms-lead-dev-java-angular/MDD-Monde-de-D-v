import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommentComponent } from './comment.component';
import { ArticleComment } from '@model/article-comment.model';

describe('CommentComponent', () => {
  let component: CommentComponent;
  let fixture: ComponentFixture<CommentComponent>;

  const mockComment: ArticleComment = {
    username: 'john_doe',
    content: 'Mon commentaire de test',
  } as ArticleComment;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommentComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CommentComponent);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('comment', mockComment);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the comment username', () => {
    const usernameElement: HTMLElement =
      fixture.nativeElement.querySelector('.comment__username');

    expect(usernameElement).toBeTruthy();
    expect(usernameElement.textContent?.trim()).toBe('john_doe');
  });

  it('should display the comment content', () => {
    const contentElement: HTMLElement =
      fixture.nativeElement.querySelector('.comment__content');

    expect(contentElement).toBeTruthy();
    expect(contentElement.textContent?.trim()).toBe(
      'Mon commentaire de test',
    );
  });

  it('should update the displayed comment when the input changes', () => {
    const updatedComment: ArticleComment = {
      username: 'jane_doe',
      content: 'Un nouveau commentaire',
    } as ArticleComment;

    fixture.componentRef.setInput('comment', updatedComment);
    fixture.detectChanges();

    const usernameElement: HTMLElement =
      fixture.nativeElement.querySelector('.comment__username');

    const contentElement: HTMLElement =
      fixture.nativeElement.querySelector('.comment__content');

    expect(usernameElement.textContent?.trim()).toBe('jane_doe');
    expect(contentElement.textContent?.trim()).toBe(
      'Un nouveau commentaire',
    );
  });
})
