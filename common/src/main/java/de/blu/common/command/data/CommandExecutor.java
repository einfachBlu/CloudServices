package de.blu.common.command.data;

import com.google.inject.Inject;
import de.blu.common.command.ConsoleInputReader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CommandExecutor {

    @Inject
    private ConsoleInputReader consoleInputReader;

    /**
     * Set this to false if you would like, that the Command is set to
     * Done after the execute Method is done.
     * This is to allow additional Questions with a simple TextBased Answer
     */
    protected boolean autoDone = true;

    /**
     * Attribute to define, if the Command is still running
     */
    protected boolean done = true;

    /**
     * This Method will be called when the Command will be executed
     *
     * @param label the label which was used for the Command
     * @param args  the arguments from the command line
     */
    public abstract void execute(String label, String[] args);
}
