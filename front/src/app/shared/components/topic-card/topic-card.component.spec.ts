import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TopicCardComponent } from './topic-card.component';
import { Topic } from '@model/topic.model';

describe('TopicCardComponent', () => {
  let component: TopicCardComponent;
  let fixture: ComponentFixture<TopicCardComponent>;

  const mockTopic = {
    publicId: '1235-qsfd-12345',
    name: 'Angular',
    description: 'Learn Angular development.',
    subscribed: false,
  } as Topic;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopicCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TopicCardComponent);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('topic', { ...mockTopic });

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('topic input', () => {
    it('should display the topic name', () => {
      const title: HTMLElement =
        fixture.nativeElement.querySelector('.topic-card__title');

      expect(title.textContent?.trim()).toBe('Angular');
    });

    it('should display the topic description', () => {
      const description: HTMLElement = fixture.nativeElement.querySelector(
        '.topic-card__description',
      );

      expect(description.textContent?.trim()).toBe(
        'Learn Angular development.',
      );
    });

    it('should update the displayed content when the topic changes', () => {
      const updatedTopic: Topic = {
        ...mockTopic,
        name: 'TypeScript',
        description: 'Learn TypeScript development.',
      };

      fixture.componentRef.setInput('topic', updatedTopic);
      fixture.detectChanges();

      const title: HTMLElement =
        fixture.nativeElement.querySelector('.topic-card__title');

      const description: HTMLElement = fixture.nativeElement.querySelector(
        '.topic-card__description',
      );

      expect(title.textContent?.trim()).toBe('TypeScript');
      expect(description.textContent?.trim()).toBe(
        'Learn TypeScript development.',
      );
    });
  });

  describe('subscribe mode', () => {
    it('should display the subscribe button when showUnsubscribe is false', () => {
      fixture.componentRef.setInput('showUnsubscribe', false);
      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.topic-card__button',
      );

      expect(button).toBeTruthy();
      expect(button.textContent?.trim()).toBe('S’abonner');
    });

    it('should emit the topic when the subscribe button is clicked', () => {
      const subscribeSpy = spyOn(component.subscribe, 'emit');

      fixture.componentRef.setInput('showUnsubscribe', false);
      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.topic-card__button',
      );

      button.click();

      expect(subscribeSpy).toHaveBeenCalledOnceWith(component.topic());
    });

    it('should display "Déjà abonné" when the topic is subscribed', () => {
      fixture.componentRef.setInput('topic', {
        ...mockTopic,
        subscribed: true,
      });
      fixture.componentRef.setInput('showUnsubscribe', false);
      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.topic-card__button',
      );

      expect(button.textContent?.trim()).toBe('Déjà abonné');
    });

    it('should add the subscribed CSS class when the topic is subscribed', () => {
      fixture.componentRef.setInput('topic', {
        ...mockTopic,
        subscribed: true,
      });
      fixture.componentRef.setInput('showUnsubscribe', false);
      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.topic-card__button',
      );

      expect(button.classList).toContain('topic-card__button--subscribed');
    });

    it('should not add the subscribed CSS class when the topic is not subscribed', () => {
      fixture.componentRef.setInput('topic', {
        ...mockTopic,
        subscribed: false,
      });
      fixture.componentRef.setInput('showUnsubscribe', false);
      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.topic-card__button',
      );

      expect(button.classList).not.toContain('topic-card__button--subscribed');
    });
  });

  describe('unsubscribe mode', () => {
    beforeEach(() => {
      fixture.componentRef.setInput('showUnsubscribe', true);
      fixture.detectChanges();
    });

    it('should display the unsubscribe button', () => {
      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.topic-card__button',
      );

      expect(button).toBeTruthy();
      expect(button.textContent?.trim()).toBe('Se désabonner');
    });

    it('should emit the topic when the unsubscribe button is clicked', () => {
      const unsubscribeSpy = spyOn(component.unsubscribe, 'emit');

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.topic-card__button',
      );

      button.click();

      expect(unsubscribeSpy).toHaveBeenCalledOnceWith(component.topic());
    });

    it('should not display the subscribe button text', () => {
      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.topic-card__button',
      );

      expect(button.textContent?.trim()).not.toBe('S’abonner');
      expect(button.textContent?.trim()).not.toBe('Déjà abonné');
    });

    it('should add the subscribed CSS class when the topic is subscribed', () => {
      fixture.componentRef.setInput('topic', {
        ...mockTopic,
        subscribed: true,
      });
      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.topic-card__button',
      );

      expect(button.classList).toContain('topic-card__button--subscribed');
    });

    it('should not add the subscribed CSS class when the topic is not subscribed', () => {
      fixture.componentRef.setInput('topic', {
        ...mockTopic,
        subscribed: false,
      });
      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.topic-card__button',
      );

      expect(button.classList).not.toContain('topic-card__button--subscribed');
    });
  });

  describe('outputs', () => {
    it('should not emit subscribe when the unsubscribe button is clicked', () => {
      const subscribeSpy = spyOn(component.subscribe, 'emit');

      fixture.componentRef.setInput('showUnsubscribe', true);
      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.topic-card__button',
      );

      button.click();

      expect(subscribeSpy).not.toHaveBeenCalled();
    });

    it('should not emit unsubscribe when the subscribe button is clicked', () => {
      const unsubscribeSpy = spyOn(component.unsubscribe, 'emit');

      fixture.componentRef.setInput('showUnsubscribe', false);
      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector(
        '.topic-card__button',
      );

      button.click();

      expect(unsubscribeSpy).not.toHaveBeenCalled();
    });
  });
});
