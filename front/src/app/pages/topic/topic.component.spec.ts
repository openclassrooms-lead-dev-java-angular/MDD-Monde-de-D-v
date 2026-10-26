import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, Subject, throwError } from 'rxjs';

import { TopicComponent } from './topic.component';
import { TopicService } from '@service/topic.service';
import { Topic } from '@model/topic.model';
import { Page } from '@type/page.type';
import { TopicCardComponent } from '@shared/components/topic-card/topic-card.component';

describe('TopicComponent', () => {
  let component: TopicComponent;
  let fixture: ComponentFixture<TopicComponent>;
  let topicService: jasmine.SpyObj<TopicService>;

  const mockTopic: Topic = {
    publicId: 'angular',
    name: 'Angular',
    description: 'Discussions autour d’Angular.',
    subscribed: false,
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-02T00:00:00Z'),
  };

  const secondTopic: Topic = {
    ...mockTopic,
    publicId: 'spring',
    name: 'Spring',
    description: 'Discussions autour de Spring.',
  };

  const createPage = (
    content: Topic[],
    number = 0,
    last = true,
  ): Page<Topic> => ({
    content,
    totalPages: last ? number + 1 : number + 2,
    totalElements: content.length,
    size: 10,
    number,
    first: number === 0,
    last,
    empty: content.length === 0,
    numberOfElements: content.length,
  });

  beforeEach(async () => {
    topicService = jasmine.createSpyObj<TopicService>('TopicService', [
      'fetchTopics',
      'subscribe',
    ]);

    topicService.fetchTopics.and.returnValue(of(createPage([])));
    topicService.subscribe.and.returnValue(of(undefined));

    await TestBed.configureTestingModule({
      imports: [TopicComponent],
      providers: [{ provide: TopicService, useValue: topicService }],
    }).compileComponents();

    fixture = TestBed.createComponent(TopicComponent);
    component = fixture.componentInstance;
  });

  // --------------------------------------------------
  // Initialisation
  // --------------------------------------------------

  describe('initialization', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should load the first page on initialization', () => {
      topicService.fetchTopics.and.returnValue(of(createPage([mockTopic])));

      fixture.detectChanges();

      expect(topicService.fetchTopics).toHaveBeenCalledTimes(1);
      expect(topicService.fetchTopics).toHaveBeenCalledWith(0, 10);
      expect(component['topicList']()).toEqual([mockTopic]);
    });

    it('should start loading when the request is pending', () => {
      const response = new Subject<Page<Topic>>();
      topicService.fetchTopics.and.returnValue(response);

      fixture.detectChanges();

      expect(component['loading']()).toBeTrue();

      response.next(createPage([mockTopic]));
      response.complete();
    });

    it('should stop loading after the initial request succeeds', () => {
      fixture.detectChanges();

      expect(component['loading']()).toBeFalse();
    });

    it('should set hasMore to false when the last page is reached', () => {
      topicService.fetchTopics.and.returnValue(
        of(createPage([mockTopic], 0, true)),
      );

      fixture.detectChanges();

      expect(component['hasMore']()).toBeFalse();
    });

    it('should set hasMore to true when more pages are available', () => {
      topicService.fetchTopics.and.returnValue(
        of(createPage([mockTopic], 0, false)),
      );

      fixture.detectChanges();

      expect(component['hasMore']()).toBeTrue();
    });
  });

  // --------------------------------------------------
  // Pagination
  // --------------------------------------------------

  describe('loadMore', () => {
    it('should load the next page when more topics are available', () => {
      topicService.fetchTopics.and.returnValues(
        of(createPage([mockTopic], 0, false)),
        of(createPage([secondTopic], 1, true)),
      );

      fixture.detectChanges();
      component['loadMore']();

      expect(topicService.fetchTopics).toHaveBeenCalledTimes(2);
      expect(topicService.fetchTopics).toHaveBeenCalledWith(1, 10);
    });

    it('should append topics to the existing list', () => {
      topicService.fetchTopics.and.returnValues(
        of(createPage([mockTopic], 0, false)),
        of(createPage([secondTopic], 1, true)),
      );

      fixture.detectChanges();
      component['loadMore']();

      expect(component['topicList']()).toEqual([mockTopic, secondTopic]);
    });

    it('should not load another page when the last page has been reached', () => {
      topicService.fetchTopics.and.returnValue(
        of(createPage([mockTopic], 0, true)),
      );

      fixture.detectChanges();
      component['loadMore']();

      expect(topicService.fetchTopics).toHaveBeenCalledTimes(1);
    });

    it('should not load another page while a request is in progress', () => {
      const response = new Subject<Page<Topic>>();
      topicService.fetchTopics.and.returnValue(response);

      fixture.detectChanges();
      component['loadMore']();

      expect(topicService.fetchTopics).toHaveBeenCalledTimes(1);

      response.next(createPage([mockTopic], 0, false));
      response.complete();
    });

    it('should update the current page after loading a page', () => {
      topicService.fetchTopics.and.returnValues(
        of(createPage([mockTopic], 0, false)),
        of(createPage([secondTopic], 1, false)),
        of(createPage([], 2, true)),
      );

      fixture.detectChanges();
      component['loadMore']();
      component['loadMore']();

      expect(topicService.fetchTopics).toHaveBeenCalledWith(2, 10);
    });
  });

  // --------------------------------------------------
  // Gestion des erreurs
  // --------------------------------------------------

  describe('error handling', () => {
    it('should stop loading when the request fails', () => {
      spyOn(console, 'error');

      topicService.fetchTopics.and.returnValue(
        throwError(() => new Error('Request failed')),
      );

      fixture.detectChanges();

      expect(component['loading']()).toBeFalse();
      expect(console.error).toHaveBeenCalled();
    });

    it('should log the error when loading topics fails', () => {
      spyOn(console, 'error');

      const error = new Error('Server unavailable');
      topicService.fetchTopics.and.returnValue(throwError(() => error));

      fixture.detectChanges();

      expect(console.error).toHaveBeenCalledWith(error);
    });

    it('should allow retrying after a failed request', () => {
      spyOn(console, 'error');

      topicService.fetchTopics.and.returnValues(
        throwError(() => new Error('Request failed')),
        of(createPage([mockTopic])),
      );

      fixture.detectChanges();
      component['loadMore']();

      expect(topicService.fetchTopics).toHaveBeenCalledTimes(2);
      expect(component['topicList']()).toEqual([mockTopic]);
    });
  });

  // --------------------------------------------------
  // Abonnement
  // --------------------------------------------------

  describe('subscribeToTopic', () => {
    it('should call the service with the topic publicId', () => {
      component['subscribeToTopic'](mockTopic);

      expect(topicService.subscribe).toHaveBeenCalledTimes(1);
      expect(topicService.subscribe).toHaveBeenCalledWith('angular');
    });

    it('should mark the topic as subscribed after success', () => {
      topicService.subscribe.and.returnValue(of(undefined));

      const topic = { ...mockTopic };

      component['subscribeToTopic'](topic);

      expect(topic.subscribed).toBeTrue();
    });

    it('should log an error when subscribing fails', () => {
      spyOn(console, 'error');

      const error = new Error('Subscription failed');
      topicService.subscribe.and.returnValue(throwError(() => error));

      const topic = { ...mockTopic };
      component['subscribeToTopic'](topic);

      expect(console.error).toHaveBeenCalledWith(error);
      expect(topic.subscribed).toBeFalse();
    });
  });

  // --------------------------------------------------
  // Template : structure et affichage
  // --------------------------------------------------

  describe('template', () => {
    it('should render the topic list section', () => {
      fixture.detectChanges();

      const section: HTMLElement | null =
        fixture.nativeElement.querySelector('section.topic-list');

      expect(section).not.toBeNull();
    });

    it('should display a card for each topic', () => {
      topicService.fetchTopics.and.returnValue(
        of(createPage([mockTopic, secondTopic])),
      );

      fixture.detectChanges();

      const cards = fixture.debugElement.queryAll(
        By.directive(TopicCardComponent),
      );

      expect(cards.length).toBe(2);
    });

    it('should pass the correct topic to each card', () => {
      topicService.fetchTopics.and.returnValue(
        of(createPage([mockTopic, secondTopic])),
      );

      fixture.detectChanges();

      const cards = fixture.debugElement.queryAll(
        By.directive(TopicCardComponent),
      );

      expect(cards[0].componentInstance.topic).toEqual(mockTopic);
      expect(cards[1].componentInstance.topic).toEqual(secondTopic);
    });

    it('should display the empty message when no topics are available', () => {
      topicService.fetchTopics.and.returnValue(of(createPage([])));

      fixture.detectChanges();

      const message: HTMLElement | null =
        fixture.nativeElement.querySelector('.topic-list__empty');

      expect(message).not.toBeNull();
      expect(message?.textContent?.trim()).toBe('Aucun thème disponible.');
    });

    it('should hide the empty message while loading', () => {
      const response = new Subject<Page<Topic>>();
      topicService.fetchTopics.and.returnValue(response);

      fixture.detectChanges();

      expect(
        fixture.nativeElement.querySelector('.topic-list__empty'),
      ).toBeNull();

      response.next(createPage([]));
      response.complete();
      fixture.detectChanges();

      expect(
        fixture.nativeElement.querySelector('.topic-list__empty'),
      ).not.toBeNull();
    });

    it('should display the loader while loading', () => {
      const response = new Subject<Page<Topic>>();
      topicService.fetchTopics.and.returnValue(response);

      fixture.detectChanges();

      const loader: HTMLElement | null = fixture.nativeElement.querySelector(
        '.topic-list__loader',
      );

      expect(loader).not.toBeNull();
      expect(loader?.textContent?.trim()).toBe('Chargement...');
    });

    it('should hide the loader after loading succeeds', () => {
      const response = new Subject<Page<Topic>>();
      topicService.fetchTopics.and.returnValue(response);

      fixture.detectChanges();

      response.next(createPage([mockTopic]));
      response.complete();
      fixture.detectChanges();

      expect(
        fixture.nativeElement.querySelector('.topic-list__loader'),
      ).toBeNull();
    });

    it('should hide the loader after loading fails', () => {
      spyOn(console, 'error');

      topicService.fetchTopics.and.returnValue(
        throwError(() => new Error('Request failed')),
      );

      fixture.detectChanges();

      expect(
        fixture.nativeElement.querySelector('.topic-list__loader'),
      ).toBeNull();
    });
  });

  // --------------------------------------------------
  // Template : scroll infini
  // --------------------------------------------------

  describe('infinite scroll', () => {
    it('should configure the infinite scroll section', () => {
      fixture.detectChanges();

      const section = fixture.debugElement.query(By.css('section.topic-list'));

      expect(section.attributes['infiniteScrollDistance']).toBe('2');
      expect(section.attributes['infiniteScrollThrottle']).toBe('150');
    });

    it('should load the next page when scrolled is emitted', () => {
      topicService.fetchTopics.and.returnValues(
        of(createPage([mockTopic], 0, false)),
        of(createPage([secondTopic], 1, true)),
      );

      fixture.detectChanges();

      const section = fixture.debugElement.query(By.css('section.topic-list'));

      section.triggerEventHandler('scrolled', undefined);

      expect(topicService.fetchTopics).toHaveBeenCalledTimes(2);
      expect(topicService.fetchTopics).toHaveBeenCalledWith(1, 10);
    });

    it('should render the newly loaded topics after scrolling', () => {
      topicService.fetchTopics.and.returnValues(
        of(createPage([mockTopic], 0, false)),
        of(createPage([secondTopic], 1, true)),
      );

      fixture.detectChanges();

      const section = fixture.debugElement.query(By.css('section.topic-list'));

      section.triggerEventHandler('scrolled', undefined);
      fixture.detectChanges();

      const cards = fixture.debugElement.queryAll(
        By.directive(TopicCardComponent),
      );

      expect(cards.length).toBe(2);
    });
  });

  // --------------------------------------------------
  // Template : événement d'abonnement de la carte
  // --------------------------------------------------

  describe('topic card events', () => {
    it('should subscribe when a card emits the subscribe event', () => {
      fixture.detectChanges();

      component['topicList'].set([{ ...mockTopic }]);
      fixture.detectChanges();

      const card = fixture.debugElement.query(By.directive(TopicCardComponent));

      card.triggerEventHandler('subscribe', mockTopic);

      expect(topicService.subscribe).toHaveBeenCalledTimes(1);
      expect(topicService.subscribe).toHaveBeenCalledWith('angular');
    });

    it('should update the card topic after subscribing', () => {
      const topic = { ...mockTopic };

      fixture.detectChanges();

      component['topicList'].set([topic]);
      fixture.detectChanges();

      const card = fixture.debugElement.query(By.directive(TopicCardComponent));

      card.triggerEventHandler('subscribe', topic);
      fixture.detectChanges();

      expect(topic.subscribed).toBeTrue();
      expect(card.componentInstance.topic.subscribed).toBeTrue();
    });
  });
});
