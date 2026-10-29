import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Subject, of, throwError } from 'rxjs';

import { TopicComponent } from './topic.component';
import { TopicCardComponent } from 'src/app/shared/components/topic-card/topic-card.component';
import { TopicService } from '@service/topic.service';
import { Topic } from '@model/topic.model';

describe('TopicComponent', () => {
  let fixture: ComponentFixture<TopicComponent>;
  let component: TopicComponent;
  let topicService: jasmine.SpyObj<TopicService>;

  const createTopic = (overrides: Partial<Topic> = {}): Topic =>
    ({
      publicId: 'topic-1',
      name: 'Angular',
      description: 'Angular development',
      subscribed: false,
      ...overrides,
    }) as Topic;

  const createPage = (
    content: Topic[] = [],
    number = 0,
    last = false,
  ): any => ({
    content,
    number,
    last,
    totalElements: content.length,
    totalPages: last ? number + 1 : number + 2,
    size: 10,
    first: number === 0,
    empty: content.length === 0,
  });

  const createComponent = (): void => {
    fixture = TestBed.createComponent(TopicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  const getText = (selector: string): string =>
    fixture.nativeElement.querySelector(selector)?.textContent?.trim() ?? '';

  beforeEach(async () => {
    topicService = jasmine.createSpyObj<TopicService>('TopicService', [
      'fetchTopics',
      'subscribe',
    ]);

    topicService.fetchTopics.and.returnValue(
      of(createPage([], 0, false)) as ReturnType<TopicService['fetchTopics']>,
    );

    topicService.subscribe.and.returnValue(
      of(undefined) as ReturnType<TopicService['subscribe']>,
    );

    await TestBed.configureTestingModule({
      imports: [TopicComponent],
      providers: [
        {
          provide: TopicService,
          useValue: topicService,
        },
      ],
    }).compileComponents();
  });

  describe('initialization', () => {
    it('should create the component', () => {
      createComponent();

      expect(component).toBeTruthy();
    });

    it('should load the first page on initialization', () => {
      createComponent();

      expect(topicService.fetchTopics).toHaveBeenCalledOnceWith(0, 10);
    });

    it('should initialize with an empty topic list', () => {
      createComponent();

      expect((component as any).topicList()).toEqual([]);
    });

    it('should initialize with loading set to false after a successful response', () => {
      createComponent();

      expect((component as any).loading()).toBeFalse();
    });

    it('should allow more pages when the first page is not the last one', () => {
      createComponent();

      expect((component as any).hasMore()).toBeTrue();
    });

    it('should stop pagination when the first page is the last one', () => {
      topicService.fetchTopics.and.returnValue(
        of(createPage([], 0, true)) as ReturnType<TopicService['fetchTopics']>,
      );

      createComponent();

      expect((component as any).hasMore()).toBeFalse();
    });
  });

  describe('topic loading', () => {
    it('should add topics returned by the service to the list', () => {
      const topics = [
        createTopic({ publicId: 'topic-1', name: 'Angular' }),
        createTopic({ publicId: 'topic-2', name: 'Spring Boot' }),
      ];

      topicService.fetchTopics.and.returnValue(
        of(createPage(topics, 0, false)) as ReturnType<
          TopicService['fetchTopics']
        >,
      );

      createComponent();

      expect((component as any).topicList()).toEqual(topics);
    });

    it('should display one topic card for each topic', () => {
      const topics = [
        createTopic({ publicId: 'topic-1', name: 'Angular' }),
        createTopic({ publicId: 'topic-2', name: 'Spring Boot' }),
      ];

      topicService.fetchTopics.and.returnValue(
        of(createPage(topics, 0, false)) as ReturnType<
          TopicService['fetchTopics']
        >,
      );

      createComponent();

      const cards = fixture.debugElement.queryAll(
        By.directive(TopicCardComponent),
      );

      expect(cards.length).toBe(2);
    });

    it('should pass each topic to its topic card', () => {
      const topic = createTopic();

      topicService.fetchTopics.and.returnValue(
        of(createPage([topic], 0, false)) as ReturnType<
          TopicService['fetchTopics']
        >,
      );

      createComponent();

      const card = fixture.debugElement.query(By.directive(TopicCardComponent));

      expect(card.componentInstance.topic()).toEqual(topic);
    });

    it('should append newly loaded topics without removing existing topics', () => {
      const firstPageTopics = [
        createTopic({ publicId: 'topic-1', name: 'Angular' }),
      ];
      const secondPageTopics = [
        createTopic({ publicId: 'topic-2', name: 'Spring Boot' }),
      ];

      topicService.fetchTopics.and.returnValues(
        of(createPage(firstPageTopics, 0, false)) as ReturnType<
          TopicService['fetchTopics']
        >,
        of(createPage(secondPageTopics, 1, true)) as ReturnType<
          TopicService['fetchTopics']
        >,
      );

      createComponent();
      (component as any).loadMore();
      fixture.detectChanges();

      expect((component as any).topicList()).toEqual([
        ...firstPageTopics,
        ...secondPageTopics,
      ]);
    });

    it('should update the current page based on the response page number', () => {
      topicService.fetchTopics.and.returnValue(
        of(createPage([], 4, false)) as ReturnType<TopicService['fetchTopics']>,
      );

      createComponent();

      expect((component as any).currentPage).toBe(5);
    });

    it('should set hasMore to false when the response indicates the last page', () => {
      topicService.fetchTopics.and.returnValue(
        of(createPage([], 0, true)) as ReturnType<TopicService['fetchTopics']>,
      );

      createComponent();

      expect((component as any).hasMore()).toBeFalse();
    });

    it('should set loading to false after a successful response', () => {
      createComponent();

      expect((component as any).loading()).toBeFalse();
    });
  });

  describe('loading indicator', () => {
    it('should display the loading indicator while a request is pending', () => {
      const response$ = new Subject<any>();

      topicService.fetchTopics.and.returnValue(
        response$ as ReturnType<TopicService['fetchTopics']>,
      );

      createComponent();
      fixture.detectChanges();

      expect((component as any).loading()).toBeTrue();
      expect(getText('.topic-list__loader')).toBe('Chargement...');
    });

    it('should hide the loading indicator after a successful response', () => {
      const response$ = new Subject<any>();

      topicService.fetchTopics.and.returnValue(
        response$ as ReturnType<TopicService['fetchTopics']>,
      );

      createComponent();

      response$.next(createPage([], 0, false));
      response$.complete();
      fixture.detectChanges();

      expect((component as any).loading()).toBeFalse();
      expect(
        fixture.nativeElement.querySelector('.topic-list__loader'),
      ).toBeNull();
    });

    it('should hide the loading indicator after a failed request', () => {
      spyOn(console, 'error');

      topicService.fetchTopics.and.returnValue(
        throwError(() => new Error('Network error')) as ReturnType<
          TopicService['fetchTopics']
        >,
      );

      createComponent();
      fixture.detectChanges();

      expect((component as any).loading()).toBeFalse();
      expect(
        fixture.nativeElement.querySelector('.topic-list__loader'),
      ).toBeNull();
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('empty state', () => {
    it('should display the empty message when there are no topics and loading is false', () => {
      topicService.fetchTopics.and.returnValue(
        of(createPage([], 0, true)) as ReturnType<TopicService['fetchTopics']>,
      );

      createComponent();

      expect(getText('.topic-list__empty')).toBe('Aucun thème disponible.');
    });

    it('should not display the empty message while loading', () => {
      const response$ = new Subject<any>();

      topicService.fetchTopics.and.returnValue(
        response$ as ReturnType<TopicService['fetchTopics']>,
      );

      createComponent();

      expect(
        fixture.nativeElement.querySelector('.topic-list__empty'),
      ).toBeNull();
    });

    it('should not display the empty message when topics are available', () => {
      topicService.fetchTopics.and.returnValue(
        of(createPage([createTopic()], 0, false)) as ReturnType<
          TopicService['fetchTopics']
        >,
      );

      createComponent();

      expect(
        fixture.nativeElement.querySelector('.topic-list__empty'),
      ).toBeNull();
    });
  });

  describe('infinite scroll and pagination', () => {
    it('should request the next page when loadMore is called', () => {
      topicService.fetchTopics.and.returnValues(
        of(createPage([], 0, false)) as ReturnType<TopicService['fetchTopics']>,
        of(createPage([], 1, false)) as ReturnType<TopicService['fetchTopics']>,
      );

      createComponent();
      (component as any).loadMore();

      expect(topicService.fetchTopics).toHaveBeenCalledTimes(2);
      expect(topicService.fetchTopics).toHaveBeenCalledWith(1, 10);
    });

    it('should not request another page while a request is pending', () => {
      const response$ = new Subject<any>();

      topicService.fetchTopics.and.returnValue(
        response$ as ReturnType<TopicService['fetchTopics']>,
      );

      createComponent();

      expect((component as any).loading()).toBeTrue();

      (component as any).loadMore();

      expect(topicService.fetchTopics).toHaveBeenCalledTimes(1);

      response$.next(createPage([], 0, false));
      response$.complete();
    });

    it('should not request another page when there are no more pages', () => {
      topicService.fetchTopics.and.returnValue(
        of(createPage([], 0, true)) as ReturnType<TopicService['fetchTopics']>,
      );

      createComponent();
      (component as any).loadMore();

      expect(topicService.fetchTopics).toHaveBeenCalledTimes(1);
    });

    it('should allow loading again after a failed request', () => {
      spyOn(console, 'error');

      topicService.fetchTopics.and.returnValues(
        throwError(() => new Error('Network error')) as ReturnType<
          TopicService['fetchTopics']
        >,
        of(createPage([], 0, false)) as ReturnType<TopicService['fetchTopics']>,
      );

      createComponent();
      (component as any).loadMore();

      expect(topicService.fetchTopics).toHaveBeenCalledTimes(2);
    });

    it('should use the page number returned by the server for the next request', () => {
      topicService.fetchTopics.and.returnValues(
        of(createPage([], 2, false)) as ReturnType<TopicService['fetchTopics']>,
        of(createPage([], 3, false)) as ReturnType<TopicService['fetchTopics']>,
      );

      createComponent();
      (component as any).loadMore();

      expect(topicService.fetchTopics.calls.argsFor(1)).toEqual([3, 10]);
    });
  });

  describe('subscribeToTopic', () => {
    it('should call the service with the topic publicId', () => {
      const topic = createTopic({ publicId: 'topic-123' });

      createComponent();
      (component as any).subscribeToTopic(topic);

      expect(topicService.subscribe).toHaveBeenCalledOnceWith('topic-123');
    });

    it('should mark the topic as subscribed after a successful request', () => {
      const topic = createTopic({ publicId: 'topic-123' });

      createComponent();
      (component as any).subscribeToTopic(topic);

      expect(topic.subscribed).toBeTrue();
    });

    it('should update the corresponding topic in the list after subscribing', () => {
      const topic = createTopic({ publicId: 'topic-123' });

      topicService.fetchTopics.and.returnValue(
        of(createPage([topic], 0, false)) as ReturnType<
          TopicService['fetchTopics']
        >,
      );

      createComponent();
      (component as any).subscribeToTopic(topic);
      fixture.detectChanges();

      expect((component as any).topicList()[0].subscribed).toBeTrue();
    });

    it('should not change other topics when subscribing to one topic', () => {
      const topic1 = createTopic({
        publicId: 'topic-1',
        subscribed: false,
      });
      const topic2 = createTopic({
        publicId: 'topic-2',
        subscribed: false,
      });

      topicService.fetchTopics.and.returnValue(
        of(createPage([topic1, topic2], 0, false)) as ReturnType<
          TopicService['fetchTopics']
        >,
      );

      createComponent();
      (component as any).subscribeToTopic(topic1);

      const topics = (component as any).topicList();

      expect(topics[0].subscribed).toBeTrue();
      expect(topics[1].subscribed).toBeFalse();
    });

    it('should handle a subscription error and set an error message', () => {
      const error = new Error('Subscription failed');
      spyOn(console, 'error');

      topicService.subscribe.and.returnValue(
        throwError(() => error) as ReturnType<TopicService['subscribe']>,
      );

      createComponent();

      const topic = createTopic();
      (component as any).subscribeToTopic(topic);

      expect(console.error).toHaveBeenCalledWith(error);
      expect((component as any).topicErrorMessage()).toBe(
        'Impossible de s’abonner au topic',
      );
    });

    it('should not mark the topic as subscribed when the request fails', () => {
      spyOn(console, 'error');

      topicService.subscribe.and.returnValue(
        throwError(() => new Error('Subscription failed')) as ReturnType<
          TopicService['subscribe']
        >,
      );

      createComponent();

      const topic = createTopic({ subscribed: false });
      (component as any).subscribeToTopic(topic);

      expect(topic.subscribed).toBeFalse();
    });

    it('should subscribe when the topic card emits the subscribe output', () => {
      const topic = createTopic({ publicId: 'topic-from-card' });

      topicService.fetchTopics.and.returnValue(
        of(createPage([topic], 0, false)) as ReturnType<
          TopicService['fetchTopics']
        >,
      );

      createComponent();

      const card = fixture.debugElement.query(By.directive(TopicCardComponent));

      card.componentInstance.subscribe.emit(topic);

      expect(topicService.subscribe).toHaveBeenCalledOnceWith(
        'topic-from-card',
      );
      expect(topic.subscribed).toBeTrue();
    });
  });

  describe('template structure', () => {
    it('should render the topic list section', () => {
      createComponent();

      expect(
        fixture.nativeElement.querySelector('section.topic-list'),
      ).not.toBeNull();
    });
  });
});
