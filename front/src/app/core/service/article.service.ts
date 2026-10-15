import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '@env/environment.prod';
import { Article, ArticleNew } from '@model/article.model';
import { Page } from '@model/page.model';
import { SortDirection } from '../types/sort-direction.type';
import { Observable } from 'rxjs';
import { AvailableResponse } from '@model/available-response.model';

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  private readonly pathService = `${environment.apiUrl}/${environment.apiRoute.article}`;

  private httpClient = inject(HttpClient);

  public getArticleBySlug(slug: string): Observable<Article> {
    return this.httpClient.get<Article>(`${this.pathService}/${slug}`);
  }

  public getArticlesFeed(
    page: number,
    size: number,
    direction: SortDirection,
  ): Observable<Page<Article>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', `createdAt,${direction}`);

    return this.httpClient.get<Page<Article>>(`${this.pathService}/feed`, {
      params,
    });
  }

  public postArticle(article: ArticleNew): Observable<Article> {
    return this.httpClient.post<Article>(`${this.pathService}`, article);
  }

  public availableSlug(slug: String): Observable<AvailableResponse> {
    return this.httpClient.get<AvailableResponse>(
      `${this.pathService}/available-slug/${slug}`,
    );
  }
}
