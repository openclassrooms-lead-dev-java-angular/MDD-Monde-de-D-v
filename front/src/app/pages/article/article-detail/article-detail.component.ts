import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Article } from '@model/article.model';
import { ArticleService } from '@service/article.service';

@Component({
  selector: 'app-article-detail',
  imports: [],
  templateUrl: './article-detail.component.html',
  styleUrl: './article-detail.component.scss',
})
export class ArticleDetailComponent implements OnInit {
  private readonly articleService = inject(ArticleService);
  private readonly route = inject(ActivatedRoute);

  protected readonly article = signal<Article | null>(null);

  protected readonly loading = signal(false);

  ngOnInit(): void {
    const slug: string = this.route.snapshot.paramMap.get('slug')!;

    this.loading.set(true);

    this.articleService.getArticleBySlug(slug).subscribe({
      next: (article) => {
        this.article.set(article);
        this.loading.set(false);
      },

      error: (error) => {
        console.error(error);
        this.loading.set(false);
      },
    });
  }
}
