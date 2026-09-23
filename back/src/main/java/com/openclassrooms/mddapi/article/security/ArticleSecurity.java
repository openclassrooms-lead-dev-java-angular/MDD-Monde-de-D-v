package com.openclassrooms.mddapi.article.security;

import com.openclassrooms.mddapi.article.repository.ArticleRepository;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("articleSecurity")
@RequiredArgsConstructor
public class ArticleSecurity {

    private final ArticleRepository articleRepository;
    private final UserService userService;

    public boolean isAuthor(final String slug) {
        User currentUser = userService.loadCurrentUserAuthor();

        return articleRepository.existsBySlugAndAuthor(
                slug,
                currentUser
        );
    }
}
