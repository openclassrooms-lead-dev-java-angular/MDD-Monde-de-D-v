import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '@env/environment';
import { ArticleComment } from '@model/article-comment.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CommentService {
  private readonly pathService = `${environment.apiUrl}/${environment.apiRoute.comment}`;

  private httpClient = inject(HttpClient);

  public postComment(
    articleSlug: string,
    comment: string,
  ): Observable<ArticleComment> {
    return this.httpClient.post<ArticleComment>(
      `${this.pathService}/${articleSlug}/comments`,
      {
        content: comment,
      },
    );
  }
}
