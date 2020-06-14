package cc.whohow.fs.command;

import cc.whohow.fs.FileCommand;
import cc.whohow.fs.FileCommandBuilder;

import java.util.Optional;

public class BestMatchCommandBuilder<C extends FileCommand<?>> implements FileCommandBuilder<C> {
    protected final FileCommandBuilder<? extends C> commandBuilder1;
    protected final FileCommandBuilder<? extends C> commandBuilder2;

    public BestMatchCommandBuilder(FileCommandBuilder<? extends C> commandBuilder1,
                                   FileCommandBuilder<? extends C> commandBuilder2) {
        this.commandBuilder1 = commandBuilder1;
        this.commandBuilder2 = commandBuilder2;
    }

    @Override
    public Optional<? extends C> newCommand(String... arguments) {
        Optional<? extends C> command1 = commandBuilder1.newCommand(arguments);
        if (command1.isPresent()) {
            if (command1.get().getMatchingScore() == FileCommand.MatchingScore.HIGH) {
                // Best Match
                return command1;
            } else {
                Optional<? extends C> command2 = commandBuilder2.newCommand(arguments);
                if (command2.isPresent()) {
                    if (command1.get().getMatchingScore() < command2.get().getMatchingScore()) {
                        // Better Match
                        return command2;
                    } else {
                        return command1;
                    }
                } else {
                    return command1;
                }
            }
        } else {
            return commandBuilder2.newCommand(arguments);
        }
    }
}
