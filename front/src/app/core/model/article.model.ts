import { ArticleComment } from './article-comment.model';
import { Topic } from './topic.model';

export interface Article {
  username: string;
  topic: Topic;
  publicId: string;
  title: string;
  content: string;
  createdAt: Date;
  updatedAt: Date;
  comments?: ArticleComment[];
}

export interface ArticleNew {
  topicPublicId: string;
  title: string;
  content: string;
}
