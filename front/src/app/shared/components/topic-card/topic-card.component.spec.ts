import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TopicCardComponent } from './topic-card.component';
import { Topic } from '@model/topic.model';

describe('TopicCardComponent', () => {
  let component: TopicCardComponent;
  let fixture: ComponentFixture<TopicCardComponent>;

  const mockTopic: Topic = {
    slug: 'angular',
    name: 'Angular',
    description: 'Discussions autour d’Angular.',
    subscribed: false,
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-02T00:00:00Z'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopicCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TopicCardComponent);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('topic', mockTopic);
    fixture.detectChanges();
  });

  function getButton(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('.topic-card__button');
  }

  describe('initialization', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize showUnsubscribe to false', () => {
      expect(component.showUnsubscribe()).toBeFalse();
    });

    it('should receive the required topic input', () => {
      expect(component.topic()).toEqual(mockTopic);
    });
  });

  describe('template rendering', () => {
    it('should render the topic card', () => {
      const card = fixture.nativeElement.querySelector('.topic-card');

      expect(card).not.toBeNull();
    });

    it('should display the topic name', () => {
      const title: HTMLElement =
        fixture.nativeElement.querySelector('.topic-card__title');

      expect(title).not.toBeNull();
      expect(title.textContent?.trim()).toBe(mockTopic.name);
    });

    it('should display the topic description', () => {
      const description: HTMLElement = fixture.nativeElement.querySelector(
        '.topic-card__description',
      );

      expect(description).not.toBeNull();
      expect(description.textContent?.trim()).toBe(mockTopic.description);
    });

    it('should render the subscription button', () => {
      expect(getButton()).not.toBeNull();
      expect(getButton().type).toBe('button');
    });
  });

  describe('subscription state', () => {
    it('should display "S’abonner" when the topic is not subscribed', () => {
      expect(getButton().textContent?.trim()).toBe('S’abonner');
    });

    it('should display "Déjà abonné" when the topic is subscribed', () => {
      fixture.componentRef.setInput('topic', {
        ...mockTopic,
        subscribed: true,
      });

      fixture.detectChanges();

      expect(getButton().textContent?.trim()).toBe('Déjà abonné');
    });

    it('should not apply the subscribed class when the topic is not subscribed', () => {
      expect(
        getButton().classList.contains('topic-card__button--subscribed'),
      ).toBeFalse();
    });

    it('should apply the subscribed class when the topic is subscribed', () => {
      fixture.componentRef.setInput('topic', {
        ...mockTopic,
        subscribed: true,
      });

      fixture.detectChanges();

      expect(
        getButton().classList.contains('topic-card__button--subscribed'),
      ).toBeTrue();
    });
  });

  describe('showUnsubscribe input', () => {
    it('should display "S’abonner" when showUnsubscribe is false and the topic is not subscribed', () => {
      fixture.componentRef.setInput('showUnsubscribe', false);
      fixture.componentRef.setInput('topic', {
        ...mockTopic,
        subscribed: false,
      });

      fixture.detectChanges();

      expect(getButton().textContent?.trim()).toBe('S’abonner');
    });

    it('should display "Déjà abonné" when showUnsubscribe is false and the topic is subscribed', () => {
      fixture.componentRef.setInput('showUnsubscribe', false);
      fixture.componentRef.setInput('topic', {
        ...mockTopic,
        subscribed: true,
      });

      fixture.detectChanges();

      expect(getButton().textContent?.trim()).toBe('Déjà abonné');
    });

    it('should display "Se désabonner" when showUnsubscribe is true', () => {
      fixture.componentRef.setInput('showUnsubscribe', true);
      fixture.detectChanges();

      expect(getButton().textContent?.trim()).toBe('Se désabonner');
    });

    it('should update the button when showUnsubscribe changes', () => {
      fixture.componentRef.setInput('showUnsubscribe', true);
      fixture.detectChanges();

      expect(getButton().textContent?.trim()).toBe('Se désabonner');

      fixture.componentRef.setInput('showUnsubscribe', false);
      fixture.detectChanges();

      expect(getButton().textContent?.trim()).toBe('S’abonner');
    });

    it('should preserve the subscribed CSS class when showUnsubscribe is true', () => {
      fixture.componentRef.setInput('topic', {
        ...mockTopic,
        subscribed: true,
      });
      fixture.componentRef.setInput('showUnsubscribe', true);

      fixture.detectChanges();

      expect(
        getButton().classList.contains('topic-card__button--subscribed'),
      ).toBeTrue();
    });
  });

  describe('subscribe output', () => {
    it('should emit the topic when the subscription button is clicked', () => {
      const emitSpy = spyOn(component.subscribe, 'emit');

      getButton().click();

      expect(emitSpy).toHaveBeenCalledOnceWith(mockTopic);
    });

    it('should emit the updated topic when the topic input changes', () => {
      const updatedTopic: Topic = {
        ...mockTopic,
        name: 'Angular avancé',
        subscribed: true,
      };

      fixture.componentRef.setInput('topic', updatedTopic);
      fixture.detectChanges();

      const emitSpy = spyOn(component.subscribe, 'emit');

      getButton().click();

      expect(emitSpy).toHaveBeenCalledOnceWith(updatedTopic);
    });

    it('should emit the topic when showUnsubscribe is true', () => {
      fixture.componentRef.setInput('showUnsubscribe', true);
      fixture.detectChanges();

      const emitSpy = spyOn(component.subscribe, 'emit');

      getButton().click();

      expect(emitSpy).toHaveBeenCalledOnceWith(mockTopic);
    });

    it('should emit only once per click', () => {
      const emitSpy = spyOn(component.subscribe, 'emit');

      getButton().click();

      expect(emitSpy).toHaveBeenCalledTimes(1);
    });
  });

  describe('unsubscribe output', () => {
    it('should expose the unsubscribe output', () => {
      expect(component.unsubscribe).toBeDefined();
    });

    it('should not emit unsubscribe when the button is clicked', () => {
      const emitSpy = spyOn(component.unsubscribe, 'emit');

      fixture.componentRef.setInput('showUnsubscribe', true);
      fixture.detectChanges();

      getButton().click();

      expect(emitSpy).not.toHaveBeenCalled();
    });
  });

  describe('input updates', () => {
    it('should update the displayed name when the topic changes', () => {
      const updatedTopic: Topic = {
        ...mockTopic,
        name: 'TypeScript',
      };

      fixture.componentRef.setInput('topic', updatedTopic);
      fixture.detectChanges();

      const title: HTMLElement =
        fixture.nativeElement.querySelector('.topic-card__title');

      expect(title.textContent?.trim()).toBe('TypeScript');
    });

    it('should update the displayed description when the topic changes', () => {
      const updatedTopic: Topic = {
        ...mockTopic,
        description: 'Un langage pour JavaScript.',
      };

      fixture.componentRef.setInput('topic', updatedTopic);
      fixture.detectChanges();

      const description: HTMLElement = fixture.nativeElement.querySelector(
        '.topic-card__description',
      );

      expect(description.textContent?.trim()).toBe(
        'Un langage pour JavaScript.',
      );
    });

    it('should update the button state when subscribed changes', () => {
      fixture.componentRef.setInput('topic', {
        ...mockTopic,
        subscribed: false,
      });
      fixture.detectChanges();

      expect(getButton().textContent?.trim()).toBe('S’abonner');

      fixture.componentRef.setInput('topic', {
        ...mockTopic,
        subscribed: true,
      });
      fixture.detectChanges();

      expect(getButton().textContent?.trim()).toBe('Déjà abonné');
    });
  });
});
