package com.openclassrooms.mddapi.cli;


import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import picocli.CommandLine.Command;
import picocli.CommandLine;

@Command(
        name = "mdd",
        description = "MDD application CLI",
        subcommands = {
                SeedCommand.class
        }
)
public class MddCli implements Runnable {

    public static void main(String[] args) {

        var context = new SpringApplicationBuilder(
                com.openclassrooms.mddapi.MddApiApplication.class
        )
                .web(WebApplicationType.NONE)
                .run(args);

        int exitCode = new CommandLine(new MddCli())
                .execute(args);

        context.close();

        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
