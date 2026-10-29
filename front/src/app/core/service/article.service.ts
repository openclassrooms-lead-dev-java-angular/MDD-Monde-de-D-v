import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '@env/environment.prod';
import { Article, ArticleNew } from '@model/article.model';
import { Page } from '@type/page.type';
import { Observable } from 'rxjs';
import { SortDirection } from '@type/sort-direction.type';

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  private readonly pathService = `${environment.apiUrl}/${environment.apiRoute.article}`;

  private httpClient = inject(HttpClient);

  public fetchArticleByPublicId(publicId: string): Observable<Article> {
    return this.httpClient.get<Article>(`${this.pathService}/${publicId}`);
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
}
