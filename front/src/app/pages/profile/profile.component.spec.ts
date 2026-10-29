import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { ProfileComponent } from './profile.component';
import { TopicService } from '@service/topic.service';
import { UserService } from '@service/user.service';
import { UserStateService } from '@service/user-state.service';
import { ValidationErrorService } from '@service/validation.service';
import { Topic } from '@model/topic.model';
import { User } from '@model/user.model';
import { Page } from '@type/page.type';

describe('ProfileComponent', () => {
  let fixture: ComponentFixture<ProfileComponent>;
  let component: ProfileComponent;

  let topicService: jasmine.SpyObj<TopicService>;
  let userService: jasmine.SpyObj<UserService>;
  let userStateService: jasmine.SpyObj<UserStateService>;
  let validationErrorService: jasmine.SpyObj<ValidationErrorService>;

  const currentUser: User = {
    email: 'test@example.com',
    username: 'testuser',
    createdAt: new Date('2025-01-01'),
    updatedAt: new Date('2025-01-01'),
  };

  const topic: Topic = {
    publicId: 'angular',
    name: 'Angular',
    description: 'Découvrez Angular.',
    subscribed: true,
    createdAt: new Date('2025-01-01'),
    updatedAt: new Date('2025-01-01'),
  };

  const emptyPage: Page<Topic> = {
    content: [],
    number: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
    empty: true,
    numberOfElements: 0,
  };

  const createPage = (
    content: Topic[],
    number = 0,
    last = true,
  ): Page<Topic> => ({
    content,
    number,
    size: 10,
    totalElements: content.length,
    totalPages: last ? 1 : 2,
    first: number === 0,
    last,
    empty: content.length === 0,
    numberOfElements: content.length,
  });

  beforeEach(async () => {
    topicService = jasmine.createSpyObj<TopicService>('TopicService', [
      'fetchSubscribed',
      'unsubscribe',
    ]);

    userService = jasmine.createSpyObj<UserService>('UserService', [
      'updateUser',
    ]);

    userStateService = jasmine.createSpyObj<UserStateService>(
      'UserStateService',
      ['getUser', 'setUser'],
    );

    validationErrorService = jasmine.createSpyObj<ValidationErrorService>(
      'ValidationErrorService',
      ['getErrors'],
    );

    userService.updateUser.and.returnValue(of(currentUser));

    userStateService.getUser.and.returnValue(currentUser);
    topicService.fetchSubscribed.and.returnValue(of(emptyPage));
    topicService.unsubscribe.and.returnValue(of(void 0));
    validationErrorService.getErrors.and.returnValue([]);

    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [
        { provide: TopicService, useValue: topicService },
        { provide: UserService, useValue: userService },
        { provide: UserStateService, useValue: userStateService },
        {
          provide: ValidationErrorService,
          useValue: validationErrorService,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function prepareFormWithoutUsernameAsyncValidator(): void {
    const usernameControl = component.userForm.controls.username;

    usernameControl.clearAsyncValidators();
    usernameControl.updateValueAndValidity();
  }

  function fillValidForm(): void {
    component.userForm.setValue({
      email: 'updated@example.com',
      username: 'updateduser',
      password: 'Password123!',
    });
  }

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should display the current user information', () => {
    expect(component.userForm.controls.email.value).toBe(currentUser.email);
    expect(component.userForm.controls.username.value).toBe(
      currentUser.username,
    );
  });

  it('should load subscribed topics on initialization', () => {
    expect(topicService.fetchSubscribed).toHaveBeenCalledWith(0, 10);
    expect(component.subscribedTopicList()).toEqual([]);
  });

  it('should display subscribed topics after loading', () => {
    topicService.fetchSubscribed.and.returnValue(of(createPage([topic])));

    (component as any).loadMoreSubscribed(0);
    fixture.detectChanges();

    expect(component.subscribedTopicList()).toEqual([topic]);
    expect(component.hasMore()).toBeFalse();
    expect(component.isLoadingTopics()).toBeFalse();
  });

  it('should display the empty state when there are no subscribed topics', () => {
    expect(
      fixture.nativeElement.querySelector('.profile__empty')?.textContent,
    ).toContain("Vous n'êtes abonné à aucun thème.");
  });

  it('should update the profile successfully', () => {
    prepareFormWithoutUsernameAsyncValidator();
    fillValidForm();

    expect(component.userForm.valid).toBeTrue();

    component.submit();

    expect(userService.updateUser).toHaveBeenCalledWith({
      email: 'updated@example.com',
      username: 'updateduser',
      password: 'Password123!',
    });

    expect(userStateService.setUser).toHaveBeenCalledWith(currentUser);
    expect(component.errorMessage()).toBe('');
    expect(component.isLoadingForm()).toBeFalse();
  });

  it('should display an error when the profile update fails', () => {
    prepareFormWithoutUsernameAsyncValidator();

    userService.updateUser.and.returnValue(
      throwError(() => new Error('Update failed')),
    );

    fillValidForm();

    expect(component.userForm.valid).toBeTrue();

    component.submit();

    expect(userService.updateUser).toHaveBeenCalledWith({
      email: 'updated@example.com',
      username: 'updateduser',
      password: 'Password123!',
    });

    expect(component.errorMessage()).toBe(
      "Une erreur est survenue lors de l'inscription.",
    );
    expect(component.isLoadingForm()).toBeFalse();
  });

  it('should not update the profile when the form is invalid', () => {
    prepareFormWithoutUsernameAsyncValidator();

    component.userForm.setValue({
      email: 'invalid-email',
      username: 'testuser',
      password: 'Password123!',
    });

    component.submit();

    expect(component.userForm.invalid).toBeTrue();
    expect(userService.updateUser).not.toHaveBeenCalled();
    expect(component.userForm.controls.email.touched).toBeTrue();
  });

  it('should not update the profile while the form is loading', () => {
    prepareFormWithoutUsernameAsyncValidator();
    fillValidForm();

    component.isLoadingForm.set(true);
    component.submit();

    expect(userService.updateUser).not.toHaveBeenCalled();
  });

  it('should remove a topic after unsubscribing successfully', () => {
    topicService.fetchSubscribed.and.returnValue(of(createPage([topic])));
    (component as any).loadMoreSubscribed(0);

    (component as any).unsubscribeToTopic(topic);

    expect(topicService.unsubscribe).toHaveBeenCalledWith(topic.publicId);
    expect(component.subscribedTopicList()).toEqual([]);
  });

  it('should set an error when unsubscribing fails', () => {
    topicService.fetchSubscribed.and.returnValue(of(createPage([topic])));
    (component as any).loadMoreSubscribed(0);

    topicService.unsubscribe.and.returnValue(
      throwError(() => new Error('Unsubscribe failed')),
    );

    (component as any).unsubscribeToTopic(topic);

    expect(component.topicErrorMessage).toBe(
      'Impossible de se désabonner au topic, veuillez rééssayer',
    );
    expect(component.subscribedTopicList()).toEqual([]);
  });

  it('should load the next page of subscribed topics', () => {
    const nextTopic: Topic = {
      ...topic,
      publicId: 'java',
      name: 'Java',
    };

    topicService.fetchSubscribed.and.returnValues(
      of(createPage([topic], 0, false)),
      of(createPage([nextTopic], 1, true)),
    );

    (component as any).loadMoreSubscribed(0);
    (component as any).loadMore();
    fixture.detectChanges();

    expect(topicService.fetchSubscribed).toHaveBeenCalledWith(1, 10);
    expect(component.subscribedTopicList()).toEqual([topic, nextTopic]);
    expect(component.hasMore()).toBeFalse();
  });

  it('should not load another page when no more topics are available', () => {
    topicService.fetchSubscribed.and.returnValue(of(emptyPage));
    (component as any).hasMore.set(false);

    (component as any).loadMore();

    expect(topicService.fetchSubscribed).toHaveBeenCalledTimes(1);
  });
});
