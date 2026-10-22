import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '@env/environment.prod';
import { Page } from '@type/page.type';
import { Topic, TopicOption } from '@model/topic.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class TopicService {
  private readonly pathService = `${environment.apiUrl}/${environment.apiRoute.topic}`;

  private httpClient = inject(HttpClient);

  public getTopics(page: number, size: number): Observable<Page<Topic>> {
    return this.httpClient.get<Page<Topic>>(`${this.pathService}`, {
      params: {
        page,
        size,
      },
    });
  }

  public getTopicList(): Observable<TopicOption[]> {
    return this.httpClient.get<TopicOption[]>(`${this.pathService}/options`);
  }

  public subscribe(slug: string): Observable<void> {
    return this.httpClient.post<void>(
      `${this.pathService}/${slug}/subscribe`,
      {},
    );
  }

  public unsubscribe(slug: string): Observable<void> {
    return this.httpClient.delete<void>(
      `${this.pathService}/${slug}/subscribe`,
    );
  }

  public getSubscribed(page: number, size: number): Observable<Page<Topic>> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.httpClient.get<Page<Topic>>(
      `${this.pathService}/subscribed`,
      { params },
    );
  }
}
