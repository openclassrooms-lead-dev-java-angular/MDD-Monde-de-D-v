import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';

import { TopicComponent } from './topic.component';
import { Page } from '@type/page.type';
import { Topic } from '@model/topic.model';
import { TopicService } from '@service/topic.service';

describe('TopicComponent', () => {
  let component: TopicComponent;
  let fixture: ComponentFixture<TopicComponent>;
  let topicService: jasmine.SpyObj<TopicService>;

  const mockTopic: Topic = {
    slug: 'angular',
    name: 'Angular',
    description: 'Discussions autour d’Angular.',
    subscribed: false,
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-02T00:00:00Z'),
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
      'getTopics',
    ]);

    topicService.getTopics.and.returnValue(of(createPage([])));

    await TestBed.configureTestingModule({
      imports: [TopicComponent],
      providers: [{ provide: TopicService, useValue: topicService }],
    })
      .overrideComponent(TopicComponent, {
        set: { template: '' },
      })
      .compileComponents();

    fixture = TestBed.createComponent(TopicComponent);
    component = fixture.componentInstance;
  });

  describe('initialization', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should load the first page on initialization', () => {
      topicService.getTopics.and.returnValue(of(createPage([mockTopic])));

      fixture.detectChanges();

      expect(topicService.getTopics).toHaveBeenCalledOnceWith(0, 10);
      expect(component['topicList']()).toEqual([mockTopic]);
    });

    it('should stop loading after the initial request succeeds', () => {
      fixture.detectChanges();

      expect(component['loading']()).toBeFalse();
    });

    it('should set hasMore to false when the last page is reached', () => {
      topicService.getTopics.and.returnValue(
        of(createPage([mockTopic], 0, true)),
      );

      fixture.detectChanges();

      expect(component['hasMore']()).toBeFalse();
    });

    it('should set hasMore to true when more pages are available', () => {
      topicService.getTopics.and.returnValue(
        of(createPage([mockTopic], 0, false)),
      );

      fixture.detectChanges();

      expect(component['hasMore']()).toBeTrue();
    });
  });

  describe('loadMore', () => {
    it('should load the next page when more topics are available', () => {
      topicService.getTopics.and.returnValues(
        of(createPage([mockTopic], 0, false)),
        of(createPage([], 1, true)),
      );

      fixture.detectChanges();

      component['loadMore']();

      expect(topicService.getTopics).toHaveBeenCalledTimes(2);
      expect(topicService.getTopics).toHaveBeenCalledWith(1, 10);
    });

    it('should append topics to the existing list', () => {
      const secondTopic: Topic = {
        ...mockTopic,
        slug: 'spring',
        name: 'Spring',
      };

      topicService.getTopics.and.returnValues(
        of(createPage([mockTopic], 0, false)),
        of(createPage([secondTopic], 1, true)),
      );

      fixture.detectChanges();
      component['loadMore']();

      expect(component['topicList']()).toEqual([mockTopic, secondTopic]);
    });

    it('should not load another page when the last page has been reached', () => {
      topicService.getTopics.and.returnValue(
        of(createPage([mockTopic], 0, true)),
      );

      fixture.detectChanges();
      component['loadMore']();

      expect(topicService.getTopics).toHaveBeenCalledTimes(1);
    });

    it('should not load another page while a request is in progress', () => {
      const responseSubject = new Subject<Page<Topic>>();

      topicService.getTopics.and.returnValue(responseSubject);

      fixture.detectChanges();

      expect(component['loading']()).toBeTrue();

      component['loadMore']();

      expect(topicService.getTopics).toHaveBeenCalledTimes(1);

      responseSubject.next(createPage([mockTopic], 0, false));
      responseSubject.complete();
    });
  });

  describe('error handling', () => {
    it('should stop loading when the request fails', () => {
      topicService.getTopics.and.returnValue(
        throwError(() => new Error('Request failed')),
      );

      spyOn(console, 'error');

      fixture.detectChanges();

      expect(component['loading']()).toBeFalse();
      expect(console.error).toHaveBeenCalled();
    });

    it('should log the error when loading topics fails', () => {
      const error = new Error('Server unavailable');

      topicService.getTopics.and.returnValue(throwError(() => error));

      spyOn(console, 'error');

      fixture.detectChanges();

      expect(console.error).toHaveBeenCalledWith(error);
    });
  });

  describe('subscribeToTopic', () => {
    it('should log the topic slug', () => {
      spyOn(console, 'log');

      component['subscribeToTopic'](mockTopic);

      expect(console.log).toHaveBeenCalledWith('Subscribe to:', 'angular');
    });
  });
});
