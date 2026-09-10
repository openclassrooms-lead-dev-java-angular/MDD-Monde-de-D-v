CREATE DATABASE IF NOT EXISTS mdd
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;


CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    role VARCHAR(10) NOT NULL,
    last_login_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT uk_user_email
        UNIQUE (email),
    CONSTRAINT uk_user_username
        UNIQUE (username)
);

CREATE TABLE articles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    author_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    content TEXT,
    media VARCHAR(255),
    status VARCHAR(15) NOT NULL ,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_article_slug
        UNIQUE (slug)
);

CREATE TABLE topics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(90) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_topic_slug
        UNIQUE (slug)
);

CREATE TABLE subscriptions (
    user_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_subscription
        PRIMARY KEY (user_id, topic_id)
);

CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE articles
    ADD CONSTRAINT fk_article_user
    FOREIGN  KEY (author_id)
    REFERENCES users(id);

ALTER TABLE articles
    ADD CONSTRAINT fk_article_topic
    FOREIGN  KEY (topic_id)
    REFERENCES topics(id);

ALTER TABLE subscriptions
    ADD CONSTRAINT fk_subscription_user
    FOREIGN  KEY (user_id)
    REFERENCES users(id);

ALTER TABLE subscriptions
    ADD CONSTRAINT fk_subscription_topic
    FOREIGN  KEY (topic_id)
    REFERENCES topics(id);

ALTER TABLE comments
    ADD CONSTRAINT fk_comment_article
    FOREIGN  KEY (article_id)
    REFERENCES articles(id);

ALTER TABLE comments
    ADD CONSTRAINT fk_comment_user
    FOREIGN  KEY (author_id)
    REFERENCES users(id);
