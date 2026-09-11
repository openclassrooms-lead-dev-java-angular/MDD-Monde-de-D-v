package com.openclassrooms.mddapi.cli;

import com.openclassrooms.mddapi.MddApiApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import picocli.CommandLine.Command;
import picocli.CommandLine;

@Command(
        name = "mdd",
        description = "MDD application CLI"
)
public class MddCli implements Runnable {

    public static void main(String[] args) {
        var context = new SpringApplicationBuilder(MddApiApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);

        CommandLine commandLine = new CommandLine(new MddCli());

        commandLine.addSubcommand(
                context.getBean(SeedCommand.class)
        );

        int exitCode = commandLine.execute(args);

        context.close();
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
