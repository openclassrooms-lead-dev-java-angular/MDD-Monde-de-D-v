import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { ProfileComponent } from './profile.component';
import { Topic } from '@model/topic.model';
import { User } from '@model/user.model';
import { AuthService } from '@service/auth.service';
import { TopicService } from '@service/topic.service';
import { UserStateService } from '@service/user-state.service';
import { UserService } from '@service/user.service';
import { ValidationErrorService } from '@service/validation.service';

describe('ProfileComponent', () => {
  let fixture: ComponentFixture<ProfileComponent>;
  let component: ProfileComponent;

  let topicService: jasmine.SpyObj<TopicService>;
  let userService: jasmine.SpyObj<UserService>;
  let userStateService: jasmine.SpyObj<UserStateService>;
  let validationErrorService: jasmine.SpyObj<ValidationErrorService>;

  const currentUser = {
    email: 'john.doe@example.com',
    username: 'John_Doe',
  } as User;

  const createTopic = (slug: string): Topic =>
    ({ slug }) as Topic;

  const createPage = (
    content: Topic[],
    number: number,
    last: boolean,
  ) => ({
    content,
    number,
    last,
  });

  beforeEach(async () => {
    topicService = jasmine.createSpyObj<TopicService>(
      'TopicService',
      ['getSubscribed', 'unsubscribe'],
    );

    userService = jasmine.createSpyObj<UserService>(
      'UserService',
      ['updateUser'],
    );

    userStateService = jasmine.createSpyObj<UserStateService>(
      'UserStateService',
      ['getUser'],
    );

    validationErrorService =
      jasmine.createSpyObj<ValidationErrorService>(
        'ValidationErrorService',
        ['getErrors'],
      );

    userStateService.getUser.and.returnValue(currentUser);
    topicService.getSubscribed.and.returnValue(
      of(createPage([], 0, true)) as ReturnType<
        TopicService['getSubscribed']
      >,
    );
    topicService.unsubscribe.and.returnValue(of(void 0));
    userService.updateUser.and.returnValue(
      of(currentUser) as ReturnType<UserService['updateUser']>,
    );
    validationErrorService.getErrors.and.returnValue([]);

    // Le validateur asynchrone peut utiliser différentes méthodes du service
    // d'authentification. Ce proxy renvoie un Observable positif pour chacune.
    const authServiceMock = new Proxy(
      {},
      {
        get: (_target, property) =>
          jasmine
            .createSpy(String(property))
            .and.returnValue(of(true)),
      },
    ) as AuthService;

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
        set: { template: '' },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;

    // Les tests du formulaire ne dépendent pas du validateur d'API.
    component['userForm'].controls.username.clearAsyncValidators();
    component['userForm'].controls.username.updateValueAndValidity();

    fixture.detectChanges();
  });

  describe('ngOnInit', () => {
    it('should initialize email and username from the current user', () => {
      expect(component['userForm'].getRawValue()).toEqual({
        email: currentUser.email,
        username: currentUser.username,
        password: '',
      });
    });

    it('should load the first page of subscribed topics', () => {
      expect(topicService.getSubscribed).toHaveBeenCalledOnceWith(0, 10);
    });

    it('should populate the subscribed topic list', () => {
      const topics = [
        createTopic('angular'),
        createTopic('spring-boot'),
      ];

      topicService.getSubscribed.and.returnValue(
        of(createPage(topics, 0, true)) as ReturnType<
          TopicService['getSubscribed']
        >,
      );

      component['loadMoreSubscribed'](0);

      expect(component['subscribedTopicList']()).toEqual(topics);
      expect(component['hasMore']()).toBeFalse();
      expect(component['isLoadingTopics']()).toBeFalse();
    });
  });

  describe('form validation state', () => {
    it('should expose username pending state', () => {
      expect(component.isUsernameLoading).toBeFalse();
    });

    it('should return email validation errors', () => {
      const errors = ['Adresse e-mail invalide'];

      validationErrorService.getErrors.and.returnValue(errors);

      expect(component.emailErrors).toEqual(errors);
      expect(validationErrorService.getErrors).toHaveBeenCalledWith(
        component['userForm'].controls.email,
        'email',
      );
    });

    it('should return username validation errors', () => {
      const errors = ['Nom d’utilisateur indisponible'];

      validationErrorService.getErrors.and.returnValue(errors);

      expect(component.usernameErrors).toEqual(errors);
      expect(validationErrorService.getErrors).toHaveBeenCalledWith(
        component['userForm'].controls.username,
        'username',
      );
    });

    it('should return password validation errors', () => {
      const errors = ['Mot de passe invalide'];

      validationErrorService.getErrors.and.returnValue(errors);

      expect(component.passwordErrors).toEqual(errors);
      expect(validationErrorService.getErrors).toHaveBeenCalledWith(
        component['userForm'].controls.password,
        'password',
      );
    });
  });

  describe('submit', () => {
    const fillValidForm = () => {
      component['userForm'].setValue({
        email: 'updated@example.com',
        username: 'Updated_User',
        password: 'StrongPassword123!',
      });
    };

    it('should update the user when the form is valid', () => {
      fillValidForm();

      const updatedUser = {
        email: 'updated@example.com',
        username: 'Updated_User',
      } as User;

      userService.updateUser.and.returnValue(
        of(updatedUser) as ReturnType<UserService['updateUser']>,
      );

      component.submit();

      expect(userService.updateUser).toHaveBeenCalledOnceWith({
        email: 'updated@example.com',
        username: 'Updated_User',
        password: 'StrongPassword123!',
      });
      expect(component.isLoadingForm).toBeFalse();
      expect(component.errorMessage).toBe('');
    });

    it('should not update the user when the form is invalid', () => {
      component['userForm'].controls.email.setValue('invalid-email');
      component['userForm'].controls.username.setValue('');
      component['userForm'].controls.password.setValue('');

      component.submit();

      expect(userService.updateUser).not.toHaveBeenCalled();
      expect(component.isLoadingForm).toBeFalse();
      expect(component['userForm'].controls.email.touched).toBeTrue();
      expect(component['userForm'].controls.username.touched).toBeTrue();
      expect(component['userForm'].controls.password.touched).toBeTrue();
    });

    it('should not submit when the form is pending', () => {
      fillValidForm();

      spyOnProperty(component['userForm'], 'pending', 'get')
        .and.returnValue(true);

      component.submit();

      expect(userService.updateUser).not.toHaveBeenCalled();
      expect(component.isLoadingForm).toBeFalse();
    });

    it('should not submit while a previous submission is loading', () => {
      fillValidForm();
      component.isLoadingForm = true;

      component.submit();

      expect(userService.updateUser).not.toHaveBeenCalled();
      expect(component['userForm'].controls.email.touched).toBeTrue();
    });

    it('should clear the previous error before submitting', () => {
      fillValidForm();
      component.errorMessage = 'Ancienne erreur';

      component.submit();

      expect(component.errorMessage).toBe('');
    });

    it('should display the server error message when the update fails', () => {
      fillValidForm();

      userService.updateUser.and.returnValue(
        throwError(() => ({
          error: { message: 'Adresse e-mail déjà utilisée' },
        })),
      );

      component.submit();

      expect(component.errorMessage).toBe(
        'Adresse e-mail déjà utilisée',
      );
      expect(component.isLoadingForm).toBeFalse();
    });

    it('should display a default error when the server gives no message', () => {
      fillValidForm();

      userService.updateUser.and.returnValue(
        throwError(() => ({})),
      );

      component.submit();

      expect(component.errorMessage).toBe(
        "Une erreur est survenue lors de l'inscription.",
      );
      expect(component.isLoadingForm).toBeFalse();
    });
  });

  describe('unsubscribeToTopic', () => {
    it('should remove the topic after successful unsubscription', () => {
      const angularTopic = createTopic('angular');
      const springTopic = createTopic('spring-boot');

      component['subscribedTopicList'].set([
        angularTopic,
        springTopic,
      ]);

      topicService.unsubscribe.and.returnValue(of(void 0));

      component['unsubscribeToTopic'](angularTopic);

      expect(topicService.unsubscribe).toHaveBeenCalledOnceWith('angular');
      expect(component['subscribedTopicList']()).toEqual([springTopic]);
      expect(component.topicErrorMessage).toBe('');
    });

    it('should remove the topic immediately while the request is pending', () => {
      const angularTopic = createTopic('angular');

      topicService.unsubscribe.and.returnValue(
        new (class extends (Object as { new (): object }) {})() as never,
      );

      component['subscribedTopicList'].set([angularTopic]);

      component['unsubscribeToTopic'](angularTopic);

      expect(component['subscribedTopicList']()).toEqual([]);
    });

    it('should display an error when unsubscription fails', () => {
      const angularTopic = createTopic('angular');

      component['subscribedTopicList'].set([angularTopic]);

      topicService.unsubscribe.and.returnValue(
        throwError(() => new Error('Request failed')),
      );

      component['unsubscribeToTopic'](angularTopic);

      expect(component.topicErrorMessage).toBe(
        'Impossible de se désabonner au topic, veuillez rééssayer',
      );
    });
  });

  describe('loadMore', () => {
    it('should load the next page when more topics are available', () => {
      const firstPage = [
        createTopic('angular'),
        createTopic('spring-boot'),
      ];
      const secondPage = [createTopic('java')];

      topicService.getSubscribed.and.returnValues(
        of(createPage(firstPage, 0, false)) as ReturnType<
          TopicService['getSubscribed']
        >,
        of(createPage(secondPage, 1, true)) as ReturnType<
          TopicService['getSubscribed']
        >,
      );

      component['loadMoreSubscribed'](0);
      component['loadMore']();

      expect(topicService.getSubscribed).toHaveBeenCalledTimes(2);
      expect(topicService.getSubscribed).toHaveBeenCalledWith(1, 10);
      expect(component['subscribedTopicList']()).toEqual([
        ...firstPage,
        ...secondPage,
      ]);
      expect(component['hasMore']()).toBeFalse();
    });

    it('should not load another page when there are no more topics', () => {
      component['hasMore'].set(false);

      topicService.getSubscribed.calls.reset();

      component['loadMore']();

      expect(topicService.getSubscribed).not.toHaveBeenCalled();
    });

    it('should not load another page while a request is in progress', () => {
      component['isLoadingTopics'].set(true);

      topicService.getSubscribed.calls.reset();

      component['loadMore']();

      expect(topicService.getSubscribed).not.toHaveBeenCalled();
    });

    it('should append topics instead of replacing existing topics', () => {
      const existingTopic = createTopic('angular');
      const newTopic = createTopic('typescript');

      component['subscribedTopicList'].set([existingTopic]);

      topicService.getSubscribed.and.returnValue(
        of(createPage([newTopic], 1, true)) as ReturnType<
          TopicService['getSubscribed']
        >,
      );

      component['loadMoreSubscribed'](1);

      expect(component['subscribedTopicList']()).toEqual([
        existingTopic,
        newTopic,
      ]);
    });

    it('should display an error when loading topics fails', () => {
      topicService.getSubscribed.and.returnValue(
        throwError(() => new Error('Request failed')),
      );

      component['loadMoreSubscribed'](0);

      expect(component.topicErrorMessage).toBe(
        'Impossible de se désabonner au topic',
      );
      expect(component['isLoadingTopics']()).toBeFalse();
    });
  });
});