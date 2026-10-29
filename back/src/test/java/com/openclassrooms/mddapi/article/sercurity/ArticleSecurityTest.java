package com.openclassrooms.mddapi.article.sercurity;

import com.openclassrooms.mddapi.article.exception.ArticleNotFoundException;
import com.openclassrooms.mddapi.article.repository.ArticleRepository;
import com.openclassrooms.mddapi.article.security.ArticleSecurity;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.subscription.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleSecurityTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleSecurity articleSecurity;

    @BeforeEach
    void setUp() {
        when(userDetailsServiceImpl.getPrincipalUserId()).thenReturn(42L);
    }

    @Test
    void canAccessArticle_shouldReturnTrueWhenUserIsSubscribed() {
        String publicId = "article-public-id";
        Long userId = 42L;
        Long topicId = 10L;

        when(articleRepository.findTopicIdByPublicId(publicId))
                .thenReturn(Optional.of(topicId));
        when(subscriptionService.existsByUserIdAndTopicId(userId, topicId))
                .thenReturn(true);

        boolean result = articleSecurity.canAccessArticle(publicId);

        assertTrue(result);
        verify(articleRepository).findTopicIdByPublicId(publicId);
        verify(subscriptionService).existsByUserIdAndTopicId(userId, topicId);
    }

    @Test
    void canAccessArticle_shouldReturnFalseWhenUserIsNotSubscribed() {
        String publicId = "article-public-id";
        Long userId = 42L;
        Long topicId = 10L;

        when(articleRepository.findTopicIdByPublicId(publicId))
                .thenReturn(Optional.of(topicId));
        when(subscriptionService.existsByUserIdAndTopicId(userId, topicId))
                .thenReturn(false);

        boolean result = articleSecurity.canAccessArticle(publicId);

        assertFalse(result);
        verify(articleRepository).findTopicIdByPublicId(publicId);
        verify(subscriptionService).existsByUserIdAndTopicId(userId, topicId);
    }

    @Test
    void canAccessArticle_shouldThrowArticleNotFoundExceptionWhenArticleDoesNotExist() {
        String publicId = "unknown-article";

        when(articleRepository.findTopicIdByPublicId(publicId))
                .thenReturn(Optional.empty());

        assertThrows(
                ArticleNotFoundException.class,
                () -> articleSecurity.canAccessArticle(publicId)
        );

        verify(articleRepository).findTopicIdByPublicId(publicId);
        verifyNoInteractions(subscriptionService);
    }
}