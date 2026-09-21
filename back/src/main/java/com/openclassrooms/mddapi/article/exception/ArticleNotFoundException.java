package com.openclassrooms.mddapi.article.exception;

public class ArticleNotFoundException extends RuntimeException {
  public ArticleNotFoundException(String message) {
    super(message);
  }
}
