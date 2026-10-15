import { Component, inject, OnInit, signal } from '@angular/core';
import { Article } from '@model/article.model';
import { ArticleService } from '@service/article.service';
import { ArticleCardComponent } from '@shared/components/article-card/article-card.component';
import { ButtonComponent } from '@shared/components/button/button.component';
import { SortDirection } from '@type/sort-direction.type';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [ButtonComponent, ArticleCardComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss',
})
export class FeedComponent implements OnInit {
  private readonly articleService = inject(ArticleService);

  protected readonly articleList = signal<Article[]>([]);
  protected readonly isLoading = signal(false);
  private currentPage = 0;

  private readonly pageSize = 10;

  protected readonly hasMore = signal(true);
  protected sortDirection = signal<SortDirection>('desc');

  ngOnInit(): void {
    this.loadMore();
  }

  protected loadMore(): void {
    if (this.isLoading() || !this.hasMore()) {
      return;
    }

    this.loadPage(this.currentPage);
  }

  private loadPage(pageNumber: number): void {
    this.isLoading.set(true);

    this.articleService
      .getArticlesFeed(pageNumber, this.pageSize, this.sortDirection())
      .subscribe({
        next: (page) => {
          console.log(page.content);
          this.articleList.update((articles) => [...articles, ...page.content]);
          this.currentPage = page.number + 1;
          this.hasMore.set(!page.last);

          this.isLoading.set(false);
        },

        error: (error) => {
          console.error(error);
          this.isLoading.set(false);
        },
      });
  }

  toggleSort(): void {}
}
