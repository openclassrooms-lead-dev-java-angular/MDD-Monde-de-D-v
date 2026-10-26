import { Component, inject, OnInit, signal } from '@angular/core';
import { Topic } from '@model/topic.model';
import { TopicService } from '@service/topic.service';
import { InfiniteScrollDirective } from 'ngx-infinite-scroll';
import { TopicCardComponent } from 'src/app/shared/components/topic-card/topic-card.component';

@Component({
  selector: 'app-topic',
  standalone: true,
  imports: [TopicCardComponent, InfiniteScrollDirective],
  templateUrl: './topic.component.html',
  styleUrl: './topic.component.scss',
})
export class TopicComponent implements OnInit {
  private readonly topicService = inject(TopicService);

  protected readonly topicList = signal<Topic[]>([]);

  protected readonly loading = signal(false);
  protected readonly hasMore = signal(true);
  private currentPage = 0;
  private readonly pageSize = 10;

  protected readonly topicErrorMessage = signal<string>('');

  ngOnInit(): void {
    this.loadMore();
  }

  protected loadMore(): void {
    if (this.loading() || !this.hasMore()) {
      return;
    }

    this.loadPage(this.currentPage);
  }

  private loadPage(pageNumber: number): void {
    this.loading.set(true);

    this.topicService.fetchTopics(pageNumber, this.pageSize).subscribe({
      next: (page) => {
        this.topicList.update((topics) => [...topics, ...page.content]);
        this.currentPage = page.number + 1;
        this.hasMore.set(!page.last);
        this.loading.set(false);
      },

      error: (error) => {
        console.error(error);
        this.loading.set(false);
      },
    });
  }

  protected subscribeToTopic(topic: Topic): void {
    this.topicService.subscribe(topic.publicId).subscribe({
      next: () => {
        this.topicList.update((topics) =>
          topics.map((t) =>
            t.publicId === topic.publicId ? { ...t, subscribed: true } : t,
          ),
        );
        topic.subscribed = true;
      },
      error: (error) => {
        console.error(error);
        this.topicErrorMessage.set('Impossible de s’abonner au topic');
      },
    });
  }
}
