package com.openclassrooms.mddapi.cli;

import com.openclassrooms.mddapi.cli.seeder.ArticleSeeder;
import com.openclassrooms.mddapi.cli.seeder.CommentSeeder;
import com.openclassrooms.mddapi.cli.seeder.TopicSeeder;
import com.openclassrooms.mddapi.cli.seeder.UserSeeder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Log4j2
@Component
@Command(
        name = "seed",
        description = "Generate application seed data"
)
@RequiredArgsConstructor
public class SeedCommand implements Runnable {

    @Option(names = "--users", description = "Seed users")
    private boolean users;

    @Option(names = "--topics", description = "Seed topics")
    private boolean topics;

    @Option(names = "--articles", description = "Seed articles")
    private boolean articles;

    @Option(names = "--comments", description = "Seed comments")
    private boolean comments;

    @Option(names = "--all", description = "Seed all")
    private boolean all;

    private final UserSeeder userSeeder;
    private final TopicSeeder topicSeeder;
    private final ArticleSeeder articleSeeder;
    private final CommentSeeder commentSeeder;

    @Override
    public void run() {
        log.info("Seed command start");

        if (all) {
            seedAll();
        } else {
            if (users) {
                userSeeder.clear();
                userSeeder.seed();
            }

            if (topics) {
                topicSeeder.clear();
                topicSeeder.seed();
            }

            if (articles) {
                articleSeeder.clear();
                articleSeeder.seed();
            }

            if (comments) {
                commentSeeder.clear();
                commentSeeder.seed();
            }
        }

        log.info("Seed command finished");
    }

    private void seedAll() {
        commentSeeder.clear();
        articleSeeder.clear();
        userSeeder.clear();
        topicSeeder.clear();

        userSeeder.seed();
        topicSeeder.seed();
        articleSeeder.seed();
        commentSeeder.seed();
    }
}
