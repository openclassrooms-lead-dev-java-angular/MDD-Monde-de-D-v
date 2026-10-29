import { Component, inject, input, OnInit, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';
import { ArticleNew } from '@model/article.model';
import { TopicOption } from '@model/topic.model';
import { ArticleService } from '@service/article.service';
import { TopicService } from '@service/topic.service';
import {
  finalize,
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
    topicPublicId: [null as string | null, Validators.required],

    title: ['', [Validators.required, Validators.maxLength(150)]],

    content: ['', [Validators.required, Validators.maxLength(10000)]],
  });

  ngOnInit(): void {
    this.isLoading.set(true);

    this.topicService
      .fetchTopicOptions()
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
    const control = this.articleForm.controls.topicPublicId;

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

    const { topicPublicId, title, content } = this.articleForm.getRawValue();

    if (!topicPublicId) {
      this.articleForm.controls.topicPublicId.markAsTouched();
      return;
    }

    const article: ArticleNew = {
      topicPublicId,
      title,
      content,
    };

    this.isLoading.set(true);

    this.articleService
      .postArticle(article)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (createdArticle) => {
          void this.router.navigate(['/articles', createdArticle.publicId]);
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
}
