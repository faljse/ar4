package info.faljse;

import picocli.CommandLine;

public class Main  {
    public static void main(String... args) {
        int exitCode = new CommandLine(new MainCmd())
                .setExecutionStrategy(new CommandLine.RunLast())
                .execute(args);
        System.exit(exitCode);
    }
}