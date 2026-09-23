package com.openclassrooms.mddapi.article.sercurity;

import com.openclassrooms.mddapi.article.repository.ArticleRepository;
import com.openclassrooms.mddapi.article.security.ArticleSecurity;
import com.openclassrooms.mddapi.factory.UserTestFactory;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleSecurityTest {
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private ArticleSecurity articleSecurity;

    @Test
    void shouldReturnTrueWhenCurrentUserIsArticleAuthor() {
        User currentUser = UserTestFactory.createUser();

        when(userService.loadCurrentUserAuthor())
                .thenReturn(currentUser);

        when(articleRepository.existsBySlugAndAuthor(
                "my-article",
                currentUser
        )).thenReturn(true);

        assertThat(articleSecurity.isAuthor("my-article"))
                .isTrue();
    }

    @Test
    void shouldReturnFalseWhenCurrentUserIsNotArticleAuthor() {
        User currentUser = UserTestFactory.createUser();

        when(userService.loadCurrentUserAuthor())
                .thenReturn(currentUser);

        when(articleRepository.existsBySlugAndAuthor(
                "my-article",
                currentUser
        ))
                .thenReturn(false);

        assertThat(articleSecurity.isAuthor("my-article"))
                .isFalse();
    }
}