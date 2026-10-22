import { DatePipe, Location } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute } from '@angular/router';

import { Article } from '@model/article.model';
import { ArticleService } from '@service/article.service';
import { CommentService } from '@service/comment.service';
import { CommentComponent } from '@shared/components/comment/comment.component';

@Component({
  selector: 'app-article-detail',
  imports: [CommentComponent, ReactiveFormsModule, MatInputModule, DatePipe],
  templateUrl: './article-detail.component.html',
  styleUrl: './article-detail.component.scss',
})
export class ArticleDetailComponent implements OnInit {
  private readonly articleService = inject(ArticleService);
  private readonly commentService = inject(CommentService);
  private readonly route = inject(ActivatedRoute);
  private readonly location = inject(Location);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly commentForm = this.fb.group({
    comment: this.fb.control('', [Validators.required]),
  });

  protected readonly article = signal<Article | null>(null);

  protected readonly isLoading = signal(false);
  protected readonly isLoadingForm = signal(false);

  ngOnInit(): void {
    const slug: string = this.route.snapshot.paramMap.get('slug')!;

    this.isLoading.set(true);

    this.articleService.getArticleBySlug(slug).subscribe({
      next: (article) => {
        this.article.set(article);
        this.isLoading.set(false);
        console.log(article);
      },
      error: (error) => {
        console.error(error);
        this.isLoading.set(false);
      },
    });
  }

  protected submitForm() {
    if (
      this.commentForm.invalid ||
      this.commentForm.pending ||
      this.isLoadingForm()
    ) {
      return;
    }

    this.isLoadingForm.set(true);

    const { comment } = this.commentForm.getRawValue();

    this.commentService.postComment(this.article()!.slug, comment).subscribe({
      next: (newComment) => {
        this.isLoadingForm.set(false);
        this.commentForm.reset();

        this.article.update((article) => {
          if (!article) return article;

          return {
            ...article,
            comments: [...(article.comments ?? []), newComment],
          };
        });
      },
      error: (error) => {
        console.error(error);
        this.isLoadingForm.set(false);
      },
    });
  }

  goBack(): void {
    this.location.back();
  }
}
