export interface Topic {
  slug: string;
  name: string;
  description: string;
  subscribed: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface TopicOption {
  slug: string;
  name: string;
}