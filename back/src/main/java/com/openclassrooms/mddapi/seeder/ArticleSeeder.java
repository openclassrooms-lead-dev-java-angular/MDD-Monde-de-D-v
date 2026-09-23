package com.openclassrooms.mddapi.seeder;

import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.article.repository.ArticleRepository;
import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.topic.repository.TopicRepository;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
public class ArticleSeeder extends AbstractSeeder<Article> {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;

    public ArticleSeeder(
            ArticleRepository articleRepository,
            UserRepository userRepository,
            TopicRepository topicRepository
    ) {
        super(articleRepository);
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        log.info("ArticleSeeder repository: {}", articleRepository);
    }

    @Override
    protected List<Article> getEntities() {
        List<User> users = userRepository.findAll();
        List<Topic> topics = topicRepository.findAll();

        if (users.isEmpty()) {
            throw new IllegalStateException("No users found.");
        }

        if (topics.isEmpty()) {
            throw new IllegalStateException("No topics found.");
        }

        return List.of(
                createArticle(
                        users.getFirst(),
                        topics.getFirst(),
                        "Comprendre les fondamentaux de Java",
                        "comprendre-les-fondamentaux-de-java"
                ),
                createArticle(
                        users.get(1 % users.size()),
                        topics.get(1 % topics.size()),
                        "Débuter avec Spring Boot",
                        "debuter-avec-spring-boot"
                ),
                createArticle(
                        users.get(2 % users.size()),
                        topics.get(2 % topics.size()),
                        "Construire une API REST avec Spring",
                        "construire-une-api-rest-avec-spring"
                ),
                createArticle(
                        users.get(3 % users.size()),
                        topics.get(3 % topics.size()),
                        "Les bonnes pratiques du développement Java",
                        "les-bonnes-pratiques-du-developpement-java"
                ),
                createArticle(
                        users.get(4 % users.size()),
                        topics.get(4 % topics.size()),
                        "Découvrir TypeScript pour les développeurs JavaScript",
                        "decouvrir-typescript-pour-les-developpeurs-javascript"
                ),
                createArticle(
                        users.getFirst(),
                        topics.getFirst(),
                        "Comprendre les relations avec JPA",
                        "comprendre-les-relations-avec-jpa"
                ),
                createArticle(
                        users.get(1 % users.size()),
                        topics.get(1 % topics.size()),
                        "Créer une architecture propre avec Spring Boot",
                        "creer-une-architecture-propre-avec-spring-boot"
                ),
                createArticle(
                        users.get(2 % users.size()),
                        topics.get(2 % topics.size()),
                        "Introduction aux bases de données relationnelles",
                        "introduction-aux-bases-de-donnees-relationnelles"
                ),
                createArticle(
                        users.get(3 % users.size()),
                        topics.get(3 % topics.size()),
                        "Pourquoi utiliser Git dans ses projets",
                        "pourquoi-utiliser-git-dans-ses-projets"
                ),
                createArticle(
                        users.get(4 % users.size()),
                        topics.get(4 % topics.size()),
                        "Organiser efficacement son code",
                        "organiser-efficacement-son-code"
                )
        );
    }

    private Article createArticle(User author, Topic topic, String title, String slug) {
        return Article
                .builder()
                .author(author)
                .topic(topic)
                .title(title)
                .slug(slug)
                .content(""" 
                                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed
                                 do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                                  Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris 
                                nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit 
                                in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat 
                                cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. 
                        """)
                .media(null)
                .build();
    }
}
