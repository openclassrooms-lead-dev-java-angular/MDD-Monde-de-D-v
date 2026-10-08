import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '@env/environment.prod';
import { Page } from '@model/page.model';
import { Topic } from '@model/topic.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class TopicService {
  private readonly pathService = `${environment.apiUrl}/${environment.apiRoute.topic}`;

  private httpClient = inject(HttpClient);

  public getTopics(page: number, size: number): Observable<Page<Topic>> {
    return this.httpClient.get<Page<Topic>>(
      `${this.pathService}`, {
      params: {
        page,
        size,
      },
    });
  }
}
