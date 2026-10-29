import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommentComponent } from './comment.component';
import { ArticleComment } from '@model/article-comment.model';

describe('CommentComponent', () => {
  let component: CommentComponent;
  let fixture: ComponentFixture<CommentComponent>;

  const mockComment: ArticleComment = {
    username: 'John_Doe',
    content: 'This is a test comment.',
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

  describe('comment input', () => {
    it('should display the comment username', () => {
      const username: HTMLElement =
        fixture.nativeElement.querySelector('.comment__username');

      expect(username).not.toBeNull();
      expect(username.textContent?.trim()).toBe('John_Doe');
    });

    it('should display the comment content', () => {
      const content: HTMLElement =
        fixture.nativeElement.querySelector('.comment__content');

      expect(content).not.toBeNull();
      expect(content.textContent?.trim()).toBe('This is a test comment.');
    });

    it('should update the username when the comment input changes', () => {
      const updatedComment: ArticleComment = {
        ...mockComment,
        username: 'Jane_Doe',
      };

      fixture.componentRef.setInput('comment', updatedComment);
      fixture.detectChanges();

      const username: HTMLElement =
        fixture.nativeElement.querySelector('.comment__username');

      expect(username.textContent?.trim()).toBe('Jane_Doe');
    });

    it('should update the content when the comment input changes', () => {
      const updatedComment: ArticleComment = {
        ...mockComment,
        content: 'Updated comment content.',
      };

      fixture.componentRef.setInput('comment', updatedComment);
      fixture.detectChanges();

      const content: HTMLElement =
        fixture.nativeElement.querySelector('.comment__content');

      expect(content.textContent?.trim()).toBe('Updated comment content.');
    });

    it('should update both username and content when the comment changes', () => {
      const updatedComment: ArticleComment = {
        ...mockComment,
        username: 'Jane_Doe',
        content: 'A completely different comment.',
      };

      fixture.componentRef.setInput('comment', updatedComment);
      fixture.detectChanges();

      const username: HTMLElement =
        fixture.nativeElement.querySelector('.comment__username');

      const content: HTMLElement =
        fixture.nativeElement.querySelector('.comment__content');

      expect(username.textContent?.trim()).toBe('Jane_Doe');
      expect(content.textContent?.trim()).toBe(
        'A completely different comment.',
      );
    });
  });

  describe('template structure', () => {
    it('should render the comment container', () => {
      const container: HTMLElement =
        fixture.nativeElement.querySelector('.comment');

      expect(container).not.toBeNull();
    });

    it('should render the username and content inside the comment container', () => {
      const container: HTMLElement =
        fixture.nativeElement.querySelector('.comment');

      const username = container.querySelector('.comment__username');
      const content = container.querySelector('.comment__content');

      expect(username).not.toBeNull();
      expect(content).not.toBeNull();
    });

    it('should render exactly one username element', () => {
      const usernames: NodeListOf<HTMLElement> =
        fixture.nativeElement.querySelectorAll('.comment__username');

      expect(usernames.length).toBe(1);
    });

    it('should render exactly one content element', () => {
      const contents: NodeListOf<HTMLElement> =
        fixture.nativeElement.querySelectorAll('.comment__content');

      expect(contents.length).toBe(1);
    });
  });

  describe('special content', () => {
    it('should render an empty username', () => {
      fixture.componentRef.setInput('comment', {
        ...mockComment,
        username: '',
      });
      fixture.detectChanges();

      const username: HTMLElement =
        fixture.nativeElement.querySelector('.comment__username');

      expect(username.textContent?.trim()).toBe('');
    });

    it('should render an empty comment content', () => {
      fixture.componentRef.setInput('comment', {
        ...mockComment,
        content: '',
      });
      fixture.detectChanges();

      const content: HTMLElement =
        fixture.nativeElement.querySelector('.comment__content');

      expect(content.textContent?.trim()).toBe('');
    });

    it('should render special characters in the comment content', () => {
      const specialContent = 'Hello <Angular> & "Jasmine"!';

      fixture.componentRef.setInput('comment', {
        ...mockComment,
        content: specialContent,
      });
      fixture.detectChanges();

      const content: HTMLElement =
        fixture.nativeElement.querySelector('.comment__content');

      expect(content.textContent?.trim()).toBe(specialContent);
    });

    it('should render multiline comment content', () => {
      const multilineContent = 'First line\nSecond line';

      fixture.componentRef.setInput('comment', {
        ...mockComment,
        content: multilineContent,
      });
      fixture.detectChanges();

      const content: HTMLElement =
        fixture.nativeElement.querySelector('.comment__content');

      const actualContent = content.textContent?.replace(/\s+/g, ' ').trim();

      expect(actualContent).toBe('First line Second line');
    });
  });
});
