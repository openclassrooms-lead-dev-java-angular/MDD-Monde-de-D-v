package com.openclassrooms.mddapi.article.exception;

public class ArticleSlugAlreadyExists extends RuntimeException {
    public ArticleSlugAlreadyExists(String message) {
        super(message);
    }
}
