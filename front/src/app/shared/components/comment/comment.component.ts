import { Component, input } from '@angular/core';
import { ArticleComment } from '@model/article-comment.model';

@Component({
  selector: 'app-comment',
  imports: [],
  templateUrl: './comment.component.html',
  styleUrl: './comment.component.scss',
})
export class CommentComponent {
  readonly comment = input.required<ArticleComment>();
}
