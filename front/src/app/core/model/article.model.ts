import { Topic } from './topic.model';

export interface Article {
  username: string;
  topic: Topic;
  slug: string;
  title: string;
  content: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface ArticleNew {
  topicSlug: string;
  title: string;
  slug: string;
  content: string;
}
