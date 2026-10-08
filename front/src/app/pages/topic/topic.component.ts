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

  private currentPage = 0;

  private readonly pageSize = 10;

  protected readonly hasMore = signal(true);

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

    this.topicService.getTopics(pageNumber, this.pageSize).subscribe({
      next: (page) => {
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
    console.log('Subscribe to:', topic.slug);
  }
}
