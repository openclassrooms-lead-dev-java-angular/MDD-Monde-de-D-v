import { Component, inject, input, OnInit, signal } from '@angular/core';
import {
  AbstractControl,
  AsyncValidatorFn,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';
import { ArticleNew } from '@model/article.model';
import { AvailableResponse } from '@model/available-response.model';
import { TopicOption } from '@model/topic.model';
import { ArticleService } from '@service/article.service';
import { TopicService } from '@service/topic.service';
import {
  catchError,
  finalize,
  map,
  Observable,
  of,
  switchMap,
  timer,
} from 'rxjs';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-article-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatIconModule,
    ButtonComponent,
    MatProgressSpinnerModule,
  ],
  templateUrl: './article-form.component.html',
  styleUrl: './article-form.component.scss',
})
export class ArticleFormComponent implements OnInit {
  private readonly articleService = inject(ArticleService);
  private readonly topicService = inject(TopicService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly topics = signal<TopicOption[]>([]);
  readonly isLoading = signal(false);
  readonly submitting = input(false);
  readonly errorMessage = signal<String>('');

  protected readonly articleForm = this.fb.nonNullable.group({
    topicSlug: [null as string | null, Validators.required],

    title: ['', [Validators.required, Validators.maxLength(150)]],

    slug: [
      '',
      {
        validators: [
          Validators.required,
          Validators.maxLength(150),
          Validators.pattern(/^[A-Za-z0-9_-]+$/),
        ],
        asyncValidators: [this.slugUniqueValidator()],
      },
    ],

    content: ['', [Validators.required, Validators.maxLength(10000)]],
  });

  ngOnInit(): void {
    this.isLoading.set(true);

    this.topicService
      .getTopicList()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (topics) => this.topics.set(topics),
        error: (error) => {
          console.error('Erreur lors du chargement des thèmes :', error);
        },
      });
  }

  protected get titleErrors(): string[] {
    const control = this.articleForm.controls.title;

    if (!control.touched && !control.dirty) {
      return [];
    }

    if (control.hasError('required')) {
      return ['Le titre est obligatoire.'];
    }

    if (control.hasError('maxlength')) {
      return ['Le titre ne doit pas dépasser 150 caractères.'];
    }

    return [];
  }

  protected get slugErrors(): string[] {
    const control = this.articleForm.controls.slug;

    if (!control.touched && !control.dirty) {
      return [];
    }

    if (control.hasError('required')) {
      return ['L’adresse est obligatoire.'];
    }

    if (control.hasError('maxlength')) {
      return ['L’adresse ne doit pas dépasser 150 caractères.'];
    }

    if (control.hasError('pattern')) {
      return [
        'Utilisez uniquement des lettres, chiffres, tirets et underscores.',
      ];
    }

    if (control.hasError('slugTaken')) {
      return ['Cette adresse est déjà utilisée.'];
    }

    if (control.hasError('slugCheckFailed')) {
      return ['Impossible de vérifier la disponibilité.'];
    }

    return [];
  }

  protected get contentErrors(): string[] {
    const control = this.articleForm.controls.content;

    if (!control.touched && !control.dirty) {
      return [];
    }

    if (control.hasError('required')) {
      return ['Le contenu est obligatoire.'];
    }

    if (control.hasError('maxlength')) {
      return ['Le contenu ne doit pas dépasser 10 000 caractères.'];
    }

    return [];
  }

  protected get topicErrors(): string[] {
    const control = this.articleForm.controls.topicSlug;

    if (control.touched && control.hasError('required')) {
      return ['Veuillez sélectionner un thème.'];
    }

    return [];
  }

  protected submitForm(): void {
    this.errorMessage.set('');

    if (
      this.articleForm.invalid ||
      this.articleForm.pending ||
      this.submitting() ||
      this.isLoading()
    ) {
      this.articleForm.markAllAsTouched();
      return;
    }

    const { topicSlug, title, slug, content } = this.articleForm.getRawValue();

    if (!topicSlug) {
      this.articleForm.controls.topicSlug.markAsTouched();
      return;
    }

    const article: ArticleNew = {
      topicSlug,
      title,
      slug,
      content,
    };

    this.isLoading.set(true);

    this.articleService
      .postArticle(article)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (createdArticle) => {
          void this.router.navigate(['/articles', createdArticle.slug]);
        },
        error: (error) => {
          console.error('Erreur lors de la création de l’article :', error);

          this.errorMessage.set(
            error.error?.message ??
              'Une erreur est survenue lors de la création de l’article.',
          );
        },
      });
  }

  protected slugUniqueValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      const slug = (control.value as string | null)?.trim();

      if (!slug) {
        return of(null);
      }

      return timer(300).pipe(
        switchMap(() => this.articleService.availableSlug(slug)),
        map((res: AvailableResponse): ValidationErrors | null =>
          res.available ? null : { slugTaken: true },
        ),
        catchError((error) => {
          console.error('Erreur de vérification du slug :', error);

          return of<ValidationErrors>({
            slugCheckFailed: true,
          });
        }),
      );
    };
  }

  protected autocompleteSlug(): void {
    const slugControl = this.articleForm.controls.slug;

    if (slugControl.value.length) {
      return;
    }

    const title = this.articleForm.controls.title.value;

    const slug = title
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .replace(/\s+/g, '-')
      .replace(/[^a-z0-9_-]/g, '')
      .replace(/_+/g, '_')
      .replace(/^[-_]+|[-_]+$/g, '')
      .slice(0, 150);

    slugControl.setValue(slug);
  }

  protected revalidateSlug(): void {
    this.articleForm.controls.slug.updateValueAndValidity();
  }
}
