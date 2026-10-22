import { Component, inject, OnInit, signal } from '@angular/core';
import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Topic } from '@model/topic.model';
import { User } from '@model/user.model';
import { AuthService } from '@service/auth.service';
import { TopicService } from '@service/topic.service';
import { UserStateService } from '@service/user-state.service';
import { UserService } from '@service/user.service';
import { ValidationErrorService } from '@service/validation.service';
import { ButtonComponent } from '@shared/components/button/button.component';
import { TopicCardComponent } from '@shared/components/topic-card/topic-card.component';
import { passwordValidator } from '@shared/validator/password.validator';
import { usernameAvailabilityValidator } from '@shared/validator/username-availbility.validator';
import { Page } from '@type/page.type';
import { InfiniteScrollDirective } from 'ngx-infinite-scroll';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-profile',
  imports: [
    ReactiveFormsModule,
    MatInputModule,
    MatProgressSpinnerModule,
    ButtonComponent,
    TopicCardComponent,
    InfiniteScrollDirective,
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent implements OnInit {
  private readonly topicService = inject(TopicService);
  private readonly userService = inject(UserService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly validationErrorService = inject(ValidationErrorService);
  private readonly authService = inject(AuthService);
  private readonly userStateService = inject(UserStateService);

  protected readonly subscribedTopicList = signal<Topic[]>([]);

  private currentPage = 0;
  private readonly pageSize = 10;
  protected readonly hasMore = signal<boolean>(true);
  protected readonly isLoadingTopics = signal<boolean>(false);
  topicErrorMessage = '';

  protected readonly userForm = this.fb.group({
    email: this.fb.control('', [
      Validators.required,
      Validators.email,
      Validators.maxLength(50),
    ]),
    username: this.fb.control(
      '',
      [Validators.required, Validators.minLength(3), Validators.maxLength(20)],
      [usernameAvailabilityValidator(this.authService)],
    ),
    password: this.fb.control('', [Validators.required, passwordValidator()]),
  });

  isLoadingForm = signal<boolean>(false);
  errorMessage = signal<string>('');

  ngOnInit() {
    const user: User = this.userStateService.getUser()!;

    this.userForm.controls.email.setValue(user.email);
    this.userForm.controls.username.setValue(user.username);

    if (this.isLoadingTopics() || !this.hasMore()) {
      return;
    }

    this.loadMoreSubscribed(this.currentPage);
  }

  get isUsernameLoading(): boolean {
    return this.userForm.controls.username.pending;
  }

  get emailErrors(): string[] {
    return this.validationErrorService.getErrors(
      this.userForm.controls.email,
      'email',
    );
  }

  get usernameErrors(): string[] {
    return this.validationErrorService.getErrors(
      this.userForm.controls.username,
      'username',
    );
  }

  get passwordErrors(): string[] {
    return this.validationErrorService.getErrors(
      this.userForm.controls.password,
      'password',
    );
  }

  submit(): void {
    this.errorMessage.set('');

    if (
      this.userForm.invalid ||
      this.userForm.pending ||
      this.isLoadingForm()
    ) {
      this.userForm.markAllAsTouched();
      return;
    }

    this.isLoadingForm.set(true);

    const { email, username, password } = this.userForm.getRawValue();

    this.userService
      .updateUser({ email, username, password })
      .pipe(
        finalize(() => {
          this.isLoadingForm.set(false);
        }),
      )
      .subscribe({
        next: (res: User) => {
          console.log(res);
        },
        error: (error: { error?: { message?: string } }) => {
          console.error('Erreur lors de l’inscription :', error);

          this.errorMessage.set(
            "Une erreur est survenue lors de l'inscription.",
          );
        },
      });
  }

  protected unsubscribeToTopic(topic: Topic): void {
    console.log(topic);
    this.subscribedTopicList.update((topics: Topic[]) =>
      topics.filter((t: Topic) => t.slug !== topic.slug),
    );
    console.log(this.subscribedTopicList());
    this.topicService.unsubscribe(topic.slug).subscribe({
      next: () => {
        this.subscribedTopicList.update((topics: Topic[]) =>
          topics.filter((t: Topic) => t.slug !== topic.slug),
        );
      },
      error: (error) => {
        console.error(error);
        this.topicErrorMessage =
          'Impossible de se désabonner au topic, veuillez rééssayer';
      },
    });
  }

  protected loadMore() {
    if (this.isLoadingTopics() || !this.hasMore()) {
      return;
    }

    this.loadMoreSubscribed(this.currentPage);
  }

  private loadMoreSubscribed(pageNumber: number): void {
    this.isLoadingTopics.set(true);

    this.topicService.getSubscribed(pageNumber, this.pageSize).subscribe({
      next: (page: Page<Topic>) => {
        this.subscribedTopicList.update((topics) => [
          ...topics,
          ...page.content,
        ]);
        this.currentPage = page.number + 1;
        this.hasMore.set(!page.last);
        this.isLoadingTopics.set(false);
      },
      error: (error) => {
        console.error(error);
        this.isLoadingTopics.set(false);
        this.topicErrorMessage = 'Impossible de se désabonner au topic';
      },
    });
  }
}
