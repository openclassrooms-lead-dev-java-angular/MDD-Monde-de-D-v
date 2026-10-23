package com.openclassrooms.mddapi.article.security;

import com.openclassrooms.mddapi.article.exception.ArticleNotFoundException;
import com.openclassrooms.mddapi.article.repository.ArticleRepository;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("articleSecurity")
@RequiredArgsConstructor
public class ArticleSecurity {

    private final SubscriptionService subscriptionService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final ArticleRepository articleRepository;

    /**
     * Determines whether the currently authenticated user can access an article
     * based on their subscription to the article's topic.
     *
     * @param publicId the public id identifying the article
     * @return {@code true} if the current user is subscribed to the article's topic,
     *         {@code false} otherwise
     * @throws ArticleNotFoundException if no article is found for the specified public id
     */
    public boolean canAccessArticle(final String publicId) {
        Long currentUserId = userDetailsServiceImpl.getPrincipalUserId();

        Long topicId = articleRepository
                .findTopicIdByPublicId(publicId)
                .orElseThrow(() -> new ArticleNotFoundException(publicId));

        return subscriptionService.existsByUserIdAndTopicId(currentUserId, topicId);
    }
}
