package com.openclassrooms.mddapi.seeder;

import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.article.repository.ArticleRepository;
import com.openclassrooms.mddapi.comment.entity.Comment;
import com.openclassrooms.mddapi.comment.repository.CommentRepository;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Log4j2
@Component
public class CommentSeeder extends AbstractSeeder<Comment> {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    private static final int COMMENTS_PER_ARTICLE = 20;

    private static final List<String> COMMENTS = List.of(
            "Lorem ipsum dolor sit amet.",
            "Lorem ipsum dolor sit amet, consectetur.",
            "Très intéressant, merci pour le partage.",
            "Merci pour cet article.",
            "Article très intéressant.");

    public CommentSeeder(CommentRepository commentRepository, ArticleRepository articleRepository, UserRepository userRepository) {
        super(commentRepository);
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected List<Comment> getEntities() {
        List<Article> articles = articleRepository.findAll();
        List<User> users = userRepository.findAll();
        if (articles.isEmpty()) {
            throw new IllegalStateException("Cannot seed comments: no articles found");
        }
        if (users.isEmpty()) {
            throw new IllegalStateException("Cannot seed comments: no users found");
        }
        List<Comment> comments = new ArrayList<>();
        for (Article article : articles) {
            for (int i = 0; i < COMMENTS_PER_ARTICLE; i++) {
                User randomUser = users.get(random.nextInt(users.size()));
                String randomContent = COMMENTS.get(random.nextInt(COMMENTS.size()));
                comments.add(new Comment(null, article, randomUser, randomContent, null));
            }
        }
        log.info("Generated {} comments for {} articles", comments.size(), articles.size());
        return comments;
    }
}
