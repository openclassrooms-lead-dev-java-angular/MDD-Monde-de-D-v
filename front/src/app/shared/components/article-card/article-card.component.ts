import { DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Article } from '@model/article.model';
import { TruncatePipe } from 'src/app/core/pipe/truncate.pipe';

@Component({
  selector: 'app-article-card',
  standalone: true,
  imports: [
    RouterLink, 
    TruncatePipe, 
    DatePipe
  ],
  templateUrl: './article-card.component.html',
  styleUrl: './article-card.component.scss',
})
export class ArticleCardComponent {
  readonly article = input.required<Article>();
}
