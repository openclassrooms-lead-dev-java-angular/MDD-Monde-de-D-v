import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile.component';
import { TopicService } from '@service/topic.service';
import { UserService } from '@service/user.service';
import { UserStateService } from '@service/user-state.service';
import { ValidationErrorService } from '@service/validation.service';
import { User } from '@model/user.model';
import { Topic } from '@model/topic.model';
import { Page } from '@type/page.type';
import { AuthService } from '@service/auth.service';
import { of, Subject, throwError } from 'rxjs';
import { Validators } from '@angular/forms';

describe('ProfileComponent', () => {
  let fixture: ComponentFixture<ProfileComponent>;
  let component: ProfileComponent;

  let topicService: jasmine.SpyObj<TopicService>;
  let userService: jasmine.SpyObj<UserService>;
  let userStateService: jasmine.SpyObj<UserStateService>;
  let validationErrorService: jasmine.SpyObj<ValidationErrorService>;

  const mockUser = {
    email: 'john@example.com',
    username: 'johnDoe',
  } as User;

  const mockTopic = {
    publicId: 'angular',
    name: 'Angular',
  } as Topic;

  const mockTopic2 = {
    publicId: 'typescript',
    name: 'TypeScript',
  } as Topic;

  const createPage = (
    content: Topic[],
    number = 0,
    last = false,
  ): Page<Topic> =>
    ({
      content,
      number,
      last,
    }) as Page<Topic>;

  beforeEach(async () => {
    topicService = jasmine.createSpyObj<TopicService>('TopicService', [
      'getSubscribed',
      'unsubscribe',
    ]);

    userService = jasmine.createSpyObj<UserService>('UserService', [
      'updateUser',
    ]);

    userStateService = jasmine.createSpyObj<UserStateService>(
      'UserStateService',
      ['getUser'],
    );

    validationErrorService = jasmine.createSpyObj<ValidationErrorService>(
      'ValidationErrorService',
      ['getErrors'],
    );

    userStateService.getUser.and.returnValue(mockUser);

    topicService.fetchSubscribed.and.returnValue(of(createPage([mockTopic])));

    topicService.unsubscribe.and.returnValue(of(void 0));

    userService.updateUser.and.returnValue(of(mockUser));

    validationErrorService.getErrors.and.returnValue([]);

    const authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', [
      'checkUsernameAvailability',
    ]);

    authServiceMock.checkUsernameAvailability.and.returnValue(
      of({
        available: true,
      }),
    );

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
        { provide: AuthService, useValue: authServiceMock },
      ],
    })
      .overrideComponent(ProfileComponent, {
        set: {
          template: '',
          imports: [],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
  });

  function initializeComponent(): void {
    fixture.detectChanges();
  }

  function prepareValidForm(): void {
    const { email, username, password } = component.userForm.controls;

    username.clearAsyncValidators();
    username.setValue('johnDoe');

    email.setValue('john@example.com');

    password.setValue('Password123!');
    password.setValidators([Validators.required]);
    password.updateValueAndValidity();
  }

  describe('creation', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });
  });

  describe('ngOnInit', () => {
    it('should initialize the form with the current user data', () => {
      initializeComponent();

      expect(component.userForm.controls.email.value).toBe(mockUser.email);
      expect(component.userForm.controls.username.value).toBe(
        mockUser.username,
      );
    });

    it('should load the first page of subscribed topics', () => {
      initializeComponent();

      expect(topicService.getSubscribed).toHaveBeenCalledOnceWith(0, 10);

      expect(component['subscribedTopicList']()).toEqual([mockTopic]);
    });

    it('should stop loading after a successful response', () => {
      initializeComponent();

      expect(component['isLoadingTopics']()).toBeFalse();
    });

    it('should update hasMore when the last page is reached', () => {
      topicService.getSubscribed.and.returnValue(
        of(createPage([mockTopic], 0, true)),
      );

      initializeComponent();

      expect(component['hasMore']()).toBeFalse();
    });

    it('should not load topics when a request is already in progress', () => {
      component['isLoadingTopics'].set(true);

      initializeComponent();

      expect(topicService.getSubscribed).not.toHaveBeenCalled();
    });

    it('should not load topics when there are no more pages', () => {
      component['hasMore'].set(false);

      initializeComponent();

      expect(topicService.getSubscribed).not.toHaveBeenCalled();
    });

    it('should set an error message when loading topics fails', () => {
      spyOn(console, 'error');

      topicService.getSubscribed.and.returnValue(
        throwError(() => new Error('Server error')),
      );

      initializeComponent();

      expect(component['isLoadingTopics']()).toBeFalse();
      expect(component.topicErrorMessage).toBe(
        'Impossible de se désabonner au topic',
      );
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('isUsernameLoading', () => {
    it('should return true when username validation is pending', () => {
      component.userForm.controls.username.markAsPending();

      expect(component.isUsernameLoading).toBeTrue();
    });

    it('should return false when username validation is not pending', () => {
      const username = component.userForm.controls.username;

      username.clearAsyncValidators();
      username.setValue('johnDoe');

      expect(component.isUsernameLoading).toBeFalse();
    });
  });

  describe('validation error getters', () => {
    it('should return email errors', () => {
      validationErrorService.getErrors.and.returnValue(['Email invalide']);

      expect(component.emailErrors).toEqual(['Email invalide']);

      expect(validationErrorService.getErrors).toHaveBeenCalledWith(
        component.userForm.controls.email,
        'email',
      );
    });

    it('should return username errors', () => {
      validationErrorService.getErrors.and.returnValue([
        'Nom utilisateur invalide',
      ]);

      expect(component.usernameErrors).toEqual(['Nom utilisateur invalide']);

      expect(validationErrorService.getErrors).toHaveBeenCalledWith(
        component.userForm.controls.username,
        'username',
      );
    });

    it('should return password errors', () => {
      validationErrorService.getErrors.and.returnValue([
        'Mot de passe invalide',
      ]);

      expect(component.passwordErrors).toEqual(['Mot de passe invalide']);

      expect(validationErrorService.getErrors).toHaveBeenCalledWith(
        component.userForm.controls.password,
        'password',
      );
    });
  });

  describe('submit', () => {
    beforeEach(() => {
      initializeComponent();
      prepareValidForm();
    });

    it('should submit the form values to the user service', () => {
      component.submit();

      expect(userService.updateUser).toHaveBeenCalledOnceWith({
        email: 'john@example.com',
        username: 'johnDoe',
        password: 'Password123!',
      });
    });

    it('should clear the previous error message', () => {
      component.errorMessage.set('Previous error');

      component.submit();

      expect(component.errorMessage()).toBe('');
    });

    it('should not submit an invalid form', () => {
      component.userForm.controls.email.setValue('');

      component.submit();

      expect(userService.updateUser).not.toHaveBeenCalled();
      expect(component.userForm.controls.email.touched).toBeTrue();
    });

    it('should not submit a pending form', () => {
      component.userForm.controls.username.markAsPending();

      component.submit();

      expect(userService.updateUser).not.toHaveBeenCalled();
    });

    it('should not submit while another submission is in progress', () => {
      component.isLoadingForm.set(true);

      component.submit();

      expect(userService.updateUser).not.toHaveBeenCalled();
    });

    it('should set loading while the request is pending', () => {
      const request = new Subject<User>();

      userService.updateUser.and.returnValue(request.asObservable());

      component.submit();

      expect(component.isLoadingForm()).toBeTrue();

      request.next(mockUser);

      expect(component.isLoadingForm()).toBeTrue();

      request.complete();

      expect(component.isLoadingForm()).toBeFalse();
    });

    it('should stop loading after a successful response', () => {
      component.submit();

      expect(component.isLoadingForm()).toBeFalse();
    });

    it('should set an error message when the request fails', () => {
      spyOn(console, 'error');

      userService.updateUser.and.returnValue(
        throwError(() => ({
          error: { message: 'Server error' },
        })),
      );

      component.submit();

      expect(component.errorMessage()).toBe(
        "Une erreur est survenue lors de l'inscription.",
      );

      expect(component.isLoadingForm()).toBeFalse();
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('unsubscribeToTopic', () => {
    beforeEach(() => {
      component['subscribedTopicList'].set([mockTopic, mockTopic2]);
    });

    it('should remove the topic optimistically', () => {
      component['unsubscribeToTopic'](mockTopic);

      expect(component['subscribedTopicList']()).toEqual([mockTopic2]);
    });

    it('should call the topic service with the topic slug', () => {
      component['unsubscribeToTopic'](mockTopic);

      expect(topicService.unsubscribe).toHaveBeenCalledOnceWith(mockTopic.slug);
    });

    it('should set an error message when unsubscribe fails', () => {
      spyOn(console, 'error');

      topicService.unsubscribe.and.returnValue(
        throwError(() => new Error('Server error')),
      );

      component['unsubscribeToTopic'](mockTopic);

      expect(component.topicErrorMessage).toBe(
        'Impossible de se désabonner au topic, veuillez rééssayer',
      );

      expect(console.error).toHaveBeenCalled();
    });

    it('should keep the topic removed after a successful unsubscribe', () => {
      component['unsubscribeToTopic'](mockTopic);

      expect(component['subscribedTopicList']()).toEqual([mockTopic2]);
    });
  });

  describe('loadMore', () => {
    it('should load the next page', () => {
      initializeComponent();

      topicService.getSubscribed.calls.reset();

      component['loadMore']();

      expect(topicService.getSubscribed).toHaveBeenCalledOnceWith(1, 10);
    });

    it('should not load another page while loading', () => {
      component['isLoadingTopics'].set(true);

      component['loadMore']();

      expect(topicService.getSubscribed).not.toHaveBeenCalled();
    });

    it('should not load another page when hasMore is false', () => {
      component['hasMore'].set(false);

      component['loadMore']();

      expect(topicService.getSubscribed).not.toHaveBeenCalled();
    });
  });

  describe('loadMoreSubscribed', () => {
    it('should append the returned topics to the existing list', () => {
      component['subscribedTopicList'].set([mockTopic]);

      topicService.getSubscribed.and.returnValue(
        of(createPage([mockTopic2], 1, false)),
      );

      component['loadMoreSubscribed'](1);

      expect(component['subscribedTopicList']()).toEqual([
        mockTopic,
        mockTopic2,
      ]);
    });

    it('should update the current page number', () => {
      topicService.getSubscribed.and.returnValue(
        of(createPage([mockTopic], 2, false)),
      );

      component['loadMoreSubscribed'](2);

      expect(component['currentPage']).toBe(3);
    });

    it('should set hasMore to false on the last page', () => {
      topicService.getSubscribed.and.returnValue(
        of(createPage([mockTopic], 0, true)),
      );

      component['loadMoreSubscribed'](0);

      expect(component['hasMore']()).toBeFalse();
    });

    it('should set loading to true while the request is pending', () => {
      const request = new Subject<Page<Topic>>();

      topicService.getSubscribed.and.returnValue(request.asObservable());

      component['loadMoreSubscribed'](0);

      expect(component['isLoadingTopics']()).toBeTrue();

      request.next(createPage([mockTopic], 0, false));
      request.complete();

      expect(component['isLoadingTopics']()).toBeFalse();
    });

    it('should reset loading and set an error message on failure', () => {
      spyOn(console, 'error');

      topicService.getSubscribed.and.returnValue(
        throwError(() => new Error('Server error')),
      );

      component['loadMoreSubscribed'](0);

      expect(component['isLoadingTopics']()).toBeFalse();
      expect(component.topicErrorMessage).toBe(
        'Impossible de se désabonner au topic',
      );

      expect(console.error).toHaveBeenCalled();
    });
  });
});
