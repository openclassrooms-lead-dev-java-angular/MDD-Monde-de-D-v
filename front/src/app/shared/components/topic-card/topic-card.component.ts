import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';
import { Topic } from '@model/topic.model';

@Component({
  selector: 'app-topic-card',
  standalone: true,
  imports: [],
  templateUrl: './topic-card.component.html',
  styleUrl: './topic-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopicCardComponent {
  readonly topic = input.required<Topic>();

  readonly showUnsubscribe = input(false);

  readonly subscribe = output<Topic>();
  readonly unsubscribe = output<Topic>();
}
