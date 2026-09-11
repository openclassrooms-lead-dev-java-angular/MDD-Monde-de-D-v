package com.openclassrooms.mddapi.cli;

import com.openclassrooms.mddapi.seeder.UserSeeder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Log4j2
@Component
@Command(
        name = "seed",
        description = "Generate application seed data"
)
@RequiredArgsConstructor
public class SeedCommand implements ApplicationRunner {

    private final UserSeeder userSeeder;

    @Override
    public void run(ApplicationArguments args) {

        if (!args.containsOption("seed")) {
            return;
        }

        log.info("Seed command start");

        if (args.containsOption("seed-users")) {
            userSeeder.seed();
        }

        if (args.containsOption("seed-all")) {
            seedAll();
        }

        log.info("Seed command finished");
    }

    private void seedAll() {
        userSeeder.seed();
    }
}
