package com.openclassrooms.mddapi.cli;

import com.openclassrooms.mddapi.seeder.UserSeeder;
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

    @Option(
            names = "--clear",
            description = "Clear the database before seeding"
    )
    private boolean clear;

    @Option(
            names = "--users",
            description = "Seed users"
    )
    private boolean users;

    @Option(
            names = "--all",
            description = "Seed all"
    )
    private boolean all;

    private final UserSeeder userSeeder;

    @Override
    public void run() {

        log.info("Seed command start");

        if (users) {
            userSeeder.seed(clear);
        }

        if (all) {
            seedAll(clear);
        }

        log.info("Seed command finished");
    }

    private void seedAll(Boolean clear) {
        userSeeder.seed(clear);
    }
}
