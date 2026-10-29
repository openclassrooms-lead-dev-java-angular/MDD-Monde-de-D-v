export interface Topic {
  publicId: string;
  name: string;
  description: string;
  subscribed: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface TopicOption {
  publicId: string;
  name: string;
}
