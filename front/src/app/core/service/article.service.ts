import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '@env/environment.prod';
import { Article } from '@model/article.model';
import { Page } from '@model/page.model';
import { SortDirection } from '../types/sort-direction.type';

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  private readonly pathService = `${environment.apiUrl}/${environment.apiRoute.article}`;

  private httpClient = inject(HttpClient);

  public getArticleBySlug(slug: string) {
    return this.httpClient.get<Article>(`${this.pathService}/${slug}`);
  }

  public getArticlesFeed(page: number, size: number, direction: SortDirection) {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', `createdAt,${direction}`);

    return this.httpClient.get<Page<Article>>(`${this.pathService}/feed`, {
      params,
    });
  }
}
